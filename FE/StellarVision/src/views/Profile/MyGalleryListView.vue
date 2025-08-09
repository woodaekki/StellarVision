<template>
  <div class="page" ref="pageRef">
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
            class="photo-box group"
            @click="viewPhoto(item.id)"
            @mouseenter="loadPhotoTags(item.id)"
          >
            <img :src="item.url" class="photo-img" />
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

        <div v-if="loading" class="loading-text">사진 불러오는 중...</div>
        <div v-if="!photos.length && !loading && !hasMore" class="loading-text">아직 사진이 없습니다.</div>
        <div v-if="!loading && hasMore" class="loading-text">스크롤하여 더 많은 사진 보기</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useAccountStore } from '@/stores/account'
import axios from 'axios'
import axiosApi from '@/api/axiosApi'

const accountStore = useAccountStore()

const pageRef = ref(null)
const galleryInput = ref(null)

const photos = ref([])
const loading = ref(false)
const page = ref(0)
const hasMore = ref(true)
const isScrolling = ref(false)
const loadingTags = ref({})

const memberId = computed(() => accountStore.myProfile?.memberId)
const canUpload = computed(() => accountStore.isLogin)

// 사진 불러오기
const fetchPhotos = async () => {
  if (loading.value || !hasMore.value || isScrolling.value) return

  isScrolling.value = true
  loading.value = true

  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/photos`, {
      params: {
        page: page.value,
        size: 8,
      },
    })

    const newPhotos = data.data.photos.map((p) => ({
      id: p.id,
      url: p.downloadUrl,
      name: p.originalFilename,
      date: p.createdAt.split('T')[0],
      tags: null, // 초기에는 태그 정보 없음
    }))

    photos.value = [...photos.value, ...newPhotos]
    hasMore.value = !data.data.isLast
    page.value++
  } catch (e) {
    console.error('사진 불러오기 실패:', e)
  } finally {
    loading.value = false
    isScrolling.value = false
  }
}

// 사진 태그 조회 (호버 시)
const loadPhotoTags = async (photoId) => {
  // 이미 태그가 로드되었거나 로딩 중이면 스킵
  const photo = photos.value.find(p => p.id === photoId)
  
  if (!photo) {
    return
  }

  if (photo.tags !== null) {
    return
  }

  if (loadingTags.value[photoId]) {
    return
  }

  loadingTags.value[photoId] = true

  try {
    const apiUrl = `/photos/${photoId}/tags`
    const response = await axiosApi.get(apiUrl)
    const { data } = response

    if (data && data.status === 'success') {
      // 해당 사진의 태그 정보 업데이트
      const photoIndex = photos.value.findIndex(p => p.id === photoId)

      if (photoIndex !== -1) {
        const tagsToSet = data.data.tags || []
        photos.value[photoIndex].tags = tagsToSet
      }
    } else {
      // 실패 시 빈 배열로 설정하여 재요청 방지
      const photoIndex = photos.value.findIndex(p => p.id === photoId)
      if (photoIndex !== -1) {
        photos.value[photoIndex].tags = []
      }
    }
  } catch (e) {
    // 에러 발생 시 빈 배열로 설정하여 재요청 방지
    const photoIndex = photos.value.findIndex(p => p.id === photoId)
    if (photoIndex !== -1) {
      photos.value[photoIndex].tags = []
    }
  } finally {
    loadingTags.value[photoId] = false
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
      headers: {
        'Content-Type': file.type,
      },
      maxContentLength: Infinity,
      maxBodyLength: Infinity,
    })

    // 업로드 완료 후 서버로부터 응답 대기
    const completeResponse = await axiosApi.post('/photos/complete', {
      memberId: memberId.value,
      originalFilename: file.name,
      s3Key: presignedData.s3Key,
    })

    // 업로드 완료 후 사진 목록 초기화 및 새로고침
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

// 단건 조회 후 새 창 열기
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

// 사진 삭제
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

// 스크롤 이벤트 핸들러
const handleScroll = () => {
  const el = pageRef.value
  if (!el || loading.value || !hasMore.value) return

  const scrollBottom = el.scrollTop + el.clientHeight
  const threshold = el.scrollHeight - 50

  if (scrollBottom >= threshold) {
    fetchPhotos()
  }
}

onMounted(async () => {
  await accountStore.fetchMyProfile()
  if (memberId.value) {
    await fetchPhotos()
  }
  if (pageRef.value) {
    pageRef.value.addEventListener('scroll', handleScroll)
  }
})

onBeforeUnmount(() => {
  if (pageRef.value) {
    pageRef.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.page {
  height: 100vh; 
  overflow-y: auto; 
}

.stars-background h2 {
  margin-top: 48px !important;
  margin-bottom: 52px !important;
  margin-left: 10px;
  text-align: left;
  font-weight: 700;
  font-size: medium;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.upload-box,
.photo-box {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background-color: #1a1a1a;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.05);
}

.upload-box {
  justify-content: center;
  align-items: center;
  font-size: 2.5rem;
  color: #999;
  background: linear-gradient(135deg, #1f1f1f, #2b2b2b);
  transition: all 0.3s ease;
}

.upload-box:hover {
  background: linear-gradient(135deg, #292929, #333);
  color: #ddd;
  border-color: #777;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.1);
}

.upload-box span {
  font-weight: 300;
  font-size: 3rem;
  line-height: 1;
}

/* 업로드 버튼 텍스트 제거 */
input[type="file"] {
  display: none !important;
}

.photo-box:hover {
  transform: scale(1.03);
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.1);
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
  background: rgba(0, 0, 0, 0.7);
  padding: 8px 10px;
  font-size: 0.75rem;
  color: #eee;
  transform: translateY(100%);
  transition: transform 0.3s ease-in-out;
}

.photo-box:hover .photo-text {
  transform: translateY(0);
}

.photo-tags {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-chip {
  background-color: rgba(59, 130, 246, 0.8);
  color: white;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 0.65rem;
  font-weight: 500;
}

.tag-loading-text {
  color: #3b82f6;
  font-size: 0.65rem;
  font-style: italic;
}

.delete-button {
  position: absolute;
  top: 8px;
  right: 8px;
  background-color: rgba(255, 0, 0, 0.75);
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 10;
}

.photo-box:hover .delete-button {
  opacity: 1;
}

.loading-text {
  text-align: center;
  margin-top: 1rem;
  color: #ccc;
}

@media (max-width: 768px) {
  .gallery-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>