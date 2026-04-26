---
stepsCompleted:
  - "step-01-init"
  - "step-02-discovery"
  - "step-01b-continue"
  - "step-02b-vision"
  - "step-02c-executive-summary"
  - "step-03-success"
  - "step-04-journeys"
  - "step-05-domain"
  - "step-06-innovation"
  - "step-07-project-type"
  - "step-08-scoping"
  - "step-09-functional"
  - "step-10-nonfunctional"
  - "step-11-polish"
  - "step-12-complete"
inputDocuments:
  - "_bmad-output/planning-artifacts/product-brief-tic-tac-tore.md"
  - "_bmad-output/planning-artifacts/product-brief-tic-tac-tore-distillate.md"
  - "_project-spec/DESIGN/project-design-description.md"
  - "_project-spec/DESIGN/DESIGN.md"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/pitch_parquet/DESIGN.md"
  - "_project-spec/table-soccer-rules/ITSF_Standard_Matchplay_Rules_2024.md"
  - "_project-spec/table-soccer-rules/ITSF-Regelwerk_Kurzfassung.md"
  - "_project-spec/table-soccer-rules/Regularien_der_Bundesligen_2024.md"
  - "docs/index.md"
  - "docs/project-overview.md"
  - "docs/architecture-backend.md"
  - "docs/architecture-frontend.md"
  - "docs/api-contracts-backend.md"
  - "docs/data-models-backend.md"
  - "docs/component-inventory-frontend.md"
  - "docs/integration-architecture.md"
  - "docs/source-tree-analysis.md"
  - "docs/development-guide.md"
documentCounts:
  briefs: 2
  research: 0
  brainstorming: 0
  projectDocs: 10
  designSpecs: 3
  ruleDocs: 3
classification:
  projectType: "Mobile-First PWA with Dual-Mode Data Entry"
  domain: "Social-Competitive Trusted-Data Analytics Platform"
  domainLayers:
    - name: "Verified Data Capture"
      priority: critical
      description: "Immutable match recording (retrospective → live), confirmation workflow"
    - name: "Multi-Rule Analytics Engine"
      priority: critical
      description: "Cross-system statistics with 3-tier output, unified RuleConfiguration model"
    - name: "Tournament Management"
      priority: important
      description: "Structured competitions with rule enforcement"
    - name: "Social Matchmaking"
      priority: deferred
      description: "Want-to-Play pools, challenges — covered by corporate chat in initial phase"
  complexity: "High (domain: rule system as cross-cutting concern) / Low→Medium (infrastructure, progressive)"
  projectContext: "Brownfield (business logic rewrite on existing architecture)"
keyDecisions:
  - id: "KD-01"
    title: "Unified Rule Model"
    description: "ITSF and DTFB are preset RuleConfigurations. Custom templates are user-created RuleConfigurations. No architectural difference at data level."
  - id: "KD-02"
    title: "3-Tier Statistics Output"
    description: "Tier 1 (universal/rule-agnostic): matches W/L, goals total, rank, points. Tier 2 (conditional): draws (draw-eligible only), games as position (between-games swap only), goals per game avg. Tier 3 (exact config): all metrics within one rule_config_id."
  - id: "KD-03"
    title: "Speed over Precision (Retrospective Mode)"
    description: "Retrospective entry is an intentional trade-off: speed over statistical accuracy. Retrospective + within-game swap = goals recorded without positional attribution (Tier 1 only)."
  - id: "KD-04"
    title: "Local Competition Context"
    description: "Players compare themselves with 5-10 known people, not global database. Each group uses 2-3 rule sets. This collapses theoretical 2592 combinations to practical 2-3 active templates per group."
  - id: "KD-05"
    title: "Filtering UX Strategy"
    description: "Phase 1: Quick filter by rule system (ITSF/DTFB/Custom Template). Advanced parameter-based filter deferred to future phases."
  - id: "KD-06"
    title: "Goals Conceded as Position"
    description: "Marked as team metric, not individual — depends on teammate's defensive ability, not player's own performance."
designPrinciples:
  - "Data integrity is existential — poisoned data kills the product"
  - "Speed over precision for retrospective entry"
  - "Local competition context drives real usage patterns"
  - "Extensible-by-design without overengineering — store data correctly, reveal UI progressively"
workflowType: 'prd'
---

# Product Requirements Document - Tic-Tac-Tore

**Author:** Pavel
**Date:** 2026-04-07

## Executive Summary

Tic-Tac-Tore is a mobile-first web application that transforms office foosball from an ephemeral pastime into a data-driven competitive experience. Built for a group of colleagues who play daily during breaks but retain zero records, the platform records matches, verifies results through opponent confirmation, and produces positional statistics deep enough to reveal skill differences invisible to the naked eye.

The core thesis: friendly competition deserves a history — not oral legends, but verified data that becomes part of office culture. Players at similar skill levels cannot distinguish who excels at defense versus attack, which teammate pairings outperform others, or how they fare against specific opponents. Tic-Tac-Tore makes these hidden layers visible, creating a new dimension of conversation beyond "I played well today." And because foosball is fundamentally a team game, winning together with a partner creates a shared history between two people — a bonding force that individual statistics alone cannot produce.

The product consolidates match recording, statistics, matchmaking ("Want to Play" pools), and tournament management into a single platform — replacing the current patchwork of nothing, spreadsheets, chat channels, and paper notes. Two match entry modes serve different moments: retrospective entry for quick post-game logging (<10 seconds), and live mode where the phone placed flat on the table becomes both a scoreboard and a goal-by-goal protocol — blending physical and digital play into something that feels like augmented reality for foosball.

No existing product in the market offers positional statistics (attack vs. defense performance), multi-rule-system support (ITSF, DTFB, custom house rules), or integrated tournament management with statistical continuity. Tic-Tac-Tore is the first to address the full problem space.

### What Makes This Special

- **Positional statistics nobody else tracks.** Individual and team performance broken down by position (attack/defense), against specific opponents, across specific pairings. This is the analytical depth that turns casual tracking into competitive intelligence — the feature that makes players say "I always felt that, but now I can see it."
- **Live mode as augmented reality.** Phone on the table during play isn't a distraction — it's an extension of the game into the digital world. One tap per goal, per-player positional attribution, live scoreboard visible to all. No post-game data entry, no separate note-taking, no referee with a clipboard.
- **Plays by your rules, not the other way around.** ITSF international, DTFB German Bundesliga, and fully configurable custom rule sets. Every office has house rules — forcing players to adopt a single standard is the #1 adoption killer for generic tracking apps. Tic-Tac-Tore treats rule configurations as first-class data entities with no architectural difference between presets and custom templates.
- **Team history as social glue.** Foosball is a 2v2 game at its core. Shared victories and losses between partners build a narrative that bonds people — statistics reveal not just who is strong individually, but which pairs thrive together and why.
- **One platform replaces four tools.** No chat channels for matchmaking, no spreadsheets for tournaments, no paper for protocols, no separate app for stats. Everything foosball lives in one place.

## Project Classification

- **Type:** Mobile-First Progressive Web Application with Dual-Mode Data Entry (retrospective + live)
- **Domain:** Social-Competitive Trusted-Data Analytics Platform
- **Domain Layers:** Verified Data Capture (critical) → Multi-Rule Analytics Engine (critical) → Tournament Management (important) → Social Matchmaking (deferred)
- **Complexity:** High at domain level (rule system as cross-cutting concern across statistics, match logic, and tournament enforcement); Low-to-Medium at infrastructure level (standard Spring Boot + Vue 3 stack, progressive delivery)
- **Context:** Brownfield — business logic rewrite on existing architecture (3 tables, 7 API endpoints, basic 2v2 recording and approval workflow already implemented; significant rework and expansion required to match target spec)

