<!-- ChatPanel.vue -->
<script setup>
import openviduService from '@/services/openviduService'
import { useAccountStore } from '@/stores/account'
import { Minimize, Minimize2 } from 'lucide-vue-next'
import { Session } from 'openvidu-browser'
import { onBeforeMount, onBeforeUnmount, onMounted, ref } from 'vue'


const {userInfo} = useAccountStore()
const props = defineProps({
  session : {type:Object, required:true},
})

const emit = defineEmits(['close'])

// 예제 채팅
const messages = ref([
  { user: 'Alice', text: '안녕하세요!' },
  { user: 'Bob',   text: '어서오세요!' }
])
const newMessage = ref('')

//openvidu 수신 콜백 핸들러
function signalHandler(event){
  let msg
  try {
    msg = JSON.parse(event.data)
  } catch (e) {
    msg = { user: 'Unknown', text: event.data }
  }
  messages.value.push({
    user: msg.user,
    text: msg.text,
    ts: msg.ts,
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

  const payload = JSON.stringify({
    user: userInfo.name,
    text,
    ts: Date.now(), // 타임스탬프(선택)
  })

  if(!text) return

  try {
    // messages.value.push({user:props.userName, text})
    await props.session.signal({      //openvidu 공식 문서 참조
      data:payload,
      type:'chat',
      to:[]
    })
    newMessage.value = '' //새로 보낼 메시지 초기화
  } catch(err){
    console.error('signal 전송실패', err)
  }
}
</script>

<template>
  <div class="chat-panel relative h-full flex flex-col">
    <!-- 닫기 버튼 -->
     <button
      @click="emit('close')"
      class="absolute right-2 top-2 text-xl font-bold z-10 text-white"
     >
    <Minimize2/>
     </button>
    <header class="chat-header">채팅</header>
    <ul class="chat-messages flex-1 overflow-y-auto p-2">
      <li v-for="(msg, i) in messages" :key="i">
        <strong>{{ msg.user }}:</strong> {{ msg.text }}
      </li>
    </ul>
    <form class="chat-form flex" @submit.prevent="sendMessage">
      <input
        v-model="newMessage"
        type="text"
        placeholder="메시지 입력..."
        autocomplete="off"
        class="flex-1 px-2 py-1"
      />
      <button type="submit" class="px-3 py-1 bg-gray-700
        text-white rounded-r-md">전송</button>
    </form>
  </div>
</template>


<style scoped>
.chat-panel {
  min-width: 240px;
  max-width: 400px;
  height: 100%;
  background: rgba(154, 153, 153, 0.9);
  border-radius: 12px;
  overflow: hidden;
}
.chat-header {
  padding: 0.5rem;
  background: #2c2c2c;
  color: #fff;
  text-align: center;
  font-weight: bold;
}

.chat-messages li {
  margin-bottom: 0.5rem;
}

</style>
