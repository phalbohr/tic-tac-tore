import { test, expect } from '@playwright/test';

test.describe('Profile Management in Personal Cabinet (ATDD)', () => {
  test.skip('[P1] Language change applies optimistic UI update', async ({ page }) => {
    // THIS TEST WILL FAIL - UI not implemented yet
    await page.goto('/cabinet');

    await page.getByRole('button', { name: 'Language' }).click();
    await page.getByRole('option', { name: 'Deutsch' }).click();

    await expect(page.getByText('Sprache')).toBeVisible(); // expecting optimistic update
  });

  test.skip('[P1] Nickname 30-day cooldown enforcement', async ({ page }) => {
    // THIS TEST WILL FAIL - UI not implemented yet
    await page.goto('/cabinet');

    await page.getByLabel('Nickname').fill('NewNick');
    await page.getByRole('button', { name: 'Save' }).click();
    
    await expect(page.getByText('Profile updated successfully')).toBeVisible();

    await page.getByLabel('Nickname').fill('AnotherNick');
    await page.getByRole('button', { name: 'Save' }).click();

    await expect(page.getByText('Nickname can only be changed once every 30 days')).toBeVisible();
  });
});
