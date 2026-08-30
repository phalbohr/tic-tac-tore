import type { PendingMatchItem } from '@/features/match/components/PendingMatches.vue'

export interface ApiMatchItem {
  id: string
  status?: string
  rejectionReason?: string
  creatorId?: string
  teamAAttackerId?: string
  teamADefenderId?: string
  teamBAttackerId?: string
  teamBDefenderId?: string
  creatorNickname?: string
  teamAAttackerNickname?: string
  teamADefenderNickname?: string
  teamBAttackerNickname?: string
  teamBDefenderNickname?: string
  creatorAvatar?: string
  teamAAttackerAvatar?: string
  teamADefenderAvatar?: string
  teamBAttackerAvatar?: string
  teamBDefenderAvatar?: string
  teamANames?: string[]
  teamBNames?: string[]
  teamAScore?: number
  teamBScore?: number
  games?: Array<{
    teamAScore: number
    teamBScore: number
    teamAAttackerId?: string
    teamADefenderId?: string
    teamBAttackerId?: string
    teamBDefenderId?: string
  }>
  createdAt?: string
  entryMode?: string
  matchFormat?: string
  confirmedByOpponentIds?: string[]
  requiredConfirmations?: number
  cooldownExpiresAt?: string
}

export function mapApiMatchItem(m: ApiMatchItem): PendingMatchItem {
  const teamANames: string[] = []
  if (m.teamAAttackerNickname) teamANames.push(m.teamAAttackerNickname)
  if (m.teamADefenderNickname) teamANames.push(m.teamADefenderNickname)

  const teamBNames: string[] = []
  if (m.teamBAttackerNickname) teamBNames.push(m.teamBAttackerNickname)
  if (m.teamBDefenderNickname) teamBNames.push(m.teamBDefenderNickname)

  const idToNickname = new Map<string, string>()
  const idToAvatar = new Map<string, string>()

  if (m.teamAAttackerId && m.teamAAttackerNickname) idToNickname.set(m.teamAAttackerId, m.teamAAttackerNickname)
  if (m.teamADefenderId && m.teamADefenderNickname) idToNickname.set(m.teamADefenderId, m.teamADefenderNickname)
  if (m.teamBAttackerId && m.teamBAttackerNickname) idToNickname.set(m.teamBAttackerId, m.teamBAttackerNickname)
  if (m.teamBDefenderId && m.teamBDefenderNickname) idToNickname.set(m.teamBDefenderId, m.teamBDefenderNickname)

  if (m.teamAAttackerId && m.teamAAttackerAvatar) idToAvatar.set(m.teamAAttackerId, m.teamAAttackerAvatar)
  if (m.teamADefenderId && m.teamADefenderAvatar) idToAvatar.set(m.teamADefenderId, m.teamADefenderAvatar)
  if (m.teamBAttackerId && m.teamBAttackerAvatar) idToAvatar.set(m.teamBAttackerId, m.teamBAttackerAvatar)
  if (m.teamBDefenderId && m.teamBDefenderAvatar) idToAvatar.set(m.teamBDefenderId, m.teamBDefenderAvatar)

  const games = (m.games || []).map((g) => {
    const aAttId = g.teamAAttackerId || m.teamAAttackerId
    const aDefId = g.teamADefenderId || m.teamADefenderId
    const bAttId = g.teamBAttackerId || m.teamBAttackerId
    const bDefId = g.teamBDefenderId || m.teamBDefenderId

    return {
      teamAScore: g.teamAScore,
      teamBScore: g.teamBScore,
      teamAAttackerId: aAttId,
      teamADefenderId: aDefId,
      teamBAttackerId: bAttId,
      teamBDefenderId: bDefId,
      teamAAttackerNickname: aAttId ? idToNickname.get(aAttId) || m.teamAAttackerNickname : undefined,
      teamADefenderNickname: aDefId ? idToNickname.get(aDefId) || m.teamADefenderNickname : undefined,
      teamBAttackerNickname: bAttId ? idToNickname.get(bAttId) || m.teamBAttackerNickname : undefined,
      teamBDefenderNickname: bDefId ? idToNickname.get(bDefId) || m.teamBDefenderNickname : undefined,
    }
  })

  return {
    id: m.id,
    status: m.status,
    rejectionReason: m.rejectionReason,
    creatorNickname: m.creatorNickname || 'Opponent',
    teamAAttackerId: m.teamAAttackerId,
    teamADefenderId: m.teamADefenderId,
    teamBAttackerId: m.teamBAttackerId,
    teamBDefenderId: m.teamBDefenderId,
    teamAAttackerNickname: m.teamAAttackerNickname,
    teamADefenderNickname: m.teamADefenderNickname,
    teamBAttackerNickname: m.teamBAttackerNickname,
    teamBDefenderNickname: m.teamBDefenderNickname,
    creatorAvatar: m.creatorAvatar,
    teamAAttackerAvatar: m.teamAAttackerAvatar,
    teamADefenderAvatar: m.teamADefenderAvatar,
    teamBAttackerAvatar: m.teamBAttackerAvatar,
    teamBDefenderAvatar: m.teamBDefenderAvatar,
    teamANames: teamANames.length > 0 ? teamANames : (m.teamANames || undefined),
    teamBNames: teamBNames.length > 0 ? teamBNames : (m.teamBNames || undefined),
    teamAScore: games[0]?.teamAScore ?? m.teamAScore,
    teamBScore: games[0]?.teamBScore ?? m.teamBScore,
    games: games.length > 0 ? games : undefined,
    createdAt: m.createdAt,
    confirmedByOpponentIds: m.confirmedByOpponentIds,
    requiredConfirmations: m.requiredConfirmations,
    cooldownExpiresAt: m.cooldownExpiresAt,
  }
}
