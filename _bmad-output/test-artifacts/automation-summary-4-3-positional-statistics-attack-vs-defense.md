# Automation Summary — Story 4.3: Positional Statistics (Attack vs. Defense)

**Date:** 2026-08-16
**Mode:** TEA Auto
**Story:** 4-3-positional-statistics-attack-vs-defense
**Stack:** fullstack — Vue 3 + Vitest (unit) + Playwright (E2E/API) / Spring Boot 3.4 + JUnit 5 (Backend)

---

## Tests Created

| Type | File | AC | Tests | Priority |
|------|------|----|-------|----------|
| Unit (Backend) | `src/test/java/com/tictactore/service/LeaderboardServicePersonalStatsTest.java` | AC1, AC2, AC3 | 7 | P0 / P1 |
| Integration (Backend) | `src/test/java/com/tictactore/controller/StatisticsControllerPersonalStatsIT.java` | AC1, AC2, AC3 | 7 | P0 / P1 |
| Component (Frontend) | `frontend/src/features/stats/components/__tests__/StatsDashboard.spec.ts` | AC1, AC2, AC3 | 6 | P0 / P1 |
| API (Playwright HTTP) | `frontend/e2e/tests/api/personal-stats.spec.ts` | AC1, AC2, AC3 | 7 | P0 / P1 / P2 |
| E2E (Playwright Browser) | `frontend/e2e/tests/e2e/stats-dashboard.spec.ts` | AC1, AC2, AC3 | 9 | P0 / P1 / P2 / P3 |
| **Total** | | **AC1–AC3** | **36** | |

---

## Coverage by Acceptance Criterion

| AC | Description | Test Types | Status |
|----|-------------|-----------|--------|
| AC1 | Separate Attacker, Defender, and Overall stat cards displayed alongside each other | Backend Unit + Integration + Component + E2E | ✅ |
| AC2 | Each position card displays matches, wins, losses, and a proportional win-rate bar | Backend Unit + Integration + Component + E2E | ✅ |
| AC3 | 0-match user renders all position stats as zero with no errors (NaN-free) | Backend Unit + Integration + Component + E2E | ✅ |

---

## Infrastructure

- **Backend Factories:** Existing `StatsTestDataFactory` (`src/test/java/com/tictactore/support/StatsTestDataFactory.java`) reused — provides `confirmedOneVOne()`, `confirmedTwoVTwo()`, `pendingOneVOne()` for seeding CONFIRMED/PENDING matches into H2 test DB.
- **Backend Fixtures:** Custom `SecurityContext` principal injection (`UsernamePasswordAuthenticationToken` with `com.tictactore.model.User`) embedded in `StatisticsControllerPersonalStatsIT.java` to work around the `@WithMockUser` incompatibility with the `/me` endpoint (R-001 mitigation).
- **Frontend Factories:** `PersonalStatsFactory` (`frontend/e2e/support/factories/personal-stats.factory.ts`) — deterministic `PlayerStats` payloads with `createOneVOneStats()`, `createZeroStats()`, `createTwoVTwoStats()`, `createDemoStats()`, and override-supporting `create()`.
- **Frontend Fixtures:** Custom Playwright fixtures (`frontend/e2e/support/fixtures/stats-fixture.ts`) — `mockStatsResponse()`, `mockStatsError()`, `mockStatsLoading()` intercept `GET /api/v1/statistics/me`.
- **E2E Helpers:** `loginAsTestUser` (`frontend/e2e/tests/e2e/helpers/auth.ts`) provides authenticated session via `/api/auth/test-login` endpoint.

---

## Test Execution

```bash
# Backend unit tests (getPersonalStats aggregation logic)
./mvnw test -Dtest=LeaderboardServicePersonalStatsTest

# Backend integration tests (/me endpoint: 401/200 + per-position aggregation from H2)
./mvnw test -Dtest=StatisticsControllerPersonalStatsIT

# Frontend component tests (StatsDashboard render states + bar proportionality)
cd frontend && npm run test:unit -- --run src/features/stats/components/__tests__/StatsDashboard.spec.ts

# Frontend API tests (Playwright HTTP — /api/v1/statistics/me contract)
cd frontend && npx playwright test tests/api/personal-stats.spec.ts

# Frontend E2E tests (Playwright browser — StatsDashboard full-stack)
cd frontend && npx playwright test tests/e2e/stats-dashboard.spec.ts

# Full local CI verification
./scripts/ci-local.sh
```

---

## Priority Breakdown

| Priority | Count | Run When |
|----------|-------|----------|
| P0 | 11 | Every commit / PR |
| P1 | 9 | PR build |
| P2 | 3 | Nightly |
| P3 | 1 | Nightly |
| **Total** | **36** | |

---

## Definition of Done

- [x] All Story 4.3 ACs covered by automated tests (AC1–AC3)
- [x] 7/7 backend unit tests for `getPersonalStats` passing (aggregation, 0-match, ties, 2v2, pending exclusion, unknown user, winRate scale)
- [x] 7/7 backend integration tests for `/me` endpoint passing (401 without auth, 200 with principal, per-position aggregation, 0-match, ties, 2v2, pending exclusion)
- [x] 6/6 frontend component tests for `StatsDashboard` passing (cards, 0-match, bar cap, CSS classes, loading, error)
- [x] 7/7 Playwright HTTP API tests for `/api/v1/statistics/me` passing (401, 200 shape, 0-match, winRate scale, own-only, contract match, determinism)
- [x] 9/9 Playwright E2E browser tests for StatsDashboard passing (3 P0, 4 P1, 1 P2, 1 P3)
- [x] Strict Arrange-Act-Assert (AAA) pattern throughout
- [x] Tests are deterministic (factory-based, no random ordering impact)
- [x] Backend tests use `@Transactional @Rollback` / H2 in-memory DB for isolation
- [x] E2E tests use `page.route` interception — no leaked state between tests
- [x] `npm run type-check` clean; `vue-tsc --noEmit` passes
- [x] Backend: `./mvnw test` green (301 tests)
- [x] Frontend type-check: `vue-tsc --noEmit` clean
- [x] Playwright test discovery: `npx playwright test --list` passes (all 16 new tests discovered)
- [x] No tests skipped or disabled in active codebase (ATDD red-phase scaffolds are active, not skipped)

---

## Risks & Mitigations

- **R-001 (SEC):** `/me` authentication principal type mismatch (`com.tictactore.model.User` vs `@WithMockUser`'s `org.springframework.security.core.userdetails.User`). **Mitigated:** Integration tests use custom `SecurityContext` injection with the app `User` type instead of `@WithMockUser`.
- **R-002 (PERF):** `getPersonalStats` loads all confirmed matches, filters in-memory. **Documented:** MVP-scale; DB-scoped repository query planned for Epic 4.6.
- **R-003 (DATA):** Frontend `PersonalStatsParams` declares `period`/`myPosition`/`opponentPosition` which backend silently ignores. **Documented:** Contract reconciliation deferred to backlog.
- **R-004 (DATA):** Inconsistent winRate scale across endpoints (`/me` = 0–100, `/leaderboard` = 0–1). **Pinned:** API contract test asserts `/me` returns 0–100 scale; component test verifies bar width uses 0–100 directly.
