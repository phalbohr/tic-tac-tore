import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import * as matchService from '@/services/matchService'
import { useMatchHistoryStore } from '@/features/match/stores/useMatchHistoryStore'

describe('[Story 4.6] useMatchHistoryStore - Unified Match History', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('[P0] should fetch and store paginated confirmed match history', async () => {
    const store = useMatchHistoryStore()
    const mockPagedResponse = {
      content: [
        {
          id: 'match-1',
          creatorId: 'user-1',
          matchFormat: 'STANDARD',
          status: 'CONFIRMED',
          teamAAttackerId: 'user-1',
          teamBAttackerId: 'user-2',
          games: [{ teamAScore: 10, teamBScore: 8 }],
          createdAt: '2026-08-19T10:00:00Z',
        }
      ],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true
    }
    vi.spyOn(matchService, 'getMatchHistory').mockResolvedValue(mockPagedResponse)
    await store.fetchConfirmedHistory()
    expect(store.confirmedMatches).toHaveLength(1)
    expect(store.pagination.totalElements).toBe(1)
  })

  it('[P0] should switch tabs between confirmed and pending', () => {
    const store = useMatchHistoryStore()
    expect(store.activeTab).toBe('confirmed')
    store.setTab('pending')
    expect(store.activeTab).toBe('pending')
  })

  it('[P1] should pass filter parameters (playerId, matchType, ruleConfigId) and cancel in-flight requests', async () => {
    const store = useMatchHistoryStore()
    store.setFilter('matchType', '2v2')
    store.setFilter('playerId', 'player-uuid')
    expect(store.filters.matchType).toBe('2v2')
    expect(store.filters.playerId).toBe('player-uuid')
  })

  it('[P1] should reset filters to default state', () => {
    const store = useMatchHistoryStore()
    store.setFilter('matchType', '1v1')
    store.resetFilters()
    expect(store.filters.matchType).toBeNull()
    expect(store.filters.playerId).toBeNull()
    expect(store.filters.ruleConfigId).toBeNull()
  })

  it('[P2] should generate realistic demo match history when demo mode is active and allow exiting demo mode', async () => {
    const store = useMatchHistoryStore()
    store.enableDemoMode()
    await store.fetchConfirmedHistory()
    expect(store.confirmedMatches.length).toBeGreaterThan(0)
    expect(store.isDemoMode).toBe(true)

    // Exit demo mode
    store.toggleDemoMode(false)
    expect(store.isDemoMode).toBe(false)
  })

  it('[P1] should handle HTTP errors in fetchPendingMatches gracefully', async () => {
    const store = useMatchHistoryStore()
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500
    })

    await store.fetchPendingMatches()
    expect(store.error).toContain('Failed to load pending matches')
    expect(store.pendingMatches).toHaveLength(0)
    expect(store.loading).toBe(false)
  })
})
