---
baseline_commit: 47c2ceb86b51bf128a3f858807a0c8b67bca9c1b
---
# Story 5.4: Third-party Referee Mode

Status: ready-for-dev

## Story

As a referee standing at the end of the table,
I want a portrait-adapted live scoring UI with full player quadrant attribution,
so that I can track a match I am not playing in with the exact same fidelity as player live mode.

## Acceptance Criteria

1. **Given** "Referee Mode" is enabled (via match configuration toggle, query parameter `?mode=referee` / `?referee=true`, or automatic detection when the user is not among the 4 registered players)
   **When** live scoring starts
   **Then** the UI operates in **portrait orientation** without displaying the landscape lock/rotation overlay
   **And** orientation locking requests `'portrait'` or allows native portrait orientation without throwing errors
   **And** the UI provides a 4-quadrant layout adapted for viewing from the narrow end of the table (FR5, PRD Journey 5)

2. **Given** the portrait referee scoring view
   **When** viewing the match grid (`data-testid="match-grid"`)
   **Then** all 4 player quadrants are rendered in a 2x2 grid representing the table viewed from the end:
     - Left column corresponds to the team on the left side of the table (Team B: defender at top-left `tl`, attacker at bottom-left `bl`)
     - Right column corresponds to the team on the right side of the table (Team A: attacker at top-right `tr`, defender at bottom-right `br`)
     - Each quadrant clearly displays the player's name and role (`data-testid="quadrant-{role}"`, e.g. `quadrant-teamB.defender`, `quadrant-teamB.attacker`, `quadrant-teamA.attacker`, `quadrant-teamA.defender`) with touch padding (`p-6`) preventing text overlap
   **And** the top header strip displays `LiveActivityTimeline` with horizontal scroll capability and the `Undo` button (`data-testid="undo-goal-btn"`) with `shrink-0` to prevent layout clipping in narrow viewports (~390px)

3. **Given** the portrait referee scoring view
   **When** a player's quadrant is tapped
   **Then** a goal is immediately attributed to that specific player and role (FR5)
   **And** the goal appears in the live activity timeline with player name, role, and relative match time formatting (`MM:SS`)
   **And** visual (flashing feedback) and SSR-safe haptic feedback (`navigator.vibrate?.([50])`) are triggered

4. **Given** one or more goals recorded in referee mode
   **When** the "Undo" button is tapped
   **Then** the last recorded goal is removed from the timeline and score history (FR9, FR10)
   **And** the Undo button becomes disabled when no goals remain in the protocol

5. **Given** the portrait referee scoring view
   **When** a team's swap button is tapped during a pause/timeout
   **Then** the attacker and defender positions for that team are swapped (FR8)
   **And** the UI immediately updates quadrant player labels
   **And** subsequent quadrant taps attribute goals to the newly assigned roles while preserving past timeline snapshots
   **And** swap buttons are positioned between each team's quadrants in portrait mode:
     - Team B swap button centered in the left column between Team B defender and attacker (`data-testid="swap-team-b-btn"`, `aria-label="Swap Team B Positions"`)
     - Team A swap button centered in the right column between Team A attacker and defender (`data-testid="swap-team-a-btn"`, `aria-label="Swap Team A Positions"`)
   **And** swap buttons meet the minimum touch target requirement of 56x56dp (PRD Sec 7.2, `w-14 h-14 min-w-[56px] min-h-[56px]`) with event propagation isolation (`@pointerdown.stop`, `@click.stop`) preventing accidental goal registration
   **And** subtle haptic feedback (`navigator.vibrate?.([30])`) is triggered upon swap tap

## Tasks / Subtasks

- [ ] Task 1: Store & Mode State Support for Referee Mode (AC 1)
  - [ ] In `frontend/src/stores/liveMatch.ts`, add `isRefereeMode` ref state (default `false`) and action `setRefereeMode(val: boolean)`
  - [ ] Ensure `recordGoal`, `undoLastGoal`, `swapPositions`, `getPlayerName`, and `goalTimeline` behave consistently regardless of referee mode
