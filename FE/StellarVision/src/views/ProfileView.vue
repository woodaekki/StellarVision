<template>
  <div class="profile-wrapper">
    <ProfileHeader />
    <main class="main">
      <MyGalleryView />
      <MyVideoView />
    </main>
  </div>
</template>


<script setup>
import ProfileHeader from '@/components/profile/ProfileHeader.vue'
import MyGalleryView from '@/views/Profile/MyGalleryView.vue'
import MyVideoView from '@/views/Profile/MyVideoView.vue'
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
