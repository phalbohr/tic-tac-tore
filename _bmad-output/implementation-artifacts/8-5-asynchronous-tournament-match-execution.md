# Story 8.5: Asynchronous Tournament Match Execution

## Context
**Epic:** 8. Tournaments
**Story:** 8.5
**Status:** ready-for-dev

This story allows players to play tournament matches flexibly without being bound by a strict global schedule. By relying on the asynchronous match generation from Story 8.3 and the general match verification flow, participants can independently pick their available opponents from their generated list, start a match, and enter the results. This improves the real-world flow of tournaments, preventing bottlenecks when tables are free but a rigid schedule blocks progress.

## User Story
As a participant, I want to play tournament matches flexibly, so that the event proceeds smoothly without bottlenecks.

## Acceptance Criteria (BDD)

- **Scenario 1: Starting a pending match out of sequence**
  - **Given** a tournament is active with generated matches available for a player
  - **When** the player views their pending tournament matches
  - **Then** they can start and record any of their assigned pending matches without adhering to a global round-by-round schedule (FR44)
  - **And** the match entry flow is initiated for the selected opponents.

- **Scenario 2: Viewing opponent availability**
  - **Given** the tournament matches list
  - **When** checking pending matches
  - **Then** matches where the opponent is currently involved in another active match are visually marked (e.g., "Opponent Busy").

## Tasks

- [ ] Update `TournamentMatchRepository` to fetch all pending matches for a given participant without strict round filtering.
- [ ] Implement service logic in `TournamentLifecycleService` or `TournamentMatchService` to validate that a selected match can be started (e.g. neither participant is currently playing another active match).
- [ ] Create endpoint `POST /api/v1/tournaments/{id}/matches/{matchId}/start` to transition a tournament match to an active state and initialize the standard match entry flow.
- [ ] Enhance the frontend `TournamentMatchList` component to display all available matches for the current user and enable starting them asynchronously.
- [ ] Add visual indicators for opponent availability in the UI.
- [ ] Unit Tests:
  - `TournamentMatchServiceTest.java` (Validation for starting matches).
- [ ] Frontend Tests:
  - `TournamentMatchList.spec.ts` (Displaying available matches, starting matches).
- [ ] Verification: Execute `./scripts/ci-local.sh` and ensure 100% pass rate.

## Dev Notes

### Architecture & Implementation Guardrails
- **API Boundary:** New endpoints must use `/api/v1/tournaments/...` and return standardized JSON responses.
- **Data Architecture:** The underlying `TournamentMatch` entity should decouple from strict sequential rounds if asynchronous play is allowed.
- **500-Line Rule (IP-04):** Ensure `TournamentMatchService.java` does not exceed 500 lines. Refactor out specific asynchronous selection rules if needed.
- **Code Style (5-style):** Keep methods concise.

### References
- [Source: _bmad-output/planning-artifacts/prd.md#Functional Requirements] (FR44)
- [Source: _bmad-output/planning-artifacts/epics.md] (Epic 8, Story 8.5)
