<template>
  <div class="page">
    <div class="stars-background">
      <div class="form-container">
        <h2 class="form-title">회원가입</h2>

        <form @submit.prevent="onSignUp" class="form-body">
          <div class="input-group-row">
            <input
              type="email"
              id="email"
              placeholder="이메일 입력"
              v-model="formData.email"
              :disabled="emailVerified"
              class="input-field"
            />
            <button
              type="button"
              @click="sendVerificationCode"
              :disabled="!isEmailValid || emailVerificationSent || emailVerified"
              class="submit-button small-button"
              :class="{ 'button-disabled': !isEmailValid || emailVerificationSent || emailVerified }"
            >
              {{ emailVerified ? '인증완료' : emailVerificationSent ? '재전송' : '인증' }}
            </button>
          </div>
          <p v-if="!isEmailValid && formData.email" class="error-message">
            유효한 이메일 주소를 입력해주세요.
          </p>
          <p v-if="emailVerificationSent && !emailVerified" class="info-message">
            {{ formData.email }}로 인증코드가 전송되었습니다.
          </p>

          <div v-if="emailVerificationSent && !emailVerified" class="input-group-row">
            <input
              type="text"
              placeholder="인증코드 입력"
              v-model="formData.verificationCode"
              maxlength="6"
              class="input-field"
            />
            <button
              type="button"
              @click="verifyCode"
              :disabled="!formData.verificationCode || isVerifying"
              class="submit-button small-button"
              :class="{ 'button-disabled': !formData.verificationCode || isVerifying }"
            >
              {{ isVerifying ? '확인중' : '확인' }}
            </button>
          </div>
          <p v-if="verificationError" class="error-message">{{ verificationError }}</p>

          <div v-if="emailVerified">
            <p class="success-message">✓ 이메일 인증이 완료되었습니다.</p>
          </div>

          <div class="input-group">
            <input
              type="text"
              placeholder="닉네임"
              v-model="formData.name"
              class="input-field"
            />
          </div>

          <div class="input-group">
            <input
              type="password"
              placeholder="비밀번호"
              v-model="formData.password1"
              class="input-field"
            />
          </div>

          <div class="input-group">
            <div class="relative-container">
              <input
                type="password"
                placeholder="비밀번호 확인"
                v-model="formData.password2"
                class="input-field"
              />
              <div v-if="formData.password2 && isPasswordMatched" class="check-icon">
                ✓
              </div>
            </div>
            <p v-if="formData.password2 && !isPasswordMatched" class="error-message">
              비밀번호가 일치하지 않습니다.
            </p>
          </div>

          <div class="input-group">
            <input
              type="date"
              v-model="formData.birthday"
              class="input-field date-input"
            />
          </div>

          <button
            type="submit"
            :disabled="!canSubmit"
            class="submit-button large-button"
            :class="{ 'button-disabled': !canSubmit }"
          >
            가입하기
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAccountStore } from '@/stores/account';
import { computed, reactive, ref } from 'vue';

const formData = reactive({
  email: '',
  name: '',
  password1: '',
  password2: '',
  birthday: '',
  verificationCode: ''
});

const accountStore = useAccountStore();

const emailVerificationSent = ref(false);
const emailVerified = ref(false);
const isVerifying = ref(false);
const verificationError = ref('');

const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(formData.email);
});

const isPasswordMatched = computed(() => {
  return formData.password1 && formData.password1 === formData.password2;
});

const canSubmit = computed(() => {
  return emailVerified.value &&
    formData.name &&
    isPasswordMatched.value &&
    formData.birthday;
});

const sendVerificationCode = async () => {
  if (!isEmailValid.value) {
    alert('유효한 이메일 주소를 입력해주세요.');
    return;
  }

  const result = await accountStore.sendEmailVerificationCode(formData.email);

  if (result.success) {
    emailVerificationSent.value = true;
    verificationError.value = '';
    formData.verificationCode = '';
    alert(`${formData.email}로 인증코드가 전송되었습니다.`);
  } else {
    alert(result.message);
  }
};

