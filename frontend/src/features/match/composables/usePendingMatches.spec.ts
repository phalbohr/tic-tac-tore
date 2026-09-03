import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { usePendingMatches } from './usePendingMatches'

describe('usePendingMatches', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.clearAllMocks()
  })

  it('[P0] should fetch pending match count from GET /api/v1/matches/pending', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ count: 3, matches: [] }),
    })
    vi.stubGlobal('fetch', mockFetch)

    const { fetchPendingCount, pendingCount } = usePendingMatches()
    await fetchPendingCount()

    expect(mockFetch).toHaveBeenCalledWith('/api/v1/matches/pending')
    expect(pendingCount.value).toBe(3)
  })

  it('[P1] should populate partiallyConfirmedMatches when fetch includes PARTIALLY_CONFIRMED status', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({
          count: 2,
          matches: [
            { id: 'm1', status: 'PENDING_APPROVAL' },
            { id: 'm2', status: 'PARTIALLY_CONFIRMED' },
            { id: 'm3', status: 'PARTIALLY_CONFIRMED' },
          ],
        }),
    })
    vi.stubGlobal('fetch', mockFetch)

    const { fetchPendingCount, partiallyConfirmedMatches, getPartiallyConfirmedCount } =
      usePendingMatches()
    await fetchPendingCount()

    expect(partiallyConfirmedMatches.value).toHaveLength(2)
    expect(partiallyConfirmedMatches.value.map((m: any) => m.id)).toEqual(['m2', 'm3'])
    expect(getPartiallyConfirmedCount()).toBe(2)
  })

  it('[P1] should carry cooldownExpiresAt on partially confirmed matches from API response', async () => {
    const futureExpiry = new Date(Date.now() + 3600_000).toISOString()
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({
          count: 1,
          matches: [
            { id: 'm-cooldown', status: 'PARTIALLY_CONFIRMED', cooldownExpiresAt: futureExpiry },
          ],
        }),
    })
    vi.stubGlobal('fetch', mockFetch)

    const { fetchPendingCount, partiallyConfirmedMatches } = usePendingMatches()
    await fetchPendingCount()

    expect(partiallyConfirmedMatches.value).toHaveLength(1)
    expect(partiallyConfirmedMatches.value[0]!.cooldownExpiresAt).toBe(futureExpiry)
  })

  it('[P1] should throttle visibility change refresh with a 10-second debounce', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ count: 1, matches: [] }),
    })
    vi.stubGlobal('fetch', mockFetch)

    const { fetchPendingCount } = usePendingMatches()
    await fetchPendingCount()
    await fetchPendingCount() // Second call within throttle window

    expect(mockFetch).toHaveBeenCalledTimes(1)
  })

  it('[P1] should bypass throttle when force is true', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ count: 2, matches: [] }),
    })
    vi.stubGlobal('fetch', mockFetch)

    const { fetchPendingCount } = usePendingMatches()
    await fetchPendingCount()
    await fetchPendingCount(true)

    expect(mockFetch).toHaveBeenCalledTimes(2)
  })

  it('[P1] should retain previous count when fetch fails', async () => {
    const mockFetch = vi.fn().mockRejectedValue(new Error('Network error'))
    vi.stubGlobal('fetch', mockFetch)

    const { fetchPendingCount, pendingCount } = usePendingMatches()
    pendingCount.value = 5
    await fetchPendingCount()

    expect(pendingCount.value).toBe(5)
  })

  it('[P0] should send unique idempotency key and X-XSRF-TOKEN when crypto.randomUUID and cookie are present', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ id: 'match-1', status: 'REJECTED' }),
    })
    vi.stubGlobal('fetch', mockFetch)
    vi.stubGlobal('crypto', { randomUUID: () => '12345678-1234-4234-8234-1234567890ab' })
    document.cookie = 'XSRF-TOKEN=test-csrf-token; path=/'

    const { rejectMatch } = usePendingMatches()
    const result = await rejectMatch('match-1', 'Wrong score', 'Detailed reason')

    expect(result.success).toBe(true)
    expect(mockFetch).toHaveBeenCalledWith('/api/v1/matches/match-1/reject', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': '12345678-1234-4234-8234-1234567890ab',
        'X-XSRF-TOKEN': 'test-csrf-token',
      },
      body: JSON.stringify({ reason: 'Wrong score', customReason: 'Detailed reason' }),
    })
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
  })

  it('[P0] should send unique generated idempotency key when crypto.randomUUID is not available', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ id: 'match-2', status: 'REJECTED' }),
    })
    vi.stubGlobal('fetch', mockFetch)
    vi.stubGlobal('crypto', undefined)

    const { rejectMatch } = usePendingMatches()
    const result1 = await rejectMatch('match-2', 'Wrong score')
    const result2 = await rejectMatch('match-2', 'Wrong score')
    const rejectCalls = mockFetch.mock.calls.filter(
      (call) => typeof call[0] === 'string' && call[0].includes('/reject'),
    )
    const opts1 = rejectCalls[0]?.[1] as { headers?: Record<string, string> } | undefined
    const opts2 = rejectCalls[1]?.[1] as { headers?: Record<string, string> } | undefined
    const key1 = opts1?.headers?.['Idempotency-Key']
    const key2 = opts2?.headers?.['Idempotency-Key']

    expect(result1.success).toBe(true)
    expect(result2.success).toBe(true)
    expect(key1).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/)
    expect(key2).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/)
    expect(key1).not.toBe(key2)
  })

  it('should return error message when server responds with non-ok error payload or undefined if absent', async () => {
    const mockFetchWithError = vi.fn().mockResolvedValue({
      ok: false,
      json: () => Promise.resolve({ message: 'Already processed' }),
    })
    vi.stubGlobal('fetch', mockFetchWithError)

    const { rejectMatch } = usePendingMatches()
    const res1 = await rejectMatch('m1', 'Wrong score')
    expect(res1.success).toBe(false)
    expect(res1.error).toBe('Already processed')

    const mockFetchWithoutMsg = vi.fn().mockResolvedValue({
      ok: false,
      json: () => Promise.reject(new Error('SyntaxError')),
    })
    vi.stubGlobal('fetch', mockFetchWithoutMsg)
    const res2 = await rejectMatch('m2', 'Wrong score')
    expect(res2.success).toBe(false)
    expect(res2.error).toBeUndefined()
  })

  it('[P1] should send POST to /api/v1/matches/{id}/confirm with Idempotency-Key header and refresh count', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ id: 'match-1', status: 'CONFIRMED' }),
    })
    vi.stubGlobal('fetch', mockFetch)
    vi.stubGlobal('crypto', { randomUUID: () => 'confirm-uuid-123' })

    const { confirmOpponent, pendingCount } = usePendingMatches()
    pendingCount.value = 2
    const result = await confirmOpponent('match-1')

    expect(result.success).toBe(true)
    expect(result.data.status).toBe('CONFIRMED')

    const confirmCall = mockFetch.mock.calls.find(
      (call) => typeof call[0] === 'string' && call[0].includes('/confirm'),
    )
    expect(confirmCall).toBeDefined()
    expect(confirmCall![0]).toBe('/api/v1/matches/match-1/confirm')
    expect(confirmCall![1].method).toBe('POST')
    expect(confirmCall![1].headers['Idempotency-Key']).toBe('confirm-uuid-123')
  })
})
