---
stepsCompleted: ["step-01-document-discovery", "step-02-prd-analysis", "step-03-epic-coverage-validation", "step-04-ux-alignment", "step-05-epic-quality-review", "step-06-final-assessment"]
documentsSelected:
  prd: "_bmad-output/planning-artifacts/prd.md"
  architecture: "_bmad-output/planning-artifacts/architecture.md"
  epics: "_bmad-output/planning-artifacts/epics.md"
  ux: "_bmad-output/planning-artifacts/ux-design-specification.md"
---

# Implementation Readiness Assessment Report

**Date:** 2026-04-30
**Project:** tic-tac-tore

## Document Discovery

### PRD Files Found
**Whole Documents:**
- prd.md (77K, 2026-04-28)

### Architecture Files Found
**Whole Documents:**
- architecture.md (12K, 2026-04-27)

### Epics & Stories Files Found
**Whole Documents:**
- epics.md (27K, 2026-04-30)

### UX Design Files Found
**Whole Documents:**
- ux-design-specification.md (98K, 2026-04-26)

## PRD Analysis

### Functional Requirements

- **FR1:** Player can create a retrospective match record for a completed 1v1 match by selecting players and entering game scores
- **FR2:** Player can create a retrospective match record for a completed 2v2 match by selecting four players and entering game scores per game
- **FR3:** Player can select a rule system (ITSF, DTFB, or a saved Custom template) before entering match data, or create a new custom template inline. The system enforces the selected rule system's constraints (game count, score limits, win conditions)
- **FR4:** Player can record a live match in real-time by tapping screen quadrants to attribute goals to specific players during play (Phase 1.5)
- **FR5:** Third-party observer (referee) can record a match they are not participating in, with adapted UI for their viewing position (Phase 1.5)
- **FR6:** System automatically completes a game when a team reaches the score limit and automatically completes a match when the win condition is met — no manual "end game" needed
- **FR7:** Player can swap teammate positions between games (mandatory or free, per rule system) in both retrospective and live entry modes
- **FR8:** Player can swap teammate positions within a game during live match mode via a per-team swap button, if the active rule system permits within-game swaps (Phase 1.5). In retrospective entry, within-game swaps cannot be tracked — this is an accepted limitation per KD-03 (Speed over Precision)
- **FR9:** Player can undo the last recorded goal during live match entry (Phase 1.5)
- **FR10:** System displays a live activity timeline showing goal sequence with scorer identification during live match mode (Phase 1.5)
- **FR11:** System prevents screen dimming during live match mode (Phase 1.5)
- **FR12:** System sends a confirmation request to the appropriate opponents when a match is submitted
- **FR13:** Confirming player completes confirmation via single-tap; upon tapping "Confirm," a 15-second undo toast appears ("Match confirmed. Tap to undo."). The match is committed only after the undo window expires without cancellation. If cancelled within 15 seconds, the confirmation is voided and the match returns to pending state
- **FR14:** System applies confirmation rules based on match context: 1v1 participant-entered (one opponent confirms), 1v1 referee-entered (both confirm), 2v2 standard (one opponent confirms, second triggers immediate publication), 2v2 random pairings (both opponents confirm), referee-entered 2v2 (one per team, or all four for immediate publication). In all cases, "confirms" means single-tap followed by the 15-second undo window completing without cancellation
- **FR15:** System enforces a 24-hour cooldown after first confirmation before publishing to statistics; second opponent's confirmation triggers immediate publication
- **FR16:** System ensures confirmed match data is immutable — it cannot be modified or deleted
- **FR17:** Player can reject a submitted match, returning it to the creator for correction with a notification
- **FR18:** System enforces a 48-hour confirmation window for tournament matches; non-confirmation results in technical defeat (Phase 3)
- **FR19:** Player can view an individual leaderboard with rankings sortable by any statistical column
- **FR20:** Player can filter statistics by rule system (ITSF/DTFB/Custom template), match type (1v1/2v2), and time period (all time/year/month/week)
- **FR21:** Player can view their individual statistics with positional breakdown: matches W/L/D, games won/lost as attacker and defender, goals scored/conceded by position
- **FR22:** Player can view visual stat bars showing proportional breakdowns (matches W/D/L, games by position, goals by position)
- **FR23:** Player can view team statistics showing pair-level performance for every teammate combination, filterable by a specific player
- **FR24:** Player can view head-to-head statistics with three cross-tabulated tables: matches (with/vs), games (with/vs), and goals (with detailed positional breakdowns: attacker-vs-defender, attacker-vs-attacker, etc.)
- **FR25:** Player can filter any statistics view by: all players, a specific named team, or the built-in "Favorites" team. In player selection during match creation, the same filter applies — favorites first by default, with quick team switching
- **FR26:** Player can filter statistics by a specific tournament and tournament stage (Phase 3)
- **FR27:** System paginates statistics with player-configurable page size (10/20/50/100)
- **FR28:** Player can set a minimum games played threshold (0/10/20/50/100) when viewing any statistics table, filtering out players with insufficient data for statistically meaningful comparisons
- **FR29:** User can sign up and log in via Google OAuth2
- **FR30:** Player can set a nickname and avatar image in their personal cabinet
- **FR31:** Player can change their nickname at most once per month
- **FR32:** Player can select interface language (English or German)
- **FR33:** Player can delete their account; system removes all personal data and replaces identity with an anonymized placeholder ("boots on a nail" avatar) while preserving match history
- **FR34:** System stores only authentication credentials, profile data (nickname, avatar, language), and match data — no additional personal data
- **FR35:** Player can create a "Want to Play" pool specifying match type (1v1/2v2) and start condition (fill-based or scheduled time) (Phase 2)
- **FR36:** Player can join an existing open pool from a list view (Phase 2)
- **FR37:** System sends push notifications when a pool fills or when a new pool matching user preferences is created (Phase 2)
- **FR38:** Player can challenge a specific player or group to a match (Phase 2)
- **FR39:** Player can create named player groups ("teams") — curated lists of players — inline during match creation or from profile settings. "Favorites" is a built-in default team. All teams function identically: they filter player selection lists and statistics views to show only members of the selected team (Phase 2)
- **FR40:** Player can set a default team and default rule template that auto-populate when creating a new match; both can be set inline during match creation without navigating to a separate settings screen (Phase 2)
- **FR41:** Player can create a tournament with configurable parameters: format (cup/championship), mode (1v1 personal, 2v2 fixed teams, 2v2 random pairings), rule system, min/max participants, registration deadline or wait time, round count, playoff option (Phase 3)
- **FR42:** Player can register for an open tournament; team partner receives confirmation notification and must accept (Phase 3)
- **FR43:** System generates the complete match table/bracket automatically when tournament starts. Seeding is strength-based: initial rounds pair players/teams by relative statistical strength, with specific algorithm and strength metric (e.g., ELO difference threshold, rank distance) defined by the pre-Phase 3 seeding spike. Tournament creators may select from available seeding algorithms as a creation parameter. **Blocking dependency:** Phase 3 implementation requires a completed seeding research spike — deliverables: evaluated ITSF/DTFB tournament seeding standards, selected algorithm, defined strength metric, documented as Phase 3 pre-implementation requirement. (Phase 3)
- **FR44:** Players can play tournament matches in any order at their own pace — not locked to a schedule (Phase 3)
- **FR45:** Tournament matches use the tournament's configured rule system and are entered through the standard match entry flow (Phase 3)
- **FR46:** System maintains tournament-specific standings, statistics, and match archive accessible after tournament completion (Phase 3)
- **FR47:** System enforces equal match distribution for 2v2 random pairing mode (Phase 3)
- **FR48:** System awards achievements (badges, pennants, cups) based on player actions, milestones, and statistical thresholds (Phase 4+)
- **FR49:** System awards humorous anti-achievements for memorable fails, designed to be lighthearted and celebratory — not shaming (Phase 4+)
- **FR50:** Player can view their award wall displaying all collected achievements (Phase 4+)
- **FR51:** Player can view progress toward locked achievements (Phase 4+)
- **FR52:** System generates narrative match broadcasts (text, audio, or animation) from live match protocols, narrating real player actions in sports commentary style (Future)
- **FR53:** System provides auto-generated statistical insights and progress tracking for individual players (Future)
- **FR54:** Application is installable as a Progressive Web App (Add to Home Screen)
- **FR55:** System delivers push notifications for match confirmations, pool events, tournament updates, and achievements
- **FR56:** New users are presented with an onboarding tutorial explaining key features
- **FR57:** System provides demo/seed data so new users see populated statistics rather than empty tables; demo data is hidden after a configurable threshold
- **FR58:** System enforces automated rate limiting on match submissions to prevent spam, with context-aware thresholds that accommodate tournament referee throughput
- **FR59:** System supports English and German interface languages; adding a new language requires only a new translation file with no code changes
- **FR60:** Player can view match history with options to filter by all players or a specific team, and a separate section for pending confirmations

