import { setActivePinia, createPinia } from 'pinia';
import { useStatsStore } from '@/features/stats/stores/useStatsStore';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import * as statisticsService from '@/services/statisticsService';

vi.mock('@/services/statisticsService', () => ({
  getPersonalStats: vi.fn(),
  getTeamPairStats: vi.fn(),
}));

describe('[Story 4.4] useStatsStore - Team Pair Statistics (ATDD)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('[P0] should fetch and store paginated team pair statistics', async () => {
    const store = useStatsStore();
    const mockResponse = {
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
    };

    // If getTeamPairStats is implemented in store
    if (typeof (store as any).fetchTeamPairStats === 'function') {
      vi.mocked((statisticsService as any).getTeamPairStats).mockResolvedValue(mockResponse);
      await (store as any).fetchTeamPairStats();
      expect((store as any).teamPairStats).toEqual(mockResponse.content);
    } else {
      // Red phase assertion
      expect(typeof (store as any).fetchTeamPairStats).toBe('function');
    }
  });

  it('[P1] should pass filter parameters (playerId, period, minMatches) when fetching team pairs', async () => {
    const store = useStatsStore();
    if (typeof (store as any).fetchTeamPairStats === 'function') {
      const getTeamPairsMock = vi.fn().mockResolvedValue({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
      (statisticsService as any).getTeamPairStats = getTeamPairsMock;

      await (store as any).fetchTeamPairStats({
        playerId: 'p1',
        period: 'LAST_MONTH',
        minMatches: 5,
        page: 1,
        size: 20,
      });

      expect(getTeamPairsMock).toHaveBeenCalledWith({
        playerId: 'p1',
        period: 'LAST_MONTH',
        minMatches: 5,
        page: 1,
        size: 20,
      });
    } else {
      expect(typeof (store as any).fetchTeamPairStats).toBe('function');
    }
  });
});
