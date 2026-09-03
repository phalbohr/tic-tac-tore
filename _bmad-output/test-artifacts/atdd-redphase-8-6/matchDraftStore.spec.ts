import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMatchDraftStore, MatchType } from '@/features/match/stores/matchDraftStore'
import { useAuthStore } from '@/stores/auth'

describe('matchDraftStore ATDD Unit Tests (Tournament Rule Enforcement)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.profile = {
      id: 'creator-uuid',
      nickname: 'Creator',
      avatar: 'avatar1',
      defaultRuleConfigurationId: null,
      defaultGroupId: null,
    } as any
    vi.restoreAllMocks()
  })

  it('should detect isTournamentMatch when tournamentMatchId is set', () => {
    const store = useMatchDraftStore()
    expect(store.isTournamentMatch).toBe(false)

    store.setTournamentContext({
      tournamentId: 't-123',
      tournamentMatchId: 'tm-456',
      ruleConfigId: 'rule-789',
      ruleSystemName: 'Official Standard',
      matchType: MatchType.ONE_VS_ONE,
      playerIds: ['p-1', 'p-2'],
    })

    expect(store.isTournamentMatch).toBe(true)
    expect(store.tournamentId).toBe('t-123')
    expect(store.tournamentMatchId).toBe('tm-456')
    expect(store.ruleConfigurationId).toBe('rule-789')
    expect(store.ruleSystem).toBe('Official Standard')
    expect(store.selectedPlayers).toEqual(['p-1', 'p-2'])
  })

  it('should include ruleConfigId and tournamentMatchId in match creation payload', async () => {
    const store = useMatchDraftStore()
    store.setTournamentContext({
      tournamentId: 't-123',
      tournamentMatchId: 'tm-456',
      ruleConfigId: 'rule-789',
      ruleSystemName: 'Official Standard',
      matchType: MatchType.ONE_VS_ONE,
      playerIds: ['p-1', 'p-2'],
    })

    store.ruleConfig = { scoreLimit: 10, gameLimit: 3, winsNeeded: 2, winByTwo: false }
    store.beginScoreEntry()
    store.incrementScore(1, 10)
    store.incrementScore(2, 5)
    store.completeCurrentGame()
    store.incrementScore(1, 10)
    store.incrementScore(2, 8)
    store.completeCurrentGame()

    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => ({ id: 'match-created-uuid' }),
    } as Response)

    store.startSubmissionTimer()

    // Fast-forward or trigger immediate execution if timer
    // Check payload passed to submission
    expect(store.pendingSubmission).not.toBeNull()
    expect(store.pendingSubmission?.payload).toMatchObject({
      tournamentMatchId: 'tm-456',
      ruleConfigId: 'rule-789',
      teamAAttackerId: 'p-1',
      teamBAttackerId: 'p-2',
    })
  })
})
