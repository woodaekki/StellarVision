<template>
  <div class="page" ref="pageRef">
    <img :src="bg" alt="우주 배경" class="bg-img" />
    <div class="stars-background">
      <div class="header-and-back-container">
        <div class="back-button-container">
          <div class="back-button-icon">
            <RouterLink :to="`/profile/${memberEmail}`" class="no-underline relative after:content-[''] after:absolute after:bottom-0 after:left-0 after:h-[2px] after:bg-[#f2f2f2] after:w-0 after:transition-all after:duration-300 hover:after:w-full font-pretendard">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M15 18l-6-6 6-6" />
              </svg>
            </RouterLink>
          </div>
        </div>
        <div class="header-container">
          <h2 class="page-title">은하 사진관</h2>
        </div>
      </div>
      <hr class="divider" />
      <div class="content-container">
        <div class="gallery-grid">
          <div class="upload-box" @click="triggerGalleryUpload">
            <span>+</span>
            <input
              type="file"
              ref="galleryInput"
              @change="uploadGalleryImage"
              accept="image/*"
              class="hidden"
            />
          </div>

          <div
            v-for="item in photos"
            :key="item.id"
            class="photo-box"
            @click="viewPhoto(item.id)"
            @mouseenter="loadPhotoTags(item.id)"
          >
            <img
              :src="item.url"
              class="photo-img"
              :class="{ loaded: item.isLoaded }"
              @load="onImageLoad(item.id)"
              @error="handleImageError"
            />
            <div class="photo-text">
              <p class="photo-title">{{ item.name }}</p>
              <p class="photo-date">{{ item.date }}</p>
              <div v-if="item.tags && item.tags.length" class="photo-tags">
                <span v-for="tag in item.tags.slice(0, 3)" :key="tag.tagId" class="tag-chip">
                  #{{ tag.tagName }}
                </span>
                <span v-if="item.tags.length > 3" class="tag-chip tag-more" @click.stop="openTagModal(item.tags, item.name)">
                  +{{ item.tags.length - 3 }}
                </span>
              </div>
              <div v-else-if="loadingTags[item.id]" class="photo-tags">
                <span class="tag-loading-text">태그 로딩중...</span>
              </div>
            </div>
            <button class="delete-button" @click.stop="deletePhoto(item.id)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                <path
                  d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"
                />
                <path
                  fill-rule="evenodd"
                  d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"
                />
              </svg>
            </button>
          </div>
        </div>

        <div v-if="loading" class="loading-spinner-container">
          <div class="spinner"></div>
          <div class="loading-text">사진 불러오는 중...</div>
        </div>
        <div v-if="!photos.length && !loading && !hasMore" class="loading-text">아직 사진이 없습니다.</div>

        <div ref="observerTarget" class="observer-target"></div>
      </div>
    </div>

    <div v-if="isBadgeModalVisible" class="modal-backdrop">
      <div class="modal-content">
        <div class="stars"></div>
        <div class="stars2"></div>
        <div class="stars3"></div>
        <div class="badge-title-container">
          <img :src="icon" alt="획득한 뱃지 이미지" class="badge-image"/>
          <h2 class="title">별조각을 찾았어요!</h2>
        </div>
        <p class="message">새로운 뱃지를 획득하셨습니다.<br>은하 수집관에서 확인해보세요!</p>
        <button @click="isBadgeModalVisible = false" class="close-button">확인</button>
      </div>
    </div>

    <div v-if="isTagModalVisible" class="modal-backdrop" @click="closeTagModal">
      <div class="tag-modal-content" @click.stop>
        <h3 class="tag-modal-title">{{ currentPhotoName }}의 모든 태그</h3>
        <div class="tag-list-container">
          <span v-for="tag in currentTags" :key="tag.tagId" class="tag-chip-full">
            #{{ tag.tagName }}
          </span>
        </div>
        <button @click="closeTagModal" class="tag-modal-close-button">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useAccountStore } from '@/stores/account';
import { useBadgeStore } from '@/stores/badge';
import axios from 'axios';
import axiosApi from '@/api/axiosApi';
import bg from '@/assets/pictures/stellabot/spaceBackground.avif';
import { useRouter, useRoute } from 'vue-router'
import icon from '@/assets/pictures/stellabot/nova.png';
const accountStore = useAccountStore();
const badgeStore = useBadgeStore();
const route = useRoute();
const pageRef = ref(null);
const galleryInput = ref(null);
const observerTarget = ref(null);
const isBadgeModalVisible = ref(false);

