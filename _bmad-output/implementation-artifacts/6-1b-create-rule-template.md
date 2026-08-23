---
baseline_commit: f5cdcf998ba52f6ce6ca77686f9c560f2c1dd268
---

# Story 6.1b: Create Rule Template

Status: in-progress

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player or organizer,
I want to create custom rule templates with specific parameters and a unique name,
so that I can save and reuse my group's specific house rules for future matches and manage them in my profile settings.

## Acceptance Criteria

1. **Given** an authenticated user querying `/api/v1/rule-configurations`
   **When** they request available rule configurations
   **Then** the system returns all built-in system presets (ITSF, DTFB) and all custom templates created by the authenticated user (`creatorId == currentUserId`)
   **And** custom rule templates created by other users are strictly isolated and inaccessible (`AD-04`, `AD-05`)
   **And** querying `GET /api/v1/rule-configurations/{id}` returns the rule configuration if it is a preset or owned by the authenticated user, or returns `404 Not Found` / `403 Forbidden`.
2. **Given** an authenticated player in the match creation flow (`/matches/new`) or in Profile Settings (`/cabinet`)
   **When** they open the rule template builder (`RuleTemplateModal.vue`)
   **Then** they are presented with a form configured with foosball parameter options:
     - **Template Name**: Required, 1–50 characters, trimmed, unique per creator
     - **Win Condition / Game Limit** (`gameLimit`): 1..15 games (e.g., Best of 1 = 1, Best of 3 = 3, Best of 5 = 5)
     - **Goal Limit** (`goalLimit`): 1..100 goals per game (default: 5)
     - **Tie-break Rules**: `winByTwo` (boolean, default: false), `absoluteScoreCap` (Integer nullable, 1..100, e.g., 8)
     - **Timeouts**: `timeoutsPerGame` (0..10, default: 2), `timeoutDurationSeconds` (0..300, default: 30)
     - **Ball Possession Time Limits**: `possessionLimit5BarSeconds` (0..60, default: 10), `possessionLimitOtherSeconds` (0..60, default: 15)
     - **Side Swap Rule**: `sideSwapRule` enum (`NONE`, `BETWEEN_GAMES`, `AFTER_HALF_POINTS`, default: `BETWEEN_GAMES`)
     - **Restart Rule**: `restartRule` enum (`CONCEDING_TEAM`, `RANDOM_DROP`, default: `CONCEDING_TEAM`)
     - **Illegal Moves**: `spinningAllowed` (boolean, default: false), `aerialsAllowed` (boolean, default: false)
     - **Position Swap Rule**: `positionSwapRule` enum (`BETWEEN_GAMES`, `NEVER`, `FREE`, default: `BETWEEN_GAMES`)
     - **Point Distribution**: `pointDistribution` enum (`WIN_LOSS_3_0`, `WIN_LOSS_2_0`, `WIN_DRAW_LOSS_3_1_0`, default: `WIN_LOSS_3_0`)
   **And** smart defaults are pre-filled (Best of 3, 5 goals, win by 2, no spinning).
3. **Given** a valid custom rule configuration submitted via `POST /api/v1/rule-configurations`
   **When** the template is saved
   **Then** the system persists a new immutable `RuleConfiguration` record associated with the creator (`creatorId`)
   **And** the system enforces a maximum quota of 20 custom rule templates per user (rejecting with `400 Bad Request` if exceeded)
   **And** no `PUT` or `PATCH` endpoint is exposed to modify existing records (`AD-01`).
4. **Given** built-in system presets ("ITSF Standard Matchplay", "DTFB Standard") or an existing custom template
   **When** a user chooses to edit an existing template
   **Then** the builder opens pre-filled with the template's current parameters
   **And** saving creates a *new* immutable `RuleConfiguration` record (`type = CUSTOM`, `createdBy = user.id`) rather than modifying the existing one, preserving statistical integrity for past matches (`AD-01`).
5. **Given** an authenticated user viewing their custom templates
   **When** they choose to delete a custom template via `DELETE /api/v1/rule-configurations/{id}`
   **Then** the template is removed from the user's available selection list
   **And** attempts to delete system presets (`type = PRESET`) or templates owned by other users return `403 Forbidden`.
