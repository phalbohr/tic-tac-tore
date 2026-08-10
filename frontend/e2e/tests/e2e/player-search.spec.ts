import { test, expect, type Page, type Route } from '@playwright/test';
import { loginAsTestUser } from './helpers/auth';

interface PlayerSearchResult {
  id: string;
  nickname: string;
  avatar: string | null;
}

function mockSearchApi(page: Page) {
  return page.route('**/api/users/me/players/search', async (route) => {
    const url = new URL(route.request().url());
    const query = url.searchParams.get('q') || '';

    if (!query.trim()) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    const lowerQuery = query.toLowerCase();
    const mockPlayers: PlayerSearchResult[] = [
      { id: 'player-search-1', nickname: 'Alice', avatar: 'avatar-alice' },
      { id: 'player-search-2', nickname: 'Alicia', avatar: 'avatar-alicia' },
      { id: 'player-search-3', nickname: 'Bob', avatar: 'avatar-bob' },
    ];

    const filtered = mockPlayers.filter((p) =>
      p.nickname.toLowerCase().includes(lowerQuery)
    );

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(filtered),
    });
  });
}

test.describe('Story 2.7: Global Player Search & Selection E2E', () => {
  test.use({ viewport: { width: 375, height: 667 } });

  test.beforeEach(async ({ page }) => {
    await loginAsTestUser(page);
    await mockSearchApi(page);
  });

  test('[P0] AC2: Should open search overlay and find player by nickname', async ({ page }) => {
    await page.goto('/');

    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: /2v2/i }).click();

    const emptySlot = page.locator('.player-slot').first();
    await emptySlot.getByRole('button', name: /search/i).click();

    const overlay = page.locator('[data-testid="player-search-overlay"]');
    await expect(overlay).toBeVisible();

    const searchInput = page.locator('[data-testid="player-search-input"]');
    await expect(searchInput).toBeFocused();

    await searchInput.fill('Ali');
    await expect(searchInput).toHaveValue('Ali');

    const resultRow = page.locator('[data-testid="search-result-row"]').first();
    await expect(resultRow).toBeVisible();
    await expect(resultRow).toContainText('Alice');
  });

  test('[P0] AC4: Should select player from search and close overlay', async ({ page }) => {
    await page.goto('/');

    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: /2v2/i }).click();

    const emptySlot = page.locator('.player-slot').first();
    await emptySlot.getByRole('button', name: /search/i).click();

    const searchInput = page.locator('[data-testid="player-search-input"]');
    await searchInput.fill('Ali');

    const aliceRow = page.locator('[data-testid="search-result-row"]').filter({ hasText: 'Alice' });
    await aliceRow.click();

    await expect(page.locator('[data-testid="player-search-overlay"]')).not.toBeVisible();

    const firstSlot = page.locator('.player-slot').first();
    await expect(firstSlot).toContainText('Alice');
  });

  test('[P1] AC3: Should order frequent opponents before alphabetical results', async ({ page }) => {
    await page.route('**/api/users/me/frequent-opponents', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 'frequent-1', nickname: 'Frank', avatar: null },
        ]),
      });
    });

    await page.goto('/');

    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: /2v2/i }).click();

    const emptySlot = page.locator('.player-slot').first();
    await emptySlot.getByRole('button', name: /search/i).click();

    const searchInput = page.locator('[data-testid="player-search-input"]');
    await searchInput.fill('A');

    const rows = page.locator('[data-testid="search-result-row"]');
    await expect(rows).toHaveCount(2);

    await expect(rows.nth(0)).toContainText('Frank');
    await expect(rows.nth(1)).toContainText('Alice');
  });

  test('[P0] AC6: Should display error when search API returns 500', async ({ page }) => {
    await page.route('**/api/users/me/players/search', async (route) => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Internal Server Error' }),
      });
    });

    await page.goto('/');

    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: /2v2/i }).click();

    const emptySlot = page.locator('.player-slot').first();
    await emptySlot.getByRole('button', name: /search/i).click();

    const searchInput = page.locator('[data-testid="player-search-input"]');
    await searchInput.fill('Ali');

    const errorBanner = page.locator('[data-testid="search-error"]');
    await expect(errorBanner).toBeVisible();
    await expect(errorBanner).toContainText('Search service unavailable');
  });

  test('[P0] AC1: Should close overlay on Escape key without selection', async ({ page }) => {
    await page.goto('/');

    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: /2v2/i }).click();

    const emptySlot = page.locator('.player-slot').first();
    await emptySlot.getByRole('button', name: /search/i).click();

    await expect(page.locator('[data-testid="player-search-overlay"]')).toBeVisible();

    await page.keyboard.press('Escape');

    await expect(page.locator('[data-testid="player-search-overlay"]')).not.toBeVisible();
  });
});
