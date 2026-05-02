import { mount, flushPromises } from '@vue/test-utils'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import OAuthRedirectHandler from '@/components/OAuthRedirectHandler.vue'

const mockPush = vi.fn()

vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router')
  return {
    ...actual,
    useRoute: () => ({
      query: { token: 'mock.jwt.token' },
    }),
    useRouter: () => ({ push: mockPush }),
  }
})

describe('OAuthRedirectHandler', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    sessionStorage.clear()
    mockPush.mockReset()
  })

  it('stores JWT token from query param in localStorage', async () => {
    mount(OAuthRedirectHandler)
    await flushPromises()

    expect(localStorage.getItem('auth_token')).toBe('mock.jwt.token')
  })

  it('redirects to Home Hub after storing token', async () => {
    mount(OAuthRedirectHandler)
    await flushPromises()

    expect(mockPush).toHaveBeenCalledWith('/')
  })

  it('redirects to intent_url from sessionStorage when present', async () => {
    sessionStorage.setItem('intent_url', '/match/confirm/abc123')

    mount(OAuthRedirectHandler)
    await flushPromises()

    expect(mockPush).toHaveBeenCalledWith('/match/confirm/abc123')
  })
})
