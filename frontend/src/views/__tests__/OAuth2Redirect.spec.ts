import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import OAuth2Redirect from '../OAuth2Redirect.vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter } from 'vue-router'

// Создаем общую мок-функцию для push
const pushMock = vi.fn()

// Мокаем зависимости vue-router
vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
  useRouter: vi.fn(() => ({
    push: pushMock
  }))
}))

describe('OAuth2Redirect', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('logs in and redirects when token is present in query', () => {
    const token = 'test-token'
    // Настраиваем мок useRoute для возврата токена
    ;(useRoute as any).mockReturnValue({
      query: { token }
    })
    
    const auth = useAuthStore()
    const loginSpy = vi.spyOn(auth, 'login')

    mount(OAuth2Redirect)

    expect(loginSpy).toHaveBeenCalledWith(token, expect.any(Object))
    expect(pushMock).toHaveBeenCalledWith('/')
  })

  it('redirects to login when token is missing', () => {
    ;(useRoute as any).mockReturnValue({
      query: {}
    })
    
    mount(OAuth2Redirect)

    expect(pushMock).toHaveBeenCalledWith('/login')
  })
})
