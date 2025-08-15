<!-- src/components/room/StreamerRoomComponent.vue -->
<script setup>
import { defineProps } from 'vue'
import { DoorOpen, Camera, Download, MessageCircle, Mic, MicOff, Square, SquareStop, ScreenShare, ScreenShareOff } from 'lucide-vue-next'

const props = defineProps({
  // 영상/구독 관련
  subscribers: { type: Array, required: true },
  attachSubElWithAI: { type: Function, required: true },

  // 방 정보
  roomTitle: { type: String, default: '' },
  userName: { type: String, default: 'Host' },

  // 퍼블리셔 엘리먼트/오버레이 전달용 ref 콜백
  setPublisherEl: { type: Function, required: true },
  setOverlayEl: { type: Function, required: true },

  // 캡쳐/업스케일 관련
  onCaptureOrDownload: { type: Function, required: true },
  hasUpscaled: { type: Boolean, required: true },
  isUpscaling: { type: Boolean, required: true },
  isDownloading: { type: Boolean, required: true },

  // 마이크 토글
  micEnabled: { type: Boolean, required: true },
  toggleMic: { type: Function, required: true },

  // 녹화 토글
  isRecording: { type: Boolean, required: true },
  isRecordingButtonDisabled: { type: Boolean, required: true },
  toggleRecording: { type: Function, required: true },

  // AI 토글
  aiOn: { type: Boolean, required: true },
  toggleAI: { type: Function, required: true },
  aiOnImg: { type: String, required: true },
  aiOffImg: { type: String, required: true },

  // 채팅 토글
  showChat: { type: Boolean, required: true },
  onToggleChat: { type: Function, required: true },

  // 종료/나가기
  handleEndRoom: { type: Function, required: true },

  // ===== 화면공유 추가 =====
  isScreenSharing: { type: Boolean, required: true },
  toggleScreenShare: { type: Function, required: true },
  // 빠른 연타 방지(선택): 부모에서 screenShare.isBusy.value 내려주면 버튼 비활성화됨
  isScreenBusy: { type: Boolean, default: false },
})

</script>

<template>
  <div class="relative bg-black transition-all duration-300 w-full h-full rounded-none">
    <!-- 로컬 프리뷰 + 오버레이 -->
    <div class="relative w-full h-full">
      <video
        :ref="(el) => setPublisherEl && setPublisherEl(el)"
        autoplay
        playsinline
        muted
        class="w-full h-full object-cover absolute inset-0 rounded-none"
      />
      <canvas
        :ref="(el) => setOverlayEl && setOverlayEl(el)"
        class="pointer-events-none absolute inset-0"
      />
    </div>

    <!-- 원격 구독 비디오 -->
    <div class="absolute inset-0 grid gap-0 p-0 z-0">
      <div
        v-for="sub in subscribers"
        :key="sub.stream.streamId"
        class="relative w-full h-full"
      >
        <video
          :data-stream-id="sub.stream.streamId"
          autoplay
          playsinline
          muted
          class="w-full h-full object-cover absolute inset-0 rounded-none"
          :ref="el => attachSubElWithAI(sub, el)"
        />
      </div>
    </div>

    <!-- 방 제목 -->
    <h2 class="text-xl text-white font-bold my-2 absolute left-3 top-3 z-10">
      방제목 {{ roomTitle }} — {{ userName }}
    </h2>

    <!-- 버튼 바 -->
    <div class="absolute left-1/2 bottom-6 -translate-x-1/2 flex gap-4 z-10">
      <!-- 화면공유 -->
      <button
        @click="toggleScreenShare"
        :disabled="isScreenBusy"
        :title="isScreenSharing ? '화면공유 중지' : '화면공유 시작'"
        class="text-white rounded-full p-4 disabled:opacity-50"
        :class="isScreenSharing ? 'bg-emerald-600' : 'bg-slate-700'"
      >
        <component :is="isScreenSharing ? ScreenShareOff : ScreenShare" />
      </button>


      <!-- 캡쳐/다운로드 토글 버튼(하나) -->
      <button
        @click="onCaptureOrDownload"
        :title="hasUpscaled ? '업스케일된 이미지 다운로드' : '현재 프레임 캡쳐 & 업스케일'"
        :disabled="isUpscaling || isDownloading"
        class="bg-gray-600 transition shadow rounded-full text-white p-4 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <component :is="hasUpscaled ? Download : Camera" />
      </button>

      <!-- 음성 버튼 -->
      <button
        @click="toggleMic"
        class="text-white rounded-full p-4 shadow transition"
        :class="micEnabled ? 'bg-green-600' : 'bg-red-600'"
      >
        <component :is="micEnabled ? Mic : MicOff" />
      </button>

      <!-- 녹화 버튼 -->
      <button
        @click="toggleRecording"
        :disabled="isRecordingButtonDisabled"
        class="bg-opacity-70 text-white rounded-full p-4 hover:bg-red-600 shadow transition"
        :class="isRecording ? 'bg-gray-600' : 'bg-red-600'"
      >
        <component :is="isRecording ? Square : SquareStop" />
      </button>

      <!-- AI 탐지 on/off -->
      <button
        @click="toggleAI"
        :aria-pressed="aiOn"
        :title="aiOn ? 'AI 탐지 끄기' : 'AI 탐지 켜기'"
        class="bg-black bg-opacity-70 inline-flex justify-center items-center
               rounded-full overflow-hidden w-14 hover:bg-gray-600 transition"
        :class="aiOn ? 'bg-black/70 hover:bg-gray-600 ring-1 ring-blue-400' : 'hover:text-sky-600'">
        <img :src="aiOn ? aiOnImg : aiOffImg" class="w-full h-full object-cover" />
      </button>

      <!-- 채팅 버튼 -->
      <button
        class="bg-yellow-400 transition shadow rounded-full text-yellow p-4"
        @click="onToggleChat()"
      >
        <MessageCircle />
      </button>
    </div>

    <!-- 스트리머 : 스트리밍 종료 버튼 -->
    <button
      @click="handleEndRoom"
      class="absolute right-3 top-3 z-10 bg-black bg-opacity-70
             text-white rounded-full px-3 py-1 hover:bg-red-600 transition"
    >
      <DoorOpen />
    </button>
  </div>
</template>
