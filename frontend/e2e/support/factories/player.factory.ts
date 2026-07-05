import { faker } from '@faker-js/faker';

export interface Player {
  id: string;
  name: string;
  email: string;
  avatarUrl?: string;
  level: 'NOVICE' | 'INTERMEDIATE' | 'PRO' | 'ELITE';
}

export class PlayerFactory {
  create(overrides: Partial<Player> = {}): Player {
    return {
      id: faker.string.uuid(),
      name: faker.person.fullName(),
      email: faker.internet.email(),
      avatarUrl: faker.image.avatar(),
      level: faker.helpers.arrayElement(['NOVICE', 'INTERMEDIATE', 'PRO', 'ELITE']),
      ...overrides,
    };
  }

  createMany(count: number, overrides: Partial<Player> = {}): Player[] {
    return Array.from({ length: count }, () => this.create(overrides));
  }
}
