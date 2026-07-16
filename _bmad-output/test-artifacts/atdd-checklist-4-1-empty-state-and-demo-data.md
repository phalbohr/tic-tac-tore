---
stepsCompleted:
  - step-01-preflight-and-context
  - step-02-generation-mode
  - step-03-test-strategy
  - step-04c-aggregate
  - step-05-validate-and-complete
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-07-04T22:12:00Z'
storyId: '4.1'
storyKey: '4-1-empty-state-and-demo-data'
storyFile: '{project-root}/_bmad-output/implementation-artifacts/4-1-empty-state-and-demo-data.md'
atddChecklistPath: '{project-root}/_bmad-output/test-artifacts/atdd-checklist-4-1-empty-state-and-demo-data.md'
generatedTestFiles: 
  - 'frontend/tests/unit/useStatsStore.spec.ts'
  - 'frontend/tests/components/DemoDataToggle.spec.ts'
  - 'frontend/e2e/scenarios/demo-data-empty-state.spec.ts'
inputDocuments:
  - '{project-root}/_bmad/tea/config.yaml'
  - '{project-root}/_bmad-output/implementation-artifacts/4-1-empty-state-and-demo-data.md'
  - 'data-factories.md'
  - 'component-tdd.md'
  - 'test-quality.md'
  - 'test-healing-patterns.md'
  - 'selector-resilience.md'
  - 'timing-debugging.md'
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
---

# ATDD Checklist: 4-1-empty-state-and-demo-data

## Step 01: Preflight & Context
- Stack detected: fullstack
- Test Framework: Playwright (frontend/e2e)
- Loaded Core and UI+API Playwright Utils knowledge base fragments.

## Step 02: Generation Mode
- Mode selected: **AI Generation**
- Reason: The UI for demo data is not implemented yet (red-phase ATDD), so we cannot record browser sessions. We will rely on the story's acceptance criteria to generate tests.

## Step 03: Test Strategy

### Mapped Scenarios & Test Levels
1. **Component Level (`DemoDataToggle.vue`)**
   - Renders correctly with toggle state reflecting `tictactore.demoModeEnabled` from localStorage.
   - Toggling updates the localStorage value.
2. **Unit Level (`useStatsStore` / `demoDataGenerator.ts`)**
   - `demoDataGenerator` returns non-empty realistic data structure.
   - `useStatsStore` returns demo data when `demoModeEnabled` is true AND matches < 5.
   - `useStatsStore` strictly returns false for demo mode when real matches >= 5, regardless of localStorage preference.
   - `useStatsStore` returns empty state (CTA trigger) when demo data is OFF and real matches = 0.
3. **E2E Level (User Journey - Playwright)**
   - New user (< 1 match) sees demo data by default on Analytics page.
   - New user can toggle demo data off in Personal Cabinet.
   - Empty state with CTA ("Record First Match") appears if demo data is toggled off and matches < 1.
   - User records 5th real match -> demo data is completely hidden, toggle disappears, true stats are shown.

### Priorities
- E2E tests: **P0** (critical user journey).
- Unit tests: **P0** (enforces critical threshold constraint and guards real data).
- Component tests: **P1**.

### Red Phase Requirements
All tests are designed to fail initially because the components (`DemoDataToggle.vue`), generators (`demoDataGenerator.ts`), state overrides (`useStatsStore`), and localStorage keys do not yet exist.

## TDD Red Phase (Current)
✅ Red-phase test scaffolds generated

- Unit/Component Tests: 2 files (all skipped)
- E2E Tests: 1 file (all skipped)

## Acceptance Criteria Coverage
- AC1: Given user has < 1 confirmed match... (Covered by E2E and Unit tests)
- AC2: Given demo data is active, When the user opens their Personal Cabinet... (Covered by E2E and Component tests)
- AC3: Given demo data is active, When the user reaches 5 confirmed real matches... (Covered by E2E and Unit tests)

## Next Steps (Task-by-Task Activation)
During implementation of each task:
1. Remove `test.skip()` or `describe.skip()` from the current test file or scenario
2. Run tests: `npm run test` or `npm run test:e2e`
3. Verify the activated test fails first, then passes after implementation (green phase)
4. If any activated tests still fail unexpectedly:
   - Either fix implementation (feature bug)
   - Or fix test (test bug)
5. Commit passing tests

## Implementation Guidance
Feature endpoints to implement:
- N/A (Frontend-only story logic for demo data generation and state management)

UI components to implement:
- DemoDataToggle.vue
- useStatsStore.ts (state management)
- Demo data generation utilities
