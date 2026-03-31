import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', component: HomeView }, { path: '/login', component: { template: 'Login' } }]
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
    expect(wrapper.text()).toContain('Not Authenticated')
    expect(wrapper.find('button').text()).toBe('Go to Login')
  })

  it('renders token panel when authenticated', async () => {
    const authStore = useAuthStore()
    authStore.login('test-token', { id: '1', name: 'Test User', email: 'test@example.com' })
    
    const wrapper = mount(HomeView, {
      global: {
        plugins: [router]
      }
    })
    
    expect(wrapper.text()).toContain('Your JWT Token')
    expect(wrapper.find('textarea').element.value).toBe('test-token')
  })

  it('calls copyToClipboard when copy button is clicked', async () => {
    const authStore = useAuthStore()
    authStore.login('test-token', { id: '1', name: 'Test User', email: 'test@example.com' })

    // Mock clipboard API
    const writeTextSpy = vi.fn().mockResolvedValue(undefined)
    Object.assign(navigator, {
      clipboard: {
        writeText: writeTextSpy
      }
    })

    const wrapper = mount(HomeView, {
      global: {
        plugins: [router]
      }
    })

    await wrapper.find('button.bg-gray-900').trigger('click')
    expect(writeTextSpy).toHaveBeenCalledWith('test-token')
  })
})
