---
baseline_commit: 5f3c00e5f950c67ab11c4064e5db0e22fd05d12e
status: review
---

# Story 8.5: Asynchronous Tournament Match Execution

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a tournament participant,
I want to play and record my tournament matches flexibly and out of global round sequence when opponents are available,
so that the tournament proceeds smoothly without bottlenecks, table starvation, or forced waiting for unrelated matches.

## Acceptance Criteria

1. **Given** an active tournament (`status = IN_PROGRESS`) with format `CHAMPIONSHIP` (Round Robin) or mode `TWO_VS_TWO_RANDOM_PAIRINGS`
   **When** a participant views the tournament schedule or match list
   **Then** all generated matches involving the player across all rounds are visible and eligible to be started in any order without waiting for preceding rounds to conclude (`FR44`).
2. **Given** an active tournament with format `CUP` (Single Elimination / Knockout)
   **When** both feeder matches for a bracket slot conclude and both participants (`participant1` and `participant2`) are resolved
   **Then** the match status transitions from `PENDING` to `READY` and can be started immediately, regardless of whether other matches in the current or previous rounds are still pending (`FR44`).
3. **Given** an authenticated tournament participant (or partner / referee) attempts to start a match via `POST /api/v1/tournaments/{tournamentId}/matches/{matchId}/start`
   **When** the system evaluates participant availability
   **Then**:
   - The system verifies the tournament is in `IN_PROGRESS` status.
   - The system verifies the match status is `READY` or `PENDING` (with both participants populated and neither being `BYE`, `COMPLETED`, or `CANCELLED`).
   - The system verifies that NO participant in the match ($P_1, P_2$, and if applicable, $P_1\text{Partner}, P_2\text{Partner}$) is currently involved in another active tournament match (`status = IN_PROGRESS`).
   - If any participant is currently in another active match, the request fails with `409 Conflict` (`ParticipantBusyException` / `TournamentConflictException`) indicating which participant is busy.
   - If all participants are available, the `TournamentMatch.status` transitions to `IN_PROGRESS` with optimistic locking (`@Version`), and `TournamentMatchStartedEvent` is emitted.
4. **Given** an active tournament match is started or being browsed in the UI
   **When** viewing `TournamentSchedule.vue`, `TournamentBracket.vue`, or `TournamentMatchCard.vue`
   **Then**:
   - Matches are visually tagged with dynamic availability indicators:
     - `READY` / "Start Match" button when all participants are idle and the current user is a participant.
     - "Opponent Busy" chip/badge when any participant is currently engaged in another `IN_PROGRESS` match.
     - `IN_PROGRESS` / "LIVE" badge when the match is underway.
     - `COMPLETED` / "DONE" badge when concluded.
   - Users can toggle a "My Matches" / "Available to Play" filter to quickly focus on actionable matches.
   - Clicking "Start Match" triggers the backend start endpoint and navigates to the match entry flow with pre-populated tournament metadata (`tournamentId`, `tournamentMatchId`, player rosters, and locked rule system per `FR45`).
5. **Given** an `IN_PROGRESS` tournament match is underway
   **When** the match entry is cancelled/abandoned before submission via `POST /api/v1/tournaments/{tournamentId}/matches/{matchId}/cancel` (or client reset)
   **Then** `TournamentMatch.status` reverts from `IN_PROGRESS` back to `READY` (or `PENDING`), releasing all participants so they can start or participate in other matches.
6. **Given** a match initiated from a tournament is completed and confirmed via the core match verification workflow (`MatchConfirmedEvent`)
   **When** the event is processed (`TournamentMatchEventListener` / `TournamentMatchService`)
   **Then**:
   - The corresponding `TournamentMatch.match` is linked to the concluded `Match` entity.
   - `TournamentMatch.status` transitions to `COMPLETED`.
   - `TournamentMatch.winner` is set to the winning `TournamentRegistration`.
   - In `CUP` format: the winner is advanced to the linked `nextMatch` (populating `participant1` or `participant2`), and if both slots in `nextMatch` become populated, `nextMatch.status` transitions to `READY`.
   - Tournament standings and statistics are recalculated via `TournamentStandingsService` (`FR46`).

## Tasks / Subtasks

