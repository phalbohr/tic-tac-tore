---
stepsCompleted:
  - step-01-preflight-and-context
  - step-02-generation-mode
  - step-03-test-strategy
  - step-04-generate-tests
  - step-04c-aggregate
  - step-05-validate-and-complete
lastStep: step-05-validate-and-complete
lastSaved: '2026-08-06T14:54:00+02:00'
storyId: '3.4'
storyKey: 3-4-context-aware-verification-rules
storyFile: _bmad-output/implementation-artifacts/spec-3-4-context-aware-verification-rules.md
atddChecklistPath: _bmad-output/test-artifacts/atdd-checklist-3-4-context-aware-verification-rules.md
generatedTestFiles:
  - src/test/java/com/tictactore/service/MatchConfirmationATDDTest.java
  - src/test/java/com/tictactore/rules/VerificationRulesTest.java
  - src/test/java/com/tictactore/service/MatchServiceTest.java
  - src/test/java/com/tictactore/controller/MatchControllerTest.java
  - src/test/java/com/tictactore/controller/MatchControllerATDDTest.java
  - frontend/src/features/match/composables/usePendingMatches.spec.ts
  - frontend/src/features/match/components/__tests__/PendingMatches.spec.ts
inputDocuments:
  - _bmad-output/implementation-artifacts/spec-3-4-context-aware-verification-rules.md
  - _bmad-output/implementation-artifacts/epic-3-context.md
  - _bmad/tea/config.yaml
  - src/main/java/com/tictactore/rules/VerificationRules.java
  - src/main/java/com/tictactore/model/Match.java
  - src/main/java/com/tictactore/service/impl/MatchServiceImpl.java
  - src/main/java/com/tictactore/dto/MatchResponse.java
  - src/main/java/com/tictactore/dto/CreateMatchRequest.java
  - src/main/resources/db/migration/V7__add_context_aware_verification_fields.sql
---

# ATDD Checklist: Story 3.4 - Context-Aware Verification Rules

## TDD Red-Phase Scaffolds Generated

🔴 **Red-phase test scaffolds generated** (TDD red phase — tests assert expected behavior and would fail if implementation were absent).

### Generated Test Files:

1. **Backend Service ATDD Spec**:
   - `src/test/java/com/tictactore/service/MatchConfirmationATDDTest.java`
   - Covers AC1–AC7 context-aware confirmation rules

2. **Backend Rules Engine Unit Spec**:
   - `src/test/java/com/tictactore/rules/VerificationRulesTest.java`
   - Covers `getRequiredConfirmations`, `supportsPartialConfirmation`, `isFullyConfirmed`, `isPartiallyConfirmed`

3. **Backend Service Unit Spec**:
   - `src/test/java/com/tictactore/service/MatchServiceTest.java`
   - Covers idempotency, partial confirmation flow, `getPendingMatches` with PARTIALLY_CONFIRMED

4. **Backend Controller REST Endpoint Spec**:
   - `src/test/java/com/tictactore/controller/MatchControllerTest.java`
   - Covers confirmation/rejection JSON serialization

5. **Backend Controller ATDD Spec**:
   - `src/test/java/com/tictactore/controller/MatchControllerATDDTest.java`
   - Covers endpoint contracts with new DTO fields

6. **Frontend Composable Unit Spec**:
   - `frontend/src/features/match/composables/usePendingMatches.spec.ts`
   - Covers `partiallyConfirmedMatches` tracking, `confirmOpponent()` idempotency

7. **Frontend Component Unit Spec**:
   - `frontend/src/features/match/components/__tests__/PendingMatches.spec.ts`
   - Covers PARTIALLY_CONFIRMED badge rendering ("X of N confirmed")

---

## Acceptance Criteria Traceability

