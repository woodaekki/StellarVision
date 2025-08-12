<template>
  <div class="app-layout">

    <div class="main-content">cccccccccccccccccccccccccccccccccccccccccccccc
      <MainHeader
        v-if="!isRoomView"
        :is-main-view="isMainView"
      />
      <main>
        <RouterView />
      </main>
      <MainFooter
        v-if="!isRoomView" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import MainHeader from '@/components/common/MainHeader.vue'
import MainFooter from './components/common/MainFooter.vue'

const route = useRoute()

const isRoomView = computed(() => route.name === 'RoomView')
const isMainView = computed(() => route.name === 'MainView')


</script>

<style lang="scss">
@use "@/assets/components.scss";

html,
body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

main {
  flex-grow: 1;
}

</style>
