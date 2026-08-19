---
baseline_commit: current
---

# Story 4.6: Unified Match History (My Matches)

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to view my match history,
so that I can track my recent performance.

## Acceptance Criteria

1. **Given** the player navigates to their match history (`/history` or equivalent)
   **When** the view loads
   **Then** it displays tabs for **Confirmed** and **Pending** matches (FR60)
   **And** it paginates the results using standard offset or cursor-based pagination
2. **Given** the player views the match history
   **When** they want to refine the list
   **Then** the system provides options to filter by all players (selecting specific opponents or teammates)
3. **Given** the match history lists matches
   **When** viewing the list items
   **Then** the UI follows the "No-Line" rule (UX-DR3) and uses `ch-` prefixed SCSS classes
   **And** pending matches display a status indicator (e.g., waiting for confirmation)
   **And** confirmed matches display final scores and whether it was a win, loss, or draw
4. **Given** the current user has < 1 confirmed match or demo mode is enabled
   **When** viewing match history
   **Then** realistic demo data is generated via `demoDataGenerator.ts`

## Tasks / Subtasks

- [ ] Task 1: Backend API & DTOs (AC1, AC2)
  - [ ] Define `MatchHistoryResponseDto.java`, `MatchHistoryItemDto.java` in `com.tictactore.dto`.
  - [ ] Add `GET /api/v1/matches/history` endpoint in `MatchController`.
  - [ ] Support query parameters: `status` (CONFIRMED/PENDING), `playerId` (filter by other player), `page`, and `size`.
- [ ] Task 2: Backend Repository & Service (AC1, AC2)
  - [ ] Implement query in `MatchRepository` to fetch paginated match history for a user, filtering by status and optional `playerId`.
  - [ ] Add `getMatchHistory(...)` to `MatchService` and `MatchServiceImpl`.
  - [ ] Enforce AD-02 (Isolated Verification Pipeline) ensuring correct status filtering (PENDING vs CONFIRMED/PUBLISHED).
- [ ] Task 3: Frontend Store & API Client (AC1, AC2)
  - [ ] Add `getMatchHistory` to `frontend/src/services/matchService.ts`.
  - [ ] Create or update `useHistoryStore.ts` (or `useMatchStore.ts`) to manage pagination, filters (status, playerId), and match list state.
- [ ] Task 4: Frontend Components (AC1, AC2, AC3, AC4)
  - [ ] Create `MatchHistoryView.vue` with tabs for Confirmed and Pending matches.
  - [ ] Create `MatchHistoryList.vue` and `MatchHistoryItem.vue`.
  - [ ] Implement Opponent/Player filter using existing `AvatarInteractive` or player selector.
  - [ ] Apply "No-Line" styling (UX-DR3) and `ch-` prefixes.
  - [ ] Integrate translations (en.json, de.json) for all tabs and statuses.
- [ ] Task 5: Testing & Quality Verification
  - [ ] Write backend unit tests for `MatchController` and `MatchService`.
  - [ ] Write frontend unit tests for history components.
  - [ ] Execute E2E Playwright tests for history navigation and filtering.

## Dev Notes

- **Architecture Compliance (AD-02, AD-05):**
  - **AD-02 (Isolated Verification Pipeline):** The separation of PENDING and CONFIRMED is crucial. Pending matches have not reached the `PUBLISHED` state. Be sure to retrieve `CONFIRMED`/`PUBLISHED` for the "Confirmed" tab, and `PENDING` for the "Pending" tab.
  - **AD-05 (Stateless Authentication):** Extract current user via `@AuthenticationPrincipal` in the Controller, not via path variables for security.
- **500-Line Rule (IP-04):**
  - Ensure neither `MatchController` nor `MatchHistoryView.vue` exceed 500 lines. Break down into `MatchHistoryFilterBar`, `MatchHistoryTabs`, etc., if necessary.
- **Frontend Styling & UX Guidelines:**
  - **No-Line Rule (UX-DR3):** Use background shifts and spacing instead of 1px borders.
  - **Demo Mode:** Ensure the `demoDataGenerator.ts` can supply mock history for empty states if demo mode is enabled.
  - All text must use `$t('...')` for i18n (en/de).
- **Learnings from Previous Stories:**
  - Leverage `resolveDisplayName` carefully for privacy (deleted accounts).
  - Use `AbortController` when fetching data on filter changes to prevent race conditions.

### Project Structure Notes

- Backend domain code remains in `src/main/java/com/tictactore/`.
- Frontend code goes to `frontend/src/features/match/` (or `history`).

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR60
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-02, IP-04, UX-DR3

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)

### Debug Log References

### Completion Notes List

### File List
