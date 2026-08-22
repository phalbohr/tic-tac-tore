import { ref, shallowRef, computed, getCurrentScope, onScopeDispose } from 'vue'

export function useWakeLock() {
  const isSupported = computed(() => {
    return (
      typeof window !== 'undefined' &&
      typeof navigator !== 'undefined' &&
      'wakeLock' in navigator &&
      Boolean(navigator.wakeLock?.request)
    )
  })

  const isActive = ref(false)
  const sentinel = shallowRef<WakeLockSentinel | null>(null)

  const handleVisibilityChange = async () => {
    if (
      typeof document !== 'undefined' &&
      document.visibilityState === 'visible' &&
      isActive.value
    ) {
      await request()
    }
  }

  const request = async (): Promise<boolean> => {
    if (!isSupported.value) {
      console.warn('Screen Wake Lock API is not supported in this environment')
      isActive.value = false
      return false
    }

    try {
      const lock = await navigator.wakeLock.request('screen')
      sentinel.value = lock
      isActive.value = true

      lock.addEventListener('release', () => {
        if (sentinel.value === lock) {
          sentinel.value = null
        }
      })

      return true
    } catch (err) {
      console.warn('Screen Wake Lock request failed:', err)
      isActive.value = false
      sentinel.value = null
      return false
    }
  }

  const release = async (): Promise<void> => {
    isActive.value = false
    if (sentinel.value) {
      try {
        await sentinel.value.release()
      } catch (err) {
        console.warn('Screen Wake Lock release failed:', err)
      } finally {
        sentinel.value = null
      }
    }
  }

  if (typeof document !== 'undefined') {
    document.addEventListener('visibilitychange', handleVisibilityChange)
  }

  const cleanup = () => {
    if (typeof document !== 'undefined') {
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
    if (sentinel.value) {
      release().catch(() => {})
    }
  }

  if (getCurrentScope()) {
    onScopeDispose(cleanup)
  }

  return {
    isSupported,
    isActive,
    sentinel,
    request,
    release,
    cleanup,
  }
}
