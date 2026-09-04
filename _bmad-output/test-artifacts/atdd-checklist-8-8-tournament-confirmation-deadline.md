---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-09-04T08:15:00+02:00'
workflowType: 'testarch-atdd'
storyId: '8.8'
storyKey: '8-8-tournament-confirmation-deadline'
storyFile: '_bmad-output/implementation-artifacts/8-8-tournament-confirmation-deadline.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-8-8-tournament-confirmation-deadline.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-8-8/MatchTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentMatchRepositoryDeadlineTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentConfirmationDeadlineServiceTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentSchedulerDeadlineTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentConfirmationDeadlineIT.java'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/8-8-tournament-confirmation-deadline.md'
  - '.agent/skills/bmad-testarch-atdd/resources/tea-index.csv'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 8.8

## Story Context
- **Story Key:** `8-8-tournament-confirmation-deadline`
- **Story ID:** `8.8`
- **Title:** Story 8.8: Tournament Confirmation Deadline
- **Stack Type:** `backend` (Spring Boot Java backend with JUnit 5, Mockito, DataJpaTest, SpringBootTest)
- **Story File:** `_bmad-output/implementation-artifacts/8-8-tournament-confirmation-deadline.md`

## Acceptance Criteria Summary
1. **AC 1 (Domain System Auto-Confirmation):** Active tournament (`status = IN_PROGRESS`) with in-progress tournament match (`status = IN_PROGRESS`) linked to core match (`status = PENDING_APPROVAL` or `PARTIALLY_CONFIRMED`). When deadline expires without opponent action (e.g. `createdAt` > 48h), system automatically transitions match to `CONFIRMED` via domain method `Match.autoConfirmBySystem()`, setting `confirmedAt = Instant.now()` and clearing `cooldownExpiresAt` (`FR18`).
2. **AC 2 (Event Publishing & Persistence):** Persisting auto-confirmed match via `MatchOperation.saveMatch(match)` publishes `MatchConfirmedEvent` with match ID and participant IDs.
3. **AC 3 (Tournament Match Completion & Bracket Advancement):** `TournamentMatchEventListener.handleMatchConfirmed()` receives the event and invokes `TournamentMatchService.completeMatch()`, setting `TournamentMatch.status = COMPLETED`, determining winner based on recorded game scores (technical defeat for unresponsive opponent), advancing winner in CUP format, and checking overall tournament completion for Championship/2v2 (`FR26`, `FR46`).
4. **AC 4 (Periodic Background Job & Error Boundary):** `TournamentScheduler.checkConfirmationDeadlines()` invokes `TournamentConfirmationDeadlineService.processExpiredConfirmationDeadlines()`, queries `TournamentMatchRepository.findExpiredUnconfirmedMatches(...)`, isolates errors per match, and logs structured audit info (`FR18`).
5. **AC 5 (Non-expired Matches & Completed Tournaments Ignored):** Matches within deadline or non-active tournaments remain untouched.

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria and strict backend domain/service/scheduler boundaries following `code-1-guide` and `code-2-test`.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Domain Auto-Confirmation Method | Unit (JUnit 5) | `MatchTest.java` | P0 | 1. Transitions `PENDING_APPROVAL` to `CONFIRMED`, sets `confirmedAt`, clears `cooldownExpiresAt`<br>2. Transitions `PARTIALLY_CONFIRMED` to `CONFIRMED`<br>3. Throws `InvalidMatchStateException` on invalid state |
| **AC 1, 4, 5** | Expired Matches Query | Repository (`@DataJpaTest`) | `TournamentMatchRepositoryDeadlineTest.java` | P0 | 1. Queries expired unconfirmed matches in active tournaments<br>2. Excludes fresh matches within 48h<br>3. Excludes matches in non-active tournaments |
| **AC 1, 2, 4** | Deadline Service Processing & Error Isolation | Unit (`Mockito`) | `TournamentConfirmationDeadlineServiceTest.java` | P0 | 1. Iterates expired matches, calls domain method and `saveMatch`<br>2. Error in one match does not abort batch<br>3. Returns 0 when no matches expired |
| **AC 4** | Tournament Scheduler Periodic Trigger | Unit (`Mockito`) | `TournamentSchedulerDeadlineTest.java` | P1 | 1. Invokes deadline service on scheduler tick<br>2. Catches and logs exceptions without propagating |
| **AC 1, 2, 3, 4, 5** | End-to-End Deadline Expiration & Tournament Progression | Component / Integration (`@SpringBootTest`) | `TournamentConfirmationDeadlineIT.java` | P0 | 1. Expired match processed: core match `CONFIRMED`<br>2. `TournamentMatch` marked `COMPLETED`<br>3. Winner determined and assigned<br>4. CUP tournament completed on final match |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Domain Entity Unit Tests:** [`MatchTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-8/MatchTest.java)
- **Repository Deadline Query Tests:** [`TournamentMatchRepositoryDeadlineTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentMatchRepositoryDeadlineTest.java)
- **Service Unit Tests:** [`TournamentConfirmationDeadlineServiceTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentConfirmationDeadlineServiceTest.java)
- **Scheduler Integration Tests:** [`TournamentSchedulerDeadlineTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentSchedulerDeadlineTest.java)
- **Integration Test:** [`TournamentConfirmationDeadlineIT.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentConfirmationDeadlineIT.java)

## Next Steps (Task-by-Task Activation)

During implementation of Story 8.8 in `bmad-dev-story`:
1. **Task 1: Domain Entity & Operation Method (AC1, AC2)**
   - Add `public void autoConfirmBySystem()` to `src/main/java/com/tictactore/model/Match.java`.
   - Move `MatchTest.java` to `src/test/java/com/tictactore/model/MatchTest.java` and verify green.
2. **Task 2: Repository Query for Expired Tournament Matches (AC1, AC4, AC5)**
   - Add `findExpiredUnconfirmedMatches(...)` query to `src/main/java/com/tictactore/repository/TournamentMatchRepository.java`.
   - Integrate scenarios from `TournamentMatchRepositoryDeadlineTest.java` into `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java`.
3. **Task 3: Tournament Confirmation Deadline Service (AC1, AC2, AC4)**
   - Create `TournamentConfirmationDeadlineService.java` interface and `TournamentConfirmationDeadlineServiceImpl.java`.
   - Move `TournamentConfirmationDeadlineServiceTest.java` to `src/test/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineServiceTest.java`.
4. **Task 4: Tournament Scheduler Integration & Configuration (AC4)**
   - Update `TournamentScheduler.java` with `@Scheduled` method `checkConfirmationDeadlines()`.
   - Update `application.properties` with `app.tournament.confirmation-deadline-hours=48` and `app.tournament.confirmation-scheduler-interval-ms=60000`.
   - Update `TournamentSchedulerTest.java`.
5. **Task 5: Component & Integration Testing (AC1-5)**
   - Move `TournamentConfirmationDeadlineIT.java` to `src/test/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineIT.java` and verify full end-to-end flow.
