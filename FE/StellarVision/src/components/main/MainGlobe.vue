<!-- MainGlobe.vue -->
<template>
  <div class="globe-wrapper">
    <div class="globe-container">
      <canvas ref="canvas"></canvas>

      <div class="tooltip" :style="tooltip.style" v-if="tooltip.title || tooltip.owner">
        <div class="speech-bubble">
          <p class="room-title">{{ tooltip.title }}</p>
          <p>{{ tooltip.owner }}</p>
          <button class="go-button" @click="handleWatchClick">참여하기</button>
        </div>
      </div>

    </div>
  </div>

  <div class="toast-wrap">
    <transition-group name="toast">
      <div v-for="t in toasts" :key="t.key" class="toast ship">
        <div class="ship-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="22" height="22">
            <path d="M12 2c3.5 0 6 2.5 6 6 0 3-1.9 6.5-4.6 9.2l-.9.9-1.9-1.9-1.9 1.9-.9-.9C7.9 14.5 6 11 6 8c0-3.5 2.5-6 6-6z" fill="url(#g)"/>
            <path d="M9.5 20.5c-.4.4-1.6.9-2.7 1 0-1.1.6-2.3 1-2.7l1.7-1.7 1.7 1.7-1.7 1.7z" fill="#ffb74d"/>
            <defs>
              <linearGradient id="g" x1="0" x2="1" y1="0" y2="1">
                <stop offset="0%" stop-color="#8ea2ff"/>
                <stop offset="100%" stop-color="#b388ff"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="ship-body">
          <div class="ship-sub">새로운 스트리밍방이 열렸습니다</div>
          <div class="ship-title">{{ t.title }}</div>
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onBeforeUnmount, computed, inject } from 'vue';
import { renderPins, setupPinClickHandler } from '../../services/globeMarker.js';
import createGlobeScene from '../../services/createMainScene.js';
import { showInfoOnClick } from '../../services/showInfoOnClick.js';
import { Engine } from '@babylonjs/core';

const props = defineProps({
  liveStreams: {
    type: Array,
    default: () => []
  }
});

const canvas = ref(null);
const engine = ref(null);
const scene = ref(null);
const camera = ref(null);
const globeCenter = ref(null);
const globeRadius = ref(null);
const isHoveringGlobe = ref(false);
const isSceneReady = computed(() => !!scene.value && !!globeCenter.value && !!globeRadius.value);

//사이드바 상태 감지
const sidebarState = inject('sidebarState', null)

// Tooltip 로직 (말풍선)
const {
  tooltip,
  handlePinInteraction,
  handleWatchClick,
  updateTooltipPosition,
  hideTooltip
} = showInfoOnClick({ engineRef: engine, cameraRef: camera, sceneRef: scene });

// 캔버스 사이즈 맞춤
function setCanvasSize() {
  if (!canvas.value) return;
  const dpr = window.devicePixelRatio || 1;
  const width = canvas.value.clientWidth * dpr;
  const height = canvas.value.clientHeight * dpr;
  if (canvas.value.width !== width || canvas.value.height !== height) {
    canvas.value.width = width;
    canvas.value.height = height;
  }
}

// 캔버스 크기를 재설정
const onResize = () => {
  setCanvasSize();
  engine.value?.resize();
};

// 마운트 후 초기화
onMounted(async () => {
  engine.value = new Engine(canvas.value, true);
  setCanvasSize();
  engine.value.resize();

  scene.value = await createGlobeScene({
    canvas: canvas.value,
    engine: engine.value,
    cameraRef: camera,
    globeCenterRef: globeCenter,
    globeRadiusRef: globeRadius,
    isHoveringRef: isHoveringGlobe,
  });

  setupPinClickHandler(scene.value, handlePinInteraction, hideTooltip);

  engine.value.runRenderLoop(() => {
    if (camera.value && !isHoveringGlobe.value && !scene.value?.metadata?.isFlying) {
      camera.value.alpha += 0.001;
    }
    scene.value.render();
    updateTooltipPosition();
  });

  window.addEventListener('resize', onResize);

  if (isSceneReady.value && (props.liveStreams?.length ?? 0) > 0) {
    await nextTick();
    await renderPins({
      scene: scene.value,
      globeCenter: globeCenter.value,
      globeRadius: globeRadius.value,
      pins: props.liveStreams
    });
  }
});

// 신규 스트리밍 안내 및 최신 id 추적
const lastMaxId = ref(null);
const toasts = ref([]);

function pushToast(title, ttl = 3000) {
  const key = Date.now() + Math.random();
  toasts.value.push({ key, title });
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.key !== key);
  }, ttl);
}

// 실시간 스트리밍 정보 가져옴 + 카메라 이동/토스트
watch(
  [isSceneReady, () => props.liveStreams],
  async ([ready, list]) => {
    if (!ready) return;
    const streams = Array.isArray(list) ? list : [];
    if (streams.length === 0) return;

    // 핀 렌더링(먼저 호출해서 월드 좌표 맵이 채워지도록)
    await nextTick();
    await renderPins({
      scene: scene.value,
      globeCenter: globeCenter.value,
      globeRadius: globeRadius.value,
      pins: streams
    });

    const newest = streams.reduce((a, b) => (a.id > b.id ? a : b));
    const prev = lastMaxId.value;
    lastMaxId.value = newest.id;

    // 카메라 이동 (가능하면 월드좌표 기반, 아니면 위경도 기반)
    const m = scene.value?.metadata || {};
    const pos = m._pinWorldPosById?.get(newest.id);

    const flyInitial = () => {
      if (pos && m.flyToWorld) m.flyToWorld(pos, { duration: 1200 });
      else if (m.flyToLatLon) m.flyToLatLon(newest.latitude, newest.longitude, { duration: 1200 });
    };
    const flyUpdate = () => {
      if (pos && m.flyToWorld) m.flyToWorld(pos, { duration: 1000 });
      else if (m.flyToLatLon) m.flyToLatLon(newest.latitude, newest.longitude, { duration: 1000 });
    };

    if (prev == null) {
      // 최초 진입
      flyInitial();
    } else if (newest.id > prev) {
      // 신규 방 등장
      flyUpdate();
      pushToast(newest.title);;
    }
  },
  { immediate: true, deep: false, flush: 'post' }
);

