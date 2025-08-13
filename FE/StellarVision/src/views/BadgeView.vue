<template>
  <div class="badge-header">
    <h2>Badge Dex</h2>
    <hr />
  </div>

  <!-- ✅ tooltip이 absolute로 붙는 기준 요소 -->
  <div class="badge-wrapper" ref="wrapperRef">
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

    <!-- ✅ tooltip은 wrapperRef 기준으로 배치됨 -->
    <div
      v-if="selectedBadge"
      ref="tooltipRef"
      :class="['tooltip', tooltipPosition, { locked: !selectedBadge.collected }]"
      :style="{ top: tooltipY + 'px', left: tooltipX + 'px' }"
    >
      <p class="badge-name">
        {{ selectedBadge.collected ? selectedBadge.koreanName : '???' }}
      </p>
      <p class="badge-description">
        {{
          selectedBadge.collected
            ? selectedBadge.koreanDescription
            : '아직 만나지 못한 별자리입니다.\n미래에 만날 수 있을까요?'
        }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import Badge from '@/components/badge/Badge.vue'
import badgeData from '@/data/badgeData.json'
import { useAccountStore } from '@/stores/account'
import { useBadgeStore } from '@/stores/badge'
import axios from 'axios'

const accountStore = useAccountStore()
const badgeStore = useBadgeStore()
const memberId = computed(() => accountStore.myProfile?.memberId)

const data = ref([])
const badgeList = ref([])

const collectedCount = computed(() =>
  badgeList.value.filter(b => b.collected).length
)

function updateBadgeList() {
  if (!data.value || !data.value.collections) return
  badgeList.value = badgeData.map(badge => {
    const hit = data.value.collections.find(x => x.id === badge.id)
    return { ...badge, collected: !!hit?.collected }
  })
}

onMounted(async () => {
  const token = localStorage.getItem('jwt')
  const response = await axios.get('https://i13c106.p.ssafy.io/api/collections', {
    headers: { Authorization: `Bearer ${token}` }
  })
  data.value = response.data.data || {}

  await accountStore.fetchMyProfile()
  if (memberId.value) await badgeStore.fetchCollectedBadges(memberId.value)
  updateBadgeList()
})

/* ── Tooltip 위치 계산 ─────────────────────────────────────────── */
const selectedBadge = ref(null)
const tooltipX = ref(0)
const tooltipY = ref(0)
const tooltipPosition = ref('right')

const wrapperRef = ref(null)    // ✅ tooltip의 좌표 기준
const containerRef = ref(null)  // 배지 그리드(컬럼 수 추출용)
const tooltipRef = ref(null)

const clamp = (min, v, max) => Math.max(min, Math.min(v, max))

async function handleBadgeClick(event, badge) {
  selectedBadge.value = badge

  const targetRect = event.currentTarget.getBoundingClientRect()
  const hostRect   = wrapperRef.value.getBoundingClientRect()   // ✅ 기준 통일

  // 배지 중심 (wrapper 기준)
  const centerX = targetRect.left + targetRect.width / 2 - hostRect.left
  const baseTop = targetRect.top - hostRect.top                 // ✅ scrollY 더하지 않음

  // 1rem(px)
  const rem = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16

  // 툴팁 렌더 후 크기 측정
  await nextTick()
  const tipEl = tooltipRef.value
  const tipW = tipEl?.offsetWidth  ?? 260
  const tipH = tipEl?.offsetHeight ?? 160

  // 반응형 간격
  const gapX = clamp(0.75 * rem, window.innerWidth * 0.03, 2.0 * rem) // ≈12~32px
  const gapY = clamp(1.0  * rem, window.innerHeight * 0.12, 10  * rem)

  // ✅ 컬럼 기반 기본 방향: 앞 절반 right, 뒤 절반 left
  let colCount = 4
  try {
    const cols = getComputedStyle(containerRef.value).gridTemplateColumns
    const parts = cols.split(' ').filter(Boolean)
    if (parts.length) colCount = parts.length
  } catch {}
  const ratio = centerX / hostRect.width
  const colIndex = clamp(0, Math.floor(ratio * colCount), colCount - 1)
  let side = colIndex < colCount / 2 ? 'right' : 'left'

  // 초기 x (wrapper 기준)
  let x
  if (side === 'right') {
    x = (targetRect.right - hostRect.left) + gapX
  } else {
    x = (targetRect.left  - hostRect.left) - tipW - gapX
  }

  // 경계 체크 → 넘치면 뒤집기
  if (side === 'right' && x + tipW > hostRect.width) {
    side = 'left'
    x = (targetRect.left - hostRect.left) - tipW - gapX
  } else if (side === 'left' && x < 0) {
    side = 'right'
    x = (targetRect.right - hostRect.left) + gapX
  }

  // 세로는 배지 기준 아래로 gapY, 경계 클램프
  let y = baseTop + gapY
  x = clamp(0, x, hostRect.width  - tipW)
  y = clamp(0, y, hostRect.height - tipH)

  tooltipX.value = x
  tooltipY.value = y
  tooltipPosition.value = side
}
</script>

<style scoped>
/* 페이지 제목 */
.badge-header { padding: 5rem 1rem 1.5rem; }
.badge-header h2 {
  font-family: 'Pretendard', sans-serif !important;
  font-size: 1.5rem; margin-bottom: .5rem;
}
.badge-header hr {
  border-top-width: 2px; border-color: rgb(229, 229, 229);
  width: 100%; margin-top: .5rem;
}

/* 뱃지 보유 현황 */
.badge-collection-info {
  text-align: right; align-self: flex-end;
  margin-right: 10vw; margin-bottom: 1rem;
  color: white; font-family: 'Pretendard', sans-serif; font-size: 1rem;
}

/* 컨테이너/래퍼 */
.badge-wrapper {
  background: black; padding-top: 58px; position: relative; /* ✅ tooltip 기준 */
  display: flex; flex-direction: column; justify-content: center; align-items: center;
}
.badge-container {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 1rem; padding: 3rem; padding-top: 0;
  overflow-x: hidden; position: relative;
}

/* 클릭시 나타나는 말풍선 */
.tooltip {
  position: absolute; max-width: 320px;
  background: rgba(255,255,255,.92);
  border: 1px solid rgba(0,0,0,.12);
  border-radius: 12px; padding: .9rem 1rem;
  box-shadow: 0 10px 24px rgba(0,0,0,.25);
  z-index: 10; backdrop-filter: blur(6px);
  white-space: normal;
}
.tooltip.right::before,
.tooltip.left::before,
.tooltip.right::after,
.tooltip.left::after {
  content: ''; position: absolute; top: 18px; width: 0; height: 0; border: 8px solid transparent;
}
.tooltip.right::before { left: -17px; border-right-color: rgba(0,0,0,.12); }
.tooltip.right::after  { left: -16px; border-right-color: rgba(255,255,255,.92); }
.tooltip.left::before  { right: -17px; border-left-color: rgba(0,0,0,.12); }
.tooltip.left::after   { right: -16px; border-left-color: rgba(255,255,255,.92); }

.badge-name { margin: 0 0 .35rem; font-weight: 800; font-size: 1.05rem; letter-spacing: .015em; color: #0f172a; line-height: 1.2; }
.tooltip:not(.locked) .badge-name { text-shadow: 0 1px 0 rgba(255,255,255,.6); }
.tooltip.locked .badge-name { color: #6b7280; font-style: italic; }
.badge-description { margin: 0; font-size: .9rem; line-height: 1.45; color: #374151; }
.tooltip.locked .badge-description { color: #9ca3af; }
</style>
