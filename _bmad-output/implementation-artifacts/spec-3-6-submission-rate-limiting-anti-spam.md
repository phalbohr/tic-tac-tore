---
title: 'Story 3.6: Submission Rate Limiting (Anti-Spam)'
type: 'feature'
created: '2026-08-07T00:05:00Z'
status: done
review_loop_iteration: 0
followup_review_recommended: false
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-3-context.md'
warnings: []
baseline_revision: '7b4f92f26b5e6e61b10567ae1edd4c874ae6f839'
final_revision: 'f490f0fc48c21e8c4efc55df4c626babb90ccb3d'
---

<intent-contract>

## Intent

**Problem:** The match submission endpoint has no rate limiting, allowing a single user to spam the platform with unlimited match submissions. This undermines data quality, wastes push notification resources, and can abuse the confirmation pipeline.

**Approach:** Add server-side rate limiting using Redis counters on the match submission endpoint. Track per-user submission frequency (max 10/hour) and rejection behavior (5+ rejections in 24h triggers throttle). Return HTTP 429 when limits are exceeded. Update the frontend to display a rate-limit error banner using the existing `ErrorToast` component.

## Boundaries & Constraints

**Always:**
- All rate-limit state is stored in Redis; no database schema changes required.
- Thresholds are configurable via `application.yml` with environment-variable fallbacks using `${VAR:default}` syntax.
- Rate-limit checks run server-side before the match is persisted.
- Error responses use the project's standard error object: `{ "code": "ERROR_CODE", "message": "Human readable", "details": {} }`.
- Rejection tracking is updated atomically when a match transitions to `REJECTED`.
- Rate limiting must enforce per-user limits using the authenticated principal's identity (do not trust client-supplied creatorId).

**Block If:**
- Redis is unavailable and the application cannot obtain a connection during a rate-limit check.

**Never:**
- Do NOT introduce a new rate-limiting library; use Redisson which is already on the classpath.
- Do NOT modify the existing match state machine or confirmation flow.
- Do NOT hardcode threshold values; always read from configuration.
- Do NOT implement tournament-referee context thresholds (30/hour) in branch logic — Epic 8 is backlog; the property is added as a config placeholder for future use only.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Happy path | User has <10 submissions this hour, <5 rejections in 24h | Match created successfully (HTTP 201) | None |
| Hourly limit exceeded | User has >=10 submissions in current hour | HTTP 429 with `details.retryAfter` in seconds | Frontend shows rate-limit error banner |
| Rejection throttle | User has >=5 rejections in last 24h | HTTP 429 with `details.retryAfter` in seconds | Frontend shows rate-limit error banner |
| Both limits exceeded | User exceeds both thresholds | HTTP 429 with `details.retryAfter` in seconds | Frontend shows rate-limit error banner |
| Redis failure | Redis unavailable during rate-limit check | HTTP 503 with standard error object | Frontend shows server error |
| Idempotent retry | Same idempotency key resubmitted within hour | Return existing match (HTTP 200), do NOT increment counter | None |

</intent-contract>

## Code Map

**Backend:**
- `src/main/java/com/tictactore/controller/MatchController.java` -- no changes; rate-limit exception propagates from service layer
- `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` -- inject `RateLimitService`, call `checkSubmissionLimit` at start of `createMatch` after idempotency check; call `recordRejection` in `rejectMatch` before persisting rejection
- `NEW` `src/main/java/com/tictactore/service/RateLimitService.java` -- interface with `checkSubmissionLimit(UUID userId)` and `recordRejection(UUID userId)`
- `NEW` `src/main/java/com/tictactore/service/impl/RateLimitServiceImpl.java` -- Redis-backed fixed-window counter for submissions, sliding-window sorted set for rejections
- `NEW` `src/main/java/com/tictactore/exception/RateLimitExceededException.java` -- carries `retryAfterSeconds` and optional cause
- `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java` -- add `@ExceptionHandler(RateLimitExceededException.class)` returning 429 or 503 with standard error object
- `src/main/java/com/tictactore/config/ApplicationProperties.java` -- add `RateLimit` nested class with configurable threshold fields
- `src/main/resources/application.yml` -- add `application.security.rate-limit` block with env-var fallbacks

**Frontend:**
- `frontend/src/features/match/stores/matchDraftStore.ts` -- in `executeCommit`, treat HTTP 429 specifically, set `submitError` with rate-limit message, return `SubmissionResult.CLIENT_ERROR` (no new enum value needed)
- `frontend/src/features/match/components/ErrorToast.vue` -- no changes; existing component renders `submitError`
- `frontend/src/views/HomeView.vue` -- no changes; existing `ErrorToast` already bound to `matchStore.submitError`

