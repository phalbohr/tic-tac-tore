---
baseline_commit: d4cc6e4c61c55b85a49510d2029b6263720fc529
status: ready-for-dev
---

# Story 8.7: Tournament Standings & Archive

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a player or tournament organizer,
I want to view live tournament standings updated in real time and browse past completed tournaments in an archive,
so that I can track competition progress, identify champions, and retain historical tournament results.

## Acceptance Criteria

1. **Given** an active (`IN_PROGRESS`) or completed (`COMPLETED`) tournament
   **When** an authenticated user queries `GET /api/v1/tournaments/{tournamentId}/standings`
   **Then** the backend returns `200 OK` with a structured list of `TournamentStandingResponse` sorted by ranking criteria, calculated dynamically from all confirmed tournament matches (`FR26`, `FR46`).
2. **Given** a tournament in format `CHAMPIONSHIP` (Round Robin) or mode `TWO_VS_TWO_RANDOM_PAIRINGS`
   **When** standings are calculated by `TournamentStandingsService`
   **Then** rankings are computed based on confirmed match results:
   - Match win awards 3 points, loss awards 0 points.
   - Accumulates matches played (`matchesPlayed`), wins (`wins`), losses (`losses`), games won (`gamesWon`), games lost (`gamesLost`), and game difference (`gameDifference = gamesWon - gamesLost`).
   - Tie-breaking order: (1) `points` descending, (2) `wins` descending, (3) `gameDifference` descending, (4) `matchesPlayed` ascending, (5) `nickname` ascending.
   - In `TWO_VS_TWO_RANDOM_PAIRINGS` mode: stub partner substitute matches (`isParticipant1Stub` / `isParticipant2Stub`) apply points and stats to the active teammate/team, but do NOT increment matches or points for the stub substitute player (`FR33`, `FR47`).
3. **Given** a tournament in format `CUP` (Single Elimination Knockout)
   **When** standings are calculated
   **Then** participants are tracked with their elimination status (`isEliminated = true` when knocked out) and ranked by deepest round reached and match wins (`FR26`).
4. **Given** an active tournament (`IN_PROGRESS`)
   **When** the final match concludes and is confirmed via `completeMatch` (in `CUP`: the final match; in `CHAMPIONSHIP` / `RANDOM_PAIRINGS`: all scheduled matches reach `COMPLETED` or `BYE` status)
   **Then**:
   - `Tournament.status` automatically transitions from `IN_PROGRESS` to `COMPLETED`.
   - `TournamentCompletedEvent` is published with the tournament ID and champion registration ID.
   - The tournament becomes immutable and permanently retained in the archive (`FR46`).
5. **Given** an authenticated user requesting historical tournaments via `GET /api/v1/tournaments` with `status=COMPLETED` (or `GET /api/v1/tournaments/archive`)
   **When** the request includes pagination parameters (e.g., `page=0`, `size=10`)
   **Then** the backend returns `200 OK` with a `Page<TournamentResponse>` sorted by `updatedAt` / `createdAt` descending, containing tournament configuration, timestamps, creator info, and completed status (`FR46`).
6. **Given** an authenticated user viewing the Tournaments page (`/tournaments`) in the frontend
   **When** the user switches between the "Active & Upcoming" tab and the "Archive" tab
   **Then**:
   - The "Archive" tab displays historical tournaments with a "COMPLETED" status badge, final completion date, and participant count.
   - Clicking on a completed tournament card opens the bracket/schedule modal with historical results and an accessible "Standings" view (`FR46`).
7. **Given** an authenticated user viewing an active or completed tournament in `TournamentsView.vue`
   **When** they view the tournament details modal or bracket view
   **Then** a dedicated `TournamentStandings.vue` component renders the standings table:
   - Rank, player/partner avatar and nickname, matches played ($P$), wins ($W$), losses ($L$), game difference ($Diff$), points ($Pts$), and status badges (`Active` / `Eliminated` / `Winner`).
   - Styled with Clubhouse design tokens (`bg-surface-container-low`, `rounded-2xl`, no 1px solid borders per `UX-DR3`).
8. **Given** a tournament participant who has deleted their account (GDPR FR33)
   **When** tournament standings or archive views are loaded
   **Then** the backend and frontend replace the deleted user's identity with "Anonymous" and the standard placeholder avatar without breaking table layout or crashing calculations (`FR33`).

## Tasks / Subtasks

