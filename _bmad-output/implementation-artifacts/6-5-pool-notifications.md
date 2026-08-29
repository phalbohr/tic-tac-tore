---
baseline_commit: 73d8059c2f3df59ec42f68e05ebb3b84827210d1
---

# Story 6.5: Pool Notifications

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to receive push notifications when new matchmaking pools matching my preferences are created and when a pool I joined becomes full,
so that I can promptly join matches and know when games are ready to begin without having to actively monitor the app.

## Acceptance Criteria

1. **Given** a user creates a new matchmaking pool (`status == OPEN`)
   **When** the pool creation transaction successfully commits
   **Then** the backend publishes a `PoolCreatedEvent` and asynchronously dispatches Web Push notifications (`type: "POOL_CREATED"`, title `"New Matchmaking Pool"`, summary `"A new {1v1|2v2} pool is looking for players"`, url `"/"`) to all users with active push subscriptions and `poolNotificationsEnabled == true`, strictly excluding the creator (FR37).
   **And** a `NotificationLog` entry is recorded for each recipient with status `DELIVERED`, `FAILED`, or `SKIPPED`.
2. **Given** an open pool reaching full capacity upon a user joining
   **When** the pool status transitions to `FILLED` and the join transaction commits
   **Then** the backend publishes a `PoolFilledEvent` and asynchronously dispatches Web Push notifications (`type: "POOL_FILLED"`, title `"Pool Filled!"`, summary `"Your {1v1|2v2} pool is full — head to the table!"`, url `"/"`) to all registered participants (host and joined players) in the pool (FR37).
   **And** a `NotificationLog` entry is recorded for each participant with status `DELIVERED`, `FAILED`, or `SKIPPED`.
3. **Given** the client Service Worker (`sw.js`) receives a Web Push event
   **When** the payload contains `type: "POOL_CREATED"` or `type: "POOL_FILLED"`
   **Then** the Service Worker renders a system notification with the corresponding title, summary, and action.
   **And** clicking/tapping the notification focuses the app window and navigates to the target URL (`/`).
4. **Given** an authenticated user managing preferences in Profile Settings (`/cabinet`)
   **When** they view the "Default Match Preferences" section (`UserPreferencesSection.vue`)
   **Then** a "Matchmaking Pool Notifications" toggle switch is displayed (defaulting to enabled / `true`).
   **And** toggling and saving updates `poolNotificationsEnabled` via `PATCH /api/v1/profile/me`.
   **And** if `poolNotificationsEnabled == false`, the user is excluded from new pool creation notifications.
5. **Given** a push service network timeout, VAPID configuration issue, or invalid subscription endpoint
   **When** a pool is created or filled
   **Then** the pool creation/join transaction commits without interruption, and the error is logged in `NotificationLog` (`status: "FAILED"`) without throwing exceptions to the caller.

## Tasks / Subtasks

- [x] Task 1: Database Migration & Domain Entity Updates (AC1, AC2, AC4, AC5)
  - [x] Create Flyway migration `src/main/resources/db/migration/V14__add_pool_notifications.sql`:
    - Add `pool_id UUID` column to `notification_log` table with foreign key `REFERENCES matchmaking_pool(id) ON DELETE SET NULL`.
    - Add index `idx_notif_log_pool_recipient ON notification_log(pool_id, recipient_id)`.
    - Add `pool_notifications_enabled BOOLEAN DEFAULT TRUE NOT NULL` to `"user"` table.
    - Add index `idx_user_pool_notifications_enabled ON "user"(pool_notifications_enabled)`.
  - [x] Update `com.tictactore.model.NotificationLog`:
    - Add `private UUID poolId;` mapped to `@Column(name = "pool_id")`.
    - Update `@Table(indexes = { ... })` with `@Index(name = "idx_notif_log_pool_recipient", columnList = "pool_id, recipient_id")`.
  - [x] Update `com.tictactore.dto.NotificationLogDto`:
    - Add `UUID poolId` to the record fields.
  - [x] Update `com.tictactore.model.User`:
    - Add `@Column(name = "pool_notifications_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE") @Builder.Default private boolean poolNotificationsEnabled = true;`.
  - [x] Update `com.tictactore.dto.ProfileDto` and `com.tictactore.dto.UpdateProfileRequest`:
    - Add `Boolean poolNotificationsEnabled` with Swagger/OpenAPI documentation.
  - [x] Update `com.tictactore.repository.UserRepository`:
    - Add `List<User> findByPoolNotificationsEnabledTrueAndIdNot(UUID excludedUserId);`.
  - [x] Update repository tests (`UserRepositoryTest.java`, `NotificationLogRepositoryTest.java`) to verify schema and queries.
