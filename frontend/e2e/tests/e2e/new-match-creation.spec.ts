import { test, expect } from '@playwright/test';

test.describe('New Match Creation E2E User Journey (ATDD)', () => {
  test.skip('[P0] should navigate from Home Hub to New Match and configure players', async ({ page }) => {
    // Navigate to home hub
    await page.goto('/');
    
    // Tap New Match
    await page.getByRole('button', { name: /new match/i }).click();

    // Verify creation screen opens
    await expect(page).toHaveURL(/.*\/match\/new/);
    await expect(page.getByRole('heading', { name: /new match/i })).toBeVisible();

    // Verify smart defaults are loaded (last rule system and frequent opponents fetch implied by API mocks we would add later)
    await expect(page.getByText('Last Rule System')).toBeVisible();
    await expect(page.getByText('Frequent Opponent 1')).toBeVisible();

    // Change Match Type to 4 Players
    await page.getByRole('combobox', { name: /match type/i }).selectOption('4_PLAYERS');

    // Expect 4 player slots to be available
    await expect(page.getByRole('button', { name: /select player 1/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /select player 2/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /select player 3/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /select player 4/i })).toBeVisible();

    // Complete selection
    await page.getByRole('button', { name: /start match/i }).click();

    // Verify match started successfully
    await expect(page).toHaveURL(/.*\/match\/\d+/);
  });

  test.skip('[P2] should adhere to UI constraints (Portrait optimization, No-Line rule)', async ({ page }) => {
    // Open in a portrait viewport
    await page.setViewportSize({ width: 375, height: 812 });

    await page.goto('/match/new');

    // Verify No-Line rule elements - no visible divider lines
    const divider = page.locator('hr');
    await expect(divider).toHaveCount(0);

    // Verify form elements are visible
    const form = page.getByRole('form');
    await expect(form).toBeVisible();
  });
});
