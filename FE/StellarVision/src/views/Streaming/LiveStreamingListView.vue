<script setup>
import VideoCard from '@/components/streaming/VideoCard.vue';
import { useRecordingStore } from '@/stores/recording';
import { ref, onMounted } from 'vue';


const recordingStore = useRecordingStore()
console.log('url:', recordingStore.videoUrl)

onMounted(async () => {
  // API 호출하여 실시간 스트리밍 목록 가져오기
  // streams.value = await fetchLiveStreams();
});
</script>

<template>
  <div class="live-streaming-list">
    <h1>실시간 방송 목록</h1>
    <VideoCard></VideoCard>
  </div>
</template>

<style scoped>
</style>
