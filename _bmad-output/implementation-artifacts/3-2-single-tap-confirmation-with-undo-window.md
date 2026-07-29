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
- **And** if the 15-second undo window expires without cancellation, the confirmation HTTP POST request (`POST /api/v1/matches/{id}/confirm`) is dispatched to the backend
- **And** the match is permanently committed and marked immutable only if the undo window expires without the user tapping undo (FR16)
- **And** if network failure occurs when the timer expires, optimistic UI retains the confirmation in local pending-sync state and retries idempotently when reconnected
- **And** the backend validates that the requesting user is a designated opponent of the match and performs optimistic locking (`@Version`) and idempotent state updates

---

## 🎯 Developer Context & Guardrails

### 1. Goal & Sequencing
- **Goal:** Implement the opponent confirmation flow with a client-side 15-second undo timer, optimistic UI toast notification, offline retry composable integration, and atomic backend API for match confirmation (`POST /api/v1/matches/{id}/confirm`).
- **Sequencing & Dependency Context:** Story 3.1 sets up the initial match pending state and push notification triggers upon creator submission. Story 3.2 enables single-tap opponent confirmation with 15-second safety undo window. Story 3.3 handles rejection with reason, Story 3.4 evaluates verification rules, and Story 3.5 manages publication & 24-hour cooldown logic.

### 2. Architecture & Data Integrity Guardrails (Backend `code-1-guide` Compliance)
- **Three-Layer Transaction Architecture (Rule 3):**
  - **Outer Service (`ConfirmationServiceImpl` / `MatchServiceImpl`):** Annotated with `@Retryable` ONLY. Validates user participation as opponent and calls inner operation. NEVER combine `@Retryable` and `@Transactional`.
  - **Inner Operation (`MatchConfirmationOperation` / `MatchOperation`):** Annotated with `@Idempotent` + `@Transactional`. Performs atomic state updates on `Match` entity and records confirmation timestamps/users.
- **Optimistic Locking (Rule 2):** `Match` entity uses `@Version Long version;`. Handle potential `OptimisticLockingFailureException` gracefully with `@Retryable`.
- **Domain Logic Encapsulation (Rules 5, 9):**
  - Match status checks and state transitions belong inside `Match.java` (e.g. `confirmByOpponent(UUID userId)`).
  - Always capture and return the instance returned by `repository.save()`.
- **Strict REST & DTO Boundaries (Rules 7, 11, 12):**
  - REST endpoint: `POST /api/v1/matches/{id}/confirm`.
  - Request DTO: `MatchConfirmationRequest` (containing optional `idempotencyKey`).
  - Response DTO: `MatchConfirmationResponse` (or `MatchResponse`).
  - Throw domain exceptions (`ParticipantNotFoundException`, `UnauthorizedMatchActionException`, `InvalidMatchStateException`) and map to 400/403/404 JSON error responses in `GlobalExceptionHandler.java`.

### 3. Frontend State & Optimistic UI Guardrails
- **Confirmation Countdown & Toast Composable:**
  - Create or extend `useConfirmationTimer.ts` (or reuse `useSubmissionTimer.ts` pattern) in `src/features/match/composables/`.
  - Tapping "Confirm" immediately sets local confirmation state to optimistic pending confirmation, starts 15s timer, and renders `<UndoToast message="Match confirmed. Tap to undo." />`.
  - Tapping "Undo" aborts timer, clears optimistic confirmation state, and restores pending confirmation card without making backend HTTP call.
  - When timer reaches 0, execute HTTP POST `POST /api/v1/matches/{id}/confirm`.
- **Offline & Retry Guardrails:**
  - On network disconnection or timeout, retain confirmation in local pending-sync state with toast `"Will retry when online"` and idempotency key.

### 4. UI/UX & Design System Compliance
- **No-Line Rule (UX-DR3):** Components must use background color shifts (`bg-surface-container-highest`, `bg-primary`, etc.) without 1px border lines.
- **Mobile-First Touch Target:** Single-tap confirm button and undo button on toast must satisfy minimum 48px touch targets (`min-h-12`).

---

## 🛠️ Tasks / Subtasks

- [ ] **Task 1: Backend Confirmation Endpoint & Domain Model** *(AC: 5, 6, 8)*
  - [ ] Add `confirmByOpponent(UUID userId)` helper method on `Match` entity.
  - [ ] Create DTOs: `MatchConfirmationRequest` and `MatchConfirmationResponse`.
  - [ ] Create `MatchConfirmationOperation` with `@Idempotent` + `@Transactional`.
  - [ ] Extend `MatchService` with `confirmMatch(UUID matchId, UUID userId, MatchConfirmationRequest request)`.
  - [ ] Add REST controller endpoint `POST /api/v1/matches/{id}/confirm` in `MatchController` (or `MatchConfirmationController`).
  - [ ] Add unit & integration tests (`MatchServiceTest`, `MatchControllerTest`) for confirmation logic, unauthorized non-opponent attempt (403), duplicate confirmation, and idempotency.

- [ ] **Task 2: Frontend State Management & Confirmation Composable** *(AC: 1, 2, 3, 4, 5, 7)*
  - [ ] Create/update confirmation store & composable `useConfirmationTimer.ts` (or in `useMatchDraftStore.ts` / `matchStore.ts`).
  - [ ] Implement `startConfirmationTimer(matchId)`, `cancelConfirmationTimer()`, and `commitConfirmation()`.
  - [ ] Write Vitest unit tests in `frontend/src/features/match/stores/matchConfirmation.spec.ts` using `vi.useFakeTimers()`.

- [ ] **Task 3: UI Components Integration** *(AC: 1, 2, 3, 4)*
  - [ ] Update match confirmation card / component to trigger single-tap confirm.
  - [ ] Renders `<UndoToast>` floating with message `"Match confirmed. Tap to undo."` during 15s window.
  - [ ] Wire undo button to abort timer and restore pending request state.

- [ ] **Task 4: End-to-End Verification** *(AC: 1-8)*
  - [ ] Create Playwright E2E test `frontend/e2e/tests/e2e/match-confirmation-undo.spec.ts`.

---

## 🔍 Dev Notes & Learnings from Previous Stories
- **From Story 2.4 & 2.5:**
  - `useSubmissionTimer.ts` pattern worked cleanly for 15s undo toast.
  - `@Retryable` and `@Transactional` must strictly stay in separate service vs operation classes.
  - `@Version Long version` on `Match` protects concurrent confirmation updates.

---

## 🧪 Testing & Verification Requirements
- Backend Unit & Integration Tests: `mvn test -Dtest=MatchControllerTest,MatchServiceTest`
- Frontend Unit Tests: `npm --prefix frontend test:unit`
- E2E Tests: `npx playwright test match-confirmation-undo.spec.ts`
- Full CI validation: `./scripts/ci-local.sh`
