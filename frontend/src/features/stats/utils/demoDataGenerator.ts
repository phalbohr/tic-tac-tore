import type { PlayerStats, TeamPairStats, Page, H2HStatsResponse } from '@/services/statisticsService'
import type { MatchResponse, PagedResponse } from '@/services/matchService'

export function generateDemoData(): PlayerStats {
  return {
    playerId: 'demo-user-123',
    playerName: 'Demo Player',
    overall: {
      matches: 42,
      wins: 28,
      losses: 14,
      winRate: 66.67
    },
    attacker: {
      matches: 22,
      wins: 16,
      losses: 6,
      winRate: 72.73
    },
    defender: {
      matches: 20,
      wins: 12,
      losses: 8,
      winRate: 60.00
    }
  }
}

export function generateDemoTeamPairStats(): Page<TeamPairStats> {
  const content: TeamPairStats[] = [
    {
      attackerId: 'demo-p1',
      attackerName: 'Alice',
      attackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Alice',
      defenderId: 'demo-p2',
      defenderName: 'Bob',
      defenderAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Bob',
      matches: 18,
      wins: 14,
      losses: 4,
      winRate: 77.78
    },
    {
      attackerId: 'demo-p2',
      attackerName: 'Bob',
      attackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Bob',
      defenderId: 'demo-p1',
      defenderName: 'Alice',
      defenderAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Alice',
      matches: 12,
      wins: 7,
      losses: 5,
      winRate: 58.33
    },
    {
      attackerId: 'demo-p1',
      attackerName: 'Alice',
      attackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Alice',
      defenderId: 'demo-p3',
      defenderName: 'Charlie',
      defenderAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Charlie',
      matches: 10,
      wins: 6,
      losses: 4,
      winRate: 60.0
    },
    {
      attackerId: 'demo-p4',
      attackerName: 'Diana',
      attackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Diana',
      defenderId: 'demo-p2',
      defenderName: 'Bob',
      defenderAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Bob',
      matches: 8,
      wins: 4,
      losses: 4,
      winRate: 50.0
    }
  ]

  return {
    content,
    page: 0,
    size: 10,
    totalElements: content.length,
    totalPages: 1,
    number: 0
  }
}

export function generateDemoH2HStats(opponentId?: string): H2HStatsResponse {
  return {
    opponent: {
      id: opponentId || 'demo-opp-1',
      nickname: 'ShadowStriker',
      avatarUrl: 'https://api.dicebear.com/7.x/identicon/svg?seed=ShadowStriker',
    },
    matches: {
      with: { matches: 6, wins: 4, losses: 2, draws: 0, winRate: 66.7 },
      vs: { matches: 12, wins: 7, losses: 5, draws: 0, winRate: 58.3 },
    },
    games: {
      with: { gamesWon: 14, gamesLost: 8, totalGames: 22, winRate: 63.6 },
      vs: { gamesWon: 25, gamesLost: 18, totalGames: 43, winRate: 58.1 },
    },
    goals: {
      attackerVsDefender: { scored: 18, conceded: 9 },
      attackerVsAttacker: { scored: 12, conceded: 15 },
      defenderVsAttacker: { scored: 9, conceded: 16 },
      defenderVsDefender: { scored: 6, conceded: 4 },
    },
  }
}

export function generateDemoMatchHistory(currentUser?: { id?: string; nickname?: string }): PagedResponse<MatchResponse> {
  const myId = currentUser?.id || 'demo-user-123'
  const myNick = currentUser?.nickname || 'Demo Player'

  const content: MatchResponse[] = [
    {
      id: 'demo-match-1',
      creatorId: myId,
      teamAAttackerId: myId,
      teamADefenderId: null,
      teamBAttackerId: 'demo-p1',
      teamBDefenderId: null,
      status: 'CONFIRMED',
      games: [{ teamAScore: 10, teamBScore: 7 }],
      createdAt: new Date(Date.now() - 3600000 * 2).toISOString(),
      teamAAttackerNickname: myNick,
      teamBAttackerNickname: 'Alice',
      teamAAttackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Demo',
      teamBAttackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Alice',
      matchFormat: 'STANDARD'
    },
    {
      id: 'demo-match-2',
      creatorId: 'demo-p2',
      teamAAttackerId: myId,
      teamADefenderId: 'demo-p3',
      teamBAttackerId: 'demo-p2',
      teamBDefenderId: 'demo-p4',
      status: 'CONFIRMED',
      games: [
        { teamAScore: 10, teamBScore: 8, teamAAttackerId: myId, teamADefenderId: 'demo-p3', teamBAttackerId: 'demo-p2', teamBDefenderId: 'demo-p4' },
        { teamAScore: 6, teamBScore: 10, teamAAttackerId: myId, teamADefenderId: 'demo-p3', teamBAttackerId: 'demo-p2', teamBDefenderId: 'demo-p4' },
        { teamAScore: 10, teamBScore: 5, teamAAttackerId: myId, teamADefenderId: 'demo-p3', teamBAttackerId: 'demo-p2', teamBDefenderId: 'demo-p4' }
      ],
      createdAt: new Date(Date.now() - 3600000 * 24).toISOString(),
      teamAAttackerNickname: myNick,
      teamADefenderNickname: 'Charlie',
      teamBAttackerNickname: 'Bob',
      teamBDefenderNickname: 'Diana',
      teamAAttackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Demo',
      teamADefenderAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Charlie',
      teamBAttackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Bob',
      teamBDefenderAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Diana',
      matchFormat: 'STANDARD'
    },
    {
      id: 'demo-match-3',
      creatorId: 'demo-p1',
      teamAAttackerId: 'demo-p1',
      teamADefenderId: null,
      teamBAttackerId: myId,
      teamBDefenderId: null,
      status: 'CONFIRMED',
      games: [{ teamAScore: 10, teamBScore: 4 }],
      createdAt: new Date(Date.now() - 3600000 * 48).toISOString(),
      teamAAttackerNickname: 'Alice',
      teamBAttackerNickname: myNick,
      teamAAttackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Alice',
      teamBAttackerAvatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=Demo',
      matchFormat: 'STANDARD'
    }
  ]

  return {
    content,
    page: 0,
    size: 10,
    totalElements: content.length,
    totalPages: 1,
    first: true,
    last: true
  }
}


