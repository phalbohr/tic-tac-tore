import { test, expect, type Route } from '@playwright/test';
import { loginAsTestUser } from './helpers/auth';
import { LeaderboardFactory } from '../../support/factories/leaderboard.factory';

const factory = new LeaderboardFactory();

test.describe('Story 4.2: Global Leaderboard with Filtering (E2E)', () => {
  test.use({ viewport: { width: 375, height: 667 } });

  test.beforeEach(async ({ page }) => {
    await loginAsTestUser(page);
  });

  test('[P0] Should display ranked leaderboard sorted by win rate', async ({ page }) => {
    const sortedPage = factory.sortedPage();

    await page.route('**/api/v1/statistics/leaderboard*', async (route: Route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(sortedPage),
      });
    });

    await page.goto('/leaderboard');

    await expect(page.getByRole('heading', { name: /Leaderboard/i })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Alice', exact: true })).toBeVisible();
    await expect(page.getByRole('cell', { name: '100.0%', exact: true })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Bob', exact: true })).toBeVisible();
    await expect(page.getByRole('cell', { name: '40.0%', exact: true })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Charlie', exact: true })).toBeVisible();
    await expect(page.getByRole('cell', { name: '0.0%', exact: true })).toBeVisible();
  });

  test('[P1] Should pass minMatches=5 by default in the first request', async ({ page }) => {
    const requestUrls: string[] = [];
    await page.route('**/api/v1/statistics/leaderboard*', async (route: Route) => {
      requestUrls.push(route.request().url());
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(factory.emptyPage()),
      });
    });

    await page.goto('/leaderboard');
    await expect(page.getByText('No players match the current filters.')).toBeVisible();

    await expect.poll(() => requestUrls.length).toBeGreaterThan(0);
    expect(requestUrls[0]).toContain('minMatches=5');
  });

  test('[P1] Should refetch with matchFormat filter when format select changes', async ({ page }) => {
    const requestUrls: string[] = [];
    await page.route('**/api/v1/statistics/leaderboard*', async (route: Route) => {
      requestUrls.push(route.request().url());
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(factory.emptyPage()),
      });
    });

    await page.goto('/leaderboard');
    await expect(page.getByText('No players match the current filters.')).toBeVisible();

    const formatSelect = page.getByRole('combobox').first();
    await formatSelect.selectOption({ label: 'Random' });
    await page.waitForLoadState('networkidle');

    expect(requestUrls[requestUrls.length - 1]).toContain('matchFormat=RANDOM');
  });

  test('[P1] Should paginate to the next page when Next is clicked', async ({ page }) => {
    const multiPage = factory.createPage({
      content: [factory.createEntry({ playerName: 'Alice', winRate: 1.0 })],
      totalPages: 2,
      totalElements: 12,
      size: 10,
      number: 0,
    });

    const requestUrls: string[] = [];
    await page.route('**/api/v1/statistics/leaderboard*', async (route: Route) => {
      requestUrls.push(route.request().url());
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(multiPage),
      });
    });

    await page.goto('/leaderboard');
    await expect(page.getByText('Page 1 of 2')).toBeVisible();

    await page.getByRole('button', { name: 'Next' }).click();
    await page.waitForLoadState('networkidle');

    expect(requestUrls[requestUrls.length - 1]).toContain('page=1');
  });

  test('[P1] Should show empty state when no players match filters', async ({ page }) => {
    await page.route('**/api/v1/statistics/leaderboard*', async (route: Route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(factory.emptyPage()),
      });
    });

    await page.goto('/leaderboard');
    await expect(page.getByText('No players match the current filters.')).toBeVisible();
  });
});
