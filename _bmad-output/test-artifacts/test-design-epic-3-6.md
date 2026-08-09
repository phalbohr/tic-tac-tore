---
workflowStatus: 'completed'
totalSteps: 5
stepsCompleted:
  - 'step-01-detect-mode'
  - 'step-02-load-context'
  - 'step-03-risk-and-testability'
  - 'step-04-coverage-plan'
  - 'step-05-generate-output'
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-08-07'
inputDocuments:
  - '_bmad-output/implementation-artifacts/spec-3-6-submission-rate-limiting-anti-spam.md'
  - '_bmad-output/implementation-artifacts/sprint-status.yaml'
  - '_bmad/tea/config.yaml'
  - 'src/main/java/com/tictactore/service/RateLimitService.java'
  - 'src/main/java/com/tictactore/service/impl/RateLimitServiceImpl.java'
  - 'src/main/java/com/tictactore/exception/RateLimitExceededException.java'
  - 'src/main/java/com/tictactore/exception/ApiError.java'
  - 'src/main/java/com/tictactore/exception/GlobalExceptionHandler.java'
  - 'src/main/java/com/tictactore/service/impl/MatchServiceImpl.java'
  - 'src/main/java/com/tictactore/config/ApplicationProperties.java'
  - 'src/main/resources/application.yml'
  - 'src/test/java/com/tictactore/service/RateLimitServiceTest.java'
  - 'src/test/java/com/tictactore/service/MatchServiceTest.java'
  - 'frontend/src/features/match/stores/matchDraftStore.ts'
  - 'frontend/src/features/match/stores/matchDraftStore.api-error.spec.ts'
  - 'frontend/src/features/match/stores/matchDraftStore.state-transition.spec.ts'
---

# Test Design: Epic 3-6 - Submission Rate Limiting (Anti-Spam)

**Date:** 2026-08-07
**Author:** Pavel
**Status:** Approved

---

## Executive Summary

**Scope:** Epic-Level test design for Story 3.6 (Submission Rate Limiting / Anti-Spam) within Epic 3. The feature adds server-side rate limiting to the match submission endpoint using Redis counters via Redisson, with per-user submission limits (10/hour) and rejection-based throttling (5 rejections/24h). Returns HTTP 429 when limits exceeded, HTTP 503 on Redis failure. Frontend `matchDraftStore.ts` handles 429 with retry-time error banner.

**Risk Summary:**

- Total risks identified: 7
- High-priority risks (score >=6): 2
- Critical categories: OPS, DATA, TECH

**Coverage Summary:**

- P0 scenarios: 14 (~4-6 hours)
- P1 scenarios: 7 (~6-10 hours)
- P2/P3 scenarios: 3 (~3-5 hours)
- **Total effort**: ~13-21 hours (~2-3 days)

---

## Not in Scope

| Item | Reasoning | Mitigation |
|---|---|---|
| **Redis cluster deployment & failover** | Redis operational setup is platform/ops responsibility, not application code | Monitored via existing Redis health checks; fail-closed behavior documented as R-001 |
| **Performance load testing under concurrent rate-limit contention** | Requires dedicated k6/Redis cluster; Story 3.6 is functionality-focused | Exploratory test planned at P3; load testing deferred to platform team |
| **Tournament referee rate-limit thresholds (30/hour)** | Explicitly deferred to Epic 8 backlog per spec constraints | Config property `tournamentSubmissionsPerHour` added as placeholder; no code branch reads it |
| **Rate-limit configuration tuning for production traffic** | Default thresholds (10/hour, 5/24h) are starting points requiring real-world calibration | Monitoring and alerting on rate-limit hit frequency (R-005); operational tuning via env vars |
| **Frontend E2E test for 429 banner rendering** | Requires full E2E stack with controlled rate-limit state injection; exceeds P0 scope | Covered via Vitest component tests in matchDraftStore.api-error.spec.ts; E2E can be added if needed |

---

## Risk Assessment

