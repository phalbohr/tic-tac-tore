# Story 8.7: Tournament Standings & Archive

Status: ready-for-dev

## Story

As a player,
I want to see the tournament results,
so that I can track progress and historical winners.

## Acceptance Criteria

1. **Given** an active or completed tournament
2. **When** a user views the tournament details page
3. **Then** the system displays live standings updated after every confirmed match (FR26)
4. **And** completed tournaments are permanently retained in an accessible archive (FR46)

## Tasks / Subtasks

- [ ] Task 1: Backend - Tournament Standings API
  - [ ] Create endpoint to fetch live standings for a given tournament ID
  - [ ] Calculate standings dynamically based on confirmed match results associated with the tournament
  - [ ] Return structured response with teams, matches played, points, and rank
- [ ] Task 2: Backend - Tournament Archive API
  - [ ] Ensure tournaments can be marked as 'completed'
  - [ ] Create endpoint to fetch paginated list of completed tournaments for the archive view
- [ ] Task 3: Frontend - Live Standings View
  - [ ] Implement Vue 3 component for the tournament details page to display standings table
  - [ ] Integrate with the standings API and handle live/refreshed data
- [ ] Task 4: Frontend - Tournament Archive View
  - [ ] Implement Vue 3 page/component for browsing historical/completed tournaments
  - [ ] Add navigation from the main tournaments page to the archive

## Dev Notes

- **Architecture Constraints**:
  - The backend uses **Spring Boot 4.0** and the frontend uses **Vue 3 PWA** with **Vite 8**.
  - All APIs must be stateless and secured via JWT.
  - Follow the existing layered architecture (Controllers -> Services -> Repositories).
  - Standings should probably be calculated on-the-fly or cached if calculations are heavy, but for this MVP, an efficient DB query or service-level aggregation of confirmed matches is required.
  - Make sure to consider the existing `RuleConfiguration` when calculating points, if applicable to tournaments.
- **Data Retention (GDPR)**:
  - Account deletion (FR33) replaces user references with "Anonymous". Ensure the standings and archive pages handle anonymized users gracefully without breaking the UI.
- **Frontend Guidelines**:
  - PWA best practices: ensure the standings table is responsive and usable on mobile.
  - Use internationalization (i18n) for all text (EN/DE support).

### Project Structure Notes

- Alignment with unified project structure: Place Vue components in appropriate features directory.
- Spring Boot Controllers should be placed under `tournament` or `analytics` depending on the domain boundary.

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Epic 8]
- [Source: _bmad-output/planning-artifacts/architecture.md#Requirements Overview]
