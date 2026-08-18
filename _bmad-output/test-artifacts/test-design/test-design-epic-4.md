---
workflowStatus: 'completed'
totalSteps: 5
stepsCompleted: ['step-01-detect-mode', 'step-02-load-context', 'step-03-risk-and-testability', 'step-04-coverage-plan', 'step-05-generate-output']
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-08-15T18:43:37+02:00'
---

# Test Design: Epic 4 - Individual & Team Analytics (Story 4.2)

**Date:** 2026-08-15  
**Author:** Pavel  
**Status:** Draft (Post-Implementation Risk Assessment)

---

## Executive Summary

**Scope:** Epic-level test design for Story 4.2 — Global Leaderboard with Filtering. The implementation is complete (`./mvnw test` — 277 backend tests pass; `npm run test:unit` — 203 frontend tests pass; `npm run type-check` — no TS errors; `npm run build` — success). This document assesses risks in the working-tree code and provides a risk-based coverage strategy.

**Risk Summary:**

- Total risks identified: 8
- High-priority risks (≥6): 3
- Critical categories: SEC, DATA, PERF

**Coverage Summary:**

- P0 scenarios: 8 (~12–20 hours)
- P1 scenarios: 8 (~8–15 hours)
- P2/P3 scenarios: 7 (~4–11 hours)
- **Total effort**: ~24–46 hours (~3–6 days)

**Working Tree Changes Assessed:**
- Backend: New `StatisticsController`, `LeaderboardService`, `LeaderboardServiceImpl`, `LeaderboardRepository`, `LeaderboardEntry`, `PageResponse` DTOs; `LeaderboardServiceTest` with 12 unit tests
- Frontend: New `LeaderboardView.vue`, route `/leaderboard`, extended `statisticsService.ts` with `matchType`/`ruleSystem` params
- Metadata: `sprint-status.yaml` — `4-2-global-leaderboard-with-filtering` marked `done`
- Coverage gaps identified: no API/integration tests for the HTTP endpoint, no security test for 401, no frontend component tests for `LeaderboardView`, no E2E test for the leaderboard flow.

---

## Not in Scope

| Item | Reasoning | Mitigation |
|------|-----------|------------|
| Database-level aggregation (GROUP BY) | In-memory aggregation used for MVP scale (10–20 players) | Documented as R-003; performance test planned for Epic 4.x |
| Frontend sortable column headers | Current implementation sorts via backend; column-header sorting is a future enhancement | Sort verified via win-rate ordering in component tests |
| Demo data overlay integration within leaderboard | Demo data is handled by `useStatsStore` (Story 4.1); leaderboard consumes real data | Existing `useStatsStore.spec.ts` covers demo data; leaderboard E2E validates integration |
| Rate limiting on leaderboard endpoint | Not implemented; MVP scope | Documented as R-001 mitigation; defer to platform-wide rate limiting effort |

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---------|----------|-------------|-------------|--------|-------|------------|-------|----------|
| R-001 | SEC | `StatisticsController` has no explicit `@PreAuthorize` annotation; authentication relies entirely on Spring Security config. If `/api/v1/statistics/**` is not secured, unauthenticated users can retrieve all player statistics (win/loss records, win rates). | 2 | 3 | 6 | Verify Spring Security protects `/api/v1/statistics/**`; add integration test asserting 401 without JWT; add `@PreAuthorize` as defense-in-depth | Backend | Sprint |
| R-002 | DATA | Redundant dual filtering: `LeaderboardRepository.findConfirmedMatchesWithFilters` applies matchFormat/matchType/period filters via JPQL, but `LeaderboardServiceImpl` re-applies the same filters in-memory (lines 40–50). If the two filter implementations diverge (e.g., SQL `IS NULL` vs Java `== null`, `isBlank()` vs `IS NULL`), results could be silently inconsistent. | 2 | 3 | 6 | Consolidate filtering to repository layer; remove redundant in-memory checks; add integration test comparing repository-only vs service-filtered results | Backend | Sprint |
| R-003 | PERF | In-memory aggregation loads all CONFIRMED matches into a Java `List<Match>`, iterates every match and every game to compute stats. No database-level `GROUP BY`. Memory grows linearly with match count; response time grows with total game count. Spec acknowledges this is suitable for MVP (10–20 players) only. | 2 | 3 | 6 | Implement database-level aggregation with `GROUP BY` and `SUM(CASE ...)` for win/loss counting; add performance test with 10k+ matches to establish baseline | Backend | Epic 4.6 |

