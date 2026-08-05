import { test, expect } from '@playwright/test';
import * as crypto from 'crypto';

test.describe('Automatic Profile Generation E2E (ATDD)', () => {
  test('[P0] should display generated nickname and avatar on first login', async ({ page }) => {
    // Given
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-profile-user-${randomSuffix}@example.com`;
    const nickname = `johndoe${randomSuffix}`;

    await page.goto(`/api/auth/test-login?email=${encodeURIComponent(email)}&nickname=${encodeURIComponent(nickname)}`);

    // Then
    await expect(page.getByText(new RegExp(nickname))).toBeVisible();
    
    const avatarImgOrSvg = page.locator('[data-testid="avatar-img"], [data-testid="avatar-svg"]').first();
    await expect(avatarImgOrSvg).toBeVisible();
  });
});
