import {
  MeshBuilder,
  StandardMaterial,
  Color3,
  PointerEventTypes
} from '@babylonjs/core';
import { adjustPinPosition } from './adjustPinPosition';

export function renderPins({ scene, globeCenter, globeRadius, pins }) {

  if (!scene || !globeCenter || !globeRadius || !Array.isArray(pins)) {
    return;
  }

  scene.meshes
    .filter(mesh => mesh.name.startsWith('pin-'))
    .forEach(mesh => mesh.dispose());

  const adjustedPins = adjustPinPosition(pins, globeRadius * 0.458, globeCenter);

  adjustedPins.forEach((pinData, idx) => {
    const position = pinData._adjustedPosition;

    if (!position || isNaN(position.x) || isNaN(position.y) || isNaN(position.z)) {
      console.warn(`[${idx}] 핀 위치가 유효하지 않음:`, position);
      return;
    }

    // 여기에 핀 모양이 결정됨
    const pin = MeshBuilder.CreateSphere(`pin-${idx}`, {
      diameter: 0.6,
      segments: 8,
    }, scene);

    pin.position = position;
    pin.lookAt(globeCenter);

    const mat = new StandardMaterial(`pin-mat-${idx}`, scene);
    mat.diffuseColor = new Color3(1, 0, 0);
    pin.material = mat;

    pin.metadata = {
      title: pinData.title,
      ownerMemberName: pinData.ownerMemberName,
      sessionId: pinData.sessionId
    };
  });
}

// 클릭시 이벤트 헨들링
export function setupPinClickHandler(scene, handlePinInteraction) {
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
        if (pointerDown) {
          moved = true;
        }
        break;
      case PointerEventTypes.POINTERUP:
        if (!moved && pointerInfo.pickInfo?.hit) {
          const mesh = pointerInfo.pickInfo.pickedMesh;
          if (mesh && mesh.name.startsWith('pin-')) {
            handlePinInteraction(mesh);
          }
        }
        pointerDown = false;
        moved = false;
        break;
    }
  });
}
