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
  test.skip('[P0] should display 4 participants (2v2 teams) on match cards in random pairing tournament (AC1, AC2, AC6)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-2v2-random-uuid';
    await page.route(`**/api/v1/tournaments/${tournamentId}`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: tournamentId,
          name: '2v2 Random Championship 2026',
          format: 'CHAMPIONSHIP',
          mode: 'TWO_VS_TWO_RANDOM_PAIRINGS',
          status: 'IN_PROGRESS',
          ruleConfiguration: { id: 'rule-1', name: 'Standard 5-Point' },
          minParticipants: 4,
          maxParticipants: 8,
          roundCount: 3,
        }),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/matches*`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
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
        ]),
      });
    });

    await page.goto(`/tournaments/${tournamentId}`);

    const matchCard = page.getByTestId('tournament-match-card').first();
    await expect(matchCard).toBeVisible();

    await expect(matchCard).toContainText('Alice');
    await expect(matchCard).toContainText('Bob');
    await expect(matchCard).toContainText('Charlie');
    await expect(matchCard).toContainText('Diana');
  });

  test.skip('[P1] should render stub partner badge when player has been replaced by stub (AC3, AC6)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-2v2-stub-uuid';
    await page.route(`**/api/v1/tournaments/${tournamentId}`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: tournamentId,
          name: '2v2 Random Championship with Stub',
          format: 'CHAMPIONSHIP',
          mode: 'TWO_VS_TWO_RANDOM_PAIRINGS',
          status: 'IN_PROGRESS',
          minParticipants: 4,
          maxParticipants: 4,
          roundCount: 1,
        }),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/matches*`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
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
        ]),
      });
    });

    await page.goto(`/tournaments/${tournamentId}`);

    const stubBadge = page.getByTestId('stub-partner-badge');
    await expect(stubBadge).toBeVisible();
    await expect(stubBadge).toContainText('Stub');
  });

  test.skip('[P1] should verify equal match distribution across tournament schedule view (AC1)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-2v2-schedule-uuid';
    await page.goto(`/tournaments/${tournamentId}`);

    const scheduleView = page.getByTestId('tournament-schedule');
    await expect(scheduleView).toBeVisible();

    const rounds = page.getByTestId('tournament-round-tab');
    await expect(rounds).toHaveCount(3);
  });
});