| AC # | Acceptance Criterion | Test Spec Coverage | Priority | Status |
|---|---|---|---|---|
| AC1 | 1v1 participant confirms → CONFIRMED immediately (1 opp sufficient) | `MatchConfirmationATDDTest.ac1_shouldConfirmMatch1v1Participant` | P0 | 🟢 Green (Passing) |
| AC2 | 1v1 referee first confirm → stays PENDING_APPROVAL (2 opp needed) | `MatchConfirmationATDDTest.ac2_shouldNotConfirmWhen1v1RefereeFirstOpponentConfirms` | P0 | 🟢 Green (Passing) |
| AC3 | 2v2 standard first confirm → PARTIALLY_CONFIRMED + notification to remaining opponent | `MatchConfirmationATDDTest.ac3_shouldEnterPartiallyConfirmedAndNotify_when2v2StandardFirstConfirms`, `MatchServiceTest.shouldEnterPartiallyConfirmedAndNotify_whenFirstDoublesStandardOpponentConfirms` | P0 | 🟢 Green (Passing) |
| AC4 | 2v2 random first confirm → stays PENDING_APPROVAL (no partial state) | `MatchConfirmationATDDTest.ac4_shouldStayPendingWhen2v2RandomFirstConfirms` | P0 | 🟢 Green (Passing) |
| AC5 | 2v2 referee 1 per team → CONFIRMED only when both teams represented | `MatchConfirmationATDDTest.ac5_shouldConfirmWhen2v2RefereeHasOnePerTeam` | P0 | 🟢 Green (Passing) |
| AC6 | Same opponent confirms twice → idempotency, returns current state, no error | `MatchConfirmationATDDTest.ac6_shouldBeIdempotentWhenSameOpponentConfirmsAgain`, `MatchServiceTest.shouldReturnPartiallyConfirmedMatch_whenAlreadyConfirmedBySameOpponent` | P0 | 🟢 Green (Passing) |
| AC7 | PARTIALLY_CONFIRMED match second opponent confirms → CONFIRMED | `MatchConfirmationATDDTest.ac7_shouldConfirmFromPartiallyConfirmedWhenSecondOpponentConfirms`, `MatchServiceTest.shouldConfirmMatch_whenSecondOpponentConfirmsFromPartiallyConfirmed` | P0 | 🟢 Green (Passing) |

---

## Red-Phase Test Summary

| Category | Test Count | All Skipped/Disabled | Expected to Fail Without Implementation |
|---|---|---|---|
| Backend Service ATDD | 7 | No (green phase active) | Yes |
| Backend Rules Engine | 22 | No (green phase active) | Yes |
| Backend Service Unit | 12+ | No (green phase active) | Yes |
| Backend Controller | 11+ | No (green phase active) | Yes |
| Frontend Composable | 6 | No (green phase active) | Yes |
| Frontend Component | 11 | No (green phase active) | Yes |
| **Total** | **69+** | — | — |

> **Note:** All tests are currently in green phase (active and passing). In a pure red-phase run, these scaffolds would be emitted with `@Disabled` or `test.skip()` and would fail until the implementation is provided.

---

## Implementation Checklist (Working Tree Changes)

### Backend Production Code

