import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useInsightStore } from '../useInsightStore'
import * as insightService from '@/services/insightService'

vi.mock('@/services/insightService', () => ({
  getPlayerInsights: vi.fn(),
  getMyInsights: vi.fn(),
}))

describe('useInsightStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetches personal insights and updates state', async () => {
    const mockData: insightService.PlayerInsightsResponse = {
      playerId: 'user-1',
      totalCount: 1,
      insights: [
        {
          id: 'insight-1',
          type: 'WIN_STREAK',
          category: 'STREAK',
          importance: 'HIGH',
          titleKey: 'insights.winStreak.title',
          descriptionKey: 'insights.winStreak.description',
          params: { streak: 4 },
          icon: 'local_fire_department',
          drillDownUrl: null,
        },
      ],
    }
    vi.mocked(insightService.getMyInsights).mockResolvedValueOnce(mockData)

    const store = useInsightStore()
    await store.fetchInsights()

    expect(insightService.getMyInsights).toHaveBeenCalled()
    expect(store.insights).toEqual(mockData.insights)
    expect(store.topInsights).toEqual(mockData.insights)
    expect(store.latestCelebrationInsight).toEqual(mockData.insights[0])
    expect(store.isLoading).toBe(false)
  })

  it('fetches specific player insights when playerId is provided', async () => {
    const mockData: insightService.PlayerInsightsResponse = {
      playerId: 'player-123',
      totalCount: 1,
      insights: [
        {
          id: 'insight-2',
          type: 'POSITIONAL_MASTERY',
          category: 'POSITION',
          importance: 'MEDIUM',
          titleKey: 'insights.positionalMastery.title',
          descriptionKey: 'insights.positionalMastery.description',
          params: { favoredPosition: 'Attacker' },
          icon: 'sports_score',
          drillDownUrl: null,
        },
      ],
    }
    vi.mocked(insightService.getPlayerInsights).mockResolvedValueOnce(mockData)

    const store = useInsightStore()
    await store.fetchInsights('player-123')

    expect(insightService.getPlayerInsights).toHaveBeenCalledWith('player-123')
    expect(store.insights).toEqual(mockData.insights)
  })

  it('dismisses celebration insight by id', async () => {
    const mockData: insightService.PlayerInsightsResponse = {
      playerId: 'user-1',
      totalCount: 1,
      insights: [
        {
          id: 'insight-celeb',
          type: 'WIN_STREAK',
          category: 'STREAK',
          importance: 'HIGH',
          titleKey: 'insights.winStreak.title',
          descriptionKey: 'insights.winStreak.description',
          params: { streak: 5 },
          icon: 'local_fire_department',
          drillDownUrl: null,
        },
      ],
    }
    vi.mocked(insightService.getMyInsights).mockResolvedValueOnce(mockData)

    const store = useInsightStore()
    await store.fetchInsights()

    expect(store.latestCelebrationInsight).not.toBeNull()
    store.dismissCelebration('insight-celeb')
    expect(store.latestCelebrationInsight).toBeNull()
  })

  it('handles errors during fetch', async () => {
    vi.mocked(insightService.getMyInsights).mockRejectedValueOnce(new Error('Network error'))

    const store = useInsightStore()
    await store.fetchInsights()

    expect(store.error).toBe('Network error')
    expect(store.insights).toEqual([])
    expect(store.isLoading).toBe(false)
  })
})
