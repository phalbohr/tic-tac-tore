---
workflowStatus: 'in-progress'
totalSteps: 5
stepsCompleted:
  - 'step-01-detect-mode'
  - 'step-02-load-context'
  - 'step-03-risk-and-testability'
  - 'step-04-coverage-plan'
  - 'step-05-generate-output'
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-07-27'
inputDocuments:
  - '_bmad-output/implementation-artifacts/sprint-status.yaml'
  - '_bmad-output/test-artifacts/test-design/test-design-epic-2.3.md'
  - '_bmad-output/test-artifacts/automation-summary.md'
  - '_bmad-output/test-artifacts/atdd-checklist-2-4-match-submission-with-undo-window.md'
---

# Test Design: Test Automation Expansion

**Date:** 2026-07-27
**Author:** Pavel
**Status:** Draft

---

## Executive Summary

**Scope:** Project-wide test automation expansion covering backend (Spring Boot), frontend (Vue 3 / Vite / Pinia), and E2E (Playwright) layers.

**Risk Summary:**

- Total risks identified: 10
- High-priority risks (≥6): 3
- Critical categories: DATA, TECH, BUS

**Coverage Summary:**

- P0 scenarios: 48 (~72 hours)
- P1 scenarios: 38 (~38 hours)
- P2 scenarios: 22 (~11 hours)
- P3 scenarios: 8 (~2 hours)
- **Total effort**: ~123 hours (~2.5 weeks)

---

## Not in Scope

| Item | Reasoning | Mitigation |
|---|---|---|
| **Epic 1 stories** (1.1–1.7) | Already have test design completed (test-design-epic-2.3.md covers epic 2.3) | Reference existing test design for epic 1 |
| **Frontend RTL/CSS tests** | Already covered by `rtl-css.spec.ts` | No additional work needed |
| **i18n locale tests** | Already covered by `locale-parity.spec.ts` and `i18n.spec.ts` | No additional work needed |
| **Auth flow E2E** | Already covered by `scenarios/login.spec.ts` and `scenarios/logout.spec.ts` | No additional work needed |

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---|---|---|---|---|---|---|---|---|
| R-001 | DATA | MatchRepository has no tests — data access layer corruption risk | 2 | 3 | 6 | Add integration tests for MatchRepository CRUD operations | QA/Dev | 2026-07-28 |
| R-002 | TECH | RuleConfigurationController untested — API contract violations possible | 2 | 3 | 6 | Add controller unit tests for all endpoints | Dev | 2026-07-28 |
| R-006 | TECH | NotificationControllerATDDTest is @Disabled — notification flow untested | 3 | 2 | 6 | Activate and complete the ATDD red-green cycle | Dev | 2026-07-28 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
|---|---|---|---|---|---|---|---|
| R-003 | BUS | UserMatchController untested — user-match association logic unverified | 2 | 2 | 4 | Add controller tests for match listing and user association | Dev |
| R-004 | TECH | ProfileApi untested — profile update/retrieval endpoints unverified | 2 | 2 | 4 | Add controller tests for profile CRUD | Dev |
| R-005 | DATA | PushSubscriptionRepository untested — notification subscription data integrity risk | 2 | 2 | 4 | Add repository integration tests | QA/Dev |
| R-007 | BUS | ScoreStepper component untested — core UX component with boundary logic | 2 | 2 | 4 | Add component unit tests for boundary conditions | QA |
| R-008 | UX | UndoToast component untested — critical user feedback mechanism | 2 | 2 | 4 | Add component tests for toast display/dismissal | QA |
| R-009 | TECH | useRuleConfigStore untested — frontend rule config state management | 2 | 2 | 4 | Add Vitest unit tests for store mutations and getters | Dev |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
|---|---|---|---|---|---|---|
| R-010 | PERF | liveMatch store untested — real-time state updates may have race conditions | 1 | 3 | 3 | Add tests for concurrent update scenarios | QA |

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
|---|---|---|---|---|
| Performance | E2E match submission < 10s | R-010 | Playwright timing assertions | Lighthouse/Performance Tab trace |
| Reliability | Notification delivery success rate > 99% | R-006 | Integration test with mock push service | Test logs, CI reports |
| Maintainability | No violations of 500-line rule | - | CI Linting | Lint report |
| Security | JWT token revocation works correctly | R-001 (indirect) | Unit tests for RedisTokenRevocationService | Test coverage report |

