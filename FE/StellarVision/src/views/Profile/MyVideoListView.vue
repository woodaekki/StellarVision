<template>
  <div class="page" ref="pageRef">
    <img :src="bg" alt="" class="bg-img" />

    <div class="stars-background">
        <div class="back-button">
          <!-- 뒤로가기 버튼 -->
          <button @click="goBackToProfile" class="back-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15 18l-6-6 6-6" />
            </svg>
          </button>
        </div>

      <div class="px-4 pt-12 pb-6">
        <!-- 본인 프로필일 때만 네비게이션 링크 표시 -->
        <div v-if="canEditDelete" class="navigation-links">
          <RouterLink :to="{ name: 'MyVideoListView', params: { id: userInfo?.email } }" class="active">
            아카이브
          </RouterLink>
          <span>|</span>
          <RouterLink :to="{ name: 'MyLikedListView', params: { id: userInfo?.email } }">
            즐겨찾기
          </RouterLink>
        </div>

        <h2 class="text-2xl mb-2 text-center font-pretendard">
          은하 영상관
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
        <h3>{{ canEditDelete ? '아직 업로드한 영상이 없습니다.' : '업로드된 영상이 없습니다.' }}</h3>
        <p>{{ canEditDelete ? '나만의 멋진 영상을 업로드해보세요!' : '이 사용자는 아직 영상을 업로드하지 않았습니다.' }}</p>
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
                :src="getVideoThumbnail(video)"
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
                <!-- 편집/삭제 권한이 있을 때만 버튼 표시 -->
                <div v-if="canEditDelete" class="button-group">
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
                <div v-if="video.tags && video.tags.length > 0" class="video-tags">
                  <span v-for="tag in video.tags.slice(0, 3)" :key="tag.tagId" class="tag-item">
                    #{{ tag.tagName }}
                  </span>
                  <span v-if="video.tags.length > 3" class="tag-count">
                    +{{ video.tags.length - 3 }}
                  </span>
                </div>
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
import axiosApi from '@/api/axiosApi';
import bg from '@/assets/pictures/stellabot/spaceBackground.avif';
import defaultBg from '@/assets/pictures/stellabot/novaStar2.png';
import { useAccountStore } from '@/stores/account';

const route = useRoute();
const router = useRouter();
const accountStore = useAccountStore();
const userInfo = computed(() => accountStore.userInfo)
const loading = ref(true);
const loadingMore = ref(false);
const hasMore = ref(true);
const page = ref(0);
const observerTarget = ref(null);

const INITIAL_PAGE_SIZE = 11;
const INFINITE_SCROLL_PAGE_SIZE = 12;

// URL에서 가져온 이메일로 현재 보고 있는 프로필의 소유자 체크
const memberEmail = computed(() => route.params.id);
// history state에서 가져온 memberId 
const memberId = ref(window.history.state?.profilePk);
// 실제 프로필 소유자의 정보 
const profileOwner = ref(null);

// 뒤로가기 
const goBackToProfile = () => {
  router.push({
    path: `/profile/${memberEmail.value}`,
    state: { profilePk: memberId.value }
  })
}

// 편집/삭제 권한 체크 
const canEditDelete = computed(() => {
  // 로그인하지 않은 경우
  if (!accountStore.isLogin) {
    return false;
  }

  // 내 프로필 정보가 없는 경우
  if (!accountStore.myProfile) {
    return false;
  }

  // 이메일로 비교 
  const emailMatch = accountStore.myProfile?.email === memberEmail.value;
  
  // memberId로 두 번째 비교 
  const idMatch = memberId.value && accountStore.myProfile?.memberId === memberId.value;
  
  // profileOwner와 마지막으로 비교 
  const ownerMatch = profileOwner.value && accountStore.myProfile?.memberId === profileOwner.value.memberId;
  
  const result = emailMatch || idMatch || ownerMatch;
  
  return result;
});

// 프로필 소유자 정보를 가져오는 함수
const fetchProfileOwner = async () => {
  try {
    // 이메일로 프로필 정보 조회
    const response = await axiosApi.get(`/profiles/email/${memberEmail.value}`);
    profileOwner.value = response.data.data;
    
    // memberId가 없었다면 여기서 설정
    if (!memberId.value && profileOwner.value) {
      memberId.value = profileOwner.value.memberId;
    }
  } catch (error) {
    console.error('프로필 소유자 정보 가져오기 실패:', error);
    // 이메일이 잘못되었거나 사용자가 존재하지 않는 경우
  }
};

// 별자리 썸네일 자동 매핑 
let STAR_IMAGES = {};
let STAR_BY_KEY = {};

