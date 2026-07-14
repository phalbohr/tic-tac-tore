import { test, expect } from '@playwright/test';

test.describe('Rule System Selection & Inline Creation E2E', () => {
    test.beforeEach(async ({ page }) => {
        await page.route('/api/auth/me', async (route) => {
            await route.fulfill({
                status: 200,
                json: {
                    id: '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a',
                    name: 'Test User',
                    email: 'test@example.com'
                }
            });
        });

        await page.route('/api/v1/rule-configurations?type=PRESET', async (route) => {
            await route.fulfill({
                status: 200,
                json: [
                    {
                        id: '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a',
                        name: 'ITSF Standard Matchplay',
                        type: 'PRESET',
                        goalLimit: 5,
                        gameLimit: 5,
                        winByTwo: true
                    }
                ]
            });
        });
    });

    test('should fetch and display presets, and create a custom rule', async ({ page }) => {
        await page.route('/api/v1/rule-configurations', async (route) => {
            if (route.request().method() === 'POST') {
                await route.fulfill({
                    status: 201,
                    json: {
                        id: 'new-uuid',
                        name: 'My New Custom Rule',
                        type: 'CUSTOM',
                        goalLimit: 10,
                        gameLimit: 3,
                        winByTwo: false
                    }
                });
            }
        });

        await page.goto('/matches/new');

        // Check if the preset is displayed
        await expect(page.getByText('ITSF Standard Matchplay (Goal Limit: 5, Game Limit: 5)')).toBeVisible();

        // Create a custom rule
        await page.fill('input[type="text"]', 'My New Custom Rule');
        await page.fill('input[type="number"]:first-of-type', '10');
        await page.fill('input[type="number"]:last-of-type', '3');
        await page.check('input[type="checkbox"]');

        page.once('dialog', dialog => dialog.accept());
        await page.click('button[type="submit"]');

        // Wait for the alert dialog to confirm the action, which means our form submitted.
        // We mocked the POST, so the frontend should receive a 201.
    });
});
