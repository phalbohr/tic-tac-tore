# Story 4.1: Empty State & Demo Data

Status: done

## Story

As a new user,
I want to see sample statistics when I have no games,
so that I can understand the value of the platform.

## Acceptance Criteria

1. **Given** user has < 1 confirmed match
   **When** Analytics section is opened
   **Then** system displays generated demo data (FR57)
2. **Given** demo data is active
   **When** the user opens their Personal Cabinet
   **Then** a "Demo Mode" toggle allows hiding/showing it
3. **Given** demo data is active
   **When** the user reaches 5 confirmed real matches
   **Then** demo data is automatically disabled and hidden

## Tasks / Subtasks

- [ ] Task 1: Create Demo Data Generator (AC: 1)
  - [ ] Create `frontend/src/features/stats/utils/demoDataGenerator.ts` (or `.js`) that returns realistic static data.
  - [ ] Demo data MUST include realistic values: player names (e.g., 'Alex', 'Sam'), typical score distributions (e.g., 20 wins, 15 losses), and realistic win rates (~55%). Do NOT modify the backend.
- [ ] Task 2: Implement `DemoDataToggle` Component (AC: 2)
  - [ ] Create `frontend/src/features/profile/components/DemoDataToggle.vue`.
  - [ ] Add the toggle to `PersonalCabinetView`.
  - [ ] Persist the toggle state exclusively in `localStorage` under the key `tictactore.demoModeEnabled`.
- [ ] Task 3: Integrate Demo Data in Statistics Views (AC: 1, 3)
  - [ ] Update `frontend/src/features/stats/stores/useStatsStore.ts` (or similar store file) to intercept state and serve demo data when `tictactore.demoModeEnabled` is true, or when confirmed matches < 1 (and lifetime matches < 5).
  - [ ] Implement the "Empty State is a CTA" UX rule: if demo data is toggled off and matches < 1, display an empty state overlay with a primary CTA button "Record First Match" and a secondary CTA button "Toggle Demo Data".

### Review Findings

- [x] [Review][Decision] Spec contradiction regarding CTA threshold — PRD dictates "If a user has < 5 matches... a demo data overlay CTA must be shown." Task 3 dictates "if demo data is toggled off and matches < 1, display an empty state". Behavior for 1 to 4 matches is undefined.
- [x] [Review][Patch] Missing component and generator source files [frontend/src/features/profile/Cabinet.vue]
- [x] [Review][Patch] Missing demo data state logic in useStatsStore.ts [frontend/src/features/stats/stores/useStatsStore.ts]
- [x] [Review][Patch] Missing ATDD unit test file useStatsStore.spec.ts [frontend/tests/unit/useStatsStore.spec.ts]
- [x] [Review][Patch] Unconditional rendering of StatsDashboard and EmptyStateCTA [frontend/src/views/HomeHub.vue:66-68]
- [x] [Review][Patch] DemoDataToggle ignores 5-match threshold [frontend/src/features/profile/Cabinet.vue:242]
- [x] [Review][Patch] Destructive changes to existing testUserData keys [frontend/e2e/fixtures/test-data.ts:1-4]
- [x] [Review][Patch] Inclusion of out-of-scope test fixtures [frontend/tests/fixtures/test-data.ts]
- [x] [Review][Patch] Fragile E2E localStorage injection [frontend/e2e/scenarios/demo-data-empty-state.spec.ts]
- [x] [Review][Patch] Missing type annotation in E2E helper [frontend/e2e/scenarios/demo-data-empty-state.spec.ts]
- [x] [Review][Patch] E2E test enforces inverse of AC3 [frontend/e2e/scenarios/demo-data-empty-state.spec.ts:74]
- [x] [Review][Patch] Missing E2E coverage for default demo data [frontend/e2e/scenarios/demo-data-empty-state.spec.ts]
- [x] [Review][Patch] DemoDataToggle component test ignores persistence [frontend/tests/components/DemoDataToggle.spec.ts]

## Dev Notes

- **UX Design Rules:** "Empty states are CTAs, never blank." If a user has < 5 matches, the leaderboard hides them (viewer-ineligible), and a demo data overlay CTA must be shown.
- **Persistence:** The `DemoDataToggle` preference persists entirely in `localStorage`. Do not add database fields for this.
- **Auto-Clear Threshold:** Demo data is strictly disabled after 5 real matches are recorded. The store logic must enforce this threshold.
- **Architecture Constraints:** 
  - Custom UI styles must use the `ch-` prefix.
  - Follow the 500-line rule (IP-04) for any new classes/components.

### ATDD Artifacts

- Checklist: `_bmad-output/test-artifacts/atdd-checklist-4-1-empty-state-and-demo-data.md`
- Unit/Component tests:
  - `frontend/tests/unit/useStatsStore.spec.ts`
  - `frontend/tests/components/DemoDataToggle.spec.ts`
- E2E tests: `frontend/e2e/scenarios/demo-data-empty-state.spec.ts`

### Project Structure Details

- **DemoDataToggle:** `frontend/src/features/profile/components/DemoDataToggle.vue`
- **Data Generator:** `frontend/src/features/stats/utils/demoDataGenerator.ts`
- **State Store:** `frontend/src/features/stats/stores/`

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR57, Demo data threshold.
- [Source: _bmad-output/planning-artifacts/epics.md] - Epic 4 list.
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md] - UX-DR, PersonalCabinetView, viewer-ineligible state.

## Dev Agent Record

### Agent Model Used



### Debug Log References



### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created
- Interactive review improvements applied

### File List

