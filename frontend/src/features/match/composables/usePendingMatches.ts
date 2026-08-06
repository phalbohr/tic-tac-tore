import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { getCookie, getCsrfHeaders } from '../../../utils/cookieUtils'

interface PendingMatchItem {
  id: string
  status?: string
  confirmedByOpponentIds?: string[]
  requiredConfirmations?: number
  cooldownExpiresAt?: string
}

function generateUUID(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    let r: number
    if (typeof crypto !== 'undefined' && typeof crypto.getRandomValues === 'function') {
      const array = new Uint8Array(1)
      crypto.getRandomValues(array)
      r = (array[0] ?? 0) % 16
    } else {
      r = (Math.random() * 16) | 0
    }
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export function usePendingMatches() {
  const pendingCount = ref(0)
  const partiallyConfirmedMatches = ref<PendingMatchItem[]>([])
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
        partiallyConfirmedMatches.value =
          data.matches?.filter((m: PendingMatchItem) => m.status === 'PARTIALLY_CONFIRMED') || []
      }
    } catch (e) {
      console.warn('Failed to fetch pending matches count:', e)
    }
    return pendingCount.value
  }

  function getPartiallyConfirmedCount(): number {
    return partiallyConfirmedMatches.value.length
  }

  async function confirmOpponent(matchId: string): Promise<{ success: boolean; data?: any; error?: string }> {
    const idempotencyKey = generateUUID()
    const headers: Record<string, string> = {
      'Idempotency-Key': idempotencyKey,
      ...getCsrfHeaders()
    }

    try {
      const res = await fetch(`/api/v1/matches/${matchId}/confirm`, {
        method: 'POST',
        headers,
        credentials: 'include'
      })

      if (res.ok) {
        const data = await res.json()
        await fetchPendingCount(true)
        return { success: true, data }
      } else {
        const errData = await res.json().catch(() => ({}))
        return { success: false, error: errData.message }
      }
    } catch (e: any) {
      return { success: false, error: e.message }
    }
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

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey,
      ...getCsrfHeaders()
    }

    try {
      const res = await fetch(`/api/v1/matches/${matchId}/reject`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ reason: trimmedReason, customReason: trimmedCustomReason })
      })

      if (res.ok) {
        const data = await res.json()
        await fetchPendingCount(true)
        return { success: true, data }
      } else {
        const errData = await res.json().catch(() => ({}))
        return { success: false, error: errData.message }
      }
    } catch (e: any) {
      return { success: false, error: e.message }
    }
  }

  async function deleteMatch(matchId: string): Promise<{ success: boolean; error?: string }> {
    const headers: Record<string, string> = { ...getCsrfHeaders() }

    try {
      const res = await fetch(`/api/v1/matches/${matchId}`, {
        method: 'DELETE',
        headers
      })

      if (res.ok || res.status === 204) {
        await fetchPendingCount(true)
        return { success: true }
      } else {
        const errData = await res.json().catch(() => ({}))
        return { success: false, error: errData.message }
      }
    } catch (e: any) {
      return { success: false, error: e.message }
    }
  }

  const collapsedMatchIds = ref<string[]>([])

  function collapseMatch(id: string) {
    if (!collapsedMatchIds.value.includes(id)) {
      collapsedMatchIds.value.push(id)
    }
  }

  function expandAllMatches() {
    collapsedMatchIds.value = []
  }

  function cleanupCollapsedMatches(activeIds: string[]) {
    collapsedMatchIds.value = collapsedMatchIds.value.filter((id) => activeIds.includes(id))
  }

  return {
    pendingCount,
    partiallyConfirmedMatches,
    getPartiallyConfirmedCount,
    fetchPendingCount,
    confirmOpponent,
    rejectMatch,
    deleteMatch,
    collapsedMatchIds,
    collapseMatch,
    expandAllMatches,
    cleanupCollapsedMatches
  }
}