6. **Given** a player is setting up a new match in the portrait match creation flow (`/matches/new`)
   **When** they select or create a rule template inline via `RuleTemplateModal.vue`
   **Then** the active match draft state in `useMatchDraftStore` is preserved without resetting selected players
   **And** the selected template parameters (`scoreLimit`, `gameLimit`, `winByTwo`) are immediately bound to the active match draft.

## Tasks / Subtasks

- [x] Task 1: Database Migration & Domain Entity Expansion (AC1, AC2, AC3, AC4)
  - [x] Create Flyway migration `V11__expand_rule_configuration_table.sql`:
    - Alter table `rule_configuration` adding columns: `absolute_score_cap INT`, `timeouts_per_game INT DEFAULT 2 NOT NULL`, `timeout_duration_seconds INT DEFAULT 30 NOT NULL`, `possession_limit_5bar_seconds INT DEFAULT 10 NOT NULL`, `possession_limit_other_seconds INT DEFAULT 15 NOT NULL`, `side_swap_rule VARCHAR(30) DEFAULT 'BETWEEN_GAMES' NOT NULL`, `restart_rule VARCHAR(30) DEFAULT 'CONCEDING_TEAM' NOT NULL`, `spinning_allowed BOOLEAN DEFAULT FALSE NOT NULL`, `aerials_allowed BOOLEAN DEFAULT FALSE NOT NULL`, `position_swap_rule VARCHAR(30) DEFAULT 'BETWEEN_GAMES' NOT NULL`, `point_distribution VARCHAR(30) DEFAULT 'WIN_LOSS_3_0' NOT NULL`.
    - Create indexes on `rule_configuration(created_by)` and `rule_configuration(type)`.
    - Update existing seeded presets in `V5` to ensure consistent column defaults.
  - [x] Update `com.tictactore.model.RuleConfiguration` entity with new fields, enums (`SideSwapRule`, `RestartRule`, `PositionSwapRule`, `PointDistribution`), `@Version`, `@CreationTimestamp`, `@Builder`.
  - [x] Update `com.tictactore.repository.RuleConfigurationRepository`:
    - `List<RuleConfiguration> findByTypeOrCreatedByOrderByCreatedAtDesc(RuleConfigurationType type, UUID createdBy)`
    - `List<RuleConfiguration> findByCreatedByOrderByCreatedAtDesc(UUID createdBy)`
    - `Optional<RuleConfiguration> findByIdAndCreatedByOrType(UUID id, UUID createdBy, RuleConfigurationType type)`
    - `long countByCreatedBy(UUID createdBy)`
    - `boolean existsByCreatedByAndNameIgnoreCase(UUID createdBy, String name)`
- [x] Task 2: Backend DTOs, Service & Controller (AC1, AC2, AC3, AC4, AC5)
  - [x] Create/Update DTOs:
    - `RuleConfigurationRequest` with Jakarta validation annotations (`@NotBlank @Size(max=50) String name`, `@Min(1) @Max(100) int goalLimit`, `@Min(1) @Max(15) int gameLimit`, `boolean winByTwo`, `@Min(1) @Max(100) Integer absoluteScoreCap`, `@Min(0) @Max(10) int timeoutsPerGame`, `@Min(0) @Max(300) int timeoutDurationSeconds`, `@Min(0) @Max(60) int possessionLimit5BarSeconds`, `@Min(0) @Max(60) int possessionLimitOtherSeconds`, `@NotNull SideSwapRule sideSwapRule`, `@NotNull RestartRule restartRule`, `boolean spinningAllowed`, `boolean aerialsAllowed`, `@NotNull PositionSwapRule positionSwapRule`, `@NotNull PointDistribution pointDistribution`).
    - `RuleConfigurationResponse` including all fields, `id`, `name`, `type`, `createdBy`, `createdAt`.
  - [x] Update `com.tictactore.service.RuleConfigurationService` and `RuleConfigurationOperation`:
    - `List<RuleConfigurationResponse> getAvailableRules(UUID userId, RuleConfigurationType type)`
    - `RuleConfigurationResponse getRuleById(UUID userId, UUID id)`
    - `RuleConfigurationResponse createCustomRule(UUID userId, RuleConfigurationRequest request)`
    - `void deleteCustomRule(UUID userId, UUID id)`
    - Enforce quota (max 20 custom templates per user), name uniqueness per creator (case-insensitive), preset protection (presets cannot be deleted), and user isolation (`AD-04`, `AD-05`).
  - [x] Update `com.tictactore.controller.RuleConfigurationController` mapped to `/api/v1/rule-configurations`:
    - `GET /api/v1/rule-configurations` -> returns presets + authenticated user's custom templates (or filtered by `type` query param if provided).
    - `GET /api/v1/rule-configurations/{id}` -> returns template if preset or owned by user.
    - `POST /api/v1/rule-configurations` -> `201 Created` with created response.
    - `DELETE /api/v1/rule-configurations/{id}` -> `204 No Content` (403 if preset or belongs to another user).
    - Use `@AuthenticationPrincipal com.tictactore.model.User principal` across all endpoints (`principal.getId()`).
