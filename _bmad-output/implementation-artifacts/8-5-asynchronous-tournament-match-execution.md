---
baseline_commit: 5f3c00e5f950c67ab11c4064e5db0e22fd05d12e
status: ready-for-dev
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

- [ ] Task 1: Backend Domain Model & Repository Enhancements (AC1, AC3)
  - [ ] Add query methods to `com.tictactore.repository.TournamentMatchRepository.java`:
    - `List<TournamentMatch> findByTournamentIdAndStatus(UUID tournamentId, TournamentMatchStatus status)`
    - `Optional<TournamentMatch> findByMatchId(UUID matchId)`
    - `@Query("SELECT tm FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId AND tm.status = :status AND (tm.participant1.id IN :regIds OR tm.participant2.id IN :regIds OR tm.participant1Partner.id IN :regIds OR tm.participant2Partner.id IN :regIds)") List<TournamentMatch> findActiveMatchesForParticipants(@Param("tournamentId") UUID tournamentId, @Param("status") TournamentMatchStatus status, @Param("regIds") Collection<UUID> regIds)`
  - [ ] Repository tests in `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java` (`@DataJpaTest`).

- [ ] Task 2: Service Layer — Tournament Match Service & Concurrency Control (AC1, AC2, AC3, AC5)
  - [ ] Create interface `com.tictactore.service.tournament.TournamentMatchService.java`:
    - `TournamentMatchResponse startMatch(UUID tournamentId, UUID tournamentMatchId, UUID currentUserId)`
    - `TournamentMatchResponse cancelMatch(UUID tournamentId, UUID tournamentMatchId, UUID currentUserId)`
    - `void completeMatch(UUID tournamentMatchId, UUID matchId)`
  - [ ] Create implementation `com.tictactore.service.tournament.impl.TournamentMatchServiceImpl.java`:
    - Validate tournament exists and is in `IN_PROGRESS` status.
    - Check user authorization (caller must be an assigned player/partner in the match or admin/referee).
    - Validate match status is `READY` or `PENDING` with non-null participants.
    - Concurrency check: query `findActiveMatchesForParticipants` to verify none of the participants are in an `IN_PROGRESS` match.
    - Transition status to `TournamentMatchStatus.IN_PROGRESS`.
    - Publish `TournamentMatchStartedEvent` / `TournamentMatchCancelledEvent`.
    - Handle optimistic locking via `@Version` on `TournamentMatch`.
  - [ ] Create `com.tictactore.exception.ParticipantBusyException.java` (extends `TournamentConflictException`).
  - [ ] Create events:
    - `com.tictactore.event.TournamentMatchStartedEvent.java` (record: `tournamentId`, `matchId`, `participantUserIds`)
    - `com.tictactore.event.TournamentMatchCancelledEvent.java` (record: `tournamentId`, `matchId`, `cancelledByUserId`)
  - [ ] Unit tests in `src/test/java/com/tictactore/service/tournament/TournamentMatchServiceTest.java`.

- [ ] Task 3: Match Completion Listener & Knockout Winner Advancement (AC2, AC6)
  - [ ] Create listener `com.tictactore.listener.TournamentMatchEventListener.java`:
    - `@EventListener public void handleMatchConfirmed(MatchConfirmedEvent event)`:
      - Query `TournamentMatch` linked to the confirmed `Match` (or by `tournament_match_id` metadata).
      - If found: set status to `COMPLETED`, link `Match`, determine winner based on games won.
      - If tournament format is `CUP` and `nextMatch` exists: assign winner to `nextMatch.participant1` or `nextMatch.participant2`. If both slots in `nextMatch` are non-null, transition `nextMatch.status` to `READY`.
      - Trigger `TournamentStandingsService.calculateStandings(tournamentId)`.
  - [ ] Unit tests in `src/test/java/com/tictactore/listener/TournamentMatchEventListenerTest.java`.

