---
baseline_commit: 251fc1a86f20e36d71223f7118909aafbac016e1
---

# Story 3.1: Confirmation Requests & Push Notifications

## 📖 Story Foundation
**User Story:** As a player who just submitted a match, I want the system to notify opponents, so that they can verify the results.
**Epic:** Epic 3: The Verification Loop & Trust Architecture
**Status:** done

**Acceptance Criteria:**
- **Given** a match has been successfully submitted by the creator (or 15-second local undo window in `useSubmissionTimer` has expired without cancellation)
- **When** the backend saves the match in `PENDING_APPROVAL` status via `POST /api/v1/matches`
- **Then** the system identifies all required opponents who must verify the match (in 1v1: the opposing player; in 2v2: both players on Team B; if submitter is an observer: at least 1 player from each team per PRD FR12/FR55)
- **And** before dispatching notifications, the backend performs **Duplicate Match Detection** (PRD line 789) by checking for existing matches (confirmed or pending) on the same calendar day (UTC) with identical participants and game scores; if a duplicate is detected, the notification payload is tagged with a visible warning label (`isDuplicateWarning: true`) to prevent blind approval
- **And** the system generates an asynchronous Web Push notification JSON payload matching the exact contract: `{"matchId": "uuid", "creatorName": "string", "summary": "string", "isDuplicateWarning": boolean, "timestamp": "ISO-8601"}` (formatting pseudonymized deleted creators like `ex-player-0042` as `"A retired player"`) and dispatches it via VAPID to all registered push subscription endpoints for required opponents
- **And** the system records an immutable audit entry in the database (`NotificationLog` entity per PRD line 814) capturing timestamp, recipient ID, match ID, delivery status (`DELIVERED`, `QUEUED`, or `FAILED`), and payload for dispute resolution
- **And** on the frontend, a Service Worker (`sw.js`) receives the Web Push event, displays an OS/browser push notification with action context, and handles click events (`notificationclick`) by deep-linking the user directly to `/match/:id/review` (or My Matches Pending tab), with a registered routing stub in `src/router/index.ts` resolving cleanly without 404s
- **And** if a user navigates to `/match/:id/review` for a stale notification (match already confirmed, rejected, or processed by another opponent), the UI gracefully displays an informational `"Already confirmed"` or `"Match processed"` state instead of throwing an error
- **And** if push notification permission was denied or delivery fails (PRD line 802), the pending confirmation remains prominently accessible via an in-app badge counter on the Home Hub and My Matches screen upon next application open
- **And** if push notification permission is revoked mid-session or across sessions, the application detects `Notification.permission === 'denied'` upon returning to foreground and surfaces a non-blocking banner prompting the user to re-enable notifications

---

## 🎯 Developer Context & Guardrails

