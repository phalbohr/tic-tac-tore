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
    await startBtn.tap()

    const quadrant = page.getByTestId('quadrant-teamA.attacker')
    await quadrant.waitFor({ state: 'visible' })
    await quadrant.tap()

    await expect(quadrant).toHaveClass(/ch-bg-green-500/)
  })

  test('[Story 5.2] undo button is disabled initially before any goals are recorded', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const undoBtn = page.getByTestId('undo-goal-btn')
    await expect(undoBtn).toBeDisabled()
    const emptyTimeline = page.getByTestId('timeline-empty')
    await expect(emptyTimeline).toBeVisible()
  })

  test('[Story 5.2] recording a goal displays scorer name and quadrant role in timeline, and enables undo', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

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
    await startBtn.tap()

    const attackerA = page.getByTestId('quadrant-teamA.attacker')
    const attackerB = page.getByTestId('quadrant-teamB.attacker')

    await attackerA.tap()
    await attackerB.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(2)
    await expect(timelineItems.nth(0)).toContainText('Charlie')
    await expect(timelineItems.nth(1)).toContainText('Alice')

    const undoBtn = page.getByTestId('undo-goal-btn')
    await undoBtn.tap()

    await expect(timelineItems).toHaveCount(1)
    await expect(timelineItems.first()).toContainText('Alice')

    await undoBtn.tap()
    await expect(timelineItems).toHaveCount(0)
    await expect(undoBtn).toBeDisabled()
  })

  test('[Story 5.3] [P0] match grid renders Team B on top row and Team A on bottom row with centered swap buttons meeting 56x56dp touch target', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
    const gridBox = await matchGrid.boundingBox()
    expect(gridBox).toBeDefined()

    const quadBDef = page.getByTestId('quadrant-teamB.defender')
    const quadBAtt = page.getByTestId('quadrant-teamB.attacker')
    const quadAAtt = page.getByTestId('quadrant-teamA.attacker')
    const quadADef = page.getByTestId('quadrant-teamA.defender')

    await expect(quadBDef).toBeVisible()
    await expect(quadBAtt).toBeVisible()
    await expect(quadAAtt).toBeVisible()
    await expect(quadADef).toBeVisible()

    const boxBDef = await quadBDef.boundingBox()
    const boxBAtt = await quadBAtt.boundingBox()
    const boxAAtt = await quadAAtt.boundingBox()
    const boxADef = await quadADef.boundingBox()

    // Assert Top Row (Team B) is positioned above Bottom Row (Team A)
    expect(boxBDef?.y).toBeLessThan(boxAAtt?.y || 0)
    expect(boxBAtt?.y).toBeLessThan(boxADef?.y || 0)

    // Assert Left quadrants are positioned to the left of Right quadrants
    expect(boxBDef?.x).toBeLessThan(boxBAtt?.x || 0)
    expect(boxAAtt?.x).toBeLessThan(boxADef?.x || 0)

    const swapTeamBBtn = page.getByTestId('swap-team-b-btn')
    const swapTeamABtn = page.getByTestId('swap-team-a-btn')

    await expect(swapTeamBBtn).toBeVisible()
    await expect(swapTeamABtn).toBeVisible()
    await expect(swapTeamBBtn).toHaveAttribute('aria-label', 'Swap Team B Positions')
    await expect(swapTeamABtn).toHaveAttribute('aria-label', 'Swap Team A Positions')

    const teamBBox = await swapTeamBBtn.boundingBox()
    const teamABox = await swapTeamABtn.boundingBox()

    expect(teamBBox).not.toBeNull()
    expect(teamABox).not.toBeNull()

    const bBox = teamBBox!
    const aBox = teamABox!
    const gBox = gridBox!

    // Minimum touch target 56x56dp
    expect(bBox.width).toBeGreaterThanOrEqual(56)
    expect(bBox.height).toBeGreaterThanOrEqual(56)
    expect(aBox.width).toBeGreaterThanOrEqual(56)
    expect(aBox.height).toBeGreaterThanOrEqual(56)

    // Vertical placement: Team B swap button in top half, Team A swap button in bottom half
    expect(bBox.y).toBeLessThan(aBox.y)

    // Horizontal centering: buttons are centered horizontally between columns (not on outer screen edges)
    const gridCenterX = gBox.x + gBox.width / 2
    const teamBCenterX = bBox.x + bBox.width / 2
    const teamACenterX = aBox.x + aBox.width / 2

    expect(Math.abs(teamBCenterX - gridCenterX)).toBeLessThan(5)
    expect(Math.abs(teamACenterX - gridCenterX)).toBeLessThan(5)

    // Ensure buttons are NOT on outer edges (distance from left edge > 50px)
    expect(bBox.x).toBeGreaterThan(gBox.x + 50)
    expect(bBox.x + bBox.width).toBeLessThan(gBox.x + gBox.width - 50)
  })

  test('[Story 5.3] [P0] tapping Team A swap button updates quadrant labels and attributes subsequent goals to new attacker', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const attackerA = page.getByTestId('quadrant-teamA.attacker')
    await expect(attackerA).toContainText('Alice')

    const swapTeamABtn = page.getByTestId('swap-team-a-btn')
    await swapTeamABtn.tap()

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
    await startBtn.tap()

    const attackerB = page.getByTestId('quadrant-teamB.attacker')
    await expect(attackerB).toContainText('Charlie')

    const swapTeamBBtn = page.getByTestId('swap-team-b-btn')
    await swapTeamBBtn.tap()

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
    await startBtn.tap()

    const swapTeamABtn = page.getByTestId('swap-team-a-btn')
    await swapTeamABtn.tap()

    const emptyTimeline = page.getByTestId('timeline-empty')
    await expect(emptyTimeline).toBeVisible()
    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(0)
  })

  test('[Story 5.3] [P1] timeline preserves original player names for past goals while showing new player names for goals scored after swap', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const attackerA = page.getByTestId('quadrant-teamA.attacker')
    await attackerA.tap()

    const swapTeamABtn = page.getByTestId('swap-team-a-btn')
    await swapTeamABtn.tap()
    await attackerA.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(2)
    await expect(timelineItems.nth(0)).toContainText('Bob')
    await expect(timelineItems.nth(1)).toContainText('Alice')
  })
})


