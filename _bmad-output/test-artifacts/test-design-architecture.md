# Test Design for Architecture: Tic-Tac-Tore

**Purpose:** This document evaluates the Tic-Tac-Tore system architecture from a testability and quality perspective. It serves as a contract between the Test Architect and the Architecture/Backend teams — defining what architectural decisions must be made, what gaps must be closed, and what risks require mitigation before QA can reliably verify the system.

---

## Executive Summary

This document covers the Tic-Tac-Tore system: a real-time table tennis score tracking application with Google OAuth2 authentication, match recording, live mode scoring, tournament management, and GDPR-compliant account deletion. Four risks identified: three high-priority (score ≥6) requiring immediate action, one medium-priority. Core architectural concerns: GDPR anonymization correctness, concurrent match data integrity, and optimistic UI state recovery.

---

## Quick Guide

### 🚨 BLOCKERS — Team Must Decide (Cannot Proceed Without)

- **Data Anonymization Mechanism:** Architecture must provide a definitive, verifiable mechanism for irreversible data anonymization (GDPR Art. 17) that does not break referential integrity for match statistics. Blocker for R-001 mitigation and E2E test design.
- **WebSocket/Push Reliability:** Architecture must define how Live Mode disruptions (connection drops, reconnects) are handled and recovered. Required before E2E tests can model network volatility scenarios.

### ⚠️ HIGH PRIORITY — Team Should Validate (Recommendation Provided, Approval Needed)

- **Optimistic UI Rollbacks:** Validate the rollback approach when a match submission fails or is rejected by the backend (HTTP 500 / 409). Must be deterministic and testable.
- **Concurrent DB Updates:** Confirm that Flyway/PostgreSQL schema uses strict optimistic locking or unique constraints on match entries to prevent corrupted statistics under concurrent load.

### 📋 INFO ONLY — Solutions Provided (No Decisions Needed)

- E2E testing strategy will rely on Playwright's network interception utilities to simulate edge cases and network delays for Live Mode without requiring backend fault injection infrastructure.

---

## Risk Assessment

### High-Priority Risks (Score ≥6) — Immediate Action Required

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---------|----------|-------------|-------------|--------|-------|------------|-------|----------|
| R-001 | SEC | GDPR anonymization failure: user deletion leaves recoverable identity links in match stats or audit logs | 2 | 3 | **6** | Implement irreversible anonymization endpoint with referential integrity constraints; audit all FK relationships pre-delete | Backend Lead | Before Epic 1 Story 1.5 |
| R-002 | DATA | Concurrent match submission: simultaneous scoring requests produce corrupted or duplicate statistics | 2 | 3 | **6** | Add optimistic locking version column to Match entity; enforce unique constraint on (match_id, scorer_id, timestamp) in PostgreSQL | Backend Lead | Before Epic 2 Story 2.4 |
| R-003 | TECH | Optimistic UI desync: UI displays committed match state that was subsequently rejected by the backend | 3 | 2 | **6** | Define frontend rollback contract: on HTTP 4xx/5xx, revert to last confirmed server state; expose `onRollback` event in match store | Frontend Lead | Before Epic 2 |

### Medium-Priority Risks (Score 3–5)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
|---------|----------|-------------|-------------|--------|-------|------------|-------|----------|
| R-004 | PERF | Live Mode sync delay: WebSocket broadcast latency under concurrent user load causes visible score lag | 2 | 2 | **4** | Benchmark WebSocket throughput at 50 concurrent connections; set SLO of <200ms broadcast latency | Backend/DevOps | Pre-release load test |

### Low-Priority Risks

None identified at this stage.

---

## Testability Concerns and Architectural Gaps

### 🚨 Actionable Concerns

#### Blockers to Fast Feedback (What Architecture Must Provide)

| Concern | Required Action | Owner | Timeline | Impact if Missed |
|---------|----------------|-------|----------|------------------|
| No test-state teardown API | Expose `/test/reset` endpoint behind Spring `test` profile; allows Playwright to wipe DB state between scenarios without container restart | Backend Lead | Sprint 1 | E2E tests will be stateful and flaky |
| Missing `data-testid` convention | Define and enforce `data-testid` attribute standard across all interactive frontend components | Frontend Lead | Sprint 1 | Playwright selectors will couple to CSS/text, breaking on refactors |

