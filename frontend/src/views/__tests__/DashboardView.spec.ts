import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../DashboardView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/dashboard', component: DashboardView }, { path: '/leaderboards', component: { template: 'Leaderboards' } }]
})

describe('DashboardView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    global.fetch = vi.fn()
  })

  it('renders loading state initially', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify([]), { status: 200 }))
    
    const wrapper = mount(DashboardView, {
      global: {
        plugins: [router]
      }
    })
    
    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('renders match approvals header', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify([]), { status: 200 }))

    const wrapper = mount(DashboardView, {
      global: {
        plugins: [router]
      }
    })
    
    expect(wrapper.find('h1').text()).toContain('Match Approvals')
  })

  it('fetches pending matches on mount', async () => {
    const authStore = useAuthStore()
    authStore.login('test-token', { id: '1', name: 'Test User', email: 'test@example.com' })
    
    const mockMatches = [{ id: 'match-1', creatorName: 'User A', status: 'PENDING', games: [] }]
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify(mockMatches), { status: 200 }))

    const wrapper = mount(DashboardView, {
      global: {
        plugins: [router]
      }
    })
    
    await vi.waitFor(() => {
        expect(fetch).toHaveBeenCalledWith(expect.stringContaining('/matches/pending'), expect.objectContaining({
            headers: { 'Authorization': 'Bearer test-token' }
        }))
    })
  })

  it('displays error message if fetch fails', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response('Error body', { status: 500 }))

    const wrapper = mount(DashboardView, {
      global: {
        plugins: [router]
      }
    })
    
    await vi.waitFor(() => {
        expect(wrapper.text()).toContain('Failed to fetch pending matches')
    })
  })
})
