<!-- StreamingLiserView.vue -->
<template>

  <div id="streaming-wrapper" class="flex items-center justify-between p-18 mb-6">
    <div>
      <button
      :class="activeTab === 'live' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-zinc-400'"
      class="pb-2 px-4 font-semibold transition"
      @click="activeTab='live'"
      > 실시간</button>
      <button
      :class="activeTab === 'vod' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-zinc-400'"
      class="pb-2 px-4 font-semibold transition"
      @click="activeTab ='vod'"
      > 다시보기 </button>
    </div>
    <input
      v-model="search"
      type="text"
      placeholder="검색"
      class="ml-4 px-3 py-2 rounded-lg bg-zinc-800 text-black w-12 focus:outline-none
      focus:ring-2 focus:ring-blue-400 transition"/>
  </div>

<!-- 세로 스크롤 -->
  <div class="flex flex-col gap-y-8 max-w-8xl w-full mx-auto px-4">

      <!-- 카드 내용(아까 만든 카드 스타일 활용) -->
      <VideoCard
      v-for="video in filteredVideos"
      :key="`${activeTab}-${video.id}`"
      :video="video"
      :type="activeTab"
      :onThumbnailClick="()=> handleThumbnailClick(video, activeTab)"
      />
  </div>

</template>

<script setup>
import { RouterLink } from 'vue-router';
import { computed, onMounted, ref, watch } from 'vue';
import VideoCard from '@/components/streaming/VideoCard.vue';
import { useStreamingStore } from '@/stores/streaming';
import { storeToRefs } from 'pinia';
import router from '@/router';

  const activeTab = ref('live')
  const search = ref('')
  const streamingStore = useStreamingStore()
  const {fetchLiveStreams, fetchReplayStreams} = streamingStore
  const {liveStreams, replayStreams} = storeToRefs(streamingStore)


  onMounted(()=>{
    fetchLiveStreams()
})

// 탭 변경시마다 해당 비디오 조회 요청
watch(activeTab, (tab)=>{
    console.log('activeTab changed:', tab);

  if (tab ==='live') fetchLiveStreams();
  else if (tab === 'vod') fetchReplayStreams();
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
</style>
