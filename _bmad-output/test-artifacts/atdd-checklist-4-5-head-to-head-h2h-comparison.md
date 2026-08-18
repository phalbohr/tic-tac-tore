---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-18T21:59:15+02:00'
storyId: '4.5'
storyKey: '4-5-head-to-head-h2h-comparison'
storyFile: '_bmad-output/implementation-artifacts/4-5-head-to-head-h2h-comparison.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-4-5-head-to-head-h2h-comparison.md'
generatedTestFiles:
  - 'src/test/java/com/tictactore/controller/StatisticsControllerATDDTest.java'
  - 'frontend/e2e/head-to-head-statistics.spec.ts'
  - 'frontend/tests/unit/h2hCrossTabMatrix.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/4-5-head-to-head-h2h-comparison.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/overview.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/api-request.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 4.5

## Story Context
- **Story Key:** `4-5-head-to-head-h2h-comparison`
- **Story ID:** `4.5`
- **Title:** Story 4.5: Head-to-Head (H2H) Comparison
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/4-5-head-to-head-h2h-comparison.md`

## Acceptance Criteria Summary
1. **AC 1:** Opponent profile header (avatar, nickname) & three cross-tabulated tables for Matches, Games, and Goals (With vs Vs performance, positional breakdown).
2. **AC 2:** Filter H2H statistics by time period (`period`), rule system (`ruleConfigId`), or match type (`matchType`).
3. **AC 3:** Empty state CTA (`EmptyStateCTA`) when 0 shared matches with action button navigating to match creation with opponent pre-selected.
4. **AC 4:** Demo mode data generation via `demoDataGenerator.ts` allowing exploration of all matrices.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria and REST/UI requirements matching established patterns in Epics 1–4.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Opponent profile header and 3 cross-tab tables (Matches, Games, Positional Goals) | API / Integration | Backend `StatisticsController` & Service | P0 | 1. Return 200 OK with `opponent`, `matches`, `games`, `goals` breakdown<br>2. Differentiate "With" (teammates in 2v2) vs "Vs" (opponents in 1v1/2v2)<br>3. Positional goal matrix (4 combinations) |
| **AC 1** | Store and UI state management for H2H matrix | Unit (Vitest) | `useStatsStore.ts` & `H2HCrossTabMatrix.vue` | P0 | 1. `fetchH2HStats(opponentId)` populates store state<br>2. Component renders 3 tables according to "No-Line" rule (UX-DR3) |
| **AC 1** | E2E Head-to-Head full user journey | E2E (Playwright) | `frontend/e2e/head-to-head-statistics.spec.ts` | P0 | 1. Open `/statistics?tab=h2h&opponentId=...`<br>2. Display opponent profile, headers, percentages |
| **AC 2** | Filter by period, ruleConfigId, and matchType | API / Integration | Backend `StatisticsController` | P1 | 1. Filter by `period` (WEEKLY, MONTHLY, YEARLY, ALL_TIME)<br>2. Filter by `ruleConfigId`<br>3. Filter by `matchType` (1v1, 2v2) |
| **AC 2** | Filter controls reactive update | E2E (Playwright) | `frontend/e2e/head-to-head-statistics.spec.ts` | P1 | 1. Changing filters in UI triggers re-fetch with new query params |
| **AC 3** | Empty state CTA when 0 matches | E2E (Playwright) / Unit | `frontend/e2e/head-to-head-statistics.spec.ts` | P1 | 1. Display `EmptyStateCTA` message and "Start a match" button<br>2. Clicking navigates to `/matches/new?opponentId=...` |
| **AC 4** | Demo mode realistic H2H data | Unit (Vitest) / E2E | `useStatsStore.ts` & `demoDataGenerator.ts` | P2 | 1. In demo mode, generate realistic 3-table data |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated (Skipped/Disabled):**
- **Backend API Tests:** `src/test/java/com/tictactore/controller/StatisticsControllerATDDTest.java` (`HeadToHeadEndpointSpecs` with 3 test cases)
- **Frontend Unit Tests:** `frontend/tests/unit/h2hCrossTabMatrix.spec.ts` (3 test cases skipped)
- **Frontend E2E Tests:** `frontend/e2e/head-to-head-statistics.spec.ts` (3 test scenarios skipped)

## Next Steps (Task-by-Task Activation)

During implementation of Story 4.5 in `dev-story`:
1. Activate backend API tests in `StatisticsControllerATDDTest.java` and implement DTOs/Service/Controller.
2. Activate frontend unit tests in `h2hCrossTabMatrix.spec.ts` and implement store/service/demo data.
3. Activate frontend E2E tests in `head-to-head-statistics.spec.ts` and implement Vue components.
4. Verify all tests pass (Green Phase) and execute `./scripts/ci-local.sh`.