- [ ] Task 1: Database Migration & Repository Enhancements (AC4, AC5)
  - [ ] Create Flyway migration `src/main/resources/db/migration/V22__add_tournament_archive_indexes.sql`:
    - Add index `idx_tournament_status_updated_at ON tournament(status, updated_at DESC)`
    - Add index `idx_tournament_status_created_at ON tournament(status, created_at DESC)`
  - [ ] Update `com.tictactore.repository.TournamentRepository.java`:
    - Add `Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable)`
    - Add `Page<Tournament> findAllByOrderByCreatedAtDesc(Pageable pageable)`
  - [ ] Repository test in `src/test/java/com/tictactore/repository/TournamentRepositoryTest.java` verifying paginated status queries (`@DataJpaTest`).

- [ ] Task 2: Backend Standings Service & Scoring Calculation (AC1, AC2, AC3, AC8)
  - [ ] Update DTO `com.tictactore.dto.tournament.TournamentStandingResponse.java` record:
    - `UUID registrationId`
    - `UUID userId`
    - `String nickname`
    - `String avatarUrl`
    - `UUID partnerUserId`
    - `String partnerNickname`
    - `String partnerAvatarUrl`
    - `int matchesPlayed`
    - `int wins`
    - `int losses`
    - `int gamesWon`
    - `int gamesLost`
    - `int gameDifference`
    - `int points`
    - `boolean isEliminated`
    - `Integer rank`
  - [ ] Update `com.tictactore.service.tournament.impl.TournamentStandingsServiceImpl.java`:
    - Enhance `StandingAccumulator` to extract game counts (`gamesWon`, `gamesLost`) from linked `Match.games` for completed matches.
    - Handle 2v2 partner nicknames/avatars for fixed teams.
    - Check for anonymized/deleted users (render "Anonymous" when user/nickname is missing per FR33).
    - Implement multi-tier sorting: `points DESC`, then `wins DESC`, then `gameDifference DESC`, then `matchesPlayed ASC`, then `nickname ASC`.
    - Assign 1-based sequential `rank` to each standing response.
  - [ ] Unit tests in `src/test/java/com/tictactore/service/tournament/TournamentStandingsServiceTest.java`.

- [ ] Task 3: Automated Tournament Completion & Event Publishing (AC4)
  - [ ] Create Event `com.tictactore.event.TournamentCompletedEvent.java` record:
    - `UUID tournamentId`
    - `UUID winnerRegistrationId`
    - `Instant completedAt`
  - [ ] Update `com.tictactore.service.tournament.impl.TournamentMatchServiceImpl.java`:
    - In `completeMatch(UUID tournamentMatchId, UUID matchId)`:
      - After saving the match and advancing cup winners, check if tournament is completed:
        - For `CUP`: check if the concluded match is the tournament Final (`nextMatch == null`).
        - For `CHAMPIONSHIP` / `RANDOM_PAIRINGS`: check if all matches for the tournament in `tournamentMatchRepository` have status `COMPLETED`, `BYE`, or `CANCELLED`.
      - If completed:
        - Set `tournament.setStatus(TournamentStatus.COMPLETED)`.
        - Persist updated tournament via `tournamentRepository.save(tournament)`.
        - Publish `TournamentCompletedEvent`.
  - [ ] Unit tests in `src/test/java/com/tictactore/service/tournament/TournamentMatchServiceTest.java`.

- [ ] Task 4: Backend Controller Endpoints & OpenAPI Documentation (AC1, AC5)
  - [ ] Update `com.tictactore.controller.TournamentController.java`:
    - Add endpoint:
      - `@GetMapping("/{id}/standings")`
      - `public ResponseEntity<List<TournamentStandingResponse>> getTournamentStandings(@PathVariable UUID id)`
      - Invokes `tournamentStandingsService.calculateStandings(id)` and returns `200 OK`.
    - Update tournament list endpoint for pagination and archive filtering:
      - `@GetMapping`
      - `public ResponseEntity<Page<TournamentResponse>> getTournaments(@RequestParam(required = false) TournamentStatus status, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable)`
  - [ ] Controller WebMvc & ATDD tests:
    - `src/test/java/com/tictactore/controller/TournamentControllerTest.java` (WebMvcTest).
    - `src/test/java/com/tictactore/controller/TournamentControllerATDDTest.java` (Full request lifecycle).

