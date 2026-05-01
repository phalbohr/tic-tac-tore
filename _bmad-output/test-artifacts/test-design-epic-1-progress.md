---
stepsCompleted:
  - step-04-coverage-plan
  - step-01-detect-mode
  - step-02-load-context
  - step-03-risk-and-testability
lastStep: 'step-04-coverage-plan'
lastSaved: '2026-05-01'
mode: 'Epic-Level'
epic_num: 1
epic_name: 'Quick Start (Auth & Basic Profile)'
---

## Step 02: Load Context
- **Extracted Epic Stories:**
  - 1.1 Auth via Google OAuth2
  - 1.2 Localization (EN/DE)
  - 1.3 Auto-profile generation
  - 1.4 Profile management
  - 1.5 Account deletion (Anonymization)
  - 1.6 Avatar selection
  - 1.7 Onboarding tutorial

## Step 03: Risk & Testability (Epic-Level)
### 1. Risk Assessment Matrix
| ID | Risk Description | Category | Prob (1-3) | Impact (1-3) | Score | Priority | Mitigation Plan | Owner | Timeline |
|---|---|---|---|---|---|---|---|---|---|
| R-E1-01 | Google OAuth2 callback failure / timeout | TECH | 2 | 3 | 6 | **High** | Mocked OAuth2 responses in E2E to test error handling and retry logic. | Dev | Sprint 1 |
| R-E1-02 | GDPR Anonymization Leak (metadata persists) | SEC | 2 | 3 | 6 | **High** | Automated integration tests verifying DB record scrub post-deletion. | QA/Sec | Sprint 1 |
| R-E1-03 | Localization bundle missing keys (UI breakage) | OPS | 2 | 2 | 4 | Med | Static analysis for i18n keys and unit tests for translation resolution. | Dev | Sprint 1 |
| R-E1-04 | Large avatar uploads exceeding storage/memory | TECH | 2 | 2 | 4 | Med | API tests for file size limits and client-side resize verification. | Dev | Sprint 1 |

### 2. Risk Findings Summary
The highest risks for Epic 1 are related to the stability of the Google OAuth2 handshake and the critical legal requirement for complete data anonymization upon account deletion. Mitigation focuses on robust mocking for external dependencies and deep database-level verification for deletion flows.
## Step 04: Coverage Plan & Execution Strategy (Epic-Level)
### 1. Coverage Matrix
| ID | Scenario | Priority | Test Level | Associated Risk |
|---|---|---|---|---|
| C-E1-01 | Successful Google OAuth2 Login & Token Issue | P0 | E2E / API | R-E1-01 |
| C-E1-02 | Auto-profile creation with email-prefix nickname | P0 | API / Unit | - |
| C-E1-03 | Account Deletion (Verifying irreversible scrubbing) | P0 | API / Integration | R-E1-02 |
| C-E1-04 | User Profile Edit (Nickname & Avatar update) | P1 | E2E | R-E1-04 |
| C-E1-05 | Localization Switch (EN <-> DE) & Persistence | P1 | Component | R-E1-03 |
| C-E1-06 | OAuth2 Callback Error Handling (Access Denied / Timeout) | P1 | E2E (Mocked) | R-E1-01 |
| C-E1-07 | Onboarding Tutorial Completion Flow | P2 | E2E | - |
| C-E1-08 | Avatar Upload Constraints (Type/Size validation) | P2 | API | R-E1-04 |

### 2. Execution Strategy
- **PR Pipeline:** All scenarios (C-E1-01 to C-E1-08). Execution target: < 5 minutes for Epic 1 specific suite.
- **Nightly:** Extended security scans for account deletion edge cases.

### 3. Resource Estimates
- **P0 Scenarios:** ~10–15 hours
- **P1 Scenarios:** ~8–12 hours
- **P2/P3 Scenarios:** ~4–6 hours
- **Total Estimate:** ~22–33 hours

### 4. Quality Gates
- **P0 Pass Rate:** 100%
- **P1 Pass Rate:** 100% (Given critical nature of Auth and GDPR)
- **High-Risk Mitigations:** Verified (OAuth2 mocking and Deletion scrub).
