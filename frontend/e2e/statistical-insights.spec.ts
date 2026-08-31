import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Auto-generated Statistical Insights (Story 7.5).
 *
 * AC 1 & 5: InsightsSection & InsightCard in Statistics Hub and Personal Cabinet with Clubhouse Editorial tokens.
 * AC 2 & 5: Dynamic insight observations (Win Streak, Form Trend, Positional Mastery, Best Partnership, Milestone Proximity) with drillDown.
 * AC 6: MicroCelebrationBanner on Home Hub with role="status", aria-live="polite", auto-dismiss after 4s.
 * AC 7 & 8: Demo mode support & German/English localization.
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-insights-${randomSuffix}@example.com`;
  const nickname = `InsightHunter${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('[Story 7.5] Auto-generated Statistical Insights E2E (ATDD RED PHASE)', () => {

  test.skip('[P0] [AC1, AC5] should render personalized statistical insights cards in Statistics Hub and navigate via drillDownUrl', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/statistics/insights', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: '00000000-0000-0000-0000-000000000001',
          totalCount: 3,
          insights: [
            {
              id: 'ins-1',
              type: 'WIN_STREAK',
              category: 'STREAK',
              importance: 'HIGH',
              titleKey: 'insights.winStreak.title',
              descriptionKey: 'insights.winStreak.description',
              params: { streak: 4 },
              icon: 'local_fire_department',
              drillDownUrl: null,
            },
            {
              id: 'ins-2',
              type: 'BEST_PARTNERSHIP',
              category: 'PARTNERSHIP',
              importance: 'MEDIUM',
              titleKey: 'insights.bestPartnership.title',
              descriptionKey: 'insights.bestPartnership.description',
              params: { partnerName: 'Alex', winRate: 80, matches: 5 },
              icon: 'group',
              drillDownUrl: '/statistics?tab=teams',
            },
            {
              id: 'ins-3',
              type: 'MILESTONE_PROXIMITY',
              category: 'MILESTONE',
              importance: 'MEDIUM',
              titleKey: 'insights.milestoneProximity.title',
              descriptionKey: 'insights.milestoneProximity.description',
              params: { badgeCode: 'MATCHES_10', remaining: 2, current: 8, target: 10 },
              icon: 'military_tech',
              drillDownUrl: '/cabinet',
            },
          ],
        }),
      });
    });

    await page.goto('/statistics');
    await expect(page.getByTestId('insights-section')).toBeVisible();

    const cards = page.getByTestId('insight-card');
    await expect(cards).toHaveCount(3);
    await expect(cards.first()).toContainText('local_fire_department');

    const partnershipCard = cards.nth(1);
    await expect(partnershipCard.getByTestId('insight-drilldown-btn')).toBeVisible();
    await partnershipCard.getByTestId('insight-drilldown-btn').click();
    await expect(page).toHaveURL(/.*tab=teams.*/);
  });

  test.skip('[P0] [AC6] should display MicroCelebrationBanner on Home Hub after match confirmation and auto-dismiss', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/statistics/insights', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: '00000000-0000-0000-0000-000000000001',
          totalCount: 1,
          insights: [
            {
              id: 'ins-celeb-1',
              type: 'WIN_STREAK',
              category: 'STREAK',
              importance: 'HIGH',
              titleKey: 'insights.winStreak.title',
              descriptionKey: 'insights.winStreak.description',
              params: { streak: 5 },
              icon: 'local_fire_department',
              drillDownUrl: '/statistics',
            },
          ],
        }),
      });
    });

    await page.goto('/');
    const banner = page.getByTestId('micro-celebration-banner');
    await expect(banner).toBeVisible();
    await expect(banner).toHaveAttribute('role', 'status');
    await expect(banner).toHaveAttribute('aria-live', 'polite');
    await expect(banner).toContainText('local_fire_department');

    await expect(banner).toBeHidden({ timeout: 6000 });
  });

  test.skip('[P1] [AC3, AC7] should display starter empty state or demo insights when player has fewer than 3 matches', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/statistics/insights', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: '00000000-0000-0000-0000-000000000001',
          totalCount: 1,
          insights: [
            {
              id: 'ins-starter',
              type: 'INSUFFICIENT_DATA',
              category: 'GENERAL',
              importance: 'LOW',
              titleKey: 'insights.empty',
              descriptionKey: 'insights.empty',
              params: {},
              icon: 'info',
              drillDownUrl: null,
            },
          ],
        }),
      });
    });

    await page.goto('/statistics');
    await expect(page.getByTestId('insights-section')).toBeVisible();
    await expect(page.getByTestId('insight-empty-state')).toBeVisible();
  });
});
