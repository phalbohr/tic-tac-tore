export interface AchievementDto {
  id: string
  code: string
  category: string
  nameKey: string
  descriptionKey: string
  icon: string
  isUnlocked: boolean
  unlockedAt: string | null
  currentProgress: number | null
  targetValue: number | null
  hasProgress: boolean
}

export interface PlayerAchievementsSummaryResponse {
  playerId: string
  totalUnlocked: number
  totalAvailable: number
  achievements: AchievementDto[]
}

export async function getPlayerAchievements(playerId: string): Promise<PlayerAchievementsSummaryResponse> {
  const res = await fetch(`/api/v1/players/${playerId}/achievements`, {
    headers: { Accept: 'application/json' },
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch achievements (${res.status})`)
  }
  return res.json()
}