- [x] Task 2: Backend Event-Driven Notification System (AC1, AC2, AC4, AC5)
  - [x] Create Domain Events in `com.tictactore.event`:
    - `PoolCreatedEvent.java` (`UUID poolId`, `UUID creatorId`, `MatchType matchType`, `SkillLevel skillLevel`, `String creatorNickname`).
    - `PoolFilledEvent.java` (`UUID poolId`, `MatchType matchType`, `List<UUID> participantUserIds`).
  - [x] Update `com.tictactore.service.PoolServiceImpl`:
    - Inject `ApplicationEventPublisher eventPublisher`.
    - In `createPool(UUID creatorId, CreatePoolRequest request)`: After saving the pool, publish `new PoolCreatedEvent(savedPool.getId(), creatorId, savedPool.getMatchType(), savedPool.getSkillLevel(), creator.getNickname())`.
    - In `joinPool(UUID poolId, UUID userId)`: When `pool.getStatus() == PoolStatus.FILLED`, publish `new PoolFilledEvent(savedPool.getId(), savedPool.getMatchType(), savedPool.getParticipants().stream().map(p -> p.getUser().getId()).toList())`.
  - [x] Update `com.tictactore.dto.PushNotificationPayload`:
    - Expand record to include: `UUID matchId`, `UUID poolId`, `String type`, `String creatorName`, `String summary`, `String url`, `boolean isDuplicateWarning`, `String timestamp`.
    - Provide overloaded constructors/factory methods to preserve backwards compatibility for existing match notification calls.
  - [x] Update `com.tictactore.service.PushNotificationService` & `com.tictactore.service.impl.PushNotificationServiceImpl`:
    - Add interface methods:
      - `void sendPoolCreatedNotification(UUID poolId, UUID creatorId, String creatorName, MatchType matchType, SkillLevel skillLevel, List<User> recipients);`
      - `void sendPoolFilledNotification(UUID poolId, MatchType matchType, List<User> participants);`
    - In `PushNotificationServiceImpl`:
      - Implement push dispatch using `PushService`, serialize `PushNotificationPayload` (`type: "POOL_CREATED"` / `"POOL_FILLED"`), retrieve subscriptions via `NotificationOperation`, record `NotificationLog` with `pool_id`, and handle exceptions with `FAILED` log without re-throwing.
      - Apply pseudonymization for retired creators (`resolveCreatorName`).
  - [x] Create `com.tictactore.listener.PoolNotificationListener`:
    - Annotate with `@Component` and `@RequiredArgsConstructor`.
    - Implement `@Async` `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` for `PoolCreatedEvent`:
      - Query eligible users via `userRepository.findByPoolNotificationsEnabledTrueAndIdNot(event.creatorId())`.
      - Call `pushNotificationService.sendPoolCreatedNotification(...)`.
    - Implement `@Async` `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` for `PoolFilledEvent`:
      - Retrieve participant users and call `pushNotificationService.sendPoolFilledNotification(...)`.
  - [x] Update `com.tictactore.service.impl.UserServiceImpl`:
    - In `updateProfile`, map `request.poolNotificationsEnabled()` to `User` entity if non-null.
  - [x] Backend Unit & ATDD Tests:
    - Create `PoolNotificationListenerTest.java` verifying asynchronous event handling, creator exclusion, and push delegation.
    - Update `PushNotificationServiceTest.java` and `PushNotificationServiceATDDTest.java` with pool creation and pool filled test cases.
    - Update `PoolServiceTest.java` verifying that `ApplicationEventPublisher` publishes events upon pool creation and full join.
    - Update `UserControllerTest.java` and `UserControllerATDDTest.java` verifying `poolNotificationsEnabled` profile updates.
