---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-28T23:19:00+02:00'
storyId: '6.4'
storyKey: '6-4-join-existing-pool'
storyFile: '_bmad-output/implementation-artifacts/6-4-join-existing-pool.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-6-4-join-existing-pool.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-6-4/PoolControllerATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-4/PoolServiceTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-4/poolStore.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-4/ActivePoolsList.spec.ts'
  - 'frontend/e2e/want-to-play-pool.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/6-4-join-existing-pool.md'
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

# Acceptance Test-Driven Development (ATDD) Checklist: Story 6.4

## Story Context
- **Story Key:** `6-4-join-existing-pool`
- **Story ID:** `6.4`
- **Title:** Story 6.4: Join Existing Pool
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/6-4-join-existing-pool.md`

## Acceptance Criteria Summary
1. **AC 1:** Given an authenticated user on the Home Hub (`/`), when active matchmaking pools with status `OPEN` exist, then `ActivePoolsList` displays open pools with creator avatar, nickname, match format (`1v1` / `2v2`), start condition, skill level badge, current participants / capacity, and participant avatars (FR36).
2. **AC 2:** Given an authenticated user viewing an `OPEN` pool where they are not a participant, when they tap "Join", then the client sends `POST /api/v1/pools/{id}/join` and receives `200 OK` with updated `PoolResponse`.
3. **AC 3:** Given a successful join request, when the participant is added, then the backend persists `PoolParticipant` with role `PLAYER`, increments count, and if capacity is reached (2 for 1v1, 4 for 2v2), transitions status to `FILLED` (FR36).
4. **AC 4:** Given a user attempting to join a pool they already participate in, when the request is processed, then the backend rejects with `409 Conflict` ("User is already a participant in this pool").
5. **AC 5:** Given a user attempting to join a pool that is no longer `OPEN` (status is `FILLED`, `CANCELLED`, or `EXPIRED`) or full, when the request is processed, then the backend rejects with `409 Conflict` ("Pool is no longer open for joining").
6. **AC 6:** Given concurrent join requests competing for the final open slot, when multiple users attempt to join simultaneously, then optimistic concurrency control (`@Version`) and unique constraints ensure exactly the required number are accepted, the winner transitions status to `FILLED`, and losers receive `409 Conflict`.
7. **AC 7:** Given a successful join response on frontend, when the store receives updated `PoolResponse`, then `ActivePoolsList` immediately updates participant slots, replaces "Join" button with "Joined" indicator, and shows success toast.
8. **AC 8:** Given an authenticated user on the Home Hub, when there are no `OPEN` pools available, then `ActivePoolsList` renders a clean empty state.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear REST API contracts for `GET /api/v1/pools` and `POST /api/v1/pools/{id}/join`, established JPA entities from Story 6.3, concurrency & conflict handling specifications, modular Clubhouse component design, and existing Vitest & Playwright fixtures.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Active pools listed on Home Hub via `ActivePoolsList` | Component (Vitest) & Controller ATDD | `ActivePoolsList.vue` & `PoolController` | P0 | 1. `GET /api/v1/pools` returns active open pools list<br>2. Cards show creator info, format badge (1v1/2v2), start condition, skill level, participant count |
| **AC 2** | Join open pool via `POST /api/v1/pools/{id}/join` | Controller ATDD / Service Unit | `PoolController` & `PoolService` | P0 | 1. 200 OK with updated `PoolResponse`<br>2. Participant added with role `PLAYER`<br>3. `currentPlayers` incremented |
| **AC 3** | Auto-transition to `FILLED` when capacity reached | Service Unit / Controller ATDD | `PoolServiceImpl` | P0 | 1. Adding 2nd player in 1v1 transitions status to `FILLED`<br>2. Adding 4th player in 2v2 transitions status to `FILLED`<br>3. 2v2 pool remains `OPEN` when slots remain |
| **AC 4** | Duplicate join rejection with 409 Conflict | Controller ATDD / Service Unit | `PoolService` & `PoolController` | P0 | 1. User already participating (HOST or PLAYER) rejected with 409 Conflict ("User is already a participant in this pool") |
| **AC 5** | Non-open / Full pool join rejection with 409 Conflict | Controller ATDD / Service Unit | `PoolService` & `PoolController` | P0 | 1. Join request on `FILLED` or `CANCELLED` pool rejected with 409 Conflict ("Pool is no longer open for joining")<br>2. Non-existent pool returns 404 Not Found |
| **AC 6** | Concurrency & Optimistic locking protection | Service Unit / Controller ATDD | `MatchmakingPool` & `PoolServiceImpl` | P1 | 1. OptimisticLockException or capacity overrun caught and translated to 409 Conflict |
| **AC 7** | Frontend store & UI reaction upon join | Pinia Unit / Component Vitest | `poolStore.ts` & `ActivePoolsList.vue` | P0 | 1. Store updates `activePools` and `currentPool`<br>2. "Join" button replaced with "Joined" indicator<br>3. Avatar appears in slot |
| **AC 8** | Clean empty state when no open pools exist | Component Vitest | `ActivePoolsList.vue` | P1 | 1. Empty state banner rendered when `activePools` is empty |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend Controller ATDD Tests:** [`PoolControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-4/PoolControllerATDDTest.java) (5 scenarios covering GET active pools, POST join 200 OK, 409 duplicate conflict, 409 not open conflict, 404 not found, 401 unauthenticated)
- **Backend Service Unit Tests:** [`PoolServiceTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-4/PoolServiceTest.java) (6 scenarios covering active pools query, join pool success, auto-fill status change, duplicate participant rejection, closed pool rejection, not found handling)
- **Frontend Pinia Store Tests:** [`poolStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-4/poolStore.spec.ts) (3 store unit test scenarios for `fetchActivePools`, `joinPool`, and error state handling)
- **Frontend Component Tests:** [`ActivePoolsList.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-4/ActivePoolsList.spec.ts) (4 component test scenarios for card rendering, Join button, Joined badge, and empty state)
- **Frontend E2E Tests:** [`want-to-play-pool.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/want-to-play-pool.spec.ts) (Extended scenarios covering browse & join flow)

