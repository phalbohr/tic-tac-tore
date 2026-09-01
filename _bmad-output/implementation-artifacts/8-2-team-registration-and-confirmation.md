---
baseline_commit: 4e6e6d16646180211d53ae7ce811b81c9f3d1755
---

# Story 8.2: Team Registration & Confirmation

Status: review

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a player,
I want to register for an open tournament (individually for 1v1/random pairings or with a partner for 2v2 fixed teams),
so that we can participate and compete once the tournament starts.

## Acceptance Criteria

1. **Given** an open tournament in `REGISTRATION_OPEN` status with mode `ONE_VS_ONE_PERSONAL` or `TWO_VS_TWO_RANDOM_PAIRINGS`
   **When** an authenticated player submits a registration request via `POST /api/v1/tournaments/{tournamentId}/registrations` without a partner
   **Then** the backend persists the `TournamentRegistration` entity with `partner_id = null` and status `CONFIRMED`, returning `201 Created` with `TournamentRegistrationResponse` (`FR42`).
2. **Given** an open tournament in `REGISTRATION_OPEN` status with mode `TWO_VS_TWO_FIXED_TEAMS`
   **When** an authenticated player submits a registration request specifying a valid partner (`partnerId`)
   **Then** the backend creates the `TournamentRegistration` entity with status `PENDING_CONFIRMATION`, emits `TournamentInviteCreatedEvent`, sends a push notification to the partner, and returns `201 Created` (`FR42`, `FR55`).
3. **Given** a pending registration (`PENDING_CONFIRMATION`) for a 2v2 fixed teams tournament
   **When** the designated partner accepts the invitation via `POST /api/v1/tournaments/{tournamentId}/registrations/{registrationId}/accept`
   **Then** the registration status transitions to `CONFIRMED`, `TournamentInviteAcceptedEvent` is emitted, an acceptance push notification is sent to the initiating player, and the team is recorded as confirmed participants (`FR42`, `FR55`).
4. **Given** a pending registration (`PENDING_CONFIRMATION`) for a 2v2 fixed teams tournament
   **When** the designated partner declines the invitation via `POST /api/v1/tournaments/{tournamentId}/registrations/{registrationId}/decline`
   **Then** the registration status transitions to `DECLINED`, `TournamentInviteDeclinedEvent` is emitted, a decline push notification is sent to the initiating player, and the registration slot is freed.
5. **Given** an active or pending registration created by the authenticated user
   **When** the player withdraws/cancels their registration before the tournament registration deadline via `DELETE /api/v1/tournaments/{tournamentId}/registrations/{registrationId}`
   **Then** the registration status transitions to `CANCELLED`, the slot is freed, and `204 No Content` is returned.
6. **Given** registration attempts with invalid constraints:
   - Tournament does not exist (`404 Not Found`)
   - Tournament is not in `REGISTRATION_OPEN` status (`409 Conflict` or `400 Bad Request`)
   - Tournament registration deadline has passed (`Instant.now() > registrationDeadline`) (`400 Bad Request`)
   - Tournament has reached `maxParticipants` capacity (`409 Conflict`)
   - Player or partner already has an active registration (`PENDING_CONFIRMATION` or `CONFIRMED`) in this tournament (`409 Conflict`)
   - In `TWO_VS_TWO_FIXED_TEAMS` mode, `partnerId` is null, non-existent, or equals the initiating `playerId` (`400 Bad Request`)
   - In `ONE_VS_ONE_PERSONAL` or `TWO_VS_TWO_RANDOM_PAIRINGS` mode, `partnerId` is provided (`400 Bad Request`)
   - A user who is not the designated `partner` attempts to accept or decline the invitation (`403 Forbidden`)
   - A user who is neither the initiating `player` nor the `partner` attempts to cancel the registration (`403 Forbidden`)
   **When** the request is submitted
   **Then** the backend rejects the request with the corresponding HTTP error code and field-level message.
7. **Given** an authenticated user viewing a tournament
   **When** they query `GET /api/v1/tournaments/{tournamentId}/registrations` or `GET /api/v1/tournaments/{tournamentId}/registrations/my`
   **Then** the backend returns `200 OK` with the roster of confirmed/pending registrations or the user's registration status in that tournament (`FR42`, `FR46`).
