---
title: "Product Brief Distillate: Tic-Tac-Tore"
type: llm-distillate
source: "product-brief-tic-tac-tore.md"
created: "2026-04-04"
purpose: "Token-efficient context for downstream PRD creation"
---

# Product Brief Distillate: Tic-Tac-Tore

## Company & Audience Context

- Internal tool for itemis GmbH (German software company); colleagues play foosball during breaks
- Currently zero match tracking — no spreadsheets, no apps, everything is gut feeling and verbal claims
- Primary audience: ~10-20 office players, mix of casual and competitive; motivated by rivalry and self-improvement
- Product owner (Pavel) is both developer and player — dogfooding from day one
- Communication language: Russian (internal); document/code language: English
- App UI languages: English and German (i18n EN/DE planned)
- No monetization planned; growth signal = organic adoption outside itemis.com domain

## Match Entry — Detailed Scenarios

- **Retrospective entry**: player knows final scores only; optimized for <10 seconds; select players, enter game scores, submit
- **Live match entry (Phase 1.5)**: phone placed flat on foosball table; landscape orientation; screen corners map to player positions on the table; one tap = one goal attributed to scoring player; undo button replaces "confirm goal" step; screen stays awake (wake lock); phone acts as live scoreboard visible to all players
- Live mode is deferred to Phase 1.5 because it adds a complex positional statistics layer that must work correctly across multiple rule systems — not because the UX is difficult
- Both modes support 1v1 and 2v2 formats
- Match can be entered by: a participating player, or a neutral third party (referee/observer)

## Rule Systems — Detailed Configuration

- **ITSF (International)**: Best of 3 or Best of 5; winner-only scoring (1 or 2 points per match)
- **DTFB (German Bundesliga)**: Exactly 2 games; 1 point per game won; match result can be 2:0 or 1:1
- **Custom**: Configurable parameters — win condition, score limit (default 5 or 7), tie-break rule (win by 2 up to X, applies at 4:4), point distribution, position swap rules
- Position rotation rule (2v2): Game 1 free choice, Game 2 mandatory teammate swap, Game 3 free choice — Custom rules may allow free swap between or within games
- Score limit in live play: configurable per rule system
- Cross-rule-system statistics aggregation is the primary technical complexity — different scoring models must not distort comparative statistics

## Match Confirmation — Full Workflow

- **Immutability principle**: confirmed match data cannot be deleted or modified; deletion is as harmful as false data
- **Anti-spam rationale**: auto-confirmation rejected because it enables flooding opponents with fake matches that are impossible to clean up; active confirmation is a deliberate integrity choice
- **Motivation assumption**: if players don't care enough to confirm, the app itself isn't needed — confirmation engagement is a health signal
- **Double-check flow**: (1) notification to confirmer → (2) "I agree with results" → (3) "Are you sure? Data cannot be deleted" → confirmed
- **24-hour cooldown**: after first opponent confirms, result waits 24h before publication; second opponent's confirmation triggers immediate publication
- **1v1 entered by participant**: only opponent confirms (with double-check)
- **1v1 entered by referee**: both players confirm (each with double-check)
- **2v2 standard**: one opponent confirms (first tier, starts cooldown); second opponent confirms → immediate publication
- **2v2 random pairings**: both opponents must confirm individually (individual competition mode)
- **Referee-entered 2v2**: at least one player from each team confirms (with double-check and cooldown), or all 4 players confirm for immediate publication
- **Tournament matches**: 48-hour confirmation window; non-confirmation = technical defeat; repeated violations = tournament removal/ban; statistics preserved for the team that confirmed

## Statistics — Depth & Scope

- **Individual**: goals scored/conceded, win/loss/draw, by position (attack/defense), against specific opponents, against specific positions
- **Team composition**: which pairs of players win together, against which pairs, in which configurations
- **Head-to-head**: full cross-tabulation — attacker-vs-defender, attacker-vs-attacker, defender-vs-defender, defender-vs-attacker
- **Leaderboard columns**: Rank, Player, Points earned, Matches played/won/draw/lost, Games won/lost (by position), Goals scored/conceded (by position)
- Statistics output is one of the simpler features to implement — the complexity is in correct data modeling across rule systems
- Auto-generated narrative insights planned (e.g., "You score 40% more from the back rod than average")

