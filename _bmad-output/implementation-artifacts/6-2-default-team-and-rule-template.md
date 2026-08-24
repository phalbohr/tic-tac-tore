---
baseline_commit: HEAD
---

# Story 6.2: Default Team and Rule Template

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to set a default player group ("team") and a default rule template in my user preferences and inline during match creation,
so that future match creation screens automatically pre-populate the selected rule template and filter the player list by the default group, saving time while allowing quick overrides without navigating away.

## Acceptance Criteria

1. **Given** an authenticated user managing preferences in Profile Settings (`/cabinet`)
   **When** they view their settings
   **Then** they see a dedicated "Default Match Preferences" section with selectors for "Default Player Group" and "Default Rule Template"
   **And** the group selector lists the user's custom player groups plus a "None" option
   **And** the rule template selector lists built-in system presets (ITSF, DTFB) and the user's custom templates plus a "None" option.
2. **Given** an authenticated user selecting a default group and/or rule template in `/cabinet` or inline in match creation (`/matches/new`)
   **When** they save their preferences via `PATCH /api/v1/profile/me`
   **Then** the backend persists `defaultGroupId` and `defaultRuleConfigurationId` to the user's record
   **And** the backend validates that `defaultGroupId` belongs to the authenticated user (`creatorId == currentUserId`) if provided (or `null`)
   **And** the backend validates that `defaultRuleConfigurationId` is either a system preset (`type == PRESET`) or belongs to the authenticated user (`createdBy == currentUserId`) if provided (or `null`)
   **And** attempts to set another user's group or template return `400 Bad Request`.
3. **Given** an authenticated user setting up a new match in the portrait match creation flow (`/matches/new`)
   **When** the match creation screen mounts
   **Then** if the user has a saved `defaultRuleConfigurationId`, that rule template is automatically pre-selected in `RulePicker.vue` (falling back to the standard preset if not set)
   **And** if the user has a saved `defaultGroupId`, that player group is automatically pre-filtered in `PlayerSelection.vue`, displaying the group's members in the quick-select list
   **And** the player can manually select a different group or rule template for the active match without mutating or overwriting the saved profile defaults (FR40).
4. **Given** an authenticated user in the match creation flow (`/matches/new`)
   **When** they interact with the active rule template chip or player group chip
   **Then** an inline "Set as Default" action/control is available to update their profile defaults directly without navigating away to the settings screen (FR40).
5. **Given** a user with saved default preferences deletes their default player group (`DELETE /api/v1/player-groups/{id}`) or default custom rule template (`DELETE /api/v1/rule-configurations/{id}`)
   **When** the deletion occurs
   **Then** database foreign key constraints (`ON DELETE SET NULL`) automatically clear the corresponding default (`default_group_id` or `default_rule_configuration_id`) on the `"user"` record to prevent orphan references and broken state.

## Tasks / Subtasks

- [ ] Task 1: Database Migration & Domain Entity Updates (AC1, AC2, AC5)
  - [ ] Create Flyway migration `V12__add_user_defaults.sql`:
    - Add nullable columns `default_group_id UUID REFERENCES player_group(id) ON DELETE SET NULL` and `default_rule_configuration_id UUID REFERENCES rule_configuration(id) ON DELETE SET NULL` to the `"user"` table.
    - Create indexes on `"user"(default_group_id)` and `"user"(default_rule_configuration_id)`.
  - [ ] Update `com.tictactore.model.User` entity with `defaultGroupId` (UUID) and `defaultRuleConfigurationId` (UUID) mappings.
  - [ ] Update `UserRepositoryTest.java` to verify persistence, retrieval, and DB-level `ON DELETE SET NULL` cascades when referenced group/rule is deleted.
- [ ] Task 2: Backend DTOs, Service Validation & Controller Integration (AC1, AC2, AC5)
  - [ ] Update `com.tictactore.dto.ProfileDto`:
    - Add `UUID defaultGroupId` and `UUID defaultRuleConfigurationId` with Swagger schema annotations.
  - [ ] Update `com.tictactore.dto.UpdateProfileRequest`:
    - Add `UUID defaultGroupId` and `UUID defaultRuleConfigurationId` with Swagger schema annotations.
  - [ ] Update `com.tictactore.service.UserService` & `UserServiceImpl.updateProfile`:
    - When `defaultGroupId` is non-null, verify existence and creator ownership in `PlayerGroupRepository` (`findByIdAndCreatorId`), throwing `IllegalArgumentException` / `400 Bad Request` if invalid or owned by another user.
    - When `defaultRuleConfigurationId` is non-null, verify existence and validity in `RuleConfigurationRepository` (`findByIdAndCreatedByOrType`), throwing `IllegalArgumentException` / `400 Bad Request` if invalid or owned by another user.
    - Map `defaultGroupId` and `defaultRuleConfigurationId` to the `User` entity and save.
    - Populate fields in the returned `ProfileDto`.
  - [ ] Update `UserController.java` (`getMyProfile` and `updateProfile`) to include `defaultGroupId` and `defaultRuleConfigurationId` in `ProfileDto`.
  - [ ] Unit & ATDD Tests:
    - Update `UserServiceTest.java` and `UserControllerTest.java` with valid, null-clearing, and unauthorized group/rule scenarios.
    - Create/Update `UserControllerATDDTest.java` with tests for profile preferences update, cross-user isolation, and preset vs custom validation.
