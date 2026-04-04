---
title: "Product Brief: Tic-Tac-Tore"
status: "complete"
created: "2026-04-03"
updated: "2026-04-04"
inputs:
  - ".gemini/project-spec/DESIGN/project-design-description.md"
  - "docs/project-overview.md"
  - "conductor/product.md"
  - "conductor/product-guidelines.md"
  - "conductor/tech-stack.md"
  - ".gemini/project-spec/table-soccer-rules/"
---

# Product Brief: Tic-Tac-Tore

## Executive Summary

Think Strava, but for table football. Tic-Tac-Tore is a mobile-first web application that transforms casual office foosball into a data-driven competitive experience. It combines match recording, positional statistics, matchmaking, and tournament management into a single platform — replacing the current reality where games are played, forgotten, and endlessly debated.

Every office that plays foosball currently stitches together disconnected tools — or, more commonly, uses nothing at all. Kickertool handles tournaments. Kicktrack does basic tracking. Slack bots post leaderboards. No single product unifies the full experience. Tic-Tac-Tore is that product.

The product starts as an internal tool for itemis GmbH, where colleagues already play regularly but have no way to track results, settle rivalries with data, or organize structured competitions. If it gains organic traction beyond the corporate domain, that signals market viability for the broader recreational foosball community.

## The Problem

Office foosball is social, competitive, and fun — but it exists in a data vacuum. At itemis, colleagues play regularly during breaks but have no record of any of it. Players have gut feelings about who's strong and who's improving, but no objective evidence.

- **No record of matches.** Games are played and forgotten. "I beat you last week" has no proof.
- **No insight into skill development.** A player who wants to improve their defense has no way to measure progress — it's all feel, no facts.
- **Organizing is friction.** Arranging a game means walking around asking "anyone want to play?" Running a tournament means someone volunteering to manage a spreadsheet.
- **Existing tools don't fit.** Generic score trackers lack foosball-specific features. Tournament tools don't track ongoing statistics. Slack bots are too shallow. Nobody has solved the full problem.

The result: a vibrant office activity that never evolves beyond casual, with no objective way to celebrate improvement, settle who's actually the best, or give structure to friendly competition.

## The Solution

Tic-Tac-Tore provides three core capabilities that reinforce each other:

**Record.** Two match entry modes designed for different moments. Quick retrospective entry for when you just know the final scores — optimized for under 10 seconds. And a live mode (Phase 1.5): place your phone flat on the table, and a landscape top-view UI maps each corner of the screen to a player position. Recording a goal is one tap — select the scoring player — with an undo button for corrections. The phone stays awake, the screen becomes a live scoreboard. This signature interaction is the "aha moment" that makes first-time users see this is not just another score tracker.

**Discover.** Rich statistics emerge from recorded matches — individual performance, team composition effectiveness, head-to-head rivalry records, and deep positional breakdowns (attack vs. defense performance against specific opponents). Auto-generated insights like "You score 40% more from the back rod than average" turn raw data into actionable understanding. Statistics are the core hook: without numbers, everyone thinks they know who's the best — with numbers, the real picture emerges.

**Organize.** "Want to Play" pools eliminate the friction of finding opponents — create a pool, wait for others to join, get notified when it's time to play. Every pool creation is effectively a push to non-users: "games are happening and you're not in them." Tournaments (cup or championship format, 1v1 or 2v2, fixed or random teams) let offices run competitions where players play at their own pace — not locked to a rigid schedule.

**Verified Match Data.** Statistics integrity is the heart of the application — poisoned data kills the product. Every statistic on the platform is opponent-verified through active confirmation, and confirmed match data is immutable. This is a deliberate design choice: if the data can't be trusted, nobody will bother recording matches.

The confirmation workflow applies the "four eyes" principle with a two-step double-check:

1. The recording player submits the match. All required confirmers receive a notification: "Player X submitted results for a match you participated in."
2. **First confirmation:** The confirming player reviews the results and taps "I agree with the results."
3. **Second confirmation (double-check):** A follow-up prompt appears: "Are you sure you want to commit this score to the statistics database? Subsequent deletion or modification of the result will not be possible." The player confirms again.
4. **Publication.** Once the first required opponent confirms (with double-check), a 24-hour cooldown begins before the result is published to statistics. If the second opponent also confirms during this window, the result is published immediately.
5. **Immutability.** Once published, match data cannot be removed or modified. Deletion would be just as harmful to statistics integrity as false data.

Confirmation rules adapt to context:
- **1v1 (entered by a participant):** Only the opponent confirms (with double-check).
- **1v1 (entered by a third party/referee):** Both players confirm (each with double-check).
- **2v2 standard:** One player from the opposing team confirms (first tier); second opponent's confirmation triggers immediate publication.
- **2v2 random pairings:** Both opponents must confirm individually (since this mode is effectively individual competition).
- **Referee-entered 2v2:** At least one player from each team confirms (each with double-check and cooldown) or all 4 players confirmation for immediately publishing.
- **Tournaments:** A 48-hour confirmation window applies. Failure to confirm results in a technical defeat for the non-confirming player; repeated violations may lead to tournament removal. Statistics are preserved for the team that fulfilled its confirmation obligations.

