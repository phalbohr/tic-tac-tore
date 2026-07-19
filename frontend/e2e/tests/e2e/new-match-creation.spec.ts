import { test, expect } from '@playwright/test'

test.describe('New Match Creation E2E Tests', () => {
  test.use({ viewport: { width: 375, height: 667 } })

  test.beforeEach(async ({ page }) => {
    await page.goto('/api/auth/test-login?email=test@example.com&nickname=testuser&tutorialCompleted=true')
  })

  test('should navigate from Home Hub to New Match and configure players', async ({ page }) => {
    await page.addInitScript(() => {
      window.localStorage.setItem('tutorial-completed', 'true')
    })
    
    await page.goto('/')

    const newMatchBtn = page.getByRole('button', { name: /New Match/i })
    await expect(newMatchBtn).toBeVisible()
    await newMatchBtn.click()

    await expect(page.getByText('Match Type')).toBeVisible()

    const scrollState = await page.evaluate(() => {
      return {
        bodyScrollWidth: document.body.scrollWidth,
        docClientWidth: document.documentElement.clientWidth
      }
    })
    
    expect(scrollState.bodyScrollWidth).toBe(scrollState.docClientWidth)

    const hrCount = page.locator('hr')
    await expect(hrCount).toHaveCount(0)
    
    const elementsWithBorder = page.locator('[class*="border-"], [class~="border"]')
    await expect(elementsWithBorder).toHaveCount(0)

    const fourPlayersBtn = page.getByRole('button', { name: /2v2/i })
    await fourPlayersBtn.click()

    const playerSlots = page.locator('.player-slot')
    await expect(playerSlots).toHaveCount(4)
  })
})
