<script setup>
import { useAccountStore } from '@/stores/account';
import { computed, reactive, watch } from 'vue';

  // 바인딩 변수
  // 수정 : 폼 데이터 하나의 reactive로 변경
  const formData = reactive({
  userId : '',
  userDomain : '',
  domainInput : '이메일을 선택해주세요',
  nickname : '',
  password1 : '',
  password2 : '',
  birthday : ''
  })


  const accountStore = useAccountStore()

  //도메인 선택이 바뀌면 자동 반영
  watch(() => formData.userDomain, (newVal) => {
      formData.domainInput = newVal
    }
  )


  //비밀번호 일치 여부
  const isPasswordMatched = computed(
    () => formData.password1 && formData.password1 === formData.password2
  )

  // submit 이벤트 발생 시 바인딩된 회원 정보를 묶어 accountStore에 전달, 회원가입 로직 실행
  const onSignUp = function(){
    const userInfo = {
      userEmail : `${formData.userId}@${formData.userDomain}`,
      nickname :formData. nickname,
      password : formData.password1,
      birthday : formData.birthday
    }
    accountStore.signUp(userInfo)
  }
</script>

<style scoped lang="scss"></style>

<template>
    <div class="signupContainer">
      <h2>회원가입</h2>
      <form class="signupForm" @submit.prevent="onSignUp">
        <div class="formGroup email-group">
          <!-- email 입력폼 -->
          <input
          type="text"
          id="userid"
          placeholder="Anonymous@email.com"
          v-model="formData.userId">

          <input
          class="box"
          id="email-text"
          type="text"
          v-model="formData.domainInput">


            <select class="box" id="email-box" v-model="formData.userDomain">
              <option disabled value="">이메일을 선택해주세요</option>
              <option value="naver.com">naver.com</option>
              <option value="google.com">google.com</option>
              <option value="hanmail.net">hanmail.net</option>
              <option value="nate.com">nate.com</option>
              <option value="msn.com">msn.com</option>
            </select>
        </div>
        <!-- 닉네임 입력 폼 -->
        <div class="formGroup">
          <input
          type="text"
          id="nickname"
          placeholder="NickName..."
          v-model="formData.nickname">
        </div>
        <!-- 비밀번호 -->
        <div class="formGroup">
          <input
          type="password"
          id="password"
          placeholder="Password..."
          v-model="formData.password1">
        </div>
        <!-- 비밀번호 2차 -->
        <div class="formGroup">
          <input
          type="password"
          id="password-confirm"
          placeholder="Confirm Password..."
          v-model="formData.password2">

          <p
          v-if="password2 && !isPasswordMatched"
          class="error"
          >
            비밀번호가 일치하지 않습니다.
          </p>

          <p
            v-else-if="isPasswordMatched"
            class="success"
          >
            비밀번호가 일치합니다.
          </p>
        </div>

        <div class="formGroup">
          <input
          type="date"
          id="birth" p
          laceholder="YYYY.MM.DD"
          v-model="formData.birthday">
        </div>
        <button type="submit">가입하기</button>

      </form>
    </div>
</template>

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
button {
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
