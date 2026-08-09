---
story: 3-6-submission-rate-limiting-anti-spam
status: done
generated: '2026-08-07T10:58:00+02:00'
---

# Implementation Checklist: Story 3.6 - Submission Rate Limiting (Anti-Spam)

## Scope

This checklist covers all code changes currently present in the working tree for Story 3.6.

## Backend Production Code

- [x] `src/main/java/com/tictactore/config/ApplicationProperties.java` — Added `RateLimit` nested class with `standaloneSubmissionsPerHour`, `rejectionThreshold`, `rejectionWindowHours`, `tournamentSubmissionsPerHour` fields and sensible defaults
- [x] `src/main/java/com/tictactore/exception/RateLimitExceededException.java` — New exception carrying `retryAfterSeconds` and optional cause; `redisFailure` flag for 503 semantics
- [x] `src/main/java/com/tictactore/exception/ApiError.java` — New record `ApiError(String code, String message, Map<String, Object> details)` for standardized error responses
- [x] `src/main/java/com/tictactore/service/RateLimitService.java` — New interface with `checkSubmissionLimit(UUID userId)` and `recordRejection(UUID userId)`
- [x] `src/main/java/com/tictactore/service/impl/RateLimitServiceImpl.java` — Implemented using Redisson `RAtomicLong` for fixed-window submissions and `RScoredSortedSet` for sliding-window rejection tracking; fail-closed on Redis errors
- [x] `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java` — Added `@ExceptionHandler(RateLimitExceededException.class)` returning 429 with standard error object including `details.retryAfter`; maps Redis-failure cause to 503
- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` — Injected `RateLimitService`, call `checkSubmissionLimit` after idempotency check in `createMatch`; call `recordRejection` in `rejectMatch` before returning
- [x] `src/main/resources/application.yml` — Added `application.security.rate-limit` block with `${TTT_RATE_LIMIT_*:default}` env-var fallbacks

## Backend Test Code

- [x] `src/test/java/com/tictactore/service/RateLimitServiceTest.java` — New unit tests with mocked Redisson client for submission counters, rejection sliding window, and Redis failure fail-closed behavior
- [x] `src/test/java/com/tictactore/service/MatchServiceTest.java` — Added integration tests verifying `createMatch` throws `RateLimitExceededException` when submission limit exceeded; verify `rejectMatch` triggers rejection recording
- [x] `src/test/java/com/tictactore/service/MatchServiceATDDTest.java` — Added `@Mock RateLimitService` for `@InjectMocks` constructor injection
- [x] `src/test/java/com/tictactore/service/MatchServiceDuplicateDetectionATDDTest.java` — Added `@Mock RateLimitService` for `@InjectMocks` constructor injection
- [x] `src/test/java/com/tictactore/service/SubmissionRateLimitRedPhaseTest.java` — New red-phase acceptance test scaffolds covering AC1–AC6 (all `@Disabled`)

## Frontend Production Code

- [x] `frontend/src/features/match/stores/matchDraftStore.ts` — In `executeCommit`, detect HTTP 429, set `submitError` with localized rate-limit message including retry time, return `SubmissionResult.CLIENT_ERROR`

## Config / Metadata

- [x] `_bmad-output/implementation-artifacts/sprint-status.yaml` — Marked story 3-6 as `done`
