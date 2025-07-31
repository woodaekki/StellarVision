import { createRouter, createWebHistory } from 'vue-router'
import LandingView from '@/views/LandingView.vue'
import LoginView from '@/views/LoginView.vue'
import SignupView from '@/views/SignupView.vue'
import ProfileView from '@/views/ProfileView.vue'
import MainView from '@/views/MainView.vue'
import StreamingListView from '@/views/StreamingListView.vue'
import MyGalleryView from '@/views/Profile/MyGalleryView.vue'
import MyGalleryListView from '@/views/Profile/MyGalleryListView.vue'
import MyVideoView from '@/views/Profile/MyVideoView.vue'
import MyVideoListView from '@/views/Profile/MyVideoListView.vue'
import LiveStreamingListView from '@/views/LiveStreamingListView.vue'
import ReplayStreamingListView from '@/views/ReplayStreamingListView.vue'
import RoomView from '@/views/RoomView.vue'
import CalenderView from '@/views/CalenderView.vue'
import BadgeView from '@/views/BadgeView.vue'
import ProfileEdit from '@/components/profile/ProfileEdit.vue'
import ProfileHeader from '@/components/profile/ProfileHeader.vue'
import axios from "axios"


const commonApi = axios.create({
  baseURL: 'http://127.0.0.1:8080',   //개발 단계에서 사용할 도메인
  timeout: 1000,
  headers: {'Content-Type' : 'application/json'}    //HTTP에 JSON 전달임을 명시
})


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'LandingView', component: LandingView,},
    { path: '/login', name: 'LoginView', component: LoginView, },
    { path: '/signup', name: 'SignUpView', component : SignupView, },
    {
      path: '/main',
      component: MainView,
      children: [
        {
          path: 'streaming',
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
              }
            ]
        },

      ]
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
          component: MyGalleryView
        },
        {
          path: 'myvideo',
          name: 'MyVideoView',
          component : MyVideoView
        },
      ]
    },
    {
      path: '/profile/:id/edit',
      name: 'ProfileEdit',
      component: ProfileEdit
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

    {
      path: '/mygallerylist',
      name: MyGalleryListView,
      component: MyGalleryListView
    },
    {
      path: '/myvideolist',
      name: MyVideoListView,
      component: MyVideoListView
    }
  ],
    }
  )

export default router
