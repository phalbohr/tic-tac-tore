import { test, expect } from '@playwright/test';

test.describe('Profile Management in Personal Cabinet (ATDD)', () => {
  test('[P1] Language change applies optimistic UI update', async ({ page }) => {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-lang-user-${randomSuffix}@example.com`;
    const nickname = `E2ELangUser${randomSuffix}`;
    await page.request.get('/api/auth/test-login', {
      params: { email, nickname }
    });

    await page.goto('/cabinet');
    await page.getByTestId('language-select').click();
    await page.getByTestId('lang-de').click();

    await expect(page.locator('h1')).toContainText('Persönliches Kabinett');
  });

  test('[P1] Nickname 30-day cooldown enforcement', async ({ page }) => {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-cooldown-user-${randomSuffix}@example.com`;
    const nickname = `E2ECooldownUser${randomSuffix}`;
    await page.request.get('/api/auth/test-login', {
      params: { email, nickname }
    });

    await page.goto('/cabinet');
    const nicknameInput = page.getByLabel('Nickname');
    await nicknameInput.fill(`NewNick${randomSuffix}`);
    await page.getByTestId('save-button').click();
    await expect(page.getByTestId('success-message')).toBeVisible();
    await nicknameInput.fill(`AnotherNick${randomSuffix}`);
    await page.getByTestId('save-button').click();

    await expect(page.getByTestId('error-message')).toContainText('Nickname can only be changed once every 30 days');
  });
});
