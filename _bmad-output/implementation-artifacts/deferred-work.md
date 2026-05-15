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
