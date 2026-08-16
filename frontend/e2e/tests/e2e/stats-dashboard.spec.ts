import { type Page } from '@playwright/test';
import { test, expect } from '../../support/fixtures/stats-fixture';
import { loginAsTestUser } from './helpers/auth';

const mockProfile = async (page: Page) => {
  await page.route('**/api/v1/profile/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 'user-uuid-1',
        nickname: 'testuser',
        avatar: null,
        language: 'en',
        tutorialCompleted: true,
      }),
    });
  });
};

const mockPendingMatches = async (page: Page) => {
  await page.route('**/api/v1/matches/pending', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ matches: [] }),
    });
  });
};

const disableDemoMode = async (page: Page) => {
  await page.addInitScript(() => {
    window.localStorage.setItem('tutorial-completed', 'true');
    window.localStorage.setItem('tictactore.demoModeEnabled_guest', 'false');
    window.localStorage.setItem('tictactore.demoModeEnabled_testuser', 'false');
  });
};

const enableDemoMode = async (page: Page) => {
  await page.addInitScript(() => {
    window.localStorage.setItem('tutorial-completed', 'true');
    window.localStorage.setItem('tictactore.demoModeEnabled_guest', 'true');
    window.localStorage.setItem('tictactore.demoModeEnabled_testuser', 'true');
  });
};

