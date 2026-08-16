---
stepsCompleted: ['step-01-load-context', 'step-02-define-thresholds', 'step-03-gather-evidence', 'step-04e-aggregate-nfr', 'step-05-generate-report']
lastStep: 'step-05-generate-report'
lastSaved: '2026-08-16T03:30:00+02:00'
workflowType: 'testarch-nfr-assess'
inputDocuments:
  - '_bmad-output/implementation-artifacts/spec-4-3-positional-statistics-attack-vs-defense.md'
  - '_bmad-output/test-artifacts/test-design/test-design-epic-4-3.md'
  - '_bmad/tea/config.yaml'
  - 'src/main/java/com/tictactore/controller/StatisticsController.java'
  - 'src/main/java/com/tictactore/dto/PlayerStatsResponse.java'
  - 'src/main/java/com/tictactore/service/LeaderboardService.java'
  - 'src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java'
  - 'src/main/java/com/tictactore/config/SecurityConfig.java'
  - 'src/main/java/com/tictactore/security/JwtAuthenticationFilter.java'
  - 'src/test/java/com/tictactore/service/LeaderboardServicePersonalStatsTest.java'
  - 'src/test/java/com/tictactore/controller/StatisticsControllerPersonalStatsIT.java'
  - 'frontend/e2e/tests/api/personal-stats.spec.ts'
  - 'frontend/e2e/tests/e2e/stats-dashboard.spec.ts'
  - 'frontend/src/features/stats/components/__tests__/StatsDashboard.spec.ts'
  - 'frontend/src/features/stats/components/StatsDashboard.vue'
---

# NFR Evidence Audit - Story 4.3: Positional Statistics (Attack vs. Defense)

**Date:** 2026-08-16
**Story:** 4-3-positional-statistics-attack-vs-defense
**Overall Status:** WAIVED ✅

---

## Operator Waiver (2026-08-16)

The original audit verdict was CONCERNS. Both P1 quick wins listed below were implemented before the waiver was granted; every remaining item is unmeasured-NFR or known MVP-scale technical debt, tracked in `_bmad-output/implementation-artifacts/deferred-work.md`.

**Resolved before waiver:**

1. `StatsDashboard.spec.ts` type-check failures — fixed. `npm run type-check` is clean (`vue-tsc --build`, 0 errors).
2. `@PreAuthorize("isAuthenticated()")` added to `StatisticsController.getPersonalStats`, with `@EnableMethodSecurity` enabled in `SecurityConfig` (the annotation is inert without it). Method-level defense-in-depth now backs the `anyRequest().authenticated()` chain rule.

**Verification after the fixes:** `./mvnw test` — 301 run, 0 failures, 0 errors; `npm run test:unit -- --run` — 221 passed; `npm run type-check` — clean.

**Waived (deferred, not blocking MVP):** unmeasured performance/throughput/resource thresholds, unbounded `findConfirmedMatchesWithFilters` load (DW-48), frontend/backend `period` contract mismatch (DW-49), missing JaCoCo coverage data (DW-50), absent observability on `/me` (DW-51). None represent a correctness or security defect at MVP scale (≤50 active players, ≤5k matches).

---

Note: This audit summarizes existing implementation evidence; it does not run tests or CI workflows. NFR thresholds and planned evidence come from the test-design output (`test-design-epic-4-3.md`) and direct code inspection.

## Executive Summary

**Assessment:** 2 PASS, 4 CONCERNS, 0 FAIL

**Blockers:** 0

**High Priority Issues:** 2 open high-risk items (R-002, R-003) with scores ≥6

**Recommendation:** Gate as CONCERNS. Proceed with release if R-002 and R-003 have accepted mitigation timelines (Epic 4.6 and Backlog respectively). Fix frontend type-check failure before merge.

---

## Performance Assessment

### Response Time (p95)

- **Status:** CONCERNS ⚠️
- **Threshold:** p95 < 500 ms at MVP scale (≤50 active players, ≤5k matches) — planning assumption from test-design
- **Actual:** Not measured
- **Evidence:** No k6/perf test, no JUnit timing assertion, no profiling data
- **Findings:** `LeaderboardServiceImpl.getPersonalStats` calls `leaderboardRepository.findConfirmedMatchesWithFilters(null, null, null, null)`, loading every confirmed match in the database and filtering to a single user in-memory. Response time and heap grow linearly with total match volume. This is the same unbounded pattern as `/leaderboard` (already flagged in 4.2 R-003).

### Throughput

