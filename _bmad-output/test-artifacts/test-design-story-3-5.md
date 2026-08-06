# Test Design: Story 3.5 - Publication Rules & 24-hour Cooldown

**Date:** 2026-08-06
**Author:** Pavel
**Status:** Draft
**Scope:** Epic-Level test design for Story 3.5 within Epic 3

---

## Executive Summary

**Scope:** Epic-Level test design for Story 3.5 (Publication Rules & 24-hour Cooldown)

**Risk Summary:**

- Total risks identified: 7
- High-priority risks (≥6): 2
- Critical categories: DATA, BUS

**Coverage Summary:**

- P0 scenarios: 9 (~6-10 hours)
- P1 scenarios: 6 (~3-5 hours)
- P2 scenarios: 5 (~2-3 hours)
- P3 scenarios: 4 (~1-2 hours)
- **Total effort**: ~12-20 hours (~1.5-2.5 days)

---

## Not in Scope

| Item       | Reasoning      | Mitigation            |
| ---------- | -------------- | --------------------- |
| **Story 3.4 state machine** | PARTIALLY_CONFIRMED state and base confirmation logic are covered by Story 3.4 test design (`test-design-epic-3.md`) | Cooldown tests extend, not duplicate, 3.4 coverage |
| **Push notification wiring** | `sendCooldownReminderNotification` is dead code until wired to a trigger (deferred as DW-40) | Notification delivery is tested in Story 3.1; trigger wiring is future work |
| **Frontend E2E match flow** | Full match lifecycle E2E is covered by existing Playwright suites | New countdown timer is validated at Component level |
| **Database migration V8 rollback** | V8 is additive (nullable column); Flyway rollback behavior is standard | Forward migration tested via integration test |
| **24-hour duration configurability** | Hardcoded 24h is accepted for MVP (deferred as DW-41) | Magic number risk documented; no test for non-existent config |

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description   | Probability | Impact | Score | Mitigation   | Owner   | Timeline |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------------ | ------- | -------- |
| R-001   | DATA      | Scheduled job race with manual confirmation: `processExpiredCooldowns()` could attempt to transition a match to CONFIRMED while an opponent is simultaneously confirming it, leading to duplicate `confirmedAt` updates or optimistic-lock conflicts. | 2           | 3      | 6     | 1) Repository query filters by `PARTIALLY_CONFIRMED` to avoid stale reads. 2) `publishAfterCooldown()` guards with status check. 3) `@Transactional` ensures single-threaded DB mutation per row. 4) Existing error-continuation test (`shouldContinue_whenOneMatchFails`) validates partial-failure resilience. | Dev | 2026-08-06 |
| R-002   | BUS      | Client-side countdown displays incorrect remaining time due to clock skew. Server stores `cooldownExpiresAt` in UTC, but client computes `remaining = cooldownExpiresAt - localNow`. If client clock is wrong, timer jumps or shows negative values, undermining the "24-hour transparency" requirement. | 2           | 3      | 6     | 1) Frontend composable (`usePendingMatches.ts`) should compute remaining from server-provided `cooldownExpiresAt` using a locally anchored interval, not raw delta against device clock. 2) Display formatted absolute expiry time as fallback. 3) Component tests mock fixed `cooldownExpiresAt` and verify formatted output across timezone offsets. | QA/Dev | 2026-08-07 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description   | Probability | Impact | Score | Mitigation   | Owner   |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------------ | ------- |
| R-003   | TECH     | `requiresCooldown()` duplicates `supportsPartialConfirmation()` logic. If one rule changes without the other, cooldown could be set on matches that don't support partial confirmation, or vice versa. | 2           | 2      | 4     | Unit tests for both methods with identical input matrix (1v1, 2v2 STANDARD, 2v2 RANDOM, 2v2 REFEREE, participant vs referee entry). Consolidation tracked as DW-43. | Dev |
| R-004   | OPS      | Scheduled job error swallowing without dead-letter queue or alerting (DW-42). A systemic failure (DB outage, JPA exception) stops auto-publication silently, leaving matches stuck in PARTIALLY_CONFIRMED indefinitely. | 1           | 3      | 3     | Acceptable for MVP. Mitigation deferred: add monitoring/alerting in future sprint. Existing log-based error handling provides traceability. | Dev |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description   | Probability | Impact | Score | Action  |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------- |
| R-005   | DATA      | `rejectByOpponent()` clears `cooldownExpiresAt`, but a race between rejection and scheduled job could leave stale data if job reads before rejection commits. | 1           | 2      | 2     | Monitor |
| R-006   | PERF      | Scheduled job scans all PARTIALLY_CONFIRMED matches every 60 seconds. Under high pending-match volume, repeated DB queries create load. | 1           | 1      | 1     | Monitor |
| R-007   | OPS      | `@EnableScheduling` added to `TicTacToreApplication.java` could be accidentally removed during refactoring, silently disabling the cooldown job. | 1           | 2      | 2     | Monitor |

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

