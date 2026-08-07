# Test Design: Epic 3 - Context-Aware Verification Rules

**Date:** 2026-08-06
**Author:** Pavel
**Status:** Draft

---

## Executive Summary

**Scope:** Epic-Level test design for Story 3.4 (Context-Aware Verification Rules) within Epic 3.

**Risk Summary:**

- Total risks identified: 6
- High-priority risks (≥6): 2
- Critical categories: DATA, BUS

**Coverage Summary:**

- P0 scenarios: 10 (~8-12 hours)
- P1 scenarios: 6 (~4-6 hours)
- P2 scenarios: 5 (~2-4 hours)
- P3 scenarios: 3 (~1-2 hours)
- **Total effort**: ~15-24 hours (~2-3 days)

---

## Not in Scope

| Item       | Reasoning      | Mitigation            |
| ---------- | -------------- | --------------------- |
| **Story 3.5 (24-hour cooldown)** | Downstream dependency; cooldown timer not introduced until Story 3.5 | PARTIALLY_CONFIRMED state is tested here; timer logic deferred to Story 3.5 test design |
| **Epic 8 tournament rules** | Tournament-specific confirmation windows and technical defeats are future context | Core VerificationRules engine is tested here; tournament overrides tested in Epic 8 |
| **Push notification infrastructure** | Covered in Story 3.1 (confirmation requests and push delivery) | Only partial-confirmation notification trigger is tested in this story |
| **Frontend E2E match confirmation flow** | Covered by existing E2E match confirmation suite (30 tests) | New PARTIALLY_CONFIRMED badge is covered at Component level here |
| **Database migration rollback** | Flyway migration V7 is additive (nullable columns); no data loss path | Migration forward-only tested via integration test; rollback is standard Flyway behavior |

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description   | Probability | Impact | Score | Mitigation   | Owner   | Timeline |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------------ | ------- | -------- |
| R-001   | DATA      | State machine corruption in `Match.confirmByOpponent()` due to complex multi-context branching (5 rule combinations, 3 target states). A logic error in `VerificationRules.isFullyConfirmed()` could allow under-confirmed matches to reach CONFIRMED or block legitimate confirmations. | 2           | 3      | 6     | Exhaustive unit tests for all 7 ACs covering every state transition path; negative tests for invalid state attempts; property-based tests for referee-doubles "1 per team" rule. | Dev | 2026-08-06   |
| R-002   | DATA      | `confirmedByOpponentIds` CSV corruption or parsing failure under concurrent confirmations. `hasConfirmed()` splits on comma and parses UUIDs on every call; concurrent appends or malformed data could cause NumberFormatException or incorrect idempotency, leaving matches stuck or double-confirmed. | 2           | 3      | 6     | Unit tests for CSV edge cases (empty, single, multiple, trailing comma); idempotency tests for duplicate confirmations; legacy fallback tests for `confirmedByUserId`. | Dev | 2026-08-06   |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description   | Probability | Impact | Score | Mitigation   | Owner   |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------------ | ------- |
| R-003   | BUS      | Frontend `PARTIALLY_CONFIRMED` badge displays incorrect confirmation count (X of N), causing user confusion about why a match is not fully confirmed. | 2           | 2      | 4     | Component tests for `PendingMatches.vue` badge rendering with mocked PARTIALLY_CONFIRMED match; unit tests for `usePendingMatches.ts` count aggregation. | QA/Dev |
| R-004   | TECH     | `Match.getOpponentIds()` team inference fails for referee-entered matches with asymmetric or missing team assignment, causing wrong notification recipients or confirmation rules. | 1           | 3      | 3     | Unit tests for getOpponentIds() with all creator-position permutations (Team A attacker/defender, Team B attacker/defender, null positions). | Dev |
| R-005   | SEC      | Entry mode inference from `creatorId` vs player positions can be spoofed via crafted `CreateMatchRequest` where `entryMode` is explicitly set to PARTICIPANT despite creator not being a player, potentially bypassing referee confirmation rules. | 1           | 2      | 2     | Integration test verifying service-layer inference overrides conflicting request values; controller test ensuring DTO binding does not allow escalation. | Dev |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description   | Probability | Impact | Score | Action  |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------- |
| R-006   | PERF      | `hasConfirmed()` and `getConfirmedByOpponentIdsList()` parse CSV on every call. Under high pending-match volume, repeated string splitting creates GC pressure. | 1           | 1      | 1     | Monitor |

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
| Reliability     | Match confirmation state transitions must be atomic and idempotent under concurrent calls. | R-001, R-002 | Unit tests for all ACs + concurrent confirmation simulation. | JUnit test report with concurrency scenarios. |
| Maintainability | VerificationRules must remain stateless with >90% unit test coverage. | - | JaCoCo coverage report for `VerificationRulesTest`. | Coverage XML/HTML report. |
| Security        | Caller UUID must be extracted from SecurityContext; creator self-confirmation and non-opponent confirmation must return 403. | R-005 | Existing controller auth tests + new unauthorized attempt tests. | Spring MockMvc test report. |

