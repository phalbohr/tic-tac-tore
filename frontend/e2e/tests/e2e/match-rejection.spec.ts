import { test, expect } from '@playwright/test';

test.describe('Story 3.3: Match Rejection with Reason', () => {
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
              id: 'match-3-3-reject-test',
              creatorNickname: 'Alice',
              teamAAttackerNickname: 'Alice',
              teamBAttackerNickname: 'Bob',
              games: [{ teamAScore: 10, teamBScore: 5 }],
              createdAt: new Date().toISOString()
            }
          ]
        })
      });
    });

    await page.goto('/api/auth/test-login?email=testopponent@example.com&nickname=OpponentUser&tutorialCompleted=true');
  });

  test('[P0] Tapping Reject opens RejectReasonSelector modal with disabled submit button', async ({ page }) => {
    await page.goto('/');

    const card = page.getByTestId('pending-match-card-match-3-3-reject-test');
    await expect(card).toBeVisible();

    const rejectBtn = page.getByTestId('reject-match-btn-match-3-3-reject-test');
    await expect(rejectBtn).toBeVisible();
    await rejectBtn.click();

    const modalTitle = page.getByTestId('rejection-dialog-title');
    await expect(modalTitle).toBeVisible();

    const submitBtn = page.getByTestId('submit-rejection-btn');
    await expect(submitBtn).toBeDisabled();
  });

  test('[P0] Selecting a reason enables submit button and sends POST /api/v1/matches/{id}/reject', async ({ page }) => {
    let rejectApiCalled = false;
    let requestPayload: any = null;

    await page.route('**/api/v1/matches/match-3-3-reject-test/reject', async (route) => {
      rejectApiCalled = true;
      requestPayload = JSON.parse(route.request().postData() || '{}');
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'match-3-3-reject-test',
          status: 'REJECTED',
          rejectedByUserId: 'opponent-uuid',
          rejectionReason: 'Wrong score'
        })
      });
    });

    await page.goto('/');

    const rejectBtn = page.getByTestId('reject-match-btn-match-3-3-reject-test');
    await rejectBtn.click();

    const wrongScoreRadio = page.getByRole('radio', { name: 'Wrong score' });
    await wrongScoreRadio.check();

    const submitBtn = page.getByTestId('submit-rejection-btn');
    await expect(submitBtn).toBeEnabled();
    await submitBtn.click();

    expect(rejectApiCalled).toBe(true);
    expect(requestPayload).toEqual({
      reason: 'Wrong score',
      customReason: ''
    });

    const card = page.getByTestId('pending-match-card-match-3-3-reject-test');
    await expect(card).not.toBeVisible();
  });

  test('[P1] Server 400/409 race condition displays alert toast', async ({ page }) => {
    await page.route('**/api/v1/matches/match-3-3-reject-test/reject', async (route) => {
      await route.fulfill({
        status: 400,
        contentType: 'application/json',
        body: JSON.stringify({
          message: 'Match was already processed by another opponent'
        })
      });
    });

    await page.goto('/');

    const rejectBtn = page.getByTestId('reject-match-btn-match-3-3-reject-test');
    await rejectBtn.click();

    const wrongScoreRadio = page.getByRole('radio', { name: 'Wrong score' });
    await wrongScoreRadio.check();

    const submitBtn = page.getByTestId('submit-rejection-btn');
    await submitBtn.click();

    const errorToast = page.getByTestId('error-toast');
    await expect(errorToast).toBeVisible();
    await expect(errorToast).toContainText('Match was already processed by another opponent');
  });
});
