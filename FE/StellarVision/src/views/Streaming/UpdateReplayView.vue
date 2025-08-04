<template>
  <div class="update-video-wrapper">
    <h1>영상 정보 수정하기</h1>
    <div class="update-video-container">
      <div class="video-frame">
        <!-- 여기에 비디오 재생용 화면 배치 -->
      </div>
      <form class="update-video-form">
        <label for="replayName">제목</label>
        <input
          type="email"
          id="replayName"
          placeholder="다시보기 영상의 제목"
          v-model="replayName">

        <label for="replayTag">태그</label>
        <input
          type="email"
          id="replayTag"
          placeholder="태그"
          v-model="replayTag">

        <div class="button-container">
          <button>수정하기</button>
          <button>취소하기</button>
        </div>
      </form>
    </div>

  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.update-video-container {
  max-width: 400px;
  margin: 0 auto;
}

.update-video-form {
  margin: 16px 0;
}

input {
  display: flex;
  width: 400px;
  height: 40px;
  padding: 8px 16px;
  align-items: center;
  gap: 16px;
  border-radius: 8px;
  border: 1px solid #E0E0E0;
  background: #FFF;
}
</style>
