---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-28T19:25:00+02:00'
storyId: '6.3'
storyKey: '6-3-create-want-to-play-pool'
storyFile: '_bmad-output/implementation-artifacts/6-3-create-want-to-play-pool.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-6-3-create-want-to-play-pool.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-6-3/PoolControllerATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-6-3/PoolServiceTest.java'
  - 'frontend/e2e/want-to-play-pool.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-3/poolStore.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-6-3/CreatePoolModal.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/6-3-create-want-to-play-pool.md'
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

# Acceptance Test-Driven Development (ATDD) Checklist: Story 6.3

## Story Context
- **Story Key:** `6-3-create-want-to-play-pool`
- **Story ID:** `6.3`
- **Title:** Story 6.3: Create "Want to Play" Pool
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/6-3-create-want-to-play-pool.md`

## Acceptance Criteria Summary
1. **AC 1:** Given an authenticated user on the Home Hub (`/`), when they tap the "Want to Play" action button, then a dedicated `CreatePoolModal.vue` dialog opens, presenting a creation form with options for Match Type (`1v1` / `2v2`), Start Condition (`FILL_BASED` / `SCHEDULED_TIME`), and optional Skill Level filter (`OPEN_FOR_ALL`, `BEGINNER`, `INTERMEDIATE`, `ADVANCED`).
2. **AC 2:** Given an authenticated user configuring an immediate/fill-based pool (`startCondition == FILL_BASED`), when they select Match Type (`1v1` requiring 2 total players or `2v2` requiring 4 total players) and submit the form via `POST /api/v1/pools`, then the backend persists the new pool with status `OPEN`, sets `creator_id` to the authenticated user's ID, automatically registers the creator as the first participant (`HOST`), and returns `201 Created` with the complete `PoolResponse` (FR35).
3. **AC 3:** Given an authenticated user configuring a scheduled pool (`startCondition == SCHEDULED_TIME`), when they specify a valid future date/time (between now and +7 days in ISO 8601 UTC) and submit the form via `POST /api/v1/pools`, then the backend persists the scheduled pool with `scheduled_time`, registers the creator as the initial participant, and returns `201 Created` with the complete `PoolResponse` (FR35).
4. **AC 4:** Given a user attempting to create a pool with invalid parameters (e.g. `scheduled_time` in the past or missing when `startCondition == SCHEDULED_TIME`, or invalid match format), when the request is received by `POST /api/v1/pools`, then the backend rejects the request with `400 Bad Request` containing standard validation error details, or `401 Unauthorized` if unauthenticated.
5. **AC 5:** Given an authenticated user who already has 3 active open pools (`status == OPEN`), when they attempt to create another pool, then the backend rejects the request with `400 Bad Request` and message `"Maximum active pools limit reached (3)"` to prevent pool spam.
6. **AC 6:** Given a user successfully submits the pool creation form in the frontend, when `POST /api/v1/pools` returns `201 Created`, then the modal closes, a success toast notification appears confirming pool creation, and the new pool is recorded in `usePoolStore`.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear REST API specification for `POST /api/v1/pools` and `GET /api/v1/pools/{id}`, well-defined database relations and quota constraints, modular Clubhouse modal UI design, and existing Playwright & MockMvc test fixtures.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | "Want to Play" button on Home Hub opens `CreatePoolModal.vue` | Component (Vitest) & E2E | `HomeView.vue` & `CreatePoolModal.vue` | P0 | 1. Button launches modal<br>2. Form has MatchType (1v1/2v2), StartCondition (FILL/SCHEDULED), SkillLevel options<br>3. DateTime picker appears conditionally when SCHEDULED_TIME is selected |
| **AC 2** | Create fill-based pool via `POST /api/v1/pools` | Controller ATDD / Service Unit | `PoolController` & `PoolService` | P0 | 1. 201 Created with status `OPEN`<br>2. `creator_id` set to authenticated user<br>3. Creator registered as first participant with role `HOST`<br>4. Required players set (2 for 1v1, 4 for 2v2) |
| **AC 3** | Create scheduled pool with valid future time (+7 days) | Controller ATDD / Service Unit | `PoolController` & `PoolService` | P0 | 1. 201 Created with persisted `scheduled_time`<br>2. Creator registered as HOST<br>3. Skill level saved |
| **AC 4** | Fail-fast parameter validation & Auth rejection | Controller ATDD / Service Unit | `PoolController` & `PoolService` | P1 | 1. Missing or past `scheduled_time` returns 400 Bad Request<br>2. `scheduled_time` beyond 7 days returns 400 Bad Request<br>3. `scheduled_time` on fill-based pool returns 400 Bad Request<br>4. Unauthenticated request returns 401 Unauthorized |
| **AC 5** | Max active pool quota (3 active `OPEN` pools per creator) | Controller ATDD / Service Unit / E2E | `PoolService` & `CreatePoolModal.vue` | P0 | 1. 4th pool attempt rejected with 400 Bad Request ("Maximum active pools limit reached (3)")<br>2. Error displayed in modal UI |
| **AC 6** | Frontend submit flow: modal close, toast notification, store recording | Pinia Unit & Playwright E2E | `poolStore.ts` & `want-to-play-pool.spec.ts` | P0 | 1. Successful POST closes modal<br>2. Toast notification displayed<br>3. Pool recorded in `usePoolStore.activePools` and `currentPool` |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend Controller ATDD Tests:** [`PoolControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-3/PoolControllerATDDTest.java) (7 scenarios covering 201 Created for 1v1/2v2, 400 validation, 400 quota limit, 401 unauthenticated, GET /api/v1/pools/{id})
- **Backend Service Unit Tests:** [`PoolServiceTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-3/PoolServiceTest.java) (6 scenarios covering business rules, optimistic lock readiness, host participant creation, and quota enforcement)
- **Frontend E2E Tests:** [`want-to-play-pool.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/want-to-play-pool.spec.ts) (3 user journeys with `test.skip()`)
- **Frontend Pinia Store Tests:** [`poolStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-3/poolStore.spec.ts) (4 store unit test scenarios)
- **Frontend Component Tests:** [`CreatePoolModal.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-6-3/CreatePoolModal.spec.ts) (3 component test scenarios)