### Medium-Priority Risks (Score 3-5)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
|---------|----------|-------------|-------------|--------|-------|------------|-------|
| R-004 | DATA | Match type inference relies solely on null `teamADefenderId`/`teamBDefenderId`. A match with asymmetric team data (one defender set, one null) is excluded from both 1v1 and 2v2 filters — users see fewer results with no indication of data loss. | 2 | 2 | 4 | Add validation during match creation enforcing team structure consistency; add unit test for asymmetric team configurations; log excluded matches at DEBUG level | Backend |
| R-005 | TECH | Frontend displays page-relative rank (`currentPage * pageSize + index + 1`) instead of true global rank. `LeaderboardEntry` record omits a `rank` field, so rank numbers jump across pages (1–20 on page 1, 21–40 on page 2). If match data changes between page loads, displayed ranks could be misleading. | 2 | 2 | 4 | Add `rank` field to `LeaderboardEntry` and backend sorting index; update frontend to display backend-provided rank; add component test verifying rank display | Backend + Frontend |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
|---------|----------|-------------|-------------|--------|-------|--------|
| R-006 | BUS | Frontend empty state shows "No players match the current filters." without distinguishing "no data exists" from "filters too restrictive." Users may think the leaderboard is empty rather than over-filtered. | 1 | 2 | 2 | Document; add component test distinguishing empty vs filtered-empty states; consider separate UI messages | Monitor |
| R-007 | DATA | Win rate sorting uses `Comparator.comparingDouble(LeaderboardEntry::winRate)` (floating-point). Players with mathematically equal win rates (e.g., 1/3 vs 2/6) could have slightly different `double` representations, causing non-deterministic ordering and inconsistent pagination. Secondary sort by wins and playerName provides a tiebreaker, but floating-point edge cases remain. | 1 | 2 | 2 | Use integer cross-multiplication for win rate comparison (wins1 × total2 vs wins2 × total1); add unit test with identical win rates | Monitor |
| R-008 | OPS | No metrics, logging, or alerting configured for the new `/api/v1/statistics/leaderboard` endpoint. Performance degradation or errors would go undetected in production. | 2 | 1 | 2 | Add Micrometer metrics (request count, p95 latency, error rate); add structured logging; configure alerts for 5xx errors | Ops |

### Risk Category Legend

- **TECH**: Technical/Architecture (flaws, integration, scalability)
- **SEC**: Security (access controls, auth, data exposure)
- **PERF**: Performance (SLA violations, degradation, resource limits)
- **DATA**: Data Integrity (loss, corruption, inconsistency)
- **BUS**: Business Impact (UX harm, logic errors, revenue)
- **OPS**: Operations (deployment, config, monitoring)

---

## NFR Planning

| NFR Category | Requirement / Threshold | Risk Link | Planned Validation | Evidence Needed |
|--------------|------------------------|-----------|-------------------|-----------------|
| Security | Unauthenticated GET `/api/v1/statistics/leaderboard` must return HTTP 401 | R-001 | API integration test with no auth token | Test report |
| Security | JPQL queries use parameterized binding (no string concatenation) | R-002 | Code review + SAST scan | SAST report |
| Security | Player nicknames are the only PII exposed (no emails, no IDs beyond UUID) | R-001 | API integration test verifying response schema excludes email | Test report |
| Performance | In-memory aggregation supports up to ~50 active players with <2s response | R-003 | k6 load test with seeded matches | k6/APm report |
| Reliability | Tied matches count as `totalMatches` without wins or losses | R-002 | Unit test (exists — `shouldCountFullyTiedMatches`) | Test report |
| Reliability | Backend errors surface as user-friendly messages (not crash) | R-006 | Component test mocking fetch rejection | Test report |
| Maintainability | Leaderboard service logic covered ≥80% by automated tests | - | Coverage report from `./mvnw test` | Coverage report |

**Unknown thresholds:** p95 latency target for leaderboard endpoint not defined in spec. Recommend 500ms as planning assumption for MVP scale. Rate limiting threshold not defined.

---

## Entry Criteria

- [x] Requirements and assumptions agreed upon by QA, Dev, PM (spec approved)
- [x] Test environment provisioned and accessible
- [x] Test data available or factories ready (existing `LeaderboardServiceTest` uses mock repository)
- [x] Feature deployed to test environment (implementation complete)
- [x] Story 4.2 implementation in working tree (uncommitted)

