export interface GameScore {
  teamAScore: number
  teamBScore: number
  teamAAttackerId?: string
  teamADefenderId?: string
  teamBAttackerId?: string
  teamBDefenderId?: string
}

export interface MatchResponse {
  id: string
  idempotencyKey?: string
  creatorId: string
  teamAAttackerId: string
  teamADefenderId?: string | null
  teamBAttackerId: string
  teamBDefenderId?: string | null
  status: string
  games: GameScore[]
  createdAt: string
  confirmedByUserId?: string | null
  confirmedAt?: string | null
  rejectedByUserId?: string | null
  rejectedAt?: string | null
  rejectionReason?: string | null
  creatorNickname?: string | null
  teamAAttackerNickname?: string | null
  teamADefenderNickname?: string | null
  teamBAttackerNickname?: string | null
  teamBDefenderNickname?: string | null
  creatorAvatar?: string | null
  teamAAttackerAvatar?: string | null
  teamADefenderAvatar?: string | null
  teamBAttackerAvatar?: string | null
  teamBDefenderAvatar?: string | null
  entryMode?: string | null
  matchFormat?: string | null
  confirmedByOpponentIds?: string[] | null
  requiredConfirmations?: number | null
  cooldownExpiresAt?: string | null
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first?: boolean
  last?: boolean
}

export interface MatchHistoryParams {
  status?: 'CONFIRMED' | 'PENDING' | 'ALL' | string
  playerId?: string | null
  ruleConfigId?: string | null
  matchType?: '1v1' | '2v2' | string | null
  page?: number
  size?: number
  token?: string
  signal?: AbortSignal
}

const API_BASE_URL = '/api/v1'

export async function getMatchHistory(params: MatchHistoryParams = {}): Promise<PagedResponse<MatchResponse>> {
  const queryParams = new URLSearchParams()
  if (params.status) queryParams.append('status', params.status)
  if (params.playerId) queryParams.append('playerId', params.playerId)
  if (params.ruleConfigId) queryParams.append('ruleConfigId', params.ruleConfigId)
  if (params.matchType) queryParams.append('matchType', params.matchType)
  if (typeof params.page === 'number') queryParams.append('page', params.page.toString())
  if (typeof params.size === 'number') queryParams.append('size', params.size.toString())

  const queryString = queryParams.toString()
  const url = `${API_BASE_URL}/matches/history${queryString ? '?' + queryString : ''}`

  const headers: Record<string, string> = {}
  if (params.token) {
    headers['Authorization'] = `Bearer ${params.token}`
  }

  const response = await fetch(url, {
    headers,
    signal: params.signal
  })

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}))
    const message = errorBody.message || `API error: ${response.status}`
    throw new Error(message)
  }

  return response.json()
}
