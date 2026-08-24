import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Default Team and Rule Template.
 * Story 6.2: Default Team and Rule Template
 *
 * AC 1: Default Match Preferences section in /cabinet with selectors for Group and Rule Template (+ None option)
 * AC 2: Persist defaultGroupId and defaultRuleConfigurationId via PATCH /api/v1/profile/me with ownership validation
 * AC 3: Auto-populate default rule template and pre-filter default player group in /matches/new with non-destructive overrides (FR40)
 * AC 4: Inline "Set as Default" action in /matches/new on rule and group chips without navigating to settings (FR40)
 * AC 5: DB foreign key cascade (ON DELETE SET NULL) when referenced group or custom rule is deleted
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-defaults-user-${randomSuffix}@example.com`;
  const nickname = `DefaultUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return nickname;
}

test.describe.skip('[Story 6.2] Default Team and Rule Template E2E User Journey (ATDD RED PHASE)', () => {

  test('[P0] should set default group and rule template in /cabinet, then auto-populate in /matches/new (AC 1, AC 3)', async ({ page }) => {
    await loginUser(page);

    // 1. Navigate to Profile Cabinet
    await page.goto('/cabinet');

    // 2. Verify "Default Match Preferences" section exists in Cabinet
    await expect(page.getByRole('heading', { name: /Default Match Preferences|Standard-Einstellungen/i })).toBeVisible();

    // 3. Select default player group and default rule template
    const groupSelect = page.getByLabel(/Default Player Group|Standard-Spielergruppe/i);
    await expect(groupSelect).toBeVisible();
    await groupSelect.selectOption({ label: 'Office Rivals' });

    const ruleSelect = page.getByLabel(/Default Rule Template|Standard-Regelwerk/i);
    await expect(ruleSelect).toBeVisible();
    await ruleSelect.selectOption({ label: 'ITSF Standard Matchplay' });

    // Save preferences if there is an explicit save button or verify auto-save toast
    const saveButton = page.getByRole('button', { name: /Save Preferences|Einstellungen speichern/i });
    if (await saveButton.isVisible()) {
      await saveButton.click();
    }

    // 4. Navigate to Match Creation (/matches/new)
    await page.goto('/matches/new');

    // 5. Verify default rule template is pre-selected
    await expect(page.getByRole('button', { name: /ITSF Standard Matchplay/i })).toHaveAttribute('data-active', 'true');

    // 6. Verify default player group is pre-filtered
    await expect(page.getByRole('button', { name: /Office Rivals/i })).toHaveAttribute('data-active', 'true');
  });

  test('[P0] should allow non-destructive overrides in /matches/new without altering profile defaults (AC 3, FR40)', async ({ page }) => {
    await loginUser(page);

    // 1. Setup profile defaults
    await page.goto('/cabinet');
    const groupSelect = page.getByLabel(/Default Player Group|Standard-Spielergruppe/i);
    if (await groupSelect.isVisible()) {
      await groupSelect.selectOption({ label: 'Office Rivals' });
    }
    const ruleSelect = page.getByLabel(/Default Rule Template|Standard-Regelwerk/i);
    if (await ruleSelect.isVisible()) {
      await ruleSelect.selectOption({ label: 'ITSF Standard Matchplay' });
    }

    // 2. Navigate to Match Creation
    await page.goto('/matches/new');

    // 3. Override rule template to standard preset or another template
    await page.getByRole('button', { name: /DTFB Classic|Standard 1on1/i }).click();

    // 4. Override player group filter to "All Players"
    await page.getByRole('button', { name: /All Players|Alle Spieler/i }).click();

    // 5. Navigate back to Cabinet and verify original defaults are unchanged
    await page.goto('/cabinet');
    await expect(page.getByLabel(/Default Player Group|Standard-Spielergruppe/i)).toHaveValue(/group-office-rivals|Office Rivals/i);
    await expect(page.getByLabel(/Default Rule Template|Standard-Regelwerk/i)).toHaveValue(/itsf-preset|ITSF Standard Matchplay/i);
  });

  test('[P0] should support inline "Set as Default" action in /matches/new (AC 4, FR40)', async ({ page }) => {
    await loginUser(page);

    // 1. Navigate directly to Match Creation
    await page.goto('/matches/new');

    // 2. Switch to a custom rule or group chip
    const customRuleChip = page.getByRole('button', { name: /Office Fast 7/i });
    await customRuleChip.click();

    // 3. Click inline "Set as Default" button/icon on the rule picker
    const setDefaultRuleBtn = page.getByRole('button', { name: /Set as default rule|Als Standard-Regelwerk setzen/i });
    await expect(setDefaultRuleBtn).toBeVisible();
    await setDefaultRuleBtn.click();

    // 4. Click inline "Set as Default" button/icon on active player group chip
    const groupChip = page.getByRole('button', { name: /Weekend Warriors/i });
    await groupChip.click();
    const setDefaultGroupBtn = page.getByRole('button', { name: /Set as default group|Als Standardgruppe setzen/i });
    await expect(setDefaultGroupBtn).toBeVisible();
    await setDefaultGroupBtn.click();

    // 5. Verify in /cabinet that defaults were persisted
    await page.goto('/cabinet');
    await expect(page.getByLabel(/Default Rule Template|Standard-Regelwerk/i)).toContainText(/Office Fast 7/i);
    await expect(page.getByLabel(/Default Player Group|Standard-Spielergruppe/i)).toContainText(/Weekend Warriors/i);
  });

  test('[P1] should handle deletion of default group gracefully via cascade without crashing /matches/new (AC 5)', async ({ page }) => {
    await loginUser(page);

    // 1. Set default player group in Cabinet
    await page.goto('/cabinet');
    await page.getByLabel(/Default Player Group|Standard-Spielergruppe/i).selectOption({ label: 'Temporary Team' });

    // 2. Delete the referenced group in Cabinet
    const deleteGroupBtn = page.locator('[data-group-name="Temporary Team"]').getByRole('button', { name: /Delete|Löschen/i });
    await deleteGroupBtn.click();
    await page.locator('[role="dialog"]').getByRole('button', { name: /Confirm|Bestätigen/i }).click();

    // 3. Verify Default Player Group resets to "None"
    await expect(page.getByLabel(/Default Player Group|Standard-Spielergruppe/i)).toHaveValue('');

    // 4. Navigate to /matches/new and verify page mounts cleanly without errors
    await page.goto('/matches/new');
    await expect(page.getByRole('heading', { name: /New Match|Neues Spiel/i })).toBeVisible();
  });
});
