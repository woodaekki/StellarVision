<template>
  <div class="signupContainer">
    <h2>회원가입</h2>
    <form class="signupForm" @submit.prevent="onSignUp">
      <!-- 이메일 입력 -->
      <div class="formGroup email-group">
        <input
          type="text"
          id="userid"
          placeholder="아이디 입력"
          v-model="formData.userId"
          :disabled="emailVerified"
        />
        <select
          class="box"
          id="email-box"
          v-model="formData.userDomain"
          :disabled="emailVerified"
        >
          <option disabled value="">이메일을 선택해주세요</option>
          <option value="naver.com">@naver.com</option>
          <option value="gmail.com">@gmail.com</option>
          <option value="hanmail.net">@hanmail.net</option>

        </select>
      </div>

      <!-- 이메일 인증 버튼 -->
      <div class="formGroup">
        <button
          type="button"
          class="btn-verify"
          @click="sendVerificationCode"
          :disabled="!isEmailValid || emailVerificationSent || emailVerified"
        >
          {{ emailVerified ? '인증완료' : emailVerificationSent ? '인증코드 재전송' : '인증코드 전송' }}
        </button>
        <p v-if="!isEmailValid && formData.userId && formData.userDomain" class="error">
          유효한 이메일 주소를 입력해주세요.
        </p>
        <p v-if="emailVerificationSent && !emailVerified" class="info">
          {{ email }}로 인증코드가 전송되었습니다.
        </p>
      </div>

      <!-- 인증코드 입력 (이메일 인증코드 전송 후 표시) -->
      <div v-if="emailVerificationSent && !emailVerified" class="formGroup">
        <div class="verification-group">
          <input
            type="text"
            id="verification-code"
            placeholder="인증코드 입력"
            v-model="formData.verificationCode"
            maxlength="6"
          />
          <button
            type="button"
            class="btn-verify-code"
            @click="verifyCode"
            :disabled="!formData.verificationCode || isVerifying"
          >
            {{ isVerifying ? '확인중...' : '확인' }}
          </button>
        </div>
        <p v-if="verificationError" class="error">
          {{ verificationError }}
        </p>
      </div>

      <!-- 인증 성공 메시지 -->
      <div v-if="emailVerified" class="formGroup">
        <p class="success">✓ 이메일 인증이 완료되었습니다.</p>
      </div>

      <!-- 닉네임 입력 -->
      <div class="formGroup">
        <input
          type="text"
          id="name"
          placeholder="닉네임명"
          v-model="formData.name"
        />
      </div>

      <!-- 비밀번호 입력 -->
      <div class="formGroup">
        <input
          type="password"
          id="password"
          placeholder="비밀번호"
          v-model="formData.password1"
        />
      </div>

      <!-- 비밀번호 확인 -->
      <div class="formGroup">
        <input
          type="password"
          id="password-confirm"
          placeholder="비밀번호 확인"
          v-model="formData.password2"
        />
        <p
          v-if="formData.password2 && !isPasswordMatched"
          class="error"
        >
          비밀번호가 일치하지 않습니다.
        </p>
        <p
          v-else-if="formData.password2 && isPasswordMatched"
          class="success"
        >
          비밀번호가 일치합니다.
        </p>
      </div>

      <!-- 생일 입력 -->
      <div class="formGroup">
        <input
          type="date"
          id="birth"
          v-model="formData.birthday"
        />
      </div>

      <button
        type="submit"
        class="btn"
        :disabled="!canSubmit"
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
  userId: '',
  userDomain: '',
  name: '',
  password1: '',
  password2: '',
  birthday: '',
  verificationCode: ''
})

const accountStore = useAccountStore()

// 이메일 인증 관련 상태
const emailVerificationSent = ref(false)
const emailVerified = ref(false)
const isVerifying = ref(false)
const verificationError = ref('')

// 이메일 유효성 검증
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email.value)
})

// 비밀번호 일치 여부 확인
const isPasswordMatched = computed(() => {
  return formData.password1 && formData.password1 === formData.password2
})

// 완성된 이메일 주소
const email = computed(() => {
  return formData.userDomain
    ? `${formData.userId}@${formData.userDomain}`
    : ''
})

// 회원가입 버튼 활성화 조건
const canSubmit = computed(() => {
  return emailVerified.value &&
         formData.name &&
         isPasswordMatched.value &&
         formData.birthday
})

// 인증코드 전송
const sendVerificationCode = async () => {
  if (!isEmailValid.value) {
    alert('유효한 이메일 주소를 입력해주세요.')
    return
  }

  const result = await accountStore.sendEmailVerificationCode(email.value)

  if (result.success) {
    emailVerificationSent.value = true
    verificationError.value = ''
    formData.verificationCode = ''
    alert(`${email.value}로 인증코드가 전송되었습니다.`)
  } else {
    alert(result.message)
  }
}

// 인증코드 검증
const verifyCode = async () => {
  if (!formData.verificationCode) {
    verificationError.value = '인증코드를 입력해주세요.'
    return
  }

  isVerifying.value = true
  verificationError.value = ''

  const result = await accountStore.verifyEmailCode(email.value, formData.verificationCode)

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

// 회원가입 요청 함수
const onSignUp = async () => {
  // 이메일 인증 확인
  if (!emailVerified.value) {
    alert('이메일 인증을 완료해주세요.')
    return
  }

  // 비밀번호 확인
  if (!isPasswordMatched.value) {
    alert('비밀번호가 일치하지 않습니다.')
    return
  }

  // 필수 정보 확인
  if (!formData.name) {
    alert('닉네임을 입력해주세요.')
    return
  }

  if (!formData.birthday) {
    alert('생년월일을 입력해주세요.')
    return
  }

  const userInfo = {
    email: email.value,
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

<style>
.signupContainer {
  max-width: 400px;
  margin: 0 auto;
  background-image: url(@/assets//pictures/wallpaper/space.jpeg); 
  padding-top: 58px; 
}

.email-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.verification-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.verification-group input {
  flex: 1;
}

.formGroup {
  margin: 16px 0;
}

.btn {
  width: 100%;
  padding: 12px;
  background: #2C2C2C;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-verify {
  width: 100%;
  padding: 10px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 8px;
}

.btn-verify:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-verify-code {
  padding: 10px 16px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
}

.btn-verify-code:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.error {
  color: #e74c3c;
  font-size: 0.9em;
  margin-top: 4px;
}

.success {
  color: #2ecc71;
  font-size: 0.9em;
  margin-top: 4px;
}

.info {
  font-size: 0.8em;
  color: #555;
  margin-top: 4px;
}

input {
  display: flex;
  width: 400px;
  height: 40px;
  padding: 8px 16px;
  align-items: center;
  gap: 16px;
  border-radius: 8px;
  border: 1px solid #E0E0E0;
  background: #FFF;
}

input:disabled {
  background: #f5f5f5;
  color: #666;
}

.box {
  display: flex;
  width: 400px;
  height: 40px;
  padding: 8px 16px;
  align-items: center;
  gap: 16px;
  border-radius: 8px;
  border: 1px solid #E0E0E0;
  background: #FFF;
}

.box:disabled {
  background: #f5f5f5;
  color: #666;
}
</style>