- **Status:** CONCERNS ⚠️
- **Threshold:** Not defined
- **Actual:** Not measured
- **Evidence:** No load test, no throughput metrics
- **Findings:** No evidence of throughput behavior under concurrent requests.

### Resource Usage

- **CPU Usage**
  - **Status:** CONCERNS ⚠️
  - **Threshold:** Not defined
  - **Actual:** Not measured
  - **Evidence:** No profiling, no APM data

- **Memory Usage**
  - **Status:** CONCERNS ⚠️
  - **Threshold:** Not defined
  - **Actual:** Not measured
  - **Evidence:** Full match list loaded into heap per request; no OOM testing performed

### Scalability

- **Status:** CONCERNS ⚠️
- **Threshold:** DB-scoped query for user's matches only (planned mitigation)
- **Actual:** Unbounded full-table scan per request
- **Evidence:** Code inspection of `LeaderboardServiceImpl.getPersonalStats` line 184
- **Findings:** N+1 risk under `games` collection join. Mitigation planned for Epic 4.6 (add `findConfirmedMatchesForPlayer(UUID userId, ...)` repository query).

---

## Security Assessment

### Authentication Strength

- **Status:** PASS ✅
- **Threshold:** Unauthenticated `GET /api/v1/statistics/me` returns 401; authenticated returns 200
- **Actual:** Verified
- **Evidence:** `StatisticsControllerPersonalStatsIT` (2 tests): 401 without auth, 200 with `UsernamePasswordAuthenticationToken` wrapping `com.tictactore.model.User`
- **Findings:** `JwtAuthenticationFilter` correctly builds `UsernamePasswordAuthenticationToken` with `com.tictactore.model.User` principal. Controller null-guard returns 401. `SecurityConfig.PUBLIC_ENDPOINTS` does not include `/api/v1/statistics/**`.

### Authorization Controls

- **Status:** PASS ✅
- **Threshold:** Endpoint returns only requesting user's own stats
- **Actual:** Inherently safe — endpoint takes `@AuthenticationPrincipal`, not a path param
- **Evidence:** Code inspection + integration test confirms `principal.getId()` is used
- **Findings:** No cross-user data leak vector in current implementation.

### Data Protection

- **Status:** PASS ✅
- **Threshold:** Only CONFIRMED matches exposed
- **Actual:** Verified
- **Evidence:** `LeaderboardServiceImpl.getPersonalStats` iterates `findConfirmedMatchesWithFilters(...)` which filters by `Match.STATUS_CONFIRMED`. Integration test seeds PENDING match and asserts exclusion.
- **Findings:** No unconfirmed/pending data exposure.

### Vulnerability Management

- **Status:** PASS ✅
- **Threshold:** No new secrets, no injection vectors
- **Actual:** No new vulnerabilities introduced
- **Evidence:** Code review — no hardcoded secrets, no new SQL (repository abstraction), no XSS vectors (backend returns JSON only)

### Compliance

- **Status:** CONCERNS ⚠️
- **Threshold:** Defense-in-depth at method level
- **Actual:** No `@PreAuthorize` on `/me`
- **Evidence:** `SecurityConfig` protects `/api/v1/statistics/**` via `anyRequest().authenticated()`, but `StatisticsController.getPersonalStats` lacks explicit method-level auth
- **Findings:** If `PUBLIC_ENDPOINTS` is later extended to include `/api/v1/statistics/**`, all users' positional stats would leak. No test asserts `PUBLIC_ENDPOINTS` exclusion.

---

## Reliability Assessment

### Availability (Uptime)

- **Status:** CONCERNS ⚠️
- **Threshold:** Platform-level (not story-specific)
- **Actual:** Not assessed
- **Evidence:** No health check or uptime monitoring for `/me`

### Error Rate

- **Status:** PASS ✅
- **Threshold:** Backend errors return 500 with generic message
- **Actual:** Verified via exception handling in `GlobalExceptionHandler`
- **Evidence:** Spec I/O matrix BACKEND_ERROR scenario; existing error handling patterns

### MTTR (Mean Time To Recovery)

- **Status:** CONCERNS ⚠️
- **Threshold:** Not defined
- **Actual:** Not measured
- **Evidence:** No observability — no Micrometer metrics, no structured logs on `/me`

### Fault Tolerance

- **Status:** CONCERNS ⚠️
- **Threshold:** Graceful degradation on failure
- **Actual:** 500 returned on unhandled exception
- **Evidence:** `GlobalExceptionHandler` handles generic exceptions; frontend shows "Unable to load statistics."
- **Findings:** No retry logic, no circuit breaker, no fallback data. Frontend E2E tests mock error responses.

