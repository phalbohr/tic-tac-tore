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

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

/**
 * Fetches the global leaderboard from the backend.
 * 
 * @param params Filtering and pagination parameters
 * @returns A paginated list of leaderboard entries
 */
export async function getLeaderboard(params: LeaderboardParams): Promise<Page<LeaderboardEntry>> {
  const queryParams = new URLSearchParams()
  
  if (params.type) queryParams.append('type', params.type)
  if (params.period) queryParams.append('period', params.period)
  if (params.minMatches !== undefined) queryParams.append('minMatches', params.minMatches.toString())
  if (params.page !== undefined) queryParams.append('page', params.page.toString())
  if (params.size !== undefined) queryParams.append('size', params.size.toString())

  const headers: Record<string, string> = {}
  if (params.token) {
    headers['Authorization'] = `Bearer ${params.token}`
  }

  const response = await fetch(`${API_BASE_URL}/statistics/leaderboard?${queryParams.toString()}`, {
    headers,
    signal: params.signal
  })

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}))
    const message = errorBody.message || `Failed to fetch leaderboard: ${response.status}`
    throw new Error(message)
  }

  return response.json()
}
