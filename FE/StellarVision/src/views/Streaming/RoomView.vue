<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import openviduService from '@/services/openviduService'
import streamingService from '@/services/streamingService'
import { DoorOpen, Camera, Download, MessageCircle, Mic, MicOff,  Square, SquareStop, ToggleLeft, ToggleRight } from 'lucide-vue-next'
import ChatPanel from '@/components/comment/ChatPanel.vue'
import { useRecordingStore } from '@/stores/recording'
import { createAIAnalyzeService } from '@/services/AIAnalyeService'
import { useAITagStore } from '@/stores/aiTags'
import { createUpscaleService } from '@/services/upscaleService'
import { latLngToPosition } from '@/services/latLngToPosition'
import { computed } from 'vue'

import { useToast } from 'primevue/usetoast'
import Toast from 'primevue/toast'
import ProgressBar from 'primevue/progressbar'
import Button from 'primevue/button'
import welcomImg from '@/assets/pictures/stellabot/logo.png'
import aiOffImg from '@/assets/pictures/stellabot/aioff.png'
import aiOnImg from '@/assets/pictures/stellabot/aiOn.png'

// ai 분석 결과를 담을 store
const aiTagStore = useAITagStore();
const upscaleService = createUpscaleService();

// 업스케일링 URL과 파일명
const upscaledUrl = ref(null)
const isDownloading = ref(false)
const isUpscaling = ref(false)

const route = useRoute()
const router = useRouter()
// 세션관련 객체
const sessionId = route.params.id
const userName = route.query.userName || 'Host'
const roomTitle = route.query.title

// 알림창 전용 객체
const toast = useToast()
const visible = ref(false);
const progress = ref(0);
const interval = ref();
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
// 업스케일링
const hasUpscaled = computed(() => !!upscaledUrl.value)

// 방 종료 모달 상태 추가
const showEndModal = ref(false)
const endModalMsg = ref('스트리머가 스트리밍을 종료했어요.') // 기본 메시지

// 캡쳐와 다운로드를 구분하는 함수
async function onCaptureOrDownload() {
  if (hasUpscaled.value) {
    await downloadUpscaled()
  } else {
    await captureAndUpscale()
  }
}

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



  // 현재 프레임을 캡쳐해 JPEG Blob으로 반환
  async function captureVideoFrame(videoEl, type = 'image/jpeg', quality = 0.92) {
    if (!videoEl || !videoEl.videoWidth || !videoEl.videoHeight) {
      throw new Error('비디오 준비가 안 됨(loadedmetadata 이후 시도)')
    }
    const canvas = document.createElement('canvas')
    canvas.width = videoEl.videoWidth
    canvas.height = videoEl.videoHeight
    const ctx = canvas.getContext('2d', { willReadFrequently: false })
    ctx.drawImage(videoEl, 0, 0, canvas.width, canvas.height)

    return new Promise((resolve, reject) => {
      canvas.toBlob((blob) => {
        if (!blob) return reject(new Error('캡쳐 실패'))
        resolve(blob)
      }, type, quality)
    })
  }

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

  function setUpscaledResult(urlFromServer) {
    upscaledUrl.value = urlFromServer
  }

  function buildFilenameFromUrl(url, fallback = 'upscaled.jpg') {
    try {
      const u = new URL(url)
      const name = u.pathname.split('/').pop()
      if (!name || !name.includes('.')) return fallback
      return name
    } catch {
      return fallback
    }
  }

  async function downloadUpscaled() {
    if (!upscaledUrl.value) return
    const filename = buildFilenameFromUrl(upscaledUrl.value, `upscaled_${Date.now()}.jpg`)

    if (isDownloading.value) return
    isDownloading.value = true
    try {
      // 1) blob으로 받아서 ObjectURL로 강제 다운로드 (가장 호환성 좋음)
      const res = await fetch(upscaledUrl.value, {
        // 필요시 인증 쿠키가 있다면:
        // credentials: 'include',
        // CORS가 필요하면 서버에서 허용해야 함(아래 주의사항 참고)
      })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const blob = await res.blob()
      const objUrl = URL.createObjectURL(blob)

      const a = document.createElement('a')
      a.href = objUrl
      a.download = filename // 저장 파일명 지정
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(objUrl)

      // 상태 초기화 -> 다시 캡쳐 할 수 있게 08.11 23:18
      resetUpscaled()
    } catch (err) {
      console.warn('blob 다운로드 실패, 새 탭으로 오픈 시도', err)
      // 2) 최후: 새 탭으로 열어서 사용자가 저장하도록 유도
      const a = document.createElement('a')
      a.href = upscaledUrl.value
      a.target = '_blank'
      a.rel = 'noopener'
      document.body.appendChild(a)
      a.click()
      a.remove()
      // 실패 시 알람 추가
      if(interval.value) {clearInterval(interval.value); interval.value=null}
      toast.add({severity:'error', summary:'다운로드 실패', detail: err?.message ?? 'unknown', life:3000})
      toast.removeGroup('download')
      visible.value = false
    } finally {
      isDownloading.value = false
    }
  }



  async function captureAndUpscale() {
    if (isUpscaling.value) return
    try {
      // 알림창 추가
      startDownloadToast('이미지 업스케일 중...')
      isUpscaling.value = true
      // 기존 URL revoke
      if (upscaledUrl.value?.startsWith('blob:')) {
        URL.revokeObjectURL(upscaledUrl.value)
        upscaledUrl.value = null
      }

      const target = pickTargetVideoEl()
      if (!target) {
        alert('캡쳐할 비디오가 없습니다.')
        return
      }

      // 1) 현재 프레임 캡쳐
      const captured = await captureVideoFrame(target, 'image/jpeg', 0.92)

      // 2) 업스케일 서버에 전송 → blob 응답
      const upscaledBlob = await upscaleService.upscaleImage(captured, 'capture.jpg')

      // 3) Object URL로 미리보기/다운로드 제공
      const url = URL.createObjectURL(upscaledBlob)
      upscaledUrl.value = url;

      // 3-1) 새 탭으로 열기(미리보기)
      window.open(url, '_blank', 'noopener')


    } catch (e) {
      console.error('업스케일 실패', e)
      alert('업스케일 중 오류가 발생했습니다: ' + (e?.message ?? 'unknown'))
    } finally {
      isUpscaling.value = false
    }
  }

  // 다운로드 후 초기화 로직  08-11 23:16분
  function resetUpscaled() {
  const url = upscaledUrl.value
  if (url?.startsWith('blob:')) URL.revokeObjectURL(url)
  upscaledUrl.value = null           // => hasUpscaled=false, 아이콘이 Camera로 변경되게끔 유도
}


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

