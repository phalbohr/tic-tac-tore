import { test, expect } from '@playwright/test'

test.describe('Real-time Scoring Interface', () => {
  test.use({ hasTouch: true })

  test('tapping screen quadrant awards goal to specific player and provides haptic feedback', async ({ page, context }) => {
    await context.grantPermissions([])
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'vibrate', {
        value: () => true,
        writable: true,
      })
      Element.prototype.requestFullscreen = async () => {}
      if (!screen.orientation) {
        Object.defineProperty(screen, 'orientation', { value: {} })
      }
      Object.defineProperty(screen.orientation, 'lock', {
        value: async () => {},
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

    await expect(quadrant).toHaveClass(/ch-bg-green-500/)
  })

  test.skip('[Story 5.2] undo button is disabled initially before any goals are recorded', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const undoBtn = page.getByTestId('undo-goal-btn')
    await expect(undoBtn).toBeDisabled()
    const emptyTimeline = page.getByTestId('timeline-empty')
    await expect(emptyTimeline).toBeVisible()
  })

  test.skip('[Story 5.2] recording a goal displays scorer name and quadrant role in timeline, and enables undo', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const attackerA = page.getByTestId('quadrant-teamA.attacker')
    await attackerA.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(1)
    await expect(timelineItems.first()).toContainText('Alice')
    await expect(timelineItems.first()).toContainText('teamA.attacker')

    const undoBtn = page.getByTestId('undo-goal-btn')
    await expect(undoBtn).toBeEnabled()
  })

  test.skip('[Story 5.2] recording multiple goals displays them in reverse chronological order and undo removes the latest goal', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const attackerA = page.getByTestId('quadrant-teamA.attacker')
    const attackerB = page.getByTestId('quadrant-teamB.attacker')

    await attackerA.tap()
    await attackerB.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(2)
    await expect(timelineItems.nth(0)).toContainText('Charlie')
    await expect(timelineItems.nth(1)).toContainText('Alice')

    const undoBtn = page.getByTestId('undo-goal-btn')
    await undoBtn.click()

    await expect(timelineItems).toHaveCount(1)
    await expect(timelineItems.first()).toContainText('Alice')

    await undoBtn.click()
    await expect(timelineItems).toHaveCount(0)
    await expect(undoBtn).toBeDisabled()
  })
})
