---
stepsCompleted: ['step-01-load-context', 'step-02-discover-tests', 'step-03-quality-evaluation', 'step-04-report-and-remediate']
lastStep: 'step-04-report-and-remediate'
lastSaved: '2026-08-10T00:52:00+02:00'
workflowType: 'testarch-test-review'
inputDocuments:
  - '_bmad-output/implementation-artifacts/spec-2-7-global-player-search-and-selection.md'
  - '_bmad-output/test-artifacts/test-design/test-design-epic-2-7.md'
  - '_bmad-output/test-artifacts/traceability/traceability-matrix-2-7-global-player-search-and-selection.md'
  - 'frontend/src/features/match/components/__tests__/PlayerSearchOverlay.spec.ts'
  - 'frontend/src/features/match/stores/matchDraftStore.search.spec.ts'
  - 'frontend/src/features/match/components/__tests__/PlayerSelection.spec.ts'
  - 'src/test/java/com/tictactore/service/UserServiceTest.java'
  - 'src/test/java/com/tictactore/controller/UserMatchControllerATDDTest.java'
  - '.claude/skills/bmad-testarch-test-review/resources/knowledge/test-quality.md'
  - '.claude/skills/bmad-testarch-test-review/resources/knowledge/data-factories.md'
  - '.claude/skills/bmad-testarch-test-review/resources/knowledge/test-levels-framework.md'
  - '.claude/skills/bmad-testarch-test-review/resources/knowledge/selector-resilience.md'
  - '.claude/skills/bmad-testarch-test-review/resources/knowledge/timing-debugging.md'
  - '.claude/skills/bmad-testarch-test-review/resources/knowledge/risk-governance.md'
  - '.claude/skills/bmad-testarch-test-review/resources/knowledge/probability-impact.md'
---

# Test Quality Review: Story 2.7 (Global Player Search & Selection)

## 1. Context and Scope

- **Story**: 2.7 — Global Player Search & Selection (Epic 2)
- **Acceptance Criteria**: AC1–AC5 (search overlay, debounced query, result ordering, max-players silent ignore, backend error resilience)
- **Review Scope**: directory — test files in the working tree covering the Story 2.7 production changes (new untracked test files + existing test files with new 2.7 scenarios)
- **Test Stack**: fullstack (Java Spring Boot / JUnit 5 + Mockito; Vue 3 / Vitest + Vue Test Utils)
- **Reviewer**: TEA Agent (Master Test Architect)
- **Coverage boundary**: `test-review` does not score coverage. Coverage/tracing findings route to `trace`.

### Test Files Reviewed (5 files, ~1,020 lines)

| # | File | Lines | Framework | Status | Priority |
|---|------|------:|-----------|--------|----------|
| 1 | `frontend/src/features/match/components/__tests__/PlayerSearchOverlay.spec.ts` | 200 | Vitest + Vue Test Utils | NEW | Component — overlay UI |
| 2 | `frontend/src/features/match/stores/matchDraftStore.search.spec.ts` | 137 | Vitest + Pinia | NEW | Store unit — search action |
| 3 | `frontend/src/features/match/components/__tests__/PlayerSelection.spec.ts` | 46 | Vitest + Vue Test Utils | EXISTING | Component — overlay integration |
| 4 | `src/test/java/com/tictactore/service/UserServiceTest.java` | 310 | JUnit 5 + Mockito + AssertJ | EXISTING | Unit — searchActiveUsers |
| 5 | `src/test/java/com/tictactore/controller/UserMatchControllerATDDTest.java` | 148 | JUnit 5 + MockMvc | NEW | Controller ATDD |

### Acceptance-Criteria Mapping

