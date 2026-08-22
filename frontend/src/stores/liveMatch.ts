import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Goal {
  id: string
  playerId: string
  playerName: string
  quadrantRole: string
  timestamp: number
}

export interface TimelineGoal extends Goal {
  playerName: string
}

function generateUUID(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    let r: number
    if (typeof crypto !== 'undefined' && typeof crypto.getRandomValues === 'function') {
      const array = new Uint8Array(1)
      crypto.getRandomValues(array)
      r = (array[0] ?? 0) % 16
    } else {
      r = (Math.random() * 16) | 0
    }
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export const useLiveMatchStore = defineStore('liveMatch', () => {
  const goals = ref<Goal[]>([])
  const matchStartTime = ref<number | null>(null)

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
      playerName: g.playerName || getPlayerName(g.playerId),
    }))
  })

  const recordGoal = (playerId: string, quadrantRole: string) => {
    if (matchStartTime.value === null) {
      matchStartTime.value = Date.now()
    }
    goals.value.push({
      id: generateUUID(),
      playerId,
      playerName: getPlayerName(playerId),
      quadrantRole,
      timestamp: Date.now(),
    })
  }

  const undoLastGoal = (): Goal | undefined => {
    if (goals.value.length === 0) return undefined
    return goals.value.pop()
  }

  const swapPositions = (team: 'teamA' | 'teamB') => {
    const targetTeam = team === 'teamA' ? teamA : teamB
    const tempAttacker = { ...targetTeam.value.attacker }
    const tempDefender = { ...targetTeam.value.defender }
    targetTeam.value.attacker = tempDefender
    targetTeam.value.defender = tempAttacker
  }

  const isRefereeMode = ref(false)

  const setRefereeMode = (val: boolean) => {
    isRefereeMode.value = val
  }

  return {
    goals,
    matchStartTime,
    teamA,
    teamB,
    isRefereeMode,
    canUndo,
    goalTimeline,
    getPlayerName,
    recordGoal,
    undoLastGoal,
    swapPositions,
    setRefereeMode,
  }
})
