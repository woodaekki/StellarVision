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

    <!-- 삭제 확인 모달 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <h3>영상 삭제</h3>
        <p>정말로 이 영상을 삭제하시겠습니까?</p>
        <p class="video-name">{{ videoToDelete?.name }}</p>
        <div class="modal-actions">
          <button @click="closeDeleteModal" class="cancel-btn">취소</button>
          <button @click="confirmDelete" class="delete-btn" :disabled="deleting">
            {{ deleting ? '삭제 중...' : '삭제' }}
          </button>
        </div>
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
const showDeleteModal = ref(false);
const videoToDelete = ref(null);
const deleting = ref(false);

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
  videoToDelete.value = video;
  showDeleteModal.value = true;
};

const closeDeleteModal = () => {
  showDeleteModal.value = false;
  videoToDelete.value = null;
};

const confirmDelete = async () => {
  if (!videoToDelete.value || deleting.value) return;

  deleting.value = true;
  
  try {
    // commonApi를 사용하여 삭제 API 호출
    await commonApi.delete(`/videos/${videoToDelete.value.id}`);
    
    // 성공 시 영상 제거
    const index = videoStore.replays.findIndex(v => v.id === videoToDelete.value.id);
    if (index > -1) {
      videoStore.replays.splice(index, 1);
    }
    
    alert('영상이 성공적으로 삭제되었습니다.');
    closeDeleteModal();
    
  } catch (error) {
    console.error('영상 삭제 실패:', error);
    alert('영상 삭제에 실패했습니다. 다시 시도해주세요.');
  } finally {
    deleting.value = false;
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

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-content h3 {
  margin: 0 0 16px 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
}

.modal-content p {
  margin: 0 0 8px 0;
  color: #666;
}

.video-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 24px !important;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.cancel-btn,
.delete-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.delete-btn {
  background-color: #dc3545;
  color: white;
}

.delete-btn:hover:not(:disabled) {
  background-color: #c82333;
}

.delete-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>