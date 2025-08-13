<template>
  <div class="page" ref="pageRef">
    <img :src="bg" alt="" class="bg-img">

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
        <h2 id="stars-background" class="text-2xl mb-2 text-center font-pretendard">
          My Space Video
        </h2>
        <hr class="border-t-2 border-neutral-200 w-full mt-2" />
      </div>

      <div class="px-4 pb-12">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner">
            <div class="spinner"></div>
            <div class="loading-text">영상 목록을 불러오는 중...</div>
          </div>
        </div>

        <div v-else-if="!loading && videos.length === 0" class="empty-state">
          <div class="empty-icon">📂</div>
          <h3>아직 업로드한 영상이 없습니다.</h3>
          <p>새로운 영상을 업로드하고 공유해 보세요!</p>
          <RouterLink :to="{ name: 'ReplayView' }" class="browse-button">
            영상 업로드하기
          </RouterLink>
        </div>

        <div v-else class="video-grid">
          <div
            v-for="(video, index) in videos"
            :key="video.id"
            class="video-cell fade-in"
            @animationend="onAnimationEnd(video.id)"
          >
            <div @click="goToReplay(video.id)" class="video-clickable-area">
              <div class="video-thumbnail-wrapper">
                <img
                  :src="video.thumbnail"
                  :alt="video.name"
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
                  <h3 class="video-title">{{ video.name }}</h3>
                  <button v-if="isUploader" @click.stop="handleDeleteVideo(video)" class="delete-button" title="삭제">
                    <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm2.46-7.12l1.41-1.41L12 12.59l2.12-2.12 1.41 1.41L13.41 14l2.12 2.12-1.41 1.41L12 15.41l-2.12 2.12-1.41-1.41L10.59 14l-2.13-2.12zM15.5 4l-1-1h-5l-1 1H5v2h14V4z" />
                    </svg>
                  </button>
                </div>
                <div class="video-meta">
                  <span class="video-date">{{ video.date }}</span>
                </div>
                <div class="video-stats">
                  <div class="tags-container">
                    <span v-for="tag in video.tags" :key="tag.id" class="video-tag">
                      #{{ tag.name }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="loadingMore" class="loading-spinner">
          <div class="spinner"></div>
          <div class="loading-text">더 많은 영상 로딩 중...</div>
        </div>

        <div v-if="!loading && !loadingMore && hasMore && videos.length > 0" class="loading-text">
          스크롤하여 더 많은 영상 보기
        </div>

        <div ref="observerTarget" class="observer-target"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useVideoStore } from '@/stores/video';
import commonApi from '@/api/commonApi';
import bg from '@/assets/pictures/stellabot/spaceBackground.avif';

const route = useRoute();
const router = useRouter();
const videoStore = useVideoStore();

const loading = ref(true);
const loadingMore = ref(false);
const hasMore = ref(true);
const page = ref(0);
const pageRef = ref(null);
const observerTarget = ref(null);

const lastFetchTime = ref(0);
const fetchCooldown = 150;
const isThrottling = ref(false);

const PAGE_SIZE = 8;
const SORT_OPTION = 0;

const myId = computed(() => route.params.id);
const profilePk = window.history.state?.profilePk || route.params.id;
const isUploader = JSON.parse(localStorage.getItem('userInfo'))?.email === route.params.id;

const videos = computed(() =>
  videoStore.replays.map(v => ({
    id: v.id,
    name: v.originalFilename,
    thumbnail: v.thumbnailDownloadUrl,
    date: v.createdAt?.split('T')[0],
    tags: (v.tags || []).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 3),
    isNew: v.isNew || false
  }))
);

const onAnimationEnd = (videoId) => {
  const video = videoStore.replays.find(v => v.id === videoId);
  if (video) video.isNew = false;
};

const fetchVideos = async (force = false) => {
  const now = Date.now();

  if (page.value > 0 && (loading.value || loadingMore.value || !hasMore.value)) return;

  if (!force && (isThrottling.value || (now - lastFetchTime.value < fetchCooldown))) {
    return;
  }

  lastFetchTime.value = now;
  isThrottling.value = true;

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
    newVideos.forEach(v => v.isNew = page.value > 0);

    if (newVideos.length === 0 || data.isLast === true) {
      hasMore.value = false;
    } else {
      hasMore.value = true;
    }

    page.value++;
  } catch (err) {
    console.error('동영상 불러오기 실패:', err);
  } finally {
    loading.value = false;
    loadingMore.value = false;
    setTimeout(() => {
      isThrottling.value = false;
    }, fetchCooldown);
  }
};

const goToReplay = (videoId) => {
  router.push(`/replay/${videoId}`);
};

