---
status: ready-for-dev
---

# Story 8.3: Automated Bracket Generation & Seeding

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a system,
I want to auto-generate the tournament bracket and schedule initial matches when a tournament's registration deadline passes,
so that participants are seeded fairly based on statistical strength and competition matches are immediately available for play.

## Acceptance Criteria

1. **Given** one or more tournaments in `REGISTRATION_OPEN` status where `registrationDeadline <= Instant.now()`
   **When** the periodic tournament starter scheduled job (`TournamentScheduler`) executes
   **Then** it selects each tournament using a pessimistic write lock (`SELECT ... FOR UPDATE`), verifies participant count against `minParticipants`, and initiates either the tournament start or cancellation routine (`FR41`, `FR43`).
2. **Given** a tournament starting with fewer `CONFIRMED` registrations than `minParticipants`
   **When** the tournament start routine executes
   **Then** the tournament status transitions to `CANCELLED`, `TournamentCancelledEvent` is emitted, and push notifications are sent to all registered participants stating the cancellation reason (`FR41`, `FR55`).
3. **Given** a tournament starting with at least `minParticipants` confirmed registrations in mode `ONE_VS_ONE_PERSONAL` or `TWO_VS_TWO_FIXED_TEAMS`
   **When** the seeding routine executes
   **Then** it evaluates participant statistical strength (`TournamentSeedingStrategy`):
   - For 1v1: Player win rate and total confirmed wins from `MatchRepository`.
   - For 2v2 Fixed Teams: Combined average win rate of player and partner.
   - Tie-breaking: Fall back to registration timestamp (`createdAt` ascending), then registration ID.
   - Assigns unique seed rankings (1 to $N$, where 1 is the highest seeded participant) (`FR43`).
4. **Given** a seeded list of participants for a `CUP` (Single Elimination) tournament
   **When** the `CupBracketGenerator` executes
   **Then** it constructs a standard binary elimination tree of size $P$ (the smallest power of 2 $\ge N$):
   - Places seeds in standard tournament bracket positions (e.g. for 8: 1 vs 8, 4 vs 5, 2 vs 7, 3 vs 6).
   - Generates `TournamentMatch` entities for Round 1 to Round $\log_2(P)$ linked via `next_match_id`.
   - Unfilled slots due to $N < P$ are assigned as `BYE`: the match status is set to `BYE`, the opponent is null, the seeded participant is marked as winner, and their registration automatically advances to the linked Round 2 `TournamentMatch` (`FR43`).
5. **Given** a seeded list of participants for a `CHAMPIONSHIP` (Round Robin) tournament
   **When** the `ChampionshipBracketGenerator` executes
   **Then** it generates `TournamentMatch` entities for all rounds using the Berger polygon round-robin scheduling algorithm, with Round 1 matches set to `READY` and subsequent rounds set to `PENDING` (`FR41`, `FR43`).
6. **Given** a tournament has successfully generated its bracket and matches
   **When** the start transaction commits
   **Then** the tournament status is updated to `IN_PROGRESS`, `TournamentStartedEvent` is emitted, and push notifications are sent to all participating players informing them that the tournament has started and their initial matches are ready (`FR43`, `FR55`).
7. **Given** an authenticated user requesting tournament bracket or match data
   **When** they query `GET /api/v1/tournaments/{tournamentId}/bracket` or `GET /api/v1/tournaments/{tournamentId}/matches`
   **Then** the backend returns `200 OK` with the complete bracket structure (`TournamentBracketResponse`), including rounds, match nodes, seeds, participant nicknames/avatars, match status, and scores (`FR43`, `FR46`).
8. **Given** an authenticated user viewing an `IN_PROGRESS` or `COMPLETED` tournament in the frontend
   **When** they view the tournament card or open the tournament details view
   **Then** `TournamentBracket.vue` (for Cup) or `TournamentSchedule.vue` (for Championship) renders the interactive bracket/schedule with Clubhouse styling (`bg-surface-container-low`, no 1px solid borders per `UX-DR3`), displaying participant seeds, avatars, and match status badges (`FR43`, `FR46`).

## Tasks / Subtasks

