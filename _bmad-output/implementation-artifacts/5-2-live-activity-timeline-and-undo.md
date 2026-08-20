# Story 5.2: Live Activity Timeline & Undo

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to see the goal sequence and correct errors,
so that the match protocol is accurate.

## Acceptance Criteria

1. **Given** a goal is recorded
   **When** viewing the interface
   **Then** event appears in live timeline (FR10)
   **And** "Undo Last Goal" removes it from history (FR9)

## Tasks / Subtasks

- [ ] Task 1: Create Live Activity Timeline Component
  - [ ] Initialize `LiveActivityTimeline.vue` in `frontend/src/features/match/`
  - [ ] Display the sequence of recorded goals in reverse chronological order (latest on top)
  - [ ] Show scorer identification (name) and the quadrant role where the goal was scored
- [ ] Task 2: Implement Undo Functionality in State
  - [ ] Update `useLiveMatchStore` in `frontend/src/stores/liveMatch.ts`
  - [ ] Add `undoLastGoal()` action that removes the most recent goal from `goals` array
  - [ ] Provide a computed property `canUndo` that returns true if `goals` array is not empty
- [ ] Task 3: Implement Undo UI
  - [ ] Add an "Undo Last Goal" button to the `LiveMatch.vue` or timeline component
  - [ ] Button should be disabled when `canUndo` is false
  - [ ] Ensure button styling aligns with "The Clubhouse Editorial" design tokens (e.g., proper contrast in dark theme)
- [ ] Task 4: Testing & Quality
  - [ ] Write unit tests for `undoLastGoal` action in Pinia store
  - [ ] Update Live Mode E2E Playwright tests to verify the timeline displays goals and the undo button removes them
  - [ ] Verify components conform to the 500-line strict limit (IP-04)

## Dev Notes

### Technical Requirements
- **State Management:** The store `liveMatch.ts` already contains a `goals` array with `playerId`, `quadrantRole`, and `timestamp`. Use this array for rendering the timeline.
- **Undo Logic:** The undo button should strictly pop the last item from the `goals` array based on the sequence they were inserted.
- **Visual Design:** The application relies on Tailwind CSS v4 and SCSS. Follow "The Clubhouse Editorial" style guide (dark theme, no pure white, strict adherence to `ch-` prefix). The timeline shouldn't obscure the live quadrants entirely.

### Architecture Compliance
- Use Vue 3 `<script setup>` syntax and Pinia for state management.
- Place new domain components in `frontend/src/features/match/`.
- All CSS must use the established design tokens.
- Ensure no source file exceeds the 500-line strict limit (IP-04).

### Previous Story Intelligence (5.1)
- Ensure E2E tests are placed in the correct location (`frontend/e2e/`), do not place them inside `frontend/tests/e2e/` (based on past review patches).
- Avoid brittle TouchEvent hacks in E2E tests when verifying clicks/taps on the undo button.

### Project Structure Notes
- Alignment with unified project structure: feature-based organization (`features/match/`).
- The state is managed within `frontend/src/stores/liveMatch.ts`.

### References
- PRD FR9: "Player can undo the last recorded goal during live match entry (Phase 1.5)"
- PRD FR10: "System displays a live activity timeline showing goal sequence with scorer identification during live match mode (Phase 1.5)"

## Dev Agent Record

### Agent Model Used
Gemini 3.1 Pro (High)

### Debug Log References
- Extracted from `epics.md`, `prd.md`, and `architecture.md`.
- Derived from `5-1-real-time-scoring-interface-landscape.md` previous intelligence.

### Completion Notes List
- Comprehensive developer guide created for timeline and undo features.

### File List
- `frontend/src/features/match/LiveActivityTimeline.vue` (to be created)
- `frontend/src/stores/liveMatch.ts` (to modify)
- `frontend/src/features/match/LiveMatch.vue` (to modify)
- `frontend/e2e/real-time-scoring-interface.spec.ts` (to modify)
