---
workflowStatus: 'completed'
totalSteps: 5
stepsCompleted: ['step-01-detect-mode', 'step-02-load-context', 'step-03-risk-and-testability', 'step-04-coverage-plan', 'step-05-generate-output']
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-08-09T23:05:59+02:00'
---

# Test Design: Epic 2.7 - Global Player Search & Selection

**Date:** 2026-08-09
**Author:** Pavel
**Status:** Draft

---

## Executive Summary

**Scope:** Epic-level test design for Story 2.7

**Risk Summary:**
- Total risks identified: 8
- High-priority risks (≥6): 2
- Critical categories: SEC, PERF

**Coverage Summary:**
- P0 scenarios: 8 (~12–20 hours)
- P1 scenarios: 5 (~5–8 hours)
- P2/P3 scenarios: 2 (~1–2 hours)
- **Total effort**: ~18–30 hours (~2.5–4 days)

---

## Not in Scope

| Item | Reasoning | Mitigation |
|------|-----------|------------|
| Full-text search relevance tuning | Backend uses simple `LIKE` — acceptable for MVP | Monitor query performance; upgrade to full-text index if latency degrades |
| Avatar generation/validation | Existing `generateDeterministicAvatar` is covered by `UserServiceTest` | No new test needed |
| Frequent-opponents strip regression | Existing tests cover `getFrequentOpponents` | Run existing test suite |
| Match submission flow | Out of scope for search feature | Existing match tests cover submission |

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---------|----------|-------------|-------------|--------|-------|------------|-------|----------|
| R-001 | SEC | Public search endpoint enumerates all active users (nicknames + IDs) without authentication or rate limiting | 2 | 3 | 6 | Add rate limiting (10 req/min per IP), monitor for scraping patterns | Backend | Sprint |
| R-002 | PERF | Unbounded result set without pagination; large user base causes slow response and UI freeze | 2 | 3 | 6 | Implement server-side pagination (limit 50–100), add virtual scroll on frontend | Backend + Frontend | Sprint |

### Medium-Priority Risks (Score 3-5)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
|---------|----------|-------------|-------------|--------|-------|------------|-------|
| R-003 | TECH | Frontend/backend contract drift: `PlayerDto` shape changes break overlay rendering | 2 | 2 | 4 | Add integration test for `/players/search` response schema; TypeScript strict mode | Frontend |
| R-004 | BUS | Result ordering incorrect: frequent opponents not prioritized or alphabetical sort unstable | 2 | 2 | 4 | Add explicit test for ordering (frequent first, then alpha by nickname) | Frontend |
| R-005 | OPS | Public endpoint lacks monitoring/alerting; abuse or degradation goes undetected | 2 | 2 | 4 | Add metrics for request count, latency, error rate; set up 5xx alert | Ops |
| R-006 | SEC | Soft-delete filter bypass if deletion strategy changes | 1 | 3 | 3 | Document soft-delete convention; add integration test verifying excluded users never appear | Backend |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
|---------|----------|-------------|-------------|--------|-------|--------|
| R-007 | DATA | Stale search result selected after user deletion between search and selection | 1 | 2 | 2 | Document; overlay handles 404 gracefully |
| R-008 | PERF | Debounce timing drift degrades UX | 1 | 1 | 1 | Document; covered if debounce tested explicitly |

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
| Security | Public endpoint must not expose email addresses | R-001 | API integration test verifying response schema excludes email | Test report |
| Security | Soft-deleted accounts must never appear in results | R-006 | Unit test (exists) + integration test with deleted users | Test report |
| Performance | Search response p95 < 200ms for typical queries | R-002 | k6 load test or API performance test | k6/APM report |
| Reliability | Backend failure shows friendly error, frequent-opponents remains functional | R-002 | Component test for error state + E2E for degraded mode | Test report |
| Maintainability | Search logic covered by automated tests | R-003 | Coverage report | Coverage report |

**Unknown thresholds:** p95 latency target not defined in spec; recommend 200ms as starting point for validation.

---

## Entry Criteria

- [ ] Requirements and assumptions agreed upon by QA, Dev, PM
- [ ] Test environment provisioned and accessible
- [ ] Test data available or factories ready
- [ ] Feature deployed to test environment
- [ ] Story 2.7 implementation merged to test branch

## Exit Criteria

