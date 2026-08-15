---
stepsCompleted:
  - step-01-preflight-and-context
  - step-02-generation-mode
  - step-03-test-strategy
  - step-04-generate-tests
  - step-04c-aggregate
  - step-05-validate-and-complete
lastStep: step-05-validate-and-complete
lastSaved: '2026-08-15T18:56:00+02:00'
storyId: '4.2'
storyKey: 4-2-global-leaderboard-with-filtering
storyFile: _bmad-output/implementation-artifacts/spec-4-2-global-leaderboard-with-filtering.md
atddChecklistPath: _bmad-output/test-artifacts/atdd-checklist-4-2-global-leaderboard-with-filtering.md
generatedTestFiles:
  - _bmad-output/test-artifacts/atdd-redphase-4-2/StatisticsControllerATDDTest.java
  - _bmad-output/test-artifacts/atdd-redphase-4-2/LeaderboardView.spec.ts
inputDocuments:
  - _bmad-output/implementation-artifacts/spec-4-2-global-leaderboard-with-filtering.md
  - _bmad-output/test-artifacts/test-design/test-design-epic-4.md
  - _bmad/tea/config.yaml
  - src/main/java/com/tictactore/controller/StatisticsController.java
  - src/main/java/com/tictactore/dto/LeaderboardEntry.java
  - src/main/java/com/tictactore/dto/PageResponse.java
  - src/main/java/com/tictactore/service/LeaderboardService.java
  - src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java
  - src/main/java/com/tictactore/repository/LeaderboardRepository.java
  - frontend/src/features/stats/views/LeaderboardView.vue
  - frontend/src/services/statisticsService.ts
  - frontend/src/router/index.ts
---

# ATDD Checklist: Story 4.2 - Global Leaderboard with Filtering

## TDD Red-Phase Scaffolds Generated

🔴 **Red-phase test scaffolds generated** (TDD red phase — tests assert expected behavior and would fail if implementation were absent).

### Generated Test Files:

1. **Backend Controller ATDD Spec**:
   - `_bmad-output/test-artifacts/atdd-redphase-4-2/StatisticsControllerATDDTest.java`
   - Covers `GET /api/v1/statistics/leaderboard` endpoint contracts: 200 with paginated leaderboard, 401 unauthenticated, 400 invalid params, empty results, filter validation (matchFormat, matchType, period), pagination metadata

2. **Frontend Component Unit Spec**:
   - `_bmad-output/test-artifacts/atdd-redphase-4-2/LeaderboardView.spec.ts`
   - Covers table rendering with entries, empty state, loading skeleton, filter changes (matchFormat, matchType, period), pagination navigation, rank display, default minMatches=5, filter change resets page

---

## Step 01: Preflight & Context
- Stack detected: fullstack
- Test Framework: JUnit 5 + MockMvc (backend), Vitest + Vue Test Utils (frontend)
- Loaded Core and UI+API Playwright Utils knowledge base fragments.

## Step 02: Generation Mode
- Mode selected: **AI Generation**
- Reason: Acceptance criteria are clear and standard (REST endpoint + Vue component). Implementation already present in working tree; tests assert expected behavior per spec.

## Step 03: Test Strategy

### Mapped Scenarios & Test Levels
1. **API Level (StatisticsController)**
   - GET /leaderboard returns 200 with paginated LeaderboardEntry[] when authenticated
   - GET /leaderboard returns 401 without JWT
   - Invalid page/size returns 400
   - No matching players returns empty content array with totalElements=0
   - Filter params (matchFormat, matchType, period) are validated and delegated to service
   - Pagination metadata (totalPages, totalElements, size, number) present in response

2. **Component Level (LeaderboardView.vue)**
   - Renders sortable table with player entries, ranks, and win rates
   - Filter chips (rule system, match type, time period) trigger API calls with correct params
   - Pagination controls render when totalPages > 1
   - Empty state shown when no entries match filters
   - Loading skeleton shown during fetch
   - minMatches defaults to 5
   - Filter changes reset page to 0

### Priorities
- API Tests: **P0** (endpoint contract, auth, validation)
- Component Tests: **P0** (critical user journey for leaderboard viewing)

