import { test, expect } from '@playwright/test';

test.describe('Account Deletion with Anonymization', () => {
  test('Account deletion flow with anonymization', async ({ page }) => {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-delete-user-${randomSuffix}@example.com`;
    const nickname = `E2EDeleteUser${randomSuffix}`;
    await page.request.get('/api/auth/test-login', { params: { email, nickname } });
    await page.goto('/cabinet');

    await page.getByRole('button', { name: /Delete Account/i }).click();
    await page.getByRole('button', { name: /Confirm/i }).click();

    await expect(page).toHaveURL('/');
    const cookies = await page.context().cookies();
    const hasToken = cookies.some(c => c.name === 'TTT_SESSION' || c.name === 'TTT_TOKEN');
    expect(hasToken).toBe(false);
    const apiResponse = await page.request.get('/api/v1/profile/me');
    expect(apiResponse.status()).toBe(401);
  });

  test('Account deletion flow should show error when API fails', async ({ page }) => {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-delete-fail-${randomSuffix}@example.com`;
    const nickname = `E2EDeleteFail${randomSuffix}`;
    await page.request.get('/api/auth/test-login', { params: { email, nickname } });
    await page.goto('/cabinet');
    await page.route('**/api/v1/profile/me', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ message: 'Failed to delete account' }),
        });
      } else {
        await route.continue();
      }
    });

    await page.getByRole('button', { name: /Delete Account/i }).click();
    await page.getByRole('button', { name: /Confirm/i }).click();

    const errorMessage = page.getByTestId('modal-error-message');
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toHaveText('Failed to delete account');
    await expect(page.getByRole('button', { name: /Confirm/i })).toBeVisible();
  });
});
