<template>
  <div class="replay-page">
    <h2>다시보기 영상</h2>
    <video v-if="videoUrl" controls width="640" height="360">
      <source :src="videoUrl" type="video/mp4" />
      브라우저가 video 태그를 지원하지 않습니다.
    </video>
    <p v-else>영상을 불러오는 중입니다...</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import commonApi from '@/api/commonApi';

const route = useRoute();
const videoId = route.params.id;
const videoUrl = ref('');

onMounted(async () => {
  console.log( videoId);

  try {
    const res = await commonApi.get(`/videos/${videoId}`);

    if (res.data.status === 'success') {
      videoUrl.value = res.data.data;
    } else {
      errorMessage.value = res.data.message || 'URL 응답 실패';
    }
  } catch (err) {
    console.error('영상 URL 요청 실패:', err);
    console.error('응답 데이터:', err.response?.data);
    errorMessage.value = err.response?.data?.message || '서버 오류';
  }
});

</script>

<style scoped>
.replay-page {
  padding: 2rem;
  color: white;
  background-color: #121212;
  text-align: center;
}
</style>
