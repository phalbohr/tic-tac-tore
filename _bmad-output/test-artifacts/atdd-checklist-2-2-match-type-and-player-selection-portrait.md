---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-07-04'
storyId: '2.2'
storyKey: '2-2-match-type-and-player-selection-portrait'
storyFile: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/implementation-artifacts/2-2-match-type-and-player-selection-portrait.md'
atddChecklistPath: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-2-2-match-type-and-player-selection-portrait.md'
generatedTestFiles: 
  - '/Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/api/new-match.spec.ts'
  - '/Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/e2e/new-match-creation.spec.ts'
inputDocuments: 
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/overview.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/api-request.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/network-recorder.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/auth-session.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/intercept-network-call.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/recurse.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/log.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/file-utils.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/network-error-monitor.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/fixtures-composition.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/playwright-cli.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '/Users/ppolukhin/Projects/tic-tac-tore/.agents/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# ATDD Checklist: Match Type and Player Selection (Portrait)

## TDD Red Phase (Current)

✅ Red-phase test scaffolds generated

- API Tests: 2 tests (all skipped)
- E2E Tests: 2 tests (all skipped)

## Acceptance Criteria Coverage

- Fetch frequent opponents API - Covered in API test (P1)
- Fetch last used rule system API - Covered in API test (P1)
- Given New Match tapped on Home Hub, When creation screen opens in portrait (UX-DR1) - Covered in E2E test (P0)
- Then 2 or 4 slots available - Covered in E2E test (P0)
- And UI follows No-Line rule (UX-DR3) - Covered in E2E test (P2)

## Next Steps (Task-by-Task Activation)

During implementation of each task:

1. Remove `test.skip()` from the current test file or scenario
2. Run tests: `npm run e2e` (or vitest)
3. Verify the activated test fails first, then passes after implementation (green phase)
4. If any activated tests still fail unexpectedly:
   - Either fix implementation (feature bug)
   - Or fix test (test bug)
5. Commit passing tests

## Implementation Guidance

Feature endpoints to implement:
- GET `/api/users/me/frequent-opponents`
- GET `/api/users/me/preferences/last-rule-system`

UI components to implement:
- Home Hub navigation to New Match
- Match Type & Player Selection Form (Portrait)
- 2-Player and 4-Player dynamic slots
