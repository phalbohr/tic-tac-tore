# Test Design: Epic 2.3 - Score Entry & Automatic Completion

**Date:** 2026-07-17
**Author:** Pavel
**Status:** Approved

---

## Executive Summary

**Scope:** Epic-Level test design for Epic 2.3 (Story 2.3)

**Risk Summary:**

- Total risks identified: 5
- High-priority risks (≥6): 1
- Critical categories: DATA, TECH

**Coverage Summary:**

- P0 scenarios: 4 (2.0 hours)
- P1 scenarios: 4 (2.0 hours)
- P2/P3 scenarios: 2 (1.0 hours)
- **Total effort**: 5.0 hours (~0.5 days)

---

## Not in Scope

| Item       | Reasoning      | Mitigation            |
| ---------- | -------------- | --------------------- |
| **Undo Match Entry** | Covered in Story 2.4 | Defer testing of the undo window until Story 2.4 |
| **Positional Swapping** | Covered in Story 2.5 | Positional logic will be tested alongside Story 2.5 |

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description   | Probability | Impact | Score | Mitigation   | Owner   | Timeline |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------------ | ------- | -------- |
| R-001   | DATA      | Score state in matchDraftStore might corrupt match scores (e.g. going negative or over limits) causing invalid match records. | 2           | 3      | 6     | Add rigorous unit tests for the increment/decrement bounds and auto-completion conditions. | QA/Dev | 2026-07-17   |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description   | Probability | Impact | Score | Mitigation   | Owner   |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------------ | ------- |
| R-002   | TECH     | API failure when loading RuleConfig might silently leave the system in a broken state. | 2           | 2      | 4     | Add fallback to standard rules and error boundaries. | Dev |
| R-003   | BUS      | Missing visual distinction between +5 and +1/−1 steppers may lead to user frustration. | 2           | 2      | 4     | Component testing to ensure size classes are applied correctly. | QA |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description   | Probability | Impact | Score | Action  |
| ------- | -------- | ------------- | ----------- | ------ | ----- | ------- |
| R-004   | PERF      | Rendering logic or watchers might block the main thread causing end-to-end entry to take longer than 10s. | 1           | 2      | 2     | Monitor |
| R-005   | UX      | 1px borders used inadvertently violating "No-Line" rule (UX-DR3). | 2           | 1      | 2     | Monitor |

### Risk Category Legend

- **TECH**: Technical/Architecture (flaws, integration, scalability)
- **SEC**: Security (access controls, auth, data exposure)
- **PERF**: Performance (SLA violations, degradation, resource limits)
- **DATA**: Data Integrity (loss, corruption, inconsistency)
- **BUS**: Business Impact (UX harm, logic errors, revenue)
- **OPS**: Operations (deployment, config, monitoring)
- **UX**: User Experience (design rules, interaction)

---

## NFR Planning

**Purpose:** Capture epic-specific NFR thresholds, planned validation, and evidence expected for later `nfr-assess`. This is not a final evidence audit.

| NFR Category    | Requirement / Threshold | Risk Link | Planned Validation                         | Evidence Needed                  |
| --------------- | ----------------------- | --------- | ------------------------------------------ | -------------------------------- |
| Performance     | End-to-end match entry must take less than 10 seconds. | R-004    | Manual profiling during exploratory testing | Lighthouse/Performance Tab trace |
| Maintainability | No violations of the 500-line rule | - | CI Linting | Lint report |

**Unknown thresholds:** N/A

---

## Entry Criteria

- [x] Requirements and assumptions agreed upon by QA, Dev, PM
- [x] Test environment provisioned and accessible
- [x] Feature deployed to test environment

## Exit Criteria

- [ ] All P0 tests passing
- [ ] All P1 tests passing (or failures triaged)
- [ ] No open high-priority / high-severity bugs
- [ ] Test coverage agreed as sufficient

---

## Test Coverage Plan

### P0 (Critical) - Run on every commit

**Criteria**: Blocks core journey + High risk (≥6) + No workaround

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| Stepper does not decrement below 0 | Unit | R-001 | 1 | DEV | Boundary check |
| Stepper +5 caps out exactly at score_limit | Unit | R-001 | 1 | DEV | Boundary check |
| Game automatically completes when score reaches limit | Unit | R-001 | 1 | DEV | Core state transition |
| Match automatically completes when winsNeeded is met | Unit | R-001 | 1 | DEV | Core state transition |

**Total P0**: 4 tests, 2.0 hours

### P1 (High) - Run on PR to main

