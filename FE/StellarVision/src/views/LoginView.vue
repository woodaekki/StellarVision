<template>
    <div class="login-page">로그인 페이지입니다.</div>
    <LoginForm></LoginForm>

</template>

<script setup>
import LoginForm from '@/components/auth/LoginForm.vue';
</script>

<style scoped>
.login-page {
    background-image: url(@/assets//pictures/wallpaper/space.jpeg); 
    padding-top: 58px; 
}
</style>
