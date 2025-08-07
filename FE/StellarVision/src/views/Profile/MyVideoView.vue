<template>
  <div class="profile-section-container">
    <div class="profile-section">
      <div class="section-header">
        <h2 class="section-title">My Video</h2>
        <button v-if="recentVideos.length > 0" @click="goVideoList" class="detail-button">
          더보기
        </button>
      </div>
      <div class="content-frames">
        <VideoFrame
          v-for="video in recentVideos"
          @click="goToReplayRoom(video.id)"
          :key="video.id"
          :video="video"
          class="content-frame video-frame"
        />
        <div v-if="recentVideos.length === 0" class="empty-frame">
          <p class="empty-text">업로드한 영상이 없습니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import VideoFrame from './VideoFrame.vue'

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
    state: {
      profilePk: props.profilePk,
    },
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
  display: flex;
  justify-content: center;
  width: 100%;
}

.profile-section {
  width: 100%;
  max-width: 1200px; 
  margin: 0;
  padding: 10px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5px;
  margin-bottom: 5px;
  padding-bottom: 5px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.detail-button {
  color: #fff;
  cursor: pointer;
  background: transparent;
  border: 1px solid #fff;
  padding: 5px 12px;
  border-radius: 4px;
}

.content-frames {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
  padding-top: 5px;
}

.content-frame,
.empty-frame {
  width: calc(33.33% - 10px);
  max-width: 300px;
  aspect-ratio: 1.4;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.empty-frame {
  background-color: #f5f5f5;
  border: 2px dashed #ccc;
  justify-content: center;
  align-items: center;
}

.empty-text {
  color: #999;
  font-size: 14px;
  font-weight: 500;
}
</style>