---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-09-03T13:15:00+02:00'
workflowType: 'testarch-atdd'
storyId: '8.6'
storyKey: '8-6-tournament-rule-system-enforcement'
storyFile: '_bmad-output/implementation-artifacts/8-6-tournament-rule-system-enforcement.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-8-6-tournament-rule-system-enforcement.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-8-6/CreateMatchRequestTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-6/TournamentMatchResponseTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-6/TournamentMatchValidatorTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-6/TournamentMatchQueryServiceTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-6/RulePicker.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-6/NewMatchFlow.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-6/matchDraftStore.spec.ts'
  - 'frontend/e2e/tests/api/tournament-rule-enforcement.spec.ts'
  - 'frontend/e2e/tournament-rule-enforcement.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/8-6-tournament-rule-system-enforcement.md'
  - '.agent/skills/bmad-testarch-atdd/resources/tea-index.csv'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 8.6

## Story Context
- **Story Key:** `8-6-tournament-rule-system-enforcement`
- **Story ID:** `8.6`
- **Title:** Story 8.6: Tournament Rule System Enforcement
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/8-6-tournament-rule-system-enforcement.md`

## Acceptance Criteria Summary
1. **AC 1 (Rule Picker Locking in Match Entry Flow):** When match entry is initiated from a tournament or navigated to `/matches/new` with tournament query parameters (`tournamentId`, `tournamentMatchId`, `ruleConfigId`), the rule system is locked to `ruleConfigId` (`FR45`). Lock icon/badge is displayed, other rule chips are disabled (`pointer-events-none`), custom rule & set as default buttons are hidden, and an informative notice is shown.
2. **AC 2 (Format & Roster Locking):** Match format (1v1 or 2v2) is locked and assigned participants from `TournamentMatch` are pre-populated into Team A / Team B slots. Arbitrary player replacement is disabled.
3. **AC 3 (Draft Submission Payload):** Match draft submission payload includes `tournamentMatchId`, `ruleConfigId`, standard player IDs, and games.
4. **AC 4 (CreateMatchRequest DTO Enhancement):** `CreateMatchRequest.java` record includes `UUID ruleConfigId` and overloaded backward-compatible constructors.
5. **AC 5 (Strict Backend Rule Enforcement & Concurrency Validation):** `POST /api/v1/matches` with `tournamentMatchId` validates tournament is `IN_PROGRESS`, verifies `ruleConfigId` strictly matches `tournament.ruleConfiguration.id` (throwing `409 Conflict` / `TournamentRuleMismatchException` on mismatch), and validates participants match `TournamentMatch`.
6. **AC 6 (Dynamic Game Limit Scoring Constraints):** Score and game count validation respects `ruleConfig.gameLimit` rather than hardcoding 3 games, rejecting violations with `400 Bad Request`.
7. **AC 7 (Tournament Match DTO Rule Attribution):** `TournamentMatchResponse` includes `UUID ruleConfigurationId` and `String ruleConfigurationName` populated from tournament rule configuration.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, well-defined backend contracts, Vue components, and architectural patterns aligned with `code-1-guide` and `code-2-test`.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Rule Picker Locking | Component (Vitest) & E2E (Playwright) | `RulePicker.spec.ts`, `tournament-rule-enforcement.spec.ts` | P0 | 1. Rule chip locked with lock icon and "Tournament Rule" badge<br>2. Other rule chips disabled (`pointer-events-none`)<br>3. Custom rule button and set default button hidden<br>4. Notice banner visible |
| **AC 2** | Format & Roster Locking | Component (Vitest) | `NewMatchFlow.spec.ts` | P0 | 1. Format locked to tournament mode<br>2. Roster pre-populated with assigned players<br>3. Arbitrary player substitution disabled |
| **AC 3** | Draft Submission Payload | Unit / Pinia Store (Vitest) | `matchDraftStore.spec.ts` | P0 | 1. `setTournamentContext` stores context<br>2. `isTournamentMatch` computed true<br>3. `submitMatchDraft` emits `ruleConfigId` and `tournamentMatchId` |
| **AC 4** | CreateMatchRequest Record | Unit (JUnit 5) | `CreateMatchRequestTest.java` | P1 | 1. Record constructor with `ruleConfigId`<br>2. Overloaded backward-compatible constructors preserve existing callers |
| **AC 5** | Strict Rule Enforcement | Unit & WebMvc Slice | `TournamentMatchValidatorTest.java`, `tournament-rule-enforcement.spec.ts` | P0 | 1. Valid ruleConfigId & participants passes validation<br>2. RuleConfigId mismatch throws 409 Conflict (`TournamentRuleMismatchException`)<br>3. Participant mismatch throws 409 Conflict (`TournamentConflictException`)<br>4. Tournament not in progress throws 409 Conflict |
| **AC 6** | Dynamic Game Limit Validation | Unit & Service | `MatchServiceTest.java` / `TournamentMatchValidatorTest.java` | P1 | 1. Game count checked against rule config `gameLimit`<br>2. Rejection with 400 Bad Request on score/limit violation |
| **AC 7** | Match DTO Rule Attribution | Unit & Slice | `TournamentMatchResponseTest.java`, `TournamentMatchQueryServiceTest.java` | P1 | 1. `TournamentMatchResponse` contains `ruleConfigurationId` and `ruleConfigurationName`<br>2. Query service maps fields from parent tournament |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend DTO Unit Tests:** [`CreateMatchRequestTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-6/CreateMatchRequestTest.java)
- **Backend DTO Response Tests:** [`TournamentMatchResponseTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-6/TournamentMatchResponseTest.java)
- **Backend Validator Unit Tests:** [`TournamentMatchValidatorTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-6/TournamentMatchValidatorTest.java)
- **Backend Query Service Tests:** [`TournamentMatchQueryServiceTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-6/TournamentMatchQueryServiceTest.java)
- **Frontend Pinia Store Tests (Vitest):** [`matchDraftStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-6/matchDraftStore.spec.ts)
- **Frontend Component Tests (Vitest):** [`RulePicker.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-6/RulePicker.spec.ts)
- **Frontend Flow Tests (Vitest):** [`NewMatchFlow.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-6/NewMatchFlow.spec.ts)
- **Frontend API Tests (Playwright):** [`frontend/e2e/tests/api/tournament-rule-enforcement.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/api/tournament-rule-enforcement.spec.ts) (marked with `test.skip()`)
- **Frontend E2E Tests (Playwright):** [`frontend/e2e/tournament-rule-enforcement.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tournament-rule-enforcement.spec.ts) (marked with `test.skip()`)

