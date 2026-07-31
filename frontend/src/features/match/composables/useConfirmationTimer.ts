import { ref, onUnmounted, getCurrentInstance } from 'vue'
import { getCookie } from '../../../utils/cookieUtils'

export interface PendingConfirmationPayload {
  matchId: string
  idempotencyKey?: string
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
  const countdown = ref<number>(15)
  const isPending = ref<boolean>(false)
  const isOfflinePending = ref<boolean>(false)
  const pendingConfirmation = ref<PendingConfirmationPayload | null>(null)

  let timerId: ReturnType<typeof setInterval> | null = null

  function startConfirmationTimer(matchId: string, idempotencyKey?: string) {
    clearTimer()
    const payload: PendingConfirmationPayload = {
      matchId,
      idempotencyKey: idempotencyKey || (typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : 'idempotency-key')
    }
    pendingConfirmation.value = payload
    countdown.value = 15
    isPending.value = true
    isOfflinePending.value = false

    timerId = setInterval(async () => {
      if (countdown.value > 1) {
        countdown.value--
      } else {
        clearTimer()
        countdown.value = 0
        try {
          const result = await commitCallback(payload)
          if (result === ConfirmationResult.SERVER_OR_NETWORK_ERROR) {
            isOfflinePending.value = true
          } else {
            isPending.value = false
            pendingConfirmation.value = null
            if (result === ConfirmationResult.SUCCESS) {
              onSuccess?.(payload)
            }
          }
        } catch {
          isOfflinePending.value = true
        }
      }
    }, 1000)
  }

  function cancelConfirmationTimer() {
    clearTimer()
    const saved = pendingConfirmation.value
    isPending.value = false
    isOfflinePending.value = false
    pendingConfirmation.value = null
    countdown.value = 15
    return saved
  }

  function clearTimer() {
    if (timerId !== null) {
      clearInterval(timerId)
      timerId = null
    }
  }

  if (getCurrentInstance()) {
    onUnmounted(() => {
      clearTimer()
    })
  }

  return {
    countdown,
    isPending,
    isOfflinePending,
    pendingConfirmation,
    startConfirmationTimer,
    cancelConfirmationTimer,
    clearTimer
  }
}
