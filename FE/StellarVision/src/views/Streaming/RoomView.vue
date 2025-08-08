<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import openviduService from '@/services/openviduService'
import streamingService from '@/services/streamingService'
import { Cone, DoorOpen, ImageDown, MessageCircle, Mic, MicOff, Square, SquareStop } from 'lucide-vue-next'
import ChatPanel from '@/components/comment/ChatPanel.vue'
import { useRecordingStore } from '@/stores/recording'
import { createAIAnalyzeService } from '@/services/AIAnalyeService'

const route = useRoute()
const router = useRouter()
// 세션관련 객체
const sessionId = route.params.id
const userName = route.query.userName || 'Host'
const roomTitle = route.query.title

const isRecording = ref(false)
const recordingId = ref(null)
const isRecordingButtonDisabled = ref(false)
const recordingStore = useRecordingStore()

const showChat = ref(false)
const micEnabled = ref(true)

// 컴포저블에서 필요한 것들 모두 꺼냄
const { session, publisher, subscribers, connect, leave, setPublisherEl, attachSubEl } = openviduService(
   sessionId,
   userName,
   e => {
     alert('연결 실패')
     console.error(e)
     router.push({ name: 'PreRoomView' })
   }
 )

  // 로컬(퍼블리셔) 프리뷰 엘리먼트
  const localVideo = ref(null)
  // 로컬 오버레이 캔버스
  const overlayLocal = ref(null)

  // 로컬용 AI 분석기
  const localAnalyzer = createAIAnalyzeService({
    endpoint: 'https://i13c106.p.ssafy.io/api/detect/streaming', // TODO: 실제 주소로 교체
    minConfidence: 0.7,
    targetFps: 3,
  })

  // 구독자별 분석기/캔버스 관리
  const subAnalyzers = new Map() // streamId -> { svc, canvas }

  // 퍼블리셔/엘리먼트 준비 시 재부착 보강
  watch([publisher, localVideo], () => {
    if (publisher.value && localVideo.value) {
      try {
        publisher.value.addVideoElement(localVideo.value)
        localVideo.value.muted = true
        localVideo.value.setAttribute('playsinline','true')
        localVideo.value.setAttribute('autoplay','true')
        localVideo.value.play?.().catch(()=>{})
      } catch {}
    }
  })

  // 구독자 비디오에 AI 오버레이 붙이는 래퍼
  function attachSubElWithAI(sub, el) {
    attachSubEl(sub, el)            // 기존 OpenVidu 부착
    if (!el) return

    const streamId = sub.stream.streamId

    // 비디오 메타데이터 준비되면 시작
    const start = () => {
      // 비디오를 감싸는 컨테이너가 relative 아니면 덮을 수 없으므로 보장
      el.parentElement?.classList.add('relative')

      // 캔버스 생성 & 비디오 위에 얹기
      const canvas = document.createElement('canvas')
      canvas.className = 'pointer-events-none absolute inset-0'
      el.parentElement?.appendChild(canvas)

      // 구독자용 분석기 만들고 시작
      const svc = createAIAnalyzeService({
        endpoint: 'https://i13c106.p.ssafy.io/api/detect/streaming',
        minConfidence: 0.7,
        targetFps: 3
      })
      svc.attach({ video: el, overlay: canvas })

      subAnalyzers.set(streamId, { svc, canvas })
    }

    if (el.readyState >= 1 && el.videoWidth) start()
    else el.addEventListener('loadedmetadata', start, { once: true })
  }

  // 구독 스트림 종료 시 정리
  onMounted(() => {
    session.on('streamDestroyed', (ev) => {
      const streamId = ev.stream.streamId
      const found = subAnalyzers.get(streamId)
      if (found) {
        found.svc.destroy()
        found.canvas.remove()
        subAnalyzers.delete(streamId)
      }
    })
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
      const res = await streamingService.stopRecording(recordingId.value)
      isRecording.value = false
      recordingId.value = null
      recorded.value = res.data.data.recordingUrl
      console.log('res: ', res.data.data)
      recordingStore.setRecordingInfo(res.data.data)
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

  const TEN_MIN = 10 * 1000
  let intervalId = null
  let inFlight = false

  async function runOnceAll() {
    if (inFlight) return
    inFlight = true
    try {
      // 로컬 1회 분석
      await localAnalyzer.once()
      // 구독자 전부 1회 분석
      for (const { svc } of subAnalyzers.values()) {
        await svc.once()
      }
    } catch (e) {
      console.debug('[AI test] once error', e)
    } finally {
      inFlight = false
    }
  }

  onMounted(() => {
    // 퍼블리셔라면 프리뷰가 이 엘리먼트에 붙는다
    setPublisherEl(localVideo.value)
    session.on('recordingStarted', ()=>{ isRecording.value = true })
    session.on('recordingStopped', ()=>{ isRecording.value = false })

    // 로컬 분석 attach (start()는 호출하지 않음)
    const v = localVideo.value
    const attachLocal = () => {
      localAnalyzer.attach({ video: v, overlay: overlayLocal.value })
    }
    if (v?.readyState >= 1 && v.videoWidth) attachLocal()
    else v?.addEventListener('loadedmetadata', attachLocal, { once: true })

    runOnceAll()
    intervalId = window.setInterval(runOnceAll, TEN_MIN)
  })

  onUnmounted(() => {
    if (intervalId) { clearInterval(intervalId); intervalId = null }
    localAnalyzer.destroy()
    subAnalyzers.forEach(({ svc, canvas }) => { svc.destroy(); canvas.remove() })
    subAnalyzers.clear()
  })
</script>

<template>
  <div>
    <div class="flex flex-col sm:flex-row w-full max-w-[1600px] mx-auto gap-2" alt="videos">
      <!-- 동영상 -->
      <div
        style="height: 700px; background: #000;"
        :class="['relative bg-black transition-all duration-300',
        showChat ? 'sm:w-[70%] w-full' :'w-full']">

        <!-- 변경: 로컬 프리뷰 + 오버레이 -->
        <div class="relative w-full h-full">
          <video
            ref="localVideo"
            autoplay
            playsinline
            muted
            class="w-full h-full object-contain rounded-2xl"
          ></video>
          <!-- 로컬 오버레이 캔버스 -->
          <canvas ref="overlayLocal" class="pointer-events-none absolute inset-0"></canvas>
        </div>

        <!-- 변경: 원격 구독 영상 컨테이너 각 비디오를 relative 래퍼로 감싸고, ref 콜백을 attachSubElWithAI로 교체 -->
        <div class="absolute inset-0 grid gap-2 p-2 z-0">
          <div
            v-for="sub in subscribers"
            :key="sub.stream.streamId"
            class="relative w-full h-full">
            <video
              :data-stream-id="sub.stream.streamId"
              autoplay
              playsinline
              muted
              class="w-full h-full object-contain"
              :ref="el => attachSubElWithAI(sub, el)"
            ></video>
            <!-- 구독자 오버레이 캔버스는 JS에서 동적 생성해서 append -->
          </div>
        </div>

          <!-- 방 제목 -->
          <h2 class="text-xl text-white font-bold my-2 absolute left-3 top-3 z-10">
            방제목 {{ roomTitle }} — {{ userName }}
          </h2>

          <!-- 음성 버튼 -->
          <div class="absolute left-1/2 bottom-6 -translate-x-1/2 flex gap-4 z-10">
            <button
              @click="toggleMic"
              class="text-white rounded-full p-4 hover:bg-green-600 shadow transition">
              <component :is="micEnabled ? Mic : MicOff"/>
            </button>
            <!-- 녹화 버튼 -->
            <button
              @click="toggleRecording"
              :disabled="isRecordingButtonDisabled"
              class="bg-opacity-70 text-white rounded-full p-4 hover:bg-red-600 shadow transition">
              <component :is="isRecording ? Square : SquareStop" />
            </button>
          </div>

          <!-- 나가기 버튼 -->
          <button @click="leave"
            class="absolute right-3 top-3 z-10 bg-black bg-opacity-70
            text-white rounded-full px-3 py-1 hover:bg-red-600 transition">
            <DoorOpen/>
          </button>

          <!-- 캡처 버튼 -->
          <button
            class="absolute left-3 bottom-6 hover:bg-gray-600 transition shadow rounded-full text-white p-4">
            <ImageDown/>
          </button>

          <!-- 채팅 버튼 -->
          <button
            class="absolute right-3 bottom-6 hover:bg-gray-600 transition shadow rounded-full text-yellow p-4"
            @click="showChat = !showChat">
            <MessageCircle/>
          </button>
      </div>

      <transition name="fade">
        <div
          v-if="showChat"
          class="basis-0 grow sm:w-[30%] w-full min-w[480px] max-w-[600px]
          h-[700px] flex-shrink-0">
            <ChatPanel
              :session="session"
              @close="showChat = false"
              class="h-full"
            />
        </div>
      </transition>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity .2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
