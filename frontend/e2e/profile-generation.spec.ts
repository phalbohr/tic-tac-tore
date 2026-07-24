import { test, expect } from '@playwright/test';
import * as crypto from 'crypto';

test.describe('Automatic Profile Generation E2E (ATDD)', () => {
  test('[P0] should display generated nickname and avatar on first login', async ({ page }) => {
    // Given
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-profile-user-${randomSuffix}@example.com`;
    const nickname = `johndoe${randomSuffix}`;

    const setupResponse = await page.request.get('/api/auth/test-login', {
      params: { email, nickname }
    });
    expect(setupResponse.ok()).toBeTruthy();

    // When
    await page.goto('/');

    // Then
    await expect(page.getByText(new RegExp(nickname))).toBeVisible();
    
    const avatarSvg = page.getByTestId('avatar-svg').first();
    await expect(avatarSvg).toBeVisible();
    await expect(avatarSvg.locator('use')).toHaveAttribute('href', /^\/avatars\.svg#/);
  });
});
