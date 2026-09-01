# Story 8.2: Team Registration & Confirmation

Status: ready-for-dev

## Story

As a player,
I want to register for a tournament with a partner,
so that we can compete.

## Acceptance Criteria

1. **Given** an open tournament in the registration phase
   **When** a player submits a registration requesting a specific partner
   **Then** the partner receives an immediate push notification to accept the invite
2. **Given** a registration request is pending
   **When** the partner accepts the invite
   **Then** the team's registration is marked as complete (FR42)
3. **Given** a registration request is pending
   **When** the partner declines the invite
   **Then** the registration is cancelled and the original player is notified

## Tasks / Subtasks

- [ ] Backend: Entity & Migration
  - [ ] Create Flyway migration for `tournament_registration` table
  - [ ] Create `TournamentRegistration` entity with `playerId`, `tournamentId`, `partnerId`, `status`
- [ ] Backend: Service & Controller
  - [ ] Implement `TournamentRegistrationService` logic for creating and updating registrations
  - [ ] Implement `TournamentRegistrationController` endpoints for POST register, POST accept, POST decline
  - [ ] Integrate with existing push notification service to alert partner
- [ ] Frontend: Registration Flow
  - [ ] Create `TournamentRegistrationModal` component to select a partner and register
  - [ ] Create `TournamentInviteModal` component to accept/decline invites
  - [ ] Add registration status display in `TournamentsView`
- [ ] Verification
  - [ ] Execute `./scripts/ci-local.sh` and ensure 100% pass rate

## Dev Notes

### Architecture & Implementation Guardrails

- **Database & Concurrency Invariants:**
  - Flyway migration script: `src/main/resources/db/migration/V19__create_tournament_registration_tables.sql`.
  - Foreign keys:
    - `tournament_id REFERENCES tournament(id) ON DELETE CASCADE`
    - `player_id REFERENCES "user"(id) ON DELETE CASCADE`
    - `partner_id REFERENCES "user"(id) ON DELETE CASCADE`
  - Unique constraint on `(tournament_id, player_id)` and `(tournament_id, partner_id)` to prevent double registration.
- **API Contracts & Security:**
  - Base path: `/api/v1/tournaments/{id}/registrations`
  - Validation: Tournament must be in `REGISTRATION` status.
- **Testing Standards (code-2-test):**
  - **Strict AAA Pattern:** All test methods adhere to Arrange-Act-Assert separated by a single blank line.

### References

- [Source: _bmad-output/planning-artifacts/prd.md#FR42]
- [Source: _bmad-output/planning-artifacts/epics.md#Epic-8]
- [Source: /Users/ppolukhin/.agents/skills/code-1-guide/SKILL.md]

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)

### Completion Notes List

- Comprehensive story context validated and optimized for dev-story execution.
- Configured exact Flyway migration version `V19__create_tournament_registration_tables.sql`.

### File List

- `src/main/resources/db/migration/V19__create_tournament_registration_tables.sql` (NEW)
- `src/main/java/com/tictactore/model/TournamentRegistration.java` (NEW)
- `src/main/java/com/tictactore/model/RegistrationStatus.java` (NEW)
- `src/main/java/com/tictactore/repository/TournamentRegistrationRepository.java` (NEW)
- `src/main/java/com/tictactore/service/TournamentRegistrationService.java` (NEW)
- `src/main/java/com/tictactore/service/TournamentRegistrationServiceImpl.java` (NEW)
- `src/main/java/com/tictactore/controller/TournamentRegistrationController.java` (NEW)
- `frontend/src/features/tournament/components/TournamentRegistrationModal.vue` (NEW)
- `frontend/src/features/tournament/components/TournamentInviteModal.vue` (NEW)
