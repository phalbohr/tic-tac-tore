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
})
