<!-- PreRoomView.vue -->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import streamingService from '@/services/streamingService'
import openviduService from '@/services/openviduService'
import { useAccountStore } from '@/stores/account'

const { create } = streamingService
const router = useRouter()

const {userInfo} = useAccountStore()
const title = ref('')
const lat = ref(null);
const lon = ref(null);
const getCurrentLocation = () => {
  return new Promise((resolve) => {
    if (!navigator.geolocation) {
      console.log('브라우저 지원 X');
      return resolve({
        lat: 37.1234,
        lng: 127.5678
      });
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        console.log(' 현재 위치:', lat, lng);
        resolve({ lat, lng });
      },
      (err) => {
        console.log('API 실패:', err.message);
        resolve({
          lat: 37.1234,
          lng: 127.5678
        });
      }
    );
  });
};
const createRoom = async () => {
  try {
    const payload = {
      title: title.value,
      latitude: location.lat,
      longitude: location.lng,
    };

    const response = await create(payload)
    const sessionId = response.data.data
    console.log('res', response.data.data)
    await openviduService().connect

    router.push({
      name: 'RoomView',
      params: {
        id: sessionId,
      },
      query: {title:title.value, userName : userInfo?.name || 'Host'}      //RoomView로 방 제목을 전달해주기 위해 쿼리에 포함시킴
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
