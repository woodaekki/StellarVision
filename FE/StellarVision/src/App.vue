<template>
  <div class="app-layout" :class="[{ 'sidebar-open': isSidebarOpen }, {'room' :isRoomView}]">
    <MainSidebar
      ref="sidebarRef"
      v-if="!isRoomView"
    />
    <div class="main-content">
       <MainHeader
        v-if="!isRoomView"
        :is-main-view="isMainView"
      />
      <main>
        <RouterView />
      </main>
      <MainFooter
      v-if="!isRoomView" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MainHeader from '@/components/common/MainHeader.vue'
import MainSidebar from '@/components/common/MainSidebar.vue'
import MainFooter from './components/common/MainFooter.vue'

const sidebarRef = ref(null)
const route = useRoute()

const isRoomView = computed(() => route.name === 'RoomView')
const isMainView = computed(() => route.name === 'MainView')

/*
const isSidebarOpen = computed(() => {
  return isStreamingView.value ? true : (sidebarRef.value?.isOpen ?? false)
})
*/
// 사이드바 고정 풀었습니다.
const isSidebarOpen = computed(() => {
  return sidebarRef.value?.isOpen ?? false
})


/**
onMounted(() => {
  watch(() => sidebarRef.value?.isOpen, (val) => {
    if (!isStreamingView.value) {
      console.log('사이드바 토글:', val)
    }
  })
})
**/
</script>

<style lang="scss">
@use "@/assets/components.scss";

html,
body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

main {
  flex-grow: 1;
}

.sidebar {
  z-index: 100;
  top:60px
}

.app-layout.room .main-content { padding-top: 0; }
.app-layout.room .sidebar { display: none; }

.app-layout {
  display: flex;
  min-height: 100vh;
  transition: margin-left 0.3s ease;

  .sidebar {
    position: fixed;
    top: 60px; // 헤더 높이
    left: 0;
    width: 240px;
    height: calc(100vh - 60px);
    background-color: #0b0c10;
    transition: transform 0.3s ease;
    transform: translateX(-100%);
    z-index: 200;
  }

  &.sidebar-open .sidebar {
    transform: translateX(0);
  }

  .main-content {
    flex-grow: 1;
    padding-top: 60px; // 헤더 높이
    transition: margin-left 0.3s ease;
  }

  &.sidebar-open .main-content {
    margin-left: 240px; // 사이드바 폭 만큼 밀기
  }
}



// 사이드바를 열었을때 중앙정렬이 되지 않는 화면이 많아 우선 주석 처리해두었습니다.
// .app-layout {
//   display: flex;
//   height: 100vh;

//   .sidebar {
//     position: fixed;
//     top: 0;
//     left: 0;
//     width: 175px;
//     height: 100vh;
//     background-color: #0b0c10;
//     z-index: 1000;
//     transition: transform 0.3s ease;
//     transform: translateX(-100%);
//   }

//   &.sidebar-open .sidebar {
//     transform: translateX(0);
//   }

//   .main-content {
//     margin-left: 0;
//     width: 100%;
//     transition: margin-left 0.3s ease, width 0.3s ease;
//     display: flex;
//     flex-direction: column;
//     height: 100vh;

//   }

//   &.sidebar-open .main-content {
//     margin-left: 175px;
//     width: calc(100% - 175px);
//   }
// }
</style>
