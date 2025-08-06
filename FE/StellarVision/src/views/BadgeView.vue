<template>
  <div class="badge-wrapper">
    <div class="badge-container" ref="containerRef">
      <Badge
        v-for="(badge, index) in badgeList"
        :key="index"
        :badge="badge"
        @click="(event) => handleBadgeClick(event, badge)"
      />
    </div>

    <div
      v-if="selectedBadge"
      class="tooltip"
      :class="tooltipPosition"
      :style="{ top: tooltipY + 'px', left: tooltipX + 'px' }"
    >
      <h3>
        {{ selectedBadge.collected ? selectedBadge.koreanName : '???' }}
      </h3>
      <p>
        {{  selectedBadge.collected ? selectedBadge.koreanDescription
            : '아직 만나지 못한 별자리입니다. 미래에 만날 수 있을까요?'
        }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Badge from '@/components/badge/Badge.vue';
import badgeData from '@/data/badgeData.json';
import axios from 'axios';
import axiosApi from '@/api/axiosApi';

// 나중에 백에서 받은 정보를 기반으로 collected 매핑으로 수정 예정
const badgeList = ref(badgeData.map(badge => ({
  ...badge,
  collected: true
})));

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
    tooltipX.value = clickX - 120;
    tooltipPosition.value = 'left';
  } else {
    tooltipX.value = clickX + 40;
    tooltipPosition.value = 'right';
  }

  tooltipY.value = clickY;
}

</script>

<style scoped>
.badge-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  padding: 1rem;
  overflow-x: hidden;
  position: relative;
}

.badge-wrapper {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.tooltip {
  position: absolute;
  max-width: 200px;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 10px;
  padding: 0.75rem;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  z-index: 10;
  white-space: normal;
}

.tooltip.right::after,
.tooltip.left::after {
  content: '';
  position: absolute;
  top: 1rem;
  border: 8px solid transparent;
}

.tooltip.right::after {
  left: -16px;
  border-right-color: #ccc;
}

.tooltip.left::after {
  right: -16px;
  border-left-color: #ccc;
}
</style>
