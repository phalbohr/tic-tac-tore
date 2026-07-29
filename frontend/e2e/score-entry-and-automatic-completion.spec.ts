import { test, expect } from '@playwright/test';

test.describe('Story 2.3: Score Entry & Automatic Completion (ATDD)', () => {
  test.beforeEach(async ({ page }) => {
    // Mock frequent players
    await page.route('**/api/users/me/frequent-opponents', async route => {
      await route.fulfill({ json: [
        { id: 'p1', nickname: 'Player 1' },
        { id: 'p2', nickname: 'Player 2' }
      ] });
    });
    // Default mock for rules to allow proceeding if not overridden
    await page.route('**/{api/rules/*,api/v1/rule-configurations*}', async route => {
      await route.fulfill({ json: { id: 'test-rule', scoreLimit: 10, winsNeeded: 1 } });
    });
    await page.route('**/api/users/me/preferences/last-rule-system', async route => {
      await route.fulfill({ json: { lastRuleSystem: 'STANDARD' } });
    });
  });

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip('[P2] should present score steppers using background shifts (no 1px borders)', async ({ page }) => {
    await page.goto('/match/draft');

    // Force score_entry state via Pinia to bypass player selection
    await page.evaluate(() => {
      // access window.__VUE_APP__ or just simulate clicks?
      // Since it's hard to access pinia directly without exposing it, let's just mock the Start Match button click after mocking the state?
    });

    // To properly click through:
    await page.getByRole('button', { name: 'Player 1' }).click();
    await page.getByRole('button', { name: 'Player 2' }).click();
    await page.getByRole('button', { name: 'Start Match' }).click();

    // Find the score stepper containers
    const steppers = page.locator('.score-stepper');
    
    // Assert there are steppers
    await expect(steppers.first()).toBeVisible();

    // Verify background classes from Tailwind tokens instead of borders
    // UI rule: bg-surface-container-highest layered over bg-surface-container-low, no borders
    await expect(steppers.first()).toHaveClass(/bg-surface-container-highest/);
    await expect(steppers.first()).not.toHaveClass(/border/);
    await expect(steppers.first()).not.toHaveClass(/divide/);
  });

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip('[P1] should hide the +5 stepper when RuleConfiguration scoreLimit < 5', async ({ page }) => {
    // Override the RuleConfiguration API
    await page.route('**/api/rules/*', async route => {
      await route.fulfill({ json: { id: 'test-rule-3', scoreLimit: 3, winsNeeded: 1 } });
    });

    await page.goto('/match/draft');
    await page.getByText('Player 1').click();
    await page.getByText('Player 2').click();
    await page.getByRole('button', { name: 'Start Match' }).click();

    // Verify +1 and -1 are visible
    await expect(page.getByRole('button', { name: '+1' }).first()).toBeVisible();
    await expect(page.getByRole('button', { name: '-1' }).first()).toBeVisible();

    // Verify +5 is NOT visible
    await expect(page.getByRole('button', { name: '+5' })).toBeHidden();
  });

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip('[P0] should automatically complete the game when a player reaches the scoreLimit', async ({ page }) => {
    // Override the RuleConfiguration API
    await page.route('**/api/rules/*', async route => {
      await route.fulfill({ json: { id: 'test-rule-5', scoreLimit: 5, winsNeeded: 2 } });
    });

    await page.goto('/match/draft');
    await page.getByText('Player 1').click();
    await page.getByText('Player 2').click();
    await page.getByRole('button', { name: 'Start Match' }).click();

    // Tap +5 for team 1 (reaches score limit of 5)
    await page.getByRole('button', { name: '+5' }).first().click();

    // Verify game completed automatically (e.g. moves to game 2, or shows game 1 result)
    await expect(page.getByText('Game 1 Complete')).toBeVisible();
    await expect(page.getByText('Game 2')).toBeVisible();
  });

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip('[P0] should automatically advance to submission when match winsNeeded are met', async ({ page }) => {
    // Override the RuleConfiguration API
    await page.route('**/api/rules/*', async route => {
      await route.fulfill({ json: { id: 'test-rule-1', scoreLimit: 1, winsNeeded: 1 } });
    });

    await page.goto('/match/draft');
    await page.getByText('Player 1').click();
    await page.getByText('Player 2').click();
    await page.getByRole('button', { name: 'Start Match' }).click();

    // Tap +1 for team 1 (reaches score limit of 1 and wins the match)
    await page.getByRole('button', { name: '+1' }).first().click();

    // Verify match auto-advances to the submission state / summary screen
    await expect(page.getByText('Match Summary')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Submit Match' })).toBeVisible();
  });
});
