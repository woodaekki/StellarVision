<template>
  <div v-if="loading">
    <p>Loading profile...</p>
  </div>
  <div v-else class="profile-wrapper bg-stone-800">
    <img :src="bg" alt="배경 이미지" class="bg-img">
    <main class="main-content">

      <div class="white-jelly-panel">

        <!-- 프로필 헤더 -->
        <div class="profile-header-wrapper">
          <ProfileHeader
            v-if="!profileUpdateLoading"
            :profile-info="profileInfo"
            :profile-followings="profileFollowings"
            :profile-followers="profileFollowers"
            :profile-email="profileEmail"
            @updateProfileImageUrl="handleUpdateImageUrl"
            @descriptionUpdated="onDescriptionUpdated"
          />
        </div>

        <hr class="inner-divider" />

        <!-- My Gallery Section -->
        <section class="section-wrapper">
          <MyGalleryView />
        </section>

        <hr class="inner-divider" />

        <!-- My Video Section -->
        <section class="section-wrapper">
          <MyVideoView
            :profilePk="profilePk"
            :recentVideos="recentVideos"
            @select="goToReplay"
          />
        </section>

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
import bg from '@/assets/pictures/stellabot/spaceBackground.avif'

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
});

async function handleUpdateImageUrl() {
  profileUpdateLoading.value = true;
  if (!profilePk.value && account.token) {
    commonApi.defaults.headers.common.Authorization = `Bearer ${account.token}`;
    const meRes = await commonApi.get('/profiles/me');
    profileInfo.value = meRes.data.data;
  } else {
    const userRes = await commonApi.get(`/profiles/${profilePk.value}`);
    profileInfo.value = userRes.data.data;
  }
  profileUpdateLoading.value = false;
}

function onDescriptionUpdated(newDesc) {
  if (!profileInfo.value) return
  profileInfo.value = { ...profileInfo.value, description: newDesc }
}

function goToReplay(videoId) {
  router.push(`/replay/${videoId}`);
}
</script>

<style scoped>
.profile-wrapper {
  position: relative;
  min-height: 100vh;
  background-image:
    linear-gradient(rgba(11, 12, 16, 0.7), rgba(11, 12, 16, 0.7)),
    url('https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=1920&q=80');
  background-size: cover;
  background-position: center;
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
  padding: 0;
  box-sizing: border-box;
}

.profile-header-wrapper {
  margin-bottom: 30px;
}

.white-jelly-panel {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 35px 40px;
  margin: 25px auto;
  max-width: 1140px;

  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);

  box-shadow:
    inset 3px 3px 6px rgba(255 255 255 / 0.5),
    inset -3px -3px 6px rgba(0 0 0 / 0.1);

  border: 1.2px solid rgba(255, 255, 255, 0.2);

  color: rgba(30, 30, 30, 0.7);

  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  letter-spacing: 0.02em;

  transition: box-shadow 0.3s ease;
  cursor: default;
}

.white-jelly-panel:hover {
  box-shadow:
    inset 5px 5px 10px rgba(255 255 255 / 0.7),
    inset -5px -5px 10px rgba(0 0 0 / 0.15);
}

.section-wrapper {
  margin-bottom: 35px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: white;
  margin: 0;
}

.btn-small {
  background: rgba(15, 20, 40, 0.4);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.7);
  padding: 6px 14px;
  border-radius: 6px;
  backdrop-filter: blur(6px);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s ease;
  text-decoration: none;
}

.btn-small:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border-color: rgba(255, 255, 255, 0.9);
}

.inner-divider {
  border: 0;
  height: 1px;
  background: rgba(255, 255, 255, 0.18);
  margin: 30px 0;
  border-radius: 1px;
}

.bg-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -1;
}
</style>
