// src/services/openviduService.js
import { ref, onMounted, onUnmounted } from 'vue'
import { OpenVidu } from 'openvidu-browser'
import streamingService from '@/services/streamingService'

export default function openviduService(streamId, userName, onError = () => {}) {
  const OV = new OpenVidu()
  const session = OV.initSession()
  const subscribers = ref([])
  const publisher = ref(null)
  const recording = ref(false)
  // OpenVidu 이벤트
  session.on('streamCreated', e => {
    const sub = session.subscribe(e.stream, undefined)
    subscribers.value.push(sub)
  })
  session.on('streamDestroyed', e => {
    subscribers.value = subscribers.value.filter(s => s.stream.streamId !== e.stream.streamId)
  })

  const connect = async () => {
    try {
      const response = await streamingService.getToken(streamId, userName)
      const token = response.data.data
      await session.connect(token, { clientData: userName })
      publisher.value = OV.initPublisher(undefined, {
        audioSource: undefined,
        videoSource: undefined,
        publishAudio: true,
        publishVideo: true,
        resolution: '640x480',
        frameRate: 30
      })
      session.publish(publisher.value)
    } catch (e) {
      onError(e)
    }
  }
// 참여자(SUBSCRIBER)로 접속
  async function connectAsSubscriber(sessionId) {
    const session = OV.initSession()
    const res = await streamingService.createToken(sessionId)
    const token = res.data.data
    await session.connect(token, { clientData: 'Guest' })
    session.on('streamCreated', ({ stream }) => {
      session.subscribe(stream)
    })
    return session
  }



  const toggleRec = async () => {
    const action = recording.value ? 'stop' : 'start'
    await streamingService.toggleRecording(streamId, action)
    recording.value = !recording.value
  }

  const leave = async () => {
    session.disconnect()
    await streamingService.end(streamId).catch(() => {})
  }

  onMounted(connect)
  onUnmounted(leave)

  return { session, publisher, subscribers, recording, toggleRec, leave, connectAsSubscriber }
}
