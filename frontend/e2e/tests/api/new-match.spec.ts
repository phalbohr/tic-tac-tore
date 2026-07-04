import { test, expect } from '@playwright/test';

test.describe('New Match API Tests (ATDD)', () => {
  test.skip('[P1] should fetch frequent opponents successfully', async ({ request }) => {
    // THIS TEST WILL FAIL - Endpoint not implemented yet
    const response = await request.get('/api/users/me/frequent-opponents');
    
    expect(response.status()).toBe(200);
    
    const opponents = await response.json();
    expect(Array.isArray(opponents)).toBe(true);
    if (opponents.length > 0) {
      expect(opponents[0]).toMatchObject({
        id: expect.any(String),
        username: expect.any(String),
        avatarUrl: expect.any(String)
      });
    }
  });

  test.skip('[P1] should fetch last used rule system successfully', async ({ request }) => {
    // THIS TEST WILL FAIL - Endpoint not implemented yet
    const response = await request.get('/api/users/me/preferences/last-rule-system');
    
    expect(response.status()).toBe(200);
    
    const preferences = await response.json();
    expect(preferences).toMatchObject({
      ruleSystem: expect.any(String)
    });
  });
});
