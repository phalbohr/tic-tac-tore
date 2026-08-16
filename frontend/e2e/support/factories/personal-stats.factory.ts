import { faker } from '@faker-js/faker';

export interface PositionStats {
  matches: number;
  wins: number;
  losses: number;
  winRate: number;
}

export interface PlayerStats {
  playerId: string;
  playerName: string;
  overall: PositionStats;
  attacker: PositionStats;
  defender: PositionStats;
}

/**
 * Factory for personal-stats API response payloads.
 *
 * <p>Story 4.3: Positional Statistics (Attack vs. Defense).
 * Generates deterministic-friendly `PlayerStats` payloads for E2E API
 * interception and response-shape validation. Supports overrides for
 * scenario-specific assertions (win-rate scale, 0-match state, 2v2 positions).
 */
export class PersonalStatsFactory {
  private counter = 0;

  create(overrides: Partial<PlayerStats> = {}): PlayerStats {
    this.counter += 1;
    const wins = faker.number.int({ min: 0, max: 20 });
    const losses = faker.number.int({ min: 0, max: 20 });
    const matches = wins + losses;
    const winRate = matches > 0 ? parseFloat(((wins / matches) * 100).toFixed(1)) : 0;

    return {
      playerId: faker.string.uuid(),
      playerName: faker.person.fullName(),
      overall: { matches, wins, losses, winRate },
      attacker: { matches: faker.number.int({ min: 0, max: matches }), wins, losses, winRate },
      defender: { matches: faker.number.int({ min: 0, max: matches }), wins, losses, winRate },
      ...overrides,
    };
  }

  createPositionStats(overrides: Partial<PositionStats> = {}): PositionStats {
    const wins = overrides.wins ?? faker.number.int({ min: 0, max: 20 });
    const losses = overrides.losses ?? faker.number.int({ min: 0, max: 20 });
    const matches = overrides.matches ?? wins + losses;
    const winRate = overrides.winRate ?? (matches > 0 ? parseFloat(((wins / matches) * 100).toFixed(1)) : 0);
    return { matches, wins, losses, winRate };
  }

  createZeroStats(playerId?: string, playerName?: string): PlayerStats {
    const empty: PositionStats = { matches: 0, wins: 0, losses: 0, winRate: 0 };
    return {
      playerId: playerId ?? faker.string.uuid(),
      playerName: playerName ?? faker.person.fullName(),
      overall: { ...empty },
      attacker: { ...empty },
      defender: { ...empty },
    };
  }

  createOneVOneStats(
    playerId: string,
    playerName: string,
    wins: number,
    losses: number
  ): PlayerStats {
    const matches = wins + losses;
    const winRate = matches > 0 ? parseFloat(((wins / matches) * 100).toFixed(1)) : 0;
    const positionStats = { matches, wins, losses, winRate };
    return {
      playerId,
      playerName,
      overall: { ...positionStats },
      attacker: { ...positionStats },
      defender: { matches: 0, wins: 0, losses: 0, winRate: 0 },
    };
  }

  createTwoVTwoStats(
    playerId: string,
    playerName: string,
    isAttacker: boolean,
    wins: number,
    losses: number
  ): PlayerStats {
    const matches = wins + losses;
    const winRate = matches > 0 ? parseFloat(((wins / matches) * 100).toFixed(1)) : 0;
    const positionStats = { matches, wins, losses, winRate };
    const empty: PositionStats = { matches: 0, wins: 0, losses: 0, winRate: 0 };
    return {
      playerId,
      playerName,
      overall: { ...positionStats },
      attacker: isAttacker ? { ...positionStats } : { ...empty },
      defender: isAttacker ? { ...empty } : { ...positionStats },
    };
  }

  createTiedMatchStats(playerId: string, playerName: string, matchCount: number): PlayerStats {
    const empty: PositionStats = { matches: matchCount, wins: 0, losses: 0, winRate: 0 };
    return {
      playerId,
      playerName,
      overall: { ...empty },
      attacker: { ...empty },
      defender: { ...empty },
    };
  }

  createDemoStats(): PlayerStats {
    return {
      playerId: 'demo-user-123',
      playerName: 'Demo Player',
      overall: { matches: 42, wins: 28, losses: 14, winRate: 66.7 },
      attacker: { matches: 22, wins: 16, losses: 6, winRate: 72.7 },
      defender: { matches: 20, wins: 12, losses: 8, winRate: 60.0 },
    };
  }
}
