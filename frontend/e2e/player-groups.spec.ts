import { test, expect, type Page } from '@playwright/test';

/**
 * ATDD Red-Phase Scaffolds for Named Player Groups ("Teams").
 * Story 6.1: Named Player Groups ("Teams")
 *
 * AC 1: Create and manage player groups in Profile Settings (/cabinet) with unique name & Favorites flag
 * AC 2: User-isolated group queries with safe member summaries (no PII, AD-04)
 * AC 3: Inline player group selection and inline group creation in portrait match creation (/matches/new) preserving draft state
 * AC 4: Filter Unified Match History (/matches) by player group chips
 * AC 5: Security isolation (ownership enforcement)
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-groups-user-${randomSuffix}@example.com`;
  const nickname = `GroupUser${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return nickname;
}

test.describe('[Story 6.1] Named Player Groups ("Teams") E2E User Journey (ATDD)', () => {

  test.skip('[P0] should create custom player group in Profile Settings (/cabinet) and list it', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/player-groups', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([
            {
              id: 'group-1',
              name: 'Office Rivals',
              isFavorite: false,
              creatorId: 'me',
              members: [
                { id: 'user-bob', nickname: 'Bob', avatar: 'avatar-bob' },
                { id: 'user-alice', nickname: 'Alice', avatar: 'avatar-alice' },
              ],
              createdAt: new Date().toISOString(),
            }
          ]),
        });
      } else if (route.request().method() === 'POST') {
        const body = JSON.parse(route.request().postData() || '{}');
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'group-2',
            name: body.name,
            isFavorite: body.isFavorite || false,
            creatorId: 'me',
            members: [],
            createdAt: new Date().toISOString(),
          }),
        });
      }
    });

    await page.goto('/cabinet');

    // Verify Player Groups section in Settings
    await expect(page.getByRole('heading', { name: /Player Groups|Teams|Spielergruppen/i })).toBeVisible();
    await expect(page.getByText('Office Rivals')).toBeVisible();

    // Click "New Group" / "Create Group"
    await page.getByRole('button', { name: /Create Group|New Group|Gruppe erstellen/i }).click();

    // Fill Modal
    await page.getByLabel(/Group Name|Name der Gruppe/i).fill('Friday Champions');
    await page.getByRole('button', { name: /Save|Speichern/i }).click();

    // Expect group created
    await expect(page.getByText('Friday Champions')).toBeVisible();
  });

  test.skip('[P0] should display player group chips during match creation (/matches/new) and filter player selection', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/player-groups', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'group-fav',
            name: 'Favorites',
            isFavorite: true,
            creatorId: 'me',
            members: [
              { id: 'user-bob', nickname: 'Bob', avatar: 'avatar-bob' }
            ],
            createdAt: new Date().toISOString(),
          },
          {
            id: 'group-custom',
            name: 'Regulars',
            isFavorite: false,
            creatorId: 'me',
            members: [
              { id: 'user-charlie', nickname: 'Charlie', avatar: 'avatar-charlie' }
            ],
            createdAt: new Date().toISOString(),
          }
        ]),
      });
    });

    await page.goto('/matches/new');

    // Verify Group quick filter chips
    await expect(page.getByRole('button', { name: /Favorites|Favoriten/i })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Regulars' })).toBeVisible();

    // Click "Favorites" chip
    await page.getByRole('button', { name: /Favorites|Favoriten/i }).click();
    await expect(page.getByText('Bob')).toBeVisible();
  });

  test.skip('[P1] should create new player group inline via modal during match creation (/matches/new) without resetting match draft state', async ({ page }) => {
    await loginUser(page);

    await page.route('**/api/v1/player-groups', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    await page.goto('/matches/new');

    // Set match type to 2v2
    const matchType2v2 = page.getByRole('button', { name: '2v2' });
    if (await matchType2v2.isVisible()) {
      await matchType2v2.click();
    }

    // Open inline group creation modal
    const createGroupBtn = page.getByRole('button', { name: /Create Group|New Team|\+ Group/i });
    await expect(createGroupBtn).toBeVisible();
    await createGroupBtn.click();

    // Close or cancel modal
    await page.getByRole('button', { name: /Cancel|Abbrechen/i }).click();

    // Verify match draft state is preserved (still 2v2 mode selected)
    await expect(matchType2v2).toHaveClass(/active|selected|ch-surface/);
  });

  test.skip('[P1] should filter Unified Match History by player group chips (/matches)', async ({ page }) => {
    await loginUser(page);

    let requestedQuery = '';
    await page.route('**/api/v1/matches/history*', async (route) => {
      requestedQuery = route.request().url();
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [],
          page: 0,
          size: 10,
          totalElements: 0,
          totalPages: 0,
        }),
      });
    });

    await page.route('**/api/v1/player-groups', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'grp-weekend',
            name: 'Weekend Crew',
            isFavorite: false,
            creatorId: 'me',
            members: [],
            createdAt: new Date().toISOString(),
          }
        ]),
      });
    });

    await page.goto('/matches');

    // Verify and click Group Filter Chip
    const groupChip = page.getByRole('button', { name: 'Weekend Crew' });
    await expect(groupChip).toBeVisible();
    await groupChip.click();

    await expect.poll(() => requestedQuery).toContain('groupId=grp-weekend');
  });

  test.skip('[P2] should adhere to Clubhouse No-Line rule for group list items (UX-DR3)', async ({ page }) => {
    await loginUser(page);
    await page.goto('/cabinet');

    const groupList = page.locator('.player-group-list, [data-testid="player-group-list"]').first();
    if (await groupList.isVisible()) {
      const borderTopWidth = await groupList.evaluate(el => window.getComputedStyle(el).borderTopWidth);
      expect(borderTopWidth).toBe('0px');
    }
  });
});
