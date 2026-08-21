import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach } from 'vitest'
import { useLiveMatchStore } from '@/stores/liveMatch'

describe('[Story 5.2] LiveMatch Store - Undo & Timeline', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('[P0] canUndo is false initially when no goals are recorded', () => {
    const store = useLiveMatchStore()
    expect(store.canUndo).toBe(false)
  })

  it('[P0] recording a goal makes canUndo true and enriches goal timeline with player name', () => {
    const store = useLiveMatchStore()
    store.recordGoal('p1', 'teamA.attacker')

    expect(store.goals).toHaveLength(1)
    expect(store.canUndo).toBe(true)
    expect(store.goalTimeline).toBeDefined()
    expect(store.goalTimeline).toHaveLength(1)
    expect(store.goalTimeline[0]).toMatchObject({
      playerId: 'p1',
      playerName: 'Alice',
      quadrantRole: 'teamA.attacker',
    })
  })

  it('[P0] undoLastGoal removes the most recent goal and updates canUndo', () => {
    const store = useLiveMatchStore()
    store.recordGoal('p1', 'teamA.attacker')
    store.recordGoal('p3', 'teamB.attacker')
    expect(store.goals).toHaveLength(2)

    const undone = store.undoLastGoal()
    expect(undone).toBeDefined()
    expect(undone?.playerId).toBe('p3')
    expect(store.goals).toHaveLength(1)
    expect(store.canUndo).toBe(true)

    const undoneFirst = store.undoLastGoal()
    expect(undoneFirst?.playerId).toBe('p1')
    expect(store.goals).toHaveLength(0)
    expect(store.canUndo).toBe(false)
  })

  it('[P1] undoLastGoal handles empty state safely without throwing', () => {
    const store = useLiveMatchStore()
    expect(() => {
      const result = store.undoLastGoal()
      expect(result).toBeUndefined()
    }).not.toThrow()
    expect(store.canUndo).toBe(false)
  })

  it('[P1] resolves player names across both teams correctly', () => {
    const store = useLiveMatchStore()
    expect(store.getPlayerName('p1')).toBe('Alice')
    expect(store.getPlayerName('p2')).toBe('Bob')
    expect(store.getPlayerName('p3')).toBe('Charlie')
    expect(store.getPlayerName('p4')).toBe('Dave')
    expect(store.getPlayerName('unknown')).toBe('Unknown Player')
  })

  it('[P1] preserves historical player name snapshot even if player roster/name changes later', () => {
    const store = useLiveMatchStore()
    store.recordGoal('p1', 'teamA.attacker')
    expect(store.goals[0].playerName).toBe('Alice')

    // Simulate name change / roster mutation
    store.teamA.attacker.name = 'Alicia'
    expect(store.goals[0].playerName).toBe('Alice')
    expect(store.goalTimeline[0].playerName).toBe('Alice')
  })
})

describe('[Story 5.3] LiveMatch Store - Position Swapping', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it.skip('[P0] swapPositions(teamA) correctly inverts attacker and defender for Team A', () => {
    const store = useLiveMatchStore()
    expect(store.teamA.attacker).toEqual({ id: 'p1', name: 'Alice' })
    expect(store.teamA.defender).toEqual({ id: 'p2', name: 'Bob' })

    // @ts-expect-error method to be implemented in Story 5.3
    store.swapPositions('teamA')

    expect(store.teamA.attacker).toEqual({ id: 'p2', name: 'Bob' })
    expect(store.teamA.defender).toEqual({ id: 'p1', name: 'Alice' })
  })

  it.skip('[P0] swapPositions(teamB) correctly inverts attacker and defender for Team B', () => {
    const store = useLiveMatchStore()
    expect(store.teamB.attacker).toEqual({ id: 'p3', name: 'Charlie' })
    expect(store.teamB.defender).toEqual({ id: 'p4', name: 'Dave' })

    // @ts-expect-error method to be implemented in Story 5.3
    store.swapPositions('teamB')

    expect(store.teamB.attacker).toEqual({ id: 'p4', name: 'Dave' })
    expect(store.teamB.defender).toEqual({ id: 'p3', name: 'Charlie' })
  })

  it.skip('[P0] subsequent goals scored after swapPositions are attributed to newly assigned player', () => {
    const store = useLiveMatchStore()
    store.recordGoal(store.teamA.attacker.id, 'teamA.attacker')
    // @ts-expect-error method to be implemented in Story 5.3
    store.swapPositions('teamA')
    store.recordGoal(store.teamA.attacker.id, 'teamA.attacker')

    expect(store.goals).toHaveLength(2)
    expect(store.goals[0]).toMatchObject({
      playerId: 'p1',
      playerName: 'Alice',
      quadrantRole: 'teamA.attacker',
    })
    expect(store.goals[1]).toMatchObject({
      playerId: 'p2',
      playerName: 'Bob',
      quadrantRole: 'teamA.attacker',
    })
  })

  it.skip('[P1] past goals in timeline retain original player snapshot after swapPositions', () => {
    const store = useLiveMatchStore()
    store.recordGoal('p1', 'teamA.attacker')
    expect(store.goalTimeline[0].playerName).toBe('Alice')

    // @ts-expect-error method to be implemented in Story 5.3
    store.swapPositions('teamA')

    expect(store.goalTimeline[0].playerName).toBe('Alice')
    expect(store.goals[0].playerName).toBe('Alice')
  })

  it.skip('[P1] resolves player names across both teams correctly after swapping positions', () => {
    const store = useLiveMatchStore()
    // @ts-expect-error method to be implemented in Story 5.3
    store.swapPositions('teamA')
    // @ts-expect-error method to be implemented in Story 5.3
    store.swapPositions('teamB')

    expect(store.getPlayerName('p1')).toBe('Alice')
    expect(store.getPlayerName('p2')).toBe('Bob')
    expect(store.getPlayerName('p3')).toBe('Charlie')
    expect(store.getPlayerName('p4')).toBe('Dave')
    expect(store.getPlayerName('unknown')).toBe('Unknown Player')
  })
})

