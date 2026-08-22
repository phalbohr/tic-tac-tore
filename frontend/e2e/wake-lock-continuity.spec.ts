import { test, expect } from '@playwright/test'

interface WakeLockTestSpy {
  getCalls: () => string[]
  isReleased: () => boolean
  sentinel: {
    released: boolean
    type: string
    release: () => Promise<void>
    addEventListener: () => void
    removeEventListener: () => void
  }
}

declare global {
  interface Window {
    __wakeLockTest?: WakeLockTestSpy
  }
}

test.describe('[Story 5.5] Screen Wake Lock & Continuity', () => {
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
        configurable: true,
        writable: true,
      })

      const mockWakeLock = {
        request: async (type: string) => {
          wakeLockCalls.push(type)
          wakeLockReleased = false
          return mockSentinel
        },
      }

      try {
        Object.defineProperty(navigator, 'wakeLock', {
          value: mockWakeLock,
          configurable: true,
          writable: true,
        })
      } catch {
        // Fallback for browsers with non-configurable navigator.wakeLock
        try {
          Object.assign(navigator, { wakeLock: mockWakeLock })
        } catch {}
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

  test('[Story 5.5] [P0] AC1: requests screen wake lock when starting match in landscape player mode', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    await expect.poll(async () => {
      return await page.evaluate(() => window.__wakeLockTest?.getCalls())
    }).toContain('screen')

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
  })

  test('[Story 5.5] [P0] AC1: requests screen wake lock when starting match in portrait referee mode', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/live-match?mode=referee')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    await expect.poll(async () => {
      return await page.evaluate(() => window.__wakeLockTest?.getCalls())
    }).toContain('screen')

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
  })

  test('[Story 5.5] [P0] AC2: re-requests wake lock when document visibility returns to visible during active match', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    await expect.poll(async () => {
      const calls = await page.evaluate(() => window.__wakeLockTest?.getCalls())
      return calls?.length
    }).toBeGreaterThanOrEqual(1)

    // Trigger visibility change to hidden then visible
    await page.evaluate(() => {
      Object.defineProperty(document, 'visibilityState', { value: 'hidden', configurable: true })
      document.dispatchEvent(new Event('visibilitychange'))
    })

    await page.evaluate(() => {
      Object.defineProperty(document, 'visibilityState', { value: 'visible', configurable: true })
      document.dispatchEvent(new Event('visibilitychange'))
    })

    await expect.poll(async () => {
      const calls = await page.evaluate(() => window.__wakeLockTest?.getCalls())
      return calls?.length
    }).toBeGreaterThanOrEqual(2)
  })

  test('[Story 5.5] [P0] AC3: releases wake lock sentinel when navigating away from live match', async ({ page }) => {
    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    await expect.poll(async () => {
      const calls = await page.evaluate(() => window.__wakeLockTest?.getCalls())
      return calls?.length
    }).toBeGreaterThanOrEqual(1)

    // Navigate client-side to unmount LiveMatch.vue without reloading whole page
    await page.evaluate(() => {
      window.history.pushState({}, '', '/leaderboard')
      window.dispatchEvent(new PopStateEvent('popstate'))
    })

    await expect.poll(async () => {
      return await page.evaluate(() => window.__wakeLockTest?.isReleased())
    }).toBe(true)
  })

  test('[Story 5.5] [P1] AC4: match starts and runs normally without errors when wake lock API rejects request', async ({ page }) => {
    await page.addInitScript(() => {
      const failingWakeLock = {
        request: async () => {
          throw new Error('NotAllowedError: Wake Lock permission denied by system')
        },
      }
      try {
        Object.defineProperty(navigator, 'wakeLock', {
          value: failingWakeLock,
          configurable: true,
          writable: true,
        })
      } catch {
        try {
          Object.assign(navigator, { wakeLock: failingWakeLock })
        } catch {}
      }
    })

    await page.goto('/live-match')
    const startBtn = page.getByTestId('start-match-btn')
    await startBtn.waitFor({ state: 'visible' })
    await startBtn.tap()

    const matchGrid = page.getByTestId('match-grid')
    await expect(matchGrid).toBeVisible()
  })
})
