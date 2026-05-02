import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // Security: XSS Exposure via LocalStorage - Removed token from localStorage.
  // The token is now stored in an HttpOnly cookie managed by the browser.
  const isMaybeAuthenticated = ref(false)

  const isAuthenticated = computed(() => isMaybeAuthenticated.value)

  function setAuthenticated(status: boolean) {
    isMaybeAuthenticated.value = status
  }

  function clearToken() {
    isMaybeAuthenticated.value = false
  }

  return { isAuthenticated, setAuthenticated, clearToken }
})
