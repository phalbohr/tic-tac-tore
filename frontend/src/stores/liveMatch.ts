import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Goal {
  id: string
  playerId: string
  quadrantRole: string
  timestamp: number
}

export const useLiveMatchStore = defineStore('match', () => {
  const goals = ref<Goal[]>([])

  const teamA = ref({
    attacker: { id: 'p1', name: 'Alice' },
    defender: { id: 'p2', name: 'Bob' },
  })

  const teamB = ref({
    attacker: { id: 'p3', name: 'Charlie' },
    defender: { id: 'p4', name: 'Dave' },
  })

  const recordGoal = (playerId: string, quadrantRole: string) => {
    goals.value.push({
      id: (crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2)),
      playerId,
      quadrantRole,
      timestamp: Date.now(),
    })
  }

  return {
    goals,
    teamA,
    teamB,
    recordGoal,
  }
})
