<!-- RoomView.vue -->
<script setup>
import { onMounted, ref, watchEffect} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import openviduService from '@/services/openviduService'
import streamingService from '@/services/streamingService'
import { Mic, MicOff, Square, SquareStop } from 'lucide-vue-next'
import ChatPanel from '@/components/comment/ChatPanel.vue'
import { useRecordingStore } from '@/stores/recording'

const route = useRoute()
const router = useRouter()
//세션관련 객체
const sessionId = route.params.id
const userName = route.params.userName || 'Host'
const roomTitle = route.query.title
const micEnabled = ref(true)
// 녹음 관련 객체
const isRecording = ref(false)
const recordingId = ref(null)
const isRecordingButtonDisabled = ref(false)
const recorded = ref('')
const recordingStore = useRecordingStore()



// 모든 로직을 가져온다
const { session, publisher,  leave } = openviduService(
  sessionId,
  userName,
  e => {
    alert('연결 실패')
    console.error(e)
    router.push({ name: 'PreRoomView' })
  }
)

// 로컬 비디오 엘리먼트에 퍼블리셔 붙이기
const localVideo = ref(null)
watchEffect(() => {
  if (publisher.value && localVideo.value) {
    publisher.value.addVideoElement(localVideo.value)
  }
})
// 녹화 시작/중단 api 요청
async function toggleRecording() {
  try {
    isRecordingButtonDisabled.value = true

    if (!isRecording.value) {
      isRecordingButtonDisabled.value = true
      const res = await streamingService.startRecording(sessionId)
      recordingId.value = res.data.data
      console.log(res.data)
      console.log('record', recordingId)
      isRecording.value = true

      alert('녹화를 시작합니다!')
    } else {
      if (!recordingId.value) {
        alert('녹화ID가 없습니다. 새로고침 후 시도하세요.')
        return
      }
      const res = await streamingService.stopRecording(recordingId.value)
      isRecording.value = false
      recordingId.value = null
      recorded.value = res.data.data.recordingUrl
      console.log('res: ', res.data.data)
      recordingStore.setRecordingInfo(res.data.data)
      alert('녹화를 중지합니다')
    }
  } catch (e) {

    if (e.response?.data?.error?.details === '409') {
      isRecording.value = true
      alert('이미 녹화가 진행 중입니다.')
    }

    alert(
      '녹화 오류: ' +
      (e.response?.data?.message || e.message) +
      (e.response?.data?.error?.details ? ` (code: ${e.response.data.error.code}, details: ${e.response.data.error.details})` : '')
    )
    console.error('녹화 토글 실패', e.response?.data || e)
  } finally {
    isRecordingButtonDisabled.value = false
  }
}

// 마이크 on/off 토글
function toggleMic(){
  if (!publisher) {
    return
  }
  micEnabled.value = !micEnabled.value
  publisher.publishAudio(micEnabled.value)  //openvidu mute 문서
}




onMounted(()=>{
  session.on('recordingStarted', ()=>{
    isRecording.value = true
  })
  session.on('recordingStopped', ()=>{
    isRecording.value = false
  })

})

</script>

<template>
  <div>
    <h2>방제목 {{ roomTitle }} — {{ userName }}</h2>
    <div class="videos">

      <div style="width: 720x; height: 480px; background: #000;">
          <video
            ref="localVideo"
            autoplay
            playsinline
            muted
            style="width:720x; height:100%; object-fit:fill;"
          ></video>
      </div>

    </div>
    <ChatPanel :session="session" :userName="userName"/>

    <div>
    <button
      @click="toggleMic"
      class="control-buttons__button control-buttons"
    >
      <component :is="micEnabled ? Mic : MicOff"/>

    </button>

    <button
      @click="toggleRecording"

      >
    <component :is="isRecording ? Square : SquareStop" />
    </button>
    <button @click="leave">나가기</button>
    </div>


  </div>
</template>
