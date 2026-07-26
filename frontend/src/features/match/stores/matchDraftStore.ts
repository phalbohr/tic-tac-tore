import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useSubmissionTimer, SubmissionResult } from '../composables/useSubmissionTimer'

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
  teamAAttackerId?: string
  teamADefenderId?: string
  teamBAttackerId?: string
  teamBDefenderId?: string
}

export interface RuleConfig {
  scoreLimit: number
  gameLimit: number
  winsNeeded: number
  winByTwo: boolean
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
  const matchState = ref<'draft' | 'score_entry' | 'ready_for_submission' | 'position_swap'>('draft')

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
          winsNeeded: Number(data.winsNeeded),
          winByTwo: Boolean(data.winByTwo)
        }
      } else {
        throw new Error('API failed')
      }
    } catch (error) {
      const e = error as Error;
      if (e.name === 'AbortError') throw e;
      console.warn('Failed to load rule config, falling back to Best of 3 rules', e)
      ruleConfig.value = { scoreLimit: 10, gameLimit: 3, winsNeeded: 2, winByTwo: false }
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
    const reachedLimit = currentGame.value.team1Score >= limit || currentGame.value.team2Score >= limit
    if (!reachedLimit) return false

    if (ruleConfig.value?.winByTwo) {
      return Math.abs(currentGame.value.team1Score - currentGame.value.team2Score) >= 2
    }

    return true
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
    const winByTwo = ruleConfig.value?.winByTwo ?? false

    if (team === 1) {
      currentGame.value.team1Score = winByTwo
        ? currentGame.value.team1Score + amount
        : Math.min(currentGame.value.team1Score + amount, limit)
    } else {
      currentGame.value.team2Score = winByTwo
        ? currentGame.value.team2Score + amount
        : Math.min(currentGame.value.team2Score + amount, limit)
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
      const prevGame = currentGame.value;
      currentGame.value = {
        team1Score: 0,
        team2Score: 0,
        teamAAttackerId: prevGame.teamAAttackerId,
        teamADefenderId: prevGame.teamADefenderId,
        teamBAttackerId: prevGame.teamBAttackerId,
        teamBDefenderId: prevGame.teamBDefenderId
      };
      if (matchType.value === MatchType.TWO_VS_TWO) {
        matchState.value = 'position_swap'
      }
    }
  }

  function beginScoreEntry() {
    if (matchType.value === MatchType.TWO_VS_TWO) {
      matchState.value = 'position_swap'
      currentGame.value.teamAAttackerId = selectedPlayers.value[0]
      currentGame.value.teamADefenderId = selectedPlayers.value[1]
      currentGame.value.teamBAttackerId = selectedPlayers.value[2]
      currentGame.value.teamBDefenderId = selectedPlayers.value[3]
    } else {
      matchState.value = 'score_entry'
    }
  }

  function confirmPositions() {
    matchState.value = 'score_entry'
  }

  function swapPositions(team: 1 | 2) {
    if (team === 1) {
      const temp = currentGame.value.teamAAttackerId
      currentGame.value.teamAAttackerId = currentGame.value.teamADefenderId
      currentGame.value.teamADefenderId = temp
    } else {
      const temp = currentGame.value.teamBAttackerId
      currentGame.value.teamBAttackerId = currentGame.value.teamBDefenderId
      currentGame.value.teamBDefenderId = temp
    }
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

  async function executeCommit(item: { idempotencyKey: string; payload: Record<string, unknown> }): Promise<SubmissionResult> {
    try {
      const res = await fetch('/api/v1/matches', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Idempotency-Key': item.idempotencyKey
        },
        body: JSON.stringify(item.payload)
      })
      if (res.ok) {
        resetDraftStateOnly()
        return SubmissionResult.SUCCESS
      } else if (res.status >= 400 && res.status < 500) {
        return SubmissionResult.CLIENT_ERROR
      } else {
        return SubmissionResult.SERVER_OR_NETWORK_ERROR
      }
    } catch {
      return SubmissionResult.SERVER_OR_NETWORK_ERROR
    }
  }

  const {
    countdown: submissionCountdown,
    isPending: isPendingSubmission,
    isOfflinePending,
    pendingSubmission,
    startTimer,
    cancelTimer,
    clearTimer
  } = useSubmissionTimer(executeCommit)

  function startSubmissionTimer() {
    const requiredPlayers = matchType.value === MatchType.TWO_VS_TWO ? 4 : 2
    if (selectedPlayers.value.length < requiredPlayers) return

    if (isGameComplete.value) {
      completeCurrentGame()
    }
    if (games.value.length === 0) return

    const idempotencyKey = crypto.randomUUID()
    
    const teamAAttackerId = selectedPlayers.value[0]
    const teamADefenderId = matchType.value === MatchType.TWO_VS_TWO ? selectedPlayers.value[1] : undefined
    const teamBAttackerId = matchType.value === MatchType.TWO_VS_TWO ? selectedPlayers.value[2] : selectedPlayers.value[1]
    const teamBDefenderId = matchType.value === MatchType.TWO_VS_TWO ? selectedPlayers.value[3] : undefined

    const payload = {
      idempotencyKey,
      creatorId: teamAAttackerId,
      teamAAttackerId,
      teamADefenderId,
      teamBAttackerId,
      teamBDefenderId,
      games: games.value.map(g => ({
        teamAScore: g.team1Score,
        teamBScore: g.team2Score,
        teamAAttackerId: g.teamAAttackerId,
        teamADefenderId: g.teamADefenderId,
        teamBAttackerId: g.teamBAttackerId,
        teamBDefenderId: g.teamBDefenderId
      }))
    }

    startTimer({ idempotencyKey, payload })
  }

  function cancelSubmissionTimer() {
    cancelTimer()
    matchState.value = 'ready_for_submission'
  }

  function resetDraftStateOnly() {
    matchType.value = MatchType.ONE_VS_ONE
    selectedPlayers.value = []
    ruleSystem.value = 'STANDARD'
    ruleConfig.value = null
    games.value = []
    currentGame.value = { team1Score: 0, team2Score: 0 }
    matchState.value = 'draft'
  }

  function reset() {
    resetDraftStateOnly()
    clearTimer()
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
    pendingSubmission,
    isPendingSubmission,
    isOfflinePending,
    submissionCountdown,
    startSubmissionTimer,
    cancelSubmissionTimer,
    fetchDefaults,
    loadRuleConfig,
    setMatchType,
    addPlayer,
    removePlayer,
    incrementScore,
    decrementScore,
    undoLastGame,
    beginScoreEntry,
    confirmPositions,
    swapPositions,
    returnToDraft,
    reset
  }
})
