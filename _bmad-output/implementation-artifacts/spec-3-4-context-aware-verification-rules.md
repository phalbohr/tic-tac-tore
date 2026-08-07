---
title: 'Story 3.4: Context-Aware Verification Rules'
type: 'feature'
created: '2026-08-06T13:51:00Z'
status: 'done'
review_loop_iteration: 0
followup_review_recommended: false
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-3-context.md'
warnings: []
baseline_revision: 'cb27c3df888a4889f8afe283061d0797a8476933'
---

<intent-contract>

## Intent

**Problem:** Currently, any single opponent can immediately confirm any match by setting its status to `CONFIRMED`. This ignores FR14, which requires context-aware verification rules: different match contexts (1v1/2v2, participant/referee-entered, standard/random) need varying numbers of opponent confirmations before a match is considered confirmed. Referee-entered and 2v2 random matches need more than one confirmation, but there is no mechanism to track or enforce this.

**Approach:** Add `entryMode` (PARTICIPANT/REFEREE) and `matchFormat` (STANDARD/RANDOM) fields to the `Match` entity. Track confirmed opponents via a `confirmedByOpponentIds` comma-separated string. Create a stateless `VerificationRules` evaluator that determines required confirmations per match context. Modify `Match.confirmByOpponent()` to accumulate confirmations and transition through `PENDING_APPROVAL` → `PARTIALLY_CONFIRMED` → `CONFIRMED` as thresholds are met. Introduce `STATUS_PARTIALLY_CONFIRMED` for 2v2-standard matches where the first opponent's confirmation triggers the 24-hour cooldown (Story 3.5). Update `MatchServiceImpl.confirmMatch()` and `getPendingMatches()` to handle the new states and idempotency via `hasConfirmed()`.

## Boundaries & Constraints

**Always:**
- Maintain the Three-Layer Transaction Architecture: `MatchServiceImpl` stays `@Retryable` ONLY; `MatchOperation` stays `@Idempotent` + `@Transactional`.
- Domain logic (confirmation rules, state transitions) lives in the `Match` entity and `VerificationRules` class, not in the service layer.
- `@Retryable` and `@Transactional` are NEVER combined on the same method.
- Authentication: caller UUID always extracted from Spring Security `SecurityContext` / `@AuthenticationPrincipal`.
- Backward compatibility: existing 1v1 participant-entered flow must remain unchanged (1 opponent → CONFIRMED immediately).
- `hasConfirmed()` must fall back to checking `confirmedByUserId` for backward compatibility with existing data.
- The `<intent-contract>` block is read-only and must not be modified during implementation.

**Block If:**
- Database migration fails on the target platform (H2 test / PostgreSQL prod).
- A verification rule cannot be determined for a given match configuration.

**Never:**
- Do NOT remove or alter the existing `confirmByOpponent()` signature — extend it.
- Do NOT combine `@Retryable` and `@Transactional` on the same method.
- Do NOT auto-request browser notifications or push permissions on page load.
- Do NOT use 1px borders in UI (UX-DR3 No-Line rule).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| 1v1 participant confirms | 1v1, PARTICIPANT, PENDING_APPROVAL, 1 opponent confirms | Status → CONFIRMED, confirmedByOpponentIds=[opp], confirmedByUserId=opp | None |
| 1v1 referee needs both | 1v1, REFEREE, first opponent confirms | Status → PENDING_APPROVAL (tracked, not CONFIRMED), confirmedByOpponentIds=[opp1] | Second opponent needed for CONFIRMED |
| 1v1 referee fully confirmed | 1v1, REFEREE, second opponent confirms | Status → CONFIRMED | None |
| 2v2 standard first confirm | 2v2, STANDARD, PARTICIPANT, 1 opponent confirms | Status → PARTIALLY_CONFIRMED, confirmedByOpponentIds=[opp] | Remaining opponent notified |
| 2v2 standard second confirm | 2v2, STANDARD, PARTICIPANT, 2nd opponent confirms | Status → CONFIRMED | None |
| 2v2 random both needed | 2v2, RANDOM, PARTICIPANT, 1 opponent confirms | Status → PENDING_APPROVAL (tracked), confirmedByOpponentIds=[opp1] | Second opponent needed |
| 2v2 random fully confirmed | 2v2, RANDOM, PARTICIPANT, 2nd opponent confirms | Status → CONFIRMED | None |
| 2v2 referee one per team | 2v2, REFEREE, 1 from each team confirms | Status → CONFIRMED | Must have 1 from Team A AND 1 from Team B |
| Double confirmation | Same opponent confirms twice | Idempotency — return current match state, no error | None |
| Creator self-confirm | Creator attempts to confirm | UnauthorizedMatchActionException (403) | Existing behavior preserved |
| Non-opponent confirm | Non-participant attempts to confirm | UnauthorizedMatchActionException (403) | Existing behavior preserved |
| Reopen confirmed match | Match already CONFIRMED, new opponent confirms | InvalidMatchStateException (400) | Existing behavior preserved |

