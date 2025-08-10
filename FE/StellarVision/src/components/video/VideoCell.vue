<template>
  <div class="video-box group" @click="$emit('select')" @mouseenter="loadVideoTags">
    <div class="thumbnail-container">
      <img :src="video.thumbnail" class="video-thumbnail" />
      <button v-if="showEdit" class="delete-button" @click.stop="handleDelete">
        삭제
      </button>
    </div>
    
    <!-- 갤러리 스타일의 비디오 정보 -->
    <div class="video-text">
      <div class="video-title-row">
        <p class="video-name">{{ video.name }}</p>
        <button
          v-if="showEdit"
          class="edit-button"
          @click.stop="goToReplayEdit"
        >
          <VideoEditIcon class="edit-icon"/>
        </button>
      </div>
      <p class="video-date">{{ video.date }}</p>
      
      <div v-if="tags && tags.length" class="video-tags">
        <span
          v-for="tag in tags"
          :key="tag.tagId"
          class="tag-chip"
        >
          {{ tag.tagName }}
        </span>
      </div>
      <div v-else-if="loadingTags" class="video-tags">
        <span class="tag-loading-text">태그 로딩중...</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import VideoEditIcon from './VideoEditIcon.vue';
import commonApi from '@/api/commonApi';

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

const emit = defineEmits(['select', 'delete'])

const router = useRouter()
const tags = ref(null) 
const loadingTags = ref(false)


const loadVideoTags = async () => {
  if (tags.value !== null || loadingTags.value) {
    return
  }

  loadingTags.value = true

  try {
    const response = await commonApi.get(`/videos/${props.video.id}/tags`)
    
    if (response.data && response.data.status === 'success') {
      tags.value = response.data.data.tags || []
    } else {
      tags.value = []
    }
  } catch (error) {
    console.error('비디오 태그 로딩 실패:', error)
    tags.value = []
  } finally {
    loadingTags.value = false
  }
}

function goToReplayEdit() {
  router.push(`/replay/${props.video.id}/edit`)
}

const handleDelete = () => {
  emit('delete', props.video);
};

function refreshTags() {
  tags.value = null 
  loadVideoTags()
}

defineExpose({
  refreshTags
})
</script>

<style scoped>
.video-box {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: #1a1a1a;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.05);
}

.video-box:hover {
  transform: scale(1.03);
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.1);
}

.thumbnail-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.video-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-text {
  position: absolute;
  bottom: 0;
  width: 100%;
  background: rgba(0, 0, 0, 0.7);
  padding: 8px 10px;
  font-size: 0.75rem;
  color: #eee;
  transform: translateY(100%);
  transition: transform 0.3s ease-in-out;
}

.video-box:hover .video-text {
  transform: translateY(0);
}

.video-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.video-name {
  margin: 0;
  font-weight: 500;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.video-date {
  margin: 0;
  color: #ccc;
  font-size: 0.7rem;
}

.video-tags {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-chip {
  background-color: rgba(59, 130, 246, 0.8);
  color: white;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 0.65rem;
  font-weight: 500;
}

.tag-loading-text {
  color: #3b82f6;
  font-size: 0.65rem;
  font-style: italic;
}

.delete-button {
  position: absolute;
  top: 8px;
  right: 8px;
  background-color: rgba(255, 0, 0, 0.75);
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 10;
}

.video-box:hover .delete-button {
  opacity: 1;
}

.edit-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px;
  color: #eee;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.edit-button:hover {
  opacity: 1;
}

.edit-icon {
  width: 14px;
  height: 14px;
}
</style>