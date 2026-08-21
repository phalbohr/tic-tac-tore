---
baseline_commit: 21502298b722ae0c59f96be798e7caa7fd6bd4ea
---
# Story 5.4: Third-party Referee Mode

Status: ready-for-dev

## Story

As a referee,
I want a simplified scoring UI,
so that I can track a match I am not playing in.

## Acceptance Criteria

1. **Given** a match configuration includes a "Referee Mode" toggle or a user who is not playing is selected as the match tracker
   **When** scoring starts
   **Then** the UI provides a simplified interface instead of the 4-quadrant player layout
   **And** the UI provides large Left/Right buttons for goals (FR5)
   
2. **Given** the referee mode UI
   **When** the Left button is tapped
   **Then** a goal is correctly attributed to the Team corresponding to the Left side
   **And** the score updates accordingly

3. **Given** the referee mode UI
   **When** the Right button is tapped
   **Then** a goal is correctly attributed to the Team corresponding to the Right side
   **And** the score updates accordingly

4. **Given** the referee mode UI
   **When** viewing the live activity timeline
   **Then** goals recorded via referee mode appear in the timeline (consistent with Story 5.2)
   **And** they can be undone using the standard undo functionality

## Developer Context

### Architecture & Technical Requirements
- **State Management**: Use the existing Pinia `liveMatch` store (`frontend/src/stores/liveMatch.ts`). Referee mode needs to map the left/right buttons to `recordGoal('teamA')` and `recordGoal('teamB')`. If the store requires a specific player ID, Referee mode should attribute it to a "Team Goal" or the appropriate default.
- **UI Layout**:
  - For Referee mode, the screen layout should focus on large Left/Right hit areas. 
  - Ensure minimum touch target requirement of 56x56dp (PRD Sec 7.2) is met, though these buttons should be much larger (taking up a significant portion of the screen halves).
  - Stop pointer and click event propagation to avoid double triggering.
- **Match Setup Context**: 
  - Need a way to initialize `liveMatch` in "Referee Mode" (e.g., a boolean `isRefereeMode` in the store or component props).
- **Accessibility**: 
  - Provide explicit accessibility labels (e.g., `aria-label="Record Goal for Team A"`).
- **Testing**:
  - Implement Unit Tests in `frontend/tests/unit/liveMatch.spec.ts` for Referee mode behaviors if state changes are required.
  - Implement E2E Playwright Tests to verify Left/Right tap interactions and goal assignments.

### Previous Story Intelligence
- In Story 5.3, we added `swapPositions`. Ensure that in Referee mode (where we may not care about specific attacker/defender quadrant taps, but only team goals), the underlying data structure remains intact, or explicitly handle how team goals are attributed if position tracking is less rigid in referee mode.
- In Story 5.1/5.2, the 4-quadrant landscape view was built. The Referee mode could be an alternative component `LiveRefereeMatch.vue` or a conditional layout within `LiveMatch.vue`. A conditional layout might be easier if they share the timeline header.

## Tasks / Subtasks

- [ ] Task 1: Extend Store/Config for Referee Mode (AC 1)
- [ ] Task 2: Implement Referee UI Layout with Left/Right hit areas (AC 1, 2, 3, 4)
- [ ] Task 3: Unit Testing
- [ ] Task 4: E2E Playwright Integration Testing
