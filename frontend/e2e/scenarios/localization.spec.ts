import { test, expect } from '@playwright/test'

test.describe('Localization', () => {
  test('[P1] shows English subtitle by default when no locale is stored', async ({ page }) => {
    await page.addInitScript(() => localStorage.removeItem('ttt_locale'))

    await page.goto('/')

    await expect(page.getByText('Foosball statistics platform')).toBeVisible()
  })

  test('[P1] shows German subtitle when ttt_locale=de is stored before navigation', async ({ page }) => {
    await page.addInitScript(() => localStorage.setItem('ttt_locale', 'de'))

    await page.goto('/')

    await expect(page.getByText('Tischkicker-Statistikplattform')).toBeVisible()
  })

  test('[P1] German locale persists across full page reload', async ({ page }) => {
    await page.addInitScript(() => localStorage.setItem('ttt_locale', 'de'))
    await page.goto('/')

    await page.reload()

    await expect(page.getByText('Tischkicker-Statistikplattform')).toBeVisible()
  })

  test('[P1] all user-facing strings render in German when locale is de', async ({ page }) => {
    await page.addInitScript(() => localStorage.setItem('ttt_locale', 'de'))

    await page.goto('/')

    await expect(page.getByText('Melde dich an, um deine Spiele zu verfolgen')).toBeVisible()
  })
})