### High-Priority Risks (Score >=6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---|---|---|---|---|---|---|---|---|
| R-001 | OPS | Fail-closed Redis unavailability blocks ALL match submissions with HTTP 503. RateLimitServiceImpl catches RedisException and throws RateLimitExceededException with redisFailure=true, which GlobalExceptionHandler maps to 503. With a shared Redis instance, a network blip or restart during peak hours causes complete submission outage for all users. | 2 | 3 | 6 | Circuit breaker pattern with fallback allow + warning logging; Redis health check with graceful degradation; monitoring on RedisException frequency; runbook for Redis recovery. Existing fail-closed behavior documented and monitored. | Backend / DevOps | 2026-08-07 |
| R-002 | DATA | Fixed-window counter burst at hour boundaries allows rate-limit bypass. RAtomicLong key `rl:submissions:{userId}:{yyyy-MM-dd-HH}` resets at the top of each hour. A user can submit 10 matches in the last second of hour H and another 10 in the first second of hour H+1, effectively doubling throughput to 20/hour. Undermines the anti-spam purpose. | 3 | 2 | 6 | Document the fixed-window limitation in code comments; consider sliding-window log pattern in future iteration; existing tests validate the fixed-window behavior per spec. | Backend | 2026-08-07 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---|---|---|---|---|---|---|---|---|
| R-003 | TECH | `recordRejection()` silently swallows Redis failures. When Redis is unavailable during `rejectMatch`, recordRejection catches RedisException and logs a warning without propagating. Rejected matches are not counted toward the rejection throttle, allowing spammy users to bypass anti-rejection protection during outages. | 2 | 2 | 4 | Add alerting/metrics on `recordRejection` failure logs; add a RedisFailureCounter metric; verify alert fires on rejection recording failures. | Backend / Observability | 2026-08-07 |
| R-004 | BUS | Frontend error message doesn't distinguish submission limit from rejection throttle. When HTTP 429 is received, frontend shows generic "Rate limit exceeded" message without indicating whether the user hit the hourly submission limit or the rejection throttle. Users whose matches were rejected by opponents may not understand why they're blocked. | 2 | 2 | 4 | Backend could include a `subCode` field in error details; frontend could display context-specific messages. Future improvement tracked as P1 gap. | Fullstack | Future |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
|---|---|---|---|---|---|---|
| R-005 | OPS | Default rate-limit thresholds (10/hour submissions, 5/24h rejections) may be too restrictive or too lenient for production traffic patterns. Configurable via env vars with `${VAR:default}` syntax, but defaults are static values decided at spec time. | 1 | 3 | 3 | Monitor |
| R-006 | SEC | CreatorId used for rate-limit key may be client-supplied. `MatchServiceImpl.createMatch()` derives `creator` from `request.creatorId()` or falls back to `request.teamAAttackerId()`. If the backend doesn't validate creatorId against the authenticated principal, a user could bypass per-user rate limiting by cycling creatorIds in request payloads. (Pre-existing issue, not introduced by Story 3.6.) | 1 | 2 | 2 | Monitor |
| R-007 | TECH | `matchDraftStore.api-error.spec.ts` contains duplicate test cases for HTTP 429 (lines 66-96 and 98-126) and HTTP 503 (lines 128-155 and 157-187). Duplicate tests increase maintenance burden and may mask flaky behavior if one copy passes and the other fails. | 1 | 2 | 2 | Deduplicate test cases; keep one assertion per scenario. |

### Risk Category Legend

- **TECH**: Technical/Architecture (flaws, integration, scalability)
- **SEC**: Security (access controls, auth, data exposure)
- **PERF**: Performance (SLA violations, degradation, resource limits)
- **DATA**: Data Integrity (loss, corruption, inconsistency)
- **BUS**: Business Impact (UX harm, logic errors, revenue)
- **OPS**: Operations (deployment, config, monitoring)

---

## NFR Planning

**Purpose:** Capture epic-specific NFR thresholds, planned validation, and evidence expected for later `nfr-assess`. This is not a final evidence audit.

