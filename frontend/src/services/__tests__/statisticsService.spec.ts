import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getLeaderboard } from '../statisticsService'

describe('statisticsService', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('fetches leaderboard data correctly with token', async () => {
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

    const result = await getLeaderboard({ 
      type: 'OVERALL', 
      page: 0, 
      size: 10,
      token: 'test-token' 
    })

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/statistics/leaderboard?type=OVERALL&page=0&size=10'),
      expect.objectContaining({
        headers: {
          'Authorization': 'Bearer test-token'
        }
      })
    )
    expect(result).toEqual(mockData)
  })

  it('handles fetch failure and parses error message', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: async () => ({ message: 'Invalid parameters' })
    } as Response)

    await expect(getLeaderboard({})).rejects.toThrow('Invalid parameters')
  })
})
