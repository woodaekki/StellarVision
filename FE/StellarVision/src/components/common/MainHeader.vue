<template>
  <header v-if="!isRoomView" :class="['fixed top-0 left-0 w-full z-50 transition-colors duration-300', headerClasses]">
    <div class="header flex justify-between items-center px-8 py-2">
      <div class="flex items-center gap-2">
        <img :src="logo" alt="로고" width="30px" height="42px" />
        <RouterLink to="/main" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-bold font-pretendard">StellaVision</RouterLink>
      </div>
      <nav class="flex items-center gap-2">
        <div class="icon-container gap-2">
          <RouterLink to="/pre" class="live-button-container no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-Poppins">
            <Video class="live-icon" :stroke-width="1" />
          </RouterLink>
          <RouterLink to="/calender" class="calender-button-container no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-Poppins">
            <img :src="calender" alt="천문일정" class="calender-icon" width="28px" height="28px">
          </RouterLink>
        </div>
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
import { RouterLink, useRoute } from 'vue-router';
import { computed } from 'vue'
import logo from "@/assets/pictures/stellabot/logo.png"
import calender from "@/assets/pictures/stellabot/calender.png"
import { useAccountStore } from "@/stores/account.js"

const route = useRoute()
const account = useAccountStore()
const isLogin = computed(() => account.isLogin)
const userInfo = computed(() => account.userInfo)

const isRoomView = computed(() => route.name === 'RoomView')

// 라우트 이름에 따라 헤더 색상을 변경
const headerClasses = computed(() => {
  if (route.name === 'MainView') {
    return 'bg-transparent text-slate-50';
  } else {
    return 'bg-black text-slate-50';
  }
});

const handleLogout = () => {
  account.logOut()
}
</script>

<style scoped>
.icon-container {
  display: flex;
}
.live-icon {
  width: 28px;
  height: 28px;
}
.live-button-container {
  display: flex;
  align-items: center;
  text-decoration: none;
  font-weight: bold;
  transition: color 0.3s ease;
  position: relative;
}
.live-button-container:hover::before {
  content: '은하 라이브관';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  background-color: #333;
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  opacity: 1;
  visibility: visible;
  transition: opacity 0.3s ease;
}
.calender-icon {
  width: 20px;
  height: 20px;
}
.calender-button-container {
  display: flex;
  align-items: center;
  text-decoration: none;
  font-weight: bold;
  transition: color 0.3s ease;
  position: relative;
}
.calender-button-container:hover::before {
  content: '은하 달력관';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  background-color: #333;
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  opacity: 1;
  visibility: visible;
  transition: opacity 0.3s ease;
}
</style>