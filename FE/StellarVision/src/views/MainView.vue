<template>
  <div class="main-wrapper">
    <section class="video-container">
      <video autoplay muted loop playsinline class="background-video" src="/videos/test1.mp4"></video>
      <MainGlobe />
    </section>
  </div>
</template>

<script setup>
import MainGlobe from '@/components/main/MainGlobe.vue';
</script>
