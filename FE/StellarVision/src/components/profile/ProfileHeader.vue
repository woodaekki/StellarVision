<template>
  <div class="profile-header">
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

    <UserListModal
      v-if="showFollowModal"
      :userList="modalList"
      @close="showFollowModal = false"
    />
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
      />
      <FollowButton
        v-else
        :profile-info="profileInfo"
        :profile-followers="profileFollowers"
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
    // console.log('파일 이름: ', file.name, '\n멤버 id: ', memberId)

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
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 24px 28px;
  background-color: #2e2e32;
}

.profile-header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.profile-header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-image {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #444;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.profile-image img,
.profile-image > span {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
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
  width: auto;
  height: auto;
  line-height: 1;
}

.profile-text {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 기본 문단 */
.profile-text p {
  margin: 0;
  color: #fff;
  line-height: 1.4;
}

/* 이메일 + 배지 한 줄 */
.email-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: -6px;
}

.email {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: .2px;
}

.desc-row {
  --desc-h: 40px;
  display: flex;
  align-items: center;
  min-height: var(--desc-h);
}

.description {
  color: #cfd1d6;
  font-size: 15px;
  line-height: var(--desc-h);
  margin: 0;
}

.desc-input {
  width: 100%;
  height: var(--desc-h);
  box-sizing: border-box;
  padding: 0 12px;
  font-size: 15px;
  line-height: var(--desc-h);
  color: #fff;
  background: #3a3a3f;
  border: 1px solid #525257;
  border-radius: 8px;
  outline: none;
}
.desc-input:focus {
  border-color: #7a7aff;
  box-shadow: 0 0 0 3px rgba(122,122,255,0.2);
}


/* 팔로잉/팔로워 줄 */
.stats-row {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-top: 6px;
}

.stats-gap { width: 8px; }

/* 칩 스타일 버튼 */
.follow-link {
  background: #3a3a3f;
  color: #ddd;
  border: 1px solid #4a4a50;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 14px;
  line-height: 1;
  transition: background-color .15s ease, color .15s ease, border-color .15s ease;
  pointer-events: auto;
}
.follow-link:hover {
  background: #4a4a50;
  color: #fff;
  border-color: #5a5a60;
}

/* 기타 */
.not-editable { cursor: default; }

@media (max-width: 640px) {
  .email { font-size: 18px; }
  .desc-input { font-size: 14px; }
  .follow-link { padding: 5px 10px; font-size: 13px; }
}
</style>

