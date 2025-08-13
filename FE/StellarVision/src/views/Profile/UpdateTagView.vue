<template>
  <div class="video-edit-page">
    <div class="edit-header">
      <h1 class="edit-title">My Space Video Edit</h1>
      <button @click="goBack" class="back-button">← Back</button>
    </div>

    <div class="tags-section">
      <div class="tags-header">
        <h3 class="tags-title">Tags from Space</h3>
        <img src="../../assets/pictures/stellabot/tag3.png" alt="노바 태그" class="tags-image">
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

    <div v-if="isLoading" class="loading">
      비디오 정보를 불러오는 중...
    </div>

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
.video-edit-page {
  padding: 50px 280px;
  background-color: #fff;
  min-height: 100vh;
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
  margin-left: 10px;
  text-align: left;
  font-weight: 700;
  font-size: medium;
}

.back-button {
  padding: 8px 16px;
  background-color: transparent;
  border: 1px solid #ccc;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.back-button:hover {
  background-color: #f0f0f0;
  border-color: #bbb;
}

.tags-section {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

.tags-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #d1d5da;
}

.tags-title {
  font-size: 1rem;
  font-weight: 600;
  color: #4a5568;
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
  color: #555;
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
  background-color: #f0f4f8;
  color: #333;
  border: none;
}

.remove-tag-btn {
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  padding: 0 0 0 6px;
}

.remove-tag-btn:hover {
  color: #d32f2f;
}

.no-tags {
  color: #999;
  font-style: italic;
  font-size: 0.9rem;
}

.add-tag-section {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.add-tag-form {
  display: flex;
  gap: 10px;
  align-items: center;
}

.tag-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 0.9rem;
  transition: border-color 0.2s;
}

.tag-input:focus {
  outline: none;
  border-color: #2196f3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
}

.add-tag-btn {
  padding: 10px 20px;
  background-color: #42a5f5;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.add-tag-btn:hover:not(:disabled) {
  background-color: #1e88e5;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
  font-size: 1rem;
}

.error-message {
  background-color: #fff1f0;
  color: #d32f2f;
  padding: 12px;
  border-radius: 4px;
  margin: 15px 0;
  border-left: 3px solid #ef5350;
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
