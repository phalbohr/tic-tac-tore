import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Humorous Anti-Achievements (Story 7.2).
 *
 * AC 1: GOOSE_EGG anti-achievement awarded when a game is lost with 0 points scored.
 * AC 2: GENEROUS_HOST anti-achievement awarded when 10+ points conceded in a game.
 * AC 3: SIEVE_DEFENSE anti-achievement awarded when Defender concedes 15+ goals in a match.
 * AC 4: HEARTBREAKER anti-achievement awarded when match lost in deciding game by 1 goal.
 * AC 5: Lighthearted, celebratory tone in EN and DE localizations.
 * AC 6: Frontend rendering in ProfileBadgesSection / BadgeCard with Material Symbols icons (egg, volunteer_activism, water_drop, heart_broken).
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-anti-achieve-${randomSuffix}@example.com`;
  const nickname = `FailMaster${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('[Story 7.2] Humorous Anti-Achievements E2E User Journey (ATDD)', () => {

  test('[P0] [AC6] should display anti-achievement badges with icons in profile badges section', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/players/*/achievements', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: '00000000-0000-0000-0000-000000000001',
          totalUnlocked: 2,
          totalAvailable: 4,
          achievements: [
            {
              id: 'anti-00000000-0000-0000-0000-000000000001',
              code: 'GOOSE_EGG',
              category: 'ANTI_ACHIEVEMENT',
              nameKey: 'achievements.goose_egg.title',
              descriptionKey: 'achievements.goose_egg.description',
              icon: 'egg',
              isUnlocked: true,
              unlockedAt: '2026-08-30T12:00:00Z',
            },
            {
              id: 'anti-00000000-0000-0000-0000-000000000002',
              code: 'GENEROUS_HOST',
              category: 'ANTI_ACHIEVEMENT',
              nameKey: 'achievements.generous_host.title',
              descriptionKey: 'achievements.generous_host.description',
              icon: 'volunteer_activism',
              isUnlocked: true,
              unlockedAt: '2026-08-30T12:05:00Z',
            },
            {
              id: 'anti-00000000-0000-0000-0000-000000000003',
              code: 'SIEVE_DEFENSE',
              category: 'ANTI_ACHIEVEMENT',
              nameKey: 'achievements.sieve_defense.title',
              descriptionKey: 'achievements.sieve_defense.description',
              icon: 'water_drop',
              isUnlocked: false,
              unlockedAt: null,
            },
            {
              id: 'anti-00000000-0000-0000-0000-000000000004',
              code: 'HEARTBREAKER',
              category: 'ANTI_ACHIEVEMENT',
              nameKey: 'achievements.heartbreaker.title',
              descriptionKey: 'achievements.heartbreaker.description',
              icon: 'heart_broken',
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
    await expect(badgeCards).toHaveCount(4);

    const unlockedBadges = page.locator('[data-testid="badge-card"][data-unlocked="true"]');
    await expect(unlockedBadges).toHaveCount(2);
  });

  test('[P1] [AC5, AC6] should display humorous modal details for unlocked anti-achievement', async ({ page }) => {
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
              id: 'anti-00000000-0000-0000-0000-000000000001',
              code: 'GOOSE_EGG',
              category: 'ANTI_ACHIEVEMENT',
              nameKey: 'achievements.goose_egg.title',
              descriptionKey: 'achievements.goose_egg.description',
              icon: 'egg',
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

    const modal = page.locator('[data-testid="badge-modal"], [data-testid="badge-popover"]');
    await expect(modal).toBeVisible();
    await expect(modal.getByText('Goose Egg', { exact: false })).toBeVisible();
  });
});
