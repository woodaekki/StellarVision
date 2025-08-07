<!-- ChatPanel.vue -->
<script setup>
import openviduService from '@/services/openviduService'
import { Session } from 'openvidu-browser'
import { onBeforeMount, onBeforeUnmount, onMounted, ref } from 'vue'


const props = defineProps({
  session : {type:Object, required:true},
  userName: {type:String, required:true},
  participants : {type: Array, required:true}
})
// 예제 채팅
const messages = ref([
  { user: 'Alice', text: '안녕하세요!' },
  { user: 'Bob',   text: '어서오세요!' }
])
const newMessage = ref('')

//openvidu 수신 콜백 핸들러
function signalHandler(event){
  messages.value.push({
    user: event.from.clientData || 'Unknown',
    text: event.data
  })
}

onMounted(()=>{
  // 채팅 메시지 수신
  props.session.on('signal:chat', signalHandler)
})

// 언마운트 시 해제
onBeforeUnmount(()=>{
  props.session.off('signal:chat', signalHandler)
})

async function sendMessage() {

  const text = newMessage.value.trim()
  if(!text) return

  try {

    await props.session.signal({      //openvidu 공식 문서 참조
      data:text,
      type:'chat',
      to:[]
    })
    newMessage.value = '' //새로 보낼 메시지 초기화
  } catch(err){
    console.error('signal 전송실패', err)
  }

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
  background: rgba(154, 153, 153, 0.9);
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
