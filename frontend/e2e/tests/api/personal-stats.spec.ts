import { test, expect, type Page } from '@playwright/test';

const TEST_EMAIL = 'stats-api-test@example.com';
const TEST_NICKNAME = 'StatsTester';

async function waitForBackend(page: Page) {
  await expect.poll(async () => {
    try {
      const res = await page.request.get('/actuator/health');
      return res.status();
    } catch {
      return 0;
    }
  }, {
    message: 'Wait for backend to be ready',
    timeout: 10000,
  }).toBe(200);
}

async function authenticateViaBrowser(page: Page) {
  await page.goto(
    `/api/auth/test-login?email=${encodeURIComponent(TEST_EMAIL)}&nickname=${encodeURIComponent(TEST_NICKNAME)}&tutorialCompleted=true`
  );
}

test.describe('Story 4.3: Personal Statistics API (/api/v1/statistics/me)', () => {
  test.beforeEach(async ({ page }) => {
    await waitForBackend(page);
    await authenticateViaBrowser(page);
  });

  test('[P0] Should return 401 when unauthenticated', async ({ browser }) => {
    const context = await browser.newContext();
    const unauthPage = await context.newPage();
    await waitForBackend(unauthPage);

    const response = await unauthPage.request.get('/api/v1/statistics/me');
    expect(response.status()).toBe(401);
    await context.close();
  });

  test('[P0] Should return 200 with PlayerStatsResponse shape when authenticated', async ({ page }) => {
    const response = await page.request.get('/api/v1/statistics/me');

    expect(response.status()).toBe(200);
    expect(response.headers()['content-type']).toContain('application/json');

    const body = await response.json();
    expect(body.playerId).toBeTruthy();
    expect(body.playerName).toBe(TEST_NICKNAME);
    expect(body.overall).toBeDefined();
    expect(body.attacker).toBeDefined();
    expect(body.defender).toBeDefined();
  });

  test('[P0] Should return zeroed stats for user with 0 confirmed matches', async ({ page }) => {
    const response = await page.request.get('/api/v1/statistics/me');
    const body = await response.json();

    expect(body.overall).toEqual({ matches: 0, wins: 0, losses: 0, winRate: 0 });
    expect(body.attacker).toEqual({ matches: 0, wins: 0, losses: 0, winRate: 0 });
    expect(body.defender).toEqual({ matches: 0, wins: 0, losses: 0, winRate: 0 });
  });

  test('[P1] Should return winRate on 0-100 scale (not 0-1 like leaderboard)', async ({ page }) => {
    const response = await page.request.get('/api/v1/statistics/me');
    const body = await response.json();

    for (const position of [body.overall, body.attacker, body.defender]) {
      expect(position.winRate).toBeGreaterThanOrEqual(0);
      expect(position.winRate).toBeLessThanOrEqual(100);
    }
  });

  test('[P1] Should return only the authenticated user own stats (no cross-user data)', async ({ page }) => {
    const response = await page.request.get('/api/v1/statistics/me');
    const body = await response.json();

    expect(body.playerName).toBe(TEST_NICKNAME);
    expect(typeof body.playerId).toBe('string');
    expect(body.playerId.length).toBeGreaterThan(0);
  });

  test('[P1] Should return response structure matching frontend PlayerStats contract', async ({ page }) => {
    const response = await page.request.get('/api/v1/statistics/me');
    const body = await response.json();

    expect(typeof body.playerId).toBe('string');
    expect(typeof body.playerName).toBe('string');

    for (const position of [body.overall, body.attacker, body.defender]) {
      expect(typeof position.matches).toBe('number');
      expect(typeof position.wins).toBe('number');
      expect(typeof position.losses).toBe('number');
      expect(typeof position.winRate).toBe('number');
    }
  });

  test('[P2] Should return consistent stats across repeated requests (deterministic)', async ({ page }) => {
    const res1 = await page.request.get('/api/v1/statistics/me');
    const body1 = await res1.json();

    const res2 = await page.request.get('/api/v1/statistics/me');
    const body2 = await res2.json();

    expect(body1).toEqual(body2);
  });
});
