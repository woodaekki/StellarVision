<!-- 아직 무한 스크롤 기능 지원x -->
<template>
  <div class="page">
    <div class="stars-background">
      <div class="gallery-grid">
        <div class="upload-box" @click="triggerGalleryUpload">
          <span>+</span>
          <input type="file" ref="galleryInput" @change="uploadGalleryImage" accept="image/*" class="hidden" />
        </div>

        <div
          v-for="(item, index) in galleryFrames"
          :key="index"
          class="photo-box"
        >
          <template v-if="item">
            <img :src="item.url" class="photo-img" />
            <div class="photo-text">
              <p>{{ item.name }}</p>
              <p class="photo-date">{{ item.date }}</p>
            </div>
          </template>
          <template v-else>

            <div class="empty-frame"></div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const memberId = 1

const galleryInput = ref(null)

const photos = ref([])
const TOTAL_FRAMES = 7

const triggerGalleryUpload = () => galleryInput.value.click()

const uploadGalleryImage = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  const { data } = await axios.post('/api/photos/presignedUrl', {
    memberId,
    originalFilename: file.name,
    contentType: file.type,
  })

  await axios.put(data.presignedUrl, file, {
    headers: { 'Content-Type': file.type },
  })

  await axios.post('/api/photos/complete', {
    memberId,
    originalFilename: file.name,
    s3Key: data.s3Key,
  })

  await fetchPhotos()
}

const fetchPhotos = async () => {
  const { data } = await axios.get(`/api/profiles/${memberId}/photos`)
  photos.value = data.data.photos.map(p => ({
    id: p.id,
    url: p.url,
    name: p.name,
    date: p.createdAt.split('T')[0],
  }))
}

const galleryFrames = computed(() => {
  const filled = photos.value.slice(0, TOTAL_FRAMES)
  const emptyCount = TOTAL_FRAMES - filled.length
  return [...filled, ...Array(emptyCount).fill(null)]
})

onMounted(() => {
  fetchPhotos()
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

</style>
