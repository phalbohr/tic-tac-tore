---
baseline_commit: HEAD
---

# Story 6.4: Join Existing Pool

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to join an open "Want to Play" pool,
so that I can quickly get into a game without having to create my own.

## Acceptance Criteria

1. **Given** an authenticated user views the list of active pools on the Home Hub
   **When** an open pool exists that they are not already a part of
   **Then** they see the pool's details (creator, match format, start condition, skill level, current participants) and a "Join" action button.
2. **Given** an authenticated user who is not yet in the pool
   **When** they tap the "Join" button for an `OPEN` pool via `POST /api/v1/pools/{id}/join`
   **Then** the backend persists a new `PoolParticipant` for the user with role `PLAYER`, updates the pool's `currentPlayers` count, and returns `200 OK` (or `201 Created`) with the updated `PoolResponse` (FR36).
3. **Given** a user attempting to join a pool they are already in
   **When** they send the join request
   **Then** the backend rejects the request with `409 Conflict` and message "User is already a participant".
4. **Given** a user attempting to join a pool that is already `FILLED` or `CANCELLED`
   **When** they send the join request
   **Then** the backend rejects the request with `409 Conflict`.
5. **Given** a successful join operation that fills the pool's required player count (2 for 1v1, 4 for 2v2)
   **When** the new participant is added
   **Then** the backend automatically updates the pool's status to `FILLED`.
6. **Given** the frontend receives a successful join response
   **Then** the UI updates immediately to show the user as a participant, the "Join" button disappears, and a success toast is shown.

## Tasks / Subtasks

- [ ] Task 1: Backend Service & Controller Updates (AC2, AC3, AC4, AC5)
  - [ ] Add `joinPool(UUID poolId, UUID userId)` to `PoolService` and `PoolServiceImpl`:
    - Fetch `MatchmakingPool` by ID. Throw 404 if not found.
    - Validate `status == OPEN`. Throw 409 if not open.
    - Validate user is not already a participant. Throw 409 if exists.
    - Create and add `PoolParticipant` with role `PLAYER`.
    - If `participants.size() == requiredPlayers` (2 for `ONE_VS_ONE`, 4 for `TWO_VS_TWO`), change `status` to `FILLED`.
    - Save and return updated `PoolResponse`.
  - [ ] Add `POST /api/v1/pools/{id}/join` to `PoolController.java`.
  - [ ] Add ATDD tests to `PoolServiceTest.java` and `PoolControllerATDDTest.java` for join logic, conflict scenarios, and automatic fill status change.
- [ ] Task 2: Frontend Service & Store Updates (AC2, AC6)
  - [ ] Update `frontend/src/features/matchmaking/services/poolService.ts` with `joinPool(id: string)` method.
  - [ ] Update `frontend/src/features/matchmaking/stores/poolStore.ts` with `joinPool` action.
  - [ ] Update store tests in `poolStore.spec.ts`.
- [ ] Task 3: Frontend UI Components (AC1, AC6)
  - [ ] Create `frontend/src/features/matchmaking/components/ActivePoolsList.vue` (or similar) to list open pools.
  - [ ] Ensure Clubhouse styling (no 1px solid borders, use `bg-surface-container`).
  - [ ] Show pool creator avatar, match type, and participants.
  - [ ] Add "Join" button for pools where the current user is not a participant.
  - [ ] Add success toast notification upon successful join.
  - [ ] Mount the list component on `HomeView.vue`.
  - [ ] Component unit tests.
- [ ] Task 4: Testing & Quality Verification
  - [ ] E2E Playwright test in `frontend/e2e/want-to-play-pool.spec.ts` for joining an existing pool.
  - [ ] Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contracts & Security:**
  - `POST /api/v1/pools/{id}/join` returns `PoolResponse`.
  - Use `@AuthenticationPrincipal` for the joining user ID.
- **Transactional Atomicity:**
  - Joining a pool and potentially updating its status to `FILLED` must be within a single `@Transactional` method.
  - Rely on optimistic locking (`@Version` on `MatchmakingPool`) to handle concurrent join requests safely.
- **Clubhouse Styling Guidelines (UX-DR3):**
  - Strictly adhere to Clubhouse "No-Line" rule: tonal shifts and elevation/shadows instead of 1px solid border lines.
- **500-Line Rule (IP-04):**
  - All new files strictly under 500 lines. Keep `HomeView.vue` clean.
- **Testing Standards:**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments.

### File Boundaries

| File | Status | Description |
|---|---|---|
| `src/main/java/com/tictactore/service/PoolService.java` | UPDATE | Add joinPool method |
| `src/main/java/com/tictactore/service/PoolServiceImpl.java` | UPDATE | Implement joinPool logic |
| `src/main/java/com/tictactore/controller/PoolController.java` | UPDATE | Add join endpoint |
| `frontend/src/features/matchmaking/services/poolService.ts` | UPDATE | API client join method |
| `frontend/src/features/matchmaking/stores/poolStore.ts` | UPDATE | Pinia action to join pool |
| `frontend/src/features/matchmaking/components/ActivePoolsList.vue` | NEW | Component to list active pools and join |
| `frontend/src/views/HomeView.vue` | UPDATE | Mount ActivePoolsList |
| `frontend/e2e/want-to-play-pool.spec.ts` | UPDATE | Playwright E2E for join flow |

### Previous Story Intelligence
- Follow the patterns from 6.3 for REST layer mapping and Vue component structure.
