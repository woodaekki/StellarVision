import { ref } from 'vue';
import { defineStore } from 'pinia';
import commonApi from '@/api/commonApi';

export const useProfileStore = defineStore('profile', () => {
  // 팔로우 관련
  const followers = ref([]);
  const followings = ref([]);
  const followId = ref(null);

  const fetchMemberFollowers = async (memberId) => {
    try {
      const res = await commonApi.get(`/members/${memberId}/followers`);
      followers.value = res.data.data;
    } catch (err) {
      console.error(`${memberId}번 회원 팔로워를 호출할 수 없었습니다:`, err);
      console.error(err.response);
    }
  };

  const fetchMemberFollowings = async (memberId) => {
    try {
      const res = await commonApi.get(`/members/${memberId}/followers`);
      followers.value = res.data.data;
    } catch (err) {
      console.error(`${memberId}번 회원 팔로잉을 호출할 수 없었습니다:`, err);
      console.error(err.response);
    }
  };

  // 팔로우 & 언팔로우
  const followMember = async (toMemberId) => {
  try {
    const res = await commonApi.post('/follows', {
      toMemberId,
    });
      followId.value = res.data?.data?.followId ?? res.data?.followId ?? null;
    } catch (err) {
      console.error(`${toMemberId}번 회원을 팔로우할 수 없었습니다:`, err);
      console.error(err.response);
    }
  };


  const unfollowMember = async (followId) => {
    try {
      const res = await commonApi.delete(`/follows/${followId}`);
      followers.value = res.data.data;
    } catch (err) {
      console.error(`${followId}번 팔로우를 취소할 수 없었습니다:`, err);
      console.error(err.response);
    }
  };

  return {
    followers, followings, fetchMemberFollowers, fetchMemberFollowings, followMember, unfollowMember
  };
});