### ATDD Artifacts
- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-3-1-confirmation-requests-and-push-notifications.md`
- **Backend Tests:** `src/test/java/com/tictactore/service/PushNotificationServiceATDDTest.java`, `src/test/java/com/tictactore/controller/NotificationControllerATDDTest.java`, `src/test/java/com/tictactore/service/MatchServiceDuplicateDetectionATDDTest.java`
- **Frontend Unit Tests:** `frontend/src/features/match/composables/usePushNotifications.spec.ts`, `frontend/src/features/match/composables/usePendingMatches.spec.ts`
- **Playwright E2E Tests:** `frontend/e2e/tests/e2e/match-confirmation-push.spec.ts`

### 1. Goal & Sequencing
- **Goal:** Build the infrastructure and workflow for asynchronous peer verification by implementing VAPID Web Push subscription management, duplicate match detection, audit logging, service worker notification handlers, and in-app fallback badges.
- **Trigger:** Transition of a newly submitted match to `PENDING_APPROVAL` status after the creator's 15-second local undo window elapses.
- **Sequencing Note:** This story is the entry point for Epic 3 (The Verification Loop). It establishes the notification and audit trail infrastructure that feeds directly into Story 3.2 (Match Confirmation & Cooldown Queue) and Story 3.3 (Match Rejection & Dispute Handling).

### 2. Architecture & Data Integrity Guardrails (Backend `code-1-guide` Compliance)
- **Three-Layer Transaction Architecture (Rule 3):**
  - **Outer Service (`PushNotificationServiceImpl` / `MatchServiceImpl`):** Annotated with `@Retryable` ONLY. Orchestrates validation, duplicate checking, and calls atomic database operations. NEVER combine `@Retryable` and `@Transactional` on the same method or class.
  - **Inner Operation (`NotificationOperation`):** Annotated with `@Idempotent` + `@Transactional`. Handles saving `PushSubscription` and `NotificationLog` entities in clean, isolated Spring transactions.
- **Optimistic Locking & Indexing (Rule 2):** Mutable JPA entities (`PushSubscription`, `NotificationLog`) MUST declare `@Version Long version;` (or `Integer` wrapper). Do NOT use primitive `long` and do NOT map the column explicitly (`@Column` is forbidden on version fields). Ensure table indexes are defined: `@Index(name = "idx_push_sub_user_id", columnList = "user_id")` and `@Index(name = "idx_notif_log_match_recipient", columnList = "match_id, recipient_id")`.
- **Tell, Don't Ask & JPA Best Practices (Rules 5, 9):** Domain rules and status transitions belong inside entities. Always capture and return the instance returned by `repository.save()`.
- **Strict DTO Boundary, Layering & Documentation (Rules 7, 11, 12, `code-4-document`):**
  - Never pass raw entities across REST boundaries. Create and use `PushSubscriptionRequest`, `PushNotificationPayload`, and `NotificationLogDto` in `src/main/java/com/tictactore/dto/`.
  - Controllers validate input (`@Valid`) and delegate to Service. Controllers MUST NEVER perform `null` checks on service responses to return 404s; domain exceptions must be thrown by services and handled by `GlobalExceptionHandler`.
  - **Zero Comments Policy:** Ordinary code gets zero comments/Javadoc. Add documentation ONLY where mandated: OpenAPI annotations (`@Operation`, `@ApiResponse`) on `NotificationController` endpoints and clean Therapi-style Javadoc on DTO records.
- **Web Push / VAPID Infrastructure & Test Environment Parity:**
  - Use `nl.martijndwars:web-push` with BouncyCastle to sign and encrypt payloads using VAPID keys.
  - Define a configuration class `VapidProperties` with safe fallback dummy keys in `application.properties` for local and test profiles (`./scripts/ci-local.sh`). Spring context must start cleanly without requiring environment variables.
  - Catch network exceptions gracefully; log delivery attempts as `FAILED` or `SIMULATED` in `NotificationLog`, and never fail the core match creation transaction due to push delivery errors.
  - Ensure exact JSON contract alignment: `{"matchId": "uuid", "creatorName": "string", "summary": "string", "isDuplicateWarning": boolean, "timestamp": "ISO-8601"}`. Pseudonymized accounts (`ex-player-*`) must be formatted as `"A retired player"`.

### 3. Frontend State, Service Worker & Routing Guardrails
- **Service Worker & VAPID Subscription:**
  - Create `frontend/public/sw.js` to listen for `push` events, display OS notifications with rich formatting, and handle `notificationclick` events to focus/navigate the client window to `/match/:id/review`.
  - In `usePushNotifications.ts`, tie `Notification.requestPermission()` strictly to explicit user gestures (e.g., CTA click in profile/onboarding) to prevent browser auto-block policies. Run passive monitoring (`checkPermissionState()`) on launch and tab resume.
- **Routing Stub (`src/router/index.ts`):**
  - Register a placeholder route for `/match/:id/review` (and `/matches` Pending tab) in `src/router/index.ts` resolving to a simple review stub or `HomeView` with pending query state so deep-links resolve cleanly without 404 errors during testing. Handle stale notifications by displaying an `"Already confirmed"` state.
- **Fallback In-App Badging & Throttled Polling:**
  - In `usePendingMatches.ts` (or `matchDraftStore.ts`), query `GET /api/v1/matches/pending` on launch and on `visibilitychange`. Apply a 10-second debounce/throttle on visibility resume to prevent server spam during rapid tab switching.
  - Display a reactive badge count on Home Hub and "My Matches" navigation items. If `permissionState === 'denied'`, display a non-blocking informational banner with instructions to re-enable notifications.

### 4. UI/UX & Design System Compliance
- **No-Line Rule (UX-DR3):** All notification banners, badge chips, and permission prompts must use color fills (`bg-surface-container-highest`, `bg-error-container`, `text-on-error-container`, etc.) for visual separation. Zero 1px borders (`border`, `border-gray-200`, `divide-y`, etc. are strictly forbidden).
- **Mobile-First Portrait Optimization:** In-app warning banners and badge counters must be designed for 360px portrait mobile screens with minimum touch targets of 56x56dp for interactive buttons.
- **500-Line Rule (IP-04):** Keep store files and components strictly under 500 lines. Extract Service Worker communication, VAPID key conversion, and permission monitoring into clean composables (`usePushNotifications.ts`, `usePendingMatches.ts`).

---

## 🛠️ Tasks / Subtasks

- [x] **Task 1: Backend Push Subscription & Audit Log Entities, DTOs & Repositories** *(AC: 5, 6)*
  - [x] Add Web Push dependency (`nl.martijndwars:web-push:5.1.1` and `org.bouncycastle:bcprov-jdk18on`) to `pom.xml`.
  - [x] Create DTOs in `src/main/java/com/tictactore/dto/`: `PushSubscriptionRequest.java`, `PushNotificationPayload.java` (matching exact JSON schema with `isDuplicateWarning` and `timestamp`), and `NotificationLogDto.java`. Add Therapi-style Javadoc on records.
  - [x] Create JPA entities in `src/main/java/com/tictactore/model/`:
    - `PushSubscription.java`: fields `id`, `userId`, `endpoint`, `p256dh`, `auth`, `createdAt`, `@Version Long version`. Add table index on `user_id`.
    - `NotificationLog.java`: fields `id`, `recipientId`, `matchId`, `type`, `payload`, `status` (`"DELIVERED"`, `"QUEUED"`, `"FAILED"`), `errorMessage`, `sentAt`, `@Version Long version`. Add table index on `match_id, recipient_id`.
  - [x] Create Spring Data JPA repositories in `src/main/java/com/tictactore/repository/`: `PushSubscriptionRepository.java` and `NotificationLogRepository.java`.
  - [x] Create atomic operation class `NotificationOperation.java` in `src/main/java/com/tictactore/service/operation/` annotated with `@Idempotent` + `@Transactional` to handle saving subscriptions and audit logs.

- [x] **Task 2: Backend Push Notification Service & REST Controller** *(AC: 5, 6)*
  - [x] Create `VapidProperties.java` in `src/main/java/com/tictactore/config/` with safe fallback dummy keys for test profiles (`application.properties`).
  - [x] Create `PushNotificationService.java` and `PushNotificationServiceImpl.java` in `src/main/java/com/tictactore/service/impl/` annotated with `@Retryable` ONLY:
    - Implement `subscribe(UUID userId, PushSubscriptionRequest request)` and `unsubscribe(UUID userId, String endpoint)`.
    - Implement `sendConfirmationRequest(Match match, List<User> opponents, boolean isDuplicateWarning)` to build exact JSON payloads (formatting pseudonymized `ex-player-*` creators as `"A retired player"`), encrypt via Web Push library, dispatch, and record results in `NotificationLog`.
    - Ensure delivery failures are caught, logged as `FAILED`, and do NOT roll back the calling match transaction.
  - [x] Create `NotificationController.java` in `src/main/java/com/tictactore/controller/` exposing `POST /api/v1/notifications/subscribe` (`201 Created` / `200 OK`) and `DELETE /api/v1/notifications/unsubscribe` (`204 No Content`), complete with OpenAPI annotations (`@Operation`, `@ApiResponse`).
  - [x] Add unit and integration tests (`PushNotificationServiceTest.java`, `NotificationControllerTest.java`) verifying subscription persistence, VAPID payload serialization, fallback keys, exception resilience, and audit log creation.

- [x] **Task 3: Duplicate Match Detection & Match Creation Integration** *(AC: 1, 2, 3, 4)*
  - [x] Update `MatchRepository.java` with a custom query method: `findMatchesByDateAndParticipants(Instant startOfDay, Instant endOfDay, UUID p1, UUID p2, UUID p3, UUID p4)` (checking both pending and confirmed matches on the same UTC calendar day).
  - [x] Update `MatchServiceImpl.createMatch()`:
    - After successfully saving the match in `PENDING_APPROVAL` status via `matchOperation.saveMatch()`, identify all required opponent `User` entities.
    - Query `MatchRepository` for duplicates on the same UTC day with identical game scores.
    - Set `boolean isDuplicateWarning = !duplicates.isEmpty();` and invoke `pushNotificationService.sendConfirmationRequest(savedMatch, opponents, isDuplicateWarning)`.
  - [x] Write tests in `MatchServiceTest.java` and `MatchServiceATDDTest.java` verifying duplicate detection on identical UTC matches sets `isDuplicateWarning = true` and records immutable audit logs.

- [x] **Task 4: Frontend Service Worker, Routing Stub & VAPID Subscription Composable** *(AC: 5, 7, 8)*
  - [x] Create `frontend/public/sw.js` with Web Push event listener (`self.addEventListener('push', ...)`) parsing the exact JSON schema and showing OS notifications, and click listener (`self.addEventListener('notificationclick', ...)`) navigating to `/match/${data.matchId}/review`.
  - [x] Update `frontend/src/router/index.ts` to register a routing stub for `/match/:id/review` (and `/matches` Pending tab) rendering a basic placeholder view that gracefully displays `"Already confirmed"` if a match status is no longer pending.
  - [x] Create `frontend/src/features/match/composables/usePushNotifications.ts`:
    - Provide reactive state: `isSupported`, `permissionState`, `isSubscribed`.
    - Implement `requestPermissionAndSubscribe()` tied strictly to explicit user gestures (CTA button click), subscribing using `VITE_VAPID_PUBLIC_KEY` and posting credentials to `/api/v1/notifications/subscribe`.
    - Implement passive `checkPermissionState()` monitoring across session resumes.
  - [x] Add Vitest unit tests in `usePushNotifications.spec.ts` mocking `navigator.serviceWorker` and `Notification` APIs with strict `afterEach` mock cleanup.

- [x] **Task 5: Frontend Fallback Badging, Throttled Polling & Permission Re-prompt UI** *(AC: 8, 9)*
  - [x] Create composable or store slice `frontend/src/features/match/composables/usePendingMatches.ts`:
    - Implement `fetchPendingCount()` calling `GET /api/v1/matches/pending` and storing unconfirmed match count.
    - Set up visibility listener (`visibilitychange`) with a 10-second debounce/throttle to refresh pending count without server spam.
  - [x] Update `frontend/src/views/HomeView.vue` and navigation components:
    - Render a reactive, No-Line compliant badge counter (`bg-error text-on-error rounded-full px-2 py-0.5 text-xs font-bold`) on "My Matches" button / Hub card whenever `pendingCount > 0`.
    - Render a non-blocking warning banner (`bg-error-container text-on-error-container p-4 rounded-xl mb-4`) if `permissionState === 'denied'` explaining: *"Push notifications are disabled. You may miss match confirmation requests. Enable notifications in your browser settings."*
  - [x] Write Vitest unit tests verifying badge rendering, throttled visibility refresh, and permission banner display.

- [x] **Task 6: End-to-End Playwright Verification with VAPID Mocking Strategy** *(AC: 1-9)*
  - [x] Create Playwright test `frontend/e2e/tests/e2e/match-confirmation-push.spec.ts` configured with mobile portrait device emulation.
  - [x] Test Push Subscription & Mock Strategy: intercept `/api/v1/notifications/subscribe` and inject browser spies for `ServiceWorkerRegistration.showNotification` to empirically verify subscription registration without flaky CI OS dependencies.
  - [x] Test Match Submission & Notification Trigger: Player A submits match against Player B -> verify match transitions to `PENDING_APPROVAL` -> verify backend audit log (`NotificationLog`) contains entry for Player B.
  - [x] Test Duplicate Warning, Routing Stub & In-App Fallback Badge: Player A submits a second identical match on the same day -> verify UI displays fallback badge counter -> verify backend logged notification payload with duplicate warning flag -> simulate notification click deep-linking to `/match/:id/review` stub resolving cleanly without 404s.

### Review Findings
- [x] [Review][Patch] Insecure Fallback to Hardcoded UUID in Notification Endpoints [NotificationController.java:36,48]
- [x] [Review][Patch] Incomplete Duplicate Match Detection Query & Logic [MatchRepository.java:24-29]
- [x] [Review][Patch] Incorrect Opponent Identification in 2v2 Matches [MatchServiceImpl.java:161-167]
- [x] [Review][Patch] Hardcoded Stub in /api/v1/matches/pending Prevents UI Fallback Badging [MatchController.java:25-30]
- [x] [Review][Patch] Missing UI CTA to Trigger Push Notification Subscription [HomeView.vue:26]
- [x] [Review][Patch] Improper Transaction Propagation for Audit Logging [NotificationOperation.java:55]
- [x] [Review][Patch] Expired Subscriptions Are Not Cleaned Up on HTTP 404/410 [PushNotificationServiceImpl.java:128-132]
- [x] [Review][Patch] Swallowed Exception & Zero Comments Policy Violation in Match Creation [MatchServiceImpl.java:168-170]
- [x] [Review][Patch] Missing Global Unique Constraint and Deduplication on Push Endpoints [PushSubscription.java:10-15]
- [x] [Review][Patch] Inaccurate Audit Log Status When No Subscription Exists [PushNotificationServiceImpl.java:80]
- [x] [Review][Patch] Repeated Instantiation of PushService in Delivery Loop [PushNotificationServiceImpl.java:111-115]
- [x] [Review][Patch] Unsafe Base64 Decoding of Subscription Auth Keys [PushNotificationServiceImpl.java:117]
- [x] [Review][Patch] Unchecked HTTP Response Status on Frontend Subscription Request [usePushNotifications.ts:60-70]
- [x] [Review][Patch] Service Worker notificationclick URL Matching Failure [sw.js:40-51]
- [x] [Review][Patch] Unhandled Promise Rejection in Service Worker showNotification [sw.js:34]
- [x] [Review][Defer] Fragile Nickname Pseudonymization Logic [PushNotificationServiceImpl.java:90-101] — deferred, pre-existing domain refinement

---

## 🏗️ Source Tree Components to Touch

### Backend (`src/main/java/com/tictactore/`)
- `NEW` `config/VapidProperties.java` (default fallback keys for test environment parity)
- `NEW` `dto/PushSubscriptionRequest.java`, `dto/PushNotificationPayload.java`, `dto/NotificationLogDto.java` (with Therapi Javadoc)
- `NEW` `model/PushSubscription.java` & `model/NotificationLog.java` (declare `@Version Long version`, indexes on user/match/recipient)
- `NEW` `repository/PushSubscriptionRepository.java` & `repository/NotificationLogRepository.java`
- `NEW` `service/operation/NotificationOperation.java` (`@Idempotent` + `@Transactional` save operations)
- `NEW` `service/PushNotificationService.java` & `service/impl/PushNotificationServiceImpl.java` (`@Retryable` orchestration)
- `NEW` `controller/NotificationController.java` (with OpenAPI annotations)
- `UPDATE` `repository/MatchRepository.java` (add `findMatchesByDateAndParticipants` query for duplicate detection)
- `UPDATE` `service/impl/MatchServiceImpl.java` (trigger confirmation push notifications and duplicate detection on match save)
- `NEW` `src/test/java/com/tictactore/service/PushNotificationServiceTest.java`
- `NEW` `src/test/java/com/tictactore/controller/NotificationControllerTest.java`
- `UPDATE` `src/test/java/com/tictactore/service/MatchServiceTest.java` & `MatchServiceATDDTest.java`
- `UPDATE` `src/main/resources/application.properties` & `src/test/resources/application.properties` (default VAPID stub properties)

### Frontend (`frontend/src/` & `frontend/public/`)
- `NEW` `public/sw.js` (Service Worker for Web Push and `notificationclick` navigation)
- `UPDATE` `src/router/index.ts` (register `/match/:id/review` routing stub and `/matches` pending view state)
- `NEW` `src/features/match/composables/usePushNotifications.ts` & `usePushNotifications.spec.ts`
- `NEW` `src/features/match/composables/usePendingMatches.ts` (with 10s visibility debounce)
- `UPDATE` `src/views/HomeView.vue` (add fallback badge counter and permission re-prompt warning banner)
- `NEW` `e2e/tests/e2e/match-confirmation-push.spec.ts`

---

## 🧪 Verification Plan

### Automated Testing Strategy
1. **Backend Unit & Integration Tests (JUnit 5 + Mockito + Spring Boot Test):**
   - `PushNotificationServiceTest`: verify exact JSON payload generation (including `"A retired player"` pseudonymization), subscription save/delete, default VAPID properties parity, and graceful exception handling when push servers return 410 Gone or network errors.
   - `NotificationControllerTest`: verify `@Valid` input validation and OpenAPI response contracts on `/api/v1/notifications/subscribe` (`201`) and `/unsubscribe` (`204`).
   - `MatchServiceTest`: verify duplicate match detection logic identifies matches with identical participants and scores on the same UTC calendar day and correctly sets `isDuplicateWarning = true`.
2. **Frontend Unit Tests (Vitest + Vue Test Utils):**
   - `usePushNotifications.spec.ts`: mock `navigator.serviceWorker` and `Notification` API; verify user-gesture permission request flow and clean mock restoration in `afterEach`.
   - `usePendingMatches.spec.ts`: verify throttled visibility change event triggers refresh of `GET /api/v1/matches/pending` and correctly computes badge counter.
   - `router.spec.ts`: verify `/match/:id/review` routing stub resolves cleanly and displays stale/already-confirmed notification states without throwing errors.
3. **End-to-End Tests (Playwright):**
   - Execute `frontend/e2e/tests/e2e/match-confirmation-push.spec.ts` using network interceptors and `showNotification` spies to verify the full loop from match submission -> duplicate check -> audit log creation -> fallback badge display -> deep-link routing stub resolution without flaky CI OS dependencies.

### Local CI Validation
Before presenting completion to the user, run the full verification script:
```bash
./scripts/ci-local.sh
```
All backend unit tests, frontend Vitest tests, linting, type-checking, and Playwright E2E suites must pass cleanly without warning or error.

---

## 🛑 What NOT To Do
1. **Do NOT combine `@Retryable` and `@Transactional`** on the same method or class in `PushNotificationServiceImpl` or `MatchServiceImpl`.
2. **Do NOT use primitive `long` for `@Version`** in `PushSubscription` or `NotificationLog`; always use nullable object wrapper `Long`.
3. **Do NOT map `@Version` column explicitly** with `@Column(name = "version")` in JPA entities.
4. **Do NOT fail match creation (`createMatch`)** if an asynchronous Web Push notification fails to deliver (e.g., network error or expired token); log the failure in `NotificationLog` and let the match transaction commit successfully so fallback badges function.
5. **Do NOT use 1px borders** in UI banners, badge counters, or toasts (`border`, `divide-y`, etc. are strictly forbidden by UX-DR3 No-Line rule).
6. **Do NOT poll the backend continuously** for pending matches on a fixed setInterval timer; rely on throttled `visibilitychange` events and initial page load to conserve battery and server resources.
7. **Do NOT request push notification permission automatically on page load** without an explicit user gesture (CTA button click); doing so causes browsers to auto-block notifications.
8. **Do NOT throw 404s or unhandled frontend errors** when a user navigates to `/match/:id/review` from a stale push notification for an already-confirmed or rejected match; gracefully display an `"Already confirmed"` state.

---

## 📝 Dev Agent Record

### Implementation Plan
1. Created Web Push and Audit Log entities (`PushSubscription`, `NotificationLog`), DTOs (`PushSubscriptionRequest`, `PushNotificationPayload`, `NotificationLogDto`), and repositories with `@Version Long version` and DB indexing.
2. Built `NotificationOperation` (`@Idempotent` + `@Transactional`) and `PushNotificationServiceImpl` (`@Retryable` ONLY) using `nl.martijndwars:web-push` for VAPID signing and payload dispatch with fallback dummy key configs.
3. Created `NotificationController` with OpenAPI annotations (`POST /subscribe`, `DELETE /unsubscribe`).
4. Integrated duplicate match detection into `MatchServiceImpl.createMatch()` on the same UTC calendar day and triggered `sendConfirmationRequest` with `isDuplicateWarning`.
5. Implemented `frontend/public/sw.js` to handle `push` and `notificationclick` events, registered routing stubs `/match/:id/review` and `/matches`, created `usePushNotifications.ts` for permission and subscription management, and `usePendingMatches.ts` with 10s throttled visibility change listeners.
6. Added in-app badge counter and non-blocking permission warning banner in `HomeView.vue`.
7. Authored unit/integration tests and Playwright E2E tests (`match-confirmation-push.spec.ts`).

### Completion Notes
- All backend unit/integration tests and frontend Vitest tests pass cleanly.
- Playwright E2E tests pass cleanly across Chromium, Firefox, and Webkit.
- Completed all tasks 1-6 according to ACs and design guardrails.

## 📁 File List
- `pom.xml`
- `src/main/resources/application.yml`
- `src/test/resources/application.properties`
- `src/main/java/com/tictactore/config/VapidProperties.java`
- `src/main/java/com/tictactore/dto/PushSubscriptionRequest.java`
- `src/main/java/com/tictactore/dto/PushNotificationPayload.java`
- `src/main/java/com/tictactore/dto/NotificationLogDto.java`
- `src/main/java/com/tictactore/model/PushSubscription.java`
- `src/main/java/com/tictactore/model/NotificationLog.java`
- `src/main/java/com/tictactore/repository/PushSubscriptionRepository.java`
- `src/main/java/com/tictactore/repository/NotificationLogRepository.java`
- `src/main/java/com/tictactore/service/operation/NotificationOperation.java`
- `src/main/java/com/tictactore/service/PushNotificationService.java`
- `src/main/java/com/tictactore/service/impl/PushNotificationServiceImpl.java`
- `src/main/java/com/tictactore/controller/NotificationController.java`
- `src/main/java/com/tictactore/repository/MatchRepository.java`
- `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java`
- `src/main/java/com/tictactore/controller/MatchController.java`
- `src/test/java/com/tictactore/controller/NotificationControllerATDDTest.java`
- `src/test/java/com/tictactore/service/PushNotificationServiceATDDTest.java`
- `src/test/java/com/tictactore/service/MatchServiceDuplicateDetectionATDDTest.java`
- `src/test/java/com/tictactore/service/MatchServiceTest.java`
- `frontend/public/sw.js`
- `frontend/src/router/index.ts`
- `frontend/src/features/match/views/MatchReviewStub.vue`
- `frontend/src/features/match/composables/usePushNotifications.ts`
- `frontend/src/features/match/composables/usePushNotifications.spec.ts`
- `frontend/src/features/match/composables/usePendingMatches.ts`
- `frontend/src/features/match/composables/usePendingMatches.spec.ts`
- `frontend/src/views/HomeView.vue`
- `frontend/e2e/tests/e2e/match-confirmation-push.spec.ts`

## 📜 Change Log
- Implemented Confirmation Requests & Push Notifications workflow (Story 3.1) with VAPID Web Push, audit logging, duplicate match warning detection, service worker navigation, and in-app fallback badging (Date: 2026-07-27).

