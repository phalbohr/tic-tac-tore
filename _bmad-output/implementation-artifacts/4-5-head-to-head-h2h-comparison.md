---
baseline_commit: 3e74d8f
---

# Story 4.5: Head-to-Head (H2H) Comparison

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to compare my stats against a specific opponent,
so that I know our historical matchup across matches, games, and positions.

## Acceptance Criteria

1. **Given** the player navigates to head-to-head statistics
   **When** they select an opponent (or arrive via deep-link with `opponentId`)
   **Then** the system displays the opponent's profile header (avatar, nickname) and three cross-tabulated tables for matches, games, and goals (FR24)
   **And** the **Matches table** cross-tabulates performance played together ("With" as teammates in 2v2) vs played against ("Vs" as opponents in 1v1 and 2v2), detailing total matches, wins, losses, draws, and win rate percentage
   **And** the **Games table** cross-tabulates games won, games lost, total games, and game win rate percentage for "With" and "Vs"
   **And** the **Goals table** displays detailed positional breakdowns (Attacker vs Defender, Attacker vs Attacker, Defender vs Attacker, Defender vs Defender) showing goals scored and conceded
2. **Given** an opponent is selected in the H2H view
   **When** the player filters by time period (`period`: `WEEKLY`, `MONTHLY`, `YEARLY`, `ALL_TIME`), rule system (`ruleConfigId`), or match type (`matchType`: `1v1`, `2v2`)
   **Then** the system filters all three cross-tabulated tables accordingly (FR20)
3. **Given** the player selects an opponent with whom they have 0 shared matches (or no matches matching active filters)
   **When** the H2H view renders
   **Then** the system displays an empty state using `EmptyStateCTA` with message: *"You haven't played [opponent] yet — start a match?"* and a button navigating to match creation with the opponent pre-selected (UX-DR3, FR24)
4. **Given** demo mode is active (`demoModeEnabled`) or the current user has < 1 confirmed match
   **When** viewing H2H statistics
   **Then** realistic demo data is generated via `demoDataGenerator.ts` allowing complete exploration of all three cross-tabulation matrices

## Tasks / Subtasks

- [ ] Task 1: Backend Domain DTOs & Projections (AC1, AC2)
  - [ ] Create `H2HStatsResponse.java`, `H2HMatchStatsDto.java`, `H2HGameStatsDto.java`, `H2HGoalStatsDto.java`, `PositionalGoalMatrixDto.java`, and `PlayerSummaryDto.java` in `com.tictactore.dto`.
  - [ ] Create repository projections/interfaces for cross-tabulation query aggregation results.
- [ ] Task 2: Backend Database Aggregations & Service Layer (AC1, AC2, AC3)
  - [ ] Implement JPA native/JPQL aggregation queries in `MatchRepository` (and/or `GameRepository` / `MatchEventRepository`) querying strictly `CONFIRMED` and `PUBLISHED` matches (`AD-02`).
  - [ ] Calculate "With" (same team in 2v2) and "Vs" (opposite teams in 1v1 and 2v2) metrics for matches and games.
  - [ ] Calculate positional goal cross-tabulation (Attacker vs Defender, Attacker vs Attacker, Defender vs Attacker, Defender vs Defender).
  - [ ] Add `H2HStatsResponse getHeadToHeadStats(UUID playerId, UUID opponentId, TimePeriod period, UUID ruleConfigId, String matchType)` to `StatisticsService` and `StatisticsServiceImpl`.
  - [ ] Apply safe player profile resolution via `resolveDisplayName` without email fallback (`AD-04`) and support deleted/anonymous accounts.
- [ ] Task 3: Backend REST Controller & Validation (AC1, AC2)
  - [ ] Add `GET /api/v1/statistics/head-to-head` endpoint in `StatisticsController`.
  - [ ] Support query parameters: `opponentId` (required UUID), `period` (TimePeriod enum, default `ALL_TIME`), `ruleConfigId` (optional UUID), `matchType` (optional string `1v1|2v2`).
  - [ ] Inject `@AuthenticationPrincipal User principal` to resolve current authenticated user as `playerId`.
- [ ] Task 4: Frontend Service & Pinia Store Integration (AC1, AC2, AC4)
  - [ ] Update `frontend/src/services/statisticsService.ts` with `getHeadToHeadStats(...)` API call.
  - [ ] Update `frontend/src/features/stats/stores/useStatsStore.ts` with `h2hStats`, `selectedOpponentId`, `fetchH2HStats(...)`, and reactive filters.
  - [ ] Update `frontend/src/features/stats/utils/demoDataGenerator.ts` to generate realistic H2H demo matrices (matches, games, positional goals).
