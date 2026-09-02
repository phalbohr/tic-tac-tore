import { test, expect } from '@playwright/test';

test.describe('Tournament 2v2 Random Pairing API Tests (ATDD Story 8.4)', () => {
  const tournamentId = '00000000-0000-0000-0000-000000008401';

  test.beforeEach(async ({ page }) => {
    await expect.poll(async () => {
      try {
        const res = await page.request.get('/api/auth/test-login?email=atdd84@example.com&nickname=ATDDTester');
        return res.status();
      } catch {
        return 0;
      }
    }, {
      message: 'Wait for backend to be ready',
      timeout: 10000,
    }).toBe(200);

    await page.goto('/api/auth/test-login?email=atdd84@example.com&nickname=ATDDTester');
  });

  test.skip('[P0] should return 4 participants and stub flags for 2v2 random pairing matches (AC2, AC6)', async ({ page }) => {
    const response = await page.request.get(`/api/v1/tournaments/${tournamentId}/matches`);

    expect(response.status()).toBe(200);
    const matches = await response.json();
    expect(Array.isArray(matches)).toBeTruthy();
    expect(matches.length).toBeGreaterThan(0);

    const firstMatch = matches[0];
    expect(firstMatch).toHaveProperty('participant1');
    expect(firstMatch).toHaveProperty('participant1Partner');
    expect(firstMatch).toHaveProperty('participant2');
    expect(firstMatch).toHaveProperty('participant2Partner');
    expect(firstMatch).toHaveProperty('isParticipant1Stub');
    expect(firstMatch).toHaveProperty('isParticipant2Stub');

    expect(typeof firstMatch.isParticipant1Stub).toBe('boolean');
    expect(typeof firstMatch.isParticipant2Stub).toBe('boolean');
  });

  test.skip('[P0] should generate equal match distribution schedule for 2v2 random pairings (AC1)', async ({ page }) => {
    const response = await page.request.get(`/api/v1/tournaments/${tournamentId}/matches`);

    expect(response.status()).toBe(200);
    const matches = await response.json();

    const counts: Record<string, number> = {};
    for (const match of matches) {
      const p1 = match.participant1.playerId;
      const p1p = match.participant1Partner.playerId;
      const p2 = match.participant2.playerId;
      const p2p = match.participant2Partner.playerId;

      [p1, p1p, p2, p2p].forEach((id) => {
        counts[id] = (counts[id] || 0) + 1;
      });
    }

    const matchCounts = Object.values(counts);
    expect(matchCounts.length).toBeGreaterThanOrEqual(4);
    const expectedMatches = matchCounts[0];
    expect(matchCounts.every((count) => count === expectedMatches)).toBe(true);
  });

  test.skip('[P1] should expose stub partner indicators when substitute partner is assigned (AC3, AC6)', async ({ page }) => {
    const response = await page.request.get(`/api/v1/tournaments/${tournamentId}/matches?hasStub=true`);

    expect(response.status()).toBe(200);
    const stubMatches = await response.json();
    expect(stubMatches.length).toBeGreaterThan(0);

    const match = stubMatches[0];
    expect(match.isParticipant1Stub || match.isParticipant2Stub).toBe(true);
  });
});
