import { test, expect, type Page } from '@playwright/test';

/**
 * Story 6.6: Challenge Player or Group E2E Tests
 */

async function loginUser(page: Page, prefix = 'challenge') {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-${prefix}-${randomSuffix}@example.com`;
  const nickname = `Player${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('[Story 6.6] Challenge Player or Group E2E Tests', () => {

  test('[P0] Authenticated user creates 1v1 challenge from Leaderboard (AC 1, AC 2)', async ({ page }) => {
    // 1. Mock leaderboard with another player
    await page.route('**/api/v1/statistics/leaderboard*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              playerId: 'opponent-id-99',
              playerName: 'StrikeMaster',
              totalMatches: 10,
              wins: 8,
              losses: 2,
              winRate: 0.8,
            },
          ],
          totalPages: 1,
          totalElements: 1,
          size: 20,
          number: 0,
        }),
      });
    });

    // 2. Mock create challenge API
    await page.route('**/api/v1/challenges', async (route) => {
      if (route.request().method() === 'POST') {
        const body = route.request().postDataJSON();
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'chal-new-1',
            challengerId: 'me-id',
            challengerNickname: 'Me',
            targetPlayerId: body.targetPlayerId,
            targetPlayerNickname: 'StrikeMaster',
            matchType: body.matchType,
            message: body.message,
            status: 'PENDING',
            createdAt: new Date().toISOString(),
          }),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page, 'challenger');

    // 3. Navigate to Leaderboard
    await page.goto('/leaderboard');

    // 4. Click Challenge button on StrikeMaster row
    const challengeBtn = page.locator('[data-testid="challenge-player-btn"]').first();
    await expect(challengeBtn).toBeVisible();
    await challengeBtn.click();

    // 5. ChallengeModal should open
    const targetPlayerName = page.locator('[data-testid="target-player-name"]');
    await expect(targetPlayerName).toBeVisible();
    await expect(targetPlayerName).toHaveText('StrikeMaster');

    // 6. Fill custom message and select 1v1
    await page.locator('[data-testid="match-type-1v1"]').click();
    await page.locator('[data-testid="challenge-message-input"]').fill('Ready for a duel?');

    // 7. Submit challenge
    await page.locator('[data-testid="challenge-submit-btn"]').click();
    await expect(targetPlayerName).toBeHidden();
  });

  test('[P0] Target user accepts incoming challenge on Home Hub (AC 2, AC 3)', async ({ page }) => {
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
    const challengeCard = page.locator('[data-testid="incoming-challenge-card"]');
    await expect(challengeCard).toBeVisible();
    await expect(page.locator('[data-testid="challenger-name"]')).toHaveText('StrikeMaster');

    // 3. Click Accept button
    const acceptBtn = page.locator('[data-testid="accept-challenge-btn"]');
    await expect(acceptBtn).toBeVisible();
    await acceptBtn.click();

    // 4. Verify challenge card is removed
    await expect(challengeCard).toBeHidden();
  });

  test('[P1] Target user declines incoming challenge on Home Hub (AC 3)', async ({ page }) => {
    await page.route('**/api/v1/challenges/incoming', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'chal-e2e-decline',
            challengerId: 'c2',
            challengerNickname: 'RivalPlayer',
            matchType: 'ONE_VS_ONE',
            status: 'PENDING',
            createdAt: new Date().toISOString(),
          },
        ]),
      });
    });

    await page.route('**/api/v1/challenges/chal-e2e-decline/decline', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          challengeId: 'chal-e2e-decline',
          status: 'DECLINED',
          message: 'Challenge declined',
        }),
      });
    });

    await loginUser(page, 'decliner');
    await page.goto('/');

    const challengeCard = page.locator('[data-testid="incoming-challenge-card"]');
    await expect(challengeCard).toBeVisible();

    const declineBtn = page.locator('[data-testid="decline-challenge-btn"]');
    await expect(declineBtn).toBeVisible();
    await declineBtn.click();

    await expect(challengeCard).toBeHidden();
  });

  test('[P1] Challenger cancels pending challenge (AC 4)', async ({ page }) => {
    await page.route('**/api/v1/challenges/outgoing', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'chal-e2e-outgoing',
            challengerId: 'me',
            challengerNickname: 'Me',
            targetPlayerId: 't1',
            targetPlayerNickname: 'Opponent',
            matchType: 'ONE_VS_ONE',
            status: 'PENDING',
            createdAt: new Date().toISOString(),
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
    await page.goto('/');

    // Switch to Sent (outgoing) tab
    const tabOutgoing = page.locator('[data-testid="tab-outgoing"]');
    await expect(tabOutgoing).toBeVisible();
    await tabOutgoing.click();

    const cancelBtn = page.locator('[data-testid="cancel-challenge-btn"]');
    await expect(cancelBtn).toBeVisible();
    await cancelBtn.click();

    await expect(page.locator('[data-testid="outgoing-challenge-card"]')).toBeHidden();
  });
});
