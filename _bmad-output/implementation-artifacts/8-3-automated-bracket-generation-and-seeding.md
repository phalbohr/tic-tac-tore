---
status: ready-for-dev
---

# Story 8.3: Automated Bracket Generation & Seeding

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As a system,
I want to auto-generate the tournament bracket and schedule initial matches when a tournament starts,
so that matches are organized fairly based on participant strength and format.

## Acceptance Criteria

1. **Given** one or more tournaments in `REGISTRATION_OPEN` status where the `registrationDeadline` is in the past
   **When** the periodic tournament starter scheduled job executes
   **Then** it selects these tournaments and initiates the tournament start routine for each, transitioning their status to `IN_PROGRESS`.
2. **Given** a tournament starting with mode `ONE_VS_ONE_PERSONAL` or `TWO_VS_TWO_FIXED_TEAMS` (excluding random pairings)
   **When** the bracket generation executes
   **Then** it retrieves all `CONFIRMED` registrations, algorithmically evaluates their strength (using a configurable `SeedingStrategy`, falling back to random/registration-time if strength is equal), and seeds the participants (`FR43`).
3. **Given** a seeded list of participants for a `CUP` format tournament
   **When** the system generates initial matches
   **Then** it creates `TournamentMatch` entities mapping seeds to a standard single-elimination bracket structure (e.g. 1 vs 16, 2 vs 15, etc.). Unfilled slots due to participant counts not being a power of two are treated as BYEs.
4. **Given** a seeded list of participants for a `CHAMPIONSHIP` (Round Robin / Swiss) format tournament
   **When** the system generates initial matches
   **Then** it creates `TournamentMatch` entities for Round 1 based on the selected format's pairing logic.
5. **Given** a tournament starting without enough `CONFIRMED` participants to meet the `minParticipants` threshold
   **When** the tournament start routine executes
   **Then** the tournament status transitions to `CANCELLED` instead of `IN_PROGRESS`, and appropriate push notifications are sent to the registered participants.
6. **Given** a tournament has successfully started and matches generated
   **When** the process completes
   **Then** push notifications are sent to all participating players that the tournament has started and their first matches are available.
7. **Given** a user viewing an `IN_PROGRESS` tournament in the frontend
   **When** they navigate to the "Bracket" or "Matches" tab
   **Then** they see the generated match tree/schedule populated with the correct seeded participants.

## Dev Notes

### Previous Learnings & Constraints
- Keep all files under 500 lines.
- `TournamentRegistrationServiceImpl` had race conditions; use pessimistic locking if manipulating state of `Tournament`. Since this is a scheduled job that modifies the tournament state, a `SELECT ... FOR UPDATE` (PESSIMISTIC_WRITE) on the Tournament entity is highly recommended when transitioning status.
- Strict AAA testing format without structural comments (`// Given`, etc.).

### Architecture & Technical Requirements
- **Scheduled Job**: Create a Spring `@Scheduled` task (e.g., runs every minute) in a `TournamentScheduler` component that finds `REGISTRATION_OPEN` tournaments where `deadline < now()`.
- **Seeding Strategy Interface**: Create a `TournamentSeedingStrategy` interface to allow swapping algorithms later (since the PRD mentions a pending spike). For this story, implement a `BasicStrengthSeedingStrategy` (can use total wins, or random if no stats available).
- **Match Generation**: The logic to generate a Cup bracket or Championship rounds can be complex. Implement these in dedicated strategy classes (e.g., `CupBracketGenerator`, `ChampionshipBracketGenerator`).
- **Entity**: `TournamentMatch` should link to the `Tournament`, the `Match` (from the core match domain, if existing), the `TournamentRegistration` participants, and round number / bracket position. Wait, if `Match` is the core entity, `TournamentMatch` could just be a wrapper or extra fields on `Match`. Let's assume a `TournamentMatch` entity that links a core `Match` or holds its own state. *Recommendation*: Create a `TournamentMatch` entity specifically for tournament bracket tracking, referencing standard `Match` when played. Or add `tournament_id` and `round` to standard `Match`. Follow existing patterns.
- **Push Notifications**: Use existing `PushNotificationService` to send notifications to participants when the tournament is started or cancelled.
- **Frontend**: Update `TournamentDetails.vue` (or equivalent) to display the Bracket/Matches.

