import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 캘린더 라이브러리
import VCalendar from 'v-calendar'
import 'v-calendar/style.css'

// 알림창 라이브러리
import { ToastService } from 'primevue'

const app = createApp(App)

app.use(VCalendar, {
  componentPrefix: 'vc',
})

app.use(ToastService)

app.use(createPinia())
app.use(router)

app.mount('#app')
