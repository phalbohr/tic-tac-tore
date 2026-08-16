import { faker } from '@faker-js/faker';

export interface LeaderboardEntry {
  playerId: string;
  playerName: string;
  totalMatches: number;
  wins: number;
  losses: number;
  winRate: number;
}

export interface LeaderboardPage {
  content: LeaderboardEntry[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

/**
 * Factory for leaderboard API response fixtures.
 *
 * <p>Story 4.2: Global Leaderboard with Filtering.
 * Produces deterministic {@link LeaderboardPage} payloads for E2E API interception
 * so leaderboard UI assertions do not depend on live database state.
 */
export class LeaderboardFactory {
  private counter = 0;

  createEntry(overrides: Partial<LeaderboardEntry> = {}): LeaderboardEntry {
    this.counter += 1;
    return {
      playerId: faker.string.uuid(),
      playerName: faker.person.fullName(),
      totalMatches: faker.number.int({ min: 5, max: 50 }),
      wins: faker.number.int({ min: 0, max: 20 }),
      losses: faker.number.int({ min: 0, max: 20 }),
      winRate: faker.number.float({ min: 0, max: 1, fractionDigits: 3 }),
      ...overrides,
    };
  }

  createMany(count: number, overrides: Partial<LeaderboardEntry> = {}): LeaderboardEntry[] {
    return Array.from({ length: count }, () => this.createEntry(overrides));
  }

  createPage(overrides: Partial<LeaderboardPage> = {}): LeaderboardPage {
    const content = overrides.content ?? [this.createEntry()];
    const size = overrides.size ?? 20;
    const totalElements = overrides.totalElements ?? content.length;
    return {
      content,
      totalPages: overrides.totalPages ?? (totalElements === 0 ? 0 : Math.max(1, Math.ceil(totalElements / size))),
      totalElements,
      size,
      number: overrides.number ?? 0,
    };
  }

  sortedPage(): LeaderboardPage {
    const content: LeaderboardEntry[] = [
      { playerId: 'p1', playerName: 'Alice', totalMatches: 10, wins: 10, losses: 0, winRate: 1.0 },
      { playerId: 'p2', playerName: 'Bob', totalMatches: 10, wins: 4, losses: 6, winRate: 0.4 },
      { playerId: 'p3', playerName: 'Charlie', totalMatches: 10, wins: 0, losses: 10, winRate: 0.0 },
    ];
    return { content, totalPages: 2, totalElements: 20, size: 10, number: 0 };
  }

  emptyPage(): LeaderboardPage {
    return { content: [], totalPages: 0, totalElements: 0, size: 20, number: 0 };
  }
}
