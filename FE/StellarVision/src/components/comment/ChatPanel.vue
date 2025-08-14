<!-- ChatPanel.vue -->
<script setup>
import openviduService from '@/services/openviduService'
import { useAccountStore } from '@/stores/account'
import { Minimize, Minimize2, Users } from 'lucide-vue-next'
import { Session } from 'openvidu-browser'
import { computed, onBeforeMount, onBeforeUnmount, onMounted, ref } from 'vue'
import bgTex from '@/assets/pictures/stellabot/chatPanel.png'
import { nextTick } from 'vue' //데이터 변경 -> 화면 반영 -> 그 다음 작업 실행의 순서를 보장해주는 기능

const {userInfo} = useAccountStore()
const props = defineProps({
  session : {type:Object, required:true},
})
const myName = computed(() => userInfo?.name || '')

const emit = defineEmits(['close'])

// 예제 채팅
const messages = ref([])
const newMessage = ref('')

const listRef = ref(null)
// 사용자가 거의 맨 아래에 있는지 여부
const isNearBottom = ref(true)


//openvidu 수신 콜백 핸들러
function signalHandler(event){
  let msg
  try {
    msg = JSON.parse(event.data)
  } catch (e) {
    // Json으로 안 올 경우 대비
    msg = { user: 'Unknown', text: event.data }
  }
  messages.value.push({
    user: msg.user,
    text: msg.text,
    ts: msg.ts,
  })
  nextTick(() => { if (isNearBottom.value) scrollToBottom() })

}

onMounted(()=>{
  // 채팅 메시지 수신
  props.session.on('signal:chat', signalHandler)
  listRef.value?.addEventListener('scroll', handleScroll, { passive: true })
  nextTick(scrollToBottom)
})

// 언마운트 시 해제
onBeforeUnmount(()=>{
  props.session.off('signal:chat', signalHandler)
  listRef.value?.removeEventListener('scroll', handleScroll)
})

// 메시지 송신
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

function atBottom(el, threshold = 40) {
  return el.scrollHeight - el.scrollTop - el.clientHeight <= threshold
}
function scrollToBottom() {
  const el = listRef.value
  if (!el) return
  el.scrollTop = el.scrollHeight
}
function handleScroll() {
  const el = listRef.value
  if (!el) return
  isNearBottom.value = atBottom(el)
}

</script>

<template>
  <aside
    :style="{
      backgroundImage: `linear-gradient(rgba(255,255,255,0.15), rgba(255,255,255,0.15)), url(${bgTex})`,
      backgroundSize: '460px',        // 크기 (예: 가로 280px)
      backgroundRepeat: 'no-repeat',       // 한 장만
      backgroundPosition: 'center' // 위치
    }"
    class="chat-panel h-full w-full flex flex-col bg-zinc-900/70 backdrop-blur-md ring-1
    ring-white/10 text-zinc-100 rounded-none sm:rounded-l-2xl overflow-hidden" >
    <header class="chat-header flex items-center gap-1 text-xs text-zinc-400">
      <div class="flex items-center gap-2 text-sm font-semibold">
        <span>채팅</span>
        <span class="flex items-center gap-1 text-xs text-zinc-400">
          <Users class="w-3.5 h-3.5" /> {{ props.session?.remoteConnections?.size ?? 0 }}
        </span>
      </div>
      <!-- 닫기 버튼 -->
      <button
        @click="emit('close')"
        class="absolute right-2 top-2 text-xl font-bold z-10 text-white">
          <Minimize2/>
      </button>
    </header>

    <!-- 메시지 리스트 -->
    <!-- 08.13 12:34 분 listRef 추가 -->
    <ul ref="listRef" class="chat-messages flex-1 overflow-y-auto p-3 space-y-2 min-h-0">
      <li v-for="(msg, i) in messages" :key="i"
          class="flex"
          :class="msg.user === myName ? 'justify-end' : 'justify-start'">
        <div
          :class="[
            'max-w-[80%] px-3 py-2 rounded-2xl leading-snug text-[13px] shadow',
            msg.user === myName
              ? 'bg-sky-600 text-white rounded-tr-sm'
              : 'bg-white/10 text-white/90 ring-1 ring-white/10 rounded-tl-sm'
          ]"
        >
          <p v-if="msg.user !== myName" class="text-[11px] text-zinc-300 mb-0.5">{{ msg.user }}</p>
          <p class="whitespace-pre-wrap break-words">{{ msg.text }}</p>
        </div>
      </li>
    </ul>

    <!-- 입력영역 -->
    <form class="caht-form flex w-full shrink-0 p-2 border-t border-white/10 bg-zinc-900/60" @submit.prevent="sendMessage">
      <div class="flex w-full items-end gap-2">
        <input
          v-model="newMessage"
          type="text"
          placeholder="메시지 입력…"
          autocomplete="off"
          class="flex-1 rounded-xl px-3 py-2 text-[13px] leading-snug
                 bg-white/5 ring-1 ring-white/10 placeholder:text-zinc-400
                 focus:outline-none focus:ring-2 focus:ring-sky-500/60"
        />
        <button type="submit"
                class="shrink-0 inline-flex items-center justtify-center gap-1.5 rounded-xl px-3 py-2 text-[13px]
                       bg-sky-600 hover:bg-sky-500 disabled:opacity-50 transition text-centerr"
                :disabled="!newMessage.trim()">
          <Send class="h-4 text-center" /> 전송
        </button>
      </div>
    </form>
  </aside>


</template>


<style scoped>

.chat-panel {

  height: 100%;
  overflow: hidden; /* 패널 바깥으로 새는 요소 차단 */
}
.chat-header {
  padding: 0.5rem;
  background: #2c2c2c;
  color: #fff;
  text-align: center;
  font-weight: bold;
}

.chat-messages li {
  margin-bottom: 0.5rem
}

ul::-webkit-scrollbar { width: 8px; }
ul::-webkit-scrollbar-thumb { background: rgba(255,255,255,.15); border-radius: 8px; }
ul::-webkit-scrollbar-track { background: transparent; }



</style>
