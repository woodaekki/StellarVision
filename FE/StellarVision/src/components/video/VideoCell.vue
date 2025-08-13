<template>
  <div class="video-cell" @click="$emit('select')" @mouseenter="loadVideoTags">
    <div class="thumbnail-container">
      <img
        :src="video.thumbnail"
        class="video-thumbnail"
        :class="{ loaded: thumbnailLoaded }"
        @load="onThumbnailLoad"
      />

      <button class="delete-button" @click.stop="handleDelete">
        삭제
      </button>
    </div>

    <div class="video-info">
      <div class="video-title">
        {{ video.name }}
        <button class="edit-button" @click.stop="goToReplayEdit">
          <VideoEditIcon class="edit-icon" />
        </button>
      </div>
      <p class="video-date">{{ video.date }}</p>

      <div class="tags-container">
        <div v-if="displayTags && displayTags.length > 0" class="tags-list">
          <span
            v-for="tag in displayTags"
            :key="tag.tagId"
            class="tag"
          >
            {{ tag.tagName }}
          </span>
        </div>
        <div v-else-if="loadingTags" class="tags-loading">
          <span class="tag-loading-text">태그 로딩중...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import VideoEditIcon from '@/components/video/VideoEditIcon.vue';
import commonApi from '@/api/commonApi';

const props = defineProps({
  video: {
    type: Object,
    required: true,
  },
  tags: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['select', 'delete']);

const router = useRouter();
const internalTags = ref(null);
const loadingTags = ref(false);
const thumbnailLoaded = ref(false);

const displayTags = computed(() => {
  if (props.tags && props.tags.length > 0) {
    return props.tags.slice(0, 4);
  }
  return internalTags.value ? internalTags.value.slice(0, 4) : [];
});

const onThumbnailLoad = () => {
  thumbnailLoaded.value = true;
};

const loadVideoTags = async () => {
  if (props.tags && props.tags.length > 0) {
    return;
  }
  if (internalTags.value !== null || loadingTags.value) {
    return;
  }

  loadingTags.value = true;

  try {
    const response = await commonApi.get(`/videos/${props.video.id}/tags`);

    if (response.data && response.data.status === 'success') {
      const allTags = response.data.data.tags || [];
      internalTags.value = allTags
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 4);
    } else {
      internalTags.value = [];
    }
  } catch (error) {
    console.error('비디오 태그 로딩 실패:', error);
    internalTags.value = [];
  } finally {
    loadingTags.value = false;
  }
};

function goToReplayEdit() {
  router.push(`/replay/${props.video.id}/edit`);
}

const handleDelete = () => {
  emit('delete', props.video);
};

function refreshTags() {
  internalTags.value = null;
  loadVideoTags();
}

onMounted(() => {
  if (!props.tags || props.tags.length === 0) {
    loadVideoTags();
  }
});

defineExpose({
  refreshTags,
});
</script>

<style scoped>
.video-cell {
  position: relative;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transform: scale(1);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-color 0.3s ease;
  box-shadow:
    inset 3px 3px 6px rgba(255 255 255 / 0.7),
    inset -3px -3px 6px rgba(0 0 0 / 0.5);
  color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  contain: layout style;
}

.video-cell:hover {
  transform: scale(1.05);
  box-shadow:
    inset 6px 6px 12px rgba(255 255 255 / 0.8),
    inset -6px -6px 12px rgba(0 0 0 / 0.3),
    0 6px 20px rgba(255, 255, 255, 0.25);
  z-index: 2;
}

.thumbnail-container {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 18px 18px 0 0;
}

.video-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: blur(4px);
  transition: filter 0.4s ease;
  opacity: 0.9;
}

.video-thumbnail.loaded {
  filter: blur(0);
}

.video-info {
  padding: 12px 15px 15px 15px;
  background: rgba(18, 25, 41, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: #f0f4f8;
}

.video-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #e0e7ff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.video-date {
  margin: 0 0 8px 0;
  color: #94a3b8;
  font-size: 0.75rem;
  font-weight: 500;
}

.tags-container {
  min-height: 24px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  background-color: rgba(62, 107, 185, 0.4);
  color: #e2e8f0;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  user-select: none;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  transition: all 0.2s ease;
}

.tag:hover {
  background-color: rgba(62, 107, 185, 0.6);
  transform: scale(1.05);
}

.tags-loading {
  display: flex;
  align-items: center;
  min-height: 20px;
}

.tag-loading-text {
  color: #60a5fa;
  font-size: 0.7rem;
  font-style: italic;
  font-weight: 500;
  user-select: none;
}

.delete-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(252, 47, 47, 0.9);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.25s ease;
  z-index: 15;
  user-select: none;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.video-cell:hover .delete-button {
  opacity: 0.95;
}

.edit-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  cursor: pointer;
  padding: 4px;
  color: #e0e7ff;
  opacity: 0.8;
  transition: all 0.2s ease;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.video-cell:hover .edit-button {
  opacity: 0.9;
}

.edit-button:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.1);
}

.edit-icon {
  width: 14px;
  height: 14px;
  display: block;
  fill: currentColor;
}

@media (max-width: 768px) {
  .video-info {
    padding: 10px 12px;
  }
  .video-title {
    font-size: 0.85rem;
  }
  .tag {
    font-size: 0.65rem;
    padding: 2px 6px;
  }
}
</style>
