<template>
  <!-- 가상으로 레이아웃만 만들어놓기 -->
 <div class="profile-header">
    <div class="profile-header-left">
      <div class="profile-image">+</div>

      <div class="profile-text">
        <p>닉네임</p>
        <FollowButton />
        <p>안녕하세요 닉네임 입니다.</p>
        <!-- 팔로잉이랑 팔로워 표시, 클릭시 모달창 나타남 -->
        <div class="follow-list-button">
          <button @click="showFollowers = true">팔로워 {{ follower.length }}</button>
          <button @click="showFollowing = true">팔로잉 {{ following.length }}</button>
          <UserListModal
            v-if="showFollowers"
            :user-list="follower"
            @close="showFollowers = false"
          />
          <UserListModal
            v-if="showFollowing"
            :user-list="following"
            @close="showFollowing = false"
          />
        </div>
      </div>
    </div>

    <div class="profile-header-right">
      <button @click="goEditProfile" class="account-edit">수정</button>
    </div>
 </div>

</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// import { useAccountStore } from '@/stores/account.js'
import FollowButton from './FollowButton.vue';
import UserListModal from './UserListModal.vue';

const router = useRouter();
const member_id = 1; //테스트용 가상 사용자 id

// 임의로 하드코딩한 팔로잉이랑 팔로워
const following = ref(
  [{
    id: 2,
    nickname: '김싸피'
  },
  {
    id: 4,
    nickname: '노바'
  },
  {
    id: 5,
    nickname: '홍길동'
  }]
)
const follower = ref(
 [{
    id: 4,
    nickname: '노바'
  }]
)

const showFollowers = ref(false)
const showFollowing = ref(false)

// 팔로잉 & 팔로워 관련 끝

const goEditProfile = () => {
  router.push(`/profile/${member_id}/edit`)
}
</script>

<style scoped>
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background-color: #2e2e32;
}

.profile-header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #444;
  color: #fff;
  font-size: 24px;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: center;
}

.profile-text {
  display: flex;
  flex-direction: column;
}

.profile-text p {
  margin: 0;
  color: #fff;
  line-height: 1.4;
}

.profile-text p:first-child {
  font-size: 18px;
  font-weight: 600;
}

.profile-text p:last-child {
  font-size: 14px;
  color: #bbb;
  margin-top: 4px;
}

.account-edit {
  padding: 8px 16px;
  background-color: #444;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.profile-header-right button:hover {
  background-color: #666;
}
</style>
