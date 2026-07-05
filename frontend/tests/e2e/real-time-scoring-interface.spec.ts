import { test, expect } from '@playwright/test'

test.describe('Real-time Scoring Interface', () => {
  test('tapping screen quadrant awards goal to specific player and provides haptic feedback', async ({ page, context }) => {
    await context.grantPermissions([])
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'vibrate', {
        value: () => true,
        writable: true,
      })
    })
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const quadrant = page.getByTestId('quadrant-teamA.attacker')
    await quadrant.waitFor({ state: 'visible' })
    await quadrant.tap()

    await expect(quadrant).toHaveClass(/flashing/)
  })
})