- [ ] Task 1: Database Migration & JPA Entities (AC1, AC4, AC5)
  - [ ] Create Flyway migration `src/main/resources/db/migration/V20__create_tournament_match_tables.sql`:
    - Add `seed` (INT) and `strength_score` (DOUBLE PRECISION) columns to `tournament_registration` table.
    - Create `tournament_match` table:
      - `id UUID PRIMARY KEY`
      - `tournament_id UUID NOT NULL REFERENCES tournament(id) ON DELETE CASCADE`
      - `match_id UUID REFERENCES match(id) ON DELETE SET NULL`
      - `round INT NOT NULL`
      - `match_order INT NOT NULL`
      - `participant1_id UUID REFERENCES tournament_registration(id) ON DELETE CASCADE`
      - `participant2_id UUID REFERENCES tournament_registration(id) ON DELETE CASCADE`
      - `seed1 INT`
      - `seed2 INT`
      - `status VARCHAR(30) NOT NULL`
      - `winner_id UUID REFERENCES tournament_registration(id) ON DELETE SET NULL`
      - `next_match_id UUID REFERENCES tournament_match(id) ON DELETE SET NULL`
      - `created_at TIMESTAMP WITH TIME ZONE NOT NULL`
      - `updated_at TIMESTAMP WITH TIME ZONE`
      - `version BIGINT NOT NULL DEFAULT 0`
    - Create indexes:
      - `idx_tournament_match_tournament_id ON tournament_match(tournament_id)`
      - `idx_tournament_match_tournament_round ON tournament_match(tournament_id, round)`
      - `idx_tournament_match_status ON tournament_match(status)`
      - `idx_tournament_match_participant1 ON tournament_match(participant1_id)`
      - `idx_tournament_match_participant2 ON tournament_match(participant2_id)`
  - [ ] Create Enum `com.tictactore.model.TournamentMatchStatus.java`:
    - `PENDING`, `READY`, `IN_PROGRESS`, `COMPLETED`, `BYE`, `CANCELLED`
  - [ ] Create Entity `com.tictactore.model.TournamentMatch.java`:
    - Annotations: `@Entity`, `@Table(name = "tournament_match")`, `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor(access = AccessLevel.PRIVATE)`
    - Fields: `UUID id`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tournament_id", nullable = false) Tournament tournament`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "match_id") Match match`, `int round`, `int matchOrder`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "participant1_id") TournamentRegistration participant1`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "participant2_id") TournamentRegistration participant2`, `Integer seed1`, `Integer seed2`, `@Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) TournamentMatchStatus status`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "winner_id") TournamentRegistration winner`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "next_match_id") TournamentMatch nextMatch`, `@CreationTimestamp Instant createdAt`, `@UpdateTimestamp Instant updatedAt`, `@Version private Long version;` (no `@Column`, code-1-guide rule 2).
  - [ ] Update Entity `com.tictactore.model.TournamentRegistration.java`:
    - Add fields: `Integer seed`, `Double strengthScore`.
  - [ ] Create Repository `com.tictactore.repository.TournamentMatchRepository.java`:
    - `List<TournamentMatch> findByTournamentIdOrderByRoundAscMatchOrderAsc(UUID tournamentId)`
    - `List<TournamentMatch> findByTournamentIdAndRoundOrderByMatchOrderAsc(UUID tournamentId, int round)`
    - `List<TournamentMatch> findByTournamentIdAndStatus(UUID tournamentId, TournamentMatchStatus status)`
    - `@Query("SELECT tm FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId AND (tm.participant1.id = :regId OR tm.participant2.id = :regId)") List<TournamentMatch> findByParticipantRegistrationId(UUID tournamentId, UUID regId)`
  - [ ] Update Repository `com.tictactore.repository.TournamentRepository.java`:
    - `@Lock(LockModeType.PESSIMISTIC_WRITE) @Query("SELECT t FROM Tournament t WHERE t.id = :id") Optional<Tournament> findByIdWithLock(UUID id)`
    - `List<Tournament> findByStatusAndRegistrationDeadlineLessThanEqual(TournamentStatus status, Instant deadline)`
  - [ ] Repository tests in `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java` (`@DataJpaTest`).