### Red Phase Requirements
All tests are designed as red-phase scaffolds. They assert expected behavior per acceptance criteria and would fail if the implementation were absent.

---

## Acceptance Criteria Traceability

| AC # | Acceptance Criterion | Test Spec Coverage | Priority | Status |
|---|---|---|---|---|
| AC1 | Given the player navigates to the leaderboard, when the view loads, then it displays a sortable list of players by rank and win-rate. | `LeaderboardView.spec.ts` renders-leaderboard-table-with-entries | P0 | 🔴 Red (Scaffold) |
| AC2 | Given filters for rule system, match type, and time period, when the user selects values, then the results reflect only CONFIRMED matches matching those criteria. | `StatisticsControllerATDDTest` shouldFilterByMatchFormat, shouldFilterByMatchType, shouldFilterByPeriod; `LeaderboardView.spec.ts` filter change tests | P0 | 🔴 Red (Scaffold) |
| AC3 | Given a minimum games threshold, when results are computed, then players with fewer confirmed matches are excluded. | `StatisticsControllerATDDTest` shouldRespectMinMatchesThreshold; `LeaderboardView.spec.ts` passes-minMatches-5-by-default | P0 | 🔴 Red (Scaffold) |
| AC4 | Given pagination parameters, when results are returned, then the response includes totalPages, totalElements, size, and number. | `StatisticsControllerATDDTest` shouldReturnPaginationMetadata, shouldReturn200WithLeaderboard | P0 | 🔴 Red (Scaffold) |
| AC5 | Given an unauthenticated request, when the endpoint is called, then the server returns HTTP 401. | `StatisticsControllerATDDTest` shouldReturn401WhenUnauthenticated | P0 | 🔴 Red (Scaffold) |

---

## Red-Phase Test Summary

| Category | Test Count | All Skipped/Disabled | Expected to Fail Without Implementation |
|---|---|---|---|
| Backend Controller ATDD | 12 | Yes (`@Disabled` on all tests) | Yes |
| Frontend Component Unit | 10 | Yes (`test.skip()` on all tests) | Yes |
| **Total** | **22** | — | — |

> **Note:** All tests are currently in red phase (disabled/scaffold). They assert expected behavior per acceptance criteria and would fail if the implementation were absent. Since Story 4.2 is already implemented, these serve as regression-guard scaffolds. Remove `@Disabled` / `test.skip()` to activate them for CI.

---

## Working Tree Changes (2026-08-15)

**Unstaged working tree changes:** Production code + test code + documentation.

| File | Change | Production Impact |
|------|--------|-------------------|
| `src/main/java/com/tictactore/controller/StatisticsController.java` | New REST controller with GET /leaderboard | Yes |
| `src/main/java/com/tictactore/dto/LeaderboardEntry.java` | New response DTO | Yes |
| `src/main/java/com/tictactore/dto/PageResponse.java` | New pagination wrapper | Yes |
| `src/main/java/com/tictactore/repository/LeaderboardRepository.java` | New custom repository with CONFIRMED match aggregation query | Yes |
| `src/main/java/com/tictactore/service/LeaderboardService.java` | New service interface | Yes |
| `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` | New service implementation with in-memory aggregation, filtering, tie handling | Yes |
| `src/main/java/com/tictactore/model/Position.java` | New enum for position filtering | Yes |
| `src/test/java/com/tictactore/service/LeaderboardServiceTest.java` | New unit tests (12 tests) | Test code |
| `frontend/src/features/stats/views/LeaderboardView.vue` | New Vue view with sortable table, filters, pagination | Yes |
| `frontend/src/router/index.ts` | Added /leaderboard route | Yes |
| `frontend/src/services/statisticsService.ts` | Extended with matchType and ruleSystem params | Yes |
| `_bmad-output/implementation-artifacts/spec-4-2-global-leaderboard-with-filtering.md` | Story spec created | Documentation |
| `_bmad-output/implementation-artifacts/epic-4-context.md` | Epic context updated | Documentation |
| `_bmad-output/implementation-artifacts/bmad-dev-auto-result-4-2-global-leaderboard-with-filtering-tea.td-1.md` | Test design metadata | Documentation |
| `_bmad-output/test-artifacts/test-design/test-design-epic-4.md` | Epic test design | Documentation |
| `_bmad-output/test-artifacts/test-design-progress.md` | Progress tracker | Documentation |

