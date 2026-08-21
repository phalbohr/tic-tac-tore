---
baseline_commit: 86c186d1da6d6132c4fcd1b65f846cba198beee3
---
# Story 5.3: Live Position Swapping

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 2v2 participant,
I want to swap positions during the match,
so that player-level stats are correct.

## Acceptance Criteria

1. **Given** a live match is active
   **When** "Swap Positions" is tapped for Team A during a timeout or pause
   **Then** the store swaps Team A's attacker and defender player assignments
   **And** the UI immediately updates Team A's quadrant player labels
   **And** subsequent goals scored by Team A are attributed to the newly assigned attacker/defender (FR8)
   **And** previously recorded goals in the timeline retain their original player and role snapshot

2. **Given** a live match is active
   **When** "Swap Positions" is tapped for Team B during a timeout or pause
   **Then** the store swaps Team B's attacker and defender player assignments
   **And** the UI immediately updates Team B's quadrant player labels
   **And** subsequent goals scored by Team B are attributed to the newly assigned attacker/defender (FR8)
   **And** previously recorded goals in the timeline retain their original player and role snapshot

3. **Given** the live scoring landscape interface
   **When** viewing the match grid
   **Then** Team B is positioned on the TOP row (`tl` and `tr`) and Team A is positioned on the BOTTOM row (`bl` and `br`)
   **And** exactly two swap buttons are rendered:
     - Team B swap button centered directly between Team B attacker and defender in the TOP row
     - Team A swap button centered directly between Team A attacker and defender in the BOTTOM row
   **And** swap buttons are NOT placed on the outer screen edges
   **And** swap buttons meet the minimum touch target requirement of 56x56dp (PRD Sec 7.2)
   **And** swap buttons include explicit accessibility labels (`aria-label="Swap Team A Positions"`, `aria-label="Swap Team B Positions"`) and test selectors (`data-testid="swap-team-a-btn"`, `data-testid="swap-team-b-btn"`)

4. **Given** a user taps a swap button
   **When** the tap event occurs
   **Then** touch and pointer event propagation is stopped (`@pointerdown.stop` / `@click.stop`)
   **And** no accidental goal is registered on the underlying screen quadrants

## Tasks / Subtasks

- [x] Task 1: Extend State Management with Swap Position Logic (AC 1, AC 2)
  - [x] In `frontend/src/stores/liveMatch.ts`, implement `swapPositions(team: 'teamA' | 'teamB'): void`
  - [x] Swap the `attacker` and `defender` objects within the specified team state
  - [x] Ensure `getPlayerName(playerId: string)` continues resolving player names correctly
  - [x] Ensure goal recording continues snapshotting `playerName` so past goals remain unchanged
- [x] Task 2: Correct Grid Layout & Implement Centered Swap Buttons (AC 3, AC 4)
  - [x] In `frontend/src/features/match/LiveMatch.vue`, update the 2x2 grid layout:
    - **Top Row**: Team B quadrants (`tl` and `tr`)
    - **Bottom Row**: Team A quadrants (`bl` and `br`)
  - [x] Add centered Swap button in the Top Row between Team B's two quadrants (`data-testid="swap-team-b-btn"`, `aria-label="Swap Team B Positions"`)
  - [x] Add centered Swap button in the Bottom Row between Team A's two quadrants (`data-testid="swap-team-a-btn"`, `aria-label="Swap Team A Positions"`)
  - [x] Ensure buttons meet the 56x56dp minimum touch target (`min-w-[56px] min-h-[56px]` / `w-14 h-14`)
  - [x] Apply "The Clubhouse Editorial" styling tokens (`ch-bg-gray-700`, `ch-border-gray-600`, `ch-text-white`, `shadow-lg`, `rounded-full`)
  - [x] Stop pointer and click event propagation (`@pointerdown.stop`, `@click.stop`) to isolate swap clicks from quadrant goal touches
  - [x] Optionally trigger subtle haptic feedback (`navigator.vibrate?.([30])`) upon swap tap
