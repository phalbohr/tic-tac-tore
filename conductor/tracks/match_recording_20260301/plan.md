# Plan: 2v2 Match Recording & Confirmation Workflow

## Phase 1: Backend Scaffolding & Data Persistence [checkpoint: 7df8ed7]
- [x] Task: Implement `Match` and `Game` entities with `MatchStatus` c009da2
    - [x] Write unit tests for Match persistence
    - [x] Implement entities and Repository
- [x] Task: Implement Match Creation API 0537e65
    - [x] Write integration tests for `POST /api/v1/matches` (validation and initial PENDING state)
    - [x] Implement Controller and Service for match submission
- [x] Task: Conductor - User Manual Verification 'Backend Scaffolding & Data Persistence' (Protocol in workflow.md)

## Phase 2: Confirmation Workflow Logic [checkpoint: 1a25915]
- [x] Task: Implement Match Approval API f132440
    - [x] Write tests for `PUT /api/v1/matches/{id}/approve` (verify only opponent can approve)
    - [x] Implement approval logic and transition to `CONFIRMED`
    - [x] Implement `PUT /api/v1/matches/{id}/approve` endpoint in `MatchController`
- [x] Task: Implement Match Rejection API 9208c94
    - [x] Write tests for `PUT /api/v1/matches/{id}/reject` (verify return to `DRAFT`)
    - [x] Implement rejection logic in `MatchService`
    - [x] Implement `PUT /api/v1/matches/{id}/reject` endpoint in `MatchController`
- [x] Task: Conductor - User Manual Verification 'Confirmation Workflow Logic' (Protocol in workflow.md)

## Phase 3: Frontend - Match Recording UI
- [x] Task: Create Match Recording Form Component a0d0637
    - [x] Write component tests for player selection and initial score setup
    - [x] Implement basic match creation form with user searching
- [x] Task: Implement Multi-Game Scoring with Position Swap Enforcement c118c47
    - [x] Write tests for the system-enforced position swap between Game 1 and Game 2
    - [x] Implement the step-by-step scoring UI
- [ ] Task: Conductor - User Manual Verification 'Frontend - Match Recording UI' (Protocol in workflow.md)

## Phase 4: Frontend - Dashboard & Final Integration
- [ ] Task: Create Pending Approvals View
    - [ ] Write tests for listing pending matches and action buttons (Approve/Reject)
    - [ ] Implement the dashboard view for match verification
- [ ] Task: Verify End-to-End Match Flow
    - [ ] Write integration tests for the full create-then-approve lifecycle
    - [ ] Perform manual end-to-end verification
- [ ] Task: Conductor - User Manual Verification 'Frontend - Dashboard & Final Integration' (Protocol in workflow.md)
