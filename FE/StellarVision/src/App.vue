<template>
  <MainHeader />

  <MainSidebar />

  <main>
    <RouterView />
  </main>

  <!-- <MainFooter /> -->
</template>

<script setup>
import { RouterView } from 'vue-router';
import MainHeader from '@/components/common/MainHeader.vue'
import MainSidebar from '@/components/common/MainSidebar.vue'
// import MainFooter from '@/components/common/MainFooter.vue'

</script>

<style lang="scss">
@use "@/assets/components.scss";

</style>
