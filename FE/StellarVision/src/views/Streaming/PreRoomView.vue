<!-- PreRoomView.vue -->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import streamingService from '@/services/streamingService'
import openviduService from '@/services/openviduService'

const { create } = streamingService
const router = useRouter()

const title = ref('')
const userName = ref('')

const createRoom = async () => {
  try {
    const payload = {
      title: title.value,
      latitude: 37.1234,        // 임의 값 설정
      longitude: 127.5678,
    };

    const response = await create(payload)
    const sessionId = response.data.data
    console.log('res', response.data.data)
    await openviduService().connect

    router.push({
      name: 'RoomView',
      params: {
        id: sessionId,
        userName: userName.value || 'Guest'
      },
      query: {title:title.value}      //RoomView로 방 제목을 전달해주기 위해 쿼리에 포함시킴
    })
  } catch (err) {
    console.error(err)
    console.error('Error details:', err.response.data.error.details);

    alert('방 생성에 실패했습니다.')
  }
}


</script>

<template>
  <div class="pre-room">
    <h1>방 생성 / 참가</h1>

    <!-- 새 방 생성 -->
    <form @submit.prevent="createRoom">
      <input v-model="title" placeholder="새 방 제목" required />
      <input v-model="userName" placeholder="내 이름 (Optional)" />
      <button type="submit">방 생성</button>
    </form>

    <hr />

  </div>
</template>

<style scoped>
.pre-room {
  max-width: 400px;
  margin: 50px auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
input, button {
  padding: .5rem;
  width: 100%;
}
hr {
  margin: 1.5rem 0;
}
</style>
