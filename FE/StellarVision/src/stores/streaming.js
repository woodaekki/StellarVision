// stores/streaming.js
import { ref } from 'vue';
import { defineStore } from 'pinia';
import commonApi from '@/api/commonApi';
import { Stream } from 'openvidu-browser';

export const useStreamingStore = defineStore('streaming', () => {
  const liveStreams = ref([]);
  const replayStreams = ref([])
  const fetchLiveStreams = async () => {
    try {
      const res = await commonApi.get(`/streamings`);
      const list = Array.isArray(res.data?.data) ? res.data.data : [];
      liveStreams.value = [...list];
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
  // 좋아요
  const likeVideo = async (videoId) =>{
    try {
      const { data } = await commonApi.post(`/videos/${videoId}/likes`)
      console.log(data?.data)
      return data?.data
    } catch(err){
      console.error('좋아요 호출 실패', err)
    }
  };
  // 좋아요 취소
  const unlikeVideo = async (videoId) => {
    try{
      const { data } = await commonApi.delete(`/videos/${videoId}/likes`)
      return data?.data
    } catch(err) {
      console.error('좋아요 취소 호출 실패', err)
    }
  };

  return {
    liveStreams, replayStreams, fetchLiveStreams, fetchReplayStreams, likeVideo, unlikeVideo
  };
});
