## Deferred from: code review of 1-1-project-initialization-and-authentication-via-google-oauth2 (2026-05-02)
- Security: JWT Leaked in URL - Use HttpOnly cookies instead of URL parameters.
- Performance: Database Exhaustion in JWT Filter - Remove the DB lookup, rely on JWT claims.
- Security: XSS Exposure via LocalStorage - Switch from LocalStorage to HttpOnly cookies.
- Security: Account Takeover via Email Collision - Require email verification or distinct provider mapping.
- Missing Redis-based denylist with Bloom filters - deferring to a separate story to avoid scope creep for basic auth.
