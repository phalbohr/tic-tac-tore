import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/dev-recording', name: 'dev-recording', component: { template: 'DevRecording' } },
    { path: '/leaderboards', name: 'leaderboards', component: { template: 'Leaderboards' } }
  ]
})

describe('HomeView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders correctly when not authenticated', () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router]
      }
    })
    expect(wrapper.text()).toContain('Please use the navigation bar to login')
    expect(wrapper.find('router-link').exists()).toBe(false)
  })

  it('renders call to action buttons when authenticated', async () => {
    const authStore = useAuthStore()
    authStore.login('test-token', { id: '1', name: 'Test User', email: 'test@example.com' })
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router]
      }
    })
    
    const links = wrapper.findAllComponents({ name: 'RouterLink' })
    expect(links.some(l => l.text() === 'Record Match')).toBe(true)
    expect(links.some(l => l.text() === 'View Rankings')).toBe(true)
  })
})
