import re

test_file = "frontend/src/features/match/stores/matchDraftStore.spec.ts"
with open(test_file, "r") as f:
    content = f.read()

# Add afterEach cleanup
old_beforeEach = """  beforeEach(() => {
    setActivePinia(createPinia())
    fetchMock = vi.fn()
    globalThis.fetch = fetchMock as unknown as typeof fetch
  })"""
new_beforeEach = """  const originalFetch = globalThis.fetch;
  beforeEach(() => {
    setActivePinia(createPinia())
    fetchMock = vi.fn()
    globalThis.fetch = fetchMock as unknown as typeof fetch
  })
  
  afterEach(() => {
    globalThis.fetch = originalFetch;
    vi.restoreAllMocks();
  })"""
content = content.replace(old_beforeEach, new_beforeEach)

# Import 'afterEach'
content = content.replace("describe, it, expect, beforeEach, vi, Mock", "describe, it, expect, beforeEach, afterEach, vi, Mock")

old_score_tests = """  describe('Score Entry and Auto-Completion', () => {
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
  })"""

new_score_tests = """  describe('Score Entry and Manual Completion', () => {
    it('handles successful loadRuleConfig', async () => {
      fetchMock.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ scoreLimit: 5, gameLimit: 3, winsNeeded: 2 })
      })
      const store = useMatchDraftStore()
      await store.loadRuleConfig()
      expect(store.ruleConfig).toEqual({ scoreLimit: 5, gameLimit: 3, winsNeeded: 2 })
    })

    it('handles API error in loadRuleConfig by throwing', async () => {
      fetchMock.mockResolvedValueOnce({
        ok: false
      })
      const store = useMatchDraftStore()
      await expect(store.loadRuleConfig()).rejects.toThrow('API failed')
    })

    it('increments score but caps at scoreLimit, requires manual complete', async () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 1, winsNeeded: 1 }
      store.incrementScore(1, 1)
      expect(store.currentGame.team1Score).toBe(1)
      
      store.incrementScore(1, 5) // Should cap at 5
      expect(store.currentGame.team1Score).toBe(5)
      expect(store.isGameComplete).toBe(true)
      expect(store.games.length).toBe(0)
      
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.games[0].team1Score).toBe(5)
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

    it('manually completes a game and starts next when scoreLimit is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2 }
      store.incrementScore(1, 5)
      expect(store.isGameComplete).toBe(true)
      
      store.completeCurrentGame()
      expect(store.games.length).toBe(1)
      expect(store.games[0].team1Score).toBe(5)
      expect(store.currentGame.team1Score).toBe(0)
      expect(store.matchState).toBe('draft') // Not ready for submission because winsNeeded = 2
    })

    it('manually completes match when winsNeeded is reached', () => {
      const store = useMatchDraftStore()
      store.ruleConfig = { scoreLimit: 5, gameLimit: 3, winsNeeded: 2 }
      
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
      store.ruleConfig = { scoreLimit: 5, gameLimit: 2, winsNeeded: 3 }
      
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
  })"""

content = content.replace(old_score_tests, new_score_tests)

with open(test_file, "w") as f:
    f.write(content)
