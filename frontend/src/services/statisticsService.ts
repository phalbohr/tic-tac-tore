export type LeaderboardType = 'OVERALL' | 'ATTACKER' | 'DEFENDER'
export type TimePeriod = 'WEEKLY' | 'MONTHLY' | 'YEARLY' | 'ALL_TIME'
export type MatchTypeFilter = '1v1' | '2v2'
export type RuleSystemFilter = 'STANDARD' | 'RANDOM'

export interface LeaderboardEntry {
  rank: number
  playerId: string
  playerName: string
  totalMatches: number
  wins: number
  losses: number
  winRate: number
}

export interface Page<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number?: number
  page?: number
}

export interface LeaderboardParams {
  type?: LeaderboardType
  period?: TimePeriod
  minMatches?: number
  matchType?: MatchTypeFilter
  ruleSystem?: RuleSystemFilter
  page?: number
  size?: number
  signal?: AbortSignal
  token?: string
}

export interface PositionStats {
  matches: number
  wins: number
  losses: number
  winRate: number
}

export interface PlayerStats {
  playerId: string
  playerName: string
  overall: PositionStats
  attacker: PositionStats
  defender: PositionStats
}

export interface H2HStats {
  opponentId: string
  opponentName: string
  matches: number
  wins: number
  losses: number
  winRate: number
}

export interface PersonalStatsParams {
  period?: TimePeriod
  myPosition?: LeaderboardType
  opponentPosition?: LeaderboardType
  page?: number
  size?: number
  token?: string
  signal?: AbortSignal
}

const API_BASE_URL = '/api/v1'

async function apiFetch<T>(endpoint: string, params: Record<string, string | number | undefined>, options: { token?: string, signal?: AbortSignal }): Promise<T> {
  const queryParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined) queryParams.append(key, value.toString())
  })

  const queryString = queryParams.toString()
  const url = `${API_BASE_URL}${endpoint}${queryString ? '?' + queryString : ''}`

  const headers: Record<string, string> = {}
  if (options.token) {
    headers['Authorization'] = `Bearer ${options.token}`
  }

  const response = await fetch(url, {
    headers,
    signal: options.signal
  })

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}))
    const message = errorBody.message || `API error: ${response.status}`
    throw new Error(message)
  }

  return response.json()
}

export async function getPersonalStats(params: PersonalStatsParams): Promise<PlayerStats> {
  return apiFetch<PlayerStats>('/statistics/me', { period: params.period }, params)
}

export async function getLeaderboard(params: LeaderboardParams): Promise<Page<LeaderboardEntry>> {
  return apiFetch<Page<LeaderboardEntry>>('/statistics/leaderboard', {
    type: params.type,
    period: params.period,
    minMatches: params.minMatches,
    matchFormat: params.ruleSystem,
    matchType: params.matchType,
    page: params.page,
    size: params.size
  }, params)
}

export interface TeamPairStats {
  attackerId: string
  attackerName: string
  attackerAvatar?: string
  defenderId: string
  defenderName: string
  defenderAvatar?: string
  matches: number
  wins: number
  losses: number
  winRate: number
}

export interface TeamPairStatsParams {
  playerId?: string
  period?: TimePeriod
  ruleConfigId?: string
  minMatches?: number
  page?: number
  size?: number
  token?: string
  signal?: AbortSignal
}

export async function getH2HStats(params: PersonalStatsParams): Promise<Page<H2HStats>> {
  const res = await apiFetch<Page<H2HStats> | H2HStats[]>('/statistics/h2h', { 
    period: params.period,
    myPosition: params.myPosition,
    opponentPosition: params.opponentPosition,
    page: params.page,
    size: params.size
  }, params)
  
  if ('content' in res && Array.isArray(res.content)) {
    return res
  }
  
  // Fallback for non-paged response
  const content = res as H2HStats[]
  return {
    content,
    totalPages: 1,
    totalElements: content.length,
    size: content.length,
    number: 0
  }
}

export async function getTeamPairStats(params: TeamPairStatsParams = {}): Promise<Page<TeamPairStats>> {
  return apiFetch<Page<TeamPairStats>>('/statistics/team-pairs', {
    playerId: params.playerId,
    period: params.period,
    ruleConfigId: params.ruleConfigId,
    minMatches: params.minMatches,
    page: params.page,
    size: params.size
  }, params)
}

export interface H2HOpponent {
  id: string
  nickname: string
  avatarUrl?: string | null
}

export interface H2HMatchStats {
  matches: number
  wins: number
  losses: number
  draws: number
  winRate: number
}

export interface H2HGameStats {
  gamesWon: number
  gamesLost: number
  totalGames: number
  winRate: number
}

export interface PositionalGoals {
  scored: number
  conceded: number
}

export interface H2HGoalStats {
  attackerVsDefender: PositionalGoals
  attackerVsAttacker: PositionalGoals
  defenderVsAttacker: PositionalGoals
  defenderVsDefender: PositionalGoals
}

export interface H2HStatsResponse {
  opponent: H2HOpponent
  matches: {
    with: H2HMatchStats
    vs: H2HMatchStats
  }
  games: {
    with: H2HGameStats
    vs: H2HGameStats
  }
  goals: H2HGoalStats
}

export interface H2HParams {
  period?: TimePeriod
  ruleConfigId?: string
  matchType?: MatchTypeFilter
  token?: string
  signal?: AbortSignal
}

export async function getHeadToHeadStats(opponentId: string, params: H2HParams = {}): Promise<H2HStatsResponse> {
  return apiFetch<H2HStatsResponse>('/statistics/head-to-head', {
    opponentId,
    period: params.period,
    ruleConfigId: params.ruleConfigId,
    matchType: params.matchType,
  }, params)
}


