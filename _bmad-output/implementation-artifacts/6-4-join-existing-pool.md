---
baseline_commit: HEAD
---

# Story 6.4: Join Existing Pool

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to browse open "Want to Play" matchmaking pools on the Home Hub and join one with a single tap,
so that I can quickly get into a game without the friction of creating my own pool or coordinating outside the app.

## Acceptance Criteria

1. **Given** an authenticated user on the Home Hub (`/`)
   **When** there are active matchmaking pools with status `OPEN`
   **Then** the Home Hub displays the `ActivePoolsList` component listing all open pools with creator avatar, nickname, match format (`1v1` or `2v2`), start condition (`FILL_BASED` / `SCHEDULED_TIME` with formatted time), skill level requirement badge, current participant count / capacity, and participant avatars (FR36).
2. **Given** an authenticated user viewing an `OPEN` pool where they are not currently a participant
   **When** they tap the "Join" button
   **Then** the client sends `POST /api/v1/pools/{id}/join` with an empty body (user principal from authentication token) and receives `200 OK` with the updated `PoolResponse`.
3. **Given** a successful join request
   **When** the participant is added
   **Then** the backend persists the new `PoolParticipant` with role `PLAYER`, increments participant count, and if the total participants count equals required player count (2 for `ONE_VS_ONE`, 4 for `TWO_VS_TWO`), automatically transitions the pool status to `FILLED` (FR36).
4. **Given** a user attempting to join a pool where they are already a participant (as `HOST` or `PLAYER`)
   **When** the request is processed
   **Then** the backend rejects the request with `409 Conflict` and message `"User is already a participant in this pool"`.
5. **Given** a user attempting to join a pool that is no longer `OPEN` (status is `FILLED`, `CANCELLED`, or `EXPIRED`) or has reached full capacity
   **When** the request is processed
   **Then** the backend rejects the request with `409 Conflict` and message `"Pool is no longer open for joining"`.
6. **Given** concurrent join requests competing for the final open slot in a pool
   **When** multiple users attempt to join simultaneously
   **Then** optimistic concurrency control (`@Version` on `MatchmakingPool`) and unique constraints (`uk_pool_participant`) ensure that exactly the required number of participants are accepted, the winning request transitions the pool to `FILLED`, and losing requests receive `409 Conflict`.
7. **Given** a successful join response on the frontend
   **When** the store receives the updated `PoolResponse`
   **Then** the pool item in `ActivePoolsList` immediately updates to show the user's avatar in the participant slots, the "Join" button is replaced by a "Joined" indicator, and a success toast notification appears.
8. **Given** an authenticated user on the Home Hub
   **When** there are no `OPEN` pools available
   **Then** `ActivePoolsList` renders a clean empty state encouraging the user to create a pool with "Want to Play", without cluttering the screen.

## Tasks / Subtasks

- [x] Task 1: Backend API & Service Enhancements (AC1, AC2, AC3, AC4, AC5, AC6)
  - [x] Update `MatchmakingPoolRepository.java`:
    - Add `@EntityGraph(attributePaths = {"participants", "participants.user", "creator"}) List<MatchmakingPool> findByStatusOrderByCreatedAtDesc(PoolStatus status);`
  - [x] Update `PoolService.java` & `PoolServiceImpl.java`:
    - Add `List<PoolResponse> getActivePools();` querying open pools.
    - Add `PoolResponse joinPool(UUID poolId, UUID userId);` implementation:
      - Fetch `MatchmakingPool` by ID. Throw `ResourceNotFoundException` (404) if not found.
      - Validate `pool.getStatus() == PoolStatus.OPEN`. Throw `IllegalStateException` / Conflict (409) if not OPEN.
      - Check if user is already a participant. Throw `IllegalStateException` / Conflict (409) with message `"User is already a participant in this pool"`.
      - Calculate `requiredPlayers` (2 for `ONE_VS_ONE`, 4 for `TWO_VS_TWO`). If `participants.size() >= requiredPlayers`, throw Conflict (409) `"Pool is no longer open for joining"`.
      - Fetch `User` entity by `userId`.
      - Create `PoolParticipant` with role `PLAYER`, joinedAt `Instant.now()`. Add to pool via `pool.addParticipant(participant)`.
      - If `pool.getParticipants().size() == requiredPlayers`, set `pool.setStatus(PoolStatus.FILLED)`.
      - Save pool via `matchmakingPoolRepository.save(pool)` (optimistic locking handled via `@Version`).
      - Return `mapToPoolResponse(savedPool)`.
  - [x] Update `PoolController.java`:
    - Add `GET /api/v1/pools` returning `ResponseEntity<List<PoolResponse>>` with `@AuthenticationPrincipal User principal`.
    - Add `POST /api/v1/pools/{id}/join` returning `ResponseEntity<PoolResponse>` (200 OK) with `@PathVariable("id") UUID id` and `@AuthenticationPrincipal User principal`.
  - [x] Ensure exception handling in `GlobalExceptionHandler.java` / controller maps state conflicts to `409 Conflict`.
  - [x] Write backend unit and integration tests:
    - `PoolServiceTest.java`: Unit tests for `getActivePools`, successful `joinPool`, duplicate join rejection (409), full pool auto-fill to `FILLED`, closed pool rejection (409).
    - `PoolControllerATDDTest.java`: Integration tests for `GET /api/v1/pools` and `POST /api/v1/pools/{id}/join` (200 OK, 401 Unauthorized, 404 Not Found, 409 Conflict).