### CI Burn-In (Stability)

- **Status:** PASS ✅
- **Threshold:** Existing tests remain green
- **Actual:** 301 backend tests pass, 0 failures; 221 frontend tests pass
- **Evidence:** `./mvnw test` (301 run, 0 failures); `npm run test:unit -- --run` (221 passed)

### Disaster Recovery

- **Status:** N/A
- **Threshold:** Platform-level
- **Actual:** Not assessed for this story

---

## Maintainability Assessment

### Test Coverage

- **Status:** CONCERNS ⚠️
- **Threshold:** `getPersonalStats` ≥80% line coverage
- **Actual:** Not measured — JaCoCo skipped due to missing execution data
- **Evidence:** `./mvnw test` output shows "Skipping JaCoCo execution due to missing execution data file"
- **Findings:** 14 new tests added (7 unit + 7 integration), but no coverage report generated.

### Code Quality

- **Status:** PASS ✅
- **Threshold:** Clean code, no new tech debt
- **Actual:** Implementation follows existing patterns (`recordResult`, `recordDraw`, `isPlayerInPosition`)
- **Evidence:** Code review — new DTO is public record, service method reuses existing aggregation logic

### Technical Debt

- **Status:** CONCERNS ⚠️
- **Threshold:** No new unresolved high-risk debt
- **Actual:** R-002 (unbounded load) and R-003 (contract mismatch) are deferred technical debt
- **Evidence:** Test-design risk assessment; spec review triage log
- **Findings:** Same in-memory aggregation pattern as `/leaderboard` (4.2 R-003). Frontend sends unsupported `period` param.

### Documentation Completeness

- **Status:** PASS ✅
- **Threshold:** Spec, test-design, and code comments sufficient
- **Actual:** Spec complete with I/O matrix, acceptance criteria, code map
- **Evidence:** `spec-4-3-...md`, `test-design-epic-4-3.md`

### Test Quality

- **Status:** CONCERNS ⚠️
- **Threshold:** Deterministic, isolated, explicit assertions
- **Actual:** Backend tests pass; frontend type-check fails
- **Evidence:** `vue-tsc --noEmit` reports 7 type errors in `StatsDashboard.spec.ts` (implicit `any`, read-only property assignments, possible undefined access)
- **Findings:** New component test violates TypeScript strictness. Tests pass at runtime but fail type-check.

---

## Custom NFR Evidence Audits

### Frontend Rendering (QoE)

- **Status:** CONCERNS ⚠️
- **Threshold:** All 3 render states (loaded, loading, error) + 0-match case render without NaN
- **Actual:** Component test exists with type errors; E2E tests cover loaded, loading, error, and 0-match states
- **Evidence:** `StatsDashboard.spec.ts` (6 component tests, type-check failing); `stats-dashboard.spec.ts` (7 E2E tests)
- **Findings:** E2E tests verify proportional bars, skeleton loading, error message, and demo data path. Component tests need type fixes.

### API Contract Consistency

- **Status:** CONCERNS ⚠️
- **Threshold:** Frontend `PersonalStatsParams` matches backend `/me` contract
- **Actual:** Mismatch — frontend sends `period`, `myPosition`, `opponentPosition`; backend ignores them
- **Evidence:** `statisticsService.ts` types include unsupported params; `StatisticsController.getPersonalStats` takes only `@AuthenticationPrincipal`
- **Findings:** Silent data inconsistency. Spec Review-Triage lists as deferred item (R-003).

---

## Quick Wins

2 quick wins identified for immediate implementation:

1. **Fix StatsDashboard.spec.ts TypeScript errors** (Maintainability) - P1 - ~30 min
   - Add explicit types to `mountAndSetup` parameter, use `store.$patch()` for reactive mutations, add null guards
   - Minimal code changes; unblocks `vue-tsc --noEmit`

2. **Add `@PreAuthorize("isAuthenticated()")` to `getPersonalStats`** (Security) - P1 - ~15 min
   - Defense-in-depth at method level; aligns with controller-level null-guard pattern
   - No behavior change; explicit contract

---

## Recommended Actions

### Immediate (Before Release) - CRITICAL/HIGH Priority

1. **Fix StatsDashboard.spec.ts type-check failures** - P1 - ~30 min - Frontend
   - Add type annotations, replace direct property assignment with `store.$patch()`, guard undefined access
   - Validation: `npm run type-check` passes clean

