<template>
  <div class="globe-wrapper">
    <div class="globe-container">
        <canvas ref="canvas"></canvas>
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
  Color4,
  Color3,
  HemisphericLight,
  Tools,
  DefaultRenderingPipeline,
  LoadAssetContainerAsync,
  PBRMaterial
} from "@babylonjs/core";

const emit = defineEmits(['ready'])

const canvas = ref(null);

let engine = null;
let camera = null;
let resizeHandler = null;
let zoomObserver = null;
let sceneRef = null;

let comp251 = [];

function setCanvasSize() {
  if (!canvas.value) return;
  const dpr = window.devicePixelRatio || 1;
  const w = canvas.value.clientWidth * dpr;
  const h = canvas.value.clientHeight * dpr;
  if (canvas.value.width !== w || canvas.value.height !== h) {
    canvas.value.width = w;
    canvas.value.height = h;
  }
}

function getVisibleMeshes(scene) {
  return scene.meshes.filter(m => m.isEnabled() && m.getTotalVertices && m.getTotalVertices() > 0);
}

function computeWorldBounds(meshes) {
  let min = new Vector3( Number.POSITIVE_INFINITY,  Number.POSITIVE_INFINITY,  Number.POSITIVE_INFINITY);
  let max = new Vector3(-Number.POSITIVE_INFINITY, -Number.POSITIVE_INFINITY, -Number.POSITIVE_INFINITY);
  meshes.forEach(m => {
    m.computeWorldMatrix(true);
    const bi = m.getBoundingInfo();
    min = Vector3.Minimize(min, bi.boundingBox.minimumWorld);
    max = Vector3.Maximize(max, bi.boundingBox.maximumWorld);
  });
  return { min, max, size: max.subtract(min), center: min.add(max).scale(0.5) };
}

function fitCameraToMeshes(scene, cam, meshes, margin = 1.2) {
  const { size, center } = computeWorldBounds(meshes);
  const aspect = engine.getRenderWidth() / Math.max(1, engine.getRenderHeight());
  const fovV = cam.fov;
  const fovH = 2 * Math.atan(Math.tan(fovV / 2) * aspect);

  const distH = (size.y / 2) / Math.tan(fovV / 2);
  const distW = (size.x / 2) / Math.tan(fovH / 2);
  const baseRadius = Math.max(distH, distW) * margin;

  cam.setTarget(center);
  cam.radius = Math.max(0.01, baseRadius);
  cam.lowerRadiusLimit = 0.01;
  cam.upperRadiusLimit = Number.MAX_SAFE_INTEGER;

  return { center, baseRadius };
}

function collect251Meshes(scene) {
  const set = new Set();
  scene.transformNodes.forEach(tn => {
    if (tn.name?.startsWith("Object_251")) {
      tn.getDescendants(true).forEach(d => { if (d.getTotalVertices) set.add(d); });
    }
  });
  scene.meshes.forEach(m => { if (m.name?.startsWith("Object_251")) set.add(m); });
  return Array.from(set);
}

function setup251ZoomComp(scene, cam) {
  const list251 = collect251Meshes(scene);
  comp251 = [];
  list251.forEach(mesh => {
    mesh.computeWorldMatrix(true);
    const pos = mesh.getAbsolutePosition();
    const dist = Vector3.Distance(cam.position, pos);
    comp251.push({
      mesh,
      baseScaling: mesh.scaling.clone(),
      refDist: Math.max(1e-6, dist)
    });
  });
}

function apply251Compensation(cam) {
  if (!comp251 || !comp251.length) return;
  for (const it of comp251) {
    const m = it.mesh;
    if (!m || !m.isEnabled()) continue;

    m.computeWorldMatrix(true);
    const pos = m.getAbsolutePosition();
    const dist = Vector3.Distance(cam.position, pos);
    const s = dist / it.refDist;
    m.scaling.set(it.baseScaling.x * s, it.baseScaling.y * s, it.baseScaling.z * s);
  }
}

function startCameraZoom(scene, cam, baseRadius, options = {}) {
  const {
    amplitudeRatio = 0.22,
    periodSec = 8.0,
    ease = (t)=>t*t*(3-2*t)
  } = options;

  if (zoomObserver) {
    scene.onBeforeRenderObservable.remove(zoomObserver);
    zoomObserver = null;
  }

  const amp   = Math.max(0.01, baseRadius * amplitudeRatio);
  const rMin = Math.max(0.01, baseRadius - amp);
  const rMax = baseRadius + amp;
  const pingpong = (x)=>1 - Math.abs((x%2)-1);

  const t0 = performance.now() / 1000;
  zoomObserver = scene.onBeforeRenderObservable.add(() => {
    const t = performance.now() / 1000 - t0;
    const k = ease(pingpong(t / (periodSec / 2)));
    cam.radius = rMin + (rMax - rMin) * k;

    apply251Compensation(cam);
  });
}