| NFR Category | Requirement / Threshold | Risk Link | Planned Validation | Evidence Needed |
|---|---|---|---|---|
| Reliability | Redis failures must not corrupt rate-limit state or cause data loss. RecordRejection failures are fire-and-forget (log only). CheckSubmissionLimit failures are fail-closed (503). | R-001, R-003 | Unit tests for RedisException paths; fail-closed behavior verified via RedisException injection. | JUnit test report (RateLimitServiceTest, RedisFailureTests). |
| Security | Rate limiting must enforce per-user limits using the authenticated principal's identity. Spam prevention: max 10 submissions/hour, 5 rejections/24h per user. | R-006 | Integration test verifying server-side identity extraction; code review of creatorId resolution. | Code review sign-off + controller auth tests. |
| Performance | Rate-limit checks must add <2ms latency to match submission under normal Redis connectivity. Counter operations are O(1) (RAtomicLong.incrementAndGet). Rejection window cleanup is O(log N) (sorted set range removal). | - | Microbenchmark RateLimitServiceImpl.checkSubmissionLimit with mocked Redisson (latency assertion <2ms). | JMH or JUnit timing assertion. |
| Maintainability | All thresholds configurable via `application.yml` with `${ENV_VAR:default}` fallbacks. No hardcoded values in production code. | R-005 | Config binding test verifying ApplicationProperties.RateLimit reads from YAML/env vars with correct defaults (10, 5, 24, 30). | Spring Boot configuration test report. |
| Observability | 429 and 503 responses must include standard error object `{ code, message, details { retryAfter } }`. RedisException must be logged with user context. | - | GlobalExceptionHandler unit test verifying response format; log statement inspection in recordRejection. | JUnit test report (GlobalExceptionHandler test). |

**Unknown thresholds:** No NFR thresholds are missing for this story — all values specified in the spec (10/hour, 5/24h, 429/503 responses).

---

## Entry Criteria

- [x] Story spec with 6 acceptance criteria reviewed and analyzed
- [x] Implementation code complete (RateLimitService, RateLimitServiceImpl, RateLimitExceededException, GlobalExceptionHandler, MatchServiceImpl, ApplicationProperties, application.yml, matchDraftStore.ts)
- [x] Existing unit tests in RateLimitServiceTest (10 tests) and MatchServiceTest (4 rate-limiting tests) written and passing
- [x] Fullstack environment available (Java 21 + Maven + Redis via Redisson + Vue 3 frontend)
- [x] CI script available: `./scripts/ci-local.sh` (backend `./mvnw clean verify` + frontend `npm ci`, `npm run type-check`, `npm run build`, `npm run test:unit`, `npm run test:e2e`)

## Exit Criteria

- [ ] All P0 tests passing
- [ ] All P1 tests passing (or failures triaged)
- [ ] No open high-priority (score >=6) risks unmitigated
- [ ] GlobalExceptionHandler 429 + 503 response format verified
- [ ] Frontend 429 handling verified in matchDraftStore.api-error.spec.ts
- [ ] Full backend test suite (`./mvnw clean verify`) passes
- [ ] Full frontend test suite passes

---

## Test Coverage Plan

**Note:** P0/P1/P2/P3 = priority and risk level, NOT execution timing. See "Execution Strategy" for when tests run.

### P0 (Critical)

**Criteria:** Blocks core functionality + High risk (score >=6) + No workaround

