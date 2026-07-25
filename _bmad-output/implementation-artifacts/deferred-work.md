# Deferred Work log

### DW-1: Follow-up review still recommended for 2-3-score-entry-and-automatic-completion after the damping cap was spent
origin: review-budget-followup
source_spec: `spec-2-3-score-entry-and-automatic-completion.md`
severity: low
reason: The follow-up-review damping cap (limits.max_followup_reviews = 1) was spent with the story finalized (status: done, verify green) while the review pass still recommended an independent follow-up. The work was committed by bmad-loop run 20260717-193102-8fc5; this entry preserves the lingering recommendation for a deliberate later review.
status: done 2026-07-23
resolution: already resolved: The follow-up code review was already performed, resulting in other DW issues being logged (e.g., DW-32, DW-33).
decision: 2026-07-19 Run Review — Run a bmad-code-review session on the score entry code to catch any remaining issues.

### DW-2: Critical security vulnerabilities, including "JWT Leaked in URL", "XSS Exposure via LocalStorage", and "Account Takeover via Email Collision", are explicitly deferred to a later time. Merging code with known critical security flaws compromises the application and user data. These vulnerabilities must be fixed in the current PR.
origin: migrated from legacy ledger ("Deferred from: code review of 1-1-project-initialization-and-authentication-via-google-oauth2 (2026-05-01)"), 2026-07-19
location: n/a
reason: [ ] Critical security vulnerabilities, including "JWT Leaked in URL", "XSS Exposure via LocalStorage", and "Account Takeover via Email Collision", are explicitly deferred to a later time. Merging code with known critical security flaws compromises the application and user data. These vulnerabilities must be fixed in the current PR.
status: done 2026-07-19
resolution: already resolved: CustomOAuth2SuccessHandler.java uses HttpOnly cookies; UserService.java:44 handles email collisions properly

### DW-3: Database exhaustion in JwtAuthenticationFilter. This filter executes a synchronous database lookup for every single authenticated request. This introduces a massive bottleneck and makes the application trivial to DoS. Statelessness of JWT is defeated.
origin: migrated from legacy ledger ("Deferred from: code review of 1-1-project-initialization-and-authentication-via-google-oauth2 (2026-05-01)"), 2026-07-19
location: n/a
reason: [ ] Database exhaustion in JwtAuthenticationFilter. This filter executes a synchronous database lookup for every single authenticated request. This introduces a massive bottleneck and makes the application trivial to DoS. Statelessness of JWT is defeated.
status: done 2026-07-19
resolution: already resolved: JwtAuthenticationFilter.java:54 builds User object from claims without database queries

### DW-4: Missing Redis-based denylist with Bloom filters. Violates: Architecture Patterns and Constraints (AD-03: Stateless JWT with Redis Denylist).
origin: migrated from legacy ledger ("Deferred from: code review of 1-1-project-initialization-and-authentication-via-google-oauth2 (2026-05-01)"), 2026-07-19
location: n/a
reason: [ ] Missing Redis-based denylist with Bloom filters. Violates: Architecture Patterns and Constraints (AD-03: Stateless JWT with Redis Denylist).
status: done 2026-07-19
resolution: already resolved: RedisTokenRevocationService.java exists and implements Bloom Filters for denylist

### DW-5: Static role assignment (ROLE_USER only) [src/main/java/com/tictactore/security/JwtAuthenticationFilter.java:46]
origin: migrated from legacy ledger ("Deferred from: code review of 1-1-project-initialization-and-authentication-via-google-oauth2 (2026-05-02)"), 2026-07-19
location: n/a
reason: [ ] [Review][Defer] Static role assignment (ROLE_USER only) [src/main/java/com/tictactore/security/JwtAuthenticationFilter.java:46] — deferred, pre-existing architecture limit.
status: done 2026-07-19
decision: 2026-07-19 Keep static roles — Static roles are sufficient for the current scope. We will defer role management until an admin panel is required.
resolution: closed by human decision: Static roles are sufficient for the current scope. We will defer role management until an admin panel is required.
decision: 2026-07-19 Keep static roles — Static roles are sufficient for the current scope. We will defer role management until an admin panel is required.