- [ ] Task 3: Frontend Store & Service Updates (AC1, AC2, AC3, AC4)
  - [ ] Update `frontend/src/stores/auth.ts`:
    - Extend `UserProfile` interface with `defaultGroupId?: string | null` and `defaultRuleConfigurationId?: string | null`.
    - Update `updateProfile` action to accept and persist `defaultGroupId` and `defaultRuleConfigurationId`.
  - [ ] Update `frontend/src/features/match/stores/matchDraftStore.ts`:
    - In `fetchDefaults()`, read `authStore.profile?.defaultRuleConfigurationId` and `authStore.profile?.defaultGroupId`.
    - Apply `defaultRuleConfigurationId` as the initial `ruleSystem` if available (falling back to `STANDARD` preset).
    - Provide a method or action to set inline defaults (`setDefaultRule(ruleId)`, `setDefaultGroup(groupId)`) calling `authStore.updateProfile`.
- [ ] Task 4: Frontend UI Components & Inline Match Creation Integration (AC1, AC3, AC4)
  - [ ] Create `frontend/src/features/profile/components/UserPreferencesSection.vue`:
    - Clubhouse-styled card (`bg-surface-container-low`, rounded-2xl, no 1px borders per `UX-DR3`).
    - Selectors for "Default Player Group" (options from `usePlayerGroupStore`) and "Default Rule Template" (options from `useRuleConfigStore`), each with a "None" option.
    - Inline save / debounced patch or save button calling `authStore.updateProfile`.
    - Add to `frontend/src/features/profile/Cabinet.vue` ensuring `Cabinet.vue` stays strictly under 500 lines (`IP-04`).
  - [ ] Update `frontend/src/features/match/components/RulePicker.vue`:
    - Check if the currently selected rule matches `authStore.profile?.defaultRuleConfigurationId`.
    - Add an inline "Set as default" action/button (star/bookmark icon) to let the user save the selected rule as their profile default directly from match setup (FR40).
  - [ ] Update `frontend/src/features/match/components/PlayerSelection.vue`:
    - On mount, if `authStore.profile?.defaultGroupId` exists and `selectedGroupId` is not set, initialize `selectedGroupId` to `defaultGroupId`.
    - Add an inline "Set as default" action/button on the active player group chip to let the user save the selected group as their profile default directly from match setup (FR40).
  - [ ] Add i18n keys to `frontend/src/locales/en.json` and `frontend/src/locales/de.json` for all new labels, tooltips, and messages.
- [ ] Task 5: Testing & Quality Verification
  - [ ] Backend Unit & ATDD Tests:
    - `UserServiceTest.java` (setting defaults, clearing defaults with null, validating group ownership, validating rule preset vs custom ownership).
    - `UserControllerATDDTest.java` (full request/response cycle for `GET /api/v1/profile/me` and `PATCH /api/v1/profile/me`).
  - [ ] Frontend Unit/Store Tests:
    - `auth.spec.ts` (store updates with `defaultGroupId` and `defaultRuleConfigurationId`).
    - `UserPreferencesSection.spec.ts` (rendering selectors, selecting None, saving preferences).
    - `RulePicker.spec.ts` and `PlayerSelection.spec.ts` (pre-population and inline set-as-default triggers).
  - [ ] E2E Playwright Tests:
    - Create `frontend/e2e/user-defaults.spec.ts`:
      - Test 1: Set default group and rule in Profile Cabinet -> open New Match -> verify rule is pre-selected and group is pre-filtered.
      - Test 2: Override rule and group in New Match -> verify match can be started with overridden values without altering profile defaults.
      - Test 3: Set default rule and group inline during match creation -> verify profile is updated.
      - Test 4: Delete the default group in Cabinet -> verify default group resets to null and does not break match creation.
  - [ ] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contracts & Security (AD-04, AD-05):**
  - Profile endpoint: `GET /api/v1/profile/me` and `PATCH /api/v1/profile/me`.
  - Authentication: Requires valid session/JWT. User principal extracted via `@AuthenticationPrincipal User principal`.
  - Validation rules:
    - `defaultGroupId`: Must exist and have `creatorId == principal.getId()`. Null or omitted values leave it unset/cleared.
    - `defaultRuleConfigurationId`: Must exist and be either `type == PRESET` or have `createdBy == principal.getId()`. Null or omitted values leave it unset/cleared.
    - Violations must throw `IllegalArgumentException` and return `400 Bad Request`.
