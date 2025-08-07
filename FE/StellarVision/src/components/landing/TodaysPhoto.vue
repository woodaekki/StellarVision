<template>
  <div class="main-container">
    <div class="black-header"></div>
    <div class="content-container">
      <div v-if="loading" class="loading-container">
        <p>Loading...</p>
      </div>
      <div v-else class="content-wrapper">
        <div class="left-section">
          <h1 class="title">{{ apod.title }}</h1>
          <p class="date">{{ apod.date }}</p>
          <img
            v-if="apod.media_type === 'image'"
            :src="apod.url"
            :alt="apod.title"
            class="main-image"
          />
          <iframe
            v-else-if="apod.media_type === 'video'"
            :src="apod.url"
            frameborder="0"
            allowfullscreen
            class="main-video"
          ></iframe>
          <div v-else class="placeholder">
            <p>오늘의 천체 사진을 불러올 수 없었습니다.</p>
          </div>
        </div>
        <div class="right-section">
          <p class="explanation">
            <span v-for="(char, index) in apod.explanation" :key="index" :style="{ 'animation-delay': `${index * 3}ms` }" class="fade-in-char">{{ char }}</span>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getTodayOrYesterdayAPOD } from '@/api/todayPhotoApi';

const apod = ref({});
const loading = ref(true);

onMounted(async () => {
  try {
    const result = await getTodayOrYesterdayAPOD();
    apod.value = result.data;
    loading.value = false;
  } catch (error) {
    console.error("Failed to fetch APOD data:", error);
    loading.value = false;
  }
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

* {
  box-sizing: border-box;
  font-family: 'Pretendard', 'Noto Sans KR', sans-serif;
}

.main-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.black-header {
  width: 100%;
  height: 58px;
  background-color: #000;
  flex-shrink: 0;
}

.content-container {
  flex: 1;
  background-color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  color: #333;
  height: calc(100vh - 58px);
}

.loading-container {
  font-size: 1.5rem;
  color: #555;
}

.content-wrapper {
  display: flex;
  width: 100%;
  max-width: 1200px;
  height: 100%;
  max-height: 800px;
  box-shadow: none;
  background-color: #fff;
  border-radius: 0;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.left-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 2rem;
  background-color: #f5f5f5;
}

.title {
  font-size: clamp(2rem, 3vw, 2.5rem);
  font-weight: 700;
  margin-bottom: 0.5rem;
  line-height: 1.2;
}

.date {
  font-size: 1rem;
  color: #777;
  margin-bottom: 1.5rem;
}

.main-image,
.main-video {
  width: 100%;
  height: auto;
  flex-grow: 1;
  object-fit: cover;
  border-radius: 5px;
}

.right-section {
  flex: 1;
  padding: 2rem;
  display: flex;
  align-items: center;
  overflow-y: auto;
  line-height: 1.8;
  font-size: clamp(0.9rem, 1.5vw, 1.1rem);
  color: #555;
  background-color: #ffffff;
}

.explanation {
  white-space: pre-wrap;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0e0e0;
  color: #a0a0a0;
  min-height: 300px;
  border-radius: 5px;
  text-align: center;
}

.right-section::-webkit-scrollbar {
  width: 8px;
}
.right-section::-webkit-scrollbar-track {
  background: #f1f1f1;
}
.right-section::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}
.right-section::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.explanation {
  white-space: pre-wrap;
  text-align: justify;
}

.fade-in-char {
  opacity: 0;
  animation: fadeInChar 0.1s forwards;
}

@keyframes fadeInChar {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
    height: auto;
    overflow-y: auto;
  }
  .left-section,
  .right-section {
    width: 100%;
    padding: 1rem;
  }
  .title {
    font-size: 2rem;
  }
  .content-container {
    padding: 1rem;
  }
}
</style>