**Tests:**
- `NEW` `src/test/java/com/tictactore/service/RateLimitServiceTest.java` -- unit tests with mocked Redisson client for submission counters, rejection sliding window, and Redis failure fail-closed behavior
- `src/test/java/com/tictactore/service/MatchServiceTest.java` -- add tests verifying `createMatch` throws `RateLimitExceededException` when limit exceeded; verify `rejectMatch` triggers rejection recording.
- `NEW` `src/test/java/com/tictactore/service/MatchServiceRateLimitTest.java` -- Extract rate-limit group from `MatchServiceTest.java`.
- `frontend/src/features/match/stores/matchDraftStore.spec.ts` -- Split into two files: one for API error handling (429/503) and one for state transitions to keep under 300 lines.
- `frontend/e2e/tests/e2e/rate-limiting.spec.ts` -- DRY up E2E setup via PageObject/helper.

## Tasks & Acceptance

**Execution:**
- [x] `src/main/java/com/tictactore/config/ApplicationProperties.java` -- Add `RateLimit` nested class with `standaloneSubmissionsPerHour`, `rejectionThreshold`, `rejectionWindowHours`, `tournamentSubmissionsPerHour` fields and sensible defaults
- [x] `src/main/resources/application.yml` -- Add `application.security.rate-limit` block with `${TTT_RATE_LIMIT_*:default}` env-var fallbacks
- [x] `src/main/java/com/tictactore/exception/RateLimitExceededException.java` -- Create exception carrying `retryAfterSeconds` and optional cause
- [x] `src/main/java/com/tictactore/service/RateLimitService.java` -- Define `checkSubmissionLimit(UUID userId)` and `recordRejection(UUID userId)` methods
- [x] `src/main/java/com/tictactore/service/impl/RateLimitServiceImpl.java` -- Implement using Redisson `RAtomicLong` for fixed-window submissions and `RScoredSortedSet` for sliding-window rejection tracking
- [x] `src/main/java/com/tictactore/exception/GlobalExceptionHandler.java` -- Add `@ExceptionHandler(RateLimitExceededException.class)` returning 429 with standard error object including `details.retryAfter`; map Redis-failure cause to 503
- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` -- Inject `RateLimitService`, call `checkSubmissionLimit` after idempotency check in `createMatch`; call `recordRejection` in `rejectMatch` before returning
- [x] `frontend/src/features/match/stores/matchDraftStore.ts` -- In `executeCommit`, detect HTTP 429, set `submitError` with localized rate-limit message including retry time, return `SubmissionResult.CLIENT_ERROR`
- [x] `src/test/java/com/tictactore/service/RateLimitServiceTest.java` -- Test submission counter increments and limits; test rejection sliding window count; test Redis failure throws `RateLimitExceededException` with 503 semantics
- [x] `src/test/java/com/tictactore/service/MatchServiceTest.java` -- Add integration tests verifying `createMatch` throws `RateLimitExceededException` when submission limit exceeded; verify `rejectMatch` records rejection and subsequent `createMatch` triggers throttle
- [x] `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java` -- Modify `createMatch` to validate `creatorId` against the authenticated principal (Security NFR fix).
- [x] `src/test/java/com/tictactore/service/MatchServiceRateLimitTest.java` -- Extract rate-limit test group from `MatchServiceTest.java` to improve maintainability.
- [x] `frontend/src/features/match/stores/matchDraftStore.spec.ts` -- Split into API-error + state-transition specs to resolve size violation.
- [x] `frontend/e2e/tests/e2e/rate-limiting.spec.ts` -- Extract shared E2E auth setup into a helper to remove duplication.

**Acceptance Criteria:**
- AC1: Given an authenticated user with fewer than 10 submissions in the current hour and fewer than 5 rejections in the last 24 hours, when they submit a match, then the match is created successfully (HTTP 201)
- AC2: Given an authenticated user with 10 or more submissions in the current hour, when they submit another match, then the backend returns HTTP 429 and the frontend displays a rate-limit error banner
- AC3: Given an authenticated user with 5 or more rejections in the last 24 hours, when they submit a match, then the backend returns HTTP 429 and the frontend displays a rate-limit error banner
- AC4: Given a rate-limit response, when the frontend receives it, then the error banner includes a human-readable message explaining the limit and when the user can retry
- AC5: Given an idempotency-key resubmission within the same hour, when the rate-limit check runs, then the existing match is returned and the submission counter is not incremented
- AC6: Given Redis is unavailable during a rate-limit check, when a user submits a match, then the submission is rejected with HTTP 503 and the frontend displays a server error
- AC7: Given a match submission, when rate limiting is applied, then the limit is keyed by the authenticated principal's ID, ignoring any spoofed creatorId in the request payload.

## Spec Change Log

<!-- Append-only. Populated by step-04 during review loops. Do not modify or delete existing entries. -->
- 2026-08-07: Implementation complete. All backend Java files (RateLimitService, RateLimitServiceImpl, RateLimitExceededException, ApiError), GlobalExceptionHandler handler, MatchServiceImpl integration, ApplicationProperties.RateLimit, and application.yml config block implemented. Frontend 429 handling in matchDraftStore.ts implemented. Unit tests in RateLimitServiceTest (10 tests) and MatchServiceTest (4 rate-limiting tests) passing. All 243 backend tests and 154 frontend tests pass. Fixed YAML indentation in application.yml (avatar key) and added missing @Mock RateLimitService to MatchServiceATDDTest and MatchServiceDuplicateDetectionATDDTest.

## Review Triage Log

<!-- Append-only. Populated by step-04 on EVERY review pass, including loopbacks and blocked exits. -->
- 2026-08-07: Initial review pass. All acceptance criteria addressed: AC1 (happy path) verified by tests; AC2 (hourly limit) implemented via RAtomicLong fixed-window counter with 429 response; AC3 (rejection throttle) implemented via RScoredSortedSet sliding window; AC4 (frontend error banner) implemented in matchDraftStore.ts with retry time; AC5 (idempotency interaction) verified - rate-limit check runs after idempotency check so existing matches don't increment counter; AC6 (Redis failure fail-closed) implemented - RedisException maps to RateLimitExceededException with 503 semantics. Fixed YAML indentation bug in application.yml that caused cascade failures across 41 tests in AuthControllerTest, NotificationControllerTest, UserControllerTest, UserRepositoryTest, JwtServiceTest, MatchCooldownServiceIntegrationTest, MatchServiceATDDTest. Added @Mock RateLimitService to MatchServiceATDDTest and MatchServiceDuplicateDetectionATDDTest for @InjectMocks constructor injection.

## Design Notes

### Rate Limiter Design
- **Submissions:** Fixed-window counter per user per hour. Redis key: `rl:submissions:{userId}:{yyyy-MM-dd-HH}`. Incremented on each `createMatch` call after the idempotency check passes. TTL: 2 hours.
- **Rejections:** Sliding-window sorted set per user. Redis key: `rl:rejections:{userId}`. Each rejection adds a scored entry with the current timestamp. Before counting, entries older than `rejectionWindowHours` are removed. If count >= `rejectionThreshold`, throttle.
- **Fail-closed:** If Redis operations throw, `RateLimitServiceImpl` throws `RateLimitExceededException` with `retryAfterSeconds = 0` and a message indicating temporary unavailability. The exception handler maps this to 503.
- **Idempotency interaction:** The rate-limit check runs AFTER the idempotency check in `MatchServiceImpl.createMatch()`. If an existing match is returned by idempotency key, the submission counter is not incremented.

### Threshold Configuration
All thresholds live under `application.security.rate-limit` in `application.yml` with `${ENV_VAR:default}` fallbacks:
```yaml
rate-limit:
  standalone-submissions-per-hour: ${TTT_RATE_LIMIT_STANDALONE_PER_HOUR:10}
  rejection-threshold: ${TTT_RATE_LIMIT_REJECTION_THRESHOLD:5}
  rejection-window-hours: ${TTT_RATE_LIMIT_REJECTION_WINDOW_HOURS:24}
  tournament-submissions-per-hour: ${TTT_RATE_LIMIT_TOURNAMENT_PER_HOUR:30}
```
The `tournament-submissions-per-hour` property is reserved for Epic 8; no code branch reads it during Epic 3.

## Verification

**Commands:**
- `./mvnw test` -- expected: all existing + new tests pass
- `npm run type-check` (frontend) -- expected: 0 errors
- `npm run test:unit -- --run` (frontend) -- expected: all tests pass
- `./scripts/ci-local.sh` -- expected: all checks pass

**Manual checks (if no CLI):**
- Submit 11 matches rapidly via API; verify 429 on the 11th with `Retry-After` header
- Reject 5 matches, then submit a new one; verify 429
- Inspect Redis keys: `rl:submissions:{userId}:{hour}` and `rl:rejections:{userId}` with expected TTLs