const verifyCode = async () => {
  if (!formData.verificationCode) {
    verificationError.value = '인증코드를 입력해주세요.';
    return;
  }

  isVerifying.value = true;
  verificationError.value = '';

  const result = await accountStore.verifyEmailCode(formData.email, formData.verificationCode);

  if (result.success) {
    emailVerified.value = true;
    emailVerificationSent.value = false;
    verificationError.value = '';
    alert('이메일 인증이 완료되었습니다!');
  } else {
    verificationError.value = result.message;
  }

  isVerifying.value = false;
};

const onSignUp = async () => {
  if (!emailVerified.value) {
    alert('이메일 인증을 완료해주세요.');
    return;
  }

  if (!isPasswordMatched.value) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }

  if (!formData.name) {
    alert('닉네임을 입력해주세요.');
    return;
  }

  if (!formData.birthday) {
    alert('생년월일을 입력해주세요.');
    return;
  }

  const userInfo = {
    email: formData.email,
    name: formData.name,
    password: formData.password1,
    birth: formData.birthday
  };

  const result = await accountStore.signUp(userInfo);

  if (!result.success) {
    alert(result.message);
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

.page {
  min-height: 100vh;
  background-size: cover;
  background-image: linear-gradient(rgba(11, 12, 16, 0.7), rgba(11, 12, 16, 0.7));
  background-position: center;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: 'Pretendard', 'Noto Sans KR', sans-serif;
  color: rgba(255, 255, 255, 0.9);
  position: relative;
}

.page::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('@/assets/pictures/stellabot/spaceBackground.avif') no-repeat center center/cover;
  z-index: -2;
}

.page::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(11, 12, 16, 0.7);
  z-index: -1;
}

.stars-background {
  position: relative;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 50px 40px;
  max-width: 500px;
  width: 100%;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow:
    inset 4px 4px 10px rgba(255 255 255 / 0.6),
    inset -4px -4px 10px rgba(0 0 0 / 0.15);
  border: 1.5px solid rgba(255 255 255 / 0.25);
  transition: box-shadow 0.3s ease;
  margin: auto;
}

.stars-background:hover {
  box-shadow:
    inset 6px 6px 14px rgba(255 255 255 / 0.85),
    inset -6px -6px 14px rgba(0 0 0 / 0.2);
}

.form-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0;
}

.form-title {
  font-size: 2.5rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 20px;
  color: #fff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.form-body {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  width: 100%;
}

.input-group-row {
  width: 100%;
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.input-field {
  flex-grow: 1;
  padding: 16px 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  font-size: 1rem;
  color: #fff;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.input-field::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.input-field:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.6);
  background-color: rgba(255, 255, 255, 0.2);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.2);
}

.date-input {
  color: rgba(255, 255, 255, 0.9);
}

.date-input::-webkit-calendar-picker-indicator {
  filter: invert(1);
}

.submit-button {
  background-color: #007bff;
  color: white;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1.1rem;
  cursor: pointer;
  border: none;
  transition: all 0.3s ease;
}

.submit-button:hover {
  background-color: #0056b3;
  box-shadow: 0 0 15px rgba(0, 123, 255, 0.5);
  transform: translateY(-2px);
}

.small-button {
  padding: 16px 20px;
  font-size: 1rem;
  flex-grow: 0;
  white-space: nowrap;
}

.large-button {
  width: 100%;
  padding: 16px;
  margin-top: 15px;
}

.button-disabled {
  background-color: rgba(0, 123, 255, 0.5);
  cursor: not-allowed;
  color: rgba(255, 255, 255, 0.8);
  box-shadow: none;
  transform: none;
}

.error-message {
  color: #ff6b6b;
  font-size: 0.9rem;
  margin-top: -10px;
  text-align: left;
  background-color: rgba(255, 107, 107, 0.1);
  border-radius: 8px;
  padding: 10px;
}

.info-message {
  color: #a0a0a0;
  font-size: 0.9rem;
  margin-top: -10px;
  text-align: left;
  background-color: rgba(160, 160, 160, 0.1);
  border-radius: 8px;
  padding: 10px;
}

.success-message {
  color: #28a745;
  font-size: 0.9rem;
  margin-top: -10px;
  text-align: left;
  background-color: rgba(40, 167, 69, 0.1);
  border-radius: 8px;
  padding: 10px;
}

.relative-container {
  position: relative;
  width: 100%;
}

.check-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #16a34a;
  font-size: 1.5rem;
}
</style>