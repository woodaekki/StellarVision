<!-- 아직 기능 지원x -->
<template>
  <p class="title">My Gallery</p>

  <div class="button-wrapper">
    <button @click="goGalleryList" class="gallery-list">상세보기</button>
  </div>

  <div class="photo-frames">
    <div class="photo-frame" v-for="n in 3" :key="n">
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import MyGalleryListView from '@/views/Profile/MyGalleryListView.vue'

const router = useRouter()
const goGalleryList = () => {
  router.push({name: MyGalleryListView})
}
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

@media (max-width: 1500px) {
  .photo-frames {
    flex-wrap: wrap;
    overflow-x: visible;   /* 스크롤 해제 */
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
