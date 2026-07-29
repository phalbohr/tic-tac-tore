import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  useConfirmationTimer,
  ConfirmationResult,
  defaultCommitConfirmation,
  type PendingConfirmationPayload
} from '../composables/useConfirmationTimer'

export const useMatchConfirmationStore = defineStore('matchConfirmation', () => {
  const confirmError = ref<string | null>(null)

  async function executeCommit(payload: PendingConfirmationPayload): Promise<ConfirmationResult> {
    const result = await defaultCommitConfirmation(payload)
    if (result === ConfirmationResult.CLIENT_ERROR) {
      confirmError.value = 'Failed to confirm match'
    }
    return result
  }

  const {
    countdown,
    isPending,
    isOfflinePending,
    pendingConfirmation,
    startConfirmationTimer,
    cancelConfirmationTimer,
    clearTimer
  } = useConfirmationTimer(executeCommit)

  function commitConfirmation(matchId: string, idempotencyKey?: string) {
    confirmError.value = null
    startConfirmationTimer(matchId, idempotencyKey)
  }

  return {
    countdown,
    isPending,
    isOfflinePending,
    pendingConfirmation,
    confirmError,
    commitConfirmation,
    startConfirmationTimer,
    cancelConfirmationTimer,
    clearTimer
  }
})
