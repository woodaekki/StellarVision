<template>
  <h1 class="title">Special Space Day</h1>
  <hr class="text-line">
  <div class="container">
    <div v-if="loading" class="loading">데이터 불러오는 중...</div>

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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAstroEventInfoUrl } from '@/api/calenderApi.js'

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
  const years = [String(currentYear - 2), String(currentYear - 1), String(currentYear)]
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
.text-line {
   border-top: 2px solid rgb(229, 229, 229);
  width: 85vw;
  max-width: 1400px; 
  margin: 0 auto; 
  margin-top: 0.5rem;
}
.container {
  background-color: #ffffff;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 auto;
  margin-top: 10px;
}

.title {
  margin-top: 100px;
  margin-left: 140px;
  text-align: left;
  font-weight: 700;
  font-size: medium;
}

.loading {
  font-size: 1.2rem;
  color: #888;
  margin: 50px 0;
}

.calendar-wrapper {
  width: 85vw; 
  max-width: 1400px;
  background-color: #fff;
  padding: 10px;
  margin: 0 auto;
  box-sizing: border-box;
}

:deep(.vc-container) {
  border: none;
  font-family: inherit;
  width: 100%;;
  table-layout: fixed;
}

:deep(.vc-title) {
  font-size: 1.6rem;
  font-weight: 600;
  color: #2c3e50;
}

:deep(.vc-day-content) {
  height: 300px;
  padding: 8px;
  background-color: #fafafa;
  border: 1px solid #ddd;
  box-sizing: border-box;
}

:deep(.vc-day) {
  height: 170px !important; 
  flex: none !important; 
}

:deep(.vc-day-content.is-today) {
  background-color: #e6f7ff;
  border-color: #66ccff;
}

.day-number {
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 6px;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event {
  background-color: #f0f4f8;
  color: #1e3a8a;
  font-size: 0.85rem;
  font-weight: 500;
  padding: 4px 6px;
  border: 1px solid #ccd6e0;
}

.event-text {
  overflow-wrap: break-word;
  word-break: break-word;
}
</style>