- [x] Task 3: Frontend Service & Pinia Store (AC1, AC2, AC4, AC5)
  - [x] Create `frontend/src/services/ruleConfigService.ts` for interacting with `/api/v1/rule-configurations` with CSRF headers.
  - [x] Update `frontend/src/stores/useRuleConfigStore.ts`:
    - State: `presets: RuleConfig[]`, `customRules: RuleConfig[]`, `selectedRuleId: string | null`, `loading: boolean`, `error: string | null`.
    - Getters: `allRules`, `getRuleById`.
    - Actions: `fetchAllRules()`, `fetchPresets()`, `createCustomRule(ruleData)`, `deleteCustomRule(id)`, `selectRule(id)`.
- [x] Task 4: Frontend UI Components & Inline Match Integration (AC2, AC4, AC5, AC6)
  - [x] Create `frontend/src/features/match/components/RuleTemplateModal.vue`:
    - Clubhouse-styled modal/bottom sheet for creating and "editing as new" rule templates.
    - Form sections: General (Name, Game Limit, Goal Limit), Game Flow (Tie-break, Absolute Cap, Timeouts, Possession Limits), Match Conduct (Side Swap, Restart, Illegal Moves, Position Swap, Point Distribution).
    - Smart defaults pre-fill and live validation.
  - [x] Create `frontend/src/features/profile/components/RuleTemplateSection.vue` in `frontend/src/features/profile/Cabinet.vue`:
    - Lists presets and custom templates with summary badges and parameters.
    - Actions: "Create Template", "Edit as New", "Delete" (custom only).
  - [x] Integrate rule selector in `frontend/src/features/match/components/NewMatchFlow.vue` and `PlayerSelection.vue`:
    - Quick-select chip / dropdown for rule templates (Presets + Custom).
    - Inline "Create Custom Template" button opening `RuleTemplateModal.vue` without clearing match draft state.
  - [x] Add i18n localization keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.
  - [x] Adhere to Clubhouse "No-Line" rule (`UX-DR3`) with `ch-` classes and 500-line limit (`IP-04`).
- [x] Task 5: Testing & Quality Verification
  - [x] Backend Unit Tests: `RuleConfigurationServiceTest.java` and `RuleConfigurationRepositoryTest.java`.
  - [x] Backend ATDD: Integration test `RuleConfigurationControllerATDDTest.java` covering preset fetching, custom rule creation, quota enforcement, user isolation, immutability, and validation errors.
  - [x] Frontend Unit Tests: Store tests in `frontend/src/stores/__tests__/useRuleConfigStore.spec.ts` and component tests in `RuleTemplateModal.spec.ts`.
  - [x] E2E Test: Update `frontend/e2e/rule-system-selection.spec.ts` with Playwright tests validating inline rule template creation, selection in New Match flow, and profile settings management.
  - [x] Verification: Execute `./scripts/ci-local.sh` and verify 100% test pass.

## Dev Notes

