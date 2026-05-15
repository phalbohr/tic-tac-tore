---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-05-15'
storyId: '1.3'
storyKey: '1-3-automatic-profile-generation-and-first-entry'
storyFile: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/implementation-artifacts/1-3-automatic-profile-generation-and-first-entry.md'
atddChecklistPath: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-1-3-automatic-profile-generation-and-first-entry.md'
generatedTestFiles: ['src/test/java/com/tictactore/service/UserServiceTest.java', 'frontend/e2e/profile-generation.spec.ts']
inputDocuments:
  - '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/implementation-artifacts/1-3-automatic-profile-generation-and-first-entry.md'
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
# Step 1 Output
Successfully detected `fullstack` environment.
Story context loaded for `1-3-automatic-profile-generation-and-first-entry`.
Prerequisites met and knowledge base fragments indexed.


# Step 2 Output
Mode chosen: AI Generation.
Reason: Acceptance criteria are clear and describe standard backend profile generation and straightforward frontend verification. Since this is a red-phase ATDD generation for a not-yet-implemented UI flow, live browser recording is unnecessary at this stage.

# Step 3 Output: Test Strategy

## Acceptance Criteria Mapping & Prioritization

**Backend Unit Tests (`UserServiceTest.java`)**
- **Scenario 1 (P0):** Nickname generation from email prefix. Strips non-alphanumeric characters (e.g., `john.doe@gmail.com` -> `johndoe`).
- **Scenario 2 (P0):** Nickname collision resolution. When `existsByNickname` returns `true`, appends a 4-digit random number. Handles up to 10 iterations safely.
- **Scenario 3 (P0):** Avatar generation. Generates Dicebear identicon URL using SHA-256 hash of the email.
- **Scenario 4 (P0):** Returning users protection. Ensure existing users' customized nicknames/avatars are NOT overwritten on subsequent logins.
- **Scenario 5 (P1):** PII exclusion. Verifies that the user's real name from OAuth provider is ignored and not mapped to the `User` entity.

**Frontend Component Tests (`HomeHub.spec.ts` or E2E)**
- **Scenario 6 (P0):** First login profile display. Verifies that after a successful authentication, the newly generated nickname and avatar are fetched and immediately displayed in the `HomeHub` component without a manual page reload.

## Red Phase Validation
All tests will be designed to fail initially (Red Phase). Backend unit tests will fail because `UserService.findOrCreate` currently doesn't implement prefix extraction or SHA-256 avatar generation. Frontend tests will fail because the `HomeHub` does not yet have the reactive state logic to display these exact fetched profiles upon first entry.

# Step 4C Output: ATDD Checklist

## TDD Red Phase (Current)
✅ Red-phase test scaffolds generated
- Backend Tests: 5 tests (all skipped with @Disabled)
- E2E Tests: 1 tests (all skipped with test.skip)

## Acceptance Criteria Coverage
- Nickname generated from email prefix
- Nickname uniqueness guaranteed
- Deterministic default placeholder avatar
- No PII extracted or stored
- Given first-time authentication, profile is created and displayed automatically

## Next Steps (Task-by-Task Activation)
During implementation of each task:
1. Remove `@Disabled` or `test.skip()` from the current test file or scenario
2. Run tests (e.g. `mvn test` or `npm run test:e2e`)
3. Verify the activated test fails first, then passes after implementation (green phase)
4. If any activated tests still fail unexpectedly, fix implementation or fix test.
5. Commit passing tests

# Step 5 Output: Validation & Completion
✅ Output validation passed. No duplications found. All tests correctly formatted as red-phase scaffolds with `@Disabled` and `test.skip()`.
✅ Story file updated with handoff paths.
✅ Checklist formatted.

## Completion Summary
- **Test files created:** 
  - `src/test/java/com/tictactore/service/UserServiceTest.java`
  - `frontend/e2e/profile-generation.spec.ts`
- **Checklist output path:** `_bmad-output/test-artifacts/atdd-checklist-1-3-automatic-profile-generation-and-first-entry.md`
- **Story handoff path:** `_bmad-output/implementation-artifacts/1-3-automatic-profile-generation-and-first-entry.md`
- **Key Risks:** OAuth2 E2E authentication mocking needs reliable fixtures; deterministic avatar verification assumes Dicebear v7 API URL structure won't change.
- **Next Workflow:** `dev-story` (to begin implementation and turn tests green).
