export interface CooldownMatchPayload {
  id: string
  status: string
  creatorNickname: string
  teamAAttackerNickname?: string
  teamADefenderNickname?: string
  teamBAttackerNickname?: string
  teamBDefenderNickname?: string
  confirmedByOpponentIds?: string[]
  requiredConfirmations?: number
  cooldownExpiresAt?: string
  games?: Array<{ teamAScore: number; teamBScore: number }>
  createdAt?: string
}

export function buildCooldownMatch(overrides: Partial<CooldownMatchPayload> = {}): CooldownMatchPayload {
  const now = Date.now()
  return {
    id: overrides.id ?? `match-${now}`,
    status: overrides.status ?? 'PARTIALLY_CONFIRMED',
    creatorNickname: overrides.creatorNickname ?? 'Alice',
    teamAAttackerNickname: overrides.teamAAttackerNickname ?? 'Alice',
    teamADefenderNickname: overrides.teamADefenderNickname ?? 'Bob',
    teamBAttackerNickname: overrides.teamBAttackerNickname ?? 'Charlie',
    teamBDefenderNickname: overrides.teamBDefenderNickname ?? 'Dave',
    confirmedByOpponentIds: overrides.confirmedByOpponentIds ?? ['opponent-a'],
    requiredConfirmations: overrides.requiredConfirmations ?? 2,
    cooldownExpiresAt: overrides.cooldownExpiresAt ?? new Date(now + 2 * 60 * 60 * 1000).toISOString(),
    games: overrides.games ?? [{ teamAScore: 10, teamBScore: 8 }],
    createdAt: overrides.createdAt ?? new Date().toISOString(),
  }
}

export function buildPendingResponse(matches: CooldownMatchPayload[] = []) {
  return {
    count: matches.length,
    matches,
  }
}