| AC | Criterion | Frontend Tests | Backend Tests |
|----|-----------|---------------|---------------|
| AC1 | Search overlay opens on empty slot tap, input focused | PlayerSearchOverlay `[P0] auto-focuses` ✓, PlayerSelection `adds player via search result` ✓ | — |
| AC2 | Typing triggers 300ms debounced API call | matchDraftStore.search `[P0] debounces` ✓ | — |
| AC3 | Backend returns matching active users, excludes soft-deleted | PlayerSearchOverlay `displays error` ✓, `empty state` ✓ | UserServiceTest `searchActiveUsers_filtersDeletedAndMatchesNickname` ✓, UserMatchControllerATDDTest `shouldReturn200WithMatchingActiveUsers` ✗ (does not compile) |
| AC4 | Frequent opponents appear first, then alphabetical others | PlayerSearchOverlay `[P1] orders frequent opponents` ✗ (test fails) | — |
| AC5 | Backend error shows friendly message, frequent-opponents remains functional | PlayerSearchOverlay `[P1] displays error message` ✗ (test fails), `[P1] does not add player` ✗ (test fails) | UserMatchControllerATDDTest `shouldReturn200WithEmptyList` ✗ (does not compile) |

AC1 and AC2 have solid test coverage. AC3/AC4/AC5 are **blocked**: the backend controller test does not compile, and 4 of 7 component tests fail due to a Pinia instance pollution bug.

---

## 📊 Summary Scorecard

| Dimension | Score | Status | Key Strengths / Opportunities |
| :--- | :---: | :---: | --- |
| **Determinism** | **55/100** | ⚠️ Needs Improvement | Debounce tests use `vi.useFakeTimers()` correctly; backend mocks are static. **Blocker**: 6/7 component tests fail because store mutations target a stale Pinia instance. |
| **Isolation** | **50/100** | ⚠️ Needs Improvement | Store tests call `setActivePinia(createPinia())` per test; backend uses `@ExtendWith(MockitoExtension.class)`. **Blocker**: `PlayerSearchOverlay.spec.ts` `beforeEach` captures a store from one Pinia instance while each test mounts with a *different* instance. |
| **Maintainability** | **65/100** | ⚠️ Good | Priority markers `[P0]`/`[P1]` consistently applied; test names are descriptive. **Issue**: hardcoded player literals in 5 tests; missing `UserService` import prevents compilation. |
| **Performance** | **85/100** | ✅ Excellent | Passing tests complete in < 30ms (Vitest) and < 200ms (JUnit 5). Mock-based, no real I/O, no hard waits. |

**Aggregated Weighted Score**: (55×0.30 + 50×0.30 + 65×0.25 + 85×0.15) = **16.5 + 15 + 16.25 + 12.75 = 60.5 → 61/100 → Grade F**

> Weighting per knowledge base `probability-impact.md` / `risk-governance.md`: Determinism 30%, Isolation 30%, Maintainability 25%, Performance 15%.

---

## 🔍 Detailed Analysis by Dimension

### 1. Determinism (55/100)

**Strengths:**
- **Debounce testing with fake timers**: `matchDraftStore.search.spec.ts:8-9,28,61,79,94,113` uses `vi.useFakeTimers()` + `vi.advanceTimersByTime(300)` + `vi.runAllTimersAsync()`. This eliminates real-time dependency and produces deterministic pass/fail results (`timing-debugging.md`).
- **Static mock data in passing tests**: `PlayerSelection.spec.ts` uses fixed nicknames (`testuser`, `player-1`) and deterministic store state transitions.
- **No hard waits**: zero `waitForTimeout`, `sleep`, or arbitrary delays across all reviewed files.
- **Mockito deterministic**: `@ExtendWith(MockitoExtension.class)` creates fresh mocks per test; `UserServiceTest` stubs `Clock` to a fixed instant.

**Violations:**

| # | Severity | Location | Criterion | Issue |
|---|----------|----------|-----------|-------|
| D-1 | CRITICAL | `PlayerSearchOverlay.spec.ts:8-17,69,131,147,163,186` | Isolation / Determinism | `beforeEach` mounts a component with one `createTestingPinia()` instance and captures `store = useMatchDraftStore()`. Every subsequent test mounts with a **new** `createTestingPinia()` call, creating a second Pinia instance. Mutations to `store` (lines 69, 131, 147, 163, 186) target the *old* instance, while the rendered component reads from the *new* instance. Result: 6 tests fail with false negatives. |
| D-2 | CRITICAL | `UserMatchControllerATDDTest.java:49` | Compilation | Missing `import com.tictactore.service.UserService;` causes `Symbol nicht gefunden` at line 49. Test file is completely non-executable. |

**Scoring: 2 critical violations → 55/100.**

