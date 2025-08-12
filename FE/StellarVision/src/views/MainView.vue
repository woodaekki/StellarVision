<template>
  <div class="main-wrapper">
    <section class="video-container">
      <video autoplay muted loop playsinline class="background-video" src="/videos/test6.mp4"></video>
      <MainGlobe :liveStreams="liveStreams" />
    </section>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useStreamingStore } from '@/stores/streaming';
import MainGlobe from '@/components/main/MainGlobe.vue';

const streamingStore = useStreamingStore();
const { liveStreams } = storeToRefs(streamingStore);

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