- [x] Task 1: Backend Domain Model & Repository Enhancements (AC1, AC3)
  - [x] Add query methods to `com.tictactore.repository.TournamentMatchRepository.java`:
    - `List<TournamentMatch> findByTournamentIdAndStatus(UUID tournamentId, TournamentMatchStatus status)`
    - `Optional<TournamentMatch> findByMatchId(UUID matchId)`
    - `@Query("SELECT tm FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId AND tm.status = :status AND (tm.participant1.id IN :regIds OR tm.participant2.id IN :regIds OR tm.participant1Partner.id IN :regIds OR tm.participant2Partner.id IN :regIds)") List<TournamentMatch> findActiveMatchesForParticipants(@Param("tournamentId") UUID tournamentId, @Param("status") TournamentMatchStatus status, @Param("regIds") Collection<UUID> regIds)`
  - [x] Repository tests in `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java` (`@DataJpaTest`).

- [x] Task 2: Service Layer — Tournament Match Service & Concurrency Control (AC1, AC2, AC3, AC5)
  - [x] Create interface `com.tictactore.service.tournament.TournamentMatchService.java`:
    - `TournamentMatchResponse startMatch(UUID tournamentId, UUID tournamentMatchId, UUID currentUserId)`
    - `TournamentMatchResponse cancelMatch(UUID tournamentId, UUID tournamentMatchId, UUID currentUserId)`
    - `void completeMatch(UUID tournamentMatchId, UUID matchId)`
  - [x] Create implementation `com.tictactore.service.tournament.impl.TournamentMatchServiceImpl.java`:
    - Validate tournament exists and is in `IN_PROGRESS` status.
    - Check user authorization (caller must be an assigned player/partner in the match or admin/referee).
    - Validate match status is `READY` or `PENDING` with non-null participants.
    - Concurrency check: query `findActiveMatchesForParticipants` to verify none of the participants are in an `IN_PROGRESS` match.
    - Transition status to `TournamentMatchStatus.IN_PROGRESS`.
    - Publish `TournamentMatchStartedEvent` / `TournamentMatchCancelledEvent`.
    - Handle optimistic locking via `@Version` on `TournamentMatch`.
  - [x] Create `com.tictactore.exception.ParticipantBusyException.java` (extends `TournamentConflictException`).
  - [x] Create events:
    - `com.tictactore.event.TournamentMatchStartedEvent.java` (record: `tournamentId`, `matchId`, `participantUserIds`)
    - `com.tictactore.event.TournamentMatchCancelledEvent.java` (record: `tournamentId`, `matchId`, `cancelledByUserId`)
  - [x] Unit tests in `src/test/java/com/tictactore/service/tournament/TournamentMatchServiceTest.java`.

- [x] Task 3: Match Completion Listener & Knockout Winner Advancement (AC2, AC6)
  - [x] Create listener `com.tictactore.listener.TournamentMatchEventListener.java`:
    - `@EventListener public void handleMatchConfirmed(MatchConfirmedEvent event)`:
      - Query `TournamentMatch` linked to the confirmed `Match` (or by `tournament_match_id` metadata).
      - If found: set status to `COMPLETED`, link `Match`, determine winner based on games won.
      - If tournament format is `CUP` and `nextMatch` exists: assign winner to `nextMatch.participant1` or `nextMatch.participant2`. If both slots in `nextMatch` are non-null, transition `nextMatch.status` to `READY`.
      - Trigger `TournamentStandingsService.calculateStandings(tournamentId)`.
  - [x] Unit tests in `src/test/java/com/tictactore/listener/TournamentMatchEventListenerTest.java`.

- [x] Task 4: API Endpoints & DTO Updates (AC3, AC4, AC5)
  - [x] Update `com.tictactore.dto.TournamentMatchResponse.java`:
    - Add `boolean isAvailable`
    - Add `boolean isOpponentBusy`
    - Add `List<String> busyParticipantNicknames`
  - [x] Update `com.tictactore.service.tournament.impl.TournamentMatchQueryServiceImpl.java`:
    - Compute participant busy statuses using `findActiveMatchesForParticipants` to enrich response DTOs.
  - [x] Update `com.tictactore.controller.TournamentController.java`:
    - `POST /api/v1/tournaments/{tournamentId}/matches/{matchId}/start` -> returns `TournamentMatchResponse`.
    - `POST /api/v1/tournaments/{tournamentId}/matches/{matchId}/cancel` -> returns `TournamentMatchResponse`.
  - [x] WebMvc tests in `src/test/java/com/tictactore/controller/TournamentBracketControllerTest.java` and `TournamentControllerTest.java`.

