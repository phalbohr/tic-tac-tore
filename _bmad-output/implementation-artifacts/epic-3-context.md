# Epic 3 Context: Data Verification & Trust

<!-- Generated from planning artifacts. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Implement the match verification pipeline that transforms submitted match results into trusted, immutable statistics. This epic ensures data integrity by requiring opponent confirmation before results are published, protecting the platform from spam and accidental errors. It delivers the core trust mechanism of the product: every statistic on the platform is opponent-verified, preventing poisoned data from contaminating downstream analytics.

## Stories

- Story 3.1: Confirmation Requests & Push Notifications
- Story 3.2: Single-tap Confirmation with Undo Window
- Story 3.3: Match Rejection with Reason
- Story 3.4: Context-Aware Verification Rules
- Story 3.5: Publication Rules & 24-hour Cooldown
- Story 3.6: Submission Rate Limiting (Anti-Spam)

## Requirements & Constraints

- **Functional:** FR12 (confirmation requests), FR13 (single-tap + 15s undo), FR14 (context-aware rules), FR15 (24h cooldown), FR16 (immutability), FR17 (rejection), FR55 (push notifications), FR58 (rate limiting).
- **Rate limiting thresholds:** max 10 standalone match submissions/hour/user; max 30/hour in tournament referee context; 5+ rejections within 24h triggers submission throttle. Thresholds are configurable, not hardcoded.
- **Server-side authority:** All time-based business logic (cooldowns, undo windows, deadlines) must be calculated server-side in UTC. Client displays converted to user's local timezone; client-side countdown is decorative only.
- **Immutable confirmed data:** Confirmed matches cannot be modified or deleted. Emergency data correction requires direct DB access, not UI-exposed.
- **Self-service, zero-admin:** Match confirmation is peer-to-peer. Abuse prevention is automated rate-limiting. No admin role in MVP.
- **Push notification fallback:** If push delivery fails or permission is denied, pending confirmations remain visible via in-app badge on next app open.

## Technical Decisions

- **AD-02: Isolated Verification Pipeline:** Match state machine (`PENDING` -> `CONFIRMED` -> `PUBLISHED`) handled by a dedicated service. Analytics Engine must only query `PUBLISHED` matches.
- **Immutable Match entities:** Confirmed match records are immutable. Direct DB access is restricted to the repository layer.
- **Undo window atomicity:** Confirmation committed server-side only after the 15-second undo window expires without cancellation. Server performs atomic rollback if client cancels; concurrent events resolved via server-side timestamp authority.
- **Idempotency keys:** Every state-changing POST (submit, confirm, reject) carries an idempotency key to dedupe under flaky networks.
- **Optimistic locking:** Concurrent match confirmation handled via optimistic locking with automatic retry. No silent data overwrites.
- **AD-06: PWA-First Infrastructure:** Service Workers handle Push API for match confirmations. Web Push subscription registered at OAuth completion.
- **Backend stack:** Spring Boot 4.0 (Java 21), PostgreSQL, Redis (denylist + rate limit state), Flyway migrations.
- **Frontend stack:** Vue 3 + TypeScript, Pinia (`VerificationQueue` store), Vite 8, Tailwind CSS v4 + SCSS (`ch-` prefix).
- **Error format:** Standard error object: `{ "code": "ERROR_CODE", "message": "Human readable", "details": {} }`.

## UX & Interaction Patterns

- **Single-tap + undo toast:** Confirmation is a single tap followed by a 15-second undo toast. No double-check dialog. Celebration (micro-insight) appears only after the undo window expires — never during.
- **Rich push notifications:** Push body shows full match context (players, scores) so the confirmer can decide without opening the app. Deep link routes to `/match/:id/review`.
- **Asymmetric participation:** One person records, others confirm asynchronously via push. No real-time multi-device sync required.
- **Unified "My Matches" screen:** Single screen with Pending + Confirmed tabs. Unconfirmed matches appear at top with status badge. Pending items have 24h reminder notification to non-confirming opponents.
- **My Matches entry points:** Home Hub "My Matches" button; deep link from confirmation flow; avatar drill from stats.
- **Rejection flow:** Opponent taps Reject, provides required reason string (<=200 chars plain textarea). Match returns to creator's queue with notification. 5+ rejections in 24h triggers throttle warning.
- **Portrait orientation:** Confirmation and review flows use portrait mode. Match entry is portrait; live mode (Epic 5) is landscape.

## Cross-Story Dependencies

- **Story 3.1** depends on **Story 2.4** (match submission with undo window) — a match must be submitted before confirmation requests can be sent.
- **Story 3.2** depends on **Story 3.1** — push notification must be sent before opponent can confirm.
- **Stories 3.4 and 3.5** depend on **Stories 3.1/3.2** — verification rules and cooldown logic apply during the confirmation phase.
- **Story 3.6** is cross-cutting — rate limiting applies at submission time, touching **Story 2.4**'s submit endpoint.
- **Epic 8 (Tournament Management)** depends on **Stories 3.4/3.5** — tournament matches use context-aware verification rules with a 48-hour confirmation window and technical defeat for non-confirmation.
- **Epic 4 (Analytics)** depends on this epic — statistics must not include `PENDING` matches; `PUBLISHED` state is the gating condition.
