---
baseline_commit: a13cf109496d851fa35bd29538fdcb8f104d2d36
status: ready-for-dev
---

# Story 8.4: Equal Match Distribution (2v2 Random Pairing)

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a system,
I want to distribute matches equally and generate balanced 2v2 pairings in random pairing mode,
so that every participant receives an equal number of matches, maximum partner variety, and a fair competition with stub-partner fallback upon player deletion.

## Acceptance Criteria

1. **Given** an open tournament with mode `TWO_VS_TWO_RANDOM_PAIRINGS` starting with $N$ confirmed participants ($N \ge 4$)
   **When** the tournament start routine calculates the match schedule (`RandomPairingBracketGenerator`)
   **Then** the algorithm guarantees that:
   - Each participant is assigned to play an exact equal number of matches $M$ as determined by round/cycle configuration (`FR47`).
   - Every match is a 2v2 contest between Team 1 (Player 1 + Partner 1) and Team 2 (Player 2 + Partner 2), referencing individual `TournamentRegistration` entries.
   - Partner repetition is minimized across matches (maximizing unique teammate pairings).
   - Opponent encounters are uniformly distributed across participants.
   - Schedule generation is deterministic and reproducible, seeded with the tournament ID/seed.
2. **Given** a 2v2 random pairing tournament match is scheduled
   **When** the `TournamentMatch` entity is persisted
   **Then** the entity records all 4 distinct participants: `participant1` ($P_1$), `participant1Partner` ($P_2$), `participant2` ($P_3$), `participant2Partner` ($P_4$) linked to their respective `TournamentRegistration` records, along with their seeds and initial `PENDING`/`READY` status (`FR47`).
3. **Given** an active tournament in mode `TWO_VS_TWO_RANDOM_PAIRINGS` and a participant initiates or completes account deletion (or withdraws)
   **When** the deletion event / handler executes (`AccountDeletionService` / `TournamentAccountDeletionHandler`)
   **Then** for all remaining pending/ready matches where the deleted player was assigned:
   - The system selects a stub partner from active tournament participants who is closest in statistical strength to the deleted player based on the frozen `strength_score` captured at tournament start (`FR33`).
   - If multiple candidates have the same closest strength difference, the selection picks deterministically among them using the tournament seed and registration ID.
   - The replaced match slot is updated with the selected stub partner and flagged as `isParticipant1Stub = true` or `isParticipant2Stub = true`.
   - `TournamentStubPartnerAssignedEvent` is emitted, and push notifications are sent to the affected teammate and assigned stub partner.
4. **Given** a stub partner plays an extra substitute match on behalf of a deleted player in a 2v2 random pairing tournament
   **When** the match concludes and standings/statistics are calculated
   **Then** the match result and score apply to the tournament standings of the active team/partner, but the stub partner's extra match does NOT count toward their own individual tournament statistics or standings (`FR33`).
   - In knockout format, the stub partner cannot be eliminated in their substitute match if they have already advanced in their own bracket branch (`FR33`).
5. **Given** a 2v2 random pairing tournament match is initiated for gameplay
   **When** the match is submitted for confirmation
   **Then** both opponents must confirm individually (because 2v2 random pairing is an individual competition), transitioning the match according to `VerificationRules` (`FR14`).
6. **Given** an authenticated user querying tournament bracket, schedule, or match details via `GET /api/v1/tournaments/{tournamentId}/matches` or `GET /api/v1/tournaments/{tournamentId}/bracket`
   **When** the request is processed
   **Then** `TournamentMatchResponse` includes both primary participants and their match partners (`participant1Partner`, `participant2Partner`), along with stub partner indicators (`isParticipant1Stub`, `isParticipant2Stub`), enabling the frontend to display full 2v2 team rosters on `TournamentMatchCard.vue` (`FR46`, `FR47`).

## Tasks / Subtasks

