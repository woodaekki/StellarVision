<template>
  <div class="landing-wrapper">
    <section class="video-container" ref="videoSection">
      <video autoplay muted loop playsinline class="background-video" src="/videos/test3.mp4"></video>
      <LandingGlobe />
    </section>

    <section class="todays-photo-container" ref="todaysPhotoSection">
      <!-- <TodaysPhoto /> -->
      <RouterView />
    </section>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue';
import LandingGlobe from '@/components/landing/LandingGlobe.vue';

const videoSection = ref(null);
const todaysPhotoSection = ref(null);
let isScrolling = false;
let scrollTimeout = null;

onMounted(() => {
  const handleWheel = (e) => {
    // 현재 스크롤 위치가 첫 번째 섹션에 있는지 확인
    const currentScrollY = window.scrollY;
    const viewportHeight = window.innerHeight;

    // 첫 번째 섹션에서 아래로 스크롤할 때
    if (currentScrollY < viewportHeight / 2 && e.deltaY > 0 && !isScrolling) {
      e.preventDefault();
      isScrolling = true;

      // 두 번째 섹션으로 스크롤
      todaysPhotoSection.value?.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });

      // 스크롤 완료 후 플래그 리셋
      scrollTimeout = setTimeout(() => {
        isScrolling = false;
      }, 1000);
    }
    // 두 번째 섹션에서 위로 스크롤할 때
    else if (currentScrollY >= viewportHeight / 2 && e.deltaY < 0 && !isScrolling) {
      e.preventDefault();
      isScrolling = true;

      // 첫 번째 섹션으로 스크롤
      videoSection.value?.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });

      scrollTimeout = setTimeout(() => {
        isScrolling = false;
      }, 1000);
    }
  };

  // 터치 이벤트도 지원
  let touchStartY = 0;
  let touchEndY = 0;

  const handleTouchStart = (e) => {
    touchStartY = e.touches[0].clientY;
  };

  const handleTouchEnd = (e) => {
  touchEndY = e.changedTouches[0].clientY;
  const touchDiff = touchStartY - touchEndY;

  if (Math.abs(touchDiff) > 50 && !isScrolling) {
    const videoSectionTop = videoSection.value.getBoundingClientRect().top;

    if (videoSectionTop >= -100 && touchDiff > 0) {
      // 아래로 스와이프
      isScrolling = true;
      todaysPhotoSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } else if (videoSectionTop <= -100 && touchDiff < 0) {
      // 위로 스와이프
      isScrolling = true;
      videoSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    setTimeout(() => { isScrolling = false; }, 1000);
  }
};


  window.addEventListener('wheel', handleWheel, { passive: false });
  window.addEventListener('touchstart', handleTouchStart, { passive: true });
  window.addEventListener('touchend', handleTouchEnd, { passive: true });

  onBeforeUnmount(() => {
    window.removeEventListener('wheel', handleWheel);
    window.removeEventListener('touchstart', handleTouchStart);
    window.removeEventListener('touchend', handleTouchEnd);
    if (scrollTimeout) {
      clearTimeout(scrollTimeout);
    }
  });
});
</script>

<style>
html {
  scroll-behavior: smooth;
}

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
  min-height: 100vh;
}
</style>
