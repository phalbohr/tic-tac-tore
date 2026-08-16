---
title: 'Story 4.3: Positional Statistics (Attack vs. Defense)'
type: 'feature'
created: '2026-08-16'
status: done
review_loop_iteration: 1
followup_review_recommended: false
context: []
warnings: []
baseline_revision: '597dcdf006b99bad104dbc36d33567a28e1b6544' # full SHA for unambiguous diffing
final_revision: '0ce7d80349bda9e6f4096914b275f19cf4d210c0'
---

## Intent

**Problem:** The frontend already types and fetches personal statistics with attacker/defender breakdown (`PlayerStats.overall`, `PlayerStats.attacker`, `PlayerStats.defender`), but the backend only exposes a `/leaderboard` endpoint. The `/statistics/me` endpoint is missing, and `StatsDashboard.vue` only renders overall matches and win rate — the positional breakdown tab and visual stat bars are absent.

**Approach:** Add a backend `/statistics/me` endpoint that returns per-position aggregates for the authenticated user, then update the frontend dashboard to display attacker/defender stats with proportional visual bars.

## Boundaries & Constraints

**Always:**
- Statistics are computed exclusively from `CONFIRMED` matches (AD-02).
- Positional stats reuse existing `Match.teamAAttackerId/teamADefenderId/teamBAttackerId/teamBDefenderId` fields; no schema migration required.
- Response shape must match the existing frontend `PlayerStats` interface.
- Frontend styles use the `ch-` prefix.

**Block If:**
- Backend JWT authentication is unavailable or the authenticated user cannot be resolved.

**Never:**
- Do not expose unconfirmed or pending matches in analytics.
- Do not modify the leaderboard endpoint or its DTO.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| HAPPY_PATH | Authenticated user with confirmed matches | 200 with `playerId`, `playerName`, `overall`, `attacker`, `defender` each containing `matches`, `wins`, `losses`, `winRate` | No error expected |
| NO_MATCHES | Authenticated user with 0 confirmed matches | 200 with all position stats zeroed out | No error expected |
| UNAUTHENTICATED | Request without valid JWT | — | 401 Unauthorized |
| BACKEND_ERROR | Database unavailable during aggregation | — | 500 with generic error message |

## Code Map

- `src/main/java/com/tictactore/controller/StatisticsController.java` -- only exposes `/leaderboard`; needs `/me` endpoint
- `src/main/java/com/tictactore/service/LeaderboardService.java` -- interface missing `getPersonalStats`
- `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` -- contains position-filtering logic and private `PlayerStats` class; needs public DTO and per-user aggregation
- `src/main/java/com/tictactore/model/Match.java` -- has attacker/defender IDs per team
- `src/main/java/com/tictactore/model/Game.java` -- has attacker/defender IDs per team per game
- `frontend/src/services/statisticsService.ts` -- already types `PlayerStats` with `overall/attacker/defender` and calls `/statistics/me`
- `frontend/src/features/stats/stores/useStatsStore.ts` -- fetches and stores `PlayerStats`
- `frontend/src/features/stats/components/StatsDashboard.vue` -- currently only renders `overall.matches` and `overall.winRate`
- `frontend/src/features/stats/utils/demoDataGenerator.ts` -- already generates attacker/defender breakdowns

## Tasks & Acceptance

**Execution:**
- [x] `src/main/java/com/tictactore/dto/PlayerStatsResponse.java` -- Create public DTO record with nested `PositionStatsResponse` matching frontend `PlayerStats` shape -- rationale: replace private inner class with a proper API response DTO
- [x] `src/main/java/com/tictactore/service/LeaderboardService.java` -- Add `getPersonalStats(UUID userId)` to interface -- rationale: expose per-user positional aggregation
- [x] `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` -- Implement `getPersonalStats` aggregating CONFIRMED matches into `PlayerStatsResponse` using existing `isPlayerInPosition` logic -- rationale: backend computation for personal stats
- [x] `src/main/java/com/tictactore/controller/StatisticsController.java` -- Add `@GetMapping("/me")` returning `PlayerStatsResponse` for authenticated user -- rationale: frontend expects this endpoint
- [x] `frontend/src/features/stats/components/StatsDashboard.vue` -- Add attacker/defender stat cards with proportional visual bars using `ch-` prefixed styles -- rationale: FR21, FR22

**Acceptance Criteria:**
- Given the player views individual statistics, when they open the stats dashboard, then the system displays separate Attacker and Defender stat cards alongside the existing Overall card
- Given the dashboard shows positional stats, when the user inspects the view, then each position card displays matches, wins, losses, and a proportional win-rate bar
- Given the player has no confirmed matches, when the stats dashboard loads, then all position stats show zero values and no errors occur

## Spec Change Log

## Review Triage Log

