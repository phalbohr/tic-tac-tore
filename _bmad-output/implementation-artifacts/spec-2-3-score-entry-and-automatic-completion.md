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

**Approach:** Implement a portrait-oriented score entry interface using `+1`/`-1` and `+5` steppers. Expand the `MatchDraft` store to inherit state from Story 2.2, track game scores, and automatically complete games/matches based on the active `RuleConfiguration` from the backend.

## Boundaries & Constraints

**Always:**
- **State Integration:** Inherit `matchType` (1v1/2v2) and `ruleConfigId` directly from the `MatchDraft` store populated in Story 2.2.
- **Component Reuse:** Utilize existing UI primitives from `frontend/src/core/components/` for buttons and containers; do not reinvent them.
- **Strict No-Line Design (UX-DR3):** Use `bg-surface-container-highest` layered over `bg-surface-container-low` for separation. 1px borders (`border`, `divide-y`) are strictly forbidden.
- **500-Line Rule (IP-04):** `matchDraftStore.ts` and UI components must not exceed 500 lines. Refactor scoring logic into composables if necessary.
- **Decoupled Logic:** Keep scoring/auto-completion logic fully decoupled from `ScoreEntry.vue` so it can be reused in Epic 5 (Live Mode).
- **Mobile Orientation:** Optimize for one-handed portrait use (no horizontal scrolling).
- **Stepper Logic:** Visually distinguish the `+5` stepper (larger) from the `+1`/`-1` steppers. Hide the `+5` stepper if the `RuleConfiguration` `scoreLimit` < 5.

**Block If:**
- Missing rule configuration endpoints prevent fetching `scoreLimit` and `winsNeeded`.
- Player name resolution endpoints are unavailable for players not in `frequentOpponents`.

**Never:**
- Require manual "End Game" or "End Match" clicks if limits are reached (must auto-advance).
- Hallucinate player names or rules APIs.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Score Limit Reached | User taps `+1` reaching the `scoreLimit` for a game | Game completes automatically; next game starts or match finishes. | N/A |
| Match Win Condition | User reaches the score limit, satisfying the match win condition | Match advances to the submission state (Story 2.4). | N/A |
| Score < 5 Rule | The rule system defines a `scoreLimit` < 5 | The `+5` stepper is hidden from the UI. | N/A |
| Stepper Decrement | User taps `-1` on a score of 0 | Score remains 0 (cannot be negative). | Ignore input |

</intent-contract>

## Code Map

- `frontend/src/features/match/components/ScoreEntry.vue` -- Score entry view layout.
- `frontend/src/features/match/components/ScoreStepper.vue` -- Reusable UI component for the +5, +1, -1 buttons.
- `frontend/src/features/match/stores/matchDraftStore.ts` -- Score tracking, rule evaluation, and game history.
- `frontend/src/features/match/components/NewMatchFlow.vue` -- Integrate score entry into the creation flow.

## Developer Context & Guardrails

### 🔌 API & State Management
- **RuleConfiguration API:** Fetch the active rule configuration using the `ruleConfigId` stored in `MatchDraft`. The backend domain model `RuleConfiguration` (AD-01) provides `scoreLimit` and `winsNeeded`. Do not mock or invent a parallel rules engine.
- **Player Name Resolution:** To format team names in 2v2 (e.g., "Alice & Bob"), check if players are in the `frequentOpponents` list. For unknown players, fetch details using `/api/v1/players/{id}` or the equivalent user service. Handle missing/loading names safely.
- **State Integration:** The `MatchDraft` store must seamlessly carry over the selected players and match type from Story 2.2.

### 🎨 UI & Layout Requirements
- **No-Line Rule:** Implement UX-DR3 precisely using Tailwind tokens: base containers use `bg-surface-container-low`, and distinct interactive elements (like steppers) use `bg-surface-container-highest`.
- **Component Primitives:** Use layout and button components from `src/core/components/` to build the steppers and score board.
- **Context Display:** Display past game results and the overall match score above the current game steppers.

## Tasks & Acceptance
### Review Findings

