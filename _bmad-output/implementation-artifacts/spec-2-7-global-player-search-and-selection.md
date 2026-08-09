---
title: 'Story 2.7: Global Player Search & Selection'
type: 'feature'
created: '2026-08-09T17:52:38+02:00'
status: 'in-progress'
review_loop_iteration: 0
followup_review_recommended: false
context: []
warnings: []
baseline_revision: 'cfe236921cda800e9da3465b72cb709ba0ba1507'
---

<intent-contract>

## Intent

**Problem:** Players can only add opponents from the hardcoded "Frequent Opponents" strip, which limits match creation to a small static set of users.

**Approach:** Add a search overlay to `PlayerSelection.vue` that queries all active registered users via a new backend endpoint, allowing the user to find and select any opponent by nickname. Frequent opponents remain as the default quick-add strip.

## Boundaries & Constraints

**Always:**
- **No-Line Rule (UX-DR3):** Use `bg-surface-container-highest` layered over `bg-surface-container-low` for all new UI boundaries; no 1px borders.
- **Component Reuse:** Use existing core UI primitives from `frontend/src/core/components/` for buttons, inputs, and avatars.
- **Overlay Pattern:** Follow the established `fixed inset-0 z-50` backdrop + `<Transition>` pattern used by `RejectReasonSelector.vue` and `AvatarPicker.vue`.
- **Active-User Filtering:** Backend search must exclude soft-deleted accounts (email starts with `deleted-`, nickname starts with `ex-player-`).
- **Mock Players:** Existing mock players from `getFrequentOpponents()` remain in the database; they are treated as regular active users in search results. Full cleanup is deferred.
- **Debounce:** Frontend search requests must be debounced (300ms) to avoid flooding the backend.
- **Case-Insensitive:** Backend search must be case-insensitive on nickname.

**Block If:**
- Backend search endpoint is unavailable or returns 5xx.
- Player profile API (`/api/v1/players/{id}`) is unavailable for unknown search results.

**Never:**
- Replace or remove the frequent-opponents quick-add strip.
- Require login or block public access for the search endpoint (consistent with `frequent-opponents`).
- Expose email addresses in search results.

</intent-contract>

## Code Map

- `frontend/src/features/match/components/PlayerSelection.vue` -- Add search trigger button + integrate `PlayerSearchOverlay`.
- `frontend/src/features/match/components/PlayerSearchOverlay.vue` -- New overlay component with search input and results list.
- `frontend/src/features/match/stores/matchDraftStore.ts` -- Add search state and `searchPlayers(query)` async action.
- `src/main/java/com/tictactore/controller/UserMatchController.java` -- Add `GET /players/search` endpoint returning `PlayerDto` list.
- `src/main/java/com/tictactore/service/UserService.java` -- Add `searchActiveUsers(String query)` method.
- `src/main/java/com/tictactore/repository/UserRepository.java` -- Add `searchActiveUsers(String query)` query.
- `src/main/java/com/tictactore/config/SecurityConfig.java` -- Register `/api/users/me/players/search` as public.

## Tasks & Acceptance

**Execution:**
- [ ] `src/main/java/com/tictactore/repository/UserRepository.java` -- Add `searchActiveUsers` query method filtering out soft-deleted accounts.
- [ ] `src/main/java/com/tictactore/service/UserService.java` -- Add `searchActiveUsers` method mapping results to `PlayerDto`.
- [ ] `src/main/java/com/tictactore/controller/UserMatchController.java` -- Add `GET /players/search?q=` endpoint.
- [ ] `src/main/java/com/tictactore/config/SecurityConfig.java` -- Register new endpoint in `PUBLIC_ENDPOINTS`.
- [ ] `frontend/src/features/match/stores/matchDraftStore.ts` -- Add `searchQuery`, `searchResults`, `isSearchOpen`, `searchLoading` state and `searchPlayers` async action with 300ms debounce.
- [ ] `frontend/src/features/match/components/PlayerSearchOverlay.vue` -- Create overlay component with search input, loading state, and selectable results list.
- [ ] `frontend/src/features/match/components/PlayerSelection.vue` -- Add search button to each empty player slot; mount `PlayerSearchOverlay`.
- [ ] `frontend/src/features/match/components/__tests__/PlayerSelection.spec.ts` -- Add test for search overlay mount and player selection via search result.
- [ ] `src/test/java/com/tictactore/service/UserServiceTest.java` -- Add unit test for `searchActiveUsers` filtering deleted users and matching nickname.

**Acceptance Criteria:**
- Given I am on the player selection screen with empty slots, when I tap the search icon in an empty slot, then a full-screen overlay with a search input appears.
- Given the search overlay is open, when I type a partial nickname, then the backend returns matching active users (excluding soft-deleted accounts) and the list updates after 300ms debounce.
- Given the search results include frequent opponents, when I view the list, then frequent opponents appear first, followed by alphabetically sorted other active users.
- Given a user is displayed in search results, when I tap their row, then they are added to the match via `store.addPlayer`, the overlay closes, and the player slot updates.
- Given I have already filled all player slots for the selected match type, when I open the search overlay, then selecting an additional user shows no error but does not add them.
- Given the backend search endpoint is unreachable, when I perform a search, then the overlay displays a friendly error message and the existing frequent-opponents strip remains functional.

## Spec Change Log

<!-- Append-only. Populated by step-04 during review loops. -->

## Review Triage Log

<!-- Append-only. Populated by step-04 on EVERY review pass. -->

## Design Notes

- **Result ordering:** Combine `frequentOpponents` IDs with backend results on the frontend. Sort results so frequent opponents appear first (stable order from `getFrequentOpponents`), then alphabetical by nickname.
- **Avatar fallback:** Search results without an avatar should fall back to a deterministic default, consistent with the existing `generateDeterministicAvatar` logic in `UserService`.
- **Overlay dismissal:** Tap backdrop or press hardware back to close without selecting. Do not add a separate close button unless required by the design system.

## Verification

**Commands:**
- `npm run test:unit frontend/src/features/match/components/__tests__/PlayerSelection.spec.ts` -- expected: existing + new search tests pass.
- `npm run test:unit frontend/src/features/match/stores/matchDraftStore.spec.ts` -- expected: no regressions.
- `./gradlew test` -- expected: backend unit tests pass, including new `UserServiceTest.searchActiveUsers`.
- `npm run lint` -- expected: 0 lint errors.
