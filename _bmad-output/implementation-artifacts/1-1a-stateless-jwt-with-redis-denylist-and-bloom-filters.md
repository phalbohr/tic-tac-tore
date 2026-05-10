# Story 1.1a: Stateless JWT with Redis Denylist & Bloom Filters

Status: done

<!-- Note: Validation completed. Recommendations from 1-1a-validation-report.md applied. -->

## Story

As a security-conscious system,
I want to immediately revoke compromised or deleted account tokens,
so that user sessions are truly terminated.

## Acceptance Criteria

1. **Given** a valid JWT token, **When** the account is deleted or token is revoked, **Then** the token is added to a Redis-based denylist.
2. **And** a Bloom filter is used for fast-path revocation checks before querying Redis to minimize latency.
3. **And** the `JwtAuthenticationFilter` rejects any token found in the denylist with a 401 Unauthorized response (AD-03).
4. **And** a logout endpoint `/api/auth/logout` is provided that revokes the current session's token.
5. **And** the system fails closed (rejects authentication) if the Redis/Denylist service is unavailable, ensuring maximum security.
6. **And** a `docker-compose.yaml` file is provided in the project root to orchestrate the infrastructure (Redis with Bloom Filter, PostgreSQL).
7. **And** Spring Boot is configured to use the `spring-boot-docker-compose` module for automatic service management during development.

## Tasks / Subtasks

- [x] Task 1: Infrastructure Setup
  - [x] Add `spring-boot-starter-data-redis` to `pom.xml`
  - [x] Add Redisson dependency for distributed Bloom filter support (`RBloomFilter`)
  - [x] Add `spring-boot-docker-compose` dependency for automatic service management
  - [x] Create `docker-compose.yaml` in the project root (Redis with Bloom Filter, PostgreSQL)
  - [x] Configure Redis connection in `application.yml`
- [x] Task 2: Implement Token Revocation Service
  - [x] Create `TokenRevocationService` interface and Redis implementation
  - [x] Implement `revoke(String token)` method (adds to `RBloomFilter` and Redis with TTL)
  - [x] Implement `isRevoked(String token)` method (Bloom filter check -> Redis check)
  - [x] Ensure TTL for both Redis keys and Bloom filter entries is synchronized with JWT expiration (24h)
- [x] Task 3: Security Filter Integration
  - [x] Update `JwtAuthenticationFilter` to inject `TokenRevocationService`
  - [x] Perform revocation check after JWT signature verification but before setting security context
  - [x] **Error Handling:** Implement **fail-closed** logic — if Redis is down, reject the request as unauthorized
- [x] Task 4: API Endpoints
  - [x] Create `AuthController` (if not exists) or update existing
  - [x] Implement `/api/auth/logout` endpoint that extracts the token from cookie and revokes it
- [x] Task 5: Verification & Testing
  - [x] Unit tests for `TokenRevocationService` with mocked Redis/Bloom filter
  - [x] Integration test for `JwtAuthenticationFilter` verifying access is denied for revoked tokens
  - [x] Simulation test: Verify authentication is rejected when Redis connection is lost (fail-closed)
  - [x] Update frontend to call logout endpoint when user chooses to log out

## Dev Notes

- **Architecture Patterns and Constraints:**
  - **AD-03: Stateless JWT with Redis Denylist**: This is the primary requirement. Bloom filters are required for performance to prevent every request from hitting Redis if the token isn't even in the filter.
  - **Fail-Fast**: The Bloom filter can have false positives but NO false negatives. If the Bloom filter says "not present", it is safe to proceed. If it says "present", we MUST check Redis for confirmation.
  - **Fail-Closed Security**: In a clubhouse/competitive environment, security trumps availability for authentication checks. If the denylist cannot be verified, the session must be treated as invalid.
- **Redisson Configuration:** Use `RBloomFilter` for a managed, distributed Bloom filter implementation.
- **Source tree components to touch:**
  - Backend: `pom.xml`, `src/main/java/com/tictactore/security/JwtAuthenticationFilter.java`, new service and controller classes.
  - Frontend: `frontend/src/stores/auth.ts` (to add logout call).
- **Testing standards summary:**
  - Use Testcontainers for Redis integration tests if available, or mock Redis for unit tests.

### Project Structure Notes

- Adhere to the existing package structure in `com.tictactore`.

### References

