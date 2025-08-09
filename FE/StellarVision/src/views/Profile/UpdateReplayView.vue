<template>
  <div class="video-edit-page">
    <div class="edit-header">
      <h1 class="edit-title">Video edit</h1>
      <button @click="goBack" class="back-button">← Back</button>
    </div>

    <!-- 비디오 정보 -->
    <div class="video-info-section" v-if="video">
      <div class="video-details">
        <h2>{{ video.name }}</h2>
        <p class="video-date">{{ video.date }}</p>
      </div>
    </div>

    <!-- 태그 관리 섹션 -->
    <div class="tags-section">
      <h3>태그 관리</h3>
      
      <!-- 기존 태그 목록 -->
      <div class="existing-tags">
        <h4>현재 태그</h4>
        <div class="tags-list" v-if="tags.length > 0">
          <span 
            v-for="tag in tags" 
            :key="tag.tagId"
            class="tag existing-tag"
          >
            {{ tag.tagName }}
            <button 
              @click="removeTag(tag.tagId)"
              class="remove-tag-btn"
              :disabled="isRemoving"
            >
              ×
            </button>
          </span>
        </div>
        <p v-else class="no-tags">아직 태그가 없습니다.</p>
      </div>

      <!-- 태그 추가 -->
      <div class="add-tag-section">
        <h4>태그 추가</h4>
        <div class="add-tag-form">
          <input 
            v-model="newTagName"
            type="text" 
            placeholder="새 태그 입력..."
            class="tag-input"
            @keyup.enter="addTag"
            :disabled="isAdding"
            maxlength="20"
          />
          <button 
            @click="addTag"
            class="add-tag-btn"
            :disabled="isAdding || !newTagName.trim()"
          >
            {{ isAdding ? '추가 중...' : '태그 추가' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading">
      비디오 정보를 불러오는 중...
    </div>

    <!-- 에러 상태 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import commonApi from '@/api/commonApi';

const route = useRoute();
const router = useRouter();

const videoId = computed(() => route.params.id);
const video = ref(null);
const tags = ref([]);
const newTagName = ref('');
const isLoading = ref(true);
const isAdding = ref(false);
const isRemoving = ref(false);
const error = ref('');

// 비디오 정보 및 태그 불러오기
const fetchVideoData = async () => {
  // videoId 유효성 검사
  if (!videoId.value || videoId.value === 'undefined') {
    error.value = '비디오 ID가 유효하지 않습니다.';
    isLoading.value = false;
    return;
  }
  
  try {
    error.value = '';
    
    // 비디오 정보 가져오기
    const videoResponse = await commonApi.get(`/videos/${videoId.value}`);
    
    if (videoResponse.data.status === 'success') {
      video.value = videoResponse.data.data;
    } else {
      error.value = videoResponse.data.message || '비디오 정보를 불러올 수 없습니다.';
    }

    // 태그 정보 가져오기
    await fetchTags();
    
  } catch (error) {
    if (error.response) {
      if (error.response.data && error.response.data.message) {
        error.value = error.response.data.message;
      } else {
        error.value = `서버 오류 (${error.response.status}): ${error.response.statusText || '알 수 없는 오류'}`;
      }
    } else if (error.request) {
      error.value = '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.';
    } else {
      error.value = '요청 설정 중 오류가 발생했습니다.';
    }
    
  } finally {
    isLoading.value = false;
  }
};

// 태그 목록 가져오기
const fetchTags = async () => {
  if (!videoId.value || videoId.value === 'undefined') {
    return;
  }
  
  try {
    const response = await commonApi.get(`/videos/${videoId.value}/tags`);
    
    if (response.data.status === 'success') {
      const tagsData = response.data.data?.tags || [];
      tags.value = tagsData;
    } else {
      tags.value = [];
    }
  } catch (error) {
    tags.value = [];
  }
};

// 태그 추가
const addTag = async () => {
  const tagName = newTagName.value.trim();
  
  if (!tagName) {
    alert('태그명을 입력해주세요.');
    return;
  }

  if (tagName.length > 20) {
    alert('태그는 최대 20자까지 입력 가능합니다.');
    return;
  }

  // 중복 체크
  const isDuplicate = tags.value.some(tag => tag.tagName === tagName);
  
  if (isDuplicate) {
    alert('이미 존재하는 태그입니다.');
    return;
  }

  isAdding.value = true;

  try {
    const response = await commonApi.post(`/videos/${videoId.value}/tags`, {
      tagName: tagName
    });

    if (response.data.status === 'success') {
      newTagName.value = '';
      await fetchTags(); 
      alert('태그가 성공적으로 추가되었습니다.');
    } else {
      alert(response.data.message || '태그 추가에 실패했습니다.');
    }
  } catch (error) {
    alert('태그 추가 중 오류가 발생했습니다.');
  } finally {
    isAdding.value = false;
  }
};

// 태그 삭제
const removeTag = async (tagId) => {
  if (!videoId.value || videoId.value === 'undefined') {
    alert('비디오 ID가 유효하지 않습니다.');
    return;
  }

  if (!confirm('정말로 이 태그를 삭제하시겠습니까?')) {
    return;
  }

  isRemoving.value = true;

  try {
    const response = await commonApi.delete(`/videos/${videoId.value}/tags/${tagId}`);
    
    if (response.data.status === 'success') {
      await fetchTags(); // 태그 목록 새로고침
      alert('태그가 성공적으로 삭제되었습니다.');
    } else {
      alert(response.data.message || '태그 삭제에 실패했습니다.');
    }
  } catch (error) {
    alert('태그 삭제 중 오류가 발생했습니다.');
  } finally {
    isRemoving.value = false;
  }
};

// 뒤로가기
const goBack = () => {
  router.back();
};

// 라우트 변경 감지
const initializeComponent = () => {
  // 초기화
  error.value = '';
  video.value = null;
  tags.value = [];
  
  fetchVideoData();
};

onMounted(initializeComponent);
</script>

<style scoped>
.video-edit-page {
  margin: 0 auto;
  padding: 100px;
  background-color: #fff;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
}

.edit-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.back-button {
  padding: 8px 16px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  color: #666;
  cursor: pointer;
  transition: background-color 0.2s;
}

.back-button:hover {
  background-color: #e9e9e9;
}

.video-info-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fafafa;
}

.video-details h2 {
  margin: 0 0 8px 0;
  font-size: 1.3rem;
  color: #333;
}

.video-date {
  color: #666;
  margin: 0;
}

.tags-section {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 25px;
}

.tags-section h3 {
  margin: 0 0 25px 0;
  font-size: 1.4rem;
  color: #333;
}

.tags-section h4 {
  margin: 0 0 12px 0;
  font-size: 1.1rem;
  color: #555;
}

.existing-tags {
  margin-bottom: 30px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 16px;
  font-size: 0.85rem;
  color: #555;
}

.existing-tag {
  background-color: #e3f2fd;
  border-color: #bbdefb;
  color: #1976d2;
}

.remove-tag-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 1.2rem;
  line-height: 1;
  padding: 0;
  margin-left: 4px;
  transition: color 0.2s;
}

.remove-tag-btn:hover {
  color: #f44336;
}

.remove-tag-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.no-tags {
  color: #999;
  font-style: italic;
  margin: 0;
}

.add-tag-section {
  border-top: 1px solid #eee;
  padding-top: 25px;
}

.add-tag-form {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.tag-input {
  flex: 1;
  padding: 10px 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.2s;
}

.tag-input:focus {
  outline: none;
  border-color: #2196f3;
}

.tag-input:disabled {
  background-color: #f5f5f5;
  opacity: 0.7;
}

.add-tag-btn {
  padding: 10px 20px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.add-tag-btn:hover:not(:disabled) {
  background-color: #1976d2;
}

.add-tag-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
  font-size: 1.1rem;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 16px;
  border-radius: 8px;
  margin: 20px 0;
  border-left: 4px solid #f44336;
  font-weight: 500;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .add-tag-form {
    flex-direction: column;
  }
  
  .edit-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
}
</style>