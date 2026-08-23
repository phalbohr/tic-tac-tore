# Story 6.1: Named Player Groups ("Teams")

Status: ready-for-dev

## Story

As a player,
I want to create named groups of people I frequently play with,
so that I can filter stats and matches easily.

## Acceptance Criteria

- **Given** a player navigates to their teams management page (or inline creation flow)
- **When** they create a new group and add selected players
- **Then** the system persists this named group
- **And** the group becomes available as a filter in the statistics views and match creation player selectors (FR39)
- **And** the group becomes available as a filter in the Match History view (from Epic 4)

## Developer Context

This story introduces the concept of named player groups (Teams) for filtering and matchmaking (FR39).
According to the UX design specification, there should NOT be a standalone "Teams & Rules" admin screen. Group management must happen **inline during match creation or in Settings**.

### Technical Requirements & Guardrails

- **Backend API & Database:** Create a new entity (e.g., `PlayerGroup`) to store the group name, its creator, and its members. Use a `player_group` table and a mapping table `player_group_members`.
- **Database Migrations:** Create a Flyway migration for the new tables using UUIDs for primary keys.
- **Backend Validation:** Ensure that groups can only be modified or deleted by their creator. Ensure appropriate endpoints are exposed for creating, reading, updating, and deleting groups.
- **UX Integration:** Update the Match Creation player selector and the Match History filter (from Epic 4) to support filtering by the newly created groups.
- **Inline Creation:** Add a mechanism to create a new group directly from the settings or player selection contexts, avoiding a separate dedicated management screen.

### Architecture Compliance

- **Feature-Based Structure:** Group the new backend classes under a new `group` or `team` feature package (e.g. `com.tictactore.feature.group`), and similarly for frontend components (`frontend/src/features/group/`).
- **Data Integrity (AD-01):** Ensure that group associations do not retroactively alter the history of played matches.
- Follow the **500-Line Rule (IP-04)** for all new files. Keep components small and focused.

### Library & Framework Requirements

- **Backend:** Spring Boot, Spring Data JPA.
- **Frontend:** Vue 3 (Composition API), Pinia for state management, Tailwind CSS v4.

### Testing Requirements

- **Strict AAA Pattern:** All new tests (backend and frontend) MUST follow the Arrange-Act-Assert pattern separated by a single blank line, with **absolutely zero structural comments** (like `// Arrange`, `// Act`, `// Assert`).
- **E2E Testing:** Add a Playwright test verifying the inline group creation flow, and verify that the new group appears as a filter in match history or player selection.
- Run `./scripts/ci-local.sh` to ensure all checks pass before marking as complete.

## Previous Story Intelligence

- Recent work on Epic 5 (e.g. `useWakeLock` and LiveMatch components) highlighted the importance of robust state management and cleanup. Ensure that state relating to selected groups in Pinia stores is correctly cleared or preserved depending on component lifecycle.
- **Transaction Boundaries:** Be careful with `@Version` and optimistic locking when persisting complex relationships.
- Ensure strict AAA pattern compliance in all tests, as this is frequently flagged in code review.

## Project Context Reference

- PRD: FR39 (Named player groups)
- UX Design Specification: "Teams & Rules: no dedicated screen | All management inline during match creation or in Settings"
- Architecture: AD-01 Immutable RuleConfiguration context, Feature-Based Layout

## Status

Status: ready-for-dev
