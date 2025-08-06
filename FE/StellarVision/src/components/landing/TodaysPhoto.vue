<template>
  <div class="todays-image-wrapper">
  <h1>{{ isYesterday ? '어제의 천체 사진' : '오늘의 천체 사진' }}</h1>
    <div v-if="loading">Loading...</div>
    <div v-else>
      <p>{{ apod.date }}</p>
      <img
        v-if="apod.media_type === 'image'"
        :src="apod.url"
        :alt="apod.title"
        class="todays-image"
      />
      <iframe
        v-else-if="apod.media_type === 'video'"
        :src="apod.url"
        frameborder="0"
        allowfullscreen
        class="todays-image"
      ></iframe>
      <div
        v-else
        class="todays-placeholder"
      >
        <p>오늘의 천체 사진을 불러올 수 없었습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getTodayOrYesterdayAPOD } from '@/api/todayPhotoApi';

const apod = ref({});
const loading = ref(true);
const isYesterday = ref(false);

onMounted(async () => {
  const result = await getTodayOrYesterdayAPOD();
  apod.value = result.data;
  isYesterday.value = result.isYesterday;
  loading.value = false;
});
</script>

<style scoped>
.todays-image-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  background-color: black;
}

.todays-image {
  max-height: 30rem;
  border-radius: 12px;
}

.todays-placeholder {
  width: 100%;
  max-width: 800px;
  height: 30rem;
  background-color: #ccc;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 1.2rem;
}
</style>
