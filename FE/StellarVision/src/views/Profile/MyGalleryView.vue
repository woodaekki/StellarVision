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
            style="width: 100%; height: 100%; object-fit: cover"
          />
          <p class="content-info photo-date">Date: {{ photo.date }}</p>
        </div>
        <div v-if="recentPhotos.length === 0" class="empty-frame">
          <p class="empty-text">사진이 없습니다.</p>
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

const memberId = computed(() => {
  const idFromStore = accountStore.myProfile?.memberId
  return idFromStore
})

const recentPhotos = computed(() => photos.value)

const fetchPhotos = async () => {
  if (!memberId.value) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/photos`, {
      params: { page: 0, size: 3 },
    })
    if (data.data && data.data.photos) {
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
  display: flex;
  justify-content: center;
  width: 100%;
}

.profile-section {
  width: 100%;
  max-width: 1200px; 
  margin: 0;
  padding: 10px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5px;
  margin-bottom: 5px;
  padding-bottom: 5px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.detail-button {
  color: #fff;
  cursor: pointer;
  background: transparent;
  border: 1px solid #fff;
  padding: 5px 12px;
  border-radius: 4px;
}

.content-frames {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
  padding-top: 5px;
}

.content-frame,
.empty-frame {
  width: calc(33.33% - 10px);
  max-width: 300px;
  aspect-ratio: 1.4;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.photo-frame {
  background: #fff;
}

.content-info {
  position: absolute;
  top: 3px;
  left: 3px;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 2px 4px;
  border-radius: 2px;
  font-size: 10px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.content-frame:hover .content-info {
  opacity: 1;
}

.empty-frame {
  background-color: #f5f5f5;
  border: 2px dashed #ccc;
  justify-content: center;
  align-items: center;
}

.empty-text {
  color: #999;
  font-size: 14px;
  font-weight: 500;
}
</style>