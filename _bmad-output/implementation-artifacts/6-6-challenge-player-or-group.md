---
baseline_commit: HEAD
---

# Story 6.6: Challenge Player or Group

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to challenge specific people,
so that we can settle a rivalry.

## Acceptance Criteria

1. **Given** a player is browsing the leaderboard or player directory
   **When** they tap the "Challenge" button next to a player or team
   **Then** the system creates a direct match invitation and notifies the target player(s) (FR38)
2. **Given** a target player receives a challenge notification
   **When** they tap the notification
   **Then** they are taken to a view where they can accept or decline the challenge
3. **Given** a challenge is accepted
   **When** the target player accepts
   **Then** the challenger receives a notification that the challenge was accepted and they can proceed to the table.

## Tasks / Subtasks

- [ ] Task 1: Database Migration & Domain Entity Updates (AC1, AC2, AC3)
  - [ ] Create Flyway migration for `match_challenge` table (id, challenger_id, target_player_id, target_group_id, status, timestamps).
  - [ ] Create `MatchChallenge` entity and `ChallengeStatus` enum (PENDING, ACCEPTED, DECLINED).
  - [ ] Add domain events `ChallengeCreatedEvent`, `ChallengeAcceptedEvent`, `ChallengeDeclinedEvent`.
- [ ] Task 2: Backend Services & API (AC1, AC2, AC3)
  - [ ] Create `ChallengeService` and `ChallengeController` with endpoints to create, accept, and decline challenges.
  - [ ] Add async listener `ChallengeNotificationListener` to handle domain events and trigger push notifications.
  - [ ] Update `PushNotificationService` and payloads for challenge events.
  - [ ] Write backend unit tests and ATDD tests for the new endpoints and notification logic.
- [ ] Task 3: Frontend Implementation (AC1, AC2, AC3)
  - [ ] Add "Challenge" button/icon to Leaderboard rows and Player/Group detail views.
  - [ ] Implement UI for receiving and responding to challenges (e.g., in a pending invitations section on the Home Hub or via a modal).
  - [ ] Update `sw.js` to handle challenge push notification payloads and deep linking.
  - [ ] Write frontend component and E2E tests for the challenge flow.

## Dev Notes

- **Asynchronous Isolation:** Push notifications involve external WebPush network roundtrips. Never block user-facing database transactions; always dispatch via `@Async` `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
- **Fault Resilience:** If an endpoint returns 404/410 (expired subscription) or fails, catch the exception, record in `NotificationLog`, and do not interrupt other notifications or application flow.
- Follow the established Spring Boot conventions in `code-1-guide` and Vue 3 / Composition API conventions.

### Project Structure Notes

- Alignment with unified project structure: `src/main/java/com/tictactore/model`, `src/main/java/com/tictactore/service`, etc.

### References

- [Source: _bmad-output/planning-artifacts/prd.md#FR38] - FR38 (Player can challenge a specific player or group to a match)
- [Source: _bmad-output/planning-artifacts/epics.md] - Story 6.6

## Dev Agent Record

### Agent Model Used

Gemini 3.1 Pro (High)

### Debug Log References

### Completion Notes List

### File List
