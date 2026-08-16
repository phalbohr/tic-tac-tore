import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import OAuthRedirectHandler from '@/components/OAuthRedirectHandler.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
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
    {
      path: '/match/:id/review',
      name: 'match-review',
      component: () => import('@/features/match/views/MatchReviewStub.vue'),
    },
    {
      path: '/matches',
      name: 'matches',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/statistics/teams',
      name: 'team-statistics',
      component: () => import('@/features/stats/components/TeamStatsView.vue'),
    },
  ],
})

export default router