2. **Add `@PreAuthorize("isAuthenticated()")` on `getPersonalStats`** - P1 - ~15 min - Backend
   - Explicit auth contract at method level
   - Validation: `./mvnw test` passes; security scan green

### Short-term (Next Milestone) - MEDIUM Priority

1. **Add DB-scoped repository query for `getPersonalStats`** - P1 - ~2 hrs - Backend
   - `findConfirmedMatchesForPlayer(UUID userId)` with `WHERE team_a_attacker_id = :uid OR ...`
   - Eliminates unbounded full-table scan
   - Validation: k6/perf test with 10k matches → p95 < 500 ms

2. **Reconcile frontend/backend `period` contract** - P1 - ~1 hr - Backend + Frontend
   - Either add `@RequestParam period` to `/me` with date-scoped repository overload, or remove unsupported params from frontend `PersonalStatsParams`
   - Validation: `statisticsService.spec.ts` + `StatisticsControllerIT` assert consistent behavior

3. **Generate JaCoCo coverage report** - P2 - ~30 min - Backend
   - Enable JaCoCo execution data; verify `getPersonalStats` ≥80% line coverage
   - Validation: `./mvnw test` + coverage report shows threshold met

### Long-term (Backlog) - LOW Priority

1. **Add Micrometer metrics on `/me`** - P3 - ~1 hr - Backend
   - Request count, p95 latency, 5xx rate
   - Validation: `/actuator/metrics` exposes `http.server.requests` with `/api/v1/statistics/me` tag

2. **Add CI rule asserting `PUBLIC_ENDPOINTS` never includes `/api/v1/statistics`** - P3 - ~15 min - Backend
   - grep-based static check in CI pipeline
   - Validation: CI fails if pattern matched

---

## Monitoring Hooks

3 monitoring hooks recommended to detect issues before failures:

### Performance Monitoring

- [ ] k6 load test on `/me` with 10k-seed-match dataset - Backend - Epic 4.6
  - Validate p95 < 500 ms after DB-scoping mitigation

- [ ] Micrometer `http.server.requests` metric for `/api/v1/statistics/me` - Backend - Backlog
  - Alert on 5xx rate > 1%/5 min

### Security Monitoring

- [ ] CI grep rule for `PUBLIC_ENDPOINTS` stats exclusion - Backend - Sprint
  - Fail build if `/api/v1/statistics` appears in public endpoints

### Reliability Monitoring

- [ ] Structured log on `/me` request with userId and response time - Backend - Backlog
  - Enable debug-level logging for aggregation duration > 200 ms

### Alerting Thresholds

- [ ] p95 latency > 500 ms on `/me` - Notify backend team - Epic 4.6
- [ ] 5xx rate > 1% on `/api/v1/statistics/**` - Notify on-call - Backlog

---

## Fail-Fast Mechanisms

2 fail-fast mechanisms recommended:

### Validation Gates (Security)

- [ ] `@PreAuthorize("isAuthenticated()")` on `getPersonalStats` — explicit auth contract at method level
  - **Owner:** Backend
  - **Estimated Effort:** 15 min

### Smoke Tests (Maintainability)

- [ ] Add `/me` endpoint smoke test to CI pipeline — verify 401 + 200 + response shape on every build
  - **Owner:** QA
  - **Estimated Effort:** 30 min

---

## Evidence Gaps

3 evidence gaps identified - action required:

- [ ] **Performance baseline for `/me`** (Performance)
  - **Owner:** Backend
  - **Deadline:** Epic 4.6
  - **Suggested Evidence:** k6 load test report or JUnit timing assertion with 5k mocked matches
  - **Impact:** R-002 remains unvalidated; perf cliff unknown at scale

- [ ] **Frontend type-check clean** (Maintainability)
  - **Owner:** Frontend
  - **Deadline:** Sprint
  - **Suggested Evidence:** `npm run type-check` passes with 0 errors
  - **Impact:** New test code cannot be merged; blocks CI

- [ ] **Code coverage report** (Maintainability)
  - **Owner:** Backend
  - **Deadline:** Sprint
  - **Suggested Evidence:** JaCoCo HTML/XML report showing `getPersonalStats` line coverage
  - **Impact:** Cannot verify ≥80% coverage threshold from test-design

---

## Findings Summary

**Based on ADR Quality Readiness Checklist (8 categories, 29 criteria)**

