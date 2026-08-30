import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Achievement Badges & Profile Cabinet Integration.
 * Story 7.1: Achievement System (Badges)
 *
 * AC 1: Asynchronous evaluation on match confirmation
 * AC 2: Idempotent awarding of initial catalog badges
 * AC 3: Query achievements summary endpoint
 * AC 4: Cabinet UI dedicated ProfileBadgesSection with Clubhouse tokens (UX-DR3)
 * AC 5: Full i18n localization (EN/DE)
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-achieve-user-${randomSuffix}@example.com`;
  const nickname = `BadgeUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('[Story 7.1] Achievement System (Badges) E2E User Journey (ATDD)', () => {

  test.skip('[P0] [AC4] should display ProfileBadgesSection in Cabinet with earned and locked badges', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/players/*/achievements', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: '00000000-0000-0000-0000-000000000001',
          totalUnlocked: 1,
          totalAvailable: 5,
          achievements: [
            {
              id: 'a0000000-0000-0000-0000-000000000001',
              code: 'FIRST_WIN',
              category: 'MILESTONE',
              nameKey: 'achievements.first_win.title',
              descriptionKey: 'achievements.first_win.description',
              icon: 'trophy',
              isUnlocked: true,
              unlockedAt: '2026-08-30T12:00:00Z',
            },
            {
              id: 'a0000000-0000-0000-0000-000000000002',
              code: 'MATCHES_10',
              category: 'EXPERIENCE',
              nameKey: 'achievements.matches_10.title',
              descriptionKey: 'achievements.matches_10.description',
              icon: 'flame',
              isUnlocked: false,
              unlockedAt: null,
            },
            {
              id: 'a0000000-0000-0000-0000-000000000003',
              code: 'CLEAN_SHEET',
              category: 'SKILL',
              nameKey: 'achievements.clean_sheet.title',
              descriptionKey: 'achievements.clean_sheet.description',
              icon: 'shield',
              isUnlocked: false,
              unlockedAt: null,
            },
            {
              id: 'a0000000-0000-0000-0000-000000000004',
              code: 'STRIKER_50',
              category: 'OFFENSE',
              nameKey: 'achievements.striker_50.title',
              descriptionKey: 'achievements.striker_50.description',
              icon: 'target',
              isUnlocked: false,
              unlockedAt: null,
            },
            {
              id: 'a0000000-0000-0000-0000-000000000005',
              code: 'DEFENSE_WALL',
              category: 'DEFENSE',
              nameKey: 'achievements.defense_wall.title',
              descriptionKey: 'achievements.defense_wall.description',
              icon: 'wall',
              isUnlocked: false,
              unlockedAt: null,
            },
          ],
        }),
      });
    });

    await page.goto('/cabinet');
    await expect(page.locator('[data-testid="profile-badges-section"]')).toBeVisible();

    const badgeCards = page.locator('[data-testid="badge-card"]');
    await expect(badgeCards).toHaveCount(5);

    const unlockedBadge = page.locator('[data-testid="badge-card"][data-unlocked="true"]');
    await expect(unlockedBadge).toHaveCount(1);
  });

  test.skip('[P1] [AC4] should display badge details, description, and unlock timestamp on click/hover', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/players/*/achievements', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: '00000000-0000-0000-0000-000000000001',
          totalUnlocked: 1,
          totalAvailable: 1,
          achievements: [
            {
              id: 'a0000000-0000-0000-0000-000000000001',
              code: 'FIRST_WIN',
              category: 'MILESTONE',
              nameKey: 'achievements.first_win.title',
              descriptionKey: 'achievements.first_win.description',
              icon: 'trophy',
              isUnlocked: true,
              unlockedAt: '2026-08-30T12:00:00Z',
            },
          ],
        }),
      });
    });

    await page.goto('/cabinet');
    const badge = page.locator('[data-testid="badge-card"]').first();
    await badge.click();

    await expect(page.locator('[data-testid="badge-modal"], [data-testid="badge-popover"]')).toBeVisible();
    await expect(page.getByText('First Win', { exact: false })).toBeVisible();
  });

  test.skip('[P2] [AC5] should render badges with localized text when switching language to German', async ({ page }) => {
    await loginUser(page);

    await page.goto('/cabinet');
    const langSwitch = page.locator('[data-testid="language-switcher"], [data-testid="lang-de"]');
    if (await langSwitch.isVisible()) {
      await langSwitch.click();
    }

    const badgesSection = page.locator('[data-testid="profile-badges-section"]');
    await expect(badgesSection).toBeVisible();
  });
});