const photos = ref([]);
const loading = ref(false);
const page = ref(0);
const hasMore = ref(true);
const isScrolling = ref(false);
const loadingTags = ref({});
const lastFetchTime = ref(0);
const fetchCooldown = 150;
const isNearBottom = ref(false);
const lastScrollTop = ref(0);
const scrollVelocity = ref(0);
const isThrottling = ref(false);
const memberEmail = computed(() => route.params.id);
const memberId = ref(window.history.state?.profilePk);
const canUpload = computed(() => accountStore.isLogin);

const isTagModalVisible = ref(false);
const currentTags = ref([]);
const currentPhotoName = ref('');

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

const onImageLoad = (photoId) => {
  const photo = photos.value.find((p) => p.id === photoId);
  if (photo) {
    photo.isLoaded = true;
  }
};

const handleImageError = (event) => {
  event.target.src = '/default-thumbnail.jpg';
};

const fetchPhotos = async (force = false) => {
  const now = Date.now();

  if (loading.value || !hasMore.value) {
    return;
  }

  if (!force && (isThrottling.value || now - lastFetchTime.value < fetchCooldown)) {
    return;
  }

  lastFetchTime.value = now;
  isThrottling.value = true;
  loading.value = true;

  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/photos`, {
      params: {
        page: page.value,
        size: 8
      }
    });

    const newPhotos = data.data.photos.map((p) => ({
      id: p.id,
      url: p.downloadUrl,
      name: p.originalFilename,
      date: formatDate(p.createdAt),
      tags: null,
      isNew: page.value > 0,
      isLoaded: false
    }));

    photos.value = [...photos.value, ...newPhotos];
    hasMore.value = !data.data.isLast;
    page.value++;
  } catch (e) {
    console.error('사진 불러오기 실패:', e);
  } finally {
    loading.value = false;
    setTimeout(() => {
      isThrottling.value = false;
    }, fetchCooldown);
  }
};

const loadPhotoTags = async (photoId) => {
  const photo = photos.value.find((p) => p.id === photoId);
  if (!photo || photo.tags !== null || loadingTags.value[photoId]) return;

  loadingTags.value[photoId] = true;

  try {
    const response = await axiosApi.get(`/photos/${photoId}/tags`);
    const { data } = response;
    const photoIndex = photos.value.findIndex((p) => p.id === photoId);
    if (photoIndex !== -1) {
      photos.value[photoIndex].tags = data?.data?.tags || [];
    }
  } catch (e) {
    const photoIndex = photos.value.findIndex((p) => p.id === photoId);
    if (photoIndex !== -1) {
      photos.value[photoIndex].tags = [];
    }
  } finally {
    loadingTags.value[photoId] = false;
  }
};

const openTagModal = (tags, photoName) => {
  currentTags.value = tags;
  currentPhotoName.value = photoName;
  isTagModalVisible.value = true;
};

const closeTagModal = () => {
  isTagModalVisible.value = false;
  currentTags.value = [];
  currentPhotoName.value = '';
};


const triggerGalleryUpload = async () => {
  if (!canUpload.value) {
    alert('업로드 권한이 없습니다. 로그인 후 다시 시도해주세요.');
    return;
  }
  if (!memberId.value) {
    await accountStore.fetchMyProfile();
  }
  if (!memberId.value) {
    alert('프로필 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
    return;
  }
  galleryInput.value.value = '';
  galleryInput.value.click();
};

const uploadGalleryImage = async (e) => {
  const file = e.target.files[0];
  if (!file || !memberId.value) return;

  try {
    await badgeStore.fetchCollectedBadges(memberId.value);
    const initialBadgeCount = badgeStore.collectedBadges.length;

    const { data: presignedResponse } = await axiosApi.post('/photos/presignedUrl', {
      memberId: memberId.value,
      originalFilename: file.name,
      contentType: file.type
    });
    const presignedData = presignedResponse.data;
    if (!presignedData.uploadUrl || !presignedData.s3Key) {
      console.error('presignedUrl 또는 s3Key가 없습니다', presignedResponse);
      return;
    }
    await axios.put(presignedData.uploadUrl, file, {
      headers: { 'Content-Type': file.type },
      maxContentLength: Infinity,
      maxBodyLength: Infinity
    });
    await axiosApi.post('/photos/complete', {
      memberId: memberId.value,
      originalFilename: file.name,
      s3Key: presignedData.s3Key
    });

    await badgeStore.fetchCollectedBadges(memberId.value);
    const newBadgeCount = badgeStore.collectedBadges.length;

    if (newBadgeCount > initialBadgeCount) {
      isBadgeModalVisible.value = true;
    } else {
      alert('사진 업로드가 완료되었습니다.');
    }
    
    page.value = 0;
    photos.value = [];
    hasMore.value = true;
    await fetchPhotos(true);
  } catch (err) {
    console.error('업로드 실패:', err);
    alert('사진 업로드에 실패했습니다. 다시 시도해주세요.');
  }
};

const viewPhoto = async (photoId) => {
  try {
    const { data } = await axiosApi.get(`/photos/${photoId}`);
    const photoUrl = data.data.downloadUrl;
    if (photoUrl) {
      window.open(photoUrl, '_blank');
    }
  } catch (e) {
    console.error(`사진(ID: ${photoId}) 조회 실패:`, e);
    alert('사진 정보를 불러오는 데 실패했습니다.');
  }
};

const deletePhoto = async (photoId) => {
  if (!canUpload.value) {
    alert('삭제 권한이 없습니다. 로그인 후 다시 시도해주세요.');
    return;
  }
  if (!confirm('정말로 이 사진을 삭제하시겠습니까?')) return;
  try {
    await axiosApi.delete(`/photos/${photoId}`);
    photos.value = photos.value.filter((p) => p.id !== photoId);
    alert('사진이 성공적으로 삭제되었습니다.');
  } catch (e) {
    console.error(`사진(ID: ${photoId}) 삭제 실패:`, e);
    alert('사진 삭제에 실패했습니다. 다시 시도해주세요.');
  }
};

const getScrollMetrics = () => {
  const scrollElement = document.documentElement;
  const scrollTop = Math.max(
    scrollElement.scrollTop,
    document.body.scrollTop,
    window.pageYOffset || 0
  );
  const scrollHeight = Math.max(scrollElement.scrollHeight, document.body.scrollHeight);
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
  if (loading.value || !hasMore.value || isThrottling.value) return false;

  const { scrollTop, scrollHeight, clientHeight, effectiveScrollHeight } = getScrollMetrics();

  if (scrollHeight <= clientHeight + 200) {
    return true;
  }

  const scrollPercentage = (scrollTop + clientHeight) / effectiveScrollHeight;
  const remaining = effectiveScrollHeight - scrollTop - clientHeight;

  const shouldLoad = scrollPercentage >= 0.7 || remaining <= 1200;

  return shouldLoad;
};

const updateScrollVelocity = () => {
  const { scrollTop } = getScrollMetrics();
  const velocity = Math.abs(scrollTop - lastScrollTop.value);
  scrollVelocity.value = velocity;
  lastScrollTop.value = scrollTop;

  if (velocity > 50 && checkScrollPosition()) {
    fetchPhotos();
  }
};

let scrollTimeout = null;
let velocityTimeout = null;
let rafId = null;
let debounceTimeout = null;

const handleScroll = () => {
  if (scrollTimeout) clearTimeout(scrollTimeout);
  if (velocityTimeout) clearTimeout(velocityTimeout);
  if (rafId) cancelAnimationFrame(rafId);
  if (debounceTimeout) clearTimeout(debounceTimeout);
  rafId = requestAnimationFrame(() => {
    updateScrollVelocity();

    if (checkScrollPosition()) {
      fetchPhotos();
    }
  });

  scrollTimeout = setTimeout(() => {
    if (checkScrollPosition()) {
      fetchPhotos();
    }
  }, 50);

  velocityTimeout = setTimeout(updateScrollVelocity, 16);

  debounceTimeout = setTimeout(() => {
    if (checkScrollPosition()) {
      fetchPhotos();
    }
  }, 500);
};

const setupIntersectionObserver = () => {
  if (!observerTarget.value) return null;

  return new IntersectionObserver(
    (entries) => {
      const entry = entries[0];
      if (entry.isIntersecting && entry.intersectionRatio > 0) {
        if (!loading.value && hasMore.value && !isThrottling.value) {
          fetchPhotos();
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
    await new Promise((resolve) => setTimeout(resolve, 100));
    retryCount++;
  }

  if (!observerTarget.value) {
    console.warn('Observer target not found after retries');
    return;
  }

  observer = setupIntersectionObserver();
  if (observer) {
    observer.observe(observerTarget.value);
  }

  const scrollOptions = { passive: true, capture: false };
  window.addEventListener('scroll', handleScroll, scrollOptions);
  document.addEventListener('scroll', handleScroll, scrollOptions);

  window.addEventListener(
    'resize',
    () => {
      setTimeout(() => {
        if (checkScrollPosition()) {
          fetchPhotos();
        }
      }, 100);
    },
    { passive: true }
  );

  const fullscreenEvents = ['fullscreenchange', 'webkitfullscreenchange', 'mozfullscreenchange'];
  fullscreenEvents.forEach((event) => {
    document.addEventListener(event, () => {
      setTimeout(() => {
        if (checkScrollPosition()) {
          fetchPhotos();
        }
      }, 300);
    });
  });

  window.addEventListener('popstate', () => {
    setTimeout(() => {
      if (checkScrollPosition()) {
        fetchPhotos();
      }
    }, 100);
  });
};

const cleanupInfiniteScroll = () => {
  if (observer) {
    observer.disconnect();
    observer = null;
  }

  const timers = [scrollTimeout, velocityTimeout, rafId, debounceTimeout];
  timers.forEach((timer) => {
    if (timer) {
      if (timer === rafId) {
        cancelAnimationFrame(timer);
      } else {
        clearTimeout(timer);
      }
    }
  });

  scrollTimeout = null;
  velocityTimeout = null;
  rafId = null;
  debounceTimeout = null;

  window.removeEventListener('scroll', handleScroll);
  document.removeEventListener('scroll', handleScroll);
  window.removeEventListener('resize', checkScrollPosition);
  window.removeEventListener('popstate', checkScrollPosition);

  const fullscreenEvents = ['fullscreenchange', 'webkitfullscreenchange', 'mozfullscreenchange'];
  fullscreenEvents.forEach((event) => {
    document.removeEventListener(event, checkScrollPosition);
  });

  isScrolling.value = false;
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
  await accountStore.fetchMyProfile();

  if (memberId.value) {
    await fetchPhotos(true);
  }

  await setupInfiniteScroll();

  let checkCount = 0;
  initialCheckInterval = setInterval(() => {
    checkCount++;
    if (checkScrollPosition()) {
      fetchPhotos();
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
  position: relative;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 30px 40px;
  max-width: 1200px;
  width: 100%;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: inset 4px 4px 10px rgba(255 255 255 / 0.6), inset -4px -4px 10px rgba(0 0 0 / 0.15);
  border: 1.5px solid rgba(255 255 255 / 0.25);
  transition: box-shadow 0.3s ease;
}
.stars-background:hover {
  box-shadow: inset 6px 6px 14px rgba(255 255 255 / 0.85), inset -6px -6px 14px rgba(0 0 0 / 0.2);
}
.header-and-back-container {
  margin-top: 20px;
  display: flex;
  align-items: center;
  padding: 0 10px 24px;
  gap: 15px;
}
.back-button-container {
  flex-shrink: 0;
}
.back-button-icon {
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
  transition: all 0.3s ease;
  backdrop-filter: blur(8px);
}
.back-button-icon:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}
.header-container {
  flex: 1;
  display: flex;
  align-items: center;
}
.page-title {
  margin: 0;
  text-align: left;
  font-weight: 700;
  font-size: 1.5rem;
  color: #ffffff;
  line-height: 1.2;
}
.divider {
  border-top: 2px solid rgba(255, 255, 255, 0.2);
  width: 100%;
  margin: 0;
}
.content-container {
  margin-top: 20px;
  padding: 1rem 0;
}
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  max-width: 100%;
  justify-content: center;
}
.upload-box,
.photo-box {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: 18px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transform: scale(1);
  transition: transform 0.25s ease, box-shadow 0.25s ease, background-color 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255 255 255 / 0.1);
  contain: layout style;
}
.upload-box {
  justify-content: center;
  align-items: center;
  font-size: 3.5rem;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.6);
  user-select: none;
  border: 2px dashed rgba(255, 255, 255, 0.3);
}
.upload-box:hover {
  background: rgba(255 255 255 / 0.15);
  color: rgba(255, 255, 255, 0.95);
  border-color: rgba(255, 255, 255, 0.6);
}
.upload-box span {
  user-select: none;
}
.photo-box:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 0.12);
  z-index: 2;
}
.photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  flex-grow: 1;
  filter: blur(8px);
  transition: filter 0.45s ease;
  border-radius: 18px;
  opacity: 0.95;
  will-change: filter;
}
.photo-img.loaded {
  filter: blur(0);
}
.photo-text {
  position: absolute;
  bottom: 0;
  width: 100%;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  padding: 12px;
  font-size: 0.85rem;
  color: #fff;
  font-weight: 600;
  transform: translateY(100%);
  transition: transform 0.3s ease-in-out;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  user-select: none;
  text-shadow: 0 0 6px rgba(0, 0, 0, 0.8);
}
.photo-box:hover .photo-text {
  transform: translateY(0);
}
.photo-title {
  margin: 0;
  font-size: 1rem;
}
.photo-date {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}
.photo-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag-chip {
  background-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  backdrop-filter: blur(5px);
  user-select: none;
  box-shadow: none;
  transition: background-color 0.3s ease;
}
.tag-loading-text {
  color: #00ffff;
  font-size: 0.75rem;
  font-style: italic;
  font-weight: 600;
  user-select: none;
}
.delete-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 50, 50, 0.8);
  color: white;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.25s ease, transform 0.3s ease;
  z-index: 15;
  user-select: none;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}
.photo-box:hover .delete-button {
  opacity: 1;
  transform: scale(1.1);
}
.badge-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
}
.loading-spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  margin: 2.5rem 0;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top: 4px solid #fff;
  border-radius: 50%;
  animation: spin 1.1s linear infinite;
  margin-bottom: 1.2rem;
}
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
.loading-text {
  text-align: center;
  margin-top: 2rem;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 600;
  user-select: none;
}
.observer-target {
  height: 200px;
  width: 100%;
  margin: 40px 0;
}
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
}
.modal-content {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 40px;
  max-width: 400px;
  width: 90%;
  text-align: center;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  animation: fadeIn 0.5s cubic-bezier(0.68, -0.55, 0.27, 1.55);
  overflow: hidden;
}
.badge-title-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  margin-bottom: 10px;
}
.stars,
.stars2,
.stars3 {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  background-image: radial-gradient(2px 2px at 20px 30px, #eee, rgba(0, 0, 0, 0)),
    radial-gradient(2px 2px at 40px 70px, #fff, rgba(0, 0, 0, 0)),
    radial-gradient(2px 2px at 50px 150px, #ddd, rgba(0, 0, 0, 0));
  background-repeat: repeat;
  animation: move-twinkle-back 200s linear infinite;
  opacity: 0.8;
}
.stars2 {
  background-image: radial-gradient(2px 2px at 30px 40px, #fff, rgba(0, 0, 0, 0)),
    radial-gradient(2px 2px at 60px 80px, #fff, rgba(0, 0, 0, 0));
  animation: move-twinkle-back 100s linear infinite;
}
.stars3 {
  background-image: radial-gradient(2px 2px at 70px 80px, #fff, rgba(0, 0, 0, 0)),
    radial-gradient(2px 2px at 90px 100px, #fff, rgba(0, 0, 0, 0));
  animation: move-twinkle-back 50s linear infinite;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(-50px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
@keyframes move-twinkle-back {
  from {
    background-position: 0 0;
  }
  to {
    background-position: -10000px 5000px;
  }
}
.badge-icon {
  width: 30px;
  height: 60px;
  animation: pulse 1s infinite alternate;
  margin-bottom: 20px;
}
@keyframes pulse {
  from {
    transform: scale(1);
  }
  to {
    transform: scale(1.1);
  }
}
.title {
  font-size: 1.7rem;
  font-weight: 700;
  margin: 0;
  color: #ffd700;
  text-shadow: none;
}
.message {
  font-size: 1rem;
  line-height: 1.5;
  margin-bottom: 30px;
  color: rgba(255, 255, 255, 0.9);
}
.close-button {
  background: #2a9df4;
  color: #fff;
  border: 1px solid #2a9df4;
  padding: 12px 30px;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(42, 157, 244, 0.4);
}
.close-button:hover {
  background: #4ab1f8;
  border: 1px solid #4ab1f8;
  box-shadow: 0 6px 16px rgba(42, 157, 244, 0.6);
  transform: translateY(-2px);
}
.tag-modal-content {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  padding: 30px;
  max-width: 450px;
  width: 90%;
  text-align: center;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(15px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
  animation: fadeIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.tag-modal-title {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 20px;
  color: #ffffff;
  text-shadow: none;
}
.tag-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-bottom: 25px;
  max-height: 200px;
  overflow-y: auto;
  padding-right: 5px;
}
.tag-list-container::-webkit-scrollbar {
  width: 6px;
}
.tag-list-container::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}
.tag-list-container::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}
.tag-chip-full {
  background-color: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  backdrop-filter: blur(5px);
  user-select: none;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.tag-modal-close-button {
  background: #2a9df4;
  color: #fff;
  border: 1px solid #2a9df4;
  padding: 10px 25px;
  border-radius: 20px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(42, 157, 244, 0.4);
}
.tag-modal-close-button:hover {
  background: #4ab1f8;
  border: 1px solid #4ab1f8;
  box-shadow: 0 6px 16px rgba(42, 157, 244, 0.6);
}
</style>