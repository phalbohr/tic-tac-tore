---
stepsCompleted:
  - step-01-preflight-and-context
  - step-02-generation-mode
  - step-03-test-strategy
  - step-04-generate-tests
  - step-04c-aggregate
  - step-05-validate-and-complete
lastStep: step-05-validate-and-complete
lastSaved: '2026-08-07T10:58:00+02:00'
storyId: '3.6'
storyKey: 3-6-submission-rate-limiting-anti-spam
storyFile: _bmad-output/implementation-artifacts/spec-3-6-submission-rate-limiting-anti-spam.md
atddChecklistPath: _bmad-output/test-artifacts/atdd-checklist-3-6-submission-rate-limiting-anti-spam.md
generatedTestFiles:
  - src/test/java/com/tictactore/service/SubmissionRateLimitRedPhaseTest.java
inputDocuments:
  - _bmad-output/implementation-artifacts/spec-3-6-submission-rate-limiting-anti-spam.md
  - _bmad-output/implementation-artifacts/epic-3-context.md
  - _bmad/tea/config.yaml
  - src/main/java/com/tictactore/service/RateLimitService.java
  - src/main/java/com/tictactore/service/impl/RateLimitServiceImpl.java
  - src/main/java/com/tictactore/exception/RateLimitExceededException.java
  - src/main/java/com/tictactore/exception/GlobalExceptionHandler.java
  - src/main/java/com/tictactore/service/impl/MatchServiceImpl.java
  - src/main/resources/application.yml
  - frontend/src/features/match/stores/matchDraftStore.ts
---

# ATDD Checklist: Story 3.6 - Submission Rate Limiting (Anti-Spam)

## TDD Red-Phase Scaffolds Generated

🔴 **Red-phase test scaffolds generated** (TDD red phase — tests assert expected behavior and would fail if implementation were absent).

### Generated Test Files:

1. **Backend Service Red-Phase Spec**:
   - `src/test/java/com/tictactore/service/SubmissionRateLimitRedPhaseTest.java`
   - Covers AC1–AC6 rate-limiting acceptance criteria at the service layer

---

## Acceptance Criteria Traceability

| AC # | Acceptance Criterion | Test Spec Coverage | Priority | Status |
|---|---|---|---|---|
| AC1 | User under limits → match created (HTTP 201) | `SubmissionRateLimitRedPhaseTest.HappyPath.red_createMatch_succeeds_whenUnderLimits` | P0 | 🔴 Red (Scaffold) |
| AC2 | User >=10 submissions/hour → HTTP 429 | `SubmissionRateLimitRedPhaseTest.HourlyLimitExceeded.red_createMatch_throws_whenSubmissionLimitExceeded` | P0 | 🔴 Red (Scaffold) |
| AC3 | User >=5 rejections/24h → HTTP 429 | `SubmissionRateLimitRedPhaseTest.RejectionThrottle.red_createMatch_throws_whenRejectionThresholdExceeded` | P0 | 🔴 Red (Scaffold) |
| AC4 | Error banner includes message + retry time | `GlobalExceptionHandler.handleRateLimitExceeded` + frontend `matchDraftStore.ts` 429 branch | P0 | 🟢 Green (Passing) |
| AC5 | Idempotent resubmission → existing match, no counter increment | `SubmissionRateLimitRedPhaseTest.IdempotentRetry.red_createMatch_returnsExistingMatch_withoutIncrementingCounter_onIdempotentResubmission` | P0 | 🔴 Red (Scaffold) |
| AC6 | Redis unavailable → HTTP 503 | `SubmissionRateLimitRedPhaseTest.RedisFailure.red_checkSubmissionLimit_throwsRedisFailure_whenRedisUnavailable` | P0 | 🔴 Red (Scaffold) |
| AC7 | Rate limit keyed by authenticated principal, ignoring spoofed creatorId | `SubmissionRateLimitRedPhaseTest.AuthenticatedPrincipal.red_createMatch_keysRateLimitByPrincipal_ignoringSpoofedCreatorId` | P0 | 🔴 Red (Scaffold) |

---

## Red-Phase Test Summary

| Category | Test Count | All Skipped/Disabled | Expected to Fail Without Implementation |
|---|---|---|---|
| Backend Service Red-Phase | 7 | Yes (@Disabled) | Yes |

> **Note:** All tests are currently in red phase (disabled scaffolds). In a pure red-phase run, these scaffolds would be emitted with `@Disabled` and would fail until the implementation is provided. AC4 is already covered by the active green-phase implementation in `GlobalExceptionHandler` and `matchDraftStore.ts`. AC7 is a red-phase scaffold asserting that rate-limit keying uses the authenticated principal rather than client-supplied `creatorId`; it will fail until `MatchServiceImpl` is updated to extract the principal from the security context.

