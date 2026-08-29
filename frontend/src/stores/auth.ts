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
  id?: string
  nickname: string
  avatar: string
  language?: string
  tutorialCompleted?: boolean
  defaultGroupId?: string | null
  defaultRuleConfigurationId?: string | null
  poolNotificationsEnabled?: boolean
  version?: number
}


export const useAuthStore = defineStore('auth', () => {
  const isMaybeAuthenticated = ref(!!getCookie(SESSION_COOKIE_NAME))
  const profile = ref<UserProfile | null>(null)

  const isAuthenticated = computed(() => isMaybeAuthenticated.value)

  function setAuthenticated(status: boolean) {
    isMaybeAuthenticated.value = status
  }

  let fetchProfilePromise: Promise<void> | null = null

  async function fetchProfile() {
    if (!isMaybeAuthenticated.value) return
    if (fetchProfilePromise) return fetchProfilePromise
    fetchProfilePromise = (async () => {
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
      } finally {
        fetchProfilePromise = null
      }
    })()
    return fetchProfilePromise
  }

  async function updateProfile(options: {
    nickname?: string
    language?: string
    avatar?: string
    tutorialCompleted?: boolean
    defaultGroupId?: string | null
    defaultRuleConfigurationId?: string | null
    clearDefaultGroup?: boolean
    clearDefaultRuleConfiguration?: boolean
    poolNotificationsEnabled?: boolean
  }) {
    if (fetchProfilePromise) {
      await fetchProfilePromise
    }
    if (!profile.value) {
      await fetchProfile()
    }
    if (!profile.value) return

    const {
      nickname,
      language,
      avatar,
      tutorialCompleted,
      defaultGroupId,
      defaultRuleConfigurationId,
      clearDefaultGroup,
      clearDefaultRuleConfiguration,
      poolNotificationsEnabled
    } = options
    const previousProfile = { ...profile.value }

    const localeStore = useLocaleStore()
    const previousLocale = localeStore.locale

    // Optimistic UI updates
    let finalNickname = nickname
    if (nickname !== undefined) {
      const sanitized = nickname.replace(/[^a-zA-Z0-9]/g, '')
      if (sanitized.length === 0) {
        throw new Error('Nickname cannot be empty')
      }
      profile.value.nickname = sanitized
      finalNickname = sanitized
    }
    if (language !== undefined) {
      profile.value.language = language
      const lang = language.toLowerCase()
      if (lang === 'en' || lang === 'de') {
        localeStore.setLocale(lang as 'en' | 'de')
      }
    }
    if (avatar !== undefined) {
      profile.value.avatar = avatar
    }
    if (tutorialCompleted !== undefined) {
      profile.value.tutorialCompleted = tutorialCompleted
    }
    if (defaultGroupId !== undefined) {
      profile.value.defaultGroupId = defaultGroupId
    }
    if (defaultRuleConfigurationId !== undefined) {
      profile.value.defaultRuleConfigurationId = defaultRuleConfigurationId
    }
    if (poolNotificationsEnabled !== undefined) {
      profile.value.poolNotificationsEnabled = poolNotificationsEnabled
    }

    try {
      const csrfToken = getCookie(CSRF_COOKIE_NAME)
      const headers: HeadersInit = {
        'Content-Type': 'application/json',
      }
      if (csrfToken) {
        headers[CSRF_HEADER_NAME] = decodeURIComponent(csrfToken)
      }

      const response = await fetch(PROFILE_ENDPOINT, {
        method: 'PATCH',
        headers,
        body: JSON.stringify({
          nickname: finalNickname,
          language,
          avatar,
          tutorialCompleted,
          defaultGroupId,
          defaultRuleConfigurationId,
          poolNotificationsEnabled,
          clearDefaultGroup: clearDefaultGroup ?? (defaultGroupId === null ? true : undefined),
          clearDefaultRuleConfiguration: clearDefaultRuleConfiguration ?? (defaultRuleConfigurationId === null ? true : undefined)
        }),
      })

      if (!response.ok) {
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
        const lang = data.language.toLowerCase()
        if (lang === 'en' || lang === 'de') {
          localeStore.setLocale(lang as 'en' | 'de')
        }
      }
    } catch (e) {
      profile.value = previousProfile
      localeStore.setLocale(previousLocale)
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
        headers,
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

  async function deleteAccount() {
    try {
      const csrfToken = getCookie(CSRF_COOKIE_NAME)
      const headers: HeadersInit = {}
      if (csrfToken) {
        headers[CSRF_HEADER_NAME] = decodeURIComponent(csrfToken)
      }

      const response = await fetch(PROFILE_ENDPOINT, {
        method: 'DELETE',
        headers,
      })

      if (!response.ok) {
        let errorMessage = 'Failed to delete account'
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

      isMaybeAuthenticated.value = false
      profile.value = null
      deleteCookie(SESSION_COOKIE_NAME)
    } catch (e) {
      console.error('Failed to delete account', e)
      throw e
    }
  }

  return {
    isAuthenticated,
    profile,
    setAuthenticated,
    fetchProfile,
    updateProfile,
    clearToken,
    logout,
    deleteAccount,
  }
})
