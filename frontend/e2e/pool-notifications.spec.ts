import { test, expect, type Page } from '@playwright/test';

/**
 * Story 6.5: Pool Notifications E2E Scaffolds (TDD Red Phase)
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-pool-notif-${randomSuffix}@example.com`;
  const nickname = `PoolNotifUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return nickname;
}

test.describe('[Story 6.5] Pool Notifications Preference & Web Push E2E Journey', () => {

  test('[P0] should display Matchmaking Pool Notifications toggle in Cabinet and allow toggling preference (AC 4)', async ({ page }) => {
    let patchPayload: { poolNotificationsEnabled?: boolean } | null = null;
    await page.route('**/api/v1/profile/me', async (route) => {
      if (route.request().method() === 'PATCH') {
        patchPayload = JSON.parse(route.request().postData() || '{}');
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            nickname: 'PoolNotifUser',
            avatar: 'avatar-1',
            poolNotificationsEnabled: patchPayload.poolNotificationsEnabled,
          }),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page);

    // 1. Navigate to Cabinet
    await page.goto('/cabinet');

    // 2. Verify "Default Match Preferences" section exists
    await expect(page.getByRole('heading', { name: /Default Match Preferences|Standard-Spieleinstellungen/i })).toBeVisible();

    // 3. Verify Matchmaking Pool Notifications toggle exists and is on by default
    const notifToggle = page.locator('[data-test="pool-notifications-toggle"]');
    await expect(notifToggle).toBeVisible();

    // 4. Toggle off
    await notifToggle.click();
    expect(patchPayload?.poolNotificationsEnabled).toBe(false);
  });

  test('[P1] should persist pool notifications preference across page reloads (AC 4)', async ({ page }) => {
    await page.route('**/api/v1/profile/me', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'user-1',
            nickname: 'PoolNotifUser',
            avatar: 'avatar-1',
            poolNotificationsEnabled: false,
          }),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page);

    await page.goto('/cabinet');
    const notifToggle = page.locator('[data-test="pool-notifications-toggle"]');
    await expect(notifToggle).toBeVisible();
    await expect(notifToggle).toHaveAttribute('aria-checked', 'false');
  });
});
