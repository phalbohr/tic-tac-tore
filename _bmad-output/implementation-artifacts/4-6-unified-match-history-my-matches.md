---
baseline_commit: 7381afc
---

# Story 4.6: Unified Match History (My Matches)

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to view a unified match history with tabs for confirmed and pending matches,
so that I can track my historical performance, review pending confirmations, and filter matches by opponent or partner.

## Acceptance Criteria

1. **Given** the player navigates to their match history (`/matches` or `/history`)
   **When** the view loads
   **Then** it displays a unified "My Matches" view with tabs for **Confirmed** and **Pending** matches (FR60, UX Flow 7)
   **And** the **Pending tab** shows badged pending confirmation cards with inline actions (Confirm / Reject) reusing the 15-second undo timer workflow
   **And** the **Confirmed tab** displays a chronological list of confirmed matches with paginated results (offset or page/size via `PagedResponse<MatchResponse>`)
2. **Given** the player is viewing the Confirmed match history tab
   **When** they use the filter controls
   **Then** the system provides thumb-friendly filter chips (`MatchFilterChips.vue`) to filter by all players, specific opponent or partner, match type (`1v1` vs `2v2`), and rule template (`ruleConfigId`) (FR60, UX Flow 7)
   **And** changing filters immediately updates the match list, using an `AbortController` to cancel in-flight requests and prevent race conditions
3. **Given** the player views match cards in the history list
   **When** viewing the list items
   **Then** the UI strictly adheres to the Clubhouse "No-Line" rule (UX-DR3) and uses `ch-` prefixed SCSS classes
   **And** confirmed match cards display final scores, outcome badges (Win / Loss / Draw), opponent/teammate avatars via `AvatarBase` / `AvatarInteractive`, date/time, and match format tags
   **And** for matches involving deleted or anonymized accounts, the UI safely renders "Retired Player" without exposing PII (AD-04)
4. **Given** the current user has < 1 confirmed match or demo mode is enabled (`demoModeEnabled`)
   **When** viewing the Confirmed match history tab
   **Then** realistic demo match history is generated via `demoDataGenerator.generateDemoMatchHistory()`
   **And** when the list has zero matches (real or filtered), tab-specific empty states are rendered:
   - **Confirmed tab (0 matches):** Displays `EmptyStateCTA` with a "Record your first match" CTA button navigating to `/matches/new`
   - **Pending tab (0 matches):** Displays an "All caught up" empty state
   - **Filtered results (0 matches):** Displays a "Try removing filters" reset CTA button

## Tasks / Subtasks

- [x] Task 1: Backend Domain & API Specifications (AC1, AC2, AC3)
  - [x] Add `GET /api/v1/matches/history` endpoint to `com.tictactore.controller.MatchController`.
  - [x] Support query parameters: `status` (optional string: `CONFIRMED`, `PENDING`, `ALL`, default `CONFIRMED`), `playerId` (optional UUID to filter by opponent or teammate), `ruleConfigId` (optional UUID), `matchType` (optional string: `1v1`, `2v2`), `page` (int, default 0), and `size` (int, default 10).
  - [x] Extract authenticated user via `@AuthenticationPrincipal User principal` (`AD-05`) and return `ResponseEntity<PagedResponse<MatchResponse>>`.
  - [x] Ensure safe display name resolution for deleted/retired players via `resolveDisplayName` (`AD-04`).
- [x] Task 2: Backend Repository & Service Layer (AC1, AC2)
  - [x] Implement paginated query in `com.tictactore.repository.MatchRepository` using `Pageable` and `JOIN FETCH m.games` to fetch matches where current user participated (as `teamAAttackerId`, `teamADefenderId`, `teamBAttackerId`, `teamBDefenderId`, or `creatorId`).
  - [x] Enforce `AD-02` (Isolated Verification Pipeline): filter by `status IN ('CONFIRMED', 'PUBLISHED')` for Confirmed history and `status = 'PENDING'` for Pending history.
  - [x] Add `PagedResponse<MatchResponse> getMatchHistory(UUID currentUserId, String status, UUID filterPlayerId, UUID ruleConfigId, String matchType, int page, int size)` to `MatchService` and `MatchServiceImpl`.
- [x] Task 3: Frontend Service & Pinia Store (AC1, AC2, AC4)
  - [x] Create or update `frontend/src/services/matchService.ts` with `getMatchHistory(params)` supporting `AbortSignal`.
  - [x] Create `frontend/src/features/match/stores/useMatchHistoryStore.ts` managing:
    - State: `activeTab` ('confirmed' | 'pending'), `confirmedMatches`, `pendingMatches`, `pagination` (page, size, totalPages, totalElements), `filters` (playerId, matchType, ruleConfigId), `loading`, `error`, `demoMode`.
    - Actions: `fetchConfirmedHistory()`, `fetchPendingMatches()`, `setFilter()`, `resetFilters()`, `setTab()`, `setPage()`.
    - Handle search debounce and request cancellation via `AbortController`.
  - [x] Update `frontend/src/features/stats/utils/demoDataGenerator.ts` to implement `generateDemoMatchHistory()` returning realistic `PagedResponse<MatchResponse>`.
