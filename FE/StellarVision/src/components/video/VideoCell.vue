<template>
  <div class="video-cell" @click="$emit('select')">
    <div class="thumbnail-container">
      <img :src="video.thumbnail" class="video-thumbnail"/>
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
      <div v-if="tags.length > 0" class="tags-container">
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

const emit = defineEmits(['select'])

const router = useRouter()
const tags = ref([])
const isLoadingTags = ref(false)

// 태그 데이터를 가져오는 함수
async function fetchTags() {
  if (!props.video.id) {
    return
  }
  
  isLoadingTags.value = true
  try {
    const response = await commonApi.get(`/videos/${props.video.id}/tags`)
    
    // 성공적인 응답 발생 시 
    if (response.data.status === 'success') {
      if (response.data.data?.tags) {
        if (response.data.data.tags.length > 0) {
          tags.value = response.data.data.tags
        } else {
          tags.value = []
        }
      } else {
        tags.value = []
      }
    } else {
      tags.value = []
    }
  } catch (error) {
    // 에러 발생 시 빈 배열로 설정 (UI가 깨지지 않도록)
    tags.value = []
  } finally {
    isLoadingTags.value = false
  }
}

function refreshTags() {
  fetchTags()
}

defineExpose({
  refreshTags
})

function goToReplayEdit() {
  router.push(`/replay/${props.video.id}/edit`)
}

onMounted(() => {
  fetchTags()
})
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
  margin: 0 0 8px 0;
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
  display: inline-block;
  padding: 2px 8px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 12px;
  font-size: 0.75rem;
  color: #555;
  white-space: nowrap;
  transition: background-color 0.2s;
}

.tag:hover {
  background-color: #e0e0e0;
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
  width: 24px;
  height: 24px;
}
</style>