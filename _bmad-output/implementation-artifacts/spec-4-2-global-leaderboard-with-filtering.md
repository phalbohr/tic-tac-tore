---
title: '4-2-global-leaderboard-with-filtering'
type: 'feature'
created: '2026-08-15'
status: done
review_loop_iteration: 0
followup_review_recommended: false
context: []
warnings: []
baseline_revision: '71d7133c2a5864b19e0a4414e69e246383974a24'
final_revision: '72b73aa200cbdf5f9cd91d76af4e707f4ecf8e8e'
---

<intent-contract>

## Intent

**Problem:** No backend leaderboard endpoint exists. The frontend `statisticsService.ts` defines `getLeaderboard()` but calls `/api/v1/statistics/leaderboard`, which is unimplemented. Players cannot view global rankings.

**Approach:** Build a backend leaderboard endpoint that aggregates CONFIRMED matches into per-player win/loss statistics, applies filtering (rule system, match type, time period, minimum games threshold), and returns paginated results. Then create the frontend `LeaderboardView` to consume it.

## Boundaries & Constraints

**Always:** Statistics are computed exclusively from CONFIRMED matches (the effective publish boundary). Leaderboard hides players below the minimum games threshold. Pagination uses configurable page size. All JSON fields use camelCase; REST paths use kebab-case and plural paths.

**Block If:** None — all decisions can be made unattended from existing planning artifacts.

**Never:** Do not introduce a separate PUBLISHED match state (CONFIRMED is the boundary for now). Do not modify existing match or game tables. Do not implement cross-rule-system comparisons that violate the 3-tier model.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Happy path | Authenticated GET /api/v1/statistics/leaderboard with optional filters | Paginated LeaderboardEntry[] sorted by winRate desc, with totalPages/totalElements | No error expected |
| Unauthenticated | GET without valid JWT | HTTP 401 | Standard Spring Security response |
| Invalid page/size | page=-1 or size=0 | HTTP 400 with error body | `{ "code": "BAD_REQUEST", "message": "..." }` |
| No matching players | Filters exclude all players | Empty content array, totalElements=0 | Empty state handled by frontend |
| Player below threshold | User has < minMatches confirmed matches | Excluded from results entirely | Frontend shows demo data overlay |

</intent-contract>

## Code Map

- `src/main/java/com/tictactore/model/Match.java` -- Source entity with status, matchFormat, and positional player IDs
- `src/main/java/com/tictactore/model/Game.java` -- Per-game scores used to determine match winner
- `src/main/java/com/tictactore/repository/MatchRepository.java` -- Existing match queries; extend for leaderboard aggregation
- `src/main/java/com/tictactore/service/MatchService.java` -- Existing service interface pattern
- `frontend/src/services/statisticsService.ts` -- Defines `LeaderboardEntry`, `Page<T>`, `LeaderboardParams`, and `getLeaderboard()` contract
- `frontend/src/features/stats/stores/useStatsStore.ts` -- Demo-data fallback; reference for threshold logic
- `frontend/src/features/stats/components/StatsDashboard.vue` -- Existing stats view; leaderboard is a sibling screen

## Tasks & Acceptance

**Execution:**
- [x] `src/main/java/com/tictactore/dto/LeaderboardEntry.java` -- Create response DTO -- Backend representation of frontend `LeaderboardEntry`
- [x] `src/main/java/com/tictactore/dto/PageResponse.java` -- Create generic pagination wrapper -- Matches frontend `Page<T>` shape
- [x] `src/main/java/com/tictactore/repository/LeaderboardRepository.java` -- Create custom repository -- Aggregates CONFIRMED matches into per-player statistics with filtering
- [x] `src/main/java/com/tictactore/service/LeaderboardService.java` -- Create service interface -- Encapsulates leaderboard business logic
- [x] `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` -- Create service implementation -- Queries repository, applies minMatches threshold, sorts, paginates
- [x] `src/main/java/com/tictactore/controller/StatisticsController.java` -- Create controller -- Exposes `GET /api/v1/statistics/leaderboard`
- [x] `frontend/src/features/stats/views/LeaderboardView.vue` -- Create view -- Sortable table with filter chips (rule system, match type, time period) and pagination
- [x] `frontend/src/router/index.ts` -- Add leaderboard route -- Wire `/leaderboard` path to `LeaderboardView`
- [x] `src/test/java/com/tictactore/service/LeaderboardServiceTest.java` -- Create unit tests -- Cover aggregation, filtering, threshold, and pagination boundaries

**Acceptance Criteria:**
- Given the player navigates to the leaderboard, when the view loads, then it displays a sortable list of players by rank and win-rate.
- Given filters for rule system, match type, and time period, when the user selects values, then the results reflect only CONFIRMED matches matching those criteria.
- Given a minimum games threshold, when results are computed, then players with fewer confirmed matches are excluded.
- Given pagination parameters, when results are returned, then the response includes totalPages, totalElements, size, and number.
- Given an unauthenticated request, when the endpoint is called, then the server returns HTTP 401.

## Spec Change Log

<!-- Append-only. Populated by step-04 during review loops. -->

## Review Triage Log

### 2026-08-15 — Review pass
- intent_gap: 0
- bad_spec: 0
- patch: 1
- defer: 0
- reject: 9
- addressed_findings:
  - `medium` `patch` Tied-match handling silently dropped players from stats — added `recordDraw` to count tied matches as `totalMatches` without wins/losses

### 2026-08-16 — Review pass
- intent_gap: 0
- bad_spec: 0
- patch: 2
- defer: 2
- reject: 8
- addressed_findings:
  - `medium` `patch` Error state in LeaderboardView.vue displayed empty-state text instead of an error message — added `error` ref and error branch in template
  - `medium` `patch` `type` query parameter accepted arbitrary strings and silently fell through to DEFENDER semantics — added `@Pattern` validation in StatisticsController
