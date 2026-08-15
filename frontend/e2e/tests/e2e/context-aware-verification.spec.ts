import { test, expect } from '@playwright/test';
import { MatchFactory } from '../../support/factories/match.factory';

test.describe('Story 3.4: Context-Aware Verification Rules E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
      window.localStorage.setItem('tutorial-completed', 'true');
    });

    await page.goto('/api/auth/test-login?email=test@example.com&nickname=testuser&tutorialCompleted=true');
  });

  test('[P0] Should display PARTIALLY_CONFIRMED badge with correct count for 2v2 standard match', async ({ page }) => {
    const matchFactory = new MatchFactory();
    const partialMatch = matchFactory.create({
      status: 'PARTIALLY_CONFIRMED',
      entryMode: 'PARTICIPANT',
      matchFormat: 'STANDARD',
      confirmedByOpponentIds: ['opponent-uuid-1'],
      requiredConfirmations: 2,
    });

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          count: 1,
          matches: [
            {
              id: partialMatch.id,
              status: partialMatch.status,
              entryMode: partialMatch.entryMode,
              matchFormat: partialMatch.matchFormat,
              confirmedByOpponentIds: partialMatch.confirmedByOpponentIds,
              requiredConfirmations: partialMatch.requiredConfirmations,
              creatorNickname: 'TestCreator',
              teamAAttackerNickname: 'PlayerA',
              teamBAttackerNickname: 'PlayerB',
              createdAt: new Date().toISOString(),
            },
          ],
        }),
      });
    });

    await page.goto('/');

    const badge = page.getByTestId(`partially-confirmed-badge-${partialMatch.id}`);
    await expect(badge).toBeVisible();
    await expect(badge).toHaveText('1 of 2 confirmed');
  });

  test('[P0] Should display PENDING_APPROVAL badge (not partial) for 2v2 random match', async ({ page }) => {
    const matchFactory = new MatchFactory();
    const pendingMatch = matchFactory.create({
      status: 'PENDING_APPROVAL',
      entryMode: 'PARTICIPANT',
      matchFormat: 'RANDOM',
      confirmedByOpponentIds: ['opponent-uuid-1'],
      requiredConfirmations: 2,
    });

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          count: 1,
          matches: [
            {
              id: pendingMatch.id,
              status: pendingMatch.status,
              entryMode: pendingMatch.entryMode,
              matchFormat: pendingMatch.matchFormat,
              confirmedByOpponentIds: pendingMatch.confirmedByOpponentIds,
              requiredConfirmations: pendingMatch.requiredConfirmations,
              creatorNickname: 'TestCreator',
              teamAAttackerNickname: 'PlayerA',
              teamBAttackerNickname: 'PlayerB',
              createdAt: new Date().toISOString(),
            },
          ],
        }),
      });
    });

    await page.goto('/');

    const card = page.getByTestId(`pending-match-card-${pendingMatch.id}`);
    await expect(card).toBeVisible();

    const partialBadge = page.getByTestId(`partially-confirmed-badge-${pendingMatch.id}`);
    await expect(partialBadge).toBeHidden();

    const matchBadge = card.locator('span').first();
    await expect(matchBadge).toHaveText(/Match 1/);
  });

  test('[P1] Should show confirm/reject buttons for PARTIALLY_CONFIRMED match', async ({ page }) => {
    const matchFactory = new MatchFactory();
    const partialMatch = matchFactory.create({
      status: 'PARTIALLY_CONFIRMED',
      entryMode: 'PARTICIPANT',
      matchFormat: 'STANDARD',
      confirmedByOpponentIds: ['opponent-uuid-1'],
      requiredConfirmations: 2,
    });

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          count: 1,
          matches: [
            {
              id: partialMatch.id,
              status: partialMatch.status,
              entryMode: partialMatch.entryMode,
              matchFormat: partialMatch.matchFormat,
              confirmedByOpponentIds: partialMatch.confirmedByOpponentIds,
              requiredConfirmations: partialMatch.requiredConfirmations,
              creatorNickname: 'TestCreator',
              teamAAttackerNickname: 'PlayerA',
              teamBAttackerNickname: 'PlayerB',
              createdAt: new Date().toISOString(),
            },
          ],
        }),
      });
    });

    await page.goto('/');

    const confirmBtn = page.getByTestId(`confirm-match-btn-${partialMatch.id}`);
    const rejectBtn = page.getByTestId(`reject-match-btn-${partialMatch.id}`);

    await expect(confirmBtn).toBeVisible();
    await expect(rejectBtn).toBeVisible();
  });

  test('[P1] Should display "1 of 2 confirmed" when one opponent confirmed in 2v2 standard', async ({ page }) => {
    const matchFactory = new MatchFactory();
    const partialMatch = matchFactory.create({
      status: 'PARTIALLY_CONFIRMED',
      entryMode: 'PARTICIPANT',
      matchFormat: 'STANDARD',
      confirmedByOpponentIds: ['opponent-uuid-1'],
      requiredConfirmations: 2,
    });

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          count: 1,
          matches: [
            {
              id: partialMatch.id,
              status: partialMatch.status,
              entryMode: partialMatch.entryMode,
              matchFormat: partialMatch.matchFormat,
              confirmedByOpponentIds: partialMatch.confirmedByOpponentIds,
              requiredConfirmations: partialMatch.requiredConfirmations,
              creatorNickname: 'TestCreator',
              teamAAttackerNickname: 'PlayerA',
              teamBAttackerNickname: 'PlayerB',
              createdAt: new Date().toISOString(),
            },
          ],
        }),
      });
    });

    await page.goto('/');

    const badge = page.getByTestId(`partially-confirmed-badge-${partialMatch.id}`);
    await expect(badge).toHaveText('1 of 2 confirmed');
  });
});
