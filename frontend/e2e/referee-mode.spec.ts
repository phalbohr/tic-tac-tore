import { test, expect } from '@playwright/test'

test.describe('[Story 5.4] Third-party Referee Mode', () => {
  test.use({
    viewport: { width: 390, height: 844 },
    hasTouch: true,
  })

  test.beforeEach(async ({ page, context }) => {
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
  })

  test('[P0] AC1: ?mode=referee query parameter starts in portrait mode without landscape rotation overlay', async ({ page }) => {
    await page.goto('/live-match?mode=referee')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const rotationOverlay = page.getByTestId('rotation-warning-overlay')
    await expect(rotationOverlay).toBeHidden()

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
  })

  test('[P0] AC2: renders 2x2 grid representing table viewed from the end (Left: Team B defender/attacker, Right: Team A attacker/defender)', async ({ page }) => {
    await page.goto('/live-match?mode=referee')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

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

    expect(boxBDef).not.toBeNull()
    expect(boxBAtt).not.toBeNull()
    expect(boxAAtt).not.toBeNull()
    expect(boxADef).not.toBeNull()

    // Left column: Team B defender (top-left) above Team B attacker (bottom-left)
    expect(boxBDef!.x).toBeLessThan(boxAAtt!.x)
    expect(boxBAtt!.x).toBeLessThan(boxADef!.x)
    expect(boxBDef!.y).toBeLessThan(boxBAtt!.y)

    // Right column: Team A attacker (top-right) above Team A defender (bottom-right)
    expect(boxAAtt!.y).toBeLessThan(boxADef!.y)
  })

  test('[P0] AC1: automatic detection enables portrait referee mode when authenticated user is not among registered players', async ({ page, baseURL }) => {
    await page.context().addCookies([
      {
        name: 'TTT_SESSION',
        value: '1',
        url: baseURL || 'http://localhost:3000',
      },
    ])
    await page.route('**/api/v1/profile/me', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'referee-user-999',
          nickname: 'RefereeViktor',
          avatar: 'referee.png',
        }),
      })
    })

    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const rotationOverlay = page.getByTestId('rotation-warning-overlay')
    await expect(rotationOverlay).toBeHidden()

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
  })

  test('[P0] AC2 & AC3: tapping quadrants attributes goals to player and role in live timeline', async ({ page }) => {
    await page.goto('/live-match?mode=referee')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const quadAAtt = page.getByTestId('quadrant-teamA.attacker')
    await quadAAtt.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(1)
    await expect(timelineItems.first()).toContainText('Alice')
    await expect(timelineItems.first()).toContainText('teamA.attacker')
  })

  test('[P0] AC4: undo button removes last goal and disables when empty in referee mode', async ({ page }) => {
    await page.goto('/live-match?mode=referee')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const quadAAtt = page.getByTestId('quadrant-teamA.attacker')
    await quadAAtt.tap()

    const undoBtn = page.getByTestId('undo-goal-btn')
    await expect(undoBtn).toBeEnabled()
    await undoBtn.tap()

    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(0)
    await expect(undoBtn).toBeDisabled()
  })

  test('[P0] AC5: swap buttons positioned centered in respective columns meeting 56x56dp touch targets', async ({ page }) => {
    await page.goto('/live-match?mode=referee')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const swapTeamBBtn = page.getByTestId('swap-team-b-btn')
    const swapTeamABtn = page.getByTestId('swap-team-a-btn')

    await expect(swapTeamBBtn).toBeVisible()
    await expect(swapTeamABtn).toBeVisible()

    const boxSwapB = await swapTeamBBtn.boundingBox()
    const boxSwapA = await swapTeamABtn.boundingBox()
    const quadBDef = await page.getByTestId('quadrant-teamB.defender').boundingBox()
    const quadAAtt = await page.getByTestId('quadrant-teamA.attacker').boundingBox()

    expect(boxSwapB).not.toBeNull()
    expect(boxSwapA).not.toBeNull()
    expect(quadBDef).not.toBeNull()
    expect(quadAAtt).not.toBeNull()

    // 56x56dp minimum touch target
    expect(boxSwapB!.width).toBeGreaterThanOrEqual(56)
    expect(boxSwapB!.height).toBeGreaterThanOrEqual(56)
    expect(boxSwapA!.width).toBeGreaterThanOrEqual(56)
    expect(boxSwapA!.height).toBeGreaterThanOrEqual(56)

    // Swap B is in left column (near Team B quadrants), Swap A is in right column (near Team A quadrants)
    expect(boxSwapB!.x).toBeLessThan(boxSwapA!.x)
    expect(Math.abs(boxSwapB!.x - quadBDef!.x)).toBeLessThan(quadBDef!.width)
    expect(Math.abs(boxSwapA!.x - quadAAtt!.x)).toBeLessThan(quadAAtt!.width)
  })

  test('[P1] AC5: tapping swap button updates future goal attribution without accidental goal registration', async ({ page }) => {
    await page.goto('/live-match?mode=referee')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const quadAAtt = page.getByTestId('quadrant-teamA.attacker')
    await expect(quadAAtt).toContainText('Alice')

    const swapTeamABtn = page.getByTestId('swap-team-a-btn')
    await swapTeamABtn.tap()

    // No accidental goal scored on tap
    const timelineItems = page.getByTestId('timeline-goal-item')
    await expect(timelineItems).toHaveCount(0)

    // Attacker role updated to Bob
    await expect(quadAAtt).toContainText('Bob')
    await quadAAtt.tap()

    await expect(timelineItems).toHaveCount(1)
    await expect(timelineItems.first()).toContainText('Bob')
  })
})