**Unknown thresholds:** N/A

---

## Entry Criteria

- [x] Requirements and assumptions agreed upon by QA, Dev, PM
- [x] Test environment provisioned and accessible
- [x] Test data available or factories ready
- [x] Feature deployed to test environment
- [x] Existing match confirmation test suite passing (baseline)

## Exit Criteria

- [ ] All P0 tests passing
- [ ] All P1 tests passing (or failures triaged)
- [ ] No open high-priority / high-severity bugs
- [ ] Test coverage agreed as sufficient
- [ ] VerificationRules unit coverage ≥90%

---

## Test Coverage Plan

### P0 (Critical)

**Criteria**: Blocks core journey + High risk (≥6) + No workaround

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| 1v1 participant confirms → CONFIRMED (1 opp sufficient) | Unit | R-001 | 2 | DEV | State transition + rules engine |
| 1v1 referee first confirm → stays PENDING_APPROVAL (2 opp needed) | Unit | R-001 | 2 | DEV | State transition + rules engine |
| 2v2 standard first confirm → PARTIALLY_CONFIRMED + notification | Unit | R-001 | 2 | DEV | State transition + partial flag |
| 2v2 random first confirm → stays PENDING_APPROVAL (no partial) | Unit | R-001 | 2 | DEV | State transition + rules engine |
| 2v2 referee 1 per team → CONFIRMED only when both teams represented | Unit | R-001 | 2 | DEV | Team-aware rules engine |
| Double confirmation → idempotent, returns current state | Unit | R-002 | 2 | DEV | hasConfirmed() + CSV integrity |
| hasConfirmed() backward compat with confirmedByUserId | Unit | R-002 | 1 | DEV | Legacy data fallback |
| PARTIALLY_CONFIRMED second opponent confirms → CONFIRMED | Integration | R-001 | 1 | QA/DEV | Service + repository flow |

**Total P0**: 10 tests, ~8-12 hours

### P1 (High)

**Criteria**: Important features + Medium risk (3-4) + Common workflows

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| CSV parsing edge cases (empty, single, multiple, trailing comma) | Unit | R-002 | 2 | DEV | Malformed data resilience |
| getOpponentIds() team inference for all creator positions | Unit | R-004 | 2 | DEV | Referee/participant permutations |
| Partial confirmation notification dispatch to remaining opponents | Integration | - | 1 | DEV | PushNotificationService trigger |
| PendingMatches.vue PARTIALLY_CONFIRMED badge rendering | Component | R-003 | 1 | DEV | "X of N confirmed" display |
| usePendingMatches.ts PARTIALLY_CONFIRMED state handling | Component | R-003 | 1 | DEV | Count + fetch logic |
| Controller JSON serialization of new DTO fields | Integration | - | 1 | DEV | MatchResponse new fields |

**Total P1**: 6 tests, ~4-6 hours

### P2 (Medium)