const handleDeleteVideo = async (video) => {
  if (!confirm('정말로 이 영상을 삭제하시겠습니까?')) return;
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

const handleImageError = (event) => {
  event.target.src = '/default-thumbnail.jpg';
};

const getScrollMetrics = () => {
  const scrollElement = document.documentElement;
  const scrollTop = Math.max(
    scrollElement.scrollTop,
    document.body.scrollTop,
    window.pageYOffset || 0
  );
  const scrollHeight = Math.max(
    scrollElement.scrollHeight,
    document.body.scrollHeight
  );
  const clientHeight = window.innerHeight || document.documentElement.clientHeight;
  const headerOffset = 60;
  const footerOffset = 40;
  const totalOffset = headerOffset + footerOffset;
  return {
    scrollTop,
    scrollHeight,
    clientHeight: clientHeight - totalOffset,
    effectiveScrollHeight: scrollHeight - totalOffset
  };
};

const checkScrollPosition = () => {
  if (loading.value || loadingMore.value || !hasMore.value || isThrottling.value) return false;
  const { scrollTop, scrollHeight, clientHeight, effectiveScrollHeight } = getScrollMetrics();
  if (scrollHeight <= clientHeight + 200) {
    return true;
  }
  const scrollPercentage = (scrollTop + clientHeight) / effectiveScrollHeight;
  const remaining = effectiveScrollHeight - scrollTop - clientHeight;
  return scrollPercentage >= 0.7 || remaining <= 1200;
};

let scrollTimeout = null;
let rafId = null;
let debounceTimeout = null;

const handleScroll = () => {
  if (scrollTimeout) clearTimeout(scrollTimeout);
  if (rafId) cancelAnimationFrame(rafId);
  if (debounceTimeout) clearTimeout(debounceTimeout);
  rafId = requestAnimationFrame(() => {
    if (checkScrollPosition()) {
      fetchVideos();
    }
  });
  scrollTimeout = setTimeout(() => {
    if (checkScrollPosition()) {
      fetchVideos();
    }
  }, 50);
  debounceTimeout = setTimeout(() => {
    if (checkScrollPosition()) {
      fetchVideos();
    }
  }, 200);
};

const setupIntersectionObserver = () => {
  if (!observerTarget.value) return null;
  return new IntersectionObserver(
    (entries) => {
      const entry = entries[0];
      if (entry.isIntersecting && entry.intersectionRatio > 0) {
        if (!loading.value && !loadingMore.value && hasMore.value && !isThrottling.value) {
          fetchVideos();
        }
      }
    },
    {
      root: null,
      rootMargin: '800px 0px 800px 0px',
      threshold: [0, 0.1, 0.25, 0.5]
    }
  );
};

let observer = null;

const setupInfiniteScroll = async () => {
  await nextTick();
  let retryCount = 0;
  while (!observerTarget.value && retryCount < 10) {
    await new Promise(resolve => setTimeout(resolve, 100));
    retryCount++;
  }
  if (!observerTarget.value) {
    return;
  }
  observer = setupIntersectionObserver();
  if (observer) {
    observer.observe(observerTarget.value);
  }
  const scrollOptions = { passive: true, capture: false };
  window.addEventListener('scroll', handleScroll, scrollOptions);
  document.addEventListener('scroll', handleScroll, scrollOptions);
  window.addEventListener('resize', () => {
    setTimeout(() => {
      if (checkScrollPosition()) {
        fetchVideos();
      }
    }, 100);
  }, { passive: true });
  const fullscreenEvents = ['fullscreenchange', 'webkitfullscreenchange', 'mozfullscreenchange'];
  fullscreenEvents.forEach(event => {
    document.addEventListener(event, () => {
      setTimeout(() => {
        if (checkScrollPosition()) {
          fetchVideos();
        }
      }, 300);
    });
  });
  window.addEventListener('popstate', () => {
    setTimeout(() => {
      if (checkScrollPosition()) {
        fetchVideos();
      }
    }, 100);
  });
};

const cleanupInfiniteScroll = () => {
  if (observer) {
    observer.disconnect();
    observer = null;
  }
  const timers = [scrollTimeout, rafId, debounceTimeout];
  timers.forEach(timer => {
    if (timer) {
      if (timer === rafId) {
        cancelAnimationFrame(timer);
      } else {
        clearTimeout(timer);
      }
    }
  });
  scrollTimeout = null;
  rafId = null;
  debounceTimeout = null;
  window.removeEventListener('scroll', handleScroll);
  document.removeEventListener('scroll', handleScroll);
  window.removeEventListener('resize', checkScrollPosition);
  window.removeEventListener('popstate', checkScrollPosition);
  const fullscreenEvents = ['fullscreenchange', 'webkitfullscreenchange', 'mozfullscreenchange'];
  fullscreenEvents.forEach(event => {
    document.removeEventListener(event, checkScrollPosition);
  });
  isThrottling.value = false;
};

const preventScrollRestore = () => {
  if ('scrollRestoration' in history) {
    history.scrollRestoration = 'manual';
  }
};

let initialCheckInterval = null;

onMounted(async () => {
  preventScrollRestore();
  await fetchVideos(true);
  await setupInfiniteScroll();
  let checkCount = 0;
  initialCheckInterval = setInterval(() => {
    checkCount++;
    if (checkScrollPosition()) {
      fetchVideos();
      if (checkCount > 3) {
        clearInterval(initialCheckInterval);
      }
    }
    if (checkCount > 150) {
      clearInterval(initialCheckInterval);
    }
  }, 200);
});

onBeforeUnmount(() => {
  cleanupInfiniteScroll();
  if (initialCheckInterval) {
    clearInterval(initialCheckInterval);
  }
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-size: cover;
  background-image: linear-gradient(rgba(11, 12, 16, 0.7), rgba(11, 12, 16, 0.7));
  background-size: cover;
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
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.loading-text {
  font-size: 16px;
  text-align: center;
  margin-top: 20px;
  color: rgba(255, 255, 255, 0.7);
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.spinner {
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top: 4px solid #fff;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1.1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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

.delete-button {
  background-color: rgba(239, 68, 68, 0.9);
  color: white;
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
  opacity: 0;
}

.video-cell:hover .delete-button {
  opacity: 1;
  transform: scale(1.1);
}

.video-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.video-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.video-tag {
  font-size: 12px;
  color: #4ade80;
  background: rgba(76, 175, 80, 0.15);
  padding: 4px 8px;
  border-radius: 8px;
  white-space: nowrap;
}

.observer-target {
  width: 100%;
  height: 200px;
}

.fade-in {
  animation: fadeIn 0.8s ease-in forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
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
  .page {
    padding: 20px 10px;
  }
}
</style>