- [ ] Task 5: Frontend Types, API Service & Pinia Store (AC1, AC5, AC6)
  - [ ] Update `frontend/src/features/tournament/types/tournament.ts`:
    - Add `TournamentStandingDto` interface.
    - Add `PageDto<T>` or paginated tournament response interface.
  - [ ] Update `frontend/src/features/tournament/services/tournamentService.ts`:
    - Add `getTournamentStandings(tournamentId: string): Promise<TournamentStandingDto[]>`.
    - Add `getTournamentsPaginated(status?: TournamentStatus, page?: number, size?: number): Promise<{ content: TournamentDto[]; totalPages: number; totalElements: number }>`.
  - [ ] Update `frontend/src/features/tournament/stores/tournamentStore.ts`:
    - State: `standings: Record<string, TournamentStandingDto[]>`, `archiveTournaments: TournamentDto[]`, `archivePage: number`, `archiveTotalPages: number`, `isArchiveLoading: boolean`.
    - Actions:
      - `fetchStandings(tournamentId: string): Promise<TournamentStandingDto[]>`.
      - `fetchArchive(page?: number, size?: number): Promise<void>`.
  - [ ] Frontend store tests in `frontend/src/features/tournament/stores/__tests__/tournamentStore.spec.ts`.

- [ ] Task 6: Frontend Standings Component & Modal Integration (AC5, AC7, AC8)
  - [ ] Create `frontend/src/features/tournament/components/TournamentStandings.vue`:
    - Clubhouse design token styling (`bg-surface-container-low`, `rounded-2xl`, elevation, no 1px solid borders per `UX-DR3`).
    - Standings table with columns: `# (Rank)`, `Player / Team`, `P (Played)`, `W (Wins)`, `L (Losses)`, `+/- (Game Diff)`, `Pts (Points)`, `Status`.
    - Responsive mobile layout: scrollable table or stacked card list on small screens.
    - Visual indicators for `Winner` (Gold/Trophy badge), `Active`, and `Eliminated`.
    - Player avatar chips with fallback generated avatar / anonymized "boots on a nail" placeholder.
  - [ ] Component tests in `frontend/src/features/tournament/components/__tests__/TournamentStandings.spec.ts`.

- [ ] Task 7: Frontend Archive Tab & Navigation in TournamentsView (AC6, AC7)
  - [ ] Update `frontend/src/features/tournament/views/TournamentsView.vue`:
    - Add Segmented Tab selector at top: `Active & Registration` vs `Archive`.
    - Under `Archive` tab:
      - Render list of completed tournaments with completion date, winner info, and format badges.
      - Add pagination controls (Previous / Next / Page numbers).
    - In Bracket / Details modal:
      - Add Sub-navigation tab toggle: `Bracket / Schedule` vs `Standings Table`.
      - Embed `TournamentStandings.vue` when `Standings` tab is selected.
  - [ ] Add i18n translation keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json` under `tournament.standings.*` and `tournament.archive.*`.
  - [ ] Update component tests in `frontend/src/features/tournament/views/__tests__/TournamentsView.spec.ts`.

## Dev Agent Guardrails & Implementation Details

### Critical Architecture & Code Standards
1. **Zero Comments Policy (code-1-guide & code-4-document)**:
   - Do NOT add explanatory comments, Javadoc, or inline comments to production code unless specifically mandated (e.g. OpenAPI annotations).
   - Code must be self-documenting.
2. **Spring Boot 4 Entity & DTO Conventions (code-1-guide)**:
   - Do NOT put `@Column` on basic fields without specific need (e.g. nullable/length/name overrides).
   - Use records for all DTOs and events (`TournamentStandingResponse`, `TournamentCompletedEvent`).
   - Use `@Version private Long version;` for optimistic locking (no `@Column`).
   - Entities must use `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor(access = AccessLevel.PRIVATE)`.
   - Never use field injection; use `@RequiredArgsConstructor` and `private final` fields.
3. **Testing Standards (code-2-test)**:
   - Use **AssertJ** (`assertThat(...)`) for assertions. Never use JUnit's `assertEquals` or `assertTrue`.
   - Follow strict **AAA pattern** (Arrange, Act, Assert) separated by blank lines — do NOT write `// Arrange`, `// Act`, `// Assert` section comments.
   - Use `@DisplayName` on every test class and method with clear descriptive sentence in active voice.