| Test ID | Requirement | Test Level | Risk Link | Owner | Notes | Status |
|---|---|---|---|---|---|---|
| P0-01 | AC1: RateLimitService allows submission when user has <10 submissions and <5 rejections | Unit | R-002 | DEV | Fixed-window counter under threshold; sorted set below rejection threshold | EXISTING |
| P0-02 | AC1: RateLimitService allows submission when counter exactly at limit (10) | Unit | R-002 | DEV | Boundary: count == threshold is allowed (only > threshold throws) | EXISTING |
| P0-03 | AC2: RateLimitService throws RateLimitExceededException when submission count exceeds limit (11 > 10) | Unit | R-002 | DEV | Fixed-window counter at threshold+1; retryAfterSeconds > 0 returned | EXISTING |
| P0-04 | AC2: MatchService.createMatch propagates RateLimitExceededException when checkSubmissionLimit throws | Unit | R-001 | DEV | Verifies exception is not swallowed in createMatch flow before persistence | EXISTING |
| P0-05 | AC3: RateLimitService throws when rejection count >= threshold (5 >= 5) | Unit | R-002 | DEV | Sliding-window sorted set at threshold; retryAfter computed from oldest entry | EXISTING |
| P0-06 | AC3: RateLimitService throws when rejection count exceeds threshold (7 > 5) | Unit | R-002 | DEV | Verifies count > threshold also triggers throttle | EXISTING |
| P0-07 | AC3: MatchService.createMatch propagates exception when rejection throttle triggers | Unit | R-003 | DEV | Verifies rejection-based throttling surfaces in createMatch | EXISTING |
| P0-08 | AC5: MatchService.createMatch does NOT call rateLimitService on idempotent resubmission | Unit | - | DEV | Idempotency check runs before rate-limit check; counter not incremented | EXISTING |
| P0-09 | AC6: RateLimitService throws RateLimitExceededException(redisFailure=true) on RedisException during submission check | Unit | R-001 | DEV | Fail-closed: RedisException caught and rethrown as RateLimitExceededException | EXISTING |
| P0-10 | AC6: RateLimitService throws RateLimitExceededException(redisFailure=true) on RedisException during rejection check | Unit | R-001 | DEV | Fail-closed at rejection check phase before submission counter | EXISTING |
| P0-11 | AC6: MatchService propagates redisFailure exception through createMatch | Unit | R-001 | DEV | End-to-end: RedisException → RateLimitExceededException → createMatch caller | EXISTING |
| P0-12 | AC3: MatchService.rejectMatch calls recordRejection before persisting rejection | Unit | R-003 | DEV | Verifies recordRejection is called in rejectMatch before matchOperation.rejectMatch | EXISTING |
| P0-13 | AC3: MatchService.createMatch triggers throttle after enough rejections (end-to-end) | Unit | R-002 | DEV | Simulated: recordRejection called N times, then createMatch triggers RateLimitExceededException | EXISTING |
| P0-14 | AC3: recordRejection does not throw on Redis failure (fire-and-forget) | Unit | R-003 | DEV | recordRejection catches RedisException, logs warning, does not propagate | EXISTING |

**Total P0:** 14 tests, ~4-6 hours

### P1 (High)

**Criteria:** Important features + Medium risk (3-4) + Common workflows

| Test ID | Requirement | Test Level | Risk Link | Owner | Notes | Status |
|---|---|---|---|---|---|---|
| P1-01 | AC2/6: GlobalExceptionHandler returns HTTP 429 with ApiError(code=RATE_LIMIT_EXCEEDED, message, details.retryAfter) | Unit | R-001 | DEV | Verify response status, body structure, retryAfter field present | NEW |
| P1-02 | AC6: GlobalExceptionHandler returns HTTP 503 with ApiError(code=RATE_LIMIT_UNAVAILABLE, details.retryAfter=0) when redisFailure=true | Unit | R-001 | DEV | Verify 503 status, RATE_LIMIT_UNAVAILABLE code, retryAfter=0 | NEW |
| P1-03 | AC4: Frontend matchDraftStore handles HTTP 429, sets submitError with retry-time message, returns CLIENT_ERROR | Component | R-004 | DEV | Verify submitError includes retryAfter seconds; ErrorToast displays it | EXISTING |
| P1-04 | AC6: Frontend matchDraftStore handles HTTP 503 as SERVER_OR_NETWORK_ERROR (not 429 banner) | Component | R-004 | DEV | 503 should not show "rate limit" message; falls to server error path | EXISTING |
| P1-05 | AC2: rateLimitService throws RateLimitExceededException with retryAfterSeconds > 0 for submission limit | Unit | R-002 | DEV | Verify computeSubmissionRetryAfter returns seconds until next hour boundary | PARTIAL |
| P1-06 | Config: ApplicationProperties.RateLimit defaults are 10/5/24/30 | Unit | R-005 | DEV | Verify default values in RateLimit nested class | NEW |
| P1-07 | Config: application.yml rate-limit block binds to ApplicationProperties via Spring Boot contextTest | Integration | R-005 | DEV | Verify env-var fallback `${TTT_RATE_LIMIT_*:default}` syntax resolves correctly | NEW |

**Total P1:** 7 tests, ~6-10 hours

### P2 (Medium)

**Criteria:** Secondary features + Low risk (1-2) + Edge cases

