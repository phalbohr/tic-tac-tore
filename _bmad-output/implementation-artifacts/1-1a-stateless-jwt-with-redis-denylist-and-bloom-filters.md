# Story 1.1a: Stateless JWT with Redis Denylist & Bloom Filters

Status: ready-for-dev

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

## Tasks / Subtasks

- [ ] Task 1: Infrastructure Setup
  - [ ] Add `spring-boot-starter-data-redis` to `pom.xml`
  - [ ] Add Redisson dependency for distributed Bloom filter support (`RBloomFilter`)
  - [ ] Configure Redis connection in `application.yml`
- [ ] Task 2: Implement Token Revocation Service
  - [ ] Create `TokenRevocationService` interface and Redis implementation
  - [ ] Implement `revoke(String token)` method (adds to `RBloomFilter` and Redis with TTL)
  - [ ] Implement `isRevoked(String token)` method (Bloom filter check -> Redis check)
  - [ ] Ensure TTL for both Redis keys and Bloom filter entries is synchronized with JWT expiration (24h)
- [ ] Task 3: Security Filter Integration
  - [ ] Update `JwtAuthenticationFilter` to inject `TokenRevocationService`
  - [ ] Perform revocation check after JWT signature verification but before setting security context
  - [ ] **Error Handling:** Implement **fail-closed** logic — if Redis is down, reject the request as unauthorized
- [ ] Task 4: API Endpoints
  - [ ] Create `AuthController` (if not exists) or update existing
  - [ ] Implement `/api/auth/logout` endpoint that extracts the token from cookie and revokes it
- [ ] Task 5: Verification & Testing
  - [ ] Unit tests for `TokenRevocationService` with mocked Redis/Bloom filter
  - [ ] Integration test for `JwtAuthenticationFilter` verifying access is denied for revoked tokens
  - [ ] Simulation test: Verify authentication is rejected when Redis connection is lost (fail-closed)
  - [ ] Update frontend to call logout endpoint when user chooses to log out

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

### Debug Log References

N/A

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created
- Applied validation recommendations: added fail-closed logic, RBloomFilter specifics, and TTL sync requirements.

### File List

- _bmad-output/implementation-artifacts/1-1a-stateless-jwt-with-redis-denylist-and-bloom-filters.md (modified)
