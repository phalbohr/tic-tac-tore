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
  const lastConfirmedMatchId = ref<string | null>(null)

  async function executeCommit(payload: PendingConfirmationPayload): Promise<ConfirmationResult> {
    const result = await defaultCommitConfirmation(payload)
    if (result === ConfirmationResult.CLIENT_ERROR) {
      confirmError.value = 'Failed to confirm match'
    }
    return result
  }

  function handleSuccess(payload: PendingConfirmationPayload) {
    lastConfirmedMatchId.value = payload.matchId
  }

  const {
    activeConfirmations,
    pendingConfirmationIds,
    countdown,
    isPending,
    isOfflinePending,
    pendingConfirmation,
    startConfirmationTimer,
    cancelConfirmationTimer,
    clearTimer
  } = useConfirmationTimer(executeCommit, handleSuccess)

  function commitConfirmation(
    matchId: string,
    matchNumberOrIdempotencyKey?: number | string,
    idempotencyKey?: string
  ) {
    confirmError.value = null
    startConfirmationTimer(matchId, matchNumberOrIdempotencyKey, idempotencyKey)
  }

  return {
    activeConfirmations,
    pendingConfirmationIds,
    countdown,
    isPending,
    isOfflinePending,
    pendingConfirmation,
    confirmError,
    lastConfirmedMatchId,
    commitConfirmation,
    startConfirmationTimer,
    cancelConfirmationTimer,
    clearTimer
  }
})
