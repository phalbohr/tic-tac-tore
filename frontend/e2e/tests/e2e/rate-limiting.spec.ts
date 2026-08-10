import { test, expect } from '@playwright/test';
import { loginAsTestUser } from './helpers/auth';

test.describe('Story 3.6: Rate Limiting (Anti-Spam) E2E', () => {
  test.use({ viewport: { width: 375, height: 667 } });

  test.beforeEach(async ({ page }) => {
    // Speed up the 15-second undo timer by making 1000ms intervals run in 10ms
    await page.addInitScript(() => {
      const originalSetInterval = window.setInterval;
      window.setInterval = function(cb: any, ms?: number) {
        return originalSetInterval(cb, ms === 1000 ? 10 : ms);
      };
    });
    await loginAsTestUser(page);
  });

  test('[P1] AC2/AC4: Should display rate-limit error banner when backend returns HTTP 429 with retryAfter', async ({ page }) => {
    await page.route('**/api/v1/matches', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 429,
          contentType: 'application/json',
          body: JSON.stringify({
            code: 'RATE_LIMIT_EXCEEDED',
            message: 'Rate limit exceeded: too many match submissions. Please try again in 42 seconds.',
            details: { retryAfter: 42 }
          })
        });
      } else {
        await route.continue();
      }
    });

    await page.goto('/');

    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: 'Alice' }).click();
    await page.getByRole('button', { name: 'Bob' }).click();
    await page.getByRole('button', { name: /Start Match/i }).click();

    const incrementBtn = page.getByRole('button', { name: 'Add 1' }).first();
    for (let i = 0; i < 5; i++) {
      await incrementBtn.click();
    }
    await page.getByRole('button', { name: /Complete Match/i }).click();

    const errorToast = page.locator('[role="alert"]').filter({ hasText: /Rate limit exceeded|Try again in 42 seconds/i });
    await expect(errorToast).toBeVisible();
    await expect(errorToast).toContainText('Try again in 42 seconds');
  });

  test('[P1] AC6: Should display server error when backend returns HTTP 503', async ({ page }) => {
    await page.route('**/api/v1/matches', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 503,
          contentType: 'application/json',
          body: JSON.stringify({
            code: 'RATE_LIMIT_UNAVAILABLE',
            message: 'Redis unavailable during rate-limit check',
            details: { retryAfter: 0 }
          })
        });
      } else {
        await route.continue();
      }
    });

    await page.goto('/');

    await page.getByRole('button', { name: /New Match/i }).click();
    await page.getByRole('button', { name: 'Alice' }).click();
    await page.getByRole('button', { name: 'Bob' }).click();
    await page.getByRole('button', { name: /Start Match/i }).click();

    const incrementBtn = page.getByRole('button', { name: 'Add 1' }).first();
    for (let i = 0; i < 5; i++) {
      await incrementBtn.click();
    }
    await page.getByRole('button', { name: /Complete Match/i }).click();

    const errorToast = page.locator('[role="alert"]');
    await expect(errorToast).toBeVisible();
    await expect(errorToast).toContainText('Redis unavailable');
  });
});
