# Test Design for QA: Tic-Tac-Tore

**Purpose:** This document is the QA execution recipe for Tic-Tac-Tore. It defines test coverage, tooling, and effort estimates for the QA team. Architecture concerns and risk ownership belong in `test-design-architecture.md`; this document focuses on HOW tests are implemented and executed.

---

## Executive Summary

**Risk summary:** Three high-priority risks (R-001 GDPR, R-002 Concurrent Data, R-003 Optimistic UI) drive P0 coverage. One medium-priority risk (R-004 WebSocket lag) covered at P1.

**Coverage summary:** 11 test scenarios across 4 priority tiers. P0: 4 scenarios (E2E + API). P1: 4 scenarios. P2: 2 scenarios. P3: 1 scenario. Estimated QA effort: ~75–125 hours.

---

## Not in Scope

- Performance testing beyond basic WebSocket load benchmarks (deferred post-MVP).
- Multi-region deployment testing.
- Chaos engineering for WebSocket reconnect paths (deferred post-MVP).

---

## Dependencies & Test Blockers

### Backend/Architecture Dependencies (Pre-Implementation)

- `data-testid` attribute convention implemented across all interactive frontend components (required for Playwright selectors).
- Spring Boot `test` profile with `/test/reset` endpoint for DB teardown between scenarios.
- GDPR anonymization strategy defined and implemented (blocker for TC-P0-004).
- Optimistic locking (`@Version`) on Match entity, HTTP 409 on conflict (required for TC-P0-002).

### QA Infrastructure Setup (Pre-Implementation)

- Playwright configured with JUnit XML reporter and artifact retention (traces, screenshots).
- Test data factories for `User` and `Match` entities.
- `@seontechnologies/playwright-utils` installed and configured.

### playwright-utils Usage Example

```typescript
import { test } from '@seontechnologies/playwright-utils/api-request/fixtures';
import { expect } from '@playwright/test';

test('TC-P0-004: account deletion removes all PII', async ({ apiRequest }) => {
  // Create user, then delete
  const deleteResponse = await apiRequest.delete('/api/users/me');
  expect(deleteResponse.status()).toBe(204);

  // Verify identity no longer accessible
  const profileResponse = await apiRequest.get('/api/users/me');
  expect(profileResponse.status()).toBe(401);
});
```

---

## Risk Assessment

| Risk ID | Category | Description | Score | QA Test Coverage |
|---------|----------|-------------|-------|-----------------|
| R-001 | SEC | GDPR anonymization failure — PII remains after deletion | **6** | TC-P0-004: E2E account deletion + DB assertion via API |
| R-002 | DATA | Concurrent match submission corrupts statistics | **6** | TC-P0-002: parallel API requests assert exactly one succeeds (HTTP 409 for rest) |
| R-003 | TECH | Optimistic UI desync — UI shows rejected state | **6** | TC-P0-001: Playwright mocks HTTP 500 on submission, asserts UI reverts |
| R-004 | PERF | Live Mode WebSocket sync delay under load | **4** | TC-P1-003: WebSocket E2E with latency monitoring; nightly k6 load test |

---

## Test Coverage Plan

> **Note:** P0/P1/P2/P3 denote **priority and risk level**, NOT execution timing. When tests run is defined in Execution Strategy below.

### P0 — Critical

**Criteria:** Blocks core functionality + high risk (score ≥6) + no acceptable workaround.  
**Purpose:** Gate for any release. Must be 100% green before merge.

| Test ID | Requirement | Test Level | Risk Link | Notes |
|---------|-------------|------------|-----------|-------|
| TC-P0-001 | Match submission — optimistic UI reverts on API failure | E2E (Playwright) | R-003 | Mock HTTP 500 via network interception; assert UI shows previous state |
| TC-P0-002 | Concurrent match submission — only one succeeds | API (JUnit) | R-002 | Two parallel threads; assert HTTP 409 on loser; verify DB has single record |
| TC-P0-003 | Google OAuth2 login and profile creation | E2E (Playwright) | — | Happy path; mock OAuth provider response |
| TC-P0-004 | Account deletion and GDPR PII removal | E2E (Playwright) | R-001 | Assert 401 on subsequent profile fetch; API assertion that anonymized record has no PII |

### P1 — High

**Criteria:** Important features + medium risk (score 3–5) + common user workflows.  
**Purpose:** Required for production readiness; minor failures may be acceptable with workaround.

