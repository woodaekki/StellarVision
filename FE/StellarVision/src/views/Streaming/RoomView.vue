<script setup>
import { ref, watch} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import openviduService from '@/services/openviduService'
import { useStreamingStore } from '@/stores/streaming'

const route = useRoute()
const router = useRouter()
// const streamId = route.params.id
// const userName = route.params.userName || 'Guest'
const store = useStreamingStore
const {roodId, userName} = store;


// composable 에서 모든 로직을 가져온다
const { subscribers, recording, toggleRec, leave } = openviduService(
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
watch(localVideo, el => {
  if (el) {
    // composable 안의 publisher.stream 가져와서 붙이기
  }
})




</script>

<template>
  <div>
    <h2>방 {{ streamId }} — {{ userName }}</h2>
    <div class="videos">
      <div v-for="sub in subscribers" :key="sub.stream.streamId">
        <video autoplay playsinline ref="el => sub.addVideoElement(el)"></video>
      </div>
      <video ref="localVideo" autoplay playsinline muted></video>
    </div>
    <button @click="toggleRec">
      {{ recording ? '녹화 중지' : '녹화 시작' }}
    </button>
    <button @click="leave">나가기</button>
  </div>
</template>
