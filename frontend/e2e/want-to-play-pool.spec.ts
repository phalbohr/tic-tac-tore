import { test, expect, type Page } from '@playwright/test';

/**
 * Story 6.3: Create "Want to Play" Pool E2E Specs (ATDD Red Phase)
 */

async function loginUser(page: Page) {
  const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
  const email = `e2e-pool-user-${randomSuffix}@example.com`;
  const nickname = `PoolHost${randomSuffix}`;
  await page.goto(`/api/auth/test-login?email=${email}&nickname=${nickname}&tutorialCompleted=true`);
  await page.waitForURL('**/*');
  return { nickname, email };
}

test.describe('[Story 6.3] Create "Want to Play" Pool E2E User Journeys', () => {

  test('[P0] should open CreatePoolModal from Home Hub and create fill-based 1v1 pool (AC 1, AC 2, AC 6)', async ({ page }) => {
    let createdPayload: any = null;

    await page.route('**/api/v1/pools', async (route) => {
      if (route.request().method() === 'POST') {
        createdPayload = JSON.parse(route.request().postData() || '{}');
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'pool-uuid-1',
            creatorId: 'user-uuid-1',
            creatorNickname: 'PoolHost',
            matchType: createdPayload.matchType,
            startCondition: createdPayload.startCondition,
            scheduledTime: null,
            skillLevel: createdPayload.skillLevel || 'OPEN_FOR_ALL',
            status: 'OPEN',
            requiredPlayers: 2,
            currentPlayers: 1,
            participants: [
              {
                userId: 'user-uuid-1',
                nickname: 'PoolHost',
                avatar: 'avatar-1',
                role: 'HOST',
                joinedAt: new Date().toISOString(),
              },
            ],
            createdAt: new Date().toISOString(),
          }),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page);

    // 1. User arrives on Home Hub
    await page.goto('/');

    // 2. Click "Want to Play" action button
    const wantToPlayBtn = page.locator('[data-test="want-to-play-button"]');
    await expect(wantToPlayBtn).toBeVisible();
    await wantToPlayBtn.click();

    // 3. Verify CreatePoolModal opens
    const modal = page.locator('[data-test="create-pool-modal"]');
    await expect(modal).toBeVisible();

    // 4. Match type 1v1 and Fill-based should be selected by default
    await expect(modal.locator('[data-test="match-type-1v1"]')).toBeVisible();
    await expect(modal.locator('[data-test="condition-fill"]')).toBeVisible();

    // 5. Submit pool creation
    await modal.locator('[data-test="submit-pool-btn"]').click();

    // 6. Modal should close and toast should appear
    await expect(modal).toBeHidden();
    await expect(page.locator('[data-testid="success-toast"], [role="status"], .toast, [role="alert"], [data-test="toast-notification"]')).toBeVisible();
    expect(createdPayload.matchType).toBe('ONE_VS_ONE');
    expect(createdPayload.startCondition).toBe('FILL_BASED');
  });

  test('[P0] should create scheduled 2v2 pool with future timestamp (AC 1, AC 3, AC 6)', async ({ page }) => {
    let createdPayload: any = null;

    await page.route('**/api/v1/pools', async (route) => {
      if (route.request().method() === 'POST') {
        createdPayload = JSON.parse(route.request().postData() || '{}');
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'pool-uuid-2',
            creatorId: 'user-uuid-1',
            creatorNickname: 'PoolHost',
            matchType: createdPayload.matchType,
            startCondition: createdPayload.startCondition,
            scheduledTime: createdPayload.scheduledTime,
            skillLevel: createdPayload.skillLevel,
            status: 'OPEN',
            requiredPlayers: 4,
            currentPlayers: 1,
            participants: [
              {
                userId: 'user-uuid-1',
                nickname: 'PoolHost',
                avatar: 'avatar-1',
                role: 'HOST',
                joinedAt: new Date().toISOString(),
              },
            ],
            createdAt: new Date().toISOString(),
          }),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page);
    await page.goto('/');

    await page.locator('[data-test="want-to-play-button"]').click();
    const modal = page.locator('[data-test="create-pool-modal"]');
    await expect(modal).toBeVisible();

    // Select 2v2
    await modal.locator('[data-test="match-type-2v2"]').click();

    // Select Scheduled Time
    await modal.locator('[data-test="condition-scheduled"]').click();

    // Set scheduled datetime
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 2);
    const dateString = futureDate.toISOString().slice(0, 16);
    await modal.locator('[data-test="datetime-picker"]').fill(dateString);

    // Select skill level
    await modal.locator('[data-test="skill-level-select"]').selectOption('ADVANCED');

    // Submit
    await modal.locator('[data-test="submit-pool-btn"]').click();

    await expect(modal).toBeHidden();
    expect(createdPayload.matchType).toBe('TWO_VS_TWO');
    expect(createdPayload.startCondition).toBe('SCHEDULED_TIME');
    expect(createdPayload.skillLevel).toBe('ADVANCED');
    expect(createdPayload.scheduledTime).toBeTruthy();
  });

  test('[P1] should show validation error when maximum pool quota is exceeded (AC 5)', async ({ page }) => {
    await page.route('**/api/v1/pools', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 400,
          contentType: 'application/json',
          body: JSON.stringify({
            message: 'Maximum active pools limit reached (3)',
          }),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page);
    await page.goto('/');

    await page.locator('[data-test="want-to-play-button"]').click();
    const modal = page.locator('[data-test="create-pool-modal"]');
    await expect(modal).toBeVisible();

    await modal.locator('[data-test="submit-pool-btn"]').click();

    // Error feedback should be displayed inside modal
    await expect(modal.locator('[data-test="error-banner"], .text-error')).toContainText('Maximum active pools limit reached');
    await expect(modal).toBeVisible();
  });
});

