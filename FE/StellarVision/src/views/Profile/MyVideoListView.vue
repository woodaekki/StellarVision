<template>
  <div class="page" ref="pageRef">
    <div class="stars-background">
      <div class="header">
        <h1>내 동영상 목록</h1>
      </div>

      <div v-if="!loading && videos.length > 0" class="video-list-wrapper">
        <VideoCell
          v-for="video in videos"
          :key="video.id"
          :video="video"
          :show-edit="isUploader"
          @select="goToReplay(video.id)"
        />
        <div v-if="loadingMore" class="loading-more">로딩 중...</div>
      </div>

      <div v-else-if="!loading" class="empty-state">
        <p>아직 업로드한 영상이 없습니다.</p>
      </div>

      <div v-else class="loading-state">
        <p>영상 목록을 불러오는 중...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useVideoStore } from '@/stores/video';
import VideoCell from '@/components/video/VideoCell.vue';

const route = useRoute();
const router = useRouter();
const videoStore = useVideoStore();

const loading = ref(true);
const loadingMore = ref(false);
const hasMore = ref(true);
const page = ref(0);
const pageRef = ref(null);

const profilePk = window.history.state?.profilePk || route.params.profilePk;
const isUploader = JSON.parse(localStorage.getItem('userInfo'))?.email === route.params.id;

const videos = computed(() =>
  videoStore.replays.map(v => ({
    id: v.id,
    name: v.originalFilename,
    thumbnail: v.thumbnailDownloadUrl,
    date: v.createdAt?.split('T')[0],
  }))
);

const fetchVideos = async () => {
  if (!hasMore.value) return;

  if (page.value === 0) loading.value = true;
  else loadingMore.value = true;

  try {
    const data = await videoStore.fetchReplays(profilePk, page.value, 10);
    hasMore.value = !data.isLast;
    page.value++;
  } catch (err) {
    console.error('동영상 불러오기 실패:', err);
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
};

const handleScroll = () => {
  const el = pageRef.value;
  if (!el || loadingMore.value || !hasMore.value) return;

  const { scrollTop, scrollHeight, clientHeight } = el;
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchVideos();
  }
};

const goToReplay = (videoId) => {
  router.push(`/replay/${videoId}`);
};

onMounted(() => {
  fetchVideos();
  pageRef.value?.addEventListener('scroll', handleScroll);
});

onBeforeUnmount(() => {
  pageRef.value?.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
.page {
  background-color: #fff;
  min-height: 100vh;
  color: white;
  font-family: sans-serif;
  overflow-y: auto;
}

.video-list-wrapper {
  text-align: center;
  padding: 1rem;
}

.loading-more,
.loading-state,
.empty-state {
  text-align: center;
  margin-top: 2rem;
}
</style>