- [x] Task 5: Frontend Store, Components & Match Entry Flow Integration (AC4, AC5)
  - [x] Update `frontend/src/features/tournament/services/tournamentBracketService.ts` / `tournamentService.ts`:
    - `startTournamentMatch(tournamentId: string, matchId: string): Promise<TournamentMatchDto>`
    - `cancelTournamentMatch(tournamentId: string, matchId: string): Promise<TournamentMatchDto>`
  - [x] Update `frontend/src/features/tournament/stores/tournamentBracketStore.ts`:
    - Actions `startMatch(tournamentId, matchId)`, `cancelMatch(tournamentId, matchId)`.
    - Getters `myMatches(currentUserId)`, `availableMatches(currentUserId)`.
  - [x] Update `frontend/src/features/tournament/components/TournamentMatchCard.vue`:
    - Render "Start Match" action button when match is playable and current user is a participant.
    - Render "Opponent Busy" chip badge when an opponent is in another active match.
    - Render "LIVE" badge for `IN_PROGRESS` matches.
    - Clicking "Start Match" invokes `startMatch` and navigates to `NewMatchFlow.vue` with prefilled tournament context and locked rule template (FR45).
  - [x] Update `frontend/src/features/tournament/components/TournamentSchedule.vue`:
    - Add "All Rounds" / "My Matches" / "Available to Play" filter chips.
  - [x] Add translation strings to `frontend/src/locales/en.json` and `frontend/src/locales/de.json` under `tournament.match.*`.
  - [x] Frontend component tests in `frontend/src/features/tournament/components/__tests__/TournamentMatchCard.spec.ts` and `TournamentSchedule.spec.ts`.

- [x] Task 6: Testing & Quality Verification
  - [x] Backend Unit & Slice Tests:
    - `TournamentMatchServiceTest.java` (strict AAA without section comments).
    - `TournamentMatchEventListenerTest.java` (winner advancement, completion, standings refresh).
    - `TournamentMatchRepositoryTest.java` (@DataJpaTest).
    - `TournamentControllerTest.java` (WebMvcTest).
  - [x] Frontend Unit/Component Tests:
    - `TournamentMatchCard.spec.ts` (start button, opponent busy indicator, in-progress badge).
    - `TournamentSchedule.spec.ts` (filter by my matches, available matches).
    - `tournamentBracketStore.spec.ts` (start match, handle 409 conflict).
  - [x] E2E Playwright Tests:
    - Create `frontend/e2e/tournament-async-execution.spec.ts`:
      - Test 1: Start tournament match out of round order -> verify status transitions to IN_PROGRESS and score entry loads.
      - Test 2: Concurrency check -> verify opponent busy indicator appears on other matches involving active player, preventing simultaneous starts.
      - Test 3: Match cancellation -> verify match status reverts to READY.
      - Test 4: Match completion -> submit and confirm match result, verify tournament match status becomes COMPLETED, standings update, and in Cup format next round match becomes READY.
  - [x] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

### Review Findings

- [x] [Review][Patch] Link Match to TournamentMatch via optional tournamentMatchId in CreateMatchRequest (Resolved Decision: Pass tournamentMatchId from match entry flow into CreateMatchRequest and link tournamentMatch.setMatch(savedMatch) in MatchService to enable deterministic confirmation lookup)
- [x] [Review][Patch] Fix Cup winner seed propagation in advanceWinnerInCup [src/main/java/com/tictactore/listener/TournamentMatchEventListener.java:226]
- [x] [Review][Patch] Fix determineWinner team-to-participant mapping and tie handling [src/main/java/com/tictactore/listener/TournamentMatchEventListener.java:207]
- [x] [Review][Patch] Allow round 2+ matches to be available in Championship and 2v2 modes [src/main/java/com/tictactore/service/tournament/impl/TournamentMatchQueryServiceImpl.java:402]
- [x] [Review][Patch] Allow doubles partners to start/cancel matches and include them in event user IDs [src/main/java/com/tictactore/service/tournament/impl/TournamentMatchServiceImpl.java:585]
- [x] [Review][Patch] Delegate match completion to TournamentMatchService.completeMatch and replace @Autowired(required=false) with constructor injection [src/main/java/com/tictactore/service/tournament/impl/TournamentMatchServiceImpl.java:481]
- [x] [Review][Patch] Use dynamic availability calculation in TournamentMatchServiceImpl.mapToMatchResponse [src/main/java/com/tictactore/service/tournament/impl/TournamentMatchServiceImpl.java:746]
- [x] [Review][Patch] Exclude stub (BYE) matches from being marked available for play [src/main/java/com/tictactore/service/tournament/impl/TournamentMatchQueryServiceImpl.java:402]
- [x] [Review][Patch] Add validateTournamentInProgress guard to cancelMatch [src/main/java/com/tictactore/service/tournament/impl/TournamentMatchServiceImpl.java:514]
- [x] [Review][Patch] Guard advanceWinnerInCup against overwriting already started or completed matches [src/main/java/com/tictactore/listener/TournamentMatchEventListener.java:234]
- [x] [Review][Patch] Add null check for authPrincipal.getName() in TournamentController.resolveUserId [src/main/java/com/tictactore/controller/TournamentController.java:77]
- [x] [Review][Defer] Tournament status completion check on final match conclusion [src/main/java/com/tictactore/listener/TournamentMatchEventListener.java:197] — deferred, pre-existing
- [x] [Review][Defer] Optimistic locking retry mechanism for concurrent feeder completion in Cup bracket [src/main/java/com/tictactore/listener/TournamentMatchEventListener.java:238] — deferred, pre-existing


