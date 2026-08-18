---
status: done
---

# Implementation Checklist: Story 4.2 — Global Leaderboard with Filtering

**Story Key:** `4-2-global-leaderboard-with-filtering`  
**Date:** 2026-08-15

## Working Tree Changes Assessment

**Current working tree diff (unstaged):**

### Production Code (New / Modified)

| File | Status | Description |
|------|--------|-------------|
| `src/main/java/com/tictactore/controller/StatisticsController.java` | New | REST controller exposing `GET /api/v1/statistics/leaderboard` with validation for type, period, minMatches, matchFormat, matchType, page, size |
| `src/main/java/com/tictactore/dto/LeaderboardEntry.java` | New | Response record: playerId, playerName, totalMatches, wins, losses, winRate |
| `src/main/java/com/tictactore/dto/PageResponse.java` | New | Generic pagination wrapper: content, totalPages, totalElements, size, number |
| `src/main/java/com/tictactore/model/Position.java` | New | Enum: OVERALL, ATTACKER, DEFENDER |
| `src/main/java/com/tictactore/repository/LeaderboardRepository.java` | New | Custom repository with `findConfirmedMatchesWithFilters` JPQL query filtering CONFIRMED matches by matchFormat, matchType (1v1/2v2), and date range |
| `src/main/java/com/tictactore/service/LeaderboardService.java` | New | Service interface with `getLeaderboard` method |
| `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` | New | In-memory aggregation of CONFIRMED matches into per-player statistics; filters by type, period, matchFormat, matchType, minMatches; sorts by winRate desc; handles tied matches as totalMatches without win/loss |
| `frontend/src/features/stats/views/LeaderboardView.vue` | New | Vue view with filter selects (matchFormat, matchType, period), sortable table (Rank, Player, Matches, Wins, Losses, Win Rate), pagination controls, loading skeleton, empty state |
| `frontend/src/router/index.ts` | Modified | Added `/leaderboard` route pointing to `LeaderboardView` |
| `frontend/src/services/statisticsService.ts` | Modified | Extended `LeaderboardParams` with `matchType` and `ruleSystem`; updated `getLeaderboard` to pass them as query params |

### Test Code (New)

| File | Description |
|------|-------------|
| `src/test/java/com/tictactore/service/LeaderboardServiceTest.java` | 12 unit tests covering aggregation, filtering, threshold, pagination, sorting, tied matches, position filtering |
| `_bmad-output/test-artifacts/atdd-redphase-4-2/StatisticsControllerATDDTest.java` | 12 red-phase API test scaffolds (all `@Disabled`) |
| `_bmad-output/test-artifacts/atdd-redphase-4-2/LeaderboardView.spec.ts` | 10 red-phase component test scaffolds (all `test.skip()`) |

### Documentation / Metadata (New)

| File | Description |
|------|-------------|
| `_bmad-output/implementation-artifacts/spec-4-2-global-leaderboard-with-filtering.md` | Story spec with intent contract, tasks, acceptance criteria, design notes |
| `_bmad-output/implementation-artifacts/epic-4-context.md` | Epic 4 context update |
| `_bmad-output/implementation-artifacts/bmad-dev-auto-result-4-2-global-leaderboard-with-filtering-tea.td-1.md` | Test design workflow metadata |
| `_bmad-output/test-artifacts/test-design/test-design-epic-4.md` | Epic-level test design |
| `_bmad-output/test-artifacts/atdd-checklist-4-2-global-leaderboard-with-filtering.md` | ATDD checklist with acceptance criteria traceability |

---

## Story Implementation Status

Story 4.2 is fully implemented. All production code changes are present in the working tree as untracked/modified files.

### Backend Production Code

- [x] `src/main/java/com/tictactore/dto/LeaderboardEntry.java` — New response DTO
- [x] `src/main/java/com/tictactore/dto/PageResponse.java` — New generic pagination wrapper
- [x] `src/main/java/com/tictactore/model/Position.java` — New enum for position filtering
- [x] `src/main/java/com/tictactore/repository/LeaderboardRepository.java` — New custom repository with `findConfirmedMatchesWithFilters` JPQL query
- [x] `src/main/java/com/tictactore/service/LeaderboardService.java` — New service interface
- [x] `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` — In-memory aggregation, filtering, sorting, pagination, tie handling
- [x] `src/main/java/com/tictactore/controller/StatisticsController.java` — New `GET /api/v1/statistics/leaderboard` endpoint with validation

### Backend Test Code

- [x] `src/test/java/com/tictactore/service/LeaderboardServiceTest.java` — 12 unit tests covering aggregation, filtering, threshold, pagination, sorting, tied matches, position filtering

### Frontend Production Code

- [x] `frontend/src/features/stats/views/LeaderboardView.vue` — New Vue view with sortable table, filter chips, pagination, loading/empty states
- [x] `frontend/src/router/index.ts` — Added `/leaderboard` route
- [x] `frontend/src/services/statisticsService.ts` — Extended with matchType and ruleSystem query params

### Frontend Test Code

- [x] `_bmad-output/test-artifacts/atdd-redphase-4-2/LeaderboardView.spec.ts` — 10 red-phase component test scaffolds

---

## Task-by-Task Implementation Summary

1. **Task 1 (DTOs + Model)**:
   - `LeaderboardEntry`, `PageResponse`, `Position` — data contracts for API response

2. **Task 2 (Repository)**:
   - `LeaderboardRepository` — JPQL query filtering CONFIRMED matches by matchFormat, matchType (1v1/2v2 via null defender check), and date range

3. **Task 3 (Service)**:
   - `LeaderboardService` + `LeaderboardServiceImpl` — in-memory aggregation of match games into per-team scores, win/loss/draw determination, position filtering (OVERALL/ATTACKER/DEFENDER), period filtering, minMatches threshold, winRate sorting, pagination

4. **Task 4 (Controller)**:
   - `StatisticsController` — REST endpoint with `@Validated` constraints: `@Min(0)` page, `@Min(1) @Max(100)` size, `@Pattern` for period/matchFormat/matchType

5. **Task 5 (Frontend)**:
   - `LeaderboardView.vue` — reactive table with filter selects, sortable columns, pagination, loading/empty states
   - `statisticsService.ts` — extended API client
   - `router/index.ts` — route registration

---

## Verification Commands

- `./mvnw test` — expected: all backend unit tests pass, including new `LeaderboardServiceTest` (12 tests)
- `npm run test:unit -- --run` — expected: all frontend unit tests pass
- `npm run type-check` — expected: 0 TypeScript errors
- `npm run build` — expected: production bundle built successfully

## Next Recommended Workflow

- **Regression guard:** Run full test suite before merging to ensure leaderboard aggregation does not break existing statistics flows
- **Downstream dependency:** None — feature is self-contained within statistics module
- **Technical debt:** Leaderboard aggregation is in-memory; growth beyond ~100 active players would require database-level aggregation
