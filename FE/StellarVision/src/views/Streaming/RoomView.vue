<!-- RoomView.vue -->
<script setup>
  import { onMounted, onUnmounted, ref, watch, computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import openviduService from '@/services/openviduService'
  import streamingService from '@/services/streamingService'
  import { Download } from 'lucide-vue-next'
  import ChatPanel from '@/components/comment/ChatPanel.vue'
  import { useRecordingStore } from '@/stores/recording'
  import { createAIAnalyzeService } from '@/services/AIAnalyzeService'
  import { useAITagStore } from '@/stores/aiTags'
  import { createUpscaleService } from '@/services/upscaleService'
  import { createScreenShareService } from '@/services/screenShareService'
  import { useToast } from 'primevue/usetoast'
  import Toast from 'primevue/toast'
  import welcomImg from '@/assets/pictures/stellabot/logo.png'
  import aiOffImg from '@/assets/pictures/stellabot/aioff.png'
  import aiOnImg from '@/assets/pictures/stellabot/aiOn.png'

  import StreamerRoomComponent from '@/components/streaming/StreamerRoomComponent.vue'
  import ViewerRoomComponent from '@/components/streaming/ViewerRoomComponent.vue'
  import { useAIAnalysis } from '@/composables/streaming/useAIAnalysis'
  import { useEndModal } from '@/composables/streaming/useEndModal'
  import { useOpenViduBindings } from '@/composables/streaming/useOpenViduBindings'
  import { useUpscale } from '@/composables/streaming/useUpscale'
  import { makeSoftBoxRenderer } from '@/composables/streaming/renderers/softBoxRenderer'
  import { CONSTELLATION_KO } from '@/constants/constellations'
  import UpscalePreviewComponent from '@/components/streaming/UpscalePreviewComponent.vue'
  import ConstellationListPanel from '@/components/streaming/ConstellationListPanel.vue'

  // ai 분석 결과를 담을 store
  const aiTagStore = useAITagStore();
  const upscaleService = createUpscaleService();

  const route = useRoute()
  const router = useRouter()
  // 세션관련 객체
  const sessionId = route.params.id
  const userName = route.query.userName || route.query.streamerName ||  'Host'
  const roomTitle = route.query.title
  console.log('route.query', route.query)
  // 알림창 전용 객체
  const toast = useToast()
  const WELCOME_TOAST_KEY = `roomview_welcome_shown:${sessionId}`  // 세션별로 첫 입장 시

  // 녹화 전용 객체
  const isRecording = ref(false)
  const recordingId = ref(null)
  const isRecordingButtonDisabled = ref(false)
  const recordingStore = useRecordingStore()

  const showChat = ref(false)
  const micEnabled = ref(true)
  // ai 버튼 및 기능
  const aiOn = ref(false)
  const toggleAI = () => { aiOn.value = !aiOn.value }

  const {
    upscaledUrl,
    isDownloading,
    isUpscaling,
    hasUpscaled,
    resetUpscaled,
    downloadUpscaled,
    makeOnCaptureOrDownload
  } = useUpscale({ 
      upscaleService, 
      onUpscaleStart: () => showUpscaleBusy('고화질 업스케일링 중...', '잠시만 기다려 주세요'),
      onUpscaleEnd: hideUpscaleBusy, 
    })

  // 모달의 상태 변수
  const previewOpen = ref(false)
    watch(upscaledUrl, (v) => {
      if (v) previewOpen.value = true
  })


  // 컴포저블에서 필요한 것들 모두 꺼냄 (isPublish, role 추가)
  const { session, publisher, subscribers, leave, setPublisherEl, attachSubEl, isPublish, endRoom } = openviduService(
   sessionId,
   userName,
    e => {
      alert('연결 실패')
      console.error(e)
      router.push({ name: 'PreRoomView' })
    }
  )

  const boxRenderer = makeSoftBoxRenderer({
    // 필요 시 옵션 조정
    color: 'rgba(255,255,255,0.55)',
    strokeWidth: 1.25,
    radius: 0,                         // 완전 네모
    bboxFormat: 'auto',
    labelMap: (cls) => CONSTELLATION_KO[cls] ?? (cls || 'Object'),
  });

  // useAIAnalysis
  const {
    attachLocal,                  // 로컬 비디오/오버레이 attach
    attachSubElWithAIFactory,     // 구독자 attach + 오버레이 팩토리
    watchAI,                      // aiOn 토글 시 주기 실행/정지
    clearAllOverlays,             // 오버레이 지우기(overlayLocal 인자로)
    onStreamDestroyed,            // 구독 스트림 파괴 시 정리
    destroy,                      // 전체 정리
    selectedClass,
    setSelectedClass,
    detectedGroups,
    runOnceAll,
  } = useAIAnalysis({
    createAIAnalyzeService,
    endpoint: 'https://i13c106.p.ssafy.io/api/detect/streaming',
    isPublish,
    sessionId,
    aiTagStore,
    renderer: boxRenderer,
  })

 const detectedList = computed(() =>
   (detectedGroups.value || []).map(item => ({
     ...item,
     // getDetectedList()가 반환하는 식별자 키는 'code'
     code: item.code,
     nameKo: CONSTELLATION_KO[item.code] || item.code,
     confPct: Math.round((item.maxConf ?? 0) * 100),
   }))
 )

  // useOpenViduBindings
  const {
    localVideo,         // <video> ref
    overlayLocal,       // <canvas> ref
    onSetPublisherEl,   // 스트리머용 ref 콜백 (내부서 addVideoElement 및 setPublisherEl 보강)
    onSetLocalVideoEl,  // 시청자용 ref 콜백
    onSetOverlayEl,     // 오버레이 ref 콜백
    baseAttachSubEl,    // 구독자 attach 기본 함수
  } = useOpenViduBindings({ publisher, setPublisherEl, attachSubEl })

  // useEndModal
  const {
    showEndModal,
    endModalMsg,
    handleEndRoom,
    confirmEndModal,
    wireSessionEvents,
  } = useEndModal({ session, endRoom, leave, router, isPublish })

  watch([localVideo, overlayLocal], ([v, ov]) => {
    if (v && ov) {
      try { attachLocal(v, ov) } catch (e) { console.debug('[AI] local attach failed', e) }
    }
  })

  function pickTargetVideoEl() {
    // 1) 퍼블리셔면 로컬 비디오
    if (isPublish.value && localVideo.value) return localVideo.value

    // 2) 구독자면 첫 번째 구독 비디오 DOM을 선택
    const first = subscribers.value?.[0]
    if (first) {
      const el = document.querySelector(
        `video[data-stream-id="${first.stream.streamId}"]`
      )
      if (el) return el
    }

    // 3) fallback - 없으면 로컬 비디오라도 반환
    return localVideo.value
  }

  const onCaptureOrDownload = makeOnCaptureOrDownload(pickTargetVideoEl)

    // AI 오버레이 붙이는 래퍼
  const attachSubElWithAI = attachSubElWithAIFactory(baseAttachSubEl)

  // 퍼블리셔/엘리먼트 준비 시 재부착 보강
  watch([publisher, localVideo], () => {
    if (publisher.value && localVideo.value) {
      try {
        publisher.value.addVideoElement(localVideo.value)
        localVideo.value.muted = true
        localVideo.value.setAttribute('playsinline','true')
        localVideo.value.setAttribute('autoplay','true')
        localVideo.value.play?.().catch(()=>{})
      } catch (err) {
        console.log(err)
    }
  }}
)

  // 구독 스트림 종료 시 정리
  onMounted(() => {
    session.on('streamDestroyed', onStreamDestroyed)
  })

  // 녹화 시작/중단 api 요청
  async function toggleRecording() {
    try {
      isRecordingButtonDisabled.value = true

      if (!isRecording.value) {
        isRecordingButtonDisabled.value = true
        const res = await streamingService.startRecording(sessionId)
        recordingId.value = res.data.data
        console.log(res.data)
        console.log('record', recordingId)
        isRecording.value = true

        alert('녹화를 시작합니다!')
      } else {
        if (!recordingId.value) {
          alert('녹화ID가 없습니다. 새로고침 후 시도하세요.')
          return
        }
        // 태그들을 가져온다.
        const tags = aiTagStore.list(sessionId).map(String)
        const payload = { tags };
        console.log("인식된 태그들: ", tags);
        const res = await streamingService.stopRecording(recordingId.value, payload,
          { headers : { 'Content-Type': 'application/json'} }
        )
        isRecording.value = false
        recordingId.value = null
        console.log('res: ', res.data.data)
        recordingStore.setRecordingInfo(res.data.data)
        aiTagStore.clear(sessionId)
        alert('녹화를 중지합니다')
      }
    } catch (e) {

      if (e.response?.data?.error?.details === '409') {
        isRecording.value = true
        alert('이미 녹화가 진행 중입니다.')
      }

      alert(
        '녹화 오류: ' +
        (e.response?.data?.message || e.message) +
        (e.response?.data?.error?.details ? ` (code: ${e.response.data.error.code}, details: ${e.response.data.error.details})` : '')
      )
      console.error('녹화 토글 실패', e.response?.data || e)
    } finally {
      isRecordingButtonDisabled.value = false
    }
  }


  // 마이크 on/off 토글
  function toggleMic() {
    if (!publisher.value) return
    micEnabled.value = !micEnabled.value
    publisher.value.publishAudio(micEnabled.value)
  }

  // 토글에 따라 실행/정지
 watch(aiOn, () => {
   watchAI(aiOn, () => clearAllOverlays(overlayLocal))
   if (aiOn.value) {
     // 켜지자마자 한 프레임 추론하여 패널을 바로 채움
     // runOnceAll는 useAIAnalysis에서 이미 노출됨
     // (만약 import 누락이면 위 구조분해에서 runOnceAll 추가)
     try { runOnceAll?.(aiOn) } catch {}
   } else {
     // 끌 때 선택 해제(선택 유지 원하면 이 줄 삭제)
     setSelectedClass(null)
   }
 })


  // 환영 알림창
  function showWelcomeToast() {
    toast.add({
      severity: 'custom',
      summary: '환영합니다!',
      group: 'welcome',                  // 템플릿에서 이 group 받음
      styleClass: 'backdrop-blur-md rounded-2xl'  // 배경 블러 등
    })
    sessionStorage.setItem(WELCOME_TOAST_KEY, '1')
  }

  onMounted(() => {
    // 퍼블리셔라면 프리뷰가 이 엘리먼트에 붙는다
    onSetPublisherEl
    session.on('recordingStarted', ()=>{ isRecording.value = true })
    session.on('recordingStopped', ()=>{ isRecording.value = false })
    session.on('streamDestroyed', onStreamDestroyed)  // 스트림 종료 시 캔버스 정리

    // 진입 시 한 번만 노출
    if (!sessionStorage.getItem(WELCOME_TOAST_KEY)) {
      // 약간의 지연을 줘서 첫 렌더 후 노출 (선택)
      setTimeout(showWelcomeToast, 150)
    }

    wireSessionEvents()
  })

  
  // 화면공유 서비스 (시스템 오디오 공유 ON, 마이크 유지)
  const screenShare = createScreenShareService(
    { sessionRef: session, publisherRef: publisher },
    { shareSystemAudio: true, keepMic: true }
  )
  async function onToggleScreenShare() {
    if (!isPublish.value) {
      alert('스트리머만 화면공유가 가능합니다.')
      return
    }
    try {
      await screenShare.toggle()
    } catch (e) {
      console.error('[ScreenShare] toggle failed', e)
      alert('화면공유에 실패했습니다. 브라우저 권한/설정을 확인해주세요.')
    }
  }

  watch(screenShare.isScreenSharing, (sharing) => {
    const el = localVideo.value
    if (!el) return
    // 혹시 남아있을 수 있는 OpenVidu 미러 클래스 제거
    el.classList.remove('OV_mirrored')

    // 인라인 스타일로 최종 강제 (inline이 가장 셈)
    el.style.transform = sharing ? 'none' : 'scaleX(-1)' // 공유중엔 해제, 웹캠일 땐 거울
  }, { immediate: true })

  function showUpscaleBusy(
    summary = '고화질 업스케일링 중...',
    detail = '잠시만 기다려 주세요'
  ) {
    toast.add({
      severity: 'custom',
      summary,
      detail,
      group: 'busy',
      life: 0,            // 수동으로 닫을 때까지 유지
      closable: false,
      styleClass: 'backdrop-blur-lg rounded-2xl'
    })
  }

  function hideUpscaleBusy() {
    toast.removeGroup('busy')
  }

  onUnmounted(() => {
    destroy();  // AI/overlay 정리
    resetUpscaled() // 업스케일 blob 정리
    screenShare.destroy()
    // 알림창 해소
    if (interval.value) {
      clearInterval(interval.value);
    }
  })
</script>



<template>
  <div>
    <div class="flex flex-col sm:flex-row h-[100svh] w-[100vw] overflow-hidden bg-zinc-900" alt="videos">
      <!-- 동영상 -->
      <div
        :class="['relative bg-black transition-all duration-300',
        showChat ? 'sm:w-[70%] w-full' :'w-full']"
        class="relative flex-1 bg-black h-full rounded-none">

        <!-- 스트리머 화면 -->
        <StreamerRoomComponent
          v-if="isPublish"
          :subscribers="subscribers"
          :attachSubElWithAI="attachSubElWithAI"
          :roomTitle="roomTitle"
          :userName="userName"
          :setPublisherEl="onSetPublisherEl"
          :setOverlayEl="onSetOverlayEl"
          :onCaptureOrDownload="onCaptureOrDownload"
          :hasUpscaled="hasUpscaled"
          :isUpscaling="isUpscaling"
          :isDownloading="isDownloading"
          :micEnabled="micEnabled"
          :toggleMic="toggleMic"
          :isRecording="isRecording"
          :isRecordingButtonDisabled="isRecordingButtonDisabled"
          :toggleRecording="toggleRecording"
          :aiOn="aiOn"
          :toggleAI="toggleAI"
          :aiOnImg="aiOnImg"
          :aiOffImg="aiOffImg"
          :showChat="showChat"
          :onToggleChat="() => showChat = !showChat"
          :handleEndRoom="handleEndRoom"
          :isScreenSharing="screenShare.isScreenSharing.value"
          :toggleScreenShare="onToggleScreenShare"
        />

        <!-- 시청자 화면 -->
        <ViewerRoomComponent
          v-else
          :subscribers="subscribers"
          :attachSubElWithAI="attachSubElWithAI"
          :roomTitle="roomTitle"
          :userName="userName"
          :setLocalVideoEl="onSetLocalVideoEl"
          :setOverlayEl="onSetOverlayEl"
          :onCaptureOrDownload="onCaptureOrDownload"
          :hasUpscaled="hasUpscaled"
          :isUpscaling="isUpscaling"
          :isDownloading="isDownloading"
          :aiOn="aiOn"
          :toggleAI="toggleAI"
          :aiOnImg="aiOnImg"
          :aiOffImg="aiOffImg"
          :showChat="showChat"
          :onToggleChat="() => showChat = !showChat"
          :leave="leave"
        />

        <transition name="fade">
          <ConstellationListPanel
            v-if="aiOn"
            :items="detectedList"
            class="absolute top-16 right-3 z-50"
            :selectedCode="selectedClass"
            @select="setSelectedClass"
          />
        </transition>

      </div>
      <!-- 채팅 창 -->
      <transition name="chat-slide">
        <aside
          v-show="showChat"
          class="sm:w-[30%] w-full min-w-[360px] max-w-[400px]
          h-full  flex-shrink-0 bg-black z-40">
            <ChatPanel
              :session="session"
              @close="showChat = false"
              class="h-full"
            />
        </aside>
      </transition>

    </div>
    <!-- 업스케일링 로딩 토스트 -->
    <Toast position="top-center" group="busy">
      <template #container="{ message }">
        <section
          class="white-jelly-panel flex items-center gap-3"
        >
          <img
            src="@/assets/pictures/stellabot/logo.png"
            alt="Nova walking"
            class="w-7 h-10"
          />
          <div class="flex flex-col">
            <span class="font-bold leading-tight">{{ message.summary }}</span>
            <small class="opacity-80">{{ message.detail }}</small>
          </div>
        </section>
      </template>
    </Toast>


    <!-- 환영 토스트: 중앙 정렬 -->
    <Toast position="center" group="welcome">
      <template #container="{ closeCallback }">
        <!-- 카드 박스 -->
        <section
          class="w-[min(92vw,520px)] max-w-[520px] mx-auto
                 bg-black/80 text-white rounded-2xl shadow-2xl
                 p-6 sm:p-8 flex flex-col items-center gap-4">

          <!-- 이미지 영역 (원하는 이미지 URL로 교체) -->
          <img
            :src="welcomImg"
            alt="Welcome"
            class="w-24 h-24 object-contain"
            onerror="this.style.display='none'"
          />

          <!-- 환영 문구 -->
          <h3 class="text-2xl font-bold text-center">관측 준비 완료!</h3>
          <p class="text-center text-white/90">
            {{ userName }}님, 방 <span class="font-semibold">“{{ roomTitle || '스트리밍' }}”</span> 에 오신 걸 환영해요. <br/>
            캡처 버튼을 누르고 잠시 기다린 후
            <span class="inline-flex items-center gap-1 whitespace-nowrap align-middle">
            <Download class="w-5 h-5 inline-block" />
            <span>를 누르면 멋있는 사진을 간직할 수 있어요!</span>
          </span>
          </p>
          <!-- 액션 -->
          <div class="mt-2 flex gap-3">
            <button
              class="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/20 transition"
              @click="closeCallback">
              시작하기
            </button>
          </div>
        </section>
      </template>
    </Toast>
    <!-- 방송 종료 모달 -->
    <div
      v-if="showEndModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
      <section class="w-[min(92vw,380px)] max-w-[380px] bg-zinc-900 text-white rounded-2xl shadow-xl p-6">
        <h3 class="text-lg font-semibold mb-2">방송 종료</h3>
        <p class="text-zinc-200 mb-6">{{ endModalMsg }}</p>
        <div class="flex justify-end gap-2">
          <button
            class="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/20 transition"
            @click="confirmEndModal">
            확인
          </button>
        </div>
      </section>
    </div>


    <UpscalePreviewComponent
      v-model="previewOpen"
      :src="upscaledUrl || ''"
      @download="downloadUpscaled()"
      @closed="resetUpscaled"
    />
