<template>
  <div class="page" ref="pageRef">
    <img :src="bg" alt="" class="bg-img" />

    <div class="stars-background">
      <div class="px-4 pt-12 pb-6">
        <div class="navigation-links">
          <RouterLink :to="{ name: 'MyVideoListView', params: { id: myId } }" class="active">
            내 비디오
          </RouterLink>
          <span>|</span>
          <RouterLink :to="{ name: 'MyLikedListView', params: { id: myId } }">
            좋아요한 영상
          </RouterLink>
        </div>

        <h2 class="text-2xl mb-2 text-center font-pretendard">
          My Space Video
        </h2>
        <hr class="border-t-2 border-neutral-200 w-full mt-2" />
      </div>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <span class="loading-text">내 영상을 불러오는 중...</span>
        </div>
      </div>

      <div v-else-if="!loading && (!videos || videos.length === 0)" class="empty-state">
        <div class="empty-icon">🎥</div>
        <h3>아직 업로드한 영상이 없습니다.</h3>
        <p>나만의 멋진 영상을 업로드해보세요!</p>
        <RouterLink :to="{ name: 'MainView' }" class="browse-button">
          영상 둘러보기
        </RouterLink>
      </div>

      <div v-else-if="videos && videos.length > 0" class="video-grid">
        <div
          v-for="video in videos"
          :key="video.id"
          class="video-cell fade-in"
        >
          <div @click="goToVideoDetail(video.id)" class="video-clickable-area">
            <div class="video-thumbnail-wrapper">
              <img
                :src="video.thumbnailDownloadUrl"
                :alt="video.originalFilename"
                class="video-thumbnail"
                @error="handleImageError"
              />
              <div class="video-overlay">
                <div class="play-button">
                  <svg width="24" height="24" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M8 5v14l11-7z" />
                  </svg>
                </div>
              </div>
            </div>

            <div class="video-info">
              <div class="video-header">
                <h3 class="video-title">{{ video.originalFilename }}</h3>
                <div class="button-group">
                  <button
                    @click.stop="handleEditVideo(video)"
                    class="edit-button"
                    title="영상 수정"
                  >
                    <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a.996.996 0 000-1.41l-2.34-2.34a.996.996 0 00-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
                    </svg>
                  </button>
                  <button
                    @click.stop="handleDeleteVideo(video)"
                    class="delete-button"
                    title="영상 삭제"
                  >
                    <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
                      <path
                        d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"
                      />
                    </svg>
                  </button>
                </div>
              </div>
              <div class="video-meta">
                <span class="video-author">{{ video.nickname }}</span>
                <span class="video-date">{{ formatDate(video.createdAt) }}</span>
              </div>
              <div class="video-stats">
                <span class="like-count">
                  <svg width="14" height="14" fill="currentColor" viewBox="0 0 24 24">
                    <path
                      d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                    />
                  </svg>
                  {{ video.likeCount }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loadingMore" class="loading-spinner">
        <div class="spinner"></div>
        <span class="loading-text">더 많은 영상 로딩 중...</span>
      </div>

      <div v-if="!loading && !loadingMore && videos.length > 0 && !hasMore" class="loading-text">
        모든 영상을 불러왔습니다.
      </div>
      <div ref="observerTarget" class="observer-target"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import commonApi from '@/api/commonApi';
import bg from '@/assets/pictures/stellabot/spaceBackground.avif';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const loadingMore = ref(false);
const hasMore = ref(true);
const page = ref(0);
const observerTarget = ref(null);

const INITIAL_PAGE_SIZE = 11;
const INFINITE_SCROLL_PAGE_SIZE = 12;

// myId computed - 사용자 ID 가져오기
const myId = computed(() => {
  const id = route.params.id;
  console.log('현재 사용자 ID:', id); // 디버깅용
  return id;
});

const videos = ref([]);

// 내 영상만 가져오는 함수
const fetchVideos = async (pageNum = 0) => {
  if (loadingMore.value || !hasMore.value) return;

  // myId가 유효하지 않으면 실행하지 않음
  if (!myId.value || myId.value === 'undefined' || myId.value === null) {
    console.error('유효하지 않은 사용자 ID:', myId.value);
    loading.value = false;
    return;
  }

  const pageSize = pageNum === 0 ? INITIAL_PAGE_SIZE : INFINITE_SCROLL_PAGE_SIZE;

  if (pageNum === 0) {
    loading.value = true;
  } else {
    loadingMore.value = true;
  }

  try {
    console.log('요청할 memberId:', myId.value, 'page:', pageNum, 'size:', pageSize); // 디버깅용

    // API 요청 - 올바른 엔드포인트로 내 영상만 가져오기
    const res = await commonApi.get(`profiles/${myId.value}/videos?page=${pageNum}&size=${pageSize}`);

    console.log('API 응답:', res.data); // 디버깅용

    const newVideos = res.data.data?.videos || [];

    if (pageNum === 0) {
      videos.value = newVideos;
    } else {
      videos.value.push(...newVideos);
    }

    hasMore.value = res.data.data.isLast !== true;
    page.value = pageNum + 1;

  } catch (err) {
    console.error('동영상 불러오기 실패:', err);
    hasMore.value = false;
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
};

const handleEditVideo = (video) => {
  router.push({ name: 'UpdateTagView', params: { id: video.id } });
};

// 날짜 포맷팅
const formatDate = (dateString) => {
  try {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    if (diffDays === 1) return '오늘';
    if (diffDays <= 7) return `${diffDays}일 전`;
    if (diffDays <= 30) return `${Math.ceil(diffDays / 7)}주 전`;
    if (diffDays <= 365) return `${Math.ceil(diffDays / 30)}개월 전`;
    return `${Math.ceil(diffDays / 365)}년 전`;
  } catch {
    return '날짜 불명';
  }
};

const handleImageError = (event) => {
  event.target.src = '/default-thumbnail.jpg';
};

const goToVideoDetail = (videoId) => {
  router.push(`/replay/${videoId}`);
};

const handleDeleteVideo = async (video) => {
  if (!confirm('정말로 이 영상을 삭제하시겠습니까?')) return;
  try {
    await commonApi.delete(`/videos/${video.id}`);
    videos.value = videos.value.filter(v => v.id !== video.id);
    alert('영상이 성공적으로 삭제되었습니다.');
  } catch (error) {
    console.error('영상 삭제 실패:', error);
    alert('영상 삭제에 실패했습니다. 다시 시도해주세요.');
  }
};

let observer = null;
const setupIntersectionObserver = () => {
  if (!observerTarget.value) return null;
  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0];
      if (entry.isIntersecting && hasMore.value) {
        fetchVideos(page.value);
      }
    },
    {
      root: null,
      rootMargin: '800px 0px',
      threshold: 0.1,
    }
  );
  observer.observe(observerTarget.value);
};

