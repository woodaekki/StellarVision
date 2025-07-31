<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { OpenVidu } from 'openvidu-browser'
import ChatPanel from '@/components/comment/ChatPanel.vue'
import streamingService from '@/services/streamingService'

// 라우터
const route  = useRoute()
const router = useRouter()
const sessionId = route.params.id

// UI 상태
const showChat = ref(false)
const micOn    = ref(true)
const videoContainer = ref(null)

// OpenVidu 객체
let OV, session, publisher

// TODO: Pinia나 JWT payload에서 실제 role 가져오기
const isPublisher = computed(() => {
  // 예: store.user.role === 'publisher'
  return true
})

async function initSession() {
  OV      = new OpenVidu()
  session = OV.initSession()

  // 구독자용: 다른 퍼블리셔가 올린 스트림을 붙이기
  session.on('streamCreated', event => {
    session.subscribe(event.stream, videoContainer.value)
  })

  // 1) 먼저 백엔드에 join 요청 → 세션 토큰 획득
  let joinRes
  try {
    joinRes = await streamingService.join(sessionId, {
      role: isPublisher.value ? 'publisher' : 'subscriber',
      userName: '내이름' // 실제 로그인한 유저명으로 바꿔주세요
    })
  } catch (e) {
    console.error('세션 입장 실패', e)
    return alert('세션 입장에 실패했습니다.')
  }

  // API 응답 구조에 맞춰 토큰 꺼내기

  const joinToken = joinRes.data // 조정 필요 joinRes.data.token ||

  // 2) 세션에 연결
  await session.connect(joinToken)

  // 3) 퍼블리셔라면 자신의 스트림을 발행
  if (isPublisher.value) {
    publisher = OV.initPublisher(undefined, {
      publishAudio: micOn.value,
      publishVideo: true,
      resolution: '640x480'
    })
    session.publish(publisher)
  }
}
// 마이크 온오픛
function toggleMic() {
  micOn.value = !micOn.value
  if (publisher) publisher.publishAudio(micOn.value)
}
// 세션 나가기
function leaveSession() {
  if (session) session.disconnect()
  router.back()
}

onMounted(initSession)
onBeforeUnmount(() => {
  if (session) session.disconnect()
})
</script>

<!-- src/views/RoomView.vue -->
<template>
  <div class="room-container">
    <!-- 1) 종료 버튼 (우측 상단) -->
    <button class="btn-exit" @click="leaveSession">🚪 나가기</button>

    <!-- 2) 비디오 스트림이 붙을 영역 -->
    <div ref="videoContainer" class="video-container"></div>

    <!-- 3) 채팅 토글 (우측 하단) -->
    <button class="btn-chat-toggle" @click="showChat = !showChat">
      {{ showChat ? '채팅 끄기' : '채팅 켜기' }}
    </button>
    <ChatPanel v-if="showChat" />

    <!-- 4) 마이크 토글 (하단 중앙, 퍼블리셔만) -->
    <button
      v-if="isPublisher"
      class="btn-mic-toggle"
      @click="toggleMic"
    >
      {{ micOn ? '🔇' : '🎤' }}
    </button>
  </div>
</template>



<style scoped>
.room-container {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #000;
}
.video-container {
  width: 100%;
  height: 100%;
}
/* 버튼 위치 */
.btn-exit        { position: absolute; top: 1rem;    right: 1rem; }
.btn-chat-toggle { position: absolute; bottom: 1rem; right: 1rem; }
.btn-mic-toggle  { position: absolute; bottom: 1rem; left: 50%; transform: translateX(-50%); }
</style>
