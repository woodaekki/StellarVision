<template>
  <div class="profile-header">
    <div class="profile-header-inner">
      <div class="profile-header-left">
        <div class="profile-image" @click="triggerProfileImageUpload">
          <img v-if="profileInfo?.profileImageUrl" :src="profileInfo.profileImageUrl" />
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

        <div class="profile-text">
          <div class="email-row">
            <span class="email">{{ profileEmail || 'null' }}</span>
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
            />
            <p v-else class="description">
              {{ profileInfo?.description || '해당 회원은 아직 자기소개를 작성하지 않았습니다.' }}
            </p>
          </div>

          <p class="stats-row">
            <button type="button" class="follow-link" @click="openModal('following')">
              팔로잉 {{ profileFollowings?.length ?? 0 }}
            </button>
            <span class="stats-gap"></span>
            <button type="button" class="follow-link" @click="openModal('follower')">
              팔로워 {{ profileFollowers?.length ?? 0 }}
            </button>
          </p>
        </div>
      </div>

      <div class="profile-header-right">
        <DeleteProfileImageButton
          v-if="editMode"
          @click="deleteProfileImage"
        />
        <EditButton
          v-if="isOwner"
          :is-editing="editMode"
          @click="toggleEditMode"
          class="edit-button"
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
import { useRoute } from 'vue-router';
import EditButton from './EditButton.vue';
import FollowButton from './FollowButton.vue';
import DeleteProfileImageButton from './DeleteProfileImageButton.vue';
import UserListModal from './UserListModal.vue';
import BadgeShelf from './BadgeShelf.vue';
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

const route = useRoute();
const isOwner = JSON.parse(localStorage.getItem('userInfo'))?.email === route.params.id;
const memberId = computed(() => props.profileInfo?.memberId ?? null);

// 편집 모드 전환
const editMode = ref(false);
const toggleEditMode = () => { editMode.value = !editMode.value };
const canEdit = computed(() => isOwner && editMode.value);

// 프로필 자기소개 수정
const descriptionDraft = ref('');
watch(editMode, (on) => {
  if (on) descriptionDraft.value = props.profileInfo?.description ?? '';
});

// 프로필 이미지 업데이트
const profileImageInput = ref(null);
const emit = defineEmits(['updateProfileImageUrl']);

const triggerProfileImageUpload = async () => {
  if (!canEdit.value) return;
  profileImageInput.value.value = '';
  profileImageInput.value.click();
};

// 프로필 이미지 업데이트
const uploadProfileImage = async (e) => {
  if (!canEdit.value) return;
  const file = e.target.files[0];
  const mid = Number(unref(memberId));
  if (!file || !mid) return;

  try {
    const { data } = await axiosApi.post('/profiles/presignedUrl', {
      memberId: mid,
      originalFilename: file.name,
    });

    const presignedData = data.data

    if (!presignedData.uploadUrl || !presignedData.s3Key) {
      console.error('presignedUrl 또는 s3Key가 없습니다', data);
      return
    };

    const uploadRes = await axios.put(presignedData.uploadUrl, file, {
      headers: {
        'Content-Type': file.type
      },
      maxContentLength: Infinity,
      maxBodyLength: Infinity,
    });

    const saveRes = await axiosApi.post('/profiles/complete', {
      memberId: mid,
      originalFilename: file.name,
      s3Key: presignedData.s3Key,
    });

    emit('updateProfileImageUrl');

  } catch (err) {
    console.error('업로드 실패:', err)
  }
}

const deleteProfileImage = async () => {
  if (!canEdit.value) return;

  try {
    await axiosApi.delete('/profiles/image');
    emit('updateProfileImageUrl');
  } catch (err) {
    console.error('프로필 이미지 삭제 실패: ', err)
  }
}

// 팔로잉 & 팔로워 목록 보여주기
const showFollowModal = ref(false);
const modalType = ref(null);

const openModal = (type) => {
  modalType.value = type;
  showFollowModal.value = true;
};

const modalList = computed(() =>
  modalType.value === 'following' ? props.profileFollowings : props.profileFollowers
);
</script>

<style scoped>
.profile-header {
  background-color: #414147;
  background: linear-gradient(135deg, #4d4949 0%, #3e3a3a 100%);
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
}

.profile-header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 12px;
  padding-right: 36px;
  padding-top: 12px;
  padding-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.profile-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-image {
  position: relative;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #444;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s ease;
  margin-top: 8px;
  margin-bottom: 8px;
}

.profile-image img,
.profile-image > span {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  color: #ccc;
  font-size: 28px;
}

/* 편집 모드 오버레이 */
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

.edit-button {
  background: #505055;
  color: #fff;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.edit-button:hover {
  background: #545459;
}

.profile-text {
  display: flex;
  flex-direction: column;
  font-size: 16px;
  color: #fff;
}

.profile-text > * {
  margin-bottom: 2px;
}

.profile-text > *:last-child {
  margin-bottom: 0;
}

/* 이메일 + 배지 한 줄 */
.email-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.2px;
}

/* 자기소개 영역 */
.desc-row {
  display: flex;
  margin-left: 3px;
  align-items: center;
  min-height: 40px;
}

.description {
  margin: 0;
  color: #cfd1d6;
  font-size: 15px;
  line-height: 1.4;
}

/* 입력창 스타일 */
.desc-input {
  width: 100%;
  max-width: 400px;
  height: 40px;
  box-sizing: border-box;
  padding: 6px 12px;
  font-size: 15px;
  color: #fff;
  background: #3a3a3f;
  border: 1px solid #525257;
  border-radius: 8px;
  outline: none;
  transition: all 0.2s ease;
  margin-bottom: 3px;
}

.desc-input:focus {
  border-color: #7a7a80;
  transform: translateY(-1px);
}

/* 팔로잉/팔로워 줄 */
.stats-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 14px;
}

/* 칩 스타일 버튼 */
.follow-link {
  background: #5a5a63;
  color: #e2e2e7;
  border: 1px solid #6c6c77;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1;
  transition: all 0.2s ease;
  cursor: pointer;
  margin-top: 1px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.follow-link:hover {
  background: #6c6c77;
  color: #ffffff;
  border-color: #888892;
  transform: translateY(-1px);
}

/* ===== 반응형 ===== */
@media (max-width: 768px) {
  .profile-header-inner {
    flex-direction: column;
    align-items: center;
    padding-left: 16px;
    padding-right: 16px;
    gap: 10px;
  }

  .profile-header-left {
    flex-direction: column;
    gap: 10px;
    align-items: center;
  }

  .profile-image {
    width: 110px;
    height: 110px;
  }

  .email-row {
    font-size: 20px;
    text-align: center;
  }

  .desc-input,
  .description {
    font-size: 14px;
    max-width: 100%;
    text-align: center;
  }

  .follow-link {
    padding: 4px 8px;
    font-size: 13px;
  }

  .profile-header-right {
    width: 100%;
    justify-content: center;
    gap: 6px;
  }

  .stats-row {
    justify-content: center;
    gap: 6px;
    margin-top: 8px;
  }
}

</style>
