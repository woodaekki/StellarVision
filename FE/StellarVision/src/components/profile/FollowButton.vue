<template>
  <button class="profile-follow-button"
  @click="clickFollowButton">
  {{ isFollowing ? '언팔로우' : '팔로우' }}
  </button>
</template>

<script setup>
import { ref } from 'vue';
// 이 지금 보는 프로필 페이지의 주인이 현재 사용자와 동일하면 버튼 자체가 나타나지 않도록 v-if 추가 필요

// DB로부터 이 사용자를 팔로우 중인지 아닌지 확인하는 로직 필요, props로 profileview에서부터 받아올 예정

const isFollowing = ref(false)

// 버튼 클릭시 이벤트
function clickFollowButton() {
  if (isFollowing.value) {
    isFollowing.value = false
    // 언팔로우 요청 들어가야 함
  } else {
    isFollowing.value = true
    // 팔로우 요청 들어가야 함
  }
}
</script>

<style scoped>
/* 임의로 프로필 수정 버튼과 동일한 스타일을 사용 */
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
