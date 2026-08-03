import { test, expect } from '@playwright/test';

test.describe('Editing Rejected Match Flow', () => {
  test.use({ viewport: { width: 375, height: 667 } });

  test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
      window.localStorage.setItem('tutorial-completed', 'true');
    });

    await page.route('**/api/v1/matches/pending', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          matches: [
            {
              id: 'rejected-match-1',
              status: 'REJECTED',
              rejectionReason: 'Wrong score',
              creatorNickname: 'Alice',
              teamAAttackerId: 'p1',
              teamBAttackerId: 'p2',
              games: [{ teamAScore: 10, teamBScore: 5 }],
              createdAt: new Date().toISOString()
            }
          ]
        })
      });
    });

    await page.goto('/api/auth/test-login?email=creator@example.com&nickname=CreatorUser&tutorialCompleted=true');
  });

  test('Editing rejected match and canceling preserves the rejected match', async ({ page }) => {
    await page.goto('/');

    // 1. Verify rejected match card is visible
    const editBtn = page.getByTestId('edit-rejection-btn-rejected-match-1');
    await expect(editBtn).toBeVisible();

    // 2. Click Edit Match
    await editBtn.click();

    // 3. Score Entry interface should open
    await expect(page.getByText('Match Score')).toBeVisible();

    // 4. Click Cancel or Back
    const cancelBtn = page.getByRole('button', { name: 'Cancel' });
    await cancelBtn.click();

    const confirmCancelBtn = page.getByRole('button', { name: 'Confirm Cancel' });
    await confirmCancelBtn.click();

    // 5. User returns to Home View and rejected match is still present
    await expect(page.getByTestId('edit-rejection-btn-rejected-match-1')).toBeVisible();
  });
});
