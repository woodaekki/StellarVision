import { ref } from 'vue';
import { defineStore } from 'pinia';
import commonApi from '@/api/commonApi';

export const useVideoStore = defineStore('video', () => {
  const replays = ref([]);

  const fetchReplays = async (memberId, page, size) => {
    try {
      const res = await commonApi.get(`/profiles/${memberId}/videos`, {
        params: {
          page: page,
          size: size
        }
      });
      replays.value = res.data.data.videos;
    } catch (err) {
      console.error(`${memberId}번 회원의 다시보기를 호출할 수 없었습니다:`, err);
      console.error(err.response);
    }
  };

  return {
    replays, fetchReplays
  };
});
