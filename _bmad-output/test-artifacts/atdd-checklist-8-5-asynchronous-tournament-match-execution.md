---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-09-02T23:05:00+02:00'
workflowType: 'testarch-atdd'
storyId: '8.5'
storyKey: '8-5-asynchronous-tournament-match-execution'
storyFile: '_bmad-output/implementation-artifacts/8-5-asynchronous-tournament-match-execution.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-8-5-asynchronous-tournament-match-execution.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentMatchServiceTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentMatchEventListenerTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentMatchRepositoryATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentControllerATDDTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentMatchCard.spec.ts'
  - 'frontend/e2e/tests/api/tournament-async-execution.spec.ts'
  - 'frontend/e2e/tournament-async-execution.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/8-5-asynchronous-tournament-match-execution.md'
  - '.agent/skills/bmad-testarch-atdd/resources/tea-index.csv'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 8.5

## Story Context
- **Story Key:** `8-5-asynchronous-tournament-match-execution`
- **Story ID:** `8.5`
- **Title:** Story 8.5: Asynchronous Tournament Match Execution
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/8-5-asynchronous-tournament-match-execution.md`

## Acceptance Criteria Summary
1. **AC 1 (Out-of-Order Execution in Championship / 2v2):** In active tournaments (`status = IN_PROGRESS`) with format `CHAMPIONSHIP` or mode `TWO_VS_TWO_RANDOM_PAIRINGS`, all generated matches involving the player across all rounds are visible and eligible to be played in any order without waiting for preceding rounds to conclude (`FR44`).
2. **AC 2 (Feeder Resolution in Cup/Knockout):** In active `CUP` tournaments, when both feeder matches for a bracket slot conclude and both participants are resolved, the match status transitions from `PENDING` to `READY` and can be started immediately, regardless of whether other matches in the current or previous rounds are still pending (`FR44`).
3. **AC 3 (Match Start Validation & Concurrency Control):** Starting a match via `POST /api/v1/tournaments/{tournamentId}/matches/{matchId}/start`:
   - Verifies tournament status is `IN_PROGRESS`.
   - Verifies match status is `READY` or `PENDING` with non-null participants.
   - Verifies NO participant ($P_1, P_2, P_1\text{Partner}, P_2\text{Partner}$) is in another active match (`status = IN_PROGRESS`).
   - If any participant is busy, returns `409 Conflict` (`ParticipantBusyException`).
   - On success, transitions status to `IN_PROGRESS` with optimistic locking (`@Version`), and emits `TournamentMatchStartedEvent`.
4. **AC 4 (Dynamic UI Availability Indicators & Filtering):**
   - Matches display "Start Match" button when idle and current user is participant.
   - "Opponent Busy" chip when any participant is currently playing another active match.
   - "LIVE" badge for `IN_PROGRESS` matches and "DONE" badge for concluded matches.
   - Filter chips: "All Rounds", "My Matches", "Available to Play".
   - Clicking "Start Match" invokes start endpoint and routes to match entry flow with prefilled metadata and locked rules (`FR45`).
5. **AC 5 (Match Cancellation & Release):** When an `IN_PROGRESS` match is cancelled/abandoned before submission via `POST /api/v1/tournaments/{tournamentId}/matches/{matchId}/cancel`, `TournamentMatch.status` reverts back to `READY` (or `PENDING`), releasing all participants so they can play other matches.
6. **AC 6 (Match Completion, Winner Advancement & Standings):** Upon receiving `MatchConfirmedEvent`:
   - Links `Match` to `TournamentMatch`.
   - Transitions `TournamentMatch.status` to `COMPLETED`.
   - Sets `winner` to winning `TournamentRegistration`.
   - In `CUP` format: advances winner to linked `nextMatch` (populating `participant1` or `participant2`); if both slots populated, transitions `nextMatch.status` to `READY`.
   - Recalculates tournament standings via `TournamentStandingsService` (`FR46`).

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, well-defined domain state transitions, concurrency rules, REST contracts, and architectural patterns aligned with `code-1-guide` and `code-2-test`.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Out-of-Order Execution | E2E (Playwright) / Service | `tournament-async-execution.spec.ts`, `TournamentMatchServiceTest.java` | P0 | 1. Round 2 match started while Round 1 is pending<br>2. All rounds browsable |
| **AC 2** | Cup Feeder Resolution & Auto-Ready | Unit & Integration | `TournamentMatchEventListenerTest.java` | P0 | 1. Feeder match completion advances winner to `nextMatch`<br>2. When both slots resolved, `nextMatch` transitions to `READY`<br>3. Partial slot resolution keeps `PENDING` |
| **AC 3** | Match Start & Concurrency Control | Unit / Service & WebMvc Slice | `TournamentMatchServiceTest.java`, `TournamentControllerATDDTest.java` | P0 | 1. Successful start sets `IN_PROGRESS` and emits event<br>2. 409 Conflict when $P_1$ or $P_2$ is busy<br>3. 409 Conflict when partner is busy (2v2)<br>4. Rejection if tournament not `IN_PROGRESS`<br>5. Authorization check (only participant/referee) |
| **AC 4** | UI Dynamic Indicators & Filters | Component (Vitest) & E2E (Playwright) | `TournamentMatchCard.spec.ts`, `tournament-async-execution.spec.ts` | P1 | 1. "Start Match" button when playable<br>2. "Opponent Busy" chip when participant busy<br>3. "LIVE" badge for `IN_PROGRESS`<br>4. Navigation to match entry flow |
| **AC 5** | Match Cancellation & Participant Release | Service & WebMvc Slice | `TournamentMatchServiceTest.java`, `TournamentControllerATDDTest.java` | P1 | 1. Revert `IN_PROGRESS` to `READY`<br>2. Release participants to start other matches<br>3. Publish `TournamentMatchCancelledEvent` |
| **AC 6** | Match Completion, Linking & Standings | Listener / Domain | `TournamentMatchEventListenerTest.java`, `TournamentMatchRepositoryATDDTest.java` | P0 | 1. Link `Match` entity to `TournamentMatch`<br>2. Status `COMPLETED` and set `winner`<br>3. Advance winner in Cup bracket<br>4. Standings recalculated |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend Service Unit Tests:** [`TournamentMatchServiceTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentMatchServiceTest.java)
- **Backend Event Listener Tests:** [`TournamentMatchEventListenerTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentMatchEventListenerTest.java)
- **Backend Repository Query Tests:** [`TournamentMatchRepositoryATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentMatchRepositoryATDDTest.java)
- **Backend Controller WebMvc Slice Tests:** [`TournamentControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentControllerATDDTest.java)
- **Frontend Component Tests (Vitest):** [`TournamentMatchCard.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-5/TournamentMatchCard.spec.ts)
- **Frontend API Tests (Playwright):** [`frontend/e2e/tests/api/tournament-async-execution.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/api/tournament-async-execution.spec.ts) (marked with `test.skip()`)
- **Frontend E2E Tests (Playwright):** [`frontend/e2e/tournament-async-execution.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tournament-async-execution.spec.ts) (marked with `test.skip()`)

