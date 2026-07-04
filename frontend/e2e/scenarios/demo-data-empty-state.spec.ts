import { test, expect } from '@playwright/test';

test.describe('Empty State & Demo Data E2E User Journey (ATDD)', () => {
  test.skip('[P0] should display generated demo data when user has < 1 confirmed match in Analytics', async ({ page }) => {
    // THIS TEST WILL FAIL - UI not implemented yet
    await page.goto('/analytics');

    // Expect demo data elements to be visible instead of empty state
    await expect(page.getByText('Demo Mode Active')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Analytics Overview' })).toBeVisible();
    // Verify some demo chart or table is shown
    await expect(page.getByTestId('demo-data-chart')).toBeVisible();
  });

  test.skip('[P1] should allow toggling demo mode in Personal Cabinet when demo data is active', async ({ page }) => {
    // THIS TEST WILL FAIL - UI not implemented yet
    await page.goto('/cabinet');

    // Toggle demo mode off
    const demoToggle = page.getByRole('switch', { name: 'Demo Mode' });
    await expect(demoToggle).toBeVisible();
    await expect(demoToggle).toBeChecked();
    
    await demoToggle.click();
    
    // Expect demo mode to be deactivated
    await expect(demoToggle).not.toBeChecked();
  });

  test.skip('[P0] should automatically disable and hide demo data upon reaching 5 confirmed real matches', async ({ page }) => {
    // THIS TEST WILL FAIL - UI not implemented yet
    // Assuming backend/fixture sets user to 4 matches, and we confirm the 5th here
    await page.goto('/matches');
    
    // Confirm the 5th match
    await page.getByRole('button', { name: 'Confirm Match' }).first().click();
    await expect(page.getByText('Match confirmed')).toBeVisible();
    
    // Navigate to Analytics
    await page.goto('/analytics');
    
    // Demo data should no longer be visible
    await expect(page.getByText('Demo Mode Active')).not.toBeVisible();
    
    // Navigate to Cabinet to verify toggle is hidden
    await page.goto('/cabinet');
    await expect(page.getByRole('switch', { name: 'Demo Mode' })).not.toBeVisible();
  });
});
