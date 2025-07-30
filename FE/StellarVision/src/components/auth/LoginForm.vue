<script setup>
import { useAccountStore } from '@/stores/account';
import { ref } from 'vue';

  const userEmail = ref('')
  const password = ref('')
  const accountStore = useAccountStore()


  // submit 이벤트 발생 시 바인딩된 회원 정보를 묶어 accountStore에 전달, 회원가입 로직 실행
  const onLogin = function(){
    const userInfo = {
      email: userEmail.value,
      password : password.value
    }
    accountStore.logIn(userInfo)
  }
</script>


<template>
    <div class="loginContainer">
      <h2>로그인</h2>
      <form class="loginForm" @submit.prevent="onLogin">
        <div class="form-group">
          <!-- email 입력폼 -->
          <input
          type="email"
          id="userEmail"
          placeholder="email 입력"
          v-model="userEmail">
        </div>

        <!-- 비밀번호 -->
        <div class="form-group">
          <input
          type="password"
          id="password"
          placeholder="Password입력"
          v-model="password">
        </div>


        <button type="submit">로그인</button>

      </form>
    </div>
</template>

<style scoped lang="scss">
.loginContainer {
  max-width: 400px;
  margin: 0 auto;
}

.form-group {
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

</style>
