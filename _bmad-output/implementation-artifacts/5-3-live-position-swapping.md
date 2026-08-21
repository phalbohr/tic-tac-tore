# Story 5.3: Live Position Swapping

Status: ready-for-dev

## Story

As a 2v2 participant, I want to swap positions during the match, So that player-level stats are correct.

## Acceptance Criteria

1. **Given** live match active
   **When** "Swap Positions" tapped (during timeout/break)
   **Then** system updates current Attacker/Defender for subsequent goals (FR8)

## Tasks / Subtasks

- [ ] Task 1: Extend State Management with Swap Position Logic
  - [ ] Implement `swapPositions(team: 'teamA' | 'teamB')` method in `liveMatch.ts`.
- [ ] Task 2: Fix Grid Layout & Implement Swap Buttons
  - [ ] **CRITICAL FIX**: Ensure the grid layout correctly places **Team B in the TOP ROW** and **Team A in the BOTTOM ROW**.
  - [ ] Add two swap buttons placed exactly in the center of their respective rows, between the Attacker and Defender.
  - [ ] DO NOT put swap buttons on the edges of the screen.
  - [ ] Bind swap buttons to `swapPositions` store method.
- [ ] Task 3: Unit Testing
  - [ ] Add unit tests in `liveMatch.spec.ts` for position swapping logic.
- [ ] Task 4: E2E Testing
  - [ ] Add Playwright E2E tests for tapping the swap button and recording a goal to verify the swapped player gets the goal.

## Dev Notes

### Technical Requirements
- **State Management (`frontend/src/stores/liveMatch.ts`):**
  - Implement a `swapPositions(team: 'teamA' | 'teamB')` method in `liveMatch.ts`.
  - The method should swap the `attacker` and `defender` objects within the specified team (e.g., `const temp = teamA.value.attacker; teamA.value.attacker = teamA.value.defender; teamA.value.defender = temp;`).
- **UI Interaction (`frontend/src/features/match/LiveMatch.vue`):**
  - **LAYOUT CORRECTION**: Ensure Team B is on the top row (`tl` and `tr`) and Team A is on the bottom row (`bl` and `br`). If the current code has this wrong, FIX IT.
  - Add exactly two buttons to trigger position swapping.
  - The Swap button for Team B must be in the **center of the TOP row** (between Team B's Attacker and Defender).
  - The Swap button for Team A must be in the **center of the BOTTOM row** (between Team A's Attacker and Defender).
  - DO NOT put buttons on the edges.
  - `data-testid="swap-team-a-btn"` and `data-testid="swap-team-b-btn"`.

### Architecture Compliance
- Use Vue 3 `<script setup lang="ts">` and Pinia setup store pattern.
- Feature components placed in `frontend/src/features/match/`.
- All CSS styles must adhere to Tailwind CSS v4 and SCSS with `ch-` prefix where custom classes are needed.
- No source file or test file may exceed the 500-line strict limit (IP-04).

### Previous Story Intelligence (5.2)
- The layout uses `w-screen h-screen overflow-hidden` with a `grid-cols-2 grid-rows-2`.
- A top strip header exists for the timeline and undo button (`z-30`).
- Ensure swap buttons are positioned so they don't obstruct quadrant tapping, for instance, by placing them centered over the gap between the two quadrants on the same row.

### Project Structure Notes
- Main View: `frontend/src/features/match/LiveMatch.vue`
- Store: `frontend/src/stores/liveMatch.ts`
- Unit Tests: `frontend/tests/unit/liveMatch.spec.ts`
- E2E Tests: `frontend/e2e/real-time-scoring-interface.spec.ts`

### References
- PRD FR8: "Player can swap attacker/defender positions during a live match (Phase 1.5)"
- Architecture AD-06: PWA-First Infrastructure