8. **Given** an authenticated user viewing tournament details or tournaments list in the frontend
   **When** they open `TournamentRegistrationModal.vue` and complete registration
   **Then** the registration request is dispatched, a success toast notification appears, the tournament store is updated, and if invited, the partner receives a prompt/card in `TournamentInviteModal.vue` to accept or decline.

## Tasks / Subtasks

- [x] Task 1: Database Migration & JPA Entities (AC1, AC2, AC3, AC4, AC5, AC6)
  - [x] Create Flyway migration `src/main/resources/db/migration/V19__create_tournament_registration_tables.sql`:
    - Create `tournament_registration` table:
      - `id UUID PRIMARY KEY`
      - `tournament_id UUID NOT NULL REFERENCES tournament(id) ON DELETE CASCADE`
      - `player_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE`
      - `partner_id UUID REFERENCES "user"(id) ON DELETE CASCADE`
      - `status VARCHAR(30) NOT NULL`
      - `created_at TIMESTAMP WITH TIME ZONE NOT NULL`
      - `updated_at TIMESTAMP WITH TIME ZONE`
      - `version BIGINT NOT NULL DEFAULT 0`
    - Create indexes:
      - `idx_tournament_registration_tournament_id ON tournament_registration(tournament_id)`
      - `idx_tournament_registration_player_id ON tournament_registration(player_id)`
      - `idx_tournament_registration_partner_id ON tournament_registration(partner_id)`
      - `idx_tournament_registration_status ON tournament_registration(status)`
    - Create unique partial indexes to prevent duplicate active registrations per tournament:
      - `CREATE UNIQUE INDEX uq_tournament_registration_player ON tournament_registration(tournament_id, player_id) WHERE status IN ('PENDING_CONFIRMATION', 'CONFIRMED');`
      - `CREATE UNIQUE INDEX uq_tournament_registration_partner ON tournament_registration(tournament_id, partner_id) WHERE status IN ('PENDING_CONFIRMATION', 'CONFIRMED') AND partner_id IS NOT NULL;`
  - [x] Create Enum `com.tictactore.model.RegistrationStatus.java`:
    - `PENDING_CONFIRMATION`, `CONFIRMED`, `DECLINED`, `CANCELLED`
  - [x] Create Entity `com.tictactore.model.TournamentRegistration.java`:
    - Annotations: `@Entity`, `@Table(name = "tournament_registration")`, `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor(access = AccessLevel.PRIVATE)`
    - Fields:
      - `UUID id`
      - `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tournament_id", nullable = false) Tournament tournament`
      - `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "player_id", nullable = false) User player`
      - `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "partner_id") User partner`
      - `@Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) RegistrationStatus status`
      - `@CreationTimestamp OffsetDateTime createdAt`
      - `@UpdateTimestamp OffsetDateTime updatedAt`
      - `@Version private Long version;` (no `@Column`, code-1-guide rule 2)
  - [x] Create Repository `com.tictactore.repository.TournamentRegistrationRepository.java`:
    - `List<TournamentRegistration> findByTournamentId(UUID tournamentId)`
    - `List<TournamentRegistration> findByTournamentIdAndStatus(UUID tournamentId, RegistrationStatus status)`
    - `long countByTournamentIdAndStatus(UUID tournamentId, RegistrationStatus status)`
    - `@Query("SELECT r FROM TournamentRegistration r WHERE r.tournament.id = :tournamentId AND (r.player.id = :userId OR r.partner.id = :userId) AND r.status IN (:statuses)")`
      `Optional<TournamentRegistration> findActiveUserRegistration(UUID tournamentId, UUID userId, Collection<RegistrationStatus> statuses)`
    - `@Query("SELECT r FROM TournamentRegistration r WHERE r.partner.id = :userId AND r.status = 'PENDING_CONFIRMATION'")`
      `List<TournamentRegistration> findPendingInvitationsForUser(UUID userId)`
  - [x] Repository test `src/test/java/com/tictactore/repository/TournamentRegistrationRepositoryTest.java` (`@DataJpaTest`).

