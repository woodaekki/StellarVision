<!-- src/components/streaming/ConstellationListPanel.vue -->
<template>
  <section
    class="white-jelly-panel text-white/90 w-[min(92vw,380px)] max-h-[70vh] overflow-auto"
    role="list"
    aria-label="탐지된 별자리 목록"
  >
    <!-- 헤더 -->
    <header class="flex items-center mb-3">
    <div class="flex items-center gap-2">
        <img
        src="@/assets/pictures/stellabot/logo.png"
        class="w-6 h-6 object-contain drop-shadow-[0_0_6px_rgba(80,130,255,.6)]"
        />
        <h3 class="text-lg font-extrabold tracking-tight">노바가 발견했어요!</h3>
    </div>
    </header>

    <!-- 비어있을 때 -->
    <div v-if="!items?.length" class="text-white/70 text-sm py-4">
      아직 인식된 별자리가 없어요.
    </div>

    <!-- 목록 -->
    <ul v-else class="space-y-1" role="presentation">
      <li v-for="it in items" :key="it.code">
        <button
          class="w-full flex items-center justify-between gap-3 px-3 py-2 rounded-xl
                 hover:bg-white/10 transition text-left"
          :class="{'bg-white/15 ring-1 ring-white/20': selectedCode === it.code}"
          @click="$emit('select', it.code)"
          :aria-pressed="selectedCode === it.code ? 'true' : 'false'"
        >
          <div class="flex items-center gap-2 min-w-0">
            <span class="text-xl">{{ emojiFor(it.code) }}</span>
            <div class="truncate">
              <span class="font-semibold">{{ displayName(it) }}</span>
              <span v-if="showCode" class="text-white/60 text-xs ml-2">({{ it.code }})</span>
            </div>
          </div>
        </button>
      </li>
    </ul>
  </section>
</template>

<script setup>
const props = defineProps({
  /**
   * items: [{ code:'Ori', nameKo:'오리온자리', nameEn:'Orion', count:3, confPct:92 }, ...]
   * 최소 요구: { code }.
   */
  items: { type: Array, default: () => [] },
  selectedCode: { type: String, default: null },
  showCode: { type: Boolean, default: true },
  showClear: { type: Boolean, default: true },
})

defineEmits(['select'])

/** 이름 표시 우선순위: nameKo > name > nameEn > code */
function displayName(it) {
  return it.nameKo || it.name || it.nameEn || it.code
}

/** 퍼센트 표기 보조: confPct(0~100) 또는 confidence(0~1) */
function toPct(it) {
  if (typeof it.confPct === 'number') return `${Math.round(it.confPct)}%`
  if (typeof it.confidence === 'number') return `${Math.round(it.confidence * 100)}%`
  return ''
}

/** 별자리 이모지 매핑 (필요시 확장 가능) */
const EMOJI_MAP = {
  // 26종 기본 매핑
  Lyr: '🎶', Aql: '🦅', Cyg: '🦢', Sge: '🏹', Sco: '🦂',
  Oph: '🐍', Dra: '🐉', Her: '💪', Vul: '🦊', Sct: '🛡️',
  Del: '🐬', Ser: '🐍', Sgr: '🏹', UMa: '🐻', UMi: '🐻‍❄️',
  Cas: '👑', Cep: '👑', Cam: '🦒', Aur: '🛞', Tau: '🐂',
  Ori: '🗡️', Eri: '🌊', Lep: '🐇', Mon: '🦄', Gem: '👯',
  Per: '⚔️',
}
function emojiFor(code) {
  return EMOJI_MAP[code] || '⭐'
}
</script>

<style scoped>
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