- [x] Task 3: Frontend Service Worker & User Preferences UI (AC3, AC4)
  - [x] Update `frontend/public/sw.js`:
    - Parse push JSON payload.
    - If `payload.type === 'POOL_CREATED'`: title = `New Matchmaking Pool: ${creatorName}`, body = `payload.summary`, `data.url = payload.url || '/'`.
    - If `payload.type === 'POOL_FILLED'`: title = `Pool Filled!`, body = `payload.summary`, `data.url = payload.url || '/'`.
    - Retain default match verification logic for match confirmation requests.
    - In `notificationclick`, navigate to and focus `event.notification.data.url`.
  - [x] Update `frontend/src/stores/auth.ts`:
    - Extend `UserProfile` interface with `poolNotificationsEnabled?: boolean`.
    - Update `updateProfile` action to accept `poolNotificationsEnabled`.
  - [x] Update `frontend/src/features/profile/components/UserPreferencesSection.vue`:
    - Add a toggle switch for "Matchmaking Pool Notifications" with descriptive caption ("Receive push notifications when new matchmaking pools are created").
    - Bind to `poolNotificationsEnabled` computed property updating `authStore.updateProfile({ poolNotificationsEnabled: val })`.
    - Clubhouse design token styling (`bg-surface-container-low`, rounded-2xl, no 1px solid borders per `UX-DR3`).
  - [x] Update i18n in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`:
    - Add translation keys for pool notifications toggle label, caption, and push titles.
  - [x] Frontend Unit/Component Tests:
    - Update `frontend/src/features/profile/components/__tests__/UserPreferencesSection.spec.ts` verifying toggle rendering and store action dispatch.
    - Update `frontend/src/stores/__tests__/auth.spec.ts`.
- [x] Task 4: Testing & Quality Verification
  - [x] Backend Test Suite:
    - Run all unit and ATDD tests for `PoolNotificationListenerTest`, `PushNotificationServiceTest`, `PushNotificationServiceATDDTest`, `PoolServiceTest`, `UserControllerATDDTest`.
  - [x] Frontend Test Suite:
    - Run Vitest tests for `UserPreferencesSection.spec.ts` and `auth.spec.ts`.
  - [x] E2E Playwright Tests:
    - Create `frontend/e2e/pool-notifications.spec.ts`:
      - Test 1: User navigates to Cabinet -> toggles Pool Notifications off -> reloads -> verifies preference persisted.
      - Test 2: User toggles Pool Notifications on -> updates profile -> verifies preference persisted.
  - [x] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **Decoupled Event Architecture (AD-04, AD-05):**
  - `PoolServiceImpl` MUST NOT directly invoke `PushNotificationService` or perform blocking network I/O.
  - Use Spring's `ApplicationEventPublisher` to publish `PoolCreatedEvent` and `PoolFilledEvent`.
  - Listeners MUST use `@Async` and `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` to ensure:
    1. Push notifications are only dispatched after the database transaction has committed.
    2. Any failures in Web Push dispatch (network timeouts, expired endpoints) NEVER roll back the pool transaction or throw exceptions to the user.
- **Push Notification Contracts & Service Worker:**
  - Base payload structure in `PushNotificationPayload`:
    - `matchId`: Optional UUID (nullable).
    - `poolId`: Optional UUID (nullable).
    - `type`: String (`"CONFIRMATION_REQUEST"`, `"MATCH_REJECTED"`, `"PARTIAL_CONFIRMATION"`, `"COOLDOWN_REMINDER"`, `"POOL_CREATED"`, `"POOL_FILLED"`).
    - `creatorName`: String (e.g. `"Pavel"`, `"A player"`, or `"A retired player"`).
    - `summary`: String describing the event.
    - `url`: Deep link target (e.g. `"/"` for pools, `"/match/{id}/review"` for matches).
    - `isDuplicateWarning`: Boolean.
    - `timestamp`: ISO-8601 string.
  - Service Worker (`sw.js`) must inspect `payload.type` and render appropriate title/body and handle click navigation via `clients.openWindow(url)` or `client.navigate(url)`.
- **Database Design & Migrations:**
  - Flyway migration script: `src/main/resources/db/migration/V14__add_pool_notifications.sql`.
  - Add `pool_id UUID REFERENCES matchmaking_pool(id) ON DELETE SET NULL` to `notification_log`.
  - Add `pool_notifications_enabled BOOLEAN DEFAULT TRUE NOT NULL` to `"user"`.
  - Indexes: `idx_notif_log_pool_recipient ON notification_log(pool_id, recipient_id)` and `idx_user_pool_notifications_enabled ON "user"(pool_notifications_enabled)`.
- **User Preference Matching (FR37):**
  - When a pool is created, query eligible users via `userRepository.findByPoolNotificationsEnabledTrueAndIdNot(creatorId)`.
  - Creators MUST NOT receive notifications for pools they created.
  - Users who have toggled `poolNotificationsEnabled = false` MUST NOT receive pool creation notifications.
  - When a pool is filled, ALL participants in the pool (host and players) receive the `POOL_FILLED` notification.
- **Clubhouse Styling Guidelines (UX-DR3):**
  - Adhere to Clubhouse "No-Line" rule: tonal shifts (`bg-surface-container-low`, `bg-surface-container-highest`) and elevation/shadows instead of 1px solid borders.
- **500-Line Rule (IP-04):**
  - Keep all new and modified files strictly under 500 lines.
- **Testing Standards:**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments (`// Given`, `// When`, `// Then` are forbidden).