**Unknown thresholds:** N/A

---

## Entry Criteria

- [ ] Requirements and assumptions agreed upon by QA, Dev, PM
- [ ] Test environment provisioned and accessible
- [ ] Test data available or factories ready
- [ ] Feature deployed to test environment
- [ ] ATDD scaffolds activated (remove @Disabled)

## Exit Criteria

- [ ] All P0 tests passing
- [ ] All P1 tests passing (or failures triaged)
- [ ] No open high-priority / high-severity bugs
- [ ] Test coverage agreed as sufficient
- [ ] All high-risk mitigations complete or waived

---

## Test Coverage Plan

### P0 (Critical) — Blocks core functionality + High risk (≥6) + No workaround

#### Backend Controllers

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| RuleConfigurationController CRUD endpoints | Unit + Integration | R-002 | 8 | Dev | All CRUD operations, error cases, validation |
| ProfileApi endpoints (GET/PUT/DELETE) | Unit + Integration | R-004 | 6 | Dev | Profile retrieval, update, deletion, not-found |
| NotificationController endpoints | Unit + Integration | R-006 | 4 | Dev | Activate ATDD scaffold, test notification dispatch |
| MatchController match creation | Integration | R-001 | 5 | Dev | Expand existing MatchControllerTest |

#### Backend Services

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| RuleConfigurationService CRUD operations | Unit | R-002 | 6 | Dev | No existing tests |
| MatchService match lifecycle | Unit | R-001 | 8 | Dev | Expand existing MatchServiceTest |

#### Backend Repositories

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| MatchRepository CRUD | Integration | R-001 | 5 | QA | No existing tests |
| RuleConfigurationRepository CRUD | Integration | R-002 | 4 | QA | No existing tests |

#### Frontend Components

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| ScoreStepper boundary conditions | Component | R-007 | 4 | QA | Min/max boundaries, step increments |
| UndoToast display/dismissal | Component | R-008 | 3 | QA | Timer, click-to-dismiss, auto-dismiss |

#### ATDD Activation

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| Activate NotificationControllerATDDTest | Integration | R-006 | 4 | Dev | Remove @Disabled, implement red-green |
| Activate MatchServiceATDDTest | Unit | R-001 | 5 | Dev | Remove @Disabled, implement red-green |
| Activate MatchControllerATDDTest | Integration | R-001 | 5 | Dev | Remove @Disabled, implement red-green |

**Total P0**: 48 tests, ~72 hours

### P1 (High) — Important features + Medium risk (3-4) + Common workflows

#### Backend Controllers

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| UserMatchController endpoints | Unit + Integration | R-003 | 5 | Dev | Match listing, filtering, pagination |
| AuthController login flow | Unit | - | 3 | Dev | Expand existing AuthControllerTest |

#### Backend Services

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| UserService user operations | Unit | - | 5 | Dev | Expand existing UserServiceTest |
| PushNotificationService dispatch | Unit | - | 4 | Dev | Activate ATDD scaffold |
| RedisTokenRevocationService token revocation | Unit | - | 3 | Dev | Expand existing test |

#### Backend Repositories

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| NotificationLogRepository CRUD | Integration | - | 3 | QA | No existing tests |
| PushSubscriptionRepository CRUD | Integration | - | 3 | QA | No existing tests |

#### Frontend Components

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| NewMatchFlow full journey | Component | - | 5 | QA | Multi-step form, validation |
| ErrorToast error display | Component | - | 2 | QA | Error state rendering |
| LiveMatch real-time updates | Component | R-010 | 4 | QA | Concurrent update handling |
| useRuleConfigStore mutations/getters | Unit | R-009 | 4 | Dev | No existing tests |

