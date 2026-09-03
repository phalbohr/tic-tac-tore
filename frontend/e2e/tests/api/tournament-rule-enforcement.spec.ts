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

  // Skipped: Requires real DB seed fixtures for tournament_match and rule_configuration entities.
  // Real verification is covered in backend slice/unit tests (TournamentMatchValidatorTest, MatchServiceTest).
  test.skip('[P0] should reject match creation with 409 Conflict when ruleConfigId mismatches tournament rule configuration (AC5)', async ({
    page,
  }) => {
    const response = await page.request.post('/api/v1/matches', {
      data: {
        idempotencyKey: crypto.randomUUID(),
        creatorId: '00000000-0000-0000-0000-000000000001',
        teamAAttackerId: '00000000-0000-0000-0000-000000000001',
        teamBAttackerId: '00000000-0000-0000-0000-000000000002',
        tournamentMatchId: '00000000-0000-0000-0000-000000000100',
        ruleConfigId: '00000000-0000-0000-0000-000000000999',
        games: [{ teamAScore: 10, teamBScore: 8 }],
        entryMode: 'MANUAL',
        matchFormat: '1v1',
      },
    })

    expect(response.status()).toBe(409)
    const body = await response.json()
    expect(body.message).toContain('rule configuration')
  })

  // Skipped: Requires real DB seed fixtures for tournament_match and rule_configuration entities.
  test.skip('[P0] should accept match creation when ruleConfigId strictly matches tournament rule configuration (AC5)', async ({
    page,
  }) => {
    const response = await page.request.post('/api/v1/matches', {
      data: {
        idempotencyKey: crypto.randomUUID(),
        creatorId: '00000000-0000-0000-0000-000000000001',
        teamAAttackerId: '00000000-0000-0000-0000-000000000001',
        teamBAttackerId: '00000000-0000-0000-0000-000000000002',
        tournamentMatchId: '00000000-0000-0000-0000-000000000100',
        ruleConfigId: '00000000-0000-0000-0000-000000000111',
        games: [{ teamAScore: 10, teamBScore: 8 }],
        entryMode: 'MANUAL',
        matchFormat: '1v1',
      },
    })

    expect(response.status()).toBe(201)
  })
})