test.describe('Story 4.3: Stats Dashboard — Positional Statistics E2E', () => {
  test.use({ viewport: { width: 375, height: 667 } });

  test.describe('Authenticated user with confirmed matches (real data)', () => {
    test.beforeEach(async ({ page }) => {
      await disableDemoMode(page);
      await mockProfile(page);
      await mockPendingMatches(page);
      await loginAsTestUser(page);
    });

    test('[P0] Should display Overall, Attacker, Defender cards with matches, W/L, and proportional win-rate bars', async ({ page, statsFactory, mockStatsResponse }) => {
      const stats = statsFactory.createOneVOneStats('user-uuid-1', 'testuser', 3, 1);
      await mockStatsResponse(stats);
      await page.reload({ waitUntil: 'networkidle' });

      await expect(page.getByText('My Statistics')).toBeVisible();
      await expect(page.getByText('Overall', { exact: true })).toBeVisible();
      await expect(page.getByText('Attacker', { exact: true })).toBeVisible();
      await expect(page.getByText('Defender', { exact: true })).toBeVisible();

      const overallCard = page.getByText('Overall', { exact: true }).locator('..');
      await expect(overallCard.getByText(stats.overall.matches.toString())).toBeVisible();
      await expect(overallCard.getByText(`W: ${stats.overall.wins} L: ${stats.overall.losses}`)).toBeVisible();
      await expect(overallCard.getByText('75.0%')).toBeVisible();

      const bars = page.locator('.ch-stat-bar-fill');
      expect(await bars.count()).toBe(3);
      await expect(bars.nth(0)).toHaveAttribute('style', /width: 75%/);
      await expect(bars.nth(1)).toHaveAttribute('style', /width: 75%/);
      await expect(bars.nth(2)).toHaveAttribute('style', /width: 0%/);
    });

    test('[P0] Should show zeroed stat cards with 0% bars and no NaN for 0-match user', async ({ page, statsFactory, mockStatsResponse }) => {
      const stats = statsFactory.createTiedMatchStats('user-uuid-1', 'testuser', 5);
      await mockStatsResponse(stats);
      await page.reload({ waitUntil: 'networkidle' });

      const overallCard = page.getByText('Overall', { exact: true }).locator('..');
      await expect(overallCard.getByText('W: 0 L: 0')).toBeVisible();
      await expect(overallCard.getByText('0.0%')).toBeVisible();

      const bars = page.locator('.ch-stat-bar-fill');
      expect(await bars.count()).toBe(3);
      for (let i = 0; i < 3; i++) {
        const style = await bars.nth(i).getAttribute('style');
        expect(style).toContain('width: 0%');
        expect(style).not.toContain('NaN');
      }
    });

    test('[P1] Should cap bar width at 100% when winRate exceeds 100', async ({ page, statsFactory, mockStatsResponse }) => {
      const stats = statsFactory.createOneVOneStats('user-uuid-1', 'testuser', 5, 0);
      await mockStatsResponse({
        ...stats,
        overall: { ...stats.overall, winRate: 150.0 },
        attacker: { ...stats.attacker, winRate: 150.0 },
        defender: { ...stats.defender, winRate: 150.0 },
      });
      await page.reload({ waitUntil: 'networkidle' });

      const bars = page.locator('.ch-stat-bar-fill');
      expect(await bars.count()).toBe(3);
      for (let i = 0; i < 3; i++) {
        const style = await bars.nth(i).getAttribute('style');
        expect(style).toContain('width: 100%');
      }
    });

    test('[P1] Should apply bg-secondary to attacker bar and bg-primary to overall/defender bars', async ({ page, statsFactory, mockStatsResponse }) => {
      const stats = statsFactory.createOneVOneStats('user-uuid-1', 'testuser', 3, 2);
      await mockStatsResponse(stats);
      await page.reload({ waitUntil: 'networkidle' });

      const bars = page.locator('.ch-stat-bar-fill');
      expect(await bars.count()).toBe(3);
      expect(await bars.nth(0).evaluate((el) => el.classList.contains('bg-primary'))).toBe(true);
      expect(await bars.nth(1).evaluate((el) => el.classList.contains('bg-secondary'))).toBe(true);
      expect(await bars.nth(2).evaluate((el) => el.classList.contains('bg-primary'))).toBe(true);
    });

    test('[P1] Should display win-rate percentage with one decimal place', async ({ page, statsFactory, mockStatsResponse }) => {
      const stats = statsFactory.createOneVOneStats('user-uuid-1', 'testuser', 2, 1);
      await mockStatsResponse(stats);
      await page.reload({ waitUntil: 'networkidle' });

      await expect(page.getByText('66.7%', { exact: true }).first()).toBeVisible();
    });
  });

  test.describe('Loading and error states', () => {
    test.beforeEach(async ({ page }) => {
      await disableDemoMode(page);
      await mockProfile(page);
      await mockPendingMatches(page);
      await loginAsTestUser(page);
    });

    test('[P1] Should show loading skeleton while fetching stats', async ({ page, mockStatsLoading }) => {
      await mockStatsLoading();
      await page.reload();

      await expect(page.locator('.animate-pulse')).toBeVisible();
      await page.waitForLoadState('networkidle');
      await expect(page.locator('.animate-pulse')).toBeHidden();
    });

    test('[P1] Should show error message when stats API fails', async ({ page, mockStatsError }) => {
      await mockStatsError(500, 'Database unavailable during aggregation');
      await page.reload({ waitUntil: 'networkidle' });

      await expect(page.getByText('Unable to load statistics.')).toBeVisible();
    });
  });

  test.describe('Demo data path', () => {
    test.beforeEach(async ({ page }) => {
      await enableDemoMode(page);
      await mockProfile(page);
      await mockPendingMatches(page);
      await loginAsTestUser(page);
    });

    test('[P2] Should render demo data when user has fewer than 5 matches and demo mode is enabled', async ({ page }) => {
      await page.reload({ waitUntil: 'networkidle' });

      await expect(page.getByText('Demo Data Active')).toBeVisible();
      await expect(page.getByText('W: 28 L: 14')).toBeVisible();
    });
  });

  test.describe('Authenticated flow navigation', () => {
    test('[P3] Should navigate to home page and display stats dashboard for authenticated user', async ({ page }) => {
      await loginAsTestUser(page);
      await page.waitForLoadState('networkidle');

      await page.goto('/');
      await page.waitForLoadState('networkidle');

      const hasStatsSection = await page.getByText('My Statistics').isVisible().catch(() => false);
      const hasStatsCards = await page.getByText('Overall').isVisible().catch(() => false) ||
        await page.getByText('Attacker').isVisible().catch(() => false);
      const hasDemoBanner = await page.getByText('Demo Data Active').isVisible().catch(() => false);

      expect(hasStatsSection || hasStatsCards || hasDemoBanner).toBe(true);
    });
  });
});
