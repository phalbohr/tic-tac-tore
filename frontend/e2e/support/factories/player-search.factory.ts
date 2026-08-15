export interface PlayerSearchResult {
  id: string;
  nickname: string;
  avatar: string | null;
}

export class PlayerSearchFactory {
  create(overrides: Partial<PlayerSearchResult> = {}): PlayerSearchResult {
    return {
      id: overrides.id ?? `player-${Math.random().toString(36).slice(2, 8)}`,
      nickname: overrides.nickname ?? 'Test Player',
      avatar: overrides.avatar ?? null,
      ...overrides,
    };
  }

  createMany(count: number, overrides: Partial<PlayerSearchResult> = {}): PlayerSearchResult[] {
    return Array.from({ length: count }, (_, i) =>
      this.create({ ...overrides, nickname: `${overrides.nickname || 'Player'} ${i + 1}` })
    );
  }

  createFrequentOpponent(overrides: Partial<PlayerSearchResult> = {}): PlayerSearchResult {
    return this.create({ id: overrides.id ?? `frequent-${Math.random().toString(36).slice(2, 8)}`, ...overrides });
  }
}
