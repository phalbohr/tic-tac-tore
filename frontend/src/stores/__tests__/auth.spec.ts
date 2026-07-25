import { describe, it, expect, beforeEach, afterEach, vi, type Mock } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'

vi.mock('../locale', () => ({
  useLocaleStore: vi.fn(() => ({
    locale: 'en',
    setLocale: vi.fn()
  }))
}))

const SESSION_COOKIE_NAME = 'TTT_SESSION'

describe('useAuthStore', () => {
  let originalFetch: typeof global.fetch

  beforeEach(() => {
    document.cookie = `${SESSION_COOKIE_NAME}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
    setActivePinia(createPinia())
    originalFetch = global.fetch
    document.cookie = `${SESSION_COOKIE_NAME}=test_token; path=/`
  })

  afterEach(() => {
    global.fetch = originalFetch
    vi.restoreAllMocks()
  })

  describe('updateProfile', () => {
    it('[P1]rolls back profile using shallow copy on error', async () => {
      const store = useAuthStore()
      global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: () => Promise.resolve({ nickname: 'OldNick', avatar: 'old.png', tutorialCompleted: undefined })
      }) as Mock
      await store.fetchProfile()

      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: () => Promise.resolve({ message: 'Internal Server Error' })
      }) as Mock

      await expect(store.updateProfile({ nickname: 'NewNick' })).rejects.toThrow('Internal Server Error')

      expect(store.profile).toEqual({
        nickname: 'OldNick',
        avatar: 'old.png',
        tutorialCompleted: undefined
      })
    })

    it('throws error when nickname is empty string', async () => {
      const store = useAuthStore()
      store.profile = { nickname: 'old', avatar: '1' }

      await expect(store.updateProfile({ nickname: '' }))
        .rejects
        .toThrow('Nickname cannot be empty')
    })

    it('throws error when nickname is whitespace string', async () => {
      const store = useAuthStore()
      store.profile = { nickname: 'old', avatar: '1' }

      await expect(store.updateProfile({ nickname: '   ' }))
        .rejects
        .toThrow('Nickname cannot be empty')
    })
  })
})
