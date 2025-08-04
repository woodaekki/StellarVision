<!-- 레이아웃만 잡힌 상태 -->
<template>
  <p class="title">My video</p>

  <div class="button-wrapper">
    <button @click="goVideoList" class="video-list">상세보기</button>
  </div>

  <div class="video-frames">
    <div class="video-frame" v-for="n in 3" :key="n">
    </div>
  </div>
</template>

<script setup>
import MyVideoListView from '@/views/Profile/MyVideoListView.vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const goVideoList = () => {
  router.push({
    name: 'MyVideoListView',
    params: { id: route.params.id }
  })
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

.video-list {
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

.video-list:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.video-frames {
  display: flex;
  gap: 32px;
  justify-content: center;
  padding: 60px 20px;
  flex-wrap: nowrap;
  overflow-x: auto;
}

.video-frame {
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

.video-frame:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

@media (max-width: 1500px) {
  .video-frames {
    flex-wrap: wrap;
    overflow-x: visible;
  }
  .video-frame {
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
  .video-list {
    font-size: 14px;
    padding: 6px 16px;
  }
  .video-frame {
    width: 100%;
    height: 200px;
  }
}
</style>
