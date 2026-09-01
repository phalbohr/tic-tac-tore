import { test, expect } from '@playwright/test';

test.describe('Tournament Bracket & Seeding API Tests (ATDD Red Phase - Story 8.3)', () => {
  test.beforeEach(async ({ page }) => {
    await expect.poll(async () => {
      try {
        const res = await page.request.get('/api/auth/test-login?email=organizer@example.com&nickname=Organizer');
        return res.status();
      } catch {
        return 0;
      }
    }, {
      message: 'Wait for backend to be ready',
      timeout: 10000,
    }).toBe(200);

    await page.goto('/api/auth/test-login?email=organizer@example.com&nickname=Organizer');
  });

  test.skip('[P0] POST /api/v1/tournaments/{id}/start should start tournament, seed participants, and generate bracket (AC 1, AC 3, AC 4, AC 6)', async ({ page }) => {
    // THIS TEST WILL FAIL - Endpoints and bracket generation not implemented yet (TDD RED PHASE)
    const tournamentId = crypto.randomUUID();

    const response = await page.request.post(`/api/v1/tournaments/${tournamentId}/start`);

    expect(response.status()).toBe(200);
    const tournament = await response.json();
    expect(tournament).toMatchObject({
      id: tournamentId,
      status: 'IN_PROGRESS',
    });
  });

  test.skip('[P1] POST /api/v1/tournaments/{id}/start should cancel tournament when registrations < minParticipants (AC 1, AC 2)', async ({ page }) => {
    // THIS TEST WILL FAIL - Tournament cancellation logic not implemented yet (TDD RED PHASE)
    const lowCapTournamentId = crypto.randomUUID();

    const response = await page.request.post(`/api/v1/tournaments/${lowCapTournamentId}/start`);

    expect(response.status()).toBe(200);
    const tournament = await response.json();
    expect(tournament).toMatchObject({
      id: lowCapTournamentId,
      status: 'CANCELLED',
    });
  });

  test.skip('[P0] GET /api/v1/tournaments/{id}/bracket should return complete binary elimination bracket with seeds and BYEs (AC 4, AC 7)', async ({ page }) => {
    // THIS TEST WILL FAIL - Bracket endpoint not implemented yet (TDD RED PHASE)
    const tournamentId = crypto.randomUUID();

    const response = await page.request.get(`/api/v1/tournaments/${tournamentId}/bracket`);

    expect(response.status()).toBe(200);
    const bracket = await response.json();
    expect(bracket).toMatchObject({
      tournamentId,
      format: 'CUP',
      totalRounds: expect.any(Number),
      rounds: expect.arrayContaining([
        expect.objectContaining({
          round: 1,
          matches: expect.any(Array),
        }),
      ]),
      seededParticipants: expect.any(Array),
    });
  });

  test.skip('[P1] GET /api/v1/tournaments/{id}/bracket should return round-robin schedule for Championship tournaments (AC 5, AC 7)', async ({ page }) => {
    // THIS TEST WILL FAIL - Championship bracket generation and retrieval not implemented yet (TDD RED PHASE)
    const tournamentId = crypto.randomUUID();

    const response = await page.request.get(`/api/v1/tournaments/${tournamentId}/bracket`);

    expect(response.status()).toBe(200);
    const bracket = await response.json();
    expect(bracket).toMatchObject({
      tournamentId,
      format: 'CHAMPIONSHIP',
      rounds: expect.any(Array),
    });
  });

  test.skip('[P1] GET /api/v1/tournaments/{id}/matches should return filtered matches by round (AC 7)', async ({ page }) => {
    // THIS TEST WILL FAIL - Matches query endpoint not implemented yet (TDD RED PHASE)
    const tournamentId = crypto.randomUUID();

    const response = await page.request.get(`/api/v1/tournaments/${tournamentId}/matches?round=1`);

    expect(response.status()).toBe(200);
    const matches = await response.json();
    expect(Array.isArray(matches)).toBe(true);
    if (matches.length > 0) {
      expect(matches[0]).toMatchObject({
        round: 1,
        status: expect.stringMatching(/READY|BYE|PENDING/),
      });
    }
  });

  test.skip('[P2] GET /api/v1/tournaments/{id}/bracket without authentication should return 401 (AC 7)', async ({ request }) => {
    // Unauthenticated direct request
    const tournamentId = crypto.randomUUID();
    const response = await request.get(`/api/v1/tournaments/${tournamentId}/bracket`);

    expect(response.status()).toBe(401);
  });
});
