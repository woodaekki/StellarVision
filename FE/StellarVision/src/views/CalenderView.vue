<template>
  <div class="container">
    <h1 class="title">올해의 천체 일정</h1>

    <div v-if="loading">데이터 불러오는 중...</div>

    <vc-calendar v-else :attributes="highlightAttributes" is-expanded>
      <template #day-content="{ day }">
        <div class="day-cell">
          <div class="day-number">{{ day.day }}</div>
          <div
            v-for="event in eventsByDate[formatDate(day.date)] || []"
            :key="event.seq"
            class="event"
          >
            {{ event.astroEvent }}
          </div>
        </div>
      </template>
    </vc-calendar>
  </div>
</template>


<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAstroEventInfoUrl } from '@/api/calenderApi.js'

const loading = ref(true)
const astroEvents = ref([])

const formatDate = (date) => date.toISOString().split('T')[0]

// 날짜별 이벤트 정리
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

// 오늘 날짜 강조
const today = ref(new Date())
const highlightAttributes = computed(() => [
  { key: 'today', highlight: true, dates: today.value }
])

// API에서 데이터 가져오기
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
          astroEvent: item.querySelector('astroEvent')?.textContent ?? '',
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
  const years = ['2020', '2021', '2022', '2023', '2024', '2025']
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
.container {
  background-image: url(@/assets//pictures/wallpaper/space.jpeg); 
  min-height: 100vh;
  padding: 16px;
  padding-top: 58px; 
  box-sizing: border-box;
  font-family: Paperlogy-8ExtraBold, sans-serif;
  color: #fff;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  font-size: 1.8rem;
  font-weight: bold;
  color: #fff;
  margin-top: 20px;
  margin-bottom: 20px;
}

.loading {
  font-size: 1rem;
  color: #666;
  margin: 30px 0;
}

.big-calendar {
  width: 100%;
  max-width: 1000px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 10px;
  margin: 0 auto;
  justify-items: center;
}

.day-cell {
  background: #393939;
  border: 1px solid #ccc;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  min-height: 80px;
  padding: 8px 6px;
  box-sizing: border-box;
  color: #f8f8f8;
  transition: background 0.2s ease;
}

.day-cell:hover {
  background: #424242;
}

.day-number {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 4px;
  color: #fff;
}

.event {
  font-size: 0.7rem;
  background-color: #a8a8a86a;
  color: #fff;
  padding: 1px 4px;
  margin: 1px 0;
  border-radius: 4px;
  line-height: 1.2;
  word-break: break-word;
  overflow-wrap: anywhere;
  max-width: 100%;
}

@media (max-width: 600px) {
  .title {
    font-size: 1.5rem;
  }

  .day-cell {
    min-height: 70px;
    padding: 4px 3px;
  }

  .event {
    font-size: 0.65rem;
    padding: 1px 3px;
  }
}
</style>


