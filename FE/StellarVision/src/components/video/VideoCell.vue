<template>
  <div class="video-cell" @click="$emit('select')">
    <img :src="video.thumbnail" class="video-thumbnail"/>
    <div class="video-info">
      <div class="video-title">
        {{ video.name }}
        <button
          v-if="showEdit"
          class="edit-button"
          @click.stop="goToReplayEdit"
        >
          <VideoEditIcon />
        </button>
      </div>
      <!-- 여기에 비디오 태그들이 들어가야 함 -->
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

// 링크는 그냥 임의로 넣음, 추후 다시보기 수정 view 생성 및 router 연결 후 고칠 예정
function goToReplayEdit() {
  router.push(`/replay/${props.video.id}/edit`)
}
</script>

<style lang="scss" scoped>
.video-cell {
  display: flex;
  align-items: center;
  border-radius: 12px;
  overflow: hidden;
  transition: background 0.2s;
  cursor: pointer;
  margin: 1rem, 0.5rem;
}

.video-cell:hover {
  background-color: white;
  color: black;
}

.video-thumbnail {
  width: 480px;
  height: 300px;
  cursor: pointer;
  display: flex;
  border: 1px solid white;
  border-radius: 12px;
  object-fit: cover;
  object-position: center;
  margin: 1rem;
}


.video-info {
  padding: 16px;
}

.video-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.video-date {
  font-size: 0.95rem;
  color: #666;
}

.edit-button {
  cursor: pointer;
  background: none;
  border: none;
  padding: 0;
}

.edit-icon {
  fill: #666;
  vertical-align: middle;
}
</style>
