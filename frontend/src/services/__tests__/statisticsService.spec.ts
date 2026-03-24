import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getLeaderboard, getPersonalStats, type PlayerStats } from '../statisticsService'

describe('statisticsService', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('fetches personal stats correctly', async () => {
    const mockStats: PlayerStats = {
      playerId: 'user-uuid',
      playerName: 'Current Player',
      overall: { matches: 20, wins: 15, losses: 5, winRate: 75 },
      attacker: { matches: 10, wins: 8, losses: 2, winRate: 80 },
      defender: { matches: 10, wins: 7, losses: 3, winRate: 70 }
    }

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => mockStats
    } as Response)

    const result = await getPersonalStats({ 
      period: 'MONTHLY',
      token: 'test-token' 
    })

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/statistics/me?period=MONTHLY'),
      expect.objectContaining({
        headers: {
          'Authorization': 'Bearer test-token'
        }
      })
    )
    expect(result).toEqual(mockStats)
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
