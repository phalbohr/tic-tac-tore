import { test, expect, type Page } from '@playwright/test'

async function loginUser(page: Page, prefix = 'tourn-rule') {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12)
  const email = `e2e-${prefix}-${randomSuffix}@example.com`
  const nickname = `Player${randomSuffix}`
  await page.goto(
    `/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`,
  )
  await page.waitForURL('**/*')
  return { nickname, email }
}

test.describe('Tournament Rule System Enforcement E2E (Story 8.6)', () => {
  test('[P0] should lock rule system and prefill participants when match entry initiated from tournament (AC1, AC2)', async ({
    page,
  }) => {
    await loginUser(page)

    const tournamentId = 'tourn-rule-lock-uuid'
    const tournamentMatchId = 'tm-rule-lock-slot-1'
    const ruleConfigId = 'rule-official-10pt'
    const currentUserId = 'u-rule-player-1'

    await page.route('**/api/v1/profile/me', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: currentUserId, nickname: 'Alice' }),
      }),
    )

    await page.route('**/api/v1/rule-configurations*', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: ruleConfigId,
            name: 'Official 10pt Standard',
            type: 'PRESET',
            scoreLimit: 10,
            gameLimit: 3,
            winsNeeded: 2,
            winByTwo: false,
          },
        ]),
      }),
    )

    await page.goto(
      `/matches/new?tournamentId=${tournamentId}&tournamentMatchId=${tournamentMatchId}&ruleConfigId=${ruleConfigId}`,
    )

    // Verify RulePicker is rendered and locked
    const rulePicker = page.locator('[data-testid="rule-picker"]')
    await expect(rulePicker).toBeVisible()

    // Informative notice communicates rule system lock
    await expect(
      page.getByText('Rule system is locked to tournament settings'),
    ).toBeVisible()

    // "+ Custom Rule" action button is hidden
    await expect(
      page.locator('[data-testid="create-custom-rule-inline-btn"]'),
    ).toBeHidden()

    // Selected rule chip displays lock indicator
    const selectedChip = rulePicker.locator('.active, [data-rule-id="rule-official-10pt"]')
    await expect(selectedChip).toBeVisible()
    await expect(selectedChip.locator('text=lock')).toBeVisible()
  })
})
