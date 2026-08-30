import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, afterEach, vi, type Mock } from 'vitest'
import { useChallengeStore } from '@/features/challenge/stores/useChallengeStore'

describe('useChallengeStore', () => {
  let fetchMock: Mock
  const originalFetch = globalThis.fetch

  beforeEach(() => {
    setActivePinia(createPinia())
    fetchMock = vi.fn()
    globalThis.fetch = fetchMock as unknown as typeof fetch
  })

  afterEach(() => {
    globalThis.fetch = originalFetch
    vi.restoreAllMocks()
  })

  it('initializes with default empty state', () => {
    const store = useChallengeStore()

    expect(store.incomingChallenges).toEqual([])
    expect(store.outgoingChallenges).toEqual([])
    expect(store.isLoading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetchIncoming() populates incomingChallenges', async () => {
    const mockChallenges = [
      {
        id: 'challenge-1',
        challengerId: 'user-1',
        challengerNickname: 'Alice',
        targetPlayerId: 'user-me',
        matchType: 'ONE_VS_ONE',
        status: 'PENDING',
        createdAt: '2026-08-30T10:00:00Z',
      },
    ]

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => mockChallenges,
    })

    const store = useChallengeStore()
    const result = await store.fetchIncoming()

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/challenges/incoming', expect.anything())
    expect(result).toEqual(mockChallenges)
    expect(store.incomingChallenges).toEqual(mockChallenges)
  })

  it('fetchOutgoing() populates outgoingChallenges', async () => {
    const mockChallenges = [
      {
        id: 'challenge-2',
        challengerId: 'user-me',
        challengerNickname: 'Me',
        targetPlayerId: 'user-2',
        matchType: 'TWO_VS_TWO',
        status: 'PENDING',
        createdAt: '2026-08-30T11:00:00Z',
      },
    ]

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => mockChallenges,
    })

    const store = useChallengeStore()
    const result = await store.fetchOutgoing()

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/challenges/outgoing', expect.anything())
    expect(result).toEqual(mockChallenges)
    expect(store.outgoingChallenges).toEqual(mockChallenges)
  })

  it('createChallenge() sends POST request and adds challenge to outgoing list', async () => {
    const createdChallenge = {
      id: 'challenge-3',
      challengerId: 'user-me',
      challengerNickname: 'Me',
      targetPlayerId: 'user-3',
      matchType: 'ONE_VS_ONE',
      status: 'PENDING',
      createdAt: '2026-08-30T12:00:00Z',
    }

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => createdChallenge,
    })

    const store = useChallengeStore()
    const result = await store.createChallenge({
      targetPlayerId: 'user-3',
      matchType: 'ONE_VS_ONE',
      message: 'Game on!',
    })

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/challenges', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({
        targetPlayerId: 'user-3',
        matchType: 'ONE_VS_ONE',
        message: 'Game on!',
      }),
    })
    expect(result.id).toBe('challenge-3')
    expect(store.outgoingChallenges).toContainEqual(createdChallenge)
  })

  it('acceptChallenge() removes challenge from incoming list', async () => {
    const store = useChallengeStore()
    store.incomingChallenges = [
      {
        id: 'challenge-accept-1',
        challengerId: 'user-1',
        challengerNickname: 'Alice',
        targetPlayerId: 'user-me',
        matchType: 'ONE_VS_ONE',
        status: 'PENDING',
        createdAt: '2026-08-30T10:00:00Z',
      },
    ]

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ challengeId: 'challenge-accept-1', status: 'ACCEPTED', message: 'Accepted' }),
    })

    await store.acceptChallenge('challenge-accept-1')

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/challenges/challenge-accept-1/accept', expect.objectContaining({
      method: 'POST',
    }))
    expect(store.incomingChallenges).toHaveLength(0)
  })

  it('declineChallenge() removes challenge from incoming list', async () => {
    const store = useChallengeStore()
    store.incomingChallenges = [
      {
        id: 'challenge-decline-1',
        challengerId: 'user-1',
        challengerNickname: 'Alice',
        targetPlayerId: 'user-me',
        matchType: 'ONE_VS_ONE',
        status: 'PENDING',
        createdAt: '2026-08-30T10:00:00Z',
      },
    ]

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ challengeId: 'challenge-decline-1', status: 'DECLINED', message: 'Declined' }),
    })

    await store.declineChallenge('challenge-decline-1')

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/challenges/challenge-decline-1/decline', expect.objectContaining({
      method: 'POST',
    }))
    expect(store.incomingChallenges).toHaveLength(0)
  })

  it('cancelChallenge() removes challenge from outgoing list', async () => {
    const store = useChallengeStore()
    store.outgoingChallenges = [
      {
        id: 'challenge-cancel-1',
        challengerId: 'user-me',
        challengerNickname: 'Me',
        targetPlayerId: 'user-2',
        matchType: 'ONE_VS_ONE',
        status: 'PENDING',
        createdAt: '2026-08-30T10:00:00Z',
      },
    ]

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ challengeId: 'challenge-cancel-1', status: 'CANCELLED', message: 'Cancelled' }),
    })

    await store.cancelChallenge('challenge-cancel-1')

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/challenges/challenge-cancel-1/cancel', expect.objectContaining({
      method: 'POST',
    }))
    expect(store.outgoingChallenges).toHaveLength(0)
  })
})
