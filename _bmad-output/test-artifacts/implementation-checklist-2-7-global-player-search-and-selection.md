---
status: in-progress
---

# Implementation Checklist: Story 2.7 — Global Player Search & Selection

**Story Key:** `2-7-global-player-search-and-selection`  
**Branch:** `story/2-7-global-player-search-and-selection`  
**Date:** 2026-08-10

## Working Tree Changes Assessment

**Current working tree diff (unstaged):**
- `_bmad-output/implementation-artifacts/bmad-dev-auto-result-2-7-global-player-search-and-selection-tea.td-1.md` — test design workflow re-run metadata
- `_bmad-output/implementation-artifacts/sprint-status.yaml` — story status `ready-for-dev` → `done`
- `_bmad-output/test-artifacts/test-design-progress.md` — timestamp update
- `_bmad-output/test-artifacts/test-design/test-design-epic-2-7.md` — status `Draft` → `Approved`, date update

**Production code changes in working tree:** None. All production code changes are already committed on this branch.

## Story Implementation Status

Story 2.7 is fully implemented and marked `done` in sprint-status.yaml. The following production code changes were already merged in commits `159ef98` and `0a18a25`:

### Backend Production Code

- [x] `src/main/java/com/tictactore/repository/UserRepository.java` — Added `searchActiveUsers(String query)` JPQL query filtering out soft-deleted accounts (`email NOT LIKE 'deleted-%'`, `nickname NOT LIKE 'ex-player-%'`) with case-insensitive `LOWER(nickname) LIKE LOWER(CONCAT('%', :query, '%'))`
- [x] `src/main/java/com/tictactore/service/UserService.java` — Added `searchActiveUsers(String query)` method mapping `User` entities to `PlayerDto` records
- [x] `src/main/java/com/tictactore/controller/UserMatchController.java` — Added `GET /players/search?q=` endpoint returning `ResponseEntity<List<PlayerDto>>`; blank/null query returns empty list
- [x] `src/main/java/com/tictactore/config/SecurityConfig.java` — Registered `/api/users/me/players/search` in `PUBLIC_ENDPOINTS`

### Frontend Production Code

- [x] `frontend/src/features/match/stores/matchDraftStore.ts` — Added `searchQuery`, `searchResults`, `searchError`, `isSearchOpen`, `searchLoading` state; added `openSearch()`, `closeSearch()`, `searchPlayers(query)` async action with 300ms debounce, AbortController for in-flight cancellation, and `onUnmounted` cleanup
- [x] `frontend/src/features/match/components/PlayerSearchOverlay.vue` — New overlay component with fixed `inset-0 z-50` backdrop + `<Transition>` pattern, search input (`data-testid="player-search-input"`), loading state, error state (`data-testid="search-error"`), empty state (`data-testid="no-results"`), selectable results (`data-testid="search-result-row"`), frequent-opponent-first ordering, Escape/backdrop dismiss
- [x] `frontend/src/features/match/components/PlayerSelection.vue` — Added search button (`data-testid="open-search-button"`) to each empty player slot; mounted `<PlayerSearchOverlay>` with `:isOpen="store.isSearchOpen"` and `@close` handler

### Backend Test Code

- [x] `src/test/java/com/tictactore/service/UserServiceTest.java` — Added `searchActiveUsers_filtersDeletedAccountsAndMatchesNickname` unit test verifying soft-delete filtering and case-insensitive nickname matching
- [x] `src/test/java/com/tictactore/controller/UserMatchControllerATDDTest.java` — Added controller ATDD specs for `GET /players/search`

### Frontend Test Code

- [x] `frontend/src/features/match/components/__tests__/PlayerSearchOverlay.spec.ts` — Added tests for overlay render, auto-focus, select/close events, loading/error/empty states, result ordering, max-players guard
- [x] `frontend/src/features/match/components/__tests__/PlayerSelection.spec.ts` — Added test for search overlay mount and player selection via search result
- [x] `frontend/src/features/match/stores/matchDraftStore.search.spec.ts` — Added tests for `searchPlayers` debounce (300ms), empty query clearing, success/error/network handling, `closeSearch` timer cleanup, `openSearch` state reset

## Working Tree Documentation Changes (Unstaged)

No production code changes are pending. The unstaged changes are documentation/metadata updates only:

| File | Change | Action Required |
|------|--------|-----------------|
| `bmad-dev-auto-result-2-7-global-player-search-and-selection-tea.td-1.md` | Re-run metadata + working tree assessment | None — already updated |
| `sprint-status.yaml` | Story status `ready-for-dev` → `done` | None — already updated |
| `test-design-progress.md` | Timestamp + working tree changes section | None — already updated |
| `test-design/test-design-epic-2-7.md` | Status `Draft` → `Approved`, date + current state section | None — already updated |

## Verification Commands

- `npm run test:unit frontend/src/features/match/components/__tests__/PlayerSelection.spec.ts` — expected: existing + new search tests pass
- `npm run test:unit frontend/src/features/match/stores/matchDraftStore.spec.ts` — expected: no regressions
- `npm run test:unit frontend/src/features/match/stores/matchDraftStore.search.spec.ts` — expected: new search store tests pass
- `npm run test:unit frontend/src/features/match/components/__tests__/PlayerSearchOverlay.spec.ts` — expected: new overlay tests pass
- `./mvnw test` — expected: backend unit tests pass, including `UserServiceTest.searchActiveUsers` + `UserMatchControllerATDDTest`
- `npm run lint` — expected: 0 lint errors

## Next Recommended Workflow

- **Regression guard:** Run full test suite before merging to ensure `PlayerSearchOverlay` and search store actions do not break existing match-draft flows
- **Downstream dependency:** None — feature is self-contained within match selection flow
