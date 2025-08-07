<template>
  <p class="title">My Gallery</p>

  <div class="button-wrapper">
    <button @click="goGalleryList" class="gallery-list">상세보기</button>
  </div>

  <div class="photo-frames">
    <div
      class="photo-frame"
      v-for="photo in recentPhotos"
      :key="photo.id"
      @click="handlePhotoClick(photo)"
    >
      <img
        :src="photo.url"
        :alt="photo.name"
        style="width: 100%; height: 100%; object-fit: cover"
      />
    </div>

    <div v-if="recentPhotos.length === 0" class="empty-frame">
      <p class="empty-text">사진이 없습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/account'
import axiosApi from '@/api/axiosApi'

const accountStore = useAccountStore()

const photos = ref([])
const loading = ref(false)

const route = useRoute()
const router = useRouter()

const memberId = computed(() => {
  const idFromStore = accountStore.myProfile?.memberId;

  console.log('스토어 프로필 ID:', idFromStore);
  return idFromStore;
});

const recentPhotos = computed(() => {
  return photos.value;
});

const fetchPhotos = async () => {
  if (!memberId.value) {
    console.warn('memberId가 없어 사진 목록을 불러오지 않습니다.');
    loading.value = false;
    return;
  }

  loading.value = true;
  console.log(`사진 목록 API 호출 시작: memberId = ${memberId.value}`);

  try {
    const { data } = await axiosApi.get(`profiles/${memberId.value}/photos`, {
      params: {
        page: 0,
        size: 3,
      },
    });

    console.log('API 응답 성공:', data);

    if (data.data && data.data.photos) {
      photos.value = data.data.photos.map((p) => ({
        id: p.id,
        url: p.downloadUrl,
        name: p.originalFilename,
        date: p.createdAt.split('T')[0],
      }));
      console.log('사진 데이터 처리 완료:', photos.value);
    } else {
      console.warn('API 응답에 예상한 사진 데이터 구조가 없습니다:', data);
      photos.value = [];
    }

  } catch (e) {
    console.error('사진 목록을 불러오는 데 실패했습니다 (AxiosError):', e);
    if (e.response) {
      console.error('서버 응답 상태 코드:', e.response.status);
      console.error('서버 응답 데이터:', e.response.data);
      console.error('서버 응답 헤더:', e.response.headers);
    }
  } finally {
    loading.value = false;
  }
};

const handlePhotoClick = (photo) => {
  console.log(`사진 클릭: ID = ${photo.id}, URL = ${photo.url}`);
  if (photo.url) {
    window.open(photo.url, '_blank');
  }
};

const goGalleryList = () => {
  console.log('상세보기 버튼 클릭, MyGalleryListView로 이동');
  router.push({
    name: 'MyGalleryListView',
    params: { id: memberId.value },
  });
};

onMounted(async () => {
  console.log(' MyGallery 컴포넌트 마운트됨');
  if (!accountStore.myProfile) {
    console.log('프로필 정보가 없어 스토어에서 불러옵니다.');
    await accountStore.fetchMyProfile();
  }

  if (memberId.value) {
    console.log('memberId 확인됨. 사진 목록 불러오기 시작.');
    fetchPhotos();
  } else {
    console.warn('마운트 시점에 memberId를 찾을 수 없습니다.');
  }
});
</script>

<style scoped>
.title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  margin-top: 60px;
  margin-bottom: 32px;
  color: #fff;
  padding: 0 20px;
}

.button-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 0 200px;
}

.gallery-list {
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  font-size: 16px;
  background: transparent;
  border: 1px solid #fff;
  padding: 8px 20px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.gallery-list:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.photo-frames {
  display: flex;
  gap: 32px;
  justify-content: center;
  padding: 60px 20px;
  flex-wrap: nowrap;
  overflow-x: auto;
}

.photo-frame {
  width: 480px;
  height: 300px;
  background: #fff;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.photo-frame:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.empty-frame {
  background-color: #f5f5f5;
  border: 2px dashed #ccc;
  width: 480px;
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

.empty-text {
  color: #999;
  font-size: 18px;
  font-weight: 500;
}

@media (max-width: 1500px) {
  .photo-frames {
    flex-wrap: wrap;
    overflow-x: visible;
  }
  .photo-frame {
    width: calc(50% - 16px);
    margin-bottom: 32px;
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 28px;
    margin-top: 40px;
    margin-bottom: 24px;
  }
  .button-wrapper {
    justify-content: center;
    padding: 0 20px;
    margin-bottom: 20px;
  }
  .gallery-list {
    font-size: 14px;
    padding: 6px 16px;
  }
  .photo-frame {
    width: 100%;
    height: 200px;
  }
}
</style>
