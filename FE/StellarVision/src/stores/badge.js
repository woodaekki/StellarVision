import { ref } from 'vue';
import { defineStore } from 'pinia';
import axiosApi from '@/api/axiosApi';
import { useAccountStore } from '@/stores/account';

export const useBadgeStore = defineStore('badge', () => {
  // 모든 요청에 자동으로 토큰을 달아둠
  axiosApi.interceptors.request.use((config) => {
    const accountStore = useAccountStore();
    const token = accountStore.token || localStorage.getItem('jwt');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  const collectedBadges = ref([]);

  const fetchCollectedBadges = async (memberId) => {
    try {
      const res = await axiosApi.get(`/profiles/${memberId}/collections`);
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
