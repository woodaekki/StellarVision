<template>
  <button class="profile-follow-button"
  @click="clickFollowButton">
  {{ isFollowing ? '언팔로우' : '팔로우' }}
  </button>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useProfileStore } from '@/stores/profile';

const profileStore = useProfileStore();
// 이 지금 보는 프로필 페이지의 주인이 현재 사용자와 동일하면 버튼 자체가 나타나지 않도록 v-if 추가 필요

const props = defineProps({
  profileInfo: {
    type: Object,
    required: false
  },
  profileFollowers: {
    type: Array,
    default: () => []
  }
});

// 프로필 조회하는 사람이 프로필 주인을 팔로우 중인지 체크
const followId = ref(null);
const isFollowing = ref(false)

console.log(props.profileFollowers)

watch(
  () => props.profileFollowers,
  (list) => {
    const viewerName = JSON.parse(localStorage.getItem('userInfo') || 'null')?.name ?? null
    const me = (list || []).find(follower => follower?.name === viewerName) || null

    isFollowing.value = !!me
    followId.value = me?.followerId ?? null // ← 실제 필드명 확인 필수!
  },
  { immediate: true, deep: true }
)

// 버튼 클릭시 이벤트
async function clickFollowButton() {
  console.log('팔로우 번호: ', followId.value)
  if (isFollowing.value) {
    isFollowing.value = false
    await profileStore.unfollowMember(followId.value);
  } else {
    isFollowing.value = true
    followId.value = await profileStore.followMember(props.profileInfo.memberId);
  }
};
</script>

<style scoped>
.profile-follow-button {
  padding: 8px 16px;
  background-color: #444;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
</style>