- [x] Task 3: Unit Testing (AC 1, AC 2)
  - [x] Extend `frontend/tests/unit/liveMatch.spec.ts` to test:
    - `swapPositions('teamA')` correctly inverts attacker and defender for Team A
    - `swapPositions('teamB')` correctly inverts attacker and defender for Team B
    - Recording goals before and after `swapPositions` attributes each goal to the correct active player while preserving past goal snapshots in `goals` and `goalTimeline`
    - Calling `getPlayerName()` resolves player names correctly before and after swapping
  - [x] Adhere to the strict 500-line file limit (IP-04)
- [x] Task 4: E2E Playwright Integration Testing (AC 1, AC 2, AC 3, AC 4)
  - [x] Update `frontend/e2e/real-time-scoring-interface.spec.ts` with test cases:
    - Verifying Team B swap button is visible in the top row and Team A swap button in the bottom row
    - Tapping Team A swap button updates quadrant labels and subsequent goal is attributed to the new attacker
    - Tapping Team B swap button updates quadrant labels and subsequent goal is attributed to the new attacker
    - Verifying timeline displays past goals with original scorer names and new goals with swapped scorer names
    - Verifying tapping swap button does not trigger an accidental goal registration

### Review Findings

- [x] [Review][Dismiss] Missing Pause/Timeout Constraint — Dismissed: leave it up to the player.
- [x] [Review][Patch] UI Overlap — Add padding to quadrants to prevent text overlap with swap buttons.
- [x] [Review][Patch] SSR ReferenceError on navigator.vibrate [`frontend/src/features/match/LiveMatch.vue:38`]
- [x] [Review][Patch] Store State Mutation (References vs IDs) [`frontend/src/stores/liveMatch.ts:84`]
- [x] [Review][Patch] Incomplete E2E assertions for Swap Button Placement [`frontend/e2e/real-time-scoring-interface.spec.ts`]
- [x] [Review][Patch] Unrelated File Modification (Whitespace) [`frontend/e2e/support/fixtures/stats-fixture.ts:21`]
- [x] [Review][Patch] Inconsistent Test Interactions (click vs tap) [`frontend/e2e/real-time-scoring-interface.spec.ts`]
- [x] [Review][Patch] Inconsistent ATDD Checklist Status [`_bmad-output/test-artifacts/atdd-checklist-5-3-live-position-swapping.md`]
- [x] [Review][Patch] Disproportionate Icon Size [`frontend/src/features/match/LiveMatch.vue`]
- [x] [Review][Defer] No Visual Distinction for Buttons — deferred, pre-existing

## Dev Agent Record

### Implementation Plan
- Extend `useLiveMatchStore` (`frontend/src/stores/liveMatch.ts`) with `swapPositions(team: 'teamA' | 'teamB')`.
- Reorder landscape grid layout in `frontend/src/features/match/LiveMatch.vue`: Team B (defender/attacker) on top row, Team A (attacker/defender) on bottom row.
- Add centered, accessible swap buttons (56x56dp minimum touch target) with event propagation isolation (`@pointerdown.stop`, `@click.stop`).
- Activate unit tests in `frontend/tests/unit/liveMatch.spec.ts` and E2E tests in `frontend/e2e/real-time-scoring-interface.spec.ts`.
- Address code review findings (SSR safety, object copying, UI padding, icon sizing, E2E assertion coverage, whitespace cleanup).
- Execute full local CI verification (`./scripts/ci-local.sh`).

### Debug Log
- Verified red-green test cycle for unit tests in `frontend/tests/unit/liveMatch.spec.ts`.
- Verified 27 Playwright tests in `frontend/e2e/real-time-scoring-interface.spec.ts` turning green.
- Addressed all 7 review patch findings:
  - Added padding (`p-6`, `px-6`) to `LiveQuadrant.vue` to prevent UI text overlap with center swap buttons.
  - Guarded `navigator.vibrate` calls with SSR-safe checks (`typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function'`).
  - Swapped positions via object cloning in `liveMatch.ts` to avoid reference mutation bugs.
  - Increased swap SVG icon size to `w-7 h-7` for proportional balance inside 56x56dp buttons.
  - Removed accidental whitespace edit in `frontend/e2e/support/fixtures/stats-fixture.ts`.
  - Switched E2E touch interactions from `.click()` to `.tap()` consistently in touch mode.
  - Added comprehensive spatial assertions for swap button centering and quadrant rows in E2E tests.
  - Synchronized ATDD checklist status to green phase complete.