- [x] Task 4: Frontend UI Components & Design System Compliance (AC1, AC2, AC3, AC4)
  - [x] Create `frontend/src/features/match/views/MyMatchesView.vue` (main orchestrator view under 500 lines per `IP-04`) with tab navigation (`Confirmed` / `Pending`) and badge indicators.
  - [x] Create `frontend/src/features/match/components/MatchHistoryList.vue` handling pagination controls and empty states.
  - [x] Create `frontend/src/features/match/components/MatchCard.vue` rendering individual match items with win/loss indicators, scores, format tags, and avatar integration following the Clubhouse "No-Line" rule (`UX-DR3`) with `ch-` classes.
  - [x] Create `frontend/src/features/match/components/MatchFilterChips.vue` providing thumb-friendly chips for player, match type, and rule filtering.
  - [x] Integrate existing `PendingMatches.vue` into the Pending tab of `MyMatchesView.vue` without breaking existing confirmation/rejection timers and toast notifications.
  - [x] Add route `/matches` (and alias `/history`) in `frontend/src/router/index.ts` pointing to `MyMatchesView.vue`.
  - [x] Add i18n translation keys in `frontend/src/locales/en.json` and `frontend/src/locales/de.json`.
- [x] Task 5: Testing & Quality Verification
  - [x] Backend: Add unit and integration tests in `MatchControllerTest.java`, `MatchServiceTest.java`, and `MatchRepositoryTest.java`.
  - [x] Backend ATDD: Implement `MatchHistoryATDDTest.java` verifying filtering, pagination, and status isolation.
  - [x] Frontend: Add component tests in `frontend/src/features/match/components/__tests__/MyMatchesView.spec.ts` and `MatchCard.spec.ts`.
  - [x] Frontend Store: Add unit tests in `frontend/src/features/match/stores/__tests__/useMatchHistoryStore.spec.ts`.
  - [x] E2E: Create Playwright test in `frontend/e2e/match-history.spec.ts` testing tab switching, filtering, and pagination.
  - [x] Verification: Execute `./scripts/ci-local.sh` and ensure all checks pass.

## Dev Notes

### Architecture & Implementation Guardrails

- **API Contract Compliance:**
  - **Endpoint:** `GET /api/v1/matches/history` in `MatchController`.
  - **Query Parameters:**
    - `status` (string, optional: `CONFIRMED`, `PENDING`, `ALL`, default: `CONFIRMED`)
    - `playerId` (UUID, optional: filter matches involving this specific user)
    - `ruleConfigId` (UUID, optional: filter by rule template)
    - `matchType` (string, optional: `1v1` | `2v2`)
    - `page` (int, default: 0)
    - `size` (int, default: 10)
  - **Response:** `200 OK` with `PagedResponse<MatchResponse>`.
  - **Security (AD-05):** Always extract current user ID from `@AuthenticationPrincipal User principal`. Reject unauthenticated requests with `401 Unauthorized`.
- **Isolated Verification Pipeline (AD-02):**
  - Confirmed match history MUST ONLY return matches with status `CONFIRMED` or `PUBLISHED`.
  - Pending match history MUST ONLY return matches with status `PENDING`.
  - `REJECTED` or draft matches must never be mixed into confirmed history results.
- **Privacy & PII Protection (AD-04):**
  - Deleted accounts must have their display name resolved as "Retired Player" (or localized equivalent) without leaking emails.
  - Avatars for deleted players should display standard anonymous placeholders.
- **500-Line Rule (IP-04):**
  - Keep `MatchController`, `MyMatchesView.vue`, and subcomponents well under 500 lines. Deconstruct UI into `MatchFilterChips.vue`, `MatchHistoryList.vue`, and `MatchCard.vue`.
- **Frontend Styling & UX Guidelines (UX-DR3 & UX Flow 7):**
  - **No-Line Rule (UX-DR3):** Do not use 1px solid border lines between cards or list rows. Use surface background tonal shifts (`background-color: var(--ch-surface-...)`), margin/padding spacing, and subtle elevation.
  - **Design Tokens:** Use `ch-` prefixed SCSS classes conforming to Clubhouse dark aesthetic.
  - **Filter Chips:** Use horizontal thumb-friendly filter chips instead of nested modal dropdowns.
  - **Tab Synchronization:** Support query parameter deep-linking (`/matches?tab=confirmed` / `/matches?tab=pending&playerId=...`).
