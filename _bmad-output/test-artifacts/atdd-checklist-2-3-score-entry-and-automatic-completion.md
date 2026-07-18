---
storyId: '2.3'
storyKey: '2-3-score-entry-and-automatic-completion'
storyFile: './_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md'
atddChecklistPath: '{project-root}/_bmad-output/test-artifacts/atdd-checklist-2-3-score-entry-and-automatic-completion.md'
generatedTestFiles: []
inputDocuments:
  - './_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md'
  - 'knowledge core fragments'
  - 'knowledge frontend fragments'
  - 'knowledge backend fragments'
  - 'knowledge playwright utils'
  - 'knowledge playwright cli'
stepsCompleted:
  - 'step-01-preflight-and-context'
  - 'step-02-generation-mode'
  - 'step-03-test-strategy'
  - 'step-04c-aggregate'
  - 'step-05-validate-and-complete'
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-07-17T22:40:28+02:00'
storyId: '2.3'
storyKey: '2-3-score-entry-and-automatic-completion'
storyFile: './_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md'
generatedTestFiles:
  - 'frontend/e2e/score-entry-and-automatic-completion.spec.ts'
---

# ATDD Checklist: 2.3 Score Entry & Automatic Completion

## Step 1: Preflight & Context Loading
- **Detected Stack**: fullstack
- **Prerequisites**: Met (Playwright and Vitest configured, development environment available, story loaded).
- **Story Context**: Loaded `2-3-score-entry-and-automatic-completion` from `./_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md`
- **Knowledge Base**: Loaded core, frontend, backend, and Playwright Utils (Full UI+API profile) knowledge fragments based on TEA config.

## Step 2: Generation Mode Selection
- **Chosen Mode**: AI Generation.
- **Reason**: The acceptance criteria for the score steppers and auto-completion logic are clear and the task does not require complex UI recording in a live browser to generate tests.

## Step 3: Test Strategy
- **E2E Tests** (Priority: P0-P2)
  - [P2] Verify stepper UI lacks 1px borders (class/style check).
  - [P1] Verify `+5` stepper is hidden when score limit `< 5`.
  - [P0] Verify game automatically completes when score reaches limit.
  - [P0] Verify match automatically advances to submission when win conditions are met.
- **Red Phase**: Tests will be scaffolded with `test.skip()` or fail immediately due to missing UI elements.

## Step 4: Red-Phase Test Generation & Aggregation (TDD RED PHASE)
- **E2E Tests Generated**: `frontend/e2e/score-entry-and-automatic-completion.spec.ts` (4 skipped tests)
- **API Tests**: None required for this UI flow
- **Fixtures**: Using `test-data.ts` and in-test API mocking

## Acceptance Criteria Coverage
- [x] Given a match draft in progress, when the score entry view opens, then the score steppers are presented using background shifts (no 1px borders). (E2E)
- [x] Given the active RuleConfiguration has a scoreLimit < 5, when the view opens, then the +5 stepper is hidden. (E2E)
- [x] Given score entry, when a player's score reaches the scoreLimit, then the game automatically completes. (E2E)
- [x] Given game completion, when the overall match winsNeeded are met, then the match automatically advances to the submission state. (E2E)

## Next Steps (Task-by-Task Activation)
During implementation of each task:
1. Remove `test.skip()` from the current test file or scenario
2. Run tests: `npm test` or `npx playwright test`
3. Verify the activated test fails first, then passes after implementation (green phase)
4. If any activated tests still fail unexpectedly:
   - Either fix implementation (feature bug)
   - Or fix test (test bug)
5. Commit passing tests

## Step 5: Validate & Complete
- **Validation**: All tests use `test.skip()` and have expected UI behavior assertions.
- **Handoff Path**: `_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md`
- **Next Recommended Workflow**: `bmad-dev-story` (to implement the features)



