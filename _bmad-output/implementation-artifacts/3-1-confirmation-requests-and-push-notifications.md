---
baseline_commit: 251fc1a86f20e36d71223f7118909aafbac016e1
---

# Story 3.1: Confirmation Requests & Push Notifications

## 📖 Story Foundation
**User Story:** As a player who just submitted a match, I want the system to notify opponents, so that they can verify the results.
**Epic:** Epic 3: The Verification Loop & Trust Architecture
**Status:** ready-for-dev

**Acceptance Criteria:**
- **Given** a match has been successfully submitted by the creator (or 15-second local undo window in `useSubmissionTimer` has expired without cancellation)
- **When** the backend saves the match in `PENDING_APPROVAL` status via `POST /api/v1/matches`
- **Then** the system identifies all required opponents who must verify the match (in 1v1: the opposing player; in 2v2: both players on Team B; if submitter is an observer: at least 1 player from each team per PRD FR12/FR55)
- **And** before dispatching notifications, the backend performs **Duplicate Match Detection** (PRD line 789) by checking for existing matches (confirmed or pending) on the same calendar day (UTC) with identical participants and game scores; if a duplicate is detected, the notification payload is tagged with a visible warning label (`isDuplicateWarning: true`) to prevent blind approval
- **And** the system generates an asynchronous Web Push notification payload containing match details (creator name, players, game scores, outcome, and duplicate warning if applicable) and dispatches it via VAPID to all registered push subscription endpoints for the required opponents
- **And** the system records an immutable audit entry in the database (`NotificationLog` entity per PRD line 814) capturing timestamp, recipient ID, match ID, delivery status (`DELIVERED`, `QUEUED`, or `FAILED`), and payload for dispute resolution
- **And** on the frontend, a Service Worker (`sw.js`) receives the Web Push event, displays an OS/browser push notification with action context, and handles click events (`notificationclick`) by deep-linking the user directly to the Match Review Screen (`/match/:id/review` or My Matches Pending tab)
- **And** if push notification permission was denied or delivery fails (PRD line 802), the pending confirmation remains prominently accessible via an in-app badge counter on the Home Hub and My Matches screen upon next application open
- **And** if push notification permission is revoked mid-session or across sessions, the application detects `Notification.permission === 'denied'` upon returning to foreground and surfaces a non-blocking banner prompting the user to re-enable notifications

---

## 🎯 Developer Context & Guardrails

### 1. Goal & Sequencing
- **Goal:** Build the infrastructure and workflow for asynchronous peer verification by implementing VAPID Web Push subscription management, duplicate match detection, audit logging, service worker notification handlers, and in-app fallback badges.
- **Trigger:** Transition of a newly submitted match to `PENDING_APPROVAL` status after the creator's 15-second local undo window elapses.
- **Sequencing Note:** This story is the entry point for Epic 3 (The Verification Loop). It establishes the notification and audit trail infrastructure that feeds directly into Story 3.2 (Match Confirmation & Cooldown Queue) and Story 3.3 (Match Rejection & Dispute Handling).

### 2. Architecture & Data Integrity Guardrails (Backend `code-1-guide` Compliance)
- **Three-Layer Transaction Architecture (Rule 3):**
  - **Outer Service (`PushNotificationServiceImpl` / `MatchServiceImpl`):** Annotated with `@Retryable` ONLY. Orchestrates validation, duplicate checking, and calls the atomic database operations. NEVER combine `@Retryable` and `@Transactional` on the same method or class.
  - **Inner Operation (`NotificationOperation`):** Annotated with `@Idempotent` + `@Transactional`. Handles saving `PushSubscription` and `NotificationLog` entities in clean, isolated Spring transactions.
- **Optimistic Locking (Rule 2):** Mutable JPA entities (`PushSubscription`, `NotificationLog`) MUST declare `@Version Long version;` (or `Integer` wrapper). Do NOT use primitive `long` and do NOT map the column explicitly (`@Column` is forbidden on version fields).
- **Tell, Don't Ask & JPA Best Practices (Rules 5, 9):** Domain rules and status transitions belong inside the entities. Always capture and return the instance returned by `repository.save()`.
- **Strict DTO Boundary & Layering (Rules 7, 11, 12):**
  - Never pass raw entities across REST boundaries. Create and use `PushSubscriptionRequest`, `PushNotificationPayload`, and `NotificationLogDto` in `src/main/java/com/tictactore/dto/`.
  - Controllers validate input (`@Valid`) and delegate to Service. Controllers MUST NEVER perform `null` checks on service responses to return 404s; domain exceptions must be thrown by services and handled by `GlobalExceptionHandler`.