## Next Steps (Task-by-Task Activation)

During implementation of Story 6.3 in `dev-story`:
1. **Task 1 (Database Migration & JPA Entities):**
   - Create Flyway migration `src/main/resources/db/migration/V13__create_matchmaking_pools.sql`.
   - Create enums (`MatchType`, `StartCondition`, `PoolStatus`, `SkillLevel`, `PoolParticipantRole`) and entities (`MatchmakingPool`, `PoolParticipant`).
   - Create repositories (`MatchmakingPoolRepository`, `PoolParticipantRepository`) and test queries in `MatchmakingPoolRepositoryTest.java`.
2. **Task 2 (Backend DTOs, Service & Controller):**
   - Move/activate `PoolControllerATDDTest.java` to `src/test/java/com/tictactore/controller/` and `PoolServiceTest.java` to `src/test/java/com/tictactore/service/`.
   - Implement `CreatePoolRequest`, `PoolResponse`, `PoolParticipantDto`, `PoolService`, `PoolServiceImpl`, and `PoolController`.
   - Verify all backend unit and ATDD tests pass.
3. **Task 3 (Frontend Types, Service & Pinia Store):**
   - Move/activate `poolStore.spec.ts` to `frontend/src/features/matchmaking/stores/__tests__/`.
   - Implement `pool.ts` types, `poolService.ts` API client, and `poolStore.ts` Pinia store.
4. **Task 4 (Frontend UI Components & Home Hub Integration):**
   - Move/activate `CreatePoolModal.spec.ts` to `frontend/src/features/matchmaking/components/__tests__/`.
   - Implement `CreatePoolModal.vue` following Clubhouse styling (no 1px borders, elevation/tonal surfaces) and 500-Line Rule (`IP-04`).
   - Add "Want to Play" action button to `HomeView.vue`.
   - Add i18n translation keys in `en.json` and `de.json`.
5. **Task 5 (Testing & Quality Verification):**
   - Activate `frontend/e2e/want-to-play-pool.spec.ts` (remove `test.skip()`).
   - Run unit, component, and E2E tests, then run full verification `./scripts/ci-local.sh`.
