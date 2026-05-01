---
stepsCompleted:
  - step-04-coverage-plan
  - step-01-detect-mode
  - step-02-load-context
  - step-03-risk-and-testability
lastStep: 'step-04-coverage-plan'
lastSaved: '2026-05-01'
mode: 'System-Level'
---

## Step 01: Detect Mode
- **Selected Mode:** System-Level Mode
- **Reason:** The user requested both System-level and Epic-level test designs. The optimal order is to start with System-Level Mode first.
- **Prerequisites Check:** PASS. `prd.md` and `architecture.md` are available in `_bmad-output/planning-artifacts/`.

## Step 02: Load Context
- **Detected Stack:** Fullstack (Spring Boot 4.0 Backend + Vue 3 Frontend with Playwright E2E).
- **Extracted Context:** 
  - NFRs covering Performance, Security (GDPR Anonymization), Scalability, and Reliability (Flyway).
  - 8 Epics identified covering Auth, Retrospective Match Entry, Verification, Analytics, Live Mode, Social, Engagement, and Tournaments.
  - Knowledge Base Fragments identified for System-Level mode (Risk Governance, Test Quality, Test Levels Framework, ADR Quality Checklist).

## Step 03: Risk & Testability
### 1. Testability Assessment (System-Level)
**✅ Testability Assessment Summary**
- **Controllability:** High. Spring Boot 4.0 provides robust profiles and embedded H2 database capabilities for reliable state seeding. Playwright network interception (`interceptNetworkCall`) provides complete frontend mockability.
- **Observability:** High. JUnit XML reporting and Playwright traces/video retention are already configured. Backend logs can be structured for easy parsing.
- **Reliability:** High. Flyway database migrations ensure deterministic schema state. Stateless JWT authentication eliminates sticky session issues, aiding parallel test execution.

**🚨 Testability Concerns**
- **Fault Injection for Live Mode:** Real-time updates (WebSockets/Push) may require specialized mocking strategies in Playwright to simulate network drops consistently.
- **Data Anonymization Verification:** Testing GDPR anonymization requires inspecting database state post-deletion, bridging frontend actions with backend assertions.

**Architecturally Significant Requirements (ASRs)**
- **GDPR Anonymization (SEC)** - ACTIONABLE: Must verify permanent link destruction upon account deletion.
- **Stateless JWT Auth (SEC)** - ACTIONABLE: Must test token expiry, renewal, and invalidation flows.
- **Live Mode / Push API (TECH)** - ACTIONABLE: Requires deterministic testing of real-time state synchronization.
- **Optimistic UI (<50ms feedback) (PERF/UX)** - ACTIONABLE: Requires testing UI state rollback on API failures.

### 2. Risk Assessment Matrix
| ID | Risk Description | Category | Prob (1-3) | Impact (1-3) | Score | Priority | Mitigation Plan | Owner | Timeline |
|---|---|---|---|---|---|---|---|---|---|
| R-01 | GDPR Anonymization Failure (data leak or link remains) | SEC | 2 | 3 | 6 | **High** | E2E and Integration tests verifying database state post-deletion. | QA/Sec | Pre-launch |
| R-02 | Concurrent Match Submission (data corruption) | DATA | 2 | 3 | 6 | **High** | Integration tests validating optimistic locking and DB constraints. | Backend Dev | MVP Phase 1 |
| R-03 | Optimistic UI out of sync with backend on failure | TECH | 3 | 2 | 6 | **High** | E2E tests asserting UI rollback mechanisms when API returns 4xx/5xx. | Frontend Dev | MVP Phase 1 |
| R-04 | Live mode synchronization delay or failure | PERF | 2 | 2 | 4 | Med | Dedicated tests for WebSocket/Push reliability under load. | Backend Dev | Post-MVP |

### 3. Risk Findings Summary
The primary risks involve data integrity (concurrent submissions), legal compliance (GDPR anonymization), and user experience (optimistic UI failures). Mitigation relies heavily on robust E2E testing to simulate API failures and verify database states.
## Step 04: Coverage Plan & Execution Strategy
### 1. Coverage Matrix
| ID | Scenario | Priority | Test Level | Associated Risk |
|---|---|---|---|---|
| C-01 | E2E Match Submission & Optimistic UI | P0 | E2E | R-03 |
| C-02 | Concurrent Match API (Optimistic Locking) | P0 | API / Integration | R-02 |
| C-03 | Google OAuth2 Login & Profile Creation | P0 | API + E2E | - |
| C-04 | Account Deletion (GDPR Anonymization verification) | P0 | API + E2E | R-01 |
| C-05 | JWT Expiration & Renewal | P1 | API | - |
| C-06 | Data Verification (Match Rejection flow) | P1 | E2E | - |
| C-07 | Live Mode Real-time WebSockets Sync | P1 | E2E | R-04 |
| C-08 | Tournament Generation & Brackets | P1 | API | - |
| C-09 | Analytics & Statistics Calculations | P2 | Unit / Component | - |
| C-10 | Social Pools & Matchmaking | P2 | API | - |
| C-11 | Achievements & Polish | P3 | Unit | - |

### 2. Execution Strategy
- **PR Pipeline:** All functional Unit, Component, API, and E2E tests. Playwright tests parallelized (target execution < 10 mins).
- **Nightly/Weekly:** Long-running Live Mode performance testing, full tournament simulations, and security/anonymization audits.

### 3. Resource Estimates
- **P0 Scenarios:** ~30–45 hours
- **P1 Scenarios:** ~25–40 hours
- **P2 Scenarios:** ~15–30 hours
- **P3 Scenarios:** ~5–10 hours
- **Total Estimate:** ~75–125 hours

### 4. Quality Gates
- **P0 Pass Rate:** 100% required for merge.
- **P1 Pass Rate:** ≥ 95% required for merge.
- **High-Risk Mitigations:** Must be completed and verified before release.
- **Code Coverage Target:** ≥ 80% overall lines covered (adjusted for purely visual components if necessary).