Total FRs: 60

### Non-Functional Requirements

- **Acceptable:** Within target (normal operation)
- **Degraded:** 2x–5x target — loading indicator shown, warning logged to observability
- **Failed:** >5x target — timeout with user-visible error message, observability alert triggered
- **Authentication:** Google OAuth2 with backend-issued authentication tokens with 24-hour expiry (currently HS256 JWT). Stateless — no server-side sessions.
- **Authorization:** Match creation restricted to participants or designated referees. Match confirmation restricted to opponents of the creator. No cross-user data access beyond public statistics.
- **Transport:** All communication over HTTPS. No exceptions.
- **Data storage:** Passwords are never stored (OAuth2 delegation). JWT signing key stored as environment variable, never in source code.
- **Token handling:** Authentication tokens stored client-side. Expired sessions redirect to login without data loss. Tokens must migrate from client-accessible storage to HttpOnly cookies before public rollout beyond home office.
- **Token invalidation on account deletion:** Deleted or banned user tokens must be rejected immediately upon next request, without waiting for natural expiry.
- **Input validation:** All user input validated server-side. Game scores bounded by rule system constraints. Player UUIDs validated for existence and uniqueness within a match.
- **XSS prevention:** Strict input sanitization on all user-generated content (nicknames, team names, template names). Content Security Policy (CSP) headers enforced from MVP.
- **Rate limiting baselines:** Max 10 standalone match submissions/hour/user; max 30/hour in tournament referee context; 5+ rejections within 24h triggers submission throttle. Thresholds configurable, not hardcoded.
- **Content filter:** Basic blocklist filter on user-generated display names (nicknames, team names) — obviously offensive terms rejected at save time. Not full moderation — minimal hygiene filter. Priority: post-MVP, before expansion beyond home office.
- **Dependency vulnerability monitoring:** Automated dependency vulnerability scanning required. Critical CVEs patched within 7 days. No dependencies with known critical vulnerabilities in production.
- **DSGVO compliance:** Pseudonymization by design; minimal data collection; irreversible anonymization on account deletion; Privacy Policy with transparent data retention disclosure; Legitimate Interest Assessment (LIA) before production launch.
- **Data portability (GDPR Art. 20):** Player data export available upon request (not required as in-app feature). Product owner can generate export manually via database query. Structured JSON format. Response within 30 days per GDPR. In-app self-service export is a future enhancement, not an MVP requirement.
- **Initial capacity:** 10–20 concurrent users, ~50 registered users within 12 months.
- **Growth scenario:** If social features and tournaments gain traction (e.g., foosball community forums adoption), user base could grow to 200–500 within 18 months.
- **Design for growth:** Stateless backend architecture (no server sessions) enables horizontal scaling if needed. Database queries must remain performant up to 10,000 confirmed matches and 500 registered users without query redesign.
- **UI data volume resilience:** All selection lists (players, templates, tournaments) must remain usable and performant at 10x expected data volume. Search/filter required for lists exceeding 20 items.
- **No premature optimization:** Current infrastructure (single Spring Boot instance + PostgreSQL) is sufficient for the initial user base. Scaling decisions deferred until actual load demands it.
- **Full parameter space coverage:** The platform must support matches under any valid combination of rule parameters (~2,500 combinations). No hard-coded rule system assumptions — ITSF and DTFB are presets, not special cases.
- **Parameter validation:** Server-side validation of rule template parameters at creation: score limit > 0, valid win condition, consistent point distribution. Invalid combinations rejected with clear error message.
- **Tournament rule compliance:** Tournaments must be configurable to run under any officially recognized rule system (ITSF, DTFB) as well as any user-created custom rule template. No tournament format is restricted to a specific rule system.
- **Forward compatibility:** New official rule systems (e.g., future ITSF revisions) must be addable as new presets without architectural changes — they are just named parameter combinations like any other template.
- **Target:** WCAG 2.1 Level A.
- **MVP priority:** Low — built for a known group of ~10 users. Level A compliance becomes a requirement before rollout beyond the home office.
- **Day-one foundations (non-negotiable):**
- **Touch targets:** Minimum 56x56dp for action buttons in live match mode (position swap, goal undo). Screen quadrants for goal attribution are naturally large (~180x320px on a 360px-wide phone) and require no additional sizing.
- **Optimistic UI:** All user-initiated actions must produce immediate visual feedback (<50ms) before backend confirmation. Goal taps, button presses, and navigation must feel instant.
- **Undo behavior:** First undo (last recorded goal) executes immediately without confirmation. Subsequent consecutive undos require a confirmation dialog ("Remove another goal?") to prevent accidental deep rollback.
- **Duplicate match detection:** Before sending any confirmation request, the system checks for existing matches (confirmed or pending) with identical players and identical scores from the same day. If a potential duplicate is found: the submitter is warned at submission time; if confirmed as new, the confirmation notification is sent with a visible warning label differentiating it from standard notifications to prevent blind approval of duplicates.
- **Data durability:** Confirmed match data is the most critical asset. Zero tolerance for data loss on confirmed matches. Database backups with point-in-time recovery.
- **Backup & Recovery:** Daily automated backups. RPO < 24 hours. RTO < 4 hours during business hours. Backup restoration procedure documented and tested at least once before production launch.
- **Optimistic locking:** Concurrent match confirmation handled via optimistic locking with automatic retry. No silent data overwrites.
- **Undo window atomicity:** Match confirmation is committed server-side only after the 15-second undo window expires without cancellation. If the client cancels during the window, the server performs an atomic rollback — no partial confirmation state is persisted. Concurrent cancellation and window-expiry events are resolved via server-side timestamp authority; the client clock is never trusted for this decision.
- **Server-side match validation:** Match data validated against rule system constraints before confirmation is accepted. Confirmation audit log recording actor, timestamp, and action for every match state transition. Emergency data correction procedure documented for confirmed bug scenarios (direct DB access, not UI-exposed).
- **Write failure resilience:** If database is temporarily unavailable for writes, match submission data must be preserved client-side and retried automatically upon recovery. Live match continues recording in client memory. Read operations (statistics) remain available.
- **Live match session recovery:** In-progress live match state auto-saved to client storage at minimum every goal event. Session recovery allows resuming a live match after browser crash, tab closure, or device restart (Phase 1.5).
- **Live match session uniqueness:** Only one device can actively record a specific live match at a time. Attempting to open the same live match on a second device shows a clear conflict message (Phase 1.5).
- **Time calculations:** All time-based business logic (cooldowns, deadlines, tournament windows) calculated server-side in UTC. Client displays converted to user's local timezone. No client-side time calculations for business rules.
- **Push notification fallback:** If push delivery fails, pending confirmations and tournament deadlines remain visible via in-app badge on next app open. Push permission revocation detected — system displays in-app prompt to re-enable.
- **Graceful degradation for unsupported PWA features:** If Screen Wake Lock API is unavailable, display user guidance to disable auto-lock manually. Never silently fail.
- **User-facing degradation notice:** When API response times exceed degraded threshold, display a non-blocking banner: "Service is temporarily slow."
- **Tournament start notification with rules reminder:** When a tournament's registration pool closes, all participants receive a dedicated notification summarizing tournament name, format, key rules, and — highlighted separately — the 48-hour confirmation deadline and technical defeat consequence.
- **Uptime expectation:** No formal SLA. Target: >99% availability during business hours (Mon–Fri, 8:00–18:00 CET). Brief maintenance windows during off-hours acceptable.
- **Account deletion during active tournament:** Deletion is never blocked but follows a 24-hour delay protocol with countdown notification and cancellation option. Partner notified immediately. Remaining tournament matches result in technical defeat. For random-pairing tournaments: stub partner assigned randomly from players closest in statistical strength to the deleted player, using frozen strength rating captured at tournament start. Stub partner's extra match does not count toward their tournament statistics. In knockout format, the stub partner cannot be eliminated in their substitute match if already advanced in their own bracket.
- **Structured logging:** All backend log output in structured JSON format. Log entries include correlation IDs for request tracing.
- **Health check:** Machine-readable health check endpoint exposed for monitoring.
- **Anomaly alerting:** System must detect and log anomalous patterns — e.g., sudden spike in rejected matches (spam signal), unusual submission volume from a single user.
- **Push notification audit trail:** Every notification sent must be logged with timestamp, recipient, delivery status. Queryable for dispute resolution (tournament technical defeats).
- **Mass push failure detection:** If push delivery failure rate exceeds threshold (e.g., >50% within 1 hour), system logs alert for operational investigation.
- **Scheduled job monitoring:** Cooldown expiry, tournament deadline enforcement, and other time-triggered jobs must be monitored. Missed execution triggers alert.
- **No heavy infrastructure required for MVP:** Actuator + structured logs is the baseline. Full monitoring stack (Prometheus/Grafana) deferred until operational scale demands it.
- **Versioned migrations:** All database schema changes managed through Flyway. No Hibernate DDL-auto in production.
- **Migration reversibility:** Every migration must be reversible or have a documented rollback plan.
- **Schema extensibility:** Initial schema must accommodate future phases (rule_configuration table, goal-level events for live mode, tournament structures) without breaking migrations.
- **Versioned API:** All endpoints under `/api/v1/` prefix (already implemented).
- **Backward compatibility policy:** No breaking changes to existing endpoints within a major version. Breaking changes require a new API version (`/api/v2/`).
- **Client-server contract:** PWA clients may be cached and running an older version after a backend deploy. API must handle gracefully — clear error messages for deprecated endpoints, not silent failures.
- **PWA cache invalidation:** Service Worker must detect new backend version and prompt user to refresh. Stale frontend must never silently fail against updated API.
- **ATDD-first strategy:** Testing follows TEA workflow (ATDD). Acceptance tests derived from functional requirements are the primary quality gate — does the implementation fulfill what the product owner specified?
- **Detailed test architecture:** Coverage targets, execution time constraints, test pyramid structure, and CI integration are defined in the Test Architecture document — a required pre-implementation artifact for Phase 1.
- **Statistical query validation:** Every statistics aggregation query must have a dedicated integration test with seed data verifying correctness across all supported rule systems.
- **Critical path E2E smoke tests as deploy gate:** Match submission, confirm, reject, login, and statistics load must be verified by automated E2E tests before any production deploy.
- **Languages:** English (primary), German (secondary). All user-facing strings externalized from day one.
- **Locale handling:** Date/time formatting, number formatting adapted to user's selected language.
- **Content language:** All generated content (notifications, achievement descriptions, tournament labels) must be localized in both supported languages.
- **RTL readiness:** CSS architecture must not preclude future RTL layout support. No RTL implementation required now, but layout patterns (flexbox direction, text alignment) must be directionality-neutral where possible.
- **Extensibility:** Adding a new language requires only a new translation file — no code changes.

