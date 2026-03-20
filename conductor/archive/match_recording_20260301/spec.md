# Specification: 2v2 Match Recording & Confirmation Workflow

## Overview
Implement the foundational workflow for players to record 2v2 (Doubles) foosball matches. This includes recording scores for a best-of-3 format, enforcing position rotation rules, and a validation system to ensure data integrity through opponent confirmation.

## Functional Requirements
- **Match Submission**:
    - An authenticated user (the "Creator") can start a new match record.
    - The Creator selects 3 other participants (Teammate, 2 Opponents).
    - The system records scores for up to 3 games (Best of 3).
- **2v2 Position Rotation (Strict Enforcement)**:
    - **Game 1**: Players choose initial positions (Attacker/Defender).
    - **Game 2**: Teammates **must** swap positions (system-enforced).
    - **Game 3**: Free position selection (if a third game is played).
- **Confirmation Workflow**:
    - Once submitted, the match is in a `PENDING_APPROVAL` state.
    - At least one player from the opposing team must approve the result.
    - If approved, the match status becomes `CONFIRMED`.
    - If rejected by an opponent, the match status becomes `DRAFT` and is returned to the Creator for correction.
- **Dashboard/UI**:
    - A view to see "Pending Approvals" for matches where the user is an opponent.
    - A simple mobile-friendly interface for match creation with clear game-by-game scoring.

## Non-Functional Requirements
- **Data Integrity**: Matches cannot contribute to leaderboards until `CONFIRMED`.
- **User Experience**: The recording flow must be optimized for mobile devices to be used immediately after a game.

## Acceptance Criteria
- [ ] Users can create a 2v2 match and select 3 other participants from the user list.
- [ ] Position swaps are strictly enforced between Game 1 and Game 2.
- [ ] Opponents can view pending matches and choose to "Approve" or "Reject".
- [ ] Match status updates correctly based on user actions (DRAFT -> PENDING -> CONFIRMED/DRAFT).
- [ ] A match is only officially recorded (CONFIRMED) after at least one opponent's approval.

## Out of Scope
- Singles (1v1) matches (to be handled in a separate track).
- Real-time push notifications.
- Detailed statistical calculations (handled in a future track).