- [x] Task 2: Frontend Types, Service & Pinia Store (AC1, AC2, AC7)
  - [x] Update `frontend/src/features/matchmaking/services/poolService.ts`:
    - Add `fetchActivePools(): Promise<PoolResponse[]>` calling `GET /api/v1/pools`.
    - Add `joinPool(id: string): Promise<PoolResponse>` calling `POST /api/v1/pools/${id}/join`.
  - [x] Update `frontend/src/features/matchmaking/stores/poolStore.ts`:
    - Add `fetchActivePools()` action: fetches open pools and updates `activePools.value`.
    - Add `joinPool(poolId: string)` action: calls service, updates pool in `activePools.value`, and sets `currentPool.value`.
  - [x] Update store tests in `frontend/src/features/matchmaking/stores/__tests__/poolStore.spec.ts`.
- [x] Task 3: Frontend UI Components & Home Hub Integration (AC1, AC2, AC7, AC8)
  - [x] Create `frontend/src/features/matchmaking/components/ActivePoolsList.vue`:
    - Self-contained component: loads active pools on mount via `usePoolStore()`.
    - Clubhouse design token styling (`bg-surface-container-low`, rounded-2xl, elevation, no 1px solid borders per `UX-DR3`).
    - Render pool cards with creator avatar + nickname, format badge (`1v1` / `2v2`), start condition badge (`Immediate` or formatted date/time), skill level badge (`OPEN_FOR_ALL`, `BEGINNER`, `INTERMEDIATE`, `ADVANCED`).
    - Participant slots visualization: show avatars of joined users and placeholder circles for remaining open slots (`1/2` or `3/4`).
    - Action button: "Join" (calls store `joinPool`, shows loading spinner, emits toast event on success). If current authenticated user is already in the pool, display "Joined" badge instead of "Join" button.
    - Clean empty state when `activePools.length === 0`.
  - [x] Update `frontend/src/views/HomeView.vue`:
    - Mount `<ActivePoolsList />` cleanly below matchmaking actions.
    - Ensure `HomeView.vue` stays modular and strictly under 500 lines (rule `IP-04`).
  - [x] Add i18n translation keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`:
    - Pool list labels, format tags, start condition labels, skill tags, "Join" button, "Joined" status, "Full" status, empty state text, success/error toast messages.
  - [x] Component unit tests in `frontend/src/features/matchmaking/components/__tests__/ActivePoolsList.spec.ts`.
- [x] Task 4: Testing & Quality Verification
  - [x] Backend Unit & Integration Tests:
    - `PoolServiceTest.java` (strict AAA pattern, 100% logic coverage).
    - `PoolControllerATDDTest.java` (full request/response lifecycle).
  - [x] Frontend Unit & Component Tests:
    - `poolStore.spec.ts` (store state transitions).
    - `ActivePoolsList.spec.ts` (rendering, join action, disabled/joined states, empty state).
  - [x] E2E Playwright Tests:
    - Update `frontend/e2e/want-to-play-pool.spec.ts`:
      - Test 1: User A creates a 1v1 pool -> User B logs in and sees pool in active list -> User B clicks "Join" -> verifies User B avatar is added, button shows "Joined", and pool status becomes `FILLED`.
      - Test 2: User cannot rejoin a pool they already participate in.
  - [x] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contracts & Security (AD-04, AD-05):**
  - Base path: `/api/v1/pools`.
  - Endpoints:
    - `GET /api/v1/pools`: Retrieve all active matchmaking pools with status `OPEN`. Returns `200 OK` with `List<PoolResponse>`.
    - `POST /api/v1/pools/{id}/join`: Join an open pool. Empty request body. Returns `200 OK` with updated `PoolResponse`.
  - Authentication: All endpoints require authentication. User principal extracted via `@AuthenticationPrincipal User principal`.
  - Conflict Handling:
    - If user is already in the pool: `409 Conflict` (`"User is already a participant in this pool"`).
    - If pool status is not `OPEN` or pool is already full: `409 Conflict` (`"Pool is no longer open for joining"`).
- **Database & Concurrency Design:**
  - Optimistic locking: Entity `MatchmakingPool` is protected by `@Version Long version`. Concurrent modifications trigger optimistic lock exceptions, which must return `409 Conflict` to the client.
  - Unique constraint: `uk_pool_participant` on `(pool_id, user_id)` prevents duplicate inserts at the database level.
  - Cascade relations: Participant removal and cascade persist are managed via JPA lifecycle.
- **UX & Clubhouse Styling Guidelines (UX-DR3):**
  - Strictly adhere to Clubhouse "No-Line" rule: tonal shifts (`bg-surface-container-low`, `bg-surface-container-high`) and elevation/shadows instead of 1px solid border lines.
  - Pool items should clearly indicate current status and user's relation (Creator / Joined / Open).
- **500-Line Rule (IP-04):**
  - All new files strictly under 500 lines. `ActivePoolsList.vue` must self-contain its data loading and presentation to avoid bloating `HomeView.vue`.
- **Testing Standards:**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments (`// Given`, `// When`, `// Then` are forbidden).

