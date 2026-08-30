import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Award Wall and Progress Tracking (Story 7.3).
 *
 * AC 1: All achievements gallery with category filter tabs (All, Badges, Anti-Achievements) and summary counters.
 * AC 2 & 5: Dynamic progress bar and numeric counter (e.g. 3 / 10) on locked progressive badges in Clubhouse Editorial styling.
 * AC 5: Detail modal with expanded progress bar, percentage, and remaining count.
 * AC 6: Full localization support in English and German.
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-progress-${randomSuffix}@example.com`;
  const nickname = `ProgressHunter${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('[Story 7.3] Award Wall & Progress Tracking E2E User Journey (ATDD)', () => {

  test('[P0] [AC1] should render category filter tabs and filter achievements gallery', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/players/*/achievements', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: '00000000-0000-0000-0000-000000000001',
          totalUnlocked: 1,
          totalAvailable: 3,
          achievements: [
            {
              id: 'ach-1',
              code: 'FIRST_WIN',
              category: 'MILESTONE',
              nameKey: 'achievements.first_win.title',
              descriptionKey: 'achievements.first_win.description',
              icon: 'trophy',
              isUnlocked: true,
              unlockedAt: '2026-08-30T12:00:00Z',
              currentProgress: 1,
              targetValue: 1,
              hasProgress: true,
            },
            {
              id: 'ach-2',
              code: 'MATCHES_10',
              category: 'EXPERIENCE',
              nameKey: 'achievements.matches_10.title',
              descriptionKey: 'achievements.matches_10.description',
              icon: 'flame',
              isUnlocked: false,
              unlockedAt: null,
              currentProgress: 3,
              targetValue: 10,
              hasProgress: true,
            },
            {
              id: 'ach-3',
              code: 'GOOSE_EGG',
              category: 'ANTI_ACHIEVEMENT',
              nameKey: 'achievements.goose_egg.title',
              descriptionKey: 'achievements.goose_egg.description',
              icon: 'egg',
              isUnlocked: false,
              unlockedAt: null,
              currentProgress: null,
              targetValue: null,
              hasProgress: false,
            },
          ],
        }),
      });
    });

    await page.goto('/cabinet');
    await expect(page.locator('[data-testid="profile-badges-section"]')).toBeVisible();

    // Verify filter tabs exist
    const tabs = page.locator('[data-testid^="category-filter-tab-"]');
    await expect(tabs).toHaveCount(3);

    // Initial "All" tab shows all 3 cards
    await expect(page.locator('[data-testid="badge-card"]')).toHaveCount(3);

    // Filter to Badges
    await page.locator('[data-testid="category-filter-tab-badges"]').click();
    await expect(page.locator('[data-testid="badge-card"]')).toHaveCount(2);

    // Filter to Anti-Achievements
    await page.locator('[data-testid="category-filter-tab-anti"]').click();
    await expect(page.locator('[data-testid="badge-card"]')).toHaveCount(1);
  });

  test('[P0] [AC2, AC5] should display progress bar on locked progressive badge and expanded modal progress', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/players/*/achievements', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: '00000000-0000-0000-0000-000000000001',
          totalUnlocked: 0,
          totalAvailable: 1,
          achievements: [
            {
              id: 'ach-2',
              code: 'MATCHES_10',
              category: 'EXPERIENCE',
              nameKey: 'achievements.matches_10.title',
              descriptionKey: 'achievements.matches_10.description',
              icon: 'flame',
              isUnlocked: false,
              unlockedAt: null,
              currentProgress: 4,
              targetValue: 10,
              hasProgress: true,
            },
          ],
        }),
      });
    });

    await page.goto('/cabinet');

    // Verify card progress bar
    const badgeCard = page.locator('[data-testid="badge-card"]').first();
    await expect(badgeCard.locator('[data-testid="badge-progress-bar"]')).toBeVisible();
    await expect(badgeCard.locator('[data-testid="badge-progress-ratio"]')).toHaveText('4 / 10');

    // Click card to open modal
    await badgeCard.click();
    const modal = page.locator('[data-testid="badge-modal"]');
    await expect(modal).toBeVisible();
    await expect(modal.locator('[data-testid="modal-progress-bar"]')).toBeVisible();
    await expect(modal.getByText('4 / 10', { exact: false })).toBeVisible();
    await expect(modal.getByText('6', { exact: false })).toBeVisible(); // 6 remaining
  });
});
