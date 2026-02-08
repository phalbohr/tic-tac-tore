import { test, expect } from '@playwright/test'

test.describe('Authentication Flow', () => {
  test('should show login button on login page', async ({ page }) => {
    await page.goto('/login')
    
    const loginButton = page.getByRole('link', { name: /login with google/i })
    await expect(loginButton).toBeVisible()
    await expect(loginButton).toHaveAttribute('href', /oauth2\/authorization\/google/)
  })

  test('should redirect unauthorized user from protected route', async ({ page }) => {
    // Attempt to visit protected /about
    await page.goto('/about')
    
    // Should be redirected to /login
    await expect(page).toHaveURL(/\/login/)
  })
})