| Test ID | Requirement | Test Level | Risk Link | Owner | Notes | Status |
|---|---|---|---|---|---|---|
| P2-01 | Edge: Both submission limit and rejection throttle exceeded simultaneously returns 429 | Unit | R-002 | DEV | checkSubmissionLimit checks rejection first, then submission; verify first trigger wins | NEW |
| P2-02 | Edge: computeRejectionRetryAfter falls back to full window when sorted set is empty during throttle | Unit | R-002 | DEV | When entryRange returns empty, retryAfter = windowMs/1000 | NEW |
| P2-03 | Integration: Full createMatch flow with mocked Redis returns HTTP 429 when limit exceeded | Integration | R-001 | DEV | End-to-end: authenticated request → MatchController → MatchServiceImpl → RateLimitService → 429 response | NEW |

**Total P2:** 3 tests, ~3-5 hours

### P3 (Low)

**Criteria:** Nice-to-have + Exploratory + Performance benchmarks

| Test ID | Requirement | Test Level | Owner | Notes |
|---|---|---|---|---|
| P3-01 | Verify Redis keys `rl:submissions:{userId}:{hour}` and `rl:rejections:{userId}` created with correct TTLs | Exploratory | QA | Manual: submit match, inspect Redis keys and TTLs |
| P3-02 | Rate-limit check adds <2ms latency to match submission under normal Redis connectivity | Performance | QA | k6 or JUnit timing assertion under concurrent load |

**Total P3:** 2 scenarios, ~1-2 hours

---

## Execution Strategy

**Philosophy:** Run everything in PRs unless there is significant infrastructure overhead. With Playwright parallelization and fast Maven/JUnit runs, all functional tests complete in <15 minutes.

### Every PR: Unit + Integration + Frontend Tests (~10 min)

All functional tests (P0, P1, P2) using the project's existing toolchains:

- Backend: `./mvnw test -Dtest='RateLimitServiceTest,MatchServiceTest,GlobalExceptionHandler*'` — unit tests with mocked Redisson and mocked MatchService
- Frontend: `npm run test:unit -- --run --match 'matchDraftStore'` — Vitest component tests for 429/503 handling
- Full backend suite: `./mvnw clean verify` (243 tests, includes the 10 new RateLimitService tests + 4 new MatchService rate-limiting tests)
- Full frontend suite: `npm run test:unit -- --run` (154 tests)

**Why run in PRs:** Fast feedback, no special infrastructure needed (Redisson is mocked in unit tests).

### Nightly: E2E + Performance (~30 min)

- Playwright E2E: `npm run test:e2e` — full match submission, 429 banner rendering, rejection flow
- k6 performance: rate-limit check latency under concurrent submissions (P3-02)

**Why defer to nightly:** Requires full environment (frontend + backend + Redis + DB); k6 cloud infrastructure is expensive.

### Weekly: Chaos & Exploratory (~2 hours)

- Redis network partition simulation (P3-01): verify fail-closed behavior and key TTLs
- Manual inspection of Redis keys and rate-limit metrics

**Why defer to weekly:** Requires special setup (network simulation), low frequency validation sufficient.

---

## Resource Estimates

### Test Development Effort

| Priority | Count | Effort Range | Notes |
|---|---|---|---|
| P0 | 14 | ~4-6 hours | 12 existing tests + 2 already in MatchServiceTest; minimal new work |
| P1 | 7 | ~4-7 hours | 3 new tests (exception handler, config binding) + 2 existing (frontend 429/503) + 2 partial |
| P2 | 3 | ~3-5 hours | Edge cases, integration flow |
| P3 | 2 | ~1-2 hours | Exploratory, performance benchmark |
| **Total** | **26** | **~12-20 hours** | **~2-3 days (1 QA/DEV)** |

**Assumptions:**
- Includes test design, implementation, debugging, CI integration
- Excludes ongoing maintenance (~10% effort)
- Assumes test infrastructure (Mockito for Redisson, Pinia/Vitest for frontend) already established from prior stories

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: >=95% (waivers required for failures)
- **P2/P3 pass rate**: >=90% (informational)
- **High-risk mitigations (R-001, R-002)**: 100% complete or approved waivers

### Coverage Targets

