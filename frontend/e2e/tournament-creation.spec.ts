import { test, expect, type Page } from '@playwright/test';

async function loginUser(page: Page) {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-tourn-user-${randomSuffix}@example.com`;
    const nickname = `TournUser${randomSuffix}`;
    await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
    await page.waitForURL('**/*');
    return nickname;
}

test.describe('Tournament Creation & Configuration E2E (Story 8.1)', () => {

    test.skip('[P0] should open CreateTournamentModal from /tournaments, fill form, and create tournament successfully (AC 1, AC 2, AC 6)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/tournaments');

        // Click "Create Tournament" action button
        await page.getByRole('button', { name: /Create Tournament|\+ Tournament/i }).click();

        // Modal should open
        const modal = page.locator('[data-testid="create-tournament-modal"]');
        await expect(modal).toBeVisible();

        // Fill form fields
        const uniqueName = `Autumn Cup ${crypto.randomUUID().substring(0, 6)}`;
        await page.getByLabel(/Tournament Name|Name/i).fill(uniqueName);

        // Select Format (CUP) and Mode (1v1)
        await page.getByRole('button', { name: /Single Elimination|Cup/i }).click();
        await page.getByRole('button', { name: /1v1 Personal|1v1/i }).click();

        // Select Rule System preset if selector exists
        const ruleSelect = page.locator('[data-testid="rule-config-select"]');
        if (await ruleSelect.isVisible()) {
            await ruleSelect.selectOption({ index: 0 });
        }

        // Set future registration deadline
        const futureDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
        const pad = (n: number) => String(n).padStart(2, '0');
        const futureDateStr = `${futureDate.getFullYear()}-${pad(futureDate.getMonth() + 1)}-${pad(futureDate.getDate())}T${pad(futureDate.getHours())}:${pad(futureDate.getMinutes())}`;
        await page.getByLabel(/Registration Deadline/i).fill(futureDateStr);

        // Set participants
        await page.getByLabel(/Min Participants/i).fill('4');
        await page.getByLabel(/Max Participants/i).fill('16');

        // Submit form
        await page.getByRole('button', { name: /Create Tournament|Submit/i }).click();

        // Modal should close and success toast appear
        await expect(modal).toBeHidden();
        await expect(page.getByText(/Tournament created successfully/i)).toBeVisible();

        // New tournament should be listed in /tournaments
        await expect(page.getByText(uniqueName)).toBeVisible();
    });

    test.skip('[P0] should navigate to Tournaments from Home Hub action button (AC 1, UX-DR6)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/');

        // Click Tournaments CTA in Home Hub
        await page.getByRole('button', { name: /Tournaments/i }).click();

        // URL should change to /tournaments
        await page.waitForURL('**/tournaments');
        await expect(page.getByRole('heading', { name: /Tournaments/i })).toBeVisible();
    });

    test.skip('[P1] should validate min participants for 2v2 modes and future registration deadline (AC 4)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/tournaments');

        await page.getByRole('button', { name: /Create Tournament|\+ Tournament/i }).click();

        // Fill invalid short name
        await page.getByLabel(/Tournament Name|Name/i).fill('AB');

        // Select 2v2 mode and min 2 participants (invalid for 2v2)
        await page.getByRole('button', { name: /2v2 Fixed Teams|2v2/i }).click();
        await page.getByLabel(/Min Participants/i).fill('2');

        // Fill past registration deadline
        await page.getByLabel(/Registration Deadline/i).fill('2020-01-01T12:00');

        await page.getByRole('button', { name: /Create Tournament|Submit/i }).click();

        // Validation errors should be displayed
        await expect(page.getByText(/Name must be at least 3 characters/i)).toBeVisible();
        await expect(page.getByText(/2v2 modes require minimum 4 participants/i)).toBeVisible();
        await expect(page.getByText(/Registration deadline must be in the future/i)).toBeVisible();
    });

    test.skip('[P1] should filter tournaments by status tabs (AC 5)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/tournaments');

        // Verify status filter tabs exist
        await expect(page.getByRole('tab', { name: /All/i })).toBeVisible();
        await expect(page.getByRole('tab', { name: /Registration Open|Open/i })).toBeVisible();
        await expect(page.getByRole('tab', { name: /In Progress/i })).toBeVisible();
        await expect(page.getByRole('tab', { name: /Completed/i })).toBeVisible();

        // Click "Registration Open" tab
        await page.getByRole('tab', { name: /Registration Open|Open/i }).click();
        await expect(page.getByTestId('tournament-card').first()).toBeVisible();
    });

    test.skip('[P2] should adhere to Clubhouse No-Line styling tokens in CreateTournamentModal (UX-DR3)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/tournaments');
        await page.getByRole('button', { name: /Create Tournament|\+ Tournament/i }).click();

        const modal = page.locator('[data-testid="create-tournament-modal"]');
        await expect(modal).toBeVisible();

        // Verify 0px solid border (Clubhouse tonal elevation rule)
        const borderBottomWidth = await modal.evaluate((el) => window.getComputedStyle(el).borderBottomWidth);
        expect(borderBottomWidth).toBe('0px');
    });
});
