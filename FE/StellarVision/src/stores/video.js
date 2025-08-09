// video.js
import { ref } from 'vue';
import { defineStore } from 'pinia';
import commonApi from '@/api/commonApi';
import { Video } from 'lucide-vue-next';

export const useVideoStore = defineStore('video', () => {
  const replays = ref([]);

  const setReplays = (videos) => {
    replays.value = videos;
  };

  const addReplays = (videos) => {
    replays.value.push(...videos);
  };

  const clearReplays = () => {
    replays.value = [];
  };

  const fetchReplays = async (memberId, page, size) => {
    try {
      const res = await commonApi.get(`/profiles/${memberId}/videos`, {
        params: { page, size }
      });

      const videos = res.data.data.videos || [];

      if (page === 0) {
        setReplays(videos);
      } else {
        addReplays(videos);
      }

      return res.data;
    } catch (err) {
      console.error(`${memberId}번 회원의 다시보기를 불러올 수 없습니다.`, err);
      throw err;
    }
  };

  return {
    replays,
    setReplays,
    addReplays,
    clearReplays,
    fetchReplays
  };
});
