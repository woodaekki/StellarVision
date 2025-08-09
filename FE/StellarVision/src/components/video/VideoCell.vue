<template>
  <div class="video-cell" @click="$emit('select')">
    <div class="thumbnail-container">
      <img :src="video.thumbnail" class="video-thumbnail"/>
    </div>
    <div class="video-info">
      <div class="video-title ">
        {{ video.name }}
        <button
          v-if="showEdit"
          class="edit-button"
          @click.stop="goToReplayEdit"
        >
          <VideoEditIcon class="edit-icon"/>
        </button>
      </div>
      <p class="video-date">{{ video.date }}</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import VideoEditIcon from './VideoEditIcon.vue';

const props = defineProps({
  video: {
    type: Object,
    required: true
  },
  showEdit: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['select'])

const router = useRouter()

function goToReplayEdit() {
  router.push(`/replay/${props.video.id}/edit`)
}
</script>

<style scoped>
.video-cell {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #e0e0e0;
  transition: transform 0.3s ease-in-out;
}

.video-cell:hover {
  transform: translateY(-3px);
}

.thumbnail-container {
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
}

.video-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.video-info {
  padding: 10px 12px;
  background-color: #fff;
  color: #333;
}

.video-title {
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.video-date {
  font-size: 0.8rem;
  color: #777;
  margin: 0;
}

.edit-button {
  cursor: pointer;
  background: none;
  border: none;
  padding: 0;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.edit-button:hover {
  opacity: 1;
}

.edit-icon {
  fill: #777;
  vertical-align: middle;
  width: 14px;
  height: 14px;
}
</style>