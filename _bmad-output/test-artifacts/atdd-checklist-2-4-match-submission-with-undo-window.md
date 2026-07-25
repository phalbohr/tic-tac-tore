---
stepsCompleted:
  - step-01-preflight-and-context
  - step-02-generation-mode
  - step-03-test-strategy
  - step-04-generate-tests
  - step-04c-aggregate
  - step-05-validate-and-complete
lastStep: step-05-validate-and-complete
lastSaved: '2026-07-25T16:15:59+02:00'
storyId: '2.4'
storyKey: 2-4-match-submission-with-undo-window
storyFile: _bmad-output/implementation-artifacts/2-4-match-submission-with-undo-window.md
atddChecklistPath: _bmad-output/test-artifacts/atdd-checklist-2-4-match-submission-with-undo-window.md
generatedTestFiles:
  - src/test/java/com/tictactore/service/MatchServiceATDDTest.java
  - src/test/java/com/tictactore/controller/MatchControllerATDDTest.java
  - frontend/e2e/tests/e2e/match-submission-undo.spec.ts
inputDocuments:
  - _bmad-output/implementation-artifacts/2-4-match-submission-with-undo-window.md
---

# ATDD Checklist: Story 2.4 - Match Submission with Undo Window

## TDD Red Phase Status (Current)

🔴 **Red-phase test scaffolds generated and skipped** (`@Disabled` / `test.skip()`).

### Generated Scaffolds:
1. **Backend Service Unit / Integration Spec**:
   - `src/test/java/com/tictactore/service/MatchServiceATDDTest.java`
2. **Backend Controller REST Endpoint Spec**:
   - `src/test/java/com/tictactore/controller/MatchControllerATDDTest.java`
3. **Frontend E2E User Journey Spec**:
   - `frontend/e2e/tests/e2e/match-submission-undo.spec.ts`

---

## Acceptance Criteria Traceability

| AC # | Acceptance Criterion | Test Spec Coverage | Priority | Status |
|---|---|---|---|---|
| AC 1, 2 | Tapping "Complete Match" displays 15s Undo Toast on Home Hub | `match-submission-undo.spec.ts` | P0 | 🔴 Red (Skipped) |
| AC 3 | Tapping "Undo" within 15s cancels submission, closes toast & restores `ScoreEntry.vue` state | `match-submission-undo.spec.ts` | P0 | 🔴 Red (Skipped) |
| AC 4 | Expiration of 15s timer sends `POST /api/v1/matches` with `PENDING_APPROVAL` & idempotency key | `MatchControllerATDDTest.java`, `match-submission-undo.spec.ts` | P0 | 🔴 Red (Skipped) |
| AC 5 | Network failure marks match as "Pending sync" and displays retry toast | `match-submission-undo.spec.ts` | P1 | 🔴 Red (Skipped) |
| AC 6 | Immutable after creation, 4 distinct players validation | `MatchServiceATDDTest.java` | P1 | 🔴 Red (Skipped) |

---

## Task-by-Task Activation Plan

During implementation (e.g. using `dev-story` / `bmad-agent-dev`):

1. **Task 1 (Backend DTOs, Service, Controller)**:
   - Remove `@Disabled` from `MatchServiceATDDTest.java` and `MatchControllerATDDTest.java`.
   - Implement `MatchService`, `MatchOperation` (`@Idempotent` + `@Transactional`), and `MatchController`.
   - Verify tests transition from RED (failing) -> GREEN (passing).

2. **Task 2 & 3 (Frontend Composable, Store, UI Toast)**:
   - Implement `useSubmissionTimer.ts`, update `matchDraftStore.ts`, and create `UndoToast.vue`.
   - Write Vitest unit tests for store timer and fetch cleanup.

3. **Task 4 (E2E Integration & Verification)**:
   - Remove `test.skip()` from `frontend/e2e/tests/e2e/match-submission-undo.spec.ts`.
   - Run `./scripts/ci-local.sh` and Playwright E2E tests to verify end-to-end functionality.
