<template>
  <div class="badge">
    <img :src="badgeImagePath" @click="(event) => $emit('click', event)" />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  badge: {
    type: Object,
    required: true
  }
})

const badgeImagePath = computed(() => {
  if (props.badge.collected) {
    return new URL(`../../assets/pictures/badge/${props.badge.imageName}.png`, import.meta.url).href
  } else {
    return new URL(`../../assets/pictures/badge/nobadge.png`, import.meta.url).href
  }
})
</script>

<style scoped>
.badge {
  --r: 18px; /* 톱니 간격/두께 느낌 */

  /* 프레임(톱니) 그대로 유지 */
  padding: var(--r);
  background: #fff;
  filter: drop-shadow(0 2px 6px rgba(0,0,0,.15));
  width: 80%;

  /* ✅ 콘텐츠 중앙 정렬 & 내부 영역의 안정적인 높이 확보 */
  display: grid;
  place-items: center;
  aspect-ratio: 3 / 4; /* 필요시 배지 비율에 맞게 1/1, 2/3 등으로 변경 */

  /* (기존과 동일) 톱니 마스크 */
  mask:
    radial-gradient(closest-side at center, transparent 66%, #000 67%) round
      var(--r) var(--r) / calc(2*var(--r)) calc(2*var(--r)),
    conic-gradient(from 0deg at center, #000 0deg, #000 360deg) content-box;
  -webkit-mask:
    radial-gradient(closest-side at center, transparent 66%, #000 67%) round
      var(--r) var(--r) / calc(2*var(--r)) calc(2*var(--r)),
    conic-gradient(from 0deg at center, #000 0deg, #000 360deg) content-box;
}

/* ✅ 이미지는 내부(content-box)의 가로·세로 80%를 동시에 차지 → 사방 여백 동일 */
.badge img {
  width: 90%;
  height: 90%;
  object-fit: contain; /* 원본 비율 유지, 남는 부분은 여백 */
  display: block;
  cursor: pointer;
  background: #fff; /* 투명 PNG 대비 */
}
</style>


