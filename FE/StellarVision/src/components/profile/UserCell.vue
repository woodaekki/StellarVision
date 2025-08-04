<template>
  <div class="user-cell" @click="goToUser">
    <span>{{ user.nickname }}</span>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  user: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['select'])

const router = useRouter()

function goToUser() {
  emit('select')
  router.push(`/profile/${props.user.id}`)
}
</script>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  color: black;
  transition: background 0.2s;
  cursor: pointer;
  gap: 10px;
}

.user-cell:hover {
  background-color: black;
  color: white;
}
</style>
