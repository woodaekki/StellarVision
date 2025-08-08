<template>
  <div class="main-wrapper">
    <section class="video-container">
      <video autoplay muted loop playsinline class="background-video" src="/videos/test6.mp4"></video>
      <MainGlobe :liveStreams="liveStreams" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useStreamingStore } from '@/stores/streaming';
import MainGlobe from '@/components/main/MainGlobe.vue';

const streamingStore = useStreamingStore();
const liveStreams = computed(() => streamingStore.liveStreams);

onMounted(async () => {
  await streamingStore.fetchLiveStreams();
  // console.log('liveStreams:', liveStreams.value);
});
</script>

<style scoped>
</style>

