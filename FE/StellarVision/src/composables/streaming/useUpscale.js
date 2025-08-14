// src/composables/streaming/useUpscale.js
import { ref, computed } from 'vue'

export function useUpscale({ upscaleService, startDownloadToast }) {
  const upscaledUrl = ref(null)
  const isDownloading = ref(false)
  const isUpscaling = ref(false)
  const hasUpscaled = computed(() => !!upscaledUrl.value)

  function buildFilenameFromUrl(url, fallback = 'upscaled.jpg') {
    try {
      const u = new URL(url)
      const name = u.pathname.split('/').pop()
      if (!name || !name.includes('.')) return fallback
      return name
    } catch {
      return fallback
    }
  }

  function resetUpscaled() {
    const url = upscaledUrl.value
    if (url?.startsWith('blob:')) URL.revokeObjectURL(url)
    upscaledUrl.value = null
  }

  async function captureVideoFrame(videoEl, type = 'image/jpeg', quality = 0.92) {
    if (!videoEl || !videoEl.videoWidth || !videoEl.videoHeight) {
      throw new Error('비디오 준비가 안 됨(loadedmetadata 이후 시도)')
    }
    const canvas = document.createElement('canvas')
    canvas.width = videoEl.videoWidth
    canvas.height = videoEl.videoHeight
    const ctx = canvas.getContext('2d', { willReadFrequently: false })
    ctx.drawImage(videoEl, 0, 0, canvas.width, canvas.height)

    return new Promise((resolve, reject) => {
      canvas.toBlob((blob) => {
        if (!blob) return reject(new Error('캡쳐 실패'))
        resolve(blob)
      }, type, quality)
    })
  }

  async function captureAndUpscale(pickTargetVideoEl, toast, intervalRef, visibleRef, progressRef) {
    if (isUpscaling.value) return
    try {
      startDownloadToast?.('이미지 업스케일 중...') // 진행 토스트
      isUpscaling.value = true
      if (upscaledUrl.value?.startsWith('blob:')) {
        URL.revokeObjectURL(upscaledUrl.value)
        upscaledUrl.value = null
      }

      const target = pickTargetVideoEl()
      if (!target) throw new Error('캡쳐할 비디오가 없습니다.')

      const captured = await captureVideoFrame(target, 'image/jpeg', 0.92)
      const upscaledBlob = await upscaleService.upscaleImage(captured, 'capture.jpg')

      const url = URL.createObjectURL(upscaledBlob)
      upscaledUrl.value = url
    } finally {
      isUpscaling.value = false
    }
  }

  async function downloadUpscaled(toast, intervalRef, visibleRef) {
    if (!upscaledUrl.value || isDownloading.value) return
    isDownloading.value = true
    try {
      const filename = buildFilenameFromUrl(upscaledUrl.value, `upscaled_${Date.now()}.jpg`)
      const res = await fetch(upscaledUrl.value, {})
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const blob = await res.blob()
      const objUrl = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = objUrl
      a.download = filename
      document.body.appendChild(a)
      a.click(); a.remove()
      URL.revokeObjectURL(objUrl)
      resetUpscaled()
    } catch (err) {
      // 실패 시 새 탭 오픈 + 토스트 에러
      const a = document.createElement('a')
      a.href = upscaledUrl.value
      a.target = '_blank'
      a.rel = 'noopener'
      document.body.appendChild(a); a.click(); a.remove()
      if (intervalRef.value) { clearInterval(intervalRef.value); intervalRef.value = null }
      toast.add({severity:'error', summary:'다운로드 실패', detail: err?.message ?? 'unknown', life:3000})
      toast.removeGroup('download')
      visibleRef.value = false
    } finally {
      isDownloading.value = false
    }
  }

  function makeOnCaptureOrDownload(pickTargetVideoEl, toast, intervalRef, visibleRef, progressRef) {
    return async () => {
      if (hasUpscaled.value) {
        await downloadUpscaled(toast, intervalRef, visibleRef)
      } else {
        await captureAndUpscale(pickTargetVideoEl, toast, intervalRef, visibleRef, progressRef)
      }
    }
  }

  return {
    upscaledUrl, isDownloading, isUpscaling, hasUpscaled,
    resetUpscaled, captureAndUpscale, downloadUpscaled, makeOnCaptureOrDownload,
  }
}
