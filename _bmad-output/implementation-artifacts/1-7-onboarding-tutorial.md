# Story 1.7: Onboarding Tutorial

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a new user,
I want an onboarding tutorial,
so that I can learn how to use the app.

## Acceptance Criteria

- **Given** a newly registered user logs in for the first time (or someone who hasn't completed/skipped the tutorial)
- **When** the Home Hub renders
- **Then** the system triggers a dismissible onboarding tutorial overlay explaining core features (FR56)
- **And** the tutorial consists of 3 swipeable slides as per the UX Design Spec
- **And** skipping or finishing the tutorial updates the user profile to prevent it from showing again

## Developer Context

This story implements the onboarding experience for new users, following the "confirmation-first organic onboarding" pattern defined in the UX specification. It intercepts the user upon first login at the `HomeHub` (or before they proceed) to show a lightweight 3-slide tutorial.

### Technical Requirements & Guardrails

- **Backend API & Data Model:** Add a `tutorialCompleted` (or `onboardingCompleted`) boolean flag to the `User` entity, database schema (via a Flyway migration), `UserService`, and Profile DTOs.
- **Frontend State:** Expose this new flag via the `auth.ts` Pinia store (`profile.tutorialCompleted`).
- **Frontend Component:** Build a `TutorialCarousel.vue` component to present the 3 slides:
  1. "Tap to record"
  2. "Tap to confirm"
  3. "Find your strength"
  (Use simple SVG illustrations/emojis or placeholder visuals matching the "Clubhouse Editorial" theme for MVP).
- **Trigger Logic:** In `HomeHub.vue`, if `authStore.profile.tutorialCompleted` is false, show the `TutorialCarousel` overlay.
- **Skip/Finish:** Implement an API call to `PATCH /api/v1/profile/me` with `{ tutorialCompleted: true }` when the user skips or finishes the tutorial, and hide the overlay.

### Architecture Compliance

- **No-Line Rule (UX-DR3):** Ensure the `TutorialCarousel` overlay relies on background shifts and spacing instead of explicit borders.
- **Dependencies:** Avoid adding heavy 3rd-party carousel libraries. Use native CSS Scroll Snap (`snap-x`, `snap-mandatory` in Tailwind) for the swipeable carousel.
- **Clubhouse Editorial Theme (UX-DR7):** Use warm darks and space grotesk/manrope typography.
- Follow the 500-Line Rule (IP-04) for all files.

### Testing Requirements

- **Strict AAA Pattern:** All new backend unit/integration tests must follow the Arrange-Act-Assert pattern with a single blank line separating the phases, and **absolutely zero structural comments** (like `// Arrange`, `// Act`, `// Assert`).
- **E2E Testing:** Add a Playwright test in `frontend/e2e/onboarding.spec.ts` that mocks a new user, verifies the tutorial appears on the Home Hub, interacts with it (skips/finishes), and verifies it doesn't appear on subsequent reloads.
- Run `./scripts/ci-local.sh` to ensure all checks pass.

## Previous Story Intelligence (Story 1.6)

- **Testing AAA Pattern:** Remember to avoid structural comments in tests.
- **Transaction Boundaries:** Be aware of `ObjectOptimisticLockingFailureException` since `User` uses `@Version`.
- **UI Consistency:** The modal/popover should maintain the "No-Line" rule and "Clubhouse Editorial" theme, matching the style established in `AvatarPicker`.

## Tasks / Subtasks

- [ ] **Backend: API & Service Implementation**
  - [ ] Add Flyway migration (e.g. `V3__add_tutorial_completed_to_user.sql`) to add `tutorial_completed` boolean column to `user` table (default `false`).
  - [ ] Update `User` entity (use `@Builder.Default` since it uses Lombok `@Builder`), `ProfileDto`, and `UpdateProfileRequest` to handle `tutorialCompleted`.
  - [ ] Update `UserService.updateProfile` to process `tutorialCompleted` flag updates.
- [ ] **Backend: Testing**
  - [ ] Add unit tests for `tutorialCompleted` update logic (AAA format, no comments).
  - [ ] Update integration tests to cover the new profile field.
- [ ] **Frontend: UI & State**
  - [ ] Update `auth.ts` Pinia store to parse and handle `tutorialCompleted`.
  - [ ] Create `TutorialCarousel.vue` component with native CSS scroll snapping.
  - [ ] Update `HomeHub.vue` to conditionally render `TutorialCarousel` based on the profile flag.
  - [ ] Refactor `updateProfile` signature in `auth.ts` to accept an object payload to safely add `tutorialCompleted`, and update all existing callers (like `Cabinet.vue`) to prevent regressions.
  - [ ] Hook up skip/finish buttons in the carousel to trigger `updateProfile({ tutorialCompleted: true })`.
  - [ ] Add localization strings for tutorial slides to `en.json` and `de.json`.
- [ ] **Frontend: E2E Validation**
  - [ ] Implement E2E test in `frontend/e2e/onboarding.spec.ts`.

## Dev Notes

- The tutorial content slides:
  1. **"Tap to record"** — 5-second clip/illustration of New Match flow.
  2. **"Tap to confirm"** — push notification → tap → done.
  3. **"Find your strength"** — leaderboard teaser with positional stats highlight.
- Since we don't have video clips yet, use simple SVG icons or styled text to represent these concepts for MVP.
- The `auth.ts` file currently defines `updateProfile` with positional arguments (`nickname?: string, language?: string`). You MUST refactor this to an object payload to safely add `tutorialCompleted`, and make sure to update existing callers like `Cabinet.vue` to prevent regressions.

## BMAD Workflow Rules (from GEMINI.md)
- **Feature Branch**: Create and work in `story/1-7-onboarding-tutorial` off `develop`.
- **Validation**: NEVER present a feature completion without first running `./scripts/ci-local.sh`.
