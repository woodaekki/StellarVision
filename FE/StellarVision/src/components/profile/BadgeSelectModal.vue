<template>
  <div class="modal-backdrop" @click.self="emit('close')">
    <div class="modal">
      <h3 class="modal-title">전시할 뱃지 선택 (최대 3개)</h3>

      <div class="grid">
        <button
          v-for="badge in collectedBadges"
         :key="badgeIdOf(badge)"
          class="cell"
          :class="{ selected: selectedSet.has(badgeIdOf(badge)), locked: isLocked && !selectedSet.has(badgeIdOf(badge)) }"
         @click="toggle(badgeIdOf(badge))"
        >
          <BadgeProfileIcon :badge="badge" />
          <span class="cell-name">{{ badge.koreanName }}</span>
        </button>
      </div>

      <div class="modal-footer">
        <span class="count">{{ selectedSet.size }} / 3</span>
        <div class="spacer" />
        <button class="btn ghost" @click="emit('close')">취소</button>
        <button class="btn primary" :disabled="selectedSet.size === 0" @click="commit">완료</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import BadgeProfileIcon from './BadgeProfileIcon.vue';

const props = defineProps({
  collectedBadges: {
    type: Array,
    default: () => []
  },
  initialSelectedIds: {
    type: Array,
    default: () => []
  }
});
const emit = defineEmits(['close', 'save']);

const selectedSet = ref(new Set(props.initialSelectedIds));
watch(() => props.initialSelectedIds, ids => { selectedSet.value = new Set(ids) }, { immediate: true });

const isLocked = computed(() => selectedSet.value.size >= 3);

const badgeIdOf = (item) => item?.badge?.id ?? item?.badgeId ?? item?.id;

function toggle(id) {
  if (selectedSet.value.has(id)) {
    selectedSet.value.delete(id);
  } else {
    if (selectedSet.value.size >= 3) return; // 최대 3개 제한
    selectedSet.value.add(id);
  }
}

function commit() {
  emit('save', Array.from(selectedSet.value));
}

console.log(props.collectedBadges);
</script>

<style scoped>
.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.55);
  display: flex; align-items: center; justify-content: center;
  z-index: 999;
}

.modal {
  width: min(720px, 92vw);
  background: #FAFAFA;
  color: #111827;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px 16px 12px;
  display: flex;
  flex-direction: column;
  max-height: 80vh;
}

.modal-title {
  margin: 0 0 12px;
  font-size: 16px; font-weight: 700;
  color: #111827;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  flex: 1;
  overflow: auto;
}

.cell {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 10px 8px;
  background: #eef0f1;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: transform .1s ease, border-color .1s ease, background .1s ease;
  color: #111827;
}
.cell:hover {
  transform: translateY(-1px);
  background: #f3f4f6;
}
.cell.selected {
  border-color: #5071b8;
  background: #d0d8f3;
  color: #051d52;
}
.cell.locked { opacity: 0.55; cursor: not-allowed; }
.cell-name { font-size: 12px; color: #6b7280; text-align: center; line-height: 1.3; }

.modal-footer {
  display: flex; align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.08); /* 구분선 */
}

.count { font-size: 12px; color: #6b7280; }
.spacer { flex: 1; }

.btn {
  border: 1px solid #e5e7eb;
  background: #FAFAFA;
  color: #111827;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-left: 8px;
  transition: background-color .15s ease, color .15s ease, border-color .15s ease;
}
.btn.ghost:hover {
  background: #f3f4f6;
}
.btn.primary {
  border-color: #000000;
  background: #000000;
  color: #ffffff;
}
.btn.primary:disabled {
  opacity: .5; cursor: not-allowed;
}
</style>

