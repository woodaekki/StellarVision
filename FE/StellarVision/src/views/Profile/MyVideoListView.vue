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
            v-for="video in videos"
            :key="video.id"
            :video="video"
            :show-edit="isUploader"
            @select="goToReplay(video.id)"
            @delete="handleDeleteVideo"
          />
        </div>

        <div v-else-if="!loading" class="loading-text">
          아직 업로드한 영상이 없습니다.
        </div>

        <div v-else class="loading-text">
          영상 목록을 불러오는 중...
        </div>

        <div v-if="loadingMore" class="loading-text">로딩 중...</div>
        <div v-if="!loading && hasMore" class="loading-text">스크롤하여 더 많은 영상 보기</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
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

const handleDeleteVideo = (video) => {
  if (!confirm('정말로 이 영상을 삭제하시겠습니까?')) return;
  
  performDelete(video);
};

const performDelete = async (video) => {
  try {
    await commonApi.delete(`/videos/${video.id}`);
    
    // 성공 시 로컬 상태에서 영상 제거
    const index = videoStore.replays.findIndex(v => v.id === video.id);
    if (index > -1) {
      videoStore.replays.splice(index, 1);
    }
    
    alert('영상이 성공적으로 삭제되었습니다.');
    
  } catch (error) {
    console.error('영상 삭제 실패:', error);
    alert('영상 삭제에 실패했습니다. 다시 시도해주세요.');
  }
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
  height: 100vh; 
  overflow-y: auto; 
}

.stars-background h2 {
  margin-top: 48px !important;
  margin-bottom: 52px !important;
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

.loading-text {
  text-align: center;
  margin-top: 1rem;
  color: #ccc;
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>