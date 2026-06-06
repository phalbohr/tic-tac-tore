import { test, expect } from '@playwright/test';

test.describe('Account Deletion with Anonymization', () => {
  test('Account deletion flow with anonymization', async ({ page }) => {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-delete-user-${randomSuffix}@example.com`;
    const nickname = `E2EDeleteUser${randomSuffix}`;
    await page.request.get('/api/auth/test-login', {
      params: { email, nickname }
    });
    
    await page.goto('/cabinet');
    await expect(page.getByRole('heading', { name: /Personal Cabinet/i })).toBeVisible();

    await page.getByRole('button', { name: /Delete Account/i }).click();
    await page.getByRole('button', { name: /Confirm/i }).click();

    await expect(page).toHaveURL('/');
    const cookies = await page.context().cookies();
    const hasToken = cookies.some(c => c.name === 'TTT_SESSION' || c.name === 'TTT_TOKEN');
    expect(hasToken).toBe(false);
    
    const isAuthenticated = await page.evaluate(() => {
      const authStore = (window as any).authStore || (window as any).__pinia__?.state?.value?.auth;
      return authStore ? authStore.isAuthenticated : false;
    });
    expect(isAuthenticated).toBe(false);
    
    await page.goto('/cabinet');
    await expect(page).toHaveURL('/');
  });
});
