import { test, expect, type Page } from '@playwright/test'

async function loginUser(page: Page, prefix = 'tourn-arch') {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12)
  const email = `e2e-${prefix}-${randomSuffix}@example.com`
  const nickname = `Player${randomSuffix}`
  await page.goto(
    `/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`,
  )
  await page.waitForURL('**/*')
  return { nickname, email }
}

test.describe.skip('Tournament Standings & Archive E2E (Story 8.7)', () => {
  test('[P0] should view archive tab, open completed tournament, and view standings table (AC1, AC6, AC7)', async ({
    page,
  }) => {
    await loginUser(page)

    const tournamentId = 'arch-tourn-uuid-1'

    await page.route('**/api/v1/tournaments?status=COMPLETED*', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              id: tournamentId,
              title: 'Championship 2025 Retrospective',
              format: 'CHAMPIONSHIP',
              mode: 'ONE_VS_ONE',
              status: 'COMPLETED',
              participantCount: 4,
              createdAt: '2025-12-01T10:00:00Z',
              updatedAt: '2025-12-05T18:00:00Z',
            },
          ],
          totalPages: 1,
          totalElements: 1,
        }),
      }),
    )

    await page.route(`**/api/v1/tournaments/${tournamentId}/standings`, (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            registrationId: 'reg-champ',
            userId: 'u-1',
            nickname: 'GrandMaster',
            matchesPlayed: 3,
            wins: 3,
            losses: 0,
            gamesWon: 6,
            gamesLost: 1,
            gameDifference: 5,
            points: 9,
            isEliminated: false,
            rank: 1,
          },
        ]),
      }),
    )

    await page.goto('/tournaments')

    // Click Archive Tab
    const archiveTab = page.locator('[data-testid="tab-archive"]')
    await expect(archiveTab).toBeVisible()
    await archiveTab.click()

    // Tournament card is visible in archive
    await expect(page.getByText('Championship 2025 Retrospective')).toBeVisible()

    // Open tournament details/bracket modal
    await page.getByText('Championship 2025 Retrospective').click()

    // Switch to Standings view tab
    const standingsTab = page.locator('[data-testid="tab-standings"]')
    await expect(standingsTab).toBeVisible()
    await standingsTab.click()

    // Check Standings table rendered
    await expect(page.getByText('GrandMaster')).toBeVisible()
    await expect(page.getByText('9')).toBeVisible()
  })
})