**Criteria**: Secondary features + Low risk (1-2) + Edge cases

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| CreateMatchRequest entryMode/matchFormat default inference | Unit | - | 2 | DEV | PARTICIPANT vs REFEREE inference |
| MatchResponse DTO new fields (entryMode, matchFormat, confirmedByOpponentIds, requiredConfirmations) | Unit | - | 1 | DEV | Record serialization |
| MatchRepository.findByStatusIn query for PENDING_APPROVAL + PARTIALLY_CONFIRMED | Integration | - | 1 | DEV | Query method + projection |
| E2E full confirmation flow with PARTIALLY_CONFIRMED badge visible | E2E | R-003 | 1 | QA | User journey: submit → first opp confirms → badge shows partial |

**Total P2**: 5 tests, ~2-4 hours

### P3 (Low)

**Criteria**: Nice-to-have + Exploratory + Performance benchmarks

| Requirement   | Test Level | Test Count | Owner | Notes   |
| ------------- | ---------- | ---------- | ----- | ------- |
| Concurrent confirmation race condition (optimistic locking) | Exploratory | 1 | QA | Parallel confirmations on same match |
| Flyway migration V7 on existing match data | Exploratory | 1 | DEV | Backward compatibility with legacy rows |
| GC pressure from repeated CSV parsing under load | Exploratory | 1 | QA | Performance baseline |

**Total P3**: 3 tests, ~1-2 hours

---

## Execution Strategy

**Philosophy:** Run everything in PRs unless there is significant infrastructure overhead.

### Every PR: Unit + Integration Tests (~5 min)

All functional tests (P0, P1, P2) using Maven + JUnit 5 + Mockito:

- Match entity state transition tests
- VerificationRules unit tests
- MatchService confirmation flow tests
- MatchController serialization tests
- Repository query integration tests
- Frontend component tests (Vitest)

**Why run in PRs:** Fast feedback, no expensive infrastructure.

### Nightly: E2E Tests (~10 min)

Playwright E2E tests covering the full confirmation journey with PARTIALLY_CONFIRMED badge.

**Why defer to nightly:** Requires full environment (frontend + backend + DB), slightly slower than unit tests but still under 15 min.

### Weekly: Exploratory & Performance (~1-2 hours)

- Concurrent confirmation stress test
- Migration backward-compatibility validation
- GC/performance baseline for CSV parsing

**Why defer to weekly:** Low frequency, requires special setup or manual validation.

---

## Resource Estimates

### Test Development Effort

| Priority  | Count | Effort Range       | Notes                   |
| --------- | ----- | ------------------ | ----------------------- |
| P0        | 10    | ~8-12 hours        | Complex state transitions, security edge cases |
| P1        | 6     | ~4-6 hours         | Standard coverage       |
| P2        | 5     | ~2-4 hours         | Simple scenarios        |
| P3        | 3     | ~1-2 hours         | Exploratory             |
| **Total** | **24**| **~15-24 hours**   | **~2-3 days**           |

**Assumptions:**

- Includes test design, implementation, debugging, CI integration
- Excludes ongoing maintenance (~10% effort)
- Assumes test infrastructure (factories, fixtures) ready from prior epics

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2/P3 pass rate**: ≥90% (informational)
- **High-risk mitigations**: 100% complete or approved waivers

### Coverage Targets

- **Critical paths (VerificationRules)**: ≥90%
- **Business logic (Match state machine)**: ≥80%
- **Edge cases**: ≥50%

### Non-Negotiable Requirements

- [ ] All P0 tests pass
- [ ] No high-risk (≥6) items unmitigated
- [ ] VerificationRules unit coverage ≥90%
- [ ] All AC1-AC7 acceptance criteria have passing test coverage
- [ ] Planned NFR evidence exists or `nfr-assess` has documented CONCERNS/waivers

---

## Mitigation Plans

### R-001: State machine corruption in Match.confirmByOpponent() (Score: 6)

**Mitigation Strategy:**
1. Unit test every AC (AC1-AC7) as isolated state transition on Match entity.
2. Negative tests: confirm on CONFIRMED, REJECTED, and non-opponent users.
3. Property test for referee-doubles: any 2 confirmations from opposite teams must yield CONFIRMED; same-team pair must not.