</intent-contract>

## Code Map

From Story 3.3 continuity (confirmed status: done):

**Backend:**
- `src/main/java/com/tictactore/model/Match.java` -- entity; needs `entryMode`, `matchFormat`, `confirmedByOpponentIds`, `STATUS_PARTIALLY_CONFIRMED`, updated `confirmByOpponent()` with `addConfirmation()` / `hasConfirmed()` helpers
- `src/main/java/com/tictactore/service/operation/MatchOperation.java` -- `@Idempotent` + `@Transactional`; `confirmMatch()` delegates to `match.confirmByOpponent()`
- `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` -- `@Retryable` ONLY; `confirmMatch()` needs PARTIALLY_CONFIRMED handling + `hasConfirmed()` idempotency; `getPendingMatches()` needs to include PARTIALLY_CONFIRMED; `isUserPendingApprover()` must exclude already-confirmed users
- `src/main/java/com/tictactore/service/MatchService.java` -- interface; may add helper method
- `src/main/java/com/tictactore/controller/MatchController.java` -- REST endpoint `POST /api/v1/matches/{id}/confirm`; no changes needed (principal-based auth)
- `src/main/java/com/tictactore/service/PushNotificationService.java` + `impl/PushNotificationServiceImpl.java` -- `sendConfirmationRequest()` notifies all required opponents; after partial confirmation, notify remaining opponents
- `src/main/java/com/tictactore/model/User.java` -- user entity for recipient lookups
- `NEW` `src/main/java/com/tictactore/rules/VerificationRules.java` -- stateless evaluator: `getRequiredConfirmations(Match)`, `supportsPartialConfirmation(Match)`, `isFullyConfirmed(Match)`
- `src/main/java/com/tictactore/dto/MatchResponse.java` -- needs `entryMode`, `matchFormat`, `confirmedByOpponentIds`, `requiredConfirmations`
- `src/main/java/com/tictactore/dto/CreateMatchRequest.java` -- needs `entryMode` (optional, inferred if absent)
- `src/main/java/com/tictactore/repository/MatchRepository.java` -- needs query for PARTIALLY_CONFIRMED matches

**Database:**
- `NEW` `src/main/resources/db/migration/V7__add_context_aware_verification_fields.sql` -- add `entry_mode`, `match_format`, `confirmed_by_opponent_ids` columns to `match` table

**Tests:**
- `src/test/java/com/tictactore/service/MatchServiceTest.java` -- update confirmation tests for new fields + add context-aware tests
- `src/test/java/com/tictactore/service/MatchConfirmationATDDTest.java` -- add ATDD specs for multi-confirmation rules
- `NEW` `src/test/java/com/tictactore/rules/VerificationRulesTest.java` -- unit tests for rules engine
- `src/test/java/com/tictactore/controller/MatchConfirmationControllerATDDTest.java` -- add partial confirmation response tests
- `src/test/java/com/tictactore/controller/MatchControllerTest.java` -- update confirmation response assertions