| Test ID | Requirement | Test Level | Risk Link | Notes |
|---------|-------------|------------|-----------|-------|
| TC-P1-001 | JWT expiration forces re-authentication | API (JUnit) | — | Issue expired token; assert 401; assert refresh succeeds |
| TC-P1-002 | Match rejection flow — data verification | API (JUnit) | R-002 | Submit match with invalid data; assert HTTP 400 + error body |
| TC-P1-003 | Live Mode WebSocket real-time score sync | E2E (Playwright) | R-004 | Two browser contexts; assert score update propagates in <1s |
| TC-P1-004 | Tournament generation and bracket creation | E2E (Playwright) | — | Create 4-player tournament; assert correct bracket structure |

### P2 — Medium

**Criteria:** Secondary features + low risk (score 1–2) + edge cases.  
**Purpose:** Quality polish; failures do not block release.

| Test ID | Requirement | Test Level | Risk Link | Notes |
|---------|-------------|------------|-----------|-------|
| TC-P2-001 | Analytics and statistics calculations | API (JUnit) | — | Assert win/loss ratios correct after series of matches |
| TC-P2-002 | Social pools and matchmaking | E2E (Playwright) | — | Create pool, add players, assert matchmaking assigns correctly |

### P3 — Low

**Criteria:** Nice-to-have + exploratory + benchmarks.  
**Purpose:** Post-release improvements only.

| Test ID | Requirement | Test Level | Risk Link | Notes |
|---------|-------------|------------|-----------|-------|
| TC-P3-001 | Achievements display and unlock | Component (Vitest) | — | Unit test achievement trigger logic; visual smoke test |

---

## Execution Strategy

**Philosophy:** Run everything in PRs if total runtime <15 min. Playwright parallelizes 100s of tests in 10–15 min. Defer only if expensive or long-running.

### Every PR — Playwright & API Tests (~10–15 min)

All P0, P1, P2 functional tests: Playwright E2E (parallelized), JUnit API integration tests, Vitest component tests.

### Nightly — Load & Performance (~30–60 min)

k6 WebSocket load test for R-004 (50 concurrent connections, assert <200ms broadcast latency). Full tournament simulation end-to-end.

### Weekly — Chaos & Long-Running (~hours)

Deep GDPR audit: scripted deletion of 100 synthetic users, SQL verification of zero residual PII. WebSocket chaos (forced disconnects, reconnect scenarios).

---

## Exit Criteria

- P0 tests: 100% passing.
- P1 tests: ≥95% passing.
- High-risk mitigations (R-001, R-002, R-003) implemented and verified.
- Code coverage: ≥80% for backend services handling match submission and user deletion.
- No open severity-1 bugs at release.

---

## QA Effort Estimate

| Priority | Scenarios | Estimate |
|----------|-----------|----------|
| P0 | 4 | ~30–45 hours |
| P1 | 4 | ~25–40 hours |
| P2 | 2 | ~15–30 hours |
| P3 | 1 | ~5–10 hours |
| **Total** | **11** | **~75–125 hours** |

Estimates include test authoring, fixture setup, and CI integration. Wide intervals reflect uncertainty in backend API stability and data factory maturity.

---

## Tooling & Access

- **E2E:** Playwright + `@seontechnologies/playwright-utils`
- **Backend integration:** JUnit 5 + AssertJ + Spring Boot Test
- **Component:** Vitest
- **Load:** k6

---

## Interworking & Regression

High interworking risk between Match Recording and Statistics modules (R-002). Any change to match submission logic requires full P0+P1 regression. Statistics calculation changes require TC-P2-001 regression.

---

## Appendix A: Code Examples & Tagging

Tag Playwright tests with `@tag('P0')`, `@tag('P1')`, etc. for selective execution. JUnit: use `@Tag("P0")` on test classes.

```typescript
// Playwright tag example
test.describe('@P0 Match submission rollback', () => {
  test('TC-P0-001: reverts UI on HTTP 500', async ({ page, apiRequest }) => {
    // intercept and force failure
    await page.route('**/api/matches', route => route.fulfill({ status: 500 }));
    // ... submit and assert rollback
  });
});
```

## Appendix B: Knowledge Base References

- `risk-governance.md`
- `probability-impact.md`
- `test-levels-framework.md`
- `test-priorities-matrix.md`
- `test-quality.md`