### File Boundaries

| File | Status | Description |
|---|---|---|
| `src/main/java/com/tictactore/repository/MatchmakingPoolRepository.java` | UPDATE | Add query `findByStatusOrderByCreatedAtDesc` with `@EntityGraph` |
| `src/main/java/com/tictactore/service/PoolService.java` | UPDATE | Add `getActivePools` and `joinPool` method signatures |
| `src/main/java/com/tictactore/service/PoolServiceImpl.java` | UPDATE | Implement `getActivePools` and `joinPool` with concurrency validations |
| `src/main/java/com/tictactore/controller/PoolController.java` | UPDATE | Add `GET /api/v1/pools` and `POST /api/v1/pools/{id}/join` endpoints |
| `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java` | UPDATE | Map IllegalStateException to 409 Conflict |
| `src/test/java/com/tictactore/service/PoolServiceTest.java` | UPDATE | Add unit tests for pool retrieval and join logic |
| `src/test/java/com/tictactore/controller/PoolControllerATDDTest.java` | UPDATE | Add ATDD tests for active pools and join API |
| `frontend/src/features/matchmaking/services/poolService.ts` | UPDATE | Add `fetchActivePools` and `joinPool` API client methods |
| `frontend/src/features/matchmaking/stores/poolStore.ts` | UPDATE | Add `fetchActivePools` and `joinPool` actions to Pinia store |
| `frontend/src/features/matchmaking/stores/__tests__/poolStore.spec.ts` | UPDATE | Unit tests for new store actions |
| `frontend/src/features/matchmaking/components/ActivePoolsList.vue` | NEW | Clubhouse component listing active pools with join actions |
| `frontend/src/features/matchmaking/components/__tests__/ActivePoolsList.spec.ts` | NEW | Component unit tests for ActivePoolsList |
| `frontend/src/views/HomeView.vue` | UPDATE | Mount `<ActivePoolsList />` |
| `frontend/src/locales/en.json` | UPDATE | English translations for pool list, join action, and states |
| `frontend/src/locales/de.json` | UPDATE | German translations for pool list, join action, and states |
| `frontend/e2e/want-to-play-pool.spec.ts` | UPDATE | Playwright E2E tests for joining open pools |

### Previous Story Intelligence (Learnings from 6.1, 6.1b, 6.2, 6.3)

