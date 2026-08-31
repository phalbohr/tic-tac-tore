---
baseline_commit: fd1456bd7ae74c098239e299fc23acb04b4596b5
---

# Story 7.5: Auto-generated Statistical Insights

Status: review

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a player,
I want to receive automated, personalized statistical insights and progress observations based on my match history,
so that I can understand my improvement over time, discover tactical strengths, and celebrate gameplay milestones without manual data analysis.

## Acceptance Criteria

1. **Given** an authenticated player with at least 3 confirmed matches
   **When** the system generates statistical insights for the player
   **Then** the `InsightEngine` dynamically evaluates match history and generates up to 5 prioritized, non-judgmental insight observations (`FR53`)
   **And** each insight includes a unique identifier, insight type, category (`STREAK`, `TREND`, `POSITION`, `PARTNERSHIP`, `MILESTONE`), localized title and description keys, dynamic parameter map, Material Symbols icon name, importance level (`HIGH`, `MEDIUM`, `LOW`), and optional navigation drill-down URL (`drillDownUrl`).
2. **Given** a player meets specific performance conditions in their match history
   **When** the insight engine runs its registered generator strategies
   **Then** the following deterministic insights are produced when their criteria are met:
   - **`WIN_STREAK`**: Player has won $\ge 3$ consecutive recent matches (params: `{streak: count}`).
   - **`FORM_TREND`**: Player's win rate in their last 5–10 matches exceeds their overall career win rate by $\ge 15\%$ (params: `{recentWinRate: %, careerWinRate: %, diff: %}`).
   - **`POSITIONAL_MASTERY`**: Player has a win rate delta $\ge 20\%$ between Attacker and Defender positions with $\ge 5$ matches played in each position (params: `{favoredPosition: "Attacker"|"Defender", higherWinRate: %, lowerWinRate: %}`).
   - **`BEST_PARTNERSHIP`**: Player has achieved a win rate $\ge 70\%$ with a specific 2v2 partner over $\ge 3$ joint matches (params: `{partnerId: UUID, partnerName: string, winRate: %, matches: count}`, `drillDownUrl: "/statistics?tab=teams"`).
   - **`MILESTONE_PROXIMITY`**: Player is within 2 matches or goals of unlocking a progressive achievement badge (e.g. 8/10 matches for `MATCHES_10` or 48/50 goals for `STRIKER_50`) (params: `{badgeCode: string, remaining: count, current: count, target: count}`, `drillDownUrl: "/cabinet"`).
3. **Given** a player has fewer than 3 confirmed matches (insufficient data)
   **When** requesting statistical insights
   **Then** the engine gracefully returns an empty insight collection or a single informational starter insight (`type: INSUFFICIENT_DATA`, `category: GENERAL`, `importance: LOW`) encouraging the player to complete more matches
   **And** no exceptions, division-by-zero errors, or NaN values are produced.
4. **Given** an API request to `GET /api/v1/players/{id}/insights` or `GET /api/v1/statistics/insights`
   **When** processed by the backend
   **Then** the endpoint returns `200 OK` with `PlayerInsightsResponse` containing `insights: List<PlayerInsightDto>` and `totalCount: int`
   **And** caller authentication is verified via JWT, ensuring no PII or private emails are leaked (`AD-04`, `AD-05`)
   **And** all calculations execute dynamically in-memory in a single pass without persisting snapshots to the database (Zero DB Write Amplification).
5. **Given** an authenticated user browsing the application
   **When** viewing the Personal Cabinet (`/cabinet`), Statistics Hub (`/statistics`), or Home Hub (`/`)
   **Then** dedicated `InsightsSection.vue` and `InsightCard.vue` components render the player's top insights using Clubhouse Editorial design tokens (`ch-` utility classes, tactile card background, no neon gaming cliches per `UX-DR3`)
   **And** tapping an insight with a `drillDownUrl` navigates the user directly to the corresponding context (e.g. H2H matrix, team stats, or achievement award wall).
