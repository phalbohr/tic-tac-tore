---
stepsCompleted:
  - 'step-01-preflight-and-context'
  - 'step-02-generation-mode'
  - 'step-03-test-strategy'
  - 'step-04c-aggregate'
  - 'step-05-validate-and-complete'
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-31'
storyId: '7.5'
storyKey: '7-5-auto-generated-statistical-insights'
storyFile: '_bmad-output/implementation-artifacts/7-5-auto-generated-statistical-insights.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-7-5-auto-generated-statistical-insights.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-7-5/InsightGeneratorATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-7-5/InsightControllerATDDTest.java'
  - 'frontend/src/features/stats/components/__tests__/InsightCard.spec.ts'
  - 'frontend/src/features/stats/components/__tests__/MicroCelebrationBanner.spec.ts'
  - 'frontend/e2e/statistical-insights.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/7-5-auto-generated-statistical-insights.md'
  - '_bmad/tea/config.yaml'
  - '.agent/skills/bmad-testarch-atdd/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/knowledge/test-priorities-matrix.md'
---

# ATDD Checklist: Story 7.5 — Auto-generated Statistical Insights

## Preflight & Context Summary

- **Story**: Story 7.5: Auto-generated Statistical Insights (`_bmad-output/implementation-artifacts/7-5-auto-generated-statistical-insights.md`)
- **Detected Stack**: `fullstack` (Spring Boot Java backend + Vite / Vue 3 / TypeScript frontend)
- **Target Acceptance Criteria**:
  - **AC1**: `InsightEngine` dynamically evaluates match history and generates up to 5 prioritized, non-judgmental insight observations (`FR53`). Unique ID, type, category (`STREAK`, `TREND`, `POSITION`, `PARTNERSHIP`, `MILESTONE`), localized title/description keys, parameter map, Material Symbols icon, importance (`HIGH`, `MEDIUM`, `LOW`), optional `drillDownUrl`.
  - **AC2**: Concrete insight generators:
    - `WIN_STREAK`: $\ge 3$ consecutive recent match wins (`{streak: count}`).
    - `FORM_TREND`: Win rate in last 5–10 matches exceeds career win rate by $\ge 15\%$ (`{recentWinRate, careerWinRate, diff}`).
    - `POSITIONAL_MASTERY`: Win rate delta $\ge 20\%$ between Attacker and Defender with $\ge 5$ matches each (`{favoredPosition, higherWinRate, lowerWinRate}`).
    - `BEST_PARTNERSHIP`: 2v2 partner win rate $\ge 70\%$ over $\ge 3$ matches (`{partnerId, partnerName, winRate, matches}`, `drillDownUrl: "/statistics?tab=teams"`).
    - `MILESTONE_PROXIMITY`: Within 2 matches or goals of unlocking a progressive achievement badge (`{badgeCode, remaining, current, target}`, `drillDownUrl: "/cabinet"`).
  - **AC3**: Graceful handling of $< 3$ matches: returns empty collection or single `INSUFFICIENT_DATA` starter insight without exceptions, division-by-zero, or NaN values.
  - **AC4**: `GET /api/v1/players/{id}/insights` and `GET /api/v1/statistics/insights` return `200 OK` with `PlayerInsightsResponse`, JWT authentication, zero PII leak (`AD-04`, `AD-05`), in-memory single-pass calculation (zero DB write amplification).
  - **AC5**: `InsightsSection.vue` and `InsightCard.vue` render insights with Clubhouse Editorial tokens (`ch-` utility classes, tactile background, no neon gaming cliches per `UX-DR3`), drill-down navigation.
  - **AC6**: `MicroCelebrationBanner.vue` appears on Home Hub after match confirmation displaying recent high-priority insight with entry fade, auto-dismisses after 4s, `role="status"` and `aria-live="polite"`.
  - **AC7**: Demo mode and $< 5$ matches support in `demoDataGenerator.ts`.
  - **AC8**: English and German localization in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.

## Generation Mode Selection

- **Selected Mode**: `AI Generation`
- **Execution Mode**: `sequential` (Deterministic generation of Backend ATDD, Frontend Component, and Playwright E2E red-phase scaffolds)

## Test Strategy & Coverage Matrix