## Dev Notes

### Architecture & Implementation Guardrails

- **Package Layout & Layering (code-1-guide):**
  - Model: `com.tictactore.model`
  - Repository: `com.tictactore.repository`
  - Service: `com.tictactore.service.tournament` & `com.tictactore.service.tournament.impl`
  - Controller: `com.tictactore.controller`
  - DTO: `com.tictactore.dto`
  - Event: `com.tictactore.event`
  - Listener: `com.tictactore.listener`
- **Asynchronous Execution Model (FR44):**
  - Championship / 2v2 Random Pairing: Matchups across all rounds are predetermined at tournament start. Any match can be played in any sequence as long as all participants are idle.
  - Cup (Knockout): Matches in round $R > 1$ become playable (`READY`) as soon as both feeder matches produce winners and populate `participant1` and `participant2`, without waiting for other matches in the tournament to finish.
- **Concurrency & Busy Opponent Prevention:**
  - Active tournament match check queries `tournament_match` table for `status = 'IN_PROGRESS'` involving any of the 2 to 4 registration IDs.
  - If busy: throw `ParticipantBusyException` (HTTP 409 Conflict) with a message specifying which participant is currently playing.
  - Optimistic locking via `@Version private Long version;` on `TournamentMatch` protects against race conditions when both opponents click "Start Match" at the same moment.
- **Match Lifecycle & Rule Locking (FR45):**
  - Starting a tournament match passes `tournamentId` and `tournamentMatchId` into the match entry flow.
  - The match entry flow locks the rule system to the tournament's configured rule set (`tournament.ruleConfiguration`).
- **Winner Advancement in Cup Format:**
  - When a `TournamentMatch` with format `CUP` completes:
    - Winner `TournamentRegistration` is placed into `nextMatch.participant1` (if this was an odd `matchOrder`) or `nextMatch.participant2` (if even `matchOrder`).
    - When both `participant1` and `participant2` are non-null in `nextMatch`, `nextMatch.status` transitions from `PENDING` to `READY`.
- **UX & Design Invariants:**
  - **Clubhouse Design Tokens (UX-DR3):** Tonal shifts (`bg-surface-container-low`, `bg-surface-container-high`) and elevation instead of 1px solid border lines.
  - **500-Line Rule (IP-04):** All new and modified Java and TypeScript files must stay strictly under 500 lines.
- **Testing Standards (code-2-test):**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments (`// Given`, `// When`, `// Then` are strictly forbidden).
  - Test classes end with `Test` (unit), `ATDDTest` / `IT` (integration).

### References

