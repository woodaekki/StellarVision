// src/services/openviduService.js
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { OpenVidu } from 'openvidu-browser'
import streamingService from '@/services/streamingService'
import router from '@/router'
import { useAccountStore } from '@/stores/account'

export default function openviduService(streamId, userName, onError = () => {}) {
  const OV = new OpenVidu()
  const session = OV.initSession()
  const subscribers = ref([])
  const publisher = ref(null)

  const role = ref('SUBSCRIBER')
  const isPublish = computed(() => role.value === 'PUBLISHER')

  // streamId -> DOM element 매핑 (원격용)
  const containerMap = new Map()
  // 퍼블리셔 프리뷰 붙일 대상
  let publisherTarget = null

  // 원격 스트림 생성 시
  session.on('streamCreated', e => {
    // 스트리머(호스트)는 원격 오디오 끔(에코 방지), 시청자는 켬
    const sub = session.subscribe(e.stream, undefined, {
      subscribeToAudio: !isPublish.value,
      subscribeToVideo: true,
      insertMode: 'APPEND',
    })
    subscribers.value.push(sub)

    // 이미 컨테이너가 준비돼 있으면 즉시 붙이기
    const el = containerMap.get(sub.stream.streamId)
    if (el) { 
      sub.addVideoElement(el)
      
      queueMicrotask(() => {
      const video = el.querySelector('video') || el
      if (video) {
        video.setAttribute('playsinline', 'true')
        video.setAttribute('autoplay', 'true')
        video.muted = true
        video.play().catch(() => {})
      }
    })
    }
  })

  // 원격 스트림 종료 시
  session.on('streamDestroyed', e => {
    subscribers.value = subscribers.value.filter(s => s.stream.streamId !== e.stream.streamId)
    containerMap.delete(e.stream.streamId)
  })

  // 퍼블리셔 프리뷰 붙일 DOM 전달
  function setPublisherEl(el) {
    publisherTarget = el
    if (publisher.value && el) {
      publisher.value.addVideoElement(el)
    }
  }

  // attachSubEl
  function attachSubEl(subOrStream, el) {
    const streamId = subOrStream?.stream?.streamId ?? subOrStream?.streamId
    if (!streamId || !el) return
    containerMap.set(streamId, el)

    const sub = subscribers.value.find(s => s.stream.streamId === streamId)
    if (!sub) return

    // ✅ video 엘리먼트에 직접 붙이고 autoplay 세팅
    try {
      el.setAttribute('playsinline', 'true')
      el.setAttribute('autoplay', 'true')
      // 스트리머는 원격 소리 끄기, 시청자는 켜기
      el.muted = !!isPublish.value
    } catch (_) {}

    sub.addVideoElement(el)

    // 재생 유도
    Promise.resolve().then(() => {
      el.play?.().catch(err => console.warn('Autoplay blocked (will need a click):', err))
    })
  }

  const connect = async () => {
    try {
      const res = await streamingService.getToken(streamId) // 백이 PUBLISHER/SUBSCRIBER 판별해서 내려준다고 가정
      const { token, role: roleFromServer } = res.data.data

      console.log('[OV] role =', role)

      role.value = roleFromServer

      const { userInfo } = useAccountStore()
      const name = userInfo?.name ?? userName ?? 'Guest'

      await session.connect(token, { clientData: name })

      if (isPublish.value) {  // PUBLISHER 이면 
        console.log('[OV] initPublisher...')
        publisher.value = OV.initPublisher(undefined, {
          audioSource: undefined,
          videoSource: undefined,
          publishAudio: true,
          publishVideo: true,
          resolution: '640x480',
          frameRate: 30,
          insertMode: 'APPEND',
          mirror: false,
        })
        // 퍼블리셔 프리뷰 DOM에 붙이기
        if (publisherTarget) {
          publisher.value.addVideoElement(publisherTarget)
        }
        await session.publish(publisher.value)
        console.log('[OV] publish done')
      }
    } catch (e) {
      console.error('connect/publish error', e)
      onError(e)
    }
  }

  const toggleRec = async () => {
    const action = recording.value ? 'stop' : 'start'
    await streamingService.toggleRecording(streamId, action)
    recording.value = !recording.value
  }


  // 시청자: 세션만 끊기
  const leave = async () => {
    try {
      session.disconnect()
      alert("스트리밍 방에서 퇴장합니다.")
    } finally {
      router.push({ name: 'MainView' })
    }
  }

  const endRoom = async () => {
    try {
      console.log('[END] try end session', { streamId, localConnected: !!session })

      // 서버에 방 종료 먼저 요청 (에러를 삼키지 말자)
      const res = await streamingService.end(streamId)
      console.log('[END] backend response', res.status, res.data)

      alert('스트리밍을 종료합니다')
    } catch (err) {
      // axios 에러 상세 출력
      const status = err?.response?.status
      const data = err?.response?.data
      console.error('[END] backend error', { status, data, err })

      // 백엔드 예외별 UX
      if (status === 403) {
        alert('호스트만 종료할 수 있습니다.')
      } else if (status === 404) {
        alert('이미 종료되었거나 세션을 찾을 수 없습니다.')
      } else if (status === 409 || data?.error?.code === 'RECORDING_IN_PROGRESS') {
        alert('녹화 중입니다. 녹화를 먼저 중지하세요.')
      } else {
        alert('방 종료 중 오류가 발생했습니다.')
      }
    } finally {
      // 2) 로컬 세션 정리 (서버가 이미 끊었어도 안전)
      try {
        console.log('[END] local disconnect start')
        session.disconnect()
        console.log('[END] local disconnect done')
      } catch (e) {
        console.warn('[END] local disconnect error', e)
      }
      router.push({ name: 'MainView' })
    }
  }

  onMounted(connect)
  onUnmounted(() => session.disconnect())

  return {
    session,
    publisher,
    subscribers,
    connect,
    leave,
    endRoom,
    setPublisherEl,   // 퍼블리셔 프리뷰 대상 전달
    attachSubEl,      // 구독 비디오 DOM 전달
    toggleRec,
    role,
    isPublish
  }
}