- [x] Task 2: Backend DTOs, Events, Push Notifications, Service & Controller (AC1–AC7)
  - [x] Create DTOs in `com.tictactore.dto`:
    - `RegisterTournamentRequest.java` record:
      - `UUID partnerId` (optional for 1v1 / 2v2 random, required for 2v2 fixed)
    - `TournamentRegistrationResponse.java` record:
      - `UUID id`, `UUID tournamentId`, `String tournamentName`, `UUID playerId`, `String playerNickname`, `String playerAvatarUrl`, `UUID partnerId`, `String partnerNickname`, `String partnerAvatarUrl`, `RegistrationStatus status`, `OffsetDateTime createdAt`, `OffsetDateTime updatedAt`
    - `MyRegistrationStatusResponse.java` record:
      - `boolean isRegistered`, `TournamentRegistrationResponse registration`, `boolean isPendingInvite`
  - [x] Create Events in `com.tictactore.event`:
    - `TournamentInviteCreatedEvent.java` record (`UUID registrationId`, `UUID tournamentId`, `String tournamentName`, `UUID inviterId`, `String inviterNickname`, `UUID partnerId`)
    - `TournamentInviteAcceptedEvent.java` record (`UUID registrationId`, `UUID tournamentId`, `String tournamentName`, `UUID partnerId`, `String partnerNickname`, `UUID inviterId`)
    - `TournamentInviteDeclinedEvent.java` record (`UUID registrationId`, `UUID tournamentId`, `String tournamentName`, `UUID partnerId`, `String partnerNickname`, `UUID inviterId`)
  - [x] Update `com.tictactore.service.PushNotificationService.java` & `impl/PushNotificationServiceImpl.java`:
    - `void sendTournamentInviteNotification(UUID tournamentId, String tournamentName, String inviterName, User recipient)`
    - `void sendTournamentInviteAcceptedNotification(UUID tournamentId, String tournamentName, String partnerName, User recipient)`
    - `void sendTournamentInviteDeclinedNotification(UUID tournamentId, String tournamentName, String partnerName, User recipient)`
  - [x] Create Event Listener `com.tictactore.listener.TournamentRegistrationNotificationListener.java`:
    - `@Component`, `@RequiredArgsConstructor`, `@Slf4j`
    - `@Async @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public void handleTournamentInviteCreated(TournamentInviteCreatedEvent event)`
    - `@Async @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public void handleTournamentInviteAccepted(TournamentInviteAcceptedEvent event)`
    - `@Async @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public void handleTournamentInviteDeclined(TournamentInviteDeclinedEvent event)`
  - [x] Create Service interface and implementation:
    - `com.tictactore.service.TournamentRegistrationService.java`
    - `com.tictactore.service.TournamentRegistrationServiceImpl.java`
    - Implement methods:
      - `TournamentRegistrationResponse register(UUID tournamentId, UUID playerId, RegisterTournamentRequest request)`:
        - Check tournament exists (`EntityNotFoundException` if missing) and status is `REGISTRATION_OPEN`.
        - Check `registrationDeadline` is strictly in the future (`Instant.now().isBefore(tournament.getRegistrationDeadline())`).
        - Mode validation:
          - If `TWO_VS_TWO_FIXED_TEAMS`: `partnerId` required, cannot equal `playerId`. Verify partner exists in `UserRepository`.
          - If `ONE_VS_ONE_PERSONAL` or `TWO_VS_TWO_RANDOM_PAIRINGS`: `partnerId` must be null.
        - Check duplicate registration: neither `playerId` nor `partnerId` can have an active registration in this tournament (`PENDING_CONFIRMATION` or `CONFIRMED`).
        - Check capacity: `countByTournamentIdAndStatus(tournamentId, CONFIRMED)` < `maxParticipants`.
        - If 2v2 fixed teams -> status `PENDING_CONFIRMATION`, publish `TournamentInviteCreatedEvent`.
        - If 1v1 / 2v2 random -> status `CONFIRMED`.
        - Save entity and return mapped `TournamentRegistrationResponse`.
      - `TournamentRegistrationResponse acceptInvitation(UUID tournamentId, UUID registrationId, UUID partnerId)`:
        - Validate registration exists, belongs to tournament, status is `PENDING_CONFIRMATION`.
        - Validate authenticated user is `registration.getPartner().getId()` (throw `AccessDeniedException` if not).
        - Check tournament status still `REGISTRATION_OPEN` and registration deadline not passed.
        - Check confirmed capacity: `countByTournamentIdAndStatus(tournamentId, CONFIRMED)` < `maxParticipants`.
        - Transition status to `CONFIRMED`, save entity, publish `TournamentInviteAcceptedEvent`.
      - `TournamentRegistrationResponse declineInvitation(UUID tournamentId, UUID registrationId, UUID partnerId)`:
        - Validate registration exists, belongs to tournament, status is `PENDING_CONFIRMATION`.
        - Validate authenticated user is `registration.getPartner().getId()` (throw `AccessDeniedException` if not).
        - Transition status to `DECLINED`, save entity, publish `TournamentInviteDeclinedEvent`.
      - `void cancelRegistration(UUID tournamentId, UUID registrationId, UUID userId)`:
        - Validate registration exists, belongs to tournament.
        - Validate authenticated user is either `player` or `partner` on the registration.
        - Check tournament status is `REGISTRATION_OPEN` and registration deadline not passed.
        - Transition status to `CANCELLED` (or delete), save entity.
      - `List<TournamentRegistrationResponse> listRegistrations(UUID tournamentId, RegistrationStatus status)` (`@Transactional(readOnly = true)`)
      - `MyRegistrationStatusResponse getMyRegistrationStatus(UUID tournamentId, UUID userId)` (`@Transactional(readOnly = true)`)
      - `List<TournamentRegistrationResponse> getPendingInvitations(UUID userId)` (`@Transactional(readOnly = true)`)
  - [x] Create Controller `com.tictactore.controller.TournamentRegistrationController.java`:
    - `@RestController @RequestMapping("/api/v1/tournaments/{tournamentId}/registrations")`
    - `POST /api/v1/tournaments/{tournamentId}/registrations`: `@Valid @RequestBody RegisterTournamentRequest request`, `@AuthenticationPrincipal User principal` -> `201 Created` with `TournamentRegistrationResponse`.
    - `GET /api/v1/tournaments/{tournamentId}/registrations`: `@RequestParam(required = false) RegistrationStatus status` -> `200 OK` with `List<TournamentRegistrationResponse>`.
    - `GET /api/v1/tournaments/{tournamentId}/registrations/my`: `@AuthenticationPrincipal User principal` -> `200 OK` with `MyRegistrationStatusResponse`.
    - `POST /api/v1/tournaments/{tournamentId}/registrations/{registrationId}/accept`: `@AuthenticationPrincipal User principal` -> `200 OK` with `TournamentRegistrationResponse`.
    - `POST /api/v1/tournaments/{tournamentId}/registrations/{registrationId}/decline`: `@AuthenticationPrincipal User principal` -> `200 OK` with `TournamentRegistrationResponse`.
    - `DELETE /api/v1/tournaments/{tournamentId}/registrations/{registrationId}`: `@AuthenticationPrincipal User principal` -> `204 No Content`.
    - `GET /api/v1/tournaments/invitations/pending`: `@AuthenticationPrincipal User principal` -> `200 OK` with `List<TournamentRegistrationResponse>`.
  - [x] Backend Unit, Slice & Listener Tests:
    - `src/test/java/com/tictactore/service/TournamentRegistrationServiceTest.java` (unit tests using strict AAA pattern, no section comments).
    - `src/test/java/com/tictactore/controller/TournamentRegistrationControllerTest.java` (WebMvcTest).
    - `src/test/java/com/tictactore/listener/TournamentRegistrationNotificationListenerTest.java`.

