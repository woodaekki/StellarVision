// /src/services/screenShareService.js
// OpenVidu 화면공유 토글 서비스
// 사용 예시(이미 RoomView에 반영됨):
// const screenShare = createScreenShareService(
//   { sessionRef: session, publisherRef: publisher },
//   { shareSystemAudio: true, keepMic: true, webcamProps: {}, screenProps: {} }
// )

import { ref, unref } from 'vue'

export function createScreenShareService(
  { sessionRef, publisherRef },
  {
    shareSystemAudio = false,
    keepMic = true,
    webcamProps = {},   // 평소 웹캠 퍼블리셔 init 시 쓰고 싶은 기본 옵션(미사용 시 비워둠)
    screenProps = {}    // 화면공유 퍼블리셔 init 시 추가 옵션
  } = {}
) {
  const isScreenSharing = ref(false)
  const isBusy = ref(false)
  

  // 내부 상태: 원래 퍼블리셔를 기억해놨다가 복귀
  let originalPublisher = null
  let screenPublisher = null

  function getSession() {
    const s = unref(sessionRef)
    if (!s || typeof s.publish !== 'function') {
      throw new Error('[ScreenShare] session is not ready')
    }
    return s
  }

  function getOVfromSession(session) {
    const OV = session?.openvidu
    if (!OV) throw new Error('[ScreenShare] session.openvidu not available')
    return OV
  }

  // 안전한 트랙 정리(실패해도 무시)
  function safeStopTracks(stream) {
    try {
      stream?.getTracks?.().forEach(t => {
        try { t.stop?.() } catch {}
      })
    } catch {}
  }

async function start() {
  if (isBusy.value) return
  isBusy.value = true
  try {
    const session = getSession()
    const pub = unref(publisherRef)
    if (!pub) throw new Error('[ScreenShare] local publisher not ready')

    // 로컬 퍼블리셔 가드
    const myId = session.connection?.connectionId
    const owner = pub.stream?.connection?.connectionId
    if (!myId || !owner || myId !== owner) {
      throw new Error('[ScreenShare] Given publisher is not local')
    }

    // 화면 + (선택) 시스템 오디오
    const ms = await navigator.mediaDevices.getDisplayMedia({
      video: true,
      audio: !!shareSystemAudio,
    })

    // 기존 트랙 백업
    const currentMs = pub.stream.getMediaStream()
    const oldVideoTrack = currentMs.getVideoTracks()[0] || null
    const oldAudioTrack = currentMs.getAudioTracks()[0] || null

    // 새 트랙
    const newVideoTrack = ms.getVideoTracks()[0]
    const newAudioTrack = ms.getAudioTracks()[0] || null

    // 공유창에서 "중지" 누르면 자동 복귀
    newVideoTrack.addEventListener('ended', () => stop().catch(() => {}))

    // 비디오 교체
    await pub.replaceTrack(newVideoTrack)

    // 오디오 정책
    if (shareSystemAudio && newAudioTrack) {
      await pub.replaceTrack(newAudioTrack) // 시스템 오디오로 교체
      pub.publishAudio(true)
    } else if (keepMic) {
      // 마이크 유지
      pub.publishAudio(true)
    } else {
      // 무음
      pub.publishAudio(false)
    }

    // 복귀용 핸들 저장
    screenPublisher = { ms, oldVideoTrack, oldAudioTrack }
    isScreenSharing.value = true
  } finally {
    isBusy.value = false
  }
}

async function stop() {
  if (isBusy.value) return
  isBusy.value = true
  try {
    const pub = unref(publisherRef)
    if (!pub) return

    const backup = screenPublisher || {}
    let camTrack = backup.oldVideoTrack
    let micTrack = backup.oldAudioTrack

    // 백업 트랙이 없거나 끝났으면 새로 획득
    const needNewCam = !camTrack || camTrack.readyState === 'ended'
    const needNewMic = keepMic && (!micTrack || micTrack.readyState === 'ended')

    if (needNewCam || needNewMic) {
      const gum = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: !!keepMic,
      })
      if (needNewCam) camTrack = gum.getVideoTracks()[0] || null
      if (keepMic && needNewMic) micTrack = gum.getAudioTracks()[0] || null
    }

    // 비디오 복원
    if (camTrack) await pub.replaceTrack(camTrack)

    // 오디오 복원
    if (keepMic) {
      if (micTrack) {
        await pub.replaceTrack(micTrack)
        pub.publishAudio(true)
      } else {
        pub.publishAudio(false)
      }
    } else {
      pub.publishAudio(false)
    }

    // 화면공유 스트림 정리
    try { backup.ms?.getTracks?.().forEach(t => t.stop?.()) } catch {}
    screenPublisher = null

    isScreenSharing.value = false

    // (선택) 재생 유도: 로컬 프리뷰가 검게 남는 브라우저 방지
    // RoomView에서 이미 addVideoElement를 하고 있어도 play가 막힐 수 있으므로 한 번 더 시도
    queueMicrotask(() => {
      const anyLocal = document.querySelector('video[playsinline][muted]')
      anyLocal?.play?.().catch(() => {})
    })
  } finally {
    isBusy.value = false
  }
}


  async function toggle() {
    if (isScreenSharing.value) return stop()
    return start()
  }

function destroy() {
  // 진행 중이면 중지
  if (isScreenSharing.value) {
    try {
      screenPublisher?.ms?.getTracks?.().forEach(t => t.stop?.())
    } catch {}
    screenPublisher = null
    isScreenSharing.value = false
  }
}

  return {
    isScreenSharing,
    isBusy,
    start,
    stop,
    toggle,
    destroy
  }
}
