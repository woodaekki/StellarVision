<template>
  <section class="parallax">
    <div class="stage">
      <div class="stage-inner">
        <!-- 상위에서 넘겨줄: 비디오/글로브 등 -->
        <slot />

        <!-- 단계별 컨텐츠(히어로/카드) : 인트로 중 숨김 -->
        <div class="steps" v-show="visible">
          <div
            v-for="(s, i) in steps"
            :key="i"
            class="step"
            :class="[
              i === activeIndex ? 'is-active' : 'is-inactive',
              s.type !== 'hero' ? 'step--center' : ''
            ]"
          >
            <!-- HERO -->
            <div v-if="s.type==='hero'" class="hero">
              <div class="hero__top">
                <h1 class="hero__title">{{ s.title }}</h1>
                <p class="hero__subtitle">{{ s.subtitle }}</p>
              </div>
              <div class="hero__bottom">
                <p class="hero__tagline">{{ s.tagline }}</p>
              </div>
            </div>

            <!-- CARD -->
            <div v-else-if="s.type==='card'" class="card-holder">
              <div class="card-wrap">
                <article class="card">
                  <div class="card__glass" aria-hidden="true"></div>

                  <div class="card__grid">
                    <div v-if="s.image" class="card__image">
                      <div class="card__imageRatio">
                        <img :src="s.image" :alt="s.imageAlt || s.title" class="card__imageEl" />
                      </div>
                    </div>

                    <header class="card__title">
                      <h3 class="card__titleText">{{ s.title }}</h3>
                    </header>

                    <div class="card__body">
                      <p class="card__bodyText">{{ s.body }}</p>

                      <div v-if="s.cta || s.secondary" class="card__actions">
                        <a v-if="s.cta" :href="s.cta.href" class="btn btn--primary">
                          {{ s.cta.text }}
                        </a>
                        <a v-if="s.secondary" :href="s.secondary.href" class="btn btn--ghost">
                          {{ s.secondary.text }}
                        </a>
                      </div>
                    </div>
                  </div>
                </article>
              </div>
            </div>
          </div> <!-- /step -->
        </div> <!-- /steps -->
      </div>
    </div>

    <!-- 마커: flow에 존재 -->
    <div>
      <div
        v-for="(_, i) in steps"
        :key="'marker-'+i"
        :ref="el => markerEls[i] = el"
        class="marker"
      />
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: true },  // 인트로 중 숨김 제어
  steps: { type: Array, required: true }      // [{type:'hero'|'card', ...}]
})

const activeIndex = ref(0)
const markerEls = []
let io = null

onMounted(async () => {
  await nextTick()

  const chooseActive = () => {
    const centerY = window.innerHeight / 2
    let best = 0, bestDist = Infinity
    markerEls.forEach((el, idx) => {
      if (!el) return
      const r = el.getBoundingClientRect()
      const mid = r.top + r.height / 2
      const d = Math.abs(mid - centerY)
      if (d < bestDist) { bestDist = d; best = idx }
    })
    activeIndex.value = best
  }

  io = new IntersectionObserver(() => { chooseActive() }, {
    root: null,
    threshold: [0, 0.01, 0.5, 0.99, 1],
    rootMargin: '-40% 0px -40% 0px'
  })
  markerEls.forEach(el => el && io.observe(el))
  chooseActive()
})

onBeforeUnmount(() => {
  if (io) { io.disconnect(); io = null }
})
</script>

<style scoped>
/* ===== 레이아웃 공통 ===== */
.parallax { position: relative; }
.stage { position: sticky; top: 0; height: 100vh; overflow: hidden; }
.stage-inner { position: relative; width: 100%; height: 100%; }

/* ===== 스텝(히어로/카드) =====
   blur가 깨지지 않도록 transform은 자식에게만 적용 */
.step { position: absolute; inset: 0; }
.step > * {
  transition: opacity 300ms ease, transform 300ms ease;
  will-change: opacity, transform;
}
.step.is-active > * { opacity: 1; transform: none; } /* 활성: transform 없음 → backdrop-filter 정상 */
.step.is-inactive > * { opacity: 0; transform: translate3d(0, 1rem, 0) scale(0.95); }
.step--center { display: grid; place-items: center; }

/* ===== HERO ===== */
.hero { position: relative; width: 100%; height: 100%; pointer-events: none; user-select: none; }
.hero__top {
  position: absolute; left: 50%; transform: translateX(-50%);
  top: 10vh; text-align: center; padding-left: 1.5rem; padding-right: 1.5rem;
}
.hero__bottom {
  position: absolute; left: 0; right: 0; bottom: 10vh;
  text-align: center; padding-left: 1.5rem; padding-right: 1.5rem;
}
.hero__title {
  font-weight: 800; letter-spacing: -0.02em; line-height: 0.95; color: #fff;
  text-shadow: 0 1px 1px rgba(0,0,0,0.5);
  font-size: clamp(40px, 8vw, 120px);
}
.hero__subtitle, .hero__tagline { color: rgba(255,255,255,0.85); }
.hero__subtitle { margin-top: .75rem; font-size: clamp(16px, 2.4vw, 28px); }
.hero__tagline  { font-size: clamp(14px, 2vw, 22px); }

