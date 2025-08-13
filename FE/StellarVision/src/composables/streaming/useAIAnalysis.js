// src/composables/streaming/useAIAnalysis.js


// AI 분석(로컬/구독자), 주기 실행, 오버레이 클리어, 정리까지 모두 캡슐화
export function useAIAnalysis({ createAIAnalyzeService, endpoint, isPublish, sessionId, aiTagStore, renderer }) {
  // 로컬 분석기 (퍼블리셔/뷰어 공용)
  const localAnalyzer = createAIAnalyzeService({
    endpoint,
    minConfidence: 0.7,
    targetFps: 3,
    renderer,
  })

  // 구독자별 분석기/캔버스
  const subAnalyzers = new Map() // streamId -> { svc, canvas }

  // 내부 주기 실행 제어
  const INTERVAL_MS = 10 * 1000
  let intervalId = null
  let inFlight = false

  /** 로컬 비디오/오버레이 붙이기 (loadedmetadata 보장) */
  function attachLocal(videoEl, overlayEl) {
    if (!videoEl) return
    const attach = () => {
      localAnalyzer.attach({ video: videoEl, overlay: overlayEl }) // 내부 그리기끄기 옵션 있으면 활용
    }
    if (videoEl.readyState >= 1 && videoEl.videoWidth) attach()
    else videoEl.addEventListener('loadedmetadata', attach, { once: true })
  }

  /**
   * 구독자 비디오에 OpenVidu attach 한 뒤, 오버레이 캔버스를 올리고
   * 개별 분석기를 붙이는 헬퍼 생성기
   * @param {(sub, el) => void} baseAttachSubEl - 기존 attachSubEl을 그대로 호출하는 래퍼
   */
  function attachSubElWithAIFactory(baseAttachSubEl) {
    return function attachSubElWithAI(sub, el) {
      baseAttachSubEl(sub, el)
      if (!el) return
      const streamId = sub.stream.streamId
      const start = () => {
        el.parentElement?.classList.add('relative')
        const canvas = document.createElement('canvas')
        canvas.className = 'pointer-events-none absolute inset-0'
        el.parentElement?.appendChild(canvas)

        const svc = createAIAnalyzeService({
          endpoint,
          minConfidence: 0.7,
          targetFps: 3,
          renderer,
        })
        svc.attach({ video: el, overlay: canvas })
        subAnalyzers.set(streamId, { svc, canvas })
      }
      if (el.readyState >= 1 && el.videoWidth) start()
      else el.addEventListener('loadedmetadata', start, { once: true })
    }
  }

  /** (디버깅용) 예쁘게 로깅 */
    function prettyLog(prefix, data) {
        try {
        const preds = data?.predictions ?? []
        console.group(prefix)
        console.log('총 개수:', preds.length)
        for (const p of preds) {
            const { class: klass, confidence, bbox } = p
            console.log(
            `• ${klass}  conf=${Number(confidence).toFixed(3)}  bbox=[${bbox?.map(n => Number(n).toFixed(2)).join(', ')}]`
            )
        }
        if (!preds.length) console.log('(빈 결과)')
        console.groupEnd()
        } catch (e) {
        console.log(prefix, data)
        }
    }

  /** aiOn이 켜져 있을 때 1회 실행 */
  async function runOnceAll(aiOnRef) {
    if (!aiOnRef.value || inFlight) return
    inFlight = true
    try {
      // 로컬
      const localRes = await localAnalyzer.once()
      prettyLog('[AI][local]', localRes)
      if (isPublish.value) {
        aiTagStore.addFromPredictions(sessionId, localRes)
      }

      // 구독자들
      for (const [streamId, { svc, video, canvas }] of subAnalyzers.entries()) {
        const subRes = await svc.once()
        prettyLog(`[AI][sub:${streamId}]`, subRes)
      }
    } finally {
      inFlight = false
    }
  }

  /**
   * aiOn 토글을 감시하여 interval 관리
   * @param {Ref<boolean>} aiOnRef
   * @param {() => void} onClearOverlays - 오버레이 지우는 콜백(부모에서 overlayLocal을 알고 있음)
   */
  function watchAI(aiOnRef, onClearOverlays) {
    if (aiOnRef.value) {
      runOnceAll(aiOnRef) // 즉시 1회
      if (!intervalId) {
        intervalId = window.setInterval(() => runOnceAll(aiOnRef), INTERVAL_MS)
      }
    } else {
      if (intervalId) { clearInterval(intervalId); intervalId = null }
      onClearOverlays?.()
    }
  }

  /** 부모의 overlayLocal을 이용해 모든 오버레이 지우기 */
  function clearAllOverlays(overlayLocal) {
    // 로컬
    const c = overlayLocal.value
    if (c) {
      const ctx = c.getContext('2d')
      if (ctx) ctx.clearRect(0, 0, c.width, c.height)
    }
    // 구독자
    for (const { canvas } of subAnalyzers.values()) {
      const ctx = canvas.getContext('2d')
      if (ctx) ctx.clearRect(0, 0, canvas.width, canvas.height)
    }
  }

  /** 세션의 streamDestroyed에서 호출해 구독자용 분석기/캔버스를 정리 */
  function onStreamDestroyed(ev) {
    const streamId = ev.stream.streamId
    const found = subAnalyzers.get(streamId)
    if (!found) return
    found.svc.destroy()
    found.canvas.remove()
    subAnalyzers.delete(streamId)
  }

  /** 전체 정리(onUnmounted 등) */
  function destroy() {
    if (intervalId) { clearInterval(intervalId); intervalId = null }
    localAnalyzer.destroy()
    subAnalyzers.forEach(({ svc, canvas }) => { svc.destroy(); canvas.remove() })
    subAnalyzers.clear()
  }

  return {
    // 객체/레퍼런스
    localAnalyzer,

    // attach helpers
    attachLocal,
    attachSubElWithAIFactory,

    // 토글/주기/정리
    runOnceAll,
    watchAI,
    clearAllOverlays,
    onStreamDestroyed,
    destroy,
  }
}
