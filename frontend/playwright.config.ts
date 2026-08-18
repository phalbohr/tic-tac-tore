import process from 'node:process'
import { defineConfig, devices } from '@playwright/test'

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// require('dotenv').config();

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './e2e',
  /* Maximum time one test can run for. */
  timeout: 60 * 1000,
  expect: {
    /**
     * Maximum time expect() should wait for the condition to be met.
     * For example in `await expect(locator).toHaveText();`
     */
    timeout: 10000,
  },
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 2 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: [
    ['html'],
    ['junit', { outputFile: 'test-results/results.xml' }],
    ['list'],
  ],
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Maximum time each action such as `click()` can take. Defaults to 0 (no limit). */
    actionTimeout: 15000,
    /* Maximum time for navigation actions. */
    navigationTimeout: 30000,
    /* Base URL to use in actions like `await page.goto('/')`. */
    baseURL: process.env.CI ? 'http://localhost:4173' : 'http://localhost:3000',

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'retain-on-failure',

    /* Run tests headless by default unless HEADED=true is explicitly set */
    headless: !process.env.HEADED,

    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
      },
    },
    ...(process.env.CI ? [] : [
      {
        name: 'firefox',
        use: {
          ...devices['Desktop Firefox'],
        },
      },
      {
        name: 'webkit',
        use: {
          ...devices['Desktop Safari'],
        },
      },
    ]),
  ],

  /* Folder for test artifacts such as screenshots, videos, traces, etc. */
  // outputDir: 'test-results/',

  /* Run your local dev server before starting the tests */
  webServer: [
    {
      command: `cd .. && SPRING_PROFILES_ACTIVE=e2e SPRING_DOCKER_COMPOSE_ENABLED=false TTT_GOOGLE_CLIENT_ID=dummy TTT_GOOGLE_CLIENT_SECRET=dummy TTT_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4173 TTT_OAUTH2_REDIRECT_URI=${process.env.CI ? 'http://localhost:4173/oauth2/redirect' : 'http://localhost:3000/oauth2/redirect'} ./mvnw spring-boot:run`,
      url: 'http://localhost:8090/actuator/health',
      reuseExistingServer: !process.env.CI,
      timeout: 120 * 1000,
    },
    {
      command: process.env.CI ? 'npm run preview' : 'npm run dev',
      port: process.env.CI ? 4173 : 3000,
      reuseExistingServer: !process.env.CI,
      timeout: 120 * 1000,
    },
  ],
})
