<template>
  <div class="app-layout" :class="{ 'sidebar-open': isSidebarOpen }">
    <MainSidebar
      v-if="!isStreamingView || isStreamingView"
      ref="sidebarRef"
      v-show="isSidebarOpen"
    />
    <div class="main-content">
      <MainHeader />
      <main>
        <RouterView />
      </main>
      <MainFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MainHeader from '@/components/common/MainHeader.vue'
import MainSidebar from '@/components/common/MainSidebar.vue'
import MainFooter from './components/common/MainFooter.vue'

const sidebarRef = ref(null)
const route = useRoute()

const isStreamingView = computed(() => route.name === 'StreamingListView')

const isSidebarOpen = computed(() => {
  return isStreamingView.value ? true : (sidebarRef.value?.isOpen ?? false)
})

onMounted(() => {
  watch(() => sidebarRef.value?.isOpen, (val) => {
    if (!isStreamingView.value) {
      console.log('사이드바 토글:', val)
    }
  })
})
</script>

<style lang="scss">
@use "@/assets/components.scss";

html,
body {
  margin: 0;
  padding: 0;
}

main {
  flex-grow: 1;
}

.app-layout {
  display: flex;
  height: 100vh;

  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    width: 175px;
    height: 100vh;
    background-color: #0b0c10;
    z-index: 1000;
    transition: transform 0.3s ease;
    transform: translateX(-100%);
  }

  &.sidebar-open .sidebar {
    transform: translateX(0);
  }

  .main-content {
    margin-left: 0;
    width: 100%;
    transition: margin-left 0.3s ease, width 0.3s ease;
    display: flex;
    flex-direction: column;
    height: 100vh;
  }

  &.sidebar-open .main-content {
    margin-left: 175px;
    width: calc(100% - 175px);
  }
}
</style>