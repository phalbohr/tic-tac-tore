---
storyId: '4.2'
storyKey: 4-2-global-leaderboard-with-filtering
lastSaved: '2026-08-15T19:43:00+02:00'
status: done
---

# Definition of Done: Story 4.2 — Global Leaderboard with Filtering

## Test Coverage

### Backend API Tests

- [x] `GET /api/v1/statistics/leaderboard` returns 401 when unauthenticated
- [x] `GET /api/v1/statistics/leaderboard` returns 200 with paginated leaderboard when authenticated
- [x] Response includes `content`, `totalPages`, `totalElements`, `size`, `number` fields
- [x] Default `minMatches=5`, `page=0`, `size=20` passed to service
- [x] `matchFormat` filter forwarded to service as `STANDARD`/`RANDOM`
- [x] `matchType` filter forwarded to service as `1v1`/`2v2`
- [x] `period` filter forwarded to service as `WEEKLY`/`MONTHLY`/`YEARLY`/`ALL_TIME`
- [x] `type` (position) filter forwarded to service as `OVERALL`/`ATTACKER`/`DEFENDER`
- [x] Invalid `page` (negative) returns 400 with `BAD_REQUEST` error code
- [x] Invalid `size` (zero) returns 400
- [x] Invalid `minMatches` (negative) returns 400
- [x] Invalid `period` value returns 400
- [x] Invalid `matchFormat` value returns 400
- [x] Invalid `matchType` value returns 400

### Backend Integration Tests (H2 + Real Service)

- [x] 401 returned when unauthenticated (no `@WithMockUser`)
- [x] 200 returned when authenticated with no matches (empty page)
- [x] Wins/losses aggregated correctly from CONFIRMED matches
- [x] Results sorted by winRate descending (tie-break: wins, then playerName)
- [x] `matchFormat` filter excludes non-matching matches
- [x] `matchType` filter excludes 2v2 matches when `1v1` selected
- [x] `period` filter (WEEKLY) excludes matches older than 7 days
- [x] `minMatches` threshold (default 5) excludes players with fewer matches
- [x] Pagination returns correct page content, totalPages, totalElements
- [x] Page beyond last returns empty content with correct pagination metadata

### Backend Service Unit Tests (Mockito)

- [x] Win/loss stats computed correctly from CONFIRMED matches
- [x] Players below `minMatches` threshold excluded
- [x] `1v1` match type filtering
- [x] `2v2` match type filtering
- [x] `STANDARD`/`RANDOM` rule system (matchFormat) filtering
- [x] `WEEKLY`/`MONTHLY`/`YEARLY`/`ALL_TIME` period filtering
- [x] Sort by winRate descending
- [x] Pagination returns correct slice
- [x] `ATTACKER`/`DEFENDER`/`OVERALL` position type filtering
- [x] Game ties counted as totalMatches without win/loss
- [x] Fully tied matches counted as totalMatches without win/loss
- [x] Empty content when no matches match filters

### Frontend Component Tests

- [x] Leaderboard table renders with ranked entries and win rate percentage
- [x] Empty state displayed when no players match filters
- [x] Loading skeleton displayed while fetching
- [x] Error handled gracefully without crashing (shows empty state)
- [x] `matchFormat` (ruleSystem) filter passed to service on select change
- [x] `matchType` filter passed to service on select change
- [x] `period` filter passed to service on select change
- [x] Default `minMatches=5` passed to service on initial load
- [x] Pagination controls rendered when `totalPages > 1`
- [x] "Next" button navigates to next page (page=1)
- [x] "Previous" button navigates to previous page (page=0)
- [x] Filter change resets to page 0

### Frontend E2E Tests

- [x] Ranked leaderboard sorted by win rate displayed on `/leaderboard` route
- [x] Default `minMatches=5` in first API request URL
- [x] `matchFormat` filter applied to API request on select change
- [x] Pagination: "Next" button navigates to page=1
- [x] Empty state shown when no players match filters

## Quality Gates

- [x] All P0 tests pass (14 tests)
- [x] All P1 tests pass (23 tests)
- [x] All P2 tests pass (10 tests)
- [x] No open high-priority / high-severity bugs
- [x] Test coverage agreed as sufficient
- [x] No secrets, keys, or credentials exposed in test code
- [x] All test assertions are deterministic (factory-based data, no random ordering)
- [x] E2E tests use `page.route` for API mocking (not real backend dependency)
- [x] No hard waits or sleeps in tests
- [x] No test interdependencies
- [x] Tests clean up their data (`@Transactional @Rollback` for backend; `page.route` fulfillment for E2E)

## Risk Mitigations

- [x] R-001 (SEC): `/api/v1/statistics/**` requires authentication — verified by 401 test
- [x] R-003 (PERF): In-memory aggregation verified by multi-match integration tests
- [x] R-005 (TECH): Client-side rank computation noted; cross-page rank verified via API-level sort assertions
- [x] R-006 (BUS): Empty state displayed when no players match filters — verified in component + E2E
- [x] R-007 (DATA): Float sort stability verified by service tests with tie-breakers

## Sign-off

| Role | Name | Status |
|------|------|--------|
| QA | Pavel | Reviewed |
| Dev | — | — |
| PM | — | — |
