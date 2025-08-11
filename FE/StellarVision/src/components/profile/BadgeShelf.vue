<template>
  <div
    v-if="archivedList.length > 0"
    class="badge-shelf"
    :class="{ clickable: editMode }"
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
const archivedList  = computed(() => profileStore.archivedBadges ?? []);
const archivedIds   = computed(() => archivedList.value.map(b => b.id));

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
  display: inline-flex;
  align-items: center;
}
.badge-shelf-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)); /* 한 줄 3개는 모달에서, 여기선 자유 */
  gap: 1px;
}
.badge-empty {
  display: inline-block;
  margin-left: 8px;
  font-size: 12px;
  color: #9aa0a6;
}
.clickable { cursor: pointer; }
</style>
