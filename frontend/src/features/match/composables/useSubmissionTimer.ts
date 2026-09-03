import { ref, onUnmounted, getCurrentInstance } from 'vue'

export interface PendingSubmissionPayload {
  idempotencyKey: string
  payload: Record<string, unknown>
}

export enum SubmissionResult {
  SUCCESS = 'SUCCESS',
  CLIENT_ERROR = 'CLIENT_ERROR',
  SERVER_OR_NETWORK_ERROR = 'SERVER_OR_NETWORK_ERROR',
}

export function useSubmissionTimer(
  commitCallback: (payload: PendingSubmissionPayload) => Promise<SubmissionResult>,
) {
  const countdown = ref<number>(15)
  const isPending = ref<boolean>(false)
  const isOfflinePending = ref<boolean>(false)
  const pendingSubmission = ref<PendingSubmissionPayload | null>(null)

  let timerId: ReturnType<typeof setInterval> | null = null

  function startTimer(payload: PendingSubmissionPayload) {
    clearTimer()
    pendingSubmission.value = payload
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
          if (result === SubmissionResult.SERVER_OR_NETWORK_ERROR) {
            isOfflinePending.value = true
          } else if (result === SubmissionResult.CLIENT_ERROR) {
            isPending.value = false
            pendingSubmission.value = null
            // For client errors we do NOT want to mark it as offline pending
            // We just clear the timer and let the caller handle the error state
          } else {
            isPending.value = false
            pendingSubmission.value = null
          }
        } catch {
          isOfflinePending.value = true
        }
      }
    }, 1000)
  }

  function cancelTimer() {
    clearTimer()
    const saved = pendingSubmission.value
    isPending.value = false
    isOfflinePending.value = false
    pendingSubmission.value = null
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
    pendingSubmission,
    startTimer,
    cancelTimer,
    clearTimer,
  }
}
