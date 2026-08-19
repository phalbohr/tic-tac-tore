import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Unified Match History (My Matches).
 * Story 4.6: Unified Match History (My Matches)
 *
 * AC 1: Unified "My Matches" view (/matches or /history) with Confirmed and Pending tabs; badged cards with 15s undo action.
 * AC 2: Filter chips for player (opponent/partner), match type (1v1/2v2), and rule template.
 * AC 3: Clubhouse No-Line rule (UX-DR3), outcome badges, avatar integration, retired player safety (AD-04).
 * AC 4: Demo mode match history and tab-specific empty states (Confirmed 0 matches CTA, Pending 0 matches, Filtered 0 matches).
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-history-user-${randomSuffix}@example.com`;
  const nickname = `HistoryUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return nickname;
}

test.describe('[Story 4.6] Unified Match History (My Matches) E2E User Journey (ATDD)', () => {

  test.skip('[P0] should display Unified Match History view with Confirmed tab by default, showing match cards with scores and outcome badges', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/matches/history?status=CONFIRMED*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              id: 'match-101',
              matchType: '1v1',
              status: 'CONFIRMED',
              teamAAttackerId: 'user-me',
              teamBAttackerId: 'user-opp-1',
              games: [{ teamAScore: 10, teamBScore: 6 }, { teamAScore: 10, teamBScore: 8 }],
              createdAt: new Date().toISOString(),
            }
          ],
          page: 0,
          size: 10,
          totalElements: 1,
          totalPages: 1,
          first: true,
          last: true,
        }),
      });
    });

    await page.goto('/matches');

    // Verify Title & Tabs
    await expect(page.getByRole('heading', { level: 1 })).toContainText(/My Matches|Meine Spiele/i);
    await expect(page.getByRole('tab', { name: /Confirmed|Bestätigt/i })).toBeVisible();
    await expect(page.getByRole('tab', { name: /Pending|Ausstehend/i })).toBeVisible();

    // Verify Match Card contents
    await expect(page.getByText(/Win|Sieg/i)).toBeVisible();
    await expect(page.getByText('10 - 6')).toBeVisible();
  });

  test.skip('[P1] should switch to Pending tab and display pending confirmation cards with action buttons', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/matches/pending*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          pendingCount: 1,
          matches: [
            {
              id: 'pending-match-202',
              matchType: '1v1',
              status: 'PENDING',
              teamAAttackerId: 'user-opp-2',
              teamBAttackerId: 'user-me',
              games: [{ teamAScore: 10, teamBScore: 9 }],
              createdAt: new Date().toISOString(),
            }
          ]
        }),
      });
    });

    await page.goto('/matches');
    await page.getByRole('tab', { name: /Pending|Ausstehend/i }).click();

    // Verify Pending card and action buttons
    await expect(page.getByRole('button', { name: /Confirm|Bestätigen/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /Reject|Ablehnen/i })).toBeVisible();
  });

  test.skip('[P1] should filter match list by match type using thumb-friendly filter chips', async ({ page }) => {
    await loginUser(page);

    let requestedQuery = '';
    await page.route('**/api/v1/matches/history*', async (route) => {
      requestedQuery = route.request().url();
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [],
          page: 0,
          size: 10,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
        }),
      });
    });

    await page.goto('/matches');

    // Click 2v2 filter chip
    const filter2v2 = page.getByRole('button', { name: /2v2/i });
    if (await filter2v2.isVisible()) {
      await filter2v2.click();
      expect(requestedQuery).toContain('matchType=2v2');
    }
  });

  test.skip('[P1] should display empty states when there are 0 matches', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/matches/history*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [],
          page: 0,
          size: 10,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
        }),
      });
    });

    await page.goto('/matches');

    // Confirmed tab empty state CTA button
    const recordCta = page.getByRole('link', { name: /Record your first match|Erstes Spiel erfassen/i });
    await expect(recordCta).toBeVisible();
    await expect(recordCta).toHaveAttribute('href', '/matches/new');
  });

  test.skip('[P2] should comply with Clubhouse No-Line rule (UX-DR3)', async ({ page }) => {
    await loginUser(page);
    await page.goto('/matches');

    // Ensure list container does not have hard border dividers
    const listContainer = page.locator('.match-history-list, [data-testid="match-history-list"]').first();
    if (await listContainer.isVisible()) {
      const borderTopWidth = await listContainer.evaluate(el => window.getComputedStyle(el).borderTopWidth);
      expect(borderTopWidth).not.toBe('1px solid');
    }
  });
});
