# Story 5.5: Screen Wake Lock & Continuity

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want the screen to stay on,
so that I don't have to touch it between goals.

## Acceptance Criteria

- **Given** live match starts
- **When** the match is active
- **Then** system prevents screen dimming (FR11)
- **And** wake lock released on match finish

## Tasks / Subtasks

- [ ] Task 1: Implement Screen Wake Lock API
  - [ ] Request wake lock on live match start
  - [ ] Handle wake lock release on match finish
  - [ ] Add fallback mechanism or graceful degradation if API is unsupported

## Dev Notes

- Relevant architecture patterns and constraints:
  - **AD-06: PWA-First Infrastructure:** Use of Screen Wake Lock API (Live Mode) for high-quality app experience without app store overhead.
- Source tree components to touch:
  - `frontend/src/features/match/` components (live match view)
- Testing standards summary:
  - Follow the 500-Line Rule (IP-04).

### Project Structure Notes

- Alignment with unified project structure (paths, modules, naming):
  - Frontend Vue components in PascalCase, functions in camelCase.
- Detected conflicts or variances (with rationale): None

### References

- [Architecture Document: AD-06 PWA-First Infrastructure](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/planning-artifacts/architecture.md#Analytics-&-UI)

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)

### Debug Log References

### Completion Notes List

### File List