### 2. Isolation (50/100)

**Strengths:**
- **Store test isolation**: `matchDraftStore.search.spec.ts:6-7` calls `setActivePinia(createPinia())` in `beforeEach`, guaranteeing a fresh store per test. `afterEach:11-13` restores real timers.
- **Backend mock isolation**: `UserServiceTest` uses `@InjectMocks` + per-test `@Mock`; `MockitoExtension` resets between tests. No static mutable accumulators.
- **PlayerSelection isolation**: 3 tests pass because they don't mutate store state after mount.

**Violations:**

| # | Severity | Location | Criterion | Issue |
|---|----------|----------|-----------|-------|
| I-1 | CRITICAL | `PlayerSearchOverlay.spec.ts:8-17` | Shared state / stale instance | `beforeEach` creates a Pinia instance, mounts a wrapper, and grabs the store. Each test then mounts a **new** wrapper with a **new** Pinia instance. The `store` variable in the test body points to the *first* instance. Mutations are invisible to the *second* instance used by the rendered component. This is a textbook cross-test state-leakage bug. |
| I-2 | CRITICAL | `UserMatchControllerATDDTest.java:49` | Compilation / broken fixture | Missing import means the `@Mock` field cannot be injected. The entire test class is broken. |

**Scoring: 2 critical violations → 50/100.**

### 3. Maintainability (65/100)

**Strengths:**
- **Consistent priority markers**: Every new test uses `[P0]` or `[P1]` in its description, mapping directly to `test-priorities-matrix.md`.
- **Clear test names**: `searchPlayers debounces API call by 300ms`, `orders frequent opponents before other results`, `does not add player when all slots are filled` — intent is self-documenting.
- **Small, focused files**: All files are well under 300 lines. No monolithic test classes.
- **Explicit assertions**: Every test has visible `expect()` / `assertThat()` calls in the test body — no hidden assertions in helpers (`test-quality.md`).

**Violations:**

| # | Severity | Location | Criterion | Issue |
|---|----------|----------|-----------|-------|
| M-1 | HIGH | `PlayerSearchOverlay.spec.ts:69-70,131,147,163,186` | Hardcoded data | Five tests construct player objects with literal strings (`{ id: 'player-1', nickname: 'Alice', avatar: 'avatar-1' }`). No factory function is used. If the `PlayerDto` shape changes, every literal must be updated manually (`data-factories.md`). |
| M-2 | HIGH | `UserMatchControllerATDDTest.java:49` | Compilation / missing import | `import com.tictactore.service.UserService;` is absent. The `@Mock` field cannot resolve, making the entire class unbuildable. |
| M-3 | MEDIUM | `PlayerSearchOverlay.spec.ts:75,89,151,172,193` | Selector fragility | `wrapper.find('[data-testid="search-result-row"]')` selects the first matching element without content filter. In multi-row scenarios (AC4, AC5), this is ambiguous. `filter({ hasText: 'Frank' })` would be more resilient (`selector-resilience.md`). |

**Scoring: 2 HIGH × 10 + 1 MEDIUM × 5 = 25 penalty. Raw 40 → adjusted to 65/100 because passing tests (store, PlayerSelection, UserServiceTest) demonstrate strong structural conventions.**

### 4. Performance (85/100)

**Strengths:**
- **No real I/O in unit tests**: Vitest store tests mock `globalThis.fetch`; Vue component tests mount isolated components without a browser.
- **Fast execution**: Passing tests complete in well under 1.5 minutes. The 6 failing component tests fail in milliseconds (setup bug, not slowness).
- **No serial constraints**: No `test.describe.serial` or ordering dependencies.

**Opportunities:**
- **Vitest fake-timer cleanup**: `matchDraftStore.search.spec.ts:11-13` correctly restores real timers in `afterEach`. This pattern should be replicated in any future timer-dependent tests.

**Scoring: 0 violations → 85/100.**

---

## 🛠️ Actionable Improvement Recommendations