## Exit Criteria

- [ ] All P0 tests passing
- [ ] All P1 tests passing (or failures triaged)
- [ ] No open high-priority / high-severity bugs
- [ ] Test coverage agreed as sufficient (≥80% backend, ≥70% frontend)
- [ ] R-001, R-002, R-003 mitigations implemented or approved waivers

---

## Test Coverage Plan

> **P0/P1/P2/P3 = priority and risk classification, NOT execution timing.** See Execution Strategy section for timing.

### P0 (Critical)

**Criteria**: Blocks core journey + High risk (≥6) + No workaround

| Test ID | Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---------|-------------|------------|-----------|------------|-------|-------|
| 4.2-UNIT-001 | Aggregation computes correct wins/losses from CONFIRMED matches | Unit | R-002 | 1 | DEV | `shouldAggregateStatsCorrectly` — exists ✓ |
| 4.2-UNIT-002 | Fully tied matches count as totalMatches without win/loss | Unit | R-002 | 1 | DEV | `shouldCountFullyTiedMatches` — exists ✓ |
| 4.2-API-001 | Unauthenticated GET `/api/v1/statistics/leaderboard` returns 401 | API | R-001 | 1 | QA | Needs integration test |
| 4.2-API-002 | Authenticated GET returns 200 with `PageResponse<LeaderboardEntry>` | API | R-001 | 1 | QA | Needs integration test |
| 4.2-API-003 | Invalid page (−1) or size (0) returns HTTP 400 | API | – | 1 | QA | Needs integration test |
| 4.2-COMP-001 | LeaderboardView loads, displays entries sorted by win rate descending | Component | R-005 | 1 | DEV | Needs component test |
| 4.2-COMP-002 | Filter changes trigger re-fetch with correct query params | Component | – | 1 | DEV | Needs component test |

**Total P0**: 7 tests, ~12–20 hours

### P1 (High)

**Criteria**: Important features + Medium risk (3-5) + Common workflows

| Test ID | Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---------|-------------|------------|-----------|------------|-------|-------|
| 4.2-API-004 | Filter by rule system (STANDARD/RANDOM) returns correct subset | API | R-002 | 1 | QA | Unit test exists (`shouldFilterByRuleSystem`); needs API-level coverage |
| 4.2-API-005 | Filter by match type (1v1/2v2) returns correct subset | API | R-004 | 1 | QA | Unit test exists (`shouldFilterByMatchType`); needs API-level coverage |
| 4.2-API-006 | Filter by time period (WEEKLY/MONTHLY/YEARLY/ALL_TIME) | API | – | 1 | QA | Unit test exists (`shouldFilterByTimePeriod`); needs API-level coverage |
| 4.2-API-007 | Min matches threshold excludes players below threshold | API | – | 1 | QA | Unit test exists (`shouldExcludePlayersBelowThreshold`); needs API-level coverage |
| 4.2-API-008 | Position type filtering (ATTACKER/DEFENDER/OVERALL) | API | – | 1 | QA | Unit tests exist (`shouldFilterByAttackerPosition`, `shouldFilterByDefenderPosition`) |
| 4.2-API-009 | Repository and service filter consistency (same results) | API | R-002 | 1 | QA | Integration test: repository-only vs service output |
| 4.2-COMP-003 | Empty state shows when no results match filters | Component | R-006 | 1 | DEV | Needs component test |
| 4.2-COMP-004 | Pagination controls (Previous/Next) navigate correctly | Component | – | 1 | DEV | Needs component test |

**Total P1**: 8 tests, ~8–15 hours

### P2 (Medium)

**Criteria**: Secondary features + Low risk (1-2) + Edge cases

| Test ID | Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---------|-------------|------------|-----------|------------|-------|-------|
| 4.2-API-010 | Combined filters (rule + type + period) produce correct intersection | API | R-002 | 1 | QA | No existing test |
| 4.2-API-011 | Requesting page beyond last page returns empty content | API | – | 1 | QA | No existing test |
| 4.2-API-012 | `minMatches=0` includes all players even with 0 matches | API | – | 1 | QA | No existing test |
| 4.2-UNIT-003 | Sorting tie-breaker for identical win rates is deterministic | Unit | R-007 | 1 | DEV | No existing test |
| 4.2-COMP-005 | Backend error shows friendly message (not crash) | Component | – | 1 | DEV | No existing test |

**Total P2**: 5 tests, ~3–8 hours

### P3 (Low)

