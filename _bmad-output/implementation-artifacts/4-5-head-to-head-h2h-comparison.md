---
baseline_commit: 3e74d8f
---

# Story 4.5: Head-to-Head (H2H) Comparison

Status: review

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

- [x] Task 1: Backend Domain DTOs & Projections (AC1, AC2)
  - [x] Create `H2HStatsResponse.java`, `H2HMatchStatsDto.java`, `H2HGameStatsDto.java`, `H2HGoalStatsDto.java`, `PositionalGoalMatrixDto.java`, and `PlayerSummaryDto.java` in `com.tictactore.dto`.
  - [x] Create repository projections/interfaces for cross-tabulation query aggregation results.
- [x] Task 2: Backend Database Aggregations & Service Layer (AC1, AC2, AC3)
  - [x] Implement JPA native/JPQL aggregation queries in `MatchRepository` (and/or `GameRepository` / `MatchEventRepository`) querying strictly `CONFIRMED` and `PUBLISHED` matches (`AD-02`).
  - [x] Calculate "With" (same team in 2v2) and "Vs" (opposite teams in 1v1 and 2v2) metrics for matches and games.
  - [x] Calculate positional goal cross-tabulation (Attacker vs Defender, Attacker vs Attacker, Defender vs Attacker, Defender vs Defender).
  - [x] Add `H2HStatsResponse getHeadToHeadStats(UUID playerId, UUID opponentId, TimePeriod period, UUID ruleConfigId, String matchType)` to `StatisticsService` and `StatisticsServiceImpl`.
  - [x] Apply safe player profile resolution via `resolveDisplayName` without email fallback (`AD-04`) and support deleted/anonymous accounts.
- [x] Task 3: Backend REST Controller & Validation (AC1, AC2)
  - [x] Add `GET /api/v1/statistics/head-to-head` endpoint in `StatisticsController`.
  - [x] Support query parameters: `opponentId` (required UUID), `period` (TimePeriod enum, default `ALL_TIME`), `ruleConfigId` (optional UUID), `matchType` (optional string `1v1|2v2`).
  - [x] Inject `@AuthenticationPrincipal User principal` to resolve current authenticated user as `playerId`.
- [x] Task 4: Frontend Service & Pinia Store Integration (AC1, AC2, AC4)
  - [x] Update `frontend/src/services/statisticsService.ts` with `getHeadToHeadStats(...)` API call.
  - [x] Update `frontend/src/features/stats/stores/useStatsStore.ts` with `h2hStats`, `selectedOpponentId`, `fetchH2HStats(...)`, and reactive filters.
  - [x] Update `frontend/src/features/stats/utils/demoDataGenerator.ts` to generate realistic H2H demo matrices (matches, games, positional goals).
- [x] Task 5: Frontend UI Component Implementation (AC1, AC2, AC3, AC4)
  - [x] Create `frontend/src/features/stats/components/H2HCrossTabMatrix.vue` displaying the 3 cross-tabulated tables following the "No-Line" rule (UX-DR3) with `ch-` SCSS classes.
  - [x] Add Opponent Selector supporting player search/autocomplete and integration with `AvatarInteractive`.
  - [x] Integrate `EmptyStateCTA.vue` for 0 shared matches with action to initiate a new match.
  - [x] Add H2H tab in `StatsDashboard.vue` and support deep-linking via route query (`/statistics?tab=h2h&opponentId=...`).
  - [x] Add i18n translation keys in `en.json` and `de.json` for all matrix labels, tooltips, and empty states.
- [x] Task 6: Testing & Quality Verification (ATDD & CI)
  - [x] Implement backend unit and integration tests: `StatisticsControllerTest.java`, `StatisticsServiceTest.java`, `StatisticsServiceIntegrationTest.java`, and `StatisticsControllerATDDTest.java`.
  - [x] Implement frontend unit tests in `frontend/tests/unit/h2hCrossTabMatrix.spec.ts`.
  - [x] Implement Playwright E2E test in `frontend/e2e/head-to-head-statistics.spec.ts`.
  - [x] Run `./scripts/ci-local.sh` and verify all tests pass.

### Review Findings

