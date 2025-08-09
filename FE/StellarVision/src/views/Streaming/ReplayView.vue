<template>
  <div class="replay-page">
    <video v-if="videoUrl" controls>
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
  padding: 0;
  margin: 0;
  background-color: #fff;
  color: white;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

video {
  width: 90vw;  
  height: 90vh; 
  max-width: 1280px; 
  max-height: 720px;  
  object-fit: contain; 
  background-color: #fff;
}
</style>