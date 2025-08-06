import { createRouter, createWebHistory } from 'vue-router'
import LandingView from '@/views/LandingView.vue'
import LoginView from '@/views/LoginView.vue'
import SignupView from '@/views/SignupView.vue'
import ProfileView from '@/views/ProfileView.vue'
import MainView from '@/views/MainView.vue'
import StreamingListView from '@/views/Streaming/StreamingListView.vue'
import MyGalleryView from '@/views/Profile/MyGalleryView.vue'
import MyGalleryListView from '@/views/Profile/MyGalleryListView.vue'
import MyVideoView from '@/views/Profile/MyVideoView.vue'
import MyVideoListView from '@/views/Profile/MyVideoListView.vue'
import RoomView from '@/views/Streaming/RoomView.vue'
import CalenderView from '@/views/CalenderView.vue'
import BadgeView from '@/views/BadgeView.vue'
import ProfileEdit from '@/components/profile/ProfileEdit.vue'
import ProfileHeader from '@/components/profile/ProfileHeader.vue'
import PreRoomView from '@/views/Streaming/PreRoomView.vue'
import ReplayView from '@/views/Streaming/ReplayView.vue'
import UpdateReplayView from '@/views/Streaming/UpdateReplayView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'LandingView', component: LandingView,},
    { path: '/login', name: 'LoginView', component: LoginView, },
    { path: '/signup', name: 'SignUpView', component : SignupView, },
    {
      path: '/main',
      component: MainView,
    },
    {
      path: '/streaming',
      name: 'StreamingListView',
      component: StreamingListView,
    },
    {
      path:'/room/:id',
      name: 'RoomView',
      component: RoomView
    },
    {
      path:'/pre',
      name:'PreRoomView',
      component:PreRoomView
    },
    {
      path: '/profile/:id',
      name: 'profileView',
      component: ProfileView,
      children: [
        {
          path: '',
          name: 'ProfileHeader',
          component: ProfileHeader
        },
        {
          path: 'mygallery',
          name: 'MyGalleryView',
          component: MyGalleryView,
        },
        {
          path: 'myvideo',
          name: 'MyVideoView',
          component : MyVideoView,
        },
      ]
    },
    {
      path: '/profile/:id/edit',
      name: 'ProfileEdit',
      component: ProfileEdit
    },
    {
      path: '/profile/:id/mygallery/detail',
      name: 'MyGalleryListView',
      component: MyGalleryListView
    },
    {
      path: '/profile/:id/myvideo/detail',
      name: 'MyVideoListView',
      component: MyVideoListView
    },
    {
      path: '/calender',
      name: 'CalenderView',
      component: CalenderView
    },
    {
      path: '/badge',
      name: 'BadgeView',
      component: BadgeView
    },
    {
      path:'/replay/:id', // 다시보기 영상도 가급적 다른 ui가 없어야 해서 children이 아닌 개별로 뺐습니다
      name: 'ReplayView',
      component: ReplayView
    },
    {
      path:'/replay/:id/edit',
      name: 'UpdateReplayView',
      component: UpdateReplayView
    },
  ],
    }
  )

export default router