| NFR Category    | Requirement / Threshold | Risk Link | Planned Validation                         | Evidence Needed                  |
| --------------- | ----------------------- | --------- | ------------------------------------------ | -------------------------------- |
| Reliability     | Cooldown auto-publication must be idempotent and resilient to partial failures (one bad match must not stop the batch). | R-001 | Unit test: `shouldContinue_whenOneMatchFails` + transactional rollback verification. | JUnit test report with concurrency scenarios. |
| Maintainability | Cooldown logic lives in domain entity (`Match`), not service layer. `Match` methods must remain deterministic and side-effect free. | R-003 | Unit tests for `confirmByOpponent()`, `publishAfterCooldown()`, `isInCooldown()`, `isCooldownExpired()` in isolation. | JaCoCo coverage report for `MatchTest`. |
| Security        | No new auth surface introduced. Existing `SecurityContext` extraction and `UnauthorizedMatchActionException` behavior preserved. | - | Existing controller auth tests + new unauthorized cooldown bypass tests. | Spring MockMvc test report. |
| Performance     | Scheduled job query must use indexed `cooldown_expires_at` + `status` columns. Job must complete in <500ms for 10k expired matches. | R-006 | Integration test with 100 mock matches; measure query plan via H2/PostgreSQL explain. | Query execution plan + JMH baseline (optional). |

**Unknown thresholds:** Exact max acceptable job execution time for production dataset size is UNKNOWN and should be validated in `nfr-assess` after load testing.

---

## Entry Criteria

- [x] Requirements and assumptions agreed upon by QA, Dev, PM
- [x] Test environment provisioned and accessible
- [x] Test data available or factories ready
- [x] Feature deployed to test environment
- [x] Existing match confirmation test suite passing (baseline from Story 3.4)

## Exit Criteria

- [ ] All P0 tests passing
- [ ] All P1 tests passing (or failures triaged)
- [ ] No open high-priority / high-severity bugs
- [ ] Test coverage agreed as sufficient
- [ ] All AC1–AC6 acceptance criteria have passing test coverage

---

## Test Coverage Plan

### P0 (Critical) - Run on every commit

**Criteria**: Blocks core journey + High risk (≥6) + No workaround

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| AC1: 2v2 standard first confirm → PARTIALLY_CONFIRMED + cooldownExpiresAt set | Unit | R-001 | 2 | DEV | State transition + 24h expiry verification |
| AC2: Second confirm during cooldown → CONFIRMED + cooldown cleared | Unit | R-001 | 2 | DEV | State transition + idempotency |
| AC3: Scheduled job auto-publishes expired PARTIALLY_CONFIRMED match | Unit | R-001 | 1 | DEV | processExpiredCooldowns() + publishAfterCooldown() |
| AC4: 1v1, 2v2 RANDOM, 2v2 REFEREE → no cooldown set | Unit | R-003 | 2 | DEV | requiresCooldown() negative matrix |
| AC5: Double confirmation → idempotent, no state change | Unit | R-001 | 1 | DEV | hasConfirmed() early return |
| AC6: Frontend countdown timer renders remaining hours/minutes | Component | R-002 | 1 | QA/DEV | usePendingMatches composable + PendingMatches.vue |

**Total P0**: 9 tests, ~6-10 hours

### P1 (High) - Run on PR to main

**Criteria**: Important features + Medium risk (3-4) + Common workflows

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| Scheduled job continues when one match fails to publish | Unit | R-001 | 1 | DEV | Error-continuation resilience |
| Cooldown exactly at expiry boundary | Unit | R-001 | 1 | DEV | isCooldownExpired() boundary |
| Rejection during cooldown clears cooldownExpiresAt | Unit | R-005 | 1 | DEV | rejectByOpponent() + field nullification |
| MatchResponse DTO serializes cooldownExpiresAt | Integration | - | 1 | DEV | Controller JSON path assertion |
| Frontend countdown handles timezone offset correctly | Component | R-002 | 1 | QA/DEV | Mock different client offsets |
| Frontend countdown shows "Expired" when cooldownExpiresAt <= now | Component | R-002 | 1 | QA/DEV | Edge case: already expired match |

**Total P1**: 6 tests, ~3-5 hours

### P2 (Medium) - Run nightly/weekly