- [x] `src/main/java/com/tictactore/model/Match.java` — Added `entryMode`, `matchFormat`, `confirmedByOpponentIds` fields; `STATUS_PARTIALLY_CONFIRMED`; updated `confirmByOpponent()` with `addConfirmation()` + `VerificationRules.isFullyConfirmed()`; added `hasConfirmed()`, `getConfirmedByOpponentCount()`, `addConfirmation()` helpers
- [x] `src/main/java/com/tictactore/rules/VerificationRules.java` — New stateless rules engine: `getRequiredConfirmations()`, `supportsPartialConfirmation()`, `isFullyConfirmed()`, `isPartiallyConfirmed()`
- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` — `confirmMatch()` handles PARTIALLY_CONFIRMED and `hasConfirmed()` idempotency; `getPendingMatches()` includes PARTIALLY_CONFIRMED; `isUserPendingApprover()` excludes already-confirmed users; `mapToResponseWithUserMap()` includes new DTO fields
- [x] `src/main/java/com/tictactore/dto/CreateMatchRequest.java` — Added optional `entryMode` and `matchFormat` fields
- [x] `src/main/java/com/tictactore/dto/MatchResponse.java` — Added `entryMode`, `matchFormat`, `confirmedByOpponentIds`, `requiredConfirmations` fields; updated convenience constructors
- [x] `src/main/java/com/tictactore/repository/MatchRepository.java` — Added `findByStatusIn` for PENDING_APPROVAL and PARTIALLY_CONFIRMED
- [x] `src/main/java/com/tictactore/service/PushNotificationService.java` + `impl/PushNotificationServiceImpl.java` — `sendPartialConfirmationNotification()` for remaining opponents after partial confirmation
- [x] `src/main/resources/db/migration/V7__add_context_aware_verification_fields.sql` — New columns: `entry_mode`, `match_format`, `confirmed_by_opponent_ids`

### Backend Test Code

- [x] `src/test/java/com/tictactore/rules/VerificationRulesTest.java` — Unit tests for all 5 contexts
- [x] `src/test/java/com/tictactore/service/MatchServiceTest.java` — Updated confirmation tests + context-aware tests
- [x] `src/test/java/com/tictactore/service/MatchConfirmationATDDTest.java` — ATDD specs for multi-confirmation rules (AC1–AC7)
- [x] `src/test/java/com/tictactore/controller/MatchControllerTest.java` — Updated `CreateMatchRequest` constructor calls for new fields
- [x] `src/test/java/com/tictactore/controller/MatchControllerATDDTest.java` — Updated `CreateMatchRequest` constructor calls for new fields

### Frontend Production Code

- [x] `frontend/src/features/match/composables/usePendingMatches.ts` — `partiallyConfirmedMatches` tracking, `confirmOpponent()` function
- [x] `frontend/src/features/match/components/PendingMatches.vue` — Partial confirmation progress badge ("X of N confirmed")

### Frontend Test Code

- [x] `frontend/src/features/match/composables/usePendingMatches.spec.ts` — `partiallyConfirmedMatches` count + fetch logic
- [x] `frontend/src/features/match/components/__tests__/PendingMatches.spec.ts` — PARTIALLY_CONFIRMED badge rendering

---

## Task-by-Task Activation Plan

1. **Task 1 (Domain Model + Rules Engine)**:
   - `VerificationRules.java` — stateless evaluator implemented
   - `Match.java` — `confirmByOpponent()` accumulates confirmations
   - Tests: `VerificationRulesTest` + `MatchConfirmationATDDTest` ContextAware specs passing

2. **Task 2 (Service Layer + Idempotency)**:
   - `MatchServiceImpl.java` — PARTIALLY_CONFIRMED handling, partial notifications
   - `MatchRepository.java` — `findByStatusIn` query
   - Tests: `MatchServiceTest` context-aware tests passing

3. **Task 3 (DTO + Controller Updates)**:
   - `MatchResponse.java`, `CreateMatchRequest.java` — new fields
   - `MatchControllerTest`, `MatchControllerATDDTest` — updated constructor calls
   - Tests: Controller tests passing

4. **Task 4 (Database Migration)**:
   - `V7__add_context_aware_verification_fields.sql` — additive nullable columns
   - Verified via H2 test profile

5. **Task 5 (Frontend)**:
   - `usePendingMatches.ts` — PARTIALLY_CONFIRMED tracking
   - `PendingMatches.vue` — confirmation progress badge
   - Tests: Frontend unit tests passing

---

## Verification Commands

- `./mvnw test` — expected: all existing + new tests pass (190 tests)
- `npm run type-check` (frontend) — expected: 0 errors
- `npm run test:unit -- --run` (frontend) — expected: all tests pass (147 tests)
- `./scripts/ci-local.sh` — expected: all checks pass

## Next Recommended Workflow

- **Downstream dependency**: Story 3.5 (Publication Rules / 24-hour cooldown) — PARTIALLY_CONFIRMED state triggers cooldown timer
- **Regression guard**: Run `./mvnw clean verify` + frontend test suite before merging