#### E2E Tests

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| New match creation E2E | E2E | - | 3 | QA | Expand existing new-match-creation.spec.ts |
| Match confirmation push E2E | E2E | - | 2 | QA | Expand existing match-confirmation-push.spec.ts |

**Total P1**: 38 tests, ~38 hours

### P2 (Medium) — Secondary features + Low risk (1-2) + Edge cases

#### Frontend Components

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| PositionSwapDialog interaction | Component | - | 3 | QA | Swap logic, cancellation |
| LiveQuadrant quadrant rendering | Component | - | 3 | QA | Score display, position rendering |
| RuleSystemSelection component | Component | - | 3 | QA | Selection logic, API integration |

#### Backend Services

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| MatchService duplicate detection | Unit | - | 3 | Dev | Expand existing MatchServiceDuplicateDetectionATDDTest |

**Total P2**: 22 tests, ~11 hours

### P3 (Low) — Nice-to-have + Exploratory + Performance benchmarks

#### Frontend Components

| Requirement | Test Level | Test Count | Owner | Notes |
|---|---|---|---|---|
| TutorialCarousel navigation | Component | 2 | QA | Slide navigation, skip |

#### Frontend Stores

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|---|---|---|---|---|---|
| liveStore concurrent updates | Unit | R-010 | 3 | QA | Race condition scenarios |

#### E2E Tests

| Requirement | Test Level | Test Count | Owner | Notes |
|---|---|---|---|---|
| Performance benchmark < 10s | E2E | 2 | QA | Manual timing |

**Total P3**: 8 tests, ~2 hours

---

## Execution Strategy

**PR / Nightly / Weekly Model:**

- **PR**: All functional tests (unit + integration + component + E2E) — target <15 min with Playwright parallelization
- **Nightly**: Full regression suite including E2E, performance benchmarks
- **Weekly**: Chaos testing, long-running integration tests, accessibility audits

**Philosophy**: Run everything in PRs if <15 min total. Defer only if expensive/long-running (perf, chaos).

---

## Resource Estimates

| Priority | Count | Hours/Test | Total Hours | Notes |
|---|---|---|---|---|
| P0 | 48 | 1.5 | ~72 | Controllers, services, repositories, ATDD activation |
| P1 | 38 | 1.0 | ~38 | Secondary services, components, E2E expansion |
| P2 | 22 | 0.5 | ~11 | Display components, edge cases |
| P3 | 8 | 0.25 | ~2 | Exploratory, cosmetic |
| **Total** | **116** | **-** | **~123** | **~2.5 weeks** |

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2/P3 pass rate**: ≥90% (informational)
- **High-risk mitigations**: 100% complete or approved waivers

### Coverage Targets

- **Critical paths**: ≥80%
- **Security scenarios**: 100%
- **Business logic**: ≥70%
- **Edge cases**: ≥50%

### Non-Negotiable Requirements

- [ ] All P0 tests pass
- [ ] No high-risk (≥6) items unmitigated
- [ ] Security tests (SEC category) pass 100%
- [ ] Performance targets met (PERF category)
- [ ] Planned NFR evidence exists or `nfr-assess` has documented CONCERNS/waivers

---

## Mitigation Plans

### R-001: MatchRepository Untested (Score: 6)

**Mitigation Strategy:** Add integration tests for MatchRepository CRUD operations using an in-memory H2 database or Testcontainers with PostgreSQL. Test create, read, update, delete, and query-by-status operations.
**Owner:** QA/Dev
**Timeline:** 2026-07-28
**Status:** Planned
**Verification:** Integration tests pass in CI; coverage report shows MatchRepository coverage ≥80%

### R-002: RuleConfigurationController Untested (Score: 6)

**Mitigation Strategy:** Add controller unit tests for all CRUD endpoints (GET, POST, PUT, DELETE) with mocked service layer. Test validation errors, not-found responses, and unauthorized access.
**Owner:** Dev
**Timeline:** 2026-07-28
**Status:** Planned
**Verification:** Unit tests pass; MockMvc tests cover all endpoints

