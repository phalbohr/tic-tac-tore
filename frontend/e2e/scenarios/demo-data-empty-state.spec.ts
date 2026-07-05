import { test, expect } from '@playwright/test';

async function loginUser(page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-demo-user-${randomSuffix}@example.com`;
  const nickname = `E2EDemoUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
}

test.describe('Empty State & Demo Data E2E User Journey (ATDD)', () => {

  test('[P0] should display empty state when user has < 1 confirmed match', async ({ page }) => {
    await loginUser(page);

    await page.evaluate(() => {
      window.localStorage.setItem('tictactore.demoModeEnabled', 'false');
    });
    await page.goto('/');

    await expect(page.getByText('No Matches Yet')).toBeVisible();
    await page.getByRole('button', { name: 'Toggle Demo Data' }).click();

    await expect(page.getByText('Demo Data Active')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'My Statistics' })).toBeVisible();
  });

  test('[P1] should allow toggling demo mode in Personal Cabinet when demo data is active', async ({ page }) => {
    await loginUser(page);

    await page.evaluate(() => {
      window.localStorage.setItem('tictactore.demoModeEnabled', 'true');
    });
    await page.goto('/cabinet');

    const demoToggle = page.getByTestId('demo-mode-toggle');
    await expect(demoToggle).toBeVisible();
    await expect(demoToggle).toHaveAttribute('aria-checked', 'true');
    
    await demoToggle.click();
    
    await expect(demoToggle).toHaveAttribute('aria-checked', 'false');
  });

  test('[P0] should automatically disable and hide demo data upon reaching 5 confirmed real matches', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/statistics/me*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          playerId: 'user-123',
          playerName: 'Real Player',
          overall: { matches: 5, wins: 3, losses: 2, winRate: 60 },
          attacker: { matches: 0, wins: 0, losses: 0, winRate: 0 },
          defender: { matches: 0, wins: 0, losses: 0, winRate: 0 }
        })
      });
    });

    await page.evaluate(() => {
      window.localStorage.setItem('tictactore.demoModeEnabled', 'true');
    });
    
    // Navigate to apply the mock data and trigger the store logic on mount
    await page.goto('/');
    
    await expect(page.getByText('Demo Data Active')).not.toBeVisible();
    
    await page.goto('/cabinet');
    
    const demoToggle = page.getByTestId('demo-mode-toggle');
    await expect(demoToggle).toBeVisible();
    await expect(demoToggle).toHaveAttribute('aria-checked', 'false');
  });
});
