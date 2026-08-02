import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useSubmissionTimer, SubmissionResult } from '../composables/useSubmissionTimer'
import { getCsrfHeaders } from '../../../utils/cookieUtils'
import { useAuthStore } from '@/stores/auth'

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
  const authStore = useAuthStore()
  const matchType = ref<MatchType>(MatchType.ONE_VS_ONE)

  const selectedPlayers = ref<string[]>([])
  const ruleSystem = ref<string>('STANDARD')
  const frequentOpponents = ref<PlayerDto[]>([])
  const fetchedPlayers = ref<Record<string, PlayerDto>>({})
  
  const ruleConfig = ref<RuleConfig | null>(null)
  const games = ref<GameScore[]>([])
  const currentGame = ref<GameScore>({ team1Score: 0, team2Score: 0 })
  const activeGameIndex = ref<number>(-1)
  const matchState = ref<'draft' | 'score_entry' | 'ready_for_submission' | 'position_swap'>('draft')
  const submitError = ref<string | null>(null)

  function clearSubmitError() {
    submitError.value = null
  }

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
      const res = await fetch(`/api/v1/rule-configurations?type=PRESET`, { signal })
      if (res.ok) {
        const data = await res.json()
        if (Array.isArray(data)) {
          const preset = data.find((p: { name?: string; id?: string }) => p.name?.toUpperCase() === ruleSystem.value.toUpperCase() || p.id === ruleSystem.value) || data[0]
          if (preset) {
            const gameLimit = Number(preset.gameLimit ?? 3)
            ruleConfig.value = {
              scoreLimit: Number(preset.goalLimit ?? preset.scoreLimit ?? 10),
              gameLimit: gameLimit,
              winsNeeded: Number(preset.winsNeeded ?? Math.floor(gameLimit / 2) + 1),
              winByTwo: Boolean(preset.winByTwo)
            }
          } else {
            throw new Error('Preset not found')
          }
        } else {
          if (typeof data.scoreLimit !== 'number' && typeof data.scoreLimit !== 'string') {
             throw new Error('Invalid numeric fields in rule config')
          }
          ruleConfig.value = { 
            scoreLimit: Number(data.scoreLimit), 
            gameLimit: Number(data.gameLimit), 
            winsNeeded: Number(data.winsNeeded),
            winByTwo: Boolean(data.winByTwo)
          }
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

  const uncommittedGame = ref<GameScore>({ team1Score: 0, team2Score: 0 })

  const savedNewGame = ref<GameScore>({ team1Score: 0, team2Score: 0 })

  const isMatchComplete = computed(() => {
    const winsNeeded = ruleConfig.value?.winsNeeded ?? 2
    const gameLimit = ruleConfig.value?.gameLimit ?? 3
    const limit = ruleConfig.value?.scoreLimit ?? 10
    const winByTwo = ruleConfig.value?.winByTwo ?? false

    let t1w = 0
    let t2w = 0
    let totalCompletedGames = 0

    games.value.forEach((g, idx) => {
      const s1 = idx === activeGameIndex.value ? currentGame.value.team1Score : g.team1Score
      const s2 = idx === activeGameIndex.value ? currentGame.value.team2Score : g.team2Score
      const reachedLimit = s1 >= limit || s2 >= limit
      const gameComplete = reachedLimit && (!winByTwo || Math.abs(s1 - s2) >= 2)
      if (gameComplete) {
        totalCompletedGames++
        if (s1 > s2) t1w++
        else if (s2 > s1) t2w++
      }
    })

    if (activeGameIndex.value === -1 && isGameComplete.value) {
      totalCompletedGames++
      if (currentGame.value.team1Score > currentGame.value.team2Score) t1w++
      else if (currentGame.value.team2Score > currentGame.value.team1Score) t2w++
    }

    return t1w >= winsNeeded || t2w >= winsNeeded || totalCompletedGames >= gameLimit
  })

  const currentActiveIndex = computed(() => (activeGameIndex.value >= 0 ? activeGameIndex.value : games.value.length))

  function selectGameToEdit(index: number) {
    if (index < 0 || index > games.value.length) return
    
    if (activeGameIndex.value >= 0 && activeGameIndex.value < games.value.length) {
      games.value[activeGameIndex.value] = { ...currentGame.value }
    } else if (activeGameIndex.value === -1) {
      savedNewGame.value = { ...currentGame.value }
    }

    if (index < games.value.length) {
      activeGameIndex.value = index
      const targetGame = games.value[index]
      if (targetGame) {
        currentGame.value = { ...targetGame }
      }
    } else if (index === games.value.length) {
      activeGameIndex.value = -1
      currentGame.value = { ...savedNewGame.value }
    }
    matchState.value = 'score_entry'
  }

  const canUndoLastGame = computed(() => {
    if (games.value.length === 0) return false
    if (matchState.value === 'ready_for_submission') return true
    if (activeGameIndex.value > 0) return true
    return activeGameIndex.value === -1 && currentGame.value.team1Score === 0 && currentGame.value.team2Score === 0
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

    if (activeGameIndex.value >= 0 && activeGameIndex.value < games.value.length) {
      const activeGame = games.value[activeGameIndex.value]
      if (activeGame) {
        activeGame.team1Score = currentGame.value.team1Score
        activeGame.team2Score = currentGame.value.team2Score
      }
    } else if (activeGameIndex.value === -1) {
      savedNewGame.value.team1Score = currentGame.value.team1Score
      savedNewGame.value.team2Score = currentGame.value.team2Score
    }
  }

  function decrementScore(team: 1 | 2) {
    if (matchState.value === 'ready_for_submission') return
    if (team === 1) {
      currentGame.value.team1Score = Math.max(0, currentGame.value.team1Score - 1)
    } else {
      currentGame.value.team2Score = Math.max(0, currentGame.value.team2Score - 1)
    }

    if (activeGameIndex.value >= 0 && activeGameIndex.value < games.value.length) {
      const activeGame = games.value[activeGameIndex.value]
      if (activeGame) {
        activeGame.team1Score = currentGame.value.team1Score
        activeGame.team2Score = currentGame.value.team2Score
      }
    } else if (activeGameIndex.value === -1) {
      savedNewGame.value.team1Score = currentGame.value.team1Score
      savedNewGame.value.team2Score = currentGame.value.team2Score
    }
  }

  function completeCurrentGame() {
    if (!isGameComplete.value) return;

    if (activeGameIndex.value >= 0 && activeGameIndex.value < games.value.length) {
      games.value[activeGameIndex.value] = { ...currentGame.value };

      if (activeGameIndex.value < games.value.length - 1) {
        const nextIndex = activeGameIndex.value + 1;
        activeGameIndex.value = nextIndex;
        const nextGame = games.value[nextIndex];
        if (nextGame) {
          currentGame.value = { ...nextGame };
        }
        return;
      }

      if (isMatchComplete.value) {
        matchState.value = 'ready_for_submission';
        return;
      }

      activeGameIndex.value = -1;
      currentGame.value = { ...savedNewGame.value };
      return;
    }

    const wasMatchComplete = isMatchComplete.value;
    games.value.push({ ...currentGame.value });
    const prevGame = currentGame.value;
    savedNewGame.value = {
      team1Score: 0,
      team2Score: 0,
      teamAAttackerId: prevGame.teamAAttackerId,
      teamADefenderId: prevGame.teamADefenderId,
      teamBAttackerId: prevGame.teamBAttackerId,
      teamBDefenderId: prevGame.teamBDefenderId
    };
    currentGame.value = { ...savedNewGame.value };
    activeGameIndex.value = -1;

    if (wasMatchComplete) {
      matchState.value = 'ready_for_submission';
    } else if (matchType.value === MatchType.TWO_VS_TWO) {
      matchState.value = 'position_swap';
    } else {
      matchState.value = 'draft';
    }
  }

  function loadFromRejectedMatch(matchItem: {
    teamAAttackerId?: string;
    teamADefenderId?: string;
    teamBAttackerId?: string;
    teamBDefenderId?: string;
    games?: Array<{ teamAScore: number; teamBScore: number; teamAAttackerId?: string; teamADefenderId?: string; teamBAttackerId?: string; teamBDefenderId?: string }>;
  }) {
    const teamAPlayers: string[] = []
    if (matchItem.teamAAttackerId) teamAPlayers.push(matchItem.teamAAttackerId)
    if (matchItem.teamADefenderId) teamAPlayers.push(matchItem.teamADefenderId)

    const teamBPlayers: string[] = []
    if (matchItem.teamBAttackerId) teamBPlayers.push(matchItem.teamBAttackerId)
    if (matchItem.teamBDefenderId) teamBPlayers.push(matchItem.teamBDefenderId)

    if (teamAPlayers.length > 1 || teamBPlayers.length > 1) {
      matchType.value = MatchType.TWO_VS_TWO
    } else {
      matchType.value = MatchType.ONE_VS_ONE
    }

    selectedPlayers.value = [...teamAPlayers, ...teamBPlayers]
    selectedPlayers.value.forEach(id => fetchPlayer(id))

    if (matchItem.games && matchItem.games.length > 0) {
      games.value = matchItem.games.map(g => ({
        team1Score: g.teamAScore,
        team2Score: g.teamBScore,
        teamAAttackerId: g.teamAAttackerId,
        teamADefenderId: g.teamADefenderId,
        teamBAttackerId: g.teamBAttackerId,
        teamBDefenderId: g.teamBDefenderId
      }))
      activeGameIndex.value = games.value.length - 1
      const lastGame = games.value[activeGameIndex.value]
      currentGame.value = lastGame ? { ...lastGame } : { team1Score: 0, team2Score: 0 }
    } else {
      games.value = []
      activeGameIndex.value = -1
      currentGame.value = { team1Score: 0, team2Score: 0 }
    }
    savedNewGame.value = { team1Score: 0, team2Score: 0 }

    if (!ruleConfig.value) {
      ruleConfig.value = { scoreLimit: 10, gameLimit: 3, winsNeeded: 2, winByTwo: false }
    }
    matchState.value = 'score_entry'
  }

  function beginScoreEntry() {
    games.value = []
    activeGameIndex.value = -1
    savedNewGame.value = { team1Score: 0, team2Score: 0 }
    currentGame.value = { team1Score: 0, team2Score: 0 }
    if (matchType.value === MatchType.TWO_VS_TWO) {
      currentGame.value.teamAAttackerId = selectedPlayers.value[0]
      currentGame.value.teamADefenderId = selectedPlayers.value[1]
      currentGame.value.teamBAttackerId = selectedPlayers.value[2]
      currentGame.value.teamBDefenderId = selectedPlayers.value[3]
      savedNewGame.value = { ...currentGame.value }
      matchState.value = 'position_swap'
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
    const popped = games.value.pop()
    if (popped) {
      currentGame.value = { ...popped }
      activeGameIndex.value = games.value.length > 0 ? games.value.length - 1 : -1
      matchState.value = 'score_entry'
    }
  }

  async function executeCommit(item: { idempotencyKey: string; payload: Record<string, unknown> }): Promise<SubmissionResult> {
    try {
      const res = await fetch('/api/v1/matches', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Idempotency-Key': item.idempotencyKey,
          ...getCsrfHeaders()
        },
        body: JSON.stringify(item.payload)
      })
      if (res.ok) {
        resetDraftStateOnly()
        return SubmissionResult.SUCCESS
      } else if (res.status >= 400 && res.status < 500) {
        let msg = 'Failed to submit match'
        try {
          const data = await res.json()
          if (data.message) msg = data.message
        } catch {
          // ignore parsing error
        }
        submitError.value = msg
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
    clearSubmitError()
    
    const requiredPlayers = matchType.value === MatchType.TWO_VS_TWO ? 4 : 2
    if (selectedPlayers.value.length < requiredPlayers) return

    if (matchState.value !== 'ready_for_submission' && isGameComplete.value) {
      completeCurrentGame()
    }
    if (games.value.length === 0) return

    const idempotencyKey = crypto.randomUUID()
    
    const teamAAttackerId = selectedPlayers.value[0]
    const teamADefenderId = matchType.value === MatchType.TWO_VS_TWO ? selectedPlayers.value[1] : undefined
    const teamBAttackerId = matchType.value === MatchType.TWO_VS_TWO ? selectedPlayers.value[2] : selectedPlayers.value[1]
    const teamBDefenderId = matchType.value === MatchType.TWO_VS_TWO ? selectedPlayers.value[3] : undefined

    const creatorId = authStore.profile?.id || teamAAttackerId
    const payload = {
      idempotencyKey,
      creatorId,
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
    if (canUndoLastGame.value) {
      undoLastGame()
    } else {
      matchState.value = 'score_entry'
    }
  }

  function resetDraftStateOnly() {
    matchType.value = MatchType.ONE_VS_ONE
    selectedPlayers.value = []
    ruleSystem.value = 'STANDARD'
    ruleConfig.value = null
    games.value = []
    currentGame.value = { team1Score: 0, team2Score: 0 }
    matchState.value = 'draft'
    clearSubmitError()
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
    savedNewGame,
    uncommittedGame,
    activeGameIndex,
    currentActiveIndex,
    matchState,
    submitError,
    clearSubmitError,
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
    selectGameToEdit,
    loadFromRejectedMatch,
    undoLastGame,
    beginScoreEntry,
    confirmPositions,
    swapPositions,
    returnToDraft,
    reset
  }
})
