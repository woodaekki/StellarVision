<!-- MainView.vue -->
<template>
  <div class="main-wrapper">
    <section class="video-container">
      <video autoplay muted loop playsinline class="background-video" src="/videos/test6.mp4"></video>
      <MainGlobe :liveStreams="liveStreams" />
    </section>
    <MainSidebar ref="sidebarRef"
    class="absolute top-0 right-0 h-screen"/>
  </div>
</template>

<script setup>
import { computed, onMounted, provide, ref } from 'vue';
import { storeToRefs } from 'pinia';
import { useStreamingStore } from '@/stores/streaming';
import MainGlobe from '@/components/main/MainGlobe.vue';
import MainSidebar from '@/components/common/MainSidebar.vue';

const streamingStore = useStreamingStore();
const { liveStreams } = storeToRefs(streamingStore);

// 사이드바 상태를 하위 컴포넌트에 제공
const sidebarRef = ref(null)

const sidebarState = computed(()=>({
  isOpen: sidebarRef.value?.isOpen || false
}))
provide('sidebarState', sidebarState)

onMounted(async () => {
  await streamingStore.fetchLiveStreams();
  // console.log('liveStreams:', liveStreams.value);
});
</script>

<style scoped>
.main-wrapper {
  position: fixed; /* 이게 있어야 메인 페이지에 스크롤이 안 생깁니다 */
  inset: 0;
  width: 100vw;
  height: 100svh;
  max-width: 100vw;
  max-height: 100svh;
  overflow: hidden;
  box-sizing: border-box;
}

.video-container {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.background-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
