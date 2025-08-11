<template>
  <div class="video-cell" @click="$emit('select')" @mouseenter="loadVideoTags">
    <div class="thumbnail-container">
      <img :src="video.thumbnail" class="video-thumbnail"/>

      <!-- 호버 시 삭제 버튼 -->
      <button v-if="showEdit" class="delete-button" @click.stop="handleDelete">
        삭제
      </button>
    </div>
    <div class="video-info">
      <div class="video-title">
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

      <!-- 태그 섹션 -->
      <div v-if="tags && tags.length > 0" class="tags-container">
        <div class="tags-list">
          <span
            v-for="tag in tags"
            :key="tag.tagId"
            class="tag"
          >
            {{ tag.tagName }}
          </span>
        </div>
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

// 비디오 태그 로드 (호버 시)
const loadVideoTags = async () => {
  // 이미 태그가 로드되었거나 로딩 중이면 스킵
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
  tags.value = null // 태그 정보 초기화
  loadVideoTags()
}

defineExpose({
  refreshTags
})
</script>

<style scoped>
.video-cell {
  position: relative;
  background-color: #fff;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.video-cell:hover {
  transform: scale(1.02);
}

.thumbnail-container {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
}

.video-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-info {
  padding: 12px;
  background-color: #fff;
  color: #333;
}

.video-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.video-date {
  margin: 0;
  color: #666;
  font-size: 0.75rem;
  margin-bottom: 8px;
}

.tags-container {
  margin-top: 8px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag {
  background-color: #e0e7ff;
  color: #3730a3;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 500;
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

.video-cell:hover .delete-button {
  opacity: 1;
}

.edit-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px;
  color: #333;
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
