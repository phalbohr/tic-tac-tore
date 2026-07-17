---
title: 'Story 2.3: Score Entry & Automatic Completion'
type: 'feature'
created: '2026-07-17T17:34:59Z'
status: 'done'
review_loop_iteration: 0
followup_review_recommended: true
context: []
warnings: []
baseline_revision: 'df7f6ee4a11a0d846e07c72f1dc6bb7a5a0f1c96'
final_revision: '85e3f2d'
---

<intent-contract>

## Intent

**Problem:** After selecting the match type and players, users need a way to enter game scores quickly and easily without manual progression logic, so they can complete the retrospective match entry flow.

**Approach:** Implement a portrait-oriented "kicker-table" style score entry interface using `+1`/`-1` and `+5` steppers. Expand the `MatchDraft` store to track game scores and automatically complete a game when the score limit is reached (and the match when win conditions are met), based on the selected rule system's configuration.

## Boundaries & Constraints

**Always:**
- Strictly follow the "No-Line" rule (UX-DR3) for UI boundaries using background color shifts.
- Optimize for one-handed mobile use in portrait orientation (no horizontal scrolling).
- Visually distinguish the `+5` stepper (larger) from the `+1`/`-1` steppers.
- Hide the `+5` stepper if the rule's `score_limit < 5`.
- End-to-end performance of match entry must remain < 10 seconds.

**Block If:**
- The rule configuration API endpoint format is unknown or not documented enough to fetch `score_limit` and win conditions.

**Never:**
- Never require the user to manually click "End Game" or "End Match" if the rule limits are reached; it must auto-advance.
- Do not use 1px borders (`border`, `divide-y`).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Score Limit Reached | User taps `+1` reaching the `score_limit` for a game | The game automatically completes, and the next game starts (or match finishes if win condition met). | No error expected |
| Match Win Condition | User reaches the score limit, satisfying the match win condition | The match is marked as ready for submission (Story 2.4). | No error expected |
| Score < 5 Rule | The rule system defines a `score_limit < 5` | The `+5` stepper is hidden from the UI. | No error expected |
| Stepper Decrement | User taps `-1` on a score of 0 | Score remains 0 (cannot be negative). | Ignore input gracefully |

</intent-contract>

## Code Map

