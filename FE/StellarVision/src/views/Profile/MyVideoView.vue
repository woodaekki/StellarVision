<template>
  <div class="profile-section-container">
    <div class="profile-section">
      <div class="section-header">
        <h2 class="section-title">My Video</h2>
        <button
          v-if="recentVideos.length > 0"
          @click="goVideoList"
          class="detail-button"
        >
          더보기
        </button>
      </div>
      <div class="content-frames">
        <div
          v-for="video in recentVideos"
          :key="video.id"
          class="content-frame video-frame"
          @click="goToReplayRoom(video.id)"
        >
          <img
            :src="getVideoThumbnail(video)"
            :alt="video.name"
            @error="handleImageError"
          />
          <div class="video-overlay">
            <div class="play-button">
              <svg width="20" height="20" fill="currentColor" viewBox="0 0 24 24">
                <path d="M8 5v14l11-7z" />
              </svg>
            </div>
          </div>
          <p class="content-info video-title">{{ video.name }}</p>
          <div v-if="video.tags && video.tags.length > 0" class="video-tags">
            <span v-for="tag in video.tags.slice(0, 2)" :key="tag.tagId" class="tag-item">
              #{{ tag.tagName }}
            </span>
            <span v-if="video.tags.length > 2" class="tag-count">
              +{{ video.tags.length - 2 }}
            </span>
          </div>
        </div>
        <div v-if="recentVideos.length === 0 && !loading" class="empty-frame">
          <div class="empty-icon">🎥</div>
          <p class="empty-text">업로드한 영상이 없습니다.</p>
        </div>
        <div v-if="loading" class="empty-frame">
          <div class="loading-spinner">
            <div class="spinner"></div>
            <p class="empty-text">로딩 중...</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAccountStore } from '@/stores/account'
import axiosApi from '@/api/axiosApi'
import defaultBg from '@/assets/pictures/stellabot/nova.png'

const router = useRouter()
const route = useRoute()
const accountStore = useAccountStore()

const loading = ref(false)
const recentVideos = ref([])

// 별자리 썸네일 자동 매핑
let STAR_IMAGES = {}
let STAR_BY_KEY = {}

try {
  // import.meta.glob을 사용하여 동적으로 이미지 로드
  STAR_IMAGES = import.meta.glob('@/assets/pictures/stars/*.{png,jpg,jpeg,webp}', {
    eager: true,
    import: 'default'
  })
} catch (error) {
  STAR_IMAGES = {}
}

// 태그 없을 경우
const defaultImg = defaultBg

// 공백제거, 소문자화, xx자리에서 '자리'를 삭제
function normalizeKoConstellation(s) {
  const normalized = s.replace(/\s+/g, '').replace(/자리$/u, '').toLowerCase()
  console.log(`🔄 정규화: "${s}" -> "${normalized}"`)
  return normalized
}

// 별 이름 앞자리로 찾기, 파일명 기준으로 매핑 테이블 구성
for (const path in STAR_IMAGES) {
  const filename = path.split('/').pop().replace(/\.(png|jpg|jpeg|webp)$/i, '')
  const normalizedKey = normalizeKoConstellation(filename)
  STAR_BY_KEY[normalizedKey] = STAR_IMAGES[path]
}


// 별자리 딕셔너리
const ALIASES = {
  // '큰곰': '큰곰자리',
}

function pickStarThumbByTags(tagList, fallback) {
  for (const t of tagList || []) {
    const raw = typeof t === 'string' ? t : (t.tagName || '')
    if (!raw) continue

    let key = normalizeKoConstellation(raw)
    if (ALIASES[key]) {
      key = ALIASES[key]
    }
  }
  return fallback
}

// 비디오 썸네일 결정 함수
const getVideoThumbnail = (video) => {
  // 태그가 있는 경우 별자리 이미지 우선 사용
  if (video.tags && video.tags.length > 0) {
    const starThumbnail = pickStarThumbByTags(video.tags, null)
    if (starThumbnail) {
      return starThumbnail
    }
  }
  return defaultImg
}

const memberId = computed(() => accountStore.myProfile?.memberId)

const fetchTagsForVideos = async (videosList) => {
  if (!videosList || videosList.length === 0) return videosList

  const tagPromises = videosList.map(async (video) => {
    try {
      const res = await axiosApi.get(`/videos/${video.id}/tags`)
      return { ...video, tags: res.data.data?.tags || [] }
    } catch (err) {
      console.error(`비디오 ${video.id}의 태그를 불러오는 데 실패했습니다:`, err)
      return { ...video, tags: [] }
    }
  })

  return Promise.all(tagPromises)
}

