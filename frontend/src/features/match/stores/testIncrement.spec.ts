import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach } from 'vitest'
import { useMatchDraftStore } from './matchDraftStore'

describe('incrementScore bug', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('allows losing team to increment score even after winning team reaches limit', () => {
    const store = useMatchDraftStore()
    // Setup rule limit
    store.ruleConfig = { scoreLimit: 10, gameLimit: 1, winsNeeded: 1 }
    
    // Team 1 reaches 10
    store.currentGame.team1Score = 10
    store.currentGame.team2Score = 5
    
    expect(store.isGameComplete).toBe(true)
    
    // Team 2 increments
    store.incrementScore(2, 1)
    expect(store.currentGame.team2Score).toBe(6)
    
    store.incrementScore(2, 5) // Should cap at 10
    expect(store.currentGame.team2Score).toBe(10)
  })
})
