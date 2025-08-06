<template>
  <div v-if="loading">
    <p>Loading profile...</p>
  </div>
  <div v-else class="profile-wrapper">
    <ProfileHeader
    v-if="!profileUpdateLoading"
    :profile-info="profileInfo"
    :profile-email="profileEmail"
    @updateProfileImageUrl="handleUpdateImageUrl"/>
    <main class="main">
      <MyGalleryView :v-if="profileInfo.isGalleryPublic"/>
      <MyVideoView :v-if="profileInfo.isVideoPublic"/>
    </main>
  </div>
</template>


<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import ProfileHeader from '@/components/profile/ProfileHeader.vue';
import MyGalleryView from '@/views/Profile/MyGalleryView.vue';
import MyVideoView from '@/views/Profile/MyVideoView.vue';
import { useAccountStore } from '@/stores/account';
import commonApi from '@/api/commonApi';

const account = useAccountStore();
const profilePk = window.history.state?.memberPk;
const route = useRoute();
const profileInfo = ref(null);
const profileEmail = route.params.id; // 이메일은 수정 불가라 ref 없이 할당
const loading = ref(true);
const profileUpdateLoading = ref(false);

onMounted(async () => {
  // 메인 헤더에서 직접 접근했을 경우 내 프로필을 호출
  if (!profilePk) {
    // 오류 방지를 위해 토큰 존재 확인
    if (account.token) {
      commonApi.defaults.headers.common.Authorization = `Bearer ${account.token}`;
      await account.fetchMyProfile();
      profileInfo.value = account.myProfile;
    }
  // 다른 경로로 들어왔을 경우 (다른 유저 프로필에 접근, pk 제공 상정)
  } else {
    profileInfo.value = await account.fetchUserProfile(profilePk);
  };

  loading.value = false;
  console.log('프로필 정보:', profileInfo.value);
})

// 프로필 이미지 갱신 캐치
async function handleUpdateImageUrl() {
  profileUpdateLoading.value = true;
  if (!profilePk && account.token) {
    commonApi.defaults.headers.common.Authorization = `Bearer ${account.token}`;
    const meRes = await commonApi.get('/profiles/me');
    profileInfo.value = meRes.data.data;
  }
  else {
    const userRes = await commonApi.get(`/profiles/${profilePk}`);
    profileInfo.value = userRes.data.data;
  }
  profileUpdateLoading.value = false;
}
</script>

<style scoped>
.profile-wrapper {
  background-color: #000;
  min-height: 100vh;
  color: white;
  display: flex;
  flex-direction: column;
}

</style>
