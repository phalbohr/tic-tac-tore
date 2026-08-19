---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-08-19T14:52:40+02:00'
storyId: '4.6'
storyKey: '4-6-unified-match-history-my-matches'
storyFile: '_bmad-output/implementation-artifacts/4-6-unified-match-history-my-matches.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-4-6-unified-match-history-my-matches.md'
generatedTestFiles:
  - 'src/test/java/com/tictactore/controller/MatchHistoryATDDTest.java'
  - 'frontend/e2e/match-history.spec.ts'
  - 'frontend/tests/unit/useMatchHistoryStore.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/4-6-unified-match-history-my-matches.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/overview.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/api-request.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 4.6

## Story Context
- **Story Key:** `4-6-unified-match-history-my-matches`
- **Story ID:** `4.6`
- **Title:** Story 4.6: Unified Match History (My Matches)
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/4-6-unified-match-history-my-matches.md`

## Acceptance Criteria Summary
1. **AC 1:** Unified "My Matches" view (`/matches` or `/history`) with tabs for **Confirmed** and **Pending** matches (FR60, UX Flow 7). Pending tab shows badged pending confirmation cards with inline actions (Confirm / Reject) reusing 15s undo timer. Confirmed tab shows paginated chronological matches (`PagedResponse<MatchResponse>`).
2. **AC 2:** Filter controls via thumb-friendly filter chips (`MatchFilterChips.vue`) for all players, specific opponent or partner, match type (`1v1` vs `2v2`), and rule template (`ruleConfigId`). Immediate reactive update with `AbortController` cancellation.
3. **AC 3:** Clubhouse "No-Line" rule compliance (UX-DR3) using `ch-` SCSS classes. Outcome badges (Win/Loss/Draw), scores, avatars (`AvatarBase`/`AvatarInteractive`), date/time, and format tags. Safe rendering of "Retired Player" without PII (AD-04).
4. **AC 4:** Realistic demo match history via `demoDataGenerator.generateDemoMatchHistory()` when < 1 match or demo mode enabled. Tab-specific empty states (Confirmed: `EmptyStateCTA` with `/matches/new` CTA; Pending: "All caught up"; Filtered: "Try removing filters" reset CTA).

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, REST contracts, and established component patterns across Epics 1–4.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Unified view with Confirmed & Pending tabs, paginated history | API / Integration | Backend `MatchController` & `MatchService` | P0 | 1. Return 200 OK with `PagedResponse<MatchResponse>` for authenticated user<br>2. Support `status=CONFIRMED` and `status=PENDING`<br>3. Paginated results with `page` and `size` parameters |
| **AC 1** | Store state management for history tabs & pagination | Unit (Vitest) | `useMatchHistoryStore.ts` | P0 | 1. `fetchConfirmedHistory()` populates store state & pagination<br>2. Tab switching between confirmed and pending |
| **AC 1** | E2E My Matches view & tab navigation | E2E (Playwright) | `frontend/e2e/match-history.spec.ts` | P0 | 1. Open `/matches` with Confirmed tab default<br>2. Switch to Pending tab with badged cards & inline actions |
| **AC 2** | Filter by player, matchType, and ruleConfigId | API / Integration | Backend `MatchController` | P1 | 1. Filter by `playerId` (opponent or partner)<br>2. Filter by `matchType` (`1v1`, `2v2`)<br>3. Filter by `ruleConfigId` |
| **AC 2** | Filter chips reactive update & AbortController | Unit (Vitest) / E2E | `useMatchHistoryStore.ts` & `MatchFilterChips.vue` | P1 | 1. Filter change triggers request with query params<br>2. In-flight requests cancelled via `AbortController` |
| **AC 3** | Clubhouse No-Line styling & Retired Player safety | API / E2E | Backend resolution & `MatchCard.vue` | P1 / P2 | 1. Backend resolves "Retired Player" without PII (AD-04)<br>2. No 1px border lines between cards (UX-DR3) |
| **AC 4** | Tab-specific empty states & Demo mode | Unit (Vitest) / E2E | `MatchHistoryList.vue` & `demoDataGenerator.ts` | P1 / P2 | 1. Confirmed empty state with `/matches/new` CTA<br>2. Pending empty state "All caught up"<br>3. Filtered empty state with reset CTA<br>4. Demo match history generation |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend API Tests:** [`MatchHistoryATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/controller/MatchHistoryATDDTest.java) (`GetMatchHistorySpecs` with 4 test cases)
- **Frontend Unit Tests:** [`useMatchHistoryStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/tests/unit/useMatchHistoryStore.spec.ts) (5 test cases skipped)
- **Frontend E2E Tests:** [`match-history.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/match-history.spec.ts) (5 test scenarios skipped)

## Next Steps (Task-by-Task Activation)

During implementation of Story 4.6 in `dev-story`:
1. Activate backend API tests in `MatchHistoryATDDTest.java` and implement `GET /api/v1/matches/history` endpoint, service, and repository queries.
2. Activate frontend unit tests in `useMatchHistoryStore.spec.ts` and implement Pinia store, service methods, and demo data generator.
3. Activate frontend E2E tests in `match-history.spec.ts` and implement Vue components (`MyMatchesView.vue`, `MatchHistoryList.vue`, `MatchCard.vue`, `MatchFilterChips.vue`).
4. Verify all tests pass (Green Phase) and execute `./scripts/ci-local.sh`.