**Criteria**: Nice-to-have + Exploratory + Performance benchmarks

| Test ID | Requirement | Test Level | Test Count | Owner | Notes |
|---------|-------------|------------|-----------|-------|-------|
| 4.2-E2E-001 | Full leaderboard flow: navigate to `/leaderboard`, filter, paginate, verify sort | E2E | 1 | QA | Playwright |
| 4.2-PERF-001 | Performance baseline with 10k+ matches (R-003 validation) | E2E/Perf | 1 | QA | Deferred until R-003 mitigated |

**Total P3**: 2 tests, ~1–3 hours

---

## Execution Order

### PR Pipeline (<15 min)

- [ ] 4.2-UNIT-001, 4.2-UNIT-002 (Unit — existing)
- [ ] 4.2-API-001 through 4.2-API-012 (API integration tests)
- [ ] 4.2-COMP-001 through 4.2-COMP-005 (Component tests)

**Total**: ~15 scenarios, estimated ~3–5 min with parallel test execution

### Nightly

- [ ] 4.2-E2E-001 (Full leaderboard E2E flow)
- [ ] 4.2-PERF-001 (Performance baseline with large dataset)

**Total**: 2 scenarios, ~10–15 min

---

## Resource Estimates

### Test Development Effort

| Priority | Count | Hours/Test | Total Hours | Notes |
|----------|-------|------------|-------------|-------|
| P0 | 7 | 1.5–2.5 | ~12–20 hrs | Complex mocks, integration setup, security tests |
| P1 | 8 | 1.0–1.5 | ~8–15 hrs | API integration coverage, component tests |
| P2 | 5 | 0.5–1.0 | ~3–8 hrs | Edge cases, unit test for sorting |
| P3 | 2 | 0.5–1.0 | ~1–3 hrs | E2E + performance baseline |
| **Total** | **22** | – | **~24–46 hrs** | **~3–6 days** |

### Prerequisites

**Test Data:**
- Match factory with CONFIRMED status, configurable team structure (1v1/2v2), game scores, and timestamps
- User factory with nickname field (for `userRepository.findById` lookups in service)
- Seed data for combined-filter scenarios (matches spanning multiple rule systems, match types, and time periods)

**Tooling:**
- JUnit 6 + Mockito + Spring `@DataJpaTest` for backend API integration tests
- Vitest + Vue Test Utils for frontend component tests
- Playwright for E2E leaderboard flow test
- k6 for performance baseline (nightly)

**Environment:**
- Test database (H2 or Testcontainers) seeded with match data
- Backend running on test port with Spring Security enabled
- Frontend dev server for component tests

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2/P3 pass rate**: ≥90% (informational)
- **High-risk mitigations**: R-001, R-002, R-003 must be implemented or have approved waivers

### Coverage Targets

- **Critical paths**: ≥80%
- **Security scenarios**: 100%
- **Business logic**: ≥70%
- **Edge cases**: ≥50%

### Non-Negotiable Requirements

- [ ] All P0 tests pass
- [ ] No high-risk (≥6) items unmitigated without waiver
- [ ] Security tests (SEC category) pass 100%
- [ ] Performance baseline established or `nfr-assess` has documented CONCERNS/waivers
- [ ] Planned NFR evidence exists

---

## Mitigation Plans

### R-001: No explicit authentication on leaderboard endpoint (Score: 6)

**Mitigation Strategy:**
1. Verify Spring Security config protects `/api/v1/statistics/**` (check `SecurityConfig.java`)
2. Add `@PreAuthorize` annotation as defense-in-depth on `StatisticsController.getLeaderboard`
3. Add integration test: `GET /api/v1/statistics/leaderboard` without JWT returns 401
4. Add integration test: `GET /api/v1/statistics/leaderboard` with valid JWT returns 200

**Owner:** Backend  
**Timeline:** Sprint  
**Status:** Planned  
**Verification:** Spring Security config review + integration test asserting 401 without token

### R-002: Redundant dual filtering creates inconsistency risk (Score: 6)

**Mitigation Strategy:**
1. Remove in-memory filter re-application in `LeaderboardServiceImpl` (lines 40–50), trusting repository query
2. Keep `isPlayerInPosition` check in service (position filtering is NOT in repository query — correct)
3. Add integration test: seed matches with mixed formats/types/dates, verify repository-only filtering matches expected results
4. Add unit test: service applied to repository-filtered results preserves correct subset