- **Database Design & Migrations:**
  - Flyway migration script: `src/main/resources/db/migration/V12__add_user_defaults.sql`.
  - Foreign keys: `default_group_id UUID REFERENCES player_group(id) ON DELETE SET NULL` and `default_rule_configuration_id UUID REFERENCES rule_configuration(id) ON DELETE SET NULL`.
  - Indexes: Explicit B-tree indexes on `"user"(default_group_id)` and `"user"(default_rule_configuration_id)`.
  - Concurrency: `@Version` column on `User` entity to support optimistic locking.
- **UX & Navigation Invariants:**
  - **No Dedicated Preferences Page:** Defaults are managed within Profile Settings (`Cabinet.vue` via `UserPreferencesSection.vue`) or inline during match setup (`NewMatchFlow.vue` / `RulePicker.vue` / `PlayerSelection.vue`).
  - **Match Draft Preservation:** Setting defaults or auto-populating defaults does NOT erase selected players or active draft state in `useMatchDraftStore`.
  - **Non-Destructive Overrides:** Selecting a different group or rule during match creation does not mutate the saved profile defaults unless the user explicitly triggers "Set as Default".
- **Clubhouse Styling Guidelines (UX-DR3):**
  - Follow Clubhouse "No-Line" rule: surface background tonal shifts and elevation instead of 1px solid border lines between form elements and cards.
- **500-Line Rule (IP-04):**
  - All new and updated files strictly under 500 lines. `UserPreferencesSection.vue` is extracted as a separate component to keep `Cabinet.vue` within limits.
- **Testing Standards:**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments.

### Previous Story Intelligence (Learnings from 6.1 and 6.1b)

- **Ownership Isolation:** Always filter and validate entities against `principal.getId()` on the backend. Never trust client-supplied entity IDs without verifying ownership or preset status.
- **FK Cascades:** Utilizing `ON DELETE SET NULL` at the database level guarantees data integrity even if groups/templates are deleted directly or through API endpoints, preventing orphan UUID errors.
- **Component Isolation:** Keep modal, section, and store state cleanly separated. Avoid polluting `useAuthStore` with match creation logic; let `useMatchDraftStore` read from `useAuthStore`.

### Project Context Reference

- [Architecture Document](_bmad-output/planning-artifacts/architecture.md) - AD-01, AD-04, AD-05, IP-04
- [PRD](_bmad-output/planning-artifacts/prd.md) - FR40 (Default team and rule template, inline match setup)
- [UX Design Specification](_bmad-output/planning-artifacts/ux-design-specification.md) - Section "Teams & Rules: no dedicated screen", UX-DR3

### Planned File List

- `src/main/resources/db/migration/V12__add_user_defaults.sql` (NEW)
- `src/main/java/com/tictactore/model/User.java` (UPDATE)
- `src/main/java/com/tictactore/dto/ProfileDto.java` (UPDATE)
- `src/main/java/com/tictactore/dto/UpdateProfileRequest.java` (UPDATE)
- `src/main/java/com/tictactore/service/UserService.java` (UPDATE)
- `src/main/java/com/tictactore/service/UserServiceImpl.java` (UPDATE)
- `src/main/java/com/tictactore/controller/UserController.java` (UPDATE)
- `src/test/java/com/tictactore/service/UserServiceTest.java` (UPDATE)
- `src/test/java/com/tictactore/controller/UserControllerTest.java` (UPDATE)
- `src/test/java/com/tictactore/controller/UserControllerATDDTest.java` (UPDATE)
- `frontend/src/stores/auth.ts` (UPDATE)
- `frontend/src/features/match/stores/matchDraftStore.ts` (UPDATE)
- `frontend/src/features/profile/Cabinet.vue` (UPDATE)
- `frontend/src/features/profile/components/UserPreferencesSection.vue` (NEW)
- `frontend/src/features/match/components/RulePicker.vue` (UPDATE)
- `frontend/src/features/match/components/PlayerSelection.vue` (UPDATE)
- `frontend/src/locales/en.json` (UPDATE)
- `frontend/src/locales/de.json` (UPDATE)
- `frontend/e2e/user-defaults.spec.ts` (NEW)
