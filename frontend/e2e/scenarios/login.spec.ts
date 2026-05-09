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
    // Перехватываем запрос к серверам Google, чтобы избежать ошибки 
    // "This browser or app may not be secure" (защита от ботов)
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

    // Chromium может "зависать" на навигации, если мы ее мокаем.
    // Поэтому просто дождемся, пока браузер попытается сделать запрос на нужный URL.
    const redirectPromise = page.waitForRequest(req => 
      req.url().includes('oauth2/authorization/google') || 
      req.url().includes('accounts.google.com')
    );

    // Кликаем без ожидания полного завершения навигации
    await signInButton.click({ noWaitAfter: true });

    // Дожидаемся запроса. Если он ушел — значит кнопка отработала корректно.
    await redirectPromise;
  });
});