- [x] [Review][Patch] Support ruleConfigId filter parameter in repository and service [src/main/java/com/tictactore/service/impl/StatisticsServiceImpl.java:90]
- [x] [Review][Patch] Add Opponent Selector and remove invalid fallback opp-user-456 [frontend/src/features/stats/components/H2HCrossTabMatrix.vue:18]
- [x] [Review][Patch] Add Rule Configuration filter dropdown in H2H filter bar [frontend/src/features/stats/components/H2HCrossTabMatrix.vue:97]
- [x] [Review][Patch] Add defensive null check for match.getGames() during statistics aggregation [src/main/java/com/tictactore/service/impl/StatisticsServiceImpl.java:134]
- [x] [Review][Patch] Add watcher for props.opponentId to refresh data when prop changes [frontend/src/features/stats/components/H2HCrossTabMatrix.vue:56]
- [x] [Review][Patch] Replace hardcoded English strings with i18n translation keys in EmptyStateCTA and StatsDashboard [frontend/src/features/stats/components/EmptyStateCTA.vue:60]
- [x] [Review][Patch] Fix German translation for startMatch in de.json [frontend/src/locales/de.json:1]
- [x] [Review][WARN] Use shared API client for opponent search [frontend/src/features/stats/components/H2HCrossTabMatrix.vue:67]
- [x] [Review][WARN] Eliminate search debounce race condition via AbortController [frontend/src/features/stats/components/H2HCrossTabMatrix.vue:64]
- [x] [Review][WARN] Clear h2hStats upon starting opponent fetch to prevent stale UI flash [frontend/src/features/stats/stores/useStatsStore.ts:124]
- [x] [Review][WARN] Fix vue-i18n translation signature in EmptyStateCTA [frontend/src/features/stats/components/EmptyStateCTA.vue:42]
- [x] [Review][NIT] Extract formatGoalDiff & getGoalDiffClass helper functions [frontend/src/features/stats/components/H2HCrossTabMatrix.vue:348]
- [x] [Review][NIT] Update route query param when selecting opponent in search modal [frontend/src/features/stats/components/H2HCrossTabMatrix.vue:79]

## Dev Record

### Implementation Summary
- **Backend**:
  - DTOs: `PlayerSummaryDto`, `H2HMatchStatsDto`, `H2HMatchTableDto`, `H2HGameStatsDto`, `H2HGameTableDto`, `PositionalGoalMatrixDto`, `H2HGoalStatsDto`, `H2HStatsResponse`.
  - Repository: Added `findHeadToHeadMatches` in `MatchRepository` filtering by confirmed/published status, participants, time periods, `ruleConfigId`, and match formats.
  - Service: Implemented `getHeadToHeadStats` in `StatisticsServiceImpl` computing cross-tabulation for "With" and "Vs" (matches and games) as well as 4-way positional goal breakdowns (Attacker vs Defender, Attacker vs Attacker, Defender vs Defender, Defender vs Attacker) with privacy-safe display name resolution and defensive null checks on match games.
  - Controller: Added `GET /api/v1/statistics/head-to-head` in `StatisticsController` supporting parameter validation and authenticated principal extraction.
- **Frontend**:
  - Service: Added `getHeadToHeadStats`, `searchPlayers`, and TypeScript interfaces in `statisticsService.ts`.
  - Store: Added H2H state and actions in `useStatsStore.ts` with demo mode support and immediate stale state clearing on fetch.
  - Component: Created `H2HCrossTabMatrix.vue` displaying opponent profile header, opponent selector modal with live search, Rule Configuration filter dropdown, 3 cross-tabulated tables (Matches, Games, Goals) following the No-Line rule (`UX-DR3`), and empty state integration with `EmptyStateCTA.vue`.
  - Translations: Added keys to `en.json` and `de.json` with German translation fixes.
  - Routing: Added `/statistics` route and tab switching in `StatsDashboard.vue`.
- **Review Resolutions**:
  - ✅ Resolved review finding: Added `ruleConfigId` filtering to `Match` entity, `MatchRepository`, and `StatisticsServiceImpl`.
  - ✅ Resolved review finding: Added defensive null checks for `match.getGames()`.
  - ✅ Resolved review finding: Removed `opp-user-456` fallback, added Opponent Search modal and watcher for `props.opponentId`.
  - ✅ Resolved review finding: Added Rule Configuration dropdown in H2H filter bar.
  - ✅ Resolved review finding: Replaced hardcoded strings in `EmptyStateCTA` and `StatsDashboard` with i18n keys.
  - ✅ Resolved review finding: Fixed German `startMatch` key in `de.json`.
  - ✅ Resolved review finding [WARN]: Routed player search through `searchPlayers` in `statisticsService.ts` for unified error handling.
  - ✅ Resolved review finding [WARN]: Added `AbortController` cancellation to player search debouncing in `H2HCrossTabMatrix.vue`.
  - ✅ Resolved review finding [WARN]: Cleared `h2hStats.value = null` on starting `fetchH2HStats` in `useStatsStore.ts`.
  - ✅ Resolved review finding [WARN]: Corrected `vue-i18n` interpolation in `EmptyStateCTA.vue` using standard named argument signature.
  - ✅ Resolved review finding [NIT]: Extracted `formatGoalDiff` and `getGoalDiffClass` helpers to reduce template duplication.
  - ✅ Resolved review finding [NIT]: Added `router.push` updating `?opponentId=` query param upon selecting opponent from search modal.
- **Testing**:
  - Backend: `StatisticsControllerATDDTest` (enabled and passing), `StatisticsServiceTest` (passing), `StatisticsControllerTest` (passing), `StatisticsServiceIntegrationTest` (passing).
  - Frontend: `h2hCrossTabMatrix.spec.ts` (passing), `H2HCrossTabMatrix.spec.ts` (passing), `EmptyStateCTA.spec.ts` (passing), `statisticsService.spec.ts` (passing), `StatsDashboard.spec.ts` (passing).
  - E2E: `head-to-head-statistics.spec.ts` (enabled and passing).
  - Verification: Full `./scripts/ci-local.sh` suite executed and verified passing.

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
