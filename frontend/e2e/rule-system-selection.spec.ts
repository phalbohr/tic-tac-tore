import { test, expect } from '@playwright/test';

test.describe('Rule System Selection & Inline Creation E2E (Story 6.1b)', () => {
    test.beforeEach(async ({ page }) => {
        await page.route('/api/auth/me', async (route) => {
            await route.fulfill({
                status: 200,
                json: {
                    id: '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a',
                    name: 'Test Player',
                    email: 'player@example.com'
                }
            });
        });

        await page.route('/api/v1/rule-configurations*', async (route) => {
            if (route.request().method() === 'GET') {
                await route.fulfill({
                    status: 200,
                    json: [
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
                    ]
                });
            } else if (route.request().method() === 'POST') {
                const body = route.request().postDataJSON();
                await route.fulfill({
                    status: 201,
                    json: {
                        id: '11111111-1111-1111-1111-111111111111',
                        ...body,
                        type: 'CUSTOM',
                        createdBy: '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a',
                        createdAt: new Date().toISOString()
                    }
                });
            } else if (route.request().method() === 'DELETE') {
                await route.fulfill({ status: 204 });
            }
        });
    });

    test.skip('[P0] should open RuleTemplateModal in /matches/new and create custom template without resetting match draft (AC 2, AC 6)', async ({ page }) => {
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
        await page.getByRole('button', { name: /Save Template|Create/i }).click();

        // Modal should close and new rule should be selected without resetting player draft
        await expect(page.getByRole('dialog')).not.toBeVisible();
        await expect(page.getByText('Office Fast 7')).toBeVisible();
    });

    test.skip('[P0] should manage rule templates in Profile Settings (/cabinet) (AC 1, AC 5)', async ({ page }) => {
        await page.goto('/cabinet');

        // Verify Rule Templates section exists
        await expect(page.getByRole('heading', { name: /Rule Templates/i })).toBeVisible();
        await expect(page.getByText('ITSF Standard Matchplay')).toBeVisible();

        // Create new template from cabinet
        await page.getByRole('button', { name: /New Template|\+ Add Rule/i }).click();
        await page.getByLabel(/Template Name/i).fill('Friday Special');
        await page.getByLabel(/Goal Limit/i).fill('10');
        await page.getByRole('button', { name: /Save Template|Create/i }).click();

        // Verify new template appears in user list
        await expect(page.getByText('Friday Special')).toBeVisible();

        // Delete template
        await page.getByRole('button', { name: /Delete Friday Special|delete-rule/i }).click();
        await page.getByRole('button', { name: /Confirm|Delete/i }).click();
        await expect(page.getByText('Friday Special')).not.toBeVisible();
    });

    test.skip('[P1] should support "Edit as New" for existing templates in /cabinet (AC 4)', async ({ page }) => {
        await page.goto('/cabinet');

        // Click "Edit as New" on ITSF Preset
        await page.getByRole('button', { name: /Edit as New|Clone/i }).click();

        // Builder opens pre-filled with preset values
        await expect(page.getByLabel(/Goal Limit/i)).toHaveValue('5');
        await expect(page.getByLabel(/Game Limit/i)).toHaveValue('3');

        // Modify and save as new custom template
        await page.getByLabel(/Template Name/i).fill('Modified ITSF');
        await page.getByLabel(/Goal Limit/i).fill('6');
        await page.getByRole('button', { name: /Save Template|Create/i }).click();

        // Verify original preset remains and new custom template is added
        await expect(page.getByText('ITSF Standard Matchplay')).toBeVisible();
        await expect(page.getByText('Modified ITSF')).toBeVisible();
    });

    test.skip('[P2] should adhere to Clubhouse No-Line styling in RuleTemplateModal (UX-DR3)', async ({ page }) => {
        await page.goto('/matches/new');
        await page.getByRole('button', { name: /Create Custom Template|\+ Custom Rule/i }).click();

        const modal = page.locator('[data-testid="rule-template-modal"]');
        await expect(modal).toBeVisible();

        // Check border style has 0px border (tonal elevation instead of border line)
        const borderBottomWidth = await modal.evaluate((el) => window.getComputedStyle(el).borderBottomWidth);
        expect(borderBottomWidth).toBe('0px');
    });
});
