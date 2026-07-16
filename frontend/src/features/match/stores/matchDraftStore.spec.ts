import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useMatchDraftStore, MatchType } from './matchDraftStore'

describe('matchDraftStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    globalThis.fetch = vi.fn() as unknown as typeof fetch
  })

  it('initializes with default values', () => {
    const store = useMatchDraftStore()
    expect(store.matchType).toBe(MatchType.ONE_VS_ONE)
    expect(store.selectedPlayers).toEqual([])
    expect(store.ruleSystem).toBe('STANDARD')
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
})