- [x] Task 3: Frontend Types, Service, Store & i18n (AC1–AC8)
  - [x] Create/Update TypeScript types `frontend/src/features/tournament/types/tournament.ts`:
    - `type RegistrationStatus = 'PENDING_CONFIRMATION' | 'CONFIRMED' | 'DECLINED' | 'CANCELLED'`
    - `interface TournamentRegistrationDto`
    - `interface RegisterTournamentPayload`
    - `interface MyRegistrationStatusDto`
  - [x] Create API service `frontend/src/features/tournament/services/tournamentRegistrationService.ts`:
    - `registerForTournament(tournamentId: string, payload: RegisterTournamentPayload): Promise<TournamentRegistrationDto>`
    - `getTournamentRegistrations(tournamentId: string, status?: RegistrationStatus): Promise<TournamentRegistrationDto[]>`
    - `getMyRegistration(tournamentId: string): Promise<MyRegistrationStatusDto>`
    - `acceptInvitation(tournamentId: string, registrationId: string): Promise<TournamentRegistrationDto>`
    - `declineInvitation(tournamentId: string, registrationId: string): Promise<TournamentRegistrationDto>`
    - `cancelRegistration(tournamentId: string, registrationId: string): Promise<void>`
    - `getPendingInvitations(): Promise<TournamentRegistrationDto[]>`
  - [x] Update Pinia store `frontend/src/features/tournament/stores/tournamentStore.ts`:
    - State: `registrations: Record<string, TournamentRegistrationDto[]>`, `myRegistrations: Record<string, MyRegistrationStatusDto>`, `pendingInvitations: TournamentRegistrationDto[]`
    - Actions: `register(tournamentId, payload)`, `acceptInvite(tournamentId, registrationId)`, `declineInvite(tournamentId, registrationId)`, `cancelRegistration(tournamentId, registrationId)`, `fetchRegistrations(tournamentId, status?)`, `fetchMyRegistration(tournamentId)`, `fetchPendingInvitations()`
  - [x] Add i18n translation keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json` under `tournament.registration.*` namespace (modal titles, partner selector placeholder, partner required warning, status badges, accept/decline action labels, success toasts, error messages).
  - [x] Frontend store tests in `frontend/src/features/tournament/stores/__tests__/tournamentRegistrationStore.spec.ts`.

- [x] Task 4: Frontend UI Components & Views (AC1, AC2, AC3, AC4, AC5, AC8)
  - [x] Create `frontend/src/features/tournament/components/TournamentRegistrationModal.vue`:
    - Clubhouse design token styling (`bg-surface-container-low`, `rounded-2xl`, elevation, no 1px solid borders per `UX-DR3`).
    - Dynamic mode-dependent UI:
      - For 1v1 or 2v2 random pairings: Solo registration confirmation prompt.
      - For 2v2 fixed teams: Partner search/selector input (with avatar, nickname, and favorites quick-picker).
    - Validation feedback, loading state on submit button, close on outside click or cancel.
  - [x] Create `frontend/src/features/tournament/components/TournamentInviteModal.vue` / `TournamentInviteCard.vue`:
    - Invitation card/modal displaying inviter info, tournament name, format, mode, and rule set.
    - "Accept" (primary) and "Decline" (secondary/tonal) action buttons with loading states.
  - [x] Create `frontend/src/features/tournament/components/TournamentRoster.vue`:
    - Display list of confirmed participants/teams, count vs `maxParticipants`, pending invites indicator.
  - [x] Update `frontend/src/features/tournament/views/TournamentsView.vue`:
    - Add "Register" CTA button on open tournament cards when current user is not registered.
    - Display "Registered" / "Invite Pending" status badge on cards.
    - Pending Invitations banner at the top of the view when user has pending invites.
  - [x] Component unit tests:
    - `frontend/src/features/tournament/components/__tests__/TournamentRegistrationModal.spec.ts`.
    - `frontend/src/features/tournament/components/__tests__/TournamentInviteModal.spec.ts`.

- [x] Task 5: Testing & Quality Verification
  - [x] Backend Unit & Slice Tests:
    - `TournamentRegistrationServiceTest.java` (strict AAA without section comments).
    - `TournamentRegistrationControllerTest.java` (WebMvcTest).
    - `TournamentRegistrationRepositoryTest.java` (@DataJpaTest).
    - `TournamentRegistrationNotificationListenerTest.java`.
  - [x] Frontend Unit/Component Tests:
    - `tournamentRegistrationStore.spec.ts`.
    - `TournamentRegistrationModal.spec.ts`.
    - `TournamentInviteModal.spec.ts`.
  - [x] E2E Playwright Tests:
    - Create `frontend/e2e/tournament-registration.spec.ts`:
      - Test 1 (1v1 Solo Registration): User registers for 1v1 tournament -> registration is confirmed -> tournament card shows "Registered".
      - Test 2 (2v2 Partner Invite & Acceptance): User A registers with User B -> User B sees pending invite -> User B accepts -> both see confirmed team registration.
      - Test 3 (2v2 Partner Decline): User A registers with User B -> User B declines -> status changes to declined -> User A can register again with another partner.
      - Test 4 (Capacity & Deadline Guards): Registration button is disabled or rejected when capacity is full or deadline has passed.
  - [x] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails

- **Package Layout & Layering (code-1-guide):**
  - Model: `com.tictactore.model`
  - Repository: `com.tictactore.repository`
  - Service: `com.tictactore.service` & `com.tictactore.service.impl`
  - Controller: `com.tictactore.controller`
  - DTO: `com.tictactore.dto`
  - Event: `com.tictactore.event`
  - Listener: `com.tictactore.listener`
- **Database & Concurrency Invariants:**
  - Flyway migration script: `src/main/resources/db/migration/V19__create_tournament_registration_tables.sql`.
  - `@Version private Long version;` on `TournamentRegistration` entity for optimistic locking (without `@Column`, as per `code-1-guide` rule 2).
  - Foreign keys:
    - `tournament_id REFERENCES tournament(id) ON DELETE CASCADE`
    - `player_id REFERENCES "user"(id) ON DELETE CASCADE`
    - `partner_id REFERENCES "user"(id) ON DELETE CASCADE`
  - Partial unique indexes in PostgreSQL ensure database-level enforcement of at most one active registration per user per tournament.
- **Event-Driven Push Notifications:**
  - Domain events (`TournamentInviteCreatedEvent`, `TournamentInviteAcceptedEvent`, `TournamentInviteDeclinedEvent`) published via `ApplicationEventPublisher`.
  - Listener uses `@Async @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` to decouple notification dispatch from database transaction commits.
- **API Contracts & Security:**
  - Base path: `/api/v1/tournaments/{tournamentId}/registrations`
  - Security: Authenticated requests only. `@AuthenticationPrincipal User principal` for actor identity.
  - Authorization:
    - Only `partner_id` can accept or decline a pending registration.
    - Only `player_id` or `partner_id` can cancel/withdraw an active registration.
- **UX & Design Invariants:**
  - **Clubhouse Design Tokens (UX-DR3):** Strictly adhere to Clubhouse "No-Line" rule: tonal shifts (`bg-surface-container-low`, `bg-surface-container-high`) and elevation/shadows instead of 1px solid border lines.
  - **500-Line Rule (IP-04):** All new and updated files must stay strictly under 500 lines.
- **Testing Standards (code-2-test):**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line, with zero structural comments (`// Given`, `// When`, `// Then` are strictly forbidden).
  - Integration tests end with `IT` or `ATDDTest` / `Test` for unit tests.

