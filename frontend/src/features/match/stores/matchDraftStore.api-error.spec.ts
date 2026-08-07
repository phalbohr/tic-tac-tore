import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, afterEach, vi, type Mock } from 'vitest'
import { useMatchDraftStore, MatchType } from './matchDraftStore'

describe('matchDraftStore API error handling', () => {
  let fetchMock: Mock

  const originalFetch = globalThis.fetch;
  beforeEach(() => {
    setActivePinia(createPinia())
    fetchMock = vi.fn()
    globalThis.fetch = fetchMock as unknown as typeof fetch
  })

  afterEach(() => {
    globalThis.fetch = originalFetch;
    vi.restoreAllMocks();
    vi.useRealTimers();
  })

  it('handles successful loadRuleConfig', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: true })
    })
    const store = useMatchDraftStore()
    await store.loadRuleConfig()
    expect(store.ruleConfig).toEqual({ scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: true })
  })

  it('handles API error in loadRuleConfig by falling back to standard', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: false
    })
    const store = useMatchDraftStore()
    await store.loadRuleConfig()
    expect(store.ruleConfig).toEqual({ scoreLimit: 10, gameLimit: 3, winsNeeded: 2, winByTwo: false })
  })

  it('executes HTTP POST and resets store when 15 seconds timer expires', async () => {
    const store = useMatchDraftStore()
    store.addPlayer('p1')
    store.addPlayer('p2')
    store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
    store.incrementScore(1, 5)
    store.completeCurrentGame()

    fetchMock.mockResolvedValueOnce({ ok: true })

    vi.useFakeTimers()
    store.startSubmissionTimer()
    expect(store.isPendingSubmission).toBe(true)

    vi.advanceTimersByTime(15000)

    await Promise.resolve()
    await Promise.resolve()

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/matches', expect.objectContaining({
      method: 'POST'
    }))
    expect(store.isPendingSubmission).toBe(false)
    expect(store.pendingSubmission).toBeNull()
  })

  it('displays rate-limit error banner when backend returns HTTP 429 with retryAfter', async () => {
    const store = useMatchDraftStore()
    store.addPlayer('p1')
    store.addPlayer('p2')
    store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
    store.incrementScore(1, 5)
    store.completeCurrentGame()

    fetchMock.mockResolvedValueOnce({
      status: 429,
      ok: false,
      json: async () => ({
        code: 'RATE_LIMIT_EXCEEDED',
        message: 'Rate limit exceeded: too many match submissions. Please try again in 42 seconds.',
        details: { retryAfter: 42 }
      })
    })

    vi.useFakeTimers()
    store.startSubmissionTimer()
    expect(store.isPendingSubmission).toBe(true)

    vi.advanceTimersByTime(15000)
    await vi.runAllTimersAsync()

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/matches', expect.objectContaining({
      method: 'POST'
    }))
    expect(store.isPendingSubmission).toBe(false)
    expect(store.pendingSubmission).toBeNull()
  })

  it('displays rate-limit error banner when backend returns HTTP 429 with retryAfter', async () => {
    const store = useMatchDraftStore()
    store.addPlayer('p1')
    store.addPlayer('p2')
    store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
    store.incrementScore(1, 5)
    store.completeCurrentGame()

    fetchMock.mockResolvedValueOnce({
      status: 429,
      ok: false,
      json: async () => ({
        code: 'RATE_LIMIT_EXCEEDED',
        message: 'Rate limit exceeded: too many match submissions. Please try again in 42 seconds.',
        details: { retryAfter: 42 }
      })
    })

    vi.useFakeTimers()
    store.startSubmissionTimer()
    expect(store.isPendingSubmission).toBe(true)

    vi.advanceTimersByTime(15000)
    await vi.runAllTimersAsync()

    expect(store.submitError).toContain('Rate limit exceeded')
    expect(store.submitError).toContain('42 seconds')
    expect(store.isPendingSubmission).toBe(false)
  })

  it('displays server error when backend returns HTTP 503', async () => {
    const store = useMatchDraftStore()
    store.addPlayer('p1')
    store.addPlayer('p2')
    store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
    store.incrementScore(1, 5)
    store.completeCurrentGame()

    fetchMock.mockResolvedValueOnce({
      status: 503,
      ok: false,
      json: async () => ({
        code: 'RATE_LIMIT_UNAVAILABLE',
        message: 'Redis unavailable during rate-limit check',
        details: { retryAfter: 0 }
      })
    })

    vi.useFakeTimers()
    store.startSubmissionTimer()
    expect(store.isPendingSubmission).toBe(true)

    vi.advanceTimersByTime(15000)
    await vi.runAllTimersAsync()

    expect(store.submitError).toContain('Redis unavailable')
    expect(store.isOfflinePending).toBe(true)
  })

  it('displays server error when backend returns HTTP 503', async () => {
    const store = useMatchDraftStore()
    store.addPlayer('p1')
    store.addPlayer('p2')
    store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
    store.incrementScore(1, 5)
    store.completeCurrentGame()

    fetchMock.mockResolvedValueOnce({
      status: 503,
      ok: false,
      json: async () => ({
        code: 'RATE_LIMIT_UNAVAILABLE',
        message: 'Redis unavailable during rate-limit check',
        details: { retryAfter: 0 }
      })
    })

    vi.useFakeTimers()
    store.startSubmissionTimer()
    expect(store.isPendingSubmission).toBe(true)

    vi.advanceTimersByTime(15000)
    await Promise.resolve()
    await Promise.resolve()
    await Promise.resolve()
    await Promise.resolve()

    expect(store.submitError).toContain('Redis unavailable')
    expect(store.isOfflinePending).toBe(true)
  })
})
