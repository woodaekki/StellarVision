<!-- StreamingListView.vue -->
<template>
  <section class="flex flex-col h-full overflow-hidden" :class="{ 'p-0 bg-transparent': isSidebar }">
    <!-- 탭 및 검색 헤더 -->
    <header class="flex-shrink-0 px-6 pt-4 pb-2 border-b border-white/10">
      <!-- 탭 네비게이션 -->
      <nav class="flex gap-2 mb-4">
        <button
          class="flex-1 px-4 py-2.5 rounded-lg text-sm font-medium border border-white/10
                 bg-transparent cursor-pointer transition-all duration-200"
          :class="activeTab === 'live'
            ? 'bg-blue-500/20 border-blue-500/30 text-blue-400'
            : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'"
          @click="activeTab = 'live'"
        >
          실시간
        </button>
        <button
          class="flex-1 px-4 py-2.5 rounded-lg text-sm font-medium border border-white/10
                 bg-transparent cursor-pointer transition-all duration-200"
          :class="activeTab === 'vod'
            ? 'bg-blue-500/20 border-blue-500/30 text-blue-400'
            : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'"
          @click="activeTab = 'vod'"
        >
          다시보기
        </button>
      </nav>

      <!-- 검색창 -->
      <div class="mb-2">
        <div class="relative flex items-center">
          <input
            v-model="search"
            type="text"
            placeholder="검색..."
            class="w-full px-4 py-2.5 pr-10 rounded-lg border border-white/10
                   bg-white/5 text-slate-50 text-sm outline-none transition-all duration-200
                   focus:border-blue-500/50 focus:bg-white/8 placeholder:text-slate-500"
          />
          <CornerDownLeft class="absolute right-3 w-4 h-4 text-slate-500 pointer-events-none" />
        </div>
      </div>
    </header>

    <!-- 로딩 스피너 -->
    <LoadingSpinner v-if="loading" class="my-8 mx-auto" />

    <!-- 콘텐츠 영역 -->
    <div class="flex-1 overflow-y-auto overflow-x-hidden p-2
                scrollbar-thin scrollbar-thumb-white/20 scrollbar-track-transparent
                hover:scrollbar-thumb-white/30 overscroll-contain touch-pan-y">

      <!-- 로딩 스켈레톤 -->
      <div v-if="loading" class="flex flex-col gap-4 p-4">
        <div
          v-for="n in 6"
          :key="n"
          class="flex gap-4 animate-pulse"
        >
          <div class="w-[120px] h-[68px] bg-white/10 rounded-lg flex-shrink-0" />
          <div class="flex-1 flex flex-col gap-2 justify-center">
            <div class="h-4 w-4/5 bg-white/10 rounded" />
            <div class="h-3 w-3/5 bg-white/10 rounded" />
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-else-if="filteredVideos.length === 0"
           class="flex flex-col items-center justify-start min-h-[700px] p-8 relative">
        <div
          class="absolute inset-0 bg-no-repeat bg-center opacity-10"
          :style="{
            backgroundImage: `url(${bgImage})`,
            backgroundSize: '620px',
            backgroundPosition: 'bottom'
          }"
          aria-hidden="true"
        />
        <div class="text-center z-10">
          <p class="text-slate-200 text-lg font-semibold m-0 mb-2 top-0">조건에 맞는 스트림이 없어요.</p>
          <p class="text-slate-500 text-sm m-0">검색어를 바꾸거나 필터를 해제해 보세요.</p>
        </div>
      </div>

      <!-- 비디오 그리드 - 세로 배치 -->
      <div v-else class="flex flex-col gap-4 p-4 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-2 items-center">
        <VideoCard
          v-for="video in filteredVideos"
          :key="`${activeTab}-${video.id}`"
          :video="video"
          :type="activeTab"
          :is-sidebar="isSidebar"
          :on-thumbnail-click="() => handleThumbnailClick(video, activeTab)"
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
import bgImage from '@/assets/pictures/stellabot/novaStar2.png';
import streamingService from '@/services/streamingService';
import { useAccountStore } from '@/stores/account';

const props = defineProps({
  isSidebar: {
    type: Boolean,
    default: false
  }
});

const activeTab = ref('live');
const search = ref('');
const loading = ref(true);

const userStore = useAccountStore()
const userName = userStore.userInfo?.name


const streamingStore = useStreamingStore();
const { fetchLiveStreams, fetchReplayStreams } = streamingStore;
const { liveStreams, replayStreams } = storeToRefs(streamingStore);

onMounted(() => {
  fetchLiveStreams();
  loading.value = false;
});

// 탭 변경시 데이터 로딩
watch(activeTab, (tab) => {
  console.log('activeTab changed:', tab);
  loading.value = true;
  if (tab === 'live') fetchLiveStreams();
  else if (tab === 'vod') fetchReplayStreams();
  loading.value = false;
});

// 필터링된 비디오 목록
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

// 썸네일 클릭 핸들러
const handleThumbnailClick = async (video, type) => {
  if (type === 'live') {
    try {

      router.push({
        name:'RoomView',
        params:{id:video.sessionId},
        query: {
          title : video.title,
          streamerName: video.ownerMemberName
        }}
        )
    }catch(err){
      console.error('방 참여 실패: ', err)
    }
  } else if (type === 'vod') {
    router.push(`/replay/${video.id}`);
  }
};
</script>

<style scoped>
/* 커스텀 스크롤바 (Webkit 기반 브라우저용) */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 모바일 대응 */
@media (max-width: 768px) {
  .px-6 {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .p-4 {
    padding: 0.5rem;
  }
}
</style>
