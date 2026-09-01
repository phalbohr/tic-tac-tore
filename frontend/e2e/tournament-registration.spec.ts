import { test, expect, type Page } from '@playwright/test';

async function loginUser(page: Page, prefix = 'tourn') {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-${prefix}-${randomSuffix}@example.com`;
  const nickname = `Player${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('Tournament Registration & Confirmation E2E (Story 8.2)', () => {
  test('[P0] should complete solo registration for 1v1 tournament and display Confirmed status (AC 1, AC 7, AC 8)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-1v1-uuid';
    const mockTournament = {
      id: tournamentId,
      name: 'Autumn Cup 2026',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      ruleConfiguration: {
        id: 'rule-1',
        name: 'Standard 5-Point',
        goalLimit: 5,
        gameLimit: 1,
        winByTwo: false,
      },
      minParticipants: 4,
      maxParticipants: 16,
      registrationDeadline: '2026-09-10T12:00:00Z',
      hasPlayoff: false,
      status: 'REGISTRATION_OPEN',
      creatorId: 'user-1',
      creatorNickname: 'Organizer',
      createdAt: new Date().toISOString(),
    };

    let isRegistered = false;

    await page.route('**/api/v1/tournaments', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      });
    });

    await page.route('**/api/v1/tournaments/invitations/pending', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations/my`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          registered: isRegistered,
          isRegistered: isRegistered,
          registration: isRegistered
            ? {
                id: 'reg-solo-1',
                tournamentId,
                tournamentName: 'Autumn Cup 2026',
                playerId: 'user-me',
                playerNickname: 'Me',
                status: 'CONFIRMED',
                createdAt: new Date().toISOString(),
              }
            : null,
          isPendingInvite: false,
        }),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations`, async (route) => {
      if (route.request().method() === 'POST') {
        isRegistered = true;
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'reg-solo-1',
            tournamentId,
            tournamentName: 'Autumn Cup 2026',
            playerId: 'user-me',
            playerNickname: 'Me',
            status: 'CONFIRMED',
            createdAt: new Date().toISOString(),
          }),
        });
      }
    });

    await page.goto('/tournaments');

    const tournamentCard = page.locator('[data-testid="tournament-card"]').first();
    await expect(tournamentCard).toBeVisible();

    // Click Register
    await tournamentCard.locator('[data-testid="register-tournament-btn"]').click();

    // Modal opens
    const modal = page.locator('[data-testid="tournament-registration-modal"]');
    await expect(modal).toBeVisible();

    // Confirm registration
    await modal.locator('[data-testid="confirm-registration-btn"]').click();

    // Toast notification and status update
    await expect(page.locator('[data-testid="tournament-toast"]')).toBeVisible();
    await expect(tournamentCard.locator('[data-testid="tournament-status-badge"]')).toBeVisible();
  });

  test('[P0] should invite partner in 2v2 fixed tournament and update state when partner accepts (AC 2, AC 3, AC 8)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-2v2-uuid';
    const mockTournament = {
      id: tournamentId,
      name: 'Winter Duo Clash',
      format: 'CUP',
      mode: 'TWO_VS_TWO_FIXED_TEAMS',
      ruleConfiguration: {
        id: 'rule-1',
        name: 'Standard 5-Point',
        goalLimit: 5,
        gameLimit: 1,
        winByTwo: false,
      },
      minParticipants: 4,
      maxParticipants: 16,
      registrationDeadline: '2026-09-10T12:00:00Z',
      hasPlayoff: false,
      status: 'REGISTRATION_OPEN',
      creatorId: 'user-1',
      creatorNickname: 'Organizer',
      createdAt: new Date().toISOString(),
    };

    let inviteAccepted = false;

    await page.route('**/api/v1/tournaments', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      });
    });

    await page.route('**/api/v1/tournaments/invitations/pending', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(
          inviteAccepted
            ? []
            : [
                {
                  id: 'reg-invite-1',
                  tournamentId,
                  tournamentName: 'Winter Duo Clash',
                  playerId: 'user-partner',
                  playerNickname: 'CaptainInviter',
                  status: 'PENDING_CONFIRMATION',
                  createdAt: new Date().toISOString(),
                },
              ]
        ),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations/my`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          registered: inviteAccepted,
          isRegistered: inviteAccepted,
          registration: inviteAccepted
            ? {
                id: 'reg-invite-1',
                tournamentId,
                tournamentName: 'Winter Duo Clash',
                playerId: 'user-partner',
                playerNickname: 'CaptainInviter',
                partnerId: 'user-me',
                partnerNickname: 'Me',
                status: 'CONFIRMED',
                createdAt: new Date().toISOString(),
              }
            : null,
          isPendingInvite: false,
        }),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations/reg-invite-1/accept`, async (route) => {
      inviteAccepted = true;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'reg-invite-1',
          tournamentId,
          tournamentName: 'Winter Duo Clash',
          playerId: 'user-partner',
          playerNickname: 'CaptainInviter',
          partnerId: 'user-me',
          partnerNickname: 'Me',
          status: 'CONFIRMED',
          createdAt: new Date().toISOString(),
        }),
      });
    });

    await page.goto('/tournaments');

    // See pending invite banner
    const inviteBanner = page.locator('[data-testid="pending-invitations-banner"]');
    await expect(inviteBanner).toBeVisible();

    // Click Respond
    await inviteBanner.locator('[data-testid="respond-invite-btn"]').click();

    // Invite modal opens
    const inviteModal = page.locator('[data-testid="tournament-invite-modal"]');
    await expect(inviteModal).toBeVisible();

    // Accept invitation
    await inviteModal.locator('[data-testid="accept-invite-btn"]').click();
    await expect(inviteModal).toBeHidden();

    // Toast notification
    await expect(page.locator('[data-testid="tournament-toast"]')).toBeVisible();
  });

  test('[P1] should handle partner declining 2v2 tournament invitation (AC 4, AC 8)', async ({ page }) => {
    await loginUser(page);

    const tournamentId = 'tourn-2v2-uuid';
    const mockTournament = {
      id: tournamentId,
      name: 'Winter Duo Clash',
      format: 'CUP',
      mode: 'TWO_VS_TWO_FIXED_TEAMS',
      ruleConfiguration: {
        id: 'rule-1',
        name: 'Standard 5-Point',
        goalLimit: 5,
        gameLimit: 1,
        winByTwo: false,
      },
      minParticipants: 4,
      maxParticipants: 16,
      registrationDeadline: '2026-09-10T12:00:00Z',
      hasPlayoff: false,
      status: 'REGISTRATION_OPEN',
      creatorId: 'user-1',
      creatorNickname: 'Organizer',
      createdAt: new Date().toISOString(),
    };

    await page.route('**/api/v1/tournaments', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([mockTournament]),
      });
    });

    await page.route('**/api/v1/tournaments/invitations/pending', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'reg-invite-1',
            tournamentId,
            tournamentName: 'Winter Duo Clash',
            playerId: 'user-partner',
            playerNickname: 'CaptainInviter',
            status: 'PENDING_CONFIRMATION',
            createdAt: new Date().toISOString(),
          },
        ]),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations/my`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          registered: false,
          isRegistered: false,
          registration: null,
          isPendingInvite: false,
        }),
      });
    });

    await page.route(`**/api/v1/tournaments/${tournamentId}/registrations/reg-invite-1/decline`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'reg-invite-1',
          tournamentId,
          tournamentName: 'Winter Duo Clash',
          playerId: 'user-partner',
          playerNickname: 'CaptainInviter',
          partnerId: 'user-me',
          partnerNickname: 'Me',
          status: 'DECLINED',
          createdAt: new Date().toISOString(),
        }),
      });
    });

    await page.goto('/tournaments');

    const inviteBanner = page.locator('[data-testid="pending-invitations-banner"]');
    await expect(inviteBanner).toBeVisible();

    await inviteBanner.locator('[data-testid="respond-invite-btn"]').click();

    const inviteModal = page.locator('[data-testid="tournament-invite-modal"]');
    await expect(inviteModal).toBeVisible();

    await inviteModal.locator('[data-testid="decline-invite-btn"]').click();
    await expect(inviteModal).toBeHidden();

    await expect(page.locator('[data-testid="tournament-toast"]')).toBeVisible();
  });
});