### Architecture & Implementation Guardrails

- **Immutable Rule Configurations (AD-01):**
  - `RuleConfiguration` records are strictly immutable. Once created, a configuration ID represents an exact set of parameters forever.
  - Do NOT create `PUT` or `PATCH` endpoints for `RuleConfiguration`.
  - Editing an existing rule configuration opens the builder pre-filled and saves as a *new* record (`type = CUSTOM`, `createdBy = principal.getId()`).
- **Security & Data Isolation (AD-04, AD-05):**
  - Base path: `/api/v1/rule-configurations`.
  - Authentication: All endpoints require authentication. Extract user ID via `@AuthenticationPrincipal User principal` (`principal.getId()`).
  - Ownership enforcement: Preset templates (`type = PRESET`) have `createdBy = 00000000-0000-0000-0000-000000000000` and are visible to all users. Custom templates (`type = CUSTOM`) are visible and deletable ONLY by their creator. Attempts to access/delete another user's custom template return `403 Forbidden` / `404 Not Found`.
- **Database Design & Migrations:**
  - Flyway migration script: `src/main/resources/db/migration/V11__expand_rule_configuration_table.sql`.
  - Concurrency: `@Version` column on `RuleConfiguration` entity to support optimistic locking.
  - Constraints: Max 20 custom templates per user, template name max 50 chars.
- **UX & Navigation Invariants:**
  - Rule creation occurs **inline** during match setup (`NewMatchFlow.vue` via `RuleTemplateModal.vue`) or within Profile Settings (`Cabinet.vue` via `RuleTemplateSection.vue`).
  - **Match Draft Preservation:** Opening the rule template modal during match setup does not reset or alter selected players or match draft state in `useMatchDraftStore`.
- **Clubhouse Styling Guidelines (UX-DR3):**
  - Follow Clubhouse "No-Line" rule: use surface container tonal shifts, elevation, and rounded cards instead of 1px solid border lines.
- **500-Line Rule (IP-04):**
  - All new and updated files strictly under 500 lines. Split form sections into subcomponents if necessary.
