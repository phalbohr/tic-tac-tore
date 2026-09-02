import { test, expect, type Page } from '@playwright/test';

async function loginUser(page: Page, prefix = 'tourn-async') {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-${prefix}-${randomSuffix}@example.com`;
  const nickname = `Player${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('Asynchronous Tournament Match Execution E2E (Story 8.5)', () => {
  test.skip('[P0] should allow starting matches out of round sequence without waiting for prior rounds (AC1, AC3, AC4)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-async-champ-uuid';
    const mockTournament = {
      id: tournamentId,
      name: 'Championship 2026',
      format: 'CHAMPIONSHIP',
      mode: 'ONE_VS_ONE',
      status: 'IN_PROGRESS',
      ruleConfiguration: { id: 'rule-std', name: 'Standard 10-Point' },
    };

    const mockMatches = [
      {
        id: 'tm-round1-m1',
        tournamentId,
        round: 1,
        matchOrder: 1,
        participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice' },
        participant2: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob' },
        status: 'READY',
        isAvailable: true,
        isOpponentBusy: false,
      },
      {
        id: 'tm-round2-m1',
        tournamentId,
        round: 2,
        matchOrder: 1,
        participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice' },
        participant2: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie' },
        status: 'READY',
        isAvailable: true,
        isOpponentBusy: false,
      },
    ];

    await page.route(`**/api/v1/tournaments/${tournamentId}`, (route) =>
      route.fulfill({ json: mockTournament })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/matches*`, (route) =>
      route.fulfill({ json: mockMatches })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/matches/tm-round2-m1/start`, (route) =>
      route.fulfill({
        json: {
          ...mockMatches[1],
          status: 'IN_PROGRESS',
        },
      })
    );

    await page.goto(`/tournaments/${tournamentId}`);

    // Round 2 match can be started directly while Round 1 is still unplayed
    const round2Card = page.locator('[data-test="tournament-match-card"]').filter({ hasText: 'Charlie' });
    await expect(round2Card.locator('[data-test="start-match-button"]')).toBeVisible();
    await round2Card.locator('[data-test="start-match-button"]').click();

    await expect(page).toHaveURL(new RegExp(`/match/new.*tournamentId=${tournamentId}`));
  });

  test.skip('[P0] should display Opponent Busy indicator and block start when opponent is in another active match (AC3, AC4)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-async-concurrency-uuid';
    const mockMatches = [
      {
        id: 'tm-active',
        tournamentId,
        round: 1,
        matchOrder: 1,
        participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice' },
        participant2: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob' },
        status: 'IN_PROGRESS',
      },
      {
        id: 'tm-blocked',
        tournamentId,
        round: 2,
        matchOrder: 2,
        participant1: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie' },
        participant2: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob' },
        status: 'READY',
        isAvailable: false,
        isOpponentBusy: true,
        busyParticipantNicknames: ['Bob'],
      },
    ];

    await page.route(`**/api/v1/tournaments/${tournamentId}/matches*`, (route) =>
      route.fulfill({ json: mockMatches })
    );

    await page.goto(`/tournaments/${tournamentId}`);

    const blockedCard = page.locator('[data-test="tournament-match-card"]').filter({ hasText: 'Charlie' });
    await expect(blockedCard.locator('[data-test="opponent-busy-badge"]')).toBeVisible();
    await expect(blockedCard.locator('[data-test="opponent-busy-badge"]')).toContainText('Opponent Busy');
    await expect(blockedCard.locator('[data-test="start-match-button"]')).toBeDisabled();
  });

  test.skip('[P1] should revert match status to READY when cancelled before completion (AC5)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-async-cancel-uuid';
    const matchId = 'tm-to-cancel';

    await page.route(`**/api/v1/tournaments/${tournamentId}/matches/${matchId}/cancel`, (route) =>
      route.fulfill({
        json: {
          id: matchId,
          tournamentId,
          status: 'READY',
        },
      })
    );

    // Call cancel endpoint and verify state reset
    const res = await page.request.post(`/api/v1/tournaments/${tournamentId}/matches/${matchId}/cancel`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('READY');
  });

  test.skip('[P0] should advance winner and transition next cup match to READY when both feeder matches conclude (AC2, AC6)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-async-cup-uuid';
    const mockBracket = {
      tournamentId,
      format: 'CUP',
      rounds: [
        {
          round: 1,
          matches: [
            {
              id: 'tm-r1-m1',
              status: 'COMPLETED',
              participant1: { id: 'reg-1', playerNickname: 'Alice' },
              participant2: { id: 'reg-2', playerNickname: 'Bob' },
              winnerRegistrationId: 'reg-1',
            },
            {
              id: 'tm-r1-m2',
              status: 'COMPLETED',
              participant1: { id: 'reg-3', playerNickname: 'Charlie' },
              participant2: { id: 'reg-4', playerNickname: 'David' },
              winnerRegistrationId: 'reg-3',
            },
          ],
        },
        {
          round: 2,
          matches: [
            {
              id: 'tm-r2-final',
              status: 'READY',
              participant1: { id: 'reg-1', playerNickname: 'Alice' },
              participant2: { id: 'reg-3', playerNickname: 'Charlie' },
              isAvailable: true,
              isOpponentBusy: false,
            },
          ],
        },
      ],
    };

    await page.route(`**/api/v1/tournaments/${tournamentId}/bracket`, (route) =>
      route.fulfill({ json: mockBracket })
    );

    await page.goto(`/tournaments/${tournamentId}`);

    const finalCard = page.locator('[data-test="tournament-match-card"]').filter({ hasText: 'Alice' }).filter({ hasText: 'Charlie' });
    await expect(finalCard.locator('[data-test="start-match-button"]')).toBeVisible();
    await expect(finalCard.locator('[data-test="start-match-button"]')).toBeEnabled();
  });
});