- **Web Push / VAPID Infrastructure:** Use a standard Java Web Push library (such as `nl.martijndwars:web-push` with BouncyCastle) to sign and encrypt payloads using VAPID keys. In development/H2 environments where external push relays may be unreachable, gracefully catch network exceptions, log delivery attempts as `FAILED` or `SIMULATED` in `NotificationLog`, and never fail the core match creation transaction due to push delivery errors.

### 3. Frontend State & Service Worker Guardrails
- **Service Worker Registration & VAPID Subscription:**
  - Create or extend `frontend/public/sw.js` to listen for `push` events, display notifications with rich formatting, and handle `notificationclick` events to focus/navigate the client window to `/match/:id/review`.
  - Implement a dedicated composable `usePushNotifications.ts` in `src/core/composables/` (or `src/features/match/composables/`) responsible for checking browser push support, querying `Notification.permission`, subscribing via `pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: ... })`, and transmitting the subscription endpoint/keys to `POST /api/v1/notifications/subscribe`.
- **Fallback In-App Badging & Permission Monitoring:**
  - Create or extend state in `matchDraftStore.ts` or a dedicated `usePendingMatchesStore.ts` to query `GET /api/v1/matches/pending` on application launch and foreground (`document.addEventListener('visibilitychange')`).
  - Display a reactive badge count on the Home Hub and "My Matches" navigation items so opponents never miss verification requests even if OS push notifications are muted or denied.
  - If `Notification.permission` transitions to `'denied'`, display an informational banner with a CTA explaining how to re-enable notifications in browser settings.

### 4. UI/UX & Design System Compliance
- **No-Line Rule (UX-DR3):** All notification banners, badge chips, and permission prompts must use color fills (`bg-surface-container-highest`, `bg-error-container`, `text-on-error-container`, etc.) for visual separation. Zero 1px borders (`border`, `border-gray-200`, `divide-y`, etc. are strictly forbidden).
- **Mobile-First Portrait Optimization:** In-app warning banners and badge counters must be designed for 360px portrait mobile screens with minimum touch targets of 56x56dp for interactive buttons.
- **500-Line Rule (IP-04):** Keep store files and components strictly under 500 lines. Extract Service Worker communication, VAPID key conversion, and permission monitoring into clean composables (`usePushNotifications.ts`, `usePendingMatches.ts`).

---

## 🛠️ Tasks / Subtasks

- [ ] **Task 1: Backend Push Subscription & Audit Log Entities, DTOs & Repositories** *(AC: 5, 6)*
  - [ ] Add Web Push dependency (`nl.martijndwars:web-push:5.1.1` and `org.bouncycastle:bcprov-jdk18on`) to `pom.xml` for VAPID JWT signing and encryption.
  - [ ] Create DTOs in `src/main/java/com/tictactore/dto/`: `PushSubscriptionRequest.java` (`endpoint`, `p256dh`, `auth`), `PushNotificationPayload.java` (`matchId`, `creatorName`, `summary`, `isDuplicateWarning`), and `NotificationLogDto.java`.
  - [ ] Create JPA entities in `src/main/java/com/tictactore/model/`:
    - `PushSubscription.java`: fields `id` (UUID), `userId` (UUID, indexed), `endpoint` (String, unique), `p256dh` (String), `auth` (String), `createdAt` (Instant), and `@Version Long version`.
    - `NotificationLog.java`: fields `id` (UUID), `recipientId` (UUID), `matchId` (UUID), `type` (String, e.g., `"CONFIRMATION_REQUEST"`), `payload` (String/JSON), `status` (`"DELIVERED"`, `"QUEUED"`, `"FAILED"`), `errorMessage` (String), `sentAt` (Instant), and `@Version Long version`.
  - [ ] Create Spring Data JPA repositories in `src/main/java/com/tictactore/repository/`: `PushSubscriptionRepository.java` and `NotificationLogRepository.java`.
  - [ ] Create atomic operation class `NotificationOperation.java` in `src/main/java/com/tictactore/service/operation/` annotated with `@Idempotent` + `@Transactional` to handle saving subscriptions and audit logs.