6. **Given** a match is confirmed by all participants
   **When** the match confirmation flow completes and the undo window expires
   **Then** `MicroCelebrationBanner.vue` appears on the Home Hub displaying the most recent high-priority insight (e.g. win streak or milestone progress) with a subtle entry fade
   **And** automatically dismisses after 4 seconds or upon user dismissal, with accessibility announcements via `role="status"` and `aria-live="polite"`.
7. **Given** Demo Mode is enabled or the user has $< 5$ confirmed matches with Demo Data toggled on
   **When** the insights store loads
   **Then** `demoDataGenerator.ts` provides realistic, localized demo insights so that new players immediately experience the discovery loop payoff (`Flow 3`).
8. **Given** interface localization in English and German (`FR59`)
   **When** rendering insight titles, descriptions, and parameter placeholders
   **Then** all strings are localized through `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.

## Tasks / Subtasks

- [x] Task 1: Backend Domain & Insight Generation Engine (AC1, AC2, AC3, AC4)
  - [x] Create domain records and enums:
    - `com.tictactore.dto.InsightType` (`WIN_STREAK`, `FORM_TREND`, `POSITIONAL_MASTERY`, `BEST_PARTNERSHIP`, `MILESTONE_PROXIMITY`, `INSUFFICIENT_DATA`).
    - `com.tictactore.dto.InsightCategory` (`STREAK`, `TREND`, `POSITION`, `PARTNERSHIP`, `MILESTONE`, `GENERAL`).
    - `com.tictactore.dto.InsightImportance` (`HIGH`, `MEDIUM`, `LOW`).
    - `com.tictactore.dto.PlayerInsightDto` (`UUID id`, `InsightType type`, `InsightCategory category`, `InsightImportance importance`, `String titleKey`, `String descriptionKey`, `Map<String, Object> params`, `String icon`, `String drillDownUrl`).
    - `com.tictactore.dto.PlayerInsightsResponse` (`UUID playerId`, `int totalCount`, `List<PlayerInsightDto> insights`).
  - [x] Create `com.tictactore.service.insight.InsightGenerator` interface:
    - `Optional<PlayerInsightDto> generate(UUID playerId, List<Match> matches, PlayerStatsContext stats, List<AchievementDto> achievements)`.
    - `int getOrder()`.
  - [x] Implement concrete generator components in `com.tictactore.service.insight.generator`:
    - `WinStreakInsightGenerator` (`WIN_STREAK` — evaluates recent consecutive match wins).
    - `FormTrendInsightGenerator` (`FORM_TREND` — compares win rate of last 5–10 matches against career average).
    - `PositionalMasteryInsightGenerator` (`POSITIONAL_MASTERY` — compares Attacker vs Defender win rates with min 5 matches each).
    - `BestPartnershipInsightGenerator` (`BEST_PARTNERSHIP` — analyzes 2v2 partner win rates with min 3 matches).
    - `MilestoneProximityInsightGenerator` (`MILESTONE_PROXIMITY` — checks locked progressive badges with remaining progress $\le 2$).
  - [x] Create `com.tictactore.service.InsightService` and `com.tictactore.service.impl.InsightServiceImpl`:
    - Collects all `InsightGenerator` beans, loads player matches via `MatchRepository.findConfirmedMatchesByPlayerId(userId)`, executes single-pass aggregation, sorts by importance, and limits output to top 5.
- [x] Task 2: REST Controller & Security Integration (AC4)
  - [x] Update `com.tictactore.controller.StatisticsController` (or add `com.tictactore.controller.InsightController`):
    - `GET /api/v1/players/{id}/insights` -> returns `200 OK` with `PlayerInsightsResponse`.
    - `GET /api/v1/statistics/insights` -> returns `200 OK` for `@AuthenticationPrincipal User principal`.
    - Enforce authentication and sanitize output (no PII/email).
- [x] Task 3: Backend Unit, ATDD & Integration Tests (AC1-AC4)
  - [x] Create `src/test/java/com/tictactore/service/insight/InsightGeneratorTest.java`: unit tests for each individual generator strategy (streak, trend, position, partnership, milestone, empty state).
  - [x] Create `src/test/java/com/tictactore/service/insight/InsightServiceTest.java`: unit test for `InsightServiceImpl` aggregation, sorting, and capping.
  - [x] Create `src/test/java/com/tictactore/controller/InsightControllerATDDTest.java`: ATDD acceptance tests verifying REST API contracts, status codes, and security.
- [x] Task 4: Frontend Service, Store & Demo Generator (AC1, AC4, AC7)
  - [x] Create `frontend/src/services/insightService.ts`:
    - TypeScript interfaces: `PlayerInsight`, `PlayerInsightsResponse`, `InsightType`, `InsightCategory`, `InsightImportance`.
    - API fetch methods: `getPlayerInsights(playerId: string)`, `getMyInsights()`.
  - [x] Create `frontend/src/features/stats/stores/useInsightStore.ts`:
    - State: `insights: PlayerInsight[]`, `isLoading: boolean`, `error: string | null`.
    - Getters: `topInsights`, `latestCelebrationInsight`.
    - Actions: `fetchInsights(playerId?: string)`.
  - [x] Update `frontend/src/features/stats/utils/demoDataGenerator.ts`:
    - Add `generateDemoInsights(): PlayerInsight[]` producing realistic demo insights.
- [x] Task 5: Frontend UI Components & Views Integration (AC5, AC6)
  - [x] Create `frontend/src/features/stats/components/InsightCard.vue`:
    - Renders icon, localized title, description with dynamic parameter interpolation, category badge, and interactive drill-down button with Clubhouse Editorial design tokens (`UX-DR3`). Keep under 500 lines (`IP-04`).
  - [x] Create `frontend/src/features/stats/components/MicroCelebrationBanner.vue`:
    - Post-confirmation reward banner on Home Hub (`role="status"`, `aria-live="polite"`, auto-dismiss after 4s, drill-link CTA).
  - [x] Create `frontend/src/features/stats/components/InsightsSection.vue`:
    - Grid/list container for displaying insights cards in `StatsDashboard.vue` and `Cabinet.vue`.
  - [x] Mount `InsightsSection.vue` in `StatsDashboard.vue` and `Cabinet.vue`.
  - [x] Mount `MicroCelebrationBanner.vue` in `HomeView.vue`.
- [x] Task 6: Localization in English & German (AC8)
  - [x] Add translation keys to `frontend/src/locales/en.json`:
    - `insights.title: "Statistical Insights"`
    - `insights.empty: "Play at least 3 matches to unlock personalized gameplay insights."`
    - `insights.winStreak.title: "On a Roll!"`
    - `insights.winStreak.description: "You're currently on a {streak}-match winning streak."`
    - `insights.formTrend.title: "Rising Form"`
    - `insights.formTrend.description: "Your recent win rate ({recentWinRate}%) is {diff}% higher than your career average."`
    - `insights.positionalMastery.title: "Positional Specialist"`
    - `insights.positionalMastery.description: "You excel as {favoredPosition} with a {higherWinRate}% win rate compared to {lowerWinRate}% on the other position."`
    - `insights.bestPartnership.title: "Dynamic Duo"`
    - `insights.bestPartnership.description: "You and {partnerName} hold a stellar {winRate}% win rate across {matches} games."`
    - `insights.milestoneProximity.title: "Milestone in Reach"`
    - `insights.milestoneProximity.description: "Only {remaining} more to unlock the next achievement badge!"`
    - `insights.drillDown: "View Details"`
  - [x] Add translation keys to `frontend/src/locales/de.json` with accurate German phrasing.
- [x] Task 7: Frontend Unit & Playwright E2E Tests (AC1-AC8)
  - [x] Create `frontend/src/features/stats/components/__tests__/InsightCard.spec.ts`.
  - [x] Create `frontend/src/features/stats/components/__tests__/MicroCelebrationBanner.spec.ts`.
  - [x] Create `frontend/src/features/stats/stores/__tests__/useInsightStore.spec.ts`.
  - [x] Create Playwright E2E test `frontend/e2e/statistical-insights.spec.ts` verifying insight rendering, demo mode display, and micro-celebration banner interaction.
- [x] Task 8: Verification & Quality Gate
  - [x] Execute `./scripts/ci-local.sh` and ensure 100% test pass across backend compilation, unit/ATDD/integration tests, frontend type-check, lint, unit tests, and Playwright E2E suite.

## Dev Notes

### Architecture & Implementation Guardrails

- **Zero DB Write Amplification:**
  - Insights are generated dynamically on query using `MatchRepository.findConfirmedMatchesByPlayerId(userId)`.
  - NEVER create database tables or columns for persisting individual insight records or periodic snapshots.
- **REST API Contracts & Security (AD-05, AD-04):**
  - Base path: `GET /api/v1/players/{id}/insights` and `GET /api/v1/statistics/insights`.
  - Authentication: Requires valid JWT. Unauthorized requests return `401 Unauthorized`.
  - PII Protection (`AD-04`): Never expose user email addresses or sensitive credentials in response DTOs.
- **The 500-Line Rule (IP-04):**
  - Keep all generator classes, services, stores, and Vue components modular and well below 500 lines.
- **Zero Comments Policy:**
  - Code must be self-documenting. Add documentation ONLY where mandated by project rules (e.g. OpenAPI annotations). Avoid trivial comments.
- **Clubhouse Editorial UI Theme (UX-DR3):**
  - Use `ch-` palette tokens (`bg-ch-surface-card`, `bg-ch-surface-highest`, `text-ch-primary`, `border-ch-border`) and Material Symbols icons.
  - No flashy gamified animations or neon styling.
- **Micro-Celebration Banner Behavior (Flow 3 / UX Spec):**
  - `MicroCelebrationBanner` renders with `role="status"` and `aria-live="polite"`.
  - Automatically dismisses after 4 seconds or upon user tap.

### Files to Modify / Create (Audit)

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/tictactore/dto/InsightType.java` | NEW | Enum for insight types |
| `src/main/java/com/tictactore/dto/InsightCategory.java` | NEW | Enum for insight categories |
| `src/main/java/com/tictactore/dto/InsightImportance.java` | NEW | Enum for insight importance levels |
| `src/main/java/com/tictactore/dto/PlayerInsightDto.java` | NEW | Record for individual insight DTO |
| `src/main/java/com/tictactore/dto/PlayerInsightsResponse.java` | NEW | Record for insights list response |
| `src/main/java/com/tictactore/service/insight/InsightGenerator.java` | NEW | Strategy interface for insight generators |
| `src/main/java/com/tictactore/service/insight/generator/WinStreakInsightGenerator.java` | NEW | Evaluator for win streak insights |
| `src/main/java/com/tictactore/service/insight/generator/FormTrendInsightGenerator.java` | NEW | Evaluator for form and improvement trends |
| `src/main/java/com/tictactore/service/insight/generator/PositionalMasteryInsightGenerator.java` | NEW | Evaluator for Attacker vs Defender mastery |
| `src/main/java/com/tictactore/service/insight/generator/BestPartnershipInsightGenerator.java` | NEW | Evaluator for 2v2 partner synergy |
| `src/main/java/com/tictactore/service/insight/generator/MilestoneProximityInsightGenerator.java` | NEW | Evaluator for achievement milestone proximity |
| `src/main/java/com/tictactore/service/InsightService.java` | NEW | Service interface for aggregating insights |
| `src/main/java/com/tictactore/service/impl/InsightServiceImpl.java` | NEW | Service implementation with single-pass aggregation |
| `src/main/java/com/tictactore/controller/InsightController.java` | NEW | REST controller for `/api/v1/players/{id}/insights` |
| `src/test/java/com/tictactore/service/insight/InsightGeneratorTest.java` | NEW | Unit tests for insight generators |
| `src/test/java/com/tictactore/service/insight/InsightServiceTest.java` | NEW | Unit tests for insight service |
| `src/test/java/com/tictactore/controller/InsightControllerATDDTest.java` | NEW | ATDD controller contract tests |
| `frontend/src/services/insightService.ts` | NEW | Frontend HTTP service for insights API |
| `frontend/src/features/stats/stores/useInsightStore.ts` | NEW | Pinia store for player insights |
| `frontend/src/features/stats/utils/demoDataGenerator.ts` | UPDATE | Add demo insights generation |
| `frontend/src/features/stats/components/InsightCard.vue` | NEW | UI card for rendering single insight |
| `frontend/src/features/stats/components/MicroCelebrationBanner.vue` | NEW | Post-confirmation reward banner |
| `frontend/src/features/stats/components/InsightsSection.vue` | NEW | Insights section container |
| `frontend/src/features/stats/components/StatsDashboard.vue` | UPDATE | Mount InsightsSection in personal stats tab |
| `frontend/src/features/profile/Cabinet.vue` | UPDATE | Mount InsightsSection in Cabinet view |
| `frontend/src/views/HomeView.vue` | UPDATE | Mount MicroCelebrationBanner |
| `frontend/src/locales/en.json` | UPDATE | English translations for insights |
| `frontend/src/locales/de.json` | UPDATE | German translations for insights |
| `frontend/src/features/stats/components/__tests__/InsightCard.spec.ts` | NEW | Component tests for InsightCard |
| `frontend/src/features/stats/components/__tests__/MicroCelebrationBanner.spec.ts` | NEW | Component tests for MicroCelebrationBanner |
| `frontend/src/features/stats/stores/__tests__/useInsightStore.spec.ts` | NEW | Unit tests for useInsightStore |
| `frontend/e2e/statistical-insights.spec.ts` | NEW | Playwright E2E tests for insights |

