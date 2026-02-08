import { describe, it, expect, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import router from '../index'
import { useAuthStore } from '@/stores/auth'

describe('Router Navigation Guard', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    // Сбрасываем состояние роутера перед каждым тестом
    await router.push('/')
  })

  it('redirects to login when accessing a protected route without auth', async () => {
    const auth = useAuthStore()
    auth.logout() // Гарантируем, что пользователь не авторизован

    // Мы ожидаем, что маршрут '/about' будет защищен.
    // Пока мы не внесли изменения в router/index.ts, этот тест провалится,
    // так как переход на '/about' выполнится успешно.
    await router.push('/about')
    
    expect(router.currentRoute.value.name).toBe('login')
  })

  it('allows access to protected route when authenticated', async () => {
    const auth = useAuthStore()
    auth.login('fake-jwt-token', { id: 1, name: 'Test User', email: 'test@example.com' })

    await router.push('/about')
    
    expect(router.currentRoute.value.name).toBe('about')
  })

  it('allows access to public routes without auth', async () => {
      const auth = useAuthStore()
      auth.logout()

      await router.push('/')
      expect(router.currentRoute.value.name).toBe('home')
  })
})
