import { test, expect } from '@playwright/test'

test.describe('New Match API Tests', () => {
  test.beforeEach(async ({ page }) => {
    await expect.poll(async () => {
      try {
        const res = await page.request.get('/api/auth/test-login?email=test@example.com&nickname=testuser')
        return res.status()
      } catch {
        return 0
      }
    }, {
      message: 'Wait for backend to be ready',
      timeout: 10000,
    }).toBe(200)

    await page.goto('/api/auth/test-login?email=test@example.com&nickname=testuser')
  })

  test('should fetch frequent opponents successfully', async ({ page }) => {
    const response = await page.request.get('/api/users/me/frequent-opponents')
    
    expect(response.status()).toBe(200)
    const data = await response.json()
    expect(Array.isArray(data)).toBeTruthy()
  })

  test('should fetch last used rule system successfully', async ({ page }) => {
    const response = await page.request.get('/api/users/me/preferences/last-rule-system')
    
    expect(response.status()).toBe(200)
    const data = await response.json()
    expect(data).toHaveProperty('lastRuleSystem')
  })
})
