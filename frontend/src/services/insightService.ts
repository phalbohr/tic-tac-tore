export type InsightType =
  | 'WIN_STREAK'
  | 'FORM_TREND'
  | 'POSITIONAL_MASTERY'
  | 'BEST_PARTNERSHIP'
  | 'MILESTONE_PROXIMITY'
  | 'INSUFFICIENT_DATA'

export type InsightCategory =
  | 'STREAK'
  | 'TREND'
  | 'POSITION'
  | 'PARTNERSHIP'
  | 'MILESTONE'
  | 'GENERAL'

export type InsightImportance = 'HIGH' | 'MEDIUM' | 'LOW'

export interface PlayerInsight {
  id: string
  type: InsightType
  category: InsightCategory
  importance: InsightImportance
  titleKey: string
  descriptionKey: string
  params: Record<string, unknown>
  icon: string
  drillDownUrl?: string | null
}

export interface PlayerInsightsResponse {
  playerId: string
  totalCount: number
  insights: PlayerInsight[]
}

export async function getPlayerInsights(
  playerId: string,
  options: { token?: string; signal?: AbortSignal } = {},
): Promise<PlayerInsightsResponse> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  const token = options.token || localStorage.getItem('token')
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  const res = await fetch(`/api/v1/players/${playerId}/insights`, {
    headers,
    signal: options.signal,
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch player insights (${res.status})`)
  }
  return res.json()
}

export async function getMyInsights(
  options: { token?: string; signal?: AbortSignal } = {},
): Promise<PlayerInsightsResponse> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  const token = options.token || localStorage.getItem('token')
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  const res = await fetch('/api/v1/statistics/insights', {
    headers,
    signal: options.signal,
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch personal insights (${res.status})`)
  }
  return res.json()
}