- **Ownership & Authentication:** Always extract user ID from `@AuthenticationPrincipal User principal` on the backend. Never allow clients to pass arbitrary joiner IDs.
- **Transactional Atomicity:** Joining a pool, adding the participant, and updating pool status to `FILLED` must occur inside a single `@Transactional` method.
- **EntityGraph Optimization:** Always fetch `participants`, `participants.user`, and `creator` eagerly using `@EntityGraph` to prevent N+1 queries when mapping `PoolResponse`.
- **Frontend Modularity:** Keep matchmaking components inside `src/features/matchmaking/` isolated from match entry and profile logic.

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-6-4-join-existing-pool.md`
- **Controller ATDD Test Scaffold:** `_bmad-output/test-artifacts/atdd-redphase-6-4/PoolControllerATDDTest.java`
- **Service ATDD Test Scaffold:** `_bmad-output/test-artifacts/atdd-redphase-6-4/PoolServiceTest.java`
- **Frontend Pinia Store Test Scaffold:** `_bmad-output/test-artifacts/atdd-redphase-6-4/poolStore.spec.ts`
- **Frontend Component Test Scaffold:** `_bmad-output/test-artifacts/atdd-redphase-6-4/ActivePoolsList.spec.ts`
- **Frontend E2E Test Suite:** `frontend/e2e/want-to-play-pool.spec.ts`

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR36 ("Join Want to Play pool")
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md] - Section "Home Hub: predictable, focused, evolving", UX Spec #7 "Skill-level matchmaking"
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-04, AD-05, IP-04 (500-Line Rule)

## Dev Agent Record

### Agent Model Used
Gemini 3.7 Flash

### Debug Log References
- Unit & ATDD Backend Tests: `PoolServiceTest`, `PoolControllerATDDTest`, `MatchmakingPoolRepositoryTest` passed 100% (31/31).
- Frontend Vitest Tests: `poolStore.spec.ts`, `ActivePoolsList.spec.ts`, and full suite passed (57 suites, 337 tests).
- E2E Tests: Playwright `want-to-play-pool.spec.ts` passed 100% across Chromium, Firefox, WebKit (15/15).
- CI Verification: `./scripts/ci-local.sh` passed 100%.

### Completion Notes List
1. Added query `findByStatusOrderByCreatedAtDesc` with `@EntityGraph` to `MatchmakingPoolRepository.java`.
2. Implemented `getActivePools()` and `joinPool(poolId, userId)` in `PoolService` / `PoolServiceImpl`, ensuring optimistic locking handling, duplicate participant prevention (409), status transitions to `FILLED` on capacity fill, and transactional atomicity.
3. Added endpoints `GET /api/v1/pools` and `POST /api/v1/pools/{id}/join` to `PoolController.java`.
4. Mapped `IllegalStateException` to HTTP 409 Conflict in `GlobalExceptionHandler.java`.
5. Added client methods in `poolService.ts` and actions `fetchActivePools`, `joinPool` in `poolStore.ts`.
6. Created `ActivePoolsList.vue` following Clubhouse styling tokens (`bg-surface-container-low`, rounded-2xl, no 1px solid border) and mounted it in `HomeView.vue`.
7. Added internationalization strings to `en.json` and `de.json`.
8. Added unit, component, and E2E Playwright tests covering all acceptance criteria.

### File List
- `src/main/java/com/tictactore/repository/MatchmakingPoolRepository.java`
- `src/main/java/com/tictactore/service/PoolService.java`
- `src/main/java/com/tictactore/service/PoolServiceImpl.java`
- `src/main/java/com/tictactore/controller/PoolController.java`
- `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java`
- `src/test/java/com/tictactore/service/PoolServiceTest.java`
- `src/test/java/com/tictactore/controller/PoolControllerATDDTest.java`
- `frontend/src/features/matchmaking/services/poolService.ts`
- `frontend/src/features/matchmaking/stores/poolStore.ts`
- `frontend/src/features/matchmaking/stores/__tests__/poolStore.spec.ts`
- `frontend/src/features/matchmaking/components/ActivePoolsList.vue`
- `frontend/src/features/matchmaking/components/__tests__/ActivePoolsList.spec.ts`
- `frontend/src/views/HomeView.vue`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`
- `frontend/e2e/want-to-play-pool.spec.ts`
