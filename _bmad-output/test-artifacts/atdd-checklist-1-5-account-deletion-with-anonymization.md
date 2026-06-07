---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-06-06T13:28:19+02:00'
storyId: '1.5'
storyKey: '1-5-account-deletion-with-anonymization'
storyFile: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/implementation-artifacts/1-5-account-deletion-with-anonymization.md'
atddChecklistPath: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-1-5-account-deletion-with-anonymization.md'
generatedTestFiles:
  - '/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/UserServiceTest.java'
  - '/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/controller/UserControllerTest.java'
  - '/Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/account-deletion.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/1-5-account-deletion-with-anonymization.md'
  - '_bmad/tea/config.yaml'
  - '_bmad/tea/resources/knowledge/data-factories.md'
  - '_bmad/tea/resources/knowledge/component-tdd.md'
  - '_bmad/tea/resources/knowledge/test-quality.md'
  - '_bmad/tea/resources/knowledge/test-healing-patterns.md'
  - '_bmad/tea/resources/knowledge/risk-governance.md'
  - '_bmad/tea/resources/knowledge/probability-impact.md'
  - '_bmad/tea/resources/knowledge/test-levels-framework.md'
  - '_bmad/tea/resources/knowledge/test-priorities-matrix.md'
  - '_bmad/tea/resources/knowledge/selector-resilience.md'
  - '_bmad/tea/resources/knowledge/timing-debugging.md'
  - '_bmad/tea/resources/knowledge/overview.md'
  - '_bmad/tea/resources/knowledge/api-request.md'
  - '_bmad/tea/resources/knowledge/network-recorder.md'
  - '_bmad/tea/resources/knowledge/auth-session.md'
  - '_bmad/tea/resources/knowledge/intercept-network-call.md'
  - '_bmad/tea/resources/knowledge/recurse.md'
  - '_bmad/tea/resources/knowledge/log.md'
  - '_bmad/tea/resources/knowledge/file-utils.md'
  - '_bmad/tea/resources/knowledge/network-error-monitor.md'
  - '_bmad/tea/resources/knowledge/fixtures-composition.md'
  - '_bmad/tea/resources/knowledge/playwright-cli.md'
  - '_bmad/tea/resources/knowledge/ci-burn-in.md'
---

# ATDD Preflight & Context Loading
- **Story**: 1.5 - Account Deletion with Anonymization
- **Stack**: fullstack (Spring Boot Backend, Vue 3 Frontend)
- **Framework Config**: Playwright config `playwright.config.ts`, JUnit 5 with Spring Boot test context.
- **TEA Config**: `tea_use_playwright_utils`: true, `tea_browser_automation`: auto, `tea_pact_mcp`: none, `tea_use_pactjs_utils`: false, `test_stack_type`: auto
- **Key Constraints**:
  - **GDPR Anonymization (AD-04)**: Do NOT `DELETE` the user record. Anonymize the row using: `id` (DO NOT MODIFY), `email` (`deleted-<UUID>@tic-tac-tore.invalid`), `nickname` (`ex-player-<UUID>`), `avatar` (`"anonymous"`), `providerId` (`null`), `language` (`null`), `lastNicknameUpdate` (`null`).
  - **Token Revocation (AD-03)**: Active JWT added to Redis denylist via `TokenRevocationService.revoke(String token)`. Extract token from Authorization header and strip "Bearer " prefix in `UserController`.
  - **Endpoint**: `DELETE /me` returning `204 No Content` in `ProfileApi` and `UserController`.
  - **Transaction Boundaries**: Redis token revocation MUST occur after the database transaction successfully commits.
  - **Frontend State**: "Delete Account" button and confirmation modal in `Cabinet.vue`. Clear Pinia auth store, storage, cookies, and redirect to `/` on success.
  - **Testing Standards**: All tests must follow AAA (Arrange-Act-Assert) pattern separated by a single blank line, with absolutely zero structural comments.

**Knowledge Fragments Loaded:**
- Core: `data-factories`, `component-tdd`, `test-quality`, `test-healing-patterns`, `risk-governance`, `probability-impact`, `test-levels-framework`, `test-priorities-matrix`
- Fullstack: `selector-resilience`, `timing-debugging`, `fixture-architecture`, `network-first`, `ci-burn-in`
- Playwright Utils: `overview`, `api-request`, `network-recorder`, `auth-session`, `intercept-network-call`, `recurse`, `log`, `file-utils`, `network-error-monitor`, `fixtures-composition`
- Playwright CLI: `playwright-cli`

