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
})