- [ ] Task 1: Database Migration & Entity Enhancements (AC1, AC2, AC3)
  - [ ] Create Flyway migration `src/main/resources/db/migration/V21__add_partners_and_stubs_to_tournament_match.sql`:
    - Add column `participant1_partner_id UUID REFERENCES tournament_registration(id) ON DELETE CASCADE`
    - Add column `participant2_partner_id UUID REFERENCES tournament_registration(id) ON DELETE CASCADE`
    - Add column `is_participant1_stub BOOLEAN NOT NULL DEFAULT FALSE`
    - Add column `is_participant2_stub BOOLEAN NOT NULL DEFAULT FALSE`
    - Create indexes:
      - `idx_tournament_match_part1_partner ON tournament_match(participant1_partner_id)`
      - `idx_tournament_match_part2_partner ON tournament_match(participant2_partner_id)`
  - [ ] Update Entity `com.tictactore.model.TournamentMatch.java`:
    - Add fields:
      - `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "participant1_partner_id") private TournamentRegistration participant1Partner;`
      - `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "participant2_partner_id") private TournamentRegistration participant2Partner;`
      - `@Column(name = "is_participant1_stub", nullable = false) private boolean isParticipant1Stub;`
      - `@Column(name = "is_participant2_stub", nullable = false) private boolean isParticipant2Stub;`
  - [ ] Update Repository `com.tictactore.repository.TournamentMatchRepository.java`:
    - `@Query("SELECT tm FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId AND (tm.participant1.id = :regId OR tm.participant2.id = :regId OR tm.participant1Partner.id = :regId OR tm.participant2Partner.id = :regId)") List<TournamentMatch> findByAnyParticipantRegistrationId(UUID tournamentId, UUID regId)`
    - `List<TournamentMatch> findByTournamentIdAndStatusIn(UUID tournamentId, Collection<TournamentMatchStatus> statuses)`
  - [ ] Repository tests in `src/test/java/com/tictactore/repository/TournamentMatchRepositoryTest.java` (`@DataJpaTest`).

- [ ] Task 2: 2v2 Random Pairing Algorithm & Bracket Generator (AC1, AC2, AC5)
  - [ ] Create `com.tictactore.service.tournament.impl.RandomPairingBracketGenerator.java` implementing `BracketGenerator`:
    - Implement Whist tournament / Social Golfer 4-tuple round-robin scheduling algorithm for $N \ge 4$ participants.
    - Calculate rounds such that each player participates in exactly $M$ matches (equal distribution).
    - Partition players each round into 4-player matches: Team 1 $(P_1, P_2)$ vs Team 2 $(P_3, P_4)$.
    - Optimize pairing matrix to minimize partner duplication and balance opponent frequencies.
    - Use deterministic pseudo-random sequence seeded with `tournament.getId().getMostSignificantBits()` for reproducible ordering.
    - Set Round 1 matches to `TournamentMatchStatus.READY`, subsequent rounds to `PENDING`.
  - [ ] Update `com.tictactore.service.tournament.impl.TournamentLifecycleServiceImpl.java`:
    - Inject `@Qualifier("randomPairingBracketGenerator") private final BracketGenerator randomPairingBracketGenerator`.
    - Route bracket generation:
      ```java
      BracketGenerator generator = (tournament.getMode() == TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
              ? randomPairingBracketGenerator
              : (tournament.getFormat() == TournamentFormat.CHAMPIONSHIP ? championshipBracketGenerator : cupBracketGenerator);
      ```
  - [ ] Unit tests in `src/test/java/com/tictactore/service/tournament/RandomPairingBracketGeneratorTest.java`:
    - Test $N = 4, 5, 6, 8, 12, 16$ participants verifying equal match counts per player.
    - Test partner diversity and deterministic reproducibility.

- [ ] Task 3: Stub Partner Selection & Account Deletion Protocol (AC3, AC4)
  - [ ] Create `com.tictactore.service.tournament.StubPartnerSelector.java` & `impl/StubPartnerSelectorImpl.java`:
    - `TournamentRegistration selectStubPartner(Tournament tournament, TournamentRegistration deletedRegistration, List<TournamentRegistration> candidatePool)`
    - Evaluate candidate pool by closest frozen `strengthScore`: $|\text{candidate.strengthScore} - \text{deletedRegistration.strengthScore}| \to \min$.
    - Exclude current match partner from candidates to prevent duplicate player in same match.
    - Deterministic tie-breaking by `registration.getId()`.
  - [ ] Create `com.tictactore.service.tournament.TournamentAccountDeletionHandler.java` & implementation:
    - Query active/pending matches in the tournament for the deleted user.
    - For 2v2 random pairings: replace slot with selected stub partner and set `isParticipant1Stub = true` or `isParticipant2Stub = true`.
    - For 1v1 / 2v2 fixed teams: mark remaining matches as technical defeat (`COMPLETED` with forfeit).
  - [ ] Create Event `com.tictactore.event.TournamentStubPartnerAssignedEvent.java` record:
    - `UUID tournamentId`, `UUID matchId`, `UUID deletedUserId`, `UUID teammateUserId`, `UUID stubPartnerUserId`
  - [ ] Update `com.tictactore.listener.TournamentNotificationListener.java`:
    - Send push notification informing the teammate and the assigned stub partner.
  - [ ] Unit tests in `src/test/java/com/tictactore/service/tournament/StubPartnerSelectorTest.java` and `TournamentAccountDeletionHandlerTest.java`.