/* ===== CARD ===== */
.card-holder { pointer-events: none; }
.card-wrap   {
  width: 100%;
  max-width: 56rem;                 /* 가로만 확장 */
  padding-left: 2rem; padding-right: 2rem;
  pointer-events: auto;
}
@media (min-width: 1024px) { .card-wrap { max-width: 64rem; } }  /* lg */
@media (min-width: 1280px) { .card-wrap { max-width: 72rem; } }  /* xl */
@media (min-width: 768px)  { .card-wrap { padding-left: 3rem; padding-right: 3rem; } } /* md */

/* 강한 스머지(blur↑, 투과↓, 밝기/대비/채도↓) */
.card {
  position: relative; overflow: hidden; border-radius: 1.5rem; color: #fff;
  background: rgba(20, 24, 28, 0.32);
  backdrop-filter: blur(90px) brightness(0.65) contrast(0.68) saturate(0.80);
  -webkit-backdrop-filter: blur(90px) brightness(0.65) contrast(0.68) saturate(0.80);
  border: 1px solid rgba(255,255,255,0.16);
  box-shadow: 0 24px 48px rgba(0,0,0,0.30);
  padding: 2rem;            /* 세로 높이는 기존 내용 기준 유지 */
}
@supports not ((backdrop-filter: blur(1px)) or (-webkit-backdrop-filter: blur(1px))) {
  .card { background: rgba(20,24,28,0.55); } /* 미지원 환경 fallback */
}
@media (min-width: 768px) {
  .card {
    background: rgba(20, 24, 28, 0.36);
    backdrop-filter: blur(120px) brightness(0.60) contrast(0.66) saturate(0.78);
    -webkit-backdrop-filter: blur(120px) brightness(0.60) contrast(0.66) saturate(0.78);
    padding: 2.5rem;
  }
}

/* 유리 오버레이: 하이라이트 + 비네팅 */
.card__glass {
  position: absolute; inset: 0; z-index: 0; pointer-events: none;
  background:
    radial-gradient(120% 100% at 50% -10%, rgba(255,255,255,0.16), rgba(255,255,255,0) 60%),
    linear-gradient(to bottom, rgba(255,255,255,0.14), rgba(255,255,255,0.10), rgba(255,255,255,0.16));
}
.card__glass::after {
  content: ""; position: absolute; inset: -1px;
  background: radial-gradient(60% 80% at 50% 50%, rgba(0,0,0,0.18), transparent 70%);
  mix-blend-mode: multiply; pointer-events: none;
}

/* 카드 내부 레이아웃 */
.card__grid { position: relative; z-index: 10; display: grid; grid-auto-rows: auto; gap: 1.5rem; }
@media (min-width: 768px) { .card__grid { gap: 2rem; grid-template-columns: 1.1fr 1fr; } }

.card__image { border-radius: .75rem; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.35); }
@media (min-width: 768px) { .card__image { grid-row: span 2 / span 2; align-self: stretch; } }
.card__imageRatio { aspect-ratio: 4 / 5; }
@media (min-width: 768px) { .card__imageRatio { height: 100%; } }
.card__imageEl { width: 100%; height: 100%; object-fit: cover; }

@media (min-width: 768px) { .card__title { grid-column: 2; align-self: end; } }
.card__titleText { font-size: clamp(1.5rem, 2.2vw, 2rem); font-weight: 700; letter-spacing: -0.01em; }

@media (min-width: 768px) { .card__body { grid-column: 2; align-self: start; } }
.card__bodyText {
  color: rgba(255,255,255,0.8);
  font-size: clamp(.95rem, 1.6vw, 1rem);
  line-height: 1.7;
  white-space: pre-line;
}

.card__actions { margin-top: 1.25rem; display: flex; flex-wrap: wrap; gap: .75rem; }

/* 버튼 */
.btn {
  display: inline-flex; align-items: center;
  border-radius: .75rem; font-weight: 600; font-size: .875rem;
  padding: .5rem 1rem; transition: background-color .2s ease, border-color .2s ease, color .2s ease;
}
.btn--primary { background: #fff; color: #000; }
.btn--primary:hover { background: rgba(255,255,255,0.9); }
.btn--ghost { border: 1px solid rgba(255,255,255,0.3); color: #fff; }
.btn--ghost:hover { background: rgba(255,255,255,0.10); }

/* ===== 마커 ===== */
.marker { height: 100vh; }
</style>
