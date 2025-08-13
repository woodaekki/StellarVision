<template>
  <div class="page">
    <div class="stars-background">
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
              class="input-field"
            >
          </div>
          <button
            type="submit"
            :disabled="isLoading"
            class="submit-button"
            :class="{ 'button-disabled': isLoading }"
          >
            {{ isLoading ? '로딩중...' : '로그인' }}
          </button>
          <p v-if="errorMsg" class="error-message">{{ errorMsg }}</p>
        </form>
      </div>
    </div>
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

.input-field {
  width: 100%;
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

.submit-button {
  width: 100%;
  padding: 16px;
  background-color: #007bff;
  color: white;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1.1rem;
  cursor: pointer;
  border: none;
  transition: all 0.3s ease;
  margin-top: 15px;
}

.submit-button:hover {
  background-color: #0056b3;
  box-shadow: 0 0 15px rgba(0, 123, 255, 0.5);
  transform: translateY(-2px);
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
  margin-top: 15px;
  text-align: center;
  background-color: rgba(255, 107, 107, 0.1);
  border-radius: 8px;
  padding: 10px;
}
</style>