### Additional Requirements

No additional unlabeled technical constraints found.

### PRD Completeness Assessment

The PRD is highly comprehensive. It outlines 60 detailed Functional Requirements covering Match Recording, Match Verification, Statistics, Profile, Social, Tournaments, and Achievements. The NFR section is exceptionally detailed with explicit targets for Performance, Security, Reliability, etc. The document provides a very solid foundation for implementation.

## Epic Coverage Validation

### Coverage Matrix
| FR Number | Status |
| --------- | ------ |
| FR1-FR17  | ✓ Covered |
| FR18      | ✓ Covered |
| FR19-FR25 | ✓ Covered |
| FR26-FR28 | ✓ Covered |
| FR29-FR34 | ✓ Covered |
| FR35-FR40 | ❌ MISSING (Social & Matchmaking) |
| FR41, 43  | ✓ Covered |
| FR42      | ❌ MISSING (Tournament Registration) |
| FR44-FR47 | ❌ MISSING / Partial (Tournament match flow & standings missing from map) |
| FR48-FR49 | ✓ Covered |
| FR50-FR53 | ❌ MISSING (Achievements & Engagement features) |
| FR54-FR55 | ✓ Covered |
| FR56      | ❌ MISSING (Onboarding tutorial) |
| FR57-FR58 | ✓ Covered |
| FR59      | ❌ MISSING (Language translation architecture explicit story) |
| FR60      | ✓ Covered |