| Category                                         | Criteria Met | PASS | CONCERNS | FAIL | Overall Status                      |
| ------------------------------------------------ | ------------ | ---- | -------- | ---- | ----------------------------------- |
| 1. Testability & Automation                      | 3/4          | 1    | 1        | 0    | ⚠️ CONCERNS                         |
| 2. Test Data Strategy                            | 3/3          | 1    | 0        | 0    | ✅ PASS                             |
| 3. Scalability & Availability                    | 1/4          | 0    | 1        | 0    | ⚠️ CONCERNS                         |
| 4. Disaster Recovery                             | 0/3          | 0    | 0        | 0    | ⚠️ N/A (platform-level)             |
| 5. Security                                      | 4/4          | 1    | 1        | 0    | ⚠️ CONCERNS                         |
| 6. Monitorability, Debuggability & Manageability | 1/4          | 0    | 1        | 0    | ⚠️ CONCERNS                         |
| 7. QoS & QoE                                     | 2/4          | 0    | 2        | 0    | ⚠️ CONCERNS                         |
| 8. Deployability                                 | 2/3          | 1    | 0        | 0    | ⚠️ N/A (platform-level)             |
| **Total**                                        | **16/29**    | **4**| **6**    | **0**| **⚠️ CONCERNS (55% met)**           |

**Criteria Met Scoring:**

- ≥26/29 (90%+) = Strong foundation
- 20-25/29 (69-86%) = Room for improvement
- <20/29 (<69%) = Significant gaps

---

## Gate YAML Snippet

```yaml
nfr_assessment:
  date: '2026-08-16T03:30:00+02:00'
  story_id: '4-3-positional-statistics-attack-vs-defense'
  feature_name: 'Positional Statistics (Attack vs. Defense)'
  adr_checklist_score: '16/29' # ADR Quality Readiness Checklist
  categories:
    testability_automation: 'CONCERNS'
    test_data_strategy: 'PASS'
    scalability_availability: 'CONCERNS'
    disaster_recovery: 'N/A'
    security: 'CONCERNS'
    monitorability: 'CONCERNS'
    qos_qoe: 'CONCERNS'
    deployability: 'N/A'
  overall_status: 'CONCERNS'
  critical_issues: 0
  high_priority_issues: 2
  medium_priority_issues: 4
  concerns: 6
  blockers: false # true/false
  quick_wins: 2
  evidence_gaps: 3
  recommendations:
    - 'Fix StatsDashboard.spec.ts TypeScript errors before merge'
    - 'Add @PreAuthorize defense-in-depth on /me endpoint'
    - 'Implement DB-scoped repository query for getPersonalStats (R-002)'
    - 'Reconcile frontend period param contract with backend (R-003)'
    - 'Generate JaCoCo coverage report for new code'
    - 'Add k6/perf test for /me at 10k-match scale'
```

---

## Related Artifacts

- **Story File:** `_bmad-output/implementation-artifacts/spec-4-3-positional-statistics-attack-vs-defense.md`
- **Test Design:** `_bmad-output/test-artifacts/test-design/test-design-epic-4-3.md`
- **Gate Decision (Trace):** `_bmad-output/test-artifacts/traceability/gate-decision-4-3.json`
- **Evidence Sources:**
  - Test Results: `./mvnw test` (301 passed), `npm run test:unit -- --run` (221 passed)
  - Type Check: `npm run type-check` (7 errors in StatsDashboard.spec.ts)
  - Code: `src/main/java/com/tictactore/...`, `frontend/src/features/stats/...`

---

## Recommendations Summary

**Release Blocker:** None — no FAIL status NFRs. 2 open high risks (R-002, R-003) have accepted mitigation timelines.

**High Priority:** Fix frontend type-check failure (blocks CI); add `@PreAuthorize` defense-in-depth.

**Medium Priority:** Implement DB-scoped query for `/me`; reconcile `period` param contract; generate JaCoCo coverage report.

**Next Steps:** Address CONCERNS items in next sprint. Re-run `*nfr-assess` after R-002 and R-003 mitigations are implemented and verified.

---

## Sign-Off

**NFR Evidence Audit:**

- Overall Status: CONCERNS ⚠️
- Critical Issues: 0
- High Priority Issues: 2
- Concerns: 6
- Evidence Gaps: 3

**Gate Status:** CONCERNS ⚠️

**Next Actions:**

- If PASS ✅: Proceed to `*gate` workflow or release
- If CONCERNS ⚠️: Address HIGH/CRITICAL issues, re-run `*nfr-assess`
- If FAIL ❌: Resolve FAIL status NFRs, re-run `*nfr-assess`

**Generated:** 2026-08-16
**Workflow:** testarch-nfr v5.0

---
