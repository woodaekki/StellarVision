<template>
  <div class="video-frame">
    <img
        :src="video.thumbnailDownloadUrl"
        style="width: 100%; height: 100%; object-fit: cover"
      />
  </div>
</template>

<script setup>
const props = defineProps({
  video: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.video-frame {
  aspect-ratio: 1.4;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  transition: transform 0.25s ease;
  overflow: hidden;
}

.video-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-frame:hover {
  transform: translateY(-4px) scale(1.02);
}

.video-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(4, 1fr); 
}

@media (max-width: 1500px) {
  .video-grid {
    grid-template-columns: repeat(3, 1fr); 
  }
}

@media (max-width: 1024px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr); 
  }
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(1, 1fr); 
  }
}
</style>
