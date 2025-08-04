<script setup>
import router from '@/router';
import { useAccountStore } from '@/stores/account';
import { ref } from 'vue';

  const userEmail = ref('')
  const password = ref('')
  const errorMsg = ref('')
  const isLoading = ref(false)

  const accountStore = useAccountStore()


  // submit 이벤트 발생 시 바인딩된 회원 정보를 묶어 accountStore에 전달, 회원가입 로직 실행
  const onLogin = async() => {
    errorMsg.value = ''
    if (!userEmail.value || !password.value) {         //이메일 혹은 패스워드 중 하나가 비었을 경우
      errorMsg.value = "이메일과 비밀번호를 모두 입력하세요."
      return
    }
    isLoading.value = true
    try {
      await accountStore.logIn(
        {
          email: userEmail.value,
          password : password.value
        })
        
        router.push({name:'LandingView'})
      }
      catch (err){
      errorMsg.value = err.response?.data.message || '로그인에 실패했습니다.'
      console.log(err)
    }
      finally {
      isLoading.value = false
      }
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


      <button type="submit" :disabled="isLoading">
        {{ isLoading ? '로딩중...' : '로그인' }}
      </button>
      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

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
