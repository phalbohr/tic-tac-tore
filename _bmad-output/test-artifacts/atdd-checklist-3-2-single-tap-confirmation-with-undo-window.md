---
stepsCompleted:
  - step-01-preflight-and-context
  - step-02-generation-mode
  - step-03-test-strategy
  - step-04-generate-tests
  - step-04c-aggregate
  - step-05-validate-and-complete
lastStep: step-05-validate-and-complete
lastSaved: '2026-07-29T14:40:00+02:00'
storyId: '3.2'
storyKey: 3-2-single-tap-confirmation-with-undo-window
storyFile: _bmad-output/implementation-artifacts/3-2-single-tap-confirmation-with-undo-window.md
atddChecklistPath: _bmad-output/test-artifacts/atdd-checklist-3-2-single-tap-confirmation-with-undo-window.md
generatedTestFiles:
  - src/test/java/com/tictactore/service/MatchConfirmationATDDTest.java
  - src/test/java/com/tictactore/controller/MatchConfirmationControllerATDDTest.java
  - frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts
inputDocuments:
  - _bmad-output/implementation-artifacts/3-2-single-tap-confirmation-with-undo-window.md
---

# ATDD Checklist: Story 3.2 - Single-tap Confirmation with Undo Window

## TDD Green Phase Status (Completed)

🟢 **Green-phase tests enabled and passing** (Scaffolds enabled, all unit, controller, and E2E specs passing).

### Enabled Specs:
1. **Backend Service Unit / Integration Spec**:
   - `src/test/java/com/tictactore/service/MatchConfirmationATDDTest.java`
2. **Backend Controller REST Endpoint Spec**:
   - `src/test/java/com/tictactore/controller/MatchConfirmationControllerATDDTest.java`
3. **Frontend E2E User Journey Spec**:
   - `frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts`

---

## Acceptance Criteria Traceability

| AC # | Acceptance Criterion | Test Spec Coverage | Priority | Status |
|---|---|---|---|---|
| AC 1, 2 | Opponent views pending match request & taps "Confirm" -> UI displays 15s "Undo" toast ("Match confirmed. Tap to undo.") | `match-confirmation-undo.spec.ts` | P0 | 🟢 Green (Passing) |
| AC 2, 3 | Tapping "Undo" within 15s cancels pending confirmation, restores pending request state, no HTTP call sent | `match-confirmation-undo.spec.ts` | P0 | 🟢 Green (Passing) |
| AC 3, 4 | Expiration of 15s timer sends `POST /api/v1/matches/{id}/confirm` with `CONFIRMED` status & optional `Idempotency-Key` | `MatchConfirmationControllerATDDTest.java`, `match-confirmation-undo.spec.ts` | P0 | 🟢 Green (Passing) |
| AC 5 | Network failure on timer expiry retains pending-sync state and retries idempotently | `match-confirmation-undo.spec.ts` | P1 | 🟢 Green (Passing) |
| AC 6 | Backend extracts caller UUID securely from Spring Security `SecurityContext`/JWT, validates opponent participation (not creator), performs optimistic locking (`@Version`) & idempotent updates | `MatchConfirmationATDDTest.java`, `MatchConfirmationControllerATDDTest.java` | P0 | 🟢 Green (Passing) |

---

## Task-by-Task Activation Plan

During implementation / verification:

1. **Task 1 (Backend DTOs, Model, Service, Controller)**:
   - Remove `@Disabled` from `MatchConfirmationATDDTest.java` and `MatchConfirmationControllerATDDTest.java`.
   - Ensure backend API endpoint `POST /api/v1/matches/{id}/confirm` validates opponent identity securely via `SecurityContext`.
   - Verify tests transition from RED (failing) -> GREEN (passing).

2. **Task 2 & 3 (Frontend State, Store, UI Components)**:
   - Implement `useConfirmationTimer.ts`, update `matchConfirmationStore.ts`, and integrate `<UndoToast>` in `HomeView.vue`.
   - Write Vitest unit tests for timer cancellation and execution.

3. **Task 4 (E2E Integration & Verification)**:
   - Remove `test.skip()` from `frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts`.
   - Run `./scripts/ci-local.sh` and Playwright E2E tests to verify complete end-to-end functionality.