- [ ] Task 2: Implement Portrait Referee Layout & Orientation Handling in LiveMatch (AC 1, AC 2)
  - [ ] In `frontend/src/features/match/LiveMatch.vue`:
    - [ ] Parse route query params (e.g. `route.query.referee === 'true' || route.query.mode === 'referee'`) or support store/props to enable referee mode
    - [ ] Suppress landscape rotation warning overlay when `isRefereeMode` is true (`v-if="isMatchStarted && !matchStore.isRefereeMode"`)
    - [ ] Adapt `startMatch()` orientation locking to request `lock('portrait')` or allow native portrait when in referee mode, safely guarded with try/catch
    - [ ] Render the 2x2 grid adapted for portrait table-end view: Left column (Team B: defender `tl`, attacker `bl`), Right column (Team A: attacker `tr`, defender `br`)
    - [ ] Ensure quadrants use `LiveQuadrant.vue` with proper test IDs, roles, player names, and padding
  - [ ] Ensure top header strip remains responsive in narrow viewports (~390px) with horizontal scroll on `LiveActivityTimeline` and `shrink-0` on Undo button
- [ ] Task 3: Position Swap Buttons for Portrait Layout (AC 5)
  - [ ] In `frontend/src/features/match/LiveMatch.vue`, position swap buttons adaptively for portrait mode:
    - [ ] Team B swap button centered in left column between Team B defender and attacker (`data-testid="swap-team-b-btn"`, `aria-label="Swap Team B Positions"`)
    - [ ] Team A swap button centered in right column between Team A attacker and defender (`data-testid="swap-team-a-btn"`, `aria-label="Swap Team A Positions"`)
    - [ ] In landscape mode, preserve standard placement (`top-1/4 left-1/2` and `top-3/4 left-1/2`)
  - [ ] Ensure swap buttons meet the 56x56dp minimum touch target (`min-w-[56px] min-h-[56px] w-14 h-14`)
  - [ ] Stop pointer and click event propagation (`@pointerdown.stop`, `@click.stop`) to prevent accidental goal scoring
  - [ ] Add SSR-safe haptic feedback (`navigator.vibrate?.([30])`)
- [ ] Task 4: Unit Testing (AC 1, AC 2, AC 3, AC 4, AC 5)
  - [ ] In `frontend/tests/unit/liveMatch.spec.ts` (or `frontend/tests/unit/LiveMatchReferee.spec.ts`):
    - [ ] Test `isRefereeMode` state toggle and getter/actions
    - [ ] Test goal attribution, undo, and position swapping in referee mode
    - [ ] Test preserving historical player names on swap during referee scoring
  - [ ] Maintain strict 500-line file limit (IP-04)
- [ ] Task 5: E2E Playwright Integration Testing (AC 1, AC 2, AC 3, AC 4, AC 5)
  - [ ] In `frontend/e2e/real-time-scoring-interface.spec.ts` (or `frontend/e2e/referee-mode.spec.ts`):
    - [ ] Test starting match in referee mode with mobile portrait viewport (`{ width: 390, height: 844 }`, `hasTouch: true`)
    - [ ] Verify landscape rotation warning overlay is NOT shown in portrait referee mode
    - [ ] Verify 2x2 grid layout and quadrant placement (Left: Team B defender/attacker, Right: Team A attacker/defender)
    - [ ] Verify Swap buttons are centered within each team's column (Team B on left, Team A on right) with >=56x56dp touch targets
    - [ ] Verify tapping quadrants records player-attributed goals in timeline
    - [ ] Verify Undo removes latest goal and disables when empty
    - [ ] Verify tapping swap buttons swaps roles and updates future goal attributions without registering accidental goals
  - [ ] Maintain strict 500-line file limit (IP-04)

## Dev Notes

### ATDD Artifacts
- **Checklist**: [_bmad-output/test-artifacts/atdd-checklist-5-4-third-party-referee-mode.md](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-5-4-third-party-referee-mode.md)
- **Unit Tests**: [frontend/tests/unit/liveMatch.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/tests/unit/liveMatch.spec.ts)
- **E2E Tests**: [frontend/e2e/referee-mode.spec.ts](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/referee-mode.spec.ts)

