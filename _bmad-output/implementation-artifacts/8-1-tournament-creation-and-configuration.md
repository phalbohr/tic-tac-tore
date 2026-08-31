---
baseline_commit: None
---

# Story 8.1: Tournament Creation & Configuration

Status: ready-for-dev

<!-- Note: Comprehensive story context validated and optimized for dev-story execution. -->

## Story

As an organizer,
I want to configure tournament parameters,
so that I can set up a structured competition.

## Acceptance Criteria

1. **Given** a user is logged in and navigates to the Tournaments section
   **When** they submit the "Create Tournament" form with valid parameters
   **Then** a new tournament is persisted with the configured format, mode, rule system, participant limits, and registration deadlines (`FR41`).
2. **Given** the form includes mode and format parameters
   **When** the user configures the tournament
   **Then** they can choose format (e.g., cup/championship) and mode (1v1 personal, 2v2 fixed teams, 2v2 random pairings).
3. **Given** the tournament is persisted
   **When** the configuration is saved
   **Then** the selected rule system, min/max participants, registration deadline, round count, and optional playoff are all recorded.

## Tasks / Subtasks

- [ ] Task 1: Create Tournament Entity and DB Schema
  - [ ] Subtask 1.1: Define `Tournament` JPA Entity mapping to a new `tournaments` table.
  - [ ] Subtask 1.2: Add Flyway migration for the schema.
  - [ ] Subtask 1.3: Create `TournamentRepository` interface.
- [ ] Task 2: Implement Tournament Creation API
  - [ ] Subtask 2.1: Create `TournamentDto` and `CreateTournamentRequest` objects with validation.
  - [ ] Subtask 2.2: Implement `TournamentService` for creation logic.
  - [ ] Subtask 2.3: Create `TournamentController` with POST endpoint.
- [ ] Task 3: Develop Frontend Tournament Creation Flow
  - [ ] Subtask 3.1: Create `TournamentCreate.vue` in `src/features/tournament/`.
  - [ ] Subtask 3.2: Implement form validation for tournament parameters (mode, format, rules, deadlines).
  - [ ] Subtask 3.3: Integrate with backend API using Pinia store or composables.

## Dev Notes

- **Architecture Constraints**: Adhere to the feature-based folder structure. Backend classes and frontend components must stay under 500 lines.
- **Rule System**: Ensure the tournament configuration is tightly linked to an immutable `RuleConfiguration`.
- **Naming Conventions**: Use consistent `ch-` prefix for custom styles if applicable.

### Project Structure Notes

- New backend entities should reside in the `domain` package or a specific `features/tournament` bounded context if using modular monolith architecture.
- Frontend: New feature folder `src/features/tournament/` containing the UI components and related state management.

### References

- [Source: _bmad-output/planning-artifacts/prd.md#FR41]

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro

### Completion Notes List

- Comprehensive story context created.
