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
        <div
          v-for="video in recentVideos"
          :key="video.id"
          class="content-frame video-frame"
          @click="goToReplayRoom(video.id)"
        >
          <img
            :src="video.thumbnailDownloadUrl"
            alt="video thumbnail"
            style="width: 100%; height: 100%; object-fit: cover"
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
  margin-bottom: 20px;
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
  padding: 0 35px;
  margin-bottom: 5px;
  padding-bottom: 5px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #000;
}

.detail-button {
  color: #000;
  cursor: pointer;
  background: transparent;
  border: 1px solid #4a4a4a;
  padding: 5px 12px;
  border-radius: 4px;
}

.content-frames {
  display: flex;
  gap: 15px;
  justify-content: flex-start;
  flex-wrap: wrap;
  padding-top: 5px;
}

.content-frame,
.empty-frame {
  flex-basis: calc(25% - 12px);
  width: calc(25% - 12px);
  max-width: 280px;
  aspect-ratio: 1.3;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.video-frame {
  background: #fff;
}

.content-info {
  position: absolute;
  top: 3px;
  left: 3px;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 2px 4px;
  border-radius: 2px;
  font-size: 10px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.content-frame:hover .content-info {
  opacity: 1;
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
