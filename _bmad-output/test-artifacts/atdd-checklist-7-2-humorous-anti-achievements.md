---
stepsCompleted:
  - 'step-01-preflight-and-context'
  - 'step-02-generation-mode'
  - 'step-03-test-strategy'
  - 'step-04c-aggregate'
  - 'step-05-validate-and-complete'
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-30'
storyId: '7.2'
storyKey: '7-2-humorous-anti-achievements'
storyFile: '_bmad-output/implementation-artifacts/7-2-humorous-anti-achievements.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-7-2-humorous-anti-achievements.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-7-2/AntiAchievementEvaluatorATDDTest.java'
  - 'frontend/e2e/achievements-anti-badges.spec.ts'
  - 'frontend/src/features/achievements/components/__tests__/AntiAchievementBadgeCard.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/7-2-humorous-anti-achievements.md'
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

# ATDD Checklist: Story 7.2 — Humorous Anti-achievements

## Preflight & Context Summary

- **Story**: Story 7.2: Humorous Anti-achievements (`_bmad-output/implementation-artifacts/7-2-humorous-anti-achievements.md`)
- **Detected Stack**: `fullstack` (Spring Boot Java backend + Vite / Vue 3 / TypeScript frontend)
- **Target Acceptance Criteria**:
  - **AC1**: `GOOSE_EGG` (`category: ANTI_ACHIEVEMENT`, `icon: egg`) awarded when a player's team lost any game with 0 points scored.
  - **AC2**: `GENEROUS_HOST` (`category: ANTI_ACHIEVEMENT`, `icon: volunteer_activism`) awarded when a player's team conceded 10+ points in a single game.
  - **AC3**: `SIEVE_DEFENSE` (`category: ANTI_ACHIEVEMENT`, `icon: water_drop`) awarded when a player participated as Defender and conceded 15+ goals in a match.
  - **AC4**: `HEARTBREAKER` (`category: ANTI_ACHIEVEMENT`, `icon: heart_broken`) awarded when player lost the deciding game by exactly 1 goal.
  - **AC5**: Tone & culture policy (FR49) — lighthearted, celebratory copy in `en.json` and `de.json`.
  - **AC6**: Frontend badge rendering in `BadgeCard.vue` and `ProfileBadgesSection.vue` with Clubhouse Editorial styling and Material Symbols icons.

## Generation Mode Selection

- **Selected Mode**: `AI Generation`
- **Execution Mode**: `sequential` (Deterministic generation of Backend ATDD, Frontend Component, and E2E red-phase scaffolds)

## Test Strategy & Coverage Matrix

| AC | Scenario | Test Level | Priority | Target Test File | Red-Phase Status |
|---|---|---|---|---|---|
| AC1 | `GOOSE_EGG` awarded when game lost with 0 points scored | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-2/AntiAchievementEvaluatorATDDTest.java` | 🔴 Red-phase scaffold |
| AC2 | `GENEROUS_HOST` awarded when 10+ points conceded in single game | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-2/AntiAchievementEvaluatorATDDTest.java` | 🔴 Red-phase scaffold |
| AC3 | `SIEVE_DEFENSE` awarded when Defender concedes 15+ goals in match | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-2/AntiAchievementEvaluatorATDDTest.java` | 🔴 Red-phase scaffold |
| AC4 | `HEARTBREAKER` awarded when match lost in deciding game by 1 goal | Backend Unit / ATDD (JUnit 5 + AssertJ) | P0 | `_bmad-output/test-artifacts/atdd-redphase-7-2/AntiAchievementEvaluatorATDDTest.java` | 🔴 Red-phase scaffold |
| AC5, AC6 | `BadgeCard.vue` renders anti-achievement category & icons (`egg`, `volunteer_activism`, `water_drop`, `heart_broken`) | Frontend Component (Vitest + VTU) | P0 | `frontend/src/features/achievements/components/__tests__/AntiAchievementBadgeCard.spec.ts` | 🔴 `describe.skip()` scaffold |
| AC6 | `ProfileBadgesSection` renders anti-achievements and modal details | Frontend E2E (Playwright) | P1 | `frontend/e2e/achievements-anti-badges.spec.ts` | 🔴 `test.skip()` scaffold |

## Generated Test Files

1. [AntiAchievementEvaluatorATDDTest.java](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-7-2/AntiAchievementEvaluatorATDDTest.java) — Backend Evaluator ATDD unit test scaffold
2. [AntiAchievementBadgeCard.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/src/features/achievements/components/__tests__/AntiAchievementBadgeCard.spec.ts) — Frontend component test scaffold (`describe.skip`)
3. [achievements-anti-badges.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/achievements-anti-badges.spec.ts) — Frontend E2E Playwright test scaffold (`test.skip`)

## Task-by-Task Red-Green-Refactor Plan (for `dev-story`)

### Task 1: Database Migration for Anti-Achievements Seed Data (AC1-AC5)
- [ ] Create Flyway migration `V17__seed_anti_achievements.sql`
- [ ] Insert 4 badges: `GOOSE_EGG`, `GENEROUS_HOST`, `SIEVE_DEFENSE`, `HEARTBREAKER` with `ANTI_ACHIEVEMENT` category

### Task 2: Concrete Evaluators Implementation (AC1-AC4)
- [ ] Implement `GooseEggEvaluator` (`GOOSE_EGG`)
- [ ] Implement `GenerousHostEvaluator` (`GENEROUS_HOST`)
- [ ] Implement `SieveDefenseEvaluator` (`SIEVE_DEFENSE`)
- [ ] Implement `HeartbreakerEvaluator` (`HEARTBREAKER`)
- [ ] Activate `AntiAchievementEvaluatorATDDTest.java` (remove `@Disabled`) and integrate into `AchievementEvaluatorTest.java` to verify green phase

### Task 3: Localization in English & German (AC5)
- [ ] Add translation strings in `frontend/src/locales/en.json` (celebratory/humorous tone)
- [ ] Add translation strings in `frontend/src/locales/de.json` (celebratory/humorous tone)

### Task 4: Frontend Badge Card & Icon Mapping (AC6)
- [ ] Update `BadgeCard.vue` and `ProfileBadgesSection.vue` to support `ANTI_ACHIEVEMENT` styling and icons (`egg`, `volunteer_activism`, `water_drop`, `heart_broken`)
- [ ] Activate `AntiAchievementBadgeCard.spec.ts` (remove `describe.skip`) and verify green phase
- [ ] Activate `achievements-anti-badges.spec.ts` (remove `test.skip`) and verify green phase

### Task 5: Full Verification
- [ ] Run `./scripts/ci-local.sh` and ensure 100% test pass.
