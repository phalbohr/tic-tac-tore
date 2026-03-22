import { useAuthStore } from '@/stores/auth'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

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
}

export async function getLeaderboard(params: LeaderboardParams): Promise<Page<LeaderboardEntry>> {
  const authStore = useAuthStore()
  const queryParams = new URLSearchParams()
  
  if (params.type) queryParams.append('type', params.type)
  if (params.period) queryParams.append('period', params.period)
  if (params.minMatches !== undefined) queryParams.append('minMatches', params.minMatches.toString())
  if (params.page !== undefined) queryParams.append('page', params.page.toString())
  if (params.size !== undefined) queryParams.append('size', params.size.toString())

  const response = await fetch(`${API_BASE_URL}/leaderboard?${queryParams.toString()}`, {
    headers: {
      'Authorization': `Bearer ${authStore.token}`
    },
    signal: params.signal
  })

  if (!response.ok) {
    throw new Error(`Failed to fetch leaderboard: ${response.status}`)
  }

  return response.json()
}