onMounted(async () => {
  engine = new Engine(canvas.value, true);
  setCanvasSize();
  engine.resize();

  const scene = await createScene();

  engine.runRenderLoop(() => {
    if (camera) camera.alpha += 0.008;
    scene.render();
  });

  resizeHandler = () => {
    setCanvasSize();
    engine.resize();
    if (sceneRef && camera) {
      const meshes = sceneRef.metadata?.shownMeshes?.length ? sceneRef.metadata.shownMeshes : getVisibleMeshes(sceneRef);
      const { baseRadius } = fitCameraToMeshes(sceneRef, camera, meshes, 1.2);

      setup251ZoomComp(sceneRef, camera);
      startCameraZoom(sceneRef, camera, baseRadius);
    }
  };

  requestAnimationFrame(() => emit('ready'));
  window.addEventListener("resize", resizeHandler);
});

onBeforeUnmount(() => {
  if (zoomObserver && sceneRef) {
    sceneRef.onBeforeRenderObservable.remove(zoomObserver);
    zoomObserver = null;
  }
  if (resizeHandler) window.removeEventListener("resize", resizeHandler);
  if (engine) engine.dispose();
});

async function createScene() {
  const scene = new Scene(engine);
  scene.clearColor = new Color4(0, 0, 0, 0);

  camera = new ArcRotateCamera("camera", Tools.ToRadians(45), Tools.ToRadians(60), 6.5, new Vector3(0, 0, 0), scene);
  camera.attachControl(canvas.value, true);
  camera.inputs.removeByType("ArcRotateCameraMouseWheelInput");

  const hemi = new HemisphericLight("hemi", new Vector3(0, 1, 0), scene);
  hemi.intensity = 0.6;

  const pipeline = new DefaultRenderingPipeline("defaultPipeline", true, scene, [camera]);
  pipeline.bloomEnabled = true;
  pipeline.bloomThreshold = 0.6;
  pipeline.bloomKernel = 48;
  (pipeline).bloomIntensity = 4.5;

  const container = await LoadAssetContainerAsync("/models/dot_globe.glb", scene);
  container.addAllToScene();

  container.animationGroups.forEach(g => { g.stop(); g.reset(); });

  const keepPrefixes = ["Object_249", "Object_250", "Object_251"];
  const keepSet = new Set();

  for (const m of [...scene.meshes, ...scene.transformNodes]) {
    if (keepPrefixes.some(p => m.name?.startsWith(p))) {
      keepSet.add(m);
    }
  }

  // keepSet에 있는 메쉬의 조상들도 포함시킵니다.
  const allNodes = Array.from(keepSet);
  for (const n of allNodes) {
    let parent = n.parent;
    while (parent) {
      keepSet.add(parent);
      parent = parent.parent;
    }
  }

  for (const m of scene.meshes) {
    if (!keepSet.has(m)) {
      m.setEnabled(false);
    }
  }

  const TINT_COLOR = new Color3(0.2, 0.3, 0.7);
  const TINT_INTENSITY = 0.3;

  for (const n of scene.meshes) {
    if (keepSet.has(n) && n.isEnabled() && n.getTotalVertices && n.getTotalVertices() > 0 && n.material) {
      if (n.material instanceof PBRMaterial) {
        n.material.albedoColor = n.material.albedoColor.add(TINT_COLOR).scale(0.5);
        n.material.emissiveColor = TINT_COLOR.scale(TINT_INTENSITY);
        n.material.emissiveIntensity = TINT_INTENSITY;
      } else {
        if (n.material.diffuseColor) {
          n.material.diffuseColor = n.material.diffuseColor.add(TINT_COLOR).scale(0.5);
        }
        if (n.material.specularColor) {
          n.material.specularColor = n.material.specularColor.add(TINT_COLOR).scale(0.5);
        }
      }
    }
  }

  const visible = Array.from(keepSet).filter(m => m.isEnabled?.() && m.getTotalVertices && m.getTotalVertices() > 0);
  scene.metadata = scene.metadata || {};
  scene.metadata.shownMeshes = visible;
  const { baseRadius } = fitCameraToMeshes(scene, camera, visible.length ? visible : getVisibleMeshes(scene), 2);

  setup251ZoomComp(scene, camera);

  startCameraZoom(scene, camera, baseRadius, {
    amplitudeRatio: 0.25,
    periodSec: 6.0
  });

  sceneRef = scene;
  return scene;
}
</script>

<style>
.globe-wrapper {
  position: absolute;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #fff;
  top: 25px;
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
  aspect-ratio: 1 / 1;
}

canvas:focus {
  outline: none;
}
</style>
