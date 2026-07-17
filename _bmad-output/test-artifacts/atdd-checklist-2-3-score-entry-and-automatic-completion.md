---
storyId: 2.3
storyKey: 2-3-score-entry-and-automatic-completion
storyFile: ./_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md
atddChecklistPath: _bmad-output/test-artifacts/atdd-checklist-2-3-score-entry-and-automatic-completion.md
generatedTestFiles:
  - frontend/e2e/score-entry-and-automatic-completion.spec.ts
inputDocuments:
  - ./_bmad/tea/config.yaml
  - ./resources/tea-index.csv
  - ./_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md
stepsCompleted:
  - step-01-preflight-and-context
  - step-02-generation-mode
  - step-03-test-strategy
  - step-04-generate-tests
  - step-04c-aggregate
lastStep: step-04c-aggregate
lastSaved: '2026-07-17T20:13:45+02:00'
---

# ATDD Checklist: 2.3 Score Entry & Automatic Completion

## Step 1: Preflight & Context Loading
- **Detected Stack**: fullstack
- **Prerequisites**: Met. (Playwright config present, dev env available, story approved with ACs).
- **Story Context**: Loaded `2-3-score-entry-and-automatic-completion`
- **Knowledge Base**: Core and UI/Fullstack patterns logically loaded.

## Step 2: Generation Mode Selection
- **Chosen Mode**: AI Generation.
- **Reason**: The acceptance criteria for the score steppers and auto-completion logic are clear and the task does not require complex UI recording in a live browser to generate unit and component tests. 

## Step 3: Test Strategy
- **E2E Tests** (Priority: P0)
  - Verify stepper UI lacks 1px borders (class/style check).
  - Verify `+5` stepper is hidden when score limit `< 5`.
  - Verify game automatically completes when score reaches limit.
  - Verify match automatically advances to submission when win conditions are met.

## Step 4: Red-Phase Test Generation & Aggregation
- **Generated**: `frontend/e2e/score-entry-and-automatic-completion.spec.ts`
- All tests are scaffolded with `test.skip()` representing the TDD Red Phase.

### Implementation Checklist
- [ ] Implement `ScoreStepper.vue` without borders/dividers.
- [ ] Implement `ScoreEntry.vue` combining the steppers and past games context.
- [ ] Connect `ScoreEntry.vue` into the `NewMatchFlow.vue` sequence.
- [ ] Implement logic in `matchDraftStore.ts` to track score.
- [ ] Add reactive watcher/logic to `matchDraftStore.ts` to trigger game auto-completion.
- [ ] Add logic in `matchDraftStore.ts` to detect match win conditions and advance flow.
- [ ] Run `vitest` unit tests for store logic validation.
- [ ] Un-skip and run Playwright E2E tests for `score-entry-and-automatic-completion.spec.ts` and verify they pass.