**Frontend:**
- `frontend/src/features/match/composables/usePendingMatches.ts` -- handle PARTIALLY_CONFIRMED in fetch/count logic
- `frontend/src/features/match/components/PendingMatches.vue` -- display confirmation progress (X of N confirmed)

## Tasks & Acceptance

**Execution:**
- [x] `src/main/java/com/tictactore/model/Match.java` -- Add `entryMode`, `matchFormat`, `confirmedByOpponentIds` fields; add `STATUS_PARTIALLY_CONFIRMED`, `ENTRY_MODE_PARTICIPANT`/`ENTRY_MODE_REFEREE`, `MATCH_FORMAT_STANDARD`/`MATCH_FORMAT_RANDOM` constants; update `confirmByOpponent()` to call `addConfirmation()` + `VerificationRules.isFullyConfirmed()`; add `hasConfirmed()`, `getConfirmedByOpponentCount()`, `addConfirmation()` helper methods
- [x] `src/main/java/com/tictactore/rules/VerificationRules.java` -- Create rules engine: `getRequiredConfirmations(Match)` returns 1 for 1v1-participant, 2 for 1v1-referee, 2 for 2v2-referee (1 per team), 2 for 2v2-standard/random (both opponents); `supportsPartialConfirmation(Match)` returns true only for 2v2-standard participant-entered
- [x] `src/main/java/com/tictactore/service/operation/MatchOperation.java` -- No signature changes; `confirmMatch()` delegates to updated `Match.confirmByOpponent()`
- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` -- Update `confirmMatch()` to handle PARTIALLY_CONFIRMED state and `hasConfirmed()` idempotency; update `getPendingMatches()` to include PARTIALLY_CONFIRMED; update `isUserPendingApprover()` to exclude already-confirmed users
- [x] `src/main/java/com/tictactore/dto/CreateMatchRequest.java` -- Add optional `entryMode` field (inferred as PARTICIPANT if absent or creatorId is a participant)
- [x] `src/main/java/com/tictactore/dto/MatchResponse.java` -- Add `entryMode`, `matchFormat`, `confirmedByOpponentIds`, `requiredConfirmations` fields to DTO
- [x] `src/main/java/com/tictactore/repository/MatchRepository.java` -- Add `findByStatusIn` for PENDING_APPROVAL and PARTIALLY_CONFIRMED
- [x] `src/main/java/com/tictactore/service/PushNotificationService.java` + `impl/PushNotificationServiceImpl.java` -- After first partial confirmation, send reminder notification to remaining required opponents
- [x] `src/main/resources/db/migration/V7__add_context_aware_verification_fields.sql` -- Add new columns to match table
- [x] `src/test/java/com/tictactore/rules/VerificationRulesTest.java` -- Unit tests for all 5 contexts
- [x] `src/test/java/com/tictactore/service/MatchServiceTest.java` -- Update existing confirmation tests + add context-aware confirmation tests
- [x] `src/test/java/com/tictactore/service/MatchConfirmationATDDTest.java` -- Add ATDD specs for multi-confirmation rules
- [x] `src/test/java/com/tictactore/controller/MatchConfirmationControllerATDDTest.java` -- No changes needed; controller delegates to service, existing response tests cover JSON serialization of new fields
- [x] `frontend/src/features/match/composables/usePendingMatches.ts` + `__tests__/usePendingMatches.spec.ts` -- Handle PARTIALLY_CONFIRMED status
- [x] `frontend/src/features/match/components/PendingMatches.vue` -- Display confirmation progress

**Acceptance Criteria:**
- AC1: Given a 1v1 participant-entered match, when an opponent confirms, then the match status becomes CONFIRMED (1 opponent sufficient)
- AC2: Given a 1v1 referee-entered match, when one opponent confirms, then the match stays in PENDING_APPROVAL until both participants confirm (2 opponents required)
- AC3: Given a 2v2 standard match, when one opponent confirms, then the match enters PARTIALLY_CONFIRMED status (triggers 24-hour cooldown per Story 3.5) and the remaining opponent is notified
- AC4: Given a 2v2 random match, when both opponents confirm, then the match status becomes CONFIRMED (2 opponents required, no partial state)
- AC5: Given a 2v2 referee-entered match, when 1 opponent from each team confirms, then the match status becomes CONFIRMED (1 per team required)
- AC6: Given any match, when the same opponent attempts to confirm twice, then the system returns the current state without error (idempotency)
- AC7: Given a match in PARTIALLY_CONFIRMED, when the remaining opponent confirms, then the match status becomes CONFIRMED

## Spec Change Log

<!-- Append-only. Populated by step-04 during review loops. -->
- 2026-08-06T14:35Z: Implementation complete. Backend 190 tests pass. Frontend 147 unit tests + 30 relevant E2E tests pass. Fixed bug in `VerificationRules.isFullyConfirmed` where `PENDING_APPROVAL` status caused early return false before checking confirmation counts. Fixed `MatchResponse` 12-arg convenience constructor arg count mismatch.

## Design Notes

### Match Context Inference
- Match type (1v1/2v2) is inferred from `teamADefenderId`: null = 1v1, set = 2v2
- Entry mode (PARTICIPANT/REFEREE) is inferred from whether `creatorId` matches any player position
- Match format (STANDARD/RANDOM) defaults to STANDARD; RANDOM reserved for tournament pairings (Epic 8)

### Confirmation Rules Summary
| Context | Entry Mode | Required Confirmations | Partial State |
|---------|-----------|----------------------|---------------|
| 1v1 | PARTICIPANT | 1 | No |
| 1v1 | REFEREE | 2 (both) | No |
| 2v2 | PARTICIPANT (STANDARD) | 2 (both opponents) | Yes (1 → PARTIALLY_CONFIRMED) |
| 2v2 | PARTICIPANT (RANDOM) | 2 (both opponents) | No |
| 2v2 | REFEREE | 2 (1 per team) | No |

### Backward Compatibility
- `hasConfirmed(UUID)` falls back to checking `confirmedByUserId` for data created before this change
- Existing 1v1 participant-entered tests remain valid (1 opponent → CONFIRMED immediately)
- Match entity `@Builder` includes new fields; existing builders omit them (defaults: entryMode=PARTICIPANT, matchFormat=STANDARD)

## Verification

**Commands:**
- `./mvnw test` -- expected: all existing + new tests pass
- `npm run type-check` (frontend) -- expected: 0 errors
- `npm run test:unit -- --run` (frontend) -- expected: all tests pass
- `./scripts/ci-local.sh` -- expected: all checks pass

## Auto Run Result

Status: done

All acceptance criteria (AC1-AC7) verified. Backend 190 tests pass via `./mvnw clean verify`. Frontend 147 unit tests, type-check, and production build pass. 30 relevant E2E tests (match confirmation, push, rejection) pass.

Key implementation artifacts:
- `VerificationRules.java` — context-aware rules engine
- `Match.java` — `confirmByOpponent()` now accumulates confirmations via `addConfirmation()` instead of immediately transitioning to CONFIRMED
- `MatchServiceImpl.java` — `PARTIALLY_CONFIRMED` state handling, partial confirmation notifications, `hasConfirmed()` idempotency
- `V7__add_context_aware_verification_fields.sql` — new columns: `entry_mode`, `match_format`, `confirmed_by_opponent_ids`
- `PendingMatches.vue` — partial confirmation progress badge ("X of N confirmed")
- `usePendingMatches.ts` — `partiallyConfirmedMatches` tracking, `confirmOpponent()` function

Bugs fixed during review:
- `VerificationRules.isFullyConfirmed` incorrectly returned false for `PENDING_APPROVAL` before checking confirmation counts (changed to check `REJECTED` instead)
- `MatchResponse` 12-arg constructor had 2 extra null arguments
- ATDD test files had 4 extra null args for `CreateMatchRequest` constructor