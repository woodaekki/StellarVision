<script setup>
import { useAccountStore } from '@/stores/account';
import { computed, ref, watch } from 'vue';

  // 바인딩 변수\

  const userId = ref('')
  const userDomain = ref('')
  const domainInput = ref('이메일을 선택해주세요')
  const nickname = ref('')
  const password1 = ref('')
  const password2 = ref('')
  const birthday = ref('')
  const accountStore = useAccountStore()

  //도메인 선택이 바뀌면 자동 반영
  watch(userDomain, newVal =>{
    domainInput.value = newVal
  })

  //비밀번호 일치 여부
  const isPasswordMatched = computed(
    () => password1.value && password1.value === password2.value
  )

  // submit 이벤트 발생 시 바인딩된 회원 정보를 묶어 accountStore에 전달, 회원가입 로직 실행
  const onSignUp = function(){
    const userInfo = {
      userEmail : `${userId.value}@${userDomain.value}`,
      nickname : nickname.value,
      password1 : password1.value,
      password2 : password2.value
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
          <input type="text" id="userid" placeholder="Anonymous@email.com" v-model="userId">
          <input class="box" id="email-text" type="text" v-model="domainInput">
            <select class="box" id="email-box" v-model="userDomain">
              <option value="naver.com">naver.com</option>
              <option value="google.com">google.com</option>
              <option value="hanmail.net">hanmail.net</option>
              <option value="nate.com">nate.com</option>
              <option value="msn.com">msn.com</option>
            </select>
        </div>

        <div class="formGroup">
          <input type="text" id="nickname" placeholder="NickName..." v-model="nickname">
        </div>

        <div class="formGroup">
          <input type="password" id="password" placeholder="Password..." v-model="password1">
        </div>

        <div class="formGroup">
          <input type="password" id="password-confirm" placeholder="Confirm Password..." v-model="password2">
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
          <input type="date" id="birth" placeholder="YYYY.MM.DD" v-model="birthday">
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
  background: #4a90e2;
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
</style>