**Criteria**: Important features + Medium risk (3-4) + Common workflows

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| Score entry view correctly hides +5 stepper if score_limit < 5 | Component | R-003 | 1 | DEV | Visual logic |
| Proper fallback if player is not in frequentOpponents | Component | - | 1 | DEV | Fallback logic |
| API loading errors fallback to standard rules | Unit | R-002 | 1 | DEV | Error handling |
| View correctly applies no-line rule styling | Component | R-005 | 1 | DEV | UX Rules |

**Total P1**: 4 tests, 2.0 hours

### P2 (Medium) - Run nightly/weekly

**Criteria**: Secondary features + Low risk (1-2) + Edge cases

| Requirement   | Test Level | Risk Link | Test Count | Owner | Notes   |
| ------------- | ---------- | --------- | ---------- | ----- | ------- |
| Display past games context above current game | Component | - | 1 | DEV | Display logic |
| Format team names properly for 2v2 | Component | - | 1 | DEV | Formatting logic |

**Total P2**: 2 tests, 1.0 hours

### P3 (Low) - Run on-demand

**Criteria**: Nice-to-have + Exploratory + Performance benchmarks

| Requirement   | Test Level | Test Count | Owner | Notes   |
| ------------- | ---------- | ---------- | ----- | ------- |
| End-to-end performance < 10 seconds | Exploratory | 1 | QA | Manual timing |

**Total P3**: 1 tests, 0.5 hours

---

## Execution Order

### Smoke Tests (<5 min)

**Purpose**: Fast feedback, catch build-breaking issues

- [ ] Run `npm run test:unit frontend/src/features/match/stores/matchDraftStore.spec.ts`

**Total**: 10 scenarios

### P0 Tests (<10 min)

**Purpose**: Critical path validation

- [ ] Stepper does not decrement below 0 (Unit)
- [ ] Stepper +5 caps out exactly at score_limit (Unit)
- [ ] Game automatically completes when score reaches limit (Unit)
- [ ] Match automatically completes when winsNeeded is met (Unit)

**Total**: 4 scenarios

### P1 Tests (<30 min)

**Purpose**: Important feature coverage

- [ ] Hides +5 stepper if score_limit < 5 (Component)
- [ ] Fallback if player not in frequentOpponents (Component)
- [ ] API error fallback (Unit)
- [ ] No-line rule styling (Component)

**Total**: 4 scenarios

---

## Resource Estimates

### Test Development Effort

| Priority  | Count             | Hours/Test | Total Hours       | Notes                   |
| --------- | ----------------- | ---------- | ----------------- | ----------------------- |
| P0        | 4                 | 0.5        | 2.0               | Unit tests for boundaries |
| P1        | 4                 | 0.5        | 2.0               | Component tests         |
| P2        | 2                 | 0.5        | 1.0               | Secondary display logic |
| P3        | 1                 | 0.5        | 0.5               | Exploratory             |
| **Total** | **11**            | **-**      | **5.5**           | **~0.5 days**           |

### Prerequisites

**Tooling:**

- Vitest for Unit/Component tests

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2/P3 pass rate**: ≥90% (informational)
- **High-risk mitigations**: 100% complete or approved waivers

### Coverage Targets

- **Critical paths**: ≥80%
- **Business logic**: ≥70%

### Non-Negotiable Requirements

- [ ] All P0 tests pass
- [ ] No high-risk (≥6) items unmitigated
- [ ] Performance targets met (PERF category)

---

## Mitigation Plans

### R-001: Data Corruption on Boundaries (Score: 6)

**Mitigation Strategy:** Extensive parameterised boundary checks in `matchDraftStore.spec.ts` that prove that negative decrementing is impossible, and score caps correctly at `scoreLimit`.
**Owner:** Dev
**Timeline:** 2026-07-17
**Status:** Complete
**Verification:** Vitest test suite passes.

---

## Assumptions and Dependencies

### Dependencies

1. Story 2.1 Rule Systems - Rules API endpoint must exist.
2. Story 2.2 Match Type Selection - State must correctly initialize before Score Entry.

---

## Approval

**Test Design Approved By:**

- [x] Product Manager: Pavel Date: 2026-07-17
- [x] Tech Lead: Pavel Date: 2026-07-17
- [x] QA Lead: Pavel Date: 2026-07-17

---

## Appendix

### Related Documents

- Epic: _bmad-output/implementation-artifacts/epic-2-context.md
- Spec: _bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `bmad-testarch-test-design`
**Version**: 4.0 (BMad v6)
