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

        <div class="parallax-nav" aria-label="Step navigation">
          <button
            class="nav-btn up"
            @click="go(-1)"
            :disabled="activeIndex === 0"
            aria-label="이전"
            title="이전 (위로)"
          >▲</button>

          <button
            class="nav-btn down"
            @click="go(1)"
            :disabled="activeIndex === steps.length - 1"
            aria-label="다음"
            title="다음 (아래로)"
          >▼</button>
        </div>
      </div>
    </div>

    <!-- 마커: flow에 존재 -->
    <div>
      <div
        v-for="(_, i) in steps"
        :key="'marker-'+i"
        :ref="el => (markerEls[i] = el)"
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
const markerEls = ref([])
let io = null

/* ===== 스크롤 연동(버튼 클릭 시 실제 스크롤 이동) ===== */
let scrollingLock = false
const SCROLL_MS = 600

function easeOutCubic(t) {
  return 1 - Math.pow(1 - t, 3)
}

function smoothScrollTo(targetY, duration = SCROLL_MS) {
  const startY = window.scrollY || window.pageYOffset || 0
  const delta = targetY - startY
  if (Math.abs(delta) < 1) return Promise.resolve()
  return new Promise(resolve => {
    const t0 = performance.now()
    function frame(t) {
      const p = Math.min(1, (t - t0) / duration)
      const y = startY + delta * easeOutCubic(p)
      window.scrollTo(0, y)
      if (p < 1) requestAnimationFrame(frame)
      else resolve()
    }
    requestAnimationFrame(frame)
  })
}

function getTop(el) {
  const r = el.getBoundingClientRect()
  return r.top + window.scrollY
}

// 마커 위치로 부드럽게 스크롤
async function scrollToMarker(i) {
  const el = markerEls.value[i]
  if (!el) return
  try {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    await new Promise(r => setTimeout(r, SCROLL_MS))
  } catch {
    await smoothScrollTo(getTop(el), SCROLL_MS)
  }
}

// 버튼 핸들러: -1(위), +1(아래)
async function go(delta) {
  if (scrollingLock) return
  const next = Math.max(0, Math.min(activeIndex.value + delta, props.steps.length - 1))
  if (next === activeIndex.value) return
  scrollingLock = true
  activeIndex.value = next            // 즉시 UI 반영(비활성 상태 등)
  await scrollToMarker(next)          // 실제 스크롤도 연동
  scrollingLock = false
  // activeIndex는 IntersectionObserver가 스크롤 결과에 따라 다시 보정합니다.
}

/* ===== 활성 인덱스 추적(IO 기반) ===== */
onMounted(async () => {
  await nextTick()

  const chooseActive = () => {
    const centerY = window.innerHeight / 2
    let best = 0, bestDist = Infinity
    markerEls.value.forEach((el, idx) => {
      if (!el) return
      const r = el.getBoundingClientRect()
      const mid = r.top + r.height / 2
      const d = Math.abs(mid - centerY)
      if (d < bestDist) { bestDist = d; best = idx }
    })
    activeIndex.value = best
  }

  io = new IntersectionObserver(() => { if (!scrollingLock) chooseActive() }, {
    root: null,
    threshold: 0.5,
    rootMargin: '0px'
  })
  markerEls.value.forEach(el => el && io.observe(el))
  chooseActive()
})

onBeforeUnmount(() => {
  if (io) { io.disconnect(); io = null }
})
</script>

<style scoped>
/* ===== 레이아웃 공통 ===== */
.parallax { position: relative; }
.stage { position: sticky; top: 0; height: 100dvh; overflow: hidden; }
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
.hero { position: relative; width: 100%; height: 84dvh; pointer-events: none; user-select: none; }
.hero__top {
  position: absolute; left: 50%; transform: translateX(-50%);
  top: 6vh; text-align: center; padding-left: 1.5rem; padding-right: 1.5rem;
}
.hero__bottom {
  position: absolute; left: 0; right: 0; bottom: 6vh;
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
  height: 80%;
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
.marker { height: 100dvh; }
@supports not (height: 100dvh) {
  .stage  { height: 100vh; }
  .marker { height: 100vh; }
}

.parallax-nav {
  position: absolute;
  left: calc(env(safe-area-inset-left, 0px) + clamp(12px, 3.5vw, 40px));
  transform: translateX(-50%);
  top: 50%; transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 10;
  pointer-events: auto;
}

/* 버튼: 원형, 반투명 유리 느낌 */
.nav-btn {
  width: 40px;
  height: 40px;
  border: 1px solid rgba(255,255,255,0.25);
  border-radius: 9999px;
  background: rgba(17,24,39,0.45); /* slate-900 45% */
  color: #fff;
  font-weight: 700;
  line-height: 1;
  display: grid;
  place-items: center;
  cursor: pointer;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease, opacity .15s ease;
}

.nav-btn:hover { transform: translateY(-1px); background: rgba(17,24,39,0.6); border-color: rgba(255,255,255,0.35); }
.nav-btn:active { transform: translateY(0); }

.nav-btn:disabled {
  opacity: .45;
  cursor: default;
  transform: none;
}

/* (선택) 위/아래 버튼 간 미세한 시각 차별 */
.nav-btn.up   { }
.nav-btn.down { }
</style>
