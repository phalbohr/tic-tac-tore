import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/oauth2/redirect',
      name: 'oauth2-redirect',
      component: () => import('../views/OAuth2Redirect.vue'),
    },
    {
      path: '/dev-recording',
      name: 'dev-recording',
      component: () => import('../views/DevRecordingView.vue'),
    },
    {
      path: '/about',
      name: 'about',
      meta: { requiresAuth: true },
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    // Если маршрут требует авторизации и пользователь не вошел в систему,
    // перенаправляем его на страницу логина.
    next({ name: 'login' })
  } else if (to.name === 'login' && auth.isAuthenticated) {
    // Если пользователь уже авторизован и пытается зайти на страницу логина,
    // отправляем его на главную.
    next({ name: 'home' })
  } else {
    // В остальных случаях разрешаем переход.
    next()
  }
})

export default router
