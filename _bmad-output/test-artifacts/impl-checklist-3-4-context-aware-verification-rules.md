---
storyKey: 3-4-context-aware-verification-rules
storyId: '3.4'
lastSaved: '2026-08-06T14:54:00+02:00'
status: complete
---

# Implementation Checklist: Story 3.4 - Context-Aware Verification Rules

## Working Tree Changes Summary

This checklist documents all production and test code changes currently in the working tree for Story 3.4.

---

## 1. Backend Production Code Changes

### 1.1 Domain Model

| File | Change | Description |
|---|---|---|
| `src/main/java/com/tictactore/model/Match.java` | Modified | Added `entryMode`, `matchFormat`, `confirmedByOpponentIds` fields; added `STATUS_PARTIALLY_CONFIRMED`; updated `confirmByOpponent()` to use `addConfirmation()` + `VerificationRules.isFullyConfirmed()`; added `hasConfirmed()`, `getConfirmedByOpponentCount()`, `addConfirmation()`, `getConfirmedByOpponentIdsList()` helpers |

### 1.2 Rules Engine (New)

| File | Change | Description |
|---|---|---|
| `src/main/java/com/tictactore/rules/VerificationRules.java` | New | Stateless evaluator with `getRequiredConfirmations(Match)`, `supportsPartialConfirmation(Match)`, `isFullyConfirmed(Match)`, `isPartiallyConfirmed(Match)` |

### 1.3 Service Layer

| File | Change | Description |
|---|---|---|
| `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` | Modified | `confirmMatch()` handles PARTIALLY_CONFIRMED state and `hasConfirmed()` idempotency; `getPendingMatches()` includes PARTIALLY_CONFIRMED; `isUserPendingApprover()` excludes already-confirmed users; `mapToResponseWithUserMap()` populates new DTO fields (`entryMode`, `matchFormat`, `confirmedByOpponentIds`, `requiredConfirmations`); `createMatch()` infers `entryMode` and `matchFormat` |

### 1.4 DTOs

| File | Change | Description |
|---|---|---|
| `src/main/java/com/tictactore/dto/CreateMatchRequest.java` | Modified | Added optional `entryMode` and `matchFormat` record components |
| `src/main/java/com/tictactore/dto/MatchResponse.java` | Modified | Added `entryMode`, `matchFormat`, `confirmedByOpponentIds`, `requiredConfirmations` record components; updated convenience constructors |

### 1.5 Repository

| File | Change | Description |
|---|---|---|
| `src/main/java/com/tictactore/repository/MatchRepository.java` | Modified | Added `findByStatusIn(List<String>)` query method for PENDING_APPROVAL and PARTIALLY_CONFIRMED |

### 1.6 Push Notifications

| File | Change | Description |
|---|---|---|
| `src/main/java/com/tictactore/service/PushNotificationService.java` | Modified | Added `sendPartialConfirmationNotification(Match, List<User>, String)` method signature |
| `src/main/java/com/tictactore/service/impl/PushNotificationServiceImpl.java` | Modified | Implemented partial confirmation notification dispatch to remaining required opponents |

### 1.7 Database Migration

| File | Change | Description |
|---|---|---|
| `src/main/resources/db/migration/V7__add_context_aware_verification_fields.sql` | New | Adds `entry_mode`, `match_format`, `confirmed_by_opponent_ids` columns to `match` table (nullable, additive migration) |

---

## 2. Backend Test Code Changes

### 2.1 New Test Files

| File | Description |
|---|---|
| `src/test/java/com/tictactore/rules/VerificationRulesTest.java` | Unit tests for all 5 verification contexts (1v1 participant, 1v1 referee, 2v2 standard, 2v2 random, 2v2 referee) |

### 2.2 Modified Test Files

| File | Change | Description |
|---|---|---|
| `src/test/java/com/tictactore/service/MatchConfirmationATDDTest.java` | Added `ContextAwareConfirmationSpecs` nested class with AC1–AC7 tests |
| `src/test/java/com/tictactore/service/MatchServiceTest.java` | Added context-aware confirmation tests (idempotency, partial confirmation flow, PARTIALLY_CONFIRMED in pending matches); updated `getPendingMatches` mocks to use `findByStatusIn`; updated `CreateMatchRequest` constructor calls with `null, null` for new fields |
| `src/test/java/com/tictactore/controller/MatchControllerTest.java` | Updated `CreateMatchRequest` constructor calls with `null, null` for new fields |
| `src/test/java/com/tictactore/controller/MatchControllerATDDTest.java` | Updated `CreateMatchRequest` constructor calls with `null, null` for new fields |
| `src/test/java/com/tictactore/service/MatchServiceDuplicateDetectionATDDTest.java` | Updated `CreateMatchRequest` constructor calls with `null, null` for new fields |

---

## 3. Frontend Production Code Changes

| File | Change | Description |
|---|---|---|
| `frontend/src/features/match/composables/usePendingMatches.ts` | Modified | Added `partiallyConfirmedMatches` ref; `confirmOpponent()` function; PARTIALLY_CONFIRMED count tracking |
| `frontend/src/features/match/components/PendingMatches.vue` | Modified | Added PARTIALLY_CONFIRMED badge rendering ("X of N confirmed") |

---

## 4. Frontend Test Code Changes

| File | Change | Description |
|---|---|---|
| `frontend/src/features/match/composables/usePendingMatches.spec.ts` | Modified | Added tests for `partiallyConfirmedMatches` population, `confirmOpponent()` idempotency |
| `frontend/src/features/match/components/__tests__/PendingMatches.spec.ts` | Modified | Added test for PARTIALLY_CONFIRMED badge rendering with `requiredConfirmations` |

---

## 5. Verification Status

| Check | Command | Expected | Actual |
|---|---|---|---|
| Backend unit tests | `./mvnw test` | 190 tests pass | ✅ 190 tests pass |
| Frontend unit tests | `npm run test:unit -- --run` | 147 tests pass | ✅ 147 tests pass |
| Frontend type-check | `npm run type-check` | 0 errors | ✅ 0 errors |

---

## 6. Implementation Notes

- **Backward compatibility**: `hasConfirmed(UUID)` falls back to checking `confirmedByUserId` for data created before this change
- **Existing 1v1 flow preserved**: 1v1 participant-entered matches still transition to CONFIRMED immediately (1 opponent sufficient)
- **Database migration**: V7 is additive (nullable columns), safe to run on existing data
- **Three-Layer Transaction Architecture**: Maintained — `MatchServiceImpl` stays `@Retryable` ONLY; `MatchOperation` stays `@Idempotent` + `@Transactional`
