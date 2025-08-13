<template>
  <div
    v-if="archivedList.length > 0"
    class="badge-shelf"
    :class="{ 'clickable': editMode }"
    @click="handleOpen"
  >
    <div class="badge-shelf-row">
      <BadgeProfileIcon
        v-for="b in archivedList"
        :key="b.id"
        :badge="b"
      />
    </div>
  </div>

  <span
    v-else-if="editMode"
    class="badge-empty clickable"
    @click="handleOpen"
  >
    전시한 뱃지가 없습니다
  </span>

  <BadgeSelectModal
    v-if="showBadgeModal"
    :collected-badges="collectedList"
    :initial-selected-ids="archivedIds"
    @close="showBadgeModal = false"
    @save="handleSave"
  />
</template>

<script setup>
import { onMounted, computed, ref } from 'vue';
import { useProfileStore } from '@/stores/profile';
import { useBadgeStore } from '@/stores/badge';
import BadgeProfileIcon from './BadgeProfileIcon.vue';
import BadgeSelectModal from './BadgeSelectModal.vue';

const props = defineProps({
  memberId: {
    type: Number,
    required: true
  },
  editMode: {
    type: Boolean,
    default: false
  }
});

const profileStore = useProfileStore();
const badgeStore = useBadgeStore();

// 스토어에서 수집/전시 목록
const collectedList = computed(() => badgeStore.collectedBadges ?? []);
const archivedList = computed(() => profileStore.archivedBadges ?? []);
const archivedIds = computed(() => archivedList.value.map(b => b.id));

onMounted(async () => {
  if (props.memberId) {
    badgeStore.fetchCollectedBadges?.(props.memberId);
    profileStore.fetchArchivedBadges?.(props.memberId);
  }
});

const showBadgeModal = ref(false);

function handleOpen() {
  if (!props.editMode) return;
  showBadgeModal.value = true;
}

async function handleSave(selectedIds) {
  const ids = selectedIds.map(x => ({ id: typeof x === 'object' ? x.id : x }));
  await profileStore.updateArchivedBadges(ids);

  // UI 즉시 반영
  const idSet = new Set(ids.map(o => o.id));
  profileStore.archivedBadges = collectedList.value.filter(b => idSet.has(b.id));

  showBadgeModal.value = false;
}
</script>

<style scoped>
.badge-shelf {
  display: flex;
  align-items: center;
  gap: 8px; /* 뱃지 간 간격 */
}
.badge-shelf-row {
  display: flex;
  gap: 8px;
}
.badge-empty {
  display: inline-block;
  margin-left: 8px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}
.clickable {
  cursor: pointer;
}
</style>