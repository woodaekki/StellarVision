<template>
  <img :src="badgeImagePath" class="badge" @click="(event) => $emit('click', event)" />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  badge: {
    type: Object,
    required: true
  }
})

const badgeImagePath = computed(() =>
  new URL(`../../assets/pictures/badge/${props.badge.imageName}.png`, import.meta.url).href
)
</script>

<style scoped>
.badge {
  width: 100%;
  height: auto;
  display: block;
  cursor: pointer;
}
</style>
