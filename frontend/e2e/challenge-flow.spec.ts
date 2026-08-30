import { test, expect, type Page } from '@playwright/test';

/**
 * Story 6.6: Challenge Player or Group E2E Scaffolds (TDD Red Phase)
 */

async function loginUser(page: Page, prefix = 'challenge') {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-${prefix}-${randomSuffix}@example.com`;
  const nickname = `Player${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('[Story 6.6] Challenge Player or Group E2E Scaffolds (ATDD Red Phase)', () => {

  test.skip('[P0] Authenticated user creates 1v1 challenge from Leaderboard (AC 1, AC 2)', async ({ page }) => {
    await loginUser(page, 'challenger');

    // 1. Navigate to Leaderboard
    await page.goto('/stats');

    // 2. Click Challenge button on another player row
    const challengeBtn = page.locator('[data-test^="challenge-player-"]').first();
    await expect(challengeBtn).toBeVisible();
    await challengeBtn.click();

    // 3. ChallengeModal should open
    const modal = page.locator('[data-test="challenge-modal"]');
    await expect(modal).toBeVisible();

    // 4. Fill custom message and select 1v1
    await page.locator('[data-test="match-type-1v1"]').click();
    await page.locator('[data-test="challenge-message-input"]').fill('Ready for a duel?');

    // 5. Submit challenge
    await page.locator('[data-test="submit-challenge-btn"]').click();
    await expect(modal).not.toBeVisible();

    // 6. Verify toast notification / pending challenge reflected
    await expect(page.locator('[data-test="toast-success"]')).toBeVisible();
  });

  test.skip('[P0] Target user accepts incoming challenge on Home Hub (AC 2, AC 3)', async ({ page }) => {
    // 1. Mock incoming challenge on home hub
    await page.route('**/api/v1/challenges/incoming', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'chal-e2e-1',
            challengerId: 'c1',
            challengerNickname: 'StrikeMaster',
            challengerAvatar: null,
            targetPlayerId: 'target-1',
            targetPlayerNickname: 'TargetPro',
            targetPlayerAvatar: null,
            targetGroupId: null,
            targetGroupName: null,
            matchType: 'ONE_VS_ONE',
            ruleConfigId: null,
            ruleConfigName: 'Standard',
            message: 'Duel me!',
            status: 'PENDING',
            createdAt: new Date().toISOString(),
            expiresAt: new Date(Date.now() + 86400000).toISOString(),
          },
        ]),
      });
    });

    await page.route('**/api/v1/challenges/chal-e2e-1/accept', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          challengeId: 'chal-e2e-1',
          status: 'ACCEPTED',
          message: 'Challenge accepted successfully',
        }),
      });
    });

    await loginUser(page, 'target');
    await page.goto('/');

    // 2. Locate Pending Challenges card on Home Hub
    const challengeCard = page.locator('[data-test="challenge-card-chal-e2e-1"]');
    await expect(challengeCard).toBeVisible();
    await expect(challengeCard).toContainText('StrikeMaster');

    // 3. Click Accept button
    const acceptBtn = page.locator('[data-test="accept-challenge-chal-e2e-1"]');
    await expect(acceptBtn).toBeVisible();
    await acceptBtn.click();

    // 4. Verify accepted status / match recording prompt
    await expect(page.locator('[data-test="toast-success"]')).toBeVisible();
  });

  test.skip('[P1] Challenger cancels pending challenge (AC 4)', async ({ page }) => {
    await page.route('**/api/v1/challenges/outgoing', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'chal-e2e-outgoing',
            challengerId: 'me',
            challengerNickname: 'Me',
            challengerAvatar: null,
            targetPlayerId: 't1',
            targetPlayerNickname: 'Opponent',
            targetPlayerAvatar: null,
            targetGroupId: null,
            targetGroupName: null,
            matchType: 'ONE_VS_ONE',
            ruleConfigId: null,
            ruleConfigName: null,
            message: 'Game?',
            status: 'PENDING',
            createdAt: new Date().toISOString(),
            expiresAt: new Date(Date.now() + 86400000).toISOString(),
          },
        ]),
      });
    });

    await page.route('**/api/v1/challenges/chal-e2e-outgoing/cancel', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          challengeId: 'chal-e2e-outgoing',
          status: 'CANCELLED',
          message: 'Challenge cancelled',
        }),
      });
    });

    await loginUser(page, 'canceller');
    await page.goto('/?tab=challenges');

    const cancelBtn = page.locator('[data-test="cancel-challenge-chal-e2e-outgoing"]');
    await expect(cancelBtn).toBeVisible();
    await cancelBtn.click();

    await expect(page.locator('[data-test="challenge-card-chal-e2e-outgoing"]')).not.toBeVisible();
  });
});