test.describe('[Story 6.4] Join Existing Matchmaking Pool E2E User Journeys', () => {

  test('[P0] should display open pools on Home Hub and join pool successfully (AC 1, AC 2, AC 3, AC 7)', async ({ page }) => {
    const hostUser = {
      userId: 'user-host-1',
      nickname: 'HostStriker',
      avatar: 'avatar-1',
      role: 'HOST',
      joinedAt: new Date().toISOString(),
    };

    let poolState: any = {
      id: 'pool-open-101',
      creatorId: 'user-host-1',
      creatorNickname: 'HostStriker',
      matchType: 'ONE_VS_ONE',
      startCondition: 'FILL_BASED',
      scheduledTime: null,
      skillLevel: 'OPEN_FOR_ALL',
      status: 'OPEN',
      requiredPlayers: 2,
      currentPlayers: 1,
      participants: [hostUser],
      createdAt: new Date().toISOString(),
    };

    await page.route('**/api/v1/pools', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([poolState]),
        });
      } else {
        await route.continue();
      }
    });

    await page.route('**/api/v1/pools/pool-open-101/join', async (route) => {
      if (route.request().method() === 'POST') {
        poolState = {
          ...poolState,
          status: 'FILLED',
          currentPlayers: 2,
          participants: [
            ...poolState.participants,
            {
              userId: 'e2e-joiner-id',
              nickname: 'JoinerPlayer',
              avatar: 'avatar-2',
              role: 'PLAYER',
              joinedAt: new Date().toISOString(),
            },
          ],
        };
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(poolState),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page);
    await page.goto('/');

    // 1. Verify ActivePoolsList renders open pool card
    const poolCard = page.locator('[data-testid="pool-card-pool-open-101"]');
    await expect(poolCard).toBeVisible();
    await expect(poolCard).toContainText('HostStriker');
    await expect(poolCard).toContainText('1v1');
    await expect(poolCard).toContainText('1/2');

    // 2. Click Join button
    const joinBtn = page.locator('[data-testid="join-pool-btn-pool-open-101"]');
    await expect(joinBtn).toBeVisible();
    await joinBtn.click();

    // 3. Verify success feedback and updated state
    await expect(page.locator('[data-testid="joined-pool-badge-pool-open-101"], [data-testid="success-toast"], .toast, [role="status"]')).toBeVisible();
  });

  test('[P1] should render clean empty state when no active pools exist (AC 8)', async ({ page }) => {
    await page.route('**/api/v1/pools', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page);
    await page.goto('/');

    const emptyState = page.locator('[data-testid="empty-pools-state"]');
    await expect(emptyState).toBeVisible();
  });

  test('[P1] should show error feedback when duplicate join is rejected with 409 Conflict (AC 4)', async ({ page }) => {
    const hostUser = {
      userId: 'user-host-1',
      nickname: 'HostStriker',
      avatar: 'avatar-1',
      role: 'HOST',
      joinedAt: new Date().toISOString(),
    };

    const poolState = {
      id: 'pool-open-101',
      creatorId: 'user-host-1',
      creatorNickname: 'HostStriker',
      matchType: 'ONE_VS_ONE',
      startCondition: 'FILL_BASED',
      scheduledTime: null,
      skillLevel: 'OPEN_FOR_ALL',
      status: 'OPEN',
      requiredPlayers: 2,
      currentPlayers: 1,
      participants: [hostUser],
      createdAt: new Date().toISOString(),
    };

    await page.route('**/api/v1/pools', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([poolState]),
        });
      } else {
        await route.continue();
      }
    });

    await page.route('**/api/v1/pools/pool-open-101/join', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 409,
          contentType: 'application/json',
          body: JSON.stringify({
            message: 'User is already a participant in this pool',
          }),
        });
      } else {
        await route.continue();
      }
    });

    await loginUser(page);
    await page.goto('/');

    const joinBtn = page.locator('[data-testid="join-pool-btn-pool-open-101"]');
    await expect(joinBtn).toBeVisible();
    await joinBtn.click();

    const errorBanner = page.locator('[data-testid="join-error-banner"]');
    await expect(errorBanner).toBeVisible();
    await expect(errorBanner).toContainText('User is already a participant in this pool');
  });
});
