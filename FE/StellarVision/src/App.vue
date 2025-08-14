<template>
  <div class="app-layout">
    <div
        :class="{ 'main-content': !isRoomView }"
>
      <MainHeader
        v-if="!isRoomView"
        :is-main-view="isMainView"
      />
      <main>
        <RouterView />
      </main>
      <MainFooter
        v-if="!isRoomView && !isLoginView && !isSignupView && !isPreroomView" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import MainHeader from '@/components/common/MainHeader.vue'
import MainFooter from './components/common/MainFooter.vue'

const route = useRoute()

const isRoomView = computed(() => route.name === 'RoomView')
const isMainView = computed(() => route.name === 'MainView')
const isLoginView = computed(() => route.name === 'LoginView')
const isSignupView = computed(() => route.name === 'SignupView')
const isPreroomView = computed(()=> route.name === 'PreRoomView')
</script>

<style lang="scss">
@use "@/assets/components.scss";

html,
body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

.main-content {
  margin-top: 58px;
}

main {
  flex-grow: 1;
}

// ------ 스크롤바 스타일링 코드 ------
::-webkit-scrollbar {
  width: 10px;
}

// 스크롤바 전체 색상
::-webkit-scrollbar-track {
  background-color: rgba(23, 23, 23, 0.6);
}

// 현재 스크롤 위치 색상
::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.359);
  border-radius:30px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: background-color 0.3s ease;
}

/* 호버 시 */
::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 255, 255, 0.6);
}

</style>
