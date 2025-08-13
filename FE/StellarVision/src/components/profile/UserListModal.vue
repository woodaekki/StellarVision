<template>
  <div class="modal-backdrop" @click.self="close">
    <div class="user-list-wrapper">
      <div class="close-button-wrapper">
        <button class="close-button" @click="close">X</button>
      </div>
      <!-- 여기서부터 유저 목록 랜더링 -->
       <div v-if="userList.length > 0">
        <UserCell
          v-for="user in userList"
          :key="user.id"
          :user="user"
          @select="close"
        />
       </div>
      <div v-else class="no-user">
        <p>여기에 아무도 없어요.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import UserCell from './UserCell.vue';

const props = defineProps({
  userList: {
    type: Array,
    default: () => []
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
  inset: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-list-wrapper {
  padding: 20px;
  padding-top: 48px; /* 👈 버튼이 차지하는 공간만큼 위쪽 여백 추가 */
  border-radius: 12px;
  width: min(300px, calc(100% - 32px));
  min-height: 200px;
  max-height: 80vh;
  overflow-y: auto;
  box-sizing: border-box;

  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -120%);
  background-color: #FAFAFA;
}

.close-button-wrapper {
  position: absolute;
  top: 12px;
  right: 12px;
}

.close-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;

  background-color: #111827;
  color: #FAFAFA;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.close-button:hover {
  background-color: #FAFAFA;
  color: #111827;
  border: 1px solid #111827;
}

.no-user {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  min-height: 25vh;
  padding: 16px;
  text-align: center;
  font-size: 14px;
  color: #6B7280;
  background-color: #F9FAFB;
  border-radius: 8px;
  border: 1px dashed #D1D5DB;
}

.no-user p {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.4;
}
</style>
