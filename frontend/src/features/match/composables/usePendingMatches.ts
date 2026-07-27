import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue'

export function usePendingMatches() {
  const pendingCount = ref(0)
  let lastFetchTime = 0
  const THROTTLE_MS = 10000

  async function fetchPendingCount(): Promise<number> {
    const now = Date.now()
    if (lastFetchTime > 0 && now - lastFetchTime < THROTTLE_MS) {
      return pendingCount.value
    }
    lastFetchTime = now

    try {
      const res = await fetch('/api/v1/matches/pending')
      if (res.ok) {
        const data = await res.json()
        pendingCount.value = typeof data.count === 'number' ? data.count : (data.matches?.length || 0)
      }
    } catch (e) {
      console.warn('Failed to fetch pending matches count:', e)
    }
    return pendingCount.value
  }

  function handleVisibilityChange() {
    if (document.visibilityState === 'visible') {
      fetchPendingCount()
    }
  }

  if (getCurrentInstance()) {
    onMounted(() => {
      fetchPendingCount()
      if (typeof window !== 'undefined') {
        document.addEventListener('visibilitychange', handleVisibilityChange)
      }
    })

    onUnmounted(() => {
      if (typeof window !== 'undefined') {
        document.removeEventListener('visibilitychange', handleVisibilityChange)
      }
    })
  }

  return {
    pendingCount,
    fetchPendingCount,
  }
}
