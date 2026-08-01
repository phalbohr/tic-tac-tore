import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue'

function generateUUID(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export function usePendingMatches() {
  const pendingCount = ref(0)
  let lastFetchTime = 0
  const THROTTLE_MS = 10000

  async function fetchPendingCount(force = false): Promise<number> {
    const now = Date.now()
    if (!force && lastFetchTime > 0 && now - lastFetchTime < THROTTLE_MS) {
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

  async function rejectMatch(matchId: string, reason: string, customReason?: string): Promise<{ success: boolean; data?: any; error?: string }> {
    const trimmedReason = reason ? reason.trim() : ''
    const trimmedCustomReason = customReason ? customReason.trim() : ''
    const idempotencyKey = generateUUID()

    try {
      const res = await fetch(`/api/v1/matches/${matchId}/reject`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Idempotency-Key': idempotencyKey
        },
        body: JSON.stringify({ reason: trimmedReason, customReason: trimmedCustomReason })
      })

      if (res.ok) {
        const data = await res.json()
        await fetchPendingCount(true)
        return { success: true, data }
      } else {
        const errData = await res.json().catch(() => ({}))
        return { success: false, error: errData.message || 'Failed to reject match' }
      }
    } catch (e: any) {
      return { success: false, error: e.message || 'Network error' }
    }
  }

  return {
    pendingCount,
    fetchPendingCount,
    rejectMatch,
  }
}
