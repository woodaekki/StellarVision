<template>
  <div class="page" ref="pageRef">
    <div class="stars-background">
      <div class="px-4 pt-12 pb-6">
        <h2 class="text-2xl mb-2 text-center font-pretendard" style="font-family: 'Pretendard', sans-serif !important;">
          My Space Video
        </h2>
        <hr class="border-t-2 border-neutral-200 w-full mt-2" />
      </div>

      <div class="px-4 pb-12">
        <div v-if="!loading && videos.length > 0" class="video-grid">
          <VideoCell
            v-for="(video, index) in videos"
            :key="video.id"
            :video="video"
            :show-edit="isUploader"
            :class="{ 'fade-in': video.isNew }"
            @select="goToReplay(video.id)"
            @delete="handleDeleteVideo"
            @animationend="onAnimationEnd(video.id)"
          />
        </div>

        <div v-else-if="!loading" class="loading-text">
          아직 업로드한 영상이 없습니다.
        </div>

        <div v-if="loading" class="loading-spinner">
          <div class="spinner"></div>
          <div class="loading-text">영상 목록을 불러오는 중...</div>
        </div>

        <div v-if="loadingMore" class="loading-spinner">
          <div class="spinner"></div>
          <div class="loading-text">더 많은 영상 로딩 중...</div>
        </div>

        <div v-if="!loading && !loadingMore && hasMore" class="loading-text">스크롤하여 더 많은 영상 보기</div>

        <!-- 무한 스크롤 (Intersection Observer 감지) -->
        <div ref="observerTarget" class="observer-target"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useVideoStore } from '@/stores/video';
import VideoCell from '@/components/video/VideoCell.vue';
import commonApi from '@/api/commonApi';

const route = useRoute();
const router = useRouter();
const videoStore = useVideoStore();

const loading = ref(true);
const loadingMore = ref(false);
const hasMore = ref(true);
const page = ref(0);
const pageRef = ref(null);
const observerTarget = ref(null);

const PAGE_SIZE = 8;
const SORT_OPTION = 0;

const profilePk = window.history.state?.profilePk || route.params.profilePk;
const isUploader = JSON.parse(localStorage.getItem('userInfo'))?.email === route.params.id;

const videos = computed(() =>
  videoStore.replays.map(v => ({
    id: v.id,
    name: v.originalFilename,
    thumbnail: v.thumbnailDownloadUrl,
    date: v.createdAt?.split('T')[0],
    isNew: v.isNew || false
  }))
);

const onAnimationEnd = (videoId) => {
  const video = videoStore.replays.find(v => v.id === videoId);
  if (video) video.isNew = false;
};

const fetchVideos = async () => {
  if (page.value > 0 && (loading.value || loadingMore.value || !hasMore.value)) return;

  if (page.value === 0) {
    loading.value = true;
  } else {
    loadingMore.value = true;
  }

  try {
    const currentLength = videoStore.replays.length;
    const data = await videoStore.fetchReplays(profilePk, page.value, PAGE_SIZE, SORT_OPTION);

    await nextTick();
    const newVideos = videoStore.replays.slice(currentLength);
    newVideos.forEach(v => v.isNew = true);

    hasMore.value = !data.isLast;
    page.value++;
  } catch (err) {
    console.error('동영상 불러오기 실패:', err);
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
};

const goToReplay = (videoId) => {
  router.push(`/replay/${videoId}`);
};

const handleDeleteVideo = (video) => {
  if (!confirm('정말로 이 영상을 삭제하시겠습니까?')) return;
  performDelete(video);
};

const performDelete = async (video) => {
  try {
    await commonApi.delete(`/videos/${video.id}`);

    const idx = videoStore.replays.findIndex(v => v.id === video.id);
    if (idx > -1) videoStore.replays.splice(idx, 1);

    alert('영상이 성공적으로 삭제되었습니다.');
  } catch (error) {
    console.error('영상 삭제 실패:', error);
    alert('영상 삭제에 실패했습니다. 다시 시도해주세요.');
  }
};

let observer = null;
const setupInfiniteScroll = () => {
  if (!observerTarget.value) return;

  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0];
      if (entry.isIntersecting && !loading.value && !loadingMore.value && hasMore.value) {
        fetchVideos();
      }
    },
    {
      rootMargin: '100px',
      threshold: 0.1,
    }
  );

  observer.observe(observerTarget.value);
};

const cleanupInfiniteScroll = () => {
  if (observer) {
    observer.disconnect();
    observer = null;
  }
};

onMounted(() => {
  fetchVideos();
  setupInfiniteScroll();
});

onBeforeUnmount(() => {
  cleanupInfiniteScroll();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
}

.stars-background h2 {
  margin-top: 20px !important;
  margin-bottom: 40px !important;
  margin-left: 10px;
  text-align: left;
  font-weight: 700;
  font-size: medium;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.fade-in {
  opacity: 0;
  transform: scale(0.98);
  animation: fadeInScale 0.5s ease-out forwards;
}

@keyframes fadeInScale {
  0% {
    opacity: 0;
    transform: scale(0.98);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 2rem 0;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  text-align: center;
  margin-top: 1rem;
  color: #ccc;
}

.observer-target {
  height: 1px;
  width: 100%;
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
