<template>
  <div class="page" ref="pageRef">
    <img :src="bg" alt="" class="bg-img">
    <div class="stars-background">
      <div class="px-4 pt-12 pb-6">
        <h2 class="text-2xl mb-2 text-center font-pretendard" style="font-family: 'Pretendard', sans-serif !important;">
          My Space Gallery
        </h2>
        <hr class="border-t-2 border-neutral-200 w-full mt-2" />
      </div>

      <div class="px-4 pb-12">
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
            v-for="(item, index) in photos"
            :key="item.id"
            class="photo-box"
            @click="viewPhoto(item.id)"
            @mouseenter="loadPhotoTags(item.id)"
            @animationend="onAnimationEnd(item.id)"
          >
            <img
              :src="item.url"
              class="photo-img"
              :class="{ 'loaded': item.isLoaded }"
              @load="onImageLoad(item.id)"
            />
            <div class="photo-text">
              <p>{{ item.name }}</p>
              <p class="photo-date">{{ item.date }}</p>
              <div v-if="item.tags && item.tags.length" class="photo-tags">
                <span
                  v-for="tag in item.tags"
                  :key="tag.tagId"
                  class="tag-chip"
                >
                  {{ tag.tagName }}
                </span>
              </div>
              <div v-else-if="loadingTags[item.id]" class="photo-tags">
                <span class="tag-loading-text">태그 로딩중...</span>
              </div>
            </div>
            <button class="delete-button" @click.stop="deletePhoto(item.id)">삭제</button>
          </div>
        </div>

        <div v-if="loading" class="loading-spinner">
          <div class="spinner"></div>
          <div class="loading-text">사진 불러오는 중...</div>
        </div>
        <div v-if="!photos.length && !loading && !hasMore" class="loading-text">아직 사진이 없습니다.</div>
        <div v-if="!loading && hasMore" class="loading-text">스크롤하여 더 많은 사진 보기</div>

        <!-- Intersection Observer 감지 요소 - 더 크게 만들어서 감지 향상 -->
        <div ref="observerTarget" class="observer-target"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useAccountStore } from '@/stores/account'
import axios from 'axios'
import axiosApi from '@/api/axiosApi'
import bg from '@/assets/pictures/stellabot/spaceBackground.avif'

const accountStore = useAccountStore()

const pageRef = ref(null)
const galleryInput = ref(null)
const observerTarget = ref(null)

const photos = ref([])
const loading = ref(false)
const page = ref(0)
const hasMore = ref(true)
const isScrolling = ref(false)
const loadingTags = ref({})

const lastFetchTime = ref(0)
const fetchCooldown = 200
const isNearBottom = ref(false)
const lastScrollTop = ref(0)
const scrollVelocity = ref(0)

const memberId = computed(() => accountStore.myProfile?.memberId)
const canUpload = computed(() => accountStore.isLogin)

const onAnimationEnd = (photoId) => {
  const photo = photos.value.find(p => p.id === photoId)
  if (photo) {
    photo.isNew = false
  }
}

const onImageLoad = (photoId) => {
  const photo = photos.value.find(p => p.id === photoId)
  if (photo) {
    photo.isLoaded = true
  }
}

