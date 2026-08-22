import { test, expect } from '@playwright/test'

test.describe('[Story 5.5] Screen Wake Lock & Continuity (ATDD Red Phase)', () => {
  test.use({
    viewport: { width: 844, height: 390 }, // Landscape
    hasTouch: true,
  })

  test.beforeEach(async ({ page, context }) => {
    await context.grantPermissions([])
    await page.addInitScript(() => {
      // Mock Screen Wake Lock API
      const wakeLockCalls: string[] = []
      let wakeLockReleased = false

      const mockSentinel = {
        released: false,
        type: 'screen',
        release: async () => {
          wakeLockReleased = true
          mockSentinel.released = true
        },
        addEventListener: () => {},
        removeEventListener: () => {},
      }

      Object.defineProperty(window, '__wakeLockTest', {
        value: {
          getCalls: () => wakeLockCalls,
          isReleased: () => wakeLockReleased,
          sentinel: mockSentinel,
        },
        writable: true,
      })

      if (!navigator.wakeLock) {
        Object.defineProperty(navigator, 'wakeLock', {
          value: {
            request: async (type: string) => {
              wakeLockCalls.push(type)
              wakeLockReleased = false
              return mockSentinel
            },
          },
          configurable: true,
          writable: true,
        })
      }

      // Safe mocks for orientation and fullscreen
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

  test.skip('[Story 5.5] [P0] AC1: requests screen wake lock when starting match in landscape player mode', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const calls = await page.evaluate(() => (window as any).__wakeLockTest?.getCalls())
    expect(calls).toContain('screen')

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
  })

  test.skip('[Story 5.5] [P0] AC1: requests screen wake lock when starting match in portrait referee mode', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/live-match?mode=referee')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const calls = await page.evaluate(() => (window as any).__wakeLockTest?.getCalls())
    expect(calls).toContain('screen')

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
  })

  test.skip('[Story 5.5] [P0] AC2: re-requests wake lock when document visibility returns to visible during active match', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    // Trigger visibility change to hidden then visible
    await page.evaluate(() => {
      Object.defineProperty(document, 'visibilityState', { value: 'hidden', configurable: true })
      document.dispatchEvent(new Event('visibilitychange'))
    })

    await page.evaluate(() => {
      Object.defineProperty(document, 'visibilityState', { value: 'visible', configurable: true })
      document.dispatchEvent(new Event('visibilitychange'))
    })

    const calls = await page.evaluate(() => (window as any).__wakeLockTest?.getCalls())
    expect(calls?.length).toBeGreaterThanOrEqual(2)
  })

  test.skip('[Story 5.5] [P0] AC3: releases wake lock sentinel when navigating away from live match', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    // Navigate away to trigger unmount
    await page.goto('/dashboard')

    const released = await page.evaluate(() => (window as any).__wakeLockTest?.isReleased())
    expect(released).toBe(true)
  })

  test.skip('[Story 5.5] [P1] AC4: match starts and runs normally without errors when wake lock API rejects request', async ({ page }) => {
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'wakeLock', {
        value: {
          request: async () => {
            throw new Error('NotAllowedError: Wake Lock permission denied by system')
          },
        },
        configurable: true,
        writable: true,
      })
    })

    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
  })
})
