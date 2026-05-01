# Test Design for Architecture: Tic-Tac-Tore

## Executive Summary
This document provides the Master Test Architect's evaluation of the Tic-Tac-Tore architecture from a testability and quality perspective. It identifies critical risks regarding GDPR anonymization, concurrent match submissions, and optimistic UI failures, and prescribes necessary architectural improvements to ensure the system is verifiable.

## Quick Guide
### 🚨 BLOCKERS - Team Must Decide (Can't Proceed Without)
- **Data Anonymization Mechanism:** The architecture must provide a definitive, verifiable mechanism for irreversible data anonymization (GDPR Art. 17) that does not break referential integrity for match statistics.
- **WebSocket/Push Reliability:** The architecture must define how Live Mode disruptions (e.g., connection drops) are handled and recovered, as this affects how E2E tests mock network volatility.

### ⚠️ HIGH PRIORITY - Team Should Validate (We Provide Recommendation, You Approve)
- **Optimistic UI Rollbacks:** Validate the approach for rolling back UI state when a match submission fails or is rejected by the backend.
- **Concurrent DB Updates:** Ensure the database architecture (Flyway/PostgreSQL) uses strict optimistic locking or constraints for match entries to avoid corrupted stats.

### 📋 INFO ONLY - Solutions Provided (Review, No Decisions Needed)
- E2E Testing strategy will rely heavily on Playwright's `interceptNetworkCall` utility to simulate edge cases and network delays for live mode without needing extensive backend fault injection.

## For Architects and Devs - Open Topics 👷

### Risk Assessment
#### High-Priority Risks (Score ≥6) - IMMEDIATE ATTENTION
- **R-01: GDPR Anonymization Failure** (Category: SEC, Score: 6) - Risk of data leak or remaining links post-deletion.
- **R-02: Concurrent Match Submission** (Category: DATA, Score: 6) - Risk of corrupted statistics during simultaneous scoring.
- **R-03: Optimistic UI Desync** (Category: TECH, Score: 6) - Risk of UI showing incorrect match state upon API failure.

#### Medium-Priority Risks (Score 3-5)
- **R-04: Live Mode Sync Delay** (Category: PERF, Score: 4) - Potential sync issues under high load in real-time scoring.

#### Low-Priority Risks (Score 1-2)
- N/A

### Testability Concerns and Architectural Gaps
#### 1. Blockers to Fast Feedback (WHAT WE NEED FROM ARCHITECTURE)
- Clear API endpoints for test-state teardown/setup without restarting the backend container.
- Identifiable locators (`data-testid`) standardized across the entire frontend.

#### 2. Architectural Improvements Needed (WHAT SHOULD BE CHANGED)
- Implement a dedicated testing profile in Spring Boot that exposes database seeding/wiping endpoints securely.

### Testability Assessment Summary
#### What Works Well
- Controllability is strong due to embedded H2 for dev/test and Spring profiles.
- Observability is well-supported through Playwright traces and JUnit reporting.
- Reliability is ensured by Flyway migrations and stateless JWT.

#### Accepted Trade-offs (No Action Required)
- Deferring complex chaos testing of WebSockets until post-MVP.

### Risk Mitigation Plans (High-Priority Risks ≥6)
- **R-01: GDPR Anonymization Failure (Score: 6) - CRITICAL**
  - *Mitigation:* API + E2E tests validating user deletion flow and checking database constraints.
- **R-02: Concurrent Match Submission (Score: 6) - CRITICAL**
  - *Mitigation:* Backend Integration tests testing concurrent optimistic locking logic.
- **R-03: Optimistic UI Desync (Score: 6) - HIGH**
  - *Mitigation:* Playwright tests mocking API failures (HTTP 500) and asserting UI rollback.

### Assumptions and Dependencies
#### Assumptions
- Spring Boot test profiles will accurately reflect production DB schemas.
- Playwright can run against a fully isolated local environment.
#### Dependencies
- Completion of Playwright network interception utilities (`tea_use_playwright_utils`).
#### Risks to Plan
- Delays in configuring CI/CD might push E2E execution to developers' local machines, reducing test frequency.