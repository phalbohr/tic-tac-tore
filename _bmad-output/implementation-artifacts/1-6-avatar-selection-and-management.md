# Story 1.6: Avatar Selection & Management

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to select an avatar,
so that I can personalize my profile.

## Acceptance Criteria

- **Given** the player is logged into their personal cabinet
- **When** they click to edit their avatar and select an image from the preset grid
- **Then** the system saves the new avatar and updates their identity globally across all app views (FR30)
- **And** the avatar is chosen from a grid of 24 preset authentic kicker SVGs (generated via Stitch) with zero asset cost.

## Developer Context

This story implements the avatar selection feature for the user profile. According to the UX design specifications, custom image upload is DEFERRED to Phase 2. Instead, you must provide a preset grid of 24 SVG icons.

**IMPORTANT NEW REQUIREMENT**: You must use the `stitch-design` skill during this story's execution to generate 24 authentic foosball (kicker) SVG icons (e.g., table players, balls, beer, etc.) instead of generic emojis. Bundle these as a tiny inline SVG sprite or lightweight Vue components.

### Technical Requirements & Guardrails

- **NO IMAGE UPLOAD:** Do not implement custom image uploads.
- **Backend API Validation:** Extend the existing profile update endpoint (`updateProfile`) to accept an `avatar` parameter. Implement a strict Enum or Set whitelist on the backend to validate the incoming string payload against the 24 allowed kicker icon names. Do NOT trust client input.
- **UX Primitive `AvatarBase`:** Create a new `AvatarBase` component as a "pure visual primitive" (replaces the older `AvatarInteractive`). It must render the selected SVG avatar. Use this component in `Cabinet.vue` and anywhere the avatar is displayed.
- **Anonymous Fallback Mapping:** The backend sets avatar to `"anonymous"` when a user is deleted (Story 1.5). `AvatarBase` MUST correctly map the `"anonymous"` state to a default fallback SVG (e.g., "boots hung on a nail").
- **Component Decoupling (`AvatarPicker`):** The `AvatarPicker` component must emit a `@select` event rather than directly mutating the Pinia store. This ensures reusability for the Onboarding flow (Story 1.7).
- **Optimistic State Updates:** Updating the avatar should optimistically reflect in the Pinia `auth` store immediately, but gracefully revert to the previous state if the API call fails, preventing broken UI states.

### Architecture Compliance

- **No-Line Rule (UX-DR3):** Ensure `AvatarPicker` grid and buttons have NO explicit borders/lines; rely on spacing, background colors, and elevation.
- **Clubhouse Editorial Theme (UX-DR7):** Use typography and color tokens consistent with the current design language.
- **Extensibility:** `AvatarBase` will eventually accept wrappers like `AvatarSelfHub` and `AvatarQuickStats` (UX spec). Build it cleanly to accept future interaction wrappers.
- Follow the 500-Line Rule (IP-04) for all files.

### Testing Requirements

- **Strict AAA Pattern:** All new tests (backend and frontend) must follow the Arrange-Act-Assert pattern separated by a single blank line, with **absolutely zero structural comments** (like `// Arrange`, `// Act`, `// Assert`).
- **E2E Testing:** Add a Playwright test verifying the avatar selection flow in the cabinet, including optimistic UI updates and persistence.
- Run `./scripts/ci-local.sh` to ensure all checks pass.

## Previous Story Intelligence (Stories 1.4 & 1.5)

- **Transaction Boundaries:** Be aware of `ObjectOptimisticLockingFailureException` since `User` uses `@Version`.
- **Testing AAA Pattern:** No structural comments in tests.

## Tasks / Subtasks

- [ ] **Icon Generation**
  - [ ] Invoke the `stitch-design` skill to generate 24 authentic kicker/foosball themed SVG icons.
- [ ] **Backend: API & Validation**
  - [ ] Extend `updateProfile` in `UserService` and `ProfileApi` to accept `avatar`.
  - [ ] Implement strict whitelist validation (Enum/Set) for the 24 icon names in the backend to reject invalid payloads.
- [ ] **Backend: Testing**
  - [ ] Add unit tests for avatar update and validation logic (AAA format, no comments).
  - [ ] Add API integration test for the profile update endpoint.
- [ ] **Frontend: Components & UI**
  - [ ] Create `AvatarBase.vue` as a pure visual primitive that handles the 24 SVG icons AND the `"anonymous"` fallback state.
  - [ ] Create `AvatarPicker.vue` that displays the grid of 24 icons and emits `@select`.
  - [ ] Update `Cabinet.vue` to use `AvatarBase` and open `AvatarPicker` via modal/overlay.
- [ ] **Frontend: State & Extensibility**
  - [ ] Update `auth.ts` Pinia store with optimistic UI update for avatar changes, with rollback on failure.
  - [ ] Add localization strings for avatar selection to `en.json` and `de.json`.
- [ ] **Frontend: E2E Validation**
  - [ ] Implement E2E test in `frontend/e2e/avatar-selection.spec.ts`.

## Dev Notes

- **Implementation Path**: Use inline SVG sprites or a lightweight icon mapping file in Vue to keep the bundle size small.
- **Backend Schema**: Ensure the `avatar` database column length is sufficient for the string keys used to reference the SVGs (e.g., `kicker-player-red`, `foosball-ball`).
- **State Reversion**: In `auth.ts`, cache the old avatar before the API call, and restore it inside the `catch` block if the request fails.

### References
- PRD: FR30 Player can set a nickname and avatar image in their personal cabinet.
- UX Design Spec: Avatar preset grid keeps MVP bundle tiny; `AvatarBase` replaces `AvatarInteractive`.

## BMAD Workflow Rules (from GEMINI.md)
- **Feature Branch**: Create and work in `story/1-6-avatar-selection-and-management` off `develop`.
- **Validation**: NEVER present a feature completion without first running `./scripts/ci-local.sh`.
