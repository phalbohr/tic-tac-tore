export type LeaderboardType = 'OVERALL' | 'ATTACKER' | 'DEFENDER'
export type TimePeriod = 'WEEKLY' | 'MONTHLY' | 'YEARLY' | 'ALL_TIME'

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
  number: number
}

export interface LeaderboardParams {
  type?: LeaderboardType
  period?: TimePeriod
  minMatches?: number
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
  token?: string
  signal?: AbortSignal
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

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
    page: params.page,
    size: params.size
  }, params)
}

export async function getH2HStats(params: PersonalStatsParams): Promise<H2HStats[]> {
  return apiFetch<H2HStats[]>('/statistics/h2h', { period: params.period }, params)
}
