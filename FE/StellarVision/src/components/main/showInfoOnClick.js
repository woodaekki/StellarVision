import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Vector3, Matrix } from '@babylonjs/core';

export function showInfoOnClick({ engineRef, cameraRef, sceneRef }) {
  const router = useRouter();

  const tooltip = reactive({
    title: '',
    owner: '',
    style: {
      left: '0px',
      top: '0px',
      display: 'none',
    },
  })

  const activePin = ref(null)

  function updateTooltipPosition() {
    const engine = engineRef.value
    const camera = cameraRef.value
    const scene = sceneRef.value
    const pin = activePin.value

    if (!scene || !camera || !engine || !pin) {
      tooltip.style.display = 'none'
      return
    }

    const pos = Vector3.Project(
      pin.position,
      Matrix.Identity(),
      scene.getTransformMatrix(),
      camera.viewport.toGlobal(engine.getRenderWidth(), engine.getRenderHeight())
    )

    if (pos.z < 0 || pos.z > 1) {
      tooltip.style.display = 'none'
      return
    }

    tooltip.style.left = `${pos.x + 10}px`
    tooltip.style.top = `${pos.y - 40}px`
    tooltip.style.display = 'block'
  }

  function handlePinInteraction(mesh) {
    activePin.value = mesh
    tooltip.title = mesh.metadata?.title || '제목 없음'
    tooltip.owner = mesh.metadata?.ownerMemberName || '발신인 불명'
    updateTooltipPosition()
  }

  function handleWatchClick() {
    if (!activePin.value) return
    const sessionId = activePin.value.metadata?.sessionId
    if (sessionId) {
      router.push({
        name: 'RoomView',
        params: { id: sessionId }
      })
    };
  };

  return {
    tooltip,
    handlePinInteraction,
    handleWatchClick,
    updateTooltipPosition,
  }
}
