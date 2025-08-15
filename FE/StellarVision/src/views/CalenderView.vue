<template>
  <div class="page">
    <img :src="bg" alt="" class="bg-img" />

    <div class="stars-background">
      <div class="px-4 pt-3 pb-6">
        <h2 class="text-2xl mb-2 text-center font-pretendard">은하 천체일정관</h2>
        <hr class="border-t-2 border-neutral-100 w-full mt-2" />
      </div>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <span class="loading-text">데이터 불러오는 중...</span>
        </div>
      </div>

      <div v-else class="calendar-wrapper">
        <vc-calendar
          :attributes="highlightAttributes"
          :masks="{ title: 'YYYY년 MMMM' }"
          class="expanded-calendar"
        >
          <template #day-content="{ day }">
            <div class="day-cell">
              <div class="day-number">{{ day.day }}</div>
              <div class="event-list">
                <div
                  v-for="event in eventsByDate[formatDate(day.date)] || []"
                  :key="event.seq"
                  class="event"
                  :title="event.astroEvent"
                >
                  <div class="event-text">{{ event.astroEvent }}</div>
                </div>
              </div>
            </div>
          </template>
        </vc-calendar>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAstroEventInfoUrl } from '@/api/calenderApi.js'
import bg from '@/assets/pictures/stellabot/spaceBackground.avif';

const loading = ref(true)
const astroEvents = ref([])

const formatDate = (date) => date.toISOString().split('T')[0]

const eventsByDate = computed(() => {
  const map = {}
  astroEvents.value.forEach((event) => {
    const y = event.locdate.slice(0, 4)
    const m = event.locdate.slice(4, 6)
    const d = event.locdate.slice(6, 8)
    const key = `${y}-${m}-${d}`
    if (!map[key]) map[key] = []
    map[key].push(event)
  })
  return map
})

const today = ref(new Date())
const highlightAttributes = computed(() => [
  { key: 'today', highlight: { class: 'today-highlight' }, dates: today.value }
])

const fetchAstroEvents = async (year, months) => {
  const results = await Promise.all(
    months.map(async (month) => {
      const url = getAstroEventInfoUrl(year, month)
      try {
        const res = await fetch(url)
        const xml = await res.text()
        const doc = new DOMParser().parseFromString(xml, 'text/xml')

        if (doc.querySelector('resultCode')?.textContent !== '00') return []

        return Array.from(doc.querySelectorAll('items > item')).map((item) => ({
          locdate: item.querySelector('locdate')?.textContent ?? '',
          seq: item.querySelector('seq')?.textContent ?? '',
          astroEvent: item.querySelector('astroEvent')?.textContent ?? ''
        }))
      } catch (err) {
        console.error(`API 오류: ${year}-${month}`, err)
        return []
      }
    })
  )
  return results.flat()
}

onMounted(async () => {
  const currentYear = new Date().getFullYear()
  const years = [
    String(currentYear - 2),
    String(currentYear - 1),
    String(currentYear),
    String(currentYear + 1)
  ]
  const months = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']

  try {
    const allData = await Promise.all(years.map((y) => fetchAstroEvents(y, months)))
    astroEvents.value = allData.flat()
  } catch (e) {
    console.error('전체 데이터 로딩 실패:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-size: cover;
  background-image: linear-gradient(rgba(11, 12, 16, 0.7), rgba(11, 12, 16, 0.7));
  background-position: center;
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 auto;
}

.bg-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -1;
}

.stars-background {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 30px 40px;
  max-width: 1200px;
  width: 100%;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow:
    inset 4px 4px 10px rgba(255 255 255 / 0.6),
    inset -4px -4px 10px rgba(0 0 0 / 0.3),
    8px 8px 30px rgba(0 0 0 / 0.4);
  overflow-x: hidden;
  overflow-y: auto;
}

.stars-background h2 {
  margin-top: 20px !important;
  margin-bottom: 40px !important;
  margin-left: 10px;
  text-align: left;
  font-weight: 700;
  color: #ffffff;
  font-family: 'Pretendard', sans-serif !important;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.spinner {
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top: 4px solid #fff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1.1s linear infinite;
}

.loading-text {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.calendar-wrapper {
  padding: 8px;
  margin: 0 auto;
  box-sizing: border-box;
}

:deep(.vc-container) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  font-family: inherit;
  width: 100%;
  table-layout: fixed;
  color: rgba(255, 255, 255, 0.9);
}

:deep(.vc-header) {
  padding: 20px 0;
  color: #f2f2f2
}

:deep(.vc-title) {
  font-size: 1.3rem;
  font-weight: 600;
  color: #ffffff;
  padding-bottom: 6px;
}

:deep(.vc-weekday) {
  color: rgba(255, 255, 255, 0.6);
  font-weight: 600;
  text-transform: uppercase;
}

:deep(.vc-day-content) {
  height: 160px;
  padding: 6px 8px;
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-sizing: border-box;
  overflow: hidden;
  border-radius: 8px;
  transition: all 0.2s ease;
  color: #fff;
}

:deep(.vc-day-content:hover) {
  background-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

:deep(.vc-day) {
  height: 115px !important;
  flex: none !important;
  padding: 4px;
}

:deep(.vc-day.is-not-in-month .vc-day-content) {
  opacity: 0.4;
  pointer-events: none;
}

:deep(.vc-day-content.today-highlight) {
  background-color: rgba(255, 255, 255, 0.15);
  border-color: #667eea;
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.4);
}

.day-number {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 105px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
}

.event-list::-webkit-scrollbar {
  width: 6px;
}

.event-list::-webkit-scrollbar-track {
  background: transparent;
}

.event-list::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.event {
  background: rgba(102, 126, 234, 0.6);
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 500;
  padding: 3px 5px;
  border: 1px solid rgba(102, 126, 234, 0.8);
  border-radius: 4px;
  white-space: normal;
  word-break: break-word;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.event-text {
  overflow-wrap: break-word;
  word-break: break-word;
  line-height: 1.1;
}

@media (max-width: 768px) {
  .stars-background {
    padding: 20px 24px;
  }
  :deep(.vc-title) {
    font-size: 1.1rem;
    padding-bottom: 4px;
  }
  :deep(.vc-day-content) {
    height: 140px;
    padding: 4px 6px;
  }
  :deep(.vc-day) {
    height: 100px !important;
  }
  .event-list {
    max-height: 85px;
  }
  .event {
    font-size: 0.7rem;
    padding: 2px 3px;
  }
}
</style>