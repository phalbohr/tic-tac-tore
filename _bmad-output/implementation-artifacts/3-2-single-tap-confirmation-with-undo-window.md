---
baseline_commit: 3b66380c5e10037a34493397368cfdc970fd63a1
---

# Story 3.2: Single-tap Confirmation with Undo Window

## 📖 Story Foundation

**User Story:** As an opponent, I want to quickly confirm a match with an undo option, so that I can easily verify results and correct mis-taps.  
**Epic:** Epic 3: Data Verification & Trust  
**Status:** done

**Acceptance Criteria:**

- **Given** an opponent views a pending match confirmation request
- **When** they tap the "Confirm" button
- **Then** the UI immediately displays a 15-second "Undo" toast notification (UX-DR4, FR13: `"Match confirmed. Tap to undo."`)
- **And** during the 15-second undo window, the pending confirmation action can be cancelled by tapping "Undo" on the toast notification, which restores the confirmation request to its pending state without sending confirmation to the backend
- **And** if the 15-second undo window expires without cancellation, the confirmation HTTP POST request (`POST /api/v1/matches/{id}/confirm`) is dispatched to the backend with optional `Idempotency-Key` header
- **And** the match is permanently committed, marked with status `CONFIRMED`, and updated with `confirmedByUserId` and `confirmedAt` only if the undo window expires without the user tapping undo (FR16)
- **And** if network failure occurs when the timer expires, optimistic UI retains the confirmation in local pending-sync state and retries idempotently when reconnected
- **And** the backend extracts the requesting user ID securely from Spring Security `SecurityContext` / JWT token, validates that the user is a designated opponent of the match (and not the match creator), and performs optimistic locking (`@Version`) and idempotent state updates

---

## 🎯 Developer Context & Guardrails

### 1. Goal & Sequencing

- **Goal:** Implement the opponent confirmation flow with a client-side 15-second undo timer, optimistic UI toast notification, offline retry composable integration, and atomic backend API for match confirmation (`POST /api/v1/matches/{id}/confirm`).
- **Sequencing & Dependency Context:** Story 3.1 sets up the initial match pending state (`PENDING_APPROVAL`) and push notification triggers upon creator submission. Story 3.2 enables single-tap opponent confirmation with 15-second safety undo window. Story 3.3 handles rejection with reason, Story 3.4 evaluates verification rules, and Story 3.5 manages publication & 24-hour cooldown logic.

### 2. Architecture & Data Integrity Guardrails (Backend `code-1-guide` Compliance)

- **Three-Layer Transaction Architecture (Rule 3):**
  - **Outer Service (`MatchServiceImpl`):** Annotated with `@Retryable` ONLY. Validates user participation as opponent and calls inner operation. NEVER combine `@Retryable` and `@Transactional`.
  - **Inner Operation (`MatchConfirmationOperation` / `MatchOperation`):** Annotated with `@Idempotent` + `@Transactional`. Performs atomic state updates on `Match` entity, setting `confirmedByUserId`, `confirmedAt`, and updating status from `PENDING_APPROVAL` to `CONFIRMED`.
- **Entity & Schema Requirements (`Match.java`):**
  - Add `@Column(name = "confirmed_by_user_id") private UUID confirmedByUserId;`
  - Add `@Column(name = "confirmed_at") private Instant confirmedAt;`
  - Encapsulate domain transition inside `Match.java`:
    ```java
    public void confirmByOpponent(UUID opponentId) {
        if (!"PENDING_APPROVAL".equals(this.status)) {
            throw new InvalidMatchStateException("Match is not in PENDING_APPROVAL status");
        }
        if (this.creatorId.equals(opponentId) || !isOpponent(opponentId)) {
            throw new UnauthorizedMatchActionException("User " + opponentId + " is not an opponent for match " + this.id);
        }
        this.status = "CONFIRMED";
        this.confirmedByUserId = opponentId;
        this.confirmedAt = Instant.now();
    }
    ```
