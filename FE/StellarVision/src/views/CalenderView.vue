<template>
  <div class="header-wrapper">
    <h1 class="title">Special Space Day</h1>
    <hr class="text-line" />
  </div>

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
  const months = ['01','02','03','04','05','06','07','08','09','10','11','12']

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
.header-wrapper {
  width: 75vw;
  max-width: 1100px;
  margin: 100px auto 0;
  padding-left: 20px;
  box-sizing: border-box;
}

.title {
  margin: 0;
  font-weight: 700;
  font-size: 1.1rem;
  text-align: left;
}

.text-line {
  border-top: 2px solid rgb(229, 229, 229);
  margin-top: 0.8rem;
  margin-bottom: 0;
  width: 100%;
}

.container {
  background-color: #ffffff;
  min-height: 100vh;
  padding: 10px 15px;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
  margin: 0 auto;
  width: 75vw;
  max-width: 1100px;
  display: flex;
  flex-direction: column;
}

.loading {
  font-size: 1rem;
  color: #888;
  margin: 30px 0;
}

.calendar-wrapper {
  width: 100%;
  background-color: #fff;
  padding: 8px;
  margin: 0 auto;
  box-sizing: border-box;
}

:deep(.vc-container) {
  border: none;
  font-family: inherit;
  width: 100%;
  table-layout: fixed;
}

:deep(.vc-title) {
  font-size: 1.3rem;
  font-weight: 600;
  color: #2c3e50;
  padding-bottom: 6px;
}

:deep(.vc-day-content) {
  height: 160px; 
  padding: 6px 8px; 
  background-color: #fafafa;
  border: 1px solid #ddd;
  box-sizing: border-box;
  overflow: hidden;
}

:deep(.vc-day) {
  height: 115px !important; 
  flex: none !important;
}

:deep(.vc-day-content.is-today) {
  background-color: #e6f7ff;
  border-color: #66ccff;
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
}

.event {
  background-color: #f0f4f8;
  color: #1e3a8a;
  font-size: 0.75rem;
  font-weight: 500;
  padding: 3px 5px; 
  border: 1px solid #ccd6e0;
  border-radius: 2px;
  white-space: normal;
  word-break: break-word;
}

.event-text {
  overflow-wrap: break-word;
  word-break: break-word;
  line-height: 1.1;
}

@media (max-width: 768px) {
  .header-wrapper,
  .container {
    width: 90vw;
    padding-left: 10px;
    padding-right: 10px;
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
