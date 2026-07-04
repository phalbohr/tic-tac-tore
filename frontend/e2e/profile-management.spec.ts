import { test, expect } from '@playwright/test';

test.describe('Profile Management in Personal Cabinet (ATDD)', () => {
  test('[P1] Language change applies optimistic UI update', async ({ page }) => {
    // Given
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-lang-user-${randomSuffix}@example.com`;
    const nickname = `E2ELangUser${randomSuffix}`;
    await page.request.get('/api/auth/test-login', {
      params: { email, nickname }
    });

    await page.goto('/cabinet');
    
    // When
    await page.getByTestId('language-select').click();
    await page.getByTestId('lang-de').click();

    // Then
    await expect(page.locator('h1')).toContainText('Persönliches Kabinett');
  });

  test('[P1] Nickname 30-day cooldown enforcement', async ({ page }) => {
    // Given
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-cooldown-user-${randomSuffix}@example.com`;
    const nickname = `E2ECooldownUser${randomSuffix}`;
    await page.request.get('/api/auth/test-login', {
      params: { email, nickname }
    });

    const newNick = `NewNick${randomSuffix}`;
    const anotherNick = `AnotherNick${randomSuffix}`;

    await page.goto('/cabinet');
    const nicknameInput = page.getByTestId('nickname-input');
    
    // When
    await nicknameInput.fill(newNick);
    await page.getByTestId('save-button').click();
    
    // Then
    await expect(page.getByTestId('success-message')).toBeVisible();
    
    // When
    await nicknameInput.fill(anotherNick);
    await page.getByTestId('save-button').click();

    // Then
    await expect(page.getByTestId('error-message')).toContainText('Nickname can only be changed once every 30 days');
  });
});