<!--  -->

  </div>
</template>

<style scoped>
/* 변경: 슬라이드 인/아웃 모션 정의 */
.chat-slide-enter-from,
.chat-slide-leave-to {
  transform: translateX(100%);     /* 오른쪽 밖에서 시작/종료 */
}

.chat-slide-enter-to,
.chat-slide-leave-from {
  transform: translateX(0);         /* 제자리 */
}

.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: transform 0.6s ease;  /* 부드럽게 */
}

.chat-slide-enter-active, .chat-slide-leave-active { will-change: transform; }

.white-jelly-panel {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 20px 24px;
  max-width: 800px;    /* ✅ 최대 가로 폭 */
  max-height: 100vh;    /* ✅ 최대 세로 높이 */
  overflow: auto;      /* 내용 넘치면 스크롤 */

  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);

  box-shadow:
    inset 3px 3px 6px rgba(255 255 255 / 0.5),
    inset -3px -3px 6px rgba(0 0 0 / 0.1);

  border: 1.2px solid rgba(255, 255, 255, 0.2);

  color: #fff !important;

  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  letter-spacing: 0.02em;

  transition: box-shadow 0.3s ease;
  cursor: default;
}

.white-jelly-panel:hover {
  box-shadow:
    inset 5px 5px 10px rgba(255 255 255 / 0.7),
    inset -5px -5px 10px rgba(0 0 0 / 0.15);
}
</style>