### Missing Requirements

### Critical Missing FRs
**FR35-FR40: Social & Matchmaking**
- Impact: Core Phase 2 functionality entirely omitted from Epics.
- Recommendation: Add Epic or incorporate into Epic 4/7.

**FR44-FR47: Tournament Logic**
- Impact: Tournament management is incomplete without match distribution and standings.
- Recommendation: Update Epic 6 to include stories covering these FRs.

### High Priority Missing FRs
- **FR50-FR53**: Award wall and narrative broadcasts.
- **FR56**: Onboarding tutorial.

### Coverage Statistics
- Total PRD FRs: 60
- FRs covered in epics: 42
- Coverage percentage: 70%

## UX Alignment Assessment

### UX Document Status
Found: `ux-design-specification.md`

### Alignment Issues
- UX requirements are accurately captured in `epics.md` as UX-DR1 through UX-DR9. 
- No architectural conflicts found (e.g. optimistic UI <50ms response is supported by architecture).

### Warnings
- None. Excellent UX/Architecture/PRD alignment.

## Epic Quality Review

### 🔴 Critical Violations
- **Incomplete Epic Coverage**: Several major Phase 2 and 3 requirements are missing from the stories.

### 🟠 Major Issues
- **Vague Acceptance Criteria**: Many stories in Epics 3, 4, 6, 7 use a shorthand format (e.g. "Match submitted -> Undo window expired -> Push sent") rather than the proper BDD Given/When/Then structure.

### 🟡 Minor Concerns
- None.

## Summary and Recommendations

### Overall Readiness Status
**NEEDS WORK**

### Critical Issues Requiring Immediate Action
1. **Fill FR Gaps**: Over 18 FRs are missing from the Epics document, particularly Social & Matchmaking (FR35-40).
2. **Rewrite ACs**: Convert shorthand Acceptance Criteria in later epics to proper BDD (Given/When/Then) format to ensure testability.

### Recommended Next Steps
1. Add a new Epic or stories to cover FR35-FR40 (Social & Matchmaking).
2. Add stories in Epic 6 for Tournament match execution (FR44-FR47).
3. Expand abbreviated ACs in Epic 3, 4, 6, and 7 to full BDD format.

### Final Note
This assessment identified 18 missing functional requirements and some issues with acceptance criteria formatting. Address the critical issues before proceeding to implementation. These findings can be used to improve the artifacts or you may choose to proceed as-is.
