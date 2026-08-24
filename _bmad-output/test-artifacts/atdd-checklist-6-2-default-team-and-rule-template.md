---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-24T16:38:00+02:00'
storyId: '6.2'
storyKey: '6-2-default-team-and-rule-template'
storyFile: '_bmad-output/implementation-artifacts/6-2-default-team-and-rule-template.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-6-2-default-team-and-rule-template.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-6-2/UserControllerATDDTest.java'
  - 'frontend/e2e/user-defaults.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-2/UserPreferencesSection.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-2/matchDraftStore.defaults.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-2/RulePicker.defaults.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-2/PlayerSelection.defaults.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/6-2-default-team-and-rule-template.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/overview.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/api-request.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 6.2

## Story Context
- **Story Key:** `6-2-default-team-and-rule-template`
- **Story ID:** `6.2`
- **Title:** Story 6.2: Default Team and Rule Template
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/6-2-default-team-and-rule-template.md`

## Acceptance Criteria Summary
1. **AC 1:** Given an authenticated user managing preferences in Profile Settings (`/cabinet`), when they view their settings, then they see a dedicated "Default Match Preferences" section with selectors for "Default Player Group" and "Default Rule Template", where the group selector lists the user's custom player groups plus a "None" option, and the rule template selector lists built-in system presets (ITSF, DTFB) and the user's custom templates plus a "None" option.
2. **AC 2:** Given an authenticated user selecting a default group and/or rule template in `/cabinet` or inline in match creation (`/matches/new`), when they save their preferences via `PATCH /api/v1/profile/me`, then the backend persists `defaultGroupId` and `defaultRuleConfigurationId` to the user's record, validates that `defaultGroupId` belongs to the authenticated user (`creatorId == currentUserId`) if provided (or `null`), validates that `defaultRuleConfigurationId` is either a system preset (`type == PRESET`) or belongs to the authenticated user (`createdBy == currentUserId`) if provided (or `null`), and rejects attempts to set another user's group or template with `400 Bad Request`.
3. **AC 3:** Given an authenticated user setting up a new match in the portrait match creation flow (`/matches/new`), when the match creation screen mounts, then if the user has a saved `defaultRuleConfigurationId`, that rule template is automatically pre-selected in `RulePicker.vue` (falling back to the standard preset if not set), if the user has a saved `defaultGroupId`, that player group is automatically pre-filtered in `PlayerSelection.vue` displaying the group's members in the quick-select list, and the player can manually select a different group or rule template for the active match without mutating or overwriting the saved profile defaults (`FR40`).
4. **AC 4:** Given an authenticated user in the match creation flow (`/matches/new`), when they interact with the active rule template chip or player group chip, then an inline "Set as Default" action/control is available to update their profile defaults directly without navigating away to the settings screen (`FR40`).
5. **AC 5:** Given a user with saved default preferences deletes their default player group (`DELETE /api/v1/player-groups/{id}`) or default custom rule template (`DELETE /api/v1/rule-configurations/{id}`), when the deletion occurs, then database foreign key constraints (`ON DELETE SET NULL`) automatically clear the corresponding default (`default_group_id` or `default_rule_configuration_id`) on the `"user"` record to prevent orphan references and broken state.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Well-defined REST contract updates on `/api/v1/profile/me`, explicit ownership validation rules (`AD-04`, `AD-05`), clear UI component extraction (`UserPreferencesSection.vue` under `IP-04` 500-line rule), and existing patterns from Stories 6.1 and 6.1b.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Dedicated "Default Match Preferences" in `/cabinet` | Component (Vitest) / E2E | `UserPreferencesSection.vue` & `Cabinet.vue` | P0 | 1. Render selectors for group and rule with "None" options<br>2. Selectors populate with user's groups and presets/custom rules |
| **AC 2** | Persist `defaultGroupId` & `defaultRuleConfigurationId` (`PATCH /api/v1/profile/me`) | API / Integration | `UserController` & `UserService` | P0 | 1. `GET /api/v1/profile/me` returns defaults from principal<br>2. `PATCH /api/v1/profile/me` persists valid group and rule IDs<br>3. Null clears default values<br>4. Foreign group returns 400 Bad Request<br>5. Foreign non-preset rule returns 400 Bad Request |
| **AC 3** | Pre-populate default rule & pre-filter default group in `/matches/new` | Store (Vitest) & E2E | `matchDraftStore.ts` & `user-defaults.spec.ts` | P0 | 1. Mount `/matches/new` with defaults set -> verify rule and group are active<br>2. Non-destructive manual override for active match without mutating profile defaults (`FR40`) |
| **AC 4** | Inline "Set as Default" action during match creation | Component (Vitest) & E2E | `RulePicker.vue`, `PlayerSelection.vue`, `user-defaults.spec.ts` | P0 | 1. Click "Set as Default" on selected rule chip -> calls `updateProfile`<br>2. Click "Set as Default" on selected group chip -> calls `updateProfile` |
| **AC 5** | Foreign key cascade cleanup (`ON DELETE SET NULL`) | Integration (JPA/Flyway) & E2E | `UserRepositoryTest.java` & `user-defaults.spec.ts` | P1 | 1. Delete group -> `default_group_id` resets to null on user<br>2. Delete custom rule -> `default_rule_configuration_id` resets to null on user<br>3. Match creation continues functioning without error |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend API Tests:** [`UserControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-2/UserControllerATDDTest.java) (6 test scenarios for GET/PATCH profile defaults, null-clearing, and ownership validation)
- **Frontend E2E Tests:** [`user-defaults.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/user-defaults.spec.ts) (4 end-to-end user journeys marked with `test.describe.skip()`)
- **Frontend Store Tests:** [`matchDraftStore.defaults.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-2/matchDraftStore.defaults.spec.ts) (4 store test scenarios marked with `describe.skip()`)
- **Frontend Component Tests:**
  - [`UserPreferencesSection.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-2/UserPreferencesSection.spec.ts) (3 component test scenarios)
  - [`RulePicker.defaults.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-2/RulePicker.defaults.spec.ts) (2 component test scenarios)
  - [`PlayerSelection.defaults.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-2/PlayerSelection.defaults.spec.ts) (2 component test scenarios)

## Next Steps (Task-by-Task Activation)

During implementation of Story 6.2 in `dev-story`:
1. **Task 1 (Database Migration & Domain Entity Updates):**
   - Create Flyway migration `V12__add_user_defaults.sql` adding `default_group_id` and `default_rule_configuration_id` with `ON DELETE SET NULL`.
   - Update `User` entity with UUID mappings and optimistic locking `@Version`.
   - Update `UserRepositoryTest.java` to verify persistence, retrieval, and DB-level `ON DELETE SET NULL` cascade behavior.
2. **Task 2 (Backend DTOs, Service Validation & Controller Integration):**
   - Move/activate `UserControllerATDDTest.java` into `src/test/java/com/tictactore/controller/`.
   - Update `ProfileDto`, `UpdateProfileRequest`, `UserService`, `UserServiceImpl`, and `UserController`.
   - Verify all unit & controller ATDD tests pass.
3. **Task 3 (Frontend Store & Service Updates):**
   - Move/activate `matchDraftStore.defaults.spec.ts` into `frontend/src/features/match/stores/__tests__/`.
   - Update `auth.ts` (`UserProfile` interface & `updateProfile` action) and `matchDraftStore.ts` (`fetchDefaults`, `setDefaultRule`, `setDefaultGroup`).
4. **Task 4 (Frontend UI Components & Inline Match Integration):**
   - Move/activate `UserPreferencesSection.spec.ts`, `RulePicker.defaults.spec.ts`, and `PlayerSelection.defaults.spec.ts` into corresponding `__tests__/` folders.
   - Implement `UserPreferencesSection.vue` and integrate into `Cabinet.vue` (keeping under 500 lines).
   - Add inline "Set as default" action triggers in `RulePicker.vue` and `PlayerSelection.vue`.
   - Add i18n keys to `en.json` and `de.json`.
5. **Task 5 (Testing & Quality Verification):**
   - Activate `frontend/e2e/user-defaults.spec.ts` (remove `test.describe.skip()`).
   - Run unit and E2E tests, then run full verification `./scripts/ci-local.sh`.