// 안전하게 이미지 로드
try {
  STAR_IMAGES = import.meta.glob('@/assets/pictures/stars/*.{png,jpg,jpeg,webp}', {
    eager: true,
    import: 'default'
  });
  
  // 파일명 기준으로 매핑 테이블 구성
  for (const path in STAR_IMAGES) {
    const filename = path.split('/').pop().replace(/\.(png|jpg|jpeg|webp)$/i, '');
    const normalizedKey = normalizeKoConstellation(filename);
    STAR_BY_KEY[normalizedKey] = STAR_IMAGES[path];
  }
  
} catch (error) {
  console.error('별자리 이미지 로드 실패:', error);
  STAR_IMAGES = {};
  STAR_BY_KEY = {};
}

// 태그 없을 시 기본 썸네일
const defaultImg = defaultBg;

// 공백제거, 소문자화, xx자리에서 '자리'를 삭제
function normalizeKoConstellation(s) {
  if (!s || typeof s !== 'string') return '';
  const normalized = s.replace(/\s+/g, '').replace(/자리$/u, '').toLowerCase();
  return normalized;
}

// 별자리 딕셔너리 
const ALIASES = {
  
};

function pickStarThumbByTags(tagList, fallback) {
  if (!tagList || !Array.isArray(tagList) || tagList.length === 0) {
    return fallback;
  }

  for (const t of tagList) {
    const raw = typeof t === 'string' ? t : (t.tagName || '');
    if (!raw) continue;
    
    let key = normalizeKoConstellation(raw);
    if (ALIASES[key]) {
      key = ALIASES[key];
    }

    // STAR_BY_KEY에서 해당 키로 이미지 찾기
    if (STAR_BY_KEY[key]) {
      console.log(`태그 "${raw}"에 대해 별자리 이미지 발견: ${key}`);
      return STAR_BY_KEY[key];
    }
  }
  return fallback;
}

// 비디오 썸네일 결정하는 함수 
const getVideoThumbnail = (video) => {
  
  // 태그가 있는 경우 별자리 이미지 사용
  if (video.tags && video.tags.length > 0) {
    const starThumbnail = pickStarThumbByTags(video.tags, null);
    if (starThumbnail) {
      return starThumbnail;
    }
  }
  return defaultImg;
};

const videos = ref([]);

const fetchTagsForVideos = async (videosList) => {
  if (!videosList || videosList.length === 0) return videosList;

  const tagPromises = videosList.map(async (video) => {
    try {
      const res = await commonApi.get(`/videos/${video.id}/tags`);
      return { ...video, tags: res.data.data?.tags || [] };
    } catch (err) {
      console.error(`비디오 ${video.id}의 태그를 불러오는 데 실패했습니다:`, err);
      return { ...video, tags: [] };
    }
  });

  return Promise.all(tagPromises);
};

const fetchVideos = async (pageNum = 0) => {
  if (loadingMore.value || !hasMore.value) return;

  // memberId가 없으면 fetchVideos 실행하지 않음
  if (!memberId.value || memberId.value === 'undefined' || memberId.value === null) {
    console.log('memberId가 없어서 fetchVideos 중단:', memberId.value);
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
    console.log('요청할 memberId:', memberId.value, 'page:', pageNum, 'size:', pageSize);
    const res = await commonApi.get(`profiles/${memberId.value}/videos?page=${pageNum}&size=${pageSize}`);
    console.log('API 응답:', res.data);

    let newVideos = res.data.data?.videos || [];
    newVideos = await fetchTagsForVideos(newVideos);

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
  if (!canEditDelete.value) {
    alert('편집 권한이 없습니다. 본인 프로필에서만 영상을 편집할 수 있습니다.');
    return;
  }
  
  router.push({ name: 'UpdateTagView', params: { id: video.id } });
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
  // 이미지 로드 실패 시 기본 이미지로 대체
  console.log('기본 이미지로 대체');
  event.target.src = defaultImg;
};

const goToVideoDetail = (videoId) => {
  router.push(`/replay/${videoId}`);
};

const handleDeleteVideo = async (video) => {
  if (!canEditDelete.value) {
    alert('삭제 권한이 없습니다. 본인 프로필에서만 영상을 삭제할 수 있습니다.');
    return;
  }
  
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
  await accountStore.fetchMyProfile();
  // 프로필 소유자 정보 가져오는 부분 
  await fetchProfileOwner();
  if (memberId.value && memberId.value !== 'undefined' && memberId.value !== null) {
    await fetchVideos(0);
  } else {
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

.video-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.tag-item {
  background-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 500;
  backdrop-filter: blur(5px);
}

.tag-count {
  background-color: rgba(255, 255, 255, 0.3);
  color: #ffffff;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 600;
  backdrop-filter: blur(5px);
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