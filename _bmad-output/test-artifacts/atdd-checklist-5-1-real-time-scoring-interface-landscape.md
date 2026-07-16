---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-07-04T22:11:30+02:00'
storyId: '5.1'
storyKey: '5-1-real-time-scoring-interface-landscape'
storyFile: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/implementation-artifacts/5-1-real-time-scoring-interface-landscape.md'
atddChecklistPath: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-5-1-real-time-scoring-interface-landscape.md'
generatedTestFiles:
  - 'frontend/tests/e2e/real-time-scoring-interface.spec.ts'
inputDocuments:
  - 'config.yaml'
  - '5-1-real-time-scoring-interface-landscape.md'
  - 'playwright.config.ts'
  - 'data-factories.md'
  - 'component-tdd.md'
  - 'test-quality.md'
  - 'test-healing-patterns.md'
  - 'selector-resilience.md'
  - 'timing-debugging.md'
  - 'test-levels-framework.md'
  - 'test-priorities-matrix.md'
  - 'ci-burn-in.md'
  - 'overview.md'
  - 'api-request.md'
  - 'network-recorder.md'
  - 'auth-session.md'
  - 'intercept-network-call.md'
  - 'recurse.md'
  - 'log.md'
  - 'file-utils.md'
  - 'network-error-monitor.md'
  - 'fixtures-composition.md'
  - 'playwright-cli.md'
---

# ATDD Checklist: 5-1-real-time-scoring-interface-landscape

## TDD Red Phase (Current)

✅ Red-phase test scaffolds generated

- API Tests: 0 tests (all skipped)
- E2E Tests: 2 tests (all skipped)

## Acceptance Criteria Coverage

- User clicks "Start Match", triggering requestFullscreen and screen.orientation.lock('landscape')
- Physical tap on the top-left area of the browser viewport correctly attributes a goal in the application's timeline

## Next Steps (Task-by-Task Activation)

During implementation of each task:

1. Remove `test.skip()` from the current test file or scenario
2. Run tests: `npm test` or `npx playwright test`
3. Verify the activated test fails first, then passes after implementation (green phase)
4. If any activated tests still fail unexpectedly:
   - Either fix implementation (feature bug)
   - Or fix test (test bug)
5. Commit passing tests

## Implementation Guidance

Feature endpoints to implement:
- (None - frontend only)

UI components to implement:
- LiveMatch.vue (fullscreen + orientation lock)
- LiveQuadrant components (interactive tap areas mapping to players)
