---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-07-27'
storyId: '3.1'
storyKey: '3-1-confirmation-requests-and-push-notifications'
storyFile: '_bmad-output/implementation-artifacts/3-1-confirmation-requests-and-push-notifications.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-3-1-confirmation-requests-and-push-notifications.md'
generatedTestFiles:
  - 'src/test/java/com/tictactore/service/PushNotificationServiceATDDTest.java'
  - 'src/test/java/com/tictactore/controller/NotificationControllerATDDTest.java'
  - 'src/test/java/com/tictactore/service/MatchServiceDuplicateDetectionATDDTest.java'
  - 'frontend/src/features/match/composables/usePushNotifications.spec.ts'
  - 'frontend/src/features/match/composables/usePendingMatches.spec.ts'
  - 'frontend/e2e/tests/e2e/match-confirmation-push.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/3-1-confirmation-requests-and-push-notifications.md'
---

# ATDD Acceptance Test Scaffolding Checklist — Story 3.1: Confirmation Requests & Push Notifications

## 1. Preflight & Context Summary
- **Story Key:** `3-1-confirmation-requests-and-push-notifications`
- **Story ID:** `3.1`
- **Detected Stack:** `fullstack` (Spring Boot Backend + Vue 3/Playwright/Vitest Frontend)
- **Story File:** `_bmad-output/implementation-artifacts/3-1-confirmation-requests-and-push-notifications.md`
- **Backend Test Framework:** JUnit 5, Mockito, Spring Boot Test (`src/test/`)
- **Frontend Test Framework:** Vitest, Playwright E2E (`frontend/src/`, `frontend/e2e/`)

## 2. Generation Mode Selection
- **Mode:** AI Generation (scaffolding test files via strict specification mapping)

## 3. Test Strategy & Acceptance Criteria Mapping

| ID | Level | Priority | Target Component / Scenario | Verification Goal |
|---|---|---|---|---|
| TC-BE-01 | Backend Unit | P0 | `PushNotificationServiceATDDTest` | Web Push JSON payload structure (`matchId`, `creatorName`, `summary`, `isDuplicateWarning`, `timestamp`) |
| TC-BE-02 | Backend Unit | P0 | `PushNotificationServiceATDDTest` | Pseudonymization of retired creators (`ex-player-*` -> `"A retired player"`) |
| TC-BE-03 | Backend Integration | P0 | `PushNotificationServiceATDDTest` | Save audit entry in `NotificationLog` (`DELIVERED`, `QUEUED`, `FAILED`) |
| TC-BE-04 | Backend Unit | P0 | `PushNotificationServiceATDDTest` | Push delivery failure handling logged as `FAILED` without failing match creation |
| TC-BE-05 | Backend Integration | P0 | `MatchServiceDuplicateDetectionATDDTest` | Duplicate match detection on same UTC day with identical scores (`isDuplicateWarning = true`) |
| TC-BE-06 | Backend API | P0 | `NotificationControllerATDDTest` | REST endpoints `POST /subscribe` (201) and `DELETE /unsubscribe` (204) validation |
| TC-FE-01 | Frontend Unit | P0 | `usePushNotifications.spec.ts` | Explicit user gesture permission request and subscription dispatch |
| TC-FE-02 | Frontend Unit | P1 | `usePendingMatches.spec.ts` | Throttled visibility change listener (10s debounce) for `GET /api/v1/matches/pending` |
| TC-FE-03 | Frontend Component | P0 | `HomeView.spec.ts` | Reactive fallback badge counter (`pendingCount > 0`) & denied permission warning banner |
| TC-FE-04 | Frontend Router | P0 | `router.spec.ts` | Deep-link stub `/match/:id/review` resolution & stale notification handling |
| TC-E2E-01 | Playwright E2E | P0 | `match-confirmation-push.spec.ts` | Full verification loop: submission -> notification log -> fallback badge -> review deep-link |

## 4. TDD Red Phase Scaffolds Created

### Backend Java Test Files
- `src/test/java/com/tictactore/service/PushNotificationServiceATDDTest.java`
- `src/test/java/com/tictactore/controller/NotificationControllerATDDTest.java`
- `src/test/java/com/tictactore/service/MatchServiceDuplicateDetectionATDDTest.java`

### Frontend Test Files
- `frontend/src/features/match/composables/usePushNotifications.spec.ts`
- `frontend/src/features/match/composables/usePendingMatches.spec.ts`
- `frontend/e2e/tests/e2e/match-confirmation-push.spec.ts`

## 5. Validation & Next Steps
- ✅ All tests are created with `@Disabled` / `it.skip` / `test.skip` for TDD Red Phase.
- ✅ All assertions map strictly to Story 3.1 acceptance criteria.
- 🚀 **Recommended Next Workflow:** Run `bmad-dev-story` to implement Story 3.1 and activate these tests step-by-step.
