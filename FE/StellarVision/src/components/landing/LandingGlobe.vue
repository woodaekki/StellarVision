<template>
  <div class="globe-wrapper">
    <div class="globe-container">
        <canvas ref="canvas"></canvas>
        <div class="text-layer">
            <p class="text-above fade-in-up">A screen. A sky. A story.</p>
          <button @click="goToMain" class="start-button">시작하기</button>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
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
  MeshBuilder
} from "@babylonjs/core";

import { useRouter } from 'vue-router';
const router = useRouter();
function goToMain() {
  router.push('/main');
}

const showTextBelow = ref(true);

function animation() {
  showTextBelow.value = false;
}

const canvas = ref(null);
let engine = null;
let camera = null;

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

onMounted(async () => {
  engine = new Engine(canvas.value, true);
  setCanvasSize();
  engine.resize();

  const scene = await createScene();
  engine.runRenderLoop(() => {
    if (camera) {
      camera.alpha += 0.001; // 여기서 지구본 회전 속도 조정
    }
    scene.render();
  });


  window.addEventListener("resize", () => {
    setCanvasSize();
    engine.resize();
  });
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", () => {
    setCanvasSize();
    engine.resize();
  });
  if (engine) engine.dispose();
});

async function createScene() {
  const scene = new Scene(engine);
  scene.clearColor = new Color4(0, 0, 0, 0);

  camera = new ArcRotateCamera(
    "camera",
    Tools.ToRadians(45),
    Tools.ToRadians(60),
    100,
    new Vector3(0, 1, 0),
    scene
  );
  camera.attachControl(canvas.value, true);
  camera.inputs.removeByType("ArcRotateCameraMouseWheelInput");

  new HemisphericLight("light", new Vector3(0, 1, 0), scene);

  const glow = new GlowLayer("glow", scene, {
    blurKernelSize: 64,
    intensity: 0.25,
  });

  // 빛나는 효과 추가
  const fogAura = MeshBuilder.CreateSphere("fogAura", { diameter: 72, segments: 96 }, scene);
  const fogMaterial = new StandardMaterial("fogMat", scene);
  fogMaterial.emissiveColor = new Color3(0.2, 0.8, 1.0);
  fogMaterial.alpha = 0.02;
  fogMaterial.backFaceCulling = false;
  fogMaterial.disableLighting = true;
  fogAura.material = fogMaterial;

  glow.addIncludedOnlyMesh(fogAura);
  fogAura.isPickable = false;
  fogAura.visibility = 1;

  // 빛나는 효과
  const pipeline = new DefaultRenderingPipeline(
    "defaultPipeline",
    true,
    scene,
    [camera]
  );
  pipeline.bloomEnabled = true;
  pipeline.bloomThreshold = 0.1;
  pipeline.bloomIntensity = -100;
  pipeline.bloomKernel = 16;


  // 3D 모델링을 로딩, scene에 추가
  const container = await LoadAssetContainerAsync("/models/globe.glb", undefined, scene);
  container.addAllToScene();

  container.meshes.forEach(mesh => {
    if (mesh.material) {
      // 모델링의 매터리얼 호출
      const originalMaterial = mesh.material.clone(`${mesh.material.name}_${mesh.name}_clone`);
      mesh.material = originalMaterial;

      if (mesh.name !== "globe") {
        return;
      }

      // 와이어프레이밍 선따오기
      const wireframeMesh = mesh.clone(`${mesh.name}_wireframe`);

      const wireMat = new StandardMaterial(`wireMat_${mesh.name}`, scene);
      wireMat.wireframe = true;

      wireMat.emissiveColor = new Color3(0.4, 0.8, 1.0);

      wireMat.disableLighting = true;
      wireMat.alpha = 0.1;
      wireframeMesh.material = wireMat;

      wireframeMesh.isPickable = false;
      wireframeMesh.scaling = wireframeMesh.scaling.multiplyByFloats(1.001, 1.001, 1.001);
    }
  });



  // 지구본 기준으로 카메라 초기 위치를 고정
  const globeMesh = container.meshes.find((m) => m.name === "globe");
  if (globeMesh) {
    const boundingInfo = globeMesh.getHierarchyBoundingVectors();
    const center = boundingInfo.min.add(boundingInfo.max).scale(0.5);
    
    const newCenter = new Vector3(center.x, center.y - 10, center.z);

    fogAura.position = center;
    camera.setTarget(center);
    camera.radius = center.subtract(boundingInfo.max).length() * 2.35;
  }

  return scene;
}
</script>

<style>
.globe-wrapper {
  position: absolute;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #fff;
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

.text-layer {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  z-index: 2;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 2rem;
  text-align: center;
  pointer-events: none;
}

.text-above,
.text-below {
  color: #f2f2f2;
  font-weight: 700;
  font-size: 38px;
}

.text-below {
  color: #fff;
  animation: fadeOut 1.5s ease forwards;
  animation-delay: 1s; 
}


@keyframes fadeOut {
  to {
    opacity: 0;
    visibility: hidden;
  }
}

.start-button {
  position: absolute;
  bottom: 35px;
  z-index: 3;
  padding: 0.55rem 1.1rem;
  color: #fff;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  text-decoration: none;
  pointer-events: auto;
  max-width: max-content;
  font-size: 1rem;
  background-color: transparent;  
  border: 1px solid #f2f2f2; 
  transition: background-color 0.3s ease, color 0.3s ease; /* 부드러운 전환 효과 */

  &:hover {
    background-color: #f2f2f2; 
    color: #0b0c10; 
  }
}

@keyframes fade-in-up {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in-up {
  animation: fade-in-up 2s ease-out forwards;
  animation-delay: 0.5s; 
}

</style>