## Next Steps (Task-by-Task Activation)

During implementation of Story 6.4 in `dev-story`:
1. **Task 1 (Backend API & Service Enhancements):**
   - Update `MatchmakingPoolRepository.java` with `findByStatusOrderByCreatedAtDesc`.
   - Update `PoolService.java` & `PoolServiceImpl.java` with `getActivePools` and `joinPool`.
   - Update `PoolController.java` with `GET /api/v1/pools` and `POST /api/v1/pools/{id}/join`.
   - Merge red-phase tests into `src/test/java/com/tictactore/service/PoolServiceTest.java` and `src/test/java/com/tictactore/controller/PoolControllerATDDTest.java`.
   - Verify all backend unit and ATDD tests pass.
2. **Task 2 (Frontend Types, Service & Pinia Store):**
   - Update `frontend/src/features/matchmaking/services/poolService.ts` with `fetchActivePools` and `joinPool`.
   - Update `frontend/src/features/matchmaking/stores/poolStore.ts` with `fetchActivePools` and `joinPool` actions.
   - Merge red-phase tests into `frontend/src/features/matchmaking/stores/__tests__/poolStore.spec.ts`.
3. **Task 3 (Frontend UI Components & Home Hub Integration):**
   - Create `frontend/src/features/matchmaking/components/ActivePoolsList.vue` following Clubhouse styling and 500-Line Rule (`IP-04`).
   - Move/activate `ActivePoolsList.spec.ts` into `frontend/src/features/matchmaking/components/__tests__/`.
   - Mount `<ActivePoolsList />` on `HomeView.vue`.
   - Add i18n translation keys in `en.json` and `de.json`.
4. **Task 4 (Testing & Quality Verification):**
   - Extend `frontend/e2e/want-to-play-pool.spec.ts` with join pool journey.
   - Run unit, component, and E2E tests, then run full verification `./scripts/ci-local.sh`.
