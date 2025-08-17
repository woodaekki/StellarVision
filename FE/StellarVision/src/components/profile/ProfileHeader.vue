<template>
  <div class="profile-header">
    <div class="profile-header-inner">
      <div class="profile-header-left">
        <div class="profile-image-container">
          <div
            class="profile-image"
            :class="{ 'editable': canEdit }"
            @click="triggerProfileImageUpload"
          >
            <img
              v-if="profileInfo?.profileImageUrl"
              :src="profileInfo.profileImageUrl"
            />
            <span v-else>+</span>
            <div class="edit-mode-overlay" v-if="editMode">
              <span>+</span>
            </div>
          </div>
          <input
            type="file"
            ref="profileImageInput"
            @change="uploadProfileImage"
            accept="image/*"
            style="display: none;"
            :disabled="!canEdit"
          />
        </div>

        <div class="profile-text">
          <div class="email-row">
            <span class="email">{{ profileInfo?.name || '닉네임' }}</span>
            <BadgeShelf
              :memberId="memberId"
              :editMode="editMode"
            />
          </div>

          <div class="desc-row">
            <input
              v-if="isOwner && editMode"
              v-model="descriptionDraft"
              type="text"
              class="desc-input"
              placeholder="자기소개를 입력하세요"
              :maxlength="255"
            />
            <p v-else class="description">
              {{ profileInfo?.description || '해당 회원은 아직 자기소개를 작성하지 않았습니다.' }}
            </p>
          </div>

          <p class="stats-row">
            <button type="button" class="follow-link" @click="openModal('following')">
              팔로잉 {{ profileFollowings?.length ?? 0 }}
            </button>
            <button type="button" class="follow-link" @click="openModal('follower')">
              팔로워 {{ profileFollowers?.length ?? 0 }}
            </button>
          </p>
        </div>
      </div>

      <div class="profile-header-right">
        <DeleteProfileImageButton
          v-if="editMode && profileInfo?.profileImageUrl"
          @click="deleteProfileImage"
        />
        <button
            class="btn"
            @click="goToBadgePage"
          >뱃지</button>
        <EditButton
          v-if="isOwner"
          :is-editing="editMode"
          :disabled="editMode && isOverLimit"
          @click="toggleEditMode"
          class="btn"
        />
        <FollowButton
          v-else
          :profile-info="profileInfo"
          :profile-followers="profileFollowers"
        />
      </div>

      <UserListModal
        v-if="showFollowModal"
        :userList="modalList"
        @close="showFollowModal = false"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, unref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import EditButton from './EditButton.vue';
import FollowButton from './FollowButton.vue';
import DeleteProfileImageButton from './DeleteProfileImageButton.vue';
import UserListModal from './UserListModal.vue';
import BadgeShelf from './BadgeShelf.vue';
import { useProfileStore } from '@/stores/profile';
import axios from 'axios';
import axiosApi from '@/api/axiosApi';