- [ ] Task 4: DTOs, Query Service & Controller Updates (AC2, AC6)
  - [ ] Update `com.tictactore.dto.TournamentMatchResponse.java` record:
    - Add `TournamentRegistrationResponse participant1Partner`
    - Add `TournamentRegistrationResponse participant2Partner`
    - Add `boolean isParticipant1Stub`
    - Add `boolean isParticipant2Stub`
  - [ ] Update `com.tictactore.service.tournament.impl.TournamentMatchQueryServiceImpl.java`:
    - Map `participant1Partner`, `participant2Partner`, `isParticipant1Stub`, `isParticipant2Stub` in `mapToTournamentMatchResponse`.
  - [ ] Update WebMvcTest in `src/test/java/com/tictactore/controller/TournamentBracketControllerTest.java`.

- [ ] Task 5: Frontend Types, Components & i18n (AC6)
  - [ ] Update TypeScript types `frontend/src/features/tournament/types/tournament.ts`:
    - Add `participant1Partner?: TournamentRegistrationDto | null`
    - Add `participant2Partner?: TournamentRegistrationDto | null`
    - Add `isParticipant1Stub?: boolean`
    - Add `isParticipant2Stub?: boolean` to `TournamentMatchDto`.
  - [ ] Update `frontend/src/features/tournament/components/TournamentMatchCard.vue`:
    - Compute `participant1Name` and `participant2Name` to include dynamic match partner for 2v2 random mode:
      `${p.playerNickname} & ${pPartner.playerNickname}`.
    - Render `(Stub)` badge when `isParticipant1Stub` or `isParticipant2Stub` is true.
  - [ ] Update `frontend/src/features/tournament/components/TournamentSchedule.vue` to support 2v2 random pairing match cards.
  - [ ] Add translation strings to `frontend/src/locales/en.json` and `frontend/src/locales/de.json` under `tournament.stub_partner` / `tournament.substitute`.
  - [ ] Frontend component tests in `frontend/src/features/tournament/components/__tests__/TournamentMatchCard.spec.ts`.

- [ ] Task 6: Testing & Quality Verification
  - [ ] Backend Unit & Slice Tests:
    - `RandomPairingBracketGeneratorTest.java` (strict AAA without section comments).
    - `StubPartnerSelectorTest.java`.
    - `TournamentAccountDeletionHandlerTest.java`.
    - `TournamentMatchRepositoryTest.java` (@DataJpaTest).
    - `TournamentLifecycleServiceTest.java`.
    - `TournamentBracketControllerTest.java` (WebMvcTest).
  - [ ] Frontend Unit/Component Tests:
    - `TournamentMatchCard.spec.ts` (2v2 random pairing rendering, stub badges).
  - [ ] E2E Playwright Tests:
    - Create `frontend/e2e/tournament-random-pairing.spec.ts`:
      - Test 1: Create 2v2 random pairing tournament -> register 8 players -> start tournament -> verify equal match distribution across all rounds.
      - Test 2: Verify match cards render 4 players per match (2 vs 2).
      - Test 3: Simulate participant deletion -> verify stub partner assigned with stub indicator without disrupting remaining schedule.
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
- **Database & Concurrency Invariants:**
  - Flyway migration script: `src/main/resources/db/migration/V21__add_partners_and_stubs_to_tournament_match.sql`.
  - Foreign keys:
    - `participant1_partner_id REFERENCES tournament_registration(id) ON DELETE CASCADE`
    - `participant2_partner_id REFERENCES tournament_registration(id) ON DELETE CASCADE`
  - `@Version private Long version;` on `TournamentMatch` entity for optimistic locking (without `@Column`, as per `code-1-guide` rule 2).