const fetchPhotos = async () => {
  const now = Date.now()
  
  if (loading.value || !hasMore.value) {
    return
  }
  
  if (isScrolling.value && (now - lastFetchTime.value < fetchCooldown)) {
    return
  }

  lastFetchTime.value = now
  isScrolling.value = true
  loading.value = true

  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/photos`, {
      params: {
        page: page.value,
        size: 7,
      },
    })

    const newPhotos = data.data.photos.map((p) => ({
      id: p.id,
      url: p.downloadUrl,
      name: p.originalFilename,
      date: p.createdAt.split('T')[0],
      tags: null,
      isNew: page.value > 0,
      isLoaded: false
    }))

    photos.value = [...photos.value, ...newPhotos]
    hasMore.value = !data.data.isLast
    page.value++
  } catch (e) {
    console.error('사진 불러오기 실패:', e)
  } finally {
    loading.value = false
    setTimeout(() => {
      isScrolling.value = false
    }, fetchCooldown)
  }
}

const loadPhotoTags = async (photoId) => {
  const photo = photos.value.find(p => p.id === photoId)
  if (!photo || photo.tags !== null || loadingTags.value[photoId]) return

  loadingTags.value[photoId] = true

  try {
    const response = await axiosApi.get(`/photos/${photoId}/tags`)
    const { data } = response
    const photoIndex = photos.value.findIndex(p => p.id === photoId)
    if (photoIndex !== -1) {
      photos.value[photoIndex].tags = data?.data?.tags || []
    }
  } catch (e) {
    const photoIndex = photos.value.findIndex(p => p.id === photoId)
    if (photoIndex !== -1) {
      photos.value[photoIndex].tags = []
    }
  } finally {
    loadingTags.value[photoId] = false
  }
}

const triggerGalleryUpload = async () => {
  if (!canUpload.value) {
    alert('업로드 권한이 없습니다. 로그인 후 다시 시도해주세요.')
    return
  }
  if (!memberId.value) {
    await accountStore.fetchMyProfile()
  }
  if (!memberId.value) {
    alert('프로필 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.')
    return
  }
  galleryInput.value.value = ''
  galleryInput.value.click()
}

const uploadGalleryImage = async (e) => {
  const file = e.target.files[0]
  if (!file || !memberId.value) return

  try {
    const { data: presignedResponse } = await axiosApi.post('/photos/presignedUrl', {
      memberId: memberId.value,
      originalFilename: file.name,
      contentType: file.type,
    })
    const presignedData = presignedResponse.data
    if (!presignedData.uploadUrl || !presignedData.s3Key) {
      console.error('presignedUrl 또는 s3Key가 없습니다', presignedResponse)
      return
    }
    await axios.put(presignedData.uploadUrl, file, {
      headers: { 'Content-Type': file.type },
      maxContentLength: Infinity,
      maxBodyLength: Infinity,
    })
    await axiosApi.post('/photos/complete', {
      memberId: memberId.value,
      originalFilename: file.name,
      s3Key: presignedData.s3Key,
    })
    page.value = 0
    photos.value = []
    hasMore.value = true
    await fetchPhotos()
    alert('사진 업로드가 완료되었습니다.')
  } catch (err) {
    console.error('업로드 실패:', err)
    alert('사진 업로드에 실패했습니다. 다시 시도해주세요.')
  }
}

const viewPhoto = async (photoId) => {
  try {
    const { data } = await axiosApi.get(`/photos/${photoId}`)
    const photoUrl = data.data.downloadUrl
    if (photoUrl) {
      window.open(photoUrl, '_blank')
    }
  } catch (e) {
    console.error(`사진(ID: ${photoId}) 조회 실패:`, e)
    alert('사진 정보를 불러오는 데 실패했습니다.')
  }
}

const deletePhoto = async (photoId) => {
  if (!canUpload.value) {
    alert('삭제 권한이 없습니다. 로그인 후 다시 시도해주세요.')
    return
  }
  if (!confirm('정말로 이 사진을 삭제하시겠습니까?')) return
  try {
    await axiosApi.delete(`/photos/${photoId}`)
    photos.value = photos.value.filter(p => p.id !== photoId)
    alert('사진이 성공적으로 삭제되었습니다.')
  } catch (e) {
    console.error(`사진(ID: ${photoId}) 삭제 실패:`, e)
    alert('사진 삭제에 실패했습니다. 다시 시도해주세요.')
  }
}

const getScrollMetrics = () => {
  const scrollElement = document.documentElement
  const scrollTop = Math.max(scrollElement.scrollTop, document.body.scrollTop, window.pageYOffset || 0)
  const scrollHeight = Math.max(scrollElement.scrollHeight, document.body.scrollHeight)
  const clientHeight = window.innerHeight || document.documentElement.clientHeight
  
  return { scrollTop, scrollHeight, clientHeight }
}

const checkScrollPosition = () => {
  if (loading.value || !hasMore.value) return false
  
  const { scrollTop, scrollHeight, clientHeight } = getScrollMetrics()
  
  if (scrollHeight <= clientHeight) return false
  
  const scrollPercentage = (scrollTop + clientHeight) / scrollHeight
  const remaining = scrollHeight - scrollTop - clientHeight
  
  const shouldLoad = scrollPercentage >= 0.8 || remaining <= 800
  
  if (shouldLoad && !isNearBottom.value) {
    isNearBottom.value = true
    return true
  } else if (!shouldLoad && isNearBottom.value) {
    isNearBottom.value = false
  }
  
  return false
}

const updateScrollVelocity = () => {
  const { scrollTop } = getScrollMetrics()
  scrollVelocity.value = Math.abs(scrollTop - lastScrollTop.value)
  lastScrollTop.value = scrollTop
  
  if (scrollVelocity.value > 100 && checkScrollPosition()) {
    fetchPhotos()
  }
}

let scrollTimeout = null
let velocityTimeout = null
let rafId = null

const handleScroll = () => {
  if (scrollTimeout) clearTimeout(scrollTimeout)
  if (velocityTimeout) clearTimeout(velocityTimeout)
  if (rafId) cancelAnimationFrame(rafId)
  
  rafId = requestAnimationFrame(() => {
    updateScrollVelocity()
    
    if (checkScrollPosition()) {
      fetchPhotos()
    }
  })
  
  scrollTimeout = setTimeout(() => {
    if (checkScrollPosition()) {
      fetchPhotos()
    }
  }, 50)
  
  velocityTimeout = setTimeout(updateScrollVelocity, 16)
}

const setupIntersectionObserver = () => {
  if (!observerTarget.value) return null
  
  return new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      if (entry.isIntersecting && entry.intersectionRatio > 0) {
        if (!loading.value && hasMore.value) {
          fetchPhotos()
        }
      }
    },
    { 
      root: null,
      rootMargin: '500px 0px 500px 0px',
      threshold: [0, 0.1, 0.25, 0.5, 0.75, 1.0]
    }
  )
}

let observer = null

const setupInfiniteScroll = async () => {
  await nextTick()
  
  if (!observerTarget.value) {
    setTimeout(setupInfiniteScroll, 100)
    return
  }

  observer = setupIntersectionObserver()
  if (observer) {
    observer.observe(observerTarget.value)
  }

  window.addEventListener('scroll', handleScroll, { passive: true })
  document.addEventListener('scroll', handleScroll, { passive: true })
  
  window.addEventListener('resize', () => {
    setTimeout(() => {
      if (checkScrollPosition()) {
        fetchPhotos()
      }
    }, 100)
  }, { passive: true })
  
  document.addEventListener('fullscreenchange', () => {
    setTimeout(() => {
      if (checkScrollPosition()) {
        fetchPhotos()
      }
    }, 300)
  })
  
  document.addEventListener('webkitfullscreenchange', () => {
    setTimeout(() => {
      if (checkScrollPosition()) {
        fetchPhotos()
      }
    }, 300)
  })
}

const cleanupInfiniteScroll = () => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
  
  if (scrollTimeout) {
    clearTimeout(scrollTimeout)
    scrollTimeout = null
  }
  
  if (velocityTimeout) {
    clearTimeout(velocityTimeout)
    velocityTimeout = null
  }
  
  if (rafId) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
  
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', checkScrollPosition)
  document.removeEventListener('fullscreenchange', checkScrollPosition)
  document.removeEventListener('webkitfullscreenchange', checkScrollPosition)
  
  isScrolling.value = false
  isNearBottom.value = false
}

const preventScrollRestore = () => {
  if ('scrollRestoration' in history) {
    history.scrollRestoration = 'manual'
  }
}

let initialCheckInterval = null

onMounted(async () => {
  preventScrollRestore()
  await accountStore.fetchMyProfile()
  
  if (memberId.value) {
    await fetchPhotos()
  }
  
  await setupInfiniteScroll()
  
  initialCheckInterval = setInterval(() => {
    if (checkScrollPosition()) {
      fetchPhotos()
      clearInterval(initialCheckInterval)
    }
  }, 200)
  
  setTimeout(() => {
    if (initialCheckInterval) {
      clearInterval(initialCheckInterval)
      initialCheckInterval = null
    }
  }, 3000)
})

onBeforeUnmount(() => {
  cleanupInfiniteScroll()
  if (initialCheckInterval) {
    clearInterval(initialCheckInterval)
  }
})
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

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
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
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-color 0.3s ease;
  box-shadow:
    inset 3px 3px 6px rgba(255 255 255 / 0.7),
    inset -3px -3px 6px rgba(0 0 0 / 0.5);
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255 255 255 / 0.12);
  contain: layout style;
}

.upload-box {
  justify-content: center;
  align-items: center;
  font-size: 3.5rem;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.6);
  user-select: none;
  box-shadow:
    inset 6px 6px 12px rgba(255 255 255 / 0.4),
    inset -6px -6px 12px rgba(0 0 0 / 0.25);
}

.upload-box:hover {
  background: rgba(255 255 255 / 0.15);
  color: rgba(255, 255, 255, 0.95);
  border-color: rgba(255, 255, 255, 0.6);
  box-shadow:
    inset 8px 8px 16px rgba(255 255 255 / 0.75),
    inset -8px -8px 16px rgba(0 0 0 / 0.3);
}

.upload-box span {
  user-select: none;
}

.photo-box:hover {
  transform: scale(1.07);
  box-shadow:
    inset 8px 8px 16px rgba(255 255 255 / 0.8),
    inset -8px -8px 16px rgba(0 0 0 / 0.3),
    0 6px 20px rgba(255, 255, 255, 0.25);
  z-index: 2;
}

.photo-img {
  height: 100vh;
  object-fit: cover;
  flex-grow: 1;
  filter: blur(8px);
  transition: filter 0.45s ease;
  border-radius: 18px;
  opacity: 0.95;

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
  padding: 10px 12px;
  font-size: 0.85rem;
  color: #fff;
  font-weight: 600;
  transform: translateY(100%);
  transition: transform 0.3s ease-in-out;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  user-select: none;
  box-shadow: 0 0 12px rgba(255 255 255 / 0.3);
  text-shadow: 0 0 6px rgba(0 0 0 / 0.8);
}

.photo-box:hover .photo-text {
  transform: translateY(0);
}

.photo-tags {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  background-color: #888888; 
  color: #f0f0f0;
  padding: 3px 8px;
  border-radius: 14px;
  font-size: 0.7rem;
  font-weight: 600;
  user-select: none;
  box-shadow: none; 
  transition: background-color 0.3s ease;
}

.tag-loading-text {
  color: #3b82f6;
  font-size: 0.75rem;
  font-style: italic;
  font-weight: 600;
  user-select: none;
}

.delete-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 50, 50, 0.95);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 5px 12px;
  cursor: pointer;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.25s ease;
  z-index: 15;
  user-select: none;
  font-weight: 600;
}

.photo-box:hover .delete-button {
  opacity: 0.9;
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

.observer-target {
  height: 100px; 
  width: 100%;
  margin: 20px 0;
}

@media (max-width: 768px) {
  .gallery-grid {
    grid-template-columns: repeat(2, 1fr);
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
</style>