## Next Steps (Task-by-Task Activation)

During implementation of Story 8.5 in `bmad-dev-story`:
1. **Task 1: Backend Domain Model & Repository Enhancements (AC1, AC3)**
   - Add query methods to `TournamentMatchRepository.java`:
     - `findByMatchId(UUID matchId)`
     - `findActiveMatchesForParticipants(...)`
   - Move and activate `TournamentMatchRepositoryATDDTest.java` into `src/test/java/com/tictactore/repository/`.
   - Verify repository tests turn GREEN.
2. **Task 2: Service Layer — TournamentMatchService & Concurrency Control (AC1, AC2, AC3, AC5)**
   - Create interface `TournamentMatchService.java` and implementation `TournamentMatchServiceImpl.java`.
   - Create `ParticipantBusyException.java` (extends `TournamentConflictException`).
   - Create `TournamentMatchStartedEvent.java` and `TournamentMatchCancelledEvent.java`.
   - Move and activate `TournamentMatchServiceTest.java` into `src/test/java/com/tictactore/service/tournament/`.
   - Verify unit tests turn GREEN.
3. **Task 3: Match Completion Listener & Knockout Winner Advancement (AC2, AC6)**
   - Create `TournamentMatchEventListener.java` handling `MatchConfirmedEvent`.
   - Implement winner advancement for CUP format and standings update call.
   - Move and activate `TournamentMatchEventListenerTest.java` into `src/test/java/com/tictactore/listener/`.
   - Verify listener tests turn GREEN.
4. **Task 4: API Endpoints & DTO Updates (AC3, AC4, AC5)**
   - Update `TournamentMatchResponse.java` (`isAvailable`, `isOpponentBusy`, `busyParticipantNicknames`).
   - Update `TournamentMatchQueryServiceImpl.java` to compute busy metadata.
   - Add start and cancel endpoints to `TournamentController.java`.
   - Move and activate `TournamentControllerATDDTest.java` into `src/test/java/com/tictactore/controller/`.
   - Verify controller slice tests turn GREEN.
5. **Task 5: Frontend Store, Components & Match Entry Flow Integration (AC4, AC5)**
   - Update `tournamentBracketService.ts`, `tournamentService.ts`, and `tournamentBracketStore.ts`.
   - Update `TournamentMatchCard.vue` and `TournamentSchedule.vue`.
   - Add translation strings to `en.json` and `de.json`.
   - Move and activate `TournamentMatchCard.spec.ts` into `frontend/src/features/tournament/components/__tests__/`.
   - Unskip and verify `tournament-async-execution.spec.ts` in Playwright.
6. **Task 6: Verification & Full CI Run**
   - Execute `./scripts/ci-local.sh` and ensure 100% pass rate.
