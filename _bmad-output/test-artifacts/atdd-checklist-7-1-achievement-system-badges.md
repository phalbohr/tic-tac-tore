---
stepsCompleted:
  - 'step-01-preflight-and-context'
  - 'step-02-generation-mode'
  - 'step-03-test-strategy'
  - 'step-04c-aggregate'
  - 'step-05-validate-and-complete'
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-30'
storyId: '7.1'
storyKey: '7-1-achievement-system-badges'
storyFile: '_bmad-output/implementation-artifacts/7-1-achievement-system-badges.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-7-1-achievement-system-badges.md'
generatedTestFiles:
  - 'src/test/java/com/tictactore/controller/AchievementControllerATDDTest.java'
  - 'frontend/e2e/achievements-profile.spec.ts'
  - 'frontend/src/features/achievements/stores/__tests__/useAchievementStore.spec.ts'
  - 'frontend/src/features/achievements/components/__tests__/BadgeCard.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/7-1-achievement-system-badges.md'
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

# ATDD Checklist: Story 7.1 — Achievement System (Badges)

## Preflight & Context Summary

- **Story**: Story 7.1: Achievement System (Badges) (`_bmad-output/implementation-artifacts/7-1-achievement-system-badges.md`)
- **Detected Stack**: `fullstack` (Spring Boot Java backend + Vite / Vue 3 / TypeScript frontend)
- **Target Acceptance Criteria**:
  - **AC1**: Match confirmed event publishes asynchronously; triggers non-blocking achievement evaluation (<200ms latency impact).
  - **AC2**: Achievement evaluation engine awards badges with unique `(user_id, achievement_id)` constraint; seeds initial 5 badges (`FIRST_WIN`, `MATCHES_10`, `CLEAN_SHEET`, `STRIKER_50`, `DEFENSE_WALL`).
  - **AC3**: `GET /api/v1/players/{id}/achievements` returns 200 OK with summary DTO, unlock status, without leaking PII.
  - **AC4**: Cabinet UI displays `ProfileBadgesSection.vue` with Clubhouse Editorial tokens and badge details on hover/tap.
  - **AC5**: i18n localization in `en.json` and `de.json`.

## Generation Mode Selection

- **Selected Mode**: `AI Generation`
- **Execution Mode**: `sequential` (Deterministic execution of API and E2E red-phase test generation)

## Test Strategy & Coverage Matrix

| AC | Scenario | Test Level | Priority | Target Test File | Red-Phase Status |
|---|---|---|---|---|---|
| AC3 | `GET /api/v1/players/{id}/achievements` returns 200 OK + summary DTO | Backend Controller (MockMvc) | P0 | `src/test/java/com/tictactore/controller/AchievementControllerATDDTest.java` | 🔴 `@Disabled` scaffold |
| AC3 | `GET /api/v1/players/{id}/achievements` requires authentication (401) | Backend Controller (MockMvc) | P0 | `src/test/java/com/tictactore/controller/AchievementControllerATDDTest.java` | 🔴 `@Disabled` scaffold |
| AC3 | `GET /api/v1/players/{id}/achievements` sanitizes PII (no email/passwords) per AD-04 | Backend Controller (MockMvc) | P0 | `src/test/java/com/tictactore/controller/AchievementControllerATDDTest.java` | 🔴 `@Disabled` scaffold |
| AC2 | Strategy Evaluators evaluate match & stats | Backend Unit (JUnit 5 + AssertJ) | P0 | `src/test/java/com/tictactore/service/achievement/AchievementEvaluatorTest.java` | 🔴 Planned in dev-story |
| AC1, AC2 | `AchievementService` evaluates match participants and awards badges idempotently | Backend Service (JUnit 5 + Mockito) | P0 | `src/test/java/com/tictactore/service/AchievementServiceTest.java` | 🔴 Planned in dev-story |
| AC4 | `ProfileBadgesSection` renders in Cabinet with earned and locked badges | Frontend E2E (Playwright) | P0 | `frontend/e2e/achievements-profile.spec.ts` | 🔴 `test.skip()` |
| AC4 | Badge details, description, and unlock timestamp display on click/hover | Frontend E2E (Playwright) | P1 | `frontend/e2e/achievements-profile.spec.ts` | 🔴 `test.skip()` |
| AC5 | Localized badge text and headers for German / English | Frontend E2E (Playwright) | P2 | `frontend/e2e/achievements-profile.spec.ts` | 🔴 `test.skip()` |
| AC3, AC4 | `useAchievementStore` fetches and splits badges into `unlockedList` and `lockedList` | Frontend Store (Vitest) | P1 | `frontend/src/features/achievements/stores/__tests__/useAchievementStore.spec.ts` | 🔴 `describe.skip()` |
| AC4, AC5 | `BadgeCard.vue` renders badges with Clubhouse styling & i18n | Frontend Component (Vitest + VTU) | P1 | `frontend/src/features/achievements/components/__tests__/BadgeCard.spec.ts` | 🔴 `describe.skip()` |

