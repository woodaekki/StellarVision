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
      <div v-if="profileInfo.galleryPublic">
        <MyGalleryView />
      </div>
      <div v-if="profileInfo.videoPublic">
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
const profileEmail = route.params.id; // 이메일은 수정 불가라 ref 없이 할당

const loading = ref(true);
const profileUpdateLoading = ref(false);

const recentVideos = computed(() => videoStore.replays);

onMounted(async () => {
  // 메인 헤더에서 직접 접근했을 경우 내 프로필을 호출
  if (!profilePk.value) {
    // 오류 방지를 위해 토큰 존재 확인
    if (account.token) {
      commonApi.defaults.headers.common.Authorization = `Bearer ${account.token}`;
      await account.fetchMyProfile();
      profileInfo.value = account.myProfile;
      profilePk.value = profileInfo.value.memberId;
      await videoStore.fetchReplays(profilePk.value, 1, 3);
    }
  // 다른 경로로 들어왔을 경우 (다른 유저 프로필에 접근, pk 제공 상정)
  } else {
    profileInfo.value = await account.fetchUserProfile(profilePk);
    await videoStore.fetchReplays(profilePk.value, 1, 3);
  }

  loading.value = false;
  console.log('프로필 정보:', profileInfo.value);
})

// 프로필 이미지 갱신 캐치
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
  background-image: url(@/assets//pictures/wallpaper/space.jpeg); 
  min-height: 100vh;
  color: white;
  display: flex;
  flex-direction: column;
}

</style>
