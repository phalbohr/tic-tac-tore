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
        const contentType = response.headers.get('content-type')
        if (!contentType || !contentType.includes('application/json')) {
          throw new Error('Invalid server response format')
        }
        const data = await response.json()
        profile.value = data
        if (data.language) {
          const localeStore = useLocaleStore()
          const lang = data.language.toLowerCase()
          if (lang === 'en' || lang === 'de') {
            localeStore.setLocale(lang)
          }
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
    if (nickname !== undefined) {
      const sanitized = nickname.replace(/[^a-zA-Z0-9]/g, '')
      if (sanitized.length > 0) {
        profile.value.nickname = sanitized
      }
    }
    if (language !== undefined) {
      profile.value.language = language
      const localeStore = useLocaleStore()
      const lang = language.toLowerCase()
      if (lang === 'en' || lang === 'de') {
        localeStore.setLocale(lang)
      }
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
        const localeStore = useLocaleStore()
        const prevLang = (previousProfile.language || 'EN').toLowerCase()
        if (prevLang === 'en' || prevLang === 'de') {
          localeStore.setLocale(prevLang)
        }
        let errorMessage = 'Failed to update profile'
        const contentType = response.headers.get('content-type')
        if (contentType && contentType.includes('application/json')) {
          try {
            const errorData = await response.json()
            errorMessage = errorData.message || errorMessage
          } catch {
            // ignore
          }
        }
        throw new Error(errorMessage)
      }

      const contentType = response.headers.get('content-type')
      if (!contentType || !contentType.includes('application/json')) {
        throw new Error('Invalid server response format')
      }
      const data = await response.json()
      profile.value = data
      if (data.language) {
        const localeStore = useLocaleStore()
        const lang = data.language.toLowerCase()
        if (lang === 'en' || lang === 'de') {
          localeStore.setLocale(lang)
        }
      }
    } catch (e) {
      profile.value = previousProfile
      const localeStore = useLocaleStore()
      const prevLang = (previousProfile.language || 'EN').toLowerCase()
      if (prevLang === 'en' || prevLang === 'de') {
        localeStore.setLocale(prevLang)
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
