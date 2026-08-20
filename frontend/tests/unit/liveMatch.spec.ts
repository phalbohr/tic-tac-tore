import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach } from 'vitest'
import { useLiveMatchStore } from '@/stores/liveMatch'

describe('[Story 5.2] LiveMatch Store - Undo & Timeline', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it.skip('[P0] canUndo is false initially when no goals are recorded', () => {
    const store = useLiveMatchStore()
    expect((store as any).canUndo).toBe(false)
  })

  it.skip('[P0] recording a goal makes canUndo true and enriches goal timeline with player name', () => {
    const store = useLiveMatchStore()
    store.recordGoal('p1', 'teamA.attacker')

    expect(store.goals).toHaveLength(1)
    expect((store as any).canUndo).toBe(true)
    expect((store as any).goalTimeline).toBeDefined()
    expect((store as any).goalTimeline).toHaveLength(1)
    expect((store as any).goalTimeline[0]).toMatchObject({
      playerId: 'p1',
      playerName: 'Alice',
      quadrantRole: 'teamA.attacker',
    })
  })

  it.skip('[P0] undoLastGoal removes the most recent goal and updates canUndo', () => {
    const store = useLiveMatchStore()
    store.recordGoal('p1', 'teamA.attacker')
    store.recordGoal('p3', 'teamB.attacker')
    expect(store.goals).toHaveLength(2)

    const undone = (store as any).undoLastGoal()
    expect(undone).toBeDefined()
    expect(undone.playerId).toBe('p3')
    expect(store.goals).toHaveLength(1)
    expect((store as any).canUndo).toBe(true)

    const undoneFirst = (store as any).undoLastGoal()
    expect(undoneFirst.playerId).toBe('p1')
    expect(store.goals).toHaveLength(0)
    expect((store as any).canUndo).toBe(false)
  })

  it.skip('[P1] undoLastGoal handles empty state safely without throwing', () => {
    const store = useLiveMatchStore()
    expect(() => {
      const result = (store as any).undoLastGoal()
      expect(result).toBeUndefined()
    }).not.toThrow()
    expect((store as any).canUndo).toBe(false)
  })

  it.skip('[P1] resolves player names across both teams correctly', () => {
    const store = useLiveMatchStore()
    expect((store as any).getPlayerName('p1')).toBe('Alice')
    expect((store as any).getPlayerName('p2')).toBe('Bob')
    expect((store as any).getPlayerName('p3')).toBe('Charlie')
    expect((store as any).getPlayerName('p4')).toBe('Dave')
    expect((store as any).getPlayerName('unknown')).toBe('Unknown Player')
  })
})
