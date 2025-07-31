<script setup>
import { ref } from 'vue'

const messages = ref([
  { user: 'Alice', text: '안녕하세요!' },
  { user: 'Bob',   text: '어서오세요!' }
])
const newMessage = ref('')

function sendMessage() {
  if (!newMessage.value.trim()) return
  messages.value.push({ user: 'Me', text: newMessage.value })
  newMessage.value = ''
  // TODO: 실제 OpenVidu signaling 채널로 메시지 전송 로직 추가
}
</script>

<!-- src/components/ChatPanel.vue -->
<template>
  <div class="chat-panel">
    <header class="chat-header">채팅</header>
    <ul class="chat-messages">
      <li v-for="(msg, i) in messages" :key="i">
        <strong>{{ msg.user }}:</strong> {{ msg.text }}
      </li>
    </ul>
    <form class="chat-form" @submit.prevent="sendMessage">
      <input
        v-model="newMessage"
        type="text"
        placeholder="메시지 입력..."
        autocomplete="off"
      />
      <button type="submit">전송</button>
    </form>
  </div>
</template>


<style scoped>
.chat-panel {
  position: absolute;
  bottom: 4rem;
  right: 1rem;
  width: 250px;
  max-height: 60vh;
  display: flex;
  flex-direction: column;
  background: rgba(255,255,255,0.9);
  border-radius: 8px;
  overflow: hidden;
}
.chat-header {
  padding: 0.5rem;
  background: #2c2c2c;
  color: #fff;
  text-align: center;
}
.chat-messages {
  flex: 1;
  padding: 0.5rem;
  overflow-y: auto;
  list-style: none;
  margin: 0;
}
.chat-messages li {
  margin-bottom: 0.5rem;
}
.chat-form {
  display: flex;
  border-top: 1px solid #ccc;
}
.chat-form input {
  flex: 1;
  padding: 0.5rem;
  border: none;
}
.chat-form button {
  padding: 0 1rem;
  border: none;
  background: #2c2c2c;
  color: white;
}
</style>
