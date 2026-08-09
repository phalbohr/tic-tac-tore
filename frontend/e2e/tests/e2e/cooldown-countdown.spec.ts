import { test, expect } from '@playwright/test'
import { buildCooldownMatch, buildPendingResponse } from '../../fixtures/cooldown-fixtures'

test.describe('Story 3.5: Publication Rules & 24-hour Cooldown E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
      window.localStorage.setItem('tutorial-completed', 'true')
    })
    await page.goto('/api/auth/test-login?email=cooldown@example.com&nickname=CooldownUser&tutorialCompleted=true')
  })

  test('[P0] AC6: Should display cooldown countdown timer for PARTIALLY_CONFIRMED match on home page', async ({ page }) => {
    const futureExpiry = new Date(Date.now() + 2 * 60 * 60 * 1000 + 15 * 60 * 1000).toISOString()
    const match = buildCooldownMatch({ cooldownExpiresAt: futureExpiry })

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(buildPendingResponse([match])),
      })
    })

    await page.goto('/')

    const card = page.getByTestId(`pending-match-card-${match.id}`)
    await expect(card).toBeVisible()

    const timer = page.getByTestId(`cooldown-timer-${match.id}`)
    await expect(timer).toBeVisible()
    await expect(timer).toContainText('Auto-publish in 2h')
  })

  test('[P1] Should not display cooldown timer for PENDING_APPROVAL match', async ({ page }) => {
    const match = buildCooldownMatch({ id: 'match-pending-e2e', status: 'PENDING_APPROVAL', cooldownExpiresAt: undefined })

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(buildPendingResponse([match])),
      })
    })

    await page.goto('/')

    const timer = page.getByTestId(`cooldown-timer-${match.id}`)
    await expect(timer).toBeHidden()
  })

  test('[P0] AC2: Should confirm match when second opponent confirms during cooldown', async ({ page }) => {
    const futureExpiry = new Date(Date.now() + 60 * 60 * 1000).toISOString()
    const match = buildCooldownMatch({ id: 'match-confirm-e2e', cooldownExpiresAt: futureExpiry })
    let isConfirmed = false

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(buildPendingResponse(isConfirmed ? [] : [match])),
      })
    })

    await page.route('**/api/v1/matches/match-confirm-e2e/confirm', async (route) => {
      isConfirmed = true
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'match-confirm-e2e',
          status: 'CONFIRMED',
          confirmedByUserId: 'cooldown-user-uuid',
          confirmedAt: new Date().toISOString(),
          cooldownExpiresAt: null,
        }),
      })
    })

    await page.goto('/')

    const card = page.getByTestId(`pending-match-card-${match.id}`)
    await expect(card).toBeVisible()

    const confirmBtn = page.getByTestId(`confirm-match-btn-${match.id}`)
    await expect(confirmBtn).toBeVisible()
    await confirmBtn.click()

    await expect(card).not.toBeVisible({ timeout: 20000 })
  })

  test('[P1] Should display "Auto-publishing soon" when cooldownExpiresAt is in the past', async ({ page }) => {
    const pastExpiry = new Date(Date.now() - 60_000).toISOString()
    const match = buildCooldownMatch({ id: 'match-expired-e2e', cooldownExpiresAt: pastExpiry })

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(buildPendingResponse([match])),
      })
    })

    await page.goto('/')

    const timer = page.getByTestId(`cooldown-timer-${match.id}`)
    await expect(timer).toBeVisible()
    await expect(timer).toContainText('Auto-publishing soon')
  })
})
