import {
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
  ActionManager,
  ExecuteCodeAction,
} from '@babylonjs/core';
import { Animation } from '@babylonjs/core/Animations/animation.js';
import { CubicEase, EasingFunction } from '@babylonjs/core/Animations/easing.js';

export default async function createGlobeScene({
  engine,
  canvas,
  globeCenterRef,
  globeRadiusRef,
  isHoveringRef,
  cameraRef,
}) {
  // 표시될 화면을 설정
  // 이 화면이 투명한 배경을 가지도록 설정
  const scene = new Scene(engine);
  scene.clearColor = new Color4(0, 0, 0, 0);

  // 카메라 추가 (카메라 오브젝트가 지구본이 움직이는 것처럼 보이게 합니다)
  const camera = new ArcRotateCamera(
    'camera',
    Tools.ToRadians(45),
    Tools.ToRadians(60),
    100,
    new Vector3(0, 1, 0),
    scene
  );
  camera.attachControl(canvas, true);
  camera.lowerRadiusLimit = 50;
  cameraRef.value = camera;

  // 가장 짧은 각도로 회전하도록 조정
  function shortestTo(current, target) {
    let d = ((target - current + Math.PI) % (2 * Math.PI)) - Math.PI;
    return current + d;
  }

  function animateCamProp(cam, prop, to, duration = 1200) {
    const from = cam[prop];
    const fps = 60;
    const ease = new CubicEase();
    ease.setEasingMode(EasingFunction.EASINGMODE_EASEINOUT);
    return Animation.CreateAndStartAnimation(
      `fly_${prop}_${Date.now()}`,
      cam, prop, fps,
      Math.max(1, Math.round((duration/1000)*fps)),
      from, to,
      Animation.ANIMATIONLOOPMODE_CONSTANT, ease
    );
  }

  // ✅ 외부에서 호출할 수 있게 scene.metadata에 붙임
  scene.metadata ||= {};
  scene.metadata.isFlying = false;

  scene.metadata.flyToWorld = (worldPos, opts = {}) => {
    if (!camera || !worldPos) return;
    const dur = opts.duration ?? 1200;

    // ArcRotateCamera 기준: target(=지구 중심)을 기준으로 방향 벡터 계산
    const dir = worldPos.subtract(camera.target).normalize();
    const targetAlpha = Math.atan2(dir.z, dir.x);
    const targetBeta  = Math.acos(Math.min(Math.max(dir.y, -1), 1));

    scene.metadata.isFlying = true;
    const a1 = animateCamProp(camera, 'alpha', shortestTo(camera.alpha, targetAlpha), dur);
    const a2 = animateCamProp(camera, 'beta',  targetBeta, dur);

    let left = 2;
    const done = () => { if (--left === 0) scene.metadata.isFlying = false; };
    a1?.onAnimationEndObservable?.addOnce(done) || (a1.onAnimationEnd = done);
    a2?.onAnimationEndObservable?.addOnce(done) || (a2.onAnimationEnd = done);
  };

  // 빛나게 하는 효과들 추가
  new HemisphericLight('light', new Vector3(0, 1, 0), scene);

  const glow = new GlowLayer('glow', scene, {
    blurKernelSize: 64,
    intensity: 0.25,
  });

  const fogAura = MeshBuilder.CreateSphere('fogAura', { diameter: 72, segments: 96 }, scene);
  const fogMaterial = new StandardMaterial('fogMat', scene);
  fogMaterial.emissiveColor = new Color3(0.2, 0.8, 1.0);
  fogMaterial.alpha = 0.005;
  fogMaterial.backFaceCulling = false;
  fogMaterial.disableLighting = true;
  fogAura.material = fogMaterial;

  glow.addIncludedOnlyMesh(fogAura);
  fogAura.isPickable = false;

  const pipeline = new DefaultRenderingPipeline('defaultPipeline', true, scene, [camera]);
  pipeline.bloomEnabled = true;
  pipeline.bloomThreshold = 0.1;
  pipeline.bloomIntensity = 0.1;

  const container = await LoadAssetContainerAsync('/models/globe.glb', undefined, scene);
  container.addAllToScene();

  // 모델링의 중심점 설정 (이게 있어야 회전시킬 때 제자리에서 빙글빙글 도는 것처럼 위치를 고정시킬 수 있음)
  // 각 스트리밍 방으로 향하는 아이콘도 이 중심점을 기준으로 좌표가 계산됩니다
  const allMeshes = container.meshes.filter((m) => m.name !== '__root__' && m.isVisible);

  if (allMeshes.length > 0) {
    const center = allMeshes[0].getBoundingInfo().boundingSphere.centerWorld.clone();
    globeCenterRef.value = center;

    let maxRadius = 0;
    allMeshes.forEach((mesh) => {
      const sphere = mesh.getBoundingInfo().boundingSphere;
      const dist = center.subtract(sphere.centerWorld).length() + sphere.radiusWorld;
      if (dist > maxRadius) maxRadius = dist;
    });
    globeRadiusRef.value = maxRadius;

    fogAura.position = center;
    camera.setTarget(center);
    camera.radius = maxRadius * 1.5;
  }

  // 모델링의 와이어프레임 효과 추가 (지구본의 테두리 선이 보이는 효과는 전부 여기서 처리됩니다)
  container.meshes.forEach((mesh) => {
    if (mesh.material && mesh.name === 'globe') {
      const wireframeMesh = mesh.clone(`${mesh.name}_wireframe`);
      const wireMat = new StandardMaterial(`wireMat_${mesh.name}`, scene);
      wireMat.wireframe = true;
      wireMat.emissiveColor = new Color3(0.4, 0.8, 1.0);
      wireMat.disableLighting = true;
      wireMat.alpha = 0.3;
      wireframeMesh.material = wireMat;
      wireframeMesh.isPickable = false;
      wireframeMesh.scaling = wireframeMesh.scaling.multiplyByFloats(1.001, 1.001, 1.001);
    }
  });

  // 호버링할 경우 회전 멈추는 이벤트 헨들러
  const globeCoreMesh = allMeshes.find((m) => m.name === 'globe_core');
  if (globeCoreMesh) {
    globeCoreMesh.actionManager = new ActionManager(scene);
    globeCoreMesh.actionManager.registerAction(
      new ExecuteCodeAction(ActionManager.OnPointerOverTrigger, () => {
        isHoveringRef.value = true;
      })
    );
    globeCoreMesh.actionManager.registerAction(
      new ExecuteCodeAction(ActionManager.OnPointerOutTrigger, () => {
        isHoveringRef.value = false;
      })
    );
  }

  return scene;
}
