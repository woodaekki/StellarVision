import {
  StandardMaterial,
  Color3,
  PointerEventTypes,
  TransformNode,
  MeshBuilder,
  Vector3
} from '@babylonjs/core';
import { LoadAssetContainerAsync } from '@babylonjs/core/Loading/sceneLoader.js';
import '@babylonjs/loaders/glTF';
import { adjustPinPosition } from './adjustPinPosition';

let pinContainer = null;
let sharedPinMat = null;

let _bobList = []; 
let _bobObserver = null;
let _bobTime = 0;

let _renderTicket = 0;

async function ensurePinTemplate(scene) {
  if (pinContainer) return pinContainer;
  // 여기서 핀의 모양이 되는 .glb 파일 선언
  pinContainer = await LoadAssetContainerAsync('/models/map_pin.glb', undefined, scene);
  return pinContainer;
}

// 핀 생성/갱신
export async function renderPins({ scene, globeCenter, globeRadius, pins }) {
  if (!scene || !globeCenter || !globeRadius || !Array.isArray(pins)) return;

  const my = ++_renderTicket;

  (scene.transformNodes || [])
    .filter(n => n.name?.startsWith('pin-root-'))
    .forEach(n => n.dispose());

  scene.meshes
    .filter(m => m.name?.startsWith('pin-') && !m.isDisposed() && !m.parent)
    .forEach(m => m.dispose());
  _bobList = [];

  const adjustedPins = adjustPinPosition(pins, globeRadius * 0.458, globeCenter);

  await ensurePinTemplate(scene);
  if (my !== _renderTicket) return;

  // 여기서 색&재질 설정
  if (!sharedPinMat || sharedPinMat.getScene() !== scene) {
    sharedPinMat = new StandardMaterial('pin-mat-shared', scene);
    sharedPinMat.diffuseColor = new Color3(1, 0, 0);
  }

  for (let idx = 0; idx < adjustedPins.length; idx++) {
    const pinData = adjustedPins[idx];
    const position = pinData._adjustedPosition;

    if (!position || isNaN(position.x) || isNaN(position.y) || isNaN(position.z)) {
      console.warn(`[${idx}] 핀 위치가 유효하지 않음:`, position);
      continue;
    }

    const holder = new TransformNode(`pin-root-${idx}`, scene);
    holder.position = position.clone();
    holder.lookAt(globeCenter);

    // 플로팅 모션이 적용된 node 생성
    const floatNode = new TransformNode(`pin-float-${idx}`, scene);
    floatNode.parent = holder;

    const inst = pinContainer.instantiateModelsToScene(
      (name) => `pin-${idx}-${name}`,
      false
    );
    inst.rootNodes.forEach(n => (n.parent = floatNode));

    // 클릭하기 쉬운 히트박스 생성
    const HIT_DIAM = Math.max(globeRadius * 0.02, 2.0); // 여기서 판정 범위 조정
    const hit = MeshBuilder.CreateSphere(`pin-${idx}-hit`, { diameter: HIT_DIAM, segments: 8 }, scene);
    hit.parent = floatNode;
    hit.isPickable = true;
    hit.visibility = 0;
    hit.metadata = {
      __isPin: true,
      title: pinData.title,
      ownerMemberName: pinData.ownerMemberName,
      sessionId: pinData.sessionId
    };

    // 템플릿화 된 속성으로 핀들 생성
    const childMeshes = [];
    inst.rootNodes.forEach(n => {
      if (typeof n.getChildMeshes === 'function') {
        childMeshes.push(...n.getChildMeshes());
      }
    });

    childMeshes.forEach((m, i) => {
      if (!m.name.startsWith('pin-')) m.name = `pin-${idx}${i ? `-${i}` : ''}`;
      m.isPickable = true;
      m.material = sharedPinMat;
      m.metadata = {
        __isPin: true,
        title: pinData.title,
        ownerMemberName: pinData.ownerMemberName,
        sessionId: pinData.sessionId
      };
      m.onDisposeObservable.addOnce(() => {
        if (!holder.isDisposed()) holder.dispose();
      });
    });

    // 여기서 각 핀들의 움직임을 설정
    const invWorld = holder.getWorldMatrix().clone(); invWorld.invert();
    const upLocal = Vector3.TransformNormal(Vector3.Up(), invWorld).normalize();
    const amp = globeRadius*0.0015; // 진폭
    const speed = 0.6 + Math.random() * 0.5;           // 속도
    const phase = Math.random() * Math.PI * 0.25;         // 위상(랜덤)
    _bobList.push({ node: floatNode, dir: upLocal, amp, speed, phase });
  }

  // 옵져버 등록 (이게 있어야 핀들 떠다니는 애니메이션이 적용)
  if (!_bobObserver) {
    _bobObserver = scene.onBeforeRenderObservable.add(() => {
      const dt = scene.getEngine().getDeltaTime() / 1000; // 초 단위
      _bobTime += dt;
      for (const b of _bobList) {
        const offset = Math.sin(b.phase + _bobTime * b.speed) * b.amp;
        b.node.position = b.dir.scale(offset)
      }
    });
  }
}

// 클릭 이벤트 핸들링
export function setupPinClickHandler(scene, handlePinInteraction, onBlankClick) {
  if (!scene) return;

  let pointerDown = false;
  let moved = false;

  scene.onPointerObservable.add((pointerInfo) => {
    switch (pointerInfo.type) {
      case PointerEventTypes.POINTERDOWN:
        pointerDown = true;
        moved = false;
        break;
      case PointerEventTypes.POINTERMOVE:
        if (pointerDown) moved = true;
        break;
      case PointerEventTypes.POINTERUP:
        if (!moved) {
          const pick = pointerInfo.pickInfo;
          const mesh = pick?.pickedMesh;
          const isPin =
            !!(mesh && (mesh.metadata?.__isPin === true || mesh.name?.startsWith?.('pin-')));

          if (isPin) {
            handlePinInteraction(mesh);
          } else {
            // 빈 곳 클릭이거나, 핀이 아닌 다른 메쉬 클릭 시
            onBlankClick && onBlankClick();
          }
        }
        pointerDown = false;
        moved = false;
        break;
    }
  });
}