- [ ] **Task 2: Backend Push Notification Service & REST Controller** *(AC: 5, 6)*
  - [ ] Create `PushNotificationService.java` and `PushNotificationServiceImpl.java` in `src/main/java/com/tictactore/service/impl/` annotated with `@Retryable` ONLY:
    - Implement `subscribe(UUID userId, PushSubscriptionRequest request)` and `unsubscribe(UUID userId, String endpoint)`.
    - Implement `sendConfirmationRequest(Match match, List<User> opponents, boolean isDuplicateWarning)` to build JSON payloads, encrypt via `nl.martijndwars.webpush.PushService`, dispatch to endpoints, and record results in `NotificationLog`.
    - Ensure delivery failures (e.g., expired endpoints, 410 Gone, offline network in dev) are caught, logged as `FAILED` in `NotificationLog`, and do NOT roll back the calling match transaction.
  - [ ] Create `NotificationController.java` in `src/main/java/com/tictactore/controller/` exposing `POST /api/v1/notifications/subscribe` (`201 Created` / `200 OK`) and `DELETE /api/v1/notifications/unsubscribe` (`204 No Content`).
  - [ ] Add unit and integration tests (`PushNotificationServiceTest.java`, `NotificationControllerTest.java`) verifying subscription persistence, VAPID payload serialization, exception resilience, and audit log creation.

- [ ] **Task 3: Duplicate Match Detection & Match Creation Integration** *(AC: 1, 2, 3, 4)*
  - [ ] Update `MatchRepository.java` with a custom query method: `findMatchesByDateAndParticipants(Instant startOfDay, Instant endOfDay, UUID p1, UUID p2, UUID p3, UUID p4)` (checking both pending and confirmed matches on the same calendar day in UTC).
  - [ ] Update `MatchServiceImpl.createMatch()`:
    - After successfully saving the match in `PENDING_APPROVAL` status via `matchOperation.saveMatch()`, identify all required opponent `User` entities (for 1v1: opponent; for 2v2: Team B players).
    - Query `MatchRepository` for duplicates on the same UTC day with identical game scores.
    - Set `boolean isDuplicateWarning = !duplicates.isEmpty();` and invoke `pushNotificationService.sendConfirmationRequest(savedMatch, opponents, isDuplicateWarning)`.
  - [ ] Write tests in `MatchServiceTest.java` and `MatchServiceATDDTest.java` verifying that creating identical matches on the same day triggers notification dispatch with `isDuplicateWarning = true` and records immutable entries in `NotificationLog`.

- [ ] **Task 4: Frontend Service Worker & VAPID Subscription Composable** *(AC: 5, 7)*
  - [ ] Create `frontend/public/sw.js` containing Web Push event listener (`self.addEventListener('push', ...)`) to parse JSON data and show OS notification (`self.registration.showNotification`), and click listener (`self.addEventListener('notificationclick', ...)`) to open or focus `/match/${data.matchId}/review`.
  - [ ] Create `frontend/src/features/match/composables/usePushNotifications.ts`:
    - Provide reactive state: `isSupported`, `permissionState` (`'default' | 'granted' | 'denied'`), `isSubscribed`.
    - Implement `requestPermissionAndSubscribe()`: request `Notification.requestPermission()`, retrieve Service Worker registration, subscribe using `VITE_VAPID_PUBLIC_KEY`, and POST subscription credentials to `/api/v1/notifications/subscribe`.
    - Implement `checkPermissionState()` to monitor permission changes across session resumes.
  - [ ] Connect `usePushNotifications` in `App.vue` or onboarding flow to initialize subscription when authenticated.
  - [ ] Add Vitest unit tests in `frontend/src/features/match/composables/usePushNotifications.spec.ts` mocking `navigator.serviceWorker` and `Notification` APIs (with strict `afterEach` cleanup of global stubs).

- [ ] **Task 5: Frontend Fallback Badging & Permission Re-prompt UI** *(AC: 8, 9)*
  - [ ] Create composable or store slice `frontend/src/features/match/composables/usePendingMatches.ts` (or in `matchDraftStore.ts`):
    - Implement `fetchPendingCount()` calling `GET /api/v1/matches/pending` and storing the count of unconfirmed matches awaiting user review.
    - Set up visibility listener (`visibilitychange`) to refresh pending count when the user returns to the tab.
  - [ ] Update `frontend/src/views/HomeView.vue` and navigation components:
    - Render a reactive, No-Line compliant badge counter (`bg-error text-on-error rounded-full px-2 py-0.5 text-xs font-bold`) on the "My Matches" button / Hub card whenever `pendingCount > 0`.
    - Render a non-blocking warning banner (`bg-error-container text-on-error-container p-4 rounded-xl mb-4`) if `permissionState === 'denied'` explaining: *"Push notifications are disabled. You may miss match confirmation requests. Enable notifications in your browser settings."*
  - [ ] Write unit tests in Vitest verifying badge rendering, visibility state refresh, and permission banner display.

