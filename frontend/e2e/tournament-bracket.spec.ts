import { test, expect, type Page } from '@playwright/test';

async function loginUser(page: Page, prefix = 'tourn-bracket') {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-${prefix}-${randomSuffix}@example.com`;
  const nickname = `Player${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('Tournament Bracket Generation & Seeding E2E (Story 8.3 - ATDD Red Phase)', () => {
  test.skip('[P0] should display binary bracket visualization for Single Elimination tournament with seeds and BYE badges (AC 4, AC 7, AC 8)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-cup-bracket-uuid';
    const mockTournament = {
      id: tournamentId,
      name: 'Grand Masters Cup 2026',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      ruleConfiguration: {
        id: 'rule-1',
        name: 'Standard 5-Point',
        goalLimit: 5,
        gameLimit: 1,
        winByTwo: false,
      },
      minParticipants: 4,
      maxParticipants: 8,
      registrationDeadline: '2026-09-01T12:00:00Z',
      hasPlayoff: false,
      status: 'IN_PROGRESS',
      creatorId: 'user-1',
      creatorNickname: 'Organizer',
      createdAt: new Date().toISOString(),
    };

    const mockBracket = {
      tournamentId,
      tournamentName: 'Grand Masters Cup 2026',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      totalRounds: 3,
      rounds: [
        {
          round: 1,
          roundName: 'Quarterfinals',
          matches: [
            {
              id: 'tm-1',
              tournamentId,
              round: 1,
              matchOrder: 1,
              participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice', seed: 1 },
              participant2: { id: 'reg-8', playerId: 'u8', playerNickname: 'Bob', seed: 8 },
              seed1: 1,
              seed2: 8,
              status: 'READY',
            },
            {
              id: 'tm-2',
              tournamentId,
              round: 1,
              matchOrder: 2,
              participant1: { id: 'reg-4', playerId: 'u4', playerNickname: 'Charlie', seed: 4 },
              participant2: { id: 'reg-5', playerId: 'u5', playerNickname: 'Dave', seed: 5 },
              seed1: 4,
              seed2: 5,
              status: 'READY',
            },
            {
              id: 'tm-3',
              tournamentId,
              round: 1,
              matchOrder: 3,
              participant1: { id: 'reg-2', playerId: 'u2', playerNickname: 'Eve', seed: 2 },
              participant2: null,
              seed1: 2,
              seed2: null,
              status: 'BYE',
              winnerRegistrationId: 'reg-2',
            },
            {
              id: 'tm-4',
              tournamentId,
              round: 1,
              matchOrder: 4,
              participant1: { id: 'reg-3', playerId: 'u3', playerNickname: 'Frank', seed: 3 },
              participant2: { id: 'reg-6', playerId: 'u6', playerNickname: 'Grace', seed: 6 },
              seed1: 3,
              seed2: 6,
              status: 'READY',
            },
          ],
        },
        {
          round: 2,
          roundName: 'Semifinals',
          matches: [
            {
              id: 'tm-5',
              tournamentId,
              round: 2,
              matchOrder: 1,
              participant1: null,
              participant2: null,
              status: 'PENDING',
            },
            {
              id: 'tm-6',
              tournamentId,
              round: 2,
              matchOrder: 2,
              participant1: { id: 'reg-2', playerId: 'u2', playerNickname: 'Eve', seed: 2 },
              participant2: null,
              status: 'PENDING',
            },
          ],
        },
        {
          round: 3,
          roundName: 'Final',
          matches: [
            {
              id: 'tm-7',
              tournamentId,
              round: 3,
              matchOrder: 1,
              participant1: null,
              participant2: null,
              status: 'PENDING',
            },
          ],
        },
      ],
      seededParticipants: [
        { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice', seed: 1, strengthScore: 0.85 },
        { id: 'reg-2', playerId: 'u2', playerNickname: 'Eve', seed: 2, strengthScore: 0.75 },
        { id: 'reg-3', playerId: 'u3', playerNickname: 'Frank', seed: 3, strengthScore: 0.65 },
        { id: 'reg-4', playerId: 'u4', playerNickname: 'Charlie', seed: 4, strengthScore: 0.55 },
        { id: 'reg-5', playerId: 'u5', playerNickname: 'Dave', seed: 5, strengthScore: 0.45 },
        { id: 'reg-6', playerId: 'u6', playerNickname: 'Grace', seed: 6, strengthScore: 0.35 },
        { id: 'reg-8', playerId: 'u8', playerNickname: 'Bob', seed: 8, strengthScore: 0.15 },
      ],
    };

    await page.route('**/api/v1/tournaments', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/bracket`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockBracket),
      });
    });

    await page.goto('/tournaments');

    const tournamentCard = page.locator('[data-testid="tournament-card"]').first();
    await expect(tournamentCard).toBeVisible();

    // Click View Bracket button
    const viewBracketBtn = tournamentCard.locator('[data-testid="view-bracket-btn"]');
    await expect(viewBracketBtn).toBeVisible();
    await viewBracketBtn.click();

    // TournamentBracket modal or view renders
    const bracketContainer = page.locator('[data-testid="tournament-bracket-view"]');
    await expect(bracketContainer).toBeVisible();

    // Verify seed badges and participants
    await expect(bracketContainer.getByText('Alice')).toBeVisible();
    await expect(bracketContainer.getByText('#1')).toBeVisible();
    await expect(bracketContainer.getByText('BYE')).toBeVisible();

    // Verify round headers
    await expect(bracketContainer.getByText('Quarterfinals')).toBeVisible();
    await expect(bracketContainer.getByText('Semifinals')).toBeVisible();
    await expect(bracketContainer.getByText('Final')).toBeVisible();
  });

  test.skip('[P0] should display round-robin schedule for Championship tournaments (AC 5, AC 7, AC 8)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-championship-uuid';
    const mockTournament = {
      id: tournamentId,
      name: 'Autumn Championship League',
      format: 'CHAMPIONSHIP',
      mode: 'ONE_VS_ONE_PERSONAL',
      ruleConfiguration: {
        id: 'rule-1',
        name: 'Standard 5-Point',
        goalLimit: 5,
        gameLimit: 1,
        winByTwo: false,
      },
      minParticipants: 4,
      maxParticipants: 4,
      registrationDeadline: '2026-09-01T12:00:00Z',
      hasPlayoff: false,
      status: 'IN_PROGRESS',
      creatorId: 'user-1',
      creatorNickname: 'Organizer',
      createdAt: new Date().toISOString(),
    };

    const mockBracket = {
      tournamentId,
      tournamentName: 'Autumn Championship League',
      format: 'CHAMPIONSHIP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      totalRounds: 3,
      rounds: [
        {
          round: 1,
          roundName: 'Round 1',
          matches: [
            {
              id: 'tm-rr-1',
              tournamentId,
              round: 1,
              matchOrder: 1,
              participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice', seed: 1 },
              participant2: { id: 'reg-4', playerId: 'u4', playerNickname: 'Dave', seed: 4 },
              status: 'READY',
            },
            {
              id: 'tm-rr-2',
              tournamentId,
              round: 1,
              matchOrder: 2,
              participant1: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob', seed: 2 },
              participant2: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie', seed: 3 },
              status: 'READY',
            },
          ],
        },
        {
          round: 2,
          roundName: 'Round 2',
          matches: [
            {
              id: 'tm-rr-3',
              tournamentId,
              round: 2,
              matchOrder: 1,
              participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice', seed: 1 },
              participant2: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie', seed: 3 },
              status: 'PENDING',
            },
            {
              id: 'tm-rr-4',
              tournamentId,
              round: 2,
              matchOrder: 2,
              participant1: { id: 'reg-4', playerId: 'u4', playerNickname: 'Dave', seed: 4 },
              participant2: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob', seed: 2 },
              status: 'PENDING',
            },
          ],
        },
      ],
      seededParticipants: [
        { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice', seed: 1 },
        { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob', seed: 2 },
        { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie', seed: 3 },
        { id: 'reg-4', playerId: 'u4', playerNickname: 'Dave', seed: 4 },
      ],
    };

    await page.route('**/api/v1/tournaments', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/bracket`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockBracket),
      });
    });

    await page.goto('/tournaments');

    const tournamentCard = page.locator('[data-testid="tournament-card"]').first();
    await tournamentCard.locator('[data-testid="view-schedule-btn"]').click();

    const scheduleContainer = page.locator('[data-testid="tournament-schedule-view"]');
    await expect(scheduleContainer).toBeVisible();

    await expect(scheduleContainer.getByText('Round 1')).toBeVisible();
    await expect(scheduleContainer.getByText('Round 2')).toBeVisible();
    await expect(scheduleContainer.getByText('Alice')).toBeVisible();
    await expect(scheduleContainer.getByText('Dave')).toBeVisible();
  });

  test.skip('[P1] should show CANCELLED status and reason banner when tournament has insufficient participants (AC 2, AC 8)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-cancelled-uuid';
    const mockTournament = {
      id: tournamentId,
      name: 'Cancelled Spring Cup',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      minParticipants: 8,
      maxParticipants: 16,
      status: 'CANCELLED',
      cancellationReason: 'Insufficient confirmed participants (minimum 8 required)',
      createdAt: new Date().toISOString(),
    };

    await page.route('**/api/v1/tournaments', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      });
    });

    await page.goto('/tournaments');

    const tournamentCard = page.locator('[data-testid="tournament-card"]').first();
    await expect(tournamentCard).toBeVisible();

    const statusBadge = tournamentCard.locator('[data-testid="tournament-status-badge"]');
    await expect(statusBadge).toHaveText(/CANCELLED/i);
    await expect(tournamentCard.getByText(/Insufficient confirmed participants/i)).toBeVisible();
  });
});
