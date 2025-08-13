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
            :src="video.thumbnailDownloadUrl"
            alt="video thumbnail"
          />
          <p class="content-info video-date">Date: {{ video.createdAt.split('T')[0] }}</p>
        </div>
        <div v-if="recentVideos.length === 0 && !loading" class="empty-frame">
          <p class="empty-text">업로드한 영상이 없습니다.</p>
        </div>
        <div v-if="loading" class="empty-frame">
          <p class="empty-text">로딩 중...</p>
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

const router = useRouter()
const route = useRoute()
const accountStore = useAccountStore()

const loading = ref(false)
const recentVideos = ref([])

const memberId = computed(() => accountStore.myProfile?.memberId)

const fetchVideos = async () => {
  if (!memberId.value) {
    recentVideos.value = []
    loading.value = false
    return
  }
  loading.value = true
  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/videos`, {
      params: { page: 0, size: 4 },
    })
    if (data.data?.videos) {
      recentVideos.value = data.data.videos.map((v) => ({
        id: v.id,
        thumbnailDownloadUrl: v.thumbnailDownloadUrl,
        createdAt: v.createdAt,
      }))
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
  gap: 15px;
  flex-wrap: wrap;
  padding-top: 5px;
  box-sizing: border-box;
}

.content-frame {
  border-radius: 10px;
  width: calc(25% - 12px);
  max-width: 280px;
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
  transition: filter 0.3s ease, opacity 0.3s ease;
  border-radius: 10px;
}

.content-frame:hover img {
  filter: brightness(1);
  opacity: 1;
}

.content-info {
  position: absolute;
  top: 6px;
  left: 6px;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s ease;
  user-select: none;
}

.content-frame:hover .content-info {
  opacity: 1;
}

.empty-frame {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 140px;
}

.empty-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin-top: 10px;
  font-weight: 500;
}


</style>