- **Translations (i18n):**
  - All text must use `$t('...')` for both `en.json` and `de.json`.
  - Required translation keys:
    - `history.title`: "My Matches" / "Meine Spiele"
    - `history.tabs.confirmed`: "Confirmed" / "Bestätigt"
    - `history.tabs.pending`: "Pending" / "Ausstehend"
    - `history.filters.all`: "All" / "Alle"
    - `history.filters.matchType`: "Match Type" / "Spielmodus"
    - `history.filters.player`: "Filter by Player" / "Nach Spieler filtern"
    - `history.filters.clear`: "Clear Filters" / "Filter zurücksetzen"
    - `history.empty.confirmedTitle`: "No confirmed matches yet" / "Noch keine bestätigten Spiele"
    - `history.empty.confirmedCta`: "Record your first match" / "Erstes Spiel erfassen"
    - `history.empty.pendingTitle`: "All caught up" / "Alles erledigt"
    - `history.empty.pendingSubtitle`: "No pending match confirmations" / "Keine ausstehenden Bestätigungen"
    - `history.empty.filteredTitle`: "No matches found" / "Keine Spiele gefunden"
    - `history.empty.filteredCta`: "Try removing filters" / "Filter entfernen"
    - `history.outcome.win`: "Win" / "Sieg"
    - `history.outcome.loss`: "Loss" / "Niederlage"
    - `history.outcome.draw`: "Draw" / "Unentschieden"

### Learnings from Previous Stories (4.1, 4.4, 4.5)

- **AbortController on Filter Change:** In `useMatchHistoryStore.ts`, abort previous pending requests when the user changes filters or tabs rapidly to avoid out-of-order response overwrites.
- **Outcome Calculation:** To determine Win/Loss/Draw for current player:
  - Check if player was on Team A (`teamAAttackerId == userId || teamADefenderId == userId`) or Team B (`teamBAttackerId == userId || teamBDefenderId == userId`).
  - Compare total games won by Team A vs Team B.
- **Preserve PendingMatches Workflow:** The Pending tab embeds existing verification actions (15s undo window, reject modal, delete draft). Do not rewrite or duplicate this logic—reuse `usePendingMatches` and `useMatchConfirmationStore`.

### References

- [Source: _bmad-output/planning-artifacts/prd.md] - FR60
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md] - Flow 7 (Browse Match History), Step 2 (Unified My Matches), UX-DR3
- [Source: _bmad-output/planning-artifacts/architecture.md] - AD-02, AD-04, AD-05, IP-04

## Dev Agent Record

### Agent Model Used

Gemini 3.7 Flash (Medium)

### Debug Log References

- Executed `./scripts/ci-local.sh` and verified backend unit/integration tests (`MatchHistoryATDDTest`, `MatchServiceGetMatchHistoryTest`, `MatchRepositoryTest`), frontend Vitest tests (`useMatchHistoryStore.spec.ts`, `MatchCard.spec.ts`, `MyMatchesView.spec.ts`), and Playwright E2E suite (`match-history.spec.ts`).

### Completion Notes List

- Implemented `GET /api/v1/matches/history` endpoint in `MatchController` with support for `status`, `playerId`, `ruleConfigId`, `matchType`, `page`, and `size`.
- Implemented `findMatchHistory` query in `MatchRepository` and `getMatchHistory` in `MatchService`/`MatchServiceImpl` with PII masking for retired players (`AD-04`).
- Created `frontend/src/services/matchService.ts` and `frontend/src/features/match/stores/useMatchHistoryStore.ts` with pagination, filtering, AbortController request cancellation, and demo mode.
- Created `MyMatchesView.vue`, `MatchHistoryList.vue`, `MatchCard.vue`, `MatchFilterChips.vue` complying with Clubhouse "No-Line" rule (`UX-DR3`) and full i18n support.
- Updated `demoDataGenerator.ts` with `generateDemoMatchHistory()`.
- Routed `/matches` and alias `/history` in `frontend/src/router/index.ts`.
- Full test pass confirmed with `./scripts/ci-local.sh` (backend tests + frontend type-check + build + unit tests + Playwright E2E).

### File List

- `src/main/java/com/tictactore/controller/MatchController.java`
- `src/main/java/com/tictactore/service/MatchService.java`
- `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java`
- `src/main/java/com/tictactore/repository/MatchRepository.java`
- `src/test/java/com/tictactore/controller/MatchHistoryATDDTest.java`
- `src/test/java/com/tictactore/service/MatchServiceGetMatchHistoryTest.java`
- `src/test/java/com/tictactore/repository/MatchRepositoryTest.java`
- `frontend/src/services/matchService.ts`
- `frontend/src/features/match/stores/useMatchHistoryStore.ts`
- `frontend/src/features/match/views/MyMatchesView.vue`
- `frontend/src/features/match/components/MatchHistoryList.vue`
- `frontend/src/features/match/components/MatchCard.vue`
- `frontend/src/features/match/components/MatchFilterChips.vue`
- `frontend/src/features/stats/utils/demoDataGenerator.ts`
- `frontend/src/router/index.ts`
- `frontend/src/locales/en.json`
- `frontend/src/locales/de.json`
- `frontend/tests/unit/useMatchHistoryStore.spec.ts`
- `frontend/src/features/match/components/__tests__/MatchCard.spec.ts`
- `frontend/src/features/match/components/__tests__/MyMatchesView.spec.ts`
- `frontend/e2e/match-history.spec.ts`
- `_bmad-output/implementation-artifacts/4-6-unified-match-history-my-matches.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