// 언마운트 시 정리
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize);
  scene.value?.onPointerObservable.clear();
  engine.value?.stopRenderLoop();
  scene.value?.dispose();
  engine.value?.dispose();
});
</script>

<style scoped>
.globe-wrapper {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  z-index: 1;
}

.globe-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

canvas {
  width: 100%;
  height: 100%;
  display: block;
  pointer-events: auto;
  outline: none;
  border: none;
}
canvas:focus {
  outline: none;
}

.tooltip {
  pointer-events: none;
  position: absolute;
  width: 580px;
  max-width: 15vw;
  z-index: 20;
  transform: translate(-50%, -100%);
  transform-origin: top center;
}

.speech-bubble {
  pointer-events: auto;
  background: rgba(18, 20, 24, 0.78);   /* 살짝 투명한 다크 */
  backdrop-filter: blur(6px);           /* 글라스 효과 */
  -webkit-backdrop-filter: blur(6px);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  padding: 10px 14px;
  box-shadow:
    0 6px 24px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  line-height: 1.35;
  max-width: clamp(220px, 28vw, 320px);
  user-select: none;
  animation: tt-pop 140ms cubic-bezier(.2,.9,.2,1) both;
}

/* 꼬리(아래 삼각형) — 중앙 정렬 */
.speech-bubble::after {
  content: "";
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 0; height: 0;
  border-width: 10px 10px 0 10px;
  border-style: solid;
  border-color: rgba(18, 20, 24, 0.78) transparent transparent transparent;
  filter: drop-shadow(0 2px 2px rgba(0,0,0,0.25));
}

.room-title {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: .2px;
  margin: 0 0 2px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.speech-bubble p:not(.room-title) {
  margin: 0;
  opacity: .9;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.go-button {
  align-self: flex-end;
  margin-top: 6px;
  padding: 6px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,.16);
  background: linear-gradient(to bottom, #ffffff, #e9eef9);
  color: #0b1020;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: transform .08s ease, box-shadow .12s ease, background .12s ease;
}

.go-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0,0,0,.25);
}

.go-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0,0,0,.2);
}

.go-button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(77, 148, 255, .55);
}

@keyframes tt-pop {
  from { transform: scale(.96); opacity: 0; }
  to   { transform: scale(1);   opacity: 1; }
}

/* 여기서부터 알림창 스타일링 */
.toast-wrap {
  position: fixed;
  top: 18px;
  right: 18px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateY(-8px) scale(.98); }
.toast-enter-active, .toast-leave-active { transition: all .18s ease; }

.toast.ship {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 260px;
  max-width: min(46vw, 380px);
  padding: 12px 14px;
  border-radius: 12px;

  /* 글라스+우주 느낌 배경 */
  background:
    radial-gradient(120% 180% at 120% -10%, rgba(142,162,255,.25), rgba(179,136,255,.1) 60%, transparent 70%),
    rgba(16, 18, 28, 0.85);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);

  border: 1px solid rgba(255,255,255,0.12);
  box-shadow:
    0 8px 28px rgba(0,0,0,.35),
    inset 0 1px 0 rgba(255,255,255,.06),
    0 0 0 1px rgba(142,162,255,.08);

  position: relative;
  overflow: hidden;
}

.toast.ship::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    radial-gradient(1px 1px at 20% 30%, rgba(255,255,255,.45), transparent 50%),
    radial-gradient(1px 1px at 70% 60%, rgba(255,255,255,.35), transparent 50%),
    radial-gradient(1px 1px at 40% 80%, rgba(255,255,255,.25), transparent 50%);
  opacity: .35;
  pointer-events: none;
}

.ship-icon {
  width: 28px; height: 28px;
  display: grid; place-items: center;
  filter: drop-shadow(0 0 6px rgba(153, 171, 255, .4));
  position: relative;
}
.ship-icon::after {
  content: "";
  position: absolute;
  bottom: -2px; left: 50%;
  width: 6px; height: 10px;
  transform: translateX(-50%);
  background: radial-gradient(50% 60% at 50% 0%, #ffd180, rgba(255,140,0,.0));
  filter: blur(0.5px);
  animation: thruster .5s ease-in-out infinite alternate;
}
@keyframes thruster {
  from { opacity: .55; transform: translateX(-50%) translateY(0) scaleY(1); }
  to   { opacity: .9;  transform: translateX(-50%) translateY(1px) scaleY(1.15); }
}

.ship-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.ship-sub {
  font-size: 12px;
  color: #cfd2e8;
  letter-spacing: .2px;
  opacity: .9;
}
.ship-title {
  margin-top: 2px;
  font-size: 14px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: .2px;
  text-shadow: 0 0 8px rgba(142,162,255,.2);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 모바일 대응 */
@media (max-width: 640px) {
  .toast-wrap { top: 12px; right: 12px; }
  .toast.ship { min-width: 220px; padding: 10px 12px; }
  .ship-title { font-size: 13px; }
}
</style>
