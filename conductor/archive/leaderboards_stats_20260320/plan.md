# Plan: Public Leaderboards & Comprehensive Player Statistics

## Phase 1: Backend Statistics Calculation & APIs [checkpoint: ba38412]
- [x] Task: Implement Statistics DTOs and Repository Queries (45f7a2d)
    - [x] Create `PlayerStatsResponse`, `LeaderboardEntryResponse`, `H2HStatsResponse`.
    - [x] Implement native SQL queries in `MatchRepository` for global rankings and H2H.
- [x] Task: Create Statistics Service (0293ec4)
    - [x] Logic for `getLeaderboard`, `getPersonalStats`, and `getH2HStats`.
- [x] Task: Expose Statistics API Endpoints (ba38412)
    - [x] `GET /statistics/leaderboard`
    - [x] `GET /statistics/me`
    - [x] `GET /statistics/h2h`
- [x] Task: Conductor - User Manual Verification 'Backend Statistics APIs' (Protocol in workflow.md)

## Phase 2: Frontend - Leaderboards & Global Filters [checkpoint: e3a5ae6]
- [x] Task: Create Leaderboard View and Service (e3a5ae6)
    - [x] Implement `statisticsService.ts` for API calls.
    - [x] Create `LeaderboardView.vue` with basic table and role tabs.
- [x] Task: Implement Filters (Time Period & Min Matches) (e3a5ae6)
    - [x] Add dropdowns for `TimePeriod` and `minMatches`.
    - [x] Implement pagination logic.
- [x] Task: Conductor - User Manual Verification 'Frontend Leaderboards' (Protocol in workflow.md)

## Phase 3: Frontend - Player Profile & Position Breakdown [checkpoint: CURRENT]
- [x] Task: Create Player Profile Dashboard (Statistics Page)
    - [x] Create `PlayerStatsSummary.vue` component.
    - [x] Integrate with "My Stats" section in `LeaderboardView.vue`.
- [x] Task: Implement H2H Analytics Table
    - [x] Create `H2HAnalyticsTable.vue`.
    - [x] Handle data fetching for specific opponents.
- [x] Task: Implement Positional H2H Filters
    - [x] Add "My Role" and "Opponent Role" selectors for H2H table.
- [x] Task: Conductor - User Manual Verification 'Frontend - Player Profile & Position Breakdown' (Protocol in workflow.md)

## Phase 4: Integration & Mobile Polish [CANCELLED/DEFERRED]
*Note: Phase 4 removed from this track per user request. Will be planned separately.*
