---
baseline_commit: af04442b1fbcbee23f847a151ac68ad0739e0b12
status: review
---

# Story 8.8: Tournament Confirmation Deadline

## Story

As a tournament organizer and participant,
I want the system to enforce a 48-hour confirmation deadline on tournament matches,
so that unconfirmed matches do not block tournament progress, and unresponsive participants automatically receive a technical defeat.

## Acceptance Criteria

1. **Given** an active tournament (`Tournament.status = IN_PROGRESS`) with an in-progress tournament match (`TournamentMatch.status = IN_PROGRESS`) linked to a core match (`Match.status = PENDING_APPROVAL` or `PARTIALLY_CONFIRMED`)
   **When** the tournament confirmation deadline expires (i.e. `Match.createdAt` is older than the configured threshold, default 48 hours) without opponent confirmation or rejection
   **Then** the system automatically transitions the core match to `CONFIRMED` via domain method `Match.autoConfirmBySystem()`, setting `confirmedAt = Instant.now()` and clearing `cooldownExpiresAt` (`FR18`).

2. **Given** an expired tournament match being auto-confirmed by the system
   **When** the match is persisted via `MatchOperation.saveMatch(match)`
   **Then** a `MatchConfirmedEvent` is published with the match ID and participant IDs.

3. **Given** a `MatchConfirmedEvent` published for an auto-confirmed tournament match
   **When** `TournamentMatchEventListener.handleMatchConfirmed()` receives the event
   **Then** `TournamentMatchService.completeMatch()` is invoked:
   - Sets `TournamentMatch.status` to `COMPLETED`.
   - Computes the winner registration via `determineWinner()` based on the recorded `match.getGames()` scores (upholding the submitted result and awarding the win to the reporting party while giving a technical defeat to the unresponsive opponent).
   - For `CUP` tournaments: automatically advances the winner to the next bracket round (`nextMatch`).
   - For `CHAMPIONSHIP` or `TWO_VS_TWO_RANDOM_PAIRINGS`: checks if all tournament matches are complete, and if so, marks the tournament `COMPLETED` and publishes `TournamentCompletedEvent` (`FR26`, `FR46`).

4. **Given** the periodic confirmation deadline background job in `TournamentScheduler`
   **When** the scheduler executes (`checkConfirmationDeadlines`)
   **Then** it invokes `TournamentConfirmationDeadlineService.processExpiredConfirmationDeadlines()`:
   - Queries `TournamentMatchRepository.findExpiredUnconfirmedMatches(...)` for eligible matches.
   - Processes each expired match in an isolated error boundary (try-catch) so an error on a single match (e.g. concurrent opponent action or optimistic lock failure) does not abort the processing of other matches.
   - Logs structured audit information containing `matchId`, `tournamentMatchId`, `tournamentId`, and unresponsive participant IDs (`FR18`).

5. **Given** a tournament match whose confirmation window has NOT yet reached the 48-hour deadline, or whose tournament is no longer `IN_PROGRESS`
   **When** the confirmation deadline job runs
   **Then** the match is ignored and remains in its current pending status.

## Tasks / Subtasks

- [x] Task 1: Domain Entity & Operation Method for System Auto-Confirmation (AC: 1, 2)
  - [x] Update `src/main/java/com/tictactore/model/Match.java`:
    - Add domain method `public void autoConfirmBySystem()`:
      - Validate `this.status` is `STATUS_PENDING_APPROVAL` or `STATUS_PARTIALLY_CONFIRMED`; throw `InvalidMatchStateException` otherwise.
      - Set `this.status = STATUS_CONFIRMED`.
      - Set `this.confirmedAt = Instant.now()`.
      - Set `this.cooldownExpiresAt = null`.
      - Adhere to *Tell, Don't Ask* (`code-1-guide`).
  - [x] Unit tests in `src/test/java/com/tictactore/model/MatchTest.java`:
    - Verify `autoConfirmBySystem()` transitions `PENDING_APPROVAL` to `CONFIRMED`.
    - Verify `autoConfirmBySystem()` transitions `PARTIALLY_CONFIRMED` to `CONFIRMED`.
    - Verify exception thrown when status is `CONFIRMED` or `REJECTED`.

- [x] Task 2: Repository Query for Expired Tournament Matches (AC: 1, 4, 5)
  - [x] Update `src/main/java/com/tictactore/repository/TournamentMatchRepository.java`:
    - Add query method:
      ```java
      @Query("SELECT tm FROM TournamentMatch tm " +
             "JOIN tm.match m " +
             "JOIN tm.tournament t " +
             "WHERE tm.status = :matchStatus " +
             "AND t.status = :tournamentStatus " +
             "AND (m.status = :pendingStatus OR m.status = :partialStatus) " +
             "AND m.createdAt <= :deadline")
      List<TournamentMatch> findExpiredUnconfirmedMatches(
              @Param("tournamentStatus") TournamentStatus tournamentStatus,
              @Param("matchStatus") TournamentMatchStatus matchStatus,
              @Param("pendingStatus") String pendingStatus,
              @Param("partialStatus") String partialStatus,
              @Param("deadline") Instant deadline
      );
      ```
    - Note: `Match.status` is a `String`, while `TournamentStatus` and `TournamentMatchStatus` are enums.
  - [x] Repository tests in `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java` (`@DataJpaTest`):
    - Verify matching of matches older than deadline.
    - Verify exclusion of matches newer than deadline.
    - Verify exclusion of completed/cancelled matches or tournaments.

