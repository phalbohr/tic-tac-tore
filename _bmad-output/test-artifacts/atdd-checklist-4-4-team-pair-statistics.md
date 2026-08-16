---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-16T20:46:10+02:00'
storyId: '4.4'
storyKey: '4-4-team-pair-statistics'
storyFile: '_bmad-output/implementation-artifacts/4-4-team-pair-statistics.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-4-4-team-pair-statistics.md'
generatedTestFiles:
  - 'src/test/java/com/tictactore/controller/StatisticsControllerATDDTest.java'
  - 'frontend/e2e/team-pair-statistics.spec.ts'
  - 'frontend/tests/unit/teamPairStats.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/4-4-team-pair-statistics.md'
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

# Acceptance Test-Driven Development (ATDD) Checklist

## Story Context
- **Story Key:** `4-4-team-pair-statistics`
- **Story ID:** `4.4`
- **Title:** Story 4.4: Team (Pair) Statistics
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/4-4-team-pair-statistics.md`

## Acceptance Criteria Summary
1. **AC 1:** Display pair-level performance for teammate combinations (FR23) differentiating specific positional synergies (`attacker_id` / `defender_id`).
2. **AC 2:** Filter team pair statistics by specific player, rule system (`ruleConfigId`), or time period (`period`) (FR20).
3. **AC 3:** Paginated results (FR27) and exclusion of pairs below minimum matches threshold (`minMatches`) (FR28).

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Acceptance criteria and contracts for REST API, JPA queries, and UI components are clear, unambiguous, and follow established project patterns.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Display pair-level performance & positional synergies (attacker/defender combinations) | API / Integration | Backend `StatisticsController` & Service | P0 | 1. Return 200 with pair statistics broken down by attacker/defender<br>2. Distinguish `(A=Attacker, B=Defender)` from `(B=Attacker, A=Defender)`<br>3. Calculate matches, wins, losses, winRate |
| **AC 1** | Store and UI state management for team pairs | Unit (Vitest) | `useStatsStore.ts` | P0 | 1. `fetchTeamPairStats()` populates pair stats state<br>2. Correctly passes positional details to store state |
| **AC 2** | Filter by player, ruleConfigId, and period | API / Integration | Backend `StatisticsController` | P1 | 1. Filter by `playerId`<br>2. Filter by `ruleConfigId`<br>3. Filter by `period` (TimePeriod enum) |
| **AC 2** | Frontend filter triggers and query param propagation | Unit (Vitest) | `useStatsStore.ts` | P1 | 1. Setting filters updates parameters and triggers re-fetch |
| **AC 3** | Pagination and minMatches threshold | API / Integration | Backend `StatisticsController` | P1 | 1. `page` and `size` parameters return paginated slice<br>2. Pairs with matches < `minMatches` are filtered out |
| **AC 3** | Empty result when below threshold or no data | API / Unit | Backend & Frontend | P2 | 1. Returns empty page when threshold excludes all pairs |

## TDD Red Phase (Current)

✅ **Red-phase test scaffolds generated and verified:**
- **Backend API Tests:** `src/test/java/com/tictactore/controller/StatisticsControllerATDDTest.java` (4 test cases, `@Disabled` red-phase scaffolds)
- **Frontend E2E Tests:** `frontend/e2e/team-pair-statistics.spec.ts` (3 test cases, `test.skip()` red-phase scaffolds)
- **Frontend Unit Tests:** `frontend/tests/unit/teamPairStats.spec.ts` (2 test cases)

## Next Steps (Task-by-Task Activation)

During implementation of each task in Story 4.4:
1. Enable / unskip tests corresponding to the task being implemented:
   - Task 1 & 2: Enable tests in `StatisticsControllerATDDTest.java`
   - Task 3: Run `teamPairStats.spec.ts` with `npm test`
   - Task 4: Unskip E2E tests in `frontend/e2e/team-pair-statistics.spec.ts`
2. Run tests to verify they transition from RED to GREEN.
3. Commit passing tests alongside implementation.