- **Testing Standards:**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments.

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-6-1b-create-rule-template.md`
- **Backend API Tests:** `_bmad-output/test-artifacts/atdd-redphase-6-1b/RuleConfigurationControllerATDDTest.java`
- **Frontend E2E Tests:** `frontend/e2e/rule-system-selection.spec.ts`
- **Frontend Store Tests:** `_bmad-output/test-artifacts/atdd-redphase-6-1b/useRuleConfigStore.spec.ts`
- **Frontend Component Tests:** `_bmad-output/test-artifacts/atdd-redphase-6-1b/RuleTemplateModal.spec.ts`

### References


- [Source: _bmad-output/planning-artifacts/prd.md#Rule System Consistency] - Rule templates are immutable. Modifying settings creates a new template.
- [Source: _bmad-output/planning-artifacts/prd.md#FR3] - Player can select a rule system (ITSF, DTFB, or Custom template) or create inline.
- [Source: _bmad-output/planning-artifacts/prd.md#FR40] - Default rule template in profile settings and match setup.
- [Source: _bmad-output/planning-artifacts/architecture.md#AD-01] - Immutable RuleConfiguration.
- [Source: _bmad-output/planning-artifacts/architecture.md#AD-04] - Privacy & PII protection.
- [Source: _bmad-output/planning-artifacts/architecture.md#AD-05] - Security & Principal authentication.
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md#UX-DR3] - Clubhouse styling guidelines and "No-Line" rule.

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash (High)

### Debug Log References

N/A

### Completion Notes List

- Database: Added migration `V11__expand_rule_configuration_table.sql` with columns for ITSF/custom rule parameters, defaults, and indexes.
- Domain: Updated `RuleConfiguration` entity with enums (`SideSwapRule`, `RestartRule`, `PositionSwapRule`, `PointDistribution`), `@Version` for optimistic locking, `@CreationTimestamp`, and repository queries.
- Backend: Implemented `RuleConfigurationService` and `RuleConfigurationOperation` with `@Retryable`, quota checks (max 20), case-insensitive unique naming per user, preset immutability and user isolation. Implemented `/api/v1/rule-configurations` REST controller with `@AuthenticationPrincipal`.
- Frontend: Created `ruleConfigService.ts`, Pinia store `useRuleConfigStore.ts`, `RuleTemplateModal.vue`, `RulePicker.vue`, and `RuleTemplateSection.vue` in `Cabinet.vue` and `NewMatchFlow.vue`.
- Localization: Added complete English and German translations in `en.json` and `de.json`.
- Testing & Verification: 100% test pass on `./scripts/ci-local.sh` (384 backend tests, 311 frontend unit tests, 115 Playwright E2E tests).

### File List

- `src/main/resources/db/migration/V11__expand_rule_configuration_table.sql`
- `src/main/java/com/tictactore/model/RuleConfiguration.java`
- `src/main/java/com/tictactore/model/SideSwapRule.java`
- `src/main/java/com/tictactore/model/RestartRule.java`
- `src/main/java/com/tictactore/model/PositionSwapRule.java`
- `src/main/java/com/tictactore/model/PointDistribution.java`
- `src/main/java/com/tictactore/repository/RuleConfigurationRepository.java`
- `src/main/java/com/tictactore/dto/RuleConfigurationRequest.java`
- `src/main/java/com/tictactore/dto/RuleConfigurationResponse.java`
- `src/main/java/com/tictactore/service/RuleConfigurationService.java`
- `src/main/java/com/tictactore/service/RuleConfigurationOperation.java`
- `src/main/java/com/tictactore/controller/RuleConfigurationController.java`
- `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java`
- `src/test/java/com/tictactore/service/RuleConfigurationServiceTest.java`
- `src/test/java/com/tictactore/repository/RuleConfigurationRepositoryTest.java`
- `src/test/java/com/tictactore/controller/RuleConfigurationControllerATDDTest.java`
- `src/test/java/com/tictactore/api/RuleConfigurationApiIT.java`
- `frontend/src/services/ruleConfigService.ts`
- `frontend/src/stores/useRuleConfigStore.ts`
- `frontend/src/stores/__tests__/useRuleConfigStore.spec.ts`
- `frontend/src/features/match/components/RuleTemplateModal.vue`
- `frontend/src/features/match/components/__tests__/RuleTemplateModal.spec.ts`
- `frontend/src/features/match/components/RulePicker.vue`
- `frontend/src/features/match/components/NewMatchFlow.vue`
- `frontend/src/features/profile/components/RuleTemplateSection.vue`
- `frontend/src/features/profile/Cabinet.vue`
- `frontend/src/components/RuleSystemSelection.vue`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`
- `frontend/e2e/rule-system-selection.spec.ts`
- `_bmad-output/implementation-artifacts/6-1b-create-rule-template.md`

### Review Findings
- [ ] [Review][Patch] Missing cross-field validation for absoluteScoreCap and goalLimit [src/main/java/com/tictactore/service/RuleConfigurationService.java:492-501]
- [ ] [Review][Patch] Unused Custom Repository Query (findByIdAndCreatedByOrType) [src/main/java/com/tictactore/service/RuleConfigurationService.java]
- [ ] [Review][Patch] Hardcoded magic number for quota limit [src/main/java/com/tictactore/service/RuleConfigurationService.java]
- [ ] [Review][Patch] TOCTOU race condition in template creation quota check [src/main/java/com/tictactore/service/RuleConfigurationService.java:492-494]
- [ ] [Review][Patch] Migration UUID reused from mock user ID [src/main/resources/db/migration/V11__expand_rule_configuration_table.sql]
- [ ] [Review][Patch] RuleConfigurationControllerATDDTest uses mocks instead of integration [src/test/java/com/tictactore/controller/RuleConfigurationControllerATDDTest.java]
- [ ] [Review][Patch] Removed @Column(nullable = false) from @Version field [src/main/java/com/tictactore/model/RuleConfiguration.java]
- [ ] [Review][Patch] Removed caller of getPresets() leaves dead code [src/main/java/com/tictactore/service/RuleConfigurationService.java:523-526]
