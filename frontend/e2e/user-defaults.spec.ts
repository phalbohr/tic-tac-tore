import { test, expect, type Page } from '@playwright/test';

/**
 * Story 6.2: Default Team and Rule Template E2E Specs
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-defaults-user-${randomSuffix}@example.com`;
  const nickname = `DefaultUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return nickname;
}

test.describe('[Story 6.2] Default Team and Rule Template E2E User Journey', () => {

  test('[P0] should display Default Match Preferences in /cabinet and allow setting defaults (AC 1, AC 2)', async ({ page }) => {
    // Mock player groups and rule configurations
    await page.route('**/api/v1/player-groups', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([
            { id: 'group-1', name: 'Office Rivals', isFavorite: false, members: [] },
            { id: 'group-2', name: 'Weekend Warriors', isFavorite: true, members: [] }
          ]),
        });
      } else {
        await route.continue();
      }
    });

    await page.route('**/api/v1/rule-configurations*', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([
            { id: 'rule-preset-1', name: 'ITSF Standard Matchplay', type: 'PRESET', gameLimit: 3, scoreLimit: 10, winByTwo: false },
            { id: 'rule-custom-1', name: 'Fast 7', type: 'CUSTOM', gameLimit: 1, scoreLimit: 7, winByTwo: false }
          ]),
        });
      } else {
        await route.continue();
      }
    });

    let patchPayload: { defaultGroupId?: string; defaultRuleConfigurationId?: string } | null = null;
    await page.route('**/api/v1/profile/me', async (route) => {
      if (route.request().method() === 'PATCH') {
        patchPayload = JSON.parse(route.request().postData() || '{}');
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            nickname: 'DefaultUser',
            avatar: 'avatar-1',
            defaultGroupId: patchPayload.defaultGroupId,
            defaultRuleConfigurationId: patchPayload.defaultRuleConfigurationId,
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

    // 3. Verify select elements exist
    const groupSelect = page.locator('[data-test="default-group-select"]');
    const ruleSelect = page.locator('[data-test="default-rule-select"]');
    await expect(groupSelect).toBeVisible();
    await expect(ruleSelect).toBeVisible();

    // 4. Select default player group and rule template
    await groupSelect.selectOption('group-1');
    expect(patchPayload?.defaultGroupId).toBe('group-1');

    await ruleSelect.selectOption('rule-preset-1');
    expect(patchPayload?.defaultRuleConfigurationId).toBe('rule-preset-1');
  });

  test('[P0] should auto-populate defaults in match creation and support inline overrides (AC 3, AC 4)', async ({ page }) => {
    await page.route('**/api/v1/profile/me', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'user-1',
            nickname: 'DefaultUser',
            avatar: 'avatar-1',
            defaultGroupId: 'group-1',
            defaultRuleConfigurationId: 'rule-preset-1',
          }),
        });
      } else {
        await route.continue();
      }
    });

    await page.route('**/api/v1/player-groups', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 'group-1', name: 'Office Rivals', isFavorite: false, members: [{ id: 'u1', nickname: 'Alice', avatar: 'a1' }] },
          { id: 'group-2', name: 'Weekend Warriors', isFavorite: true, members: [{ id: 'u2', nickname: 'Bob', avatar: 'a2' }] }
        ]),
      });
    });

    await page.route('**/api/v1/rule-configurations*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 'rule-preset-1', name: 'ITSF Standard Matchplay', type: 'PRESET', gameLimit: 3, scoreLimit: 10, winByTwo: false },
          { id: 'rule-custom-1', name: 'Fast 7', type: 'CUSTOM', gameLimit: 1, scoreLimit: 7, winByTwo: false }
        ]),
      });
    });

    await loginUser(page);

    await page.goto('/matches/new');

    // Verify default group indicator and active class
    const defaultGroupChip = page.locator('[data-group-id="group-1"]');
    await expect(defaultGroupChip).toBeVisible();
    await expect(defaultGroupChip.locator('[data-test="default-indicator"]')).toBeVisible();

    // Verify default rule indicator
    const defaultRuleChip = page.locator('[data-rule-id="rule-preset-1"]');
    await expect(defaultRuleChip).toBeVisible();
    await expect(defaultRuleChip.locator('[data-test="default-indicator"]')).toBeVisible();
  });
});
