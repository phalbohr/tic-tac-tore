import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getLeaderboard, getPersonalStats, getH2HStats, searchPlayers, type PlayerStats } from '../statisticsService'

describe('statisticsService', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('fetches H2H stats correctly', async () => {
    const mockH2H = {
      content: [
        {
          opponentId: 'opp-1',
          opponentName: 'Rival 1',
          matches: 5,
          wins: 3,
          losses: 2,
          winRate: 60.0
        }
      ],
      totalPages: 1,
      totalElements: 1,
      size: 10,
      number: 0
    }

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => mockH2H
    } as Response)

    const result = await getH2HStats({ 
      period: 'YEARLY',
      token: 'test-token' 
    })

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/statistics/h2h?period=YEARLY'),
      expect.objectContaining({
        headers: {
          'Authorization': 'Bearer test-token'
        }
      })
    )
    expect(result).toEqual(mockH2H)
  })

  it('handles fallback for non-paged H2H stats response', async () => {
    const mockH2HArray = [
      {
        opponentId: 'opp-1',
        opponentName: 'Rival 1',
        matches: 5,
        wins: 3,
        losses: 2,
        winRate: 60.0
      },
      {
        opponentId: 'opp-2',
        opponentName: 'Rival 2',
        matches: 10,
        wins: 5,
        losses: 5,
        winRate: 50.0
      }
    ]

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => mockH2HArray
    } as Response)

    const result = await getH2HStats({
      period: 'ALL_TIME',
      token: 'test-token'
    })

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/statistics/h2h?period=ALL_TIME'),
      expect.objectContaining({
        headers: {
          'Authorization': 'Bearer test-token'
        }
      })
    )
    expect(result).toEqual({
      content: mockH2HArray,
      totalPages: 1,
      totalElements: 2,
      size: 2,
      number: 0
    })
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
      totalElements: 1,
      size: 10,
      number: 0
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

  it('handles fetch failure with invalid JSON body', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: async () => {
        throw new Error('Invalid JSON')
      }
    } as unknown as Response)

    await expect(getLeaderboard({})).rejects.toThrow('API error: 400')
  })

  it('searches players correctly and handles signal and token', async () => {
    const mockPlayers = [{ id: 'p1', nickname: 'Alex', avatar: 'avatar1' }]
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => mockPlayers
    } as Response)

    const controller = new AbortController()
    const result = await searchPlayers('Alex', { signal: controller.signal, token: 'auth-token' })

    expect(fetch).toHaveBeenCalledWith(
      '/api/users/me/players/search?q=Alex',
      expect.objectContaining({
        signal: controller.signal,
        headers: {
          'Authorization': 'Bearer auth-token'
        }
      })
    )
    expect(result).toEqual(mockPlayers)
  })

  it('throws error when searchPlayers fails', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => ({ message: 'Search failure' })
    } as Response)

    await expect(searchPlayers('test')).rejects.toThrow('Search failure')
  })
})
