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
        />
        <select
          class="box"
          id="email-box"
          v-model="formData.userDomain"
        >
          <option disabled value="">이메일을 선택해주세요</option>
          <option value="naver.com">@naver.com</option>
          <option value="google.com">@google.com</option>
          <option value="hanmail.net">@hanmail.net</option>
          <option value="nate.com">@nate.com</option>
          <option value="msn.com">@msn.com</option>
        </select>
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

      <button type="submit" class="btn">가입하기</button>
    </form>
  </div>
</template>

<script setup>
import { useAccountStore } from '@/stores/account';
import { computed, reactive, watch } from 'vue';

const formData = reactive({
  userId: '',
  userDomain: '',
  name: '',
  password1: '',
  password2: '',
  birthday: ''
})

const accountStore = useAccountStore()

// 비밀번호 일치 여부 확인
const isPasswordMatched = computed(() => {
  return formData.password1 && formData.password1 === formData.password2
})

const email = computed(() => {
  return formData.userDomain
    ? `${formData.userId}@${formData.userDomain}`
    : ''
})

// 회원가입 요청 함수
const onSignUp = () => {
  // 이메일 유효성 확인
  if (!formData.userId || !formData.userDomain) {
    alert('이메일 주소를 정확히 입력해주세요.')
    return
  }

  // 비밀번호 확인
  if (!isPasswordMatched.value) {
    alert('비밀번호가 일치하지 않습니다.')
    return
  }

  const userInfo = {
    email: email.value,
    name: formData.name,
    password: formData.password1,
    birth: formData.birthday
  }

  accountStore.signUp(userInfo)
}
</script>

<style>
.signupContainer {
  max-width: 400px;
  margin: 0 auto;
}
.email-group {
  display: flex;
  align-items: center;
  gap: 4px;
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
}
.error {
  color: #e74c3c;
  font-size: 0.9em;
}
.success {
  color: #2ecc71;
  font-size: 0.9em;
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
</style>
