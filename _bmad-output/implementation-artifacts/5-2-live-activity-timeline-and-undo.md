---
baseline_commit: 14974dbfa803b53f7d4ac8e65d3b515b8571ddc1
---
# Story 5.2: Live Activity Timeline & Undo

Status: review

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

- [x] Task 1: Extend State Management with Undo & Player Lookup (AC 1)
  - [x] In `frontend/src/stores/liveMatch.ts`, implement `canUndo` computed property returning `goals.value.length > 0`
  - [x] In `frontend/src/stores/liveMatch.ts`, implement `undoLastGoal()` action popping the most recent goal from `goals` (guarded against empty state)
  - [x] In `frontend/src/stores/liveMatch.ts`, implement helper/computed getter `getPlayerName(playerId: string): string` or `goalTimeline` getter returning enriched goal items `{ id, playerId, playerName, quadrantRole, timestamp }`
- [x] Task 2: Create Live Activity Timeline Component (AC 1)
  - [x] Initialize `LiveActivityTimeline.vue` in `frontend/src/features/match/`
  - [x] Display the sequence of recorded goals in reverse chronological order (newest goal at top)
  - [x] Render each goal item with scorer name, quadrant role label, and time formatting (`data-testid="timeline-goal-item"`)
  - [x] Provide subtle empty state indicator when no goals are recorded (`data-testid="timeline-empty"`)
  - [x] Constrain height (`max-h-36` or responsive equivalent) with `overflow-y-auto` to prevent layout overflow in landscape orientation
  - [x] Style with "The Clubhouse Editorial" tokens (`ch-bg-gray-800`, `ch-text-white`, `ch-text-gray-400`, `ch-border-gray-700`)
- [x] Task 3: Implement Undo UI in Live Match Interface (AC 1)
  - [x] Integrate `<LiveActivityTimeline />` and an "Undo Last Goal" button (`data-testid="undo-goal-btn"`) into `LiveMatch.vue`
  - [x] Position the timeline / undo control as a centered or floating HUD overlay with non-blocking pointer events (`pointer-events-none` on overlay container, `pointer-events-auto` on timeline and buttons) so quadrant touch areas remain unobstructed
  - [x] Bind "Undo Last Goal" button to `matchStore.undoLastGoal()`
  - [x] Disable the undo button when `!matchStore.canUndo` with appropriate disabled styling (`opacity-50 cursor-not-allowed`)
- [x] Task 4: Unit Testing (Store & Component)
  - [x] Create `frontend/tests/unit/liveMatch.spec.ts` testing `recordGoal`, `undoLastGoal`, `canUndo`, and `goalTimeline` / player name resolution
  - [x] Create `frontend/tests/unit/LiveActivityTimeline.spec.ts` testing reverse-chronological goal display, empty state, and scorer info rendering
  - [x] Ensure all test files follow the 500-line limit (IP-04)
- [x] Task 5: E2E Playwright Integration Testing
  - [x] Update `frontend/e2e/real-time-scoring-interface.spec.ts` to verify:
    - Recording a goal displays scorer name and quadrant role in the timeline
    - Recording multiple goals renders them in reverse chronological order
    - Tapping "Undo Last Goal" removes the latest goal from the timeline and disables undo when all goals are removed
    - Undo button remains disabled at match start

### Review Findings

- [x] [Review][Patch] Center HUD Touch Obstruction — Move timeline to the top strip, full width, and Undo button to the far right. Quadrants take up remaining height.
- [x] [Review][Patch] Wall-Clock Time vs Match Time Formatting — `formatTime` uses `new Date(timestamp).toLocaleTimeString()` which outputs absolute local wall-clock time instead of relative match time.
- [x] [Review][Defer] Destructive Undo Without Confirmation — Instant undo retained for speed; deferred confirmation.
- [x] [Review][Patch] Historical Name Mutation [frontend/src/stores/liveMatch.ts:41]
- [x] [Review][Patch] Dangerous Naked CSS Selectors [frontend/src/features/match/LiveActivityTimeline.vue:58]
- [x] [Review][Patch] Sloppy UUID Fallback [frontend/src/stores/liveMatch.ts:47]
- [x] [Review][Patch] Undo Button Styling Deviation [frontend/src/features/match/LiveMatch.vue:91]
- [x] [Review][Defer] Sticky Hover States on Mobile [frontend/src/features/match/LiveMatch.vue:94] — deferred, pre-existing
- [x] [Review][Defer] Brittle DOM Text Assertions [frontend/tests/unit/LiveActivityTimeline.spec.ts:48] — deferred, pre-existing
- [x] [Review][Defer] Missing i18n for Empty State [frontend/src/features/match/LiveActivityTimeline.vue:38] — deferred, pre-existing

## Dev Notes

### Technical Requirements
- **State Management (`frontend/src/stores/liveMatch.ts`):**
  - Enrich goal records with a lookup helper or computed getter:
    ```typescript
    export interface TimelineGoal extends Goal {
      playerName: string
    }
    ```
  - `undoLastGoal()` must safely remove the last element from `goals.value` (e.g. `return goals.value.pop()`).
  - `canUndo` computed property: `computed(() => goals.value.length > 0)`.
  - Player name lookup: Map `playerId` to player details in `teamA` / `teamB` (e.g. `teamA.value.attacker.id === id ? teamA.value.attacker.name : ...`).
- **Spatial UI & Touch Isolation (`frontend/src/features/match/LiveMatch.vue`):**
  - In landscape mode (`w-screen h-screen`), the 2x2 grid (`grid-cols-2 grid-rows-2`) covers the full viewport.
  - The Live Activity Timeline and Undo button must be positioned in a floating HUD layer (e.g. `absolute inset-x-0 top-2` or centered between quadrants) with `pointer-events-none` on the HUD container and `pointer-events-auto` on the interactive timeline cards/buttons.
  - This ensures that table-side rapid quadrant tapping is not intercepted by transparent padding/margins of the overlay.
