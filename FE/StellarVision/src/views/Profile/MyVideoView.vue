<template>
  <div class="profile-section-container">
    <div class="profile-section">
      <div class="section-header">
        <h2 class="section-title">My Video</h2>
        <button
          v-if="recentVideos.length > 0"
          @click="goVideoList"
          class="detail-button"
        >
          더보기
        </button>
      </div>
      <div class="content-frames">
        <div
          v-for="video in recentVideos"
          :key="video.id"
          class="content-frame video-frame"
          @click="goToReplayRoom(video.id)"
        >
          <img
            :src="video.thumbnailDownloadUrl"
            alt="video thumbnail"
          />
          <p class="content-info video-date">Date: {{ video.createdAt?.split('T')[0] }}</p>
        </div>
        <div v-if="recentVideos.length === 0" class="empty-frame">
          <p class="empty-text">업로드한 영상이 없습니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  profilePk: {
    type: Number,
    required: false,
  },
  recentVideos: {
    type: Array,
    default: () => [],
  },
})

const goVideoList = () => {
  router.push({
    name: 'MyVideoListView',
    params: { id: route.params.id },
    state: { profilePk: props.profilePk },
  })
}

const goToReplayRoom = (videoId) => {
  router.push({
    name: 'ReplayView',
    params: { id: videoId },
  })
}
</script>

<style scoped>
.profile-section-container {
  margin-top: 15px;
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
  width: 100%;
  font-family: 'Pretendard', sans-serif;
  color: white;
  padding: 20px 0;
  border-radius: 20px;
}

.profile-section {
  width: 100%;
  max-width: 1200px;
  padding: 10px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 35px;
  margin-bottom: 10px;
}

.section-title {
  font-size: 25px;
  font-weight: 700;
}

.detail-button {
  color: white;
  cursor: pointer;
  background: rgba(15, 20, 40, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.7);
  padding: 6px 14px;
  border-radius: 6px;
  backdrop-filter: blur(6px);
  transition: background 0.3s ease;
}
.detail-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.content-frames {
  max-width: calc(100% - 70px);
  margin: 0 auto;
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  padding-top: 5px;
  box-sizing: border-box;
}

.content-frame {
  border-radius: 10px;
  width: calc(25% - 12px);
  max-width: 280px;
  aspect-ratio: 1.3;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.05));
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  box-shadow: 0 4px 12px rgba(0,0,0,0.4);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}
.content-frame:hover {
  box-shadow: 0 8px 20px rgba(0,0,0,0.7);
  transform: translateY(-5px);
}

.video-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.8);
  opacity: 0.97;
  transition: filter 0.3s ease, opacity 0.3s ease;
  border-radius: 10px 10px 0 0;
}
.content-frame:hover img {
  filter: brightness(1);
  opacity: 1;
}

.content-info {
  position: absolute;
  bottom: 6px;
  left: 6px;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  padding: 4px 6px;
  border-radius: 6px;
  font-size: 12px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s ease;
  user-select: none;
}
.content-frame:hover .content-info {
  opacity: 1;
}

.empty-frame {
  justify-content: center;
  align-items: center;
  min-height: 140px;
}

.empty-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin-top: 10px;
  font-weight: 500;
}
</style>