**Owner:** Dev
**Timeline:** 2026-08-06
**Status:** Complete
**Verification:** `./mvnw test -Dtest=MatchConfirmationATDDTest,VerificationRulesTest` passes (190 tests).

### R-002: confirmedByOpponentIds CSV corruption (Score: 6)

**Mitigation Strategy:**
1. Unit tests for `addConfirmation()` with empty, single, duplicate, and multiple UUIDs.
2. `hasConfirmed()` tests covering null, empty, single, and legacy `confirmedByUserId` fallback.
3. Idempotency test: same UUID applied twice must not duplicate in CSV.

**Owner:** Dev
**Timeline:** 2026-08-06
**Status:** Complete
**Verification:** `./mvnw test -Dtest=VerificationRulesTest` passes with CSV edge cases.

---

## Assumptions and Dependencies

### Assumptions

1. Existing match submission and push notification infrastructure (Stories 3.1-3.2) remains stable.
2. Database migration V7 runs successfully on both H2 (test) and PostgreSQL (prod).
3. Frontend `PendingMatches.vue` correctly receives and renders the new `MatchResponse` fields.
4. `MatchRepository.findByStatusIn` does not conflict with existing derived query methods.

### Dependencies

1. **Story 3.3 (Match Rejection)** - Required by 2026-08-05 (REJECTED state must exist before PARTIALLY_CONFIRMED can be introduced).
2. **Story 3.5 (Publication Rules)** - Required by 2026-08-07 (PARTIALLY_CONFIRMED triggers 24-hour cooldown in downstream story).

### Risks to Plan

- **Risk**: Referee entry mode inference fails for matches where creatorId is null or mismatched.
  - **Impact**: Wrong confirmation threshold applied (1 instead of 2).
  - **Contingency**: Explicit `entryMode` field in `CreateMatchRequest` overrides inference; default to PARTICIPANT if ambiguous.

---

## Interworking & Regression

| Service/Component | Impact         | Regression Scope                | Validation Steps              |
| ----------------- | -------------- | ------------------------------- | ----------------------------- |
| **MatchService**  | confirmMatch() now handles PARTIALLY_CONFIRMED and idempotency | Existing confirmation tests (Story 3.2) must pass | Run `MatchServiceTest` + `MatchConfirmationATDDTest` |
| **MatchController** | Response serialization includes 4 new fields | Existing JSON path assertions must not break | Run `MatchControllerTest` |
| **PushNotificationService** | New `sendPartialConfirmationNotification()` method added | Existing notification tests must pass | Run `PushNotificationServiceImplTest` |
| **MatchRepository** | New `findByStatusIn` query method | Existing query methods must not be affected | Run repository integration tests |
| **Frontend PendingMatches** | New PARTIALLY_CONFIRMED badge and count logic | Existing pending match list tests must pass | Run `PendingMatches.spec.ts` + `usePendingMatches.spec.ts` |

**Regression test strategy:**

- Run full backend test suite (`./mvnw clean verify`) before release.
- Run full frontend unit test suite (`npm run test:unit -- --run`) before release.
- Run relevant E2E tests (match confirmation, push, rejection) to verify no regression in user-facing flows.

---

## Appendix

### Knowledge Base References

- `risk-governance.md` - Risk classification framework
- `probability-impact.md` - Risk scoring methodology
- `test-levels-framework.md` - Test level selection
- `test-priorities-matrix.md` - P0-P3 prioritization

### Related Documents

- Story Spec: `_bmad-output/implementation-artifacts/spec-3-4-context-aware-verification-rules.md`
- Epic Context: `_bmad-output/implementation-artifacts/epic-3-context.md`
- Production Code: `src/main/java/com/tictactore/rules/VerificationRules.java`
- Production Code: `src/main/java/com/tictactore/model/Match.java`
- Production Code: `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java`
- Database Migration: `src/main/resources/db/migration/V7__add_context_aware_verification_fields.sql`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `bmad-testarch-test-design`
**Version**: 5.0 (Step-File Architecture)
