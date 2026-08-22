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
  let requestPromise: Promise<boolean> | null = null

  const handleVisibilityChange = async () => {
    if (typeof document === 'undefined') return
    if (document.visibilityState === 'hidden') {
      sentinel.value = null
    } else if (document.visibilityState === 'visible' && isActive.value) {
      await request()
    }
  }

  const request = async (): Promise<boolean> => {
    if (!isSupported.value) {
      console.warn('Screen Wake Lock API is not supported in this environment')
      isActive.value = false
      return false
    }

    if (sentinel.value && !sentinel.value.released) {
      isActive.value = true
      return true
    }

    if (requestPromise) {
      return requestPromise
    }

    isActive.value = true

    requestPromise = (async () => {
      try {
        const lock = await navigator.wakeLock.request('screen')

        if (!isActive.value) {
          await lock.release().catch(() => {})
          return false
        }

        if (sentinel.value && sentinel.value !== lock && !sentinel.value.released) {
          await sentinel.value.release().catch(() => {})
        }

        sentinel.value = lock

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
      } finally {
        requestPromise = null
      }
    })()

    return requestPromise
  }

  const release = async (): Promise<void> => {
    isActive.value = false
    if (sentinel.value) {
      const currentSentinel = sentinel.value
      sentinel.value = null
      try {
        await currentSentinel.release()
      } catch (err) {
        console.warn('Screen Wake Lock release failed:', err)
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
    release().catch(() => {})
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
