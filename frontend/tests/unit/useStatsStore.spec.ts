import { setActivePinia, createPinia } from 'pinia';
import { useStatsStore } from '@/features/stats/stores/useStatsStore';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import * as statisticsService from '@/services/statisticsService';

vi.mock('@/services/statisticsService', () => ({
  getPersonalStats: vi.fn(),
}));

describe('[Story 4.1] useStatsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('[P0] should show demo data implicitly when matches < 5 and not disabled', () => {
    const store = useStatsStore();
    store.confirmedMatchesCount = 4;
    expect(store.shouldShowDemoData).toBe(true);
  });

  it('[P0] should disable implicit demo data when explicit toggle is false', () => {
    localStorage.setItem('tictactore.demoModeEnabled', 'false');
    const store = useStatsStore();
    store.confirmedMatchesCount = 4;
    expect(store.shouldShowDemoData).toBe(false);
  });

  it('[P0] should auto-disable demo data when fetching real stats with >= 5 matches', async () => {
    const store = useStatsStore();
    vi.mocked(statisticsService.getPersonalStats).mockResolvedValue({
      playerId: 'test',
      playerName: 'Test',
      overall: { matches: 5, wins: 0, losses: 0, winRate: 0 },
      attacker: { matches: 0, wins: 0, losses: 0, winRate: 0 },
      defender: { matches: 0, wins: 0, losses: 0, winRate: 0 }
    });

    await store.fetchStats();
    
    expect(store.confirmedMatchesCount).toBe(5);
    expect(store.isDemoModeEnabled).toBe(false);
    expect(localStorage.getItem('tictactore.demoModeEnabled')).toBe('false');
  });

  it('[P1] should persist demo mode toggle to localStorage', () => {
    const store = useStatsStore();
    store.toggleDemoMode(true);
    expect(store.isDemoModeEnabled).toBe(true);
    expect(localStorage.getItem('tictactore.demoModeEnabled')).toBe('true');

    store.toggleDemoMode(false);
    expect(store.isDemoModeEnabled).toBe(false);
    expect(localStorage.getItem('tictactore.demoModeEnabled')).toBe('false');
  });
});