## Success Criteria

### User Success

- **The "That's me on top" moment.** Every player finds at least one statistical dimension where they excel — whether it's overall ranking, best defender, top scorer against a specific opponent, or champion of a particular rule system. The depth of statistics is not analytical overhead; it is a recognition engine that gives significance to every participant.
- **Hidden skills become visible.** Players who feel they are strong in defense can now prove it with data. Attackers who dominate are visible by default; defenders and niche performers need statistics to earn the same recognition.
- **Shared history with partners.** Team statistics reveal which pairings work and why — creating conversational hooks between teammates that extend beyond the match itself.
- **From gut feeling to evidence.** "I think I play better in Best of 5" or "I always lose to Alex" becomes verifiable — statistics replace speculation and fuel meaningful discussion.

### Business Success

**8-week adoption ladder (Phase 1 validation):**

1. All matches the product owner plays are recorded by opponents willingly
2. Colleagues record matches played without the product owner present
3. 4–5 players in the office have the app installed and use it regularly
4. At least one player outside the home office registers — organic word-of-mouth signal
5. One tournament successfully conducted using the app exclusively (no spreadsheets, no paper)

**12-month success indicators:**
- New matches recorded daily, including from players outside the home office
- Regular "Want to Play" pool creation (social matchmaking actively used)
- At least one tournament conducted outside the home office

**Growth signal:** Registration and active use by players with no direct connection to the original office group — proves the product has value beyond the initial context.

### Technical Success

- **Data integrity from day one.** Confirmation workflow, double-check, immutability, and 24-hour cooldown are non-negotiable MVP requirements. Poisoned statistics at any point — early or late — contaminate the entire dataset irreversibly.
- **Sub-10-second retrospective entry.** Match recording must be fast enough that players record immediately after playing, not "later" (which means never).
- **Cross-rule-system statistical accuracy.** Statistics aggregated across ITSF, DTFB, and custom rules must never produce misleading comparisons. The 3-tier statistics model (universal → conditional → exact config) ensures clean separation.
- **Mobile-first performance.** Core flows (match entry, statistics viewing, confirmation) must be responsive and usable on smartphone screens without horizontal scrolling in portrait mode.

### Measurable Outcomes

| Metric | Target | Timeframe |
|--------|--------|-----------|
| Match recording adoption (home office) | ≥60% of regular players (2+/week) | 8 weeks |
| Confirmation rate | >90% of submitted matches confirmed | 8 weeks |
| Weekly statistics page views | Active weekly usage by registered players | 8 weeks |
| App installations (home office) | 4–5 players | 8 weeks |
| External registrations | ≥1 player outside home office | 8 weeks |
| Daily new matches (including external) | At least 1/day | 12 months |
| External tournaments | ≥1 conducted outside home office | 12 months |

## Product Scope

### MVP — Minimum Viable Product

