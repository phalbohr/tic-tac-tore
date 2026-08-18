# Epic 4 Context: Individual & Team Analytics

<!-- Generated from planning artifacts. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Epic 4 delivers the analytics and statistics engine that transforms verified match data into competitive intelligence. It provides players with personal and comparative insights — leaderboards, positional breakdowns, team synergies, and head-to-head records — so that hidden skill dimensions become visible and drive engagement. Demo data ensures newcomers experience value immediately, even before accumulating matches.

## Stories

- Story 4.1: Empty State & Demo Data
- Story 4.2: Global Leaderboard with Filtering
- Story 4.3: Positional Statistics (Attack vs. Defense)
- Story 4.4: Team (Pair) Statistics
- Story 4.5: Head-to-Head (H2H) Comparison
- Story 4.6: Unified Match History (My Matches)

## Requirements & Constraints

- Statistics are computed exclusively from PUBLISHED matches; unconfirmed or pending matches are excluded from analytics.
- Leaderboards enforce a minimum games-played threshold before a player becomes visible.
- Results must be paginated with a configurable page size.
- Filtering must support rule system, match type (1v1/2v2), and time period.
- Team statistics expose pair-level performance with teammate-level filtering.
- Head-to-head views present cross-tabulated match/game/goal breakdowns with positional detail.
- Demo/seed data is shown when a user has fewer than 1 confirmed match, with a toggle to hide/show it, and automatically disables after 5 real matches.
- Match history must separate Confirmed and Pending tabs and allow filtering by all players (and, later, by named teams or Favorites).
- Statistics must never compare across incompatible rule configurations; the 3-tier model enforces this: universal metrics (Tier 1), conditional positional stats (Tier 2), and exact-config deep metrics (Tier 3).

## Technical Decisions

- AD-05 (3-Tier Statistics Model): Tier 1 is rule-agnostic (W/L/D, goals total, rank); Tier 2 is conditional (positional stats only where rules permit); Tier 3 is exact-config. Cross-rule comparisons default to Tier 1.
- AD-02 (Isolated Verification Pipeline): Analytics queries only PUBLISHED matches. The verification pipeline is a hard boundary protecting statistics from dirty data.
- AD-01 (Immutable RuleConfiguration): Rule templates are immutable; statistics are always matched by underlying parameter combination, not by template name.
- Statistics are served via dedicated backend analytics services; frontend state is managed through Pinia stores named `use[Name]Store`.
- Feature-based layout: statistics logic lives under `features/stats/`; shared UI primitives under `core/components/` with `ch-` prefixed styles.
- All JSON API responses use camelCase; REST endpoints use kebab-case and plural paths.

## UX & Interaction Patterns

- Progressive disclosure: casual users see win/loss summary; engaged users drill into positional breakdowns; power users filter by exact rule configuration.
- Statistics screens are browse-mode: context-dependent editorial asymmetry is permitted (asymmetric layouts, warm typography), unlike speed-critical flows which remain symmetric.
- Avatar is a universal interactive element: tapping any player avatar opens a quick-stats popover without navigation.
- Micro-celebration after confirmation may reference newly unlocked statistical insight ("Win streak: 4!").
- Home Hub surfaces passive rank-movement insights so users discover statistics without navigating.
- Design system: "The Clubhouse Editorial" — warm dark surfaces, no pure white text, Space Grotesk display / Manrope body, background-shift boundaries (No-Line Rule), editorial off-center placement on browse-mode screens.
- Mobile-first responsive: full-width on mobile, max-width card layout on larger screens.

## Cross-Story Dependencies

- Story 4.2 (leaderboard) and Story 4.6 (match history) depend on the verification pipeline from Epic 3 producing PUBLISHED matches.
- Story 4.4 (team stats) depends on Epic 6 named team groups ("teams") becoming available as a filter.
- Story 4.1 demo data automatically disables after 5 real matches, bridging the gap until organic data accumulation creates meaningful statistics.
