import { test as base } from '@playwright/test';
import { PlayerFactory } from '../factories/player.factory';
import { MatchFactory } from '../factories/match.factory';
import { interceptNetworkCall } from '../helpers/network';

/**
 * Custom fixtures for Tic-Tac-Tore E2E tests.
 */
export const test = base.extend<{
  playerFactory: PlayerFactory;
  matchFactory: MatchFactory;
  interceptNetworkCall: typeof interceptNetworkCall;
}>({
  playerFactory: async ({}, use) => {
    await use(new PlayerFactory());
  },
  matchFactory: async ({}, use) => {
    await use(new MatchFactory());
  },
  interceptNetworkCall: async ({}, use) => {
    await use(interceptNetworkCall);
  },
});

export { expect } from '@playwright/test';
