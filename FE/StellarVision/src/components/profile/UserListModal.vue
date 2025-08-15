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
  backdrop-filter: blur(5px);
}

.user-list-wrapper {
  padding: 40px;
  padding-top: 60px;
  border-radius: 20px;
  width: min(400px, calc(100% - 40px));
  min-height: 200px;
  max-height: 80vh;
  overflow-y: auto;
  box-sizing: border-box;
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -120%);
  background: rgba(255, 255, 255, 0.2); 
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: inset 4px 4px 10px rgba(255 255 255 / 0.6), inset -4px -4px 10px rgba(0 0 0 / 0.15), 0 8px 32px 0 rgba(0, 0, 0, 0.37); 
  border: 1.5px solid rgba(255 255 255 / 0.4);  
  animation: fadeIn 0.5s cubic-bezier(0.68, -0.55, 0.27, 1.55);
}

.close-button-wrapper {
  position: absolute;
  top: 15px;
  right: 15px;
}

.close-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  background-color: rgba(255, 255, 255, 0.1);
  color: #FAFAFA;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.close-button:hover {
  background-color: #FAFAFA;
  color: #111827;
  transform: scale(1.1);
  border-color: #FAFAFA;
}

.no-user {
  display: flex;
  justify-content: center;
  min-height: 10vh;
  text-align: center;
  font-size: 16px;
  color: #fff;
}

.no-user p {
  margin: 12px 0 0;
  font-size: 16px;
  line-height: 1.6;
  font-weight: 500;
  text-shadow: 0 0 4px rgba(0, 0, 0, 0.5);
}
</style>