- [ ] All P0 tests passing
- [ ] All P1 tests passing (or failures triaged)
- [ ] No open high-priority / high-severity bugs
- [ ] Test coverage agreed as sufficient
- [ ] R-001 and R-002 mitigations implemented or approved waivers

---

## Test Coverage Plan

### P0 (Critical)

**Criteria**: Blocks core journey + High risk (≥6) + No workaround

| Test ID | Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---------|-------------|------------|-----------|------------|-------|-------|
| 2.7-API-001 | `GET /players/search` returns 200 with matching active users, excludes soft-deleted | API | R-001, R-006 | 1 | QA | Integration test against test DB |
| 2.7-API-002 | `GET /players/search?q=` with blank query returns empty list | API | — | 1 | QA | Edge case |
| 2.7-API-003 | `GET /players/search` returns 500 with friendly error when DB unavailable | API | R-002 | 1 | QA | Fault injection |
| 2.7-UNIT-001 | `UserService.searchActiveUsers` maps User → PlayerDto correctly | Unit | R-003 | 1 | DEV | Mock repository |
| 2.7-COMP-001 | Search overlay opens on empty slot tap, input focused | Component | — | 1 | DEV | Vue Test Utils |
| 2.7-COMP-002 | Typing triggers 300ms debounced API call | Component | R-002 | 1 | DEV | Mock fetch, advance timers |
| 2.7-COMP-003 | Selecting result calls `store.addPlayer`, closes overlay | Component | — | 1 | DEV | Mock store |
| 2.7-COMP-004 | Backend error shows friendly message, frequent-opponents still visible | Component | R-002 | 1 | DEV | Mock fetch rejection |

**Total P0**: 8 tests, ~12–20 hours

### P1 (High)

**Criteria**: Important features + Medium risk (3-4) + Common workflows

| Test ID | Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---------|-------------|------------|-----------|------------|-------|-------|
| 2.7-COMP-005 | Frequent opponents appear before alphabetical others | Component | R-004 | 1 | DEV | Seed mixed frequent/non-frequent results |
| 2.7-COMP-006 | Max players reached — additional selection is silently ignored | Component | — | 1 | DEV | Mock store with full selectedPlayers |
| 2.7-COMP-007 | Escape key and backdrop click close overlay without selection | Component | — | 1 | DEV | Keyboard + click events |
| 2.7-API-004 | Case-insensitive nickname matching works | API | R-006 | 1 | QA | Direct DB seed + API call |
| 2.7-API-005 | Special characters in query don't cause SQL errors | API | R-006 | 1 | QA | JPA param binding |

**Total P1**: 5 tests, ~5–8 hours

### P2 (Medium)

**Criteria**: Secondary features + Low risk (1-2) + Edge cases

| Test ID | Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---------|-------------|------------|-----------|------------|-------|-------|
| 2.7-UNIT-002 | Debounce timer cleared on overlay close | Unit | — | 1 | DEV | Mock setTimeout |
| 2.7-UNIT-003 | Empty query clears results without API call | Unit | — | 1 | DEV | Mock fetch |

**Total P2**: 2 tests, ~1–2 hours

---

## Execution Order

### PR Pipeline (<15 min)

- [ ] 2.7-API-001 (API integration)
- [ ] 2.7-API-002 (API edge case)
- [ ] 2.7-UNIT-001 (Unit)
- [ ] 2.7-COMP-001 through 2.7-COMP-004 (Component)
- [ ] 2.7-COMP-005 through 2.7-COMP-007 (Component)
- [ ] 2.7-API-004 through 2.7-API-005 (API)
- [ ] 2.7-UNIT-002 through 2.7-UNIT-003 (Unit)

**Total**: ~15 scenarios, estimated ~3–5 min with parallel test execution

### Nightly

- [ ] 2.7-API-003 (Fault injection with testcontainers)
- [ ] Performance baseline for `/players/search` (deferred until R-002 mitigation)

---

## Resource Estimates

### Test Development Effort

| Priority | Count | Hours/Test | Total Hours | Notes |
|----------|-------|------------|-------------|-------|
| P0 | 8 | 1.5–2.5 | ~12–20 hrs | Complex mocks, integration setup |
| P1 | 5 | 1.0–1.5 | ~5–8 hrs | Standard coverage |
| P2 | 2 | 0.5–1.0 | ~1–2 hrs | Simple scenarios |
| **Total** | **15** | — | **~18–30 hrs** | **~2.5–4 days** |

### Prerequisites