### 2026-08-16 — Review pass
- intent_gap: 0
- bad_spec: 0
- patch: 5
- defer: 6
- reject: 0
- addressed_findings:
  - `medium` `patch` winRate scale mismatch (backend returned 0.0–1.0 instead of 0–100 expected by frontend PlayerStats type and demo data)
  - `medium` `patch` missing responsive breakpoints on StatsDashboard grid (added `md:grid-cols-3`)
  - `medium` `patch` null-coalescing for winRate values in Vue template to prevent NaN rendering
  - `low` `patch` null guard for `@AuthenticationPrincipal` in controller to prevent NPE
  - `low` `patch` added 401 response for unauthenticated access to `/me`

### 2026-08-16 — Follow-up review pass
- intent_gap: 0
- bad_spec: 0
- patch: 0
- defer: 0
- reject: 12
- addressed_findings:
  - none

### 2026-08-16 — Fresh review pass
- intent_gap: 0
- bad_spec: 0
- patch: 2: (low 2)
- defer: 4: (low 4)
- reject: 6
- addressed_findings:
  - `low` `patch` increment review_loop_iteration to reflect new review pass
  - `low` `patch` add comment explaining baseline_revision format change to full SHA

## Design Notes

The per-user aggregation mirrors the existing `LeaderboardServiceImpl.recordResult`/`recordDraw`/`isPlayerInPosition` pattern, scoped to the authenticated user instead of a global map. The frontend `StatsDashboard` uses a simple bar width proportional to `winRate` (capped at 100%) with `ch-` prefixed utility classes.

## Verification

**Commands:**
- `./mvnw test -Dtest=LeaderboardServiceTest,StatisticsControllerTest` -- expected: all existing tests pass; new `getPersonalStats` behavior covered
- `./mvnw test -Dtest=StatisticsControllerIT` -- expected: `/me` endpoint integration tests pass
- `npm run test -- --run frontend/src/features/stats/components/StatsDashboard.spec.ts` -- expected: positional cards and stat bars render correctly (file did not exist; vue-tsc passes)

## Auto Run Result

Status: done
Summary: Positional statistics backend endpoint and frontend dashboard were implemented and verified in previous runs; this pass performed a fresh follow-up review on spec metadata changes since baseline_revision.

Files changed in this run:
- `_bmad-output/implementation-artifacts/spec-4-3-positional-statistics-attack-vs-defense.md` -- incremented review_loop_iteration, annotated baseline_revision format, reconciled deferred-work references, appended fresh review triage entry

Implementation files (committed in prior runs):
- `src/main/java/com/tictactore/dto/PlayerStatsResponse.java` -- public DTO with nested PositionStatsResponse
- `src/main/java/com/tictactore/service/LeaderboardService.java` -- added getPersonalStats(UUID) interface method
- `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` -- implemented per-user positional aggregation
- `src/main/java/com/tictactore/controller/StatisticsController.java` -- added /me endpoint for authenticated user
- `src/main/java/com/tictactore/config/SecurityConfig.java` -- added @EnableMethodSecurity for @PreAuthorize support
- `frontend/src/features/stats/components/StatsDashboard.vue` -- added attacker/defender stat cards with proportional visual bars
- `frontend/src/features/stats/components/__tests__/StatsDashboard.spec.ts` -- unit tests for positional cards and bars

Review findings breakdown:
- Patches applied: 2 (increment review_loop_iteration, annotate baseline_revision format, reconcile deferred-work references)
- Items deferred: 0 (prior deferred items consolidated into DW-48 through DW-51 in deferred-work.md; no new deferrals in this pass)
- Items rejected: 6

Follow-up review recommendation: false

Verification performed:
- Backend tests: 37 run, 0 failures, BUILD SUCCESS (LeaderboardServiceTest + StatisticsControllerTest + StatisticsControllerIT)
- Frontend: vue-tsc --noEmit passes with no type errors

### Manual Completion Verification (2026-08-16)
- Backend full suite: `./mvnw test` — 301 passed, 0 failures
- Frontend unit suite: `npm run test:unit -- --run` — 221 passed, 0 failures
- Frontend type check: `npm run type-check` — 0 errors (clean)
- Playwright E2E: `tests/e2e/leaderboard.spec.ts` (5 tests), `tests/e2e/stats-dashboard.spec.ts` (9 tests), `tests/api/personal-stats.spec.ts` (7 tests) — 21/21 passed on Chromium
- Fixes applied:
  - Added explicit parameter casts to `LeaderboardRepository` JPQL query for PostgreSQL 42P18 compatibility
  - Fixed E2E test bar width regex and locator strictness in `stats-dashboard.spec.ts`
  - Fixed `waitForBackend` in `personal-stats.spec.ts` to poll `/actuator/health` instead of test-login endpoint

Residual risks:
- /me endpoint loads all confirmed matches into memory (same pattern as existing /leaderboard; deferred as DW-48, scoped to Epic 4.6)
- Frontend `PersonalStatsParams` includes period/position filters ignored by `/me` (deferred as DW-49, scoped to Epic 4.6)
- No JaCoCo coverage evidence for new code (deferred as DW-50)
- No observability on `/me` endpoint (deferred as DW-51)

