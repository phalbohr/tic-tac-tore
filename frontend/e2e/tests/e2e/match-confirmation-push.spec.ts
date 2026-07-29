import { test, expect } from '@playwright/test'

test.describe('Story 3.1: Confirmation Requests & Push Notifications E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/api/auth/test-login?email=test@example.com&nickname=testuser')
  })

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

  test('[P1] should display pending match badge when pendingCount > 0', async ({ page }) => {
    await page.addInitScript(() => {
      window.__PENDING_COUNT__ = 2
    })

    await page.route('**/api/v1/matches/pending', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ count: 2, matches: [] }),
      })
    })

    await page.reload()
    await expect(page.getByTestId('pending-badge-counter')).toBeVisible()
    await expect(page.getByTestId('pending-badge-counter')).toHaveText('2')
  })

  test('[P1] should display enable-notifications CTA when permission is default', async ({ page }) => {
    await page.addInitScript(() => {
      Object.defineProperty(window, 'Notification', {
        value: { permission: 'default', requestPermission: async () => 'granted' },
        configurable: true,
      })
    })

    await page.goto('/')
    await expect(page.getByTestId('enable-notifications-btn')).toBeVisible()
    await expect(page.getByText(/Enable push notifications/i)).toBeVisible()
  })
})
