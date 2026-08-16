import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Team (Pair) Statistics.
 * Story 4.4: Team (Pair) Statistics
 *
 * AC 1: Pair-level performance for teammate combinations & positional synergies
 * AC 2: Filter by specific player, rule system, or time period
 * AC 3: Pagination and minimum matches threshold exclusion
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-stats-user-${randomSuffix}@example.com`;
  const nickname = `StatsUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return nickname;
}

test.describe('[Story 4.4] Team (Pair) Statistics E2E User Journey (ATDD)', () => {

  test.skip('[P0] should display team pair statistics with positional synergies (attacker/defender)', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/statistics/team-pairs*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              attackerId: 'user-a',
              attackerName: 'Alice',
              defenderId: 'user-b',
              defenderName: 'Bob',
              matches: 10,
              wins: 7,
              losses: 3,
              winRate: 70.0
            },
            {
              attackerId: 'user-b',
              attackerName: 'Bob',
              defenderId: 'user-a',
              defenderName: 'Alice',
              matches: 8,
              wins: 4,
              losses: 4,
              winRate: 50.0
            }
          ],
          page: 0,
          size: 10,
          totalElements: 2,
          totalPages: 1
        })
      });
    });

    await page.goto('/statistics/teams');

    // Verify view header and pair cards/rows
    await expect(page.getByRole('heading', { name: /Team Statistics|Командная статистика/i })).toBeVisible();
    await expect(page.getByText('Alice (Attacker)')).toBeVisible();
    await expect(page.getByText('Bob (Defender)')).toBeVisible();
    await expect(page.getByText('70%')).toBeVisible();
  });

  test.skip('[P1] should filter team pair statistics by player, rule system, and period', async ({ page }) => {
    await loginUser(page);

    let requestedUrl = '';
    await page.route('**/api/v1/statistics/team-pairs*', async (route) => {
      requestedUrl = route.request().url();
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [],
          page: 0,
          size: 10,
          totalElements: 0,
          totalPages: 0
        })
      });
    });

    await page.goto('/statistics/teams');

    // Select time period filter
    const periodSelect = page.getByTestId('stats-period-select');
    if (await periodSelect.isVisible()) {
      await periodSelect.selectOption('LAST_MONTH');
    }

    // Expect query params updated in request
    expect(requestedUrl).toContain('period=LAST_MONTH');
  });

  test.skip('[P1] should paginate results and respect minimum matches threshold', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/statistics/team-pairs*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              attackerId: 'user-a',
              attackerName: 'Alice',
              defenderId: 'user-c',
              defenderName: 'Charlie',
              matches: 12,
              wins: 8,
              losses: 4,
              winRate: 66.7
            }
          ],
          page: 1,
          size: 1,
          totalElements: 25,
          totalPages: 25
        })
      });
    });

    await page.goto('/statistics/teams');

    // Verify pagination controls
    const nextPageButton = page.getByRole('button', { name: /Next|Вперед/i });
    await expect(nextPageButton).toBeVisible();
  });
});
