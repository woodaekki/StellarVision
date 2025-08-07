<!-- myvideolistview -->
<template>
  <div class="page" ref="pageRef">
    <div class="stars-background">
      <div v-if="!loading && videos.length > 0" class="video-list-wrapper">
        <VideoCell
          v-for="video in videos"
          :key="video.id"
          :video="video"
          :show-edit="isUploader"
          @select="goToReplay(video.id)"
        />
      </div>

      <div v-else-if="!loading && videos.length === 0" class="video-list-wrapper">
        <p>아직 업로드한 영상이 없습니다.</p>
      </div>

      <div v-else class="video-list-wrapper">
        <p>로딩 중...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useVideoStore } from '@/stores/video';
import VideoCell from '@/components/video/VideoCell.vue';

const router = useRouter();
const route = useRoute();
const videoStore = useVideoStore();
const profilePk = window.history.state?.profilePk;

const loading = ref(true);
const pageRef = ref(null);
const page = ref(0);
const hasMore = ref(true);

// 캐시에 담겨진 이메일과 route에 들어간 이메일 대조
// 각 video마다 계산하지 않고 통합하는 이유: 어차피 내 프로필 페이지는 거기 영상이 전부 내 거거나 전부 내 게 아니라서
// 로그인 유저 검사
const isUploader = (JSON.parse(localStorage.getItem('userInfo')).email === route.params.id);

const videos = computed(() =>
  videoStore.replays.map(v => ({
    id: v.id,
    name: v.originalFilename?.slice(0, -4).replace('_', ' ') || '제목 없음',
    thumbnail: v.thumbnailDownloadUrl,
    date: v.createdAt?.split('T')[0] || '1969-07-29',
  }))
);

// 스크롤 페칭
const handleScroll = () => {
  const el = pageRef.value;
  if (!el) return;
  const { scrollTop, scrollHeight, clientHeight } = el;
  if (scrollTop + clientHeight >= scrollHeight - 50 && hasMore.value) {
    page.value++;
    fetchVideos();
  }
};

const fetchVideos = async () => {
  if (!hasMore.value) return;

  loading.value = true;
  try {
    const data = await videoStore.fetchReplays(profilePk, page.value, 10);
    hasMore.value = !data.data.isLast;
  } catch (err) {
    console.error('영상 불러오기 실패:', err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchVideos();
  pageRef.value.addEventListener('scroll', handleScroll);
});

onBeforeUnmount(() => {
  pageRef.value?.removeEventListener('scroll', handleScroll);
});

function goToReplay(videoId) {
  router.push(`/replay/${videoId}`);
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

.video-list-wrapper {
  text-align: center;
}
</style>
