import { ref, reactive, computed, onUnmounted, getCurrentInstance } from 'vue'
import { getCookie } from '../../../utils/cookieUtils'

export interface PendingConfirmationPayload {
  matchId: string
  matchNumber?: number
  idempotencyKey?: string
}

export interface ActiveConfirmationItem {
  matchId: string
  matchNumber: number
  idempotencyKey: string
  countdown: number
  isPending: boolean
  isOfflinePending: boolean
  timerId: ReturnType<typeof setInterval> | null
}

export enum ConfirmationResult {
  SUCCESS = 'SUCCESS',
  CLIENT_ERROR = 'CLIENT_ERROR',
  SERVER_OR_NETWORK_ERROR = 'SERVER_OR_NETWORK_ERROR'
}

export async function defaultCommitConfirmation(payload: PendingConfirmationPayload): Promise<ConfirmationResult> {
  try {
    const csrfToken = getCookie('XSRF-TOKEN')
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    }
    if (payload.idempotencyKey) {
      headers['Idempotency-Key'] = payload.idempotencyKey
    }
    if (csrfToken) {
      headers['X-XSRF-TOKEN'] = csrfToken
    }

    const res = await fetch(`/api/v1/matches/${payload.matchId}/confirm`, {
      method: 'POST',
      headers
    })

    if (res.ok) {
      return ConfirmationResult.SUCCESS
    } else if (res.status >= 400 && res.status < 500) {
      return ConfirmationResult.CLIENT_ERROR
    } else {
      return ConfirmationResult.SERVER_OR_NETWORK_ERROR
    }
  } catch {
    return ConfirmationResult.SERVER_OR_NETWORK_ERROR
  }
}

export function useConfirmationTimer(
  commitCallback: (payload: PendingConfirmationPayload) => Promise<ConfirmationResult> = defaultCommitConfirmation,
  onSuccess?: (payload: PendingConfirmationPayload) => void
) {
  const activeConfirmations = ref<ActiveConfirmationItem[]>([])

  function startConfirmationTimer(
    matchId: string,
    matchNumberOrIdempotencyKey?: number | string,
    idempotencyKey?: string
  ) {
    let matchNumber = 1
    let key: string | undefined = idempotencyKey

    if (typeof matchNumberOrIdempotencyKey === 'number') {
      matchNumber = matchNumberOrIdempotencyKey
    } else if (typeof matchNumberOrIdempotencyKey === 'string') {
      key = matchNumberOrIdempotencyKey
    }

    // Clear any previous timer for the exact same matchId if re-triggered
    cancelConfirmationTimer(matchId)

    const finalKey = key || (typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : 'idempotency-key')

    const item = reactive<ActiveConfirmationItem>({
      matchId,
      matchNumber,
      idempotencyKey: finalKey,
      countdown: 15,
      isPending: true,
      isOfflinePending: false,
      timerId: null
    })

    item.timerId = setInterval(async () => {
      if (item.countdown > 1) {
        item.countdown--
      } else {
        if (item.timerId !== null) {
          clearInterval(item.timerId)
          item.timerId = null
        }
        item.countdown = 0
        const payload: PendingConfirmationPayload = {
          matchId: item.matchId,
          matchNumber: item.matchNumber,
          idempotencyKey: item.idempotencyKey
        }
        try {
          const result = await commitCallback(payload)
          if (result === ConfirmationResult.SERVER_OR_NETWORK_ERROR) {
            item.isOfflinePending = true
          } else {
            const idx = activeConfirmations.value.findIndex(c => c.matchId === matchId)
            if (idx !== -1) {
              activeConfirmations.value.splice(idx, 1)
            }
            if (result === ConfirmationResult.SUCCESS) {
              onSuccess?.(payload)
            }
          }
        } catch {
          item.isOfflinePending = true
        }
      }
    }, 1000)

    activeConfirmations.value.push(item)
  }

  function cancelConfirmationTimer(targetMatchId?: string): PendingConfirmationPayload | null {
    if (activeConfirmations.value.length === 0) return null

    let targetIdx = -1
    if (targetMatchId) {
      targetIdx = activeConfirmations.value.findIndex(item => item.matchId === targetMatchId)
    } else {
      targetIdx = activeConfirmations.value.length - 1
    }

    if (targetIdx === -1) return null

    const removed = activeConfirmations.value.splice(targetIdx, 1)[0]
    if (!removed) return null

    if (removed.timerId !== null) {
      clearInterval(removed.timerId)
      removed.timerId = null
    }

    return {
      matchId: removed.matchId,
      matchNumber: removed.matchNumber,
      idempotencyKey: removed.idempotencyKey
    }
  }

  function clearTimer() {
    activeConfirmations.value.forEach(item => {
      if (item.timerId !== null) {
        clearInterval(item.timerId)
        item.timerId = null
      }
    })
    activeConfirmations.value = []
  }

  const isPending = computed(() => activeConfirmations.value.some(i => i.isPending && !i.isOfflinePending))
  const isOfflinePending = computed(() => activeConfirmations.value.some(i => i.isOfflinePending))
  const pendingConfirmation = computed<PendingConfirmationPayload | null>(() => {
    if (activeConfirmations.value.length === 0) return null
    const first = activeConfirmations.value[0]
    if (!first) return null
    return {
      matchId: first.matchId,
      matchNumber: first.matchNumber,
      idempotencyKey: first.idempotencyKey
    }
  })
  const countdown = computed<number>(() => {
    const first = activeConfirmations.value[0]
    return first ? first.countdown : 15
  })
  const pendingConfirmationIds = computed<string[]>(() => {
    return activeConfirmations.value.map(i => i.matchId)
  })

  if (getCurrentInstance()) {
    onUnmounted(() => {
      clearTimer()
    })
  }

  return {
    activeConfirmations,
    pendingConfirmationIds,
    countdown,
    isPending,
    isOfflinePending,
    pendingConfirmation,
    startConfirmationTimer,
    cancelConfirmationTimer,
    clearTimer
  }
}
