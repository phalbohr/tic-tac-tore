import { test, expect } from '@playwright/test';
import { MatchFixtures } from './fixtures/match-fixtures';

test.describe('Epic 2.3: Score Entry & Automatic Completion', () => {
  let fixtures: MatchFixtures;

  test.beforeEach(async ({ page }) => {
    fixtures = new MatchFixtures(page);
    await fixtures.navigateAndInitializeMatch();
  });

  test('P0: Score Limit Progression and Auto-Completion', async ({ page }) => {
    // Intercept rules API to return scoreLimit 10, gameLimit 1
    await fixtures.mockRulesEndpoint({ scoreLimit: 10, winsNeeded: 1 });
    await fixtures.proceedToScoreEntry();

    // Verify initial state
    await expect(page.locator('text=Match Score')).toBeVisible();
    await expect(page.getByText('0 - 0', { exact: true })).toBeVisible();

    const team1Plus1 = page.locator('.score-stepper-btn.plus-1').first();
    const team1Plus5 = page.locator('.score-stepper-btn.plus-5').first();

    // Increment Team 1 by 5
    await team1Plus5.click();
    await expect(page.locator('.score-display').first()).toHaveText('5');

    // Increment 4 more times using +1
    for (let i = 0; i < 4; i++) {
      await team1Plus1.click();
    }
    await expect(page.locator('.score-display').first()).toHaveText('9');

    // Final point to reach score limit of 10
    await team1Plus1.click();

    // Since winsNeeded is 1, reaching 10 should auto-complete the match
    // Check if transition to 'ready_for_submission' occurred (e.g., summary view)
    await expect(page.locator('text=Match Summary')).toBeVisible({ timeout: 5000 });
  });

  test('P1: +5 Stepper is hidden when scoreLimit < 5', async ({ page }) => {
    // Intercept rules API to return scoreLimit 3
    await fixtures.mockRulesEndpoint({ scoreLimit: 3, winsNeeded: 1 });
    await fixtures.proceedToScoreEntry();

    // Verify +5 stepper is NOT visible
    await expect(page.locator('.score-stepper-btn.plus-5')).not.toBeVisible();
    // +1 should still be visible
    await expect(page.locator('.score-stepper-btn.plus-1').first()).toBeVisible();
  });

  test('P1: API Error Fallback to Standard Rules', async ({ page }) => {
    // Intercept rules API to fail with 500
    await fixtures.mockRulesEndpointError(500);
    await fixtures.proceedToScoreEntry();

    // The app should fallback to Standard Rules seamlessly (limit 10)
    await expect(page.locator('.score-stepper-btn.plus-5').first()).toBeVisible();
    
    // Add 10 points
    const team2Plus5 = page.locator('.score-stepper-btn.plus-5').nth(1);
    await team2Plus5.click();
    await team2Plus5.click();
    
    // Check if match completed (default standard rules usually win by 1 game limit)
    await expect(page.locator('text=Match Summary')).toBeVisible({ timeout: 5000 });
  });

  test('P2: 2v2 Format Combines Player Names', async ({ page }) => {
    await fixtures.mockRulesEndpoint({ scoreLimit: 10, winsNeeded: 1 });
    // Initialize a 2v2 match specifically
    await fixtures.setupTwoVsTwoMatch(['p1', 'p2', 'p3', 'p4']);
    await fixtures.proceedToScoreEntry();

    // Validate Team 1 name format
    await expect(page.getByText('Player p1 & Player p2')).toBeVisible();
    // Validate Team 2 name format
    await expect(page.getByText('Player p3 & Player p4')).toBeVisible();
  });

  test('P2: Visual Styling and No-Line Rule Compliance', async ({ page }) => {
    await fixtures.mockRulesEndpoint({ scoreLimit: 10, winsNeeded: 1 });
    await fixtures.proceedToScoreEntry();

    // Ensure there are no 1px borders used (checking computed styles for main elements)
    const stepperContainer = page.locator('.bg-surface-container-low');
    const borderStyle = await stepperContainer.evaluate((el) => {
      const style = window.getComputedStyle(el);
      return style.borderWidth;
    });

    expect(borderStyle).toBe('0px');
  });
});