### ATDD Artifacts

- Checklist: `_bmad-output/test-artifacts/atdd-checklist-7-5-auto-generated-statistical-insights.md`
- Backend ATDD Controller test: `src/test/java/com/tictactore/controller/InsightControllerATDDTest.java`
- Backend Unit test: `src/test/java/com/tictactore/service/insight/InsightGeneratorTest.java`
- Frontend Component tests: `frontend/src/features/stats/components/__tests__/InsightCard.spec.ts`
- Frontend E2E test: `frontend/e2e/statistical-insights.spec.ts`

## Dev Agent Record

### File List

- `src/main/java/com/tictactore/dto/InsightType.java`
- `src/main/java/com/tictactore/dto/InsightCategory.java`
- `src/main/java/com/tictactore/dto/InsightImportance.java`
- `src/main/java/com/tictactore/dto/PlayerInsightDto.java`
- `src/main/java/com/tictactore/dto/PlayerInsightsResponse.java`
- `src/main/java/com/tictactore/service/insight/InsightGenerator.java`
- `src/main/java/com/tictactore/service/insight/generator/WinStreakInsightGenerator.java`
- `src/main/java/com/tictactore/service/insight/generator/FormTrendInsightGenerator.java`
- `src/main/java/com/tictactore/service/insight/generator/PositionalMasteryInsightGenerator.java`
- `src/main/java/com/tictactore/service/insight/generator/BestPartnershipInsightGenerator.java`
- `src/main/java/com/tictactore/service/insight/generator/MilestoneProximityInsightGenerator.java`
- `src/main/java/com/tictactore/service/InsightService.java`
- `src/main/java/com/tictactore/service/impl/InsightServiceImpl.java`
- `src/main/java/com/tictactore/controller/InsightController.java`
- `src/test/java/com/tictactore/service/insight/InsightGeneratorTest.java`
- `src/test/java/com/tictactore/service/insight/InsightServiceTest.java`
- `src/test/java/com/tictactore/controller/InsightControllerATDDTest.java`
- `frontend/src/services/insightService.ts`
- `frontend/src/features/stats/stores/useInsightStore.ts`
- `frontend/src/features/stats/utils/demoDataGenerator.ts`
- `frontend/src/features/stats/components/InsightCard.vue`
- `frontend/src/features/stats/components/MicroCelebrationBanner.vue`
- `frontend/src/features/stats/components/InsightsSection.vue`
- `frontend/src/features/stats/components/StatsDashboard.vue`
- `frontend/src/features/profile/Cabinet.vue`
- `frontend/src/views/HomeView.vue`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`
- `frontend/src/features/stats/components/__tests__/InsightCard.spec.ts`
- `frontend/src/features/stats/components/__tests__/MicroCelebrationBanner.spec.ts`
- `frontend/src/features/stats/stores/__tests__/useInsightStore.spec.ts`
- `frontend/e2e/statistical-insights.spec.ts`

