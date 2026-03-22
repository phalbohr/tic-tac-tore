import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { getLeaderboard } from '../statisticsService'

describe('statisticsService', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.stubGlobal('fetch', vi.fn())
  })

  it('fetches leaderboard data correctly', async () => {
    const mockData = {
      content: [
        {
          rank: 1,
          playerId: 'uuid-1',
          playerName: 'Player 1',
          totalMatches: 10,
          wins: 8,
          losses: 2,
          winRate: 80.0
        }
      ],
      totalPages: 1,
      totalElements: 1
    }

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => mockData
    } as Response)

    const result = await getLeaderboard({ type: 'OVERALL', page: 0, size: 10 })

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/leaderboard?type=OVERALL&page=0&size=10'),
      expect.any(Object)
    )
    expect(result).toEqual(mockData)
  })

  it('handles fetch failure', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: false,
      status: 500
    } as Response)

    await expect(getLeaderboard({})).rejects.toThrow('Failed to fetch leaderboard: 500')
  })
})