---

## Implementation Checklist (Working Tree Changes)

### Backend Production Code

- [x] `src/main/java/com/tictactore/dto/LeaderboardEntry.java` — New record with playerId, playerName, totalMatches, wins, losses, winRate
- [x] `src/main/java/com/tictactore/dto/PageResponse.java` — New generic pagination wrapper with content, totalPages, totalElements, size, number
- [x] `src/main/java/com/tictactore/model/Position.java` — New enum: OVERALL, ATTACKER, DEFENDER
- [x] `src/main/java/com/tictactore/repository/LeaderboardRepository.java` — New interface extending JpaRepository with `findConfirmedMatchesWithFilters` JPQL query filtering by matchFormat, matchType (1v1/2v2), date range
- [x] `src/main/java/com/tictactore/service/LeaderboardService.java` — New interface with `getLeaderboard` method
- [x] `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` — In-memory aggregation of CONFIRMED matches into per-player stats; filters by type (OVERALL/ATTACKER/DEFENDER), period, matchFormat, matchType, minMatches; sorts by winRate desc, then wins, then name; handles tied matches as totalMatches without win/loss
- [x] `src/main/java/com/tictactore/controller/StatisticsController.java` — New `@RestController` at `/api/v1/statistics` with `GET /leaderboard` accepting type, period, minMatches, matchFormat, matchType, page, size; validated with @Min/@Max/@Pattern

### Backend Test Code

- [x] `src/test/java/com/tictactore/service/LeaderboardServiceTest.java` — 12 unit tests covering aggregation, filtering, threshold, pagination, sorting, tied matches, position filtering, edge cases

### Frontend Production Code

- [x] `frontend/src/features/stats/views/LeaderboardView.vue` — New Vue view with filter selects (matchFormat, matchType, period), sortable table (Rank, Player, Matches, Wins, Losses, Win Rate), pagination controls (Previous/Next, page indicator), loading skeleton, empty state
- [x] `frontend/src/router/index.ts` — Added `/leaderboard` route pointing to LeaderboardView
- [x] `frontend/src/services/statisticsService.ts` — Extended `LeaderboardParams` with matchType and ruleSystem fields; updated `getLeaderboard` to pass them as query params

### Frontend Test Code

- [ ] `frontend/src/features/stats/views/__tests__/LeaderboardView.spec.ts` — Component tests (not yet created in project test dir; red-phase scaffold in ATDD artifacts)

---

## Task-by-Task Activation Plan

1. **Task 1 (DTOs + Model)**:
   - `LeaderboardEntry`, `PageResponse`, `Position` — data contracts
   - Tests: `LeaderboardServiceTest` aggregation specs passing

2. **Task 2 (Repository)**:
   - `LeaderboardRepository` — JPQL query with CONFIRMED status filter and match type inference
   - Tests: Repository integration verified via service tests

3. **Task 3 (Service)**:
   - `LeaderboardService` + `LeaderboardServiceImpl` — in-memory aggregation, filtering, sorting, pagination
   - Tests: 12 unit tests passing

4. **Task 4 (Controller)**:
   - `StatisticsController` — REST endpoint with validation
   - Tests: `StatisticsControllerATDDTest` endpoint specs

5. **Task 5 (Frontend)**:
   - `LeaderboardView.vue` — sortable table, filters, pagination
   - `statisticsService.ts` — extended params
   - `router/index.ts` — route registration
   - Tests: `LeaderboardView.spec.ts` component specs

---

## Verification Commands

- `./mvnw test` — expected: all backend unit tests pass (277+ tests), including `LeaderboardServiceTest`
- `npm run test:unit -- --run` — expected: all frontend unit tests pass (203+ tests)
- `npm run type-check` — expected: 0 TypeScript errors
- `npm run build` — expected: production bundle built successfully

## Next Recommended Workflow

- **Regression guard:** Run full test suite before merging to ensure leaderboard aggregation and filtering do not break existing statistics flows
- **Downstream dependency:** None — feature is self-contained within statistics module
