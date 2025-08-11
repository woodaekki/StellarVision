<!-- PreRoomView.vue -->
<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import streamingService from '@/services/streamingService'
import openviduService from '@/services/openviduService'
import { useAccountStore } from '@/stores/account'
import preroomImg from '@/assets/pictures/stellabot/preroom.png'

const { create } = streamingService
const router = useRouter()

const {userInfo} = useAccountStore()
const title = ref('')
const MAX_TITLE = 50  //제목 최대 제한
const titleInvalid = computed(() => title.value.length < 2 || title.value.length > MAX_TITLE)  // 제목 요건(추가예정)
const creating = ref(false)

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
    const location = await getCurrentLocation()
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

  <div class="min-h-[60vch] bg-zinc-50 flex items-center justify-center px-4 py-12">
    <section class="w-full max-w-5xl rounded-2xl overflow-hidden shadow-md ring-1 ring-black/5">
      <!-- 좌우 2열 나누기 -->
       <div class="grid grid-cols-1 md:grid-cols-2">
        <!-- 왼쪽 영역 -->
         <div class="relative bg-[#101A63]">
          <!-- 장식용 패턴(옵션) -->
            <div class="absolute inset-0 opacity-20 pointer-events-none"
               style="background: radial-gradient(60% 50% at 60% 40%, white, transparent);"></div>

            <!-- 일러스트 이미지 -->
            <img
              :src=preroomImg
              alt="방 일러스트"
              class="relative z-10 w-full h-full object-contain p-10 md:p-14"
            />
          </div>
          <!-- 오른쪽 영역 -->
        <div class="bg-white p-8 md:p-12 flex flex-col justify-center">
          <header class="mb-8">
            <h1 class="text-3xl font-semibold text-zinc-900">방 이름을 정해주세요!</h1>
          </header>

          <form @submit.prevent="createRoom" class="space-y-12">
            <div class="pt-4 py-8">
             <label for="title" class="block text-md font-medium text-zinc-800 pt-4">방 제목</label>
             <div class="pt-2 relative">
              <input
                id="title"
                v-model.trim="title"
                :maxlength="MAX_TITLE"
                placeholder="MidNight Orion"
                class="w-full h-12 rounded-xl bg-white shadow-sm ring-1 px-4
                        focus:outline-none focus:ring-2 transition placeholder:text-zinc-400"
                :class="titleInvalid ? 'ring-red-300 focus:ring-red-500' : 'ring-zinc-300 focus-blue-600'"
                autocomplete="off"
                required/>
             </div>
             <p v-if="titleInvalid" class="pt-2 text-xs text-red-500">제목은 2~50자 사이여야 합니다</p>
            </div>
            <button
              type="submit"
              :disabled="creating || titleInvalid"
              class="mt-8 h-12 w-fuull rounded-xl bg-blue-200 text-zinc-900 font-medium
                    hover:bg-blue-300 active:translate-y-[1px] transition focus:outline-none
                    focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:pointer-events-none"
              >
              {{  creating ? '생성 중...' : '계속하기' }}
            </button>
          </form>
         </div>
       </div>
    </section>
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