## "Want to Play" Pools — Detailed Design

- Player creates a pool, adds themselves, selects game type (1v1 or 2v2)
- **Fill-based**: pool starts when enough players join (2 for 1v1, 4 for 2v2)
- **Scheduled**: pool has a start time (e.g., 12:30 lunch break); game starts at that time if enough players
- Notifications: mandatory when pool fills; optional when new player joins
- Key value: eliminates the social friction of walking around asking "anyone want to play?"
- This is the "wow feature" for office adoption — colleagues don't need to coordinate manually

## Tournaments — Detailed Design

- **Formats**: Cup (elimination) and Championship (round-robin/league)
- **Team modes**: 1v1 personal, 2v2 with pre-formed fixed teams, 2v2 with random pairings (individual winner across random team compositions)
- **Flexible scheduling**: all tournament matches visible upfront; players play in any order, at their own pace; not tied to specific days
- **Use case**: a player visits the office once a month and wants to play 2-3 of their 15 scheduled tournament matches in one visit
- **Creation parameters**: min/max participants, wait time or fixed start date, format, rule system (score limit, tie-break, point distribution), round count (championship), playoff option
- Tournament matches accessed via New Match → select tournament → see pending matches → choose one to play
- Matches close as they are played and confirmed; tournament completes when all matches are done
- There is real demand at itemis for tournaments, but no technical means to manage them currently

## Achievements System (Phase 4+)

- **Achievements**: streaks (consecutive wins, goals in every match), milestones (100th match, 1000th goal), personal records
- **Anti-achievements**: humorous awards for memorable fails — e.g., "Hummels Medal" for chronic own goals, "Drought Award" for longest goalless streak
- **Critical design principle**: anti-achievements MUST be lighthearted and funny; if they feel like shaming, they become a bullying vector; the goal is laughing together at memorable moments, not a "wall of shame"
- Low priority — adds engagement and atmosphere but not core functionality

## Privacy Design Decisions

- Players use nicknames and custom avatars — not real names or corporate identities
- Google OAuth for authentication only; email addresses not stored or displayed in the application
- No mechanism to correlate player profile with specific employee without personal knowledge of nickname mapping
- Nickname change limit: 1 per month
- Personal cabinet: avatar, nickname, language selection (EN/DE)
- GDPR/Betriebsrat assessment: low risk due to pseudonymization; participation voluntary; app promotes positive office culture
- **Rejected idea**: pre-populating employee roster from company directory — violates DSGVO (processing personal data without consent)

## Technical Context

