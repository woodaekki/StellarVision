<!-- VideoCard.vue -->
<template>
  <div class="bg-zinc-800 rounded-2xl shadow-lg flex items-center p-6 w-full min-h-[180px] relative transition"
  @click="onThumbnailClick">
    <!-- 썸네일 -->
    <div class="relative flex-shrink-0">
      <img
        :src="thumbnail"
        class="w-108 h-72 object-cover rounded-xl"
        alt="썸네일"
      />
      <span v-if="type === 'live'"
        class="absolute top-3 right-3 bg-red-500 text-xs text-white px-2 py-0.5 rounded-full shadow">
        LIVE
      </span>
    </div>
    <!-- 텍스트 넉넉하게 세로정렬 -->
     <div class="w-20"></div>
        <div class="flex flex-col justify-center ml-20 gap-y-6 min-w-0 w-full">
        <div class="text-xl font-bold mb-2 truncate">
        {{ title }}
      </div>
        <div class="text-lg text-zinc-300">
        {{ user }}
      </div>
        <!-- <div class="text-base text-zinc-400 mb-2 truncate">{{ video.tags }}</div> 태그는 나중에 -->
      <div v-if="type === 'vod'" class="text-xs text-zinc-400">{{ video.createdAt }}</div>
    </div>

  </div>
</template>


<script setup>
import { User } from 'lucide-vue-next';
import { ref, reactive, getCurrentInstance, onMounted, computed } from 'vue';


const props = defineProps({
  video: {
    type:Object,
    required: true
  },
  type: {
    type: String, default: 'live'     //activeTab에 따른 tab 정보
  },
  onThumbnailClick: {
    type: Function,
     required: true }
});

// 썸네일
const thumbnail = computed(() =>
  props.type === 'live'
    ? '/assets/pictures/stellabot/logo.png'
    : props.video.thumbnailDownloadUrl || '/assets/pictures/stellabot/logo.png'
);


const title = computed(() =>
  props.type === 'live'
    ? props.video.title
    : props.video.originalFilename
)

const user = computed(() =>
  props.type === 'live'
    ? props.video.ownerMemberName
    : `ID: ${props.video.memberId}`
)


function enterRoom(videoId) {

}

</script>


<style scoped lang="scss">
  .card {
    background-color: bg-zinc-800;
    border-radius: round-lg;

  }
</style>


<!-- tailwon -->
