import { test, expect } from '@playwright/test';

test.describe('Automatic Profile Generation E2E (ATDD)', () => {
  test('[P0] should display generated nickname and avatar on first login', async ({ page, context }) => {
    await context.addCookies([{
      name: 'TTT_SESSION',
      value: 'true',
      domain: 'localhost',
      path: '/',
    }]);

    await page.route('**/api/v1/profile/me', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          nickname: 'johndoe',
          avatar: 'https://api.dicebear.com/7.x/identicon/svg?seed=79e78294ea51167733230b503067672e811197931f6d3f2ec8c9035293ca7a77',
          language: 'en'
        }),
      });
    });

    await page.goto('/');

    await expect(page.getByText(/johndoe/)).toBeVisible();
    
    const avatarImg = page.locator('img[alt="User Avatar"]');
    await expect(avatarImg).toBeVisible();
    await expect(avatarImg).toHaveAttribute('src', /dicebear/);
  });
});