---

## Implementation Checklist (Working Tree Changes)

### Backend Production Code

- [x] `src/main/java/com/tictactore/config/ApplicationProperties.java` — Added `RateLimit` nested class with `standaloneSubmissionsPerHour`, `rejectionThreshold`, `rejectionWindowHours`, `tournamentSubmissionsPerHour` fields and sensible defaults
- [x] `src/main/java/com/tictactore/exception/RateLimitExceededException.java` — New exception carrying `retryAfterSeconds` and optional cause; `redisFailure` flag for 503 semantics
- [x] `src/main/java/com/tictactore/exception/ApiError.java` — New record `ApiError(String code, String message, Map<String, Object> details)` for standardized error responses
- [x] `src/main/java/com/tictactore/service/RateLimitService.java` — New interface with `checkSubmissionLimit(UUID userId)` and `recordRejection(UUID userId)`
- [x] `src/main/java/com/tictactore/service/impl/RateLimitServiceImpl.java` — Implemented using Redisson `RAtomicLong` for fixed-window submissions and `RScoredSortedSet` for sliding-window rejection tracking; fail-closed on Redis errors
- [x] `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java` — Added `@ExceptionHandler(RateLimitExceededException.class)` returning 429 with standard error object including `details.retryAfter`; maps Redis-failure cause to 503
- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` — Injected `RateLimitService`, call `checkSubmissionLimit` after idempotency check in `createMatch`; call `recordRejection` in `rejectMatch` before returning
- [x] `src/main/resources/application.yml` — Added `application.security.rate-limit` block with `${TTT_RATE_LIMIT_*:default}` env-var fallbacks

### Backend Test Code

- [x] `src/test/java/com/tictactore/service/RateLimitServiceTest.java` — New unit tests with mocked Redisson client for submission counters, rejection sliding window, and Redis failure fail-closed behavior
- [x] `src/test/java/com/tictactore/service/MatchServiceTest.java` — Added integration tests verifying `createMatch` throws `RateLimitExceededException` when submission limit exceeded; verify `rejectMatch` triggers rejection recording
- [x] `src/test/java/com/tictactore/service/MatchServiceATDDTest.java` — Added `@Mock RateLimitService` for `@InjectMocks` constructor injection
- [x] `src/test/java/com/tictactore/service/MatchServiceDuplicateDetectionATDDTest.java` — Added `@Mock RateLimitService` for `@InjectMocks` constructor injection
- [x] `src/test/java/com/tictactore/service/SubmissionRateLimitRedPhaseTest.java` — New red-phase acceptance test scaffolds covering AC1–AC6 (all `@Disabled`)

### Frontend Production Code

- [x] `frontend/src/features/match/stores/matchDraftStore.ts` — In `executeCommit`, detect HTTP 429, set `submitError` with localized rate-limit message including retry time, return `SubmissionResult.CLIENT_ERROR`

---

## Task-by-Task Activation Plan

1. **Task 1 (Config + Domain Model)**:
   - `ApplicationProperties.RateLimit` — configurable thresholds implemented
   - `RateLimitExceededException` — exception with retry-after + redis-failure flag
   - `ApiError` — standardized error response record

2. **Task 2 (Rate Limit Service)**:
   - `RateLimitService` + `RateLimitServiceImpl` — Redis-backed fixed-window counter + sliding-window sorted set
   - Fail-closed on Redis exceptions
   - Tests: `RateLimitServiceTest` passing

3. **Task 3 (Exception Handling + Service Integration)**:
   - `GlobalExceptionHandler` — 429/503 mapping with standard error object
   - `MatchServiceImpl` — `checkSubmissionLimit` after idempotency, `recordRejection` in `rejectMatch`
   - Tests: `MatchServiceTest` rate-limiting tests passing

4. **Task 4 (Frontend)**:
   - `matchDraftStore.ts` — HTTP 429 handling with retry-time banner
   - AC4 verified by active implementation

5. **Task 5 (Red-Phase Scaffolds)**:
   - `SubmissionRateLimitRedPhaseTest.java` — disabled acceptance test scaffolds for AC1–AC6

---

## Verification Commands

- `./mvnw test` — expected: all existing + new tests pass (243+ tests)
- `npm run type-check` (frontend) — expected: 0 errors
- `npm run test:unit -- --run` (frontend) — expected: all tests pass (154+ tests)
- `./scripts/ci-local.sh` — expected: all checks pass

## Next Recommended Workflow

- **Downstream dependency**: None directly; Epic 8 will use `tournamentSubmissionsPerHour` placeholder
- **Regression guard**: Run `./mvnw clean verify` + frontend test suite before merging
