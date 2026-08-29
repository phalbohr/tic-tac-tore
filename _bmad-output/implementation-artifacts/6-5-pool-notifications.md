---
baseline_commit: HEAD
---

# Story 6.5: Pool Notifications

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a player,
I want to be notified when pools matching my criteria are available or when a pool I'm in fills up,
so that I don't miss out on games.

## Acceptance Criteria

1. **Given** a player has push notifications enabled
   **When** a new pool is created that matches their saved preferences
   **Then** the system immediately dispatches a push notification to their device (FR37).
2. **Given** an open pool that a player is participating in
   **When** the pool reaches its participant capacity and transitions to `FILLED`
   **Then** the system dispatches a push notification to all participants in that pool.
3. **Given** the notifications are sent
   **When** they are received by the client
   **Then** tapping the notification opens the relevant pool view in the app.

## Developer Context

**Goal**: Implement push notifications for pool creation and filling events.

**Technical Requirements**:
- Must integrate with the existing `PushNotificationService`.
- When a `Pool` is created (`PoolServiceImpl.createPool`), check for users with matching preferences and send a notification.
- When a `Pool` reaches `FILLED` status (e.g. in `PoolServiceImpl.joinPool`), notify all participants in the pool.
- Follow the 500-Line Rule (IP-04).
- Do not introduce regressions into existing pool creation or joining logic.

**Architecture Compliance**:
- Use Spring Boot event publishing (`ApplicationEventPublisher`) to decouple the notification logic from `PoolServiceImpl` and avoid circular dependencies or tight coupling.
- Notifications should be handled asynchronously (`@Async`).

**Testing Requirements**:
- Write unit tests for the notification event listeners.
- Use ATDD style tests where possible (see `PushNotificationServiceATDDTest` for examples).
- Coverage should meet project standards.
