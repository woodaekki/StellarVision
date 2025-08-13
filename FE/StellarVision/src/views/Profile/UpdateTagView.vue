<template>
  <div class="page video-edit-page">
    <img :src="bg" alt="우주 배경" class="bg-img" />

    <div class="stars-background">
      <div class="edit-header">
        <h1 class="edit-title">태그 수정</h1>
        <button @click="goBack" class="back-button">← Back</button>
      </div>

      <div v-if="isLoading" class="loading-container">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <span class="loading-text">태그 정보를 불러오는 중...</span>
        </div>
      </div>

      <div v-else-if="error" class="error-message">
        {{ error }}
      </div>

      <div v-else class="tags-section">
        <div class="tags-header">
          <h3 class="tags-title">Tags from Space</h3>
          <img :src="nova" alt="노바 태그" class="tags-image">
        </div>

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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import commonApi from '@/api/commonApi';
import bg from '@/assets/pictures/stellabot/spaceBackground.avif';
import nova from '@/assets/pictures/stellabot/nova.png';

const route = useRoute();
const router = useRouter();

const videoId = computed(() => route.params.id);
const tags = ref([]);
const newTagName = ref('');
const isLoading = ref(true);
const isAdding = ref(false);
const isRemoving = ref(false);
const error = ref('');

// 비디오 정보 및 태그 불러오기
const fetchVideoData = async () => {
  if (!videoId.value || videoId.value === 'undefined') {
    error.value = '비디오 ID가 유효하지 않습니다.';
    isLoading.value = false;
    return;
  }

  try {
    error.value = '';
    await fetchTags();
  } catch (err) {
    if (err.response) {
      if (err.response.data && err.response.data.message) {
        error.value = err.response.data.message;
      } else {
        error.value = `서버 오류 (${err.response.status}): ${err.response.statusText || '알 수 없는 오류'}`;
      }
    } else if (err.request) {
      error.value = '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.';
    } else {
      error.value = '요청 설정 중 오류가 발생했습니다.';
    }
  } finally {
    isLoading.value = false;
  }
};

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
  } catch (err) {
    tags.value = [];
  }
};

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
  } catch (err) {
    alert('태그 추가 중 오류가 발생했습니다.');
  } finally {
    isAdding.value = false;
  }
};

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
      await fetchTags();
      alert('태그가 성공적으로 삭제되었습니다.');
    } else {
      alert(response.data.message || '태그 삭제에 실패했습니다.');
    }
  } catch (err) {
    alert('태그 삭제 중 오류가 발생했습니다.');
  } finally {
    isRemoving.value = false;
  }
};

const goBack = () => {
  router.back();
};

const initializeComponent = () => {
  error.value = '';
  tags.value = [];
  fetchVideoData();
};

onMounted(initializeComponent);
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-size: cover;
  background-image: linear-gradient(rgba(11, 12, 16, 0.7), rgba(11, 12, 16, 0.7));
  background-position: center;
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 auto;
}

.bg-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -1;
}

.stars-background {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 30px 40px;
  max-width: 800px;
  width: 100%;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow:
    inset 4px 4px 10px rgba(255 255 255 / 0.6),
    inset -4px -4px 10px rgba(0 0 0 / 0.3),
    8px 8px 30px rgba(0 0 0 / 0.4);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 0;
  border-bottom: none;
}

.edit-title {
  margin: 0;
  text-align: left;
  font-weight: 700;
  font-size: 1.5rem;
  color: white;
}

.back-button {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
  backdrop-filter: blur(4px);
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}

.tags-section {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.tags-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.2);
}

.tags-title {
  font-size: 1rem;
  font-weight: 600;
  color: white;
  margin: 0;
  margin-right: 15px;
}

.tags-image {
  width: 100px;
  height: auto;
}

.tags-section h4 {
  font-size: 1rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 15px;
}

.existing-tags {
  margin-bottom: 20px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.85rem;
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  backdrop-filter: blur(5px);
}

.remove-tag-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  padding: 0 0 0 6px;
  transition: color 0.2s;
}

.remove-tag-btn:hover {
  color: #ff4d4f;
}

.no-tags {
  color: rgba(255, 255, 255, 0.6);
  font-style: italic;
  font-size: 0.9rem;
}

.add-tag-section {
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.add-tag-form {
  display: flex;
  gap: 10px;
  align-items: center;
}

.tag-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  font-size: 0.9rem;
  transition: border-color 0.2s;
  background: rgba(0, 0, 0, 0.2);
  color: white;
}

.tag-input:focus {
  outline: none;
  border-color: #00ffff;
  box-shadow: 0 0 0 2px rgba(0, 255, 255, 0.2);
}

.add-tag-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.add-tag-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.spinner {
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top: 4px solid #fff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1.1s linear infinite;
}

.loading-text {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background-color: rgba(255, 0, 0, 0.2);
  color: #ff4d4f;
  padding: 12px;
  border-radius: 8px;
  margin: 15px 0;
  border-left: 3px solid #ff4d4f;
  font-weight: 500;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .add-tag-form {
    flex-direction: column;
    gap: 8px;
  }

  .edit-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .video-edit-page {
    padding: 15px;
  }
}
</style>