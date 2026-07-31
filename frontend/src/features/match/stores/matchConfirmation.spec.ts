import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, afterEach, vi, type Mock } from 'vitest'
import { useMatchConfirmationStore } from './matchConfirmationStore'

describe('matchConfirmationStore', () => {
  let fetchMock: Mock
  const originalFetch = globalThis.fetch

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
    fetchMock = vi.fn()
    globalThis.fetch = fetchMock as unknown as typeof fetch
  })

  afterEach(() => {
    globalThis.fetch = originalFetch
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  it('starts 15-second confirmation timer when commitConfirmation is called', () => {
    const store = useMatchConfirmationStore()
    store.commitConfirmation('match-123', 'idempotency-abc')

    expect(store.isPending).toBe(true)
    expect(store.countdown).toBe(15)
    expect(store.pendingConfirmation).toEqual({
      matchId: 'match-123',
      matchNumber: 1,
      idempotencyKey: 'idempotency-abc'
    })
  })

  it('cancels confirmation timer and restores pending request state', () => {
    const store = useMatchConfirmationStore()
    store.commitConfirmation('match-123', 'idempotency-abc')
    expect(store.isPending).toBe(true)

    const saved = store.cancelConfirmationTimer()

    expect(store.isPending).toBe(false)
    expect(store.pendingConfirmation).toBeNull()
    expect(saved).toEqual({
      matchId: 'match-123',
      matchNumber: 1,
      idempotencyKey: 'idempotency-abc'
    })
  })

  it('handles multiple active confirmation timers simultaneously', () => {
    const store = useMatchConfirmationStore()
    store.commitConfirmation('match-1', 1, 'key-1')
    store.commitConfirmation('match-2', 2, 'key-2')

    expect(store.activeConfirmations).toHaveLength(2)
    expect(store.pendingConfirmationIds).toEqual(['match-1', 'match-2'])
    expect(store.activeConfirmations[0]?.matchNumber).toBe(1)
    expect(store.activeConfirmations[1]?.matchNumber).toBe(2)
  })

  it('decrements countdown second by second for active confirmations', () => {
    const store = useMatchConfirmationStore()
    store.commitConfirmation('match-1', 1, 'key-1')

    expect(store.activeConfirmations[0]?.countdown).toBe(15)
    vi.advanceTimersByTime(1000)
    expect(store.activeConfirmations[0]?.countdown).toBe(14)
    vi.advanceTimersByTime(2000)
    expect(store.activeConfirmations[0]?.countdown).toBe(12)
  })

  it('cancels specific confirmation timer by matchId', () => {
    const store = useMatchConfirmationStore()
    store.commitConfirmation('match-1', 1, 'key-1')
    store.commitConfirmation('match-2', 2, 'key-2')

    const canceled = store.cancelConfirmationTimer('match-1')

    expect(canceled?.matchId).toBe('match-1')
    expect(store.activeConfirmations).toHaveLength(1)
    expect(store.pendingConfirmationIds).toEqual(['match-2'])
  })

  it('dispatches POST /api/v1/matches/{id}/confirm when 15s timer reaches 0', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ status: 'CONFIRMED' })
    })

    const store = useMatchConfirmationStore()
    store.commitConfirmation('match-123', 'idempotency-abc')

    vi.advanceTimersByTime(15000)
    await vi.runAllTimersAsync()

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/matches/match-123/confirm', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': 'idempotency-abc'
      }
    })
    expect(store.isPending).toBe(false)
    expect(store.lastConfirmedMatchId).toBe('match-123')
  })

  it('sets isOfflinePending when network failure occurs upon timer expiration', async () => {
    fetchMock.mockRejectedValueOnce(new Error('Network error'))

    const store = useMatchConfirmationStore()
    store.commitConfirmation('match-123', 'idempotency-abc')

    vi.advanceTimersByTime(15000)
    await vi.runAllTimersAsync()

    expect(store.isOfflinePending).toBe(true)
  })
})
