<script setup>
import { ref, watch, watchEffect} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import openviduService from '@/services/openviduService'
import { useStreamingStore } from '@/stores/streaming'
import streamingService from '@/services/streamingService'

const route = useRoute()
const router = useRouter()
const streamId = route.params.id
const userName = route.params.userName || 'Host'
const isRecording = ref(false)

// const store = useStreamingStore
// const {roodId, userName} = store;


// 모든 로직을 가져온다
const { publisher, subscribers, recording, leave } = openviduService(
  streamId,
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

async function toggleRecording(action) {
  try {

    if (action === 'start') {
      await streamingService.startRecording(streamId)
      isRecording.value = true
    } else {
      await streamingService.stopRecording(streamId)
      isRecording.value = false
    }
  } catch (e) {
    console.error('녹화 토글 실패', e.response?.data || e)
  }
}

</script>

<template>
  <div>
    <h2>방제목 {{ streamId }} — {{ userName }}</h2>
    <div class="videos">


      <div style="width: 1080px; height: 720px; background: #000;">
          <video
            ref="localVideo"
            autoplay
            playsinline
            muted
            style="width:100%; height:100%; object-fit:fill;"
          ></video>
      </div>
    </div>

    <button @click="toggleRecording">
      {{ recording ? '녹화 중지' : '녹화 시작' }}
    </button>
    <button @click="leave">나가기</button>
  </div>
</template>
