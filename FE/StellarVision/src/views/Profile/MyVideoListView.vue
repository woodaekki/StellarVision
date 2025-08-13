<template>
  <div class="page" ref="pageRef">
    <img :src="bg" alt="" class="bg-img">
    <div class="stars-background">
      <div class="px-4 pt-12 pb-6">
        <h2 id="stars-background" class="text-2xl mb-2 text-center font-pretendard" style="font-family: 'Pretendard', sans-serif !important;">
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
            :tags="video.tags.slice(0, 3)"
            :show-edit="isUploader"
            :class="{ 'fade-in': video.isNew }"
            @select="goToReplay(video.id)"
            @delete="handleDeleteVideo"
            @animationend="onAnimationEnd(video.id)"
          />
        </div>

        <div v-else-if="!loading && !videos.length && !hasMore" class="loading-text">
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
import VideoCell from '@/components/video/VideoCell.vue';
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

.stars-background h2 {
  margin-top: 20px !important;
  margin-bottom: 40px !important;
  margin-left: 10px;
  text-align: left;
  font-weight: 700;
  color: #ffffff;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  max-width: 100%;
  justify-content: center;
}

.loading-text {
  font-size: 16px;
  text-align: center;
  margin-top: 20px;
  color: rgba(255, 255, 255, 0.7);
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
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

.delete-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(239, 68, 68, 0.9);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: opacity 0.25s ease;
  z-index: 15;
  user-select: none;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.video-cell:hover .delete-button {
  opacity: 0.95;
  border: none;
}
</style>