- [ ] Task 5: Frontend UI Component Implementation (AC1, AC2, AC3, AC4)
  - [ ] Create `frontend/src/features/stats/components/H2HCrossTabMatrix.vue` displaying the 3 cross-tabulated tables following the "No-Line" rule (UX-DR3) with `ch-` SCSS classes.
  - [ ] Add Opponent Selector supporting player search/autocomplete and integration with `AvatarInteractive`.
  - [ ] Integrate `EmptyStateCTA.vue` for 0 shared matches with action to initiate a new match.
  - [ ] Add H2H tab in `StatsDashboard.vue` and support deep-linking via route query (`/statistics?tab=h2h&opponentId=...`).
  - [ ] Add i18n translation keys in `en.json` and `de.json` for all matrix labels, tooltips, and empty states.
- [ ] Task 6: Testing & Quality Verification (ATDD & CI)
  - [ ] Implement backend unit and integration tests: `StatisticsControllerTest.java`, `StatisticsServiceTest.java`, `StatisticsServiceIntegrationTest.java`, and `StatisticsControllerATDDTest.java`.
  - [ ] Implement frontend unit tests in `frontend/tests/unit/h2hCrossTabMatrix.spec.ts`.
  - [ ] Implement Playwright E2E test in `frontend/e2e/head-to-head-statistics.spec.ts`.
  - [ ] Run `./scripts/ci-local.sh` and verify all tests pass.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contract Compliance:**
  - **Path:** MUST use `GET /api/v1/statistics/head-to-head` in `StatisticsController`.
  - **Parameters:** `opponentId` (required UUID), `period` (`TimePeriod`: `WEEKLY`, `MONTHLY`, `YEARLY`, `ALL_TIME`, default `ALL_TIME`), `ruleConfigId` (optional UUID), `matchType` (optional `1v1` | `2v2`).
  - **Status Code:** `200 OK` on success, `400 Bad Request` if `opponentId` is invalid or equals current user ID, `401 Unauthorized` if unauthenticated.
- **Architecture Compliance (AD-02, AD-04, AD-05):**
  - **AD-02 (Isolated Verification Pipeline):** Query ONLY matches where `status IN ('CONFIRMED', 'PUBLISHED')`. Never aggregate pending or rejected matches.
  - **AD-04 (Privacy & PII Protection):** Do not expose email addresses in display name resolution; fallback to "retired player" for deleted accounts.
  - **AD-05 (Stateless Authentication):** Verify session via JWT security context.
- **500-Line Rule (IP-04):**
  - No file or test class can exceed 500 lines. Split UI components (e.g., separating tables into subcomponents if necessary) and repository helper methods.
- **Cross-Database Compatibility:**
  - Aggregations in native queries must execute cleanly on both H2 (in-memory test DB) and PostgreSQL (production), especially regarding UUID casting (`CAST(? AS uuid)` or string representations).
- **Frontend Styling & UX Guidelines:**
  - **No-Line Rule (UX-DR3):** Do not use solid border lines (`border: 1px solid ...`) to separate table cells or cards. Use subtle background tone shifts (`background-color: var(--ch-surface-...)`), border radius, and spacing.
  - **Design System:** Use `ch-` prefix for custom SCSS styling matching the Clubhouse dark aesthetic.
  - **Translations:** No hardcoded English strings. All matrix headers, column titles, and empty states must use `$t('...')` with keys added to `en.json` and `de.json`.

### Learnings from Previous Stories (4.1, 4.3, 4.4)

- **Demo Data Toggle:** In `useStatsStore.ts`, ensure demo mode toggle properly swaps between live backend data and `demoDataGenerator.ts` without dropping selected opponent state.
- **Positional Data Accuracy:** In 1v1 matches, both players occupy single positions; in 2v2, check whether players played attacker or defender to compute the 4 goal matrix cells (`attackerVsDefender`, `attackerVsAttacker`, `defenderVsAttacker`, `defenderVsDefender`).
- **Empty State Consistency:** When `totalMatches == 0`, render `EmptyStateCTA` with button navigating to match creation (`router.push({ path: '/matches/new', query: { opponentId } })`).

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-4-5-head-to-head-h2h-comparison.md`
- **Backend API tests:** `src/test/java/com/tictactore/controller/StatisticsControllerATDDTest.java`
- **Backend Service tests:** `src/test/java/com/tictactore/service/StatisticsServiceTest.java`
- **Frontend Unit tests:** `frontend/tests/unit/h2hCrossTabMatrix.spec.ts`
- **Frontend E2E tests:** `frontend/e2e/head-to-head-statistics.spec.ts`

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR20, FR24, FR27, FR28.
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md] - Section 4 & Component hierarchy (`H2HCrossTabMatrix`, `PlayerDetailDrawer`, `EmptyStateCTA`).
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-02, AD-04, AD-05, IP-04.
