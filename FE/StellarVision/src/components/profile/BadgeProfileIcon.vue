<template>
  <div class="icon-card">
    <div class="icon">
      {{ iconToShow }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import badgeProfileIconData from '@/data/badgeProfileIconData.json';

const props = defineProps({
  badge: {
    type: Object,
    required: true
}
});

const iconToShow = computed(() => {
  const passed = props.badge?.icon
  if (passed && String(passed).trim() !== '') return passed

  const id = props.badge?.id
  const found = badgeProfileIconData.find(b => String(b.id) === String(id))
  return found?.icon ?? '🔭'
})
</script>

<style scoped>
.icon-card {
  width: 100%;
  display: flex; align-items: center; justify-content: center;
}
.icon {
  font-size: 24px; line-height: 1;
  display: inline-flex; align-items: center; justify-content: center;
  width: 40px; height: 40px;
}
.icon img { max-width: 100%; max-height: 100%; object-fit: contain; }
</style>
