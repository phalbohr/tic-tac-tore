import { setActivePinia, createPinia } from 'pinia'
import { useStatsStore } from '@/features/stats/stores/useStatsStore'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import * as statisticsService from '@/services/statisticsService'

vi.mock('@/services/statisticsService', () => ({
  getPersonalStats: vi.fn(),
  getTeamPairStats: vi.fn(),
  getHeadToHeadStats: vi.fn(),
}))

describe('[Story 4.5] H2H Cross-Tab Matrix & Store (ATDD Red Phase)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('[P0] should fetch and populate head-to-head statistics in store for selected opponent', async () => {
    const store = useStatsStore()
    const mockH2HResponse = {
      opponent: {
        id: 'opp-1',
        nickname: 'RivalPlayer',
        avatarUrl: null,
      },
      matches: {
        with: { matches: 5, wins: 4, losses: 1, draws: 0, winRate: 80.0 },
        vs: { matches: 10, wins: 6, losses: 4, draws: 0, winRate: 60.0 },
      },
      games: {
        with: { gamesWon: 12, gamesLost: 5, totalGames: 17, winRate: 70.6 },
        vs: { gamesWon: 20, gamesLost: 15, totalGames: 35, winRate: 57.1 },
      },
      goals: {
        attackerVsDefender: { scored: 15, conceded: 8 },
        attackerVsAttacker: { scored: 10, conceded: 12 },
        defenderVsAttacker: { scored: 8, conceded: 14 },
        defenderVsDefender: { scored: 5, conceded: 4 },
      },
    }

    vi.mocked(statisticsService.getHeadToHeadStats).mockResolvedValue(mockH2HResponse)

    await store.fetchH2HStats('opp-1')

    expect(store.h2hStats).toEqual(mockH2HResponse)
  })

  it('[P1] should pass filtering parameters (period, ruleConfigId, matchType) when fetching H2H stats', async () => {
    const store = useStatsStore()
    const getH2HMock = vi.fn().mockResolvedValue(null)
    vi.mocked(statisticsService.getHeadToHeadStats).mockImplementation(getH2HMock)

    await store.fetchH2HStats('opp-1', {
      period: 'WEEKLY',
      ruleConfigId: 'rule-cfg-1',
      matchType: '2v2',
    })

    expect(getH2HMock).toHaveBeenCalledWith('opp-1', {
      period: 'WEEKLY',
      ruleConfigId: 'rule-cfg-1',
      matchType: '2v2',
    })
  })

  it('[P2] should generate demo H2H matrix when demo mode is active', async () => {
    const store = useStatsStore()
    store.toggleDemoMode(true)

    expect(store.h2hStats).toBeDefined()
    expect(store.h2hStats?.matches?.vs?.matches ?? store.h2hStats?.matches?.vs?.totalMatches).toBeGreaterThan(0)
  })

  it('[P3] should clear h2hStats immediately when starting a new fetch to prevent stale UI data', async () => {
    const store = useStatsStore()
    store.h2hStats = {
      opponent: { id: 'old-opp', nickname: 'OldOpponent' },
      matches: { with: {} as any, vs: {} as any },
      games: { with: {} as any, vs: {} as any },
      goals: {} as any,
    }

    let resolveFetch: (val: any) => void
    const fetchPromise = new Promise((resolve) => {
      resolveFetch = resolve
    })
    vi.mocked(statisticsService.getHeadToHeadStats).mockReturnValue(fetchPromise as any)

    const callPromise = store.fetchH2HStats('new-opp')

    expect(store.h2hStats).toBeNull()
    expect(store.isH2HLoading).toBe(true)

    resolveFetch!({
      opponent: { id: 'new-opp', nickname: 'NewOpponent' },
      matches: { with: { matches: 1 }, vs: { matches: 2 } },
      games: { with: {}, vs: {} },
      goals: {},
    })

    await callPromise
    expect(store.h2hStats?.opponent?.nickname).toBe('NewOpponent')
    expect(store.isH2HLoading).toBe(false)
  })
})
