<!-- MyGalleryView -->
<template>
  <div class="profile-section-container">
    <div class="profile-section">
      <div class="section-header">
        <h2 class="section-title">My Gallery</h2>
        <button @click="goGalleryList" class="detail-button">더보기</button>
      </div>
      <div class="content-frames">
        <div
          class="content-frame photo-frame"
          v-for="photo in recentPhotos"
          :key="photo.id"
          @click="handlePhotoClick(photo)"
        >
          <img
            :src="photo.url"
            :alt="photo.name"
          />
          <p class="content-info photo-date">Date: {{ photo.date }}</p>
        </div>
        <div v-if="recentPhotos.length === 0" class="empty-frame">
          <p class="empty-text">업로드한 사진이 없습니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/account'
import axiosApi from '@/api/axiosApi'

const accountStore = useAccountStore()
const photos = ref([])
const loading = ref(false)
const router = useRouter()

const memberId = computed(() => accountStore.myProfile?.memberId)

const recentPhotos = computed(() => photos.value)

const fetchPhotos = async () => {
  if (!memberId.value) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/photos`, {
      params: { page: 0, size: 4 },
    })
    if (data.data?.photos) {
      photos.value = data.data.photos.map((p) => ({
        id: p.id,
        url: p.downloadUrl,
        name: p.originalFilename,
        date: p.createdAt.split('T')[0],
      }))
    } else {
      photos.value = []
    }
  } catch (e) {
    console.error('사진 목록을 불러오는 데 실패했습니다 (AxiosError):', e)
  } finally {
    loading.value = false
  }
}

const handlePhotoClick = (photo) => {
  if (photo.url) {
    window.open(photo.url, '_blank')
  }
}

const goGalleryList = () => {
  router.push({ name: 'MyGalleryListView', params: { id: memberId.value } })
}

onMounted(async () => {
  if (!accountStore.myProfile) {
    await accountStore.fetchMyProfile()
  }
  if (memberId.value) {
    fetchPhotos()
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
  padding: 10px 0;
}

.section-header {
  padding: 0 35px; 
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
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  padding-top: 5px;
}

.photo-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.8);
  opacity: 0.97;
  transition: filter 0.3s ease, opacity 0.3s ease;
}

.photo-frame:hover img {
  filter: brightness(1);
  opacity: 1;
}

.photo-frame:hover .content-info {
  opacity: 1;
}

.content-info {
  position: absolute;
  top: 6px;
  left: 6px;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  padding: 2px 4px;
  border-radius: 4px;
  font-size: 10px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.empty-frame {
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
</style>
