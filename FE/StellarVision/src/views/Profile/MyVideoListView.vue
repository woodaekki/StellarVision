<template>
  <!-- 가장 외부 스타일들은 MyGalleryListView와 동일하게 만들었습니다 -->
  <div class="page">
    <div class="stars-background">
      <div class="video-list-wrapper">
        <VideoCell
        v-for="video in videos"
        :key="video.id"
        :video="video"
        :show-edit="isUploader"
        @select="goToReplay(video.id)"/>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import VideoCell from '@/components/video/VideoCell.vue';

const router = useRouter();

// 프로필 소유자의 Id를 임의로 하드코딩, 추후 변경 필요
const memberId = 1;

const videos = ref([]);

// 지금 이 페이지를 열람하는 사람과 프로필의 소유자가 동일한지 확인한 후 bool값 받아오는 걸로 추후 교체 필요
// 각 video마다 계산하지 않고 통합하는 이유: 어차피 내 프로필 페이지는 거기 영상이 전부 내 거거나 전부 내 게 아니라서
const isUploader = true;

// 백이랑 연결할 때는 주석처리한 걸 해제하고 아래의 videos.value 삽입을 제거해주세요

// const fetchVideos = async () => {
//   const { data } = await axios.get(`/api/profiles/${memberId}/videos`)
//   videos.value = data.data.videos.map(v => ({
//     id: v.id,
//     name: v.originalFilename.slice(0, -4), // 각 방제가 파일명이기 때문에 뒤의 .mp4 확장자명을 자름
//     thumbnail: v.thumbnailDownloadUrl,
//     date: v.createdAt.split('T')[0],
//   }))
// }

// onMounted(() => {
//   fetchVideos()
// })

videos.value = [
  {
    id: '1',
    name: 'sunset_drive',
    thumbnail: 'https://images.unsplash.com/photo-1506748686214-e9df14d4d9d0',
    date: '2025-08-01',
  },
  {
    id: '2',
    name: 'mountain_hike',
    thumbnail: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470',
    date: '2025-08-01',
  },
  {
    id: '3',
    name: 'ocean_waves',
    thumbnail: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e',
    date: '2025-08-01',
  },
  {
    id: '4',
    name: 'city_lights',
    thumbnail: 'https://images.unsplash.com/photo-1491553895911-0055eca6402d',
    date: '2025-08-02',
  },
  {
    id: '5',
    name: 'forest_walk',
    thumbnail: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e',
    date: '2025-08-02',
  },
  {
    id: '6',
    name: 'cozy_fireplace',
    thumbnail: 'https://images.unsplash.com/photo-1519681393784-d120267933ba',
    date: '2025-08-02',
  },
  {
    id: '7',
    name: 'snow_trail',
    thumbnail: 'https://images.unsplash.com/photo-1516455590571-18256e5bb9ff',
    date: '2025-08-02',
  },
  {
    id: '8',
    name: 'beach_sunset',
    thumbnail: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e',
    date: '2025-08-03',
  },
  {
    id: '9',
    name: 'deep_forest',
    thumbnail: 'https://images.unsplash.com/photo-1508921912186-1d1a45ebb3c1',
    date: '2025-08-03',
  },
  {
    id: '10',
    name: 'road_trip',
    thumbnail: 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee',
    date: '2025-08-03',
  },
  {
    id: '11',
    name: 'campfire_night',
    thumbnail: 'https://images.unsplash.com/photo-1514518878964-b8d4a3f5a76b',
    date: '2025-08-03',
  },
  {
    id: '12',
    name: 'foggy_morning',
    thumbnail: 'https://images.unsplash.com/photo-1446034295857-c39f8844fad4',
    date: '2025-08-04',
  },
  {
    id: '13',
    name: 'drone_view',
    thumbnail: 'https://images.unsplash.com/photo-1504198458649-3128b932f49b',
    date: '2025-08-04',
  },
  {
    id: '14',
    name: 'night_sky',
    thumbnail: 'https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3',
    date: '2025-08-04',
  },
  {
    id: '15',
    name: 'river_path',
    thumbnail: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470',
    date: '2025-08-04',
  },
  {
    id: '16',
    name: 'star_gazing',
    thumbnail: 'https://images.unsplash.com/photo-1519608487953-e999c86e7455',
    date: '2025-08-05',
  },
  {
    id: '17',
    name: 'sunrise_peak',
    thumbnail: 'https://images.unsplash.com/photo-1493244040629-496f6d136cc3',
    date: '2025-08-05',
  },
  {
    id: '18',
    name: 'desert_wind',
    thumbnail: 'https://images.unsplash.com/photo-1504198453319-5ce911bafcde',
    date: '2025-08-05',
  },
  {
    id: '19',
    name: 'lake_house',
    thumbnail: 'https://images.unsplash.com/photo-1493246507139-91e8fad9978e',
    date: '2025-08-05',
  },
  {
    id: '20',
    name: 'trail_run',
    thumbnail: 'https://images.unsplash.com/photo-1470770841072-f978cf4d019e',
    date: '2025-08-05',
  },
];

function goToReplay(videoId) {
  router.push(`/replay/${videoId}`)
}
</script>

<style scoped>
.page {
  background-color: black;
  min-height: 100vh;
  color: white;
  font-family: sans-serif;
}

.stars-background {
  padding: 2rem;
  background: #262626;
  position: relative;
}
</style>