### File Boundaries

| File | Status | Description |
|---|---|---|
| `src/main/resources/db/migration/V14__add_pool_notifications.sql` | NEW | Flyway migration for `notification_log.pool_id` and `user.pool_notifications_enabled` |
| `src/main/java/com/tictactore/event/PoolCreatedEvent.java` | NEW | Domain event for pool creation |
| `src/main/java/com/tictactore/event/PoolFilledEvent.java` | NEW | Domain event for pool capacity reached |
| `src/main/java/com/tictactore/listener/PoolNotificationListener.java` | NEW | Asynchronous transactional event listener for pool events |
| `src/main/java/com/tictactore/model/NotificationLog.java` | UPDATE | Add `poolId` field and pool index |
| `src/main/java/com/tictactore/dto/NotificationLogDto.java` | UPDATE | Add `poolId` field to DTO |
| `src/main/java/com/tictactore/model/User.java` | UPDATE | Add `poolNotificationsEnabled` field with default true |
| `src/main/java/com/tictactore/dto/ProfileDto.java` | UPDATE | Add `poolNotificationsEnabled` field |
| `src/main/java/com/tictactore/dto/UpdateProfileRequest.java` | UPDATE | Add `poolNotificationsEnabled` field |
| `src/main/java/com/tictactore/repository/UserRepository.java` | UPDATE | Add `findByPoolNotificationsEnabledTrueAndIdNot` query |
| `src/main/java/com/tictactore/service/PushNotificationService.java` | UPDATE | Add pool notification methods |
| `src/main/java/com/tictactore/service/impl/PushNotificationServiceImpl.java` | UPDATE | Implement pool push dispatch, payload formatting, and logging |
| `src/main/java/com/tictactore/service/PoolServiceImpl.java` | UPDATE | Publish `PoolCreatedEvent` and `PoolFilledEvent` |
| `src/main/java/com/tictactore/service/impl/UserServiceImpl.java` | UPDATE | Map `poolNotificationsEnabled` in `updateProfile` |
| `src/main/java/com/tictactore/dto/PushNotificationPayload.java` | UPDATE | Add `poolId`, `type`, `url` fields |
| `src/test/java/com/tictactore/listener/PoolNotificationListenerTest.java` | NEW | Unit tests for pool notification listener |
| `src/test/java/com/tictactore/service/PushNotificationServiceTest.java` | UPDATE | Unit tests for pool push methods |
| `src/test/java/com/tictactore/service/PushNotificationServiceATDDTest.java` | UPDATE | ATDD tests for pool push payloads |
| `src/test/java/com/tictactore/service/PoolServiceTest.java` | UPDATE | Verify event publishing on create/join |
| `src/test/java/com/tictactore/controller/UserControllerATDDTest.java` | UPDATE | Verify profile preference updates |
| `frontend/public/sw.js` | UPDATE | Handle pool push notifications and deep linking |
| `frontend/src/stores/auth.ts` | UPDATE | Add `poolNotificationsEnabled` to profile state |
| `frontend/src/features/profile/components/UserPreferencesSection.vue` | UPDATE | Add pool notifications toggle switch |
| `frontend/src/locales/en.json` | UPDATE | English i18n keys for pool notifications |
| `frontend/src/locales/de.json` | UPDATE | German i18n keys for pool notifications |
| `frontend/src/features/profile/components/__tests__/UserPreferencesSection.spec.ts` | UPDATE | Component tests for preference toggle |
| `frontend/e2e/pool-notifications.spec.ts` | NEW | Playwright E2E tests for pool notification preferences |

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-6-5-pool-notifications.md`
- **Backend Listener Tests:** `_bmad-output/test-artifacts/atdd-redphase-6-5/PoolNotificationListenerTest.java`
- **Backend Service ATDD Tests:** `_bmad-output/test-artifacts/atdd-redphase-6-5/PushNotificationServiceATDDTest.java`
- **Backend Controller ATDD Tests:** `_bmad-output/test-artifacts/atdd-redphase-6-5/UserControllerATDDTest.java`
- **Frontend Component Tests:** `_bmad-output/test-artifacts/atdd-redphase-6-5/UserPreferencesSection.spec.ts`
- **Frontend E2E Tests:** `frontend/e2e/pool-notifications.spec.ts`

### Previous Story Intelligence (Learnings from 6.1–6.4 & 3.1)

- **Asynchronous Isolation:** Push notifications involve external WebPush network roundtrips. Never block user-facing database transactions; always dispatch via `@Async` `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
- **Pseudonymization Invariant:** Respect user privacy for deleted/anonymized users: use `resolveCreatorName` to convert retired accounts (`ex-player-*`) to `"A retired player"`.
- **Fault Resilience:** If an endpoint returns 404/410 (expired subscription) or fails, catch the exception, record `NotificationLog(status = "FAILED")`, and do not interrupt other notifications or application flow.
- **Frontend State Integration:** Leverage existing `useAuthStore` and `UserPreferencesSection.vue` without creating separate redundant preference pages.

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR37 (Push notifications for pool events), Push Notification Strategy Table
- [Source: _bmad-output/planning-artifacts/epics.md] - Story 6.5 (Pool Notifications)
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-04, AD-05, IP-04 (500-Line Rule)
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md] - UX-DR3 (Clubhouse Design Tokens)

