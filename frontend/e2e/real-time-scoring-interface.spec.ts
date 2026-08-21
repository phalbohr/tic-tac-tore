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

  test('[Story 5.2] undo button is disabled initially before any goals are recorded', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const undoBtn = page.getByTestId('undo-goal-btn')
    await expect(undoBtn).toBeDisabled()
    const emptyTimeline = page.getByTestId('timeline-empty')
    await expect(emptyTimeline).toBeVisible()
  })

  test('[Story 5.2] recording a goal displays scorer name and quadrant role in timeline, and enables undo', async ({ page }) => {
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

  test('[Story 5.2] recording multiple goals displays them in reverse chronological order and undo removes the latest goal', async ({ page }) => {
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

  test('[Story 5.3] [P0] match grid renders Team B on top row and Team A on bottom row with centered swap buttons meeting 56x56dp touch target', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const swapTeamBBtn = page.getByTestId('swap-team-b-btn')
    const swapTeamABtn = page.getByTestId('swap-team-a-btn')

    await expect(swapTeamBBtn).toBeVisible()
    await expect(swapTeamABtn).toBeVisible()
    await expect(swapTeamBBtn).toHaveAttribute('aria-label', 'Swap Team B Positions')
    await expect(swapTeamABtn).toHaveAttribute('aria-label', 'Swap Team A Positions')

    const teamBBox = await swapTeamBBtn.boundingBox()
    const teamABox = await swapTeamABtn.boundingBox()

    expect(teamBBox?.width).toBeGreaterThanOrEqual(56)
    expect(teamBBox?.height).toBeGreaterThanOrEqual(56)
    expect(teamABox?.width).toBeGreaterThanOrEqual(56)
    expect(teamABox?.height).toBeGreaterThanOrEqual(56)
    expect(teamBBox?.y).toBeLessThan(teamABox?.y || 0)
  })

  test('[Story 5.3] [P0] tapping Team A swap button updates quadrant labels and attributes subsequent goals to new attacker', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const attackerA = page.getByTestId('quadrant-teamA.attacker')
    await expect(attackerA).toContainText('Alice')

    const swapTeamABtn = page.getByTestId('swap-team-a-btn')
    await swapTeamABtn.click()

    await expect(attackerA).toContainText('Bob')

    await attackerA.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(1)
    await expect(timelineItems.first()).toContainText('Bob')
    await expect(timelineItems.first()).toContainText('teamA.attacker')
  })

  test('[Story 5.3] [P0] tapping Team B swap button updates quadrant labels and attributes subsequent goals to new attacker', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const attackerB = page.getByTestId('quadrant-teamB.attacker')
    await expect(attackerB).toContainText('Charlie')

    const swapTeamBBtn = page.getByTestId('swap-team-b-btn')
    await swapTeamBBtn.click()

    await expect(attackerB).toContainText('Dave')

    await attackerB.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(1)
    await expect(timelineItems.first()).toContainText('Dave')
    await expect(timelineItems.first()).toContainText('teamB.attacker')
  })

  test('[Story 5.3] [P1] tapping swap button does not trigger accidental goal registration on underlying quadrants', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const swapTeamABtn = page.getByTestId('swap-team-a-btn')
    await swapTeamABtn.click()

    const emptyTimeline = page.getByTestId('timeline-empty')
    await expect(emptyTimeline).toBeVisible()
    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(0)
  })

  test('[Story 5.3] [P1] timeline preserves original player names for past goals while showing new player names for goals scored after swap', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.click()

    const attackerA = page.getByTestId('quadrant-teamA.attacker')
    await attackerA.tap()

    const swapTeamABtn = page.getByTestId('swap-team-a-btn')
    await swapTeamABtn.click()
    await attackerA.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(2)
    await expect(timelineItems.nth(0)).toContainText('Bob')
    await expect(timelineItems.nth(1)).toContainText('Alice')
  })
})