| AC | Scenario | Test Level | Priority | Target Test File | Red-Phase Status |
|---|---|---|---|---|---|
| AC2 | `WIN_STREAK`: $\ge 3$ consecutive wins generates high importance insight; $< 3$ or broken streak returns empty | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-5/InsightGeneratorATDDTest.java` | 🔴 Red-phase scaffold |
| AC2 | `FORM_TREND`: Recent win rate (last 5–10) exceeding career by $\ge 15\%$ generates insight; $< 15\%$ returns empty | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-5/InsightGeneratorATDDTest.java` | 🔴 Red-phase scaffold |
| AC2 | `POSITIONAL_MASTERY`: Win rate delta $\ge 20\%$ with $\ge 5$ matches each position generates insight | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-5/InsightGeneratorATDDTest.java` | 🔴 Red-phase scaffold |
| AC2 | `BEST_PARTNERSHIP`: 2v2 partner win rate $\ge 70\%$ with $\ge 3$ matches generates insight with `drillDownUrl` | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-5/InsightGeneratorATDDTest.java` | 🔴 Red-phase scaffold |
| AC2 | `MILESTONE_PROXIMITY`: Locked progressive badge with remaining $\le 2$ generates proximity insight with `drillDownUrl` | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-5/InsightGeneratorATDDTest.java` | 🔴 Red-phase scaffold |
| AC3 | Safe math & insufficient data: $< 3$ matches produces safe starter or empty response without division-by-zero or NaN | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-5/InsightGeneratorATDDTest.java` | 🔴 Red-phase scaffold |
| AC4 | `GET /api/v1/players/{id}/insights` and `GET /api/v1/statistics/insights` return 200 OK with `PlayerInsightsResponse` | Backend Integration / Controller ATDD (MockMvc) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-5/InsightControllerATDDTest.java` | 🔴 Red-phase scaffold |
| AC4 | Auth enforcement (401 Unauthorized for anonymous) and zero PII leak in responses | Backend Integration / Controller ATDD (MockMvc) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-5/InsightControllerATDDTest.java` | 🔴 Red-phase scaffold |
| AC5 | `InsightCard.vue` renders icon, localized title, interpolated description, category badge, and drill-down CTA button | Frontend Component (Vitest + VTU) | P0 | `frontend/src/features/stats/components/__tests__/InsightCard.spec.ts` | 🔴 `describe.skip()` scaffold |
| AC6 | `MicroCelebrationBanner.vue` renders with `role="status"` & `aria-live="polite"`, auto-dismisses after 4s, dismiss button | Frontend Component (Vitest + VTU) | P0 | `frontend/src/features/stats/components/__tests__/MicroCelebrationBanner.spec.ts` | 🔴 `describe.skip()` scaffold |
| AC1, AC5, AC6, AC7 | End-to-end insights user journey: personal insights in Statistics Hub, drill-down navigation, Home celebration banner, demo mode | Frontend E2E (Playwright) | P0 | `frontend/e2e/statistical-insights.spec.ts` | 🔴 `test.skip()` scaffold |

## Generated Test Files

