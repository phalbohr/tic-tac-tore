---
stepsCompleted:
  - 'step-01-preflight-and-context'
  - 'step-02-generation-mode'
  - 'step-03-test-strategy'
  - 'step-04c-aggregate'
  - 'step-05-validate-and-complete'
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-30'
storyId: '7.3'
storyKey: '7-3-award-wall-and-progress-tracking'
storyFile: '_bmad-output/implementation-artifacts/7-3-award-wall-and-progress-tracking.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-7-3-award-wall-and-progress-tracking.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-7-3/AchievementProgressEvaluatorATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-7-3/AchievementProgressControllerATDDTest.java'
  - 'frontend/e2e/achievements-progress-wall.spec.ts'
  - 'frontend/src/features/achievements/components/__tests__/BadgeCardProgress.spec.ts'
  - 'frontend/src/features/achievements/components/__tests__/ProfileBadgesSectionFilter.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/7-3-award-wall-and-progress-tracking.md'
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

# ATDD Checklist: Story 7.3 — Award Wall and Progress Tracking

## Preflight & Context Summary

- **Story**: Story 7.3: Award Wall and Progress Tracking (`_bmad-output/implementation-artifacts/7-3-award-wall-and-progress-tracking.md`)
- **Detected Stack**: `fullstack` (Spring Boot Java backend + Vite / Vue 3 / TypeScript frontend)
- **Target Acceptance Criteria**:
  - **AC1**: Award Wall gallery (`FR50`), category filter tabs (`All`, `Badges`, `Anti-Achievements`), summary counters (`totalUnlocked / totalAvailable` + category counts).
  - **AC2**: Dynamic numerical progress calculation for locked progressive achievements (`MATCHES_10`, `STRIKER_50`, `DEFENSE_WALL`, `FIRST_WIN`) via `AchievementEvaluator.getProgress(userId, stats)` using in-memory `PlayerStatsContext` (zero DB write amplification).
  - **AC3**: Non-progressive achievements return `hasProgress=false, currentProgress=null, targetValue=null`. Unlocked progressive achievements return `isUnlocked=true, hasProgress=true, currentProgress=targetValue`.
  - **AC4**: `GET /api/v1/players/{id}/achievements` returns 200 with `PlayerAchievementsSummaryResponse` enriched with progress metadata; auth check; zero PII leak (`AD-04`, `AD-05`).
  - **AC5**: `BadgeCard.vue` renders subtle Clubhouse Editorial progress bar and ratio (`3 / 10`) for locked progressive badges; detail modal displays full progress bar with percentage and remaining count (`UX-DR3`).
  - **AC6**: English and German localization (`en.json`, `de.json`) for filter tabs, progress labels, and modal counters (`FR59`).

## Generation Mode Selection

- **Selected Mode**: `AI Generation`
- **Execution Mode**: `sequential` (Deterministic generation of Backend ATDD, Frontend Component, and Playwright E2E red-phase scaffolds)

## Test Strategy & Coverage Matrix