### Technical Requirements
- **Perspective & Spatial Layout Mapping (Portrait vs Landscape)**:
  - **Landscape Grid (Story 5.1/5.3)**:
    - Top Row (Row 1): Team B Defender (`tl`), Team B Attacker (`tr`) — Swap button centered at `top-1/4 left-1/2`
    - Bottom Row (Row 2): Team A Attacker (`bl`), Team A Defender (`br`) — Swap button centered at `top-3/4 left-1/2`
  - **Portrait Grid (Story 5.4 - Referee View from Table End)**:
    - 2 columns × 2 rows:
      - Left Column (Col 1 - Team B): Defender (`tl` - top left), Attacker (`bl` - bottom left) — Swap button centered in left column at `left-1/4 top-1/2` (or `portrait:left-1/4 portrait:top-1/2`)
      - Right Column (Col 2 - Team A): Attacker (`tr` - top right), Defender (`br` - bottom right) — Swap button centered in right column at `left-3/4 top-1/2` (or `portrait:left-3/4 portrait:top-1/2`)
  - **Rotation Warning Bypass**:
    - The rotation overlay (`landscape:hidden`) MUST be suppressed when `isRefereeMode` is true (`v-if="isMatchStarted && !matchStore.isRefereeMode"`), allowing full portrait usage without interruption.
  - **Screen Orientation Lock**:
    - Guard orientation locking with try/catch and SSR check. In referee mode, request `lock('portrait')` or skip landscape locking.

- **Test Selectors Contract**:
  - `data-testid="start-match-btn"` — Start match button
  - `data-testid="match-grid"` — 2x2 grid container
  - `data-testid="undo-goal-btn"` — Undo last goal button
  - `data-testid="live-activity-timeline"` — Timeline container
  - `data-testid="timeline-goal-item"` — Individual goal entry in timeline
  - `data-testid="timeline-empty"` — Empty state message
  - `data-testid="swap-team-a-btn"` — Swap Team A positions button (`aria-label="Swap Team A Positions"`)
  - `data-testid="swap-team-b-btn"` — Swap Team B positions button (`aria-label="Swap Team B Positions"`)
  - `data-testid="quadrant-teamA.attacker"` — Team A attacker quadrant
  - `data-testid="quadrant-teamA.defender"` — Team A defender quadrant
  - `data-testid="quadrant-teamB.attacker"` — Team B attacker quadrant
  - `data-testid="quadrant-teamB.defender"` — Team B defender quadrant

### Architecture Compliance
- Use Vue 3 `<script setup lang="ts">` and Pinia store.
- Component styling: Tailwind CSS v4 + SCSS with `ch-` prefix design tokens ("The Clubhouse Editorial").
- Minimum touch target: >= 56x56dp (`w-14 h-14 min-w-[56px] min-h-[56px]`) for all interactive buttons.
- Touch & Pointer Event Isolation: Use `@pointerdown.stop` and `@click.stop` on all interactive overlay buttons to prevent accidental goal scoring on underlying quadrants.
- SSR Safety: Ensure all browser APIs (`navigator.vibrate`, `screen.orientation`, `document.fullscreenElement`) are guarded against SSR execution.
- Strict 500-line file limit (IP-04) for all source and test files.

### Previous Story Intelligence (5.1, 5.2, 5.3)
- Goal snapshots must preserve `playerName` upon scoring so past goals in `goalTimeline` do not mutate when positions are swapped.
- Use `.tap()` consistently for touch interactions in Playwright E2E tests (`hasTouch: true`).
- Keep E2E tests strictly under `frontend/e2e/` and unit tests under `frontend/tests/unit/`.
- Ensure quadrants include padding (`p-6`) to prevent text overlap with centered swap buttons.

### References
- **PRD FR5**: "Third-party observer (referee) can record a match they are not participating in, with adapted UI for their viewing position (Phase 1.5)"
- **PRD FR14**: "System applies confirmation rules based on match context: 1v1 participant-entered, 1v1 referee-entered, 2v2 standard, 2v2 random pairings, referee-entered 2v2..."
- **PRD Journey 5 (lines 267–281)**: Viktor, The Referee standing at the narrow end in portrait orientation, viewing 4 player positions, tapping quadrants for goal attribution, using undo and position swaps.
- **UX Design Specification (line 65)**: Persona Viktor (The Referee) — "Portrait referee view, live mode, streamlined per-match flow".