## What Makes This Different

- **Positional statistics.** No existing foosball app tracks attack-vs-defense performance at the individual level. This is the core analytical differentiator — the depth of insight that turns casual tracking into genuine competitive intelligence.
- **Plays by YOUR rules.** ITSF (international), DTFB (German Bundesliga), and fully configurable custom rules. Every office has its own house rules — forcing players to change how they play is the #1 reason generic tracking apps fail. Tic-Tac-Tore adapts to you.
- **One platform, not four tools.** The market is fragmented: one app for tournaments, another for tracking, a Slack bot for leaderboards, a spreadsheet for everything else. Tic-Tac-Tore consolidates the full experience — and because everything feeds into one data model, each feature makes the others more valuable.
- **Office-first design.** "Want to Play" pools, match confirmations, flexible tournament scheduling, and a table-top phone UI are all designed for the reality of playing during a work break — not for weekend league administration.

## Who This Serves

**Primary: Office foosball players at itemis GmbH.** Colleagues who play during breaks, range from casual to competitive, and are motivated by friendly rivalry and personal improvement. They need something that takes seconds to use at the table and rewards engagement with meaningful insights.

**Secondary (future): Recreational foosball communities.** Any group of players — clubs, friend groups, league participants — who want structured tracking without the overhead of professional league software.

**User types within the primary audience:**
- **The Competitor** — wants rankings, head-to-head data, proof they're the best
- **The Improver** — wants to see trends, understand weaknesses, track growth
- **The Social Player** — wants to find games easily, participate in tournaments
- **The Organizer** — wants to run tournaments without spreadsheet management

## Privacy

Tic-Tac-Tore is designed with privacy by default. Players use nicknames and custom avatars — not real names or corporate identities. Google OAuth is used solely for authentication; email addresses are not stored or displayed anywhere in the application. There is no mechanism to correlate a player profile with a specific employee unless someone personally knows the nickname-to-person mapping. The app fosters friendly office culture and social interaction; participation is entirely voluntary.

## Success Criteria

**Phase 1 (Internal validation — first 8 weeks after launch):**
- At least 60% of regular players (those who play 2+ times/week) actively recording matches
- Match confirmation rate above 90%
- Weekly active users viewing statistics pages

**Growth signal:**
- Users outside the itemis.com domain registering and actively using the platform — this is the green light for broader product investment

## Scope

**MVP (Phase 1) — The Recording & Insights Engine:**
Match data is the atomic unit of the product. Everything else — live tracking, matchmaking, tournaments — is a multiplier on top of that data. Phase 1 establishes the foundation that makes every subsequent feature valuable.

- Retrospective match entry (1v1 and 2v2)
- Multiple rule system support (ITSF, DTFB, Custom)
- Match confirmation workflow (Verified Match Data)
- Individual and team statistics
- Basic leaderboards with ranking movement

**Phase 1.5 — Live Match Mode:**
- Real-time goal-by-goal entry with landscape table-top UI
- Positional attribution (who scored from which position, against whom)
- Extended positional statistics layer

**Phase 2 — Social & Matchmaking:**
- "Want to Play" pools (fill-based and scheduled)
- Notifications for pool status
- Favorites and saved team compositions

**Phase 3 — Tournaments:**
- Tournament creation (cup/championship, 1v1/2v2, fixed/random teams)
- Flexible play scheduling (play in any order)
- Tournament standings and progress tracking
- 48-hour confirmation enforcement with technical defeat rules

**Phase 4+ — Engagement & Delight:**
- Achievements and humorous anti-achievements (e.g., "Hummels Medal" for chronic own goals)
- Streaks, milestones, and personal records
- Anti-achievements designed to be lighthearted and funny — celebrating memorable fails, not shaming players

**Out of scope (for now):** Monetization, native mobile apps, Slack/Teams integration, public API, multi-language beyond EN/DE.

## Risks & Adoption

- **Cold start.** Empty leaderboards don't inspire engagement. Mitigation: after initial testing, seed adoption with a launch event (e.g., "30-day office championship" where all recorded matches count toward bragging rights). During development, provide demo data so potential users can see what populated statistics look like rather than staring at empty tables.
- **Data entry fatigue.** If recording a match takes more than a few taps, players will stop. Mitigation: retrospective entry optimized for speed; QR code at the table for instant access.
- **Partial adoption.** Statistics only become meaningful when most matches are recorded. The 60% adoption threshold is the minimum for credible data.
- **Cross-rule-system statistics complexity.** Supporting multiple rule systems with different scoring models creates complexity in how statistics are aggregated and displayed without distortion. This is the primary technical risk and the reason live mode is deferred to Phase 1.5.

## Vision

If Tic-Tac-Tore proves itself at itemis, the path forward is clear: expand to other offices and foosball communities, enable inter-company rivalries, and build the platform that turns every foosball table into a source of stories told through data. The combination of deep positional analytics, flexible rules, social features, and a personality-rich achievement system creates a product that serves everyone from office casuals to amateur league players.
