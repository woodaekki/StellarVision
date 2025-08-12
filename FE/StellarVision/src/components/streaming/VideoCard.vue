<!-- VideoCard.vue -->
<template>
  <!--  -->
  <article
    class ="group relative rounded-xl overflow-hidden
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
      <h3 class="text-[24px] font-semibold text-white
       line-clamp-1 ">
        {{ title }}
      </h3>
      <p class="mt-0.5 text-[20px] text-zinc-100 line-clamp-1">
        {{ user }}
      </p>
        <!-- <div class="text-base text-zinc-400 mb-2 truncate">{{ video.tags }}</div> 태그는 나중에 -->
      <div v-if="type === 'vod'" class="mt-0.5 text-xs text-white">{{ date }}</div>

      <!-- 좋아요 버튼 -->
      <button v-if="type === 'vod'"
        @click.stop.prevent="onLikeClick"
        class="absolute right-2
         bottom-5 z-3 flex-items-center gap-2 rounded-full
        hover:bg-zinc-100/80 px-3 py-1 transition focus:outline-none focus:ring-0
         disabled:opacity-50 disabled:pointer-events-none">
        <Star :class="liked ? 'fill-yellow-200 text-yellow-400' : 'fill-none text-white-800'"/>
        <span class="text-sm font-medium text-zinc-200 select-none">
           {{ likeCount }}
        </span>
      </button>
    </div>
    <!-- 테두리 글래스 효과 -->
     <!-- <div class="pointer-events-none absolute inset-0 rounded-2xl ring-1 ring-white/10"></div> -->
</article>
</template>


<script setup>
import {  computed, onMounted, ref, watch } from 'vue';
import defaultLogo from '@/assets/pictures/wallpaper/11.jpg'
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'
import commonApi from '@/api/commonApi';
import { Star } from 'lucide-vue-next';
import { useStreamingStore } from '@/stores/streaming';

const store = useStreamingStore()
const processing = ref(false)  // 중복 방지

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
      console.log('로드된 태그:', tags.value) // ★ 로드된 태그 출력

    }
    } catch (err) {
      console.error('비디오 태그 로딩 실패: ', err)
      tags.value = []
  } finally{
    loadingTags.value = false
  }
}

const liked = ref(!!props.video.liked)
const likeCount = ref(props.video.likeCount ?? 0)

// 비디오 동기화
watch(() => props.video, (v) => {
    liked.value = !!v.liked
    likeCount.value = v.likeCount ?? 0
  }, { deep: false }
)

const onLikeClick = async () => {
  if (processing.value) return
  processing.value = true

  const prevLiked = liked.value
  const prevCount = likeCount.value

  liked.value = !prevLiked
  likeCount.value = prevCount ? Math.max(prevCount -1, 0) : prevCount + 1

  try {
    //서버 요청 (이전 클릭을 기준으로 다시 클릭할 시 보내는 요청을 다르게 한다)
    const res = prevLiked
    ? await store.unlikeVideo(props.video.id)
    : await store.likeVideo(props.video.id)

    if (res) {
      liked.value = !!res.liked
      likeCount.value = res.likeCount ?? likeCount.value
    }
  } catch(err) {
    liked.value = prevLiked // 에러시 이전 기록으로 복구
    likeCount.value = prevCount
    console.error('좋아요 토글 실패', err)
  } finally {
    processing.value = false
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
opacity : 불투명 정도
cursor-pointer : 마우스를 갖다대면 커서 모양이 바뀐다.
overflow-hidden : 넘치는 부분을

-->
