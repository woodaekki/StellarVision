<template>
  <button class="profile-follow-button"
  @click="clickFollowButton">
  {{ isFollowing ? '언팔로우' : '팔로우' }}
  </button>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useProfileStore } from '@/stores/profile';

const profileStore = useProfileStore();

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
const isFollowing = ref(false);

const viewer = JSON.parse(localStorage.getItem('userInfo') || 'null');
const viewerName = viewer?.name ?? null;
const targetId = computed(() => props.profileInfo?.memberId ?? null);

watch(
  () => props.profileFollowers,
  (list) => {
    const arr = Array.isArray(list) ? list : [];
    let me = null;

    for (const follower of arr) {
      if (String(follower?.name) === String(viewerName)) {
        me = follower;
        break;
      }
    }

    if (me) {
      isFollowing.value = true; 
      followId.value = me?.followerId ?? null;
    } else {
      isFollowing.value = false;
      followId.value = null;
    }
  },
  { immediate: true, deep: true }
);


// 버튼 클릭시 이벤트
async function clickFollowButton() {
  if (isFollowing.value) {
    await profileStore.unfollowMember(followId.value);
  } else {
    await profileStore.followMember(targetId.value);
  }

  if (targetId.value) await profileStore.fetchMemberFollowers(targetId.value);
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
