---
stepsCompleted:
  - 'step-01-preflight-and-context'
  - 'step-02-generation-mode'
  - 'step-03-test-strategy'
  - 'step-04-generate-tests'
  - 'step-04c-aggregate'
  - 'step-05-validate-and-complete'
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-22T19:41:46+02:00'
storyId: '5.4'
storyKey: '5-4-third-party-referee-mode'
storyFile: '_bmad-output/implementation-artifacts/5-4-third-party-referee-mode.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-5-4-third-party-referee-mode.md'
generatedTestFiles:
  - 'frontend/tests/unit/liveMatch.spec.ts'
  - 'frontend/e2e/referee-mode.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/5-4-third-party-referee-mode.md'
  - '_bmad/tea/config.yaml'
---

# ATDD Checklist: Story 5.4 — Third-party Referee Mode

## 🔴 TDD Red Phase Summary

- **TDD Phase**: RED (Test scaffolds generated with `it.skip()` / `test.skip()`)
- **Unit Tests (Vitest)**: 2 test scaffolds in `frontend/tests/unit/liveMatch.spec.ts` (Skipped)
- **E2E Tests (Playwright)**: 6 test scaffolds in `frontend/e2e/referee-mode.spec.ts` (Skipped)
- **Total Tests**: 8 red-phase test scaffolds

---

## 📋 Acceptance Criteria Traceability & Scaffolds

### AC 1: Portrait Referee Mode & Rotation Overlay Suppression
- [ ] Query parameter `?mode=referee` or `?referee=true` activates referee mode in `LiveMatch.vue`
- [ ] UI operates in portrait orientation without displaying the landscape lock/rotation warning overlay
- [ ] Orientation lock requests `'portrait'` or allows native portrait without throwing errors
- **Test Scaffolds**:
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P0] isRefereeMode is false by default and can be toggled via setRefereeMode`
  - `frontend/e2e/referee-mode.spec.ts` → `[Story 5.4] [P0] AC1: ?mode=referee query parameter starts in portrait mode without landscape rotation overlay`

### AC 2: 2x2 Grid Adapted for Table-End View & Header Responsiveness
- [ ] Left column displays Team B (defender at top-left `tl`, attacker at bottom-left `bl`)
- [ ] Right column displays Team A (attacker at top-right `tr`, defender at bottom-right `br`)
- [ ] Each quadrant clearly displays player name and role (`data-testid="quadrant-{role}"`) with padding (`p-6`)
- [ ] Header strip displays `LiveActivityTimeline` with horizontal scroll and `Undo` button (`data-testid="undo-goal-btn"`) with `shrink-0` in narrow viewports (~390px)
- **Test Scaffolds**:
  - `frontend/e2e/referee-mode.spec.ts` → `[Story 5.4] [P0] AC2: renders 2x2 grid representing table viewed from the end (Left: Team B defender/attacker, Right: Team A attacker/defender)`

### AC 3: Touch Quadrant Goal Attribution in Referee Mode
- [ ] Tapping a player's quadrant immediately attributes a goal to that player and role
- [ ] Goal appears in the live activity timeline with player name, role, and relative match time (`MM:SS`)
- [ ] Visual flashing feedback and SSR-safe haptic feedback (`navigator.vibrate?.([50])`) triggered
- **Test Scaffolds**:
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P0] recordGoal, undoLastGoal, and swapPositions operate identically in referee mode`
  - `frontend/e2e/referee-mode.spec.ts` → `[Story 5.4] [P0] AC2 & AC3: tapping quadrants attributes goals to player and role in live timeline`

### AC 4: Goal Undo Functionality in Referee Mode
- [ ] Tapping "Undo" removes the last recorded goal from timeline and score history
- [ ] Undo button becomes disabled when no goals remain in protocol
- **Test Scaffolds**:
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P0] recordGoal, undoLastGoal, and swapPositions operate identically in referee mode`
  - `frontend/e2e/referee-mode.spec.ts` → `[Story 5.4] [P0] AC4: undo button removes last goal and disables when empty in referee mode`

### AC 5: Column-Centered Position Swap Buttons & Touch Targets
- [ ] Team B swap button centered in left column between Team B defender and attacker (`data-testid="swap-team-b-btn"`, `aria-label="Swap Team B Positions"`)
- [ ] Team A swap button centered in right column between Team A attacker and defender (`data-testid="swap-team-a-btn"`, `aria-label="Swap Team A Positions"`)
- [ ] Swap buttons meet minimum touch target >= 56x56dp (`w-14 h-14 min-w-[56px] min-h-[56px]`)
- [ ] Event propagation isolation (`@pointerdown.stop`, `@click.stop`) prevents accidental goal scoring
- [ ] Subsequent goals attributed to newly assigned roles while preserving past timeline snapshots
- **Test Scaffolds**:
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P0] recordGoal, undoLastGoal, and swapPositions operate identically in referee mode`
  - `frontend/e2e/referee-mode.spec.ts` → `[Story 5.4] [P0] AC5: swap buttons positioned centered in respective columns meeting 56x56dp touch targets`
  - `frontend/e2e/referee-mode.spec.ts` → `[Story 5.4] [P1] AC5: tapping swap button updates future goal attribution without accidental goal registration`

---

## 🛠️ Implementation Guidance for `bmad-dev-story`

During story implementation:

1. **State Store (`frontend/src/stores/liveMatch.ts`)**:
   - Add `isRefereeMode` ref (default `false`) and `setRefereeMode(val: boolean)` action.
   - Verify `recordGoal`, `undoLastGoal`, `swapPositions`, `getPlayerName`, and `goalTimeline` work seamlessly.
   - Unskip and activate unit tests in `frontend/tests/unit/liveMatch.spec.ts`.
   - Verify unit tests turn green.

2. **Component UI (`frontend/src/features/match/LiveMatch.vue`)**:
   - Detect route query parameter `?mode=referee` or `?referee=true` and sync with store.
   - Suppress landscape warning overlay in portrait referee mode (`v-if="isMatchStarted && !matchStore.isRefereeMode"`).
   - Adapt 2x2 grid layout for table-end view: Left column (Team B defender `tl`, attacker `bl`), Right column (Team A attacker `tr`, defender `br`).
   - Position swap buttons centered within respective columns (Team B in left column, Team A in right column) with touch target >= 56x56dp and `@pointerdown.stop` / `@click.stop`.
   - Unskip and activate E2E tests in `frontend/e2e/referee-mode.spec.ts`.
   - Verify E2E tests turn green.

---

## 🚀 Execution Report

- **Execution Mode**: AI Test Architecture & Scaffold Generation (ATDD Red Phase)
- **Status**: Red-phase scaffolds generated and verified
- **Next Workflow**: `bmad-agent-dev` / `bmad-dev-story` (Story Implementation)
