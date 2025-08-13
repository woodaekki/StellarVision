// src/composables/streaming/useOpenViduBindings.js
import { ref, watch } from 'vue'

export function useOpenViduBindings({ publisher, setPublisherEl, attachSubEl }) {
  const localVideo = ref(null)
  const overlayLocal = ref(null)

  // 스트리머용: ref 콜백
  const onSetPublisherEl = (el) => {
    localVideo.value = el
    if (el) {
      try { setPublisherEl(el) } catch {}
    }
  }
  // 시청자용: ref 콜백
  const onSetLocalVideoEl = (el) => { localVideo.value = el }
  // 오버레이 ref
  const onSetOverlayEl = (el) => { overlayLocal.value = el }

  // 퍼블리셔 비디오 addVideoElement 보강
  watch([publisher, localVideo], () => {
    if (publisher.value && localVideo.value) {
      try {
        publisher.value.addVideoElement(localVideo.value)
        localVideo.value.muted = true
        localVideo.value.setAttribute('playsinline','true')
        localVideo.value.setAttribute('autoplay','true')
        localVideo.value.play?.().catch(()=>{})
      } catch {}
    }
  })

  // 구독자 비디오 attach + 상대 컨테이너에 relative + overlay 캔버스는 외부에서 붙이도록 hook으로 분리 (AI에서 씀)
  function baseAttachSubEl(sub, el) {
    attachSubEl(sub, el)
    return el
  }

  return {
    localVideo, overlayLocal,
    onSetPublisherEl, onSetLocalVideoEl, onSetOverlayEl,
    baseAttachSubEl,
  }
}
