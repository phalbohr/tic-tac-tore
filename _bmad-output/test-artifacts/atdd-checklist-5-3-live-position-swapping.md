---
stepsCompleted:
  - 'step-01-preflight-and-context'
  - 'step-02-generation-mode'
  - 'step-03-test-strategy'
  - 'step-04-generate-tests'
  - 'step-04c-aggregate'
  - 'step-05-validate-and-complete'
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-21T14:39:50+02:00'
storyId: '5.3'
storyKey: '5-3-live-position-swapping'
storyFile: '_bmad-output/implementation-artifacts/5-3-live-position-swapping.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-5-3-live-position-swapping.md'
generatedTestFiles:
  - 'frontend/tests/unit/liveMatch.spec.ts'
  - 'frontend/e2e/real-time-scoring-interface.spec.ts'
inputDocuments:
  - '_bmad-output/implementation-artifacts/5-3-live-position-swapping.md'
  - '_bmad/tea/config.yaml'
---

# ATDD Checklist: Story 5.3 — Live Position Swapping

## 🔴 TDD Red Phase Summary

- **TDD Phase**: RED (All test scaffolds generated with `.skip()` and verified ready for incremental activation)
- **Unit Tests (Vitest)**: 5 tests in `frontend/tests/unit/liveMatch.spec.ts`
- **E2E Tests (Playwright)**: 5 tests in `frontend/e2e/real-time-scoring-interface.spec.ts`
- **Total Red-Phase Tests**: 10 tests

---

## 📋 Acceptance Criteria Traceability & Scaffolds

### AC 1: Team A Position Swapping & Live State Update
- [ ] Store implements `swapPositions('teamA')` inverting attacker and defender
- [ ] UI immediately updates Team A quadrant labels upon swap
- [ ] Subsequent goals scored by Team A are attributed to the newly assigned attacker/defender
- [ ] Historical goals in timeline retain their original player and role snapshot
- **Test Scaffolds**:
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P0] swapPositions(teamA) correctly inverts attacker and defender for Team A`
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P0] subsequent goals scored after swapPositions are attributed to newly assigned player`
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P1] past goals in timeline retain original player snapshot after swapPositions`
  - `frontend/e2e/real-time-scoring-interface.spec.ts` → `[Story 5.3] [P0] tapping Team A swap button updates quadrant labels and attributes subsequent goals to new attacker`

### AC 2: Team B Position Swapping & Live State Update
- [ ] Store implements `swapPositions('teamB')` inverting attacker and defender
- [ ] UI immediately updates Team B quadrant labels upon swap
- [ ] Subsequent goals scored by Team B are attributed to the newly assigned attacker/defender
- [ ] Historical goals in timeline retain their original player and role snapshot
- **Test Scaffolds**:
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P0] swapPositions(teamB) correctly inverts attacker and defender for Team B`
  - `frontend/tests/unit/liveMatch.spec.ts` → `[P1] resolves player names across both teams correctly after swapping positions`
  - `frontend/e2e/real-time-scoring-interface.spec.ts` → `[Story 5.3] [P0] tapping Team B swap button updates quadrant labels and attributes subsequent goals to new attacker`

### AC 3: Grid Layout Contract & Centered Swap Buttons
- [ ] Match grid layout in landscape places Team B on the TOP row (`tl`, `tr`) and Team A on the BOTTOM row (`bl`, `br`)
- [ ] Exactly two swap buttons rendered: Team B centered between Team B quadrants (top row), Team A centered between Team A quadrants (bottom row)
- [ ] Swap buttons NOT placed on outer screen edges
- [ ] Minimum 56x56dp touch target met (`min-w-[56px] min-h-[56px]` / `w-14 h-14`)
- [ ] Explicit accessibility labels (`aria-label="Swap Team A Positions"`, `aria-label="Swap Team B Positions"`) and test selectors (`data-testid="swap-team-a-btn"`, `data-testid="swap-team-b-btn"`)
- **Test Scaffolds**:
  - `frontend/e2e/real-time-scoring-interface.spec.ts` → `[Story 5.3] [P0] match grid renders Team B on top row and Team A on bottom row with centered swap buttons meeting 56x56dp touch target`

### AC 4: Event Propagation Isolation & Accidental Goal Prevention
- [ ] Touch/pointer event propagation stopped on swap button (`@pointerdown.stop` / `@click.stop`)
- [ ] No accidental goal registered on underlying screen quadrants when tapping swap buttons
- **Test Scaffolds**:
  - `frontend/e2e/real-time-scoring-interface.spec.ts` → `[Story 5.3] [P1] tapping swap button does not trigger accidental goal registration on underlying quadrants`
  - `frontend/e2e/real-time-scoring-interface.spec.ts` → `[Story 5.3] [P1] timeline preserves original player names for past goals while showing new player names for goals scored after swap`

---

## 🛠️ Implementation Guidance for `bmad-dev-story`

During story implementation:

1. **State Store (`frontend/src/stores/liveMatch.ts`)**:
   - Implement `swapPositions(team: 'teamA' | 'teamB'): void`.
   - Ensure `goals` snapshot behavior retains previous records.
   - Unskip and activate unit tests in `frontend/tests/unit/liveMatch.spec.ts`.
   - Verify unit tests turn green.

2. **Component UI (`frontend/src/features/match/LiveMatch.vue`)**:
   - Correct grid layout: Team B on Top Row (`tl`, `tr`), Team A on Bottom Row (`bl`, `br`).
   - Add centered swap buttons with minimum 56x56dp size (`w-14 h-14` / `min-w-[56px] min-h-[56px]`), `z-20`, `@pointerdown.stop`, `@click.stop`, aria labels, and test IDs.
   - Unskip and activate E2E tests in `frontend/e2e/real-time-scoring-interface.spec.ts`.
   - Verify E2E tests turn green.

---

## 🚀 Execution Report

- **Execution Mode**: AI Generation (Sequential)
- **Status**: Red-Phase Acceptance Test Scaffolding Complete
- **Next Workflow**: `bmad-dev-story` (`/bmad-agent-dev:dev 5-3`)
