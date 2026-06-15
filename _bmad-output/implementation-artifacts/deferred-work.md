# Deferred Work log

## Deferred from: code review of 1-1-project-initialization-and-authentication-via-google-oauth2 (2026-05-01)
- [ ] Critical security vulnerabilities, including "JWT Leaked in URL", "XSS Exposure via LocalStorage", and "Account Takeover via Email Collision", are explicitly deferred to a later time. Merging code with known critical security flaws compromises the application and user data. These vulnerabilities must be fixed in the current PR.
- [ ] Database exhaustion in JwtAuthenticationFilter. This filter executes a synchronous database lookup for every single authenticated request. This introduces a massive bottleneck and makes the application trivial to DoS. Statelessness of JWT is defeated.
- [ ] Missing Redis-based denylist with Bloom filters. Violates: Architecture Patterns and Constraints (AD-03: Stateless JWT with Redis Denylist).

## Deferred from: code review of 1-1-project-initialization-and-authentication-via-google-oauth2 (2026-05-02)
- [ ] [Review][Defer] Static role assignment (ROLE_USER only) [src/main/java/com/tictactore/security/JwtAuthenticationFilter.java:46] — deferred, pre-existing architecture limit.
- [ ] [Review][Defer] Missing production CORS config [src/main/java/com/tictactore/config/SecurityConfig.java:27] — deferred, out of scope for initialization.

## Deferred from: code review of 1-1a-stateless-jwt-with-redis-denylist-and-bloom-filters (2026-05-10)
- [ ] [Review][Defer] Consistency: `isRevoked()` checks only today/yesterday Bloom Filters, but `revoke()` writes to all filters until token expiration — tokens revoked >2 days ago will pass as valid if Redis bucket expired. Is a rolling 2-day window acceptable, or must coverage match JWT TTL exactly? [`RedisTokenRevocationService.java`] — deferred, need to investigate.

## Deferred from: code review of 1-2-localization-and-translation-architecture.md (2026-05-15)
- Anonymization verification test logic flaw: TC-P0-004 plans to assert that an anonymized record has no PII via an API call after account deletion. However, account deletion revokes authentication (401), so a client-side E2E test cannot assert state without elevated endpoints.

## Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)
- [ ] [Review][Defer] Missing DB Migration for Non-Nullable Nickname [src/main/java/com/tictactore/model/User.java:672-673] — Nickname column is added as non-nullable, unique, but no DB migration script exists to backfill existing users.
- [ ] Complete API Mocking in E2E Tests [frontend/e2e/profile-generation.spec.ts:409-418] — Playwright E2E tests mock the profile API entirely, reducing integration validation quality.
- Potential Nickname Length Overflow: 64-char email prefix plus UUID can exceed 73 chars.
- Inefficient Nickname Collision Resolution: loop does 10 sync queries on creation.
- Redundant Database Query on Profile Fetch: hits DB to fetch avatar/language when they could be deterministic/client-side.
- Semantic Mismatch in JWT Claims: old 'name' claim might inject spaces into nickname.
- Unused and Unnecessary Versioning: @Version added to User but not utilized.
- Over-engineered Transaction Boundaries: REQUIRES_NEW used without active parent transaction.

## Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)
- [ ] [Review][Defer] Improper Exception Type for Missing User [src/main/java/com/tictactore/service/UserService.java]
- [ ] [Review][Defer] Missing Null Check in generateUniqueNickname [src/main/java/com/tictactore/service/UserService.java]
- [ ] [Review][Defer] High Collision Probability in Nickname Suffix [src/main/java/com/tictactore/service/UserService.java]
- [ ] [Review][Defer] E2E Test Bypasses Backend with Complete API Mocking [frontend/e2e/profile-generation.spec.ts]

## Deferred from: code review of 1-4-profile-management-in-personal-cabinet (2026-05-30)
- [x] [Review][Defer] Unhandled OptimisticLockingFailureException on concurrent updates [UserService.java] — deferred, pre-existing

## Deferred from: code review of 1-6-avatar-selection-and-management.md (2026-06-13)
- [x] [Review][Defer] Shallow copy for rollback might corrupt state [frontend/src/stores/auth.ts] — deferred, pre-existing limitation for flat state, YAGNI.
- [x] [Review][Defer] Nickname passed as empty or whitespace string silently dropped [frontend/src/stores/auth.ts] — deferred, pre-existing
- [x] [Review][Defer] Brittle Optimistic Rollbacks in auth.ts [frontend/src/stores/auth.ts] — deferred, pre-existing limitation for flat state
