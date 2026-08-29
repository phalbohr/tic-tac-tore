---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-29T13:31:00+02:00'
storyId: '6.5'
storyKey: '6-5-pool-notifications'
storyFile: '_bmad-output/implementation-artifacts/6-5-pool-notifications.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-6-5-pool-notifications.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-6-5/PoolNotificationListenerTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-5/PushNotificationServiceATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-5/UserControllerATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-5/UserPreferencesSection.spec.ts'
  - 'frontend/e2e/pool-notifications.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/6-5-pool-notifications.md'
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

# Acceptance Test-Driven Development (ATDD) Checklist: Story 6.5

## Story Context
- **Story Key:** `6-5-pool-notifications`
- **Story ID:** `6.5`
- **Title:** Story 6.5: Pool Notifications
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/6-5-pool-notifications.md`

## Acceptance Criteria Summary
1. **AC 1:** Given a user creates a new matchmaking pool (`status == OPEN`), when the pool creation transaction commits, then the backend publishes a `PoolCreatedEvent` and asynchronously dispatches Web Push notifications (`type: "POOL_CREATED"`, title `"New Matchmaking Pool"`, summary `"A new {1v1|2v2} pool is looking for players"`, url `"/"`) to all users with active push subscriptions and `poolNotificationsEnabled == true`, strictly excluding the creator (FR37). A `NotificationLog` entry is recorded for each recipient with status `DELIVERED`, `FAILED`, or `SKIPPED`.
2. **AC 2:** Given an open pool reaching full capacity upon a user joining, when the pool status transitions to `FILLED` and the join transaction commits, then the backend publishes a `PoolFilledEvent` and asynchronously dispatches Web Push notifications (`type: "POOL_FILLED"`, title `"Pool Filled!"`, summary `"Your {1v1|2v2} pool is full — head to the table!"`, url `"/"`) to all registered participants in the pool (FR37). A `NotificationLog` entry is recorded for each participant with status `DELIVERED`, `FAILED`, or `SKIPPED`.
3. **AC 3:** Given the client Service Worker (`sw.js`) receives a Web Push event, when the payload contains `type: "POOL_CREATED"` or `type: "POOL_FILLED"`, then the Service Worker renders a system notification with corresponding title, summary, and action, and clicking focuses the app window and navigates to target URL (`/`).
4. **AC 4:** Given an authenticated user managing preferences in Profile Settings (`/cabinet`), when they view the "Default Match Preferences" section (`UserPreferencesSection.vue`), then a "Matchmaking Pool Notifications" toggle switch is displayed (defaulting to enabled / `true`). Toggling and saving updates `poolNotificationsEnabled` via `PATCH /api/v1/profile/me`. If `false`, the user is excluded from new pool creation notifications.
5. **AC 5:** Given a push service network timeout, VAPID configuration issue, or invalid subscription endpoint, when a pool is created or filled, then the pool creation/join transaction commits without interruption, and the error is logged in `NotificationLog` (`status: "FAILED"`) without throwing exceptions to the caller.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear event contracts (`PoolCreatedEvent`, `PoolFilledEvent`), transactional event listener isolation requirements (`AFTER_COMMIT`), profile preferences REST endpoints, and existing Vitest & Playwright testing harnesses.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Pool creation push dispatch, creator exclusion & audit log | Unit / ATDD | `PoolNotificationListener` & `PushNotificationService` | P0 | 1. Query subscribers with `poolNotificationsEnabled = true` excluding creator<br>2. Format `POOL_CREATED` payload with title, summary, url `/`<br>3. Pseudonymize retired creators<br>4. Record `NotificationLog` entries with `pool_id` |
| **AC 2** | Pool filled push dispatch to all participants | Unit / ATDD | `PoolNotificationListener` & `PushNotificationService` | P0 | 1. Query all participants on `PoolFilledEvent`<br>2. Format `POOL_FILLED` payload with title, summary, url `/`<br>3. Record `NotificationLog` entries with `pool_id` |
| **AC 3** | Service Worker push rendering & deep linking | Integration / E2E | `frontend/public/sw.js` | P1 | 1. Service Worker displays push notification for `POOL_CREATED` and `POOL_FILLED`<br>2. Clicking focuses window and navigates to `/` |
| **AC 4** | Pool notification preferences in Cabinet & REST API | Controller ATDD / Component Vitest / E2E | `UserController`, `UserPreferencesSection.vue`, `pool-notifications.spec.ts` | P0 | 1. `GET /api/v1/profile/me` exposes `poolNotificationsEnabled` (default `true`)<br>2. `PATCH /api/v1/profile/me` updates `poolNotificationsEnabled`<br>3. Component toggle switch renders and updates store<br>4. E2E journey persists preference |
| **AC 5** | Network/VAPID error resilience without breaking pool flow | Unit / ATDD | `PoolNotificationListener` & `PushNotificationService` | P0 | 1. External push failures do not throw exceptions to caller<br>2. Failure logged as `FAILED` in `NotificationLog` |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend Listener Tests:** [`PoolNotificationListenerTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-5/PoolNotificationListenerTest.java) (5 scenarios covering event dispatch, subscriber filtering, creator exclusion, participant notification, and error suppression)
- **Backend Service ATDD Tests:** [`PushNotificationServiceATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-5/PushNotificationServiceATDDTest.java) (6 scenarios covering pool push payload contracts, pseudonymization, audit logs with `pool_id`, and delivery failure handling)
- **Backend Controller ATDD Tests:** [`UserControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-5/UserControllerATDDTest.java) (3 scenarios covering GET profile default and PATCH preference update to true/false)
- **Frontend Component Tests:** [`UserPreferencesSection.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-5/UserPreferencesSection.spec.ts) (2 component scenarios covering toggle rendering, default state, and store action dispatch)
- **Frontend E2E Tests:** [`pool-notifications.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/pool-notifications.spec.ts) (2 Playwright E2E scenarios covering toggle interactions and reload persistence with `test.skip()`)

