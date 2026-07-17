import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi, Mock } from 'vitest'
import { useMatchDraftStore, MatchType } from './matchDraftStore'

describe('matchDraftStore', () => {
  let fetchMock: Mock

  beforeEach(() => {
    setActivePinia(createPinia())
    fetchMock = vi.fn()
    globalThis.fetch = fetchMock as unknown as typeof fetch
  })

  it('initializes with default values', () => {
    const store = useMatchDraftStore()
    expect(store.matchType).toBe(MatchType.ONE_VS_ONE)
    expect(store.selectedPlayers).toEqual([])
    expect(store.ruleSystem).toBe('STANDARD')
    expect(store.matchState).toBe('draft')
  })

  it('changes match type and truncates players if needed', () => {
    const store = useMatchDraftStore()
    store.setMatchType(MatchType.TWO_VS_TWO)
    store.addPlayer('p1')
    store.addPlayer('p2')
    store.addPlayer('p3')
    
    expect(store.matchType).toBe(MatchType.TWO_VS_TWO)
    expect(store.selectedPlayers.length).toBe(3)
    
    store.setMatchType(MatchType.ONE_VS_ONE)
    expect(store.selectedPlayers.length).toBe(2)
  })

  it('prevents adding empty players', () => {
    const store = useMatchDraftStore()
    store.addPlayer('   ')
    expect(store.selectedPlayers).toEqual([])
  })

  describe('Score Entry and Auto-Completion', () => {
    it('handles successful loadRuleConfig', async () => {
      fetchMock.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ scoreLimit: 5, gameLimit: 3, winsNeeded: 2 })
      })
      const store = useMatchDraftStore()
      await store.loadRuleConfig()
      expect(store.ruleConfig).toEqual({ scoreLimit: 5, gameLimit: 3, winsNeeded: 2 })
    })

    it('handles API error in loadRuleConfig by falling back to standard', async () => {
      fetchMock.mockResolvedValueOnce({
        ok: false
      })
      const store = useMatchDraftStore()
      await store.loadRuleConfig()
      expect(store.ruleConfig).toEqual({ scoreLimit: 10, gameLimit: 1, winsNeeded: 1 })
    })

    it('increments score but caps at scoreLimit', async () => {
      const store = useMatchDraftStore()
      // manually set config
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1 }
      store.incrementScore(1, 1)
      expect(store.currentGame.team1Score).toBe(1)
      
      store.incrementScore(1, 5) // Should cap at 5
      // Wait, checkAutoCompletion pushes game and resets currentGame.
      // If we reach limit, it resets to 0. So let's check games array.
      expect(store.games.length).toBe(1)
      expect(store.games[0].team1Score).toBe(5)
      expect(store.currentGame.team1Score).toBe(0)
    })

    it('decrements score but prevents negative', () => {
      const store = useMatchDraftStore()
      store.incrementScore(2, 2)
      expect(store.currentGame.team2Score).toBe(2)
      store.decrementScore(2)
      expect(store.currentGame.team2Score).toBe(1)
      store.decrementScore(2)
      store.decrementScore(2)
      expect(store.currentGame.team2Score).toBe(0)
    })

    it('auto-completes a game and starts next when scoreLimit is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2 }
      store.incrementScore(1, 5)
      expect(store.games.length).toBe(1)
      expect(store.games[0].team1Score).toBe(5)
      expect(store.currentGame.team1Score).toBe(0)
      expect(store.matchState).toBe('draft') // Still not ready for submission because winsNeeded = 2
    })

    it('auto-completes match when winsNeeded is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2 }
      
      // Game 1
      store.incrementScore(1, 5)
      expect(store.games.length).toBe(1)
      expect(store.matchState).toBe('draft')
      
      // Game 2
      store.incrementScore(1, 5)
      expect(store.games.length).toBe(2)
      expect(store.matchState).toBe('ready_for_submission')
    })
    
    it('auto-completes match when gameLimit is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 2, winsNeeded: 3 }
      
      // Game 1
      store.incrementScore(2, 5)
      expect(store.games.length).toBe(1)
      expect(store.matchState).toBe('draft')
      
      // Game 2
      store.incrementScore(2, 5)
      expect(store.games.length).toBe(2)
      expect(store.matchState).toBe('ready_for_submission')
    })
  })
})