**Criteria**: Secondary features + Low risk (1-2) + Edge cases

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| CONFIRMED match with stale cooldown_expires_at in DB is skipped by job | Integration | R-005 | 1 | DEV | Query status filter prevents reprocessing |
| Empty PARTIALLY_CONFIRMED result set handled gracefully | Unit | - | 1 | DEV | Already covered in MatchCooldownServiceTest |
| Scheduled job skips non-expired cooldowns | Unit | - | 1 | DEV | Already covered in MatchCooldownServiceTest |
| `publishAfterCooldown()` throws when match not PARTIALLY_CONFIRMED | Unit | - | 1 | DEV | InvalidMatchStateException guard |
| `publishAfterCooldown()` throws when cooldown not yet expired | Unit | - | 1 | DEV | InvalidMatchStateException guard |

**Total P2**: 5 tests, ~2-3 hours

### P3 (Low) - Run on-demand

**Criteria**: Nice-to-have + Exploratory + Performance benchmarks

| Requirement   | Test Level | Test Count | Owner | Notes   |
| ------------- | ---------- | ---------- | ----- | ------- |
| Concurrent scheduled job + manual confirmation on same match | Exploratory | 1 | QA | Optimistic locking / race condition |
| GC pressure from repeated `isInCooldown()` under 10k pending matches | Exploratory | 1 | QA | Performance baseline |
| Clock skew: client 5 minutes behind server | Exploratory | 1 | QA/DEV | Countdown shows "Expired" prematurely |
| `@EnableScheduling` accidentally removed during refactoring | Exploratory | 1 | DEV | Integration test verifying job context loads |

**Total P3**: 4 tests, ~1-2 hours

---

## Execution Strategy

**Philosophy:** Run everything in PRs unless there is significant infrastructure overhead.

### Every PR: Unit + Integration + Component Tests (~5 min)

All functional tests (P0, P1, P2) using Maven + JUnit 5 + Mockito + Vitest:

- Match entity state transition tests
- MatchCooldownService scheduled job tests
- VerificationRules requiresCooldown() tests
- MatchService confirmation flow tests
- MatchController DTO serialization tests
- Frontend component tests (usePendingMatches + PendingMatches.vue)

**Why run in PRs:** Fast feedback, no expensive infrastructure.

### Nightly: E2E Tests (~10 min)

Playwright E2E tests covering the full match confirmation journey with countdown timer visible.

**Why defer to nightly:** Requires full environment (frontend + backend + DB).

### Weekly: Exploratory & Performance (~1-2 hours)

- Concurrent confirmation + scheduled job stress test
- GC/performance baseline for cooldown helpers
- Clock-skew boundary validation

**Why defer to weekly:** Low frequency, requires special setup or manual validation.

---

## Resource Estimates

### Test Development Effort

| Priority  | Count | Effort Range       | Notes                   |
| --------- | ----- | ------------------ | ----------------------- |
| P0        | 9     | ~6-10 hours        | Complex state transitions, scheduled job, frontend countdown |
| P1        | 6     | ~3-5 hours         | Standard coverage       |
| P2        | 5     | ~2-3 hours         | Simple scenarios        |
| P3        | 4     | ~1-2 hours         | Exploratory             |
| **Total** | **24**| **~12-20 hours**   | **~1.5-2.5 days**       |

**Assumptions:**

- Includes test design, implementation, debugging, CI integration
- Excludes ongoing maintenance (~10% effort)
- Assumes test infrastructure (factories, fixtures) ready from prior stories

**Prerequisites**

**Test Data:**

- Match factory with all format/mode permutations (STANDARD/RANDOM, PARTICIPANT/REFEREE)
- Fixture for PARTIALLY_CONFIRMED match with configurable cooldownExpiresAt

**Tooling:**

- JUnit 5 + Mockito + AssertJ for backend unit/integration tests
- Vitest for frontend component tests
- Spring `@Scheduled` test support (fixed rate override or manual trigger)

**Environment:**

- H2 in-memory DB for integration tests
- UTC-timezone JVM for deterministic Instant calculations
- Frontend test runner with mocked Date for countdown

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2/P3 pass rate**: ≥90% (informational)
- **High-risk mitigations**: 100% complete or approved waivers

### Coverage Targets

- **Critical paths (cooldown state machine)**: ≥90%
- **Business logic (Match entity)**: ≥80%
- **Scheduled job**: 100% branch coverage
- **Frontend countdown**: ≥80%

### Non-Negotiable Requirements

- [ ] All P0 tests pass
- [ ] No high-risk (≥6) items unmitigated
- [ ] All AC1–AC6 acceptance criteria have passing test coverage
- [ ] Scheduled job test coverage ≥90%
- [ ] Planned NFR evidence exists or `nfr-assess` has documented CONCERNS/waivers

---

## Mitigation Plans

### R-001: Scheduled job race with manual confirmation (Score: 6)

**Mitigation Strategy:**
1. Repository query `findByCooldownExpiresAtBeforeAndStatus` filters by `PARTIALLY_CONFIRMED` to exclude already-transitioned matches.
2. `publishAfterCooldown()` asserts `STATUS_PARTIALLY_CONFIRMED` before mutating state.
3. `@Transactional` on `processExpiredCooldowns()` ensures each match is processed in its own transaction boundary.
4. Existing error-continuation test verifies batch resilience.

