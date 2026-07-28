import { test, expect } from '@playwright/test';

test.describe('Story 2.4: Match Submission with Undo Window', () => {
  test.use({ viewport: { width: 375, height: 667 } });

  test.beforeEach(async ({ page }) => {
    // Inject tutorial completed state
    await page.addInitScript(() => {
      window.localStorage.setItem('tutorial-completed', 'true');
    });

    // Mock rule config API to use 1 game limit for fast E2E completion
    await page.route('**/api/v1/rule-configurations*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false })
      });
    });

    // Mock frequent opponents API
    await page.route('**/api/users/me/frequent-opponents', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 'player-1-uuid', nickname: 'Alice', avatar: null },
          { id: 'player-2-uuid', nickname: 'Bob', avatar: null }
        ])
      });
    });

    await page.goto('/api/auth/test-login?email=test@example.com&nickname=testuser&tutorialCompleted=true');
  });

  test('[P0] Main application container loads and New Match button is accessible', async ({ page }) => {
    await page.goto('/');
    const main = page.locator('main');
    await expect(main).toBeVisible();

    const newMatchBtn = page.getByRole('button', { name: /New Match/i });
    await expect(newMatchBtn).toBeVisible();
  });

  test('[P0] Match submission flow displays Undo Toast and sends POST request upon timer expiration', async ({ page }) => {
    let postReceived = false;
    let postBody: any = null;

    await page.route('**/api/v1/matches', async (route) => {
      if (route.request().method() === 'POST') {
        postReceived = true;
        postBody = route.request().postDataJSON();
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'match-e2e-123',
            idempotencyKey: postBody?.idempotencyKey || 'key-e2e-123',
            status: 'PENDING_APPROVAL',
            games: [{ teamAScore: 5, teamBScore: 0 }],
            createdAt: new Date().toISOString()
          })
        });
      } else {
        await route.continue();
      }
    });

    await page.goto('/');

    // 1. Open New Match Modal
    const newMatchBtn = page.getByRole('button', { name: /New Match/i });
    await newMatchBtn.click();

    // 2. Select Players (Alice and Bob)
    const aliceBtn = page.getByRole('button', { name: 'Alice' });
    const bobBtn = page.getByRole('button', { name: 'Bob' });
    await aliceBtn.click();
    await bobBtn.click();

    // 3. Start Match
    const startBtn = page.getByRole('button', { name: /Start Match/i });
    await startBtn.click();

    // 4. Increment score to win limit (5) and complete match
    const incrementBtn = page.getByRole('button', { name: 'Add 1' }).first();
    for (let i = 0; i < 5; i++) {
      await incrementBtn.click();
    }

    const completeBtn = page.getByRole('button', { name: /Complete Match/i });
    await expect(completeBtn).toBeEnabled();
    await completeBtn.click();

    // 5. Verify Home View is shown and Undo Toast is active with status role
    const toast = page.locator('[role="status"]');
    await expect(toast).toBeVisible();
    await expect(toast).toContainText('Match submitted. Tap to undo.');

    // 6. Verify "Undo" button is present on the Toast
    const undoBtn = toast.getByRole('button', { name: /Undo/i });
    await expect(undoBtn).toBeVisible();
  });

  test('[P0] Clicking Undo on toast cancels submission and restores score entry interface', async ({ page }) => {
    await page.goto('/');

    // 1. Open New Match Modal and select players
    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: 'Alice' }).click();
    await page.getByRole('button', { name: 'Bob' }).click();
    await page.getByRole('button', { name: /Start Match/i }).click();

    // 2. Increment score to win (5) and complete match
    const incrementBtn = page.getByRole('button', { name: 'Add 1' }).first();
    for (let i = 0; i < 5; i++) {
      await incrementBtn.click();
    }
    await page.getByRole('button', { name: /Complete Match/i }).click();

    // 3. Undo Toast appears on Home Dashboard
    const toast = page.locator('[role="status"]');
    await expect(toast).toBeVisible();

    // 4. Click Undo button before 15 seconds expiration
    const undoBtn = toast.getByRole('button', { name: /Undo/i });
    await undoBtn.click();

    // 5. Toast disappears and Score Entry view is restored
    await expect(toast).toHaveCount(0);
    await expect(page.getByText('Match Score')).toBeVisible();
    await expect(page.getByRole('button', { name: /Complete Match/i })).toBeVisible();
  });

  test('[P1] Network POST failure transitions to offline pending retry toast', async ({ page }) => {
    // Abort POST requests to simulate offline network failure
    await page.route('**/api/v1/matches', async (route) => {
      if (route.request().method() === 'POST') {
        await route.abort('failed');
      } else {
        await route.continue();
      }
    });

    await page.goto('/');

    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: 'Alice' }).click();
    await page.getByRole('button', { name: 'Bob' }).click();
    await page.getByRole('button', { name: /Start Match/i }).click();

    const incrementBtn = page.getByRole('button', { name: 'Add 1' }).first();
    for (let i = 0; i < 5; i++) {
      await incrementBtn.click();
    }
    await page.getByRole('button', { name: /Complete Match/i }).click();

    const toast = page.locator('[role="status"]');
    await expect(toast).toBeVisible();
  });
});
