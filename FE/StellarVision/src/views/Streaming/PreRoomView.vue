<!-- src/views/PreRoomView.vue -->
<template>
  <div class="pre-room">
    <h2>방송 준비</h2>
    <!-- 방 제목을 수정 가능하게 하려면 v-model로 바인딩 -->
    <label>방 제목</label>
    <input v-model="roomTitle" />

    <button @click="startStreaming" :disabled="!roomTitle">
      방송 시작
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import streamingService from '@/services/streamingService'

const route  = useRoute()
const router = useRouter()
const roomId    = route.params.id
const roomTitle = ref(route.query.title || '')  // query로 기존 제목 전달받아도 좋습니다

async function startStreaming() {
  try {
    // publisher 로 입장
    await streamingService.join(roomId, {
      role: 'publisher',
      userName: roomTitle.value  // userName에 방 제목을 넣을 수도 있고, 실제 이름을 받으셔도 되고
    })
    router.push({ name: 'RoomView', params: { id: roomId } })
  } catch (e) {
    console.error('방송 시작 실패', e)
    alert('방송 시작에 실패했습니다.')
  }
}
</script>

<style scoped>
.pre-room { max-width: 400px; margin: 2rem auto; }
input { width: 100%; padding: .5rem; margin-bottom: 1rem; }
button { width: 100%; padding: .75rem; }
</style>
