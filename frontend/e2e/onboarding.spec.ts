import { test, expect } from '@playwright/test';

test.describe('Onboarding Tutorial Flow', () => {
  test('should show tutorial on first login and not show after skip', async ({ page }) => {
    // Setup a new user with tutorialCompleted = false via test-login
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-onboarding-user-${randomSuffix}@example.com`;
    const nickname = `E2EOnboardingUser${randomSuffix}`;
    
    // Explicitly set tutorial completed false for test robustness, though default is false
    await page.request.get('/api/auth/test-login', {
      params: { email, nickname, tutorialCompleted: false }
    });

    await page.goto('/');

    // Wait for the tutorial overlay to appear
    const tutorialCarousel = page.getByTestId('tutorial-carousel');
    await expect(tutorialCarousel).toBeVisible();

    // Verify slide 1 content
    await expect(page.getByText('Tap to record')).toBeVisible();

    // Skip tutorial
    await page.getByTestId('tutorial-skip').click();

    // Verify tutorial disappears
    await expect(tutorialCarousel).toBeHidden();

    // Reload page
    await page.reload();

    // Verify tutorial does NOT reappear
    await expect(page.getByTestId('tutorial-carousel')).toBeHidden();
  });

  test('should show tutorial on first login and not show after finish', async ({ page }) => {
    // Setup another new user
    const randomSuffix = crypto.randomUUID().replace(/[^a-zA-Z0-9]/g, '').substring(0, 12);
    const email = `e2e-onboarding-user2-${randomSuffix}@example.com`;
    const nickname = `E2EOnboardingUserTwo${randomSuffix}`;

    await page.request.get('/api/auth/test-login', {
      params: { email, nickname, tutorialCompleted: false }
    });

    await page.goto('/');

    // Wait for the tutorial overlay to appear
    const tutorialCarousel = page.getByTestId('tutorial-carousel');
    await expect(tutorialCarousel).toBeVisible();

    // Verify slide 1 content
    await expect(page.getByText('Tap to record')).toBeVisible();

    // Click "Next" to go to slide 2
    await page.getByTestId('tutorial-next').click();
    await expect(page.getByText('Tap to confirm')).toBeVisible();
    // To ensure scroll animation finishes and isScrolling is reset, wait for the Next button to be stable
    // A robust way without exposing state is to wait for the scroll element's left position to stabilize.
    await expect(async () => {
      const scrollLeft = await tutorialCarousel.evaluate((el) => el.scrollLeft);
      const width = await tutorialCarousel.evaluate((el) => el.clientWidth);
      expect(scrollLeft).toBeCloseTo(width, -1); // Roughly equal to 1 width
    }).toPass();

    // Click "Next" to go to slide 3
    // Wait until 'Find your strength' slide is not invisible (which implies scroll reached halfway)
    // and wait for scroll to finish by ensuring 'Finish' button is present and not disabled?
    // In our new implementation, we can just wait for the button text to change to 'Let's Go'
    await page.getByTestId('tutorial-next').click();
    await expect(page.getByText('Find your strength')).toBeVisible();
    await expect(page.getByTestId('tutorial-finish')).toBeVisible();

    // Click "Finish"
    await page.getByTestId('tutorial-finish').click();

    // Verify tutorial disappears
    await expect(tutorialCarousel).toBeHidden();

    // Reload page
    await page.reload();

    // Verify tutorial does NOT reappear
    await expect(page.getByTestId('tutorial-carousel')).toBeHidden();
  });
});
