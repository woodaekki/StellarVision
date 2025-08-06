<template>

  <div class="bg-blue-600 text-white p-6 rounded-lg shadow-xl text-2xl">
    Tailwind 적용 테스트!
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, onMounted } from 'vue';

const _this = getCurrentInstance().proxy;

const props = defineProps({});

const emits = defineEmits();
</script>

<style scoped lang="scss">
  .card {
    background-color: bg-zinc-800;
    border-radius: round-lg;

  }
</style>
