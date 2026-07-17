import { defineStore } from 'pinia'
import { ref } from 'vue'

export enum MatchType {
  ONE_VS_ONE = '1v1',
  TWO_VS_TWO = '2v2'
}

export interface PlayerDto {
  id: string
  nickname: string
  avatar: string
}

export interface GameScore {
  id?: string
  team1Score: number
  team2Score: number
}

export interface RuleConfig {
  scoreLimit: number
  gameLimit: number
  winsNeeded: number
}

export const useMatchDraftStore = defineStore('matchDraft', () => {
  const matchType = ref<MatchType>(MatchType.ONE_VS_ONE)
  const selectedPlayers = ref<string[]>([])
  const ruleSystem = ref<string>('STANDARD')
  const frequentOpponents = ref<PlayerDto[]>([])
  
  const ruleConfig = ref<RuleConfig | null>(null)
  const games = ref<GameScore[]>([])
  const currentGame = ref<GameScore>({ team1Score: 0, team2Score: 0 })
  const matchState = ref<'draft' | 'score_entry' | 'ready_for_submission'>('draft')

  async function fetchDefaults() {
    try {
      const results = await Promise.allSettled([
        fetch('/api/users/me/frequent-opponents'),
        fetch('/api/users/me/preferences/last-rule-system')
      ])
      
      const opponentsRes = results[0]
      if (opponentsRes.status === 'fulfilled' && opponentsRes.value.ok) {
        frequentOpponents.value = await opponentsRes.value.json()
      }
      
      const prefsRes = results[1]
      if (prefsRes.status === 'fulfilled' && prefsRes.value.ok) {
        const data = await prefsRes.value.json()
        if (data.lastRuleSystem) {
          ruleSystem.value = data.lastRuleSystem
        }
      }
    } catch (e) {
      console.error('Failed to fetch match defaults', e)
    }
  }

  async function loadRuleConfig() {
    try {
      const res = await fetch(`/api/rules/${ruleSystem.value}`)
      if (res.ok) {
        ruleConfig.value = await res.json()
      } else {
        throw new Error('API failed')
      }
    } catch (e) {
      console.warn('Failed to load rule config, falling back to Standard rules', e)
      ruleConfig.value = { scoreLimit: 10, gameLimit: 1, winsNeeded: 1 }
    }
  }

  function setMatchType(type: MatchType) {
    matchType.value = type
    if (type === MatchType.ONE_VS_ONE) {
      selectedPlayers.value = selectedPlayers.value.slice(0, 2)
    }
  }

  function addPlayer(playerId: string) {
    if (!playerId.trim()) return;
    const maxPlayers = matchType.value === MatchType.ONE_VS_ONE ? 2 : 4
    if (selectedPlayers.value.length < maxPlayers && !selectedPlayers.value.includes(playerId)) {
      selectedPlayers.value.push(playerId)
    }
  }

  function removePlayer(playerId: string) {
    selectedPlayers.value = selectedPlayers.value.filter(id => id !== playerId)
  }

  function incrementScore(team: 1 | 2, amount: number) {
    if (matchState.value === 'ready_for_submission') return
    const limit = ruleConfig.value?.scoreLimit ?? 10
    if (team === 1) {
      currentGame.value.team1Score = Math.min(currentGame.value.team1Score + amount, limit)
    } else {
      currentGame.value.team2Score = Math.min(currentGame.value.team2Score + amount, limit)
    }
    checkAutoCompletion()
  }

  function decrementScore(team: 1 | 2) {
    if (team === 1) {
      currentGame.value.team1Score = Math.max(0, currentGame.value.team1Score - 1)
    } else {
      currentGame.value.team2Score = Math.max(0, currentGame.value.team2Score - 1)
    }
  }

  function checkAutoCompletion() {
    const limit = ruleConfig.value?.scoreLimit ?? 10
    if (currentGame.value.team1Score >= limit || currentGame.value.team2Score >= limit) {
      games.value.push({ ...currentGame.value })
      currentGame.value = { team1Score: 0, team2Score: 0 }
      
      const winsNeeded = ruleConfig.value?.winsNeeded ?? 1
      const gameLimit = ruleConfig.value?.gameLimit ?? 1
      
      const team1Wins = games.value.filter(g => g.team1Score > g.team2Score).length
      const team2Wins = games.value.filter(g => g.team2Score > g.team1Score).length
      
      if (team1Wins >= winsNeeded || team2Wins >= winsNeeded || games.value.length >= gameLimit) {
        matchState.value = 'ready_for_submission'
      }
    }
  }

  function beginScoreEntry() {
    matchState.value = 'score_entry'
  }

  function reset() {
    matchType.value = MatchType.ONE_VS_ONE
    selectedPlayers.value = []
    ruleSystem.value = 'STANDARD'
    ruleConfig.value = null
    games.value = []
    currentGame.value = { team1Score: 0, team2Score: 0 }
    matchState.value = 'draft'
  }

  return {
    matchType,
    selectedPlayers,
    ruleSystem,
    frequentOpponents,
    ruleConfig,
    games,
    currentGame,
    matchState,
    fetchDefaults,
    loadRuleConfig,
    setMatchType,
    addPlayer,
    removePlayer,
    incrementScore,
    decrementScore,
    beginScoreEntry,
    reset
  }
})
