---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-05-25T10:14:00Z'
storyId: '1.4'
storyKey: '1-4-profile-management-in-personal-cabinet'
storyFile: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/implementation-artifacts/1-4-profile-management-in-personal-cabinet.md'
atddChecklistPath: '/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-1-4-profile-management-in-personal-cabinet.md'
generatedTestFiles: 
  - '/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/controller/UserControllerTest.java'
  - '/Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/profile-management.spec.ts'
  - '/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/UserServiceTest.java'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/1-4-profile-management-in-personal-cabinet.md'
  - '.agents/skills/bmad-testarch-atdd/resources/tea-index.csv'
---

# ATDD Preflight & Context Loading
- **Story**: 1.4 - Profile Management in Personal Cabinet
- **Stack**: fullstack (Spring Boot Backend, Vue 3 Frontend)
- **Framework Config**: Playwright config `playwright.config.ts`, JUnit 5 with Spring Boot test context.
- **TEA Config**: `tea_use_playwright_utils`: true, `tea_browser_automation`: auto
- **Key Constraints**: 
  - 30-day nickname cooldown logic requires mock `Clock`.
  - Optimistic UI updates <50ms without complete page reload.
  - Strict AAA pattern with blank line separation and no structural comments.
  - Do not bypass backend with complete API mocking in E2E tests for this flow.

**Knowledge Fragments Loaded:**
- Core: `data-factories`, `component-tdd`, `test-quality`, `test-healing-patterns`
- Fullstack: `selector-resilience`, `timing-debugging`, `fixture-architecture`, `network-first`, `test-levels-framework`, `test-priorities-matrix`, `ci-burn-in`
- Playwright Utils: `overview`, `api-request`, `network-recorder`, `auth-session`, `intercept-network-call`, `recurse`, `log`, `file-utils`, `network-error-monitor`, `fixtures-composition`

# Step 2: Generation Mode
- **Mode Chosen**: AI Generation
- **Reason**: The acceptance criteria for the profile management form (nickname and language updates) are clear, and the interactions (inputs, selects, buttons) are standard UI patterns. Live browser recording is unnecessary at this stage.

# Step 3: Test Strategy

## Test Levels & Priority Mapping

### 1. Unit Tests (Backend)
**File**: `src/test/java/com/tictactore/service/UserServiceTest.java`
- **[P0]** `updateProfile_shouldUpdateNickname_whenCooldownPassed`: Validates the 30-day cooldown logic using a mock `Clock`.
- **[P0]** `updateProfile_shouldThrowException_whenCooldownNotPassed`: Validates rejection if updated less than 30 days ago.
- **[P0]** `updateProfile_shouldSanitizeNickname`: Validates that nickname sanitization logic works on update.
- **[P1]** `updateProfile_shouldThrowException_whenNicknameNotUnique`: Validates handling of uniqueness constraints (`DataIntegrityViolationException`).

### 2. API Integration Tests (Backend)
**File**: `src/test/java/com/tictactore/controller/UserControllerTest.java`
- **[P1]** `PATCH /me` - `shouldUpdateLanguageAndNickname`: Returns 200 OK and updated DTO.
- **[P1]** `PATCH /me` - `shouldReturn400_whenCooldownNotPassed`: Validates HTTP 400 when business logic rejects nickname update.

### 3. End-to-End Tests (Frontend + Backend)
**File**: `frontend/e2e/profile-management.spec.ts`
- **[P1]** `Language change applies optimistic UI update`: User navigates to Cabinet, changes language to DE, interface instantly updates (<50ms). API is called for real (no complete mock).
- **[P1]** `Nickname 30-day cooldown enforcement`: User changes nickname (success). User attempts to change again immediately -> UI shows error message/blocks update.

## Red Phase Confirmation
All these tests are designed to execute against the current codebase where `last_nickname_update`, `language`, the `PATCH /me` endpoint, and the `Cabinet.vue` page **do not yet exist**. Thus, they will strictly fail during the red-phase execution.

# Step 4C: Aggregation
- **TDD Validation**: ✅ All tests have `@Disabled` or `test.skip()` and assert expected behavior.
- **Unit & API Tests generated**: `src/test/java/com/tictactore/service/UserServiceTest.java` (appended) and `src/test/java/com/tictactore/controller/UserControllerTest.java`.
- **E2E Tests generated**: `frontend/e2e/profile-management.spec.ts`.

## Next Steps (Task-by-Task Activation)
During implementation of each task:
1. Remove `test.skip()` / `@Disabled` from the current test file or scenario.
2. Run tests.
3. Verify the activated test fails first, then passes after implementation (green phase).
4. Commit passing tests.

# Step 5: Validate & Complete
- **Validation**:
  - ✅ All tests correctly generated as RED PHASE scaffolds with `test.skip()` or `@Disabled`.
  - ✅ Acceptance criteria comprehensively covered (E2E + Unit + API).
  - ✅ File lists explicitly tracked in frontmatter.
- **Workflow State**: Complete.