- **RateLimitServiceImpl**: >=80% line coverage (fixed-window counter + sliding-window rejection logic)
- **GlobalExceptionHandler (rate-limit handlers)**: 100%
- **matchDraftStore 429/503 paths**: 100%
- **MatchServiceImpl rate-limit integration**: >=80% (createMatch + rejectMatch rate-limit call sites)

### Non-Negotiable Requirements

- [ ] All P0 tests pass (14 tests)
- [ ] GlobalExceptionHandler returns 429 with `details.retryAfter` and 503 with `RATE_LIMIT_UNAVAILABLE` code
- [ ] Frontend displays rate-limit error banner with retry time on HTTP 429
- [ ] No high-risk (score >=6) items unmitigated (R-001 fail-closed documented + monitored, R-002 fixed-window documented)
- [ ] Full backend test suite (`./mvnw clean verify`) passes with 0 failures
- [ ] Full frontend test suite passes with 0 failures

---

## Mitigation Plans

### R-001: Fail-closed Redis unavailability blocks ALL match submissions (Score: 6)

**Mitigation Strategy:**
1. Unit tests inject RedisException via mocked RedissonClient and verify 503 propagation path (EXISTING in RateLimitServiceTest.RedisFailureTests).
2. Add GlobalExceptionHandler test verifying 503 status, RATE_LIMIT_UNAVAILABLE code, retryAfter=0 (P1-02).
3. Add monitoring: instrument RedisException catch blocks with Micrometer counters; alert on >5 exceptions/minute.
4. Document fail-closed as intentional design choice in ADR or code comments; provide runbook for Redis recovery.

**Owner:** Backend / DevOps
**Timeline:** 2026-08-07
**Status:** Complete (tests) / Planned (monitoring)
**Verification:** `./mvnw test -Dtest='RateLimitServiceTest,GlobalExceptionHandler*'` passes; alerting rule deployed to monitoring.

### R-002: Fixed-window burst at hour boundaries (Score: 6)

**Mitigation Strategy:**
1. Unit test verifies boundary behavior: count == threshold is allowed, count == threshold+1 throws (EXISTING: shouldAllowSubmissionWhenExactlyAtLimit, shouldThrowWhenSubmissionExceedsLimit).
2. Document the fixed-window limitation in RateLimitServiceImpl class javadoc and spec.
3. Future: consider sliding-window log pattern (Redis sorted set with timestamps) to eliminate burst; tracked as P3 exploratory item.

**Owner:** Backend
**Timeline:** 2026-08-07
**Status:** Complete
**Verification:** `./mvnw test -Dtest='RateLimitServiceTest'` passes; code comment documents limitation.

---

## Assumptions and Dependencies

### Assumptions

1. Authenticated principal's userId is available in the security context and matches the `creatorId` used for rate-limit keying.
2. Redis is deployed and accessible via Redisson client configuration at application startup.
3. Frontend `ErrorToast` component already renders `submitError` (confirmed: HomeView.vue binds matchStore.submitError to ErrorToast).
4. Existing test infrastructure (Mockito, JUnit 5, Vitest) supports the new test scenarios without framework changes.
5. Redisson `RAtomicLong` and `RScoredSortedSet` APIs behave as documented (no mock-to-real divergence for core operations).

### Dependencies

1. **Story 3.4 (Context-Aware Verification Rules)** - Already done; provides the match state machine that rejection tracking depends on.
2. **Story 3.3 (Match Rejection)** - Already done; REJECTED state must exist for recordRejection to be meaningful.
3. **Redis availability** - Platform team provides Redis cluster; required by Story 3.6 for rate-limit counters.
4. **Prior system-level test design** - `test-design-architecture.md` + `test-design-qa.md` (Epic 3 system-level) provide NFR context and execution strategy.

### Risks to Plan

- **Risk:** Redis client configuration error causes Redisson to fail at startup, preventing application boot.
  - **Impact:** Complete service outage (worse than fail-closed per-request).
  - **Contingency:** Verify Redis connection at startup via health check; application should still boot even if Redis is temporarily unavailable (Redisson lazy connection).

- **Risk:** Mock-based tests pass but real Redis behaves differently (e.g., expire() timing, sorted set score eviction).
  - **Impact:** Tests give false confidence; production rate limiting doesn't work as expected.
  - **Contingency:** Add integration test with Testcontainers Redis (P2-03) to validate real Redis behavior.

