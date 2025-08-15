<!-- MyVideoView -->
<template>
  <div class="profile-section-container">
    <div class="profile-section">
      <div class="section-header">
        <h2 class="section-title">은하 영상관</h2>
        <button @click="goVideoList" class="detail-button">더보기</button>
      </div>
      <div class="content-frames">
        <div
          class="content-frame video-frame"
          v-for="video in videosWithTags"
          :key="video.id || video.videoId || video.replayId"
          @click="handleVideoClick(video)"
        >
          <div class="video-thumbnail">
            <img 
              :src="getVideoThumbnail(video)" 
              :alt="video.title || video.originalFilename || '비디오 썸네일'"
              @error="handleImageError"
            />
          </div>
          <p class="content-info video-title">{{ video.title || video.originalFilename || '제목 없음' }}</p>
        </div>
        <div v-if="(!videosWithTags || videosWithTags.length === 0) && !isLoading" class="empty-frame">
          <p class="empty-text">업로드한 영상이 없습니다.</p>
        </div>
        <div v-if="isLoading" class="empty-frame">
          <p class="empty-text">비디오를 불러오는 중...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { watch, ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import commonApi from '@/api/commonApi';
import defaultBg from '@/assets/pictures/stellabot/novaStar2.png'; // MyVideoListView와 동일한 경로로 변경

const props = defineProps({
  profileEmail: {
    type: String,
    required: true
  },
  profilePk: {
    type: [String, Number],
    required: true
  },
  recentVideos: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['select']);
const router = useRouter();

const isLoading = ref(true);
const videosWithTags = ref([]);

const loadVideoTags = async (video) => {
  try {
    const res = await commonApi.get(`/videos/${video.id}/tags`);
    console.log('태그 데이터:', res.data);
    if (res.data?.status === 'success') {
      return { ...video, tags: res.data.data.tags || [] };
    }
    return { ...video, tags: [] };
  } catch (err) {
    console.error(`비디오 ${video.id}의 태그 로딩 실패:`, err);
    return { ...video, tags: [] };
  }
};

// 모든 비디오에 대해 태그 정보를 로드
const loadAllVideoTags = async (videos) => {
  if (!videos || videos.length === 0) {
    videosWithTags.value = [];
    return;
  }
  const tagPromises = videos.map(video => loadVideoTags(video));
  const results = await Promise.all(tagPromises);
  videosWithTags.value = results;
};

// 별자리 썸네일 자동 매핑 
let STAR_IMAGES = {};
let STAR_BY_KEY = {};

try {
  // import.meta.glob을 사용하여 동적으로 이미지 로드
  STAR_IMAGES = import.meta.glob('@/assets/pictures/stars/*.{png,jpg,jpeg,webp}', {
    eager: true,
    import: 'default'
  });
} catch (error) {
  console.error('STAR_IMAGES 로드 실패:', error);
  STAR_IMAGES = {};
}

// 태그 없을 시 기본 썸네일
const defaultImg = defaultBg;

// 공백제거, 소문자화, xx자리에서 '자리'를 삭제
function normalizeKoConstellation(s) {
  const normalized = s.replace(/\s+/g, '').replace(/자리$/u, '').toLowerCase();
  return normalized;
}

// 별 이름 앞자리로 찾기, 파일명 기준으로 매핑 테이블 구성
for (const path in STAR_IMAGES) {
  const filename = path.split('/').pop().replace(/\.(png|jpg|jpeg|webp)$/i, '');
  const normalizedKey = normalizeKoConstellation(filename);
  STAR_BY_KEY[normalizedKey] = STAR_IMAGES[path];
}

// 별자리 딕셔너리
const ALIASES = {
  // '큰곰': '큰곰자리',
};

function pickStarThumbByTags(tagList, fallback) {
  if (!tagList || tagList.length === 0) {
    console.log('태그 목록이 비어있음');
    return fallback;
  }
  
  for (const t of tagList) {
    const raw = typeof t === 'string' ? t : (t.tagName || '');    
    if (!raw) {
      continue;
    }
    let key = normalizeKoConstellation(raw);
    if (ALIASES[key]) {
      key = ALIASES[key];
    }

    if (STAR_BY_KEY[key]) {
      return STAR_BY_KEY[key];
    } 
  };
  return fallback;
}

// 비디오 썸네일 결정 함수 
const getVideoThumbnail = (video) => {
  // 태그가 있는 경우 별자리 이미지 우선 사용
  if (video.tags && video.tags.length > 0) {
    console.log('🏷️ 태그가 있음, 별자리 이미지 검색 시작');
    console.log('첫 번째 태그:', video.tags[0]);
    const starThumbnail = pickStarThumbByTags(video.tags, null);
    if (starThumbnail) {
      console.log('⭐ 별자리 썸네일 선택됨:', starThumbnail);
      return starThumbnail;
    } else {
      console.log('🌌 별자리 이미지 없음, 기본 이미지로 fallback');
    }
  } return defaultImg;
};

// recentVideos가 변경될 때마다 태그 로딩 
watch(() => props.recentVideos, async (newVideos) => {
  isLoading.value = true;
  
  if (newVideos && newVideos.length > 0) {
    await loadAllVideoTags(newVideos);;
  } else {
    videosWithTags.value = [];
  }
  isLoading.value = false;
}, { 
  immediate: true,
  deep: true 
});

watch(() => props.profilePk, (newPk) => {
  if (newPk) {
    isLoading.value = true;
  }
});

onMounted(async () => {
  if (props.recentVideos && props.recentVideos.length > 0) {
    await loadAllVideoTags(props.recentVideos);
    isLoading.value = false;
  }
  
  setTimeout(() => {
    if (isLoading.value) {
      isLoading.value = false;
    }
  }, 3000);
});

function handleVideoClick(video) {
  const videoId = video.id || video.videoId || video.replayId;
  if (videoId) {
    emit('select', videoId);
  }
}

function goVideoList() {
  router.push({
    name: 'MyVideoListView', 
    params: { id: props.profileEmail },
    state: { profilePk: props.profilePk }
  });
}

function formatDate(dateString) {
  if (!dateString) return '날짜 정보 없음';
  
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
  } catch (error) {
    return dateString;
  }
}

function handleImageError(event) {
  // 이미지 로드 실패 시 기본 이미지로 대체
  event.target.src = defaultImg;
}
</script>

<style scoped>
.profile-section-container {
  margin-top: 15px;
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
  width: 100%;
  font-family: 'Pretendard', sans-serif;
  color: white;
  background: url('/assets/space-bg.jpg') center/cover no-repeat;
  padding: 20px 0;
}

.profile-section {
  width: 100%;
  max-width: 1200px;
}

.section-header {
  padding: 0 35px;
}

.content-frames {
  max-width: calc(100% - 70px);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding-top: 5px;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 35px;
  margin-bottom: 10px;
}

.section-title {
  font-size: 25px;
  font-weight: 700;
}

.detail-button {
  color: white;
  cursor: pointer;
  background: rgba(15, 20, 40, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.7);
  padding: 6px 14px;
  border-radius: 6px;
  backdrop-filter: blur(6px);
  transition: background 0.3s ease;
}

.detail-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.video-frame .video-thumbnail {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.video-frame .video-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.8);
  opacity: 0.97;
  transition: filter 0.3s ease, opacity 0.3s ease;
}

.video-frame:hover .video-thumbnail img {
  filter: brightness(1);
  opacity: 1;
}

.no-thumbnail {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: rgba(255, 255, 255, 0.5);
  background: rgba(0, 0, 0, 0.3);
}

.video-frame:hover .content-info {
  opacity: 1;
}

.content-info {
  position: absolute;
  top: 6px;
  left: 6px;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  padding: 2px 4px;
  border-radius: 4px;
  font-size: 10px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.empty-frame {
  justify-content: center;
  align-items: center;
  min-height: 140px;
  width: 100%;
  display: flex;
}

.empty-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin-top: 10px;
  font-weight: 500;
}

.content-frame {
  border-radius: 10px;
  width: calc(33.333% - 14px);
  max-width: 380px;
  aspect-ratio: 1.3;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.05));
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.content-frame:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.7);
  transform: translateY(-5px);
}
</style>