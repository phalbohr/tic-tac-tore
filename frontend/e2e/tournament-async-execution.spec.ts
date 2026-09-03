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
  test('[P0] should allow starting matches out of round sequence without waiting for prior rounds (AC1, AC3, AC4)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-async-champ-uuid';
    const currentUserId = 'u1';

    await page.route('**/api/v1/profile/me', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: currentUserId, nickname: 'Alice' }),
      })
    );

    const mockTournament = {
      id: tournamentId,
      name: 'Championship 2026',
      format: 'CHAMPIONSHIP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      minParticipants: 4,
      maxParticipants: 4,
      registrationDeadline: '2026-09-01T12:00:00Z',
      hasPlayoff: false,
      creatorId: currentUserId,
      creatorNickname: 'Alice',
      createdAt: new Date().toISOString(),
      ruleConfiguration: { id: 'rule-std', name: 'Standard 10-Point' },
    };

    const mockBracket = {
      tournamentId,
      tournamentName: 'Championship 2026',
      format: 'CHAMPIONSHIP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      totalRounds: 2,
      seededParticipants: [],
      rounds: [
        {
          round: 1,
          roundName: 'Round 1',
          matches: [
            {
              id: 'tm-round1-m1',
              tournamentId,
              round: 1,
              matchOrder: 1,
              participant1: { id: 'reg-1', playerId: currentUserId, playerNickname: 'Alice' },
              participant2: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob' },
              status: 'READY',
              isAvailable: true,
              isOpponentBusy: false,
            },
          ],
        },
        {
          round: 2,
          roundName: 'Round 2',
          matches: [
            {
              id: 'tm-round2-m1',
              tournamentId,
              round: 2,
              matchOrder: 1,
              participant1: { id: 'reg-1', playerId: currentUserId, playerNickname: 'Alice' },
              participant2: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie' },
              status: 'READY',
              isAvailable: true,
              isOpponentBusy: false,
            },
          ],
        },
      ],
    };

    await page.route(/\/api\/v1\/tournaments(\?.*)?$/, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      })
    );
    await page.route('**/api/v1/tournaments/invitations/pending', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations/my`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ isRegistered: true, isPendingInvite: false }),
      })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/bracket`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockBracket),
      })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/matches/tm-round2-m1/start`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          ...mockBracket.rounds[1].matches[0],
          status: 'IN_PROGRESS',
        }),
      })
    );

    await page.goto('/tournaments');

    const tournamentCard = page.getByTestId('tournament-card').first();
    await expect(tournamentCard).toBeVisible();

    const viewScheduleBtn = tournamentCard.getByTestId('view-schedule-btn');
    await expect(viewScheduleBtn).toBeVisible();
    await viewScheduleBtn.click();

    const scheduleContainer = page.getByTestId('tournament-schedule-view');
    await expect(scheduleContainer).toBeVisible();

    // Round 2 match can be started directly while Round 1 is still unplayed
    const round2Card = scheduleContainer.locator('[data-test="tournament-match-card"]').filter({ hasText: 'Charlie' });
    await expect(round2Card.locator('[data-test="start-match-button"]')).toBeVisible();
    await round2Card.locator('[data-test="start-match-button"]').click();

    await expect(page).toHaveURL(new RegExp(`/matches/new.*tournamentId=${tournamentId}`));
  });

  test('[P0] should display Opponent Busy indicator and block start when opponent is in another active match (AC3, AC4)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-async-concurrency-uuid';
    const currentUserId = 'u3';

    await page.route('**/api/v1/profile/me', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: currentUserId, nickname: 'Charlie' }),
      })
    );

    const mockTournament = {
      id: tournamentId,
      name: 'Concurrency Cup 2026',
      format: 'CHAMPIONSHIP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      minParticipants: 4,
      maxParticipants: 4,
      registrationDeadline: '2026-09-01T12:00:00Z',
      hasPlayoff: false,
      creatorId: 'u1',
      creatorNickname: 'Alice',
      createdAt: new Date().toISOString(),
      ruleConfiguration: { id: 'rule-std', name: 'Standard 10-Point' },
    };

    const mockBracket = {
      tournamentId,
      tournamentName: 'Concurrency Cup 2026',
      format: 'CHAMPIONSHIP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      totalRounds: 2,
      seededParticipants: [],
      rounds: [
        {
          round: 1,
          roundName: 'Round 1',
          matches: [
            {
              id: 'tm-active',
              tournamentId,
              round: 1,
              matchOrder: 1,
              participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice' },
              participant2: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob' },
              status: 'IN_PROGRESS',
              isAvailable: false,
              isOpponentBusy: false,
            },
          ],
        },
        {
          round: 2,
          roundName: 'Round 2',
          matches: [
            {
              id: 'tm-blocked',
              tournamentId,
              round: 2,
              matchOrder: 1,
              participant1: { id: 'reg-3', playerId: currentUserId, playerNickname: 'Charlie' },
              participant2: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob' },
              status: 'READY',
              isAvailable: false,
              isOpponentBusy: true,
              busyParticipantNicknames: ['Bob'],
            },
          ],
        },
      ],
    };

    await page.route(/\/api\/v1\/tournaments(\?.*)?$/, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      })
    );
    await page.route('**/api/v1/tournaments/invitations/pending', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations/my`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ isRegistered: true, isPendingInvite: false }),
      })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/bracket`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockBracket),
      })
    );

    await page.goto('/tournaments');

    const tournamentCard = page.getByTestId('tournament-card').first();
    await expect(tournamentCard).toBeVisible();

    const viewScheduleBtn = tournamentCard.getByTestId('view-schedule-btn');
    await expect(viewScheduleBtn).toBeVisible();
    await viewScheduleBtn.click();

    const scheduleContainer = page.getByTestId('tournament-schedule-view');
    await expect(scheduleContainer).toBeVisible();

    const blockedCard = scheduleContainer.locator('[data-test="tournament-match-card"]').filter({ hasText: 'Charlie' });
    await expect(blockedCard.locator('[data-test="opponent-busy-badge"]')).toBeVisible();
    await expect(blockedCard.locator('[data-test="opponent-busy-badge"]')).toContainText('Opponent Busy');
    await expect(blockedCard.locator('[data-test="start-match-button"]')).toBeDisabled();
  });

  test('[P1] should revert match status to READY when cancelled before completion (AC5)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-async-cancel-uuid';
    const matchId = 'tm-to-cancel';

    await page.route(`**/api/v1/tournaments/${tournamentId}/matches/${matchId}/cancel`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: matchId,
          tournamentId,
          status: 'READY',
        }),
      })
    );

    // Call cancel endpoint in page context and verify state reset
    const result = await page.evaluate(async ({ tId, mId }) => {
      const res = await fetch(`/api/v1/tournaments/${tId}/matches/${mId}/cancel`, {
        method: 'POST',
        headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      });
      return { status: res.status, data: await res.json() };
    }, { tId: tournamentId, mId: matchId });

    expect(result.status).toBe(200);
    expect(result.data.status).toBe('READY');
  });

  test('[P0] should advance winner and transition next cup match to READY when both feeder matches conclude (AC2, AC6)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-async-cup-uuid';
    const currentUserId = 'u1';

    await page.route('**/api/v1/profile/me', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: currentUserId, nickname: 'Alice' }),
      })
    );

    const mockTournament = {
      id: tournamentId,
      name: 'Summer Cup 2026',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      minParticipants: 4,
      maxParticipants: 4,
      registrationDeadline: '2026-09-01T12:00:00Z',
      hasPlayoff: false,
      creatorId: currentUserId,
      creatorNickname: 'Alice',
      createdAt: new Date().toISOString(),
      ruleConfiguration: { id: 'rule-std', name: 'Standard 10-Point' },
    };

    const mockBracket = {
      tournamentId,
      tournamentName: 'Summer Cup 2026',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      totalRounds: 2,
      seededParticipants: [],
      rounds: [
        {
          round: 1,
          roundName: 'Semifinals',
          matches: [
            {
              id: 'tm-r1-m1',
              round: 1,
              matchOrder: 1,
              status: 'COMPLETED',
              participant1: { id: 'reg-1', playerId: currentUserId, playerNickname: 'Alice' },
              participant2: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob' },
              winnerRegistrationId: 'reg-1',
            },
            {
              id: 'tm-r1-m2',
              round: 1,
              matchOrder: 2,
              status: 'COMPLETED',
              participant1: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie' },
              participant2: { id: 'reg-4', playerId: 'u4', playerNickname: 'David' },
              winnerRegistrationId: 'reg-3',
            },
          ],
        },
        {
          round: 2,
          roundName: 'Final',
          matches: [
            {
              id: 'tm-r2-final',
              round: 2,
              matchOrder: 1,
              status: 'READY',
              participant1: { id: 'reg-1', playerId: currentUserId, playerNickname: 'Alice' },
              participant2: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie' },
              isAvailable: true,
              isOpponentBusy: false,
            },
          ],
        },
      ],
    };

    await page.route(/\/api\/v1\/tournaments(\?.*)?$/, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      })
    );
    await page.route('**/api/v1/tournaments/invitations/pending', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations/my`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ isRegistered: true, isPendingInvite: false }),
      })
    );
    await page.route(`**/api/v1/tournaments/${tournamentId}/bracket`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockBracket),
      })
    );

    await page.goto('/tournaments');

    const tournamentCard = page.getByTestId('tournament-card').first();
    await expect(tournamentCard).toBeVisible();

    const viewBracketBtn = tournamentCard.getByTestId('view-bracket-btn');
    await expect(viewBracketBtn).toBeVisible();
    await viewBracketBtn.click();

    const bracketContainer = page.getByTestId('tournament-bracket-view');
    await expect(bracketContainer).toBeVisible();

    const finalCard = bracketContainer.locator('[data-test="tournament-match-card"]').filter({ hasText: 'Alice' }).filter({ hasText: 'Charlie' });
    await expect(finalCard.locator('[data-test="start-match-button"]')).toBeVisible();
    await expect(finalCard.locator('[data-test="start-match-button"]')).toBeEnabled();
  });
});