**Owner:** Backend  
**Timeline:** Sprint  
**Status:** Planned  
**Verification:** Integration test comparing repository query results vs service output; 100% consistency

### R-003: In-memory aggregation doesn't scale (Score: 6)

**Mitigation Strategy:**
1. Document current limitation: suitable for MVP (10–20 active players)
2. Implement database-level aggregation using `GROUP BY player_id` with `SUM(CASE WHEN team_a_wins > team_b_wins THEN 1 ELSE 0 END)` pattern
3. Add performance test in k6: 10k matches, verify p95 < 500ms
4. Create follow-up ticket for Epic 4.6 migration

**Owner:** Backend  
**Timeline:** Epic 4.6  
**Status:** Planned  
**Verification:** k6 load test with 10k matches; p95 latency < 500ms

---

## Assumptions and Dependencies

### Assumptions

1. Spring Security is configured to protect `/api/v1/statistics/**` (spec AC-5 requires 401)
2. Existing `MatchRepository` query for CONFIRMED matches is performant (no N+1 issues)
3. `useStatsStore.spec.ts` test infrastructure is sufficient for leaderboard component tests
4. Test database can be seeded with matches having various statuses (CONFIRMED/PENDING/REJECTED)
5. Backend test environment allows direct API calls without browser-based OAuth flow

### Dependencies

1. Story 3.4–3.7 (context-aware verification, publication rules) — Required by test design; CONFIRMED match data must exist for seeding
2. Test data factory for Match/Game entities — Required by API integration tests
3. Spring Security configuration review — Required before R-001 mitigation test

### Risks to Plan

- **Risk**: R-003 in-memory aggregation limit exceeded before database-level migration
  - **Impact**: Slow leaderboard responses, potential OOM with >100 active players
  - **Contingency**: Add server-side circuit breaker; return error to frontend with "statistics unavailable, try again later" message

---

## Interworking & Regression

| Service/Component | Impact | Regression Scope |
|-------------------|--------|------------------|
| `StatisticsController` | New endpoint `/api/v1/statistics/leaderboard` added; no existing endpoints modified | Existing controller tests must pass; security config tests must pass |
| `LeaderboardService` / `LeaderboardServiceImpl` | New service; no existing services modified | Existing `MatchService` and related tests must pass; 277 backend tests must pass |
| `LeaderboardRepository` | New `findConfirmedMatchesWithFilters` query method; extends `JpaRepository<Match, UUID>` | Existing `MatchRepository` queries must remain unchanged; existing match tests must pass |
| `Match` model | Unchanged (read-only access to CONFIRMED matches) | All existing `Match` tests must pass |
| `statisticsService.ts` | `MatchTypeFilter` and `RuleSystemFilter` types added; `getLeaderboard()` extended with new params | Existing `statisticsService.spec.ts` test for `getLeaderboard` must be updated to verify new params are sent |
| `router/index.ts` | New `/leaderboard` route added; existing routes unchanged | Existing router tests must pass |
| `LeaderboardView.vue` | New component; no existing components modified | No existing frontend tests impacted |
| `useStatsStore.ts` | Unchanged (existing demo data logic for Story 4.1) | Existing `useStatsStore.spec.ts` tests must pass |

---

## Appendix

### Knowledge Base References

- `risk-governance.md` — Risk classification framework (scores ≥6 require mitigation, =9 blocks)
- `probability-impact.md` — Risk scoring methodology (1-3 scale, P × I = 1-9)
- `test-levels-framework.md` — Test level selection (unit → integration → E2E pyramid)
- `test-priorities-matrix.md` — P0-P3 prioritization (P0 = blocks core + high risk + no workaround)
- `nfr-criteria.md` — NFR validation thresholds (security, performance, reliability, maintainability)

### Related Documents

- Spec: `_bmad-output/implementation-artifacts/spec-4-2-global-leaderboard-with-filtering.md`
- Epic Context: `_bmad-output/implementation-artifacts/epic-4-context.md`
- Existing Tests: `src/test/java/com/tictactore/service/LeaderboardServiceTest.java` (12 unit tests)
- Frontend Service Test: `frontend/src/services/__tests__/statisticsService.spec.ts`
- Frontend Store Test: `frontend/tests/unit/useStatsStore.spec.ts`
- TEA Config: `_bmad/tea/config.yaml`

---

**Generated by**: BMad TEA Agent - Test Architect Module  
**Workflow**: `bmad-testarch-test-design`  
**Version**: 4.0 (BMad v6)
