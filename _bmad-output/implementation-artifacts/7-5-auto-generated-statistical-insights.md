# Story 7.5: Auto-generated Statistical Insights

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to receive insights,
so that I can understand my improvement over time.

## Acceptance Criteria

1. **Given** a player has sufficient match data
2. **When** the system analyzes their performance
3. **Then** it provides auto-generated statistical insights and progress tracking (FR53)

## Tasks / Subtasks

- [ ] Task 1: Create Statistics Analysis Engine
  - [ ] Implement backend service for calculating player insights.
  - [ ] Create DTOs and endpoints to expose insights to frontend.
- [ ] Task 2: Build Insight UI
  - [ ] Create Vue components for rendering insights and progress over time.
  - [ ] Add localization for insight messages.
- [ ] Task 3: Testing & Verification
  - [ ] Write ATDD/unit tests for the backend analysis engine.
  - [ ] Write component and E2E tests for the frontend UI.

## Dev Notes

- Relevant architecture patterns and constraints:
  - Adhere to the established modular monolith pattern.
  - Zero DB Write Amplification: Calculate insights on-the-fly where possible or use scheduled background jobs if computationally expensive. Ensure no redundant columns are created.
  - The 500-Line Rule (IP-04): Keep components small.

### Project Structure Notes

- Alignment with unified project structure: Follow the existing patterns in `src/main/java/com/tictactore/service/` and `frontend/src/features/`.

### References

- Epic 7 (Engagement & Polish): Engagement metrics via FR53.

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)
