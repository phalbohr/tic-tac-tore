import { test, expect } from '@playwright/test';
import { faker } from '@faker-js/faker';

test.describe('Onboarding Tutorial Flow', () => {
  test('[P0] should show tutorial on first login and not show after skip', async ({ page }) => {
    // Given - Setup a new user with tutorialCompleted = false via test-login
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-onboarding-user-${randomSuffix}@example.com`;
    const nickname = `E2EOnboardingUser${randomSuffix}`;
    
    // Explicitly set tutorial completed false for test robustness, though default is false
    await page.request.get('/api/auth/test-login', {
      params: { email, nickname, tutorialCompleted: false }
    });

    // When
    await page.goto('/');

    // Then - Wait for the tutorial overlay to appear
    const tutorialCarousel = page.getByTestId('tutorial-carousel');
    await expect(tutorialCarousel).toBeVisible();

    // Verify slide 1 content
    await expect(page.getByText('Tap to record')).toBeVisible();

    // When - Skip tutorial
    await page.getByTestId('tutorial-skip').click();

    // Then - Verify tutorial disappears
    await expect(tutorialCarousel).toBeHidden();

    // When - Reload page
    await page.reload();

    // Then - Verify tutorial does NOT reappear
    await expect(page.getByTestId('tutorial-carousel')).toBeHidden();
  });

  test('[P0] should show tutorial on first login and not show after finish', async ({ page }) => {
    // Given - Setup another new user
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-onboarding-user2-${randomSuffix}@example.com`;
    const nickname = `E2EOnboardingUserTwo${randomSuffix}`;

    await page.request.get('/api/auth/test-login', {
      params: { email, nickname, tutorialCompleted: false }
    });

    // When
    await page.goto('/');

    // Then - Wait for the tutorial overlay to appear
    const tutorialCarousel = page.getByTestId('tutorial-carousel');
    await expect(tutorialCarousel).toBeVisible();

    // Verify slide 1 content
    await expect(page.getByText('Tap to record')).toBeVisible();

    // When - Click "Next" to go to slide 2
    await page.getByTestId('tutorial-next').click();
    
    // Then
    await expect(page.getByText('Tap to confirm')).toBeVisible();
    await expect(async () => {
      const scrollLeft = await tutorialCarousel.evaluate((el) => el.scrollLeft);
      const width = await tutorialCarousel.evaluate((el) => el.clientWidth);
      expect(scrollLeft).toBeCloseTo(width, -1);
    }).toPass();

    // When - Click "Next" to go to slide 3
    await page.getByTestId('tutorial-next').click();
    
    // Then
    await expect(page.getByText('Find your strength')).toBeVisible();
    await expect(page.getByTestId('tutorial-finish')).toBeVisible();

    // When - Click "Finish"
    await page.getByTestId('tutorial-finish').click();

    // Then - Verify tutorial disappears
    await expect(tutorialCarousel).toBeHidden();

    // When - Reload page
    await page.reload();

    // Then - Verify tutorial does NOT reappear
    await expect(page.getByTestId('tutorial-carousel')).toBeHidden();
  });
});