| AC | Scenario | Test Level | Priority | Target Test File | Red-Phase Status |
|---|---|---|---|---|---|
| AC2 | Partial & capped progress calculation on progressive evaluators (`MATCHES_10`, `STRIKER_50`, `DEFENSE_WALL`, `FIRST_WIN`) | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-3/AchievementProgressEvaluatorATDDTest.java` | 🔴 `@Disabled` scaffold |
| AC3 | Non-progressive evaluators return `hasProgress=false, current=0, target=0` via default `getProgress` | Backend Unit / ATDD (JUnit 5 + AssertJ) | P1 | `_bmad-output/test-artifacts/atdd-redphase-7-3/AchievementProgressEvaluatorATDDTest.java` | 🔴 `@Disabled` scaffold |
| AC2, AC3, AC4 | `GET /api/v1/players/{id}/achievements` returns enriched DTO with progress metadata, sets `currentProgress=targetValue` on unlocked badges, handles non-progressive nulls | Backend Integration / Controller ATDD (MockMvc) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-3/AchievementProgressControllerATDDTest.java` | 🔴 `@Disabled` scaffold |
| AC4 | Auth enforcement (401 for unauthenticated) and zero PII leak in response | Backend Integration / Controller ATDD (MockMvc) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-3/AchievementProgressControllerATDDTest.java` | 🔴 `@Disabled` scaffold |
| AC5 | `BadgeCard.vue` displays mini progress bar and ratio (`4 / 10`) for locked progressive badge; hidden when unlocked or non-progressive | Frontend Component (Vitest + VTU) | P0 | `frontend/src/features/achievements/components/__tests__/BadgeCardProgress.spec.ts` | 🔴 `describe.skip()` scaffold |
| AC1, AC5 | `ProfileBadgesSection.vue` renders filter tabs, filters badge list on tab switch, renders modal progress bar with remaining count | Frontend Component (Vitest + Pinia + VTU) | P0 | `frontend/src/features/achievements/components/__tests__/ProfileBadgesSectionFilter.spec.ts` | 🔴 `describe.skip()` scaffold |
| AC1, AC2, AC5 | End-to-end user journey in Personal Cabinet (`/cabinet`): filter tabs switching, card progress bar, detail modal with remaining count | Frontend E2E (Playwright) | P0 | `frontend/e2e/achievements-progress-wall.spec.ts` | 🔴 `test.skip()` scaffold |

## Generated Test Files

1. [`AchievementProgressEvaluatorATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-7-3/AchievementProgressEvaluatorATDDTest.java) — Backend Evaluator unit ATDD test scaffold
2. [`AchievementProgressControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-7-3/AchievementProgressControllerATDDTest.java) — Backend REST Controller integration ATDD test scaffold
3. [`BadgeCardProgress.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/src/features/achievements/components/__tests__/BadgeCardProgress.spec.ts) — Frontend component test scaffold (`describe.skip`)
4. [`ProfileBadgesSectionFilter.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/src/features/achievements/components/__tests__/ProfileBadgesSectionFilter.spec.ts) — Frontend component filtering test scaffold (`describe.skip`)
5. [`achievements-progress-wall.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/achievements-progress-wall.spec.ts) — Frontend E2E Playwright test scaffold (`test.skip`)

## Task-by-Task Red-Green-Refactor Plan (for `dev-story`)

### Task 1: Backend Domain & Evaluator Extensions (AC2, AC3)
- [ ] Create `com.tictactore.service.achievement.ProgressInfo` record (`long current`, `long target`, `boolean hasProgress`).
- [ ] Add default method `ProgressInfo getProgress(UUID userId, PlayerStatsContext stats)` to `AchievementEvaluator`.
- [ ] Implement `getProgress` in `MatchesPlayedEvaluator`, `StrikerGoalsEvaluator`, `DefenseWallEvaluator`, `FirstWinEvaluator`.
- [ ] Activate `AchievementProgressEvaluatorATDDTest.java` (remove `@Disabled`) and integrate into `AchievementEvaluatorTest.java` to verify green phase.

### Task 2: Backend DTO & Service Implementation (AC2, AC3, AC4)
- [ ] Update `AchievementDto` record: add `Long currentProgress`, `Long targetValue`, `boolean hasProgress`.
- [ ] Update `AchievementServiceImpl.getPlayerAchievements(UUID playerId)` to calculate progress on-the-fly via `PlayerStatsContext`.
- [ ] Activate `AchievementProgressControllerATDDTest.java` (remove `@Disabled`) and verify green phase.

### Task 3: Frontend Types, Store & Localization (AC1, AC4, AC6)
- [ ] Update TypeScript interface `AchievementDto` in `frontend/src/services/achievementService.ts`.
- [ ] Update `useAchievementStore.ts` with getters for filtered badges (`badgesList`, `antiAchievementsList`).
- [ ] Add localization keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json` (`achievements.filterAll`, `achievements.filterBadges`, `achievements.filterAnti`, `achievements.progress`, `achievements.remaining`).

### Task 4: Frontend UI Components (AC1, AC5)
- [ ] Update `BadgeCard.vue` to render mini progress bar and ratio (`4 / 10`) when `badge.hasProgress && !badge.isUnlocked`.
- [ ] Update `ProfileBadgesSection.vue` to include filter tabs and modal progress bar with remaining count.
- [ ] Activate `BadgeCardProgress.spec.ts` and `ProfileBadgesSectionFilter.spec.ts` (remove `describe.skip`) and verify green phase.

### Task 5: E2E Verification & Full Quality Gate
- [ ] Activate `frontend/e2e/achievements-progress-wall.spec.ts` (remove `test.skip`).
- [ ] Execute `./scripts/ci-local.sh` and ensure 100% test pass across backend, frontend unit tests, and Playwright E2E suites.
