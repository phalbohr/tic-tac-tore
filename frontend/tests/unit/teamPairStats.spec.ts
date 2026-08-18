import { setActivePinia, createPinia } from 'pinia'
import { useStatsStore } from '@/features/stats/stores/useStatsStore'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import * as statisticsService from '@/services/statisticsService'

vi.mock('@/services/statisticsService', () => ({
  getPersonalStats: vi.fn(),
  getTeamPairStats: vi.fn(),
}))

describe('[Story 4.4] useStatsStore - Team Pair Statistics (ATDD)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('[P0] should fetch and store paginated team pair statistics', async () => {
    const store = useStatsStore()
    const mockResponse: statisticsService.Page<statisticsService.TeamPairStats> = {
      content: [
        {
          attackerId: 'p1',
          attackerName: 'Alice',
          defenderId: 'p2',
          defenderName: 'Bob',
          matches: 10,
          wins: 8,
          losses: 2,
          winRate: 80.0,
        },
      ],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
    }

    vi.mocked(statisticsService.getTeamPairStats).mockResolvedValue(mockResponse)
    await store.fetchTeamPairStats()
    expect(store.teamPairStats).toEqual(mockResponse.content)
  })

  it('[P1] should pass filter parameters (playerId, period, minMatches) when fetching team pairs', async () => {
    const store = useStatsStore()
    const getTeamPairsMock = vi.fn().mockResolvedValue({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 })
    vi.mocked(statisticsService.getTeamPairStats).mockImplementation(getTeamPairsMock)

    await store.fetchTeamPairStats({
      playerId: 'p1',
      period: 'LAST_MONTH',
      minMatches: 5,
      page: 1,
      size: 20,
    })

    expect(getTeamPairsMock).toHaveBeenCalledWith({
      playerId: 'p1',
      period: 'LAST_MONTH',
      minMatches: 5,
      page: 1,
      size: 20,
    })
  })

  it('should restore realTeamPairPage when demo mode is toggled off', async () => {
    const store = useStatsStore()
    const mockResponse: statisticsService.Page<statisticsService.TeamPairStats> = {
      content: [
        {
          attackerId: 'p1',
          attackerName: 'Alice',
          defenderId: 'p2',
          defenderName: 'Bob',
          matches: 10,
          wins: 8,
          losses: 2,
          winRate: 80.0,
        },
      ],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
    }

    vi.mocked(statisticsService.getTeamPairStats).mockResolvedValue(mockResponse)
    await store.fetchTeamPairStats()

    expect(store.teamPairStats).toEqual(mockResponse.content)
    expect(store.teamPairPage).toEqual(mockResponse)

    // Toggle demo mode ON
    store.toggleDemoMode(true)
    expect(store.isDemoModeEnabled).toBe(true)
    expect(store.teamPairStats.length).toBeGreaterThan(1)

    // Toggle demo mode OFF
    store.toggleDemoMode(false)
    expect(store.isDemoModeEnabled).toBe(false)
    expect(store.teamPairStats).toEqual(mockResponse.content)
    expect(store.teamPairPage).toEqual(mockResponse)
  })
})
