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
    // Intercept requests to Google servers to avoid the error
    // "This browser or app may not be secure" (bot protection)
    await page.route('**/*accounts.google.com/**', route => {
      route.fulfill({
        status: 200,
        contentType: 'text/html',
        body: '<html><body>Mocked Google Login Page</body></html>'
      });
    });

    await page.goto('/');

    const signInButton = page.getByRole('button', { name: /Sign in with Google/i });
    await expect(signInButton).toBeVisible();

    // Chromium might "hang" on navigation if we mock it.
    // So we just wait until the browser attempts to make a request to the needed URL.
    const redirectPromise = page.waitForRequest(req => 
      req.url().includes('oauth2/authorization/google') || 
      req.url().includes('accounts.google.com')
    );

    // Click without waiting for full navigation to complete
    await signInButton.click({ noWaitAfter: true });

    // Wait for the request. If it was sent, the button works correctly.
    await redirectPromise;
  });
});