### DW-6: Missing production CORS config [src/main/java/com/tictactore/config/SecurityConfig.java:27]
origin: migrated from legacy ledger ("Deferred from: code review of 1-1-project-initialization-and-authentication-via-google-oauth2 (2026-05-02)"), 2026-07-19
location: n/a
reason: [ ] [Review][Defer] Missing production CORS config [src/main/java/com/tictactore/config/SecurityConfig.java:27] — deferred, out of scope for initialization.
status: done 2026-07-24
resolution: already resolved: src/main/java/com/tictactore/config/SecurityConfig.java:43-52 and commit ac4aa43 configured production CORS using allowedOriginPatterns and application.security.cors.allowed-origins property.

### DW-7: Consistency: `isRevoked()` checks only today/yesterday Bloom Filters, but `revoke()` writes to all filters until token expiration
origin: migrated from legacy ledger ("Deferred from: code review of 1-1a-stateless-jwt-with-redis-denylist-and-bloom-filters (2026-05-10)"), 2026-07-19
location: n/a
reason: [ ] [Review][Defer] Consistency: `isRevoked()` checks only today/yesterday Bloom Filters, but `revoke()` writes to all filters until token expiration — tokens revoked >2 days ago will pass as valid if Redis bucket expired. Is a rolling 2-day window acceptable, or must coverage match JWT TTL exactly? [`RedisTokenRevocationService.java`] — deferred, need to investigate.
status: done 2026-07-19
decision: 2026-07-19 Accept 2-day window — The 2-day window is an acceptable trade-off for performance. JWT TTL should be kept under 2 days.
resolution: closed by human decision: The 2-day window is an acceptable trade-off for performance. JWT TTL should be kept under 2 days.
decision: 2026-07-19 Accept 2-day window — The 2-day window is an acceptable trade-off for performance. JWT TTL should be kept under 2 days.

### DW-8: Anonymization verification test logic flaw: TC-P0-004 plans to assert that an anonymized record has no PII via an API call after account deletion. However, account deletion revokes authentication (401), so a client-side E2E test cannot assert state without elevated endpoints.
origin: migrated from legacy ledger ("Deferred from: code review of 1-2-localization-and-translation-architecture.md (2026-05-15)"), 2026-07-19
location: n/a
reason: Anonymization verification test logic flaw: TC-P0-004 plans to assert that an anonymized record has no PII via an API call after account deletion. However, account deletion revokes authentication (401), so a client-side E2E test cannot assert state without elevated endpoints.
status: open
decision: 2026-07-19 Build admin endpoint — Create a test-only admin endpoint to query the database state for deleted accounts to verify anonymization.
decision: 2026-07-19 Build admin endpoint — Create a test-only admin endpoint to query the database state for deleted accounts to verify anonymization.

### DW-9: Missing DB Migration for Non-Nullable Nickname [src/main/java/com/tictactore/model/User.java:672-673]
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: [ ] [Review][Defer] Missing DB Migration for Non-Nullable Nickname [src/main/java/com/tictactore/model/User.java:672-673] — Nickname column is added as non-nullable, unique, but no DB migration script exists to backfill existing users.
status: done 2026-07-19
resolution: already resolved: V2__add_profile_fields.sql:3-6 adds migration logic for non-nullable nickname

### DW-10: Complete API Mocking in E2E Tests
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: frontend/e2e/profile-generation.spec.ts:409-418
reason: [ ] Complete API Mocking in E2E Tests [frontend/e2e/profile-generation.spec.ts:409-418] — Playwright E2E tests mock the profile API entirely, reducing integration validation quality.
status: open

### DW-11: Potential Nickname Length Overflow: 64-char email prefix plus UUID can exceed 73 chars.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Potential Nickname Length Overflow: 64-char email prefix plus UUID can exceed 73 chars.
status: open

