---
stepsCompleted:
  - step-01-detect-mode
  - step-02-load-context
  - step-03-risk-and-testability
  - step-04-coverage-plan
lastStep: 'step-04-coverage-plan'
lastSaved: '2026-05-01'
---

# Test Design: Epic 1 - Quick Start (Auth & Basic Profile)

**Date:** 2026-05-01
**Author:** Pavel
**Status:** Draft

---

## Executive Summary

**Scope:** Epic-level test design for Epic 1 (Authentication and User Profile).

**Risk Summary:**
- Total risks identified: 4
- High-priority risks (≥6): 2 (OAuth2 Stability, GDPR Anonymization)
- Critical categories: SEC, TECH

**Coverage Summary:**
- P0 scenarios: 3 (~10-15 hours)
- P1 scenarios: 3 (~8-12 hours)
- P2/P3 scenarios: 2 (~4-6 hours)
- **Total effort**: ~22-33 hours (~3-5 days)

---

## Not in Scope

| Item | Reasoning | Mitigation |
|------|-----------|------------|
| Google OAuth2 Sandbox Management | Handled by infra/ops | Use service account/test credentials |
| Legacy profile migration | No legacy profiles exist (Brownfield refactor but user data is new) | N/A |

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---------|----------|-------------|-------------|--------|-------|------------|-------|----------|
| R-E1-01 | TECH | Google OAuth2 callback failure / timeout | 2 | 3 | 6 | Mocked responses in E2E to test error handling. | Dev | Sprint 1 |
| R-E1-02 | SEC | GDPR Anonymization Leak (metadata persists) | 2 | 3 | 6 | Automated DB scrubbing checks post-deletion. | QA/Sec | Sprint 1 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
|---------|----------|-------------|-------------|--------|-------|------------|-------|
| R-E1-03 | OPS | Localization bundle missing keys | 2 | 2 | 4 | Static analysis of i18n JSON files. | Dev |
| R-E1-04 | TECH | Large avatar uploads exceeding limits | 2 | 2 | 4 | API-level validation of file size and type. | Dev |

---

## Entry Criteria
- [ ] Requirements and assumptions agreed upon by QA, Dev, PM
- [ ] Test environment provisioned (including OAuth2 Redirect URI config)
- [ ] Test data factories for User entity ready
- [ ] Feature deployed to test environment

## Exit Criteria
- [ ] 100% P0 tests passing
- [ ] 100% P1 tests passing (due to legal/auth criticality)
- [ ] No open high-priority bugs
- [ ] GDPR verification script passed

---

## Test Coverage Plan

### P0 (Critical) - Run on every commit
**Criteria**: Blocks core journey + High risk (≥6)

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|-------------|------------|-----------|------------|-------|-------|
| Google OAuth2 Login | E2E / API | R-E1-01 | 3 | QA | Verify token issue and session creation |
| Auto-profile creation | API | - | 2 | DEV | Verify email-prefix logic |
| Account Deletion | API / Int | R-E1-02 | 4 | QA | Assert DB record is scrubbed |

**Total P0**: 9 tests, ~10-15 hours

### P1 (High) - Run on PR to main
**Criteria**: Important features + Medium risk (3-4)

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|-------------|------------|-----------|------------|-------|-------|
| Profile Update | E2E | R-E1-04 | 3 | QA | Nickname & Avatar change persistence |
| Localization EN/DE | Component | R-E1-03 | 5 | DEV | Text content presence check |
| OAuth Error Handling | E2E (Mock) | R-E1-01 | 4 | QA | Access Denied, Timeout scenarios |

**Total P1**: 12 tests, ~8-12 hours

### P2 (Medium) - Run nightly/weekly
**Criteria**: Secondary features + Low risk (1-2)

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
|-------------|------------|-----------|------------|-------|-------|
| Onboarding Tutorial | E2E | - | 2 | QA | Visual flow completion |
| Avatar Constraints | API | R-E1-04 | 6 | DEV | Size limits, MIME types |

**Total P2**: 8 tests, ~4-6 hours

---

## Resource Estimates
### Test Development Effort

| Priority | Count | Hours/Test | Total Hours | Notes |
|----------|-------|------------|-------------|-------|
| P0 | 9 | 1.5 | ~13.5 | Security/Auth setup complexity |
| P1 | 12 | 0.8 | ~9.6 | UI and Mock heavy |
| P2 | 8 | 0.6 | ~4.8 | Standard validations |
| **Total** | **29** | - | **~28 hours** | |

---

## Quality Gates
- **P0 Pass Rate:** 100%
- **P1 Pass Rate:** 100%
- **Anonymization Verified:** Mandatory for Story 1.5.
- **Data-TestId coverage:** 100% for interactive elements in Epic 1.