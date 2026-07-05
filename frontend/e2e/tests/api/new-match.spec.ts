import { test, expect } from '@playwright/test'

test.describe('New Match API Tests', () => {
  test('should fetch frequent opponents successfully', async ({ page, request }) => {
    await page.goto('/api/auth/test-login?email=test@example.com&nickname=testuser')
    const cookies = await page.context().cookies()
    const cookieStr = cookies.map(c => `${c.name}=${c.value}`).join('; ')

    await expect.poll(async () => {
      const res = await request.get('/api/users/me/frequent-opponents', {
        headers: cookieStr ? { 'Cookie': cookieStr } : {}
      })
      return res.status()
    }, {
      message: 'Wait for endpoint to be available',
      timeout: 10000,
    }).toBe(200)

    const response = await request.get('/api/users/me/frequent-opponents', {
      headers: cookieStr ? { 'Cookie': cookieStr } : {}
    })
    expect(response.status()).toBe(200)
    const data = await response.json()
    expect(Array.isArray(data)).toBeTruthy()
  })

  test('should fetch last used rule system successfully', async ({ page, request }) => {
    await page.goto('/api/auth/test-login?email=test@example.com&nickname=testuser')
    const cookies = await page.context().cookies()
    const cookieStr = cookies.map(c => `${c.name}=${c.value}`).join('; ')

    await expect.poll(async () => {
      const res = await request.get('/api/users/me/preferences/last-rule-system', {
        headers: cookieStr ? { 'Cookie': cookieStr } : {}
      })
      return res.status()
    }, {
      timeout: 10000,
    }).toBe(200)

    const response = await request.get('/api/users/me/preferences/last-rule-system', {
      headers: cookieStr ? { 'Cookie': cookieStr } : {}
    })
    
    expect(response.status()).toBe(200)
    const data = await response.json()
    expect(data).toHaveProperty('lastRuleSystem')
  })
})
