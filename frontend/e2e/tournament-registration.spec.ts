import { test, expect, type Page } from '@playwright/test';

async function loginUser(page: Page, customNickname?: string) {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-tourn-user-${randomSuffix}@example.com`;
    const nickname = customNickname || `TournUser${randomSuffix}`;
    await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
    await page.waitForURL('**/*');
    return { email, nickname };
}

test.describe('Tournament Registration & Confirmation E2E (Story 8.2)', () => {

    test.skip('[P0] should complete solo registration for 1v1 tournament and display Confirmed status (AC 1, AC 7, AC 8)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/tournaments');

        const tournamentCard = page.locator('[data-testid="tournament-card"]').first();
        await expect(tournamentCard).toBeVisible();

        // Click Register CTA on open tournament card
        await tournamentCard.getByRole('button', { name: /Register/i }).click();

        // Registration modal opens
        const modal = page.locator('[data-testid="tournament-registration-modal"]');
        await expect(modal).toBeVisible();

        // Submit solo registration
        await modal.getByRole('button', { name: /Confirm Registration|Register/i }).click();

        // Modal should close and success toast appears
        await expect(modal).toBeHidden();
        await expect(page.getByText(/Successfully registered for tournament/i)).toBeVisible();

        // Card should reflect registered status
        await expect(tournamentCard.getByText(/Registered/i)).toBeVisible();
    });

    test.skip('[P0] should invite partner in 2v2 fixed tournament and update state when partner accepts (AC 2, AC 3, AC 8)', async ({ browser }) => {
        const contextA = await browser.newContext();
        const contextB = await browser.newContext();

        const pageA = await contextA.newPage();
        const pageB = await contextB.newPage();

        const userA = await loginUser(pageA, 'PlayerAlpha');
        const userB = await loginUser(pageB, 'PlayerBeta');

        // User A opens 2v2 fixed tournament registration modal
        await pageA.goto('/tournaments');
        const tournament2v2Card = pageA.locator('[data-testid="tournament-card"]')
            .filter({ hasText: /2v2 Fixed/i })
            .first();
        await tournament2v2Card.getByRole('button', { name: /Register/i }).click();

        const modalA = pageA.locator('[data-testid="tournament-registration-modal"]');
        await expect(modalA).toBeVisible();

        // Search and select partner User B
        await modalA.getByLabel(/Search Partner|Partner/i).fill(userB.nickname);
        await modalA.getByTestId('partner-search-result').filter({ hasText: userB.nickname }).click();

        // Send invite
        await modalA.getByRole('button', { name: /Send Invitation|Register/i }).click();
        await expect(modalA).toBeHidden();
        await expect(tournament2v2Card.getByText(/Invite Pending/i)).toBeVisible();

        // User B visits /tournaments, sees pending invite banner / modal
        await pageB.goto('/tournaments');
        const inviteBanner = pageB.locator('[data-testid="pending-invites-banner"]');
        await expect(inviteBanner).toBeVisible();

        await inviteBanner.getByRole('button', { name: /View Invite|Accept/i }).click();
        const inviteModalB = pageB.locator('[data-testid="tournament-invite-modal"]');
        await expect(inviteModalB).toBeVisible();

        // Accept invitation
        await inviteModalB.getByRole('button', { name: /Accept/i }).click();
        await expect(inviteModalB).toBeHidden();

        // Both players now see confirmed registration
        await expect(pageB.getByText(/Registration Confirmed/i)).toBeVisible();

        await pageA.reload();
        await expect(tournament2v2Card.getByText(/Registered/i)).toBeVisible();

        await contextA.close();
        await contextB.close();
    });

    test.skip('[P1] should handle partner declining 2v2 tournament invitation and free the slot (AC 4, AC 8)', async ({ browser }) => {
        const contextA = await browser.newContext();
        const contextB = await browser.newContext();

        const pageA = await contextA.newPage();
        const pageB = await contextB.newPage();

        await loginUser(pageA, 'InviterOne');
        const userB = await loginUser(pageB, 'InvitedTwo');

        // User A invites User B
        await pageA.goto('/tournaments');
        const tournamentCard = pageA.locator('[data-testid="tournament-card"]').filter({ hasText: /2v2/i }).first();
        await tournamentCard.getByRole('button', { name: /Register/i }).click();

        const modal = pageA.locator('[data-testid="tournament-registration-modal"]');
        await modal.getByLabel(/Search Partner|Partner/i).fill(userB.nickname);
        await modal.getByTestId('partner-search-result').filter({ hasText: userB.nickname }).click();
        await modal.getByRole('button', { name: /Send Invitation|Register/i }).click();

        // User B declines
        await pageB.goto('/tournaments');
        const inviteBanner = pageB.locator('[data-testid="pending-invites-banner"]');
        await inviteBanner.getByRole('button', { name: /View Invite/i }).click();

        const inviteModal = pageB.locator('[data-testid="tournament-invite-modal"]');
        await inviteModal.getByRole('button', { name: /Decline/i }).click();
        await expect(inviteModal).toBeHidden();

        // Inviter sees slot freed and can register again
        await pageA.reload();
        await expect(tournamentCard.getByRole('button', { name: /Register/i })).toBeVisible();

        await contextA.close();
        await contextB.close();
    });

    test.skip('[P1] should withdraw and cancel active registration before deadline (AC 5)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/tournaments');

        const tournamentCard = page.locator('[data-testid="tournament-card"]').first();
        await tournamentCard.getByRole('button', { name: /Register/i }).click();

        const modal = page.locator('[data-testid="tournament-registration-modal"]');
        await modal.getByRole('button', { name: /Confirm Registration/i }).click();
        await expect(modal).toBeHidden();

        // Withdraw registration
        await tournamentCard.getByRole('button', { name: /Cancel Registration|Withdraw/i }).click();

        // Confirm cancellation in prompt
        const confirmBtn = page.getByRole('button', { name: /Confirm Cancel|Yes, Withdraw/i });
        if (await confirmBtn.isVisible()) {
            await confirmBtn.click();
        }

        await expect(page.getByText(/Registration cancelled/i)).toBeVisible();
        await expect(tournamentCard.getByRole('button', { name: /Register/i })).toBeVisible();
    });

    test.skip('[P1] should enforce partner selection requirement for 2v2 fixed teams (AC 6)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/tournaments');

        const tournament2v2Card = page.locator('[data-testid="tournament-card"]')
            .filter({ hasText: /2v2 Fixed/i })
            .first();
        await tournament2v2Card.getByRole('button', { name: /Register/i }).click();

        const modal = page.locator('[data-testid="tournament-registration-modal"]');
        await expect(modal).toBeVisible();

        // Attempt submit without partner selected
        await modal.getByRole('button', { name: /Send Invitation|Register/i }).click();

        await expect(page.getByText(/Please select a partner/i)).toBeVisible();
    });

    test.skip('[P2] should adhere to Clubhouse No-Line styling tokens in modals (UX-DR3)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/tournaments');

        const tournamentCard = page.locator('[data-testid="tournament-card"]').first();
        await tournamentCard.getByRole('button', { name: /Register/i }).click();

        const modal = page.locator('[data-testid="tournament-registration-modal"]');
        await expect(modal).toBeVisible();

        // Verify 0px solid border (Clubhouse tonal elevation rule)
        const borderBottomWidth = await modal.evaluate((el) => window.getComputedStyle(el).borderBottomWidth);
        expect(borderBottomWidth).toBe('0px');
    });
});
