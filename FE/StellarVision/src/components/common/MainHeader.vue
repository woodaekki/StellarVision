<template>
  <header class="header-wrap">
    <div class="header">
      <div class="header-left">
        <img :src="logo" alt="로고" width="30px" height="42px" />
        <RouterLink to="/">StellaVision</RouterLink>
        <RouterLink to="/pre">시작</RouterLink>
      </div>
      <nav class="header-right">
        <RouterLink to="/signup">회원가입</RouterLink>
        <RouterLink to="/login">로그인</RouterLink>
      </nav>
    </div>
  </header>
</template>


<script setup>
import { RouterLink } from 'vue-router'
import logo from "@/assets/pictures/stellabot/logo.png"

</script>

<style lang="scss">
</style>