1. [`InsightGeneratorATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-7-5/InsightGeneratorATDDTest.java) — Backend Evaluator unit ATDD test scaffold
2. [`InsightControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-7-5/InsightControllerATDDTest.java) — Backend REST Controller integration ATDD test scaffold
3. [`InsightCard.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/src/features/stats/components/__tests__/InsightCard.spec.ts) — Frontend component test scaffold (`describe.skip`)
4. [`MicroCelebrationBanner.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/src/features/stats/components/__tests__/MicroCelebrationBanner.spec.ts) — Frontend celebration banner test scaffold (`describe.skip`)
5. [`statistical-insights.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/statistical-insights.spec.ts) — Frontend E2E Playwright test scaffold (`test.skip`)

## Task-by-Task Red-Green-Refactor Plan (for `dev-story`)

### Task 1: Backend Domain & Insight Generation Engine (AC1, AC2, AC3, AC4)
- [ ] Create domain records and enums:
  - `com.tictactore.dto.InsightType` (`WIN_STREAK`, `FORM_TREND`, `POSITIONAL_MASTERY`, `BEST_PARTNERSHIP`, `MILESTONE_PROXIMITY`, `INSUFFICIENT_DATA`).
  - `com.tictactore.dto.InsightCategory` (`STREAK`, `TREND`, `POSITION`, `PARTNERSHIP`, `MILESTONE`, `GENERAL`).
  - `com.tictactore.dto.InsightImportance` (`HIGH`, `MEDIUM`, `LOW`).
  - `com.tictactore.dto.PlayerInsightDto` (`UUID id`, `InsightType type`, `InsightCategory category`, `InsightImportance importance`, `String titleKey`, `String descriptionKey`, `Map<String, Object> params`, `String icon`, `String drillDownUrl`).
  - `com.tictactore.dto.PlayerInsightsResponse` (`UUID playerId`, `int totalCount`, `List<PlayerInsightDto> insights`).
- [ ] Create `com.tictactore.service.insight.InsightGenerator` interface (`Optional<PlayerInsightDto> generate(...)`, `int getOrder()`).
- [ ] Implement concrete generator components in `com.tictactore.service.insight.generator`:
  - `WinStreakInsightGenerator` (`WIN_STREAK` — evaluates recent consecutive match wins).
  - `FormTrendInsightGenerator` (`FORM_TREND` — compares win rate of last 5–10 matches against career average).
  - `PositionalMasteryInsightGenerator` (`POSITIONAL_MASTERY` — compares Attacker vs Defender win rates with min 5 matches each).
  - `BestPartnershipInsightGenerator` (`BEST_PARTNERSHIP` — analyzes 2v2 partner win rates with min 3 matches).
  - `MilestoneProximityInsightGenerator` (`MILESTONE_PROXIMITY` — checks locked progressive badges with remaining progress $\le 2$).
- [ ] Create `com.tictactore.service.InsightService` and `com.tictactore.service.impl.InsightServiceImpl`.
- [ ] Move `InsightGeneratorATDDTest.java` to `src/test/java/com/tictactore/service/insight/InsightGeneratorTest.java` and verify green phase.

### Task 2: REST Controller & Security Integration (AC4)
- [ ] Create `com.tictactore.controller.InsightController` (or update `StatisticsController`):
  - `GET /api/v1/players/{id}/insights` -> returns `200 OK` with `PlayerInsightsResponse`.
  - `GET /api/v1/statistics/insights` -> returns `200 OK` for `@AuthenticationPrincipal User principal`.
  - Enforce authentication and sanitize output (no PII/email).
- [ ] Move `InsightControllerATDDTest.java` to `src/test/java/com/tictactore/controller/InsightControllerATDDTest.java` and verify green phase.

### Task 3: Frontend Service, Store & Demo Generator (AC1, AC4, AC7)
- [ ] Create `frontend/src/services/insightService.ts` with TypeScript interfaces and API methods (`getPlayerInsights`, `getMyInsights`).
- [ ] Create `frontend/src/features/stats/stores/useInsightStore.ts` with Pinia state (`insights`, `isLoading`, `error`), getters (`topInsights`, `latestCelebrationInsight`), and actions (`fetchInsights`).
- [ ] Update `frontend/src/features/stats/utils/demoDataGenerator.ts` to include `generateDemoInsights(): PlayerInsight[]`.

### Task 4: Frontend UI Components & Views Integration (AC5, AC6)
- [ ] Create `frontend/src/features/stats/components/InsightCard.vue` with Clubhouse Editorial tokens (`UX-DR3`, `IP-04`).
- [ ] Create `frontend/src/features/stats/components/MicroCelebrationBanner.vue` with `role="status"`, `aria-live="polite"`, 4s auto-dismiss.
- [ ] Create `frontend/src/features/stats/components/InsightsSection.vue`.
- [ ] Mount `InsightsSection.vue` in `StatsDashboard.vue` and `Cabinet.vue`.
- [ ] Mount `MicroCelebrationBanner.vue` in `HomeView.vue`.
- [ ] Activate `InsightCard.spec.ts` and `MicroCelebrationBanner.spec.ts` (remove `describe.skip`) and verify green phase.

### Task 5: Localization in English & German (AC8)
- [ ] Add translation keys to `frontend/src/locales/en.json` and `frontend/src/locales/de.json` for insight titles, descriptions, categories, and empty states.

### Task 6: E2E Verification & Full Quality Gate
- [ ] Activate `frontend/e2e/statistical-insights.spec.ts` (remove `test.skip`).
- [ ] Execute `./scripts/ci-local.sh` and ensure 100% test pass across backend compilation, unit/ATDD/integration tests, frontend type-check, lint, unit tests, and Playwright E2E suite.
