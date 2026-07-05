import { test, expect } from '@playwright/test'

test.describe('New Match API Tests', () => {
  test('should fetch frequent opponents successfully', async ({ request }) => {
    const loginResp = await request.get('/api/auth/test-login?email=test@example.com&nickname=testuser')
    const cookiesArray = loginResp.headersArray().filter(h => h.name.toLowerCase() === 'set-cookie')
    let cookieStr = '';
    if (cookiesArray.length > 0) {
      cookieStr = cookiesArray.map(c => c.value.split(';')[0]).join('; ')
    }

    const response = await request.get('/api/users/me/frequent-opponents', {
      headers: cookieStr ? { 'Cookie': cookieStr } : {}
    })
    
    // We expect 200 or 404 depending on if it's mapped. Since it's getting 404, 
    // there's a problem with spring boot recompilation not taking effect.
    // Let's assert 200 and wait for it.
    expect(response.status()).toBe(200)
    const data = await response.json()
    expect(Array.isArray(data)).toBeTruthy()
  })

  test('should fetch last used rule system successfully', async ({ request }) => {
    const loginResp = await request.get('/api/auth/test-login?email=test@example.com&nickname=testuser')
    const cookiesArray = loginResp.headersArray().filter(h => h.name.toLowerCase() === 'set-cookie')
    let cookieStr = '';
    if (cookiesArray.length > 0) {
      cookieStr = cookiesArray.map(c => c.value.split(';')[0]).join('; ')
    }

    const response = await request.get('/api/users/me/preferences/last-rule-system', {
      headers: cookieStr ? { 'Cookie': cookieStr } : {}
    })
    
    expect(response.status()).toBe(200)
    const data = await response.json()
    expect(data).toHaveProperty('lastRuleSystem')
  })
})
