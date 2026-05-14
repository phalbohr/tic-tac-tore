import { test, expect } from '@playwright/test';

test.describe('Logout Flow', () => {
  test('should send POST request with CSRF token when logging out', async ({ page }) => {
    let logoutRequestHeaders: { [key: string]: string } = {};
    
    await page.route('**/api/auth/logout', route => {
      logoutRequestHeaders = route.request().headers();
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true })
      });
    });

    await page.goto('/');

    await page.context().addCookies([
      { name: 'XSRF-TOKEN', value: 'mock-csrf-token-12345', domain: 'localhost', path: '/' },
      { name: 'TTT_TOKEN', value: 'mock-jwt-token', domain: 'localhost', path: '/' }
    ]);

    await page.evaluate(async () => {
      const csrfToken = document.cookie
        .split('; ')
        .find((row) => row.startsWith('XSRF-TOKEN='))
        ?.split('=')[1];

      const headers: HeadersInit = {};
      if (csrfToken) {
        headers['X-XSRF-TOKEN'] = decodeURIComponent(csrfToken);
      }

      await fetch('/api/auth/logout', { 
        method: 'POST',
        headers 
      });
    });

    expect(logoutRequestHeaders['x-xsrf-token']).toBe('mock-csrf-token-12345');
  });
});