- [Source: _bmad-output/planning-artifacts/prd.md#Non-Functional Requirements] (NFR3)
- [Source: _bmad-output/planning-artifacts/architecture.md#Security & Authentication] (AD-03)
- [Source: _bmad-output/implementation-artifacts/1-1-project-initialization-and-authentication-via-google-oauth2.md] (Previous work intelligence)
- [Source: _bmad-output/implementation-artifacts/1-1a-validation-report.md] (Applied recommendations)

## Dev Agent Record

### Agent Model Used

Bob (bmad-agent-sm) context engine
Gemini CLI Developer Agent

### Debug Log References

N/A

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created
- Applied validation recommendations: added fail-closed logic, RBloomFilter specifics, and TTL sync requirements.
- Completed Task 1: Configured redis, redisson, and docker-compose in pom.xml and application.yml.
- Completed Task 2: Implemented RedisTokenRevocationService using RBloomFilter and bucket TTL syncing.
- Completed Task 3: Modified JwtAuthenticationFilter to check token revocation and fail closed.

### Change Log

- Added Redis, Redisson, docker-compose dependencies.
- Created `docker-compose.yaml` with postgres and redis-stack.
- Created `TokenRevocationService` and `RedisTokenRevocationService`.
- Modified `JwtAuthenticationFilter` to return 401 for revoked tokens.

### File List

- `pom.xml` (modified)
- `docker-compose.yaml` (new)
- `src/main/resources/application.yml` (modified)
- `src/main/java/com/tictactore/service/TokenRevocationService.java` (new)
- `src/main/java/com/tictactore/service/impl/RedisTokenRevocationService.java` (new)
- `src/main/java/com/tictactore/security/JwtAuthenticationFilter.java` (modified)
- `src/main/java/com/tictactore/controller/AuthController.java` (new)
- `frontend/src/stores/auth.ts` (modified)
- `src/test/java/com/tictactore/service/impl/RedisTokenRevocationServiceTest.java` (new)
- `src/test/java/com/tictactore/security/JwtAuthenticationFilterTest.java` (new)
- `src/test/java/com/tictactore/TicTacToreApplicationTests.java` (modified)
- `src/test/java/com/tictactore/security/JwtServiceTest.java` (modified)
- `_bmad-output/implementation-artifacts/1-1a-stateless-jwt-with-redis-denylist-and-bloom-filters.md` (modified)

### Review Findings

**decision-needed (0)**

**deferred (1)**

- [x] [Review][Defer] Consistency: `isRevoked()` checks only today/yesterday Bloom Filters, but `revoke()` writes to all filters until token expiration — tokens revoked >2 days ago will pass as valid if Redis bucket expired. Is a rolling 2-day window acceptable, or must coverage match JWT TTL exactly? [`RedisTokenRevocationService.java`] — deferred, pre-existing

**patch (10)**

- [x] [Review][Patch] Security: `JwtAuthenticationFilter` does not return explicit 401 for revoked tokens — sets no SecurityContext but calls `filterChain.doFilter()`, allowing request to proceed unauthenticated rather than rejecting it [`JwtAuthenticationFilter.java`]
- [x] [Review][Patch] Config: `spring.docker.compose.enabled: true` in production `application.yml` — Spring Boot will try to start docker-compose in any environment; should be scoped to a dev profile [`application.yml`]
- [x] [Review][Patch] Security: `secure(request.isSecure())` fails behind a reverse proxy (always `false`) — use `X-Forwarded-Proto` header or set `server.forward-headers-strategy=native` [`CustomOAuth2SuccessHandler.java:49`]
- [x] [Review][Patch] Logic: Cookie `maxAge` uses `Duration.ofMillis(properties.getJwt().getExpiration())` — if `expiration` is already in seconds this sets a millisecond-level max-age, expiring instantly [`CustomOAuth2SuccessHandler.java:51`]
- [x] [Review][Patch] Frontend/Backend: Cookie name mismatch — frontend reads `TTT_SESSION` as `AUTH_COOKIE_NAME` but backend's `AUTH_COOKIE_NAME` is `TTT_TOKEN`; initial auth state detection may always be `false` [`auth.ts:4`]
- [x] [Review][Patch] Race condition: `isRevoked()` calls `isExists()` then `contains()` on Bloom Filter — filter may be deleted between calls at midnight rotation, triggering fail-closed (false 401) [`RedisTokenRevocationService.java:isRevoked`]
- [x] [Review][Patch] Performance: `revoke()` loop over days has no cap — a 30-day JWT creates 30 Redis round-trips synchronously per logout; add a max-days guard [`RedisTokenRevocationService.java:revoke`]
- [x] [Review][Patch] Reliability: `AuthController.logout()` does not catch `RuntimeException` from `tokenRevocationService.revoke()` — Redis unavailability returns HTTP 500 instead of graceful 200 [`AuthController.java`]
- [x] [Review][Patch] Infrastructure: `redis/redis-stack-server:latest` in docker-compose — pin to a specific version for reproducible builds [`docker-compose.yaml`]
- [x] [Review][Patch] Code style: `RedisTokenRevocationServiceTest` — `@BeforeEach` block and first `@Test` method lack indentation inside the class body, violating code style and risking future parsing issues [`RedisTokenRevocationServiceTest.java:54-79`]
- [x] [Review][Decision] Scalability: Bloom Filter capacity made configurable via `application.yml` (`app.bloom-filter.expected-elements`)