---

## Interworking & Regression

| Service/Component | Impact | Regression Scope | Validation Steps |
|---|---|---|---|
| **RateLimitServiceImpl** | New service; no existing code modified except MatchServiceImpl injection point | RateLimitServiceTest (10 tests) + MatchServiceTest rate-limiting tests (4 tests) must pass | `./mvnw test -Dtest='RateLimitServiceTest,MatchServiceTest'` |
| **GlobalExceptionHandler** | New @ExceptionHandler for RateLimitExceededException; existing handlers unaffected | All existing exception handler tests must pass; new handler tests added | `./mvnw test -Dtest='*ExceptionHandler*'` |
| **MatchServiceImpl** | New RateLimitService injected; checkSubmissionLimit called after idempotency check; recordRejection called in rejectMatch | All existing MatchServiceTest tests (40+), MatchServiceATDDTest, MatchServiceDuplicateDetectionATDDTest must pass | `./mvnw test -Dtest='MatchService*Test'` |
| **ApplicationProperties** | New RateLimit nested class added; existing properties unaffected | Existing ApplicationProperties tests must pass; new RateLimit binding tests | `./mvnw test -Dtest='*ApplicationProperties*'` |
| **matchDraftStore.ts** | New 429 handling branch; existing executeCommit logic unchanged for other status codes | matchDraftStore.api-error.spec.ts (429/503 tests) + matchDraftStore.state-transition.spec.ts (state tests) must pass | `npm run test:unit -- --run --match 'matchDraftStore'` |
| **application.yml** | New rate-limit config block added; existing config unaffected (YAML indentation fix applied) | All Spring Boot configuration loading tests must pass; existing application.yml-bound properties still resolve | `./mvnw test` (full suite) |

**Regression test strategy:**
- Run full backend test suite (`./mvnw clean verify`) — all 243 existing + new tests must pass with 0 failures.
- Run full frontend unit test suite (`npm run test:unit -- --run`) — all 154 tests must pass.
- Run frontend type-check (`npm run type-check`) — 0 errors.
- Run frontend E2E (`npm run test:e2e`) — full match submission and rejection flows must work end-to-end.

---

## Appendix

### Knowledge Base References

- `risk-governance.md` — Risk classification framework (score ≥6 requires mitigation, score = 9 blocks)
- `probability-impact.md` — Risk scoring methodology (P 1-3 × I 1-3 = Score 1-9)
- `test-levels-framework.md` — Test level selection (unit for logic, integration for service flow, E2E for critical paths)
- `test-priorities-matrix.md` — P0-P3 criteria and priority-based execution ordering

### Related Documents

- Story Spec: `_bmad-output/implementation-artifacts/spec-3-6-submission-rate-limiting-anti-spam.md`
- Epic Context: `_bmad-output/implementation-artifacts/epic-3-context.md`
- Sprint Status: `_bmad-output/implementation-artifacts/sprint-status.yaml`
- TEA Config: `_bmad/tea/config.yaml`
- Production Code: `src/main/java/com/tictactore/service/RateLimitService.java`
- Production Code: `src/main/java/com/tictactore/service/impl/RateLimitServiceImpl.java`
- Production Code: `src/main/java/com/tictactore/exception/RateLimitExceededException.java`
- Production Code: `src/main/java/com/tictactore/exception/ApiError.java`
- Production Code: `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java`
- Production Code: `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java`
- Production Code: `src/main/java/com/tictactore/config/ApplicationProperties.java`
- Config: `src/main/resources/application.yml`
- Unit Test: `src/test/java/com/tictactore/service/RateLimitServiceTest.java`
- Integration Test: `src/test/java/com/tictactore/service/MatchServiceTest.java`
- Frontend Store: `frontend/src/features/match/stores/matchDraftStore.ts`
- Frontend Test: `frontend/src/features/match/stores/matchDraftStore.api-error.spec.ts`
- Frontend Test: `frontend/src/features/match/stores/matchDraftStore.state-transition.spec.ts`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `bmad-testarch-test-design`
**Version**: 5.0 (Step-File Architecture)
