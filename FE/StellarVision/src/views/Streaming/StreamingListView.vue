<!-- StreamingLiserView.vue -->
<template>
  <div class="flex items-center justify-between mb-6">
    <div>
      <button
      :class="activeTab === 'live' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-zinc-400'"
      class="pb-2 px-4 font-semibold transition"
      @click="activeTab='live'"
      > 실시간</button>
      <button
      :class="activeTab === 'vod' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-zinc-400'"
      class="pb-2 px-4 font-semibold transition"
      @click="activeTab = 'vod'"
      > 다시보기 </button>
    </div>
    <input
      v-model="search"
      type="text"
      placeholder="검색"
      class="ml-4 px-3 py-2 rounded-lg bg-zinc-800 text-black w-12 focus:outline-none
      focus:ring-2 focus:ring-blue-400 transition"/>
  </div>

<!-- 가로 스크롤 -->
  <div class="flex flex-col gap-y-8 max-w-8xl w-full mx-auto px-4">

      <!-- 카드 내용(아까 만든 카드 스타일 활용) -->
      <VideoCard
      v-for="video in filteredVideos"
      :key="video.title"
      :video="video"
      />
  </div>

</template>

<script setup>
import { RouterLink } from 'vue-router';
import { computed, ref } from 'vue';
import VideoCard from '@/components/streaming/VideoCard.vue';

  const activeTab = ref('live')

  const videos = [
    {
      title : "Jupiter's Moons Odyssey",
      thumb : "https://picsum.photos/400/300?random=1",
      tags : "#planet #space",
      user : 'Kim jin kyuong',
      isLive: true,
      type:'live'
    },
    {
      title: "오리온자리 별의 노래",
      thumb: 'https://picsum.photos/400/300?random=2',
      tags: '#planet #space',
      user: 'lee',
      isLive: true,
      type: 'live'
    },
    {
      title: "은하수를 거닐다",
      thumb: 'https://picsum.photos/400/300?random=3',
      tags: '#planet #space',
      user: 'park',
      isLive: true,
      type: 'live' },
    {
      title: "Venus Replay",
      thumb: 'https://picsum.photos/400/300?random=4',
      tags: '#planet #space',
      user: 'kim',
      isLive: false,
      type: 'vod' },
 {
      title: "은하수를 거닐다",
      thumb: 'https://picsum.photos/400/300?random=5',
      tags: '#planet #space',
      user: 'park',
      isLive: true,
      type: 'live' }, {
      title: "은하수를 거닐다",
      thumb: 'https://picsum.photos/400/300?random=6',
      tags: '#planet #space',
      user: 'park',
      isLive: true,
      type: 'live' },
       {
      title: "은하수를 거닐다",
      thumb: 'https://picsum.photos/400/300?random=7',
      tags: '#planet #space',
      user: 'park',
      isLive: true,
      type: 'live' }, {
      title: "은하수를 거닐다",
      thumb: 'https://picsum.photos/400/300?random=8',
      tags: '#planet #space',
      user: 'park',
      isLive: true,
      type: 'live' }, {
      title: "은하수를 거닐다",
      thumb: 'https://picsum.photos/400/300?random=9',
      tags: '#planet #space',
      user: 'park',
      isLive: true,
      type: 'live' },
  ]

  const filteredVideos = computed(() =>
  videos.filter(v => v.type === activeTab.value)
)

</script>

<style scoped lang="scss"></style>