- [ ] Task 2: Seeding Strategy, Bracket Generators & Lifecycle Service (AC1, AC2, AC3, AC4, AC5, AC6)
  - [ ] Create DTO `com.tictactore.dto.tournament.SeededParticipant.java` record:
    - `TournamentRegistration registration`, `int seed`, `double strengthScore`
  - [ ] Create Seeding Strategy interface & implementations:
    - `com.tictactore.service.tournament.TournamentSeedingStrategy.java`:
      - `List<SeededParticipant> seed(Tournament tournament, List<TournamentRegistration> registrations)`
    - `com.tictactore.service.tournament.impl.StrengthBasedSeedingStrategy.java`:
      - Evaluate strength using player confirmed matches count & win rate from `MatchRepository`.
      - Calculate team strength for 2v2 as average of player and partner.
      - Sort by strength score descending, tie-break by `createdAt` ascending, then `id`.
    - `com.tictactore.service.tournament.impl.RandomSeedingStrategy.java`:
      - Fallback shuffle seeding strategy.
  - [ ] Create Bracket Generator interface & implementations:
    - `com.tictactore.service.tournament.BracketGenerator.java`:
      - `List<TournamentMatch> generateBracket(Tournament tournament, List<SeededParticipant> seededParticipants)`
    - `com.tictactore.service.tournament.impl.CupBracketGenerator.java`:
      - Determine bracket size $P = 2^{\lceil \log_2 N \rceil}$.
      - Place seeds according to standard tournament binary pairing.
      - Generate placeholder tree nodes for subsequent rounds with `next_match_id`.
      - Handle BYEs: mark match as `BYE`, opponent null, winner set to participant 1, advance participant 1 to linked next round match node.
    - `com.tictactore.service.tournament.impl.ChampionshipBracketGenerator.java`:
      - Generate Round Robin pairings using Berger circle algorithm for configured `roundCount`.
      - Set Round 1 matches to `READY`, subsequent rounds to `PENDING`.
  - [ ] Create Lifecycle Service interface & implementation:
    - `com.tictactore.service.tournament.TournamentLifecycleService.java`
    - `com.tictactore.service.tournament.impl.TournamentLifecycleServiceImpl.java`:
      - `@Transactional public TournamentResponse startTournament(UUID tournamentId)`:
        - Lock tournament with `findByIdWithLock`.
        - Validate status is `REGISTRATION_OPEN`.
        - Query confirmed registrations.
        - If count < `minParticipants` -> transition status to `CANCELLED`, save, publish `TournamentCancelledEvent`.
        - If count >= `minParticipants` -> transition status to `IN_PROGRESS`, seed participants, generate bracket, save `TournamentMatch` records, publish `TournamentStartedEvent`.
  - [ ] Create Scheduled Job `com.tictactore.scheduler.TournamentScheduler.java`:
    - `@Component`, `@RequiredArgsConstructor`, `@Slf4j`
    - `@Scheduled(fixedDelayString = "${app.tournament.scheduler-interval-ms:60000}")`
    - Queries open tournaments past registration deadline and triggers `startTournament` for each.
  - [ ] Unit tests in `src/test/java/com/tictactore/service/tournament/StrengthBasedSeedingStrategyTest.java`, `CupBracketGeneratorTest.java`, `ChampionshipBracketGeneratorTest.java`, `TournamentLifecycleServiceTest.java`.

- [ ] Task 3: Domain Events, Push Notifications, Controller & DTOs (AC1, AC2, AC6, AC7)
  - [ ] Create Events in `com.tictactore.event`:
    - `TournamentStartedEvent.java` record (`UUID tournamentId`, `String tournamentName`, `TournamentFormat format`, `TournamentMode mode`, `List<UUID> participantUserIds`, `int totalMatches`)
    - `TournamentCancelledEvent.java` record (`UUID tournamentId`, `String tournamentName`, `String reason`, `List<UUID> participantUserIds`)
  - [ ] Update `com.tictactore.service.PushNotificationService.java` & `impl/PushNotificationServiceImpl.java`:
    - `void sendTournamentStartedNotification(UUID tournamentId, String tournamentName, User recipient)`
    - `void sendTournamentCancelledNotification(UUID tournamentId, String tournamentName, String reason, User recipient)`
  - [ ] Update Event Listener `com.tictactore.listener.TournamentNotificationListener.java`:
    - `@Async @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public void handleTournamentStarted(TournamentStartedEvent event)`
    - `@Async @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public void handleTournamentCancelled(TournamentCancelledEvent event)`
  - [ ] Create DTOs in `com.tictactore.dto`:
    - `TournamentMatchResponse.java` record:
      - `UUID id`, `UUID tournamentId`, `int round`, `int matchOrder`, `UUID matchId`, `TournamentRegistrationResponse participant1`, `TournamentRegistrationResponse participant2`, `Integer seed1`, `Integer seed2`, `TournamentMatchStatus status`, `UUID winnerRegistrationId`, `UUID nextMatchId`, `Instant createdAt`
    - `RoundMatchesResponse.java` record:
      - `int round`, `String roundName`, `List<TournamentMatchResponse> matches`
    - `TournamentBracketResponse.java` record:
      - `UUID tournamentId`, `String tournamentName`, `TournamentFormat format`, `TournamentMode mode`, `TournamentStatus status`, `int totalRounds`, `List<RoundMatchesResponse> rounds`, `List<TournamentRegistrationResponse> seededParticipants`
  - [ ] Update Controller `com.tictactore.controller.TournamentController.java`:
    - `POST /api/v1/tournaments/{id}/start`: `@AuthenticationPrincipal User principal` -> `200 OK` with `TournamentResponse` (manual start trigger).
    - `GET /api/v1/tournaments/{id}/bracket`: returns `200 OK` with `TournamentBracketResponse`.
    - `GET /api/v1/tournaments/{id}/matches`: `@RequestParam(required = false) Integer round` -> returns `200 OK` with `List<TournamentMatchResponse>`.
  - [ ] Controller WebMvcTest in `src/test/java/com/tictactore/controller/TournamentBracketControllerTest.java`.

