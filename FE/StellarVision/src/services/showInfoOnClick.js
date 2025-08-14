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

  function hideTooltip() {
    activePin.value = null;
    tooltip.title = '';
    tooltip.owner = '';
    tooltip.style.display = 'none';
  }

  function updateTooltipPosition() {
    const engine = engineRef.value
    const camera = cameraRef.value
    const scene = sceneRef.value
    const pin = activePin.value

    if (!scene || !camera || !engine || !pin) {
      tooltip.style.display = 'none'
      return
    }

  const world = (pin.getAbsolutePosition ? pin.getAbsolutePosition() : pin.position)
  const pos = Vector3.Project(
      world,
      Matrix.Identity(),
      scene.getTransformMatrix(),
      camera.viewport.toGlobal(engine.getRenderWidth(), engine.getRenderHeight())
    )

    if (pos.z < 0 || pos.z > 1) {
      tooltip.style.display = 'none'
      return
    }

    const rect = engine.getRenderingCanvasClientRect
     ? engine.getRenderingCanvasClientRect()
     : engine.getRenderingCanvas().getBoundingClientRect();
    const cssX = (pos.x / engine.getRenderWidth())  * rect.width;
    const cssY = (pos.y / engine.getRenderHeight()) * rect.height;

    tooltip.style.left = `${cssX + 10}px`
    tooltip.style.top  = `${cssY - 40}px`
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
        params: { id: sessionId },
        query: {
          title : activePin.value.metadata?.title,
          streamerName : activePin.value.metadata?.ownerMemberName
        }
      })
    };
  };

  return {
    tooltip,
    handlePinInteraction,
    handleWatchClick,
    updateTooltipPosition,
    hideTooltip
  }
}
