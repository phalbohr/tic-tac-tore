import { faker } from '@faker-js/faker';
import { type Player, PlayerFactory } from './player.factory';

export interface Match {
  id: string;
  players: {
    team1: Player[];
    team2: Player[];
  };
  score: {
    team1: number;
    team2: number;
  };
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED';
  recordedAt: Date;
  tableId?: string;
}

export class MatchFactory {
  private playerFactory = new PlayerFactory();

  create(overrides: Partial<Match> = {}): Match {
    const team1 = overrides.players?.team1 || this.playerFactory.createMany(2);
    const team2 = overrides.players?.team2 || this.playerFactory.createMany(2);

    return {
      id: faker.string.uuid(),
      players: { team1, team2 },
      score: {
        team1: faker.number.int({ min: 0, max: 10 }),
        team2: faker.number.int({ min: 0, max: 10 }),
      },
      status: 'PENDING',
      recordedAt: new Date(),
      tableId: faker.string.alphanumeric(5).toUpperCase(),
      ...overrides,
    };
  }
}
