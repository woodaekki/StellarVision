<template>
    <div class="login-page">
      <LoginForm></LoginForm>
    </div>
</template>

<script setup>
import LoginForm from '@/components/auth/LoginForm.vue';
</script>

<style scoped>
.login-page {
  background-image: #f2f2f2;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
</style>
