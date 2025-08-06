import { ref } from 'vue';
import { defineStore } from 'pinia';
import commonApi from '@/api/commonApi';

export const useBadgeStore = defineStore('badge', () => {
  const collectedBadges = ref([]);

  const fetchCollectedBadges = async (memberId) => {
    try {
      const res = await commonApi.get(`/profiles/${memberId}/collections`);
      collectedBadges.value = res.data.data.collections;
    } catch (err) {
      console.error(`${memberId}번 회원이 수집한 뱃지를 호출할 수 없었습니다:`, err);
      console.error(err.response);
    }
  };

  return {
    collectedBadges, fetchCollectedBadges
  };
});