**Owner:** Dev
**Timeline:** 2026-08-06
**Status:** Complete
**Verification:** `./mvnw test -Dtest=MatchCooldownServiceTest` passes (5 tests).

### R-002: Client-side countdown clock skew (Score: 6)

**Mitigation Strategy:**
1. Frontend composable calculates remaining time from server-provided `cooldownExpiresAt` using a monotonically advancing local interval.
2. Display formatted absolute expiry time as fallback when remaining time is ambiguous.
3. Component tests mock fixed `cooldownExpiresAt` and verify output at various simulated timezone offsets.

**Owner:** QA/Dev
**Timeline:** 2026-08-07
**Status:** Planned
**Verification:** Component tests pass; manual E2E validation with client clock skew.

---

## Assumptions and Dependencies

### Assumptions

1. Existing match submission and push notification infrastructure (Stories 3.1–3.2) remains stable.
2. Database migration V8 runs successfully on both H2 (test) and PostgreSQL (prod).
3. Frontend `PendingMatches.vue` correctly receives and renders the new `cooldownExpiresAt` field from `MatchResponse`.
4. `MatchRepository.findByCooldownExpiresAtBeforeAndStatus` query performs efficiently with a composite index on `(cooldown_expires_at, status)`.

### Dependencies

1. **Story 3.4 (Context-Aware Verification Rules)** - Required (PARTIALLY_CONFIRMED state must exist before cooldown can be introduced).
2. **Story 3.1 (Match Submission)** - Required (match creation and initial PENDING_APPROVAL flow).

### Risks to Plan

- **Risk**: Business changes cooldown duration from 24h to another value.
  - **Impact**: Requires code change in `Match.confirmByOpponent()`.
  - **Contingency**: Extract to `application.properties` or feature flag (tracked as DW-41).

---

## Interworking & Regression

| Service/Component | Impact         | Regression Scope                | Validation Steps              |
| ----------------- | -------------- | ------------------------------- | ----------------------------- |
| **Match** | `confirmByOpponent()` now sets/clears `cooldownExpiresAt`; `rejectByOpponent()` clears it; new helpers `isInCooldown()`, `isCooldownExpired()`, `publishAfterCooldown()` | Existing state transition tests (Story 3.4) must pass | Run `MatchTest` + `MatchServiceTest` |
| **MatchCooldownService** | New `@Scheduled` job scanning expired cooldowns | No existing scheduled jobs in project; new infrastructure | Run `MatchCooldownServiceTest` |
| **MatchRepository** | New `findByCooldownExpiresAtBeforeAndStatus` query method | Existing query methods must not be affected | Run repository integration tests |
| **VerificationRules** | New `requiresCooldown()` rule | Existing rules engine tests must pass | Run `VerificationRulesTest` |
| **MatchResponse** | New `cooldownExpiresAt` field in DTO | Existing JSON serialization tests must not break | Run `MatchControllerTest` |
| **Frontend usePendingMatches** | New `cooldownExpiresAt` exposure + countdown logic | Existing pending match fetch/compose tests must pass | Run `usePendingMatches.spec.ts` |
| **Frontend PendingMatches** | New countdown timer render for PARTIALLY_CONFIRMED | Existing match list rendering tests must pass | Run `PendingMatches.spec.ts` |

**Regression test strategy:**

- Run full backend test suite (`./mvnw clean verify`) before release.
- Run full frontend unit test suite (`npm run test:unit -- --run`) before release.
- Run relevant E2E tests (match confirmation, rejection) to verify no regression in user-facing flows.

---

## Appendix

### Knowledge Base References

- `risk-governance.md` - Risk classification framework
- `probability-impact.md` - Risk scoring methodology
- `test-levels-framework.md` - Test level selection
- `test-priorities-matrix.md` - P0-P3 prioritization

### Related Documents

- Story Spec: `_bmad-output/implementation-artifacts/spec-3-5-publication-rules-and-24-hour-cooldown.md`
- Epic Context: `_bmad-output/implementation-artifacts/epic-3-context.md`
- Prior Test Design: `_bmad-output/test-artifacts/test-design-epic-3.md` (Story 3.4)
- Deferred Work: `_bmad-output/implementation-artifacts/deferred-work.md`
- Production Code: `src/main/java/com/tictactore/model/Match.java`
- Production Code: `src/main/java/com/tictactore/service/MatchCooldownService.java`
- Production Code: `src/main/java/com/tictactore/rules/VerificationRules.java`
- Database Migration: `src/main/resources/db/migration/V8__add_cooldown_expires_at.sql`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `bmad-testarch-test-design`
**Version**: 5.0 (Step-File Architecture)
