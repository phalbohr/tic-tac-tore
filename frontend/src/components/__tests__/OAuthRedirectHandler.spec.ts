import { mount, flushPromises } from '@vue/test-utils'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import OAuthRedirectHandler from '@/components/OAuthRedirectHandler.vue'
import { useAuthStore } from '@/stores/auth'

const mockPush = vi.fn()

vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router')
  return {
    ...actual,
    useRoute: () => ({
      query: { }, // Security: JWT Leaked in URL - Token removed from query
    }),
    useRouter: () => ({ push: mockPush }),
  }
})

describe('OAuthRedirectHandler', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
    mockPush.mockReset()
  })

  it('marks user as authenticated in auth store', async () => {
    const authStore = useAuthStore()
    mount(OAuthRedirectHandler)
    await flushPromises()

    expect(authStore.isAuthenticated).toBe(true)
  })

  it('redirects to Home Hub', async () => {
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
