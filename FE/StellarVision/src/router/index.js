import { createRouter, createWebHistory } from 'vue-router'
import LandingView from '@/views/LandingView.vue'
import LoginView from '@/views/LoginView.vue'
import SignupView from '@/views/SignupView.vue'
import ProfileView from '@/views/ProfileView.vue'
import ProfileEdit from '@/components/profile/ProfileEdit.vue'
import StreamingListView from '@/views/StreamingListView.vue'
import MyGalleryView from '@/views/MyGalleryView.vue'
import MyVideoView from '@/views/MyVideoView.vue'
import LiveStreamingListView from '@/views/LiveStreamingListView.vue'
import ReplayStreamingListView from '@/views/ReplayStreamingListView.vue'
import RoomView from '@/views/RoomView.vue'
import CalenderView from '@/views/CalenderView.vue'
import BadgeView from '@/views/BadgeView.vue'
import MainView from '@/views/MainView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'LandingView', component: LandingView,},
    { path: '/login', name: 'LoginView', component: LoginView, },
    { path: '/signup', name: 'SignUpView', component : SignupView, },

    {
      path: '/main',
      name: 'MainView',
      componen : MainView,
      children: [
        {
          path: 'streaming',
          component: StreamingListView,
          children: [
            { path: 'livestream', name: 'LiveStreamingListView',  component: LiveStreamingListView },
            { path: '/replay',     name: 'ReplayStreamingListView', component: ReplayStreamingListView },

            { path: 'room/:id',   name: 'RoomView',                component: RoomView, props: true },
            ]
        },


        {
          path: '/profile/:id',
          name: 'profileView',
          component: ProfileView,
          children: [
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

      ]
    }
      ],
    },
        { path: '/badge',   name: 'BadgeView',    component: BadgeView },
        { path: '/calendar', name: 'CalendarView', component: CalenderView },
  )

export default router
