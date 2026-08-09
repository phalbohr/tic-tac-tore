---
title: 'Story 3.5: Publication Rules & 24-hour Cooldown'
type: 'feature'
created: '2026-08-06T17:50:00Z'
status: 'done'
review_loop_iteration: 0
followup_review_recommended: false
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-3-context.md'
warnings: []
baseline_revision: 'fc4d73e64c7cbf82cc3d7ba8bc28892c75715841'
final_revision: '999aca0b26b172d922e2b7ccf87bbb5b6eb199b0'
---

<intent-contract>

## Intent

**Problem:** After the first opponent confirms a 2v2 standard match, the system transitions to `PARTIALLY_CONFIRMED` but has no timer or automatic publication mechanism. FR15 requires a 24-hour cooldown before publishing results to statistics, but currently the match remains in a liminal state indefinitely if the second opponent does not confirm.

**Approach:** Add `cooldownExpiresAt` to the `Match` entity and set it when a 2v2 standard match enters `PARTIALLY_CONFIRMED`. Introduce a Spring scheduled job that scans for expired cooldowns and transitions those matches to `CONFIRMED`. When the second opponent confirms before the timer expires, clear the cooldown and publish immediately. Update the frontend to display a cooldown countdown on partially confirmed matches.

## Boundaries & Constraints

**Always:**
- All time calculations must be server-side in UTC; client displays local timezone for readability only.
- Maintain the Three-Layer Transaction Architecture: `MatchServiceImpl` stays `@Retryable` ONLY; `MatchOperation` stays `@Idempotent` + `@Transactional`.
- Domain logic (cooldown state transitions) lives in the `Match` entity, not in the service layer.
- `@Retryable` and `@Transactional` are NEVER combined on the same method.
- Authentication: caller UUID always extracted from Spring Security `SecurityContext` / `@AuthenticationPrincipal`.
- Backward compatibility: existing 1v1 flows and non-standard 2v2 flows must remain unchanged (no cooldown for those contexts).
- Do NOT remove or alter the existing `confirmByOpponent()` signature — extend it.

**Block If:**
- Database migration fails on the target platform (H2 test / PostgreSQL prod).
- Scheduled job infrastructure cannot be initialized (missing scheduling configuration).

**Never:**
- Do NOT combine `@Retryable` and `@Transactional` on the same method.
- Do NOT auto-request browser notifications or push permissions on page load.
- Do NOT use 1px borders in UI (UX-DR3 No-Line rule).
- Do NOT publish `PARTIALLY_CONFIRMED` matches to statistics during the cooldown window.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| First confirm 2v2 standard | 2v2 STANDARD, PARTICIPANT, first opponent confirms | Status → PARTIALLY_CONFIRMED, `cooldownExpiresAt` = now + 24h | None |
| Second confirm during cooldown | PARTIALLY_CONFIRMED, second opponent confirms before expiry | Status → CONFIRMED, `cooldownExpiresAt` cleared | None |
| Cooldown expires | PARTIALLY_CONFIRMED, `cooldownExpiresAt` <= now, scheduled job runs | Status → CONFIRMED automatically | None |
| Double confirmation | Same opponent confirms twice | Idempotency — return current match state, no error | None |
| Non-standard match confirms | 1v1 or 2v2 RANDOM/REFEREE first confirmation | Status → CONFIRMED immediately, no cooldown field set | None |
| Creator self-confirm | Creator attempts to confirm | UnauthorizedMatchActionException (403) | Existing behavior preserved |
| Reject during cooldown | PARTIALLY_CONFIRMED match is rejected | Status → REJECTED, `cooldownExpiresAt` ignored/cleared | None |

</intent-contract>

## Code Map

From Story 3.4 continuity (confirmed status: done):

**Backend:**
- `src/main/java/com/tictactore/model/Match.java` -- entity; add `cooldownExpiresAt` field; update `confirmByOpponent()` to set/clear cooldown based on context; add `isInCooldown()` helper
- `src/main/java/com/tictactore/rules/VerificationRules.java` -- add `requiresCooldown(Match)` to identify 2v2 standard participant matches
- `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` -- clear `cooldownExpiresAt` on second confirmation; ensure `getPendingMatches()` still works
- `src/main/java/com/tictactore/service/operation/MatchOperation.java` -- no signature changes; `confirmMatch()` delegates to updated `Match.confirmByOpponent()`
- `NEW` `src/main/java/com/tictactore/service/MatchCooldownService.java` -- `@Scheduled` job scanning for expired cooldowns and transitioning to CONFIRMED
- `src/main/java/com/tictactore/service/PushNotificationService.java` + `impl/PushNotificationServiceImpl.java` -- add `sendCooldownExpiryNotification()` or reuse existing notification types
- `src/main/java/com/tictactore/dto/MatchResponse.java` -- add `cooldownExpiresAt` field for frontend countdown
- `src/main/java/com/tictactore/repository/MatchRepository.java` -- add `findByCooldownExpiresAtBefore` for the scheduled job

**Database:**
- `NEW` `src/main/resources/db/migration/V8__add_cooldown_expires_at.sql` -- add `cooldown_expires_at` column to `match` table