## Generated Test Files

1. [AchievementControllerATDDTest.java](file:///Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/controller/AchievementControllerATDDTest.java) — Backend REST ATDD test scaffold (`@Disabled`)
2. [achievements-profile.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/achievements-profile.spec.ts) — Frontend E2E Playwright test scaffold (`test.skip`)
3. [useAchievementStore.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/src/features/achievements/stores/__tests__/useAchievementStore.spec.ts) — Pinia store test scaffold (`describe.skip`)
4. [BadgeCard.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/src/features/achievements/components/__tests__/BadgeCard.spec.ts) — Vue component test scaffold (`describe.skip`)

## Task-by-Task Red-Green-Refactor Plan (for `dev-story`)

### Task 1: Database Migration & Domain Entities (AC1, AC2)
- [ ] Flyway migration `V16__create_achievements_tables.sql` with unique constraint `(user_id, achievement_id)`
- [ ] JPA entities `Achievement` and `PlayerAchievement`
- [ ] Repositories `AchievementRepository` and `PlayerAchievementRepository`

### Task 2: Domain Events & Evaluator Engine (AC1, AC2)
- [ ] `MatchConfirmedEvent` record and publisher integration in `MatchServiceImpl`
- [ ] `AchievementEvaluator` interface and 5 evaluators (`FirstWinEvaluator`, `MatchesPlayedEvaluator`, `CleanSheetEvaluator`, `StrikerGoalsEvaluator`, `DefenseWallEvaluator`)
- [ ] `@Async` and `@TransactionalEventListener(phase = AFTER_COMMIT)` `AchievementEventListener`
- [ ] `AchievementService` and `AchievementServiceImpl`

### Task 3: REST Controller & DTOs (AC3)
- [ ] DTOs `AchievementDto` and `PlayerAchievementsSummaryResponse`
- [ ] `AchievementController` mapping `GET /api/v1/players/{id}/achievements`
- [ ] Activate `AchievementControllerATDDTest.java` (remove `@Disabled`) and verify green phase

### Task 4: Frontend Service & Pinia Store (AC3, AC4)
- [ ] `achievementService.ts`
- [ ] `useAchievementStore.ts`
- [ ] Activate `useAchievementStore.spec.ts` and verify green phase

### Task 5: Frontend UI Components & Profile Integration (AC4, AC5)
- [ ] `BadgeCard.vue` and `ProfileBadgesSection.vue` with Clubhouse styling (`ch-` tokens)
- [ ] Integration into `Cabinet.vue`
- [ ] i18n translation keys in `en.json` and `de.json`
- [ ] Activate `BadgeCard.spec.ts` and `achievements-profile.spec.ts`, verify green phase

### Task 6: Full Verification
- [ ] Run `./scripts/ci-local.sh` and ensure 100% test pass.