- Executed full local CI verification (`./scripts/ci-local.sh`).

### Completion Notes
- Implemented `swapPositions(team)` in `useLiveMatchStore` to swap attacker/defender for either team.
- Corrected landscape grid order in `LiveMatch.vue`: Team B on top row, Team A on bottom row.
- Added centered swap action buttons with 56x56dp touch targets, SVG bidirectional swap icons (`w-7 h-7`), aria-labels, and test IDs `swap-team-a-btn` / `swap-team-b-btn`.
- Verified isolation of touch events preventing accidental goal scoring when tapping swap buttons.
- ✅ Resolved review finding [Patch]: UI Overlap (added quadrant padding in LiveQuadrant.vue).
- ✅ Resolved review finding [Patch]: SSR ReferenceError on navigator.vibrate (added SSR guard in LiveMatch.vue and LiveQuadrant.vue).
- ✅ Resolved review finding [Patch]: Store State Mutation (swapped using object cloning).
- ✅ Resolved review finding [Patch]: Incomplete E2E assertions for Swap Button Placement (added comprehensive grid & button layout assertions).
- ✅ Resolved review finding [Patch]: Unrelated File Modification (Whitespace) (reverted trailing spaces in stats-fixture.ts).
- ✅ Resolved review finding [Patch]: Inconsistent Test Interactions (click vs tap) (used .tap() consistently).
- ✅ Resolved review finding [Patch]: Inconsistent ATDD Checklist Status (updated ATDD checklist).
- ✅ Resolved review finding [Patch]: Disproportionate Icon Size (adjusted icon to w-7 h-7).
- All acceptance criteria AC 1, AC 2, AC 3, AC 4 verified and covered by automated tests.

## File List

- `frontend/src/stores/liveMatch.ts` (modified)
- `frontend/src/features/match/LiveMatch.vue` (modified)
- `frontend/src/features/match/LiveQuadrant.vue` (modified)
- `frontend/tests/unit/liveMatch.spec.ts` (modified)
- `frontend/e2e/real-time-scoring-interface.spec.ts` (modified)
- `_bmad-output/test-artifacts/atdd-checklist-5-3-live-position-swapping.md` (modified)
- `_bmad-output/implementation-artifacts/5-3-live-position-swapping.md` (modified)
- `_bmad-output/implementation-artifacts/sprint-status.yaml` (modified)

## Change Log

- 2026-08-21: Implemented Story 5.3 live position swapping (store logic, UI layout correction, centered swap buttons with touch target >=56dp, unit & Playwright E2E tests).
- 2026-08-21: Addressed code review findings (SSR safety, store state cloning, UI padding, icon proportion, E2E assertion coverage, whitespace cleanup).

## Dev Notes

### Technical Requirements
- **State Management (`frontend/src/stores/liveMatch.ts`):**
  - Implement `swapPositions(team: 'teamA' | 'teamB'): void`:
    ```typescript
    const swapPositions = (team: 'teamA' | 'teamB') => {
      const targetTeam = team === 'teamA' ? teamA : teamB
      const temp = targetTeam.value.attacker
      targetTeam.value.attacker = targetTeam.value.defender
      targetTeam.value.defender = temp
    }
    ```
  - `recordGoal` snapshots `playerName: getPlayerName(playerId)` at score time. Swapping positions changes `teamA.value` / `teamB.value` for subsequent goals without mutating already recorded `Goal` objects in `goals.value`.
  - `getPlayerName(playerId)` checks both `attacker.id` and `defender.id` for each team, remaining fully valid regardless of swap count.

