import { describe, it, expect, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import router from '../index'
import { useAuthStore } from '@/stores/auth'

describe('Router Navigation Guard', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    // Reset router state before each test
    await router.push('/')
  })

  it('redirects to login when accessing a protected route without auth', async () => {
    const auth = useAuthStore()
    auth.logout()

    await router.push('/dashboard')
    expect(router.currentRoute.value.name).toBe('login')
  })

  it('allows access to protected route when authenticated', async () => {
    const auth = useAuthStore()
    auth.login('fake-jwt-token', { id: '1', name: 'Test User', email: 'test@example.com' })

    await router.push('/dashboard')
    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('allows access to public routes without auth', async () => {
      const auth = useAuthStore()
      auth.logout()

      await router.push('/leaderboards')
      expect(router.currentRoute.value.name).toBe('leaderboards')
  })
})
