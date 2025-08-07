// src/services/openviduService.js
import { ref, onMounted, onUnmounted } from 'vue'
import { OpenVidu } from 'openvidu-browser'
import streamingService from '@/services/streamingService'
import router from '@/router'
import { useAccountStore } from '@/stores/account'

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
      const response = await streamingService.getToken(streamId)
      const token = response.data.data.token   //data에서 token과 role을 받아옴
      const role = response.data.data.role
      const {userInfo} = useAccountStore()
      console.log('이름 전달', userInfo.name)
      console.log('token, role', token, role)
      await session.connect(token, { clientData: userInfo.name })
      if(role === 'PUBLISHER' ){
        publisher.value = OV.initPublisher(undefined, {     //undefiend 대신 태그ID나 DOM 지정하면 바로 스트림이 붙는다.
        audioSource: undefined,
        videoSource: undefined,
        publishAudio: true,
        publishVideo: true,
        resolution: '640x480',
        frameRate: 30
      })
        session.publish(publisher.value)
      }
    } catch (e) {
      onError(e)
      console.error('pulisher init error', e)
    }
  }

  const toggleRec = async () => {
    const action = recording.value ? 'stop' : 'start'
    await streamingService.toggleRecording(streamId, action)
    recording.value = !recording.value
  }

  const leave = async () => {
    session.disconnect()
    await streamingService.end(streamId).catch(() => {})
    alert('스트리밍을 종료합니다')
    router.push({name:'MainView'})
  }

  onMounted(connect)
  onUnmounted(leave)

  return { session, publisher, subscribers, recording, toggleRec, leave, connect }
}
