<script setup>
import { useRecordingStore } from '@/stores/recording';
import { ref, onMounted } from 'vue';

const streams = ref([]);

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
    <ul>
      <!-- <li v-for="stream in streams" :key="stream.id">
        <router-link :to="`/stream/${stream.sessionId}`">{{ stream.title }}</router-link>
      </li> -->
      <video
      v-if="recordingStore.videoUrl"
      ></video>
    </ul>
  </div>
</template>

<style scoped>
</style>
