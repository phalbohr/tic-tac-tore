import { describe, it, expect, beforeEach, vi, type Mock } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'
import { useLocaleStore } from '../locale'

vi.mock('../locale', () => ({
  useLocaleStore: vi.fn(() => ({
    locale: 'en',
    setLocale: vi.fn()
  }))
}))

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('updateProfile', () => {
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
