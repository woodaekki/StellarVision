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
  --r: 1px; /* 톱니 간격/두께 느낌 */
  filter: drop-shadow(0 2px 6px rgba(0,0,0,.15));
  width: 90%;
  margin: auto 0;
  /* 콘텐츠 중앙 정렬 & 내부 영역의 안정적인 높이 확보 */
  display: grid;
  place-items: center;
  aspect-ratio: 3 / 4; /* 필요시 배지 비율에 맞게 1/1, 2/3 등으로 변경 */
}

/* 이미지는 내부(content-box)의 가로·세로 80%를 동시에 차지 → 사방 여백 동일 */
.badge img {
  border-radius: 10px;
  width: 100%;
  height: 90%;
  object-fit: contain;
  display: block;
  padding-top: 16px;
  padding-bottom: 16px;
  padding-left: 4px;
  padding-right: 4px;
  cursor: pointer;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1.5px solid rgba(255, 255, 255, 0.253);
  background-color: rgb(98, 98, 98);
}
</style>