**Test Data:**
- User factory with soft-delete capability (deleted-*/ex-player-* prefixes)
- Seed data for frequent opponents + mixed nickname casing

**Tooling:**
- Vitest + Vue Test Utils for frontend component tests
- JUnit 6 + Mockito + Spring Test for backend API tests
- Playwright (optional) for E2E overlay interaction if needed

**Environment:**
- Test database with seed users
- Backend running on test port

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2 pass rate**: ≥90% (informational)
- **High-risk mitigations**: R-001 and R-002 must be implemented or approved waivers before release

### Coverage Targets

- **Critical paths**: ≥80%
- **Security scenarios**: 100%
- **Business logic**: ≥70%
- **Edge cases**: ≥50%

### Non-Negotiable Requirements

- [ ] All P0 tests pass
- [ ] No high-risk (≥6) items unmitigated without waiver
- [ ] Security tests (SEC category) pass 100%
- [ ] Performance targets met (PERF category) or deferred with plan
- [ ] Planned NFR evidence exists or `nfr-assess` has documented CONCERNS/waivers

---

## Mitigation Plans

### R-001: Public endpoint enumerates all active users (Score: 6)

**Mitigation Strategy:**
1. Implement IP-based rate limiting (10 requests/minute) on `/api/users/me/players/search`
2. Add monitoring for request volume anomalies
3. Consider CAPTCHA if abuse patterns detected

**Owner:** Backend team
**Timeline:** Sprint
**Status:** Planned
**Verification:** Load test confirms rate limit enforced; monitoring dashboard shows request metrics

### R-002: Unbounded result set causes performance degradation (Score: 6)

**Mitigation Strategy:**
1. Add `LIMIT 50` to `searchActiveUsers` query
2. Add `maxResults` parameter with validation (max 100)
3. Implement frontend virtual scroll or lazy rendering for large result sets
4. Add p95 latency metric and alert at 300ms

**Owner:** Backend + Frontend
**Timeline:** Sprint
**Status:** Planned
**Verification:** k6 load test with 10k users confirms p95 < 200ms; frontend renders 100+ results without freeze

---

## Assumptions and Dependencies

### Assumptions

1. Test database can be seeded with users having `deleted-*` and `ex-player-*` prefixes
2. Frequent opponents API (`/api/users/me/frequent-opponents`) remains stable and returns expected test data
3. Existing `PlayerSelection.spec.ts` test infrastructure is sufficient for new overlay tests
4. Backend test environment allows direct DB access for integration tests

### Dependencies

1. Story 2.7 implementation merged to test branch — Required by test design finalization
2. Test data factory for User entity — Required by API integration tests

### Risks to Plan

- **Risk**: R-001 rate limiting breaks existing `frequent-opponents` public access pattern
  - **Impact**: Existing public endpoints may need auth if IP limits too aggressive
  - **Contingency**: Scope rate limiting to `/players/search` only; exempt `/frequent-opponents`

---

## Interworking & Regression

| Service/Component | Impact | Regression Scope |
|-------------------|--------|------------------|
| `UserMatchController` | New endpoint added; existing endpoints unchanged | Existing `/frequent-opponents` and `/preferences/last-rule-system` tests must pass |
| `UserService` | New `searchActiveUsers` method added; existing methods unchanged | All existing `UserServiceTest` cases must pass |
| `UserRepository` | New query method added; existing queries unchanged | Existing repository integration tests must pass |
| `SecurityConfig` | New public endpoint registered; auth flow unchanged | Existing auth security tests must pass |
| `matchDraftStore` | New search state + action added; existing actions unchanged | Existing `matchDraftStore.spec.ts` tests must pass |
| `PlayerSelection.vue` | Search button + overlay mount added; existing slot rendering unchanged | Existing `PlayerSelection.spec.ts` tests must pass |

---

## Appendix

### Knowledge Base References

- `risk-governance.md` — Risk classification framework
- `probability-impact.md` — Risk scoring methodology
- `test-levels-framework.md` — Test level selection
- `test-priorities-matrix.md` — P0-P3 prioritization

### Related Documents

- Story Spec: `_bmad-output/implementation-artifacts/spec-2-7-global-player-search-and-selection.md`
- Backend Tests: `src/test/java/com/tictactore/service/UserServiceTest.java`
- Frontend Tests: `frontend/src/features/match/components/__tests__/PlayerSelection.spec.ts`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `bmad-testarch-test-design`
**Version**: 4.0 (BMad v6)
