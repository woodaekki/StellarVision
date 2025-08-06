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
  .video-frame {
    width: calc(50% - 16px);
    margin-bottom: 32px;
  }
}

@media (max-width: 768px) {
  .video-frame {
    width: 100%;
    height: 200px;
  }
}
</style>
