import { test, expect } from '@playwright/test';

test.describe('Avatar Selection & Management E2E', () => {
  test('[P1] Select avatar from preset grid and verify persistence', async ({ page }) => {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-avatar-user-${randomSuffix}@example.com`;
    const nickname = `E2EAvatarUser${randomSuffix}`;
    
    await page.request.get('/api/auth/test-login', {
      params: { email, nickname }
    });

    await page.goto('/cabinet');
    
    await page.getByTestId('change-avatar-button').click();
    await page.getByTestId('avatar-option-ball-classic').click();

    await expect(page.getByTestId('success-message')).toContainText('Avatar updated successfully');

    const avatarUse = page.getByTestId('avatar-svg').locator('use');
    await expect(avatarUse).toHaveAttribute('href', '/avatars.svg#ball-classic');

    await page.reload();
    await expect(page.getByTestId('avatar-svg').locator('use')).toHaveAttribute('href', '/avatars.svg#ball-classic');
  });
});
