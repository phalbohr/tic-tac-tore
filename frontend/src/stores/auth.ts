import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCookie } from '../utils/cookieUtils'

const SESSION_COOKIE_NAME = 'TTT_SESSION'
const CSRF_COOKIE_NAME = 'XSRF-TOKEN'
const CSRF_HEADER_NAME = 'X-XSRF-TOKEN'
const LOGOUT_ENDPOINT = '/api/auth/logout'
const METHOD_POST = 'POST'

export const useAuthStore = defineStore('auth', () => {
  const isMaybeAuthenticated = ref(!!getCookie(SESSION_COOKIE_NAME))

  const isAuthenticated = computed(() => isMaybeAuthenticated.value)

  function setAuthenticated(status: boolean) {
    isMaybeAuthenticated.value = status
  }

  async function logout() {
    try {
      const csrfToken = getCookie(CSRF_COOKIE_NAME)

      const headers: HeadersInit = {}
      if (csrfToken) {
        headers[CSRF_HEADER_NAME] = decodeURIComponent(csrfToken)
      }

      const response = await fetch(LOGOUT_ENDPOINT, { 
        method: METHOD_POST,
        headers 
      })

      if (!response.ok && response.status === 0) {
        console.warn('Offline logout — local state cleared, but server session may persist')
      }
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
