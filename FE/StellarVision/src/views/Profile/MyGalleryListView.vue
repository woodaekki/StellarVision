<template>
  <div class="page">
    <div class="stars-background">
      <div class="gallery-grid">

        <!-- 업로드 박스 -->
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

        <!-- 사진 리스트 -->
        <div
          v-for="(item, index) in photos"
          :key="item.id"
          class="photo-box"
        >
          <img :src="item.url" class="photo-img" />
          <div class="photo-text">
            <p>{{ item.name }}</p>
            <p class="photo-date">{{ item.date }}</p>
          </div>
        </div>

        <!-- 빈 프레임 채우기 (선택사항) -->
        <div
          v-for="n in (TOTAL_FRAMES - photos.length)"
          :key="'empty-' + n"
          class="empty-frame"
          v-if="photos.length < TOTAL_FRAMES"
        ></div>

        <!-- 무한스크롤 감지 요소 -->
        <div ref="loadMoreTrigger" class="load-more-trigger"></div>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="loading" class="loading-text">사진 불러오는 중...</div>
      <div v-if="!hasMore && photos.length > 0" class="loading-text">모든 사진을 불러왔습니다.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAccountStore } from '@/stores/account'
import axios from 'axios'
import axiosApi from '@/api/axiosApi'

const accountStore = useAccountStore()

const galleryInput = ref(null)
const loadMoreTrigger = ref(null)

const photos = ref([])
const page = ref(1)
const limit = 7
const loading = ref(false)
const hasMore = ref(true)

const TOTAL_FRAMES = 7

const memberId = computed(() => accountStore.myProfile?.memberId)
const canUpload = computed(() => accountStore.isLogin)

// 사진 불러오기 (페이지네이션)
const fetchPhotos = async () => {
  if (loading.value || !hasMore.value) return
  if (!memberId.value) return

  loading.value = true

  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/photos`, {
      params: { page: page.value, limit }
    })

    // API 구조에 맞게 변경 필요
    const newPhotos = data.data.photos.map(p => ({
      id: p.id,
      url: p.downloadUrl,       
      name: p.originalFilename, 
      date: p.createdAt.split('T')[0],
    }))

    photos.value = [...photos.value, ...newPhotos]

    if (photos.value.length >= data.data.totalCount) {
      hasMore.value = false
    } else {
      page.value += 1
    }
  } catch (e) {
    console.error('사진 불러오기 실패:', e)
  } finally {
    loading.value = false
  }
}

// 업로드 트리거
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

// 업로드 처리
const uploadGalleryImage = async (e) => {
  const file = e.target.files[0]
  if (!file || !memberId.value) return

  try {
    const { data } = await axiosApi.post('/photos/presignedUrl', {
      memberId: memberId.value,
      originalFilename: file.name,
    })

    const presignedData = data.data

    if (!presignedData.uploadUrl || !presignedData.s3Key) {
      console.error('presignedUrl 또는 s3Key가 없습니다', data)
      return
    }

    await axios.put(presignedData.uploadUrl, file, {
      headers: {
        'Content-Type': file.type
      },
      maxContentLength: Infinity,
      maxBodyLength: Infinity,
    })

    await axiosApi.post('/photos/complete', {
      memberId: memberId.value,
      originalFilename: file.name,
      s3Key: presignedData.s3Key,
    })

    // 업로드 후 최신 사진 목록 다시 불러오기
    photos.value = []
    page.value = 1
    hasMore.value = true
    await fetchPhotos()

  } catch (err) {
    console.error('업로드 실패:', err)
  }
}

// IntersectionObserver 무한 스크롤 구현
let observer = null

onMounted(async () => {
  await accountStore.fetchMyProfile()
  await fetchPhotos()

  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
      fetchPhotos()
    }
  }, {
    rootMargin: '200px', // 화면 아래 200px 접근 시 미리 로딩
  })

  if (loadMoreTrigger.value) {
    observer.observe(loadMoreTrigger.value)
  }
})

onUnmounted(() => {
  if (observer && loadMoreTrigger.value) {
    observer.unobserve(loadMoreTrigger.value)
  }
})
</script>

<style scoped>
.page {
  background-color: black;
  min-height: 100vh;
  color: white;
  font-family: sans-serif;
}

.stars-background {
  padding: 2rem;
  background: #262626;
  position: relative;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(4, auto);
  gap: 1rem;
  max-width: 640px;
  margin: 0 auto;
}

.upload-box,
.photo-box {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background-color: #ababab;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.upload-box {
  justify-content: center;
  align-items: center;
  font-size: 3rem;
  cursor: pointer;
  transition: background 0.3s;
}
.upload-box:hover {
  background-color: #3a3a3a;
}

.photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  flex-grow: 1;
}

.photo-text {
  position: absolute;
  bottom: 0;
  width: 100%;
  background: rgba(0, 0, 0, 0.6);
  padding: 0.4rem;
  font-size: 0.75rem;
}

.empty-frame {
  width: 100%;
  height: 100%;
  background-color: #ababab;
}

.hidden {
  display: none;
}

.load-more-trigger {
  height: 1px;
}

.loading-text {
  text-align: center;
  margin-top: 1rem;
  color: #ccc;
}
</style>