- **Backend**: Java 21, Spring Boot 3.4.0, layered architecture with three-layer transaction pattern (Service @Retryable → Operation @Transactional → Entity domain logic)
- **Frontend**: Vue 3 + TypeScript, Pinia state management, Vue Router, TailwindCSS
- **Database**: PostgreSQL (Supabase-compatible) in production, H2 for dev; optimistic locking on Match entity with @Retryable
- **Auth**: Google OAuth2 → Backend JWT (HS256, 24h expiry) → localStorage; stateless
- **Design system**: Dark theme — green (#2d5a27 table), red (#a64d32) / yellow (#f1c40f figures), warm brown (#2b2624 brick); Space Grotesk headlines, Manrope body
- **Orientation**: Landscape for match entry screens (Results Entry, Live Match); portrait for everything else
- **No migration framework yet** (Flyway/Liquibase); no CI/CD or Docker
- **Testing**: JUnit 5 + JaCoCo (backend), Vitest + Playwright (frontend)
- Complex analytics via native SQL with CTEs for performance

## Competitive Landscape

- **Kicktrack**: Mobile app, match tracking, rankings, achievements, team stats, Slack integration, tournaments up to 128 players; NO positional stats, NO multi-rule support, NO live tracking, NO matchmaking
- **Kickertool**: Browser-based tournament management (elimination, round robin, MonsterDYP); tournament-only — no match tracking, no stats over time, no mobile-first UX
- **Kicker.cool**: League app with badges, scoring history, win/loss, doubles stats (last 100 matches); limited stats, no tournaments, no rule configurability
- **Foosball Goalkeeper (Tuogol)**: Club-based scoring, 4 game types, leaderboards, team creation, trash-talk bot; no positional stats, no tournaments, no multi-rules
- **Papio (Slack bot)**: Slack-native ELO rankings; locked to Slack, minimal stats, no web UI
- **Multiple GitHub open-source projects**: recurring developer itch, no dominant solution captured the market
- **Key gap across all competitors**: none offer positional statistics or multi-rule-system support

## Market Data

- Global foosball table market: $1.70-1.74B (2023) → $2.73B by 2033 (CAGR ~4.9%)
- Europe (Germany, France, Austria, Switzerland) dominates hardware market and competitive play
- ITSF World Championships: ~500 players from 30+ countries
- Adjacent market (PongUp, ELO Rankings App, RecLeague) validates demand for lightweight competitive tracking in offices
- Post-COVID return-to-office renewed investment in office recreation and social bonding tools
- PWA technology matured (offline, push notifications, installability) — web apps can feel native without app store friction

## Rejected Ideas & Decisions

- **Auto-confirmation of matches**: REJECTED — enables spam/flooding with fake matches; confirmed data is immutable so damage is irreversible
- **Pre-populated employee roster**: REJECTED — violates DSGVO; personal data cannot be used without consent
- **Slack integration in MVP**: DEFERRED — not a priority; may come later but doesn't drive core value
- **Native mobile apps**: DEFERRED — web-first approach to avoid platform-specific development overhead
- **Live Match in MVP**: DEFERRED to Phase 1.5 — cross-rule-system statistics complexity, not UX difficulty
- **Monetization planning**: NOT APPLICABLE at this stage — focus on internal validation first

## Audience Size & Office Context

- Regular daily players: 6-8 people; additional ~4 play occasionally but not daily
- Multiple offices exist within the company — expansion potential beyond one location
- No connectivity issues at the primary office; offline mode not a priority until external adoption creates demand

## Demo/Test Data Strategy

- App should ship with pre-populated demo statistics so new users see what a filled-out experience looks like rather than empty tables
- Demo data removal: either auto-removed after a specific date, or hidden once a player reaches a threshold (~50 matches)
- Until threshold is reached, player can toggle "Show demo statistics" in personal cabinet settings

## Cross-Rule-System Statistics — Resolved Design Direction

- Statistics should include all data from all recorded matches matching the query — no rule-system filtering by default
- Goals and match wins/losses are straightforward across rule systems
- **Draw problem**: draws only exist under DTFB-style rules (exactly 2 games, 1:1 possible); Best-of-3/5 rules cannot produce draws. If a player mostly plays Best-of-3 but occasionally plays DTFB, a small draw percentage in aggregate stats misrepresents their actual draw ratio within draw-eligible formats
- **Proposed solution**: show draws only when filtering by rule systems that allow draws; aggregate view excludes draws from the win/draw/loss percentage bar unless the player has significant draw-eligible match volume
- Full statistical breakdown per data point to be defined in detail during PRD phase, referencing `project-design-description.md`

## Challenge Feature (Resolved)

- Direct player-to-player challenge concept is confirmed as desirable — invite a specific player or group to a match
- Implement within Phase 2 ("Want to Play") if scope allows; otherwise a separate sub-phase
- "Want to Play" pools and direct challenges are complementary: pools = open matchmaking, challenges = targeted invitations

## Slack Integration (Resolved Direction)

- Slack is the corporate messenger at itemis
- Slack integration for "Want to Play" notifications is a good idea but deferred — implement "Want to Play" through the app's own interface first
- Slack integration becomes a follow-up enhancement after core "Want to Play" is validated

## Lightweight Season Standings (Resolved)

- No lightweight "season standings" feature before Phase 3 — avoid scope creep; wait for full tournament implementation
- Focus development effort on core recording and statistics engine

## Open Questions (Remaining)

- How should statistics handle edge cases where Custom rules are modified after matches have already been played under the original configuration?
- Detailed statistical display layout per data point — to be defined in PRD phase using `project-design-description.md` as reference