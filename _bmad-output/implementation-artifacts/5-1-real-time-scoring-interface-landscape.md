# Story 5.1: Real-time Scoring Interface (Landscape)

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to tap screen quadrants,
so that goals are recorded instantly without manual input.

## Acceptance Criteria

1. **Given** device in landscape mode
   **When** screen quadrant tapped
   **Then** goal awarded to specific player (FR4)
   **And** haptic feedback provided

## Tasks / Subtasks

- [ ] Task 1: Create Live Mode Component Base
  - [ ] Initialize `LiveMatch.vue` and `<LiveQuadrant />` in `frontend/src/features/match/`
  - [ ] Configure route for live match scoring in Vue Router
- [ ] Task 2: Implement Fullscreen & Landscape Orientation Lock
  - [ ] Require a "Start Match" user interaction to trigger `element.requestFullscreen()` first
  - [ ] Use Screen Orientation API (`screen.orientation.lock('landscape')`) once in fullscreen
  - [ ] Add CSS fallback instructions (e.g., "Please rotate your device") for unsupported devices
- [ ] Task 3: Implement Touch Quadrants via CSS Grid
  - [ ] Use `grid-cols-2 grid-rows-2 h-screen w-screen overflow-hidden` to cover the screen perfectly
  - [ ] Map quadrants to physical table corners: `teamA.attacker`, `teamA.defender`, `teamB.attacker`, `teamB.defender`
  - [ ] Apply "The Clubhouse Editorial" design tokens (dark theme, no pure white, strict adherence to `ch-` prefix)
- [ ] Task 4: Zero-Latency Touch & State Integration
  - [ ] Use CSS `touch-action: manipulation` and `@touchstart` (or fast tap equivalent) to eliminate 300ms mobile delay
  - [ ] Provide haptic feedback via `navigator.vibrate([50])` and an immediate visual flash for fallback
  - [ ] Dispatch explicit action to Pinia (e.g., `recordGoal(playerId, quadrantRole)`)
- [ ] Task 5: Testing
  - [ ] Add unit tests for touch quadrant mapping
  - [ ] Add E2E Playwright tests verifying goal registration in Live Mode
  - [ ] Adhere to the 500-Line rule for all new test files

## Dev Notes

### ATDD Artifacts
- Checklist: `/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-checklist-5-1-real-time-scoring-interface-landscape.md`
- E2E tests: `frontend/tests/e2e/real-time-scoring-interface.spec.ts`

### Technical Requirements
- **Fullscreen Prerequisite:** Mobile browsers (especially Android/Chrome and Safari) require the application to be in fullscreen mode before `screen.orientation.lock()` will work.
- **Zero-Latency Touch:** 300ms tap delay is unacceptable for live sports. Use `touch-action: manipulation` and appropriate touch events.
- **Visual Fallback for Haptics:** If the Vibration API is unsupported or blocked (e.g. battery saver), the user must still receive immediate visual confirmation (a brief flash) of the quadrant tap.
- **Touch Targets:** The quadrants must cover the entire screen (`100vw/100vh`) using CSS Grid to prevent missed taps and accidental scrolling.
- **Visual Design:** Live mode is an exception to portrait-first. The spatial UI mapping means the screen layout physically mirrors the table space. Follow "The Clubhouse Editorial" style guide.

### Architecture Compliance
- Use Vue 3 `<script setup>` syntax and Pinia for state management.
- Pinia store (`useLiveMatchStore`) MUST define state properties mapping the 4 physical quadrants to the current player assignments (`teamA.attacker`, `teamA.defender`, `teamB.attacker`, `teamB.defender`).
- All styles must use Tailwind CSS v4 and SCSS with the `ch-` prefix for custom styles.
- Components must be placed in `frontend/src/features/match/`. Extract `<LiveQuadrant />` for reusability.
- Ensure no source file exceeds the 500-line strict limit (IP-04).

### Project Structure Notes
- Alignment with unified project structure: features/match for domain-specific components.
- The state should be managed within a dedicated Pinia store (`useLiveMatchStore`).

### References
- PRD FR4: "Player can record a live match in real-time by tapping screen quadrants."
- UX-DR1: Retrospective is portrait, but Live Match mode (Phase 1.5) is landscape. The screen physically lies on the table and maps to the playing field.
- Architecture: AD-06 PWA-First Infrastructure

## Dev Agent Record

### Agent Model Used
Gemini 3.1 Pro (High)

### Debug Log References
- Extracted from `epics.md`, `architecture.md`, and `ux-design-specification.md`.

### Completion Notes List
- Ultimate context engine analysis completed - comprehensive developer guide created

### File List
- `frontend/src/features/match/LiveMatch.vue` (to be created)
- `frontend/src/features/match/LiveMatch.spec.ts` (to be created)

### Review Findings
- [x] [Review][Patch] Missing CSS fallback & robust error handling for fullscreen/landscape mode [`frontend/src/features/match/LiveMatch.vue:15`]
- [x] [Review][Patch] Missing Tailwind v4, SCSS, and design tokens [`frontend/src/features/match/LiveMatch.vue`]
- [x] [Review][Patch] Missing unit tests for quadrant mapping
- [x] [Review][Patch] Incorrect Pinia store naming [`frontend/src/stores/match.ts`]
- [x] [Review][Patch] Incorrect E2E test file location [`frontend/e2e/real-time-scoring-interface.spec.ts`]
- [x] [Review][Patch] crypto.randomUUID() lacks fallback [`frontend/src/stores/match.ts`]
- [x] [Review][Patch] Visual flash truncated on rapid taps [`frontend/src/features/match/LiveQuadrant.vue:24`]
- [x] [Review][Patch] Brittle Playwright TouchEvent hack [`frontend/e2e/real-time-scoring-interface.spec.ts`]
- [x] [Review][Patch] E2E test fails to verify business logic [`frontend/e2e/real-time-scoring-interface.spec.ts`]
- [x] [Review][Patch] Test fixture data scattered across directories
- [x] [Review][Patch] Unrelated skipped tests committed [`frontend/tests/unit/useStatsStore.spec.ts`]
- [x] [Review][Defer] Hardcoded team names in Pinia store [`frontend/src/stores/match.ts:14`] — deferred, pre-existing
- [x] [Review][Defer] Goals can be infinitely added to a finished match [`frontend/src/stores/match.ts:24`] — deferred, pre-existing