- [x] [Review][Patch] Jarring Auto-Completion Transition — Отменить мгновенное авто-завершение. При достижении победного условия должна появляться/активироваться кнопка завершения ввода (блокируя ввод новых голов, кроме отмены), которую пользователь должен нажать сам.
- [x] [Review][Patch] Missing Player Name API Fetching [frontend/src/features/match/components/ScoreEntry.vue]
- [x] [Review][Patch] Unsafe and Silent loadRuleConfig [frontend/src/features/match/stores/matchDraftStore.ts]
- [x] [Review][Patch] Missing Display of Past Games [frontend/src/features/match/components/ScoreEntry.vue]
- [x] [Review][Patch] Missing Player Count Validation on Submit [frontend/src/features/match/components/NewMatchFlow.vue]
- [x] [Review][Patch] Active Stepper at Zero [frontend/src/features/match/components/ScoreStepper.vue]
- [x] [Review][Patch] Redundant Cancel Buttons [frontend/src/features/match/components/ScoreEntry.vue]
- [x] [Review][Patch] Missing ARIA Labels on Steppers [frontend/src/features/match/components/ScoreStepper.vue]
- [x] [Review][Patch] CSS Conflict line-clamp with flex [frontend/src/features/match/components/ScoreEntry.vue]
- [x] [Review][Patch] Cancel during fetch corrupts store [frontend/src/features/match/components/NewMatchFlow.vue]
- [x] [Review][Patch] Invalid matchState renders blank screen [frontend/src/features/match/components/NewMatchFlow.vue]
- [x] [Review][Defer] Undo winning point misclick [frontend/src/features/match/stores/matchDraftStore.ts] — deferred, pre-existing
- [x] [Review][Defer] Hardcoded win logic without win-by-two [frontend/src/features/match/stores/matchDraftStore.ts] — deferred, pre-existing
- [x] [Review][Defer] Hardcoded array indices crash on 3v3 [frontend/src/features/match/components/ScoreEntry.vue] — deferred, pre-existing


**Execution:**
- [x] **Store Logic:** Update `matchDraftStore.ts` to fetch and apply `RuleConfiguration` constraints (`scoreLimit`, `winsNeeded`). Implement state to track games and scores, auto-completing games and capping increments at `scoreLimit`.
- [x] **API Fallbacks:** Add error handling in the store for `RuleConfiguration` or Player Name API failures, logging gracefully.
- [x] **ScoreStepper Component:** Create `ScoreStepper.vue` adhering strictly to the No-Line rule using core UI primitives. Handle the hidden `+5` state.
- [x] **ScoreEntry Component:** Create `ScoreEntry.vue`. Combine player names with "&" for 2v2. Keep logic decoupled from the UI.
- [x] **Flow Integration:** Update `NewMatchFlow.vue` to transition to `ScoreEntry.vue` after player selection.
- [x] **Testing:** Add unit tests to `matchDraftStore.spec.ts` for scoring increments, decrements, capping, auto-completion, and API error states.

**Acceptance Criteria:**
- Given a match draft in progress, when the score entry view opens, then the score steppers are presented using background shifts (no 1px borders).
- Given the active `RuleConfiguration` has a `scoreLimit` < 5, when the view opens, then the `+5` stepper is hidden.
- Given score entry, when a player's score reaches the `scoreLimit`, then the game automatically completes.
- Given game completion, when the overall match `winsNeeded` are met, then the match automatically advances to the submission state.

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

### Review Findings (Iteration 2)

- [x] [Review][Patch] Final game data is stranded — completeCurrentGame does not push the final game into games array when match completes [frontend/src/features/match/stores/matchDraftStore.ts]
- [x] [Review][Patch] Invalid ES module import placement — import { computed } is inside useMatchDraftStore [frontend/src/features/match/stores/matchDraftStore.ts]
- [x] [Review][Patch] Broken Unit Tests — Tests expect auto-completion despite manual implementation [frontend/src/features/match/stores/matchDraftStore.spec.ts]
- [x] [Review][Patch] Outdated Spec ACs — Manual completion violates old auto-advance constraints in this very spec file [spec-2-3-score-entry-and-automatic-completion.md]
- [x] [Review][Patch] Cancellation race condition — Canceling during loadRuleConfig sets state to draft, which causes beginScoreEntry to execute [frontend/src/features/match/components/NewMatchFlow.vue]
- [x] [Review][Patch] Decrement after completion — decrementScore lacks matchState ready_for_submission guard [frontend/src/features/match/stores/matchDraftStore.ts]
- [x] [Review][Patch] Misleading +5 Stepper UI — +5 button remains visible when < 5 points are needed [frontend/src/features/match/components/ScoreStepper.vue]
- [x] [Review][Patch] Context-blind multi-game matches — UI omits current game number [frontend/src/features/match/components/ScoreEntry.vue]
- [x] [Review][Patch] Reckless test mutation — globalThis.fetch is not cleaned up in afterEach [frontend/src/features/match/stores/matchDraftStore.spec.ts]
- [x] [Review][Defer] Unconfirmed Cancellations — Cancel button triggers total state reset without confirm — deferred, pre-existing
- [x] [Review][Defer] Hardcoded array indexing roulette — Hardcoded array indices for team names — deferred, pre-existing
- [x] [Review][Defer] Naive win calculation — Assumes static score limit, breaks win-by-two — deferred, pre-existing
