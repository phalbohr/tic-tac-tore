# Story 1.1b: E2E Test for OAuth2 Login Flow

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a Quality Engineer,
I want an E2E Playwright test for the Google OAuth2 login flow,
so that regressions in routing, asset loading, or proxying are caught automatically before merging.

## Acceptance Criteria

1. **Given** the local development environment is running
2. **When** Playwright executes the login test suite
3. **Then** it navigates to the home page and verifies that the `main.css` styles are loaded correctly
4. **And** it clicks the "Sign in with Google" button and verifies that the Vite proxy successfully redirects to the backend `/oauth2/authorization/google` endpoint
5. **And** the CI pipeline executes this test successfully during the `frontend-e2e` stage.

## Tasks / Subtasks

- [ ] Task 1 (AC: 1-4) Implement Playwright E2E test for Login
  - [ ] Configure Playwright to start the Spring Boot backend and Vite frontend if not already running, or use a setup script.
  - [ ] Write a test to assert the presence of CSS/styles on the home page.
  - [ ] Write a test to click "Sign in with Google" and verify the URL changes correctly.
- [ ] Task 2 (AC: 5) Enable E2E in CI
  - [ ] Uncomment the `frontend-e2e` stage in `.github/workflows/test.yml`.
  - [ ] Ensure the CI environment properly runs the E2E tests.

## Dev Notes

- **Boundary Testing**: Features involving frontend-to-backend proxies, static asset bundling, or OAuth redirects cannot be validated by unit tests alone. You must either write an E2E test (Playwright) or manually verify the integration using `curl`/shell scripts before marking the task complete.

### Project Structure Notes

- E2E tests should be placed in `frontend/e2e/`.

### References

- [Source: GEMINI.md#Agent Execution & Validation Rules]
- [Source: docs/ci.md#Quality Gates]

## Dev Agent Record

### Agent Model Used

Gemini 3.0 Flash

### Debug Log References

### Completion Notes List

### File List