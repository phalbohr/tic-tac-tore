import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChallengeItem, CreateChallengePayload, ChallengeActionResponse } from '@/services/challengeService'
import * as challengeService from '@/services/challengeService'

export const useChallengeStore = defineStore('challenge', () => {
  const incomingChallenges = ref<ChallengeItem[]>([])
  const outgoingChallenges = ref<ChallengeItem[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  async function fetchIncoming(): Promise<ChallengeItem[]> {
    isLoading.value = true
    error.value = null
    try {
      const items = await challengeService.getIncomingChallenges()
      incomingChallenges.value = items
      return items
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch incoming challenges'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function fetchOutgoing(): Promise<ChallengeItem[]> {
    isLoading.value = true
    error.value = null
    try {
      const items = await challengeService.getOutgoingChallenges()
      outgoingChallenges.value = items
      return items
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch outgoing challenges'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function createChallenge(payload: CreateChallengePayload): Promise<ChallengeItem> {
    isLoading.value = true
    error.value = null
    try {
      const item = await challengeService.createChallenge(payload)
      outgoingChallenges.value.unshift(item)
      return item
    } catch (err: any) {
      error.value = err.message || 'Failed to create challenge'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function acceptChallenge(id: string): Promise<ChallengeActionResponse> {
    isLoading.value = true
    error.value = null
    try {
      const res = await challengeService.acceptChallenge(id)
      incomingChallenges.value = incomingChallenges.value.filter((c) => c.id !== id)
      return res
    } catch (err: any) {
      error.value = err.message || 'Failed to accept challenge'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function declineChallenge(id: string): Promise<ChallengeActionResponse> {
    isLoading.value = true
    error.value = null
    try {
      const res = await challengeService.declineChallenge(id)
      incomingChallenges.value = incomingChallenges.value.filter((c) => c.id !== id)
      return res
    } catch (err: any) {
      error.value = err.message || 'Failed to decline challenge'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function cancelChallenge(id: string): Promise<ChallengeActionResponse> {
    isLoading.value = true
    error.value = null
    try {
      const res = await challengeService.cancelChallenge(id)
      outgoingChallenges.value = outgoingChallenges.value.filter((c) => c.id !== id)
      return res
    } catch (err: any) {
      error.value = err.message || 'Failed to cancel challenge'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  return {
    incomingChallenges,
    outgoingChallenges,
    isLoading,
    error,
    fetchIncoming,
    fetchOutgoing,
    createChallenge,
    acceptChallenge,
    declineChallenge,
    cancelChallenge,
  }
})
