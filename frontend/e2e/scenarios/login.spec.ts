import { test, expect } from '@playwright/test';

test.describe('Login Flow', () => {
  test('should load the home page and verify main.css styles', async ({ page }) => {
    await page.goto('/');

    const heading = page.getByRole('heading', { name: 'Tic-Tac-Tore' });
    await expect(heading).toBeVisible();

    await expect(heading).toHaveCSS('font-weight', '700');
  });

  test('should redirect to Google OAuth2 endpoint when clicking sign in', async ({ page }) => {
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

    const redirectPromise = page.waitForRequest(req => 
      req.url().includes('oauth2/authorization/google') || 
      req.url().includes('accounts.google.com')
    );

    await signInButton.click({ noWaitAfter: true });

    await redirectPromise;
  });
});

