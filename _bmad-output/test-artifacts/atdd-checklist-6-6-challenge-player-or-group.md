---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-30T13:28:00+02:00'
storyId: '6.6'
storyKey: '6-6-challenge-player-or-group'
storyFile: '_bmad-output/implementation-artifacts/6-6-challenge-player-or-group.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-6-6-challenge-player-or-group.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-6-6/ChallengeControllerATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-6/ChallengeNotificationListenerTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-6/ChallengeServiceATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-6/ChallengeModal.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-6/PendingChallenges.spec.ts'
  - 'frontend/e2e/challenge-flow.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/6-6-challenge-player-or-group.md'
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

# Acceptance Test-Driven Development (ATDD) Checklist: Story 6.6

## Story Context
- **Story Key:** `6-6-challenge-player-or-group`
- **Story ID:** `6.6`
- **Title:** Story 6.6: Challenge Player or Group
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/6-6-challenge-player-or-group.md`

## Acceptance Criteria Summary
1. **AC 1 (Challenge Creation & Push Dispatch):** An authenticated player can initiate a challenge from the Leaderboard, Player Directory / Search, or Player Groups via `ChallengeModal.vue` (`1v1` / `2v2`, optional rule config, optional message max 255 chars). Submitting sends `POST /api/v1/challenges`. Backend validates no self-challenge, target exists, no active duplicate `PENDING` challenge. `match_challenge` record is saved with status `PENDING`. Upon commit, `ChallengeCreatedEvent` dispatches Web Push (`type: "CHALLENGE_RECEIVED"`) to the target player or group members (excluding challenger). `NotificationLog` entry is recorded with `challenge_id`.
2. **AC 2 (Pending Challenges Feed):** Pending incoming challenges (direct or group) and outgoing challenges are displayed on the Home Hub or Challenges list with challenger avatar, nickname, match type chip, rule template name, message, timestamp, and action buttons ("Accept" / "Decline").
3. **AC 3 (Challenge Acceptance & Match Launch):** Target player or group member taps "Accept" (`POST /api/v1/challenges/{id}/accept`). Backend validates status `PENDING` and authorization. Status updates to `ACCEPTED`. Upon commit, `ChallengeAcceptedEvent` dispatches Web Push (`type: "CHALLENGE_ACCEPTED"`) to challenger. Both users can launch match recording with pre-filled configuration.
4. **AC 4 (Decline and Cancel):** Target taps "Decline" (`POST /api/v1/challenges/{id}/decline`) -> transitions to `DECLINED`, `ChallengeDeclinedEvent` published. Challenger taps "Cancel" (`POST /api/v1/challenges/{id}/cancel`) -> transitions to `CANCELLED`.
5. **AC 5 (Service Worker Push & Deep Link):** Service Worker (`sw.js`) renders system push notification for `CHALLENGE_RECEIVED` and `CHALLENGE_ACCEPTED`, clicking navigates to target deep link (`/?tab=challenges` or `/?challengeId={id}`).
6. **AC 6 (Resilience & Audit Logging):** Push dispatch timeouts or VAPID errors do not fail the database transaction and are recorded in `NotificationLog` (`status: "FAILED"`).

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Well-defined REST contracts (`/api/v1/challenges`), domain entities (`MatchChallenge`), domain events (`ChallengeCreatedEvent`, `ChallengeAcceptedEvent`, `ChallengeDeclinedEvent`), decoupled event listeners with `@TransactionalEventListener(phase = AFTER_COMMIT)`, and existing Vitest & Playwright test suites.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Challenge creation, validations (self-challenge, duplicate pending), DB persistence & push event dispatch | Controller ATDD / Service ATDD / Listener ATDD | `ChallengeController`, `ChallengeService`, `ChallengeNotificationListener` | P0 | 1. Create 1v1 challenge successfully (201 Created)<br>2. Reject self-challenge (400 Bad Request)<br>3. Reject duplicate pending challenge (409 Conflict)<br>4. Dispatch `CHALLENGE_RECEIVED` push excluding challenger<br>5. Log push audit record with `challenge_id` |
| **AC 2** | Incoming & outgoing pending challenge feeds | Controller ATDD / Component Vitest | `ChallengeController`, `PendingChallenges.vue` | P0 | 1. `GET /api/v1/challenges/incoming` returns active pending challenges<br>2. `GET /api/v1/challenges/outgoing` returns active pending challenges<br>3. Component displays card details with avatar, format chip, and action buttons |
| **AC 3** | Challenge acceptance, authorization checks & push event | Controller ATDD / Service ATDD / Listener ATDD | `ChallengeController`, `ChallengeService`, `ChallengeNotificationListener` | P0 | 1. Accept challenge updates status to `ACCEPTED` (200 OK)<br>2. Reject unauthorized acceptance (403 Forbidden)<br>3. Reject acceptance of non-pending challenge (409 Conflict)<br>4. Dispatch `CHALLENGE_ACCEPTED` push notification to challenger |
| **AC 4** | Decline and cancel challenge actions | Controller ATDD / Service ATDD / Listener ATDD | `ChallengeController`, `ChallengeService`, `ChallengeNotificationListener` | P1 | 1. Decline challenge updates status to `DECLINED`<br>2. Cancel challenge updates status to `CANCELLED`<br>3. Dispatch `CHALLENGE_DECLINED` notification |
| **AC 5** | Service Worker push rendering & deep linking | Integration / E2E | `frontend/public/sw.js`, `challenge-flow.spec.ts` | P1 | 1. Service Worker renders system notification for `CHALLENGE_RECEIVED` and `CHALLENGE_ACCEPTED`<br>2. Notification click navigates to deep link |
| **AC 6** | Push network timeout / VAPID failure isolation | Unit / Listener ATDD | `ChallengeNotificationListener`, `PushNotificationService` | P0 | 1. Async push failures do not bubble exceptions or roll back DB transaction<br>2. Push failure recorded in `NotificationLog` with `status = FAILED` |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend Controller ATDD Tests:** [`ChallengeControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-6/ChallengeControllerATDDTest.java) (9 scenarios covering create, incoming/outgoing listings, accept, decline, cancel, self-challenge 400, duplicate pending 409, unauthorized accept 403, and not found 404)
- **Backend Event Listener ATDD Tests:** [`ChallengeNotificationListenerTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-6/ChallengeNotificationListenerTest.java) (4 scenarios covering individual target push, group member push excluding challenger, error suppression, acceptance push, and decline push)
- **Backend Service ATDD Tests:** [`ChallengeServiceATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-6/ChallengeServiceATDDTest.java) (6 scenarios covering business rules, state transitions to PENDING/ACCEPTED/DECLINED/CANCELLED, event publishing, self-challenge validation, and duplicate pending rejection)
- **Frontend Component Tests:**
  - [`ChallengeModal.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-6/ChallengeModal.spec.ts) (3 scenarios covering modal display, 1v1/2v2 selection, and store action dispatch)
  - [`PendingChallenges.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-6/PendingChallenges.spec.ts) (3 scenarios covering incoming challenge card rendering, accept action, and decline action)
