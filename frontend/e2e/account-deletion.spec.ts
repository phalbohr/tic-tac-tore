import { test, expect } from '@playwright/test';
import { faker } from '@faker-js/faker';

test.describe('Account Deletion with Anonymization', () => {
  test('[P0] Account deletion flow with anonymization', async ({ page }) => {
    // Given
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-delete-user-${randomSuffix}@example.com`;
    const nickname = `E2EDeleteUser${randomSuffix}`;
    await page.request.get('/api/auth/test-login', { params: { email, nickname } });
    await page.goto('/cabinet');

    // When
    await page.getByTestId('delete-account-button').click();
    await page.getByTestId('confirm-delete-button').click();

    // Then
    await expect(page).toHaveURL('/');
    const cookies = await page.context().cookies();
    const hasToken = cookies.some(c => c.name === 'TTT_SESSION' || c.name === 'TTT_TOKEN');
    expect(hasToken).toBe(false);
    const apiResponse = await page.request.get('/api/v1/profile/me');
    expect(apiResponse.status()).toBe(401);
  });

  test('[P1] Account deletion flow should show error when API fails', async ({ page }) => {
    // Given
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-delete-fail-${randomSuffix}@example.com`;
    const nickname = `E2EDeleteFail${randomSuffix}`;
    await page.request.get('/api/auth/test-login', { params: { email, nickname } });
    
    // Network-first pattern
    await page.route('**/api/v1/profile/me', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ message: 'Internal Server Error' }),
        });
      } else {
        await route.continue();
      }
    });
    await page.goto('/cabinet');

    // When
    await page.getByTestId('delete-account-button').click();
    await page.getByTestId('confirm-delete-button').click();

    // Then
    const errorMessage = page.getByTestId('modal-error-message');
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toHaveText('Failed to delete account');
    await expect(page.getByTestId('confirm-delete-button')).toBeVisible();
  });
});
