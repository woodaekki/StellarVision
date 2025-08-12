import {
  PointerEventTypes,
  TransformNode,
  MeshBuilder,
  Vector3,
  Color3
} from '@babylonjs/core';
import { LoadAssetContainerAsync } from '@babylonjs/core/Loading/sceneLoader.js';
import '@babylonjs/loaders/glTF';
import { adjustPinPosition } from './adjustPinPosition';

let pinContainer = null;

let _bobList = [];
let _bobObserver = null;
let _bobTime = 0;

let _renderTicket = 0;

async function ensurePinTemplate(scene) {
  if (pinContainer && pinContainer.scene !== scene) {
    try { pinContainer.dispose?.(); } catch {}
    pinContainer = null;
  }

  if (pinContainer) return pinContainer;

  // 여기서 핀의 모양이 되는 .glb 파일 선언
  pinContainer = await LoadAssetContainerAsync('/models/map_pin.glb', undefined, scene);
  return pinContainer;
}

const BASE_RED = new Color3(1.0, 0.16, 0.16);
const ORANGE = new Color3(1.0, 0.749, 0.0)

// 선택된 핀에만 추가 효과 적용
function setEmissive(mat, color, intensity) {
  if (!mat || !mat.getClassName) return;
  const klass = mat.getClassName();

  if (klass === 'PBRMaterial') {
    mat.emissiveColor = color;
    if ('emissiveIntensity' in mat) mat.emissiveIntensity = intensity;
  } else if (klass === 'StandardMaterial') {
    mat.emissiveColor = color.scale(intensity);
  }
}

function ensureUniqueMaterial(mesh, suffix = '') {
  const mat = mesh.material;
  if (!mat) return;
  if (mesh.metadata?.__pinHasUniqueMat) return;

  const cloned = mat.clone?.(`${mat.name || 'mat'}-pin${suffix ? `-${suffix}` : ''}`);
  if (cloned) {
    mesh.material = cloned;
    mesh.metadata ||= {};
    mesh.metadata.__pinHasUniqueMat = true;
  }
}

function applyBaseTint(mesh)    { setEmissive(mesh.material, BASE_RED, 0.18); }
function applyHighlight(mesh)   { setEmissive(mesh.material, ORANGE, 0.35); }

// === 추가: 배열 대상 유틸 ===
function forEachMesh(target, fn) {
  const list = Array.isArray(target) ? target : [target];
  list.forEach(m => fn(m));
}
function ensureUniqueMaterials(target, suffix) {
  forEachMesh(target, m => ensureUniqueMaterial(m, suffix));
}
function highlightTarget(target) {
  forEachMesh(target, m => applyHighlight(m));
}
function baseTintTarget(target) {
  forEachMesh(target, m => applyBaseTint(m));
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

  console.log('[pins] adjustedPins:', adjustedPins.length);
  console.log('[pins] container exist?', !!pinContainer);
  console.log('[pins] container.scene === scene ?', pinContainer?.scene === scene);

  for (let idx = 0; idx < adjustedPins.length; idx++) {
    const pinData = adjustedPins[idx];
    const position = pinData._adjustedPosition;

    (scene.metadata ||= {})._pinWorldPosById ??= new Map();
    scene.metadata._pinWorldPosById.set(pinData.id, position.clone());

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
    console.log(`[pins] inst #${idx} roots:`, inst.rootNodes?.length || 0);

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

    // 클릭하면 강조되게
    hit.metadata.__pinMeshes = childMeshes;

    childMeshes.forEach((m, i) => {
      if (!m.name.startsWith('pin-')) m.name = `pin-${idx}${i ? `-${i}` : ''}`;
      m.isPickable = true;
      applyBaseTint(m);
      m.metadata = {
        __isPin: true,
        title: pinData.title,
        ownerMemberName: pinData.ownerMemberName,
        sessionId: pinData.sessionId,
        __pinMeshes: childMeshes
      };
      m.onDisposeObservable.addOnce(() => {
        if (!holder.isDisposed()) holder.dispose();
      });
    });

    // 여기서 각 핀들의 움직임을 설정
    const invWorld = holder.getWorldMatrix().clone(); invWorld.invert();
    const upLocal = Vector3.TransformNormal(Vector3.Up(), invWorld).normalize();
    const amp = globeRadius*0.0015; // 진폭
    const speed = 2.1 + Math.random();           // 속도
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
  let _hovered = null;   // 보이는 메쉬 배열(or null)
  let _selected = null;  // 보이는 메쉬 배열(or null)

  scene.onPointerObservable.add((pointerInfo) => {
    switch (pointerInfo.type) {
      case PointerEventTypes.POINTERDOWN:
        pointerDown = true;
        moved = false;
        break;

      case PointerEventTypes.POINTERMOVE: {
        // hover 감지 및 하이라이트
        const pick = pointerInfo.pickInfo;
        const mesh = pick?.pickedMesh;
        const isPin = !!(mesh && (mesh.metadata?.__isPin === true || mesh.name?.startsWith?.('pin-')));

        const visuals = isPin ? (mesh.metadata?.__pinMeshes || [mesh]) : null;
        if (visuals !== _hovered) {
          // 이전 hover 해제 (선택된 대상이 아니면 기본으로 복귀)
          if (_hovered && _hovered !== _selected) baseTintTarget(_hovered);
          _hovered = visuals;

          // 신규 hover 강조
          if (_hovered && _hovered !== _selected) {
            ensureUniqueMaterials(_hovered, 'hover');
            highlightTarget(_hovered);
          }
        }
        if (pointerDown) moved = true;
        break;
      }

      case PointerEventTypes.POINTERUP: {
        if (!moved) {
          const pick = pointerInfo.pickInfo;
          const mesh = pick?.pickedMesh;
          const isPin =
            !!(mesh && (mesh.metadata?.__isPin === true || mesh.name?.startsWith?.('pin-')));

          if (isPin) {
            const visuals = mesh.metadata?.__pinMeshes || [mesh];
            // 이전 선택 복귀
            if (_selected && _selected !== visuals) {
              baseTintTarget(_selected);
            }
            // 새 선택 강조 고정
            _selected = visuals;
            ensureUniqueMaterials(_selected, 'selected');
            highlightTarget(_selected);

            handlePinInteraction(mesh);
          } else {
            // 빈 곳 클릭 시 선택 해제
            if (_selected) { baseTintTarget(_selected); _selected = null; }
            onBlankClick && onBlankClick();
          }
        }
        pointerDown = false;
        moved = false;
        break;
      }
    }
  });
}