## Next Steps (Task-by-Task Activation)

During implementation of Story 6.5 in `dev-story`:
1. **Task 1 (Database Migration & Domain Entity Updates):**
   - Create Flyway migration `src/main/resources/db/migration/V14__add_pool_notifications.sql`.
   - Update `NotificationLog.java`, `NotificationLogDto.java`, `User.java`, `ProfileDto.java`, `UpdateProfileRequest.java`, `UserRepository.java`.
   - Activate and merge `UserControllerATDDTest.java` into `src/test/java/com/tictactore/controller/UserControllerATDDTest.java`.
2. **Task 2 (Backend Event-Driven Notification System):**
   - Create `PoolCreatedEvent.java`, `PoolFilledEvent.java`, `PoolNotificationListener.java`.
   - Update `PoolServiceImpl.java` with `eventPublisher.publishEvent`.
   - Update `PushNotificationPayload.java`, `PushNotificationService.java`, `PushNotificationServiceImpl.java`, `UserServiceImpl.java`.
   - Activate and merge `PoolNotificationListenerTest.java` and `PushNotificationServiceATDDTest.java` into `src/test/java/com/tictactore/`.
3. **Task 3 (Frontend Service Worker & User Preferences UI):**
   - Update `frontend/public/sw.js` for `POOL_CREATED` and `POOL_FILLED` payloads.
   - Update `frontend/src/stores/auth.ts` with `poolNotificationsEnabled`.
   - Update `frontend/src/features/profile/components/UserPreferencesSection.vue` with toggle switch.
   - Update i18n in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.
   - Activate and merge `UserPreferencesSection.spec.ts` into `frontend/src/features/profile/components/__tests__/`.
4. **Task 4 (Testing & Quality Verification):**
   - Activate `frontend/e2e/pool-notifications.spec.ts` by removing `test.skip()`.
   - Run unit, component, and E2E tests, then execute `./scripts/ci-local.sh` for full verification.
