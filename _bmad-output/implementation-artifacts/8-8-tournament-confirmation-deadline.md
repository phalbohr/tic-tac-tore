# Story 8.8: Tournament Confirmation Deadline

Status: ready-for-dev

## Story

As a system, 
I want to enforce confirmation deadlines, 
so that the tournament isn't blocked.

## Acceptance Criteria

1. **Given** a tournament match is submitted for confirmation
2. **When** the 48-hour tournament confirmation window expires without opponent action
3. **Then** the system automatically applies a technical defeat to the unresponsive party or auto-confirms based on the rules (FR18)

## Tasks / Subtasks

- [ ] Task 1: Schedule detection of expired unconfirmed tournament matches (AC: 1, 2)
  - [ ] Add query to `TournamentMatchRepository` to find `IN_PROGRESS` tournament matches where the core `Match` is `PENDING_APPROVAL` (or `PARTIALLY_CONFIRMED`) and the `Match.createdAt` (or `updatedAt`) is older than 48 hours.
  - [ ] Extend `TournamentScheduler` to periodically run this query and process these matches.
- [ ] Task 2: Implement auto-confirmation / technical defeat (AC: 3)
  - [ ] Add logic in `TournamentMatchServiceImpl` (or `MatchServiceImpl`) to forcefully confirm the match on behalf of the system.
  - [ ] Ensure the tournament bracket advances normally (by letting the `TournamentMatchEventListener` react to the core match confirmation, or by directly triggering completion).
- [ ] Task 3: Write automated tests
  - [ ] Add unit tests for the scheduler logic.
  - [ ] Add integration/component tests to verify the 48-hour expiration triggers bracket progression.

## Dev Notes

- **Zero Comments Policy (`code-1-guide`, `code-4-document`)**: Do NOT add explanatory comments, Javadoc, or inline comments to production code unless specifically mandated. Code must be self-documenting.
- **Architecture Compliance**: Utilize existing scheduled tasks framework. The `TournamentScheduler` is already present (`src/main/java/com/tictactore/scheduler/TournamentScheduler.java`) and should be extended rather than creating a new scheduler.
- **Domain Modeling**: A "technical defeat" in this context is best handled by accepting the submitted match result (auto-confirming) since the unresponsive party failed to reject it within 48 hours. Ensure that `Match` domain validation allows system-level confirmation (which might bypass the opponent user ID check, or pass a system ID).
- **Previous Learnings (from Story 8.7)**: Tournament completion is detected in `TournamentMatchServiceImpl.completeMatch`. Standings and archives are updated on completion. Auto-confirming a match must trigger the standard completion pathways to ensure standings are recalculated.

### Project Structure Notes

- `src/main/java/com/tictactore/scheduler/TournamentScheduler.java`
- `src/main/java/com/tictactore/repository/TournamentMatchRepository.java`
- `src/main/java/com/tictactore/service/tournament/impl/TournamentMatchServiceImpl.java`

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 8.8]
- [Source: _bmad-output/planning-artifacts/prd.md#FR18]
- [Source: src/main/java/com/tictactore/scheduler/TournamentScheduler.java]

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)

### Debug Log References

### Completion Notes List

### File List
