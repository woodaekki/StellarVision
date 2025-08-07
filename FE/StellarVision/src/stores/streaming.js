import { ref } from 'vue';
import { defineStore } from 'pinia';
import commonApi from '@/api/commonApi';

export const useStreamingStore = defineStore('streaming', () => {
  const liveStreams = ref([]);

  const fetchLiveStreams = async () => {
    try {
      const res = await commonApi.get(`/streamings`);
      liveStreams.value = res.data.data;
      console.log(liveStreams.value);
    } catch (err) {
      console.error(`실시간 스트리밍 목록을 호출할 수 없었습니다:`, err);
      console.error(err.response);
    }
  };

  return {
    liveStreams, fetchLiveStreams
  };
});