The recording and verified statistics engine. Match data is the atomic unit — everything else is a multiplier on top. For detailed MVP feature justification and parallelization strategy, see [Project Scoping & Phased Development](#project-scoping--phased-development).

- **Retrospective match entry** (1v1 and 2v2) with kicker table top-view UI
- **Multiple rule system support** (ITSF, DTFB, Custom) — unified RuleConfiguration model
- **Match confirmation workflow** — double-check, 24-hour cooldown, immutability (full Verified Match Data spec)
- **Individual statistics** — leaderboard with positional breakdown (attack/defense), points, W/L/D
- **Team statistics** — pair-level performance tracking
- **Head-to-head statistics** — cross-tabulated match/game/goal breakdowns between specific players
- **Basic filtering** — by rule system, match type (1v1/2v2), time period
- **Google OAuth2 authentication** with pseudonymized profiles (nickname + avatar)
- **Personal cabinet** — avatar, nickname (1 change/month), language selection (EN/DE)

### Growth Features (Post-MVP)

- **Live Match Mode (Phase 1.5)** — real-time goal-by-goal entry with landscape table-top UI, per-goal positional attribution, extended positional statistics layer
- **"Want to Play" pools (Phase 2)** — fill-based and scheduled matchmaking, notifications
- **Direct challenges (Phase 2)** — targeted player-to-player match invitations
- **Favorites and saved teams (Phase 2)** — quick team selection, default configurations

### Vision (Future)

- **Tournament system (Phase 3)** — cup/championship formats, 1v1/2v2/random pairings, flexible scheduling, 48-hour confirmation enforcement, tournament-specific statistics
- **Achievements and anti-achievements (Phase 4+)** — a full system of badges, pennants, cups, and other foosball paraphernalia. Player award wall showcasing collected achievements. Streaks, milestones, humorous anti-achievements (lighthearted celebration of memorable fails, not shaming)
- **Progress tracking** — starting with small auto-generated insights ("You score 40% more from defense than average"), evolving into comprehensive progress tracking with trend analysis and skill development visualization
- **Match broadcast narration** — transforming match protocols (goal sequences, game scores, momentum shifts) into text, audio, or animated commentary in the style of a real football broadcast. Players become heroes of their own "big match" — narrated by a commentator, complete with dramatic buildup and play-by-play. The ultimate engagement layer: +10 to epicness

## User Journeys

### Journey 1: "The Defender's Vindication" — Max, The Competitor

**Who:** Max has been playing foosball at the office for two years. He knows he's one of the strongest defenders — he can feel it in every match. But nobody talks about defense. The attackers get the glory, the high-fives after goals, the bragging rights. Max's contributions are invisible.

**Opening Scene:** Lunch break, another 2v2 game. Max's partner scores the winning goal and gets the credit. Max held the defense the entire match — three critical saves — but nobody noticed. "I'm telling you, I'm the best defender here," he says. His colleague laughs: "Prove it."

**Rising Action:** Max opens Tic-Tac-Tore. He's been recording matches for three weeks now. He navigates to Statistics → Individual → sorts by defensive metrics. There it is: his name at the top of the "Goals Conceded as Defender" column — fewest goals let through per match. He scrolls to Head-to-Head: against the office's best attacker, Max has the highest defensive win rate of anyone.

**Climax:** Max screenshots his stats and drops it in the group chat. "Still think I can't prove it?" The conversation explodes — teammates start checking their own positional stats, discovering surprising strengths and weaknesses. The office's "best attacker" discovers he actually performs better in defense against one specific opponent.

**Resolution:** Recording matches is no longer a chore — it's building Max's case. Every match adds data to his story. He starts caring about which rule system they play because his Best-of-5 stats look even better than Best-of-3. The statistics didn't just prove his point — they changed how the entire group talks about the game.

**Capabilities revealed:** Individual positional statistics, head-to-head cross-tabulation, leaderboard sorting by column, rule system filtering, shareable stats.

---

### Journey 2: "The Lunch Break Pool" — Lisa, The Social Player

**Who:** Lisa plays foosball casually — maybe twice a week when she catches people at the table. She doesn't care about rankings, but she enjoys the social break. Her problem: she often walks to the foosball room and nobody's there, or she's working and misses a game happening without her.

**Opening Scene:** Lisa sees a notification on her phone: "A Want to Play pool needs 1 more player for a 2v2 match. Join now?" She taps it, joins, and within five minutes gets another notification: "Pool full — head to the table!" She finishes her task and walks down. All four players are there, ready to play.

**Rising Action:** After the match, her partner records the result in under 10 seconds. Lisa gets a confirmation request — she taps "Confirm," double-checks, done. She doesn't think about statistics or rankings — but the next time she's bored, she creates her own pool: "2v2, anyone free after 14:00?" Two people join within the hour.

**Climax:** Over the following weeks, Lisa notices she's playing more often — not because she seeks out games, but because the pools come to her. She opens the app one day and discovers she's accumulated 30 confirmed matches. Out of curiosity, she checks her stats and sees she has a surprisingly high win rate when paired with one specific colleague. She messages him: "Hey, apparently we're an unbeatable duo — want to sign up for the office tournament as a team?"

**Resolution:** Lisa never set out to become competitive, but the frictionless matchmaking turned her from a casual drop-in player into a regular with a team partner she discovered through data. The pool system didn't just find her games — it created relationships.

**Capabilities revealed:** Want to Play pools (fill-based and scheduled), push notifications, quick confirmation flow, passive statistics accumulation, team composition discovery.

---

### Journey 3: "The Championship Without Spreadsheets" — Oleg, The Organizer

**Who:** Oleg has been trying to organize an office foosball championship for months. He has a spreadsheet draft, half-collected registrations via email, and no idea how to verify match results when he's not physically present.

**Opening Scene:** Oleg opens Tic-Tac-Tore and taps "Create Tournament." He sets the parameters: Championship format, 2v2 with fixed teams, DTFB rules (the group's preferred rule set for competitions), minimum 6 teams, registration open for one week. He posts the tournament link in the office chat: "Sign up — first official championship, no spreadsheets required."

**Rising Action:** Teams register through the app. Partners receive confirmation notifications — both must accept. After one week, 8 teams are registered. The tournament generates the full round-robin table automatically. Matches are visible to all participants — they can play in any order, at their own pace, whenever both teams are free. Results are entered via the app and confirmed through the standard double-check workflow. The 48-hour confirmation window keeps things moving.

**Climax:** Three weeks in, all but two matches are played. The leaderboard is live — every office break becomes a conversation about standings. One team that was written off stages a comeback with three consecutive wins. The tournament statistics page shows who the MVP is, which team has the tightest defense, and which matchup produced the most dramatic games. Oleg hasn't touched a single spreadsheet.

**Resolution:** The championship concludes with a clear winner and a complete statistical archive. Oleg immediately starts planning the next one — this time a cup format. Several colleagues from another office floor ask: "Can we join next time?" Oleg sends them the app link.

**Capabilities revealed:** Tournament creation with configurable rules, team registration with partner confirmation, automatic match table generation, flexible scheduling, 48-hour confirmation enforcement, tournament-specific statistics and leaderboard, tournament archive.

---

### Journey 4: "Wait, What's This App?" — Kai, The Newcomer

**Who:** Kai just joined the company. On his second day, he sees colleagues gathered around the foosball table. Someone scores and another player taps their phone lying flat on the table. "What are you guys doing?"

**Opening Scene:** "We're recording the match live — want to play next?" Kai joins the next game. Afterward, a colleague says: "Download the app, I'll add you to the match." Kai signs in with Google, picks a nickname ("Kai-zer"), chooses an avatar, and is greeted by a quick tutorial: "Welcome! Here's what you can do — challenge friends, join a pool, record matches live, check your stats, enter a tournament."

**Rising Action:** Kai's first match is already recorded — his colleague entered it, and Kai gets a confirmation notification. He reviews the score, taps "Confirm," then "Are you sure? This data is permanent." Confirmed. Over the next week, Kai plays four more matches. Each time, someone else enters the result, and Kai just confirms. He hasn't recorded a single match himself yet.

**Climax:** After a week, Kai opens Statistics out of curiosity. He has 5 matches, a 60% win rate, and he's ranked 8th overall. He notices he's won every match where he played defense but lost both matches as attacker. He didn't know that about himself. The next game, he asks: "Can I play defense this time?"

**Resolution:** Kai went from "what's this app?" to making position choices based on data in one week — without ever reading a manual. The onboarding was organic: watch others use it, get added to a match, confirm, discover your own stats.

**Capabilities revealed:** Google OAuth onboarding, tutorial slides, organic first-match experience (confirmation-first), passive stat accumulation, positional self-discovery, low barrier to entry.

---

### Journey 5: "Twenty Matches Before Lunch" — Viktor, The Referee

**Who:** Viktor volunteered to referee the office tournament semifinals. He's standing at the end of the table with his phone in hand — portrait orientation, since he's at the narrow end.

**Opening Scene:** Viktor opens the tournament, selects the semifinal match, and enters live mode. The UI detects he's not one of the four registered players and switches to portrait referee view. The four player positions are visible on screen. The match begins.

**Rising Action:** Goal scored by Team A's attacker — Viktor taps the corresponding quadrant, and the goal is immediately registered. The scorer's initials appear on the activity timeline. If Viktor misidentifies the scorer, one tap on the crossed-out ball icon removes the last goal. Game 1 ends 5:3. The app automatically starts Game 2 with mandatory position swap. Viktor records 6 more matches in the tournament bracket that afternoon — all confirmed by players via double-check within hours.

**Climax:** At the end of the day, the tournament bracket is half-complete. Every match has a full goal-by-goal protocol with positional attribution. Viktor didn't need a clipboard, a spreadsheet, or a pen. He just tapped his phone.

**Resolution:** The tournament archive contains richer data than any paper protocol could — not just scores, but who scored from which position against whom.

**Open Question:** Should the referee's identity be recorded in the match protocol? In official foosball, referees are documented. In this app, if a referee makes an error and both teams reject, the system currently shows the match as entered by "an unknown player." Recording the referee provides accountability; omitting them keeps the system simpler. **This requires further discussion before implementation.**

**Capabilities revealed:** Referee/third-party match entry, portrait orientation for live mode (referee view), tournament match selection, automatic position swap enforcement, goal-by-goal protocol, batch tournament recording.

---

### Journey 6: "The Badge Collector" — Anna, The Achievement Hunter (Future)

**Who:** Anna plays foosball regularly but what really drives her isn't the ranking — it's collecting things. When she heard about the achievement system, she opened her award wall immediately.

**Opening Scene:** Anna notices a new badge notification: "Iron Wall — Block 50 goals as defender without conceding more than 3 in a single game." She's at 47/50. Three more defensive games and it's hers.

**Rising Action:** Anna starts specifically requesting to play defense — not because she loves defense, but because she wants the badge. She checks her award wall: 12 badges, 2 pennants, 1 cup from winning the summer championship. She sees a locked badge: "Nemesis — Beat the same opponent 10 times in a row." She's at 7 against Kai. She creates a Want to Play pool hoping Kai joins.

**Climax:** Game 50 as defender. She concedes only 2 goals. The badge appears with a satisfying animation. But then — a surprise notification: "Congratulations! You've also earned: 'Hummels Medal' — concede 5 own goals in a single tournament." Anna bursts out laughing. She didn't even notice she'd earned the anti-achievement.

**Resolution:** Anna's award wall becomes a conversation piece. Colleagues browse each other's collections, compete for rare badges, and laugh together about anti-achievements. The achievement system isn't just gamification — it's a story engine that gives every match additional stakes.

**Capabilities revealed:** Achievement/badge system, award wall, progress tracking toward badges, anti-achievements (humorous, not shaming), notification system for achievements, social sharing of collections.

---

### Edge Case: "The Spam Problem" — Automated Abuse Prevention

**Scenario:** A disgruntled player submits 20 fake match results against a colleague. None will be confirmed, but the victim receives 20 confirmation requests — a nuisance.

**System Response (automated, no admin intervention):**
- Rate limiting on unconfirmed submissions: if a player accumulates N rejected/expired match submissions within a timeframe, submission privileges are temporarily throttled
- Tournament context exception: during active tournaments, referees may legitimately submit 20–30 results per day — rate limits must account for tournament roles
- Repeated rejection patterns trigger a warning to the submitter
- No manual admin intervention required — rules are fixed and automated

**Resolved:** Rate-limiting thresholds defined in Non-Functional Requirements > Security: max 10 standalone submissions/hour/user, max 30/hour in tournament referee context, 5+ rejections within 24h triggers throttle. Thresholds are configurable.

**Capabilities revealed:** Automated abuse prevention, context-aware rate limiting, no admin dependency.

---

### Journey Requirements Summary

| Journey | Key Capabilities Revealed |
|---------|--------------------------|
| Max (Competitor) | Positional stats, H2H cross-tabulation, leaderboard sorting, rule filtering, shareable stats |
| Lisa (Social Player) | Want to Play pools, notifications, quick confirmation, passive stat accumulation, team discovery |
| Oleg (Organizer) | Tournament creation, team registration, auto-generated brackets, flexible scheduling, tournament stats |
| Kai (Newcomer) | OAuth onboarding, tutorial, confirmation-first experience, organic stat discovery, low entry barrier |
| Viktor (Referee) | Third-party entry, portrait live mode, tournament match selection, batch recording |
| Anna (Achievement Hunter) | Badges, award wall, progress tracking, anti-achievements, social collection browsing |
| Abuse Prevention | Automated rate limiting, context-aware thresholds (tournament vs. standalone), no admin role |

**Cross-cutting insight:** The platform is designed to be fully self-service with no admin role. All moderation — confirmation, abuse prevention, tournament enforcement — is handled through automated rules. Manual intervention is a last resort, not an operational model.

## Domain-Specific Requirements

### Data Integrity & Immutability

- **Match immutability is absolute.** Confirmed match data cannot be modified or deleted under any circumstances. This is the foundational trust guarantee — without it, statistics are meaningless and the product has no value.
- **Account deletion preserves match history.** When a player deletes their account, their personal data (email, nickname, avatar) is removed. Match records are reassigned to a placeholder identity (e.g., "ex-player-0042") with a standardized "boots hung on a nail" avatar — the universal football symbol for retirement. This keeps deleted players visually distinct and culturally recognizable without requiring explanation. The player's historical statistics become anonymous but the match graph remains intact.
- **No retroactive data modification.** A player cannot invoke any right — legal or otherwise — to alter match results or statistics. The platform records objective outcomes confirmed by multiple parties; these are facts, not personal opinions subject to editing.

### Privacy & DSGVO Compliance

- **Pseudonymization by design.** Players are identified by self-chosen nicknames and avatars. Google OAuth email addresses are used exclusively for authentication and are not displayed, shared, or accessible within the application.
- **Right to erasure (GDPR Art. 17).** Account deletion removes all personal data: email, nickname, avatar, OAuth provider ID. Match statistics are retained in anonymized form (placeholder identity). This approach follows established industry precedent (Hattrick, Lichess, Chess.com, EA Sports). Legal basis:
  - **Anonymization as erasure:** Regulators (e.g., Austrian DPA) confirm that replacing personal data with irreversible dummy data constitutes legitimate fulfillment of Art. 17 right to erasure. Once fully anonymized, data falls outside GDPR scope entirely.
  - **Art. 17(3)(b):** Processing necessary for compliance with a legal obligation or performance of a task in the public interest.
  - **Art. 17(3)(e):** Processing necessary for the establishment, exercise, or defense of legal claims (e.g., retaining ban records to prevent repeat violations).
  - **Legitimate Interest / Art. 89 (Statistical Purposes):** Game-state data affecting other participants' rights justifies retention as shared records whose removal would destroy service integrity and statistics. No explicit "shared data exception" exists in GDPR text, but industry practice consistently uses legitimate interest and statistical purposes as legal bases.
- **Critical: Anonymization vs. Pseudonymization.** If any key exists that could re-associate a placeholder identity with a real person, the data remains pseudonymized and subject to GDPR. The account deletion process must ensure the mapping is **irreversibly destroyed** — no recovery path, no audit trail linking placeholder to original account.
- **Transparency obligation.** The Privacy Policy must inform users at registration that match history will be preserved in anonymized form upon account deletion. This is a legal requirement, not optional.
- **Pre-launch requirement: Legitimate Interest Assessment (LIA).** A documented LIA must be prepared before production launch, balancing the platform's interest in preserving game-state integrity against the individual's right to erasure.
- **No pre-populated user data.** User roster is never sourced from company directories or external systems. All registration is voluntary and self-initiated. Rejected design: pre-populating employee roster from company directory violates DSGVO (processing personal data without consent).
- **Minimal data collection.** Only data necessary for the application's function is collected: authentication credentials, user profile (nickname, avatar, language preference), and match data.

### Rule System Consistency

- **Rule templates are immutable.** Once a rule template is created (with a specific combination of parameters: win condition, score limit, tie-break, point distribution, position swap rules), its parameters cannot be changed. Modifying settings creates a new template. This eliminates the problem of statistical drift — every match played under a template uses exactly the same rules forever.
- **Templates as named pointers to parameter combinations.** The parameter space is finite (~2,500 combinations per KD-04). Each template is a user-assigned name pointing to one specific combination. Two templates with different names but identical parameters reference the same statistical pool. ITSF and DTFB are built-in named presets pointing to specific parameter combinations — architecturally identical to custom templates (KD-01).
- **Statistics aggregation by parameter combination, not by template name.** When filtering statistics by rule system, the system matches on the underlying parameter combination, not the template label. This ensures that identically-configured rules from different groups produce comparable statistics.

### Domain-Specific Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Poisoned data (confirmed false results) | Irreversible — corrupts all downstream statistics | Double-check confirmation workflow; 24h cooldown; immutability makes both creation and correction deliberate |
| Statistical distortion across rule systems | Misleading comparisons, loss of trust in data | 3-tier statistics model (KD-02); rule-agnostic Tier 1, conditional Tier 2, exact-config Tier 3 |
| Template parameter drift | Incomparable statistics under "same" template | Immutable templates; parameter change = new template; stats matched by parameter combination |
| Account deletion data loss | Broken match graph, orphaned statistics | Placeholder identity ("boots on a nail" avatar); irreversible anonymization; match graph preserved |
| DSGVO compliance gap | Legal exposure if anonymization is reversible | Irreversible destruction of mapping; LIA before launch; transparent Privacy Policy |
| Spam/abuse (fake match submissions) | User annoyance, notification fatigue | Automated rate limiting; context-aware thresholds (tournament vs. standalone); no admin dependency |
| Ban evasion (re-registration after ban) | Repeat offenders circumvent enforcement | Art. 17(3)(e) permits retention of device hashes/unique IDs for ban enforcement |

## Innovation & Novel Patterns

### Detected Innovation Areas

**1. Live Data Capture → Positional Statistics Pipeline (Core Innovation)**

These two capabilities are inseparable: no competitor offers real-time goal-by-goal recording with per-player positional attribution during a foosball match. This is what makes positional statistics possible — and positional statistics are what make Tic-Tac-Tore fundamentally different from every existing tracker. The innovation is not in any single technique, but in the end-to-end pipeline: phone on table → one tap per goal → automatic positional attribution → attack/defense cross-tabulation → H2H breakdowns that reveal hidden skill layers. No existing product connects these dots because none starts with live positional capture.

**2. Multi-Rule-System Architecture (Problem-Driven Innovation)**

Born from a real observation: 8–10 players in one office use 2–3 different rule sets. Every existing foosball app assumes a single rule system. Tic-Tac-Tore treats rule configurations as first-class data entities (~2,500 parameter combinations), making ITSF, DTFB, and custom house rules architecturally identical. This is not a theoretical feature — it solves an immediate, practical problem that blocks adoption of competing products.

**3. Real-Player Match Broadcast — AI-Powered (Future — Unique Twist on Existing Concept)**

Text match narration is not new — Hattrick.org has generated text broadcasts from computer-simulated matches since 1997. But in 15 years, virtually nothing has changed there: the same text-based narration of computer-simulated play. Tic-Tac-Tore's innovation is twofold:

- **Real matches, real people.** Four colleagues who just played during lunch break become the heroes — possibly comic book superheroes — of a professional-style match narration. This is not "reading a game report" but "reliving your own match through the lens of sports commentary."
- **AI-generated audio and animation.** Modern AI technology enables what was impossible when Hattrick launched: generated voice commentary with professional intonation, and animated match visualizations. The leap from text to audio/animation fundamentally changes the emotional impact.

**The deeper effect: perception shift.** A player who hears their match narrated as an epic battle — with commentary about "brilliant defensive saves" and "clinical finishing from the attack" — will never see the foosball table the same way again. Next time they stand at the table, the ball moving between figures is no longer just a ball. It's a pass, a feint, a shot. The broadcast doesn't just document the match — it rewires how players experience future matches. The physical game becomes richer because the digital layer gave it narrative meaning.

### Market Context & Competitive Landscape

| Capability | Tic-Tac-Tore | Kicktrack | Kickertool | Kicker.cool | Foosball Goalkeeper |
|-----------|-------------|-----------|------------|-------------|-------------------|
| Live goal-by-goal recording | Yes (phone on table) | No | No | No | No |
| Positional statistics (attack/defense) | Yes (from live mode) | No | No | No | No |
| Multi-rule-system support | Yes (ITSF/DTFB/Custom) | No | No | No | No |
| Tournament management | Yes (Phase 3) | Limited | Yes (core feature) | No | No |
| Matchmaking pools | Yes (Phase 2) | No | No | No | No |
| Match broadcast narration | Yes (Future) | No | No | No | No |
| Integrated statistics + tournaments | Yes | No | No | No | No |

**Key gap across all competitors:** None offer positional statistics or multi-rule-system support. These are the two capabilities that define Tic-Tac-Tore's competitive moat.

### Validation Approach

| Innovation | Validation Method | Success Signal |
|-----------|------------------|----------------|
| Live capture + positional stats | MVP Phase 1.5: deploy live mode, measure adoption vs. retrospective entry | >30% of matches recorded in live mode within 4 weeks of availability |
| Multi-rule support | MVP Phase 1: offer ITSF/DTFB/Custom from day one, track which rules are used | Multiple rule systems actively used by the same player group |
| Match broadcast narration | Prototype with text narration for 10 real matches; gather qualitative feedback | Players voluntarily share/replay broadcasts; "this is amazing" reactions |

### Risk Mitigation

| Innovation Risk | Fallback |
|----------------|----------|
| Live mode too disruptive during play (players don't want to tap phone) | Retrospective entry remains the primary mode; live mode is additive, not required |
| Positional statistics too complex to display clearly | Start with simple positional win rates; add depth incrementally as users demonstrate demand |
| Multi-rule aggregation produces confusing statistics | 3-tier statistics model (KD-02) isolates cross-rule comparisons; default view is rule-agnostic Tier 1 |
| Match broadcast narration feels gimmicky | Test with a small group first; treat as engagement layer, not core feature; can be deprioritized without product impact |

## Web Application (PWA) Specific Requirements

### Project-Type Overview

Tic-Tac-Tore is a Single-Page Application (Vue 3) delivered as an installable Progressive Web App. Mobile-first design with responsive support for larger screens. No native apps in current scope. PWA covers all required device capabilities for Phases 1–4. Native mobile apps (iOS/Android) remain a long-term possibility contingent on adoption scale and available resources — this is an indie project, and the decision will be revisited after full PWA feature parity is achieved.

### Browser & Device Matrix

| Platform | Priority | Browsers | Notes |
|----------|----------|----------|-------|
| **Mobile (primary)** | Critical | Chrome (Android), Safari (iOS) | Core experience — match entry, confirmation, pools |
| **Tablet** | Important | Chrome, Safari | Statistics viewing benefits from larger screen |
| **Desktop** | Supported | Chrome, Firefox, Safari, Edge | Statistics, tournament management, deep analytics |

- **Minimum browser versions:** Latest 2 major versions of each supported browser
- **Target screen sizes:** 360px (mobile) → 1440px (desktop)
- **No IE support**

### Responsive Design Strategy

| Screen Context | Orientation | Layout Approach |
|---------------|-------------|-----------------|
| Match entry (retrospective) | Landscape (mobile) | Kicker table top-view, full-width |
| Live match (player) | Landscape (mobile) | Kicker table top-view, wake lock active |
| Live match (referee) | Portrait (mobile) | Adapted referee view from table end |
| Statistics, leaderboards | Portrait (mobile) / Responsive (desktop) | Tables with horizontal scroll on mobile; full layout on desktop |
| Home hub, pools, tournaments | Portrait (mobile) / Responsive (desktop) | Card-based responsive grid |
| Personal cabinet | Portrait (mobile) / Responsive (desktop) | Simple form layout |

**Key principle:** Statistics are designed mobile-first but must be genuinely comfortable on large screens — this is where users will do deep analysis.

### PWA Capabilities

| Capability | MVP | Post-MVP | Notes |
|-----------|-----|----------|-------|
| **Installability** | Yes | — | Add to Home Screen, app-like experience |
| **Push notifications** | Yes | — | System-level notifications via Service Worker + Push API. Critical for: pool fill alerts, confirmation requests, tournament updates |
| **Screen wake lock** | Yes | — | Prevents screen dimming during live match mode |
| **Orientation lock** | Yes | — | Force landscape for match entry/live mode on mobile |
| **Offline support** | No | Future | Not needed — home office has reliable connectivity. Add when external adoption creates demand |
| **Camera access** | No | — | Not needed — avatar is uploaded as image file, not captured |
| **Background sync** | No | Future | Could enable offline match entry queuing in future |

### Push Notification Strategy

Push notifications are essential for the social features to work — users must not need to be actively in the app.

| Event | Notification | Priority |
|-------|-------------|----------|
| New pool created (matching user's preferences) | "A new 2v2 pool is looking for players" | MVP (Phase 2) |
| Pool filled (user is a member) | "Your pool is full — head to the table!" | MVP (Phase 2) |
| Match confirmation request | "Player X submitted results for your match — confirm?" | MVP |
| Match rejected (returned to creator) | "Your match was rejected — review and resubmit" | MVP |
| Tournament registration invite (team partner) | "Player X registered you for a tournament — confirm?" | Phase 3 |
| Tournament match available | "Your next tournament match is ready to play" | Phase 3 |
| Achievement unlocked | "You earned: Iron Wall!" | Phase 4+ |

**Implementation:** PWA Push API via Service Worker. Requires HTTPS, user permission grant, and a push notification service (e.g., web-push library or Firebase Cloud Messaging).

### Performance Targets

See [Non-Functional Requirements > Performance](#performance) for detailed metrics, degradation tiers, and regression testing strategy.

### SEO Strategy

Minimal — the application is behind authentication with no public content to index.

- Basic meta tags on the landing/login page (title, description, Open Graph) for link sharing
- No sitemap, no structured data, no content indexing strategy needed
- If a public-facing marketing page is added later, SEO can be revisited

### Accessibility

See [Non-Functional Requirements > Accessibility](#accessibility) for WCAG targets, day-one foundations, and post-MVP trigger.

### Implementation Considerations

- **Tech stack confirmed:** Vue 3 + TypeScript (SPA), Spring Boot 3.4.0 + Java 21 (API), PostgreSQL (production), H2 (development)
- **Service Worker scope:** Push notifications (MVP), caching strategy (post-MVP), offline support (future)
- **Image upload for avatars:** Client-side resize/crop before upload; server stores optimized thumbnails
- **i18n from day one:** English primary, German secondary (EN/DE). Vue i18n plugin with externalized string resources

## Project Scoping & Phased Development

### MVP Strategy & Philosophy

**MVP Approach:** Problem-solving MVP — the minimum that makes colleagues start recording matches and see statistics they couldn't access before.

**Resource Context:** Indie project with augmented development capacity. The product owner is the primary developer, architect, and player. Additionally, Google Jules (Gemini 3.1 Pro, up to 100 autonomous tasks/day, 15 concurrent) is available for parallelizable work. This shapes the delivery strategy: within each phase, tasks are structured for maximum parallelism — isolated by module/file boundary to avoid merge conflicts. The primary developer focuses on architecture decisions, complex integrations, and domain logic while Jules handles well-scoped, isolated implementation tasks.

**Parallelization Strategy:**

| Track | Owner | Characteristics |
|-------|-------|----------------|
| **Architecture & Integration** | Primary developer | Schema design, cross-module wiring, complex business logic, code review |
| **Backend modules** | Jules (parallelizable) | Isolated service/controller/repository implementations per feature |
| **Frontend components** | Jules (parallelizable) | Isolated Vue components, views, stores — each in separate files |
| **Tests** | Jules (parallelizable) | Unit and integration tests for completed modules |
| **Configuration & i18n** | Jules (parallelizable) | Translation files, config setup, migration scripts |

**Merge-conflict avoidance principles:**
- Each Jules task targets a distinct file or module — no two tasks touch the same file
- New features are built in new files; existing files modified only by the primary developer or in sequential (non-parallel) batches
- Backend and frontend are naturally isolated (different directories) and can be developed in parallel
- Interface contracts (DTOs, API specs) are defined by the primary developer first, then implementations are parallelized

**Key principle:** Multi-rule support is an MVP requirement, not a deferrable feature. The first users play with 2–3 different rule sets — launching without this would force them to change behavior, which is the #1 adoption killer.

### MVP Feature Set (Phase 1)

**Core User Journeys Supported:**
- Max (Competitor) — record matches, view positional stats, compare with rivals
- Kai (Newcomer) — sign up, get added to a match, confirm, discover stats
- Partial: Lisa (confirmation-first experience, passive stat accumulation)

**Must-Have Capabilities:**

| Capability | Justification |
|-----------|--------------|
| Retrospective match entry (1v1 + 2v2) | Atomic unit of the product — without matches, nothing exists |
| Kicker table top-view UI (landscape) | The signature interaction; differentiates from generic forms |
| Multiple rule systems (ITSF, DTFB, Custom) | First users use 2–3 rule sets; immutable templates as parameter pointers |
| Match confirmation workflow (full spec) | Data integrity from day one — non-negotiable |
| Individual statistics with positional breakdown | The "That's me on top" moment; recognition engine |
| Team statistics (pair-level) | "Shared history as social glue" — core value proposition |
| Head-to-head statistics | Cross-tabulated breakdowns; "I always lose to Alex" becomes verifiable |
| Leaderboard with filtering (rule system, match type, period) | Entry point for statistics discovery |
| Google OAuth2 authentication | Secure, frictionless sign-up |
| Personal cabinet (nickname, avatar, language EN/DE) | Pseudonymized identity; DSGVO compliance |
| Push notifications (confirmation requests) | Users must not miss confirmation requests |
| Onboarding tutorial slides | Smooth newcomer experience |
| Played matches screen (history + confirmation queue) | Match lifecycle visibility |

**Explicitly NOT in MVP:**
- Live match mode (Phase 1.5)
- Want to Play pools (Phase 2)
- Direct challenges (Phase 2)
- Favorites and saved teams (Phase 2)
- Tournaments (Phase 3)
- Achievements (Phase 4+)
- Match broadcast narration (Future)
- Offline support (Future)

### Phased Development Roadmap

```
Phase 1 (MVP)                    Phase 1.5                     Phase 2
─────────────────────────────    ─────────────────────────     ─────────────────────────
Retrospective entry (1v1/2v2)    Live match mode (landscape)   Want to Play pools
Multi-rule system (ITSF/DTFB/    Referee view (portrait)       Direct challenges
  Custom templates)              Per-goal positional            Favorites & saved teams
Confirmation workflow (full)       attribution                  Push notifications
Individual stats + positional    Extended positional              (pools, challenges)
Team stats                         statistics layer
H2H stats
Leaderboard + filtering
Personal cabinet + OAuth
Push (confirmations)
Onboarding tutorial
Match history + confirmation
  queue

Phase 3                          Phase 4+                      Future
─────────────────────────────    ─────────────────────────     ─────────────────────────
Tournament system                Achievements & anti-          Match broadcast narration
  (cup/championship)               achievements                 (AI audio/animation)
Tournament modes (1v1,           Badge/pennant/cup system      Offline support
  2v2 fixed, 2v2 random)        Player award wall              Native apps (contingent)
48h confirmation enforcement     Progress tracking              Cross-office rivalries
Tournament-specific stats        Auto-generated insights
Flexible scheduling
```

**Phase Dependencies:**

| Phase | Depends On | Reason |
|-------|-----------|--------|
| 1.5 (Live mode) | Phase 1 | Needs match entry infrastructure + rule system engine |
| 2 (Social) | Phase 1 | Pools need player base + notification infrastructure |
| 3 (Tournaments) | Phase 1 + 1.5 | Tournaments use both entry modes; referee view from 1.5 |
| 4+ (Achievements) | Phase 1 + 1.5 | Badges require deep statistics data from live mode |
| Future (Broadcasts) | Phase 1.5 | Narration needs goal-by-goal protocol from live mode |

### Risk Mitigation Strategy

**Technical Risks:**

| Risk | Severity | Mitigation |
|------|----------|------------|
| Cross-rule statistics aggregation complexity | High | 3-tier model (KD-02) isolates complexity; start with Tier 1 (universal) in MVP, add Tier 2/3 incrementally |
| Database schema must support all future phases | Medium | Design schema for extensibility from day one (rule_configuration table, goal-level events); use migration framework (Flyway) |
| Push notification reliability across browsers | Medium | PWA Push API + Service Worker; test on Chrome Android + Safari iOS early; fallback to in-app notification badge |

**Market Risks:**

| Risk | Severity | Mitigation |
|------|----------|------------|
| Cold start — empty leaderboards don't inspire | High | Seed with demo data; demo data hidden after threshold (~50 matches); launch event ("30-day championship") |
| Data entry fatigue — recording feels like a chore | High | Sub-10-second retrospective entry; kicker table UI makes it feel like part of the game, not admin work |
| Partial adoption — stats meaningful only with most matches recorded | Medium | 60% adoption threshold is minimum; social pressure from visible leaderboard drives recording behavior |

**Resource Risks:**

| Risk | Severity | Mitigation |
|------|----------|------------|
| Solo developer — bus factor of 1 | High | Clean architecture, comprehensive documentation, automated tests; Jules handles parallelizable tasks |
| Scope creep across phases | Medium | Strict phase boundaries; each phase has clear entry/exit criteria; no feature borrowing between phases |
| Motivation loss on long project | Low | Dogfooding from day one — developer is also a player; each phase delivers visible value |

## Functional Requirements

### Match Recording

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

### Match Verification

- **FR12:** System sends a confirmation request to the appropriate opponents when a match is submitted
- **FR13:** Confirming player must complete a double-check flow ("I agree" → "Are you sure? This is permanent") before a match is confirmed
- **FR14:** System applies confirmation rules based on match context: 1v1 participant-entered (one opponent confirms), 1v1 referee-entered (both confirm), 2v2 standard (one opponent confirms, second triggers immediate publication), 2v2 random pairings (both opponents confirm), referee-entered 2v2 (one per team, or all four for immediate publication)
- **FR15:** System enforces a 24-hour cooldown after first confirmation before publishing to statistics; second opponent's confirmation triggers immediate publication
- **FR16:** Confirmed match data is immutable — cannot be modified or deleted
- **FR17:** Player can reject a submitted match, returning it to the creator for correction with a notification
- **FR18:** System enforces a 48-hour confirmation window for tournament matches; non-confirmation results in technical defeat (Phase 3)

### Statistics & Analytics

- **FR19:** Player can view an individual leaderboard with rankings sortable by any statistical column
- **FR20:** Player can filter statistics by rule system (ITSF/DTFB/Custom template), match type (1v1/2v2), and time period (all time/year/month/week)
- **FR21:** Player can view their individual statistics with positional breakdown: matches W/L/D, games won/lost as attacker and defender, goals scored/conceded by position
- **FR22:** Player can view visual stat bars showing proportional breakdowns (matches W/D/L, games by position, goals by position)
- **FR23:** Player can view team statistics showing pair-level performance for every teammate combination, filterable by a specific player
- **FR24:** Player can view head-to-head statistics with three cross-tabulated tables: matches (with/vs), games (with/vs), and goals (with detailed positional breakdowns: attacker-vs-defender, attacker-vs-attacker, etc.)
- **FR25:** Player can filter any statistics view by: all players, a specific named team, or the built-in "Favorites" team. In player selection during match creation, the same filter applies — favorites first by default, with quick team switching
- **FR26:** Player can filter statistics by a specific tournament and tournament stage (Phase 3)
- **FR27:** Statistics are paginated with configurable page size (10/20/50/100)
- **FR28:** Player can set a minimum games played threshold (0/10/20/50/100) when viewing any statistics table, filtering out players with insufficient data for statistically meaningful comparisons

### Player Identity & Profile

- **FR29:** User can sign up and log in via Google OAuth2
- **FR30:** Player can set a nickname and avatar image in their personal cabinet
- **FR31:** Player can change their nickname at most once per month
- **FR32:** Player can select interface language (English or German)
- **FR33:** Player can delete their account; system removes all personal data and replaces identity with an anonymized placeholder ("boots on a nail" avatar) while preserving match history
- **FR34:** System stores only authentication credentials, profile data (nickname, avatar, language), and match data — no additional personal data

### Social & Matchmaking

- **FR35:** Player can create a "Want to Play" pool specifying match type (1v1/2v2) and start condition (fill-based or scheduled time) (Phase 2)
- **FR36:** Player can join an existing open pool from a list view (Phase 2)
- **FR37:** System sends push notifications when a pool fills or when a new pool matching user preferences is created (Phase 2)
- **FR38:** Player can challenge a specific player or group to a match (Phase 2)
- **FR39:** Player can create named player groups ("teams") — curated lists of players. "Favorites" is a built-in default team. All teams function identically: they filter player selection lists and statistics views to show only members of the selected team (Phase 2)
- **FR40:** Player can set a default team and default rule template that auto-populate when creating a new match (Phase 2)

### Tournament Management

- **FR41:** Player can create a tournament with configurable parameters: format (cup/championship), mode (1v1 personal, 2v2 fixed teams, 2v2 random pairings), rule system, min/max participants, registration deadline or wait time, round count, playoff option (Phase 3)
- **FR42:** Player can register for an open tournament; team partner receives confirmation notification and must accept (Phase 3)
- **FR43:** System generates the complete match table/bracket automatically when tournament starts. Seeding is strength-based: initial rounds pair players/teams of similar strength using statistical data. Multiple seeding algorithms may be offered as a tournament creation option. **Open question:** research needed on ITSF and DTFB tournament seeding rules (Phase 3)
- **FR44:** Players can play tournament matches in any order at their own pace — not locked to a schedule (Phase 3)
- **FR45:** Tournament matches use the tournament's configured rule system and are entered through the standard match entry flow (Phase 3)
- **FR46:** System maintains tournament-specific standings, statistics, and match archive accessible after tournament completion (Phase 3)
- **FR47:** System enforces equal match distribution for 2v2 random pairing mode (Phase 3)

### Achievements & Engagement

- **FR48:** System awards achievements (badges, pennants, cups) based on player actions, milestones, and statistical thresholds (Phase 4+)
- **FR49:** System awards humorous anti-achievements for memorable fails, designed to be lighthearted and celebratory — not shaming (Phase 4+)
- **FR50:** Player can view their award wall displaying all collected achievements (Phase 4+)
- **FR51:** Player can view progress toward locked achievements (Phase 4+)
- **FR52:** System generates narrative match broadcasts (text, audio, or animation) from live match protocols, narrating real player actions in sports commentary style (Future)
- **FR53:** System provides auto-generated statistical insights and progress tracking for individual players (Future)

### Platform & System

- **FR54:** Application is installable as a Progressive Web App (Add to Home Screen)
- **FR55:** System delivers push notifications for match confirmations, pool events, tournament updates, and achievements via Service Worker
- **FR56:** New users are presented with an onboarding tutorial explaining key features
- **FR57:** System provides demo/seed data so new users see populated statistics rather than empty tables; demo data is hidden after a configurable threshold
- **FR58:** System enforces automated rate limiting on match submissions to prevent spam, with context-aware thresholds that accommodate tournament referee throughput
- **FR59:** System supports English and German interface languages with externalized string resources (i18n)
- **FR60:** Player can view match history with options to filter by all players or a specific team, and a separate section for pending confirmations

## Non-Functional Requirements

### Performance

| Metric | Requirement | Context |
|--------|------------|---------|
| First Contentful Paint | <1.5s | Mobile 4G connection |
| Time to Interactive | <3s | Mobile 4G connection |
| Retrospective match entry (end-to-end) | <10s | From opening form to submission |
| Live mode goal registration (visual feedback) | <50ms | Optimistic UI — instant visual response before backend confirmation |
| Live mode goal registration (backend) | <200ms | Server-side processing and persistence |
| Statistics page load | <2s | Including API response with complex aggregation queries |
| Push notification delivery | <5s | From event trigger to device notification |
| Player selection search | <300ms | Filtering player list by name or team |
| Leaderboard sorting | <500ms | Client-side re-sort after column click |

**Degradation tiers:**
- **Acceptable:** Within target (normal operation)
- **Degraded:** 2x–5x target — loading indicator shown, warning logged to observability
- **Failed:** >5x target — timeout with user-visible error message, observability alert triggered

**Performance regression testing:** Benchmark dataset (1,000 matches, 50 users, 5 rule configs) used for automated performance tests. Statistics queries must meet targets on benchmark data at every release.

### Security

- **Authentication:** Google OAuth2 with backend-issued JWT tokens (HS256, 24h expiry). Stateless — no server-side sessions.
- **Authorization:** Match creation restricted to participants or designated referees. Match confirmation restricted to opponents of the creator. No cross-user data access beyond public statistics.
- **Transport:** All communication over HTTPS. No exceptions.
- **Data storage:** Passwords are never stored (OAuth2 delegation). JWT signing key stored as environment variable, never in source code.
- **Token handling:** JWT stored in localStorage on the client. Token refresh and expiry handled gracefully — expired sessions redirect to login without data loss.
- **Token invalidation on account deletion:** Deleted user's JWT must be rejected immediately, not after natural 24h expiry. Implementation: short-lived revocation list checked only for delete/ban events — minimal impact on stateless architecture.
- **Input validation:** All user input validated server-side. Game scores bounded by rule system constraints. Player UUIDs validated for existence and uniqueness within a match.
- **XSS prevention:** Strict input sanitization on all user-generated content (nicknames, team names, template names). Content Security Policy (CSP) headers enforced from MVP. Migration to HttpOnly cookies planned before public rollout beyond home office.
- **Rate limiting baselines:** Max 10 standalone match submissions/hour/user; max 30/hour in tournament referee context; 5+ rejections within 24h triggers submission throttle. Thresholds configurable, not hardcoded.
- **Content filter:** Basic blocklist filter on user-generated display names (nicknames, team names) — obviously offensive terms rejected at save time. Not full moderation — minimal hygiene filter. Priority: post-MVP, before expansion beyond home office.
- **Dependency vulnerability monitoring:** Automated scanning (Dependabot or equivalent). Critical CVEs patched within 7 days. No dependencies with known critical vulnerabilities in production.
- **DSGVO compliance:** Pseudonymization by design; minimal data collection; irreversible anonymization on account deletion; Privacy Policy with transparent data retention disclosure; Legitimate Interest Assessment (LIA) before production launch.
- **Data portability (GDPR Art. 20):** Player data export available upon request (not required as in-app feature). Product owner can generate export manually via database query. Structured JSON format. Response within 30 days per GDPR. In-app self-service export is a future enhancement, not an MVP requirement.

### Scalability

- **Initial capacity:** 10–20 concurrent users, ~50 registered users within 12 months.
- **Growth scenario:** If social features and tournaments gain traction (e.g., foosball community forums adoption), user base could grow to 200–500 within 18 months.
- **Design for growth:** Stateless backend architecture (no server sessions) enables horizontal scaling if needed. Database queries must remain performant up to 10,000 confirmed matches and 500 registered users without query redesign.
- **UI data volume resilience:** All selection lists (players, templates, tournaments) must remain usable and performant at 10x expected data volume. Search/filter required for lists exceeding 20 items.
- **No premature optimization:** Current infrastructure (single Spring Boot instance + PostgreSQL) is sufficient for the initial user base. Scaling decisions deferred until actual load demands it.

### Rule System Completeness

- **Full parameter space coverage:** The platform must support matches under any valid combination of rule parameters (~2,500 combinations). No hard-coded rule system assumptions — ITSF and DTFB are presets, not special cases.
- **Parameter validation:** Server-side validation of rule template parameters at creation: score limit > 0, valid win condition, consistent point distribution. Invalid combinations rejected with clear error message.
- **Tournament rule compliance:** Tournaments must be configurable to run under any officially recognized rule system (ITSF, DTFB) as well as any user-created custom rule template. No tournament format is restricted to a specific rule system.
- **Forward compatibility:** New official rule systems (e.g., future ITSF revisions) must be addable as new presets without architectural changes — they are just named parameter combinations like any other template.

### Accessibility

- **Target:** WCAG 2.1 Level A.
- **MVP priority:** Low — built for a known group of ~10 users. Level A compliance becomes a requirement before rollout beyond the home office.
- **Day-one foundations (non-negotiable):**
  - Semantic HTML elements throughout
  - Keyboard navigability for all interactive elements
  - Sufficient color contrast in dark theme (verify green/red/yellow on dark brown against WCAG AA contrast ratios)
  - `aria-label` on all icon-only buttons (goal undo, position swap, notifications)
  - Focus management in modal dialogs
  - No information conveyed by color alone (e.g., win/loss indicators must have text or icon alternatives)

### Touch Interaction & UX Quality

- **Touch targets:** Minimum 56x56dp for action buttons in live match mode (position swap, goal undo). Screen quadrants for goal attribution are naturally large (~180x320px on a 360px-wide phone) and require no additional sizing.
- **Optimistic UI:** All user-initiated actions must produce immediate visual feedback (<50ms) before backend confirmation. Goal taps, button presses, and navigation must feel instant.
- **Undo behavior:** First undo (last recorded goal) executes immediately without confirmation. Subsequent consecutive undos require a confirmation dialog ("Remove another goal?") to prevent accidental deep rollback.
- **Duplicate match detection:** Before sending any confirmation request, the system checks for existing matches (confirmed or pending) with identical players and identical scores from the same day. If a potential duplicate is found: the submitter is warned at submission time; if confirmed as new, the confirmation notification is sent with a visible warning label differentiating it from standard notifications to prevent blind approval of duplicates.

### Reliability & Data Integrity

- **Data durability:** Confirmed match data is the most critical asset. Zero tolerance for data loss on confirmed matches. Database backups with point-in-time recovery.
- **Backup & Recovery:** Daily automated backups. RPO < 24 hours. RTO < 4 hours during business hours. Backup restoration procedure documented and tested at least once before production launch.
- **Optimistic locking:** Concurrent match confirmation handled via `@Version` + retry. No silent data overwrites.
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

### Observability

- **Structured logging:** All backend log output in structured JSON format. Log entries include correlation IDs for request tracing.
- **Health check:** Spring Boot Actuator health endpoint exposed for monitoring.
- **Anomaly alerting:** System must detect and log anomalous patterns — e.g., sudden spike in rejected matches (spam signal), unusual submission volume from a single user.
- **Push notification audit trail:** Every notification sent must be logged with timestamp, recipient, delivery status. Queryable for dispute resolution (tournament technical defeats).
- **Mass push failure detection:** If push delivery failure rate exceeds threshold (e.g., >50% within 1 hour), system logs alert for operational investigation.
- **Scheduled job monitoring:** Cooldown expiry, tournament deadline enforcement, and other time-triggered jobs must be monitored. Missed execution triggers alert.
- **No heavy infrastructure required for MVP:** Actuator + structured logs is the baseline. Full monitoring stack (Prometheus/Grafana) deferred until operational scale demands it.

### Database Governance

- **Versioned migrations:** All database schema changes managed through Flyway. No Hibernate DDL-auto in production.
- **Migration reversibility:** Every migration must be reversible or have a documented rollback plan.
- **Schema extensibility:** Initial schema must accommodate future phases (rule_configuration table, goal-level events for live mode, tournament structures) without breaking migrations.

### API Compatibility

- **Versioned API:** All endpoints under `/api/v1/` prefix (already implemented).
- **Backward compatibility policy:** No breaking changes to existing endpoints within a major version. Breaking changes require a new API version (`/api/v2/`).
- **Client-server contract:** PWA clients may be cached and running an older version after a backend deploy. API must handle gracefully — clear error messages for deprecated endpoints, not silent failures.
- **PWA cache invalidation:** Service Worker must detect new backend version and prompt user to refresh. Stale frontend must never silently fail against updated API.

### Testability

- **ATDD-first strategy:** Testing follows TEA workflow (ATDD). Acceptance tests derived from functional requirements are the primary quality gate — does the implementation fulfill what the product owner specified?
- **Detailed test architecture:** Coverage targets, execution time constraints, test pyramid structure, and CI integration to be defined by the Test Architect during implementation planning.
- **Statistical query validation:** Every statistics aggregation query must have a dedicated integration test with seed data verifying correctness across all supported rule systems.
- **Critical path E2E smoke tests as deploy gate:** Match submission, confirm, reject, login, and statistics load must be verified by automated E2E tests before any production deploy.

### Internationalization

- **Languages:** English (primary), German (secondary). All user-facing strings externalized from day one.
- **Locale handling:** Date/time formatting, number formatting adapted to user's selected language.
- **Content language:** All generated content (notifications, achievement descriptions, tournament labels) must be localized in both supported languages.
- **RTL readiness:** CSS architecture must not preclude future RTL layout support. No RTL implementation required now, but layout patterns (flexbox direction, text alignment) must be directionality-neutral where possible.
- **Extensibility:** Adding a new language requires only a new translation file — no code changes.