4. **Clubhouse Design Tokens (UX-DR3)**:
   - Use Tailwind utility classes with semantic design tokens: `bg-surface-container-low`, `text-on-surface`, `text-on-surface-variant`, `rounded-2xl`, `rounded-xl`.
   - Do NOT use harsh `1px solid` border dividers; use surface elevation, subtle backgrounds, and border opacity (`border-outline-variant/10`).
5. **GDPR / Anonymization (FR33)**:
   - Always handle null players or deleted users gracefully. Never call `.getNickname()` or `.getId()` on null objects without safe null checks or default fallbacks (`"Anonymous"`).

### Files Being Modified (UPDATE vs NEW)

| File | Status | Description / Guardrails |
|---|---|---|
| `src/main/resources/db/migration/V22__add_tournament_archive_indexes.sql` | **NEW** | Flyway migration for archive performance indexes |
| `src/main/java/com/tictactore/dto/tournament/TournamentStandingResponse.java` | **UPDATE** | Add `gamesWon`, `gamesLost`, `gameDifference`, `avatarUrl`, `partner*` fields, `rank` |
| `src/main/java/com/tictactore/event/TournamentCompletedEvent.java` | **NEW** | Event emitted when tournament finishes |
| `src/main/java/com/tictactore/repository/TournamentRepository.java` | **UPDATE** | Add paginated query methods `findByStatus` and `findAllByOrderByCreatedAtDesc` |
| `src/main/java/com/tictactore/service/tournament/impl/TournamentStandingsServiceImpl.java` | **UPDATE** | Calculate game difference from match games, sort with tie-breakers, assign ranks |
| `src/main/java/com/tictactore/service/tournament/impl/TournamentMatchServiceImpl.java` | **UPDATE** | Detect tournament completion in `completeMatch`, update status to `COMPLETED`, publish event |
| `src/main/java/com/tictactore/controller/TournamentController.java` | **UPDATE** | Add `GET /{id}/standings` and paginated `GET` list endpoint |
| `src/test/java/com/tictactore/service/tournament/TournamentStandingsServiceTest.java` | **NEW / UPDATE** | Unit tests for standings calculation, tie-breaking, stub handling, anonymization |
| `src/test/java/com/tictactore/controller/TournamentControllerTest.java` | **UPDATE** | WebMvc tests for standings and archive endpoints |
| `frontend/src/features/tournament/types/tournament.ts` | **UPDATE** | Add `TournamentStandingDto` and paginated response types |
| `frontend/src/features/tournament/services/tournamentService.ts` | **UPDATE** | Add `getTournamentStandings` and `getTournamentsPaginated` |
| `frontend/src/features/tournament/stores/tournamentStore.ts` | **UPDATE** | Add standings state, archive state, and fetch actions |
| `frontend/src/features/tournament/components/TournamentStandings.vue` | **NEW** | Vue 3 standings table component with Clubhouse design tokens |
| `frontend/src/features/tournament/components/__tests__/TournamentStandings.spec.ts` | **NEW** | Vitest tests for standings component |
| `frontend/src/features/tournament/views/TournamentsView.vue` | **UPDATE** | Add Archive tab, standings toggle in modal, pagination controls |
| `frontend/src/locales/en.json` | **UPDATE** | Add English translation keys for standings and archive |
| `frontend/src/locales/de.json` | **UPDATE** | Add German translation keys for standings and archive |

## Previous Story Intelligence & Learnings

- **From Story 8.1 & 8.2**: `Tournament` status lifecycle is `REGISTRATION_OPEN` -> `IN_PROGRESS` -> `COMPLETED` / `CANCELLED`. Registrations link to `User` entity via `player` and optional `partner`.
- **From Story 8.3 & 8.4**: Tournament brackets generate `TournamentMatch` entities with `participant1`, `participant2`, and partner fields. 2v2 random pairing stub matches are flagged with `isParticipant1Stub` / `isParticipant2Stub`. Standings calculation must ignore stub matches for the stub substitute player.
- **From Story 8.5**: Match completion is handled via `TournamentMatchServiceImpl.completeMatch` and triggered by `TournamentMatchEventListener` when core `MatchConfirmedEvent` arrives.
- **From Story 8.6**: Tournament match rules are strictly locked to `RuleConfiguration`.

## References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 8.7]
- [Source: _bmad-output/planning-artifacts/prd.md#FR26, FR46, FR33]
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md]
- [Source: src/main/java/com/tictactore/service/tournament/TournamentStandingsService.java]

