import { test, expect } from '@playwright/test';

test.describe('Profile Management in Personal Cabinet (ATDD)', () => {
  test('[P1] Language change applies optimistic UI update', async ({ page }) => {
    const response = await page.request.get('/api/auth/test-login', {
      params: {
        email: 'e2e-lang-user@example.com',
        nickname: 'E2ELangUser'
      }
    });
    expect(response.ok()).toBeTruthy();

    await page.goto('/cabinet');

    await page.getByRole('button', { name: 'Language' }).click();
    await page.getByRole('option', { name: 'Deutsch' }).click();

    await expect(page.getByText('Sprache')).toBeVisible();
  });

  test('[P1] Nickname 30-day cooldown enforcement', async ({ page }) => {
    const response = await page.request.get('/api/auth/test-login', {
      params: {
        email: 'e2e-cooldown-user@example.com',
        nickname: 'E2ECooldownUser'
      }
    });
    expect(response.ok()).toBeTruthy();

    await page.goto('/cabinet');

    const nicknameInput = page.getByLabel('Nickname');
    await nicknameInput.fill('NewNick');
    await page.getByRole('button', { name: 'Save' }).click();
    
    await expect(page.getByText('Profile updated successfully')).toBeVisible();

    await nicknameInput.fill('AnotherNick');
    await page.getByRole('button', { name: 'Save' }).click();

    await expect(page.getByText('Nickname can only be changed once every 30 days')).toBeVisible();
  });
});