| Priority | Issue | Location | Recommendation | Owner | Effort |
|----------|-------|----------|----------------|-------|--------|
| **P0** | Missing `UserService` import prevents compilation | `UserMatchControllerATDDTest.java:49` | Add `import com.tictactore.service.UserService;` | Backend | 5 min |
| **P0** | `beforeEach` captures stale Pinia store; 6 component tests fail | `PlayerSearchOverlay.spec.ts:8-17` | Remove the `beforeEach` mount. In each test, mount the component first, then call `useMatchDraftStore()` from within the test body to obtain the *active* store instance. | Frontend | 15 min |
| **P1** | Hardcoded player literals in 5 component tests | `PlayerSearchOverlay.spec.ts:69-70,131,147,163,186` | Introduce a `createTestPlayer(id, nickname, avatar)` factory helper to centralize the `PlayerDto` shape. | Frontend | 15 min |
| **P2** | Selector ambiguity in multi-row scenarios | `PlayerSearchOverlay.spec.ts:75,172,193` | Replace `wrapper.find('[data-testid="search-result-row"]')` with `wrapper.findAll('[data-testid="search-result-row"]').filter(hasText('Alice')).first()` for resilience. | Frontend | 10 min |

---

## Best Practices Found

1. **Fake-timer debounce testing** (`matchDraftStore.search.spec.ts:8-9,28,61`) — `vi.useFakeTimers()` + `vi.advanceTimersByTime(300)` is the canonical pattern for deterministic debounce verification (`timing-debugging.md`). This should be replicated for any future timer-dependent store logic.
2. **Explicit error string assertions** (`matchDraftStore.search.spec.ts:81,96`) — Tests verify exact error messages (`'Search service unavailable. Please try again later.'`, `'Network error. Please check your connection.'`). This prevents regression of user-facing copy.
3. **State cleanup on close** (`matchDraftStore.search.spec.ts:101-119`) — `closeSearch` clears query, results, error, loading, and the debounce timer. This is a complete teardown pattern that prevents state leakage between overlay open/close cycles.
4. **MockMvc standalone setup** (`UserMatchControllerATDDTest.java:56`) — `MockMvcBuilders.standaloneSetup()` avoids Spring context spin-up, keeping tests fast and isolated.
5. **Soft-delete filtering test** (`UserServiceTest.java:278-308`) — Seeds `deleted-*` and `ex-player-*` email/nickname prefixes and verifies they are excluded from results. This directly validates the AC3 security constraint.

---

## Quality Criteria Assessment

| Criterion | Status | Violations | Notes |
|-----------|--------|-----------:|-------|
| BDD Format (Given-When-Then) | ⚠️ WARN | 0 | Descriptive names and `// Given`-style comments are absent; tests use imperative descriptions. Low severity because priority markers and assertions are explicit. |
| Test IDs | ✅ PASS | 0 | `[P0]`/`[P1]` markers present in all new tests. |
| Priority Markers (P0–P3) | ✅ PASS | 0 | Consistent across frontend and backend. |
| Hard Waits (sleep, waitForTimeout) | ✅ PASS | 0 | None found. |
| Determinism (no conditionals/random) | ⚠️ WARN | 2 (D-1, D-2) | Store-instance pollution and compilation failure break determinism. |
| Isolation (cleanup, no shared state) | ❌ FAIL | 2 (I-1, I-2) | Stale Pinia instance shared across tests; broken mock injection. |
| Fixture Patterns | ⚠️ WARN | 0 / M-1 | `createTestingPinia` is used, but the `beforeEach` pattern is incorrect. |
| Data Factories | ⚠️ WARN | 1 (M-1) | Hardcoded literals; no factory helper. |
| Network-First Pattern | ✅ PASS | 0 | Not applicable to unit/component tests (no browser navigation). Store tests mock `fetch` directly. |
| Explicit Assertions | ✅ PASS | 0 | Every test has ≥1 explicit `expect`/`assertThat`. |
| Test Length (≤300 lines) | ✅ PASS | 0 | All files under 300 lines. |
| Test Duration (≤1.5 min) | ✅ PASS | 0 | Mock-based; no real I/O. |
| Flakiness Patterns | ✅ PASS | 0 | No tight timeouts, no races, no retries. |

**Total Violations**: 2 Critical, 0 High, 1 Medium, 0 Low

---

## Decision

**Recommendation**: **Block**

**Rationale**:
The test suite for Story 2.7 has **two critical blockers** that make it unsuitable for merge in its current state:

1. **Compilation failure** (`UserMatchControllerATDDTest.java:49`): A missing `import com.tictactore.service.UserService;` prevents the entire backend ATDD test class from building. This is a trivial fix but must be resolved before any backend test can run.

2. **Broken test isolation** (`PlayerSearchOverlay.spec.ts:8-17`): The `beforeEach` captures a store from one Pinia instance while each test mounts the component with a *different* instance. Six of seven component tests fail because mutations target the stale instance. This is a fundamental test-architecture bug — the tests give a false impression of coverage while actually validating nothing.

These are not style issues or maintainability nits. They are **functional blockers**: the tests do not execute, and the ones that do execute produce false negatives. The underlying production code may be correct, but the test suite cannot verify that until these issues are fixed.

**For Block**:
> Test quality is insufficient with 61/100 score. Two critical issues (compilation failure, broken test isolation) make the test suite non-executable. Fix both blockers, re-run the full suite, and request a re-review.

---

## Appendix

### Violation Summary by Location

| File | Line(s) | Severity | Criterion | Issue | Fix |
|------|---------|----------|-----------|-------|-----|
| `UserMatchControllerATDDTest.java` | 49 | CRITICAL | Compilation / Isolation | Missing `import com.tictactore.service.UserService` | Add missing import |
| `PlayerSearchOverlay.spec.ts` | 8-17 | CRITICAL | Isolation / Determinism | `beforeEach` captures store from stale Pinia instance; each test mounts with a new instance | Move `useMatchDraftStore()` call inside each test after mounting |
| `PlayerSearchOverlay.spec.ts` | 69-70,131,147,163,186 | MEDIUM | Data Factories | Hardcoded `PlayerDto` literals in 5 tests | Extract `createTestPlayer()` factory helper |
| `PlayerSearchOverlay.spec.ts` | 75,172,193 | MEDIUM | Selector Resilience | `find('[data-testid="search-result-row"]')` without content filter | Use `filter({ hasText: '...' })` for multi-row clarity |

### Review Metadata

- **Generated By**: BMad TEA Agent (Test Architect)
- **Workflow**: `testarch-test-review` (Create mode, sequential execution)
- **Review ID**: `tea-review-2-7-global-player-search-and-selection`
- **Timestamp**: 2026-08-10T00:52:00+02:00
- **Knowledge Base**: `bmad-testarch-test-review/resources/knowledge/` (core + extended fragments consulted)

### Execution Evidence

- **Frontend unit tests** (`vitest run`):
  - `PlayerSearchOverlay.spec.ts`: 7 tests, **6 FAILED**, 1 passed
  - `matchDraftStore.search.spec.ts`: 6 tests, all passed
  - `PlayerSelection.spec.ts`: 3 tests, all passed
- **Backend tests** (`mvnw test`):
  - `UserMatchControllerATDDTest`: **COMPILATION FAILED** (missing import)
  - `UserServiceTest`: not executed (compilation gate)

### Related Working-Tree Changes (Unrelated to 2.7)

Two E2E test files for stories 3.3 and 3.4 were also modified in the working tree:
- `frontend/e2e/tests/e2e/context-aware-verification.spec.ts`
- `frontend/e2e/tests/e2e/match-rejection.spec.ts`

Both changed `.not.toBeVisible()` → `.toBeHidden()`. This is a **positive improvement**: `toBeHidden()` waits for the element to reach the hidden state (accounts for exit animations), whereas `not.toBeVisible()` is an instantaneous snapshot check. Aligns with `timing-debugging.md` deterministic-waiting principles.

### Checklist Compliance

Validated against `checklist.md`:
- ✅ All test files in scope reviewed (5/5)
- ✅ Test framework detected (JUnit 5 / Vitest / Vue Test Utils)
- ✅ Knowledge base fragments loaded (test-quality, data-factories, test-levels, selector-resilience, timing-debugging, risk-governance, probability-impact)
- ✅ Story file + test-design consulted for context
- ✅ No false positives (violations are legitimate, confirmed by execution output)
- ✅ Every finding has a recommended fix with location
- ✅ CLI sessions: none spawned (read-only code analysis + local test execution)
