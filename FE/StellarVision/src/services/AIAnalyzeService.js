// src/services/AIAnalyzeService.js
import axios from 'axios'

/**
 * createAIAnalyzeService
 * - 비디오 프레임을 주기적으로 캡처해 AI 서버로 전송
 * - 응답 bbox를 오버레이 캔버스에 렌더링
 * - 최근 예측을 보관하고, class별 그룹으로 제공
 * - 선택된 class만 오버레이로 그릴 수 있는 필터 기능
 */
export function createAIAnalyzeService({
  endpoint,                 // 필수: 'https://ai.example.com/api/detect/streaming'
  minConfidence = 0.7,
  targetFps = 3,
  getAuthToken = null,      // 선택: () => 'Bearer ...'
  jpegQuality = 0.8,
  timeoutMs = 5000,
  renderer = null,
} = {}) {
  if (!endpoint) throw new Error('AI endpoint is required')

  // 내부 상태
  let videoEl = null
  let overlayEl = null
  let overlayCtx = null
  let offscreen = null
  let offctx = null
  let inFlight = false
  let timer = null

  // 최근 예측 & 선택된 클래스 코드(약어/라벨)
  let lastPredictions = []
  let selectedCode = null

  const api = axios.create()

  function _ensureCanvas(w, h) {
    if (!offscreen) {
      offscreen = document.createElement('canvas')
      offctx = offscreen.getContext('2d')
    }
    if (offscreen.width !== w || offscreen.height !== h) {
      offscreen.width = w
      offscreen.height = h
    }
    if (overlayEl && (!overlayCtx || overlayEl.width !== w || overlayEl.height !== h)) {
      overlayEl.width = w
      overlayEl.height = h
      overlayCtx = overlayEl.getContext('2d')
    }
  }

  // 기본 드로잉(필터 적용 전용)
  function _drawDefault(preds = []) {
    if (!overlayCtx || !overlayEl) return
    overlayCtx.clearRect(0, 0, overlayEl.width, overlayEl.height)

    overlayCtx.lineWidth = 2
    overlayCtx.font = '14px sans-serif'

    for (const p of preds) {
      const [x1, y1, x2, y2] = p.bbox || []
      if (x1 == null || y1 == null || x2 == null || y2 == null) continue
      const w = Math.max(0, x2 - x1)
      const h = Math.max(0, y2 - y1)

      overlayCtx.strokeStyle = 'lime'
      overlayCtx.strokeRect(x1, y1, w, h)

      const label = `${p.class} ${((p.confidence ?? 0) * 100).toFixed(1)}%`
      const pad = 4
      const tw = overlayCtx.measureText(label).width
      const tx = x1
      const ty = Math.max(12, y1 - 6)

      overlayCtx.fillStyle = 'rgba(0,0,0,0.6)'
      overlayCtx.fillRect(tx - pad, ty - 12, tw + pad * 2, 16)
      overlayCtx.fillStyle = 'white'
      overlayCtx.fillText(label, tx, ty)
    }
  }

  // 현재 선택/신뢰도 기준으로 필터링 후 그리기
  function _drawFiltered(preds = []) {
    if (!overlayEl || !overlayCtx) return
    // 항상 먼저 캔버스 비움
    overlayCtx.clearRect(0, 0, overlayEl.width, overlayEl.height)
    // ❗선택 전(null)에는 아무것도 그리지 않음 = “리스트에서 클릭해야만 그림”
    if (selectedCode == null) return

    const byConf = preds.filter(p => (p.confidence ?? 0) >= minConfidence)
    const final = byConf.filter(p => p.class === selectedCode)
    if (renderer) { renderer(final, overlayCtx, overlayEl, { minConfidence }) } else { _drawDefault(final) }
  }

  // class별 그룹핑
  function _groupPredictions(preds = []) {
    const groups = new Map() // code -> { code, items: [], count, maxConf }
    for (const p of preds) {
      const code = p?.class
      if (!code) continue
      const g = groups.get(code) || { code, items: [], count: 0, maxConf: 0 }
      g.items.push(p)
      g.count += 1
      g.maxConf = Math.max(g.maxConf, Number(p.confidence ?? 0))
      groups.set(code, g)
    }
    return Array.from(groups.values()).sort(
      (a, b) => b.maxConf - a.maxConf || b.count - a.count
    )
  }

  async function analyzeOnce() {
    if (!videoEl) return
    const vw = videoEl.videoWidth
    const vh = videoEl.videoHeight
    if (!vw || !vh) return

    _ensureCanvas(vw, vh)
    offctx.drawImage(videoEl, 0, 0, vw, vh)

    return new Promise((resolve) => {
      offscreen.toBlob(async (blob) => {
        if (!blob) return resolve(null)

        try {
          const form = new FormData()
          form.append('file', blob, 'frame.jpg')

          const headers = {}
          if (getAuthToken) headers.Authorization = getAuthToken()

          const { data } = await api.post(
            endpoint,
            form,
            { headers, timeout: timeoutMs, withCredentials: false }
          )

          // [CHANGED] 예측 결과 보관 + 필터렌더
          lastPredictions = data?.predictions ?? []
          _drawFiltered(lastPredictions)

          resolve(data)
        } catch (e) {
          // 실패해도 앱 끊기지 않게 조용히
          console.debug('[AIAnalyze] detect error', e)
          resolve(null)
        }
      }, 'image/jpeg', jpegQuality)
    })
  }

  async function _tick() {
    if (inFlight) return
    inFlight = true
    try {
      await analyzeOnce()
    } finally {
      inFlight = false
    }
  }

  // === Public API ===
  return {
    /**
     * 비디오/오버레이 엘리먼트 연결
     * - overlayEl은 null 가능(그냥 추론만 하고 싶을 때)
     */
    attach({ video, overlay }) {
      videoEl = video
      overlayEl = overlay || null
      // overlayCtx는 _ensureCanvas에서 크기 맞출 때 생성
    },

    start() {
      if (!videoEl) throw new Error('Call attach({ video, overlay }) first')
      if (timer) return
      const interval = Math.max(1, Math.floor(1000 / targetFps))
      timer = window.setInterval(_tick, interval)
    },

    stop() {
      if (timer) {
        clearInterval(timer)
        timer = null
      }
    },

    async once() {
      return analyzeOnce()
    },

    setMinConfidence(v) {
      minConfidence = v
      // 신뢰도 변경 시 즉시 재렌더
      _drawFiltered(lastPredictions)
    },

    setTargetFps(fps) {
      targetFps = fps
      if (timer) {
        this.stop()
        this.start()
      }
    },

    // 최근 결과 반환(필요 시 외부에서 직접 접근)
    getLastPredictions() {
      return Array.isArray(lastPredictions) ? lastPredictions.slice() : []
    },

    // class별 그룹 리스트 반환 (패널에 사용)
    getGroups() {
      return _groupPredictions(lastPredictions)
    },

    // 선택한 class만 오버레이로 그리기 (null이면 전체)
    select(code /* string|null */) {
      selectedCode = code || null
      _drawFiltered(lastPredictions)
    },

    // 오버레이 수동 초기화
    clearOverlay() {
      if (overlayCtx && overlayEl) overlayCtx.clearRect(0, 0, overlayEl.width, overlayEl.height)
    },

    destroy() {
      this.stop()
      videoEl = null
      overlayEl = null
      overlayCtx = null
      offscreen = null
      offctx = null
      inFlight = false

      // [ADDED] 상태도 정리
      lastPredictions = []
      selectedCode = null
    },
  }
}
