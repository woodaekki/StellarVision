<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import openviduService from '@/services/openviduService'
import streamingService from '@/services/streamingService'
import { DoorOpen, ImageDown, MessageCircle, Mic, MicOff,  Square, SquareStop, ToggleLeft, ToggleRight } from 'lucide-vue-next'
import ChatPanel from '@/components/comment/ChatPanel.vue'
import { useRecordingStore } from '@/stores/recording'
import { createAIAnalyzeService } from '@/services/AIAnalyeService'
import { useAITagStore } from '@/stores/aiTags'

// ai 분석 결과를 담을 store
const aiTagStore = useAITagStore();


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

const aiOn = ref(false)
const toggleAI = () => { aiOn.value = !aiOn.value }

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
      } catch (err) {
        console.log(err)
    }
  }}
)


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
        // 태그들을 가져온다.
        const tags = aiTagStore.list(sessionId)
        console.log("인식된 태그들: ", tags);
        const res = await streamingService.stopRecording(recordingId.value, tags)
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

  const TEN_MIN = 10 * 1000
  let intervalId = null
  let inFlight = false

  // 기존 runOnceAll() 전체 교체
  async function runOnceAll() {
    // 토글 꺼져 있으면 실행 안 함
    if (!aiOn.value) return
    if (inFlight) return
    inFlight = true
    try {
      // 로컬 응답 로깅
      const localRes = await localAnalyzer.once()
      prettyLog('[AI][local]', localRes)
      if (isPublish.value) {    // 퍼블리셔일 때만 태그 누적
        aiTagStore.addFromPredictions(sessionId, localRes)
      }

      // 구독자별 응답 로깅
      for (const [streamId, { svc }] of subAnalyzers.entries()) {
        const subRes = await svc.once()
        prettyLog(`[AI][sub:${streamId}]`, subRes)
      }
    } catch (e) {
      console.debug('[AI] once error', e)
    } finally {
      inFlight = false
    }
  }

  // ✅ ADD: AI 응답 보기 좋게 콘솔에 출력
  function prettyLog(prefix, data) {
    try {
      const preds = data?.predictions ?? []
      console.group(prefix)
      console.log('총 개수:', preds.length)
      for (const p of preds) {
        const { class: klass, confidence, bbox } = p
        console.log(
          `• ${klass}  conf=${Number(confidence).toFixed(3)}  bbox=[${bbox?.map(n => Number(n).toFixed(2)).join(', ')}]`
        )
      }
      if (!preds.length) console.log('(빈 결과)')
      console.groupEnd()
    } catch (e) {
      console.log(prefix, data)
    }
  }

  // ✅ ADD: 토글에 따라 실행/정지
  watch(aiOn, (on) => {
    if (on) {
      runOnceAll() // 즉시 1회
      if (!intervalId) intervalId = window.setInterval(runOnceAll, TEN_MIN)
    } else {
      if (intervalId) { clearInterval(intervalId); intervalId = null }
      clearAllOverlays() // 선택: 화면에서 박스 지우기
    }
  })

  // ✅ ADD: 캔버스 클리어 함수
  function clearAllOverlays() {
    // 로컬
    const c = overlayLocal.value
    if (c) {
      const ctx = c.getContext('2d')
      if (ctx) ctx.clearRect(0, 0, c.width, c.height)
    }
    // 구독자
    for (const { canvas } of subAnalyzers.values()) {
      const ctx = canvas.getContext('2d')
      if (ctx) ctx.clearRect(0, 0, canvas.width, canvas.height)
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
    <div class="flex flex-col sm:flex-row w-full h-[100svh] w-[100vw] gap-0" alt="videos">
      <!-- 동영상 -->
      <div
        :class="['relative bg-black transition-all duration-300',
        showChat ? 'sm:w-[70%] w-full' :'w-full']"
        class="h-full rounded-none">

        <!-- 변경: 로컬 프리뷰 + 오버레이 -->
        <div class="relative w-full h-full">
          <video
            ref="localVideo"
            autoplay
            playsinline
            muted
            class="w-full h-full object-contain rounded-none"
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

          <!-- 스트리머 : 스트리밍 종료 버튼 -->
          <button v-if="isPublish" @click="endRoom"
            class="absolute right-3 top-3 z-10 bg-black bg-opacity-70
            text-white rounded-full px-3 py-1 hover:bg-red-600 transition">
            <DoorOpen/>
          </button>


          <!-- 시청자: 나가기 버튼 -->
          <button v-else @click="leave"
            class="absolute right-3 top-3 z-10 bg-black bg-opacity-70
            text-white rounded-full px-3 py-1 hover:bg-red-600 transition">
            <DoorOpen/>
          </button>

          <!-- AI 탐지 on/off -->
          <button
            @click="toggleAI"
            :aria-pressed="aiOn"
            :title="aiOn ? 'AI 탐지 끄기' : 'AI 탐지 켜기'"
            class="absolute right-20 top-2 z-10 bg-black bg-opacity-70 w-15 h-10 inline-flex
                 justify-center items-center text-white rounded-full px-3 py-1 hover:bg-gray-600 transition "
            :class="aiOn ? ' hover:text-sky-600' : 'bg-black/70 hover:bg-gray-600'">
            <component :is="aiOn ? ToggleLeft : ToggleRight"
              class="w-10 h-10 "
              :class="aiOn ? 'text-white/80' : 'text-sky-400'"/>
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
          v-show="showChat"
          class="ml-auto basis-0 grow sm:w-[30%] w-full min-w-[360px] max-w-[600px]
          h-full h-[700px] flex-shrink-0 bg-black pl-36">
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
