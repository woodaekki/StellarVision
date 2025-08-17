<template>
  <div class="landing-wrapper">
    <section>
      <video
        v-show="showIntro"
        ref="introRef"
        class="background-video opacity-100 transition-opacity duration-700"
        src="/videos/test3.mp4" autoplay muted playsinline @ended="onIntroEnded"
      />
      <video
        ref="loopRef"
        class="background-video opacity-0 transition-opacity duration-700"
        :class="{ 'opacity-100': loopVisible }"
        src="/videos/test6.mp4" autoplay muted playsinline loop preload="auto"
      />
      <div class="background-globe opacity-0 transition-opacity duration-700"
          :class="{ 'opacity-100': globeVisible }">
        <LandingGlobe @ready="onGlobeReady" />
      </div>

      <LandingParallax
        class="landing-parallax"
        :steps="steps"
        :visible="contentVisible"
      />
    </section>

    <section class="min-h-screen px-5 pt-12 pb-0 bg-gradient-to-b from-transparent to-black/20">
      <TodaysPhoto />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import LandingParallax from '@/components/landing/LandingParallax.vue'
import LandingGlobe from '@/components/landing/LandingGlobe.vue'
import TodaysPhoto from '@/components/landing/TodaysPhoto.vue'
import DetectionIcon from '@/assets/pictures/landing/detection_icon.png'
import StreamingIcon from '@/assets/pictures/landing/streaming_icon.png'
import UpscalingIcon from '@/assets/pictures/landing/upscaling_icon.png'

const showIntro = ref(true)
const introRef = ref(null)
const loopRef = ref(null)
const introEnded = ref(false)
const loopReady = ref(false)
const globeReady = ref(false)
const loopVisible = ref(false)
const globeVisible = ref(false)

const contentVisible = computed(() =>
  !showIntro.value && loopVisible.value && globeVisible.value
)

const steps = ref([
  {
    type: 'hero',
    title: 'StellarVision',
    subtitle: '시간, 공간, 마음을 이어주는 우주의 창문',
    tagline: 'AI와 함께 하는 별자리 스트리밍 서비스'
  },
  {
    type: 'card',
    title: '별빛을 공유하며 연결되는 우주',
    body: '실시간 스트리밍 서비스를 통해 자신이 나누고 싶은 밤하늘을 지구 반대편 사람과도 공유하세요.\n또한 비, 구름, 공해 등으로 별이 보이지 않을 때,\nStellarVision을 켜 누군가의 멋진 밤하늘을 함께 볼 수 있습니다.',
    image: StreamingIcon
  },
  {
    type: 'card',
    title: '별자리를 탐지하는 AI와 함께',
    body: '지금 바라보는 하늘에 어떤 별자리가 있는지 궁금하신가요?\n우리의 친절한 AI 친구 NOVA가 도와드립니다!\nAI 인식 기능을 통해 스트리밍 중 보이는 별자리가 무엇인지 알려드립니다.',
    image: DetectionIcon
  },
  {
    type: 'card',
    title: '아름다운 하늘을 사진으로',
    body: '스트리밍 중 마음에 드는 밤하늘의 장면을 캡쳐하여 배경화면으로.\n업스케일링으로 아름다운 고화질 이미지를 제공 받을 수 있습니다.',
    image: UpscalingIcon
  },
  {
    type: 'card',
    title: '지금 시작해볼까요?',
    body: '버튼을 누르면 여행이 시작됩니다.',
    cta: { text: '시작하기', href: '/main' },
  }
])

onMounted(() => {
  const loop = loopRef.value
  if (loop) {
    const markReady = () => { loopReady.value = true; cleanup(); maybeShow() }
    const cleanup = () => {
      loop.removeEventListener('canplaythrough', markReady)
      loop.removeEventListener('loadeddata',     markReady)
      loop.removeEventListener('canplay',        markReady)
    }
    loop.addEventListener('canplaythrough', markReady, { once: true })
    loop.addEventListener('loadeddata',     markReady, { once: true })
    loop.addEventListener('canplay',        markReady, { once: true })
    loop.load()
  }
  introRef.value?.play?.().catch(() => {})
})

function onGlobeReady() {
  globeReady.value = true
  maybeShow()
}
function onIntroEnded() {
  introEnded.value = true
  maybeShow()
}
function maybeShow() {
  if (!(introEnded.value && loopReady.value && globeReady.value)) return
  loopRef.value?.play?.().catch(() => {})
  loopVisible.value  = true
  globeVisible.value = true
  const el = introRef.value
  if (el) {
    el.classList.add('opacity-0')
    setTimeout(() => { el.pause?.(); showIntro.value = false }, 700)
  }
}

onBeforeUnmount(() => {
  introRef.value?.pause?.()
  loopRef.value?.pause?.()
})
</script>

<style>
html, body {
  margin: 0; padding: 0;
  height: 100%; width: 100%;
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
.background-video,
.background-globe {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  object-fit: cover;
  z-index: -1;
  pointer-events: auto;
}

/* Loop: 페이드인 */
.loopVideo {
  opacity: 0;
  transition: opacity 1.6s ease;
  z-index: -2;
}
.loopVideo.is-visible { opacity: 1; }

/* Intro: 페이드아웃 */
.introVideo {
  opacity: 1;
  transition: opacity 1.6s ease;
  z-index: -1;
}
.introVideo.fade-out { opacity: 0; }

/* Globe도 동일 트랜지션으로 페이드인 (Loop와 타이밍 일치) */
.globe-holder {
  position: relative;
  width: 100%; height: 100%;
  opacity: 0;
  transition: opacity 1.6s ease;
  z-index: 0; /* 비디오 위에 */
}
.globe-holder.is-visible {
  opacity: 1;
}

.todays-photo-container {
  position: relative;
  z-index: 1;
  color: white;
  margin: auto;
}
</style>
