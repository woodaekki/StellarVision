<template>
  <div class="todays-image-wrapper">
  <h1>오늘의 천체 사진</h1>
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchPhoto } from '@/api/todayPhotoApi'

const apod = ref({})
const loading = ref(true)
const today = new Date().toISOString().split('T')[0]
const cacheKey = `apod-${today}`

onMounted(async () => {
  const cached = localStorage.getItem(cacheKey)
  if (cached) {
    apod.value = JSON.parse(cached)
    loading.value = false
    return
  }

  try {
    const data = await fetchPhoto(today)
    apod.value = data
    localStorage.setItem(cacheKey, JSON.stringify(data))
  } catch (err) {
    console.error(err, 'Could not load data from API')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.todays-image-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.todays-image {
  max-height: 30rem;
  border-radius: 12px;
}
</style>
