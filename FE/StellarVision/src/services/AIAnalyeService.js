// src/services/AIAnalyzeService.js
import axios from 'axios'

/**
 * createAIAnalyzeService
 * - 비디오 프레임을 주기적으로 캡처해 AI 서버로 전송
 * - 응답 bbox를 오버레이 캔버스에 렌더링
 */
export function createAIAnalyzeService({
  endpoint,                 // 필수: 'https://ai.example.com/api/detect/streaming'
  minConfidence = 0.7,
  targetFps = 3,
  getAuthToken = null,      // 선택: () => 'Bearer ...'
  jpegQuality = 0.8,
  timeoutMs = 5000,
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

  function _drawOverlay(preds = []) {
    if (!overlayCtx || !overlayEl) return
    overlayCtx.clearRect(0, 0, overlayEl.width, overlayEl.height)

    const filtered = preds.filter(p => (p.confidence ?? 0) >= minConfidence)

    overlayCtx.lineWidth = 2
    overlayCtx.font = '14px sans-serif'

    for (const p of filtered) {
      const [x1, y1, x2, y2] = p.bbox
      const w = Math.max(0, x2 - x1)
      const h = Math.max(0, y2 - y1)

      overlayCtx.strokeStyle = 'lime'
      overlayCtx.strokeRect(x1, y1, w, h)

      const label = `${p.class} ${(p.confidence * 100).toFixed(1)}%`
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

          const headers = { }
          if (getAuthToken) headers.Authorization = getAuthToken()

          const { data } = await api.post(
            endpoint,
            form,
            { headers, timeout: timeoutMs, withCredentials: false }
          )
          _drawOverlay(data?.predictions ?? [])
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
     * - CSS는 같은 컨테이너에 비디오/캔버스 둘 다 absolute로 겹치고 object-contain 권장
     */
    attach({ video, overlay }) {
      videoEl = video
      overlayEl = overlay || null
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
    },

    setTargetFps(fps) {
      targetFps = fps
      if (timer) {
        this.stop()
        this.start()
      }
    },

    destroy() {
      this.stop()
      videoEl = null
      overlayEl = null
      overlayCtx = null
      offscreen = null
      offctx = null
      inFlight = false
    },
  }
}