### R-006: NotificationControllerATDDTest Disabled (Score: 6)

**Mitigation Strategy:** Remove @Disabled annotation, implement red-phase test scaffolds, then drive green phase by implementing the notification controller endpoints and verifying ATDD acceptance criteria.
**Owner:** Dev
**Timeline:** 2026-07-28
**Status:** Planned
**Verification:** ATDD tests transition from RED → GREEN; all acceptance criteria pass

---

## Assumptions and Dependencies

### Assumptions

1. Existing test infrastructure (JUnit 5, Mockito, Vitest, Playwright) is functional and up-to-date.
2. Test data factories and fixtures exist or can be created as part of P0 work.
3. The CI pipeline can accommodate the additional test execution time.
4. ATDD scaffolds (MatchServiceATDDTest, MatchControllerATDDTest) are compatible with the current implementation.

### Dependencies

1. **RuleConfigurationController implementation** — Required before RuleConfigurationController tests can be written. Needed by 2026-07-28.
2. **ProfileApi implementation** — Required before ProfileApi tests can be written. Needed by 2026-07-28.
3. **NotificationController implementation** — Required before NotificationControllerATDDTest can be activated. Needed by 2026-07-28.
4. **MatchRepository implementation** — Required before MatchRepository integration tests can be written. Needed by 2026-07-28.

### Risks to Plan

- **Risk**: ATDD scaffolds may not align with final implementation
  - **Impact**: Tests fail in red phase, requiring rework
  - **Contingency**: Dev to review scaffold alignment during implementation

---

## Follow-on Workflows

1. Run `*atdd` workflow for P0 test generation (separate workflow; not auto-run).
2. Run `*automate` for broader coverage once implementation exists.
3. Run `*trace` to update the traceability matrix and calculate final test coverage gates.
4. Run `*nfr-assess` when implementation evidence exists to validate NFR thresholds.

---

## Approval

**Test Design Approved By:**

- [ ] Product Manager: Pavel Date: 2026-07-27
- [ ] Tech Lead: Pavel Date: 2026-07-27
- [ ] QA Lead: Pavel Date: 2026-07-27

---

## Interworking & Regression

| Service/Component | Impact | Regression Scope |
|---|---|---|
| **MatchService** | New tests added to MatchServiceTest and MatchServiceATDDTest | Existing MatchServiceTest, MatchServiceDuplicateDetectionATDDTest |
| **MatchController** | New tests added to MatchControllerTest and MatchControllerATDDTest | Existing MatchControllerTest, MatchControllerATDDTest |
| **RuleConfigurationService** | New tests for RuleConfigurationService | None (no existing tests) |
| **RuleConfigurationController** | New tests for RuleConfigurationController | None (no existing tests) |
| **NotificationController** | ATDD scaffold activation | NotificationControllerATDDTest (currently @Disabled) |
| **ProfileApi** | New tests for ProfileApi | None (no existing tests) |
| **UserMatchController** | New tests for UserMatchController | None (no existing tests) |

---

## Appendix

### Knowledge Base References

- `risk-governance.md` - Risk classification framework
- `probability-impact.md` - Risk scoring methodology
- `test-levels-framework.md` - Test level selection
- `test-priorities-matrix.md` - P0-P3 prioritization

### Related Documents

- Epic 2 Context: `_bmad-output/implementation-artifacts/epic-2-context.md`
- Spec 2.3: `_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md`
- Spec 2.4: `_bmad-output/implementation-artifacts/2-4-match-submission-with-undo-window.md`
- Existing Test Design (Epic 2.3): `_bmad-output/test-artifacts/test-design/test-design-epic-2.3.md`
- Automation Summary (Story 2.4): `_bmad-output/test-artifacts/automation-summary.md`
- ATDD Checklist (Story 2.4): `_bmad-output/test-artifacts/atdd-checklist-2-4-match-submission-with-undo-window.md`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `bmad-testarch-test-design`
**Version**: 4.0 (BMad v6)
