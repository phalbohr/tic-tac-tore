import { test as base, type Route } from '@playwright/test';
import { PersonalStatsFactory, type PlayerStats } from '../factories/personal-stats.factory';

export interface StatsFixtures {
  statsFactory: PersonalStatsFactory;
  mockStatsResponse: (stats: PlayerStats) => Promise<void>;
  mockStatsError: (status: number, message?: string) => Promise<void>;
  mockStatsLoading: () => Promise<void>;
}

/**
 * Custom Playwright fixtures for Story 4.3: Positional Statistics.
 *
 * <p>Extends the base `test` with stats-specific helpers:
 * - `statsFactory` — PersonalStatsFactory instance for response payloads
 * - `mockStatsResponse` — intercepts `GET /api/v1/statistics/me` with a given payload
 * - `mockStatsError` — intercepts `GET /api/v1/statistics/me` with an error status
 * - `mockStatsLoading` — delays the response to test loading skeleton
 */
export const test = base.extend<StatsFixtures>({
   
   
  statsFactory: async ({ page: _page }, use) => {
    await use(new PersonalStatsFactory());
  },

  mockStatsResponse: async ({ page }, use) => {
    const mockFn = async (stats: PlayerStats) => {
      await page.route('**/api/v1/statistics/me*', async (route: Route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(stats),
        });
      });
    };
    await use(mockFn);
  },

  mockStatsError: async ({ page }, use) => {
    const mockFn = async (status: number, message = 'Internal server error') => {
      await page.route('**/api/v1/statistics/me*', async (route: Route) => {
        await route.fulfill({
          status,
          contentType: 'application/json',
          body: JSON.stringify({ message }),
        });
      });
    };
    await use(mockFn);
  },

  mockStatsLoading: async ({ page }, use) => {
    const mockFn = async () => {
      await page.route('**/api/v1/statistics/me*', async (route: Route) => {
        await new Promise((resolve) => setTimeout(resolve, 200));
        await route.continue();
      });
    };
    await use(mockFn);
  },
});

export { expect } from '@playwright/test';
