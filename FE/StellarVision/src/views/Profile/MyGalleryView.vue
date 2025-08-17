<template>
  <div class="profile-section-container">
    <div class="profile-section">
      <div class="section-header">
        <h2 class="section-title">은하 사진관</h2>
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
          <p class="content-info photo-title">{{ photo.name }}</p>
        </div>
        <div v-if="recentPhotos.length === 0 && !loading" class="empty-frame">
          <p class="empty-text">업로드한 사진이 없습니다.</p>
        </div>
        <div v-if="loading" class="empty-frame">
          <p class="empty-text">사진을 불러오는 중...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axiosApi from '@/api/axiosApi'

const photos = ref([])
const loading = ref(false)
const router = useRouter()

const props = defineProps({
  profilePk: {
    type: [Number, Object], // ref 객체도 받을 수 있도록 수정
    required: true
  },
  profileEmail: {
    type: String,
    required: true // required로 변경
  }
})

// profilePk가 ref인지 확인하고 적절히 처리
const memberId = computed(() => {
  if (typeof props.profilePk === 'object' && props.profilePk.value !== undefined) {
    return props.profilePk.value; // ref 객체인 경우
  }
  return props.profilePk; // 일반 값인 경우
})

// 최근 사진 3장만 표시
const recentPhotos = computed(() => photos.value.slice(0, 3))

const fetchPhotos = async () => {
  const currentMemberId = memberId.value;
  
  if (!currentMemberId) {
    console.log('memberId가 없어서 fetchPhotos 중단');
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    console.log('사진 가져오기 시도, memberId:', currentMemberId);
    const { data } = await axiosApi.get(`profiles/${currentMemberId}/photos`, {
      params: { page: 0, size: 3 },
    })
    if (data.data?.photos) {
      photos.value = data.data.photos.map((p) => ({
        id: p.id,
        url: p.downloadUrl,
        name: p.originalFilename,
        date: p.createdAt.split('T')[0],
      }))
      console.log('사진 가져오기 성공:', photos.value.length, '개');
    } else {
      photos.value = []
      console.log('사진 데이터가 없음');
    }
  } catch (e) {
    console.error('사진 목록을 불러오는 데 실패했습니다:', e)
    photos.value = []
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
  const currentMemberId = memberId.value;
  router.push({
    name: 'MyGalleryListView', 
    params: { id: props.profileEmail },
    state: { profilePk: currentMemberId } 
  })
}

// memberId 변화 감지
watch(memberId, (newMemberId, oldMemberId) => {
  console.log('memberId 변화 감지:', oldMemberId, '->', newMemberId);
  if (newMemberId && newMemberId !== oldMemberId) {
    fetchPhotos();
  }
}, { immediate: false })

onMounted(async () => {
  console.log('MyGalleryView mounted');
  console.log('props.profilePk:', props.profilePk);
  console.log('props.profileEmail:', props.profileEmail);
  console.log('computed memberId:', memberId.value);
  
  if (memberId.value) {
    await fetchPhotos()
  } else {
    console.log('memberId가 없어서 fetchPhotos 건너뜀');
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
  padding: 0 35px;
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
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.7);
  transform: translateY(-5px);
}
</style>