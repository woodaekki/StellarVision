<template>
  <div v-if="loading">
    <p>Loading profile...</p>
  </div>
  <div v-else class="profile-wrapper">
    <main class="main-content">
      <div class="content-section">
         <ProfileHeader
            v-if="!profileUpdateLoading"
            :profile-info="profileInfo"
            :profile-followings="profileFollowings"
            :profile-followers="profileFollowers"
            :profile-email="profileEmail"
            @updateProfileImageUrl="handleUpdateImageUrl"/>
      </div>
      <div class="content-section">
        <MyGalleryView />
      </div>
      <hr class="section-divider">
      <div class="content-section">
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
import { useProfileStore } from '@/stores/profile';
import commonApi from '@/api/commonApi';

const account = useAccountStore();
const videoStore = useVideoStore();
const profileStore = useProfileStore();
const route = useRoute();
const router = useRouter();

const profilePk = ref(window.history.state?.memberPk);
const profileInfo = ref(null);
const profileEmail = route.params.id;
const profileFollowers = computed(() => profileStore.followers);
const profileFollowings = computed(() => profileStore.followings);

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
      await profileStore.fetchMemberFollowers(profilePk.value);
      await profileStore.fetchMemberFollowings(profilePk.value);
    }
  } else {
    profileInfo.value = await account.fetchUserProfile(profilePk.value);
    await videoStore.fetchReplays(profilePk.value, 0, 3);
    await profileStore.fetchMemberFollowers(profilePk.value);
    await profileStore.fetchMemberFollowings(profilePk.value);
  }

  loading.value = false;
  console.log('지금 보는 페이지의 프로필 정보:', profileInfo.value);
  console.log('내 정보:', JSON.parse(localStorage.getItem('userInfo')));
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
  background-color: #ffffff;
  background-size: cover;
  min-height: 100vh;
  color: white;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin: 0 auto;
  padding: 0 0;
  box-sizing: border-box;
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
</style>
