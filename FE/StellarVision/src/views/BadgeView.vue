<template>
  <div class="badge-header">
    <h2>Badge Dex</h2>
    <hr />
  </div>

  <div class="badge-wrapper">
    <div class="badge-collection-info">
      <p>획득한 뱃지: {{ collectedCount }}/{{ badgeList.length }}</p>
    </div>

    <div class="badge-container" ref="containerRef">
      <Badge
        v-for="badge in badgeList"
        :key="badge.name"
        :badge="badge"
        @click="(event) => handleBadgeClick(event, badge)"
      />
    </div>

    <div
      v-if="selectedBadge"
      :class="['tooltip', tooltipPosition, { locked: !selectedBadge.collected }]"
      :style="{ top: tooltipY + 'px', left: tooltipX + 'px' }"
    >
      <p class="badge-name">
        {{ selectedBadge.collected ? selectedBadge.koreanName : '???' }}
      </p>
      <p class="badge-description">
        {{ selectedBadge.collected ? selectedBadge.koreanDescription
            : '아직 만나지 못한 별자리입니다. 미래에 만날 수 있을까요?' }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Badge from '@/components/badge/Badge.vue';
import badgeData from '@/data/badgeData.json';
import { useAccountStore } from '@/stores/account'
import { useBadgeStore } from '@/stores/badge';
import axios from 'axios';


const accountStore = useAccountStore();
const badgeStore = useBadgeStore();
// 현재 전체 뱃지 조회는 자기 것만 되는 걸 상정하기 때문에 myProfile에서 가져옴
const memberId = computed(() => accountStore.myProfile?.memberId);

const data = ref([]);

// .json과 api에서 받아온 정보를 합침
const badgeList = ref([]);

const collectedCount = computed(() => {
  return badgeList.value.filter(badge => badge.collected).length;
});

function updateBadgeList() {
  if (!data.value || !data.value.collections) {
    console.warn('컬렉션 데이터 없음');
    return;
  }

  console.log('data.value.collections:', data.value.collections);

  badgeList.value = badgeData.map((badge) => {
    const matchedApiBadge = data.value.collections.find(
      (apiBadge) => apiBadge.id === badge.id , console.log("id",badge.id , badge)
    );

    return {
      ...badge,
      collected: matchedApiBadge ? matchedApiBadge.collected : false
    };
  });

  console.log('badgeList 완성:', badgeList.value);
}

onMounted(async () => {
  const token = localStorage.getItem('jwt');
  const response = await axios.get('https://i13c106.p.ssafy.io/api/collections', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  console.log(response);
  data.value = response.data.data || {};


  await accountStore.fetchMyProfile()
  if (memberId.value) {
    await badgeStore.fetchCollectedBadges(memberId.value)
  }
  updateBadgeList();

})

// 각 뱃지 클릭 시 말풍선이 뜨는 위치 계산 로직
const selectedBadge = ref(null);
const tooltipX = ref(0);
const tooltipY = ref(0);
const tooltipPosition = ref('right');
const containerRef = ref(null);

function handleBadgeClick(event, badge) {
  selectedBadge.value = badge;

  const targetRect = event.currentTarget.getBoundingClientRect();
  const wrapperRect = containerRef.value.getBoundingClientRect();

  const clickX = targetRect.left + targetRect.width / 2 - wrapperRect.left;
  const clickY = targetRect.top - wrapperRect.top + window.scrollY;

  const wrapperMid = wrapperRect.width / 2;

  if (clickX > wrapperMid) {
    tooltipX.value = clickX - 160;
    tooltipPosition.value = 'left';
  } else {
    tooltipX.value = clickX + 240;
    tooltipPosition.value = 'right';
  }

  tooltipY.value = clickY + 120;
}
</script>

<style scoped>
/* 페이지 제목 */
.badge-header {
  padding-left: 1rem;
  padding-right: 1rem;
  padding-top: 5rem;
  padding-bottom: 1.5rem;
}

.badge-header h2 {
  font-family: 'Pretendard', sans-serif !important;
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.badge-header hr {
  border-top-width: 2px; 
  border-color: rgb(229, 229, 229);
  width: 100%;
  margin-top: 0.5rem;  
}

/* 뱃지 보유 현황 */
.badge-collection-info {
  text-align: right;
  align-self: flex-end;
  margin-right: 10vw;
  margin-bottom: 1rem;
  color: white;
  font-family: 'Pretendard', sans-serif;
  font-size: 1rem;
}

/* 뱃지 목록 */
.badge-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  padding: 3rem;
  padding-top: 0;
  overflow-x: hidden;
  position: relative;
}

.badge-wrapper {
  background: black;
  padding-top: 58px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center; 
}

/* 클릭시 나타나는 말풍선 */
.tooltip {
  position: absolute;
  max-width: 260px;
  background: rgba(255,255,255,.92);
  border: 1px solid rgba(0,0,0,.12);
  border-radius: 12px;
  padding: 0.9rem 1rem;
  box-shadow: 0 10px 24px rgba(0,0,0,.25);
  z-index: 10;
  white-space: normal;
  backdrop-filter: blur(6px);     /* 유리 느낌 (지원 브라우저에서만) */
}

.tooltip.right::before,
.tooltip.left::before,
.tooltip.right::after,
.tooltip.left::after {
  content: '';
  position: absolute;
  top: 18px;
  width: 0; height: 0;
  border: 8px solid transparent;
}
.tooltip.right::before {
  left: -17px;
  border-right-color: rgba(0,0,0,.12);
}
.tooltip.right::after {
  left: -16px;
  border-right-color: rgba(255,255,255,.92);
}
.tooltip.left::before {
  right: -17px;
  border-left-color: rgba(0,0,0,.12);
}
.tooltip.left::after {
  right: -16px;
  border-left-color: rgba(255,255,255,.92);
}

.badge-name {
  margin: 0 0 .35rem;
  font-weight: 800;
  font-size: 1.05rem;
  letter-spacing: .015em;
  color: #0f172a;
  line-height: 1.2;
}

.tooltip:not(.locked) .badge-name {
  text-shadow: 0 1px 0 rgba(255,255,255,.6);
}

.tooltip.locked .badge-name {
  color: #6b7280;
  font-style: italic;
}

.badge-description {
  margin: 0;
  font-size: .9rem;
  line-height: 1.45;
  color: #374151;
}

.tooltip.locked .badge-description {
  color: #9ca3af;
}
</style>