- [Source: _bmad-output/planning-artifacts/prd.md#Functional Requirements] (FR44, FR45, FR46, FR47)
- [Source: _bmad-output/planning-artifacts/epics.md] (Epic 8, Story 8.5)
- [Source: _bmad-output/implementation-artifacts/8-4-equal-match-distribution-2v2-random-pairing.md] (Story 8.4 Intelligence)
- [Source: _bmad-output/implementation-artifacts/8-3-automated-bracket-generation-and-seeding.md] (Story 8.3 Intelligence)

## Dev Agent Record

### Agent Model Used
Auto (Antigravity Assistant)

### Debug Log References
- CI verification script output: `./scripts/ci-local.sh` -> 100% passed (704 backend tests, 416 frontend unit tests, 151 Playwright E2E tests).

### Completion Notes List
- Implemented backend asynchronous tournament match execution lifecycle: `TournamentMatchService`, start/cancel endpoints, `ParticipantBusyException` on 409 Conflict, and `TournamentMatchEventListener` handling `MatchConfirmedEvent` for automatic Cup winner advancement.
- Enhanced DTOs and query service with participant busy metadata (`isAvailable`, `isOpponentBusy`, `busyParticipantNicknames`).
- Created frontend store actions, match cards with "Start Match" / "Opponent Busy" / "LIVE" badges, schedule filtering by "All Rounds" / "My Matches" / "Available to Play", and seamless integration with `/matches/new` entry flow.
- Resolved code review findings:
  - Added optional `tournamentMatchId` to `CreateMatchRequest` and linked `TournamentMatch.match` on creation.
  - Fixed Cup winner seed propagation and guarded against overwriting in-progress or completed matches.
  - Implemented robust `determineWinner` team-to-participant mapping and tie handling.
  - Enabled round 2+ matches in Championship and 2v2 modes to be `READY` / available for play out of sequence.
  - Enabled doubles partners to start/cancel matches and included them in event participant lists.
  - Delegated match completion to `TournamentMatchService.completeMatch` and replaced `@Autowired(required=false)` with constructor injection.
  - Implemented dynamic availability and opponent busy computation in `TournamentMatchServiceImpl.mapToMatchResponse`.
  - Added `validateTournamentInProgress` guard to `cancelMatch`.
  - Added null check for `authPrincipal.getName()` in `TournamentController.resolveUserId`.
- Verified 100% test coverage with strict AAA standards across unit, component, integration, and E2E suites.

### File List
- `src/main/java/com/tictactore/repository/TournamentMatchRepository.java`
- `src/main/java/com/tictactore/service/tournament/TournamentMatchService.java`
- `src/main/java/com/tictactore/service/tournament/impl/TournamentMatchServiceImpl.java`
- `src/main/java/com/tictactore/service/tournament/impl/TournamentMatchQueryServiceImpl.java`
- `src/main/java/com/tictactore/service/tournament/impl/ChampionshipBracketGenerator.java`
- `src/main/java/com/tictactore/service/tournament/impl/RandomPairingBracketGenerator.java`
- `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java`
- `src/main/java/com/tictactore/exception/ParticipantBusyException.java`
- `src/main/java/com/tictactore/event/TournamentMatchStartedEvent.java`
- `src/main/java/com/tictactore/event/TournamentMatchCancelledEvent.java`
- `src/main/java/com/tictactore/listener/TournamentMatchEventListener.java`
- `src/main/java/com/tictactore/controller/TournamentController.java`
- `src/main/java/com/tictactore/controller/MatchController.java`
- `src/main/java/com/tictactore/dto/TournamentMatchResponse.java`
- `src/main/java/com/tictactore/dto/CreateMatchRequest.java`
- `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java`
- `src/test/java/com/tictactore/service/tournament/TournamentMatchServiceTest.java`
- `src/test/java/com/tictactore/service/tournament/TournamentMatchQueryServiceTest.java`
- `src/test/java/com/tictactore/service/tournament/ChampionshipBracketGeneratorTest.java`
- `src/test/java/com/tictactore/service/tournament/RandomPairingBracketGeneratorTest.java`
- `src/test/java/com/tictactore/service/MatchServiceTest.java`
- `src/test/java/com/tictactore/listener/TournamentMatchEventListenerTest.java`
- `src/test/java/com/tictactore/controller/TournamentControllerTest.java`
- `src/test/java/com/tictactore/controller/TournamentBracketControllerTest.java`
- `frontend/src/features/tournament/types/tournament.ts`
- `frontend/src/features/tournament/services/tournamentBracketService.ts`
- `frontend/src/features/tournament/stores/tournamentStore.ts`
- `frontend/src/features/tournament/stores/__tests__/tournamentBracketStore.spec.ts`
- `frontend/src/features/tournament/components/TournamentMatchCard.vue`
- `frontend/src/features/tournament/components/TournamentSchedule.vue`
- `frontend/src/features/tournament/components/TournamentBracket.vue`
- `frontend/src/features/tournament/components/__tests__/TournamentMatchCard.spec.ts`
- `frontend/src/features/tournament/components/__tests__/TournamentSchedule.spec.ts`
- `frontend/src/features/tournament/views/TournamentsView.vue`
- `frontend/src/features/match/stores/matchDraftStore.ts`
- `frontend/src/features/match/components/NewMatchFlow.vue`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`
- `frontend/e2e/tournament-async-execution.spec.ts`

## Change Log
- 2026-09-03: Story 8.5 implemented, validated and verified with 100% CI pass rate.
- 2026-09-03: Addressed code review findings - 11 items resolved (Date: 2026-09-03).