### ATDD Artifacts

- **Checklist:** `_bmad-output/test-artifacts/atdd-checklist-8-2-team-registration-and-confirmation.md`
- **Backend API Scaffolds:** `_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentRegistrationControllerATDDTest.java`
- **Frontend E2E Scaffolds:** `frontend/e2e/tournament-registration.spec.ts`
- **Frontend Store Scaffolds:** `_bmad-output/test-artifacts/atdd-redphase-8-2/tournamentRegistrationStore.spec.ts`
- **Frontend Component Scaffolds:**
  - `_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentRegistrationModal.spec.ts`
  - `_bmad-output/test-artifacts/atdd-redphase-8-2/TournamentInviteModal.spec.ts`
- **Fixtures:** `frontend/e2e/fixtures/tournament-registration-data.ts`

### References

- [Source: _bmad-output/planning-artifacts/prd.md#FR42]
- [Source: _bmad-output/planning-artifacts/epics.md#Epic-8]
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md#Tournaments]
- [Source: /Users/ppolukhin/.agents/skills/code-1-guide/SKILL.md]
- [Source: /Users/ppolukhin/.agents/skills/code-2-test/SKILL.md]

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash

### Completion Notes List

- Comprehensive story context validated and optimized for dev-story execution.
- Configured exact Flyway migration version `V19__create_tournament_registration_tables.sql`.
- Modeled support for all tournament formats/modes (1v1 solo, 2v2 fixed teams with partner invite/accept/decline, 2v2 random pairings).
- Specified backend model package `com.tictactore.model`, service layer, DTO records, controller endpoints, events, async push notification listener, and frontend Vue/Pinia feature architecture.
- Added strict AAA testing requirements, Playwright E2E test plan, and `./scripts/ci-local.sh` verification gate.

### File List

- `src/main/resources/db/migration/V19__create_tournament_registration_tables.sql` (NEW)
- `src/main/java/com/tictactore/model/RegistrationStatus.java` (NEW)
- `src/main/java/com/tictactore/model/TournamentRegistration.java` (NEW)
- `src/main/java/com/tictactore/repository/TournamentRegistrationRepository.java` (NEW)
- `src/main/java/com/tictactore/dto/RegisterTournamentRequest.java` (NEW)
- `src/main/java/com/tictactore/dto/TournamentRegistrationResponse.java` (NEW)
- `src/main/java/com/tictactore/dto/MyRegistrationStatusResponse.java` (NEW)
- `src/main/java/com/tictactore/event/TournamentInviteCreatedEvent.java` (NEW)
- `src/main/java/com/tictactore/event/TournamentInviteAcceptedEvent.java` (NEW)
- `src/main/java/com/tictactore/event/TournamentInviteDeclinedEvent.java` (NEW)
- `src/main/java/com/tictactore/listener/TournamentRegistrationNotificationListener.java` (NEW)
- `src/main/java/com/tictactore/service/TournamentRegistrationService.java` (NEW)
- `src/main/java/com/tictactore/service/TournamentRegistrationServiceImpl.java` (NEW)
- `src/main/java/com/tictactore/controller/TournamentRegistrationController.java` (NEW)
- `src/main/java/com/tictactore/service/PushNotificationService.java` (UPDATE)
- `src/main/java/com/tictactore/service/impl/PushNotificationServiceImpl.java` (UPDATE)
- `frontend/src/features/tournament/types/tournament.ts` (UPDATE)
- `frontend/src/features/tournament/services/tournamentRegistrationService.ts` (NEW)
- `frontend/src/features/tournament/stores/tournamentStore.ts` (UPDATE)
- `frontend/src/features/tournament/components/TournamentRegistrationModal.vue` (NEW)
- `frontend/src/features/tournament/components/TournamentInviteModal.vue` (NEW)
- `frontend/src/features/tournament/components/TournamentRoster.vue` (NEW)
- `frontend/src/features/tournament/views/TournamentsView.vue` (UPDATE)
- `frontend/src/locales/en.json` (UPDATE)
- `frontend/src/locales/de.json` (UPDATE)
- `src/test/java/com/tictactore/service/TournamentRegistrationServiceTest.java` (NEW)
- `src/test/java/com/tictactore/controller/TournamentRegistrationControllerTest.java` (NEW)
- `src/test/java/com/tictactore/repository/TournamentRegistrationRepositoryTest.java` (NEW)
- `src/test/java/com/tictactore/listener/TournamentRegistrationNotificationListenerTest.java` (NEW)
- `frontend/src/features/tournament/stores/__tests__/tournamentRegistrationStore.spec.ts` (NEW)
- `frontend/src/features/tournament/components/__tests__/TournamentRegistrationModal.spec.ts` (NEW)
- `frontend/src/features/tournament/components/__tests__/TournamentInviteModal.spec.ts` (NEW)
- `frontend/e2e/tournament-registration.spec.ts` (NEW)
