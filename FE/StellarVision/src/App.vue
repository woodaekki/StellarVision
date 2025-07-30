<template>
  <div class="app-layout" :class="{ 'sidebar-open': isSidebarOpen }">
    <MainSidebar ref="sidebarRef" />
    <div class="main-content">
      <MainHeader />
      <main>
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import MainHeader from '@/components/common/MainHeader.vue'
import MainSidebar from '@/components/common/MainSidebar.vue'

const sidebarRef = ref(null)
const isSidebarOpen = ref(false)

onMounted(() => {
  if (sidebarRef.value?.isOpen !== undefined) {
    watch(() => sidebarRef.value.isOpen, (val) => {
      isSidebarOpen.value = val
    }, { immediate: true })
  }
})
</script>

<style lang="scss">
@use "@/assets/components.scss";

html,
body {
  margin: 0;
  padding: 0;
}

.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;

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
    transform: translateX(0); // slide in
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
    width: calc(100% - 175px); // shrink to make room
  }

  .main-content > header {
    height: 58px;
    background: black;
    color: white;
    position: sticky;
    top: 0;
    z-index: 999;
    width: 100%;
  }

  .main-content > main {
    flex-grow: 1;
    overflow-y: auto;
  }
}
</style>

