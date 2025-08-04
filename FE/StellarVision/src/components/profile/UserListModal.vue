<template>
  <div class="modal-backdrop" @click.self="close">
    <div class="user-list-wrapper">
      <div class="close-button-wrapper">
        <button class="close-button" @click="close">X</button>
      </div>
      <!-- 여기서부터 유저 목록 랜더링 -->
      <UserCell
        v-for="user in userList"
        :key="user.id"
        :user="user"
        @select="close"
      />
    </div>
  </div>
</template>

<script setup>
import UserCell from './UserCell.vue';

const props = defineProps({
  userList: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['close'])

function close() {
  emit('close')
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0; left: 0;
  width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-list-wrapper {
  padding: 20px;
  border-radius: 12px;
  width: 300px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  background-color: white;
}

.close-button {
  color: white;
  background-color: black;
}
</style>