const preventScrollRestore = () => {
  if ('scrollRestoration' in history) {
    history.scrollRestoration = 'manual';
  }
};

// route params가 변경되면 다시 로드
watch(() => route.params.id, (newId) => {
  if (newId) {
    videos.value = [];
    page.value = 0;
    hasMore.value = true;
    fetchVideos(0);
  }
});

onMounted(async () => {
  preventScrollRestore();

  // myId가 유효한 경우에만 영상 로드
  if (myId.value && myId.value !== 'undefined' && myId.value !== null) {
    console.log('사용자 영상 로딩 시작:', myId.value);
    await fetchVideos(0);
  } else {
    console.error('유효하지 않은 사용자 ID:', myId.value);
    loading.value = false;
  }

  await nextTick();
  setupIntersectionObserver();
});

onBeforeUnmount(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-size: cover;
  background-image: linear-gradient(rgba(11, 12, 16, 0.7), rgba(11, 12, 16, 0.7));
  background-position: center;
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 auto;
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

.stars-background {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 30px 40px;
  max-width: 1200px;
  width: 100%;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow:
    inset 4px 4px 10px rgba(255 255 255 / 0.6),
    inset -4px -4px 10px rgba(0 0 0 / 0.3),
    8px 8px 30px rgba(0 0 0 / 0.4);
  overflow-x: hidden;
  overflow-y: auto;
}

.navigation-links {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
  font-size: 16px;
}

.navigation-links a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.navigation-links a:hover,
.navigation-links a.active {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
}

.navigation-links span {
  color: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
}

.stars-background h2 {
  margin-top: 20px !important;
  margin-bottom: 40px !important;
  margin-left: 10px;
  text-align: left;
  font-weight: 700;
  color: #ffffff;
  font-family: 'Pretendard', sans-serif !important;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.spinner {
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top: 4px solid #fff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1.1s linear infinite;
}

.loading-text {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
  gap: 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 10px;
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

.empty-state p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.browse-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.browse-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  max-width: 100%;
  justify-content: center;
}

.video-cell {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.video-cell:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 0.12);
}

.video-clickable-area {
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.video-thumbnail-wrapper {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.video-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.video-cell:hover .video-thumbnail {
  transform: scale(1.05);
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.video-cell:hover .video-overlay {
  opacity: 1;
}

.play-button {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.play-button:hover {
  background: #ffffff;
  transform: scale(1.1);
}

.video-info {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.video-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.video-title {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
  flex-grow: 1;
  padding-right: 10px;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.video-author {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.video-date {
  color: rgba(255, 255, 255, 0.6);
}

.video-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.like-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

.button-group {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.edit-button,
.delete-button {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.edit-button:hover {
  background: rgba(0, 255, 255, 0.2);
  transform: scale(1.1);
  color: #00ffff;
}

.delete-button {
  background: rgba(255, 0, 0, 0.1);
  color: #ff4d4f;
}

.delete-button:hover {
  background: rgba(255, 0, 0, 0.2);
  transform: scale(1.1);
}

.fade-in {
  animation: fadeIn 0.8s ease-in forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.observer-target {
  height: 10px;
  margin-top: 20px;
}
</style>