## Next Steps (Task-by-Task Activation)

During implementation of Story 8.6 in `bmad-dev-story`:
1. **Task 1: Backend DTOs & Entity Enhancements (AC4, AC7)**
   - Update `CreateMatchRequest.java` (add `ruleConfigId` and overloaded constructors).
   - Update `TournamentMatchResponse.java` (add `ruleConfigurationId`, `ruleConfigurationName`).
   - Update `TournamentMatchQueryServiceImpl.java` and `TournamentMatchServiceImpl.java`.
   - Move and activate `CreateMatchRequestTest.java`, `TournamentMatchResponseTest.java`, and `TournamentMatchQueryServiceTest.java`.
   - Verify unit tests turn GREEN.
2. **Task 2: Backend Tournament Validation & Match Service Integration (AC5, AC6)**
   - Create `TournamentRuleMismatchException.java` (extends `TournamentConflictException` -> HTTP 409).
   - Create `TournamentMatchValidator.java` and `TournamentMatchValidatorImpl.java` to prevent `MatchServiceImpl.java` from exceeding 500 lines.
   - Inject `TournamentMatchValidator` into `MatchServiceImpl.java`.
   - Move and activate `TournamentMatchValidatorTest.java`.
   - Verify unit tests turn GREEN.
3. **Task 3: Frontend Store & Draft Submission Payload (AC2, AC3)**
   - Update `frontend/src/features/tournament/types/tournament.ts`.
   - Update `frontend/src/features/match/stores/matchDraftStore.ts` (`isTournamentMatch`, `setTournamentContext`, payload `ruleConfigId`).
   - Move and activate `matchDraftStore.spec.ts` into `frontend/src/features/match/stores/__tests__/`.
   - Verify unit tests turn GREEN.
4. **Task 4: Frontend RulePicker & Match Entry Flow Locking (AC1, AC2)**
   - Update `frontend/src/features/match/components/RulePicker.vue` (lock badge, disable chips, hide custom & default buttons, notice banner).
   - Update `frontend/src/features/match/components/NewMatchFlow.vue` and `TournamentsView.vue`.
   - Move and activate `RulePicker.spec.ts` and `NewMatchFlow.spec.ts`.
   - Unskip and verify Playwright API & E2E tests.
5. **Task 5: Verification & Full CI Run**
   - Execute `./scripts/ci-local.sh` and ensure 100% pass rate.
