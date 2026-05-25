import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCookie, deleteCookie } from '../utils/cookieUtils'
import { useLocaleStore } from './locale'

const SESSION_COOKIE_NAME = 'TTT_SESSION'
const CSRF_COOKIE_NAME = 'XSRF-TOKEN'
const CSRF_HEADER_NAME = 'X-XSRF-TOKEN'
const LOGOUT_ENDPOINT = '/api/auth/logout'
const PROFILE_ENDPOINT = '/api/v1/profile/me'
const METHOD_POST = 'POST'

interface UserProfile {
  nickname: string
  avatar: string
  language?: string
}

export const useAuthStore = defineStore('auth', () => {
  const isMaybeAuthenticated = ref(!!getCookie(SESSION_COOKIE_NAME))
  const profile = ref<UserProfile | null>(null)

  const isAuthenticated = computed(() => isMaybeAuthenticated.value)

  function setAuthenticated(status: boolean) {
    isMaybeAuthenticated.value = status
  }

  async function fetchProfile() {
    if (!isMaybeAuthenticated.value) return
    try {
      const response = await fetch(PROFILE_ENDPOINT)
      if (response.ok) {
        const data = await response.json()
        profile.value = data
        if (data.language) {
          const localeStore = useLocaleStore()
          localeStore.setLocale(data.language.toLowerCase() as any)
        }
      } else {
        isMaybeAuthenticated.value = false
        profile.value = null
        deleteCookie(SESSION_COOKIE_NAME)
      }
    } catch (e) {
      console.error('Failed to fetch profile', e)
      isMaybeAuthenticated.value = false
      profile.value = null
      deleteCookie(SESSION_COOKIE_NAME)
    }
  }

  async function updateProfile(nickname?: string, language?: string) {
    if (!profile.value) return

    const previousProfile = { ...profile.value }

    // Optimistic UI updates
    if (nickname !== undefined) profile.value.nickname = nickname
    if (language !== undefined) {
      profile.value.language = language
      const localeStore = useLocaleStore()
      localeStore.setLocale(language.toLowerCase() as any)
    }

    try {
      const csrfToken = getCookie(CSRF_COOKIE_NAME)
      const headers: HeadersInit = {
        'Content-Type': 'application/json'
      }
      if (csrfToken) {
        headers[CSRF_HEADER_NAME] = decodeURIComponent(csrfToken)
      }

      const response = await fetch(PROFILE_ENDPOINT, {
        method: 'PATCH',
        headers,
        body: JSON.stringify({ nickname, language })
      })

      if (!response.ok) {
        profile.value = previousProfile
        if (previousProfile.language) {
          const localeStore = useLocaleStore()
          localeStore.setLocale(previousProfile.language.toLowerCase() as any)
        }
        const errorText = await response.text()
        let errorMessage = 'Failed to update profile'
        try {
          const errorData = JSON.parse(errorText)
          errorMessage = errorData.message || errorMessage
        } catch {
          // ignore
        }
        throw new Error(errorMessage)
      }

      const data = await response.json()
      profile.value = data
      if (data.language) {
        const localeStore = useLocaleStore()
        localeStore.setLocale(data.language.toLowerCase() as any)
      }
    } catch (e) {
      profile.value = previousProfile
      if (previousProfile.language) {
        const localeStore = useLocaleStore()
        localeStore.setLocale(previousProfile.language.toLowerCase() as any)
      }
      throw e
    }
  }

  async function logout() {
    isMaybeAuthenticated.value = false
    profile.value = null
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
      profile.value = null
    }
  }

  function clearToken() {
    isMaybeAuthenticated.value = false
    profile.value = null
  }

  return { isAuthenticated, profile, setAuthenticated, fetchProfile, updateProfile, clearToken, logout }
})