- [x] Task 3: Tournament Confirmation Deadline Service (AC: 1, 2, 4)
  - [x] Create interface `src/main/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineService.java`:
    - `int processExpiredConfirmationDeadlines();`
  - [x] Create implementation `src/main/java/com/tictactore/service/tournament/impl/TournamentConfirmationDeadlineServiceImpl.java`:
    - Inject `TournamentMatchRepository`, `MatchOperation`, and `@Value("${app.tournament.confirmation-deadline-hours:48}") int deadlineHours`.
    - Annotate `@Service`, `@RequiredArgsConstructor`, `@Transactional`.
    - Calculate deadline timestamp: `Instant.now().minus(Duration.ofHours(deadlineHours))`.
    - Query `findExpiredUnconfirmedMatches(...)`.
    - Iterate over expired matches with per-item try-catch block:
      - Call `coreMatch.autoConfirmBySystem()`.
      - Persist via `matchOperation.saveMatch(coreMatch)` (which automatically publishes `MatchConfirmedEvent`).
      - Log structured audit trail at INFO level: match ID, tournament match ID, tournament ID, and deadline hours.
      - Increment processed counter.
      - Catch `OptimisticLockException`, `InvalidMatchStateException`, and generic `Exception` per match, logging warning without aborting the batch.
    - Return processed count.
  - [x] Unit tests in `src/test/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineServiceTest.java`:
    - Test successful auto-confirmation and event triggering.
    - Test empty list returns 0.
    - Test error isolation when one match throws an exception.

- [x] Task 4: Tournament Scheduler Integration & Configuration (AC: 4)
  - [x] Update `src/main/java/com/tictactore/scheduler/TournamentScheduler.java`:
    - Inject `TournamentConfirmationDeadlineService`.
    - Add scheduled task:
      ```java
      @Scheduled(fixedDelayString = "${app.tournament.confirmation-scheduler-interval-ms:60000}")
      public void checkConfirmationDeadlines() {
          try {
              int processed = tournamentConfirmationDeadlineService.processExpiredConfirmationDeadlines();
              if (processed > 0) {
                  log.info("Processed {} expired tournament match confirmations", processed);
              }
          } catch (Exception e) {
              log.error("Failed to process expired tournament match confirmations", e);
          }
      }
      ```
  - [x] Update `src/main/resources/application.properties`:
    - Add `app.tournament.confirmation-deadline-hours=48`
    - Add `app.tournament.confirmation-scheduler-interval-ms=60000`
  - [x] Unit tests in `src/test/java/com/tictactore/scheduler/TournamentSchedulerTest.java`:
    - Verify `checkConfirmationDeadlines()` invokes `processExpiredConfirmationDeadlines()`.
    - Verify exceptions from service are caught and logged without propagating.

- [x] Task 5: Component & Integration Testing (AC: 1, 2, 3, 4, 5)
  - [x] Create `src/test/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineIT.java` (`@SpringBootTest`):
    - Set up an active tournament with participants and an in-progress tournament match.
    - Attach a `Match` in `PENDING_APPROVAL` with `createdAt` set to 49 hours ago.
    - Invoke `tournamentConfirmationDeadlineService.processExpiredConfirmationDeadlines()`.
    - Assert core `Match.status` is `CONFIRMED`.
    - Assert `TournamentMatch.status` transitioned to `COMPLETED`.
    - Assert `TournamentMatch.winner` is determined based on games recorded.
    - Assert CUP format advances winner to next round or completes tournament if final match.
    - Follow `code-2-test` conventions (AAA pattern, zero section comments, strict Mockito assertions).

## Dev Notes

