# Story 1.4: Profile Management in Personal Cabinet

Status: ready-for-dev

## Story

As a player,
I want to change my nickname and language in the cabinet,
So that I can personalize my experience.

## Acceptance Criteria

### Functional Requirements
- **Given** player is on Home Hub
- **When** they tap avatar in header (UX-DR5)
- **Then** cabinet page opens (UX-DR6/7)
- **And** nickname can be changed at most once per exactly 30 days (FR31)
- **And** user can select interface language (EN/DE) (FR32)
- **And** the updated profile data is reflected immediately across the app without a full page reload via optimistic UI updates (<50ms visual feedback) (FR59, NFR7).

### Technical Requirements & Guardrails
- **Database Schema**: Add a `last_nickname_update` timestamp column (`java.time.Instant` mapped to `TIMESTAMP WITH TIME ZONE`) and a `language` column (String or Enum `EN`, `DE`) to the `User` entity to enforce the 30-day cooldown and store language preference.
- **Database Migration**: Create a new Flyway migration script (e.g. `V2__add_profile_fields.sql`) in `src/main/resources/db/migration/`. This script MUST also include the `ALTER TABLE "user" ALTER COLUMN nickname SET NOT NULL;` constraint to resolve technical debt from Story 1.3.
- **Strict Layering & Object Retrieval**: Profile updates must be performed via the Service layer. Do not rely solely on the JWT token for language/avatar, as the token may only hold basic claims. Re-fetch user if needed or just update DB and return fresh DTO.
- **Validation & Code Reuse**: Nickname must remain unique (handle `DataIntegrityViolationException` properly) and strip non-alphanumeric characters. Extract and reuse the existing nickname sanitization logic in `UserService` rather than duplicating it for the update flow.
- **API Endpoint**: Create a `@PatchMapping("/me")` in `ProfileApi.java` and implement in `UserController.java` accepting an `UpdateProfileRequest` DTO for partial updates.
- **Frontend State**: Update Pinia `auth.ts` to push the new profile data up optimistically without an infinite pulse loading loop. Use reactive state and handle rollbacks if the API fails.
- **Testing Standards & Time Simulation**: All tests must use AAA (Arrange-Act-Assert) pattern separated by a single blank line, with absolutely zero structural comments. In `UserServiceTest`, inject a mock `java.time.Clock` bean to reliably test the 30-day cooldown logic without brittle date math.

## Previous Story Intelligence (Story 1.3)
- **Learnings**: Transaction boundaries in user creation caused `UnexpectedRollbackException` when catching `DataIntegrityViolationException`. While updating the profile, ensure uniqueness checks handle transactions correctly.
- **Pattern**: `existsByNickname` was used in `UserService`. Use it to validate nickname uniqueness before saving to avoid DB exceptions if possible, but still handle concurrent DB exceptions gracefully.
- **Testing**: Previous tests were rejected for using hardcoded translated strings in E2E. Use test IDs or robust locators in `profile-management.spec.ts`. Do not bypass backend with complete API mocking in E2E tests for this flow.
- **Security**: The previous story identified issues with DB exception masking. Ensure `UserService.updateProfile` throws clear custom exceptions (like `IllegalArgumentException` or a custom `ValidationException`) instead of generic 500 errors.

## Architecture Compliance
- **Styles**: Any new UI components for the cabinet must follow the "No-Line" rule (UX-DR3) and use the Clubhouse Editorial theme (UX-DR7). Use `ch-` prefixed SCSS classes if custom styles are needed to avoid Tailwind conflicts.
- **Limits**: Follow the 500-Line rule for all new components/classes (IP-04). If `UserService` exceeds 500 lines after adding update logic, split it.
- **Naming**: Ensure `UpdateProfileRequest` uses camelCase properties for JSON serialization. Endpoints must be kebab-case.

## Library & Framework Requirements
- **Backend**: Spring Boot 4.0, Java 21. Use standard Jakarta validation annotations for DTOs.
- **Frontend**: Vue 3 with Composition API, Vite 8, Pinia, Tailwind v4 + SCSS. Implement localization (FR59) using `vue-i18n`.

## File Structure Requirements
- **Feature Grouping**: Place frontend components in `src/features/profile/` (e.g. `Cabinet.vue`).
- **Localization Files**: Place translation files in `frontend/src/locales/` (e.g., `en.json`, `de.json`).
- **Tests**: Keep unit tests co-located (e.g. `src/test/java/com/tictactore/service/UserServiceTest.java`). E2E in `frontend/e2e/`.

## Testing Requirements
- Unit tests for the 30-day cooldown logic in `UserService` using a mock `Clock`.
- Unit tests for nickname sanitization and uniqueness check on update.
- API tests in `UserControllerTest`.
- E2E test in Playwright (`frontend/e2e/profile-management.spec.ts`) validating that changing the name works, changing it again fails, and changing language immediately updates UI (FR59). Do not bypass the backend with complete API mocking for this flow.

## BMAD Workflow Rules (from GEMINI.md)
- **Feature Branch**: Create and work in `story/1-4-profile-management-in-personal-cabinet` off `develop`.
- **GitHub Sync**: 
  1. Sync this story to a GitHub issue: `gh issue list --search "Story 1.4"`. If none, create it with the FULL verbatim text of this story file.
  2. Assign it to `@phalbohr` and add label `ready-for-dev`.
- **Validation**: NEVER present a bug fix or feature completion without first running `./scripts/ci-local.sh`.
- **No Premature Completion**: Do not mark this story `done` until a test verifies the functionality.

## Project Context Reference
- Epic 1: Quick Start (Auth & Basic Profile)
- Associated FRs: FR31, FR32, FR59.

## Completion Notes
Ultimate context engine analysis completed - comprehensive developer guide created.
