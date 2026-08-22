---
baseline_commit: f13c3d41f9c985bab5e35d2071a49abb98f4f787
---
# Story 5.4: Third-party Referee Mode

Status: ready-for-dev

## Story

As a referee standing at the end of the table,
I want a portrait-adapted live scoring UI with full player quadrant attribution,
so that I can track a match I am not playing in with the exact same fidelity as player live mode.

## Acceptance Criteria

1. **Given** "Referee Mode" is enabled (via match configuration toggle or automatic detection of a non-player observer)
   **When** live scoring starts
   **Then** the UI operates in **portrait orientation** without displaying the landscape lock/rotation overlay
   **And** the UI provides a 4-quadrant layout adapted for viewing from the narrow end of the table (FR5, PRD Journey 5)

2. **Given** the portrait referee scoring view
   **When** viewing the match grid
   **Then** all 4 player quadrants are rendered:
     - Left column corresponds to the team on the left side of the table (Team B: defender/attacker)
     - Right column corresponds to the team on the right side of the table (Team A: attacker/defender)
     - Each quadrant clearly displays the player's name and role (`data-testid="quadrant-{role}"`)
   **And** the top strip displays `LiveActivityTimeline` and the `Undo` button (`data-testid="undo-goal-btn"`)

3. **Given** the portrait referee scoring view
   **When** a player's quadrant is tapped
   **Then** a goal is immediately attributed to that specific player and role (FR5)
   **And** the goal appears in the live activity timeline with player initials and timestamp
   **And** visual (flashing feedback) and haptic feedback (`navigator.vibrate([50])`) are triggered

4. **Given** one or more goals recorded in referee mode
   **When** the "Undo" button is tapped
   **Then** the last recorded goal is removed from the timeline and score history (FR9, FR10)

5. **Given** the portrait referee scoring view
   **When** a team's swap button is tapped during a pause/timeout
   **Then** the attacker and defender positions for that team are swapped (FR8)
   **And** the UI immediately updates quadrant player labels
   **And** subsequent quadrant taps attribute goals to the newly assigned roles while preserving past timeline snapshots
   **And** swap buttons meet the minimum touch target requirement of 56x56dp (PRD Sec 7.2) with event propagation isolation (`@pointerdown.stop`, `@click.stop`)

## Tasks / Subtasks

- [ ] Task 1: Store & Mode State Support for Referee Mode (AC 1)
  - [ ] In `frontend/src/stores/liveMatch.ts` or component props, support `isRefereeMode` flag
  - [ ] Ensure `recordGoal`, `undoLastGoal`, and `swapPositions` behave identically for referee mode
- [ ] Task 2: Implement Portrait Referee Layout & Orientation Handling in LiveMatch (AC 1, AC 2)
  - [ ] In `frontend/src/features/match/LiveMatch.vue`, add support for portrait orientation when `isRefereeMode` is true:
    - Bypass/disable the landscape rotation warning overlay when in referee mode
    - Lock/request portrait orientation (or allow native portrait) during `startMatch`
  - [ ] Build the portrait 2x2 grid layout reflecting table-end view (columns for table sides, rows for attacker/defender)
  - [ ] Ensure quadrants use `LiveQuadrant.vue` with proper test IDs and labels
- [ ] Task 3: Position Swap Buttons for Portrait Layout (AC 5)
  - [ ] Add accessible swap buttons positioned between each team's quadrants in portrait mode (`data-testid="swap-team-b-btn"`, `data-testid="swap-team-a-btn"`)
  - [ ] Ensure buttons meet the 56x56dp minimum touch target (`min-w-[56px] min-h-[56px]` / `w-14 h-14`)
  - [ ] Stop pointer/click event propagation to prevent accidental goal scoring
- [ ] Task 4: Unit Testing (AC 1, AC 3, AC 4, AC 5)
  - [ ] Extend `frontend/tests/unit/liveMatch.spec.ts` (or dedicated unit tests) to verify referee mode state, goal attribution, and swaps
  - [ ] Ensure strict 500-line file limit (IP-04) is maintained
- [ ] Task 5: E2E Playwright Integration Testing (AC 1, AC 2, AC 3, AC 4, AC 5)
  - [ ] Create or update E2E tests in `frontend/e2e/referee-mode.spec.ts` (or `frontend/e2e/real-time-scoring-interface.spec.ts`) testing:
    - Starting match in referee mode in portrait viewport
    - Verifying no landscape warning overlay is shown in portrait mode
    - Tapping quadrants records player-attributed goals into timeline
    - Undo removes last goal
    - Swapping positions in portrait view updates quadrant roles and future goal attributions

## Dev Notes

### Technical Requirements
- **Perspective & Layout Mapping (Portrait Orientation)**:
  - Referee stands at the narrow end of the table (торец стола).
  - Landscape grid (Story 5.1/5.3): Row 1 = Team B, Row 2 = Team A.
  - Portrait grid (Story 5.4):
    - 2 columns × 2 rows:
      - Left column: Team B (`tl` = defender, `bl` = attacker, or table-oriented)
      - Right column: Team A (`tr` = attacker, `br` = defender, or table-oriented)
    - Alternatively, column-based or row-based 2x2 grid configured with CSS Grid (`portrait:grid-cols-2 portrait:grid-rows-2`).
  - The rotation overlay (`landscape:hidden`) MUST be suppressed when `isRefereeMode` is true, allowing full portrait usage.

- **Test Selectors Contract**:
  - `data-testid="start-match-btn"` — Start match button
  - `data-testid="undo-goal-btn"` — Undo last goal button
  - `data-testid="swap-team-a-btn"` — Swap Team A positions button
  - `data-testid="swap-team-b-btn"` — Swap Team B positions button
  - `data-testid="quadrant-teamA.attacker"` — Team A attacker quadrant
  - `data-testid="quadrant-teamA.defender"` — Team A defender quadrant
  - `data-testid="quadrant-teamB.attacker"` — Team B attacker quadrant
  - `data-testid="quadrant-teamB.defender"` — Team B defender quadrant

### Architecture Compliance
- Use Vue 3 `<script setup lang="ts">` and Pinia store.
- Component styling: Tailwind CSS v4 + SCSS with `ch-` prefix design tokens ("The Clubhouse Editorial").
- Minimum touch target: >= 56x56dp for all interactive buttons.
- Strict 500-line file limit (IP-04).

### References
- **PRD FR5**: "Third-party observer (referee) can record a match they are not participating in, with adapted UI for their viewing position (Phase 1.5)"
- **PRD Journey 5 (lines 267–281)**: Viktor, The Referee standing at the narrow end in portrait orientation, viewing 4 player positions, tapping quadrants for goal attribution.
- **UX Design Specification (line 65)**: Persona Viktor (The Referee) — "Portrait referee view, live mode, streamlined per-match flow".