#### Architectural Improvements Needed (What Must Be Changed)

| Concern | Required Change | Owner | Timeline |
|---------|----------------|-------|----------|
| No dedicated test Spring profile | Add `application-test.properties` exposing seeding/wiping endpoints securely (disabled in prod profile) | Backend Lead | Sprint 1 |
| Anonymization not defined | Define anonymization strategy (replace PII fields with deterministic hash, preserve FK references) before deletion stories are implemented | Architect | Before Story 1.5 |

### Testability Assessment Summary

#### What Works Well

- **Controllability:** H2 embedded DB in dev/test profile provides fast, isolated test execution.
- **Observability:** Playwright trace capture and JUnit XML reporting already supported by framework.
- **Reliability:** Flyway migrations ensure deterministic schema state; stateless JWT eliminates session coupling.

#### Accepted Trade-offs (No Action Required)

- Chaos testing of WebSocket reconnect paths deferred to post-MVP. Covered by R-004 mitigation at load test stage.

---

## Risk Mitigation Plans (High-Priority Risks ≥6)

### R-001: GDPR Anonymization Failure (Score: 6 — CRITICAL)

**Strategy:**
1. Audit all tables with FK references to `users.id` — document which must anonymize vs. cascade-delete.
2. Implement `AnonymizationService` that replaces PII fields with SHA-256(user_id + salt); preserves `user_id` FK as anonymized placeholder.
3. Add database trigger or application-layer guard preventing re-linkage of anonymized records.
4. Write integration test asserting zero remaining PII fields post-deletion (Backend-owned).

**Owner:** Backend Lead  
**Timeline:** Completed before Story 1.5 is marked ready-for-dev  
**Status:** Open  
**Verification:** Integration test suite passes; manual SQL audit shows no recoverable PII for deleted user

---

### R-002: Concurrent Match Submission (Score: 6 — CRITICAL)

**Strategy:**
1. Add `version` column (optimistic locking) to `Match` entity via Flyway migration.
2. Add unique constraint: `UNIQUE(match_id, round_number)` in PostgreSQL.
3. Configure `@Version` annotation in JPA entity; ensure `OptimisticLockException` is handled and returns HTTP 409.
4. Backend integration test: two threads submit same match simultaneously, assert only one succeeds.

**Owner:** Backend Lead  
**Timeline:** Completed before Story 2.4 is marked ready-for-dev  
**Status:** Open  
**Verification:** Concurrent load test (10 parallel requests) produces exactly 1 successful submission; all others return 409

---

### R-003: Optimistic UI Desync (Score: 6 — HIGH)

**Strategy:**
1. Define rollback contract in frontend match store: on any non-2xx response, revert `matchState` to `lastConfirmedState` snapshot.
2. Expose `onSubmitRollback` event/callback for components to re-render rollback indicator.
3. Document the state machine in ADR (optimistic → pending → confirmed | rolled-back).

**Owner:** Frontend Lead  
**Timeline:** Completed before Epic 2 implementation begins  
**Status:** Open  
**Verification:** Code review of state machine; Playwright test (QA-owned) confirms UI reverts correctly on mocked HTTP 500

---

## Assumptions and Dependencies

### Architectural Assumptions

1. Spring Boot test profile will mirror production DB schema (via Flyway) with identical constraints.
2. Playwright can execute against a fully isolated local environment (no external services required).
3. WebSocket implementation uses STOMP over SockJS — standard enough for Playwright's network interception.

### Dependencies

| Dependency | Required By | Target Date |
|------------|-------------|-------------|
| `playwright-utils` network interception library | QA E2E test authoring | Sprint 1 |
| Spring `test` profile with seeding endpoint | E2E test isolation | Sprint 1 |
| Anonymization strategy decision (BLOCKER) | Story 1.5 ready-for-dev | Before Epic 1 Story 1.5 |

### Risks to Plan

- CI/CD pipeline delays may force E2E tests to developer local machines, reducing automated test frequency. Contingency: gate merges on local E2E run output until CI is stable.
