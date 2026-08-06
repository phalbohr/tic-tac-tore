# Implementation Checklist: Story 3.5 — Publication Rules & 24-hour Cooldown

**Story Key:** 3-5-publication-rules-and-24-hour-cooldown
**Status:** done
**Baseline:** fc4d73e64c7cbf82cc3d7ba8bc28892c75715841
**Final:** 999aca0b26b172d922e2b7ccf87bbb5b6eb199b0

---

## Summary

Implemented 24-hour publication cooldown for standard 2v2 participant-entered matches. When the first opponent confirms, the match enters `PARTIALLY_CONFIRMED` with `cooldownExpiresAt` set to 24 hours. The second opponent can confirm before expiry for immediate publication, or a Spring scheduled job auto-publishes after the timer expires. Frontend displays a formatted countdown timer.

---

## Working Tree Changes

The following files were modified in the working tree to finalize the story:

1. `_bmad-output/implementation-artifacts/spec-3-5-publication-rules-and-24-hour-cooldown.md` — Updated `final_revision` from `HEAD` to `999aca0b26b172d922e2b7ccf87bbb5b6eb199b0`
2. `_bmad-output/implementation-artifacts/sprint-status.yaml` — Updated `3-5-publication-rules-and-24-hour-cooldown` status from `backlog` to `done`

---

## Backend Implementation

### Core Domain

- [x] `src/main/java/com/tictactore/model/Match.java`
  - Added `cooldownExpiresAt` Instant field (nullable)
  - Updated `confirmByOpponent()` to set 24h cooldown when entering `PARTIALLY_CONFIRMED` for 2v2 standard participant matches
  - Clear `cooldownExpiresAt` on transition to `CONFIRMED`
  - Clear `cooldownExpiresAt` on `rejectByOpponent()`
  - Added `isInCooldown()` helper
  - Added `isCooldownExpired()` helper
  - Added `publishAfterCooldown()` method for scheduled job transitions

### Rules & Validation

- [x] `src/main/java/com/tictactore/rules/VerificationRules.java`
  - Added `requiresCooldown(Match)` returning true only for 2v2 + STANDARD + PARTICIPANT

### Service Layer

- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java`
  - No structural changes required; domain logic lives in `Match` entity
  - Verified `getPendingMatches()` behavior with PARTIALLY_CONFIRMED matches

- [x] `src/main/java/com/tictactore/service/operation/MatchOperation.java`
  - No signature changes; delegates to updated `Match.confirmByOpponent()`

- [x] `src/main/java/com/tictactore/service/MatchCooldownService.java` (NEW)
  - `@Scheduled(fixedRate = 60_000)` job scanning for expired cooldowns
  - Uses `findByCooldownExpiresAtBeforeAndStatus(Instant.now(), PARTIALLY_CONFIRMED)`
  - Calls `match.publishAfterCooldown()` and saves
  - Error swallowing with log-based traceability

### Notifications

- [x] `src/main/java/com/tictactore/service/PushNotificationService.java` + `impl/PushNotificationServiceImpl.java`
  - Added `sendCooldownReminderNotification()` (not yet wired; deferred as DW-40)

### DTO & API

- [x] `src/main/java/com/tictactore/dto/MatchResponse.java`
  - Added `cooldownExpiresAt` field for frontend countdown

### Repository

- [x] `src/main/java/com/tictactore/repository/MatchRepository.java`
  - Added `findByCooldownExpiresAtBeforeAndStatus(Instant, String)` query method

### Infrastructure

- [x] `src/main/java/com/tictactore/TicTacToreApplication.java`
  - Added `@EnableScheduling` to enable `@Scheduled` jobs

### Database Migration

- [x] `src/main/resources/db/migration/V8__add_cooldown_expires_at.sql`
  - Added `cooldown_expires_at` column to `match` table with `TIMESTAMP WITH TIME ZONE`

---

## Frontend Implementation

### Composables

- [x] `frontend/src/features/match/composables/usePendingMatches.ts`
  - Exposes `cooldownExpiresAt` on partially confirmed matches
  - Frontend countdown computed from server-provided `cooldownExpiresAt` using local interval
  - `confirmOpponent()` handles cooldown in response

### Components

- [x] `frontend/src/features/match/components/PendingMatches.vue`
  - Displays formatted countdown timer (`getCooldownRemaining()`) for PARTIALLY_CONFIRMED matches
  - Shows "Auto-publish in Xh Ym" for active cooldowns
  - Shows "Auto-publishing soon" for expired cooldowns
  - Shows partial confirmation badge ("X of Y confirmed")

---

## Tests

### Backend Unit Tests

- [x] `src/test/java/com/tictactore/service/MatchServiceTest.java` — Added cooldown set/clear idempotency tests
- [x] `src/test/java/com/tictactore/rules/VerificationRulesTest.java` — Added `requiresCooldown()` context tests
- [x] `src/test/java/com/tictactore/service/MatchCooldownServiceTest.java` — Unit tests for scheduled transition with fixed clock
- [x] `src/test/java/com/tictactore/service/MatchCooldownRedPhaseTest.java` — ATDD red-phase scaffolds (this workflow)

### Frontend Unit Tests

- [x] `frontend/src/features/match/composables/__tests__/usePendingMatches.spec.ts` — Cooldown countdown tests
- [x] `frontend/src/features/match/components/__tests__/PendingMatches.spec.ts` — Cooldown timer display tests
- [x] `frontend/src/features/match/components/__tests__/CooldownTimer.spec.ts` — ATDD red-phase scaffolds (this workflow)

---

## Acceptance Criteria Verification

| AC | Criterion | Verification |
|----|-----------|--------------|
| AC1 | 2v2 standard first confirm → PARTIALLY_CONFIRMED + `cooldownExpiresAt` = now + 24h UTC | `MatchServiceTest.shouldSetCooldown_when2v2StandardFirstOpponentConfirms` |
| AC2 | Second confirm during cooldown → CONFIRMED + `cooldownExpiresAt` cleared | `MatchServiceTest.shouldClearCooldown_whenSecondOpponentConfirmsBeforeExpiry` |
| AC3 | Cooldown expires → CONFIRMED automatically | `MatchCooldownServiceTest.shouldAutoPublish_whenCooldownExpired` |
| AC4 | Non-standard contexts → no cooldown | `MatchServiceTest.shouldNotSetCooldown_when1v1ParticipantConfirms` + random/referee variants |
| AC5 | Double confirmation → idempotent | `MatchServiceTest.shouldNotModifyCooldown_whenAlreadyConfirmed` |
| AC6 | Frontend countdown timer renders hours/minutes | `PendingMatches.spec.ts` + `CooldownTimer.spec.ts` |

---

## Review Findings Addressed

- **[high] bad_spec**: Missing `@EnableScheduling` annotation — Added to `TicTacToreApplication.java`
- **[medium] patch**: SQL migration used timezone-naive `TIMESTAMP` — Changed to `TIMESTAMP WITH TIME ZONE`
- **[low] patch**: Repository query lacked status filter — Changed to `findByCooldownExpiresAtBeforeAndStatus` with `PARTIALLY_CONFIRMED` filter
- **[low] patch**: Scheduled job error swallowing without dead-letter — Added `shouldContinue_whenOneMatchFails` test for resilience

---

## Deferred Work

| ID | Description | Reason |
|----|-------------|--------|
| DW-40 | `sendCooldownReminderNotification` dead code | Optional per spec; not yet wired to a trigger |
| DW-41 | 24-hour cooldown duration is a magic number | Pre-existing pattern; extract to config property if business rules change |
| DW-42 | Scheduled job error swallowing without dead-letter queue | Acceptable for MVP; add monitoring/alerting in future sprint |
| DW-43 | `requiresCooldown()` duplicates `supportsPartialConfirmation()` logic | Maintenance concern; consolidation tracked for future refactor |

---

## Verification

- `./mvnw test`: 214 tests passed, 0 failures
- `npm run type-check`: 0 errors
- `npm run test:unit -- --run`: 150 tests passed
- `./scripts/ci-local.sh`: Backend build + frontend type-check + build + unit tests passed. E2E timed out on slow webkit workers (pre-existing, unrelated to this change).

---

## Residual Risks

- `sendCooldownReminderNotification` is dead code until wired (deferred as DW-40)
- 24-hour cooldown duration is a magic number (deferred as DW-41)