// 스트리머가 종료시 시청자도 자동 종료하는 로직
async function handleEndRoom() {
  try {
    await session.signal({  // 시그널로 보내면
      type: 'room-ended',
      data: JSON.stringify({ ts: Date.now() })
    }).catch(() => {})
    await endRoom()  // 기다린 후 시청자 전용 로직인 endRoom을 실행한다
  } catch (e) {
    console.error('방 종료 실패', e)
    alert('방 종료 중 오류가 발생했습니다.')
  }
}


// 모달 열기/확인 핸들러
function openEndModal(msg) {
  endModalMsg.value = msg || '스트리머가 스트리밍을 종료했어요.'
  showEndModal.value = true
}
function confirmEndModal() {
  showEndModal.value = false
  leave()
  router.replace({ name: 'PreRoomView', query: { ended: '1' } })
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

  // 다운로드 알림 띄우기
  function startDownloadToast(summary = '이미지 캡쳐 중입니다...'){
    toast.add({
      severity: 'custom',
      summary,
      group: 'download',
      styleClass: 'backdrop-blur-lg rounded-2xl'
    })
    visible.value = true
    progress.value = 0
    // 임의로 진행하는 가짜 진행 바입니다 대충 5초? 정도 걸리게 해놨음. 더 천천히 하길 원한다면 +=7을 조절하면 됨
    if (interval.value) clearInterval(interval.value)
    interval.value = setInterval(()=>{
      if (progress.value <= 100) {
        progress.value +=4.4
      }
      if (progress.value >=100) {
        progress.value = 100
        clearInterval(interval.value)
      }
      }, 300)
  }


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

    // 진입 시 한 번만 노출
    if (!sessionStorage.getItem(WELCOME_TOAST_KEY)) {
      // 약간의 지연을 줘서 첫 렌더 후 노출 (선택)
      setTimeout(showWelcomeToast, 150)
    }

    // 스트리머 종료 감지 -> 시청자 모달 발생 & 자동 처리 로직 실행
    session.on('sessionDisconnected', (ev) => {
      if (!isPublish.value) {   // 호스트가 아니라면
        const reason = ev?.reason || ''
      if (reason.includes('session') || reason.includes('force') || reason.includes('network') || reason.includes('disconnect')) {
          openEndModal('스트리머가 스트리밍을 종료했어요.')
        }
      }
    })
    // 커스텀 종료 시그널 수신
    session.on('signal:room-ended', (e) => {
    if (!isPublish.value) {
      openEndModal('스트리머가 스트리밍을 종료했어요.') // 여기서 leave 로직 실행됨
    }
  })
  })

  onUnmounted(() => {
    if (intervalId) { clearInterval(intervalId); intervalId = null }
    localAnalyzer.destroy()
    subAnalyzers.forEach(({ svc, canvas }) => { svc.destroy(); canvas.remove() })
    subAnalyzers.clear()

    if (upscaledUrl.value?.startsWith('blob:')) {
      URL.revokeObjectURL(upscaledUrl.value)
      upscaledUrl.value = null
    }

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

        <!-- 변경: 로컬 프리뷰 + 오버레이 -->
        <div class="relative w-full h-full">
          <video
            ref="localVideo"
            autoplay
            playsinline
            muted
            class="w-full h-full object-cover absolute inset-0 rounded-none"
          ></video>
          <!-- 로컬 오버레이 캔버스 -->
          <canvas ref="overlayLocal" class="pointer-events-none absolute inset-0"></canvas>
        </div>

        <!-- 변경: 원격 구독 영상 컨테이너 각 비디오를 relative 래퍼로 감싸고, ref 콜백을 attachSubElWithAI로 교체 -->
        <div class="absolute inset-0 grid gap-0 p-0 z-0">
          <div
            v-for="sub in subscribers"
            :key="sub.stream.streamId"
            class="relative w-full h-full">
            <video
              :data-stream-id="sub.stream.streamId"
              autoplay
              playsinline
              muted
              class="w-full h-full object-cover absolute inset-0 rounded-none"
              :ref="el => attachSubElWithAI(sub, el)"
            ></video>
            <!-- 구독자 오버레이 캔버스는 JS에서 동적 생성해서 append -->
          </div>
        </div>

          <!-- 방 제목 -->
          <h2 class="text-xl text-white font-bold my-2 absolute left-3 top-3 z-10">
            방제목 {{ roomTitle }} — {{ userName }}
          </h2>

          <!-- 버튼 바 -->
          <div class="absolute left-1/2 bottom-6 -translate-x-1/2 flex gap-4 z-10">

            <!-- 캡쳐/다운로드 토글 버튼(하나) -->
            <button
              @click="onCaptureOrDownload"
              :title="hasUpscaled ? '업스케일된 이미지 다운로드' : '현재 프레임 캡쳐 & 업스케일'"
              :disabled="isUpscaling || isDownloading"
              class=" bg-gray-600 transition shadow rounded-full text-white p-4 disabled:opacity-50 disabled:cursor-not-allowed">
              <component :is="hasUpscaled ? Download : Camera"/>
            </button>
            <!-- 음성 버튼 -->
            <button
              v-if="isPublish"
              @click="toggleMic"
              class="text-white rounded-full p-4  shadow transition"
              :class="micEnabled ? 'bg-green-600' : 'bg-red-600' ">
              <component :is="micEnabled ? Mic : MicOff"/>
            </button>
            <!-- 녹화 버튼 -->
            <button
              v-if="isPublish"
              @click="toggleRecording"
              :disabled="isRecordingButtonDisabled"
              class="bg-opacity-70 text-white rounded-full p-4 hover:bg-red-600 shadow transition"
              :class="isRecording ? 'bg-gray-600' : 'bg-red-600 ' ">
              <component :is="isRecording ? Square : SquareStop" />
            </button>

            <!-- AI 탐지 on/off -->
          <button
            @click="toggleAI"
            :aria-pressed="aiOn"
            :title="aiOn ? 'AI 탐지 끄기' : 'AI 탐지 켜기'"
            class="bg-black bg-opacity-70 inline-flex justify-center items-center
                   rounded-full overflow-hidden w-14 hover:bg-gray-600 transition "
            :class="aiOn ? 'bg-black/70 hover:bg-gray-600 ring-1 ring-blue-400' : 'hover:text-sky-600'">
            <img :src="aiOn ? aiOnImg : aiOffImg"
              class="w-full h-full object-cover"/>
          </button>

          <!-- 채팅 버튼 -->
          <button
            class=" bg-yellow-400 transition shadow rounded-full text-yellow p-4"
            @click="showChat = !showChat">
            <MessageCircle class=""/>
          </button>


          </div>

          <!-- 스트리머 : 스트리밍 종료 버튼 / 기존의 endRoom 버튼을 스트리머 종료시도 자동종료도 포함되게 바꿨습니다.-->
          <button v-if="isPublish" @click="handleEndRoom"
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



          <!-- 캡처 버튼 -->
          <!-- <button
            @click="captureAndUpscale"
            :disabled="isUpscaling"
            class="absolute left-3 bottom-6 hover:bg-gray-600 transition shadow rounded-full text-white p-4">
            <ImageDown/>
          </button> -->

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
    <!-- 업스케일링 토스트 -->
    <Toast position="top-center" group="download" @close="visible = false">
      <template #container="{ message, closeCallback }">
        <section class="flex flex-col p-4 gap-4 w-full bg-primary/70 rounded-xl">
          <div class="flex items-center gap-4">
            <i class="pi pi-cloud-download text-white text-2xl"></i>
            <span class="font-bold text-base text-white">{{ message.summary }}</span>
          </div>
          <div class="flex flex-col gap-2">
            <ProgressBar :value="progress" :showValue="false" :style="{ height: '4px' }" class="!bg-primary/80" />
            <label class="text-sm font-bold text-white">{{ progress }}% 진행 중</label>
          </div>
          <div class="flex gap-3 justify-end text-white">
            <Button size="small" label="닫기" @click="closeCallback" />
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

</style>
