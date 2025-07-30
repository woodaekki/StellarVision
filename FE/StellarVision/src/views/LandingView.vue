<template>
  <div class="landing-wrapper">
    <section class="video-container">
      <video autoplay muted loop playsinline class="background-video" src="/videos/test1.mp4"></video>
      <LandingGlobe />
    </section>

    <section class="todays-photo-container">
      <TodaysPhoto />
    </section>
  </div>
</template>

<script setup>
import LandingGlobe from '@/components/landing/LandingGlobe.vue';
import TodaysPhoto from '@/components/landing/TodaysPhoto.vue';
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow-x: hidden;
}

.landing-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.video-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.background-video {
  position: absolute;
  width: 100%;
  height: 100%;
  object-fit: cover;
  top: 0;
  left: 0;
  z-index: -1;
}

.todays-photo-container {
  position: relative;
  z-index: 1;
  color: white;
  background: rgb(0, 0, 0);
}
</style>