### DW-12: Inefficient Nickname Collision Resolution: loop does 10 sync queries on creation.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Inefficient Nickname Collision Resolution: loop does 10 sync queries on creation.
status: open

### DW-13: Redundant Database Query on Profile Fetch: hits DB to fetch avatar/language when they could be deterministic/client-side.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Redundant Database Query on Profile Fetch: hits DB to fetch avatar/language when they could be deterministic/client-side.
status: open

### DW-14: Semantic Mismatch in JWT Claims: old 'name' claim might inject spaces into nickname.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Semantic Mismatch in JWT Claims: old 'name' claim might inject spaces into nickname.
status: done 2026-07-19
resolution: already resolved: JwtService.java:41 uses sanitized nickname from user object, preventing space injection

### DW-15: Unused and Unnecessary Versioning: @Version added to User but not utilized.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Unused and Unnecessary Versioning: @Version added to User but not utilized.
status: open

### DW-16: Over-engineered Transaction Boundaries: REQUIRES_NEW used without active parent transaction.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Over-engineered Transaction Boundaries: REQUIRES_NEW used without active parent transaction.
status: open

### DW-17: Improper Exception Type for Missing User
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: [ ] [Review][Defer] Improper Exception Type for Missing User
status: open

### DW-18: Missing Null Check in generateUniqueNickname
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: [ ] [Review][Defer] Missing Null Check in generateUniqueNickname
status: done 2026-07-24
resolution: already resolved: src/main/java/com/tictactore/service/UserService.java:123 includes explicit null check: if (email == null) throw new IllegalArgumentException('Email cannot be null');

### DW-19: High Collision Probability in Nickname Suffix
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: [ ] [Review][Defer] High Collision Probability in Nickname Suffix
status: open

### DW-20: Unhandled OptimisticLockingFailureException on concurrent updates [UserService.java]
origin: migrated from legacy ledger ("Deferred from: code review of 1-4-profile-management-in-personal-cabinet (2026-05-30)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Unhandled OptimisticLockingFailureException on concurrent updates [UserService.java] — deferred, pre-existing
status: done 2026-07-24
resolution: already resolved: src/main/java/com/tictactore/service/UserService.java:177,186 annotates updateProfile and deleteAccount with @Retryable(retryFor = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100)).

