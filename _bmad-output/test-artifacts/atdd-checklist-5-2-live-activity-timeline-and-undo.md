---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-20'
storyId: '5.2'
storyKey: '5-2-live-activity-timeline-and-undo'
storyFile: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/implementation-artifacts/5-2-live-activity-timeline-and-undo.md'
atddChecklistPath: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-5-2-live-activity-timeline-and-undo.md'
generatedTestFiles:
  - 'frontend/tests/unit/liveMatch.spec.ts'
  - 'frontend/tests/unit/LiveActivityTimeline.spec.ts'
  - 'frontend/e2e/real-time-scoring-interface.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/5-2-live-activity-timeline-and-undo.md'
  - '_bmad/tea/config.yaml'
  - 'frontend/playwright.config.ts'
  - 'frontend/vitest.config.ts'
---

# Step 1 Output: Preflight & Context Loading
- Detected stack: `frontend` / `fullstack` (Targeting Vue 3, Pinia, Vitest & Playwright)
- Story context loaded: `5-2-live-activity-timeline-and-undo.md`
- Acceptance criteria extracted: Live activity timeline (FR10) and Undo last goal (FR9)
- Test frameworks verified: Vitest and Playwright configured and operational

# Step 2 Output: Generation Mode Selection
- Selected Mode: `AI Generation`
- Rationale: Standard UI component rendering, state manipulation in Pinia, and user interaction flows with deterministic expected behavior.

# Step 3 Output: Test Strategy & Acceptance Criteria Mapping

## Test Strategy & Scenarios

### 1. Store Unit Tests (`frontend/tests/unit/liveMatch.spec.ts`)
- **[P0]** `canUndo` is `false` initially when no goals are recorded.
- **[P0]** `recordGoal` sets `canUndo` to `true` and enriches `goalTimeline` with player names resolved from team rosters.
- **[P0]** `undoLastGoal` removes the most recent goal (LIFO) and sets `canUndo` back to `false` when empty.
- **[P1]** `undoLastGoal` safely handles empty goal lists without throwing errors.
- **[P1]** Player name lookup helper resolves IDs across both Team A and Team B rosters.

### 2. Component Unit Tests (`frontend/tests/unit/LiveActivityTimeline.spec.ts`)
- **[P0]** Displays empty state message (`data-testid="timeline-empty"`) when no goals are recorded.
- **[P0]** Renders goal entries (`data-testid="timeline-goal-item"`) in reverse chronological order (newest goal at top) with scorer name and quadrant role.
- **[P1]** Layout constraints applied (`max-h-36`, `overflow-y-auto`) to ensure HUD layer fits landscape viewport.

### 3. E2E Acceptance Tests (`frontend/e2e/real-time-scoring-interface.spec.ts`)
- **[P0]** "Undo Last Goal" button is disabled initially before any goals are recorded.
- **[P0]** Recording a goal displays scorer name and role in live timeline and enables the undo button.
- **[P0]** Recording multiple goals displays them in reverse chronological order, and clicking "Undo Last Goal" sequentially removes the newest goals and disables undo when empty.

## Red Phase Validation
All newly created test cases are scaffolded with `it.skip` / `test.skip` and contain concrete assertions against the target component structure and store API. When unskipped during story implementation, they will fail until the features in `LiveMatch.vue`, `LiveActivityTimeline.vue`, and `liveMatch.ts` are implemented.

# Step 4C Output: Aggregation & Infrastructure
- Unit Test Files:
  - `frontend/tests/unit/liveMatch.spec.ts` (5 tests scaffolded)
  - `frontend/tests/unit/LiveActivityTimeline.spec.ts` (3 tests scaffolded)
- E2E Test Files:
  - `frontend/e2e/real-time-scoring-interface.spec.ts` (3 E2E test scenarios added)
- Scaffolding compliance verified: All test cases use `it.skip` / `test.skip` to prevent CI breakages while preserving full test intent.

# Step 5 Output: Validation & Completion
- Verification: Unit test suite execution verified (Vitest parsed test files cleanly with 8 skipped tests).
- Handoff linkage: Story file updated with ATDD checklist and test paths under Dev Notes.

## Next Steps (Task-by-Task Activation)
1. In Task 1: Unskip tests in `frontend/tests/unit/liveMatch.spec.ts` and implement store actions (`undoLastGoal`, `canUndo`, `goalTimeline`).
2. In Task 2: Unskip tests in `frontend/tests/unit/LiveActivityTimeline.spec.ts` and implement `LiveActivityTimeline.vue`.
3. In Task 3 & 5: Unskip tests in `frontend/e2e/real-time-scoring-interface.spec.ts` and integrate timeline and undo HUD into `LiveMatch.vue`.
