import { test, expect } from '@playwright/test';

test.describe('Score Entry & Automatic Completion', () => {

  test.skip('score steppers are presented without 1px borders', async ({ page }) => {
    // Arrange: Start a match draft
    await page.goto('/new-match');
    // ... complete player selection
    
    // Act: View score entry screen
    await expect(page.getByTestId('score-entry-view')).toBeVisible();

    // Assert: Verify stepper UI does not have 1px borders (class check or style check)
    const stepper = page.getByTestId('score-stepper-t1');
    await expect(stepper).not.toHaveClass(/border/);
    await expect(stepper).not.toHaveClass(/divide-y/);
  });

  test.skip('+5 stepper is hidden when score limit is < 5', async ({ page }) => {
    // Arrange: Mock rule system configuration with score_limit < 5
    await page.route('**/api/rules/**', route => {
      route.fulfill({ json: { score_limit: 3, wins_needed: 2 } });
    });
    
    // Act: Navigate to score entry
    await page.goto('/match/score-entry');
    
    // Assert: +5 stepper should be hidden
    await expect(page.getByTestId('btn-plus-5-t1')).toBeHidden();
  });

  test.skip('game automatically completes when score reaches limit', async ({ page }) => {
    // Arrange: Match with score limit 5
    await page.route('**/api/rules/**', route => {
      route.fulfill({ json: { score_limit: 5, wins_needed: 2 } });
    });
    await page.goto('/match/score-entry');

    // Act: Tap +5 button
    await page.getByTestId('btn-plus-5-t1').click();

    // Assert: Game completes, next game starts or screen updates
    await expect(page.getByTestId('game-complete-banner')).toBeVisible();
    await expect(page.getByTestId('current-game-indicator')).toHaveText('Game 2');
  });

  test.skip('match automatically advances to submission when win conditions are met', async ({ page }) => {
    // Arrange: Match with score limit 5, wins needed 1
    await page.route('**/api/rules/**', route => {
      route.fulfill({ json: { score_limit: 5, wins_needed: 1 } });
    });
    await page.goto('/match/score-entry');

    // Act: Tap +5 button to win the game, thereby winning the match
    await page.getByTestId('btn-plus-5-t1').click();

    // Assert: Match advances to submission state/view
    await expect(page.url()).toContain('/match/submit');
    await expect(page.getByTestId('match-summary')).toBeVisible();
  });
});
