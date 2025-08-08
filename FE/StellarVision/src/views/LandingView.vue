<template>
  <div class="landing-wrapper">
    <section class="video-container">
      <video
        v-show="showIntro"
        ref="introRef"
        class="background-video introVideo"
        src="/videos/test3.mp4"
        autoplay
        muted
        playsinline
        @ended="onIntroEnded"
      ></video>

      <video
        ref="loopRef"
        class="background-video loopVideo"
        src="/videos/test6.mp4"
        autoplay
        muted
        playsinline
        loop
        preload="auto"
        :class="{ 'is-visible': loopVisible }"
      ></video>

      <LandingGlobe />
    </section>

    <section class="todays-photo-container">
      <TodaysPhoto />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import LandingGlobe from '@/components/landing/LandingGlobe.vue'
import TodaysPhoto from '@/components/landing/TodaysPhoto.vue'

const showIntro = ref(true)
const loopVisible = ref(false)
const introRef = ref(null)
const loopRef = ref(null)

onMounted(() => {
  loopRef.value?.load?.()
  introRef.value?.play?.().catch(() => {})
})

const onIntroEnded = () => {
  loopRef.value?.play?.().catch(() => {})
  loopVisible.value = true

  introRef.value?.classList.add('fade-out')

  setTimeout(() => {
    introRef.value?.pause?.()
    showIntro.value = false
  }, 900)
}

onBeforeUnmount(() => {
  introRef.value?.pause?.()
  loopRef.value?.pause?.()
})
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
  top: 0; left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -1;
}

.loopVideo {
  opacity: 0;
  transition: opacity 1.6s ease;
  z-index: -2;
}
.loopVideo.is-visible {
  opacity: 1;
}

.introVideo {
  opacity: 1;
  transition: opacity 1.6s ease;
  z-index: -1;
}
.introVideo.fade-out {
  opacity: 0;
}

.todays-photo-container {
  position: relative;
  z-index: 1;
  color: white;
}
</style>

