# Story 4.4: Team (Pair) Statistics

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to see how I perform with different partners,
so that I can find the best synergy.

## Acceptance Criteria

1. **Given** the player navigates to team statistics
   **When** the view loads
   **Then** it displays pair-level performance for teammate combinations (FR23)
   **And** it differentiates between specific positional synergies (e.g., A as attacker/B as defender vs. B as attacker/A as defender)
2. **Given** the team statistics view is loaded
   **When** the player filters by a specific player, rule system, or time period
   **Then** the system filters the statistics accordingly (FR20)
3. **Given** there are many team combinations
   **When** viewing the statistics
   **Then** the results are paginated (FR27) and exclude pairs below the minimum matches threshold (FR28)

## Tasks / Subtasks

- [ ] Task 1: Backend Database Queries and Services
  - [ ] Create a JPA native query to aggregate performance by team pairs. Group by specific positions (`attacker_id`, `defender_id`) to provide positional synergy insights. If fallback to unordered pairs is needed, use `LEAST(player1_id, player2_id)` and `GREATEST(player1_id, player2_id)`.
  - [ ] Filter strictly for `CONFIRMED`/`PUBLISHED` matches (AD-02).
  - [ ] Create service layer methods returning paginated `Page<TeamPairStatsResponse>`.
- [ ] Task 2: Backend REST Endpoint
  - [ ] Create `GET /api/v1/statistics/team-pairs` in `StatisticsController` to maintain REST consistency.
  - [ ] Implement query parameters: `playerId` (optional filter), `period` (TimePeriod enum), `ruleConfigId` (optional), `page` (int), `size` (int), and `minMatches` (int).
- [ ] Task 3: Frontend Data Store Integration
  - [ ] Update `frontend/src/features/stats/stores/useStatsStore.ts` to fetch paginated team pair statistics with new filters.
  - [ ] Update `frontend/src/features/stats/utils/demoDataGenerator.ts` to generate realistic demo data for team pairs.
- [ ] Task 4: UI Implementation
  - [ ] Create `TeamStatsView.vue` or a new tab in `StatsDashboard.vue`.
  - [ ] Implement controls for pagination, specific player filter, rule system filter, and time period filter.
  - [ ] Present the data visually following the "No-Line" rule (UX-DR3).
  - [ ] Use `ch-` prefixed utility classes for custom SCSS styling.

## Dev Notes

- **API Contract Compliance:**
  - **Path:** MUST use `/api/v1/statistics/team-pairs` (not `/stats/`).
  - **Parameters:** Must support `page`, `size`, `period`, and `minMatches` to align with FR20/FR27/FR28 and existing `StatisticsController` patterns.
- **Architecture & Implementation:**
  - **Positional Synergy:** The aggregation query should group by `(attacker_id, defender_id)` to show true synergy. 
  - **AD-02 (Isolated Verification Pipeline):** Query ONLY verified matches.
  - **IP-04 (500-Line Rule):** Do not exceed 500 lines per file.
- **Previous Story Context (4-1 Empty State):**
  - If `tictactore.demoModeEnabled` is true or the user has < 1 confirmed match, reuse the existing `EmptyState` component or demo data patterns established in Story 4.1 rather than building new fallbacks.

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR23: Team statistics showing pair-level performance.
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-02, AD-05, and IP-04 architectural invariants.

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)

### Debug Log References

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created

### File List

