import { test, expect } from '@playwright/test';

test.describe('Login Flow', () => {
  test('should load the home page and verify main.css styles', async ({ page }) => {
    await page.goto('/');

    // Verify main.css is loaded by checking for Tic-Tac-Tore heading
    // and its styles (Tailwind font-bold)
    const heading = page.getByRole('heading', { name: 'Tic-Tac-Tore' });
    await expect(heading).toBeVisible();

    // Check for a specific style that comes from tailwind/main.css
    // font-weight: 700 is applied by font-bold
    await expect(heading).toHaveCSS('font-weight', '700');
  });

  test('should redirect to Google OAuth2 endpoint when clicking sign in', async ({ page }) => {
    await page.goto('/');

    const signInButton = page.getByRole('button', { name: /Sign in with Google/i });
    await expect(signInButton).toBeVisible();

    // We expect the click to trigger a redirect to the backend
    // Since we are not mocking the backend completely, we just check if it tries to go to the right URL
    // We can use page.waitForURL or check if the request is made.
    // However, clicking it will change window.location.href.

    // In Playwright, if it redirects to an external site (or another port not covered by the current test)
    // we might need to handle it.

    // Let's verify it redirects to the backend oauth2 endpoint
    await signInButton.click();

    // The backend should redirect to Google, but first it hits our backend.
    // Vite proxy should handle /oauth2/authorization/google -> http://localhost:8080/oauth2/authorization/google

    // We expect to be redirected to a URL containing /oauth2/authorization/google
    // or to the google accounts login page if the backend redirect works.

    await expect(page).toHaveURL(/.*(oauth2\/authorization\/google|accounts\.google\.com).*/);
  });
});
