<template>
  <div class="globe-wrapper">
    <div class="globe-container" style="position: relative;">
      <canvas ref="canvas"></canvas>

      <GlobePins
        v-if="scene && globeCenter && globeRadius"
        :scene="scene"
        :globeCenter="globeCenter"
        :globeRadius="globeRadius"
        :pins="pins"
        @pin-click="handlePinClick"
      />

      <div
        ref="tooltip"
        class="tooltip speech-bubble"
        :style="tooltipStyle"
        v-if="tooltipText"
        @click="() => { tooltipText = ''; activePin = null; }"
      >
        {{ tooltipText }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from "vue";
import "@babylonjs/loaders/glTF";
import {
  Engine,
  Scene,
  ArcRotateCamera,
  Vector3,
  Color3,
  Color4,
  HemisphericLight,
  Tools,
  DefaultRenderingPipeline,
  LoadAssetContainerAsync,
  GlowLayer,
  StandardMaterial,
  MeshBuilder,
  Matrix,
} from "@babylonjs/core";
import GlobePins from "./GlobeMarker.vue";

const canvas = ref(null);
const engine = ref(null);
const scene = ref(null);
const camera = ref(null);

const globeCenter = ref(null);
const globeRadius = ref(null);

const pins = [
  { lat: 37.5665, lon: 126.9780, label: "서울에서 즐기는 은하수" },
  { lat: 51.509865, lon: -0.118092, label: "런던에서 흐르는 밤하늘" },
  { lat: 35.6895, lon: 139.6917, label: "도쿄에서 같이 올려다 보며" },
];

let tooltipText = ref("");
const tooltipStyle = reactive({
  left: "0px",
  top: "0px",
  display: "none",
});
let activePin = ref(null);

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

function updateTooltipPosition() {
  if (!scene.value || !camera.value || !engine.value || !activePin.value) {
    tooltipStyle.display = "none";
    return;
  }

  const pos = Vector3.Project(
    activePin.value.position,
    Matrix.Identity(),
    scene.value.getTransformMatrix(),
    camera.value.viewport.toGlobal(engine.value.getRenderWidth(), engine.value.getRenderHeight())
  );

  // 지구본 뒷편의 말풍선은 가려지게
  if (pos.z < 0 || pos.z > 1) {
    tooltipStyle.display = "none";
    return;
  }

  tooltipStyle.left = `${pos.x + 10}px`;
  tooltipStyle.top = `${pos.y - 40}px`;
  tooltipStyle.display = "block";
}

function handlePinClick(mesh) {
  activePin.value = mesh;
  tooltipText.value = mesh.metadata?.label || "";
  updateTooltipPosition();
}

onMounted(async () => {
  engine.value = new Engine(canvas.value, true);
  setCanvasSize();
  engine.value.resize();

  scene.value = await createScene();

  engine.value.runRenderLoop(() => {
    if (camera.value) {
      camera.value.alpha += 0.001; // 지구본 속도 조정
    }
    scene.value.render();
    updateTooltipPosition();
  });

  window.addEventListener("resize", () => {
    setCanvasSize();
    engine.value.resize();
  });
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", () => {
    setCanvasSize();
    engine.value.resize();
  });
  if (engine.value) engine.value.dispose();
});

async function createScene() {
  const sceneLocal = new Scene(engine.value);
  sceneLocal.clearColor = new Color4(0, 0, 0, 0);

  camera.value = new ArcRotateCamera(
    "camera",
    Tools.ToRadians(45),
    Tools.ToRadians(60),
    100,
    new Vector3(0, 1, 0),
    sceneLocal
  );
  camera.value.attachControl(canvas.value, true);
  camera.value.lowerRadiusLimit = 50;

  new HemisphericLight("light", new Vector3(0, 1, 0), sceneLocal);

  const glow = new GlowLayer("glow", sceneLocal, {
    blurKernelSize: 64,
    intensity: 0.25,
  });

  const fogAura = MeshBuilder.CreateSphere("fogAura", { diameter: 72, segments: 96 }, sceneLocal);
  const fogMaterial = new StandardMaterial("fogMat", sceneLocal);
  fogMaterial.emissiveColor = new Color3(0.2, 0.8, 1.0);
  fogMaterial.alpha = 0.005;
  fogMaterial.backFaceCulling = false;
  fogMaterial.disableLighting = true;
  fogAura.material = fogMaterial;

  glow.addIncludedOnlyMesh(fogAura);
  fogAura.isPickable = false;
  fogAura.visibility = 1;

  const pipeline = new DefaultRenderingPipeline("defaultPipeline", true, sceneLocal, [camera.value]);
  pipeline.bloomEnabled = true;
  pipeline.bloomThreshold = 0.1;
  pipeline.bloomIntensity = 0.1;

  const container = await LoadAssetContainerAsync("/models/globe.glb", undefined, sceneLocal);
  container.addAllToScene();

  const allMeshes = container.meshes.filter((m) => m.name !== "__root__" && m.isVisible);

  if (allMeshes.length > 0) {
    globeCenter.value = allMeshes[0].getBoundingInfo().boundingSphere.centerWorld.clone();

    let maxRadius = 0;
    allMeshes.forEach((mesh) => {
      const sphere = mesh.getBoundingInfo().boundingSphere;
      const dist = globeCenter.value.subtract(sphere.centerWorld).length() + sphere.radiusWorld;
      if (dist > maxRadius) maxRadius = dist;
    });
    globeRadius.value = maxRadius;

    fogAura.position = globeCenter.value;
    camera.value.setTarget(globeCenter.value);
    camera.value.radius = globeRadius.value * 1.2;
  }

  container.meshes.forEach((mesh) => {
    if (mesh.material && mesh.name === "globe") {
      const wireframeMesh = mesh.clone(`${mesh.name}_wireframe`);
      const wireMat = new StandardMaterial(`wireMat_${mesh.name}`, sceneLocal);
      wireMat.wireframe = true;
      wireMat.emissiveColor = new Color3(0.4, 0.8, 1.0);
      wireMat.disableLighting = true;
      wireMat.alpha = 0.3;
      wireframeMesh.material = wireMat;
      wireframeMesh.isPickable = false;
      wireframeMesh.scaling = wireframeMesh.scaling.multiplyByFloats(1.001, 1.001, 1.001);
    }
  });

  return sceneLocal;
}
</script>

<<<<<<< HEAD
<style scoped>
=======
<style>
>>>>>>> b772b0b1caa4a23035b5735d2a3ec0dd2efe4517
.globe-wrapper {
  position: absolute;
  top: 60px;
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

.tooltip.speech-bubble {
  position: absolute;
  background: #222;
  color: white;
  padding: 8px 12px;
  border-radius: 12px;
  max-width: 200px;
  font-size: 14px;
  pointer-events: auto;
  cursor: pointer;
  user-select: none;
  z-index: 20;
}

.tooltip.speech-bubble::after {
  content: "";
  position: absolute;
  bottom: -10px;
  left: 20px;
  border-width: 10px 10px 0;
  border-style: solid;
  border-color: #222 transparent transparent transparent;
  width: 0;
  height: 0;
}
</style>
