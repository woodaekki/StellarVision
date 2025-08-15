<template>
  <teleport to="body">
    <transition name="fade">
      <div
        v-if="modelValue"
        class="nova-overlay"
        @click.self="close"
        @keydown.esc="close"
        role="dialog"
        aria-modal="true"
        aria-labelledby="nova-title"
      >
        <div class="white-jelly-panel" ref="panel" tabindex="-1">
          <!-- 헤더 -->
          <div class="nova-header">
            <div class="nova-title">
              <!-- 노바 아이콘 (심플한 꼬리별 SVG) -->
              <img 
                src="@/assets/pictures/stellabot/logo.png"
                class="nova-icon"
              />
              <h3 id="nova-title">고화질 미리보기</h3>
            </div>
            <div class="nova-actions">
              <button class="nova-btn ghost" @click="$emit('download')">다운로드</button>
              <button class="nova-btn" @click="close" aria-label="닫기">닫기</button>
            </div>
          </div>

          <!-- 콘텐츠 -->
          <div class="nova-body">
            <img
              v-if="src"
              :src="src"
              alt="업스케일 미리보기"
              class="nova-image"
              @load="$emit('loaded')"
            />
            <div v-else class="nova-empty">미리볼 이미지가 없습니다.</div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false }, // v-model
  src: { type: String, default: '' }             // blob:… 혹은 http…
})
const emit = defineEmits(['update:modelValue', 'download', 'loaded', 'closed'])

const panel = ref(null)
function close () {
  emit('update:modelValue', false)
  emit('closed')
}

onMounted(() => {
  // 열릴 때 포커스 이동
  watch(() => props.modelValue, (open) => {
    if (open) requestAnimationFrame(() => panel.value?.focus())
  }, { immediate: true })
})
</script>

<style scoped>
.nova-overlay {
  position: fixed;       /* ✅ 뷰포트 기준 고정 */
  inset: 0;              /* top/right/bottom/left: 0 */
  z-index: 9999;         /* ✅ 무엇보다 위로 */
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,.55); /* 배경 딤 */
}

.nova-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.nova-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nova-title h3 {
  margin: 0;
  font-size: 18px;
  line-height: 1;
  font-weight: 800;
  color: #f7f7f7;
  letter-spacing: .02em;
  text-shadow: 0 1px 0 rgba(0,0,0,.25);
}

.nova-header .nova-title > img.nova-icon {
  width: 52px !important;
  height: 52px !important;
  flex: 0 0 auto;
  display: inline-block;
  object-fit: contain;
  vertical-align: middle;
  filter: drop-shadow(0 0 4px rgba(80,130,255,.55));
}

.nova-btn {
  padding: 8px 14px;
  border-radius: 12px;
  
  background: rgba(255, 255, 255, 0.08);
  border: 1.2px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);


  box-shadow:
    inset 2px 2px 4px rgba(255, 255, 255, 0.5),
    inset -2px -2px 4px rgba(0, 0, 0, 0.1);

  color: #f3f3f3;
  font-weight: 700;
  letter-spacing: .02em;
  transition: box-shadow 0.25s ease, background 0.25s ease, border-color 0.25s ease;
  cursor: pointer;
}

.nova-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  box-shadow:
    inset 3px 3px 6px rgba(255, 255, 255, 0.6),
    inset -3px -3px 6px rgba(0, 0, 0, 0.12);
}

.nova-btn:active {
  transform: translateY(1px);
  box-shadow:
    inset 1px 1px 2px rgba(255, 255, 255, 0.4),
    inset -1px -1px 2px rgba(0, 0, 0, 0.08);
}

.nova-btn.ghost {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.18);
}


@media (max-width: 640px) {
  .nova-title h3 { font-size: 16px; }
  .nova-header .nova-title > img.nova-icon { width: 26px !important; height: 26px !important; }
}

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

  color: rgba(30, 30, 30, 0.7);

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

