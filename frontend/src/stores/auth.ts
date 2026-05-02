import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('auth_token'))

  const isAuthenticated = computed(() => token.value !== null)

  function setToken(jwt: string) {
    token.value = jwt
    localStorage.setItem('auth_token', jwt)
  }

  function clearToken() {
    token.value = null
    try { localStorage.removeItem('auth_token') } catch(e) { console.warn('localStorage disabled') }
  }

  return { token, isAuthenticated, setToken, clearToken }
})
