import { test, expect } from '@playwright/test';

test.describe('Tournament Asynchronous Match Execution API Tests (ATDD Story 8.5)', () => {
  const tournamentId = '00000000-0000-0000-0000-000000008501';
  const matchId = '00000000-0000-0000-0000-000000008511';

  test.beforeEach(async ({ page }) => {
    await expect.poll(async () => {
      try {
        const res = await page.request.get('/api/auth/test-login?email=atdd85@example.com&nickname=ATDD85User');
        return res.status();
      } catch {
        return 0;
      }
    }, {
      message: 'Wait for backend to be ready',
      timeout: 10000,
    }).toBe(200);

    await page.goto('/api/auth/test-login?email=atdd85@example.com&nickname=ATDD85User');
  });

  test.skip('[P0] should successfully start an available tournament match (AC3)', async ({ page }) => {
    const response = await page.request.post(`/api/v1/tournaments/${tournamentId}/matches/${matchId}/start`);

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.id).toBe(matchId);
    expect(body.status).toBe('IN_PROGRESS');
  });

  test.skip('[P0] should return 409 Conflict when starting match with busy participant (AC3)', async ({ page }) => {
    const response = await page.request.post(`/api/v1/tournaments/${tournamentId}/matches/${matchId}/start`);

    expect(response.status()).toBe(409);
    const body = await response.json();
    expect(body.message).toMatch(/busy|playing|conflict/i);
  });

  test.skip('[P1] should revert match status to READY when match entry is cancelled (AC5)', async ({ page }) => {
    const response = await page.request.post(`/api/v1/tournaments/${tournamentId}/matches/${matchId}/cancel`);

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.id).toBe(matchId);
    expect(body.status).toBe('READY');
  });

  test.skip('[P1] should return availability and busy status metadata in match list (AC4)', async ({ page }) => {
    const response = await page.request.get(`/api/v1/tournaments/${tournamentId}/matches`);

    expect(response.status()).toBe(200);
    const matches = await response.json();
    expect(Array.isArray(matches)).toBe(true);
    expect(matches.length).toBeGreaterThan(0);

    const match = matches[0];
    expect(match).toHaveProperty('isAvailable');
    expect(match).toHaveProperty('isOpponentBusy');
    expect(match).toHaveProperty('busyParticipantNicknames');
  });
});
