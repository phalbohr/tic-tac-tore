import { test, expect, type Page } from '@playwright/test';

async function loginUser(page: Page) {
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-rules-user-${randomSuffix}@example.com`;
    const nickname = `RuleUser${randomSuffix}`;
    await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
    await page.waitForURL('**/*');
    return nickname;
}

test.describe('Rule System Selection & Inline Creation E2E (Story 6.1b)', () => {
    let rulesList: any[] = [];

    test.beforeEach(async ({ page }) => {
        rulesList = [
            {
                id: '00000000-0000-0000-0000-000000000001',
                name: 'ITSF Standard Matchplay',
                type: 'PRESET',
                createdBy: '00000000-0000-0000-0000-000000000000',
                goalLimit: 5,
                gameLimit: 3,
                winByTwo: true,
                absoluteScoreCap: 8,
                timeoutsPerGame: 2,
                timeoutDurationSeconds: 30,
                possessionLimit5BarSeconds: 10,
                possessionLimitOtherSeconds: 15,
                sideSwapRule: 'BETWEEN_GAMES',
                restartRule: 'CONCEDING_TEAM',
                spinningAllowed: false,
                aerialsAllowed: false,
                positionSwapRule: 'BETWEEN_GAMES',
                pointDistribution: 'WIN_LOSS_3_0',
            }
        ];

        await page.route('**/api/v1/rule-configurations/**', async (route) => {
            if (route.request().method() === 'GET') {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    body: JSON.stringify(rulesList)
                });
            } else if (route.request().method() === 'POST') {
                const body = route.request().postDataJSON();
                const created = {
                    id: crypto.randomUUID(),
                    ...body,
                    type: 'CUSTOM',
                    createdBy: 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
                    createdAt: new Date().toISOString()
                };
                rulesList.push(created);
                await route.fulfill({
                    status: 201,
                    contentType: 'application/json',
                    body: JSON.stringify(created)
                });
            } else if (route.request().method() === 'DELETE') {
                const url = route.request().url();
                const id = url.split('/').pop()?.split('?')[0];
                rulesList = rulesList.filter((r) => r.id !== id);
                await route.fulfill({ status: 204 });
            }
        });

        await page.route('**/api/v1/rule-configurations', async (route) => {
            if (route.request().method() === 'GET') {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    body: JSON.stringify(rulesList)
                });
            } else if (route.request().method() === 'POST') {
                const body = route.request().postDataJSON();
                const created = {
                    id: crypto.randomUUID(),
                    ...body,
                    type: 'CUSTOM',
                    createdBy: 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
                    createdAt: new Date().toISOString()
                };
                rulesList.push(created);
                await route.fulfill({
                    status: 201,
                    contentType: 'application/json',
                    body: JSON.stringify(created)
                });
            }
        });
    });

    test('[P0] should open RuleTemplateModal in /matches/new and create custom template without resetting match draft (AC 2, AC 6)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/matches/new');

        // Verify preset option is visible in rule selector
        await expect(page.getByRole('button', { name: /ITSF Standard Matchplay/i })).toBeVisible();

        // Click create custom rule template inline
        await page.getByRole('button', { name: /Create Custom Template|\+ Custom Rule/i }).click();

        // Verify modal opens with foosball parameters and smart defaults
        await expect(page.getByRole('dialog', { name: /Rule Template Builder|Create Rule Template/i })).toBeVisible();
        await expect(page.getByLabel(/Goal Limit/i)).toHaveValue('5');
        await expect(page.getByLabel(/Game Limit/i)).toHaveValue('3');

        // Fill custom template parameters
        await page.getByLabel(/Template Name/i).fill('Office Fast 7');
        await page.getByLabel(/Goal Limit/i).fill('7');
        await page.getByLabel(/Game Limit/i).fill('1');

        // Save custom template
        await page.getByRole('button', { name: 'Save Template' }).click();

        // Modal should close and new rule should be selected without resetting player draft
        await expect(page.getByRole('dialog')).not.toBeVisible();
        await expect(page.getByText('Office Fast 7')).toBeVisible();
    });

    test('[P0] should manage rule templates in Profile Settings (/cabinet) (AC 1, AC 5)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/cabinet');

        // Verify Rule Templates section exists
        await expect(page.getByRole('heading', { name: /Rule Templates/i })).toBeVisible();
        await expect(page.getByText('ITSF Standard Matchplay')).toBeVisible();

        // Create new template from cabinet
        await page.getByRole('button', { name: /New Template|\+ Add Rule|Create Template/i }).click();
        await page.getByLabel(/Template Name/i).fill('Friday Special');
        await page.getByLabel(/Goal Limit/i).fill('10');
        await page.getByRole('button', { name: 'Save Template' }).click();

        // Verify new template appears in user list
        await expect(page.getByText('Friday Special')).toBeVisible();

        // Delete template
        await page.getByRole('button', { name: /Delete Friday Special|delete-rule/i }).click();
        await page.locator('[data-testid="delete-rule-confirm-modal"]').getByRole('button', { name: /Delete|Confirm/i }).click();
        await expect(page.getByText('Friday Special')).not.toBeVisible();
    });

    test('[P1] should support "Edit as New" for existing templates in /cabinet (AC 4)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/cabinet');

        // Click "Edit as New" on ITSF Preset
        await page.getByRole('button', { name: /Edit as New/i }).first().click();

        // Builder opens pre-filled with preset values
        await expect(page.getByLabel(/Goal Limit/i)).toHaveValue('5');
        await expect(page.getByLabel(/Game Limit/i)).toHaveValue('3');

        // Modify and save as new custom template
        await page.getByLabel(/Template Name/i).fill('Modified ITSF');
        await page.getByLabel(/Goal Limit/i).fill('6');
        await page.getByRole('button', { name: 'Save Template' }).click();

        // Verify original preset remains and new custom template is added
        await expect(page.getByText('ITSF Standard Matchplay')).toBeVisible();
        await expect(page.getByText('Modified ITSF')).toBeVisible();
    });

    test('[P2] should adhere to Clubhouse No-Line styling in RuleTemplateModal (UX-DR3)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/matches/new');
        await page.getByRole('button', { name: /Create Custom Template|\+ Custom Rule/i }).click();

        const modal = page.locator('[data-testid="rule-template-modal"]');
        await expect(modal).toBeVisible();

        // Check border style has 0px border (tonal elevation instead of border line)
        const borderBottomWidth = await modal.evaluate((el) => window.getComputedStyle(el).borderBottomWidth);
        expect(borderBottomWidth).toBe('0px');
    });
});