### DW-21: Shallow copy for rollback might corrupt state [frontend/src/stores/auth.ts]
origin: migrated from legacy ledger ("Deferred from: code review of 1-6-avatar-selection-and-management.md (2026-06-13)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Shallow copy for rollback might corrupt state [frontend/src/stores/auth.ts] — deferred, pre-existing limitation for flat state, YAGNI.
status: done 2026-07-19
resolution: already resolved: frontend/src/stores/auth.ts:70 uses JSON.parse(JSON.stringify) for deep copy

### DW-22: Nickname passed as empty or whitespace string silently dropped [frontend/src/stores/auth.ts]
origin: migrated from legacy ledger ("Deferred from: code review of 1-6-avatar-selection-and-management.md (2026-06-13)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Nickname passed as empty or whitespace string silently dropped [frontend/src/stores/auth.ts] — deferred, pre-existing
status: open

### DW-23: Brittle Optimistic Rollbacks in auth.ts [frontend/src/stores/auth.ts]
origin: migrated from legacy ledger ("Deferred from: code review of 1-6-avatar-selection-and-management.md (2026-06-13)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Brittle Optimistic Rollbacks in auth.ts [frontend/src/stores/auth.ts] — deferred, pre-existing limitation for flat state
status: open

### DW-24: Hardcoded and Unmanaged Z-Index [TutorialCarousel.vue]
origin: migrated from legacy ledger ("Deferred from: code review of 1-7-onboarding-tutorial (2026-06-17)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Hardcoded and Unmanaged Z-Index [TutorialCarousel.vue] — deferred, pre-existing
status: done 2026-07-24
resolution: already resolved: frontend/src/components/TutorialCarousel.vue:114 uses z-[100] with Tailwind design tokens (bg-surface-container/95, backdrop-blur-xl).

### DW-25: Concurrency Blindspot in Profile Updates
origin: migrated from legacy ledger ("Deferred from: code review of 1-7-onboarding-tutorial.md (2026-06-21)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Concurrency Blindspot in Profile Updates — deferred, pre-existing. The updateProfile method reads the user, mutates the state, and saves it without any explicit optimistic locking mechanism handling, silently overwriting concurrent updates.
status: open

### DW-26: Hardcoded team names in Pinia store [`frontend/src/stores/match.ts:14`]
origin: migrated from legacy ledger ("Deferred from: code review of 5-1-real-time-scoring-interface-landscape.md (2026-07-05)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Hardcoded team names in Pinia store [`frontend/src/stores/match.ts:14`] — deferred, pre-existing
status: done 2026-07-19
resolution: already resolved: ScoreEntry.vue computes team names dynamically; matchDraftStore.ts does not hardcode team names

### DW-27: Goals can be infinitely added to a finished match [`frontend/src/stores/match.ts:24`]
origin: migrated from legacy ledger ("Deferred from: code review of 5-1-real-time-scoring-interface-landscape.md (2026-07-05)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Goals can be infinitely added to a finished match [`frontend/src/stores/match.ts:24`] — deferred, pre-existing
status: done 2026-07-19
resolution: already resolved: matchDraftStore.ts:144 and :147 reject increments when match is ready_for_submission and cap at limit

### DW-28: Provide an undo mechanism for game transitions in multi-game matches before final submission.
origin: migrated from legacy ledger ("Deferred from: code review of 5-1-real-time-scoring-interface-landscape.md (2026-07-05)"), 2026-07-19
location: n/a
reason: Provide an undo mechanism for game transitions in multi-game matches before final submission.
status: done 2026-07-25
resolution: fixed in PR #139 (with canUndoLastGame guard against active score data loss)

### DW-29: Allow score decrementing to revert a game win state if tapped immediately.
origin: migrated from legacy ledger ("Deferred from: code review of 5-1-real-time-scoring-interface-landscape.md (2026-07-05)"), 2026-07-19
location: n/a
reason: Allow score decrementing to revert a game win state if tapped immediately.
status: done 2026-07-19
resolution: already resolved: ScoreEntry.vue:112 manual 'Next Game' button requires completeCurrentGame() explicitly, decrement freely available before that

### DW-30: Add confirmation dialog when clicking Cancel in the score entry view to prevent accidental resets.
origin: migrated from legacy ledger ("Deferred from: code review of 5-1-real-time-scoring-interface-landscape.md (2026-07-05)"), 2026-07-19
location: n/a
reason: Add confirmation dialog when clicking Cancel in the score entry view to prevent accidental resets.
status: done 2026-07-25
resolution: fixed in PR #140

### DW-31: Add a back button in the score entry view to return to player selection.
origin: migrated from legacy ledger ("Deferred from: code review of 5-1-real-time-scoring-interface-landscape.md (2026-07-05)"), 2026-07-19
location: n/a
reason: Add a back button in the score entry view to return to player selection.
status: open

### DW-32: Hardcoded win logic without win-by-two: Assumes naive absolute score ceiling, breaks for win-by-two.
origin: migrated from legacy ledger ("Deferred from: code review (spec-2-3-score-entry-and-automatic-completion.md)"), 2026-07-19
location: n/a
reason: Hardcoded win logic without win-by-two: Assumes naive absolute score ceiling, breaks for win-by-two.
status: open

### DW-33: Hardcoded array indices crash on 3v3: Array index hardcoding in `ScoreEntry.vue` fails if matchType is extended.
origin: migrated from legacy ledger ("Deferred from: code review (spec-2-3-score-entry-and-automatic-completion.md)"), 2026-07-19
location: n/a
reason: Hardcoded array indices crash on 3v3: Array index hardcoding in `ScoreEntry.vue` fails if matchType is extended.
status: done 2026-07-25
resolution: fixed in PR #138 (dynamic team names and tests for 1v1, 2v2, 3v3)
