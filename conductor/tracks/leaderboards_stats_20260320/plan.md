# Plan: Public Leaderboards & Comprehensive Player Statistics

## Phase 1: Backend Statistics Calculation & APIs [checkpoint: ba38412]
- [x] Task: Implement Statistics DTOs and Repository Queries (45f7a2d)
    - [x] Create `PlayerStatsResponse` and `H2HStatsResponse` DTOs
    - [x] Write unit tests for statistics calculation logic (calculating wins, losses, win rates from match lists)
    - [x] Implement repository methods (using JPA or Native SQL) to aggregate match statistics for a given user or globally, supporting pagination
- [x] Task: Implement Global Leaderboard API (7a3b4c1)
    - [x] Write integration tests for `GET /api/v1/statistics/leaderboard` with optional time period, match threshold, and pagination (page, size) parameters
    - [x] Implement `StatisticsService` and `StatisticsController` for global paginated rankings
- [x] Task: Implement Personal Statistics API (9d2e1f8)
    - [x] Write integration tests for `GET /api/v1/statistics/me` (personal position-based stats)
    - [x] Write integration tests for `GET /api/v1/statistics/h2h` (head-to-head records against opponents)
    - [x] Implement service and controller logic for user-specific stats
- [x] Task: Conductor - User Manual Verification 'Backend Statistics Calculation & APIs' (Protocol in workflow.md) (ba38412)

## Phase 2: Frontend - Global Leaderboards UI [checkpoint: ]
- [x] Task: Create Leaderboard View and Service (97319b7)
    - [x] Implement API client for `GET /api/v1/statistics/leaderboard`
    - [x] Write component tests for displaying paginated rankings (Overall, Attack, Defense) with page size selector (10, 20, 50, 100)
    - [x] Create `LeaderboardView.vue` with tabbed layout for different roles and pagination controls
- [x] Task: Implement Time Period and Threshold Filters (68bd4a5)
    - [x] Add Weekly/Monthly/Yearly/All-time selector in the UI
    - [x] Implement the threshold filtering logic to re-fetch or filter leaderboard results
    - [x] Verify that filters and pagination work correctly together
- [ ] Task: Conductor - User Manual Verification 'Frontend - Global Leaderboards UI' (Protocol in workflow.md)

## Phase 3: Frontend - Player Profile & Position Breakdown [checkpoint: ]
- [ ] Task: Create Player Profile Dashboard
    - [ ] Implement API client for `GET /api/v1/statistics/me`
    - [ ] Write component tests for the position-based breakdown (Matches, Wins, Win%)
    - [ ] Design and implement the profile summary section on the "Stats" page
- [ ] Task: Implement Head-to-Head (H2H) Analytics Table
    - [ ] Implement API client for `GET /api/v1/statistics/h2h`
    - [ ] Write component tests for the H2H table with sorting capabilities
    - [ ] Add the H2H table to the player profile section
- [ ] Task: Implement Positional H2H Filters
    - [ ] Implement UI for positional H2H filtering (4 combinations: Me as Att/Def vs Opponent as Att/Def)
    - [ ] Write tests to verify positional filtering logic in the H2H table
- [ ] Task: Conductor - User Manual Verification 'Frontend - Player Profile & Position Breakdown' (Protocol in workflow.md)

## Phase 4: Integration & Mobile Polish [checkpoint: ]
- [ ] Task: Navigation & Route Setup
    - [ ] Add "Leaderboards" link to the navigation bar or dashboard
    - [ ] Ensure proper route guarding and error handling for the new statistics page
- [ ] Task: Mobile Responsiveness Verification
    - [ ] Test the new "Leaderboards & Stats" page on mobile views (verified with Safari DevTools)
    - [ ] Optimize tables and pagination controls for small screens (e.g., using horizontal scrolling or simplified views)
- [ ] Task: Final End-to-End Verification
    - [ ] Perform manual end-to-end verification: Confirm a match -> Check updated leaderboards (including next pages) -> Verify profile stats
- [ ] Task: Conductor - User Manual Verification 'Integration & Mobile Polish' (Protocol in workflow.md)