- **Optimistic Locking (Rule 2):** `Match` entity uses `@Version Long version;`. Handle potential `OptimisticLockingFailureException` gracefully with `@Retryable`.
- **Strict REST & Security Boundaries (Rules 7, 11, 12):**
  - REST endpoint: `POST /api/v1/matches/{id}/confirm`.
  - Request Header: `Idempotency-Key` (Optional UUID).
  - User ID extraction: Extract caller UUID strictly from Spring Security `SecurityContext` / JWT token (`@AuthenticationPrincipal` or `JwtAuthenticationToken`). Do NOT accept `userId` in request body.
  - Response DTO: `MatchResponse` (or `MatchConfirmationResponse` containing `MatchResponse`).
  - Throw domain exceptions (`ParticipantNotFoundException`, `UnauthorizedMatchActionException`, `InvalidMatchStateException`) and map to 400/403/404 JSON error responses in `GlobalExceptionHandler.java`.

### 3. Frontend State & Optimistic UI Guardrails

- **Confirmation Countdown & Component Reuse:**
  - Reuse and extend `UndoToast.vue` from `src/features/match/components/` (message: `"Match confirmed. Tap to undo."`).
  - Reuse or extend `useSubmissionTimer.ts` pattern in `src/features/match/composables/` for handling the 15-second confirmation countdown.
  - In `HomeView.vue` (or pending match list card), tapping "Confirm" immediately sets local confirmation state to optimistic pending, starts 15s timer, and renders `<UndoToast message="Match confirmed. Tap to undo." />`.
  - Tapping "Undo" aborts timer, clears optimistic confirmation state, and restores pending confirmation card without making backend HTTP call.
  - When timer reaches 0, execute HTTP POST `POST /api/v1/matches/{id}/confirm` with `Idempotency-Key` header.
- **Offline & Retry Guardrails:**
  - On network disconnection or timeout, retain confirmation in local pending-sync state with toast `"Will retry when online"` and idempotency key.

### 4. UI/UX & Design System Compliance

- **No-Line Rule (UX-DR3):** Components must use background color shifts (`bg-surface-container-highest`, `bg-primary`, etc.) without 1px border lines.
- **Mobile-First Touch Target:** Single-tap confirm button and undo button on toast must satisfy minimum 48px touch targets (`min-h-12`).

---

## 🛠️ Tasks / Subtasks

- [x] **Task 1: Backend Confirmation Endpoint & Domain Model** _(AC: 5, 6, 8)_
  - [x] Add `confirmedByUserId` (`UUID`) and `confirmedAt` (`Instant`) columns to `Match.java`.
  - [x] Add `confirmByOpponent(UUID opponentId)` helper method on `Match` entity validating opponent role and status transition from `PENDING_APPROVAL` to `CONFIRMED`.
  - [x] Create DTOs: `MatchConfirmationRequest` (optional idempotency key) and `MatchConfirmationResponse`.
  - [x] Create `MatchConfirmationOperation` (or add to `MatchOperation`) with `@Idempotent` + `@Transactional`.
  - [x] Extend `MatchService` with `confirmMatch(UUID matchId, UUID userId, String idempotencyKey)`.
  - [x] Add REST controller endpoint `POST /api/v1/matches/{id}/confirm` in `MatchController` obtaining authenticated `userId` securely from `SecurityContext`.
  - [x] Update `GlobalExceptionHandler.java` for `UnauthorizedMatchActionException` (403) and `InvalidMatchStateException` (400).
  - [x] Add unit & integration tests (`MatchServiceTest`, `MatchControllerTest`) for confirmation logic, unauthorized non-opponent attempt (403), creator self-confirmation attempt (403), duplicate confirmation, and idempotency.

- [x] **Task 2: Frontend State Management & Confirmation Composable** _(AC: 1, 2, 3, 4, 5, 7)_
  - [x] Create/update confirmation store & composable `useConfirmationTimer.ts` reusing timer patterns from `useSubmissionTimer.ts`.
  - [x] Implement `startConfirmationTimer(matchId)`, `cancelConfirmationTimer()`, and `commitConfirmation()`.
  - [x] Write Vitest unit tests in `frontend/src/features/match/stores/matchConfirmation.spec.ts` using `vi.useFakeTimers()`.

