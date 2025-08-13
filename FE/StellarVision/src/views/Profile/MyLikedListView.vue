<template>
  <div class="page" ref="pageRef">
    <img :src="bg" alt="" class="bg-img" />

    <div class="stars-background">
      <div class="back-button">  
        <RouterLink :to="`/profile/${userInfo?.email}`" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-pretendard">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </RouterLink>
      </div>

      <div class="px-4 pt-12 pb-6">
        <div class="navigation-links">
          <RouterLink :to="{ name: 'MyVideoListView', params: { id: myId } }" >
            내 비디오
          </RouterLink>
          <span>|</span>
          <RouterLink :to="{ name: 'MyLikedListView', params: { id: myId } }" class="active" >
            좋아요한 영상
          </RouterLink>
        </div>

        <h2 class="text-2xl mb-2 text-center font-pretendard">
          My Liked Video
        </h2>
        <hr class="border-t-2 border-neutral-200 w-full mt-2" />
      </div>

      <div v-if="isLoading" class="loading-container">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <span class="loading-text">좋아요한 영상을 불러오는 중...</span>
        </div>
      </div>

      <div v-else-if="!isLoading && (!likedVideos || likedVideos.length === 0)" class="empty-state">
        <div class="empty-icon">⭐</div>
        <h3>좋아요한 영상이 없습니다</h3>
        <p>마음에 드는 영상에 좋아요를 눌러보세요!</p>
        <RouterLink :to="{ name: 'MainView' }" class="browse-button">
          영상 둘러보기
        </RouterLink>
      </div>

      <div v-else-if="likedVideos && likedVideos.length > 0" class="video-grid">
        <div
          v-for="video in likedVideos"
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
                  <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
                    <path
                      d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
                    />
                  </svg>
                </div>
              </div>
            </div>

            <div class="video-info">
              <div class="video-header">
                <h3 class="video-title">{{ video.originalFilename }}</h3>
                <button
                  @click.stop="handleUnlike(video.id)"
                  class="unlike-button"
                  title="좋아요 취소"
                >
                  <svg width="16" height="16" fill="#FFD700" viewBox="0 0 24 24">
                    <path
                      d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
                    />
                  </svg>
                </button>
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStreamingStore } from '@/stores/streaming.js';
import commonApi from '@/api/commonApi';
import bg from '@/assets/pictures/stellabot/spaceBackground.avif';

const route = useRoute();
const router = useRouter();
const streamingStore = useStreamingStore();
const memberEmail = computed(() => route.params.id);

const pageRef = ref(null);
const allVideos = ref([]);
const likedVideos = ref([]);
const isLoading = ref(false);

const myId = computed(() => {
  const id = route.params.id;
  return id;
});

const fetchUserVideos = async (userId) => {
  try {
    isLoading.value = true;
    const res = await commonApi.get(`/videos/search?userId=${userId}`);
    const videos = res.data.data?.videos || [];
    allVideos.value = videos;
    return videos;
  } catch (err) {
    allVideos.value = [];
    return [];
  } finally {
    isLoading.value = false;
  }
};

const filterLikedVideos = (videos) => {
  const filtered = videos.filter(video => video.liked === true);
  likedVideos.value = filtered;
  return filtered;
};

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
  router.push({ name: 'MyVideoListView', params: { id: videoId } });
};

const handleUnlike = async (videoId) => {
  try {
    const res = await commonApi.delete(`/videos/${videoId}/likes`);
    if (res.status === 200) {
      const originalVideo = allVideos.value.find(v => v.id === videoId);
      if (originalVideo) {
        originalVideo.liked = false;
      }
      likedVideos.value = likedVideos.value.filter(v => v.id !== videoId);
    }
  } catch (error) {
    alert('좋아요 취소 중 오류가 발생했습니다.');
  }
};

onMounted(async () => {
  if (myId.value) {
    const videos = await fetchUserVideos(myId.value);
    filterLikedVideos(videos);
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

.back-button {
  position: absolute;
  top: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
  backdrop-filter: blur(8px);
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
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
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.navigation-links a.active {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
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
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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

.unlike-button {
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
  margin-left: auto;
}

.unlike-button:hover {
  background: rgba(255, 255, 255, 0.2);
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

@media (max-width: 1024px) {
  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
  .stars-background {
    padding: 20px 24px;
  }
  .navigation-links {
    font-size: 14px;
    gap: 10px;
  }
}

@media (max-width: 480px) {
  .video-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>