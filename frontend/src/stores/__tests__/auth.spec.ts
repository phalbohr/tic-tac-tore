import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('initial state is unauthenticated', () => {
    const auth = useAuthStore()
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.user).toBeNull()
    expect(auth.token).toBeNull()
  })

  it('login action updates state', () => {
    const auth = useAuthStore()
    const user = { id: '1', name: 'Test User', email: 'test@example.com' }
    const token = 'fake-jwt-token'

    auth.login(token, user)

    expect(auth.isAuthenticated).toBe(true)
    expect(auth.user).toEqual(user)
    expect(auth.token).toBe(token)
  })

  it('logout action clears state', () => {
    const auth = useAuthStore()
    const user = { id: '1', name: 'Test User', email: 'test@example.com' }
    const token = 'fake-jwt-token'

    auth.login(token, user)
    auth.logout()

    expect(auth.isAuthenticated).toBe(false)
    expect(auth.user).toBeNull()
    expect(auth.token).toBeNull()
  })
})
