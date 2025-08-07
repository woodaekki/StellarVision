<!-- RoomView.vue -->
<script setup>
import { onMounted, ref, watchEffect} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import openviduService from '@/services/openviduService'
import streamingService from '@/services/streamingService'
import { Camera, DoorOpen, ImageDown, MessageCircle, Mic, MicOff, Square, SquareStop } from 'lucide-vue-next'
import ChatPanel from '@/components/comment/ChatPanel.vue'
import { useRecordingStore } from '@/stores/recording'

const route = useRoute()
const router = useRouter()
//세션관련 객체
const sessionId = route.params.id
const userName = route.params.userName || 'Host'
const roomTitle = route.query.title
// 녹음 관련 객체
const isRecording = ref(false)
const recordingId = ref(null)
const isRecordingButtonDisabled = ref(false)
const recorded = ref('')
const recordingStore = useRecordingStore()
// 상태 관리
const showChat = ref(false)
const micEnabled = ref(true)



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
  <div class="pt-16">
    <div class="flex flex-col sm:flex-row w-full max-w-[1600px] mx-auto gap-2" alt="videos">
      <!-- 동영상 -->
      <div style="height: 640px; background: #000;"
        :class="['relative bg-black transition-all duration-300',
        showChat ? 'sm:w-[70%] w-full' :'w-full']">
          <video
            ref="localVideo"
            autoplay
            playsinline
            muted
            class="w-full h-full object-fill rounded-2xl"
          ></video>
        <!-- 방 제목 -->
        <h2 class="text-xl text-white font-bold my-2 absolute left-3 top-3 z-10">방제목 {{ roomTitle }} — {{ userName }}</h2>

        <!-- 음성 버튼-->
          <div class="absolute left-1/2 bottom-6 -translate-x-1/2 flex gap-4 z-10">
            <button
              @click="toggleMic"
              class=" text-white rounded-full p-4 hover:bg-green-600 shadow transition">
              <component :is="micEnabled ? Mic : MicOff"/>
            </button>
        <!-- 녹화 버튼 -->
            <button
              @click="toggleRecording"
              class=" bg-opacity-70 text-white rounded-full p-4 hover:bg-red-600 shadow transition">
            <component :is="isRecording ? Square : SquareStop" />
            </button>
          </div>
          <!-- 나가기 버튼 -->
          <button @click="leave"
          class="absolute right-3 top-3 z-10 bg-black bg-opacity-70
          text-white rounded-full px-3 py-1 hover:bg-red-600 transition">
            <DoorOpen/>
          </button>
          <!-- 캡처 버튼 -->
          <button
            class="absolute left-3 bottom-6 hover:bg-gray-600 transition shadow rounded-full text-white p-4">
              <ImageDown/>
          </button>
          <!-- 채팅 버튼 -->
           <button
            class="absolute right-3 bottom-6 hover:bg-gray-600 transition shadow rounded-full text-yellow p-4"
            @click="showChat = !showChat">
            <MessageCircle/>
           </button>
      </div>
      <transition name="fade">
        <div
          v-if="showChat"
          class="basis-0 grow sm:w-[30%] w-full min-w[480px] max-w-[600px]
          h-[640px] flex-shrink-0">
            <ChatPanel
              :session="session"
              :userName="userName"
              @close="showChat = false"
              class="h-full"
              />
        </div>

      </transition>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity .2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
