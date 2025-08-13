// src/composables/streaming/useEndModal.js
import { ref } from 'vue'

export function useEndModal({ session, endRoom, leave, router, isPublish }) {
  const showEndModal = ref(false)
  const endModalMsg = ref('스트리머가 스트리밍을 종료했어요.')

  async function handleEndRoom() {
    try {
      await session.signal({ type: 'room-ended', data: JSON.stringify({ ts: Date.now() }) }).catch(()=>{})
      await endRoom()
    } catch (e) {
      console.error('방 종료 실패', e)
      alert('방 종료 중 오류가 발생했습니다.')
    }
  }

  function openEndModal(msg) {
    endModalMsg.value = msg || '스트리머가 스트리밍을 종료했어요.'
    showEndModal.value = true
  }
  function confirmEndModal() {
    showEndModal.value = false
    leave()
    router.replace({ name: 'PreRoomView', query: { ended: '1' } })
  }

  function wireSessionEvents() {
    session.on('sessionDisconnected', (ev) => {
      if (!isPublish.value) {
        const reason = ev?.reason || ''
        if (reason.includes('session') || reason.includes('force') || reason.includes('network') || reason.includes('disconnect')) {
          openEndModal('스트리머가 스트리밍을 종료했어요.')
        }
      }
    })
    session.on('signal:room-ended', () => {
      if (!isPublish.value) openEndModal('스트리머가 스트리밍을 종료했어요.')
    })
  }

  return { showEndModal, endModalMsg, handleEndRoom, openEndModal, confirmEndModal, wireSessionEvents }
}
