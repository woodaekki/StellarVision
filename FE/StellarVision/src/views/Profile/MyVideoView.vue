<template>
    <p class="title">My video</p>

    <div class="button-wrapper">
      <button v-if="recentVideos.length > 0"
        @click="goVideoList"
        class="video-list">
        상세보기
      </button>
    </div>

    <div class="video-frames" v-if="recentVideos.length > 0">
      <VideoFrame
       v-for="video in recentVideos"
       @click="goToReplayRoom(video.id)"
       :key="video.id"
       :video="video"
      />
    </div>

    <div v-else class="no-video">
      <p>업로드한 영상이 없습니다.</p>
    </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import VideoFrame from './VideoFrame.vue';

const router = useRouter();
const route = useRoute();

const props = defineProps({
  profilePk: {
    type: Number,
    required: false
  },
  recentVideos : {
    type: Array,
    default: () => []
  }
})

const goVideoList = () => {
  router.push({
    name: 'MyVideoListView',
    params: { id: route.params.id },
    state: {
      profilePk: props.profilePk
    }
  })
};

const goToReplayRoom = (videoId) => {
  router.push({
    name: 'ReplayView',
    params: { id: videoId },
  })
};
</script>

<style scoped>
.title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  margin-top: 60px;
  margin-bottom: 32px;
  color: #fff;
  padding: 0 20px;
}

.button-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 0 200px;
}

.video-list {
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  font-size: 16px;
  background: transparent;
  border: 1px solid #fff;
  padding: 8px 20px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.video-list:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.video-frames {
  display: flex;
  gap: 32px;
  justify-content: center;
  padding: 60px 20px;
  flex-wrap: nowrap;
  overflow-x: auto;
}

@media (max-width: 1500px) {
  .video-frames {
    flex-wrap: wrap;
    overflow-x: visible;
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 28px;
    margin-top: 40px;
    margin-bottom: 24px;
  }
  .button-wrapper {
    justify-content: center;
    padding: 0 20px;
    margin-bottom: 20px;
  }
  .video-list {
    font-size: 14px;
    padding: 6px 16px;
  }
}

.no-video {
  text-align: center;
  margin: 12rem;
}
</style>