# Step 2: Generation Mode
- **Mode Chosen**: AI Generation
- **Reason**: The acceptance criteria for the account deletion (button/modal click, store cleanup, redirect) and API endpoint (DELETE /me, token revocation) are clear and follow standard fullstack patterns. Live browser recording is not needed.

# Step 3: Test Strategy

## Test Levels & Priority Mapping

### 1. Unit Tests (Backend)
**File**: `src/test/java/com/tictactore/service/UserServiceTest.java`
- **[P0]** `deleteAccount_shouldAnonymizeUserData`: Verify that calling `deleteAccount` correctly anonymizes user data. (PK preserved, other fields mapped to `deleted-...`, `ex-player-...`, `"anonymous"`, `null`).
- **[P1]** `deleteAccount_shouldThrowException_whenUserNotFound`: Verify that ResourceNotFoundException is thrown when attempting to delete a non-existent user.

### 2. API Integration Tests (Backend)
**File**: `src/test/java/com/tictactore/controller/UserControllerTest.java`
- **[P0]** `deleteAccount_shouldReturn204AndRevokeToken_whenAuthenticated`: Verify that `DELETE /me` with a valid JWT token deletes the user profile and revokes the token. Check that the token revocation occurs after the DB transaction commits.
- **[P1]** `deleteAccount_shouldReturn401_whenUnauthenticated`: Verify that `DELETE /me` returns 401 Unauthorized when no token is provided.

### 3. End-to-End Tests (Frontend + Backend)
**File**: `frontend/e2e/account-deletion.spec.ts`
- **[P0]** `Account deletion flow with anonymization`: User logs in, visits the Personal Cabinet, clicks "Delete Account", confirms the deletion in the modal. Verify redirect to `/`, clear state, active token revocation, and subsequent redirect to login on trying to access a protected page.

## Red Phase Confirmation
All tests are written with `@Disabled` or `test.skip()`. Since the `DELETE /me` API endpoint, the service `deleteAccount` logic, and the Cabinet deletion modal do not exist yet, all tests are designed to fail when run without mock/implementation, confirming they are in the TDD Red Phase.

# Step 4C: Aggregation

- **TDD Validation**: ✅ All tests have `@Disabled` or `test.skip()` and assert expected behavior.
- **Unit & API Tests generated**: `src/test/java/com/tictactore/service/UserServiceTest.java` (appended) and `src/test/java/com/tictactore/controller/UserControllerTest.java` (appended).
- **E2E Tests generated**: `frontend/e2e/account-deletion.spec.ts` (created).
- **Fixtures created**: `N/A` (no external test data fixtures required for red phase).

## Next Steps (Task-by-Task Activation)
During implementation of each task:
1. Remove `@Disabled("ATDD Red Phase")` or `test.skip()` from the current test file or scenario.
2. Run tests.
3. Verify the activated test fails first (red phase), then passes after implementation (green phase).
4. Commit passing tests.

# Step 5: Validate & Complete

## Validation Verification
- **Prerequisites Satisfied**: ✅ Story AC is testable, frameworks configured.
- **Test Files Created/Appended**: ✅ 
  - `src/test/java/com/tictactore/service/UserServiceTest.java` (appended)
  - `src/test/java/com/tictactore/controller/UserControllerTest.java` (appended)
  - `frontend/e2e/account-deletion.spec.ts` (created)
- **TDD Red Phase Scaffolds**: ✅ Verified all tests use `@Disabled("ATDD Red Phase")` or `test.skip()`.
- **Story Integration**: ✅ ATDD Artifacts links injected under Dev Notes in story file.

## Completion Summary
- **Story Key / ID**: `1-5-account-deletion-with-anonymization` / `1.5`
- **Total Tests Generated**: 5
  - Unit: 2 (UserServiceTest)
  - Integration: 2 (UserControllerTest)
  - E2E: 1 (account-deletion.spec.ts)
- **Next Recommended Workflow**: `dev-story` (BMM developer execution)
