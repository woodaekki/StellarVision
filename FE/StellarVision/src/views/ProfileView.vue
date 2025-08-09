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
    <main class="main-content">
      <div v-if="profileInfo.galleryPublic" class="content-section">
        <MyGalleryView />
      </div>
      <hr class="section-divider" v-if="profileInfo.galleryPublic && profileInfo.videoPublic">
      <div v-if="profileInfo.videoPublic" class="content-section">
        <MyVideoView
          :profilePk="profilePk"
          :recentVideos="recentVideos"
          @select="goToReplay(video.id)"
        />
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ProfileHeader from '@/components/profile/ProfileHeader.vue';
import MyGalleryView from '@/views/Profile/MyGalleryView.vue';
import MyVideoView from '@/views/Profile/MyVideoView.vue';
import { useAccountStore } from '@/stores/account';
import { useVideoStore } from '@/stores/video';
import commonApi from '@/api/commonApi';

const account = useAccountStore();
const videoStore = useVideoStore();
const route = useRoute();
const router = useRouter();

const profilePk = ref(window.history.state?.memberPk);
const profileInfo = ref(null);
const profileEmail = route.params.id;

const loading = ref(true);
const profileUpdateLoading = ref(false);

const recentVideos = computed(() => videoStore.replays);

onMounted(async () => {
  if (!profilePk.value) {
    if (account.token) {
      commonApi.defaults.headers.common.Authorization = `Bearer ${account.token}`;
      await account.fetchMyProfile();
      profileInfo.value = account.myProfile;
      profilePk.value = profileInfo.value.memberId;
      await videoStore.fetchReplays(profilePk.value, 0, 3);
    }
  } else {
    profileInfo.value = await account.fetchUserProfile(profilePk);
    await videoStore.fetchReplays(profilePk.value, 0, 3);
  }

  loading.value = false;
  console.log('프로필 정보:', profileInfo.value);
})

async function handleUpdateImageUrl() {
  profileUpdateLoading.value = true;
  if (!profilePk.value && account.token) {
    commonApi.defaults.headers.common.Authorization = `Bearer ${account.token}`;
    const meRes = await commonApi.get('/profiles/me');
    profileInfo.value = meRes.data.data;
  }
  else {
    const userRes = await commonApi.get(`/profiles/${profilePk.value}`);
    profileInfo.value = userRes.data.data;
  }
  profileUpdateLoading.value = false;
}

function goToReplay(videoId) {
  router.push(`/replay/${videoId}`)
}
</script>

<style scoped>
.profile-wrapper {
  padding-top: 58px;
  background-color: #ffffff;
  background-size: cover;
  min-height: 100vh;
  color: white;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin: 0 auto;
  padding: 0 10px;
  box-sizing: border-box;
  overflow-y: auto;
  max-height: calc(100vh - 130px); /* 기본(데스크톱) */
}

.content-section {
  width: 100%;
}

.section-divider {
  width: 85%;
  border: 0;
  height: 1px;
  background-color: #ccc;
  margin: 10px 0;
}

/* 1500px 이하: 패딩 조금 늘리고 높이 보정 */
@media (max-width: 1500px) {
  .main-content {
    padding: 0 20px;
    max-height: calc(100vh - 120px);
  }
  .section-divider {
    width: 90%;
  }
}

@media (max-width: 1024px) {
  .main-content {
    max-height: calc(100vh - 110px);
  }
}

@media (max-width: 767px) {
  .profile-wrapper {
    padding-top: 50px;
  }
  .main-content {
    padding: 0 10px;
    max-height: calc(100vh - 300px);
  }
  .section-divider {
    width: 95%;
    margin: 5px 0;
  }
}
</style>

