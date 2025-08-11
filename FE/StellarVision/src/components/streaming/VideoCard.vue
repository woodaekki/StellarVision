<!-- VideoCard.vue -->
<template>
  <!--  -->
  <article
    class ="group relative w-full max-w-[36rem]  rounded-2xl
     hover:ring-white/20 hover:-translate-y-0.5  cursor-pointer"
    @click="onThumbnailClick"
    :aria-label="title"
    role="button"
    tabindex="0"
    @keydown.enter="onThumbnailClick"
  >

    <!-- 썸네일 -->
     <!-- 비디오 종횡비로 구성 -->
    <div class="relative aspect-video overflow-hidden rounded-xl bg-white
             shadow-md ring-1 ring-black/5 transition-transform duration-500 ease-out
             group-hover:-translate-y-0.5 group-hover:shadow-lg group-active:translate-y-0">
      <img
        :src="thumbnail"
        class="h-full w-full object-cover [filter:drop-shadow(0_4px_8px_rgba(0,0,0,0.15))]"
        loading="lazy"
        alt="썸네일"
      />
      <!-- 우측 상단에 라이브 아이콘 -->
      <span v-if="type === 'live'"
        class="absolute top-3 right-3 bg-red-500 text-xs text-white px-2 py-0.5 rounded-full shadow font-semibold">
        LIVE
      </span>

      <!-- 태그 표시 -->
      <div v-if="type === 'vod' && tags.length > 0"
        class="absolute left-2.5 bottom-2.5 right-2.5 flex flex-wrap gap-1.5">
        <!-- 일단 태그 3개 정도만, 과하면 별로일듯 -->
        <span
          v-for="tag in tags.slice(0, 3)"
          :key="tag.tagId"
          class="px-2 py-0.5 rounded-full text-[11px] font-medium
           text-white/95 bg-black/35 backdrop-blur-[2px] ring-1 ring-white/20"
        >
          {{ tag.tagName }}
        </span>
      </div>

      <!-- hover 시 은은한 라이트 주기 -> 입체감 조성해줌 -->
      <div class="pointer-events-none absolute inset-0 opacity-0 group-hover:opacity-100 transition bg-gradient-to-t
        from-black/30 via-transparent to-transparent "></div>
    </div>

    <!-- 여기부터 본문입니다 -->
    <div class="mt-3 px-1">
      <h3 class="text-[24px] font-semibold text-black line-clamp-1">
        {{ title }}
      </h3>
      <p class="mt-0.5 text-[20px] text-zinc-600 line-clamp-1">
        {{ user }}
      </p>
        <!-- <div class="text-base text-zinc-400 mb-2 truncate">{{ video.tags }}</div> 태그는 나중에 -->
      <div v-if="type === 'vod'" class="mt-0.5 text-xs text-zinc-700">{{ date }}</div>
    </div>
    <!-- 테두리 글래스 효과 -->
     <!-- <div class="pointer-events-none absolute inset-0 rounded-2xl ring-1 ring-white/10"></div> -->
</article>
</template>


<script setup>
import {  computed, onMounted, ref } from 'vue';
import defaultLogo from '@/assets/pictures/wallpaper/11.jpg'
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'
import commonApi from '@/api/commonApi';

dayjs.extend(relativeTime) // 플러그인 적용 (시간설정)
dayjs.locale('ko') // 한국어로 적용

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
    ? defaultLogo
    : props.video.thumbnailDownloadUrl || '/pictures/wallpaper/3.jpg'
);


const title = computed(() =>
  props.type === 'live'
    ? props.video.title
    : props.video.originalFilename
)

const user = computed(() =>
  props.type === 'live'
    ? props.video.ownerMemberName
    : props.video.nickname
)

const date = computed(()=>{
  const d = dayjs(props.video.createdAt)
  const days = dayjs().diff(d, 'day')
  return days >=365 ? d.format('YYYY.MM.DD') : d.fromNow()

})

const tags = ref([])
const loadingTags = ref(false)

const loadVideoTags = async () => {
  if (loadingTags.value || tags.value.length > 0) return
  loadingTags.value = true
  try {
    const res = await commonApi.get(`/videos/${props.video.id}/tags`)
    console.log('tage data', res.data)
    if (res.data?.status === 'success'){
      tags.value = res.data.data.tags || []
      console.log('✅ 로드된 태그:', tags.value) // ★ 로드된 태그 출력

    }
    } catch (err) {
      console.error('비디오 태그 로딩 실패: ', err)
      tags.value = []
  } finally{
    loadingTags.value = false
  }
}

onMounted(()=>{
  if(props.type === 'vod') loadVideoTags()
})

</script>


<style scoped lang="scss">

</style>


<!--
loading = lazy : 이미지 로딩 시점을 늦춰 페이지 로딩 속도를 향상시키는 속성


**tailwind**
aspect-video : video 종횡비 구성 조절
line-clamp-1 : 텍스트를 특정 줄 수로 제한한다(현재는 한줄로 제한)


-->
