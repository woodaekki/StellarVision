<script setup>
import { watch, defineEmits } from 'vue'
import { Vector3, MeshBuilder, StandardMaterial, Color3, PointerEventTypes } from '@babylonjs/core'

const emit = defineEmits(['pin-click'])

const props = defineProps({
  scene: Object,
  globeCenter: Object,
  globeRadius: Number,
  pins: Array,
})

// 받은 위도 경도 위치를 기준으로 아이콘의 좌표를 계산해서 반환
function latLngToPosition(lat, lng, radius, center) {
  const phi = (90 - lat) * (Math.PI / 180);
  const theta = (lng + 180) * (Math.PI / 180);
  const x = radius * Math.sin(phi) * Math.cos(theta);
  const y = radius * Math.cos(phi);
  const z = radius * Math.sin(phi) * Math.sin(theta);
  return new Vector3(x, y, z).add(center);
}

watch(
  () => [props.scene, props.globeCenter, props.globeRadius, props.pins],
  ([scene, center, radius, pins]) => {
    if (!scene || !center || !radius || !Array.isArray(pins)) return

    // 핀 중복 생성 방지
    scene.meshes
      .filter(mesh => mesh.name.startsWith('pin-'))
      .forEach(mesh => mesh.dispose())

    pins.forEach((pinData, idx) => {
      const { lat, lon } = pinData
      const position = latLngToPosition(lat, lon, radius * 0.458, center)

      // 핀 형태 결정 (추후 여기 부분을 수정해서 핀 모양을 바꿈)
      const pin = MeshBuilder.CreateSphere(`pin-${idx}`, {
        diameter: 0.6,
        segments: 16,
      }, scene)

      pin.position = position
      pin.lookAt(center)

      const mat = new StandardMaterial(`pin-mat-${idx}`, scene)
      mat.diffuseColor = new Color3(1, 0, 0)
      pin.material = mat

      // 클릭시 나타나는 정보 저장
      pin.metadata = { label: pinData.label }

      // 클릭 이벤트 추가
      scene.onPointerObservable.add((pointerInfo) => {
        if (
          pointerInfo.type === PointerEventTypes.POINTERPICK &&
          pointerInfo.pickInfo?.hit &&
          pointerInfo.pickInfo.pickedMesh === pin
        ) {
          emit('pin-click', pin)
        }
      })
    })
  },
  { immediate: true }
)
</script>
