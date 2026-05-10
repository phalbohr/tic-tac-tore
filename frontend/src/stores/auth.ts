import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCookie } from '../utils/cookieUtils'

export const useAuthStore = defineStore('auth', () => {
  // Security: XSS Exposure via LocalStorage - Removed token from localStorage.
  // The token is now stored in an HttpOnly cookie managed by the browser.
  const isMaybeAuthenticated = ref(false)

  const isAuthenticated = computed(() => isMaybeAuthenticated.value)

  function setAuthenticated(status: boolean) {
    isMaybeAuthenticated.value = status
  }

  async function logout() {
    try {
      // Извлекаем XSRF-TOKEN из куки, чтобы отправить его в заголовке
      const csrfToken = getCookie('XSRF-TOKEN')

      const headers: HeadersInit = {}
      if (csrfToken) {
        headers['X-XSRF-TOKEN'] = decodeURIComponent(csrfToken)
      }

      await fetch('/api/auth/logout', { 
        method: 'POST',
        headers 
      })
    } catch (e) {
      console.error('Logout failed', e)
    } finally {
      isMaybeAuthenticated.value = false
    }
  }

  function clearToken() {
    isMaybeAuthenticated.value = false
  }

  return { isAuthenticated, setAuthenticated, clearToken, logout }
})