- [x] **Task 3: UI Components Integration** _(AC: 1, 2, 3, 4)_
  - [x] Update pending match confirmation card in `HomeView.vue` / `PendingMatches.vue` to trigger single-tap confirm.
  - [x] Reuse `<UndoToast>` floating with message `"Match confirmed. Tap to undo."` during 15s window.
  - [x] Wire undo button to abort timer and restore pending request state.

- [x] **Task 4: End-to-End Verification** _(AC: 1-8)_
  - [x] Create Playwright E2E test `frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts`.

---

## 📁 File List

- `src/main/java/com/tictactore/model/Match.java`
- `src/main/java/com/tictactore/dto/MatchResponse.java`
- `src/main/java/com/tictactore/dto/MatchConfirmationRequest.java`
- `src/main/java/com/tictactore/dto/MatchConfirmationResponse.java`
- `src/main/java/com/tictactore/exception/UnauthorizedMatchActionException.java`
- `src/main/java/com/tictactore/exception/InvalidMatchStateException.java`
- `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java`
- `src/main/java/com/tictactore/service/operation/MatchOperation.java`
- `src/main/java/com/tictactore/service/MatchService.java`
- `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java`
- `src/main/java/com/tictactore/controller/MatchController.java`
- `src/test/java/com/tictactore/service/MatchServiceTest.java`
- `src/test/java/com/tictactore/controller/MatchControllerTest.java`
- `frontend/src/features/match/composables/useConfirmationTimer.ts`
- `frontend/src/features/match/stores/matchConfirmationStore.ts`
- `frontend/src/features/match/stores/matchConfirmation.spec.ts`
- `frontend/src/features/match/components/PendingMatches.vue`
- `frontend/src/features/match/components/UndoToast.vue`
- `frontend/src/views/HomeView.vue`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`
- `frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts`

---

## 📝 Change Log

- Implement single-tap match confirmation backend API `POST /api/v1/matches/{id}/confirm` with `@Idempotent`, `@Transactional`, `@Retryable`, and Spring Security user extraction.
- Add `confirmedByUserId` and `confirmedAt` domain fields and validation rules in `Match.java`.
- Create domain exceptions `UnauthorizedMatchActionException` (403) and `InvalidMatchStateException` (400) mapped in `GlobalExceptionHandler`.
- Implement `useConfirmationTimer` composable and `matchConfirmationStore` with 15-second undo countdown timer and fake timers Vitest tests.
- Integrate `PendingMatches.vue` card and `<UndoToast>` in `HomeView.vue` with single-tap confirmation and immediate undo restoration.
- Add Playwright E2E tests in `match-confirmation-undo.spec.ts` covering confirmation flow, undo window cancellation, and backend dispatch.

---

## 🤖 Dev Agent Record

### Implementation Plan

1. Backend domain model & exception mapping: Added `confirmedByUserId`, `confirmedAt`, and `confirmByOpponent` on `Match`. Created `UnauthorizedMatchActionException` and `InvalidMatchStateException`, mapped to 403 and 400 in `GlobalExceptionHandler`.
2. Three-layer transaction architecture: `MatchOperation.confirmMatch` (`@Idempotent` + `@Transactional`), `MatchServiceImpl.confirmMatch` (`@Retryable`), and `MatchController.confirmMatch` (`POST /api/v1/matches/{id}/confirm`).
3. Frontend composable & store: Built `useConfirmationTimer.ts` and `matchConfirmationStore.ts` with 15s countdown timer.
4. UI integration: Created `PendingMatches.vue` and integrated floating `<UndoToast>` in `HomeView.vue` with `min-h-12` touch targets.
5. Verification: Comprehensive unit tests in `MatchServiceTest`, `MatchControllerTest`, `matchConfirmation.spec.ts`, and Playwright E2E in `match-confirmation-undo.spec.ts`.

### Completion Notes

- All 101 backend Java tests passed cleanly (`mvnw test`).
- All 99 frontend unit tests passed cleanly (`vitest`).
- All Playwright E2E tests for `match-confirmation-undo.spec.ts` passed across Chromium, Firefox, and Webkit.
