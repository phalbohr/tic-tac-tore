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
status: done 2026-08-09
resolution: already resolved: Commit 56ca00e removed E2E API mocking for profile generation; current frontend/e2e/profile-generation.spec.ts uses real backend navigation with no route/mock/fulfill patterns.

### DW-11: Potential Nickname Length Overflow: 64-char email prefix plus UUID can exceed 73 chars.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Potential Nickname Length Overflow: 64-char email prefix plus UUID can exceed 73 chars.
status: done 2026-08-09
resolution: already resolved: UserService.java:142 truncates email prefix to 40 chars; suffixes are 8 chars (line 151 numeric, line 163 hex); max nickname = 48 chars, well within 255-char default column (commit 53a3f27).

### DW-12: Inefficient Nickname Collision Resolution: loop does 10 sync queries on creation.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Inefficient Nickname Collision Resolution: loop does 10 sync queries on creation.
status: done 2026-08-09
resolution: already resolved: UserService.java:154 and :166 use batch UserRepository.findExistingNicknames(List) queries instead of 10 individual sync queries per loop iteration (commit 05d115a, merged in PR #123).

### DW-13: Redundant Database Query on Profile Fetch: hits DB to fetch avatar/language when they could be deterministic/client-side.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Redundant Database Query on Profile Fetch: hits DB to fetch avatar/language when they could be deterministic/client-side.
status: done 2026-08-09
resolution: already resolved: UserController.java:27-41 builds ProfileDto directly from @AuthenticationPrincipal User principal with no DB call; JwtService.java:40-52 embeds avatar/language/tutorialCompleted in JWT claims (commit 7616595).

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
status: done 2026-08-09
resolution: already resolved: User.java:40-41 @Version is now utilized: JPA optimistic locking triggers ObjectOptimisticLockingFailureException on version mismatch, caught by @Retryable on UserService.updateProfile (UserService.java:193-197, commits 6d21262 and 38dadc3).

### DW-16: Over-engineered Transaction Boundaries: REQUIRES_NEW used without active parent transaction.
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: Over-engineered Transaction Boundaries: REQUIRES_NEW used without active parent transaction.
status: open

### DW-17: Improper Exception Type for Missing User
origin: migrated from legacy ledger ("Deferred from: code review of 1-3-automatic-profile-generation-and-first-entry.md (2026-05-24)"), 2026-07-19
location: n/a
reason: [ ] [Review][Defer] Improper Exception Type for Missing User
status: done 2026-08-09
resolution: already resolved: UserService.java:92 throws UserNotFoundException; UserOperation.java:53 throws UserNotFoundException which extends ResourceNotFoundException (commits 1a2f792 and f51f61e, merged in PR #126).

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
status: done 2026-08-09
resolution: already resolved: UserService.java:151 generates suffix via random.nextInt(100_000_000) formatted as %08d — 100M possible values; UUID fallback at line 163 provides 4B possible values (commit 7e63c72, merged in PR #127).

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
status: done 2026-08-09
resolution: already resolved: frontend/src/stores/auth.ts:94-97 throws 'Nickname cannot be empty' for empty or whitespace strings instead of silently dropping them (commit debdb08).

### DW-23: Brittle Optimistic Rollbacks in auth.ts [frontend/src/stores/auth.ts]
origin: migrated from legacy ledger ("Deferred from: code review of 1-6-avatar-selection-and-management.md (2026-06-13)"), 2026-07-19
location: n/a
reason: [x] [Review][Defer] Brittle Optimistic Rollbacks in auth.ts [frontend/src/stores/auth.ts] — deferred, pre-existing limitation for flat state
status: done 2026-08-09
resolution: already resolved: frontend/src/stores/auth.ts rollback at line 86,157-158 uses { ...profile.value } shallow copy, which is sufficient for the flat UserProfile state and avoids JSON serialization brittleness (commit b41c777).

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
status: done 2026-08-09
resolution: already resolved: User.java:40-41 @Version enables JPA optimistic locking; UserService.updateProfile (UserService.java:193-197) is @Retryable for ObjectOptimisticLockingFailureException with maxAttempts=3, backoff=100ms (commits 6d21262 and 38dadc3).

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
status: done 2026-07-25
resolution: fixed in PR #141 (added back button in ScoreEntry and returnToPlayerSelection in store)

### DW-32: Hardcoded win logic without win-by-two: Assumes naive absolute score ceiling, breaks for win-by-two.
origin: migrated from legacy ledger ("Deferred from: code review (spec-2-3-score-entry-and-automatic-completion.md)"), 2026-07-19
location: n/a
reason: Hardcoded win logic without win-by-two: Assumes naive absolute score ceiling, breaks for win-by-two.
status: done 2026-07-25
resolution: fixed in PR #142 (added winByTwo support in matchDraftStore and ScoreStepper)

### DW-33: Hardcoded array indices crash on 3v3: Array index hardcoding in `ScoreEntry.vue` fails if matchType is extended.
origin: migrated from legacy ledger ("Deferred from: code review (spec-2-3-score-entry-and-automatic-completion.md)"), 2026-07-19
location: n/a
reason: Hardcoded array indices crash on 3v3: Array index hardcoding in `ScoreEntry.vue` fails if matchType is extended.
status: done 2026-07-25
resolution: fixed in PR #138 (dynamic team names and tests for 1v1, 2v2, 3v3)

### DW-34: Unrelated deletion of Story 2.3 TEA result files during Story 2.4 work
origin: code review of story 2-4-match-submission-with-undo-window.md (2026-07-25)
location: `_bmad-output/implementation-artifacts/bmad-dev-auto-result-2-3-*.md`
reason: Unrelated TEA artifact files were deleted during story execution. Pre-existing cleanup noise.
status: done 2026-08-09
resolution: closed by human decision: TEA result files are build artifacts, not source code; their absence does not affect functionality, and git history preserves the original content if ever needed.
decision: 2026-08-09 Close — TEA result files are build artifacts, not source code; their absence does not affect functionality, and git history preserves the original content if ever needed.

### DW-35: Non-atomic idempotency key check outside `MatchOperation` transaction
origin: code review of 2-5-position-swapping-between-games.md (2026-07-26)
location: `src/main/java/com/tictactore/service/impl/MatchServiceImpl.java:37-42`
reason: [x] [Review][Defer] Non-atomic idempotency key check outside `MatchOperation` transaction — deferred, pre-existing concurrency refinement.
status: open

### DW-36: Hardcoded String used for match status instead of Enum
origin: code review of 2-5-position-swapping-between-games.md (2026-07-26)
location: `src/main/java/com/tictactore/model/Match.java:43`
reason: [x] [Review][Defer] Hardcoded String used for match status instead of Enum — deferred, pre-existing domain refinement.
status: open

### DW-37: Fragile Nickname Pseudonymization Logic
origin: code review of 3-1-confirmation-requests-and-push-notifications.md (2026-07-27)
location: `src/main/java/com/tictactore/service/impl/PushNotificationServiceImpl.java:90-101`
reason: [x] [Review][Defer] Fragile Nickname Pseudonymization Logic [src/main/java/com/tictactore/service/impl/PushNotificationServiceImpl.java:90-101] — deferred, pre-existing domain refinement.
status: open

### DW-38: Positional Null Parameter Creep in MatchResponse.java Record Constructors
origin: code review of 3-3-match-rejection-with-reason.md (2026-08-01)
location: `src/main/java/com/tictactore/dto/MatchResponse.java:1`
reason: [x] [Review][Defer] Positional Null Parameter Creep in MatchResponse.java Constructors — deferred, pre-existing constructor chaining smell.
status: open

### DW-39: Mock-Only Playwright Test Coverage for Match Rejection
origin: code review of 3-3-match-rejection-with-reason.md (2026-08-01)
location: `frontend/e2e/tests/e2e/match-rejection.spec.ts:1`
reason: [x] [Review][Defer] Mock-Only Playwright Test Coverage — deferred, pre-existing mock pattern in E2E suite.
status: open

### DW-40: Unused `sendCooldownReminderNotification` method
origin: code review of 3-5-publication-rules-and-24-hour-cooldown.md (2026-08-06)
location: `src/main/java/com/tictactore/service/PushNotificationService.java:24` and `src/main/java/com/tictactore/service/impl/PushNotificationServiceImpl.java:176`
reason: [x] [Review][Defer] `sendCooldownReminderNotification` is fully implemented but never called — dead code from optional cooldown reminder feature.
status: open

### DW-41: Hardcoded 24-hour cooldown duration
origin: code review of 3-5-publication-rules-and-24-hour-cooldown.md (2026-08-06)
location: `src/main/java/com/tictactore/model/Match.java:141`
reason: [x] [Review][Defer] 24-hour cooldown is a magic number inline in `Match.confirmByOpponent()` — cannot be changed without code change and redeploy.
status: open

### DW-42: Scheduled job error swallowing without dead-letter
origin: code review of 3-5-publication-rules-and-24-hour-cooldown.md (2026-08-06)
location: `src/main/java/com/tictactore/service/MatchCooldownService.java:33-35`
reason: [x] [Review][Defer] `processExpiredCooldowns()` catches and logs per-match failures with no dead-letter, alerting, or max-retry counter.
status: open

### DW-43: `requiresCooldown` duplicates `supportsPartialConfirmation` logic
origin: code review of 3-5-publication-rules-and-24-hour-cooldown.md (2026-08-06)
location: `src/main/java/com/tictactore/rules/VerificationRules.java:36-44`
reason: [x] [Review][Defer] `requiresCooldown()` checks `isDoubles && isParticipantEntered && STANDARD`, identical to `supportsPartialConfirmation()` — silent divergence risk if rules change.
status: open

### DW-44: No quantitative latency baseline for the leaderboard endpoint
origin: NFR assessment of 4-2-global-leaderboard-with-filtering (2026-08-15)
location: `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java`
severity: medium
reason: In-memory aggregation iterates all matches and games on every request with no caching, and no load test exists, so p95 latency and throughput are unknown; the DB-level `GROUP BY` migration that would replace this is already scoped to Epic 4.6, so measuring the interim implementation was deferred rather than blocking the story.
status: open

### DW-45: JaCoCo coverage report not generated for the leaderboard code
origin: NFR assessment of 4-2-global-leaderboard-with-filtering (2026-08-15)
location: `pom.xml` (JaCoCo report goal) and `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java`
severity: low
reason: 36 backend and 215 frontend tests pass but no coverage percentage is produced, so the maintainability NFR has no quantitative evidence; wiring the report is a build-configuration task independent of this story's scope. Root cause: `maven-surefire-plugin` in `pom.xml:144-150` hardcodes `<argLine>`, overwriting the `-javaagent` set by `prepare-agent`, so `jacoco.exec` is not created. Fix is `<argLine>@{argLine} -Dnet.bytebuddy.experimental=true</argLine>`.
status: open

### DW-46: No rate limiting on `/api/v1/statistics/leaderboard`
origin: NFR assessment of 4-2-global-leaderboard-with-filtering (2026-08-15)
location: `src/main/java/com/tictactore/controller/StatisticsController.java`
severity: medium
reason: The endpoint recomputes the full aggregation per request and is unthrottled, which is a cheap amplification target; documented as risk R-001 in the epic 4 test design and deferred to the platform-wide rate-limiting effort rather than adding a one-off limiter here.
status: open

### DW-47: No metrics, structured logging or alerting on the leaderboard endpoint
origin: NFR assessment of 4-2-global-leaderboard-with-filtering (2026-08-15)
location: `src/main/java/com/tictactore/controller/StatisticsController.java` and `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java`
severity: medium
reason: Neither the controller nor the service emits Micrometer timings or correlated structured logs and no alerts or SLA are defined, so production request rate, latency and error rate are invisible; observability is a platform-wide concern deferred out of this story.
status: open

### DW-48: Unbounded match load in `getPersonalStats`
origin: NFR assessment of 4-3-positional-statistics-attack-vs-defense (2026-08-16)
location: `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java` (`getPersonalStats`)
severity: medium
reason: `getPersonalStats` calls `findConfirmedMatchesWithFilters(null, null, null, null)` and filters to a single user in memory, so response time and heap grow linearly with total match volume. This is the same pattern already tracked as DW-44 for `/leaderboard`; the DB-scoped `findConfirmedMatchesForPlayer(UUID)` query is scoped to Epic 4.6, so it was deferred rather than duplicated here. Acceptable at MVP scale (≤50 active players, ≤5k matches).
status: open

### DW-49: Frontend sends `period`/`myPosition`/`opponentPosition` that `/statistics/me` ignores
origin: NFR assessment of 4-3-positional-statistics-attack-vs-defense (2026-08-16)
location: `frontend/src/services/statisticsService.ts` (`PersonalStatsParams`) and `src/main/java/com/tictactore/controller/StatisticsController.java` (`getPersonalStats`)
severity: medium
reason: `PersonalStatsParams` types filter params the backend endpoint does not accept, so a caller passing `period` silently receives all-time data. Resolving it requires either date-scoped repository overloads or trimming the frontend contract — a decision belonging with the Epic 4.6 query rework, so it was deferred (risk R-003 in the epic 4 test design).
status: open

### DW-50: No JaCoCo coverage evidence for `getPersonalStats`
origin: NFR assessment of 4-3-positional-statistics-attack-vs-defense (2026-08-16)
location: `pom.xml` (JaCoCo report goal)
severity: low
reason: 14 new tests (7 unit + 7 integration) pass, but `./mvnw test` reports "Skipping JaCoCo execution due to missing execution data file", so the ≥80% line-coverage threshold has no quantitative evidence. Same build-configuration gap as DW-45 (`maven-surefire-plugin` argLine override).
status: open

### DW-51: No observability or fault tolerance on `/api/v1/statistics/me`
origin: NFR assessment of 4-3-positional-statistics-attack-vs-defense (2026-08-16)
location: `src/main/java/com/tictactore/controller/StatisticsController.java` (`getPersonalStats`)
severity: medium
reason: The endpoint emits no Micrometer timings or structured logs, and has no retry, circuit breaker or fallback path — unhandled failures surface as a generic 500. MTTR and availability are therefore unmeasurable. Same platform-wide observability concern as DW-47.
status: open

### DW-52: Redundant service-layer filtering in LeaderboardServiceImpl
origin: code review of 4-2-global-leaderboard-with-filtering (2026-08-16)
location: `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java:40-50`
severity: low
reason: [x] [Review][Defer] Redundant service-layer filtering in LeaderboardServiceImpl duplicates filters already applied by the repository query; defensive duplication is tolerable for MVP scale.
status: open

### DW-53: N+1 `userRepository.findById` inside aggregation loop
origin: code review of 4-2-global-leaderboard-with-filtering (2026-08-16)
location: `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java:119,140,152`
severity: medium
reason: [x] [Review][Defer] N+1 `userRepository.findById` inside aggregation loop fetches nicknames individually during in-memory aggregation; deferred to Epic 4.6 DB-level migration.
status: open

### DW-54: Match type inference relies on null defender ID
origin: residual risk of 4-2-global-leaderboard-with-filtering (2026-08-16)
location: `src/main/java/com/tictactore/service/impl/LeaderboardServiceImpl.java:48-51`
severity: low
reason: [x] [Review][Defer] Match type (1v1 vs 2v2) inference relies on null defender IDs; corrupted or partial legacy data could misclassify match types.
status: open

### DW-55: Aggregate team pair synergy across intra-game position swaps
origin: code review of 4-4-team-pair-statistics.md (2026-08-16)
location: `src/main/java/com/tictactore/repository/MatchRepository.java:63`
reason: [x] [Review][Defer] Aggregate team pair synergy across intra-game position swaps from Story 2.5 — deferred, future enhancement for game-level positional synergy breakdown.
status: open

### DW-56: Player and Rule System filter UI controls in TeamStatsView.vue
origin: code review of 4-4-team-pair-statistics.md (2026-08-16)
location: `frontend/src/features/stats/components/TeamStatsView.vue:65`
reason: [x] [Review][Defer] AC2 Player & Rule System Filter UI Controls in TeamStatsView.vue — deferred, basic filters (Period & Min Matches) sufficient for MVP pair stats, full player/rule UI controls deferred.
status: open

## Unrecoverable Triage & Review Summaries Note

- **Story 4-3 Review Triage:** The review triage log references 6 + 4 deferred findings from initial and follow-up passes. These items were consolidated into DW-48 through DW-51 (unbounded load, ignored query params, JaCoCo build config, observability). Detailed itemized logs were collapsed without individual names preserved.
- **Story 4-2 Rejected Findings:** 17 review findings (9 on 2026-08-15, 8 on 2026-08-16) were evaluated as noise/non-actionable and rejected per spec logs.

## Deferred from: code review of 5-2-live-activity-timeline-and-undo.md (2026-08-20)

- Sticky Hover States on Mobile [frontend/src/features/match/LiveMatch.vue:94]: The "Undo Last Goal" button uses `hover:ch-bg-gray-700`. On touch devices, hover states frequently stick after the user lifts their finger.
- Brittle DOM Text Assertions [frontend/tests/unit/LiveActivityTimeline.spec.ts:48]: Tests assert `.toContain('teamA.attacker')`, coupling the test to the UI text formatting.
- Missing i18n for Empty State [frontend/src/features/match/LiveActivityTimeline.vue:38]: "No goals recorded" is hardcoded directly into the template.
- Destructive Undo Without Confirmation: Instant undo is retained for speed; deferred confirmation.
## Deferred from: code review (2026-08-21)
* [Review][Defer] No Visual Distinction for Buttons — Team A and Team B swap buttons use the exact same styling and icon.

## Deferred from: code review of 5-4-third-party-referee-mode.md (2026-08-22)
- DRY violation with LiveQuadrant components [frontend/src/features/match/LiveMatch.vue:103-169] — deferred, pre-existing
- Undo button touch target size below 56x56dp [frontend/src/features/match/LiveMatch.vue:92] — deferred, pre-existing
- Swap buttons absolute positioning ruins focus order [frontend/src/features/match/LiveMatch.vue:173-208] — deferred, pre-existing

## Deferred from: code review of 5-5-screen-wake-lock-and-continuity.md (2026-08-23)
- Fragile Unmount Simulation in E2E test [frontend/e2e/wake-lock-continuity.spec.ts]
- Ineffective Fallback Assertions in AC4 [frontend/e2e/wake-lock-continuity.spec.ts]
- Brittle Mock Restoration in unit tests [frontend/tests/unit/useWakeLock.spec.ts]

## Deferred from: code review of 6-1-named-player-groups-teams (2026-08-23)
- Testing anti-pattern in Vue components [PlayerSelection.vue]: `useI18n()` is wrapped in a silent `try/catch` block labeled "// fallback for tests". If translation fails, errors are swallowed and users see raw localization keys.

## Deferred from: code review of 6-4-join-existing-pool (2026-08-28)
- No pagination or limits on pool fetching: PoolServiceImpl.getActivePools() fetches all OPEN pools indiscriminately, risking memory exhaustion. (MatchmakingPoolRepository.java)
- Hardcoded match format labels break i18n: Match format tags (1v1 / 2v2) in ActivePoolsList.vue are hardcoded strings.

## Deferred from: code review of 6-5-pool-notifications (2026-08-29)

- Ignored "Matching Criteria" Requirement: The original user story requests notifications for "pools matching my criteria," but the implementation (and the mutated Acceptance Criteria) drops this logic, blasting POOL_CREATED notifications to every user in the system regardless of preferences.

## Deferred from: code review of 6-6-challenge-player-or-group.md (2026-08-30)

- Redundant authentication boilerplate in controller: Endpoints manually check if principal == null and throw 401 instead of relying on Spring Security.
- Database chattiness in createChallenge: Creating a challenge triggers up to four separate findById calls instead of using getReferenceById.
- SQL IN clause hack with random UUID: findIncomingChallenges uses UUID.randomUUID() to bypass SQL syntax errors for empty collections.
- Exception swallowing in ChallengeNotificationListener: Methods log exceptions and continue, potentially masking systemic failures.
- Sequential blocking network calls for group push notifications: Iterating over group members and making sequential push network calls.
- Complex OR conditions in findIncomingChallenges: Query spans different relationships, potentially defeating query planner.
- Bypassed standard validation for CreateChallengeRequest: Target validation throws a custom ValidationException instead of using a class-level @Constraint.
