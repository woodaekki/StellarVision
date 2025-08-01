<template>
  <img :src="badgeImagePath" class="badge" @click="(event) => $emit('click', event)" />
</template>

<script setup>
import { defineProps, computed } from 'vue'

const props = defineProps({
  badge: Object
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
