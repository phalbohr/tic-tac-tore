import { test, expect } from '@playwright/test'

test.describe('New Match Creation E2E Tests', () => {
  // Use a fixed viewport to simulate mobile portrait
  test.use({ viewport: { width: 375, height: 667 } })

  test.beforeEach(async ({ page }) => {
    // Login with mock TestAuthController
    // Also add tutorialCompleted=true to the test login so we don't get the overlay
    await page.goto('/api/auth/test-login?email=test@example.com&nickname=testuser&tutorialCompleted=true')
  })

  test('should navigate from Home Hub to New Match and configure players', async ({ page }) => {
    // Dismiss the tutorial if it appears
    await page.addInitScript(() => {
      window.localStorage.setItem('tutorial-completed', 'true')
    })
    
    await page.goto('/')

    // Check for new match button
    const newMatchBtn = page.getByRole('button', { name: /New Match/i })
    await expect(newMatchBtn).toBeVisible()
    await newMatchBtn.click()

    // Wait for New Match inline view to appear
    await expect(page.getByText('Match Type')).toBeVisible()

    // Verify Portrait optimization (viewport is mobile)
    const boundingBox = await page.evaluate(() => {
      const body = document.body
      return { width: body.clientWidth, height: body.clientHeight }
    })
    expect(boundingBox.width).toBeLessThanOrEqual(500) // Ensure it's not expanding horizontally
    expect(boundingBox.height).toBeGreaterThan(boundingBox.width) // Ensure portrait ratio

    // Verify No-Line rule - zero <hr> tags
    const hrCount = await page.locator('hr').count()
    expect(hrCount).toBe(0)
    
    // Verify No-Line rule - no elements have border classes (approximate check for tailwind border classes)
    const elementsWithBorder = await page.locator('[class*="border-"], [class~="border"]').count()
    expect(elementsWithBorder).toBe(0)

    // Select 4_PLAYERS match type
    const fourPlayersBtn = page.getByRole('button', { name: /2v2/i })
    await fourPlayersBtn.click()

    // Verify 4 slots are rendered
    const playerSlots = page.locator('.player-slot')
    await expect(playerSlots).toHaveCount(4)
  })
})