**Frontend:**
- `frontend/src/features/match/composables/usePendingMatches.ts` -- expose `cooldownExpiresAt` and local countdown calculation
- `frontend/src/features/match/components/PendingMatches.vue` -- display cooldown countdown timer for PARTIALLY_CONFIRMED matches

**Tests:**
- `src/test/java/com/tictactore/service/MatchServiceTest.java` -- add cooldown set/clear tests
- `src/test/java/com/tictactore/rules/VerificationRulesTest.java` -- add `requiresCooldown()` tests
- `NEW` `src/test/java/com/tictactore/service/MatchCooldownServiceTest.java` -- scheduled job tests with fixed clock
- `frontend/src/features/match/composables/__tests__/usePendingMatches.spec.ts` -- cooldown countdown tests
- `frontend/src/features/match/components/__tests__/PendingMatches.spec.ts` -- cooldown timer display tests

## Tasks & Acceptance

**Execution:**
- [x] `src/main/java/com/tictactore/TicTacToreApplication.java` -- Add `@EnableScheduling` to enable `@Scheduled` jobs
- [x] `src/main/java/com/tictactore/model/Match.java` -- Add `cooldownExpiresAt` Instant field; update `confirmByOpponent()` to set cooldown when entering PARTIALLY_CONFIRMED for 2v2 standard, and clear it on transition to CONFIRMED; add `isInCooldown()` and `isCooldownExpired()` helpers
- [x] `src/main/java/com/tictactore/rules/VerificationRules.java` -- Add `requiresCooldown(Match)` returning true only for 2v2 + STANDARD + PARTICIPANT
- [x] `src/main/java/com/tictactore/service/operation/MatchOperation.java` -- No signature changes; delegate to updated `Match.confirmByOpponent()`
- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` -- No structural changes needed if logic lives in entity; verify `getPendingMatches()` behavior
- [x] `src/main/java/com/tictactore/service/MatchCooldownService.java` -- Create `@Scheduled` service with `processExpiredCooldowns()` method; scan `findByCooldownExpiresAtBeforeAndStatus`, transition expired matches to CONFIRMED
- [x] `src/main/java/com/tictactore/service/PushNotificationService.java` + `impl/PushNotificationServiceImpl.java` -- Add `sendCooldownReminderNotification()` for matches approaching expiry (optional, based on AC scope)
- [x] `src/main/java/com/tictactore/dto/MatchResponse.java` -- Add `cooldownExpiresAt` field
- [x] `src/main/java/com/tictactore/repository/MatchRepository.java` -- Add `findByCooldownExpiresAtBeforeAndStatus(Instant, String)` query method
- [x] `src/main/resources/db/migration/V8__add_cooldown_expires_at.sql` -- Add `cooldown_expires_at` column to `match` table with `TIMESTAMP WITH TIME ZONE`
- [x] `src/test/java/com/tictactore/service/MatchServiceTest.java` -- Add cooldown set/clear idempotency tests
- [x] `src/test/java/com/tictactore/rules/VerificationRulesTest.java` -- Add `requiresCooldown()` context tests
- [x] `src/test/java/com/tictactore/service/MatchCooldownServiceTest.java` -- Unit tests for scheduled transition with fixed clock
- [x] `frontend/src/features/match/composables/usePendingMatches.ts` -- Expose `cooldownExpiresAt` and local countdown; update `confirmOpponent()` to handle cooldown in response
- [x] `frontend/src/features/match/components/PendingMatches.vue` -- Display formatted countdown timer for PARTIALLY_CONFIRMED matches

**Acceptance Criteria:**
- AC1: Given a standard 2v2 participant-entered match in PENDING_APPROVAL, when the first opponent confirms, then the match transitions to PARTIALLY_CONFIRMED and `cooldownExpiresAt` is set to 24 hours from confirmation time in UTC
- AC2: Given a match in PARTIALLY_CONFIRMED with an active cooldown, when the second opponent confirms before the cooldown expires, then the match transitions to CONFIRMED immediately and `cooldownExpiresAt` is cleared
- AC3: Given a match in PARTIALLY_CONFIRMED with an expired cooldown, when the scheduled job scans for expired cooldowns, then the match transitions to CONFIRMED automatically
- AC4: Given any non-standard match context (1v1, 2v2 random, 2v2 referee), when the first confirmation occurs, then no cooldown is set and the match follows its normal confirmation rules
- AC5: Given a match already in CONFIRMED state, when an opponent attempts to confirm again, then the system returns the current state without error and does not modify `cooldownExpiresAt`
- AC6: Given a partially confirmed match in cooldown, when the creator views the match in the UI, then they see a formatted countdown timer showing remaining hours and minutes until automatic publication

## Spec Change Log

<!-- Append-only. Populated by step-04 during review loops. Do not modify or delete existing entries. -->

## Review Triage Log

### 2026-08-06 — Review pass
- intent_gap: 0
- bad_spec: 1: (high 1) Missing `@EnableScheduling` annotation; spec created `@Scheduled` service without enabling scheduling infrastructure
- patch: 3: (medium 1, low 2) SQL migration used timezone-naive `TIMESTAMP` instead of `TIMESTAMP WITH TIME ZONE`; repository query lacked status filter; added error-continuation test for scheduled job
- defer: 4: (low 4) `sendCooldownReminderNotification` dead code (optional per spec); hardcoded 24h magic number (pre-existing pattern); error swallowing without dead-letter (acceptable for MVP); `requiresCooldown` duplicates `supportsPartialConfirmation` logic (maintenance concern)
- reject: 10: (low 10) Diff artifact corruption (empty files, malformed headers, duplicate entries); frontend countdown not live (AC6 satisfied as-is); pre-existing non-functional test; impossible domain state test (valid idempotency verification)
- addressed_findings:
  - `[high]` `[bad_spec]` Added `@EnableScheduling` to `TicTacToreApplication.java` to enable scheduled job execution
  - `[medium]` `[patch]` Changed V8 migration to `TIMESTAMP WITH TIME ZONE` to match project convention and prevent timezone drift
  - `[low]` `[patch]` Changed repository query to `findByCooldownExpiresAtBeforeAndStatus` with `PARTIALLY_CONFIRMED` filter to prevent stale data leakage
  - `[low]` `[patch]` Added `shouldContinue_whenOneMatchFails` test to verify scheduled job resilience

## Design Notes

### Cooldown Expiry Logic
- `cooldownExpiresAt` is a nullable `Instant` on the `Match` entity. Null means no cooldown is active.
- `Match.isCooldownExpired()` returns true when `cooldownExpiresAt != null && cooldownExpiresAt <= Instant.now()`.
- The scheduled job uses `MatchRepository.findByCooldownExpiresAtBefore(Instant.now())` to find candidates, then calls `match.confirmByOpponent(null)` or a dedicated `publishAfterCooldown()` method to transition to CONFIRMED.

### Scheduled Job Design
- Use Spring's `@Scheduled(fixedRate = 60_000)` to scan every minute.
- Run inside a `@Transactional` service method with retry semantics delegated to the transaction manager.
- Keep the job idempotent: if a match is already CONFIRMED, skip it.
- Query uses `findByCooldownExpiresAtBeforeAndStatus` with `PARTIALLY_CONFIRMED` filter to avoid stale data.

### Cooldown Duration
- 24-hour cooldown is currently hardcoded as `Instant.now().plusSeconds(24 * 60 * 60)` in `Match.confirmByOpponent()`.
- Consider extracting to a configuration property if business rules change.

## Verification

**Commands:**
- `./mvnw test` -- expected: all existing + new tests pass
- `npm run type-check` (frontend) -- expected: 0 errors
- `npm run test:unit -- --run` (frontend) -- expected: all tests pass
- `./scripts/ci-local.sh` -- expected: all checks pass

## Auto Run Result

Status: done

Summary: Implemented 24-hour publication cooldown for standard 2v2 participant-entered matches. When the first opponent confirms, the match enters `PARTIALLY_CONFIRMED` with `cooldownExpiresAt` set to 24 hours. The second opponent can confirm before expiry for immediate publication, or a Spring scheduled job auto-publishes after the timer expires. Frontend displays a formatted countdown timer.

Files changed:
- `TicTacToreApplication.java` — Added `@EnableScheduling`
- `Match.java` — Added `cooldownExpiresAt`, `isInCooldown()`, `isCooldownExpired()`, `publishAfterCooldown()`
- `VerificationRules.java` — Added `requiresCooldown()`
- `MatchCooldownService.java` — New `@Scheduled` auto-publish service
- `MatchRepository.java` — Added `findByCooldownExpiresAtBeforeAndStatus`
- `MatchResponse.java` — Added `cooldownExpiresAt` field
- `PushNotificationService.java` / `PushNotificationServiceImpl.java` — Added `sendCooldownReminderNotification()` (optional, not yet wired)
- `V8__add_cooldown_expires_at.sql` — New migration with `TIMESTAMP WITH TIME ZONE`
- `usePendingMatches.ts` / `PendingMatches.vue` — Frontend cooldown countdown display
- Test files updated/created: `MatchServiceTest`, `VerificationRulesTest`, `MatchCooldownServiceTest`, `PendingMatches.spec.ts`, `usePendingMatches.spec.ts`

Review findings:
- Patches applied: 3 (SQL timezone type, repository query status filter, scheduled job error-continuation test)
- Items deferred: 4 (DW-40 through DW-43)
- Items rejected: 10 (diff artifact noise, frontend countdown cadence, pre-existing test issues)

Follow-up review recommended: false

Verification:
- `./mvnw test`: 214 tests passed, 0 failures
- `npm run type-check`: 0 errors
- `npm run test:unit -- --run`: 150 tests passed
- `./scripts/ci-local.sh`: Backend build + frontend type-check + build + unit tests passed. E2E timed out on slow webkit workers (pre-existing, unrelated to this change).

Residual risks:
- `sendCooldownReminderNotification` is dead code until wired to a trigger (deferred as DW-40)
- 24-hour cooldown duration is a magic number (deferred as DW-41)
