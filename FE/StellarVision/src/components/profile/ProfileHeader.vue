<template>
 <div class="profile-header">
    <div class="profile-header-left">
      <div class="profile-image">+</div>

      <div class="profile-text">
        <p>{{ email || 'null' }}</p>
        <p>안녕하세요 {{ email || 'null' }} 입니다.</p>
      </div>
    </div>

    <div class="profile-header-right">
      <RouterLink :to="`/profile/${userInfo?.email}/edit`">
        수정
      </RouterLink>
    </div>
 </div>
</template>

<script setup>
import { useAccountStore } from '@/stores/account.js'
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const account = useAccountStore()
const { userInfo } = account
const email = computed(() => userInfo?.email || 'null')
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
