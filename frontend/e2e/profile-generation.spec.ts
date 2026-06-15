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
          avatar: 'ball-classic',
          language: 'en'
        }),
      });
    });

    await page.goto('/');

    await expect(page.getByText(/johndoe/)).toBeVisible();
    
    const avatarSvg = page.getByTestId('avatar-svg').first();
    await expect(avatarSvg).toBeVisible();
    await expect(avatarSvg.locator('use')).toHaveAttribute('href', '/avatars.svg#ball-classic');
  });
});
