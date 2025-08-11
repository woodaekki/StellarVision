<template>
  <button class="profile-edit-button" @click="clickEditButton">
    {{ isEditing ? '완료' : '수정' }}
  </button>
</template>

<script setup>

const props = defineProps({
    isEditing: { 
        type: Boolean, 
        default: false 
    }
});
const emit = defineEmits(['click']);

function clickEditButton() {
  emit('click');
}
</script>


<style scoped>
.profile-edit-button {
  padding: 8px 16px;
  background-color: #444;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
</style>