- **Grid Layout Correction & Placement (`frontend/src/features/match/LiveMatch.vue`):**
  - **Grid Layout Contract (Landscape)**:
    - **Top Row (Row 1 - Team B)**:
      - Left (`tl`): `teamB.defender` (or assigned quadrant role)
      - Center: Team B Swap Button (`data-testid="swap-team-b-btn"`)
      - Right (`tr`): `teamB.attacker` (or assigned quadrant role)
    - **Bottom Row (Row 2 - Team A)**:
      - Left (`bl`): `teamA.attacker` (or assigned quadrant role)
      - Center: Team A Swap Button (`data-testid="swap-team-a-btn"`)
      - Right (`br`): `teamA.defender` (or assigned quadrant role)
  - **Swap Button Specs**:
    - Place each button in the center between the two quadrants on that row (e.g. absolute positioning centered on the row seam or relative flex divider with `z-20`).
    - DO NOT place swap buttons on the outer screen edges.
    - Size: Minimum 56x56dp (`w-14 h-14` / `min-w-[56px] min-h-[56px]`) to comply with PRD Section 7.2.
    - Touch isolation: Prevent event bubbling using `@pointerdown.stop` / `@click.stop` to avoid triggering `@pointerdown.prevent` in `LiveQuadrant.vue`.
    - Accessibility: `aria-label="Swap Team A Positions"`, `aria-label="Swap Team B Positions"`, and an intuitive SVG swap icon (bidirectional arrows).

- **Test Selectors Contract:**
  - `data-testid="swap-team-a-btn"` — Team A swap button
  - `data-testid="swap-team-b-btn"` — Team B swap button
  - `data-testid="quadrant-teamA.attacker"` — Team A attacker quadrant
  - `data-testid="quadrant-teamA.defender"` — Team A defender quadrant
  - `data-testid="quadrant-teamB.attacker"` — Team B attacker quadrant
  - `data-testid="quadrant-teamB.defender"` — Team B defender quadrant

### Architecture Compliance
- Use Vue 3 `<script setup lang="ts">` and Pinia setup store pattern.
- Feature components placed in `frontend/src/features/match/`.
- All CSS styles must adhere to Tailwind CSS v4 and SCSS with `ch-` prefix where custom classes are needed.
- No source file or test file may exceed the 500-line strict limit (IP-04).

### Previous Story Intelligence (5.1 & 5.2)
- Top strip header contains `LiveActivityTimeline` and `Undo` button (`z-30`).
- Ensure swap buttons in the grid (`z-20`) do not overlap or interfere with the top header (`z-30`).
- Use relative match time formatting (`MM:SS`) in timeline.
- Maintain UUID fallback (`generateUUID()`) in `liveMatch.ts`.
- In Playwright E2E tests, use `.tap()` or `.click()` directly without brittle TouchEvent hack dispatches.

### Project Structure Notes
- Component: `frontend/src/features/match/LiveMatch.vue`
- Store: `frontend/src/stores/liveMatch.ts`
- Unit Tests: `frontend/tests/unit/liveMatch.spec.ts`
- E2E Tests: `frontend/e2e/real-time-scoring-interface.spec.ts`

### ATDD Artifacts
- Checklist: `_bmad-output/test-artifacts/atdd-checklist-5-3-live-position-swapping.md`
- Unit Tests: `frontend/tests/unit/liveMatch.spec.ts`
- E2E Tests: `frontend/e2e/real-time-scoring-interface.spec.ts`

### References
- PRD FR8: "Player can swap teammate positions within a game during live match mode via a per-team swap button, if the active rule system permits within-game swaps (Phase 1.5)"
- PRD Section 7.1: Screen reader accessibility and aria-labels on action buttons
- PRD Section 7.2: Minimum 56x56dp touch targets for live action buttons
- UX Specification: "STRICT RULE FOR LIVE MATCH LAYOUT (LANDSCAPE): Team B on TOP ROW, Team A on BOTTOM ROW, two swap buttons placed directly in the center between attacker and defender of the same team"
- Architecture AD-06: PWA-First Infrastructure
