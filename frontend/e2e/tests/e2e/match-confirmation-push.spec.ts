import { test, expect } from '@playwright/test'

test.describe('Story 3.1: Confirmation Requests & Push Notifications E2E', () => {
  test('[P0] should resolve deep-link match review stub cleanly without 404s', async ({ page }) => {
    await page.goto('/match/test-match-id-123/review')
    await expect(page.getByText(/Review Match #test-match-id-123|Match Processed/i)).toBeVisible()
    await expect(page.getByRole('link', { name: /Return to Home Hub/i })).toBeVisible()
  })

  test('[P1] should show non-blocking warning banner when push notification permission is denied', async ({ page }) => {
    await page.addInitScript(() => {
      Object.defineProperty(window, 'Notification', {
        value: { permission: 'denied', requestPermission: async () => 'denied' },
        configurable: true,
      })
    })

    await page.goto('/')
    await expect(page.getByTestId('permission-warning-banner')).toBeVisible()
    await expect(page.getByText(/Push notifications are disabled/i)).toBeVisible()
  })
})
