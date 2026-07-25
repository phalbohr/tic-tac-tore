import { computed } from 'vue'
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
  const fetchedPlayers = ref<Record<string, PlayerDto>>({})
  
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

  async function loadRuleConfig(signal?: AbortSignal) {
    try {
      const res = await fetch(`/api/rules/${encodeURIComponent(ruleSystem.value)}`, { signal })
      if (res.ok) {
        const data = await res.json()
        if (typeof data.scoreLimit !== 'number' && typeof data.scoreLimit !== 'string') {
           throw new Error('Invalid numeric fields in rule config')
        }
        ruleConfig.value = { 
          scoreLimit: Number(data.scoreLimit), 
          gameLimit: Number(data.gameLimit), 
          winsNeeded: Number(data.winsNeeded) 
        }
      } else {
        throw new Error('API failed')
      }
    } catch (error) {
      const e = error as Error;
      if (e.name === 'AbortError') throw e;
      console.warn('Failed to load rule config, falling back to Best of 3 rules', e)
      ruleConfig.value = { scoreLimit: 10, gameLimit: 3, winsNeeded: 2 }
    }
  }

  function setMatchType(type: MatchType) {
    matchType.value = type
    if (type === MatchType.ONE_VS_ONE) {
      selectedPlayers.value = selectedPlayers.value.slice(0, 2)
    }
  }

    async function fetchPlayer(id: string) {
    if (frequentOpponents.value.find(o => o.id === id)) return;
    if (fetchedPlayers.value[id]) return;
    try {
      const res = await fetch(`/api/v1/players/${id}`);
      if (res.ok) {
        fetchedPlayers.value[id] = await res.json();
      }
    } catch (e) {
      console.warn('Failed to fetch player profile', e);
    }
  }

  function addPlayer(playerId: string) {
    if (!playerId.trim()) return;
    const maxPlayers = matchType.value === MatchType.ONE_VS_ONE ? 2 : 4
    if (selectedPlayers.value.length < maxPlayers && !selectedPlayers.value.includes(playerId)) {
      selectedPlayers.value.push(playerId)
      fetchPlayer(playerId)
    }
  }

  function removePlayer(playerId: string) {
    selectedPlayers.value = selectedPlayers.value.filter(id => id !== playerId)
  }

  

  const isGameComplete = computed(() => {
    const limit = ruleConfig.value?.scoreLimit ?? 10
    return currentGame.value.team1Score >= limit || currentGame.value.team2Score >= limit
  })

  const isMatchComplete = computed(() => {
    const winsNeeded = ruleConfig.value?.winsNeeded ?? 1
    const gameLimit = ruleConfig.value?.gameLimit ?? 1
    
    let t1w = games.value.filter(g => g.team1Score > g.team2Score).length
    let t2w = games.value.filter(g => g.team2Score > g.team1Score).length
    
    if (isGameComplete.value) {
       if (currentGame.value.team1Score > currentGame.value.team2Score) t1w++;
       else if (currentGame.value.team2Score > currentGame.value.team1Score) t2w++;
    }
    
    return t1w >= winsNeeded || t2w >= winsNeeded || (games.value.length + 1) >= gameLimit
  })

  const canUndoLastGame = computed(() => {
    if (games.value.length === 0) return false
    if (matchState.value === 'ready_for_submission') return true
    return currentGame.value.team1Score === 0 && currentGame.value.team2Score === 0
  })

  function incrementScore(team: 1 | 2, amount: number) {
    if (matchState.value === 'ready_for_submission') return
    
    const limit = ruleConfig.value?.scoreLimit ?? 10
    if (team === 1) {
      currentGame.value.team1Score = Math.min(currentGame.value.team1Score + amount, limit)
    } else {
      currentGame.value.team2Score = Math.min(currentGame.value.team2Score + amount, limit)
    }
  }

  function decrementScore(team: 1 | 2) {
    if (matchState.value === 'ready_for_submission') return
    if (team === 1) {
      currentGame.value.team1Score = Math.max(0, currentGame.value.team1Score - 1)
    } else {
      currentGame.value.team2Score = Math.max(0, currentGame.value.team2Score - 1)
    }
  }

  function completeCurrentGame() {
    if (!isGameComplete.value) return;
    
    const wasMatchComplete = isMatchComplete.value;
    games.value.push({ ...currentGame.value });
    
    if (wasMatchComplete) {
      matchState.value = 'ready_for_submission';
    } else {
      currentGame.value = { team1Score: 0, team2Score: 0 };
    }
  }

  function beginScoreEntry() {
    matchState.value = 'score_entry'
  }

  function returnToDraft() {
    games.value = []
    currentGame.value = { team1Score: 0, team2Score: 0 }
    matchState.value = 'draft'
  }

  function undoLastGame() {
    if (!canUndoLastGame.value) return
    const lastGame = games.value.pop()
    if (lastGame) {
      currentGame.value = { ...lastGame }
      matchState.value = 'score_entry'
    }
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
    fetchedPlayers,
    isGameComplete,
    isMatchComplete,
    canUndoLastGame,
    completeCurrentGame,
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
    undoLastGame,
    beginScoreEntry,
    returnToDraft,
    reset
  }
})
