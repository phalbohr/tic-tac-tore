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

    test('[P0] should open RuleTemplateModal in /matches/new and create custom template without resetting match draft (AC 2, AC 6)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/matches/new');

        // Verify preset option is visible in rule selector from real backend
        await expect(page.getByRole('button', { name: /ITSF Standard Matchplay/i })).toBeVisible();

        // Click create custom rule template inline
        await page.getByRole('button', { name: /Create Custom Template|\+ Custom Rule/i }).click();

        // Verify modal opens with foosball parameters and smart defaults
        await expect(page.getByRole('dialog', { name: /Rule Template Builder|Create Rule Template/i })).toBeVisible();
        await expect(page.getByLabel(/Goal Limit/i)).toHaveValue('5');
        await expect(page.getByLabel(/Game Limit/i)).toHaveValue('3');

        // Fill custom template parameters with unique name
        const uniqueName = `Office Fast ${crypto.randomUUID().substring(0, 6)}`;
        await page.getByLabel(/Template Name/i).fill(uniqueName);
        await page.getByLabel(/Goal Limit/i).fill('7');
        await page.getByLabel(/Game Limit/i).fill('1');

        // Save custom template to real backend
        await page.getByRole('button', { name: 'Save Template' }).click();

        // Modal should close and new rule should be selected without resetting player draft
        await expect(page.getByRole('dialog')).not.toBeVisible();
        await expect(page.getByText(uniqueName)).toBeVisible();
    });

    test('[P0] should manage rule templates in Profile Settings (/cabinet) (AC 1, AC 5)', async ({ page }) => {
        await loginUser(page);
        await page.goto('/cabinet');

        // Verify Rule Templates section exists with preset
        await expect(page.getByRole('heading', { name: /Rule Templates/i })).toBeVisible();
        await expect(page.getByText('ITSF Standard Matchplay')).toBeVisible();

        // Create new template from cabinet
        const uniqueName = `Friday Special ${crypto.randomUUID().substring(0, 6)}`;
        await page.getByRole('button', { name: /New Template|\+ Add Rule|Create Template/i }).click();
        await page.getByLabel(/Template Name/i).fill(uniqueName);
        await page.getByLabel(/Goal Limit/i).fill('10');
        await page.getByRole('button', { name: 'Save Template' }).click();

        // Verify new template appears in user list
        await expect(page.getByText(uniqueName)).toBeVisible();

        // Delete template
        await page.getByRole('button', { name: new RegExp(`Delete ${uniqueName}|delete-rule`, 'i') }).click();
        await page.locator('[data-testid="delete-rule-confirm-modal"]').getByRole('button', { name: /Delete|Confirm/i }).click();
        await expect(page.getByText(uniqueName)).not.toBeVisible();
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
        const modifiedName = `Modified ITSF ${crypto.randomUUID().substring(0, 6)}`;
        await page.getByLabel(/Template Name/i).fill(modifiedName);
        await page.getByLabel(/Goal Limit/i).fill('6');
        await page.getByRole('button', { name: 'Save Template' }).click();

        // Verify original preset remains and new custom template is added
        await expect(page.getByText('ITSF Standard Matchplay')).toBeVisible();
        await expect(page.getByText(modifiedName)).toBeVisible();
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
