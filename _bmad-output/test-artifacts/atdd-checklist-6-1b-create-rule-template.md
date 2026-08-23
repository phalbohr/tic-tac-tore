---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-23T21:49:25+02:00'
storyId: '6.1b'
storyKey: '6-1b-create-rule-template'
storyFile: '_bmad-output/implementation-artifacts/6-1b-create-rule-template.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-6-1b-create-rule-template.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-6-1b/RuleConfigurationControllerATDDTest.java'
  - 'frontend/e2e/rule-system-selection.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-1b/useRuleConfigStore.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-1b/RuleTemplateModal.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/6-1b-create-rule-template.md'
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

# Acceptance Test-Driven Development (ATDD) Checklist: Story 6.1b

## Story Context
- **Story Key:** `6-1b-create-rule-template`
- **Story ID:** `6.1b`
- **Title:** Story 6.1b: Create Rule Template
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/6-1b-create-rule-template.md`

## Acceptance Criteria Summary
1. **AC 1:** Given an authenticated user querying `/api/v1/rule-configurations`, when they request available rule configurations, then the system returns all built-in system presets (ITSF, DTFB) and all custom templates created by the authenticated user (`creatorId == currentUserId`), and custom rule templates created by other users are strictly isolated and inaccessible (`AD-04`, `AD-05`), and `GET /api/v1/rule-configurations/{id}` returns the template or `404 Not Found` / `403 Forbidden`.
2. **AC 2:** Given an authenticated player in match creation (`/matches/new`) or Profile Settings (`/cabinet`), when they open the rule template builder (`RuleTemplateModal.vue`), then they are presented with a form configured with foosball parameters (Template Name 1..50 chars, `gameLimit` 1..15, `goalLimit` 1..100, `winByTwo`, `absoluteScoreCap`, `timeoutsPerGame`, `timeoutDurationSeconds`, `possessionLimit5BarSeconds`, `possessionLimitOtherSeconds`, `sideSwapRule`, `restartRule`, `spinningAllowed`, `aerialsAllowed`, `positionSwapRule`, `pointDistribution`) and smart defaults pre-filled.
3. **AC 3:** Given a valid custom rule configuration submitted via `POST /api/v1/rule-configurations`, when the template is saved, then the system persists a new immutable `RuleConfiguration` record associated with the creator (`creatorId`), enforces a max quota of 20 custom templates per user (rejecting with `400 Bad Request` if exceeded), and no `PUT` or `PATCH` endpoint is exposed (`AD-01`).
4. **AC 4:** Given built-in presets or an existing custom template, when a user chooses to edit an existing template, then the builder opens pre-filled with current parameters, and saving creates a *new* immutable `RuleConfiguration` record (`type = CUSTOM`, `createdBy = user.id`) rather than modifying the existing one.
5. **AC 5:** Given an authenticated user viewing their custom templates, when they choose to delete a custom template via `DELETE /api/v1/rule-configurations/{id}`, then the template is removed from available selection, and attempts to delete system presets (`type = PRESET`) or templates owned by other users return `403 Forbidden`.
6. **AC 6:** Given a player setting up a new match in portrait mode (`/matches/new`), when they select or create a rule template inline via `RuleTemplateModal.vue`, then the active match draft state in `useMatchDraftStore` is preserved without resetting selected players, and parameters are bound to the match draft.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, well-defined immutable REST contracts (`AD-01`), strict ownership isolation (`AD-04`, `AD-05`), and existing patterns from Story 6.1 and earlier epics.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Available rule configs & data isolation (`AD-04`, `AD-05`) | API / Integration | `RuleConfigurationController` & `RuleConfigurationService` | P0 | 1. `GET /api/v1/rule-configurations` returns presets + user custom templates<br>2. Filter by `type=PRESET`<br>3. `GET /api/v1/rule-configurations/{id}` returns template or 403 on foreign template |
| **AC 2** | Foosball parameter form & smart defaults | Component (Vitest) / E2E | `RuleTemplateModal.vue` & `rule-system-selection.spec.ts` | P0 | 1. Form inputs render with smart defaults (Best of 3, 5 goals, win by 2, no spinning)<br>2. Parameter bounds and required name validation (1..50 chars) |
| **AC 2** | Pinia store state management for rules | Unit (Vitest) | `useRuleConfigStore.ts` | P0 | 1. `fetchAllRules()` populates presets and customRules<br>2. `createCustomRule()` and `deleteCustomRule()` mutate store state<br>3. `selectedRule` getter |
| **AC 3** | Persist immutable custom template & Quota (max 20) | API / Integration | `RuleConfigurationController` & `RuleConfigurationService` | P0 | 1. `POST /api/v1/rule-configurations` returns 201 Created with immutable record<br>2. Max 20 custom templates per user -> 400 Bad Request<br>3. Duplicate template name for same creator -> 400 Bad Request |
| **AC 4** | "Edit as New" template cloning | E2E (Playwright) / Component | `RuleTemplateModal.vue` & `Cabinet.vue` | P1 | 1. Opening existing preset/custom template pre-fills builder form<br>2. Saving submits `POST` creating new `CUSTOM` record without mutating original |
| **AC 5** | Delete custom rule template & preset protection | API / Integration & E2E | `RuleConfigurationController` & `Cabinet.vue` | P0 | 1. `DELETE /api/v1/rule-configurations/{id}` returns 204 No Content for owned rule<br>2. DELETE on system preset returns 403 Forbidden<br>3. DELETE on another user's rule returns 403 Forbidden |
| **AC 6** | Inline creation in `/matches/new` without draft reset | E2E (Playwright) | `NewMatchFlow.vue` & `useMatchDraftStore` | P0 | 1. Opening/creating rule template inline in New Match flow preserves selected players and draft state |
| **UX** | Clubhouse No-Line styling compliance (`UX-DR3`) | E2E (Playwright) | `frontend/e2e/rule-system-selection.spec.ts` | P2 | 1. Verify modal container uses surface container tonal elevation with 0px solid border |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend API Tests:** [`RuleConfigurationControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-1b/RuleConfigurationControllerATDDTest.java) (9 test scenarios across GET, POST, DELETE, quota, duplicate names, presets)
- **Frontend E2E Tests:** [`rule-system-selection.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/rule-system-selection.spec.ts) (4 test scenarios marked with `test.skip()`)
- **Frontend Store Tests:** [`useRuleConfigStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-1b/useRuleConfigStore.spec.ts) (6 store test cases)
- **Frontend Component Tests:** [`RuleTemplateModal.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-1b/RuleTemplateModal.spec.ts) (4 modal component test cases)

## Next Steps (Task-by-Task Activation)

During implementation of Story 6.1b in `dev-story`:
1. **Task 1 (Database Migration & Domain Entity Expansion):**
   - Create Flyway migration `V11__expand_rule_configuration_table.sql`.
   - Update `RuleConfiguration` entity with new fields, enums (`SideSwapRule`, `RestartRule`, `PositionSwapRule`, `PointDistribution`), and `@Version`.
   - Update `RuleConfigurationRepository` with custom lookup methods and `existsByCreatedByAndNameIgnoreCase`.
2. **Task 2 (Backend DTOs, Service & Controller):**
   - Move/activate `RuleConfigurationControllerATDDTest.java` into `src/test/java/com/tictactore/controller/`.
   - Update `RuleConfigurationRequest`, `RuleConfigurationResponse`, `RuleConfigurationService`, and `RuleConfigurationController`.
   - Verify all unit & controller ATDD tests pass.
3. **Task 3 (Frontend Service & Pinia Store):**
   - Move/activate `useRuleConfigStore.spec.ts` into `frontend/src/stores/__tests__/`.
   - Update `frontend/src/services/ruleConfigService.ts` and `frontend/src/stores/useRuleConfigStore.ts`.
4. **Task 4 (Frontend UI Components & Inline Match Integration):**
   - Move/activate `RuleTemplateModal.spec.ts` into `frontend/src/features/match/components/__tests__/`.
   - Implement `RuleTemplateModal.vue`, `RuleTemplateSection.vue` in `Cabinet.vue`, and inline rule selection in `NewMatchFlow.vue` without resetting `useMatchDraftStore`.
   - Add localization strings in `en.json` and `de.json`.
5. **Task 5 (Testing & Quality Verification):**
   - Activate `frontend/e2e/rule-system-selection.spec.ts` (remove `test.skip()`).
   - Run unit and E2E tests, then run full verification `./scripts/ci-local.sh`.
