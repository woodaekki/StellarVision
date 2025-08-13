<!-- src/components/room/ViewerRoomComponent.vue -->
<script setup>
import { defineProps } from 'vue'
import { DoorOpen, Camera, Download, MessageCircle } from 'lucide-vue-next'

const props = defineProps({
  // 영상/구독 관련
  subscribers: { type: Array, required: true },
  attachSubElWithAI: { type: Function, required: true },

  // 방 정보
  roomTitle: { type: String, default: '' },
  userName: { type: String, default: 'Viewer' },

  // 로컬 비디오/오버레이 전달용 ref
  setLocalVideoEl: { type: Function, required: true },
  setOverlayEl: { type: Function, required: true },

  // 캡쳐/업스케일 관련
  onCaptureOrDownload: { type: Function, required: true },
  hasUpscaled: { type: Boolean, required: true },
  isUpscaling: { type: Boolean, required: true },
  isDownloading: { type: Boolean, required: true },

  // AI 토글
  aiOn: { type: Boolean, required: true },
  toggleAI: { type: Function, required: true },
  aiOnImg: { type: String, required: true },
  aiOffImg: { type: String, required: true },

  // 채팅 토글
  showChat: { type: Boolean, required: true },
  onToggleChat: { type: Function, required: true },

  // 나가기
  leave: { type: Function, required: true },
})
</script>

<template>
  <div class="relative bg-black transition-all duration-300 w-full h-full rounded-none">
    <!-- 로컬 비디오 + 오버레이 (시청자 입장에선 원격 영상일 수도 있음) -->
    <div class="relative w-full h-full">
      <video
        :ref="(el) => setLocalVideoEl && setLocalVideoEl(el)"
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
      <!-- 캡쳐/다운로드 버튼 -->
      <button
        @click="onCaptureOrDownload"
        :title="hasUpscaled ? '업스케일된 이미지 다운로드' : '현재 프레임 캡쳐 & 업스케일'"
        :disabled="isUpscaling || isDownloading"
        class="bg-gray-600 transition shadow rounded-full text-white p-4 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <component :is="hasUpscaled ? Download : Camera" />
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

    <!-- 시청자: 나가기 버튼 -->
    <button
      @click="leave"
      class="absolute right-3 top-3 z-10 bg-black bg-opacity-70
             text-white rounded-full px-3 py-1 hover:bg-red-600 transition"
    >
      <DoorOpen />
    </button>
  </div>
</template>
