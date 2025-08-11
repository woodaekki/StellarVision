<template>
  <header v-if="!isRoomView" :class="['fixed top-0 left-0 w-full z-50 transition-colors duration-300', headerClasses]">
    <div class="header flex justify-between items-center px-8 py-2">
      <div class="flex items-center gap-2">
        <img :src="logo" alt="로고" width="30px" height="42px" />
        <RouterLink to="/" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-bold font-pretendard">StellaVision</RouterLink>
        <RouterLink to="/pre" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-pretendard"><Video class="video-icon"/></RouterLink>
      </div>
      <nav class="flex items-center gap-4">
        <template v-if="isLogin">
          <RouterLink :to="`/profile/${userInfo?.email}`" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-pretendard">
            {{ userInfo?.name }}님
          </RouterLink>
          <button @click="handleLogout" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-pretendard bg-transparent border-none text-white cursor-pointer inline-block">로그아웃</button>
        </template>
        <template v-else>
          <RouterLink to="/signup" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-pretendard">회원가입</RouterLink>
          <RouterLink to="/login" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-pretendard">로그인</RouterLink>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { Video } from 'lucide-vue-next';
import { RouterLink, useRoute } from 'vue-router'
import { computed } from 'vue'
import logo from "@/assets/pictures/stellabot/logo.png"
import { useAccountStore } from "@/stores/account.js"

const route = useRoute()
const account = useAccountStore()
const isLogin = computed(() => account.isLogin)
const userInfo = computed(() => account.userInfo)

const isRoomView = computed(() => route.name === 'RoomView')

// 라우트 이름에 따라 헤더 색상을 변경
const headerClasses = computed(() => {
  if (route.name === 'MainView') {
    return 'bg-transparent text-[#cfd8dc]';
  } else {
    return 'bg-black text-white';
  }
});

const handleLogout = () => {
  account.logOut()
}
</script>

<style>
.video-icon {
  color:#FF0000 ;
}
</style>
