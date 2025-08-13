<template>
  <div class="page">
    <div class="stars-background">
      <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <div class="loading-text">Loading...</div>
      </div>
      <div v-else class="content-wrapper">
        <div class="header-container">
          <div class="header-content">
            <h2 class="page-title">{{ apod.title }}</h2>
            <p class="date">{{ apod.date }}</p>
          </div>
          <hr class="divider" />
        </div>
        <div class="main-content-container">
          <div class="media-section">
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
              <p>오늘의 천체 사진을 불러올 수 없습니다.</p>
            </div>
          </div>
          <div class="explanation-section">
            <p class="explanation">
              <span v-for="(char, index) in apod.explanation" :key="index" :style="{ 'animation-delay': `${index * 3}ms` }" class="fade-in-char">{{ char }}</span>
            </p>
          </div>
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

.page {
  min-height: 100vh;
  background-size: cover;
  background-image: linear-gradient(rgba(11, 12, 16, 0.7), rgba(11, 12, 16, 0.7));
  background-position: center;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: 'Pretendard', 'Noto Sans KR', sans-serif;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 auto;
  position: relative;
}

.page::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('@/assets/pictures/stellabot/spaceBackground.avif') no-repeat center center/cover;
  z-index: -2;
}

.page::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(11, 12, 16, 0.7);
  z-index: -1;
}

.stars-background {
  position: relative;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 30px 40px;
  max-width: 1200px;
  width: 100%;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow:
    inset 4px 4px 10px rgba(255 255 255 / 0.6),
    inset -4px -4px 10px rgba(0 0 0 / 0.15);
  border: 1.5px solid rgba(255 255 255 / 0.25);
  transition: box-shadow 0.3s ease;
  min-height: 80vh;
  display: flex;
  flex-direction: column;
}

.stars-background:hover {
  box-shadow:
    inset 6px 6px 14px rgba(255 255 255 / 0.85),
    inset -6px -6px 14px rgba(0 0 0 / 0.2);
}

.loading-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top: 4px solid #fff;
  border-radius: 50%;
  animation: spin 1.1s linear infinite;
  margin-bottom: 1.2rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  text-align: center;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 600;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.header-container {
  margin-bottom: 24px;
}

.header-content {
  padding: 0 10px;
}

.page-title {
  margin-top: 0;
  margin-bottom: 5px;
  font-weight: 700;
  font-size: 2.2rem;
  line-height: 1.2;
  color: #ffffff;
}

.date {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 0;
}

.divider {
  border-top: 2px solid rgba(255, 255, 255, 0.2);
  width: 100%;
  margin-top: 20px;
}

.main-content-container {
  display: flex;
  gap: 20px;
  flex: 1;
}

.media-section,
.explanation-section {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.media-section {
  flex: 1.2;
  display: flex;
  flex-direction: column;
}

.explanation-section {
  flex: 1;
  padding: 25px;
  overflow-y: auto;
  line-height: 1.7;
  font-size: 1rem;
  color: #e0e0e0;
}

.main-image {
  width: 100%;
  height: auto;
  flex-grow: 1;
  object-fit: contain;
  border-radius: 5px;
  max-height: 100%;
}

.main-video {
  width: 100%;
  height: auto;
  flex-grow: 1;
  border-radius: 5px;
  min-height: 400px;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.3);
  min-height: 300px;
  border-radius: 5px;
  text-align: center;
}

.explanation-section::-webkit-scrollbar {
  width: 8px;
}
.explanation-section::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
.explanation-section::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}
.explanation-section::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

.explanation {
  white-space: pre-wrap;
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

@media (max-width: 992px) {
  .main-content-container {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .page {
    padding: 20px 10px;
  }
  .stars-background {
    padding: 20px;
  }
  .page-title {
    font-size: 1.8rem;
  }
  .media-section,
  .explanation-section {
    padding: 15px;
  }
  .main-video {
    min-height: 300px;
  }
}
</style>