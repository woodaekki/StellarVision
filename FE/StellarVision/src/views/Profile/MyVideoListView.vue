<template>
  <div class="page" ref="pageRef">
    <img :src="bg" alt="" class="bg-img">
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

        <!-- 더 이상 로드할 영상이 있을 때만 스크롤 문구 표시 -->
        <div v-if="!loading && !loadingMore && hasMore && videos.length > 0" class="loading-text">
          스크롤하여 더 많은 영상 보기
        </div>

        <!-- 디버깅용: 현재 상태 표시 -->
        <div class="debug-info" style="color: yellow; font-size: 12px; text-align: center;">
          loading: {{ loading }}, loadingMore: {{ loadingMore }}, hasMore: {{ hasMore }}, videos: {{ videos.length }}
        </div>

        <!-- Intersection Observer 감지 요소 -->
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
import bg from '@/assets/pictures/stellabot/spaceBackground.avif'

const route = useRoute();
const router = useRouter();
const videoStore = useVideoStore();

const loading = ref(true);
const loadingMore = ref(false);
const hasMore = ref(true);
const page = ref(0);
const pageRef = ref(null);
const observerTarget = ref(null);

// 개선된 스크롤 관련 상태
const lastFetchTime = ref(0);
const fetchCooldown = 150;
const isNearBottom = ref(false);
const lastScrollTop = ref(0);
const scrollVelocity = ref(0);
const isThrottling = ref(false);

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

  // 스로틀링 체크 (강제 호출이 아닌 경우)
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

    // 디버깅용 로그 추가
    console.log('API 응답:', data);
    console.log('data.isLast:', data.isLast);
    console.log('새로 로드된 비디오 수:', newVideos.length);

    // 단순한 로직: 새 비디오가 없으면 더 이상 로드하지 않음
    if (newVideos.length === 0) {
      hasMore.value = false;
    } else if (data.isLast === true) {
      hasMore.value = false;
    } else {
      // 새 비디오가 있고 isLast가 true가 아니면 계속 로드
      hasMore.value = true;
    }

    console.log('hasMore 업데이트됨:', hasMore.value);

    page.value++;
  } catch (err) {
    console.error('동영상 불러오기 실패:', err);
  } finally {
    loading.value = false;
    loadingMore.value = false;
    // 스로틀링 해제 지연
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

// 개선된 스크롤 메트릭 계산 (헤더/푸터 고려)
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

  // 헤더/푸터 오프셋 고려
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

// 개선된 스크롤 위치 체크
const checkScrollPosition = () => {
  if (loading.value || loadingMore.value || !hasMore.value || isThrottling.value) return false;

  const { scrollTop, scrollHeight, clientHeight, effectiveScrollHeight } = getScrollMetrics();

  // 컨텐츠가 화면보다 작으면 바로 로드
  if (scrollHeight <= clientHeight + 200) {
    return true;
  }

  const scrollPercentage = (scrollTop + clientHeight) / effectiveScrollHeight;
  const remaining = effectiveScrollHeight - scrollTop - clientHeight;

  // 더 관대한 조건으로 변경
  const shouldLoad = scrollPercentage >= 0.7 || remaining <= 1200;

  return shouldLoad;
};

// 스크롤 속도 기반 예측 로딩
const updateScrollVelocity = () => {
  const { scrollTop } = getScrollMetrics();
  const velocity = Math.abs(scrollTop - lastScrollTop.value);
  scrollVelocity.value = velocity;
  lastScrollTop.value = scrollTop;

  // 빠른 스크롤 시 미리 로드
  if (velocity > 50 && checkScrollPosition()) {
    fetchVideos();
  }
};

let scrollTimeout = null;
let velocityTimeout = null;
let rafId = null;
let debounceTimeout = null;

// 개선된 스크롤 핸들러
const handleScroll = () => {
  // 기존 타이머들 정리
  if (scrollTimeout) clearTimeout(scrollTimeout);
  if (velocityTimeout) clearTimeout(velocityTimeout);
  if (rafId) cancelAnimationFrame(rafId);
  if (debounceTimeout) clearTimeout(debounceTimeout);

  // 즉시 실행 (requestAnimationFrame 사용)
  rafId = requestAnimationFrame(() => {
    updateScrollVelocity();

    if (checkScrollPosition()) {
      fetchVideos();
    }
  });

  // 빠른 체크 (50ms)
  scrollTimeout = setTimeout(() => {
    if (checkScrollPosition()) {
      fetchVideos();
    }
  }, 50);

  // 속도 업데이트 (16ms - 60fps)
  velocityTimeout = setTimeout(updateScrollVelocity, 16);

  // 디바운스 체크 (200ms)
  debounceTimeout = setTimeout(() => {
    if (checkScrollPosition()) {
      fetchVideos();
    }
  }, 200);
};

// 개선된 Intersection Observer 설정
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
      rootMargin: '800px 0px 800px 0px', // 더 큰 마진
      threshold: [0, 0.1, 0.25, 0.5]
    }
  );
};

