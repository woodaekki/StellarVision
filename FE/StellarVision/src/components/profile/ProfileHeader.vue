<template>
 <div class="profile-header">
    <div class="profile-header-left">
      <div class="profile-image" @click="triggerProfileImageUpload">
        <img v-if="profileInfo?.profileImageUrl"
        :src="profileInfo.profileImageUrl"
        />
        <span v-else>+</span>
        <div
          class="edit-mode-overlay"
          v-if="editMode"
        >
          <span>+</span>
        </div>
      </div>
      <input
        type="file"
        ref="profileImageInput"
        @change="uploadProfileImage"
        accept="image/*"
        style="display: none;"
      />

      <div class="profile-text">
        <p>{{ profileEmail || 'null' }}</p>
        <ProfileEdit @click="toggleEditMode" />
        <p>{{ profileInfo?.description || '해당 회원은 아직 자기소개를 설정하지 않았습니다.' }}</p>
      </div>

    </div>

    <div class="profile-header-right">
      <RouterLink :to="`/profile/${profileEmail}/edit`">
        수정
      </RouterLink>
    </div>
 </div>
</template>

<script setup>
import { ref } from 'vue';
import { RouterLink } from 'vue-router';
import ProfileEdit from './ProfileEdit.vue';
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
  }
});

const memberId = props.profileInfo?.memberId;

// 편집 모드 전환
const editMode = ref(false);

function toggleEditMode() {
  editMode.value = !editMode.value;
  console.log('편집 모드:', editMode.value);
};

// 프로필 이미지 업데이트
const profileImageInput = ref(null);
const emit = defineEmits(['updateProfileImageUrl']);

const triggerProfileImageUpload = async () => {
  profileImageInput.value.value = '';
  profileImageInput.value.click();
};

// 프로필 이미지 업데이트
const uploadProfileImage = async (e) => {
  const file = e.target.files[0];
  if (!file || !memberId ) return

  try {
    const { data } = await axiosApi.post('/profiles/presignedUrl', {
      memberId: memberId,
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

    // console.log('✅ S3 업로드 성공:', uploadRes.status, uploadRes.statusText)
    // console.log(presignedData.s3Key)

    const saveRes = await axiosApi.post('/profiles/complete', {
      memberId: memberId,
      originalFilename: file.name,
      s3Key: presignedData.s3Key,
    });

    // console.log('✅ DB에 프로필 이미지 정보 저장 완료:', saveRes.data)
    emit('updateProfileImageUrl');

  } catch (err) {
    console.error('업로드 실패:', err)
  }
}
</script>

<style scoped>
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background-color: #2e2e32;
}

.profile-header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-image {
  position: relative;
  width: 80px;
  height: 80px;
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
}

.profile-text p {
  margin: 0;
  color: #fff;
  line-height: 1.4;
}

.profile-text p:first-child {
  font-size: 18px;
  font-weight: 600;
}

.profile-text p:last-child {
  font-size: 14px;
  color: #bbb;
  margin-top: 4px;
}

.profile-header-right a {
  color: #fff;
  text-decoration: none;
}

.profile-header-right a:hover {
  text-decoration: underline;
}
</style>