- **Zero Comments Policy (`code-1-guide`, `code-4-document`)**: Do NOT add explanatory comments, Javadoc, or inline comments to production code. Code must be self-documenting.
- **Tell, Don't Ask (`code-1-guide`)**: Entity state transitions belong inside entity domain methods. `Match.autoConfirmBySystem()` mutates internal state (`status`, `confirmedAt`, `cooldownExpiresAt`) rather than services modifying entity fields directly.
- **Event-Driven Progression**: Do NOT call `tournamentMatchService.completeMatch()` directly in the deadline service. Calling `matchOperation.saveMatch(match)` after auto-confirmation publishes `MatchConfirmedEvent`, which is already handled by `TournamentMatchEventListener.handleMatchConfirmed()`. The listener delegates to `tournamentMatchService.completeMatch()`, which already handles winner determination, Cup winner advancement, and tournament completion check.
- **Single Responsibility Principle (SRP)**: Do not bloat `TournamentMatchServiceImpl.java` (currently ~501 lines). Encapsulate deadline expiration in `TournamentConfirmationDeadlineServiceImpl.java`. Keep `TournamentScheduler.java` as a thin scheduling trigger.
- **Batch Error Isolation**: The scheduler processes background jobs. Any individual match failure (e.g. concurrent opponent action, database lock) must be caught inside the loop and logged; it must not fail the execution of subsequent matches.
- **Technical Defeat Semantics**: Per PRD FR18 and product specifications, a technical defeat for an unresponsive opponent is achieved by auto-confirming the recorded match submission. `determineWinner()` evaluates `match.getGames()` and designates the winning registration accordingly.

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-8-8-tournament-confirmation-deadline.md`
- **Red-phase Test Scaffolds:**
  - `_bmad-output/test-artifacts/atdd-redphase-8-8/MatchTest.java`
  - `_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentMatchRepositoryDeadlineTest.java`
  - `_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentConfirmationDeadlineServiceTest.java`
  - `_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentSchedulerDeadlineTest.java`
  - `_bmad-output/test-artifacts/atdd-redphase-8-8/TournamentConfirmationDeadlineIT.java`

### Project Structure Notes

- `src/main/java/com/tictactore/model/Match.java` (UPDATE)
- `src/main/java/com/tictactore/repository/TournamentMatchRepository.java` (UPDATE)
- `src/main/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineService.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/impl/TournamentConfirmationDeadlineServiceImpl.java` (NEW)
- `src/main/java/com/tictactore/scheduler/TournamentScheduler.java` (UPDATE)
- `src/main/resources/application.properties` (UPDATE)
- `src/test/java/com/tictactore/model/MatchTest.java` (UPDATE/NEW)
- `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java` (UPDATE)
- `src/test/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineServiceTest.java` (NEW)
- `src/test/java/com/tictactore/scheduler/TournamentSchedulerTest.java` (UPDATE/NEW)
- `src/test/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineIT.java` (NEW)

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 8.8]
- [Source: _bmad-output/planning-artifacts/prd.md#FR18]
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md#Autonomous Tournaments]
- [Source: src/main/java/com/tictactore/model/Match.java]
- [Source: src/main/java/com/tictactore/repository/TournamentMatchRepository.java]
- [Source: src/main/java/com/tictactore/service/tournament/impl/TournamentMatchServiceImpl.java]
- [Source: src/main/java/com/tictactore/listener/TournamentMatchEventListener.java]
- [Source: src/main/java/com/tictactore/scheduler/TournamentScheduler.java]

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash (Medium)

### Debug Log References

- Executed targeted unit tests: `MatchTest`, `TournamentMatchRepositoryTest`, `TournamentConfirmationDeadlineServiceTest`, `TournamentSchedulerTest`.
- Executed component & integration test: `TournamentConfirmationDeadlineIT`.
- Ran full local verification suite via `./scripts/ci-local.sh`: 152 passed, 0 failed.

### Completion Notes List

- Implemented `Match.autoConfirmBySystem()` with state validation (*Tell, Don't Ask*).
- Added `findExpiredUnconfirmedMatches` JPQL query in `TournamentMatchRepository`.
- Created `TournamentConfirmationDeadlineService` and `TournamentConfirmationDeadlineServiceImpl` with batch error isolation and structured audit logging.
- Integrated `checkConfirmationDeadlines()` scheduled task into `TournamentScheduler`.
- Added configuration properties to `application.yml` and `application.properties`.
- Validated event-driven chain triggering `TournamentMatchEventListener.handleMatchConfirmed()` to complete match, advance brackets, and complete tournaments.
- Verified all acceptance criteria and test suites (100% passing).

### File List

- `src/main/java/com/tictactore/model/Match.java`
- `src/main/java/com/tictactore/repository/TournamentMatchRepository.java`
- `src/main/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineService.java`
- `src/main/java/com/tictactore/service/tournament/impl/TournamentConfirmationDeadlineServiceImpl.java`
- `src/main/java/com/tictactore/scheduler/TournamentScheduler.java`
- `src/main/resources/application.yml`
- `src/test/resources/application.properties`
- `src/test/java/com/tictactore/model/MatchTest.java`
- `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java`
- `src/test/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineServiceTest.java`
- `src/test/java/com/tictactore/scheduler/TournamentSchedulerTest.java`
- `src/test/java/com/tictactore/service/tournament/TournamentConfirmationDeadlineIT.java`
- `_bmad-output/implementation-artifacts/8-8-tournament-confirmation-deadline.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
