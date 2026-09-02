import { test, expect, type Page } from '@playwright/test';

async function loginUser(page: Page, prefix = 'tourn-2v2') {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-${prefix}-${randomSuffix}@example.com`;
  const nickname = `Player${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('Equal Match Distribution & 2v2 Random Pairing E2E (Story 8.4)', () => {
  test('[P0] should display 4 participants (2v2 teams) on match cards in random pairing tournament (AC1, AC2, AC6)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-2v2-random-uuid';
    const mockTournament = {
      id: tournamentId,
      name: '2v2 Random Championship 2026',
      format: 'CHAMPIONSHIP',
      mode: 'TWO_VS_TWO_RANDOM_PAIRINGS',
      status: 'IN_PROGRESS',
      ruleConfiguration: { id: 'rule-1', name: 'Standard 5-Point' },
      minParticipants: 4,
      maxParticipants: 8,
      roundCount: 3,
    };

    const mockBracket = {
      tournamentId,
      tournamentName: '2v2 Random Championship 2026',
      format: 'CHAMPIONSHIP',
      mode: 'TWO_VS_TWO_RANDOM_PAIRINGS',
      status: 'IN_PROGRESS',
      totalRounds: 3,
      rounds: [
        {
          round: 1,
          roundName: 'Round 1',
          matches: [
            {
              id: 'tm-84-1',
              tournamentId,
              round: 1,
              matchOrder: 1,
              participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice', seed: 1 },
              participant1Partner: { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob', seed: 2 },
              participant2: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie', seed: 3 },
              participant2Partner: { id: 'reg-4', playerId: 'u4', playerNickname: 'Diana', seed: 4 },
              isParticipant1Stub: false,
              isParticipant2Stub: false,
              status: 'READY',
            },
          ],
        },
      ],
      seededParticipants: [
        { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice', seed: 1 },
        { id: 'reg-2', playerId: 'u2', playerNickname: 'Bob', seed: 2 },
        { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie', seed: 3 },
        { id: 'reg-4', playerId: 'u4', playerNickname: 'Diana', seed: 4 },
      ],
    };

    await page.route(/\/api\/v1\/tournaments(\?.*)?$/, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      });
    });

    await page.route('**/api/v1/tournaments/invitations/pending', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
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

    const tournamentCard = page.getByTestId('tournament-card').first();
    await expect(tournamentCard).toBeVisible();

    const viewScheduleBtn = tournamentCard.getByTestId('view-schedule-btn');
    await expect(viewScheduleBtn).toBeVisible();
    await viewScheduleBtn.click();

    const matchCard = page.getByTestId('tournament-match-card').first();
    await expect(matchCard).toBeVisible();

    await expect(matchCard).toContainText('Alice & Bob');
    await expect(matchCard).toContainText('Charlie & Diana');
  });

  test('[P1] should render stub partner badge when player has been replaced by stub (AC3, AC6)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-2v2-stub-uuid';
    const mockTournament = {
      id: tournamentId,
      name: '2v2 Random Championship with Stub',
      format: 'CHAMPIONSHIP',
      mode: 'TWO_VS_TWO_RANDOM_PAIRINGS',
      status: 'IN_PROGRESS',
      minParticipants: 4,
      maxParticipants: 4,
      roundCount: 1,
    };

    const mockBracket = {
      tournamentId,
      tournamentName: '2v2 Random Championship with Stub',
      format: 'CHAMPIONSHIP',
      mode: 'TWO_VS_TWO_RANDOM_PAIRINGS',
      status: 'IN_PROGRESS',
      totalRounds: 1,
      rounds: [
        {
          round: 1,
          roundName: 'Round 1',
          matches: [
            {
              id: 'tm-84-2',
              tournamentId,
              round: 1,
              matchOrder: 1,
              participant1: { id: 'reg-1', playerId: 'u1', playerNickname: 'Alice', seed: 1 },
              participant1Partner: { id: 'reg-stub', playerId: 'u-stub', playerNickname: 'SubstitutePlayer', seed: 3 },
              participant2: { id: 'reg-3', playerId: 'u3', playerNickname: 'Charlie', seed: 2 },
              participant2Partner: { id: 'reg-4', playerId: 'u4', playerNickname: 'Diana', seed: 4 },
              isParticipant1Stub: true,
              isParticipant2Stub: false,
              status: 'READY',
            },
          ],
        },
      ],
      seededParticipants: [],
    };

    await page.route(/\/api\/v1\/tournaments(\?.*)?$/, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      });
    });

    await page.route('**/api/v1/tournaments/invitations/pending', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
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

    const tournamentCard = page.getByTestId('tournament-card').first();
    await expect(tournamentCard).toBeVisible();

    const viewScheduleBtn = tournamentCard.getByTestId('view-schedule-btn');
    await expect(viewScheduleBtn).toBeVisible();
    await viewScheduleBtn.click();

    const stubBadge = page.getByTestId('stub-partner-badge');
    await expect(stubBadge).toBeVisible();
    await expect(stubBadge).toContainText('Stub');
  });
});