let observer = null;

const setupInfiniteScroll = async () => {
  await nextTick();

  // Observer 타겟 확인 및 재시도
  let retryCount = 0;
  while (!observerTarget.value && retryCount < 10) {
    await new Promise(resolve => setTimeout(resolve, 100));
    retryCount++;
  }

  if (!observerTarget.value) {
    console.warn('Observer target not found after retries');
    return;
  }

  // Intersection Observer 설정
  observer = setupIntersectionObserver();
  if (observer) {
    observer.observe(observerTarget.value);
  }

  // 스크롤 이벤트 리스너 추가 (passive 옵션으로 성능 향상)
  const scrollOptions = { passive: true, capture: false };
  window.addEventListener('scroll', handleScroll, scrollOptions);
  document.addEventListener('scroll', handleScroll, scrollOptions);

  // 리사이즈 이벤트
  window.addEventListener('resize', () => {
    setTimeout(() => {
      if (checkScrollPosition()) {
        fetchVideos();
      }
    }, 100);
  }, { passive: true });

  // 전체화면 변경 이벤트
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

  // 브라우저 뒤로가기/앞으로가기 처리
  window.addEventListener('popstate', () => {
    setTimeout(() => {
      if (checkScrollPosition()) {
        fetchVideos();
      }
    }, 100);
  });
};

const cleanupInfiniteScroll = () => {
  // Observer 정리
  if (observer) {
    observer.disconnect();
    observer = null;
  }

  // 타이머들 정리
  const timers = [scrollTimeout, velocityTimeout, rafId, debounceTimeout];
  timers.forEach(timer => {
    if (timer) {
      if (timer === rafId) {
        cancelAnimationFrame(timer);
      } else {
        clearTimeout(timer);
      }
    }
  });

  // 변수 초기화
  scrollTimeout = null;
  velocityTimeout = null;
  rafId = null;
  debounceTimeout = null;

  // 이벤트 리스너 제거
  window.removeEventListener('scroll', handleScroll);
  document.removeEventListener('scroll', handleScroll);
  window.removeEventListener('resize', checkScrollPosition);
  window.removeEventListener('popstate', checkScrollPosition);

  const fullscreenEvents = ['fullscreenchange', 'webkitfullscreenchange', 'mozfullscreenchange'];
  fullscreenEvents.forEach(event => {
    document.removeEventListener(event, checkScrollPosition);
  });

  // 상태 초기화
  isNearBottom.value = false;
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

  // 초기 상태 체크를 더 자주, 더 오래 실행
  let checkCount = 0;
  initialCheckInterval = setInterval(() => {
    checkCount++;
    if (checkScrollPosition()) {
      fetchVideos();
      if (checkCount > 3) { // 몇 번 성공하면 중단
        clearInterval(initialCheckInterval);
      }
    }

    // 최대 30초 후 중단
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
    inset -4px -4px 10px rgba(0 0 0 / 0.15);
  border: 1.5px solid rgba(255 255 255 / 0.25);
  transition: box-shadow 0.3s ease;
}

.stars-background:hover {
  box-shadow:
    inset 6px 6px 14px rgba(255 255 255 / 0.85),
    inset -6px -6px 14px rgba(0 0 0 / 0.2);
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
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  /* 영상 비율 문제 해결을 위한 스타일 추가 */
}

/* VideoCell 컴포넌트가 자체적으로 aspect-ratio를 관리하므로 제거 */
.video-grid > * {
  width: 100%;
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
  margin: 2.5rem 0;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 4px solid rgba(255, 255, 255, 0.15);
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1.2s linear infinite;
  margin-bottom: 1.2rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  text-align: center;
  margin-top: 2rem;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 600;
  user-select: none;
}

/* 개선된 Observer 타겟 - 더 큰 감지 영역 */
.observer-target {
  height: 200px; /* 더 큰 감지 영역 */
  width: 100%;
  margin: 40px 0;
  /* 디버깅용 (배포시 제거 가능) */
  /* background: rgba(255, 0, 0, 0.1); */
  /* border: 1px dashed rgba(255, 255, 255, 0.3); */
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .observer-target {
    height: 150px;
    margin: 30px 0;
  }
}

@media (max-width: 480px) {
  .video-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
}

/* 전체화면 모드 최적화 */
:fullscreen .page,
:-webkit-full-screen .page,
:-moz-full-screen .page {
  padding: 20px 10px;
}

:fullscreen .stars-background,
:-webkit-full-screen .stars-background,
:-moz-full-screen .stars-background {
  max-width: 95vw;
  padding: 20px 30px;
}

/* 스크롤 성능 최적화 */
* {
  scroll-behavior: smooth;
}

.page {
  will-change: scroll-position;
}

/* VideoCell에 적용될 스타일 (VideoCell 컴포넌트에 추가 가능) */
.video-grid > * {
  will-change: transform;
  contain: layout style;
}
</style>
