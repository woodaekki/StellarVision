<template>
  <!-- 가상으로 레이아웃만 만들어놓기 -->
 <div class="profile-header">
    <div class="profile-header-left">
      <div class="profile-image">+</div>

      <div class="profile-text">
        <p>닉네임</p>
        <p>안녕하세요 닉네임 입니다.</p>
      </div>
    </div>


    <div class="profile-header-right">
      <button @click="goEditProfile">수정</button>
    </div>
 </div>



</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAccountStore } from '@/stores/account.js'

const router = useRouter();
const member_id = 1; //테스트용 가상 사용자 id

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

.profile-header-right button {
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
