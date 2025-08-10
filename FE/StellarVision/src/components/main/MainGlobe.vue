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
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
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
    if (camera.value && !isHoveringGlobe.value) {
      camera.value.alpha += 0.001;
    }
    scene.value.render();
    updateTooltipPosition();
  });

  window.addEventListener('resize', onResize);

  if (props.liveStreams?.length) {
    await renderPins({
      scene: scene.value,
      globeCenter: globeCenter.value,
      globeRadius: globeRadius.value,
      pins: props.liveStreams
    });
  }
});

// 실시간 스트리밍 정보 가져옴
watch(
  [() => props.liveStreams, scene, globeCenter, globeRadius],
  async () => {
    if (!scene.value || !globeCenter.value || !globeRadius.value) return;
    await nextTick();
    await renderPins({
      scene: scene.value,
      globeCenter: globeCenter.value,
      globeRadius: globeRadius.value,
      pins: props.liveStreams
    });
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
  top: 55px;
  left: 0;
  width: 100vw;
  height: calc(100vh - 60px);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  z-index: 0;
}

.globe-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  z-index: 1;
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
</style>