const props = defineProps({
  profileInfo: {
    type: Object,
    required: false
  },
  profileEmail: {
    type: String,
    required: false
  },
  profileFollowings: {
    type: Array,
    default: () => []
  },
  profileFollowers: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits([
  'updateProfileImageUrl',
  'description-updated'
]);

const route = useRoute();
const router = useRouter();
const profileStore = useProfileStore();
const userInfo = JSON.parse(localStorage.getItem('userInfo'));
const isOwner = computed(() => userInfo?.email === route.params.id);
const memberId = computed(() => props.profileInfo?.memberId ?? null);

const editMode = ref(false);
const canEdit = computed(() => isOwner.value && editMode.value);

// 프로필 자기소개 수정
const descriptionDraft = ref('');

watch(editMode, (on) => {
  if (on) descriptionDraft.value = props.profileInfo?.description ?? '';
});

const isWhitespaceOnly = computed(() => /^\s*$/.test(descriptionDraft.value ?? ''));
const isUnchanged = computed(() => (descriptionDraft.value ?? '') === (props.profileInfo?.description ?? ''));
const isOverLimit = computed(() => (descriptionDraft.value?.length ?? 0) > 255);

const toggleEditMode = async () => {
  if (editMode.value) {
    if (isOverLimit.value) return;

    if (isUnchanged.value) {
      editMode.value = false;
      return;
    }

    if (isWhitespaceOnly.value) {
      editMode.value = false;
      return;
    }

    try {
      await profileStore.updateDescription(descriptionDraft.value);
      emit('description-updated', descriptionDraft.value);
      editMode.value = false;
    } catch (err) {
      console.error('자기소개 수정 실패:', err);
    }
  } else {
    editMode.value = true;
  }
};

const profileImageInput = ref(null);

const triggerProfileImageUpload = async () => {
  if (!canEdit.value) return;
  profileImageInput.value.value = '';
  profileImageInput.value.click();
};

const uploadProfileImage = async (e) => {
  if (!canEdit.value) return;
  const file = e.target.files[0];
  const mid = Number(unref(memberId));
  if (!file || !mid) return;

  try {
    const { data } = await axiosApi.post('/profiles/presignedUrl', {
      memberId: mid,
      originalFilename: file.name,
      contentType: file.type,
    });

    const presignedData = data.data;
    if (!presignedData.uploadUrl || !presignedData.s3Key) {
      console.error('presignedUrl 또는 s3Key가 없습니다', data);
      return;
    }

    await axios.put(presignedData.uploadUrl, file, {
      headers: { 'Content-Type': file.type },
      maxContentLength: Infinity,
      maxBodyLength: Infinity,
    });

    await axiosApi.post('/profiles/complete', {
      memberId: mid,
      originalFilename: file.name,
      s3Key: presignedData.s3Key,
    });

    emit('updateProfileImageUrl');
  } catch (err) {
    console.error('업로드 실패:', err);
  }
};

const deleteProfileImage = async () => {
  if (!canEdit.value) return;
  try {
    await axiosApi.delete('/profiles/image');
    emit('updateProfileImageUrl');
  } catch (err) {
    console.error('프로필 이미지 삭제 실패: ', err);
  }
};

const showFollowModal = ref(false);
const modalType = ref(null);
const openModal = (type) => {
  modalType.value = type;
  showFollowModal.value = true;
};
const modalList = computed(() => (modalType.value === 'following' ? props.profileFollowings : props.profileFollowers));

function goToBadgePage() {
  if (memberId.value) {
    router.push({
      path: '/badge',
      state: { profilePk: memberId.value }
    });
  }
};
</script>

<style scoped>
.profile-header {
  margin-top: 15px;
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
  width: 100%;
  font-family: 'Pretendard', sans-serif;
  color: white;
  background: url('/assets/space-bg.jpg') center/cover no-repeat;
  padding: 20px 0;
}

.profile-header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 10px 35px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.profile-header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 30px;
}

.profile-image-container {
  position: relative;
}

.profile-image {
  position: relative;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(15, 20, 40, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: default;
  transition: border-color 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  opacity: 0.9;
}
.profile-image.editable {
  cursor: pointer;
}
.profile-image.editable:hover {
  border-color: rgba(255, 255, 255, 0.9);
}

.profile-image img,
.profile-image > span {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  color: #ccc;
  font-size: 28px;
  border-radius: 50%;
  text-align: center;
}

.edit-mode-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  z-index: 1;
}

.edit-mode-overlay span {
  font-size: 34px;
  color: white;
  line-height: 1;
}

.btn,
.follow-link {
  background: rgba(15, 20, 40, 0.4);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.7);
  padding: 6px 14px;
  border-radius: 6px;
  backdrop-filter: blur(6px);
  font-size: 15px;
  cursor: pointer;
  transition: background 0.3s ease;
  box-shadow: none;
  font-weight: 400;
  text-decoration: none;
}

.btn:hover,
.follow-link:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-color: rgba(255, 255, 255, 0.9);
}

.profile-text {
  display: flex;
  flex-direction: column;
  font-size: 16px;
  color: white;
  gap: 8px;
}

.email-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: 0.2px;
}

.desc-row {
  min-height: 40px;
}

.description {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  font-size: 15px;
  line-height: 1.4;
}

.desc-input {
  width: 100%;
  max-width: 400px;
  box-sizing: border-box;
  padding: 6px 12px;
  font-size: 15px;
  color: white;
  background: rgba(15, 20, 40, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  outline: none;
  transition: all 0.2s ease;
  line-height: 1.2;
}

.desc-input:focus {
  border-color: rgba(255, 255, 255, 0.9);
  transform: translateY(-1px);
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 0;
  font-size: 14px;
}

.stats-row button.follow-link {
  height: 32px;
  line-height: 32px;
  padding: 0 12px;
  font-size: 14px;
}
</style>