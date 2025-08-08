<template>
  <div class="form-container">
    <h2 class="form-title">로그인</h2>
    <form class="form-body" @submit.prevent="onLogin">
      <div class="input-group">
        <input
          type="email"
          id="userEmail"
          placeholder="이메일 입력"
          v-model="userEmail"
          class="input-field"
        >
      </div>

      <div class="input-group">
        <input
          type="password"
          id="password"
          placeholder="비밀번호 입력"
          v-model="password"
          class="input-field">
      </div>

      <button
        type="submit"
        :disabled="isLoading"
        class="black-button large-button"
        :class="{ 'button-disabled': isLoading }"
      >
        {{ isLoading ? '로딩중...' : '로그인' }}
      </button>

      <p v-if="errorMsg" class="error-message">{{ errorMsg }}</p>

    </form>
  </div>
</template>

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
  if (!userEmail.value || !password.value) {
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

<style scoped>
.form-container {
  width: 100%;
  max-width: 550px;
  margin: 0 auto;
  padding: 80px 40px;
}

.form-title {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
  margin-top: 0;
  margin-bottom: 40px;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
}

.input-field {
  width: 100%;
  padding: 16px 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 18px;
}

.black-button {
  background-color: black;
  color: white;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: background-color 0.3s;
}

.black-button:hover {
  background-color: #333;
}

.large-button {
  width: 100%;
  padding: 18px;
  margin-top: 15px;
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: bold;
}

.button-disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
  color: #6b7280;
}

.error-message {
  color: #ef4444;
  font-size: 16px;
  margin-top: 15px;
  text-align: center;
}

/* 모바일 화면 (767px 이하) */
@media (max-width: 767px) {
  .form-container {
    max-width: 380px;
    padding: 60px 20px;
  }

  .form-title {
    font-size: 24px;
    margin-bottom: 30px;
  }

  .form-body {
    gap: 15px;
  }

  .input-field {
    padding: 12px 16px;
    border-radius: 6px;
    font-size: 14px;
  }

  .black-button {
    border-radius: 6px;
  }

  .large-button {
    padding: 15px;
    margin-top: 15px;
    font-size: 16px;
  }

  .error-message {
    font-size: 12px;
    margin-top: 10px;
  }
}
</style>
