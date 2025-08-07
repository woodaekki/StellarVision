<template>
  <header
    v-if="!isRoomView"
    :class="[
      'top-0 left-0 w-full z-50',
      isGalleryPage ? 'bg-black text-white' : 'bg-transparent text-[#cfd8dc]',
    ]"
  >
    <div class="header flex justify-between items-center px-8 py-2">
      <div class="flex items-center gap-2">
        <img :src="logo" alt="로고" width="30px" height="42px" />
        <RouterLink
          to="/"
          class="no-underline relative font-bold font-pretendard after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full"
          >StellaVision</RouterLink
        >
        <RouterLink
          to="/pre"
          class="no-underline relative font-pretendard after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full"
          ><Video class="video-icon"
        /></RouterLink>
      </div>
      <nav class="flex items-center gap-4">
        <template v-if="isLogin">
          <RouterLink
            :to="`/profile/${userInfo?.email}`"
            class="no-underline relative font-pretendard after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full"
          >
            {{ userInfo?.name }}님
          </RouterLink>
          <button
            @click="handleLogout"
            class="inline-block cursor-pointer border-none bg-transparent text-white no-underline font-pretendard relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full"
          >
            로그아웃
          </button>
        </template>
        <template v-else>
          <RouterLink
            to="/signup"
            class="no-underline relative font-pretendard after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full"
            >회원가입</RouterLink
          >
          <RouterLink
            to="/login"
            class="no-underline relative font-pretendard after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full"
            >로그인</RouterLink
          >
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { Video } from 'lucide-vue-next';
import { RouterLink, useRoute } from 'vue-router';
import { computed } from 'vue';
import logo from '@/assets/pictures/stellabot/logo.png';
import { useAccountStore } from '@/stores/account.js';

const route = useRoute();
const account = useAccountStore();
const isLogin = computed(() => account.isLogin);
const userInfo = computed(() => account.userInfo);

const isGalleryPage = computed(() => route.name === 'MyGalleryListView' || route.path.includes('gallery'));
const isRoomView = computed(() => route.name === 'RoomView');
const handleLogout = () => {
  account.logOut();
};
console.log(userInfo.value);
</script>

<style>
.video-icon {
  color: #ff0000;
}
</style>