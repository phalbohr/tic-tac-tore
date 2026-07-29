---
baseline_commit: 3b66380c5e10037a34493397368cfdc970fd63a1
---

# Story 3.2: Single-tap Confirmation with Undo Window

## 📖 Story Foundation
**User Story:** As an opponent, I want to quickly confirm a match with an undo option, so that I can easily verify results and correct mis-taps.  
**Epic:** Epic 3: Data Verification & Trust  
**Status:** ready-for-dev  

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

- [ ] **Task 1: Backend Confirmation Endpoint & Domain Model** *(AC: 5, 6, 8)*
  - [ ] Add `confirmedByUserId` (`UUID`) and `confirmedAt` (`Instant`) columns to `Match.java`.
  - [ ] Add `confirmByOpponent(UUID opponentId)` helper method on `Match` entity validating opponent role and status transition from `PENDING_APPROVAL` to `CONFIRMED`.
  - [ ] Create DTOs: `MatchConfirmationRequest` (optional idempotency key) and `MatchConfirmationResponse`.
  - [ ] Create `MatchConfirmationOperation` (or add to `MatchOperation`) with `@Idempotent` + `@Transactional`.
  - [ ] Extend `MatchService` with `confirmMatch(UUID matchId, UUID userId, String idempotencyKey)`.
  - [ ] Add REST controller endpoint `POST /api/v1/matches/{id}/confirm` in `MatchController` obtaining authenticated `userId` securely from `SecurityContext`.
  - [ ] Update `GlobalExceptionHandler.java` for `UnauthorizedMatchActionException` (403) and `InvalidMatchStateException` (400).
  - [ ] Add unit & integration tests (`MatchServiceTest`, `MatchControllerTest`) for confirmation logic, unauthorized non-opponent attempt (403), creator self-confirmation attempt (403), duplicate confirmation, and idempotency.

- [ ] **Task 2: Frontend State Management & Confirmation Composable** *(AC: 1, 2, 3, 4, 5, 7)*
  - [ ] Create/update confirmation store & composable `useConfirmationTimer.ts` reusing timer patterns from `useSubmissionTimer.ts`.
  - [ ] Implement `startConfirmationTimer(matchId)`, `cancelConfirmationTimer()`, and `commitConfirmation()`.
  - [ ] Write Vitest unit tests in `frontend/src/features/match/stores/matchConfirmation.spec.ts` using `vi.useFakeTimers()`.

- [ ] **Task 3: UI Components Integration** *(AC: 1, 2, 3, 4)*
  - [ ] Update pending match confirmation card in `HomeView.vue` / `PendingMatches.vue` to trigger single-tap confirm.
  - [ ] Reuse `<UndoToast>` floating with message `"Match confirmed. Tap to undo."` during 15s window.
  - [ ] Wire undo button to abort timer and restore pending request state.

- [ ] **Task 4: End-to-End Verification** *(AC: 1-8)*
  - [ ] Create Playwright E2E test `frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts`.

---

## 🏗️ Source Tree Components to Touch

### Backend (`src/main/java/com/tictactore/`)
- `UPDATE` `model/Match.java` (add `confirmedByUserId`, `confirmedAt`, and `confirmByOpponent` domain logic)
- `NEW` `dto/MatchConfirmationRequest.java` & `dto/MatchConfirmationResponse.java`
- `NEW` `exception/UnauthorizedMatchActionException.java` & `exception/InvalidMatchStateException.java`
- `UPDATE` `exception/GlobalExceptionHandler.java` (map 403 Forbidden and 400 Bad Request responses)
- `UPDATE` `service/operation/MatchOperation.java` (`@Idempotent` + `@Transactional` confirmation operation)
- `UPDATE` `service/MatchService.java` & `service/impl/MatchServiceImpl.java` (`@Retryable` confirmation method)
- `UPDATE` `controller/MatchController.java` (add `POST /api/v1/matches/{id}/confirm`)
- `UPDATE` `src/test/java/com/tictactore/service/MatchServiceTest.java`
- `UPDATE` `src/test/java/com/tictactore/controller/MatchControllerTest.java`

### Frontend (`frontend/src/`)
- `REUSE` `features/match/components/UndoToast.vue`
- `NEW` `features/match/composables/useConfirmationTimer.ts` (or extend `useSubmissionTimer.ts`)
- `UPDATE` `features/match/stores/matchDraftStore.ts` (or new `matchConfirmationStore.ts`)
- `UPDATE` `views/HomeView.vue` (wire pending confirmation card, floating UndoToast, and undo state restoration)
- `NEW` `frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts`

---

## 🔍 Dev Notes & Learnings from Previous Stories
- **From Story 2.4 & 2.5:**
  - `UndoToast.vue` and `useSubmissionTimer.ts` pattern worked cleanly for 15s undo toast. Reuse these components to maintain UX and code consistency.
  - `@Retryable` and `@Transactional` must strictly stay in separate service vs operation classes.
  - `@Version Long version` on `Match` protects concurrent confirmation updates.

---

## 🧪 Testing & Verification Requirements
- Backend Unit & Integration Tests: `./mvnw test -Dtest=MatchControllerTest,MatchServiceTest`
- Frontend Unit Tests: `npm run test:unit`
- E2E Tests: `npm run test:e2e -- frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts`
- Full CI validation: `./scripts/ci-local.sh`
