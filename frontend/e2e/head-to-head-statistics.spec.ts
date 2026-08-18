import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Head-to-Head (H2H) Comparison.
 * Story 4.5: Head-to-Head (H2H) Comparison
 *
 * AC 1: Opponent profile header and 3 cross-tabulated tables (Matches, Games, Goals)
 * AC 2: Filter by time period, rule system, or match type
 * AC 3: Empty state CTA with match creation navigation when 0 shared matches
 * AC 4: Demo mode realistic data exploration
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-h2h-user-${randomSuffix}@example.com`;
  const nickname = `H2HUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return nickname;
}

test.describe('[Story 4.5] Head-to-Head (H2H) Comparison E2E User Journey (ATDD)', () => {

  test.skip('[P0] should display opponent profile and three cross-tabulated tables (Matches, Games, Goals)', async ({ page }) => {
    await loginUser(page);

    const opponentId = 'opp-user-456';
    await page.route('**/api/v1/statistics/head-to-head*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          opponent: {
            id: opponentId,
            nickname: 'RivalChampion',
            avatarUrl: null,
          },
          matches: {
            with: { matches: 6, wins: 4, losses: 2, draws: 0, winRate: 66.7 },
            vs: { matches: 12, wins: 7, losses: 5, draws: 0, winRate: 58.3 },
          },
          games: {
            with: { gamesWon: 14, gamesLost: 8, totalGames: 22, winRate: 63.6 },
            vs: { gamesWon: 25, gamesLost: 18, totalGames: 43, winRate: 58.1 },
          },
          goals: {
            attackerVsDefender: { scored: 18, conceded: 9 },
            attackerVsAttacker: { scored: 12, conceded: 15 },
            defenderVsAttacker: { scored: 9, conceded: 16 },
            defenderVsDefender: { scored: 6, conceded: 4 },
          },
        }),
      });
    });

    await page.goto(`/statistics?tab=h2h&opponentId=${opponentId}`);

    // Verify Opponent Header
    await expect(page.getByText('RivalChampion')).toBeVisible();

    // Verify 3 Cross-tabulated tables
    await expect(page.getByRole('heading', { name: /Matches|Матчи/i })).toBeVisible();
    await expect(page.getByRole('heading', { name: /Games|Игры/i })).toBeVisible();
    await expect(page.getByRole('heading', { name: /Goals|Голы/i })).toBeVisible();

    // Verify "With" vs "Vs" stats in Matches table
    await expect(page.getByText('66.7%')).toBeVisible();
    await expect(page.getByText('58.3%')).toBeVisible();
  });

  test.skip('[P1] should filter H2H statistics by period, ruleConfigId, and matchType', async ({ page }) => {
    await loginUser(page);

    const opponentId = 'opp-user-456';
    let requestedUrl = '';
    await page.route('**/api/v1/statistics/head-to-head*', async (route) => {
      requestedUrl = route.request().url();
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          opponent: { id: opponentId, nickname: 'RivalChampion', avatarUrl: null },
          matches: { with: { matches: 0, wins: 0, losses: 0, draws: 0, winRate: 0 }, vs: { matches: 0, wins: 0, losses: 0, draws: 0, winRate: 0 } },
          games: { with: { gamesWon: 0, gamesLost: 0, totalGames: 0, winRate: 0 }, vs: { gamesWon: 0, gamesLost: 0, totalGames: 0, winRate: 0 } },
          goals: {
            attackerVsDefender: { scored: 0, conceded: 0 },
            attackerVsAttacker: { scored: 0, conceded: 0 },
            defenderVsAttacker: { scored: 0, conceded: 0 },
            defenderVsDefender: { scored: 0, conceded: 0 },
          },
        }),
      });
    });

    await page.goto(`/statistics?tab=h2h&opponentId=${opponentId}`);

    // Select time period filter
    const periodSelect = page.getByTestId('stats-period-select');
    await expect(periodSelect).toBeVisible();
    await periodSelect.selectOption('WEEKLY');

    // Select match type filter
    const matchTypeSelect = page.getByTestId('stats-match-type-select');
    if (await matchTypeSelect.isVisible()) {
      await matchTypeSelect.selectOption('2v2');
    }

    expect(requestedUrl).toContain('opponentId=' + opponentId);
  });

  test.skip('[P1] should show empty state CTA when 0 shared matches and navigate to new match', async ({ page }) => {
    await loginUser(page);

    const opponentId = 'opp-user-empty';
    await page.route('**/api/v1/statistics/head-to-head*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          opponent: { id: opponentId, nickname: 'NewOpponent', avatarUrl: null },
          matches: { with: { matches: 0, wins: 0, losses: 0, draws: 0, winRate: 0 }, vs: { matches: 0, wins: 0, losses: 0, draws: 0, winRate: 0 } },
          games: { with: { gamesWon: 0, gamesLost: 0, totalGames: 0, winRate: 0 }, vs: { gamesWon: 0, gamesLost: 0, totalGames: 0, winRate: 0 } },
          goals: {
            attackerVsDefender: { scored: 0, conceded: 0 },
            attackerVsAttacker: { scored: 0, conceded: 0 },
            defenderVsAttacker: { scored: 0, conceded: 0 },
            defenderVsDefender: { scored: 0, conceded: 0 },
          },
        }),
      });
    });

    await page.goto(`/statistics?tab=h2h&opponentId=${opponentId}`);

    // Verify EmptyStateCTA is visible
    await expect(page.getByText(/haven't played|не играли/i)).toBeVisible();

    // Verify CTA button navigates to new match
    const ctaButton = page.getByRole('button', { name: /Start a match|Начать матч/i });
    await expect(ctaButton).toBeVisible();
    await ctaButton.click();
    await expect(page).toHaveURL(new RegExp(`/matches/new.*opponentId=${opponentId}`));
  });
});
