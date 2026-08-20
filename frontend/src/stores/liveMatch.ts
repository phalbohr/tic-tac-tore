import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Goal {
  id: string
  playerId: string
  quadrantRole: string
  timestamp: number
}

export interface TimelineGoal extends Goal {
  playerName: string
}

export const useLiveMatchStore = defineStore('liveMatch', () => {
  const goals = ref<Goal[]>([])

  const teamA = ref({
    attacker: { id: 'p1', name: 'Alice' },
    defender: { id: 'p2', name: 'Bob' },
  })

  const teamB = ref({
    attacker: { id: 'p3', name: 'Charlie' },
    defender: { id: 'p4', name: 'Dave' },
  })

  const canUndo = computed(() => goals.value.length > 0)

  const getPlayerName = (playerId: string): string => {
    if (teamA.value.attacker.id === playerId) return teamA.value.attacker.name
    if (teamA.value.defender.id === playerId) return teamA.value.defender.name
    if (teamB.value.attacker.id === playerId) return teamB.value.attacker.name
    if (teamB.value.defender.id === playerId) return teamB.value.defender.name
    return 'Unknown Player'
  }

  const goalTimeline = computed<TimelineGoal[]>(() => {
    return goals.value.map(g => ({
      ...g,
      playerName: getPlayerName(g.playerId),
    }))
  })

  const recordGoal = (playerId: string, quadrantRole: string) => {
    goals.value.push({
      id: (typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2)),
      playerId,
      quadrantRole,
      timestamp: Date.now(),
    })
  }

  const undoLastGoal = (): Goal | undefined => {
    if (goals.value.length === 0) return undefined
    return goals.value.pop()
  }

  return {
    goals,
    teamA,
    teamB,
    canUndo,
    goalTimeline,
    getPlayerName,
    recordGoal,
    undoLastGoal,
  }
})
