# Story 8.4: Equal Match Distribution (2v2 Random Pairing)

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a system,
I want to distribute matches equally in random pairing mode,
so that the tournament is fair.

## Acceptance Criteria

1. **Given** a 2v2 random pairing tournament generates its match schedule, **When** the pairings are calculated, **Then** the algorithm guarantees each participant is assigned an equal number of matches as per the format constraints (FR47).
2. **And** in 2v2 random pairings, both opponents must confirm individually (because this mode is an individual competition) (FR14).
3. **And** if a player's account is deleted during an active tournament, a stub partner is assigned randomly from players closest in statistical strength to the deleted player (FR33).

## Tasks / Subtasks

- [ ] Task 1 (AC: 1) Algorithm for Equal Match Distribution
  - [ ] Implement match generation algorithm for 2v2 random pairing
  - [ ] Ensure algorithm constraints guarantee equal match count per player
  - [ ] Unit test the algorithm against mathematical constraints
- [ ] Task 2 (AC: 2) Match Confirmation Logic
  - [ ] Implement individual confirmation requirement for 2v2 random pairing
  - [ ] Update state machine for match status to track both confirmations
- [ ] Task 3 (AC: 3) Stub Partner Assignment
  - [ ] Implement strength-based stub partner selection algorithm
  - [ ] Update tournament state upon player deletion
  - [ ] Ensure stub partner matches do not skew global player statistics

## Dev Notes

- **Architecture Patterns and Constraints:**
  - The match generation algorithm MUST be deterministic, using tournament seed and ID for reproducibility.
  - The algorithm should handle odd numbers of players or edge cases if possible, but based on tournament requirements, 2v2 random pairings typically require a multiple of 4 participants. Document and throw domain exceptions for invalid configurations.
  - **Account Deletion Rule:** "Deletion is never blocked but follows a 24-hour delay protocol with countdown notification and cancellation option. Partner notified immediately. Remaining tournament matches result in technical defeat. For random-pairing tournaments: stub partner assigned randomly from players closest in statistical strength to the deleted player, using frozen strength rating captured at tournament start."
- **Source tree components to touch:**
  - Backend: `TournamentService`, `MatchGenerationStrategy`, `TournamentEntity`, `MatchEntity`
- **Testing standards summary:**
  - High coverage required for the match generation algorithm using property-based testing or exhaustive combinations (JUnit 6, AssertJ).

### Project Structure Notes

- Adhere to the established Domain-Driven Design patterns in the backend.

### References

- [Source: _bmad-output/planning-artifacts/prd.md#Functional Requirements] (FR14, FR33, FR47)
- [Source: _bmad-output/planning-artifacts/epics.md] (Epic 8, Story 8.4)

## Dev Agent Record

### Agent Model Used
Gemini 3.1 Pro (High)

### Debug Log References
N/A

### Completion Notes List
- Ultimate context engine analysis completed - comprehensive developer guide created

### File List
N/A

## Change Log
- Initial creation of the story document.

### Review Findings
N/A
