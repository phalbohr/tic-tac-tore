import { createRouter, createWebHistory } from 'vue-router'
import HomeHub from '@/views/HomeHub.vue'
import OAuthRedirectHandler from '@/components/OAuthRedirectHandler.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeHub,
    },
    {
      path: '/oauth2/redirect',
      name: 'oauth2-redirect',
      component: OAuthRedirectHandler,
    },
    {
      path: '/cabinet',
      name: 'cabinet',
      component: () => import('@/features/profile/Cabinet.vue'),
    },
    {
      path: '/live-match',
      name: 'live-match',
      component: () => import('@/features/match/LiveMatch.vue'),
    },
    {
      path: '/matches/new',
      name: 'new-match',
      component: () => import('@/components/RuleSystemSelection.vue'),
    },
  ],
})

export default router