const fetchVideos = async () => {
  if (!memberId.value) {
    recentVideos.value = []
    loading.value = false
    return
  }
  loading.value = true
  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/videos`, {
      params: { page: 0, size: 3 },
    })
    if (data.data?.videos) {
      let videos = data.data.videos.map((v) => ({
        id: v.id,
        thumbnailDownloadUrl: v.thumbnailDownloadUrl,
        createdAt: v.createdAt,
        name: v.originalFilename,
      }))

      // 태그 정보를 추가로 불러오기
      videos = await fetchTagsForVideos(videos)
      recentVideos.value = videos
    } else {
      recentVideos.value = []
    }
  } catch (e) {
    console.error('영상 목록 불러오기 실패:', e)
    recentVideos.value = []
  } finally {
    loading.value = false
  }
}

const handleImageError = (event) => {
  // 이미지 로드 실패 시 기본 이미지로 대체
  event.target.src = defaultImg
}

const goVideoList = () => {
  router.push({
    name: 'MyVideoListView',
    params: { id: memberId.value },
  })
}

const goToReplayRoom = (videoId) => {
  router.push({
    name: 'ReplayView',
    params: { id: videoId },
  })
}

onMounted(async () => {
  if (!accountStore.myProfile) {
    await accountStore.fetchMyProfile()
  }
  if (memberId.value) {
    fetchVideos()
  }
})
</script>

<style scoped>
.profile-section-container {
  margin-top: 15px;
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
  width: 100%;
  font-family: 'Pretendard', sans-serif;
  color: white;
  background: url('/assets/space-bg.jpg') center/cover no-repeat;
  padding: 20px 0;
}

.profile-section {
  width: 100%;
  max-width: 1200px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 35px;
  margin-bottom: 10px;
}

.section-title {
  font-size: 25px;
  font-weight: 700;
}

.detail-button {
  color: white;
  cursor: pointer;
  background: rgba(15, 20, 40, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.7);
  padding: 6px 14px;
  border-radius: 6px;
  backdrop-filter: blur(6px);
  transition: background 0.3s ease;
}
.detail-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.content-frames {
  max-width: calc(100% - 70px);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding-top: 5px;
  box-sizing: border-box;
}

.content-frame {
  border-radius: 10px;
  width: calc(33.333% - 14px);
  max-width: 380px;
  aspect-ratio: 1.3;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.05));
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.content-frame:hover {
  box-shadow: 0 8px 20px rgba(0,0,0,0.7);
  transform: translateY(-5px);
}

.content-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.8);
  opacity: 0.97;
  transition: filter 0.3s ease, opacity 0.3s ease, transform 0.3s ease;
  border-radius: 10px;
}

.content-frame:hover img {
  filter: brightness(1);
  opacity: 1;
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
  border-radius: 10px;
}

.content-frame:hover .video-overlay {
  opacity: 1;
}

.play-button {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.play-button:hover {
  background: #ffffff;
  transform: scale(1.1);
}

.content-info {
  position: absolute;
  top: 8px;
  left: 8px;
  color: white;
  background: rgba(0, 0, 0, 0.6);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s ease;
  user-select: none;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  max-width: calc(100% - 16px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-frame:hover .content-info {
  opacity: 1;
}

.video-tags {
  position: absolute;
  bottom: 8px;
  left: 8px;
  right: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 10;
}

.content-frame:hover .video-tags {
  opacity: 1;
}

.tag-item {
  background-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 9px;
  padding: 2px 6px;
  border-radius: 8px;
  font-weight: 500;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.tag-count {
  background-color: rgba(255, 255, 255, 0.3);
  color: #ffffff;
  font-size: 9px;
  padding: 2px 6px;
  border-radius: 8px;
  font-weight: 600;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.empty-frame {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 140px;
  width: 100%;
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 8px;
  opacity: 0.7;
}

.empty-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin-top: 10px;
  font-weight: 500;
  text-align: center;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.spinner {
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-top: 3px solid #fff;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .content-frames {
    flex-direction: column;
    align-items: center;
  }

  .content-frame {
    width: 100%;
    max-width: 300px;
  }

  .section-header {
    padding: 0 20px;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 20px;
  }

  .content-frame {
    max-width: 250px;
  }
}
</style>
