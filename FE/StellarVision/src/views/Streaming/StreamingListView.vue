<template>
+  <div class="streaming-layout">
+    <h2>스트리밍 페이지입니다.</h2>
+    <!-- (선택) 탭 네비게이션 -->
+    <nav>
+      <RouterLink to="/streaming/livestreaminglist">실시간</RouterLink>
+      <RouterLink to="/streaming/replay">다시보기</RouterLink>
+    </nav>
+
+    <!-- 여기에 자식 라우트(PreRoomView, RoomView 등)가 렌더됩니다 -->
+    <router-view />
+  </div>

    <br>
</template>

<script setup>
import { RouterLink } from 'vue-router';
import LiveStreamingListView from './LiveStreamingListView.vue';
import { ref } from 'vue';

const liveClick = ref(true)
const replayClick = ref(false)

</script>

<style scoped lang="scss"></style>
