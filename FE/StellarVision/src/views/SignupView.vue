<template>
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
          class="input-field-inline"
        />
        <button
          type="button"
          @click="sendVerificationCode"
          :disabled="!isEmailValid || emailVerificationSent || emailVerified"
          class="black-button small-button"
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
          class="input-field-inline"
        />
        <button
          type="button"
          @click="verifyCode"
          :disabled="!formData.verificationCode || isVerifying"
          class="black-button small-button"
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
        <p v-if="formData.password2 && !isPasswordMatched" class="error-message">
          비밀번호가 일치하지 않습니다.
        </p>
      </div>

      <div class="input-group">
        <input
          type="date"
          v-model="formData.birthday"
          class="input-field"
        />
      </div>

      <button
        type="submit"
        :disabled="!canSubmit"
        class="black-button large-button"
        :class="{ 'button-disabled': !canSubmit }"
      >
        가입하기
      </button>
    </form>
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
})

const accountStore = useAccountStore()

const emailVerificationSent = ref(false)
const emailVerified = ref(false)
const isVerifying = ref(false)
const verificationError = ref('')

const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(formData.email)
})

const isPasswordMatched = computed(() => {
  return formData.password1 && formData.password1 === formData.password2
})

const canSubmit = computed(() => {
  return emailVerified.value &&
           formData.name &&
           isPasswordMatched.value &&
           formData.birthday
})

const sendVerificationCode = async () => {
  if (!isEmailValid.value) {
    alert('유효한 이메일 주소를 입력해주세요.')
    return
  }

  const result = await accountStore.sendEmailVerificationCode(formData.email)

  if (result.success) {
    emailVerificationSent.value = true
    verificationError.value = ''
    formData.verificationCode = ''
    alert(`${formData.email}로 인증코드가 전송되었습니다.`)
  } else {
    alert(result.message)
  }
}

const verifyCode = async () => {
  if (!formData.verificationCode) {
    verificationError.value = '인증코드를 입력해주세요.'
    return
  }

  isVerifying.value = true
  verificationError.value = ''

  const result = await accountStore.verifyEmailCode(formData.email, formData.verificationCode)

  if (result.success) {
    emailVerified.value = true
    emailVerificationSent.value = false
    verificationError.value = ''
    alert('이메일 인증이 완료되었습니다!')
  } else {
    verificationError.value = result.message
  }

  isVerifying.value = false
}

const onSignUp = async () => {
  if (!emailVerified.value) {
    alert('이메일 인증을 완료해주세요.')
    return
  }

  if (!isPasswordMatched.value) {
    alert('비밀번호가 일치하지 않습니다.')
    return
  }

  if (!formData.name) {
    alert('닉네임을 입력해주세요.')
    return
  }

  if (!formData.birthday) {
    alert('생년월일을 입력해주세요.')
    return
  }

  const userInfo = {
    email: formData.email,
    name: formData.name,
    password: formData.password1,
    birth: formData.birthday
  }

  const result = await accountStore.signUp(userInfo)

  if (!result.success) {
    alert(result.message)
  }
}
</script>

<style scoped>
.form-container {
  width: 100%;
  max-width: 341px;
  margin: 0 auto;
  background-image: #f2f2f2;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-title {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 26px;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-group {
  display: flex;
  flex-direction: column;
}

.input-group-row {
  display: flex;
  gap: 8px;
}

.input-field {
  width: 100%;
  padding: 8px 13px;
  border: 1px solid #d1d5db;
  border-radius: 5px;
  font-size: 12px;
}

.input-field-inline {
  flex-grow: 1;
  padding: 8px 13px;
  border: 1px solid #d1d5db;
  border-radius: 5px;
  font-size: 12px;
}

.black-button {
  background-color: black;
  color: white;
  border-radius: 5px;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.small-button {
  padding: 8px 13px;
  font-size: 10px;
  white-space: nowrap;
}

.large-button {
  width: 100%;
  padding: 10px;
  margin-top: 8px;
  font-size: 12px;
  font-weight: bold;
}

.button-disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
  color: #6b7280;
}

.error-message {
  color: #ef4444;
  font-size: 9px;
  margin-top: 2px;
}

.info-message {
  color: #4b5563;
  font-size: 9px;
  margin-top: 2px;
}

.relative-container {
  position: relative;
}

.check-icon {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #16a34a;
  font-size: 16px;
}

.success-message {
  color: #16a34a;
  font-size: 9px;
}

@media (min-width: 768px) {
  .form-container {
    max-width: 512px;
    padding: 64px 16px;
  }

  .form-title {
    font-size: 30px;
    margin-top: 15px;
    margin-bottom: 40px;
  }

  .form-body {
    gap: 16px;
  }

  .input-group-row {
    gap: 12px;
  }

  .input-field,
  .input-field-inline {
    padding: 12px 20px;
    border-radius: 8px;
    font-size: 18px;
  }

  .black-button {
    border-radius: 8px;
  }

  .small-button {
    padding: 12px 20px;
    font-size: 16px;
  }

  .large-button {
    padding: 16px;
    margin-top: 12px;
    font-size: 18px;
  }

  .error-message,
  .info-message,
  .success-message {
    font-size: 14px;
    margin-top: 4px;
  }

  .check-icon {
    right: 16px;
    font-size: 24px;
  }
}
</style>
