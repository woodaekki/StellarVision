<template>
  <div
    class="fixed inset-0 z-[60] grid place-items-center bg-black/40 backdrop-blur-sm"
    role="status"
    aria-live="polite"
    aria-label="로딩 중"
  >
    <div class="flex flex-col items-center gap-3">
      <!-- Tailwind 기본 회전 스피너 -->
      <div class="h-10 w-10 rounded-full border-4 border-white/30 border-t-white animate-spin" />
      <p class="text-sm text-zinc-200">로딩 중…</p>
    </div>
  </div>
</template>

<script setup>

</script>