- **Frontend E2E Tests:** [`challenge-flow.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/challenge-flow.spec.ts) (3 Playwright E2E scenarios covering challenge creation from Leaderboard, incoming challenge acceptance on Home Hub, and cancellation with `test.skip()`)

## Next Steps (Task-by-Task Activation)

During implementation of Story 6.6 in `dev-story`:
1. **Task 1 (Database Migration & Domain Entity Updates):**
   - Create Flyway migration `src/main/resources/db/migration/V15__create_match_challenges.sql`.
   - Create `ChallengeStatus.java`, `MatchChallenge.java`, update `NotificationLog.java` and `NotificationLogDto.java`.
   - Create `MatchChallengeRepository.java` and repository unit tests.
2. **Task 2 (Backend Challenge Service, Events & REST API):**
   - Create DTOs (`CreateChallengeRequest.java`, `ChallengeResponse.java`, `ChallengeActionResponse.java`).
   - Create Domain Events (`ChallengeCreatedEvent.java`, `ChallengeAcceptedEvent.java`, `ChallengeDeclinedEvent.java`).
   - Update `PushNotificationPayload.java`, `PushNotificationService.java`, `PushNotificationServiceImpl.java`.
   - Create `ChallengeNotificationListener.java`, `ChallengeService.java`, `ChallengeServiceImpl.java`, `ChallengeController.java`.
   - Activate and merge `ChallengeControllerATDDTest.java`, `ChallengeNotificationListenerTest.java`, and `ChallengeServiceATDDTest.java` into `src/test/java/com/tictactore/`.
3. **Task 3 (Frontend Challenge Components, Store & UI Integration):**
   - Create `frontend/src/services/challengeService.ts` and Pinia store `frontend/src/features/challenge/stores/useChallengeStore.ts`.
   - Create `ChallengeModal.vue` and `PendingChallenges.vue`.
   - Integrate challenge buttons into `LeaderboardView.vue`, `PlayerGroupSection.vue`, `PlayerSearchOverlay.vue`, and Home Hub widget in `HomeView.vue`.
   - Update `frontend/public/sw.js` and i18n locales (`en.json`, `de.json`).
   - Activate and merge `ChallengeModal.spec.ts` and `PendingChallenges.spec.ts` into `frontend/src/features/challenge/components/__tests__/`.
4. **Task 4 (E2E Testing & Quality Verification):**
   - Activate `frontend/e2e/challenge-flow.spec.ts` by removing `test.skip()`.
   - Run unit, component, and E2E tests, then execute `./scripts/ci-local.sh` for full verification.