## Dev Agent Record

- **Implemented By:** Developer Agent (Amelia)
- **Status Transition:** `in-progress` -> `review`
- **Verification Results:** Full `./scripts/ci-local.sh` suite passed with 100% success (125 Playwright E2E tests, 455 backend unit/IT tests, 57 Vitest suites with 343 unit/component tests).
- **Key Changes:**
  - Database schema: `V14__add_pool_notifications.sql` with `notification_log.pool_id` (FK to `matchmaking_pool` with index) and `"user".pool_notifications_enabled` (boolean, indexed).
  - Event-driven architecture: Domain events `PoolCreatedEvent` and `PoolFilledEvent` published from `PoolServiceImpl` on transaction commit.
  - Event Listener: `PoolNotificationListener` listening via `@Async` `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` with robust try/catch error containment.
  - Push service enhancements: `PushNotificationService` and `PushNotificationServiceImpl` formatting `POOL_CREATED` and `POOL_FILLED` payloads, logging `NotificationLog` entries with `pool_id`, and handling subscriber exclusions and retired creator pseudonymization.
  - Frontend Service Worker & Profile UI: `sw.js` payload parsing and deep-link click handling; `useAuthStore` updated with `poolNotificationsEnabled`; `UserPreferencesSection.vue` updated with Clubhouse-styled toggle switch; i18n added for English and German.

- **Review Findings Resolution (2026-08-29):**
  - Resolved [Patch] OOM & thread starvation: Added `Slice<User>` pagination (`BATCH_SIZE = 100`) in `PoolNotificationListener` and `UserRepository` to stream and batch dispatch notifications safely.
  - Resolved [Patch] Missing loading guard & stale state: Added `isUpdating` guard, safe profile fetching on mount/toggle, `:disabled` and `:aria-busy` bindings with loading styles in `UserPreferencesSection.vue`.
  - Resolved [Patch] Missing catch block: Wrapped all asynchronous `authStore.updateProfile` calls (`togglePoolNotifications`, `selectedGroupId`, `selectedRuleId`) in `try/catch/finally` blocks in `UserPreferencesSection.vue`.
  - Resolved [Patch] SW title mismatch: Corrected `sw.js` push title for `POOL_CREATED` to `"New Matchmaking Pool"` strictly matching AC1.

### Review Findings

- [x] [Review][Patch] OOM and massive thread starvation during batch dispatching [src/main/java/com/tictactore/listener/PoolNotificationListener.java]
- [x] [Review][Patch] Missing loading guard in UserPreferencesSection toggle [frontend/src/features/profile/components/UserPreferencesSection.vue]
- [x] [Review][Patch] Stale State in Vue Component toggle [frontend/src/features/profile/components/UserPreferencesSection.vue]
- [x] [Review][Patch] Title format in Service Worker contradicts AC1 constraints [frontend/public/sw.js]
- [x] [Review][Defer] Ignored "Matching Criteria" Requirement — deferred, pre-existing
