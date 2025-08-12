<!-- MainSidebar.vue (Tailwind 적용) -->
<template>
  <!-- 사이드바 토글 버튼 - 우측 중앙에 반구 형태 -->
  <button
    class="fixed right-0 top-1/2 z-40 -translate-y-1/2 w-12 h-20
           bg-slate-900/90 backdrop-blur-md border border-white/10 border-r-0
           rounded-l-3xl flex items-center justify-center pl-2 text-slate-200
           transition-all duration-300 ease-out cursor-pointer
           hover:bg-slate-900/95 hover:-translate-x-1 hover:-translate-y-1/2
           shadow-[-4px_0_20px_rgba(0,0,0,0.3)] hover:shadow-[-8px_0_30px_rgba(0,0,0,0.4)]"
    :class="isOpen ? 'bg-blue-500/90 -translate-x-2' : ''"
    @click="toggleSidebar"
    aria-label="스트리밍 목록 보기"
  >
    <div class="flex items-center justify-center transition-transform duration-300">
      <svg
        v-if="!isOpen"
        viewBox="0 0 24 24"
        class="w-5 h-5"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <path d="M15 6l-6 6 6 6"/>
      </svg>
      <svg
        v-else
        viewBox="0 0 24 24"
        class="w-5 h-5"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <path d="M9 18l6-6-6-6"/>
      </svg>
    </div>
  </button>

  <!-- 사이드바 배경 오버레이 -->
  <div
    v-if="isOpen"
    class="fixed inset-0  mix-blend-multiply backdrop-blur-2xl z-45 opacity-8 animate-[fadeIn_0.3s_ease_forwards]"
    @click="closeSidebar"
  ></div>
<!-- bg-black/80  bg-slate-900/95 -->
  <!-- 사이드바 -->
  <nav
    class="fixed top-0 right-0 w-[400px] max-w-[90vw] h-screen z-50
            bg-slate-900/55 backdrop-blur-2xl border-l border-white/10
           flex flex-col transition-transform duration-[400ms] ease-[cubic-bezier(0.4,0,0.2,1)]
           shadow-[-10px_0_50px_rgba(0,0,0,0.5)] overscroll-contain"
    :class="isOpen ? 'translate-x-0' : 'translate-x-full'"
    @wheel="handleSidebarWheel"
    @touchmove="handleSidebarTouch"
  >
    <!-- 사이드바 헤더 -->
    <div class="flex items-center justify-between px-6 py-6 pb-4 border-b border-white/10 flex-shrink-0">
      <h2 class="text-xl font-semibold text-slate-50 m-0">스트리밍 목록</h2>
      <button
        class="flex items-center justify-center w-8 h-8 rounded-lg bg-transparent
               border border-white/10 text-slate-400 cursor-pointer transition-all duration-200
               hover:bg-white/10 hover:text-slate-50"
        @click="closeSidebar"
        aria-label="닫기"
      >
        <svg viewBox="0 0 24 24" class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>
    </div>

    <!-- 스트리밍 리스트 컨테이너 -->
    <div class="flex-1 overflow-hidden flex flex-col">
      <StreamingListView :is-sidebar="true" />
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import StreamingListView from '@/views/Streaming/StreamingListView.vue';

const isOpen = ref(false);

const toggleSidebar = () => {
  isOpen.value = !isOpen.value;
};

const closeSidebar = () => {
  isOpen.value = false;
};

// 사이드바 내 스크롤과 지구본 줌 간섭 방지
const handleSidebarWheel = (e) => {
  e.stopPropagation();
};

const handleSidebarTouch = (e) => {
  e.stopPropagation();
};

// ESC 키로 사이드바 닫기
const handleKeydown = (e) => {
  if (e.key === 'Escape' && isOpen.value) {
    closeSidebar();
  }
};

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown);
});

defineExpose({ isOpen, toggleSidebar, closeSidebar });
</script>

<style scoped>
@keyframes fadeIn {
  to { opacity: 1; }
}

/* 모바일 대응 */
@media (max-width: 768px) {
  .sidebar-mobile {
    width: 100vw;
    max-width: none;
  }

  .toggle-mobile {
    width: 2.5rem;
    height: 3.75rem;
  }
}
</style>