- **Equal Match Distribution Algorithm (FR47):**
  - Problem formulation: Whist tournament / Social Golfer scheduling.
  - For $N$ players where $N \ge 4$:
    - Total player slots per match $= 4$.
    - A schedule of $K$ matches uses $4K$ slots. Equal distribution requires $4K = N \times M$ where $M$ is the number of matches per player.
    - When $N$ is a multiple of 4 ($N = 4, 8, 12, 16$), each round consists of $N / 4$ matches and every player plays exactly 1 match per round ($M = R$).
    - When $N$ is not a multiple of 4 ($N = 5, 6, 7$), scheduling partitions players across multiple micro-rounds with balanced BYEs so every player reaches exactly $M$ matches at the end of the tournament cycle.
    - Partner diversity metric: minimize $\sum_{i < j} (\text{partnerCount}(i,j) - \mu)^2$.
    - Opponent diversity metric: minimize $\sum_{i < j} (\text{opponentCount}(i,j) - \nu)^2$.
- **Stub Partner Selection & Account Deletion Protocol (FR33):**
  - When an account deletion is processed during an active tournament:
    - "Deletion is never blocked but follows a 24-hour delay protocol with countdown notification and cancellation option. Partner notified immediately. Remaining tournament matches result in technical defeat. For random-pairing tournaments: stub partner assigned randomly from players closest in statistical strength to the deleted player, using frozen strength rating captured at tournament start. Stub partner's extra match does not count toward their tournament statistics. In knockout format, the stub partner cannot be eliminated in their substitute match if already advanced in their own bracket."
  - Strength rating comparison:
    $\Delta_i = |\text{candidate}_i.\text{strengthScore} - \text{deleted}.\text{strengthScore}|$.
  - Candidates are sorted by $\Delta_i$ ascending; ties broken deterministically by `registration.getId().getMostSignificantBits()`.
  - The substitute match is flagged `isParticipant1Stub = true` or `isParticipant2Stub = true`.
- **Match Confirmation Integration (FR14):**
  - In 2v2 random pairings, both opponents confirm individually (`VerificationRules.java` from Story 3.4).
  - When tournament match is started, core `Match` entity is created with `matchFormat = MATCH_FORMAT_RANDOM` and 4 player UUIDs (`teamAAttackerId = P1`, `teamADefenderId = P1Partner`, `teamBAttackerId = P2`, `teamBDefenderId = P2Partner`).
- **UX & Design Invariants:**
  - **Clubhouse Design Tokens (UX-DR3):** Tonal shifts (`bg-surface-container-low`, `bg-surface-container-high`) and elevation instead of 1px solid border lines.
  - **500-Line Rule (IP-04):** All new and modified files must stay strictly under 500 lines.
- **Testing Standards (code-2-test):**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments (`// Given`, `// When`, `// Then` are strictly forbidden).
  - Integration tests end with `IT` or `ATDDTest` / `Test` for unit tests.

### References

- [Source: _bmad-output/planning-artifacts/prd.md#Functional Requirements] (FR14, FR33, FR41, FR43, FR46, FR47)
- [Source: _bmad-output/planning-artifacts/epics.md] (Epic 8, Story 8.4)
- [Source: _bmad-output/implementation-artifacts/8-3-automated-bracket-generation-and-seeding.md] (Previous Story 8.3 Intelligence)

## Dev Agent Record

### Agent Model Used
Gemini 3.7 Flash (High)

### Debug Log References
N/A

### Completion Notes List
- Comprehensive developer guidance created for Story 8.4 following validation checklist.
- Added database migration V21 for 4-player tournament matches and stub partner flags.
- Defined Whist/Social Golfer equal match distribution algorithm and routing for `TWO_VS_TWO_RANDOM_PAIRINGS`.
- Specified stub partner selection and account deletion protocol adhering to FR33.
- Integrated DTOs, frontend components, and comprehensive test suite with strict AAA compliance.

### File List
N/A

## Change Log
- Initial creation of the story document.
- Validation improvements applied: 4-player tournament match database support, Whist equal match distribution algorithm, stub partner selection protocol, frontend 2v2 random match display, and test suites.

### Review Findings
N/A
