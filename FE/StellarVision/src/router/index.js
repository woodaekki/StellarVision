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
import LiveStreamingListView from '@/views/Streaming/LiveStreamingListView.vue'
import ReplayStreamingListView from '@/views/Streaming/ReplayStreamingListView.vue'
import RoomView from '@/views/Streaming/RoomView.vue'
import CalenderView from '@/views/CalenderView.vue'
import BadgeView from '@/views/BadgeView.vue'
import ProfileEdit from '@/components/profile/ProfileEdit.vue'
import ProfileHeader from '@/components/profile/ProfileHeader.vue'
import PreRoomView from '@/views/Streaming/PreRoomView.vue'

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
          component: StreamingListView,
            children:[
              {
                path:'livestreaminglist',
                name:'LiveStreamingListView',
                component: LiveStreamingListView
              },
              {
                path:'replay',
                name: 'ReplayStreamingListView',
                component: ReplayStreamingListView
              },
              {
                path:'room/:id',
                name: 'RoomView',
                component: RoomView
              },
            ]
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
        path: 'edit',
        name: 'ProfileEdit',
        component: ProfileEdit
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
      path: '/profile/:id/mygallery/detail',
      name: 'MyGalleryListView',
      component: MyGalleryListView
    },
    {
      path: '/calender',
      name: CalenderView,
      component: CalenderView
    },
    {
      path: '/badge',
      name: 'BadgeView',
      component: BadgeView
    },
  ],
    }
  )

export default router