- [ ] Task 4: API Endpoints & DTO Updates (AC3, AC4, AC5)
  - [ ] Update `com.tictactore.dto.TournamentMatchResponse.java`:
    - Add `boolean isAvailable`
    - Add `boolean isOpponentBusy`
    - Add `List<String> busyParticipantNicknames`
  - [ ] Update `com.tictactore.service.tournament.impl.TournamentMatchQueryServiceImpl.java`:
    - Compute participant busy statuses using `findActiveMatchesForParticipants` to enrich response DTOs.
  - [ ] Update `com.tictactore.controller.TournamentController.java`:
    - `POST /api/v1/tournaments/{tournamentId}/matches/{matchId}/start` -> returns `TournamentMatchResponse`.
    - `POST /api/v1/tournaments/{tournamentId}/matches/{matchId}/cancel` -> returns `TournamentMatchResponse`.
  - [ ] WebMvc tests in `src/test/java/com/tictactore/controller/TournamentBracketControllerTest.java` and `TournamentControllerTest.java`.

- [ ] Task 5: Frontend Store, Components & Match Entry Flow Integration (AC4, AC5)
  - [ ] Update `frontend/src/features/tournament/services/tournamentBracketService.ts` / `tournamentService.ts`:
    - `startTournamentMatch(tournamentId: string, matchId: string): Promise<TournamentMatchDto>`
    - `cancelTournamentMatch(tournamentId: string, matchId: string): Promise<TournamentMatchDto>`
  - [ ] Update `frontend/src/features/tournament/stores/tournamentBracketStore.ts`:
    - Actions `startMatch(tournamentId, matchId)`, `cancelMatch(tournamentId, matchId)`.
    - Getters `myMatches(currentUserId)`, `availableMatches(currentUserId)`.
  - [ ] Update `frontend/src/features/tournament/components/TournamentMatchCard.vue`:
    - Render "Start Match" action button when match is playable and current user is a participant.
    - Render "Opponent Busy" chip badge when an opponent is in another active match.
    - Render "LIVE" badge for `IN_PROGRESS` matches.
    - Clicking "Start Match" invokes `startMatch` and navigates to `NewMatchFlow.vue` with prefilled tournament context and locked rule template (FR45).
  - [ ] Update `frontend/src/features/tournament/components/TournamentSchedule.vue`:
    - Add "All Rounds" / "My Matches" / "Available to Play" filter chips.
  - [ ] Add translation strings to `frontend/src/locales/en.json` and `frontend/src/locales/de.json` under `tournament.match.*`.
  - [ ] Frontend component tests in `frontend/src/features/tournament/components/__tests__/TournamentMatchCard.spec.ts` and `TournamentSchedule.spec.ts`.

- [ ] Task 6: Testing & Quality Verification
  - [ ] Backend Unit & Slice Tests:
    - `TournamentMatchServiceTest.java` (strict AAA without section comments).
    - `TournamentMatchEventListenerTest.java` (winner advancement, completion, standings refresh).
    - `TournamentMatchRepositoryTest.java` (@DataJpaTest).
    - `TournamentControllerTest.java` (WebMvcTest).
  - [ ] Frontend Unit/Component Tests:
    - `TournamentMatchCard.spec.ts` (start button, opponent busy indicator, in-progress badge).
    - `TournamentSchedule.spec.ts` (filter by my matches, available matches).
    - `tournamentBracketStore.spec.ts` (start match, handle 409 conflict).
  - [ ] E2E Playwright Tests:
    - Create `frontend/e2e/tournament-async-execution.spec.ts`:
      - Test 1: Start tournament match out of round order -> verify status transitions to IN_PROGRESS and score entry loads.
      - Test 2: Concurrency check -> verify opponent busy indicator appears on other matches involving active player, preventing simultaneous starts.
      - Test 3: Match completion -> submit and confirm match result, verify tournament match status becomes COMPLETED, standings update, and in Cup format next round match becomes READY.
  - [ ] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

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
Gemini 3.7 Flash (High)

### Debug Log References
N/A

### Completion Notes List
- Comprehensive developer guidance created for Story 8.5 following validation checklist.
- Defined asynchronous match execution model for Championship, 2v2 Random Pairing, and Cup formats (FR44).
- Specified concurrency validation preventing participants from playing multiple simultaneous matches.
- Detailed match start, cancellation/revert, and completion lifecycle with winner advancement and rule locking (FR45).
- Integrated frontend components, availability indicators ("Opponent Busy", "LIVE", "Start Match"), and test suites with strict AAA compliance.

### File List
N/A

## Change Log
- Initial creation of the story document.
- Validation improvements applied: comprehensive asynchronous execution rules across formats, participant busy concurrency check, TournamentMatchService lifecycle methods, winner advancement in Cup brackets, frontend match card availability indicators, and full test suite specification.
