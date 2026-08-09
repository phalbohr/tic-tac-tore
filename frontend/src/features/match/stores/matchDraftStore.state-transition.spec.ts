import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, afterEach, vi, type Mock } from 'vitest'
import { useMatchDraftStore, MatchType } from './matchDraftStore'

describe('matchDraftStore state transitions', () => {
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

  describe('Position Swapping', () => {
    it('sets state to score_entry for 2v2 games on beginScoreEntry', () => {
      const store = useMatchDraftStore()
      store.setMatchType(MatchType.TWO_VS_TWO)
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.addPlayer('p3')
      store.addPlayer('p4')

      store.beginScoreEntry()
      expect(store.matchState).toBe('score_entry')
      expect(store.currentGame.teamADefenderId).toBe('p1')
      expect(store.currentGame.teamAAttackerId).toBe('p2')
      expect(store.currentGame.teamBDefenderId).toBe('p3')
      expect(store.currentGame.teamBAttackerId).toBe('p4')
    })

    it('sets state to score_entry for 1v1 games on beginScoreEntry', () => {
      const store = useMatchDraftStore()
      store.setMatchType(MatchType.ONE_VS_ONE)
      store.addPlayer('p1')
      store.addPlayer('p2')

      store.beginScoreEntry()
      expect(store.matchState).toBe('score_entry')
    })

    it('swaps positions correctly', () => {
      const store = useMatchDraftStore()
      store.setMatchType(MatchType.TWO_VS_TWO)
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.addPlayer('p3')
      store.addPlayer('p4')

      store.beginScoreEntry()
      expect(store.currentGame.teamADefenderId).toBe('p1')
      expect(store.currentGame.teamAAttackerId).toBe('p2')

      store.swapPositions(1)
      expect(store.currentGame.teamADefenderId).toBe('p2')
      expect(store.currentGame.teamAAttackerId).toBe('p1')
    })

    it('swaps positions for team 2', () => {
      const store = useMatchDraftStore()
      store.setMatchType(MatchType.TWO_VS_TWO)
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.addPlayer('p3')
      store.addPlayer('p4')

      store.beginScoreEntry()
      expect(store.currentGame.teamBDefenderId).toBe('p3')
      expect(store.currentGame.teamBAttackerId).toBe('p4')

      store.swapPositions(2)
      expect(store.currentGame.teamBDefenderId).toBe('p4')
      expect(store.currentGame.teamBAttackerId).toBe('p3')
    })

    it('swaps positions back and forth', () => {
      const store = useMatchDraftStore()
      store.setMatchType(MatchType.TWO_VS_TWO)
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.addPlayer('p3')
      store.addPlayer('p4')

      store.beginScoreEntry()

      store.swapPositions(1)
      expect(store.currentGame.teamADefenderId).toBe('p2')
      expect(store.currentGame.teamAAttackerId).toBe('p1')

      store.swapPositions(1)
      expect(store.currentGame.teamADefenderId).toBe('p1')
      expect(store.currentGame.teamAAttackerId).toBe('p2')
    })

    it('isolates position swap to target game without affecting subsequent games', () => {
      const store = useMatchDraftStore()
      store.setMatchType(MatchType.TWO_VS_TWO)
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.addPlayer('p3')
      store.addPlayer('p4')
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: false }

      store.beginScoreEntry()
      store.swapPositions(1, 0)
      expect(store.currentGame.teamADefenderId).toBe('p2')
      expect(store.currentGame.teamAAttackerId).toBe('p1')

      store.incrementScore(1, 5)
      store.completeCurrentGame()

      expect(store.currentGame.teamADefenderId).toBe('p2')
      expect(store.currentGame.teamAAttackerId).toBe('p1')

      store.swapPositions(1, 0)
      expect(store.games[0]?.teamADefenderId).toBe('p1')
      expect(store.currentGame.teamADefenderId).toBe('p2')
    })
  })

  describe('Score Entry and Manual Completion', () => {
    it('increments score but caps at scoreLimit, requires manual complete', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }
      store.incrementScore(1, 1)
      expect(store.currentGame.team1Score).toBe(1)

      store.incrementScore(1, 5)
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

    it('increments score past limit when winByTwo is true', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: true }

      store.incrementScore(1, 4)
      store.incrementScore(2, 4)
      expect(store.currentGame.team1Score).toBe(4)
      expect(store.currentGame.team2Score).toBe(4)
      expect(store.isGameComplete).toBe(false)

      store.incrementScore(1, 1)
      expect(store.currentGame.team1Score).toBe(5)
      expect(store.isGameComplete).toBe(false)

      store.incrementScore(2, 1)
      expect(store.currentGame.team2Score).toBe(5)
      expect(store.isGameComplete).toBe(false)

      store.incrementScore(1, 1)
      expect(store.currentGame.team1Score).toBe(6)
      expect(store.isGameComplete).toBe(false)

      store.incrementScore(1, 1)
      expect(store.currentGame.team1Score).toBe(7)
      expect(store.isGameComplete).toBe(true)
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
      expect(store.matchState).toBe('score_entry')
    })

    it('manually completes match when winsNeeded is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2, winByTwo: false }

      store.incrementScore(1, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.matchState).toBe('score_entry')

      store.incrementScore(1, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(2)
      expect(store.matchState).toBe('ready_for_submission')
    })

    it('manually completes match when gameLimit is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 2, winsNeeded: 3, winByTwo: false }

      store.incrementScore(2, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.matchState).toBe('score_entry')

      store.incrementScore(2, 5)
      store.completeCurrentGame()
      expect(store.games.length).toBe(2)
      expect(store.matchState).toBe('ready_for_submission')
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

    it('preserves swapped position mapping in submission payload', () => {
      const store = useMatchDraftStore()
      store.setMatchType(MatchType.TWO_VS_TWO)
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.addPlayer('p3')
      store.addPlayer('p4')
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false }

      store.beginScoreEntry()
      store.swapPositions(1)
      store.swapPositions(2)

      store.incrementScore(1, 5)
      store.completeCurrentGame()

      store.startSubmissionTimer()

      expect(store.pendingSubmission).not.toBeNull()
      const payload = store.pendingSubmission?.payload as { teamAAttackerId?: string; teamADefenderId?: string; teamBAttackerId?: string; teamBDefenderId?: string; games: Array<Record<string, unknown>> }

      expect(payload?.teamADefenderId).toBe('p1')
      expect(payload?.teamAAttackerId).toBe('p2')
      expect(payload?.teamBDefenderId).toBe('p3')
      expect(payload?.teamBAttackerId).toBe('p4')

      expect(payload?.games?.[0]?.teamADefenderId).toBe('p2')
      expect(payload?.games?.[0]?.teamAAttackerId).toBe('p1')
      expect(payload?.games?.[0]?.teamBDefenderId).toBe('p4')
      expect(payload?.games?.[0]?.teamBAttackerId).toBe('p3')
    })

    it('aborts timer and restores score_entry state when cancelSubmissionTimer is called', () => {
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
      expect(store.matchState).toBe('score_entry')
      expect(store.games.length).toBe(1)
      expect(fetchMock).not.toHaveBeenCalledWith('/api/v1/matches', expect.anything())
    })
  })

  describe('Retrospective Editing & Rejected Match Loading Specs', () => {
    it('loads rejected match into draft store positioned at the last game', () => {
      const store = useMatchDraftStore()
      const sampleRejected = {
        teamAAttackerId: 'p1',
        teamBAttackerId: 'p2',
        games: [
          { teamAScore: 10, teamBScore: 8 },
          { teamAScore: 7, teamBScore: 10 }
        ]
      }

      store.loadFromRejectedMatch(sampleRejected)

      expect(store.matchType).toBe(MatchType.ONE_VS_ONE)
      expect(store.selectedPlayers).toEqual(['p1', 'p2'])
      expect(store.games.length).toBe(2)
      expect(store.activeGameIndex).toBe(1)
      expect(store.currentGame.team1Score).toBe(7)
      expect(store.currentGame.team2Score).toBe(10)
      expect(store.matchState).toBe('score_entry')
    })

    it('allows editing an earlier game without resetting scores of other games', () => {
      const store = useMatchDraftStore()
      store.loadFromRejectedMatch({
        teamAAttackerId: 'p1',
        teamBAttackerId: 'p2',
        games: [
          { teamAScore: 5, teamBScore: 8 },
          { teamAScore: 7, teamBScore: 10 }
        ]
      })

      store.selectGameToEdit(0)
      expect(store.activeGameIndex).toBe(0)
      expect(store.currentGame.team1Score).toBe(5)

      store.incrementScore(1, 1)

      expect(store.games[0]?.team1Score).toBe(6)
      expect(store.games[0]?.team2Score).toBe(8)
      expect(store.games[1]?.team1Score).toBe(7)
      expect(store.games[1]?.team2Score).toBe(10)
    })

    it('preserves uncommitted game score when jumping to an earlier game during new match creation', () => {
      const store = useMatchDraftStore()
      store.addPlayer('p1')
      store.addPlayer('p2')
      store.ruleConfig = { scoreLimit: 10, gameLimit: 3, winsNeeded: 2, winByTwo: false }
      store.beginScoreEntry()

      store.incrementScore(1, 10)
      store.incrementScore(2, 5)
      store.completeCurrentGame()

      expect(store.games.length).toBe(1)
      expect(store.games[0]).toMatchObject({ team1Score: 10, team2Score: 5 })

      store.incrementScore(1, 4)
      store.incrementScore(2, 2)
      expect(store.currentGame).toMatchObject({ team1Score: 4, team2Score: 2 })

      store.selectGameToEdit(0)
      expect(store.activeGameIndex).toBe(0)
      expect(store.currentGame).toMatchObject({ team1Score: 10, team2Score: 5 })

      store.selectGameToEdit(1)
      expect(store.activeGameIndex).toBe(-1)
      expect(store.currentGame).toMatchObject({ team1Score: 4, team2Score: 2 })
      expect(store.isGameComplete).toBe(false)
      expect(store.isMatchComplete).toBe(false)
    })

    it('does not submit match when pressing next game on non-last game of rejected match', () => {
      const store = useMatchDraftStore()
      store.loadFromRejectedMatch({
        teamAAttackerId: 'p1',
        teamBAttackerId: 'p2',
        games: [
          { teamAScore: 10, teamBScore: 5 },
          { teamAScore: 10, teamBScore: 8 }
        ]
      })

      store.selectGameToEdit(0)
      expect(store.activeGameIndex).toBe(0)
      expect(store.currentGame.team1Score).toBe(10)

      store.incrementScore(2, 1)
      store.completeCurrentGame()

      expect(store.activeGameIndex).toBe(1)
      expect(store.currentGame.team1Score).toBe(10)
      expect(store.currentGame.team2Score).toBe(8)
      expect(store.matchState).toBe('score_entry')
    })
  })
})
