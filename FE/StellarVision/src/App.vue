<script setup>
import { RouterLink, RouterView } from 'vue-router';
</script>

<template>

  <header>
    <div>
      <nav>
        <RouterLink to="/signup">회원가입</RouterLink>
        <RouterView/>
      </nav>
    </div>
  </header>


</template>

<style scoped>

</style>