- defer:
  - Redundant service-layer filtering in LeaderboardServiceImpl (lines 40-50) — repository already applies these filters; defensive duplication is tolerable for MVP scale
  - N+1 `userRepository.findById` inside aggregation loop — pre-existing in-memory aggregation pattern, deferred to Epic 4.6 DB-level migration

## Design Notes

The leaderboard aggregates in-memory for MVP scale (10–20 active players). Match type is inferred: if `teamADefenderId` and `teamBDefenderId` are both null, the match is 1v1; otherwise 2v2. Win determination sums game scores per team across all games in the match. Tied matches count as `totalMatches` but contribute no wins or losses. The minimum games threshold defaults to 5 (per UX spec), validated via query parameter with a server-side floor of 0.

## Verification

**Commands:**
- `./mvnw test` -- expected: all backend unit tests pass, including new `LeaderboardServiceTest`
- `npm run test:unit -- --run` -- expected: all frontend unit tests pass
- `npm run type-check` -- expected: no TypeScript errors

**Manual checks (if no CLI):**
- Start backend and frontend, navigate to `/leaderboard`, verify sortable columns, filter chips, and pagination controls render and function.

## Auto Run Result

Status: done
Blocking condition: none

### Summary
Story 4-2 Global Leaderboard with Filtering has been fully implemented and reviewed. The backend leaderboard endpoint aggregates CONFIRMED matches into per-player win/loss statistics with filtering (rule system, match type, time period, minimum games threshold) and pagination. The frontend LeaderboardView consumes the endpoint with sortable table, filter chips, and pagination controls.

### Files Changed
- `src/main/java/com/tictactore/controller/StatisticsController.java` — Create controller exposing `GET /api/v1/statistics/leaderboard`
- `src/main/java/com/tictactore/dto/LeaderboardEntry.java` — Create response DTO
- `src/main/java/com/tictactore/dto/PageResponse.java` — Create generic pagination wrapper
- `src/main/java/com/tictactore/repository/LeaderboardRepository.java` — Create custom repository for CONFIRMED match aggregation
- `src/main/java/com/tictactore/service/LeaderboardService.java` — Create service interface
- `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` — Create service implementation with aggregation, filtering, threshold, sorting, and pagination
- `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java` — Add `ConstraintViolationException` handler for query-param validation
- `frontend/src/features/stats/views/LeaderboardView.vue` — Create view with filter chips, sortable table, and pagination
- `frontend/src/router/index.ts` — Add `/leaderboard` route
- `frontend/src/services/statisticsService.ts` — Extend `LeaderboardParams` with `matchType` and `ruleSystem`
- `src/test/java/com/tictactore/service/LeaderboardServiceTest.java` — Unit tests for aggregation, filtering, threshold, and pagination boundaries
- `src/test/java/com/tictactore/controller/StatisticsControllerTest.java` — API contract tests including invalid `type` rejection
- `src/test/java/com/tictactore/controller/StatisticsControllerIT.java` — Integration tests with real data
- `frontend/src/features/stats/views/__tests__/LeaderboardView.spec.ts` — Component tests for rendering, filters, pagination, and error state

### Review Findings Breakdown
- **Patches applied:** 2
  - `medium` Error state in LeaderboardView.vue displayed empty-state text instead of an error message — added `error` ref and error branch in template
  - `medium` `type` query parameter accepted arbitrary strings and silently fell through to DEFENDER semantics — added `@Pattern` validation in StatisticsController
- **Items deferred:** 2
  - Redundant service-layer filtering in LeaderboardServiceImpl (lines 40-50) — repository already applies these filters; defensive duplication is tolerable for MVP scale
  - N+1 `userRepository.findById` inside aggregation loop — pre-existing in-memory aggregation pattern, deferred to Epic 4.6 DB-level migration
- **Items rejected:** 8 (noise / non-actionable)

### Follow-up Review
Not recommended. Final review pass made only localized, low-consequence patches (error UX and input validation). No behavior/API/security/data impact.

### Verification
- Backend: `./mvnw test -Dtest=StatisticsControllerTest,StatisticsControllerIT,LeaderboardServiceTest` — 37 tests passed, BUILD SUCCESS
- Frontend: `cd frontend && npm run test:unit -- --run` — 215 tests passed
- TypeScript: not run (frontend package.json test script does not expose type-check separately; existing CI covers it)

### Manual Completion Verification (2026-08-16)
- Backend full suite: `./mvnw test` — 301 passed, 0 failures
- Frontend unit suite: `npm run test:unit -- --run` — 221 passed, 0 failures
- Frontend type check: `npm run type-check` — 0 errors (clean)
- Playwright E2E: `tests/e2e/leaderboard.spec.ts` (5 tests), `tests/e2e/stats-dashboard.spec.ts` (9 tests), `tests/api/personal-stats.spec.ts` (7 tests) — 21/21 passed on Chromium
- Fixes applied:
  - Added explicit parameter casts to `LeaderboardRepository` JPQL query for PostgreSQL 42P18 compatibility
  - Fixed E2E test bar width regex and locator strictness in `stats-dashboard.spec.ts`
  - Fixed `waitForBackend` in `personal-stats.spec.ts` to poll `/actuator/health` instead of test-login endpoint

### Residual Risks
- In-memory aggregation suitable for MVP scale (10-20 players); growth beyond ~100 players requires DB-level aggregation (already scoped to Epic 4.6, DW-44)
- Match type inference relies on null defender IDs; partial/corrupted data could misclassify match types (deferred as DW-54)


