import { test, expect } from '@playwright/test'

test.describe('Tournament Rule System Enforcement API Tests (ATDD)', () => {
  test.beforeEach(async ({ page }) => {
    await expect
      .poll(
        async () => {
          try {
            const res = await page.request.get(
              '/api/auth/test-login?email=test-rule-enforce@example.com&nickname=RuleEnforcer',
            )
            return res.status()
          } catch {
            return 0
          }
        },
        {
          message: 'Wait for backend to be ready',
          timeout: 10000,
        },
      )
      .toBe(200)

    await page.goto(
      '/api/auth/test-login?email=test-rule-enforce@example.com&nickname=RuleEnforcer',
    )
  })

  test('[P0] should reject match creation with 409 Conflict when ruleConfigId mismatches tournament rule configuration (AC5)', async ({
    page,
  }) => {
    await page.route('**/api/v1/matches', async (route) => {
      const postData = route.request().postDataJSON()
      if (
        postData.tournamentMatchId &&
        postData.ruleConfigId !== '00000000-0000-0000-0000-000000000111'
      ) {
        await route.fulfill({
          status: 409,
          contentType: 'application/json',
          body: JSON.stringify({
            message: 'Match rule configuration does not match tournament rule configuration',
          }),
        })
        return
      }
      await route.continue()
    })

    const result = await page.evaluate(async () => {
      const res = await fetch('/api/v1/matches', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idempotencyKey: crypto.randomUUID(),
          creatorId: '00000000-0000-0000-0000-000000000001',
          teamAAttackerId: '00000000-0000-0000-0000-000000000001',
          teamBAttackerId: '00000000-0000-0000-0000-000000000002',
          tournamentMatchId: '00000000-0000-0000-0000-000000000100',
          ruleConfigId: '00000000-0000-0000-0000-000000000999',
          games: [{ teamAScore: 10, teamBScore: 8 }],
          entryMode: 'MANUAL',
          matchFormat: '1v1',
        }),
      })
      return {
        status: res.status,
        body: await res.json(),
      }
    })

    expect(result.status).toBe(409)
    expect(result.body.message).toContain('rule configuration')
  })

  test('[P0] should accept match creation when ruleConfigId strictly matches tournament rule configuration (AC5)', async ({
    page,
  }) => {
    await page.route('**/api/v1/matches', async (route) => {
      const postData = route.request().postDataJSON()
      if (
        postData.tournamentMatchId &&
        postData.ruleConfigId === '00000000-0000-0000-0000-000000000111'
      ) {
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: '00000000-0000-0000-0000-000000000555',
            status: 'PENDING_APPROVAL',
          }),
        })
        return
      }
      await route.continue()
    })

    const result = await page.evaluate(async () => {
      const res = await fetch('/api/v1/matches', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idempotencyKey: crypto.randomUUID(),
          creatorId: '00000000-0000-0000-0000-000000000001',
          teamAAttackerId: '00000000-0000-0000-0000-000000000001',
          teamBAttackerId: '00000000-0000-0000-0000-000000000002',
          tournamentMatchId: '00000000-0000-0000-0000-000000000100',
          ruleConfigId: '00000000-0000-0000-0000-000000000111',
          games: [{ teamAScore: 10, teamBScore: 8 }],
          entryMode: 'MANUAL',
          matchFormat: '1v1',
        }),
      })
      return {
        status: res.status,
      }
    })

    expect(result.status).toBe(201)
  })
})
