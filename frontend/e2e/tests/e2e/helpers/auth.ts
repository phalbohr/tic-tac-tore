import { type Page, type Route } from '@playwright/test';

export async function loginAsTestUser(page: Page, email = 'test@example.com', nickname = 'testuser') {
  await page.addInitScript(() => {
    window.localStorage.setItem('tutorial-completed', 'true');
  });

  await page.route('**/api/v1/rule-configurations*', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ scoreLimit: 5, gameLimit: 1, winsNeeded: 1, winByTwo: false })
    });
  });

  await page.route('**/api/users/me/frequent-opponents', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([
        { id: 'player-1-uuid', nickname: 'Alice', avatar: null },
        { id: 'player-2-uuid', nickname: 'Bob', avatar: null }
      ])
    });
  });

  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
}