- [ ] Task 4: Frontend Types, Service, Store, Components & i18n (AC7, AC8)
  - [ ] Update TypeScript types `frontend/src/features/tournament/types/tournament.ts`:
    - `type TournamentMatchStatus = 'PENDING' | 'READY' | 'IN_PROGRESS' | 'COMPLETED' | 'BYE' | 'CANCELLED'`
    - `interface TournamentMatchDto`
    - `interface RoundMatchesDto`
    - `interface TournamentBracketDto`
  - [ ] Create API service `frontend/src/features/tournament/services/tournamentBracketService.ts`:
    - `getTournamentBracket(tournamentId: string): Promise<TournamentBracketDto>`
    - `getTournamentMatches(tournamentId: string, round?: number): Promise<TournamentMatchDto[]>`
    - `startTournament(tournamentId: string): Promise<TournamentDto>`
  - [ ] Update Pinia store `frontend/src/features/tournament/stores/tournamentStore.ts`:
    - State: `brackets: Record<string, TournamentBracketDto>`, `matches: Record<string, TournamentMatchDto[]>`
    - Actions: `fetchBracket(tournamentId)`, `fetchMatches(tournamentId, round?)`, `startTournament(tournamentId)`
  - [ ] Create UI components:
    - `frontend/src/features/tournament/components/TournamentMatchCard.vue`:
      - Participant rows with seed number badge (#1, #2), avatar, nickname, status indicator (BYE, Ready, Score).
    - `frontend/src/features/tournament/components/TournamentBracket.vue`:
      - Multi-round column layout with bracket lines, scrolling support, and responsive scaling.
    - `frontend/src/features/tournament/components/TournamentSchedule.vue`:
      - Round-by-round accordion / tab schedule for Championship format.
  - [ ] Update `frontend/src/features/tournament/views/TournamentsView.vue`:
    - Add "View Bracket" / "Schedule" action button on active or completed tournaments.
    - Render modal or expanded view showing `TournamentBracket.vue` or `TournamentSchedule.vue`.
  - [ ] Add i18n translation keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json` under `tournament.bracket.*` and `tournament.schedule.*` (Round names, BYE, Seed #, Start Tournament, Match Statuses).
  - [ ] Frontend component tests in `frontend/src/features/tournament/components/__tests__/TournamentBracket.spec.ts` and `TournamentMatchCard.spec.ts`.

- [ ] Task 5: Testing & Quality Verification
  - [ ] Backend Unit & Slice Tests:
    - `TournamentLifecycleServiceTest.java` (strict AAA without section comments).
    - `StrengthBasedSeedingStrategyTest.java`.
    - `CupBracketGeneratorTest.java` (powers of 2, non-power of 2 with BYEs, winner propagation).
    - `ChampionshipBracketGeneratorTest.java`.
    - `TournamentSchedulerTest.java`.
    - `TournamentMatchRepositoryTest.java` (@DataJpaTest).
    - `TournamentBracketControllerTest.java` (WebMvcTest).
    - `TournamentNotificationListenerTest.java`.
  - [ ] Frontend Unit/Component Tests:
    - `tournamentStore.spec.ts` (bracket actions).
    - `TournamentBracket.spec.ts`.
    - `TournamentMatchCard.spec.ts`.
  - [ ] E2E Playwright Tests:
    - Create `frontend/e2e/tournament-bracket.spec.ts`:
      - Test 1 (Cup Bracket Generation): Create tournament -> register 6 players -> advance time/trigger start -> verify 8-slot bracket created with 2 BYEs, correct seeds #1 to #6, and Round 1 pairings.
      - Test 2 (Cancellation on Low Capacity): Create tournament with minParticipants=4 -> register 2 players -> start routine runs -> tournament transitions to CANCELLED and participants see cancellation status.
      - Test 3 (Championship Schedule): Create round robin tournament -> register 4 players -> start -> verify 3 rounds of pairings generated.
  - [ ] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **Package Layout & Layering (code-1-guide):**
  - Model: `com.tictactore.model`
  - Repository: `com.tictactore.repository`
  - Service: `com.tictactore.service` & `com.tictactore.service.tournament`
  - Controller: `com.tictactore.controller`
  - Scheduler: `com.tictactore.scheduler`
  - DTO: `com.tictactore.dto`
  - Event: `com.tictactore.event`
  - Listener: `com.tictactore.listener`
- **Database & Concurrency Invariants:**
  - Flyway migration script: `src/main/resources/db/migration/V20__create_tournament_match_tables.sql`.
  - Pessimistic write locking on `Tournament` (`findByIdWithLock`) during start routine prevents race conditions when multiple scheduler nodes execute concurrently.
  - `@Version private Long version;` on `TournamentMatch` entity for optimistic locking (without `@Column`, as per `code-1-guide` rule 2).
  - Foreign keys:
    - `tournament_id REFERENCES tournament(id) ON DELETE CASCADE`
    - `participant1_id REFERENCES tournament_registration(id) ON DELETE CASCADE`
    - `participant2_id REFERENCES tournament_registration(id) ON DELETE CASCADE`
    - `winner_id REFERENCES tournament_registration(id) ON DELETE SET NULL`
    - `next_match_id REFERENCES tournament_match(id) ON DELETE SET NULL`
- **Seeding Calculation & Fallback Rules (FR43):**
  - Evaluates win percentage from confirmed matches: $\text{winRate} = \frac{\text{wins}}{\text{totalMatches}}$.
  - For 1v1: player's individual win percentage; tie-breaker: total wins, then registration `createdAt`.
  - For 2v2 Fixed Teams: average win percentage of player + partner; tie-breaker: combined total wins, then registration `createdAt`.
  - Zero-match participants are seeded below participants with match history, sorted by registration timestamp.
- **Single-Elimination Bracket & BYE Handling:**
  - Size $P = 2^{\lceil \log_2 N \rceil}$. Total rounds $R = \log_2 P$.
  - Binary bracket placement guarantees Seed 1 and Seed 2 cannot meet until the Final, and Seeds 1–4 cannot meet until the Semifinals.
  - BYE matches (where `participant2 == null`):
    - `status = TournamentMatchStatus.BYE`
    - `winner = participant1`
    - Immediately advance `participant1` to the assigned slot in `nextMatch`.
- **Event-Driven Push Notifications (FR55):**
  - `TournamentStartedEvent` and `TournamentCancelledEvent` published via `ApplicationEventPublisher`.
  - `TournamentNotificationListener` handles events with `@Async @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
- **UX & Design Invariants:**
  - **Clubhouse Design Tokens (UX-DR3):** Tonal shifts (`bg-surface-container-low`, `bg-surface-container-high`) and elevation instead of 1px solid border lines.
  - **500-Line Rule (IP-04):** All new and modified files must stay strictly under 500 lines.
- **Testing Standards (code-2-test):**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments (`// Given`, `// When`, `// Then` are strictly forbidden).
  - Integration tests end with `IT` or `ATDDTest` / `Test` for unit tests.

### References

- [Source: _bmad-output/planning-artifacts/prd.md#FR41]
- [Source: _bmad-output/planning-artifacts/prd.md#FR43]
- [Source: _bmad-output/planning-artifacts/prd.md#FR46]
- [Source: _bmad-output/planning-artifacts/prd.md#FR55]
- [Source: _bmad-output/planning-artifacts/epics.md#Story-8.3]
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md#Tournaments]
- [Source: /Users/ppolukhin/.agents/skills/code-1-guide/SKILL.md]
- [Source: /Users/ppolukhin/.agents/skills/code-2-test/SKILL.md]

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash

### Debug Log References

### Completion Notes List

- Comprehensive story context validated and optimized for dev-story execution.
- Configured Flyway migration version `V20__create_tournament_match_tables.sql`.
- Specified `TournamentMatch` entity, `TournamentMatchStatus` enum, and repository with pessimistic locking.
- Defined `TournamentSeedingStrategy` (strength-based and random fallback) and bracket generators (`CupBracketGenerator`, `ChampionshipBracketGenerator`).
- Designed `TournamentLifecycleService`, scheduled job `TournamentScheduler`, domain events, and async push notification listeners.
- Defined REST API endpoints for bracket visualization and manual start.
- Specified Vue 3 / Pinia components (`TournamentBracket.vue`, `TournamentSchedule.vue`, `TournamentMatchCard.vue`) conforming to Clubhouse design tokens.
- Added strict AAA testing plan, Playwright E2E test plan, and `./scripts/ci-local.sh` verification gate.

### File List

- `src/main/resources/db/migration/V20__create_tournament_match_tables.sql` (NEW)
- `src/main/java/com/tictactore/model/TournamentMatchStatus.java` (NEW)
- `src/main/java/com/tictactore/model/TournamentMatch.java` (NEW)
- `src/main/java/com/tictactore/model/TournamentRegistration.java` (UPDATE)
- `src/main/java/com/tictactore/repository/TournamentMatchRepository.java` (NEW)
- `src/main/java/com/tictactore/repository/TournamentRepository.java` (UPDATE)
- `src/main/java/com/tictactore/dto/tournament/SeededParticipant.java` (NEW)
- `src/main/java/com/tictactore/dto/TournamentMatchResponse.java` (NEW)
- `src/main/java/com/tictactore/dto/RoundMatchesResponse.java` (NEW)
- `src/main/java/com/tictactore/dto/TournamentBracketResponse.java` (NEW)
- `src/main/java/com/tictactore/event/TournamentStartedEvent.java` (NEW)
- `src/main/java/com/tictactore/event/TournamentCancelledEvent.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/TournamentSeedingStrategy.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/impl/StrengthBasedSeedingStrategy.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/impl/RandomSeedingStrategy.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/BracketGenerator.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/impl/CupBracketGenerator.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/impl/ChampionshipBracketGenerator.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/TournamentLifecycleService.java` (NEW)
- `src/main/java/com/tictactore/service/tournament/impl/TournamentLifecycleServiceImpl.java` (NEW)
- `src/main/java/com/tictactore/scheduler/TournamentScheduler.java` (NEW)
- `src/main/java/com/tictactore/listener/TournamentNotificationListener.java` (UPDATE)
- `src/main/java/com/tictactore/service/PushNotificationService.java` (UPDATE)
- `src/main/java/com/tictactore/service/impl/PushNotificationServiceImpl.java` (UPDATE)
- `src/main/java/com/tictactore/controller/TournamentController.java` (UPDATE)
- `frontend/src/features/tournament/types/tournament.ts` (UPDATE)
- `frontend/src/features/tournament/services/tournamentBracketService.ts` (NEW)
- `frontend/src/features/tournament/stores/tournamentStore.ts` (UPDATE)
- `frontend/src/features/tournament/components/TournamentMatchCard.vue` (NEW)
- `frontend/src/features/tournament/components/TournamentBracket.vue` (NEW)
- `frontend/src/features/tournament/components/TournamentSchedule.vue` (NEW)
- `frontend/src/features/tournament/views/TournamentsView.vue` (UPDATE)
- `frontend/src/locales/en.json` (UPDATE)
- `frontend/src/locales/de.json` (UPDATE)
- `src/test/java/com/tictactore/service/tournament/TournamentLifecycleServiceTest.java` (NEW)
- `src/test/java/com/tictactore/service/tournament/StrengthBasedSeedingStrategyTest.java` (NEW)
- `src/test/java/com/tictactore/service/tournament/CupBracketGeneratorTest.java` (NEW)
- `src/test/java/com/tictactore/service/tournament/ChampionshipBracketGeneratorTest.java` (NEW)
- `src/test/java/com/tictactore/scheduler/TournamentSchedulerTest.java` (NEW)
- `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java` (NEW)
- `src/test/java/com/tictactore/controller/TournamentBracketControllerTest.java` (NEW)
- `src/test/java/com/tictactore/listener/TournamentNotificationListenerTest.java` (UPDATE)
- `frontend/src/features/tournament/stores/__tests__/tournamentStore.spec.ts` (UPDATE)
- `frontend/src/features/tournament/components/__tests__/TournamentBracket.spec.ts` (NEW)
- `frontend/src/features/tournament/components/__tests__/TournamentMatchCard.spec.ts` (NEW)
- `frontend/e2e/tournament-bracket.spec.ts` (NEW)

