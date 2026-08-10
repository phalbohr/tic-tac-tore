---
storyId: '2.7'
storyKey: 2-7-global-player-search-and-selection
lastSaved: '2026-08-10T16:08:00+02:00'
status: draft
---

# Definition of Done: Story 2.7 — Global Player Search & Selection

## Test Coverage

### Backend API Tests

- [x] `GET /api/users/me/players/search?q=` returns 200 with matching active users
- [x] `GET /api/users/me/players/search?q=` returns 200 with empty list for blank query
- [x] `GET /api/users/me/players/search` returns 200 with empty list when query parameter is missing
- [x] Search is case-insensitive (nickname LIKE lower)
- [x] Response excludes email addresses (security)
- [x] Soft-deleted accounts (`deleted-*`, `ex-player-*`) never appear in results

### Frontend Component Tests

- [x] Search overlay renders when `isOpen` is true
- [x] Search overlay does not render when `isOpen` is false
- [x] Search input auto-focuses when overlay opens
- [x] Selecting result row emits select event and closes overlay
- [x] Backdrop click closes overlay without selection
- [x] Escape key closes overlay without selection
- [x] Loading state displayed while searching
- [x] Error message displayed when search fails
- [x] Empty state displayed when no results found
- [x] Frequent opponents ordered before alphabetical results
- [x] Max players reached — additional selection silently ignored

### Frontend Store Tests

- [x] `searchPlayers` debounces API call by 300ms
- [x] `searchPlayers` clears results when query is empty
- [x] `searchPlayers` handles successful API response
- [x] `searchPlayers` handles API error response (500)
- [x] `searchPlayers` handles network error
- [x] `closeSearch` clears debounce timer
- [x] `openSearch` resets search state

### Frontend E2E Tests

- [x] Open search overlay from empty player slot
- [x] Type partial nickname and see matching players
- [x] Select player from results and verify slot updates
- [x] Frequent opponents appear before alphabetical results
- [x] Error state displayed when backend returns 500
- [x] Escape key closes overlay without selection

## Quality Gates

- [x] All P0 tests pass
- [x] All P1 tests pass
- [x] No open high-priority / high-severity bugs
- [x] Test coverage agreed as sufficient
- [x] No secrets, keys, or credentials exposed in test code
- [x] All test assertions are deterministic
- [x] E2E tests use `data-testid` selectors (not CSS classes)
- [x] No hard waits or sleeps in tests
- [x] No test interdependencies
- [x] Tests clean up their data (no environment pollution)

## Risk Mitigations

- [x] R-001: Rate limiting planned for `/players/search` endpoint
- [x] R-002: Server-side pagination limit planned
- [x] R-003: `PlayerDto` contract validated by API integration tests
- [x] R-004: Result ordering verified by component + E2E tests
- [x] R-006: Soft-delete filter verified by unit + API tests

## Sign-off

| Role | Name | Status |
|------|------|--------|
| QA | Pavel | Reviewed |
| Dev | — | — |
| PM | — | — |
