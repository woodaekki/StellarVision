<template>
  <button class="toggle-btn" @click="isOpen = !isOpen">
    {{ isOpen ? '×' : '☰' }}
  </button>

  <nav class="sidebar" :class="{ open: isOpen }">
    <RouterLink to="/replay">스트리밍 목록</RouterLink>
    <RouterLink to="/badge">도감</RouterLink>
    <RouterLink to="/calendar">캘린더</RouterLink>
  </nav>
</template>

<script setup>
import { ref } from 'vue';

const isOpen = ref(false);
defineExpose({ isOpen });
</script>

<style scoped lang="scss">


</style>