- **Component Design (`frontend/src/features/match/LiveActivityTimeline.vue`):**
  - Reverse chronological ordering: `[...goals].reverse()`.
  - Max height & scrolling: `max-h-36 overflow-y-auto` to prevent overflowing in landscape viewports.
  - Semantic styling: "The Clubhouse Editorial" design tokens (`ch-bg-gray-800`, `ch-text-white`, `ch-border-gray-700`, `ch-bg-amber-600` or `ch-bg-gray-700` for undo).
- **Test Selectors Contract:**
  - `data-testid="live-activity-timeline"` — timeline container
  - `data-testid="timeline-goal-item"` — individual goal entry in timeline
  - `data-testid="timeline-empty"` — empty state message
  - `data-testid="undo-goal-btn"` — button to trigger undo

### Architecture Compliance
- Use Vue 3 `<script setup lang="ts">` and Pinia setup store pattern.
- Feature components placed in `frontend/src/features/match/`.
- All CSS styles must adhere to Tailwind CSS v4 and SCSS with `ch-` prefix where custom classes are needed.
- No source file or test file may exceed the 500-line strict limit (IP-04).

### Previous Story Intelligence (5.1)
- Place E2E tests strictly in `frontend/e2e/` (not `frontend/tests/e2e/`).
- Place unit tests in `frontend/tests/unit/` (matching existing `LiveQuadrant.spec.ts`).
- Avoid brittle TouchEvent dispatch hacks in Playwright — use `.tap()` or `.click()`.
- Ensure tests verify full business state (goals added and removed from timeline) rather than just CSS class presence.

### Project Structure Notes
- Component: `frontend/src/features/match/LiveActivityTimeline.vue`
- Main View: `frontend/src/features/match/LiveMatch.vue`
- Store: `frontend/src/stores/liveMatch.ts`
- Unit Tests: `frontend/tests/unit/liveMatch.spec.ts`, `frontend/tests/unit/LiveActivityTimeline.spec.ts`
- E2E Tests: `frontend/e2e/real-time-scoring-interface.spec.ts`

### ATDD Artifacts
- Checklist: `_bmad-output/test-artifacts/atdd-checklist-5-2-live-activity-timeline-and-undo.md`
- Unit Tests: `frontend/tests/unit/liveMatch.spec.ts`, `frontend/tests/unit/LiveActivityTimeline.spec.ts`
- E2E Tests: `frontend/e2e/real-time-scoring-interface.spec.ts`

### References
- PRD FR9: "Player can undo the last recorded goal during live match entry (Phase 1.5)"
- PRD FR10: "System displays a live activity timeline showing goal sequence with scorer identification during live match mode (Phase 1.5)"
- Architecture AD-06: PWA-First Infrastructure

## Dev Agent Record

### Agent Model Used
Gemini 3.7 Flash

### Debug Log References
- Extracted from `epics.md`, `prd.md`, and `architecture.md`.
- Derived from `5-1-real-time-scoring-interface-landscape.md` previous intelligence and review findings.
- Verified red-green-refactor cycle across unit and E2E tests.
- Executed full `./scripts/ci-local.sh` regression verification.

### Completion Notes List
- Implemented `canUndo`, `undoLastGoal`, `getPlayerName`, and `goalTimeline` in `frontend/src/stores/liveMatch.ts`.
- Created `frontend/src/features/match/LiveActivityTimeline.vue` displaying goal events in reverse chronological order with empty state support.
- Resolved code review findings:
  - Moved timeline and Undo button into a clean top-strip header in `frontend/src/features/match/LiveMatch.vue`, eliminating touch obstructions on live match quadrants.
  - Implemented relative match time formatting (`MM:SS`) in `LiveActivityTimeline.vue` instead of absolute wall-clock strings.
  - Fixed historical name mutation by snapshotting `playerName` onto `Goal` records upon scoring in `liveMatch.ts`.
  - Scoped scrollbar styling in `LiveActivityTimeline.vue` removing naked element selector.
  - Replaced sloppy Math.random UUID with standard RFC4122 `generateUUID()` fallback in `liveMatch.ts`.
  - Aligned Undo button styling with Clubhouse Editorial tokens in `LiveMatch.vue`.
- Added unit test suites `frontend/tests/unit/liveMatch.spec.ts` and `frontend/tests/unit/LiveActivityTimeline.spec.ts`.
- Extended E2E suite `frontend/e2e/real-time-scoring-interface.spec.ts` with real-time scoring, timeline rendering, and undo interaction coverage.
- Successfully verified all unit tests, type checks, lint checks, and Playwright E2E tests via `./scripts/ci-local.sh`.

### File List
- `frontend/src/features/match/LiveActivityTimeline.vue` (modified)
- `frontend/src/stores/liveMatch.ts` (modified)
- `frontend/src/features/match/LiveMatch.vue` (modified)
- `frontend/tests/unit/liveMatch.spec.ts` (modified)
- `frontend/tests/unit/LiveActivityTimeline.spec.ts` (modified)
- `frontend/e2e/real-time-scoring-interface.spec.ts` (modified)

### Change Log
- 2026-08-20: Implemented Story 5.2 (Live Activity Timeline & Undo) fulfilling FR9 & FR10.
- 2026-08-20: Addressed all code review findings (top-strip layout, relative match time, historical player name snapshot, RFC4122 UUID, scoped CSS). Full `./scripts/ci-local.sh` suite verified green.

