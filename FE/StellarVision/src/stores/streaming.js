import { ref } from 'vue';
import { defineStore } from 'pinia';
import commonApi from '@/api/commonApi';

export const useStreamingStore = defineStore('streaming', () => {
  const liveStreams = ref([]);
  const replayStreams = ref([])
  const fetchLiveStreams = async () => {
    try {
      const res = await commonApi.get(`/streamings`);
      liveStreams.value = res.data.data;
      console.log('실시간 목록', liveStreams.value);
    } catch (err) {
      console.error(`실시간 스트리밍 목록을 호출할 수 없었습니다:`, err);
      console.error(err.response);
    }
  };

  const fetchReplayStreams = async () =>{
    try{
      const res = await commonApi.get('/videos');
      replayStreams.value = res.data.data?.videos || []
      console.log('다시보기 목록', replayStreams.value)
    } catch(err){
      console.error('다시보기 목록을 호출 할 수 없습니다.', err)
      console.error(err.response)
    }
  };




  return {
    liveStreams, replayStreams, fetchLiveStreams, fetchReplayStreams,
  };
});