- `frontend/src/features/match/components/ScoreEntry.vue` -- New component for entering scores using steppers.
- `frontend/src/features/match/components/ScoreStepper.vue` -- Reusable UI component for the +5, +1, -1 buttons.
- `frontend/src/features/match/stores/matchDraftStore.ts` -- Needs to track current scores, game history, and evaluate auto-completion rules.
- `frontend/src/features/match/components/NewMatchFlow.vue` -- Integrate the score entry screen into the flow after player selection.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/features/match/components/ScoreStepper.vue` -- Create reusable stepper component with `+5`, `+1`, `-1` buttons, handling the hidden `+5` state.
- [x] `frontend/src/features/match/components/ScoreEntry.vue` -- Create the score entry view with "kicker-table" layout, adhering to "No-Line" rule. Display past games/match score context above the current game. Format team names properly for 2v2 (combine player names with "&") and safely fallback user name lookup if they aren't in `frequentOpponents`.
- [x] `frontend/src/features/match/stores/matchDraftStore.ts` -- Add state for current games and scores. Add logic to fetch rule limits and automatically complete games/matches when limits are reached. Cap the score at `goalLimit` when incrementing (e.g. `+5`). Fix `gameLimit` vs `winsNeeded` logic for draws/completion. Add error handling for API failures in `loadRuleConfig` and log if falling back to Standard rules.
- [x] `frontend/src/features/match/components/NewMatchFlow.vue` -- Update to transition to `ScoreEntry` after player selection is complete.
- [x] `frontend/src/features/match/stores/matchDraftStore.spec.ts` -- Add unit tests for score incrementing (including capping at limit), decrementing (prevent <0), auto-completion logic, and API error handling.

**Acceptance Criteria:**
- Given a match draft in progress, when the score entry view opens, then the score steppers are presented without 1px borders.
- Given the score limit is `< 5`, when the score entry view opens, then the `+5` stepper is hidden.
- Given score entry, when a player's score reaches the limit, then the game automatically completes.
- Given game completion, when the overall match win conditions are met, then the match automatically advances to the submission state.

## Spec Change Log

- **Trigger:** Review pass (Invisible Past Games, Anonymous Teams in 2v2)
- **Amended:** Added tasks for displaying past games, combining player names for 2v2, handling API loading errors, fixing gameLimit logic, and capping the score.
- **State Avoided:** Lack of context in multi-game matches, meaningless "Team 1" labels in 2v2, broken state on network failure, and stats corruption via +5 over-scoring.
- **KEEP:** The `ScoreStepper.vue` UI implementation matching the No-Line rule, the integration of `ScoreEntry.vue` into `NewMatchFlow.vue`, and the `games` array structure in `matchDraftStore`.

## Review Triage Log

### 2026-07-17 — Review pass
- intent_gap: 0
- bad_spec: 2 (high 1, medium 1, low 0)
- patch: 5 (high 2, medium 2, low 1)
- defer: 4 (high 0, medium 2, low 2)
- reject: 3 (high 1, medium 1, low 1)
- addressed_findings:
  - `[high]` `[bad_spec]` Invisible Past Games: Added task to display past games.
  - `[medium]` `[bad_spec]` Anonymous Teams in 2v2: Added task to combine player names for 2v2.
  - `[high]` `[patch]` loadRuleConfig network failure: Added task to handle API errors.
  - `[high]` `[patch]` gameLimit reached but winsNeeded not met: Added task to fix logic.
  - `[medium]` `[patch]` Uncapped Over-Scoring: Added task to cap score at limit.
  - `[low]` `[patch]` Faulty Current User Naming: Added task to safely fallback.
  - `[low]` `[patch]` Silent Fallback to Standard Rules: Added task to log fallback.

## Design Notes

- **Score Steppers:** Ensure the `+5` button uses a visually distinct/larger size class than `+1` to be instantly found by the eye.
- **Backgrounds:** Use `bg-surface-container-highest` for the stepper buttons, laid over `bg-surface-container-low` for the score area, enforcing the no-line rule.

## Verification

**Commands:**
- `npm run test:unit frontend/src/features/match/stores/matchDraftStore.spec.ts` -- expected: all score entry and auto-completion logic passes.
- `npm run lint` -- expected: 0 errors, no violations of the 500-line rule.

## Auto Run Result

**Status:** done
**Blocking condition:** None

**Summary:** 
Implemented the portrait-oriented "kicker-table" score entry interface per Story 2.3. Created reusable steppers conforming to the No-Line rule, integrated the view into `NewMatchFlow`, and updated `matchDraftStore` to track `games` and enforce `scoreLimit` auto-completions according to the active `RuleConfig`.

**Files Changed:**
- `frontend/src/features/match/components/ScoreEntry.vue` — New score entry UI view.
- `frontend/src/features/match/components/ScoreStepper.vue` — Reusable +5/+1/-1 stepper component.
- `frontend/src/features/match/stores/matchDraftStore.ts` — State logic for scoring, games history, and rule evaluation.
- `frontend/src/features/match/components/NewMatchFlow.vue` — Integrates score entry into the match creation flow.
- `frontend/src/features/match/stores/matchDraftStore.spec.ts` — Extensive unit testing for state boundaries and completion conditions.

**Review Breakdown (Final Pass):**
- Patches applied: 5 (Fixed undefined player crash, locked score incrementing after match completion, avoided duplicate API calls, updated layout alignment, migrated completion check to reactive watcher).
- Items deferred: 4 (Deferred game win undo, Cancel button confirmation, Back button feature, and match finality over-scoring to subsequent PRs).
- Items rejected: 3 (Rejected -5 button logic, undefined API client migration, missing win-by-two logic outside spec scope).

**Verification Performed:**
- `vitest src/features/match/stores/matchDraftStore.spec.ts` passed (10/10).
- `npm run lint` validated code style correctly.

**Residual Risks:**
- Multi-game undo states and final submission flow rely on the subsequent implementation of Story 2.4.

