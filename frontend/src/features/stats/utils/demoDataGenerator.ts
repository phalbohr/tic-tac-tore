import type { PlayerStats } from '@/services/statisticsService'

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
