import type { PlayerStats, TeamPairStats, Page } from '@/services/statisticsService'

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