- [ ] **Task 6: End-to-End Playwright Verification** *(AC: 1-9)*
  - [ ] Create Playwright test `frontend/e2e/tests/e2e/match-confirmation-push.spec.ts` configured with mobile portrait device emulation.
  - [ ] Test Push Subscription Flow: mock browser Notification API and VAPID key -> verify application attempts to register push subscription on backend via `/api/v1/notifications/subscribe`.
  - [ ] Test Match Submission & Notification Trigger: Player A submits match against Player B -> verify match transitions to `PENDING_APPROVAL` -> verify backend audit log (`NotificationLog`) contains entry for Player B.
  - [ ] Test Duplicate Warning & In-App Fallback Badge: Player A submits a second identical match on the same day -> verify UI displays fallback badge counter on Home Hub and My Matches -> verify backend logged notification payload with duplicate warning flag.

---

## 🏗️ Source Tree Components to Touch

### Backend (`src/main/java/com/tictactore/`)
- `NEW` `dto/PushSubscriptionRequest.java`, `dto/PushNotificationPayload.java`, `dto/NotificationLogDto.java`
- `NEW` `model/PushSubscription.java` & `model/NotificationLog.java` (declare `@Version Long version`, no `@Column` on version)
- `NEW` `repository/PushSubscriptionRepository.java` & `repository/NotificationLogRepository.java`
- `NEW` `service/operation/NotificationOperation.java` (`@Idempotent` + `@Transactional` save operations)
- `NEW` `service/PushNotificationService.java` & `service/impl/PushNotificationServiceImpl.java` (`@Retryable` orchestration)
- `NEW` `controller/NotificationController.java`
- `UPDATE` `repository/MatchRepository.java` (add `findMatchesByDateAndParticipants` query for duplicate detection)
- `UPDATE` `service/impl/MatchServiceImpl.java` (trigger confirmation push notifications and duplicate detection on match save)
- `NEW` `src/test/java/com/tictactore/service/PushNotificationServiceTest.java`
- `NEW` `src/test/java/com/tictactore/controller/NotificationControllerTest.java`
- `UPDATE` `src/test/java/com/tictactore/service/MatchServiceTest.java` & `MatchServiceATDDTest.java`

### Frontend (`frontend/src/` & `frontend/public/`)
- `NEW` `public/sw.js` (Service Worker for Web Push and `notificationclick` navigation)
- `NEW` `src/features/match/composables/usePushNotifications.ts` & `usePushNotifications.spec.ts`
- `NEW` `src/features/match/composables/usePendingMatches.ts` (or update `matchDraftStore.ts` for fallback polling)
- `UPDATE` `src/views/HomeView.vue` (add fallback badge counter and permission re-prompt warning banner)
- `NEW` `e2e/tests/e2e/match-confirmation-push.spec.ts`

---

## 🧪 Verification Plan

### Automated Testing Strategy
1. **Backend Unit & Integration Tests (JUnit 5 + Mockito + Spring Boot Test):**
   - `PushNotificationServiceTest`: verify VAPID payload generation, subscription save/delete, and graceful exception handling when push servers return 410 Gone or network errors.
   - `NotificationControllerTest`: verify `@Valid` input validation on `/api/v1/notifications/subscribe` (`201`) and `/unsubscribe` (`204`).
   - `MatchServiceTest`: verify duplicate match detection logic identifies matches with identical participants and scores on the same UTC calendar day and correctly sets `isDuplicateWarning = true`.
2. **Frontend Unit Tests (Vitest + Vue Test Utils):**
   - `usePushNotifications.spec.ts`: mock `navigator.serviceWorker` and `Notification` API; verify subscription state transitions and clean mock restoration in `afterEach`.
   - `usePendingMatches.spec.ts`: verify visibility change event triggers refresh of `GET /api/v1/matches/pending` and correctly computes badge counter.
3. **End-to-End Tests (Playwright):**
   - Execute `frontend/e2e/tests/e2e/match-confirmation-push.spec.ts` verifying the full loop from match submission -> duplicate check -> audit log creation -> fallback badge display.

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
6. **Do NOT poll the backend continuously** for pending matches on a fixed setInterval timer; rely on `visibilitychange` events and initial page load to conserve battery and server resources.
