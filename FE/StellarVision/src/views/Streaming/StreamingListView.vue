<!-- StreamingLiserView.vue -->
<template>
  <section class="w-full px-4 md:px-8 bg-zinc-50">
    <!-- 상단 툴바 -->
   <header class="w-full sticky top-15 z-10 py-4 backdrop-blur border-b border-white/5">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <!-- 탭 -->
        <nav class="flex items-center gap-6">
          <button
          :class="activeTab === 'live' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-zinc-800 hover:text-zinc-700'"
          class="pb-1 text-lg  font-semibold transition hover:text-zinc-200"
          @click="activeTab='live'"
          > 실시간</button>
          <button
          :class="activeTab === 'vod' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-zinc-800'"
          class="pb-1 text-lg  font-semibold transition hover:text-zinc-200"
          @click="activeTab ='vod'"
          > 다시보기 </button>
        </nav>
        <!--검색-->
        <div class="flex w-full md:w-auto items-center gap-3">
          <div class="relative w-full md:w-80">
            <input
              v-model="search"
              type="text"
              placeholder="검색"
              class="w-full rounded-xl bg-white ring-1 ring-zinc-300 focus:ring-2 focus:ring-blue-800
              text-zinc-900 placeholder:text-zinc-400 px-4 py-2 outline-none"/>
            <span class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-zinc-500"><component :is="CornerDownLeft" ></component></span>
          </div>
        </div>
      </div>
    </header>
    <LoadingSpinner v-if="loading"/>
    <!-- 비디오 영역 -->
    <div class="py-6 pt-16 ">
      <!-- 임의로 로딩 넣음 추후 변경 예정 -->
      <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-6">
        <div 
          v-for="n in 8"
          :key="n"
          class="animate-pulse rounded-2xl bg-zinc-900/60 ring-1 ring-white/10">
          <div class="aspect-video bg-zinc-800/60" />
            <div class="p-4 space-y-2">
              <div class="h-4 w-3/4 bg-zinc-800 rounded" />
              <div class="h-3 w-1/2 bg-zinc-800 rounded" />
            </div>
        </div>
      </div>

      <!-- 빈 상태 (나중에 이미지로 교체 예정) -->
      <div v-else-if="filteredVideos.length === 0" 
        class="relative grid place-items-center text-center py-16 min-h-[48vh] rounded-xl overflow-hidden">
        <div 
          :style="{
             backgroundImage: `url(${bgImage})`,
            backgroundSize: '520px',        // 크기 (예: 가로 280px)
            backgroundRepeat: 'no-repeat',       // 한 장만
            backgroundPosition: 'center' // 위치
           }"
          class="absolute inset-0 bg-no-repeat bg-center opacity-30" aria-hidden="true"></div>
          <div class="relative z-10 -translate-y-48 md:-translate-y-56">
            <p class="text-zinc-700 text-lg font-semibold">조건에 맞는 스트림이 없어요.</p>
            <p class="text-zinc-500 text-sm mt-1">검색어를 바꾸거나 필터를 해제해 보세요.</p>
          </div>

      </div>
        
      <!-- 비디오 그리드 -->
      <div v-else class="grid grid-cols-1 rounded-md sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-3 2xl:grid-cols-3 gap-6">
        <VideoCard
          v-for="video in filteredVideos"
          :key="`${activeTab}-${video.id}`"
          :video="video"
          :type="activeTab"
          :onThumbnailClick="()=> handleThumbnailClick(video, activeTab)"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import VideoCard from '@/components/streaming/VideoCard.vue';
import { useStreamingStore } from '@/stores/streaming';
import { storeToRefs } from 'pinia';
import router from '@/router';
import { CornerDownLeft } from 'lucide-vue-next';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import bgImage from '@/assets/pictures/stellabot/noResult.png'

  const activeTab = ref('live')
  const search = ref('')
  const loading = ref(true)

  const streamingStore = useStreamingStore()
  const {fetchLiveStreams, fetchReplayStreams} = streamingStore
  const {liveStreams, replayStreams} = storeToRefs(streamingStore)


  onMounted(()=>{
    fetchLiveStreams()
    loading.value = false
})

// 탭 변경시마다 해당 비디오 조회 요청
watch(activeTab, (tab)=>{
    console.log('activeTab changed:', tab);
  loading.value = true
  if (tab ==='live') fetchLiveStreams();
  else if (tab === 'vod') fetchReplayStreams();
  loading.value = false
})



// 실시간/다시보기 공통 필터링 (탭마다 구조 다름)
const filteredVideos = computed(() => {
  const keyword = search.value.toLowerCase();
  if (activeTab.value === 'live') {
        console.log('필터링 대상(live):', liveStreams.value);

    return (liveStreams.value || []).filter(
      v =>
        (v.title || '').toLowerCase().includes(keyword) ||
        (v.ownerMemberName || '').toLowerCase().includes(keyword)
    );
  } else {
        console.log('필터링 대상(vod):', replayStreams.value);

    return (replayStreams.value || []).filter(
      v => (v.originalFilename || '').toLowerCase().includes(keyword)
    );
  }
});

// 각 경로로 이동
const handleThumbnailClick = async (video, type) => {
  // 실시간일 경우
  if (type === 'live') {
    router.push({ name: 'RoomView', params: { sessionId: video.sessionId } })  // 그냥 세션 아이디 주고 바로 router로 보내기
  }  // 다시보기일 경우
  else if (type === 'vod') {
    router.push(`/replay/${video.id}`)
}
}

</script>

<style scoped>
#streaming-wrapper{
  background-image: url(@/assets//pictures/wallpaper/space.jpeg);
}

.video-grid {
  display: flex;
  flex-wrap: wrap;       /* 줄 바꿈 허용 */
  gap: 1.5rem;           /* 카드 간격 */
}

</style>
