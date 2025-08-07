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
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { renderPins, setupPinClickHandler } from './globeMarker.js';
import createGlobeScene from './createMainScene.js';
import { showInfoOnClick } from './showInfoOnClick.js';
import { Engine } from '@babylonjs/core';

// Props
const props = defineProps({
  liveStreams: {
    type: Array,
    default: () => []
  }
});

// Babylon 관련 ref들
const canvas = ref(null);
const engine = ref(null);
const scene = ref(null);
const camera = ref(null);
const globeCenter = ref(null);
const globeRadius = ref(null);
const isHoveringGlobe = ref(false);

// Tooltip(지구본의 각 아이콘을 클릭할 때 나타나는 말풍선) 로직
const {
  tooltip,
  handlePinInteraction,
  handleWatchClick,
  updateTooltipPosition
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

  setupPinClickHandler(scene.value, handlePinInteraction);

  engine.value.runRenderLoop(() => {
    if (camera.value && !isHoveringGlobe.value) {
      camera.value.alpha += 0.001;
    }
    scene.value.render();
    updateTooltipPosition();
  });

  window.addEventListener('resize', () => {
    setCanvasSize();
    engine.value.resize();
  });
});

// 실시간으로 스트리밍 정보를 가져오는 watch
// 지구본 핀 랜더링 watch와 꼭 분리되어 있어야 합니다, 통합시킬 경우 무한 api 요청을 보내 페이지가 먹통됩니다
watch(
  () => props.liveStreams,
  () => {
    if (!scene.value || !globeCenter.value || !globeRadius.value) return;
    renderPins({
      scene: scene.value,
      globeCenter: globeCenter.value,
      globeRadius: globeRadius.value,
      pins: props.liveStreams
    });
  },
  {
    immediate: true,
    deep: true
  }
);

// 지구본에 아이콘을 추가 생성하기 위한 watch
watch(
  [scene, globeCenter, globeRadius],
  () => {
    if (!scene.value || !globeCenter.value || !globeRadius.value) return;
    renderPins({
      scene: scene.value,
      globeCenter: globeCenter.value,
      globeRadius: globeRadius.value,
      pins: props.liveStreams
    });
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => {
    setCanvasSize();
    engine.value.resize();
  });
  if (engine.value) engine.value.dispose();
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
  position: absolute;
  z-index: 10;
  pointer-events: auto;
  transform: translate(-50%, -100%) !important;
  transform-origin: top left !important;
}

.speech-bubble {
  position: relative;        /* ::after 위치 기준 */
  background: black;
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  line-height: 1.4;
}

.speech-bubble::after {
  content: "";
  position: absolute;
  bottom: -8px;
  left: 20px;        /* 꼬리 좌우 위치 조정, 이걸 추후 고쳐야 함 */
  width: 0;
  height: 0;
  border-width: 8px 8px 0 8px;
  border-style: solid;
  border-color: black transparent transparent transparent;
}

.room-title {
  font-size: large;
  margin-bottom: 4px;
}

.go-button {
  border-radius: 8px;
  background-color: white;
  color: black;
  margin-top: 6px;
}
</style>
