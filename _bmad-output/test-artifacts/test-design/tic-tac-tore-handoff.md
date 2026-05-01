# TEA → BMAD Integration Handoff

## Purpose
This document provides structured handoff guidance from the Test Architect (TEA) to the Product/Dev Agent (BMAD) for integrating test-driven constraints into the Epic and Story creation process.

## TEA Artifacts Inventory
- **Architecture Test Design:** `_bmad-output/test-artifacts/test-design-architecture.md`
- **QA Test Design:** `_bmad-output/test-artifacts/test-design-qa.md`
- **Test Design Progress:** `_bmad-output/test-artifacts/test-design-progress.md`

## Epic-Level Integration Guidance
### Risk References
When creating Epics, explicitly reference these high risks:
- **R-01 (SEC):** GDPR Anonymization Failure. Ensure Account Deletion stories detail verification steps.
- **R-02 (DATA):** Concurrent Match Submission. Ensure Match Entry stories detail optimistic locking constraints.
- **R-03 (TECH):** Optimistic UI Desync. Ensure UI interactions define rollback states.

### Quality Gates
- Epics must mandate 100% automated coverage for P0 scenarios and >=95% for P1 before being marked as Done.

## Story-Level Integration Guidance
### P0/P1 Test Scenarios → Story Acceptance Criteria
- Story for **Account Deletion** must include AC: "Backend completely removes identity mapping and replaces with anonymized dummy record, verified by database assertion."
- Story for **Match Submission** must include AC: "If API returns 500 or 409 (Conflict), the optimistic UI state is instantly reverted to previous state."
- Story for **Live Mode** must include AC: "Websocket connection drops are gracefully caught and attempt reconnection without crashing the app."

### Data-TestId Requirements
- Every interactive element (buttons, forms, match entries) MUST have a `data-testid` attribute added during implementation to support Playwright tests.

## Risk-to-Story Mapping
| Risk ID | Description | Target Epic/Story Area |
|---------|-------------|-------------------------|
| R-01 | GDPR Anonymization Failure | Epic 1: Quick Start -> Story 1.5 Account Deletion |
| R-02 | Concurrent Match Submission | Epic 2: Retrospective Match Entry -> Story 2.4 Match Submission |
| R-03 | Optimistic UI Desync | Epic 2 & Epic 5 -> Any Match Entry / Live scoring component |

## Recommended BMAD → TEA Workflow Sequence
1. Implement Epics based on this handoff.
2. Developer Agent uses `bmad-testarch-atdd` to write failing tests.
3. Feature is implemented.
4. QA Agent executes automated suites.

## Phase Transition Quality Gates
The implementation phase can begin once Stories contain the necessary ACs mandated above.