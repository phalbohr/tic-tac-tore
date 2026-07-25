import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, afterEach, vi, type Mock } from 'vitest'
import { useMatchDraftStore, MatchType } from './matchDraftStore'

describe('matchDraftStore', () => {
  let fetchMock: Mock

  const originalFetch = globalThis.fetch;
  beforeEach(() => {
    setActivePinia(createPinia())
    fetchMock = vi.fn()
    globalThis.fetch = fetchMock as unknown as typeof fetch
  })
  
  afterEach(() => {
    globalThis.fetch = originalFetch;
    vi.restoreAllMocks();
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

  describe('Score Entry and Manual Completion', () => {
    it('handles successful loadRuleConfig', async () => {
      fetchMock.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: true })
      })
      const store = useMatchDraftStore()
      await store.loadRuleConfig()
      expect(store.ruleConfig).toEqual({ scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: true })
    })

    it('handles API error in loadRuleConfig by falling back to standard', async () => {
      fetchMock.mockResolvedValueOnce({
        ok: false
      })
      const store = useMatchDraftStore()
      await store.loadRuleConfig()
      expect(store.ruleConfig).toEqual({ scoreLimit: 10, gameLimit: 3, winsNeeded: 2, winByTwo: false })
    })

    it('increments score but caps at scoreLimit, requires manual complete', async () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
      store.incrementScore(1, 1)
      expect(store.currentGame.team1Score).toBe(1)
      
      store.incrementScore(1, 5) // Should cap at 5
      expect(store.currentGame.team1Score).toBe(5)
      expect(store.isGameComplete).toBe(true)
      expect(store.games.length).toBe(0)
      
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.games[0]?.team1Score).toBe(5)
      expect(store.matchState).toBe('ready_for_submission')
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

    it('increments score past limit when winByTwo is true', async () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: true }

      store.incrementScore(1, 4)
      store.incrementScore(2, 4)
      expect(store.currentGame.team1Score).toBe(4)
      expect(store.currentGame.team2Score).toBe(4)
      expect(store.isGameComplete).toBe(false)

      store.incrementScore(1, 1) // 5 - 4
      expect(store.currentGame.team1Score).toBe(5)
      expect(store.isGameComplete).toBe(false) // Needs 2 point lead

      store.incrementScore(2, 1) // 5 - 5
      expect(store.currentGame.team2Score).toBe(5)
      expect(store.isGameComplete).toBe(false)

      store.incrementScore(1, 1) // 6 - 5
      expect(store.currentGame.team1Score).toBe(6)
      expect(store.isGameComplete).toBe(false)

      store.incrementScore(1, 1) // 7 - 5
      expect(store.currentGame.team1Score).toBe(7)
      expect(store.isGameComplete).toBe(true) // Has 2 point lead
    })

    it('manually completes a game and starts next when scoreLimit is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: false }
      store.incrementScore(1, 5)
      expect(store.isGameComplete).toBe(true)
      
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.games[0]?.team1Score).toBe(5)
      expect(store.currentGame.team1Score).toBe(0)
      expect(store.matchState).toBe('draft') // Not ready for submission because winsNeeded = 2
    })

    it('manually completes match when winsNeeded is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: false }
      
      // Game 1
      store.incrementScore(1, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.matchState).toBe('draft')
      
      // Game 2
      store.incrementScore(1, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(2)
      expect(store.matchState).toBe('ready_for_submission')
    })
    
    it('manually completes match when gameLimit is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 2, winsNeeded: 3, winByTwo: false }
      
      // Game 1
      store.incrementScore(2, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.matchState).toBe('draft')
      
      // Game 2
      store.incrementScore(2, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(2)
      expect(store.matchState).toBe('ready_for_submission')
    })

    it('undos last game correctly', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: false }

      store.incrementScore(1, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.matchState).toBe('draft')

      store.undoLastGame()
      expect(store.games.length).toBe(0)
      expect(store.currentGame.team1Score).toBe(5)
      expect(store.matchState).toBe('score_entry')
    })

    it('undos last game from ready_for_submission state', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 2, winsNeeded: 3, winByTwo: false }

      store.incrementScore(1, 5)
      store.completeCurrentGame()

      store.incrementScore(1, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(2)
      expect(store.matchState).toBe('ready_for_submission')

      store.undoLastGame()
      expect(store.games.length).toBe(1)
      expect(store.currentGame.team1Score).toBe(5)
      expect(store.matchState).toBe('score_entry')
    })

    it('prevents undo when points are already scored in current game', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: false }

      store.incrementScore(1, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)

      store.incrementScore(2, 2)
      expect(store.canUndoLastGame).toBe(false)
      store.undoLastGame()
      expect(store.games.length).toBe(1)
      expect(store.currentGame.team2Score).toBe(2)
    })
  })

  describe('Submission Timer & Undo Window', () => {
    beforeEach(() => {
      vi.useFakeTimers()
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('initializes countdown timer and payload when startSubmissionTimer is called', () => {
      const store = useMatchDraftStore()
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
      store.incrementScore(1, 5)
      store.completeCurrentGame()

      store.startSubmissionTimer()

      expect(store.isPendingSubmission).toBe(true)
      expect(store.submissionCountdown).toBe(15)
      expect(store.pendingSubmission).not.toBeNull()
      expect(store.pendingSubmission?.payload.teamAAttackerId).toBe('p1')
    })

    it('executes HTTP POST and resets store when 15 seconds timer expires', async () => {
      const store = useMatchDraftStore()
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
      store.incrementScore(1, 5)
      store.completeCurrentGame()

      fetchMock.mockResolvedValueOnce({ ok: true })

      store.startSubmissionTimer()
      expect(store.isPendingSubmission).toBe(true)

      vi.advanceTimersByTime(15000)

      await Promise.resolve()
      await Promise.resolve()

      expect(fetchMock).toHaveBeenCalledWith('/api/v1/matches', expect.objectContaining({
        method: 'POST'
      }))
      expect(store.isPendingSubmission).toBe(false)
      expect(store.pendingSubmission).toBeNull()
    })

    it('aborts timer and restores ready_for_submission state when cancelSubmissionTimer is called', () => {
      const store = useMatchDraftStore()
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
      store.incrementScore(1, 5)
      store.completeCurrentGame()

      store.startSubmissionTimer()
      vi.advanceTimersByTime(5000)

      store.cancelSubmissionTimer()

      expect(store.isPendingSubmission).toBe(false)
      expect(store.pendingSubmission).toBeNull()
      expect(store.matchState).toBe('ready_for_submission')
      expect(fetchMock).not.toHaveBeenCalledWith('/api/v1/matches', expect.anything())
    })
  })
})
