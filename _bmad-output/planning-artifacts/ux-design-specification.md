---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
lastStep: 14
inputDocuments:
  - "_bmad-output/planning-artifacts/product-brief-tic-tac-tore.md"
  - "_bmad-output/planning-artifacts/product-brief-tic-tac-tore-distillate.md"
  - "_bmad-output/planning-artifacts/prd.md"
  - "docs/index.md"
  - "docs/project-overview.md"
  - "docs/architecture-frontend.md"
  - "docs/component-inventory-frontend.md"
  - "docs/architecture-backend.md"
  - "docs/data-models-backend.md"
  - "docs/api-contracts-backend.md"
  - "docs/integration-architecture.md"
  - "docs/development-guide.md"
  - "docs/source-tree-analysis.md"
  - "_project-spec/DESIGN/DESIGN.md"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/home_hub_font_updated/screen.png"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/personal_cabinet_font_updated/screen.png"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/new_match_selection_cleaned/screen.png"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/results_entry_landscape_mobile/screen.png"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/live_match_annotated_updates_refined/screen.png"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/teams_rules_font_updated/screen.png"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/i_want_to_play_font_updated/screen.png"
  - "_project-spec/DESIGN/stitch_tic_tac_tore/tournament_match_annotated_updates_applied/screen.png"
documentCounts:
  briefs: 2
  prd: 1
  projectDocs: 10
  designSpecs: 1
  uiMockups: 8
---

# UX Design Specification Tic-Tac-Tore

**Author:** Pavel
**Date:** 2026-04-08

---

## Executive Summary

### Project Vision

Tic-Tac-Tore transforms office foosball from an ephemeral pastime into a data-driven competitive experience. The platform records matches, verifies results through opponent confirmation, and produces positional statistics deep enough to reveal skill differences invisible to the naked eye — creating a new layer of office culture built on verified data, shared team history, and friendly rivalry.

The product consolidates match recording, verified statistics, social matchmaking ("Want to Play" pools), and tournament management into a single mobile-first PWA — replacing the current reality where games are played, forgotten, and endlessly debated. Two match entry modes serve different moments: retrospective entry for quick post-game logging (<10 seconds), and live mode where the phone placed flat on the table becomes both a scoreboard and a goal-by-goal protocol.

The app serves the physical world — not every player at the table needs a phone. One person records, others confirm later via push notifications. This asymmetric model removes pressure for group device synchronization and matches how office foosball actually works.

No existing product in the market offers positional statistics (attack vs. defense performance), multi-rule-system support (ITSF, DTFB, custom house rules), or integrated tournament management with statistical continuity.

### Target Users

**Primary audience:** 10–20 office foosball players at itemis GmbH — a mix of casual and competitive players motivated by friendly rivalry and personal improvement. Tech-savvy (software engineers), smartphone-first usage during work breaks.

**Audience priority:** The app is built for enthusiasts first — players who play regularly and want to see dimensions of their game invisible without statistics. Casual and social players are welcome and accommodated, but do not drive core design decisions.

**User archetypes (ordered by design priority):**

| Archetype | Core Need | UX Implication | Priority |
|-----------|-----------|----------------|----------|
| **The Competitor** (Max) | Prove skill with data, compare H2H | Deep positional stats, sortable leaderboards, H2H cross-tabulation, share button | Primary |
| **The Referee** (Viktor) | Record matches efficiently during tournaments | Portrait referee view, live mode, streamlined per-match flow | Primary |
| **The Organizer** (Oleg) | Run tournaments without spreadsheets | Self-service tournament creation, fully autonomous after setup | Primary |
| **The Newcomer** (Kai) | Organic, zero-friction onboarding | Confirmation-first experience, tutorial slides, demo data (FR57) | Secondary |
| **The Social Player** (Lisa) | Find games without social friction | Want to Play pools, push notifications, minimal interaction | Secondary |
| **The Achievement Hunter** (Anna) | Collect badges and share stories | Award wall, narrative achievements, sequential rollout (Phase 4+) | Future |

**Frequency-based action prioritization:**

| Action | Frequency | Entry Point |
|--------|-----------|-------------|
| Record match | Daily, multiple times | In-app: Home Hub → New Match |
| Want to Play (create/join pool) | Several times per day/week | In-app: Home Hub → Want to Play (Phase 2) |
| Confirm match | Daily | 90% via push notification deep link; in-app badge as fallback |
| View statistics | Several times per week | In-app: Home Hub → Statistics |
| Create tournament | Monthly, then autonomous | In-app: one-time action |

### Key Design Challenges

1. **Orientation follows physical context.** Retrospective match entry stays in portrait — the standard phone-in-hand posture — eliminating the forced rotation that would consume 3–5 seconds of the 10-second budget. Landscape orientation is reserved exclusively for Live Match mode (Phase 1.5), where the phone physically lies on the table and the screen maps to the playing field. The kicker table visual metaphor (team colors, player positioning, field aesthetics) is carried into portrait through iconography and layout, not forced orientation change.

2. **Speed as survival metric.** Retrospective match entry must complete in under 10 seconds end-to-end. Rules and team defaults pre-set from last used configuration. Player slots start empty (not pre-filled — verifying pre-filled names is slower than filling blanks). Frequent opponents prioritized at top of player selection lists.

3. **Confirmation without bureaucracy.** Single-tap confirmation replaces the double-check flow (PRD FR13 override). Triple insurance model: 1 submitter + 2 confirmers = sufficient protection. After confirmation, a 15-second undo toast provides misclick protection without blocking flow. Rich push notifications show match details (players, scores) so users can decide before opening the app.

4. **Multi-rule system without cognitive overload.** ~2,500 possible parameter combinations collapse to 2–3 active templates per player group (KD-04). The UI shows familiar template names (ITSF, DTFB, "Friday Night Fun"), never raw parameters. Template creation is a power-user flow, not a default path. Rule sharing: ability to save a rule template from a played game, enabling organic rule propagation between players.

5. **Progressive complexity for the entire interface.** Statistical depth follows progressive disclosure: casual users see simple win/loss; engaged users discover positional breakdowns; power users filter by exact rule configuration. This principle extends beyond statistics to every screen — the interface reveals complexity as the user demonstrates engagement, never upfront.

6. **Home Hub: predictable, focused, evolving.** Home Hub is always the start screen on normal app launch — predictability over cleverness. Muscle memory matters: the user's finger is already hovering before the screen loads. MVP: 2–3 primary buttons (New Match, Statistics, Played Matches). Phase 2 adds Want to Play. Phase 3 adds Tournaments. Each phase organically adds one button — the Hub grows with the product. Push notifications are the only exception — they deep link to the relevant screen.

7. **Self-service, zero-admin as design principle.** The platform operates without an admin role. Tournaments run fully autonomously after creation — deadline enforcement, technical defeats, bracket progression, completion. Match confirmation is peer-to-peer. Abuse prevention is automated rate-limiting. Every screen and flow must be designed for self-service operation.

8. **Casual-friendly tone of voice.** Button labels and screen names should feel inviting, not analytical. Consider renaming "Statistics" → "My Game", "Leaderboards" → contextual fun names. The language should say "this is fun", not "this is serious." Requires a dedicated naming brainstorm session.

9. **Kicker table metaphor: functional where it matters, ambient elsewhere.** The table-top visual metaphor is functionally critical in live mode (screen corners = player positions). In all other screens, the foosball identity lives through the design system (dark theme, green/red/yellow palette, Space Grotesk/Manrope typography) — not through forced table layouts. Screens are what they need to be: forms, tables, cards — in brand colors.

### Design Opportunities

1. **Live mode as augmented reality for foosball.** Phone flat on the table during play, screen corners mapped to player positions, one tap per goal — the physical and digital game merge. This is the feature that makes first-time observers say "what is that app?" and creates organic adoption.

2. **Statistics as recognition engine.** Every player finds at least one dimension where they excel. The depth of positional analytics means nobody is "just average" — there is always a hidden strength to discover. For Phase 4+, achievements become narrative artifacts — not just badges, but stories: when it was earned, which matches contributed, how the player compares to others who earned it. Achievement rollout should be sequential (core badges first → narrative layer → meta-statistics).

3. **Proactive insights without navigation.** Rank movement displayed on Home Hub — entered/exited top-10, milestone movements (top-50, top-40, top-30). H2H comparison limited to favorites/teams (10-15 players max, performance to be validated with architect). Users discover statistics passively through contextual micro-insights, not only through dedicated statistics screens.

4. **Micro-celebration after confirmation.** Every match confirmation triggers an instant insight — "Your win streak: 4 matches", "You're now #3 in defense ranking." Transforms a routine obligation into a micro-reward moment. Minimal dev cost, maximum emotional impact.

5. **Team history as social glue.** Pair-level statistics create conversational hooks between teammates: "apparently we're unbeatable together" drives social bonding beyond the match itself.

6. **Hall of Fame and native sharing.** MVP: share button on statistics screens via Web Share API (zero backend). Phase 4: personal "Hall of Fame" gallery — curated stat highlights and achievement moments. Other players view someone's Hall of Fame via long-press on avatar → fullscreen slideshow. Statistics transform from private data into social currency.

7. **Skill-level matchmaking.** "Want to Play" pools and tournaments support optional skill-level restrictions — minimum and/or maximum thresholds. This serves both directions: professionals training seriously can exclude mismatched opponents, and — equally important — newcomers see an explicit signal that a pool welcomes their level. Without level labels, a newcomer may self-exclude from any pool, assuming "everyone there is a pro and won't enjoy playing with me." A pool labeled "all levels welcome" or "max level: intermediate" is an invitation, not just a filter.

8. **Activity feed on Home Hub.** Compact match feed below primary buttons — last 3-5 matches in the group. Social proof that games are happening, FOMO for casual players, context without navigation. Scrollable area under fixed action buttons.

9. **Avatar as universal interaction point.** Tap on avatar anywhere (leaderboard, match card, player list) → quick stats popup. Long-press → profile / Hall of Fame (Phase 4). One element, consistent behavior everywhere. Design principle: "avatar is always interactive."

### Design Decisions Log

| Decision | Rationale | PRD Impact |
|----------|-----------|------------|
| Portrait for retrospective entry | Speed > visual metaphor; phone in hand = portrait | Overrides landscape assumption from mockups |
| Remove double-check, add undo toast | Triple insurance sufficient; undo toast = misclick protection without bureaucracy | Overrides FR13 |
| Home Hub always the start screen | Predictability > contextual smartness; muscle memory | Confirms PRD intent |
| Teams & Rules: no dedicated screen | All management inline during match creation or in Settings; no standalone admin screen | Simplifies navigation |
| Played Matches + Confirmation Queue → unified "My Matches" | Users think "my matches", not "history" vs "queue" | Simplifies navigation |
| Player list: frequent opponents at top, not pre-filled | Empty slots faster than verifying/replacing pre-filled names | Refines match entry UX |
| Hub evolves by phase | MVP: 2-3 buttons; Phase 2: +Want to Play; Phase 3: +Tournaments | Phased UI growth |
| App serves the physical world | Not every player needs a phone at the table; one records, others confirm via push | Core design principle |

## Core User Experience

### Defining Experience

The core experience of Tic-Tac-Tore is a three-stage loop that must feel like a single continuous motion:

**Record → Confirm → Discover**

1. **Record:** One player captures the match result in under 10 seconds. This is the atomic action — without it, nothing else exists. The recording flow must be so fast that it becomes part of the post-match ritual, like shaking hands after a game.

2. **Confirm:** Opponents validate the result with a single tap from a push notification. This is a reactive, near-zero-effort action. The push notification contains enough context (players, scores) to make a decision without opening the app.

3. **Discover:** Statistics reveal hidden dimensions of the player's game. This is the reward — the reason the first two steps matter. Discovery happens both actively (navigating to statistics) and passively (micro-insights on Home Hub, post-confirmation celebrations).

The loop is asymmetric by design: one person records (proactive effort), multiple people confirm (reactive, minimal effort), and everyone benefits from discovery (passive reward). This matches the physical reality of office foosball — one person takes responsibility, everyone participates.

**The critical interaction to get right:** Match recording. If recording is too slow, too complex, or too many steps — players say "I'll do it later" and never do. Every match lost to friction is data that never enters the system, weakening statistics for everyone. Recording speed is not a feature — it is the product's survival metric.

### Platform Strategy

**Primary platform:** Mobile-first Progressive Web App (PWA), installable via Add to Home Screen.

| Context | Platform | Orientation | Priority |
|---------|----------|-------------|----------|
| Match recording (retrospective) | Smartphone | Portrait | Critical — MVP |
| Match confirmation | Smartphone (push notification) | Portrait | Critical — MVP |
| Live match recording | Smartphone on table | Landscape | Phase 1.5 |
| Statistics deep dive | Smartphone / Tablet / Desktop | Portrait / Responsive | Important — MVP |
| Tournament management | Smartphone / Desktop | Portrait / Responsive | Phase 3 |
| Home Hub / daily use | Smartphone | Portrait | Critical — MVP |

**Touch-first design:** All primary interactions designed for thumb-reach on smartphone screens. Desktop is a secondary viewport for statistics analysis and tournament setup — functional but not the design driver.

**PWA capabilities leveraged:**
- Push notifications (critical for confirmation flow and pool alerts)
- Screen Wake Lock (live match mode — Phase 1.5)
- Orientation Lock (landscape for live mode — Phase 1.5)
- Add to Home Screen (app-like launch experience)
- Web Share API (share statistics — MVP)

**Not required for MVP:** Offline support, camera access, background sync.

**Single-device model:** The app does not assume all players have phones at the table. One device records, others confirm asynchronously via push. No real-time multi-device synchronization required for core flows.

### Effortless Interactions

**Zero-thought actions (must feel automatic):**

| Interaction | Target Feel | How |
|-------------|-------------|-----|
| Open app → see Home Hub | Instant recognition, finger already moving | Predictable layout, never changes structure on normal launch |
| New Match → select players | 3-4 taps to start entering scores | Frequent opponents at top of list, last-used rules pre-selected |
| Enter game scores | Tap +/- or type number | Large touch targets, auto-advance to next game when score limit reached |
| Submit match | One tap | Single "Submit" button, no confirmation dialog |
| Confirm match from push | Tap notification → tap Confirm | Rich notification with match details; one tap confirms, undo toast for safety |
| See own ranking | Glance at Home Hub | Rank movement and micro-insights displayed without navigation |

**Automatic system actions (no user intervention):**

| System Action | Trigger |
|---------------|---------|
| Apply correct confirmation rules (1v1/2v2/referee) | Automatically based on match context |
| Enforce position swap between games | Automatically based on selected rule system |
| Complete game when score limit reached | Automatically during score entry |
| Complete match when win condition met | Automatically based on rule system |
| Publish to statistics after cooldown | 24h timer or second confirmation |
| Tournament bracket progression | Automatic after match confirmation |
| Rate limiting on submissions | Automatically, context-aware thresholds |

### Critical Success Moments

**Make-or-break moments (failure here = product failure):**

1. **First match recorded.** The moment a player records their first match and sees it appear in the system. If this takes more than 30 seconds end-to-end (including app open), the player may never record a second one. Demo data provides context, but the first real match is the commitment point.

2. **First confirmation from push.** The first time an opponent taps a push notification and confirms a match in under 5 seconds. This establishes the pattern: confirming is trivial, not a chore. If the first confirmation experience involves too many screens or unclear UI, the confirmation rate drops permanently.

3. **First statistical insight.** The moment a player discovers something they didn't know about their own game — "I'm the best defender", "My win rate with Alex is 78%", "I always lose in Best-of-5." This is the AHA moment that transforms recording from an obligation into an investment. Requires ~10-15 confirmed matches to generate meaningful data; demo data bridges the gap.

4. **First shared statistic.** The moment a player shares a stat in a group chat and it sparks conversation. This is the viral loop — statistics become social currency, motivating others to record matches and check their own stats.

**Delight moments (success here = emotional connection):**

- Post-confirmation micro-celebration: "Win streak: 4 matches 🔥"
- Rank milestone on Home Hub: "You're now Top 3 in defense!"
- Activity feed showing recent matches: social proof that the community is active
- Discovering a surprising H2H stat against a rival

### Experience Principles

1. **"Record it or lose it."** Every design decision in the match recording flow must answer: does this make recording faster? If not, it doesn't belong in the flow. Speed is not a feature — it is survival.

2. **"One person records, everyone benefits."** The app serves the physical world. Not every player needs a phone at the table. The UX supports asymmetric participation: one recorder, multiple confirmers, universal statistics access.

3. **"Statistics find you, you don't find statistics."** Proactive insights on Home Hub, micro-celebrations after confirmation, contextual stats on avatar tap. The dedicated Statistics screen exists for deep dives, but casual discovery happens everywhere.

4. **"Predictability is speed."** Home Hub never changes structure on normal launch. Navigation is consistent. Elements are in the same place every time. Muscle memory is the fastest UI — never break it with "smart" layout changes.

5. **"The interface grows with the product."** MVP is minimal and focused (2-3 Hub buttons). Each phase adds one capability, one button. Users who grow with the product experience each addition as a reward, not complexity. New users in later phases see a richer but still logical layout.

6. **"Confirm in one gesture, celebrate in one line."** Confirmation is a single tap + undo safety net. The celebration that follows (micro-insight) is the reward that makes the next confirmation feel worthwhile, not obligatory.

## Desired Emotional Response

### Primary Emotional Goals

**1. Pride of Discovery — "I knew it, and now I can prove it."**

The defining emotion of Tic-Tac-Tore. A player who suspects they're a strong defender opens the statistics and sees their name at the top of the defensive rankings. This is validation of something felt but never proven. The app exists to make invisible skills visible.

**2. Sense of Growth — "I'm getting better, and I can see it."**

Beyond static rankings, players want trajectory — a win rate climbing over weeks, a defensive record improving against a specific opponent, a milestone reached. Growth sustains engagement after the initial pride of discovery fades.

**3. Minimal Tolerable Effort — "Recording just happened."**

Match recording must sit below the threshold of irritation. It will never be truly effortless (10 seconds, player selection, score entry), but it must never feel like an obligation worth postponing. The recording process itself should evoke zero emotional resistance — no frustration, no decision fatigue, no "I'll do it later." Neutral is the target; anything above neutral is a win.

### Emotional Design Principles

1. **"The app amplifies the table moment."** The strongest emotions happen at the physical table — the winning goal, the comeback, the high-five with your partner. The app's job is to capture and extend these moments into data that can be relived, shared, and built upon. The digital experience serves the physical one, never the other way around.

2. **"Every number shows where your next breakthrough hides."** Statistics are treasure maps, not exam grades. When showing a player's data, frame it as potential: what small improvement yields a big rank jump, where an untapped strength lies, which matchup is ripe for a turnaround. Never suggest "play more matches" — show only controllable, quality-based improvement paths accessible to every player regardless of how often they can play.

3. **"Every player has a leaderboard where they shine."** Multiple ranking dimensions (overall, positional, rule-system-specific, period-based, peer-group) ensure that no player is "just bad at everything." Newcomers see rankings among peers with similar match counts. Veterans see all-time records alongside weekly sprints. The recognition engine finds each player's strength — even if the player hasn't found it themselves yet.

4. **"Data shows, interface doesn't judge."** The app presents honest facts without evaluative commentary. Positive pair insights are automated ("You and Alex: 6 wins in a row!"). Negative patterns (poor partner compatibility, declining performance) are visible in the data but never highlighted with judgmental framing. Hard truth is the default tone for enthusiasts; soft framing applies only to objectively difficult situations (extended losing streaks, newcomer onboarding).

5. **"Shared victories bond deeper than individual stats."** Foosball is a team game. Post-confirmation micro-celebrations sometimes highlight the pair, not just the individual. Team statistics create conversational hooks between partners that extend beyond the match. The emotional design recognizes that "we won together" is a more powerful memory than "I won."

### Emotional Journey Map

| Moment | Target Emotion | Risk Emotion | Design Response |
|--------|---------------|-------------|-----------------|
| **Opening the app** | Familiarity, anticipation | Indifference | Predictable Home Hub + proactive rank movement insights + activity feed |
| **Recording a match** | Zero resistance (neutral) | Obligation ("I'll do it later") | Sub-10s flow, pre-set defaults, frequent opponents at top, micro-acknowledgment after submit ("Match submitted! Awaiting confirmation from Alex") |
| **Confirming a match** | Light participation | Annoyance | Rich push with full context, single tap + undo toast, micro-celebration after |
| **Discovering statistics** | Pride + surprise | Overwhelm | Progressive disclosure, personal bests highlighted, breakthrough hints |
| **Seeing rank change (up)** | Motivation, pride | — | Celebrate with context: what drove the climb |
| **Seeing rank change (down)** | Determination, curiosity | Helplessness | Show controllable improvement paths, never "play more"; period leaderboards for comeback opportunities |
| **Sharing a stat** | Social validation | Embarrassment | Player chooses what to share; share button on positive-framed screens |
| **Returning next day** | Habit, curiosity | Forgetting | Activity feed, push for pools and confirmations, rank change notifications |
| **Something goes wrong** | Trust | Frustration | Undo toast (15s), rejection with message to creator, 24h cooldown window for disputes |

### Edge Case Emotional Handling

These are implementation-level guidelines for situations that will inevitably occur:

| Situation | Emotional Risk | Design Response |
|-----------|---------------|-----------------|
| **Extended losing streak (10+ losses)** | Demoralization | Shift focus from win/loss to activity metrics and micro-improvements within losses ("2 fewer goals conceded than last week") |
| **Former leader declining** | Identity loss | Archival achievements as identity anchor ("3-month reign as #1 — longest in office history"); period leaderboards for comeback opportunities |
| **Group inactivity** | "Everyone left" feeling | Replace empty feed with provocative content: unplayed rivalries, frozen rankings at stake, implicit call to action |
| **Newcomer at bottom of rankings** | Shame, exclusion | Personal progress focus; peer comparison (similar match count); minimum match threshold for public leaderboard (FR28) |
| **Uncomfortable team compatibility data** | Social awkwardness | Data visible but never auto-commented; no "you play worse with X" insights; only positive pair celebrations are automated |
| **Pending matches unconfirmed for days** | Effort feels wasted | Pending status visible on Home Hub; reminder notification to opponents after 24h |

## UX Pattern Analysis & Inspiration

### Approach

This project is not inspired by any specific product. It grows organically from a real need: tracking office foosball matches and progressively building a richer experience. Rather than copying existing apps, the UX pattern analysis identifies proven interaction patterns that solve Tic-Tac-Tore's specific design challenges — and anti-patterns from competitors to deliberately avoid.

### Transferable UX Patterns

#### Speed-Critical Data Entry

**Challenge addressed:** Sub-10-second match recording (Design Challenge #2)

| Pattern | How It Works | Application in Tic-Tac-Tore |
|---------|-------------|------------------------------|
| **Smart defaults with override** | Pre-fill the most likely option, let user change if needed | Last-used rule system pre-selected; match type defaults to most frequent (2v2 or 1v1) |
| **Frequency-sorted lists** | Most-used items at top, not alphabetical | Player selection: frequent opponents first, then alphabetical |
| **Stepper controls (+/- and +5)** | Tap to increment/decrement; large-step shortcut for common scores | Score entry: +1/−1 for fine-tuning, +5 shortcut for fast entry. Game to 10 = two taps (+5, +5) instead of ten. Game to 5 = one tap |
| **Auto-advance on completion** | When a field is complete, move to next automatically | Game auto-completes when score limit reached; match auto-completes when win condition met |
| **Inline validation, not blocking dialogs** | Show errors contextually, never in a popup | "Score exceeds limit" shown inline, not as alert |

#### Asynchronous Confirmation

**Challenge addressed:** Confirmation without bureaucracy (Design Challenge #3)

| Pattern | How It Works | Application in Tic-Tac-Tore |
|---------|-------------|------------------------------|
| **Rich notification with action** | Notification body contains enough context to act | Push shows full match details: players, scores, outcome |
| **Single-action + undo** | One tap to act, brief window to reverse | Confirm tap + 15-second undo toast |
| **Status badges** | Visual indicator of pending items | Badge count on My Matches for unconfirmed items |
| **Passive escalation** | Automatic reminder if no action taken | 24h reminder notification to non-confirming opponents |

#### Progressive Disclosure for Complex Data

**Challenge addressed:** Statistical depth without overwhelm (Design Challenge #5)

| Pattern | How It Works | Application in Tic-Tac-Tore |
|---------|-------------|------------------------------|
| **Summary → detail drill-down** | Show headline metric, tap to expand | Leaderboard shows rank + win rate; tap row for full positional breakdown |
| **Contextual tooltips on tap** | Explain a metric when tapped, not upfront | "What is defense win rate?" appears on first tap, not cluttering the UI |
| **Layered tabs** | Separate views by depth level | Overall → Positional → H2H as tab layers within Statistics |
| **Avatar as info trigger** | Tap on a person element for contextual data | Tap any player avatar → quick stats popup without navigation |

#### Habit-Forming Micro-Loops

**Challenge addressed:** Retention and daily return (Emotional Goal: Minimal Tolerable Effort)

| Pattern | How It Works | Application in Tic-Tac-Tore |
|---------|-------------|------------------------------|
| **Activity feed as landing content** | Show recent social activity on main screen | Home Hub: compact match feed below action buttons |
| **Change notifications** | Alert users to meaningful state changes | Rank movement, new personal best, milestone reached |
| **Micro-celebrations** | Brief positive feedback after actions | Post-confirmation: "Win streak: 4!" or "You and Alex: unbeaten in 6!" |
| **Pending item visibility** | Show incomplete items prominently | Unconfirmed matches at top of My Matches with highlight |

#### Physical-Digital Bridge

**Challenge addressed:** App amplifies the table moment (Emotional Principle #1)

| Pattern | How It Works | Application in Tic-Tac-Tore |
|---------|-------------|------------------------------|
| **Spatial UI mapping** | Screen layout mirrors physical space | Live mode: screen layout mirrors physical space |
| **Wake lock for active sessions** | Screen stays on during physical activity | Live match: screen never dims while match is in progress |
| **One-hand operation** | All critical actions reachable with thumb | Portrait mode: action buttons in bottom half of screen |
| **Glanceable status** | Key info visible at arm's length | Live mode scores: large numerals readable by all players at the table |

### Anti-Patterns to Avoid

Derived from competitor analysis (Kicktrack, Kickertool, Kicker.cool, Foosball Goalkeeper) and general mobile UX failures:

| Anti-Pattern | Why It Fails | Tic-Tac-Tore Avoidance |
|-------------|-------------|------------------------|
| **Single rule system assumption** | Every office has house rules; forcing one standard kills adoption | Unified RuleConfiguration model; ITSF/DTFB/Custom are equal |
| **Tournament-only or tracking-only** | Fragmenting the experience across tools | Single platform for all foosball activities |
| **Desktop-first responsive** | Foosball happens at a table, not at a desk | Mobile-first; desktop is secondary viewport for deep analysis |
| **Manual leaderboard management** | Spreadsheets, manual calculations | All statistics auto-calculated from confirmed match data |
| **Complex onboarding forms** | Requiring profile completion before first use | Google OAuth + nickname/avatar is enough; depth comes later |
| **Notification spam** | Every event triggers a push | Only actionable notifications: confirmations, pool fills, tournament deadlines |
| **All-or-nothing statistics** | Either no stats or overwhelming dashboards | 3-tier progressive disclosure: simple → positional → deep config-specific |
| **Forced social features** | Requiring friends/follows before core functionality | Core loop (record → confirm → discover) works without any social setup |

### Design Inspiration Strategy

**Adopt (proven patterns, use directly):**
- Smart defaults + frequency-sorted lists for match entry speed
- Single-action + undo toast for confirmation flow
- Summary → detail drill-down for statistics progressive disclosure
- Activity feed bundle landing content
- Avatar as universal interactive element

**Adapt (proven patterns, modify for context):**
- Spatial UI mapping → only for live mode landscape, not forced everywhere
- Micro-celebrations → tied to statistical insights, not generic animations
- Status badges → combined My Matches screen (pending + confirmed), not separate dashboard

**Avoid (competitor mistakes and general anti-patterns):**
- Single rule system lock-in
- Desktop-first layouts on mobile screens
- Notification overload
- Complex onboarding before value delivery
- Forced social graphs before core utility works

## Design System Foundation

### Design System Choice

**Google Stitch as primary design tool + Tailwind CSS v4 for implementation.**

The design system is defined and managed in Google Stitch (project ID: `6006028140652349802`), which serves as the single source of truth for visual design — replacing Figma. Stitch is controlled programmatically via MCP tools, enabling AI-driven design iteration directly from the coding agent. Implementation uses Tailwind CSS v4 with custom design tokens mapped 1:1 from Stitch's Material Design 3 color scheme.

**Existing Stitch screens are pre-BMAD drafts.** The 60+ screens in the Stitch project were created before the full BMAD discovery cycle. Many UX decisions have changed (portrait retrospective entry, Home Hub simplification, unified "My Matches", removal of Teams & Rules screen, undo toast instead of double-check, etc.). These screens must be regenerated or significantly edited using Stitch MCP tools to match this UX specification. Existing screens serve as visual vocabulary and starting point, not as specification.

### Design System: "The Clubhouse Editorial"

The creative north star defined in Stitch is **"The Speakeasy Stadium"** — moving away from sterile "gaming app" aesthetics toward the tactile, moody atmosphere of a high-end office lounge. Exposed brick, dark wood, focused intensity under warm lamps.

**Core Design Rules (from Stitch):**

| Rule | Description | MVP Scope |
|------|-------------|-----------|
| **No-Line Rule** | No 1px solid borders. Boundaries via background color shifts and tonal transitions | Layout level: strict. Data-dense screens (tables, lists): ghost dividers at 10-15% opacity allowed |
| **Surface Hierarchy** | UI as physical layers: Base → Section → Focus | MVP: 3 levels (`surface`, `surface-container`, `surface-container-highest`). Full 5-level hierarchy documented for future |
| **Glass & Gradient** | Glassmorphism for floating elements; gradient CTAs | MVP: solid surfaces everywhere (performance). Glassmorphism deferred as future enhancement |
| **Typography Scale** | Extreme scale differences for editorial feel | Maintained — core identity element |
| **No Pure White** | All "white" text uses `on-surface` (#EBE0DD) — warm cream | Maintained — zero exceptions |
| **Ambient Shadows** | Extra-diffused shadows for floating elements | MVP: one shadow token (`shadow-ambient`). Full shadow spectrum deferred |
| **Asymmetric Layouts** | Editorial off-center placement for visual interest | Context-dependent: symmetric for speed-critical flows (match entry, confirmation); editorial for browse-mode (statistics, Home Hub feed) |

**Signature component: "Rod" Scoreboard** — horizontal line with team-colored player icons. Scope: Phase 1.5 (live mode), not MVP retrospective entry.

### Rationale for Selection

| Factor | Decision Driver |
|--------|----------------|
| **Stitch project exists** | 60+ draft screens, full design system defined, MCP-controllable |
| **Replaces Figma** | No separate design tool needed — Stitch generates from text descriptions |
| **Tailwind in codebase** | Already installed with Vite plugin; maps naturally to Stitch tokens |
| **Solo developer + AI** | Stitch MCP + Tailwind = design-to-code pipeline without manual handoff |
| **1:1 token naming** | Tailwind classes match Stitch tokens exactly — zero translation errors |

### Implementation Approach

**Design-to-code pipeline:**

```
UX Specification (this document)
    ↓ screen descriptions
Stitch (authoring surface for visuals)
    ↓ visual validation
Tailwind CSS (implementation contract — tokens in tailwind.config)
    ↓ implementation
Vue 3 components
```

**Source-of-truth contract (supply chain mitigation).** Stitch is the **authoring surface** for visuals — the place where screens are generated, edited, and reviewed. Tailwind tokens defined in `tailwind.config` are the **implementation contract** — the binding agreement between design and code. The 1:1 naming between Stitch tokens and Tailwind classes is deliberate: if Stitch becomes unavailable (service outage, project deletion, MCP contract change), existing tokens and already-produced screen specs remain usable, and production can continue without Stitch access. Stitch dependency is operational, not architectural.

**Token synchronization protocol.** Stitch tokens and Tailwind config drift is the most likely technical failure of the 1:1 mapping guarantee. To prevent silent drift:

- Stitch token changes (rename, add, remove, value change) must be reflected in `tailwind.config` within the same commit group. A Stitch-only update without corresponding Tailwind update is a broken contract.
- Before any MVP release, verify the token set in Stitch matches the token set in `tailwind.config` (manual diff acceptable for MVP; automated diff recommended once token count exceeds ~30).
- If Stitch access is temporarily lost, `tailwind.config` is the fallback truth; restore Stitch from Tailwind, not the reverse.

**Consistency rule:**

- UX spec change → Stitch screen update → code update. Code never diverges from the Stitch-produced specs; Stitch screens never diverge from this UX specification.
- Each top-level Vue 3 component that implements a named screen references its Stitch screen ID in a single-line comment at the top of the `<script setup>` block (e.g., `// Stitch screen: home_hub_v2`). This is a lightweight traceability marker, not a strict enforcement — but it surfaces drift during code review: if a screen has changed significantly in code and the Stitch ID still points to an old version, the reviewer sees the mismatch immediately.

**Treatment of pre-BMAD Stitch drafts (all 60+ existing screens, including the 8 referenced in `inputDocuments`):**

- **All pre-BMAD screens are rejected as target specifications.** They do not define what the final screens should look like. However, they remain useful as *analytical artifacts*: shared visual vocabulary, understanding of what the "Sterile Gaming App" direction looked like in practice, and occasional Stitch input when generating new screens with style references. Rejection applies to their authority, not their existence.
- **Full regeneration is required.** Every screen will be re-generated (or substantially edited) via Stitch MCP tools to match the UX decisions in this document. Key deltas the regenerated screens must apply:
  - Portrait orientation for retrospective match entry (not landscape)
  - Home Hub with 2–3 primary buttons at MVP, evolving per phase
  - Single-tap confirmation + 15-second undo toast (no double-check dialog)
  - Player selection via avatar grid with team switcher and 3-tap auto-fill (not dropdowns)
  - Score entry via +5 (larger) / +1 steppers; `+5` hidden when `score_limit < 5`
  - Unified "My Matches" screen (pending + confirmed), no separate Confirmation Queue
  - No standalone Teams & Rules screen; management inline or in Settings
  - Post-submit state with "Record Another Match" (pre-filled) and "Home" options

**Regeneration scope & cost.** Approximately 60+ pre-BMAD screens exist in Stitch project `6006028140652349802`. Full regeneration is planned as a **dedicated effort during the Stitch screen production phase**, not as a side activity within feature implementation. This is a significant visual workstream, not a byproduct.

**Regeneration prioritization tiers.** Not all 60+ screens are equal:

- **Tier 1 (MVP blocking, ~12–15 screens):** Home Hub, New Match setup, Player Selection, Score Entry, Submit/Success, My Matches, Match Detail, Confirmation Flow. Must be regenerated before MVP.
- **Tier 2 (MVP polish):** Statistics entry points, Profile, Onboarding. May ship with heavy edits of pre-BMAD drafts if time-constrained, with a backlog ticket for proper regeneration.
- **Tier 3 (Phase 2+):** all remaining screens (Tournaments, Want to Play pools, advanced settings, admin-adjacent flows). Regenerate when the owning phase starts.

Exact screen-by-screen effort (regenerate from scratch vs. heavy edit) will be determined by the Stitch production plan that follows this specification — out of scope for this UX document but explicitly acknowledged as non-zero cost.

**Quality Gates for the Chosen Direction.** The chosen direction introduces three rules that require explicit enforcement to avoid silent drift:

| Rule | Enforcement Mechanism | Owner | Inception Deadline |
|------|----------------------|-------|--------------------|
| **Context-Dependent Asymmetry** | Per-screen classification checklist maintained in the Stitch design document: every screen is tagged as *speed-critical* (symmetric required) or *browse-mode* (editorial asymmetry permitted). Asymmetry is not a free choice — it must be justified per-screen. | Stitch design document | Before the first Tier 1 screen is regenerated |
| **WCAG AA Contrast** | Automated contrast validation in CI (e.g., Lighthouse / axe-core / Tailwind-native contrast linter). Green-on-green accessibility rule (small text on `primary-container` must use `on-surface`, not `on-primary-container`) is a validated constraint, not a convention. | CI pipeline | Before MVP release (blocking) |
| **No-Line Rule** | Code-review checklist flags any `border-*` Tailwind classes; exceptions permitted only for ghost dividers on data-dense screens with explicit `opacity-10` / `opacity-15` utilities, or for `ring-*` focus indicators. | Pull request review | Before the first PR merged after lock |

These gates turn style rules into verifiable contracts. Without them, the direction erodes quietly — one `border-gray-200` at a time.

### Design Rationale

Four direct connections between the chosen direction and the foundations established earlier in this document:

| UX Foundation | How "The Clubhouse Editorial" Serves It |
|---------------|-----------------------------------------|
| **Emotional Principle #1** — *"The app amplifies the table moment"* (Step 4) | Warm palette, tactile surfaces, and editorial typography extend the physical atmosphere of a foosball lounge into the screen — instead of replacing it with a neutral sports-tracker chrome. |
| **Emotional Principle #4** — *"Data shows, interface doesn't judge"* (Step 4) | Editorial restraint and generous typographic hierarchy present numbers as facts, not verdicts. A gaming-UI aesthetic (pulsing CTAs, achievement pop-ups, bright gradients) would contradict the neutral tone the product requires. |
| **Experience Principle #4** — *"Predictability is speed"* (Step 3) | Context-dependent asymmetry is the compromise: speed-critical flows stay symmetric and predictable (muscle memory preserved), while browse-mode screens earn the editorial off-center treatment (attention earned, not demanded). |
| **Enthusiast-first audience priority** (Step 2) | IT enthusiasts typically work in minimalist-brutalist tool environments (Vim, tmux, dense dashboards). The Clubhouse Editorial is deliberately the opposite — warm, atmospheric, loft-like — because this is a **leisure-context** product, not a work tool. Enthusiasts do not want to sit on wooden stools and sleep on iron beds after work; they want the office break-time app to feel like a clubhouse, not another terminal. The direction counterbalances the minimalism of the rest of their day. |

### Design Directions Explored

This project did not run a formal design exploration with parallel direction candidates. The final direction crystallized organically during the BMAD discovery cycle (Steps 2–6) as the product's emotional goals and audience priorities became concrete. The table below compares the pre-BMAD aesthetic that the project inherited against the direction that emerged from discovery — not as two equal candidates, but as *before* and *after* the BMAD work.

| # | Direction | Character | Status |
|---|-----------|-----------|--------|
| **A** | **"Sterile Gaming App"** (pre-BMAD Stitch drafts) | 60+ Stitch screens generated before the BMAD discovery cycle. Landscape-first match entry, generic neon-on-dark gaming aesthetic, hard borders, symmetric grid layouts across all screens, notification-heavy flows. Indistinguishable from competitor products (Kicktrack, Kickertool, Kicker.cool). | ❌ Rejected |
| **B** | **"The Clubhouse Editorial"** (Speakeasy Stadium) | Warm dark theme (`#171211` base, `#EBE0DD` cream text — never pure white). Surface hierarchy via tonal shifts, not borders (No-Line Rule). Editorial typography (Space Grotesk Bold display, Manrope body). Context-dependent asymmetry, ambient shadows, reserved team colors. | ✅ Selected |

**Other aesthetic families implicitly ruled out.** The binary in the table above does not mean only two directions were ever possible. Several other candidate aesthetics were not formally evaluated but are incompatible with foundations established in Steps 2–4:

- **Scandinavian minimalism** (light, clean, monochromatic) — contradicts the "app amplifies the table moment" principle. Too work-like; the app is leisure-context.
- **Skeuomorphism** (wood grain, felt surfaces, literal foosball table) — trivializes the physical metaphor. The kicker table is already there physically; the app should not imitate it, it should amplify it.
- **Brutalist digital** (raw, high-contrast, aggressively functional) — aligns with enthusiast minimalist-tool reflex but contradicts the leisure-counterbalance rationale (see Design Rationale → Enthusiast-first audience).
- **Retro / 8-bit gaming** — would align the product with arcade culture rather than office culture; inappropriate for the professional context.
- **Neumorphism / glassmorphism as primary language** — performance-costly and dates quickly; glassmorphism deferred as future enhancement for floating elements only.

These were not formally compared; they were ruled out by the emotional goals and audience priorities established before visual direction work began.

**Why the shift happened.** The 60+ pre-BMAD Stitch drafts were produced without the discovery work documented in Steps 2–4 of this specification. Once user archetypes were prioritized (enthusiasts first), emotional goals defined (Pride of Discovery, Sense of Growth, Minimal Tolerable Effort), and core interactions stress-tested (sub-10s recording, single-tap confirmation, unified "My Matches"), the generic gaming aesthetic stopped fitting the product. A warmer, more tactile, more confident visual language was needed — one that treats the app as an office clubhouse, not an arcade.

## User Journey Flows

Six MVP-critical flows organized around the **Record → Confirm → Discover** core loop, plus sustaining flows for onboarding, profile management, abuse prevention, and match history. Each flow specifies a Mermaid happy path, error/edge paths, component hints (Step 11 forecast), and optimization decisions. PRD personas mapped per flow.

### Flow 1: Record Retrospective Match (Critical — Survival Metric)

**Persona:** Any recorder (Max, Lisa, Kai, Viktor for non-tournament). **PRD reference:** embedded across J1/J2/J4. **Target:** end-to-end ≤10 seconds.

**Happy path:**

```mermaid
flowchart TD
    A[Home Hub] -->|Tap "New Match"| B[Match Type Picker]
    B -->|1v1: 2 player slots| C1[Player Selection — 1v1]
    B -->|2v2: 4 player slots — last used pre-selected| C2[Player Selection — 2v2]
    C1 --> D[Rule Template Picker]
    C2 --> D
    D -->|Last-used template pre-selected; "Create new..." link for power users| E[Score Entry Screen]
    E -->|Stepper +1/-1, +5 shortcut, auto-advance per game| F[Auto-detected Match End — rule template defines max_games + win_condition]
    F -->|Tap Submit| G[Submitted Toast: "Awaiting confirmation from Alex"]
    G -->|Auto-return| A
```

**Match scope is rule-template-determined.** Number of games and win condition fixed at template selection — no mid-match scope ambiguity. Auto-detected end is deterministic, not magic.

**Entry points:** Home Hub primary CTA; deep link from Want-to-Play pool match (Phase 2); QR code at table → /login → /match/new (returning user fast path).

**Component hints (Step 11 forecast):** `MatchTypeToggle`, `PlayerSlotGrid` (variant: 1v1 = 2 slots, 2v2 = 4 slots), `FrequencyOrderedPlayerList`, `RuleTemplateChip` + `RuleTemplateBuilder` (sub-flow for "Create new..."), `ScoreStepper` (with `+5` button conditionally rendered when `score_limit ≥ 5`), `SubmitFAB`, `ToastNotification`. Match draft state in `useMatchDraftStore` (Pinia) with reactive bindings to active rule template.

**Error & edge paths:**

- Duplicate player selected → inline red border on slot, "Already in match" hint, no blocking dialog
- Score exceeds rule limit → inline error under stepper, submit disabled until corrected
- All slots empty + tap Submit → Submit button stays disabled (no validation popup needed)
- Network failure on submit → optimistic UI marks match as "Pending sync"; retry on next foreground; toast: "Will retry when online". Server idempotency key (`creator_id + match_payload_hash + nonce`) prevents duplicates on retry
- **Submitter not in player slots (third-party/observer)** → both teams must confirm (PRD confirmation rule); flow appends "Confirmation requires 1 player from each team" hint above Submit button
- **Within-game position swap enabled in custom rule template** → KD-03 inline hint on Score Entry: *"Mid-game position swap recorded — positional stats limited to Tier 1 (overall). Use Live Mode for full breakdown."* Surface before user enters scores so expectations align with output
- Custom rule template needed → "Create template" link inside Rule Template picker → opens template builder sub-flow (parameters: win_condition, score_limit, tie_break, point_distribution, swap_rule). Template immutable once saved (PRD KD)

**Optimization decisions:**

- Pre-fill last-used rule + match type cuts ~2 taps from PRD baseline
- Empty player slots > pre-filled (verifying pre-filled names is slower — KD from Step 2)
- `+5` stepper hidden when `score_limit < 5`
- No confirmation dialog on Submit — Submit IS the commitment; protection via opponent confirmation
- Idempotency key on POST prevents duplicate creation on flaky networks (architectural requirement)

---

### Flow 2: Confirm Match from Push (Critical — Verification Pipeline)

**Persona:** Any confirmer (Kai's J4 act 2 maps directly). **PRD reference:** core verified-data loop. **Override of PRD FR13:** single tap + 15s undo, no double-check screen.

**Happy path:**

```mermaid
flowchart TD
    P[Push Notification — rich body: players, scores, outcome] -->|Tap notification| R[Match Review Screen]
    R -->|Verify scores at glance| C{User decision}
    C -->|Tap "Confirm"| U[15s Undo Toast — countdown visible]
    U -->|15s elapses without undo| MC[Micro-celebration banner: "Win streak: 4!"]
    U -->|User taps Undo within 15s| RB[Match returned to "Awaiting" state — no penalty to creator]
    MC -->|Auto-dismiss| S[Match committed to confirmation cooldown queue]
    S -->|First opponent: 24h cooldown begins<br/>Second opponent: immediate publish| PUB[Published to Statistics]
    C -->|Tap "Reject"| RJ[See Flow 2b: Reject Match]
```

**Micro-celebration sequencing (decided):** Celebration appears **after** the 15s undo window expires — not during. Rationale: confirmation isn't truly final until undo lapses; celebrating a stat that could be reversed creates dissonance.

**Entry points:** Push notification (90% — primary); My Matches → Pending tab badge (10% fallback when notifications dismissed/blocked).

**Component hints:** Service Worker `notificationclick` handler routes to `/match/:id/review` (Web Push subscription registered at OAuth completion, not as Vue component). `MatchReviewCard`, `ConfirmRejectButtonPair`, `UndoToast` (15s countdown emits `undo-window-expired` event for tests + celebration trigger), `MicroCelebrationBanner` ("Win streak: 4!", "You & Alex: unbeaten in 6"). Idempotency key on Confirm POST: `match_id + confirmer_id + nonce` — server dedupes if double-fired before undo expires.

**Error & edge paths:**

- User taps Undo within 15s → match returned to "Awaiting" state, micro-celebration suppressed
- Push permission denied at OAuth time → fallback flow: My Matches badge, Hub banner "3 matches awaiting your confirmation"
- Push permission revoked mid-session → on next app foreground, detect via `Notification.permission === "denied"` → show one-time re-prompt banner with "Enable notifications" CTA
- Second opponent confirms during 24h cooldown → publish immediately, both confirmers see micro-celebration
- 24h cooldown expires with only 1 confirmation (2v2 standard) → publish (one confirmation sufficient per PRD). Server is source of truth for the 15s window; client-side countdown is decorative
- Confirmer offline → push queued by OS; processed on reconnect
- Stale push (match already confirmed by other opponent) → review screen shows "Already confirmed" state, Confirm button replaced with "View match"
- **Match submitted by deleted account (placeholder identity)** → review screen shows "A retired player submitted..." copy instead of `ex-player-0042`; preserves DSGVO pseudonymization without confusing UX

**Optimization decisions:**

- Rich push body shows full match context — confirmer can decide without opening app
- 15s undo replaces double-check (FR13 override) — protects misclicks without bureaucracy
- Reject reason captured for abuse-detection telemetry (feeds Flow 6)
- Server idempotency required (architectural)

---

### Flow 2b: Reject Match (Critical — Verification Counter-Path)

**Persona:** Any confirmer who disputes a submitted match. **PRD reference:** part of verified-data loop; promoted to standalone for clarity.

**Happy path:**

```mermaid
flowchart TD
    R[Match Review Screen — from Flow 2 entry] -->|Tap "Reject"| RS[Reject Reason Selector — mandatory]
    RS -->|Choose: Wrong score / Wrong player / Match didn't happen / Other| RT[Optional free-text note ≤200 chars]
    RT -->|Tap "Submit rejection"| N[Notification sent to creator: "Alex rejected your match. Reason: Wrong score."]
    N -->|Match removed from confirmation queue| H[Return to Hub or My Matches]
```

**Entry points:** Match Review Screen (from Flow 2 push deep link or My Matches Pending tab).

**Component hints:** `RejectReasonSelector` (radio list with predefined reasons), `RejectFreeTextField` (optional, char-limited), `RejectSubmitButton`. Server emits notification to creator; updates rate-limit counters feeding Flow 6.

**Error & edge paths:**

- Rejection without reason → Submit button disabled until reason chosen
- Creator submits identical match again after rejection → permitted, but counts toward Flow 6 rejection-density throttle (5+ rejections in 24h)
- Both 2v2 opponents reject → match destroyed, no statistics impact
- Reject after first opponent already confirmed (race) → server returns conflict; client shows "Match was just confirmed by other opponent. Dispute via..." (resolution path TBD — likely chat-based out-of-band, not in MVP UX)

**Optimization decisions:**

- Reason-after-action, not before (don't ask "are you sure?" — ask "why?" after the fact). Reason becomes telemetry, not friction
- Free-text optional, never required — friction must be lower than ignoring the match
- Rejection feedback explicit to creator (closes the loop, supports self-service principle)

---

### Flow 3: Discover Statistical Insight (Critical — Loop Payoff)

**Persona:** Max (Defender's Vindication). **PRD reference:** J1. **Goal:** "That's me on top" moment.

**Happy path:**

```mermaid
flowchart TD
    A[Home Hub — rank movement banner visible] --> E1[Entry choice]
    E1 -->|Tap "My Game" tile| L[Leaderboard — Overall tab with sticky self-row]
    E1 -->|Tap own avatar in Hub header| QS[Personal Cabinet — see Flow 5]
    E1 -->|Tap any other avatar elsewhere| QSP[Quick Stats Popover]
    E1 -->|Post-confirm micro-celebration tap| MC[Inline insight chip → drill]
    L -->|Tab switch| LP[Positional tab — Attack / Defense]
    LP -->|Sort by Defense Goals Conceded ASC| HL[Self-row sticky at viewport top, original rank shown as numeric badge]
    HL -->|Tap own row| PD[Player Detail — full positional breakdown]
    PD -->|Tap H2H link| H2H[Head-to-head with opponent picker]
    H2H -->|Select opponent| HD[Cross-tab: matches/games/goals by position]
    HD -->|Tap Share| SH[Web Share API → group chat]
```

**Avatar interaction rule (documented):**

| Where avatar tapped | Action |
|---------------------|--------|
| Own avatar in Hub header | → Personal Cabinet (Flow 5) |
| Own avatar **anywhere else** (leaderboard self-row, match card) | → Quick Stats Popover |
| Any other player's avatar (anywhere) | → Quick Stats Popover |

Hub header is the single explicit Cabinet entry; everywhere else avatar = popover.

**Entry points (multiple per "Statistics find you" principle):** Home Hub rank-movement banner, Home Hub "My Game" button, avatar tap (rules above), post-confirmation micro-celebration deep link, push on rank milestone.

**Component hints:** `RankMovementBanner`, `LeaderboardTable` (sortable columns, virtualized, sticky self-row), `PositionTab` (Attack/Defense/Overall), `PlayerDetailDrawer`, `H2HCrossTabMatrix`, `AvatarInteractive` (polymorphic with `interactionMode` prop: `self-cabinet | quick-stats | profile-drawer`), `ShareButton` (Web Share API with feature detection cached in `useFeatureFlags` composable).

**Error & edge paths:**

- Player has <5 confirmed matches → leaderboard hides them (PRD FR28); demo data overlay on stats screen with "Toggle demo data" switch in Personal Cabinet (persistence: localStorage flag, auto-clears at 50 confirmed matches per PRD)
- Filter returns zero matches → empty state with "Try removing filters" CTA
- Tier 3 stats requested with mixed `rule_config_ids` → conditional fields greyed with tooltip "Available when filtered to single rule template" (KD-02 enforcement)
- Share API unsupported → fallback: copy-to-clipboard with toast "Stat copied"
- H2H requested against opponent with 0 shared matches → "You haven't played [opponent] yet — start a match?" CTA
- **Shared payload contains zero PII** — only nickname, avatar, stats; no email or OAuth identifier (DSGVO)

**Optimization decisions:**

- Three entry vectors (Hub banner, avatar, micro-celebration) — discovery happens passively, not only via dedicated screen
- Sticky self-row regardless of sort — finding own row never requires scrolling
- H2H is drill-from-detail, not top-level nav

---

### Flow 4: Onboard New Player (Important — First Impression)

**Persona:** Kai (Newcomer). **PRD reference:** J4. **Goal:** OAuth → first useful state in <60s.

**Happy path:**

```mermaid
flowchart TD
    ENTRY[Entry: PWA install / QR scan / push deep link to /match/:id/review] --> AUTH{Authenticated?}
    AUTH -->|No| L[Login Screen — Google OAuth]
    L -->|Tap Google sign-in| OA[OAuth2 redirect — captures original deep-link URL]
    OA -->|Returns with token| EX{Existing user?}
    EX -->|Yes| RD[Redirect to original deep link or Hub]
    EX -->|No| NP[Nickname Picker — async uniqueness check]
    NP -->|Type nickname, debounced 300ms, last-request-wins token| AV[Avatar Picker — 24 preset emoji/illustration grid]
    AV -->|Select avatar| TUT[Tutorial — 3 swipeable slides]
    TUT -->|Skip or finish| RD
    RD --> H[Original target screen — Hub or pending confirmation]
    AUTH -->|Yes| RD
```

**Tutorial content (3 slides mapping to core loop):**

1. **"Tap to record"** — 5-second clip/illustration of New Match flow
2. **"Tap to confirm"** — push notification → tap → done
3. **"Find your strength"** — leaderboard teaser with positional stats highlight

**Entry points:** PWA install + first launch; QR code at foosball table → web URL → /login; **deep link from match-confirmation push** (Kai's organic path) — captured pre-OAuth, restored post-OAuth.

**Component hints:** `GoogleOAuthButton`, `OAuthRedirectHandler` (preserves `intent_url` query param across redirect), `NicknameInput` (debounced uniqueness check + last-request-wins token), `AvatarPicker` (24 preset SVG emoji, zero asset cost), `TutorialCarousel` (3 slides), `HomeHub`.

**Error & edge paths:**

- OAuth fail / popup blocked → inline error with retry CTA, no nuclear logout
- Nickname taken → inline red, suggestion chips ("Pavel2", "Pavel-de", "PavelFC")
- Nickname violates rules (length, special chars) → inline regex hint
- **Deep-link-before-OAuth** (Kai pattern): user taps push from colleague before having registered → redirect URL captured in OAuth state param → post-auth resolves to original `/match/:id/review`
- Tutorial skipped → marker stored in profile; user can re-open from Personal Cabinet later
- User added to a match before completing onboarding → confirmation push waits in queue, surfaces on Hub after onboarding completion
- Email already registered (re-login) → skip nickname/avatar/tutorial, jump to Hub or deep-link target

**Optimization decisions:**

- 3-slide tutorial, not 7 — minimal commitment
- Confirmation-first organic onboarding: lowest possible barrier
- No email shown anywhere (DSGVO pseudonymization)
- Avatar preset grid keeps MVP bundle tiny (24 SVG emoji); custom upload deferred to Phase 2
- Deep-link preservation across OAuth redirect is non-trivial — flagged for architecture review

---

### Flow 5: Manage Profile (Important — Identity Maintenance)

**Persona:** All players. **PRD reference:** FR profile management (gap fill — no PRD journey covers).

**Happy path:**

```mermaid
flowchart TD
    H[Home Hub] -->|Tap own avatar in header| PC[Personal Cabinet]
    PC -->|Edit nickname| NN[Nickname Edit — locked banner with next-eligible date if within 30d cooldown]
    PC -->|Change avatar| AV[Avatar Picker — same component as Flow 4]
    PC -->|Switch language EN ↔ DE| LS[Language Switcher — i18n hot-reload]
    PC -->|Toggle "Show demo statistics"| DS[Demo data toggle — persists in profile, auto-clears at 50 confirmed matches]
    PC -->|Tap "Sign out"| SO[Sign Out Confirm Dialog]
    NN -->|Save| PC
    AV -->|Save| PC
    LS -->|Apply| PC_RELOAD[Cabinet re-renders in new locale]
```

**Entry points:** Avatar in Home Hub header (universal — "avatar always interactive" principle, Hub-header exception in avatar interaction rule).

**Component hints:** `AvatarHeaderButton`, `PersonalCabinetView`, `NicknameEditField` (with `next_change_eligible_at` lockout banner — explicit countdown), `AvatarPicker`, `LocaleSwitcher`, `DemoDataToggle`, `SignOutConfirmDialog`.

**Error & edge paths:**

- Nickname change attempted within 30 days of last change → field disabled with "Next change available on {date}"
- Nickname taken at save time (race) → re-validate, re-show suggestion chips
- Locale switch mid-session → i18n hot-reload, no app restart; pending match drafts preserved
- Sign out → token cleared, redirect to /login; PWA install state preserved
- Account deletion not in MVP — deferred (PRD requires LIA before launch)

**Optimization decisions:**

- Avatar tap = universal entry; no Settings cog or overflow menu
- Nickname lockout date shown explicitly (predictable system behavior)

---

### Flow 6: Throttle Spam Submitter (Defensive — MVP Scope)

**Persona:** Implicit (rate-limited submitter). **PRD reference:** Edge case "Spam Problem". **MVP scope reduced:** standalone limit only; tournament-referee context bypass deferred to Phase 3 (no tournament feature in MVP, no need for context-aware threshold).

**Happy path (server-driven, minimal UX surface):**

```mermaid
flowchart TD
    SUB[Submit match attempt] -->|API call| RL{Server rate-limit check}
    RL -->|Within 10/h limit| OK[Submit accepted, Flow 1 completes]
    RL -->|Exceeded 10/h| TH[Top-screen non-blocking banner: "You've submitted many matches recently. Try again in {N} minutes."]
    RL -->|5+ rejections in 24h| WARN[Warning banner: "Multiple rejections detected. Continued submissions may be throttled."]
```

**Entry points:** Triggered transparently on every match submit; no dedicated UI surface.

**Component hints:** Global `<AppThrottleBanner>` mounted in `App.vue`, listens to global error event bus; appears for any 429 response from match-submit endpoint. `WarningBanner` for soft warnings. Server-side rate-limit middleware enforces; thresholds in server config.

**Error & edge paths:**

- Throttle expired but user re-submits identical match → server idempotency dedupes (same key as Flow 1)
- Throttled user closes app → no blocking; throttle is server-enforced regardless of client state
- False positive (high-frequency legit player) → telemetry surfaces threshold-tuning need

**Optimization decisions:**

- Banner non-blocking (no modal dialog — matches "inline validation, not blocking dialogs" anti-pattern)
- Threshold values configurable from server config
- MVP scope = standalone only; Phase 3 adds tournament context bypass

---

### Flow 7: Browse Match History (Important — Sustaining Engagement)

**Persona:** All players. **PRD reference:** "Played Matches" screen + unified "My Matches" decision from Step 2.

**Happy path:**

```mermaid
flowchart TD
    H[Home Hub] -->|Tap "My Matches"| MM[My Matches — unified view]
    MM -->|Tab: Pending| MMP[Pending confirmations — badged]
    MM -->|Tab: Confirmed| MMC[Confirmed matches — chronological]
    MM -->|Filter chip: All / Favorites / By opponent / By rule| MMF[Filtered list]
    MMC -->|Tap match card| MD[Match Detail — full game-by-game breakdown, position attribution if Tier 2/3]
    MD -->|Tap player avatar| QSP[Quick Stats Popover]
    MD -->|Tap "Share"| SH[Web Share API]
```

**Entry points:** Home Hub "My Matches" button; deep link from confirmation flow ("View confirmed match"); avatar drill from H2H stats.

**Component hints:** `MyMatchesView` with tabs (`Pending` / `Confirmed`), `MatchCard` (variant per tab), `MatchFilterChips`, `MatchDetailView`, reuse `AvatarInteractive` + `ShareButton`.

**Error & edge paths:**

- Empty Confirmed tab (newcomer) → demo data option + "Record your first match" CTA
- Empty Pending tab → "All caught up" empty state
- Filter combination yields zero results → "Try removing filters" CTA
- Confirmed match selected for view but match was reassigned to deleted-account placeholder → show "A retired player participated in this match" hint, no PII

**Optimization decisions:**

- Single screen, two tabs (per Step 2 unified "My Matches" decision)
- Filter chips, not dropdowns — thumb-friendly, immediately visible
- Match card double-duty: Pending = action surface (Confirm/Reject buttons inline), Confirmed = navigation surface (tap to detail)

---

### Journey Patterns

Patterns extracted across the seven flows; promote to global UX rules.

#### Navigation Patterns

- **Avatar as universal lateral nav with documented Hub-header exception.** Tap own avatar in Hub header → Personal Cabinet. Tap own avatar elsewhere or any other player's avatar → Quick Stats Popover. One element, three depths (Hub-header tap = Cabinet route, anywhere-else tap = Popover, long-press = Profile drawer Phase 4).
- **Push notification as first-class entry.** Confirmation (Flow 2), pool fills (Phase 2), tournament deadlines (Phase 3) all enter via rich push with deep link. Hub badge is fallback, not primary.
- **Home Hub never branches contextually on launch.** Predictability over cleverness; phase-based growth is the only structural change.
- **Deep links survive auth boundary.** Pre-OAuth deep links captured in OAuth state param, restored post-auth (Flow 4 + applies to push deep links during onboarding).

#### Decision Patterns

- **Smart defaults pre-fill, never pre-confirm.** Last-used rule + match type pre-selected; submit always requires explicit tap. Pre-filled players rejected as anti-pattern.
- **Reason-after-action, not before.** Reject reason captured after Reject tap (Flow 2b). Don't ask "are you sure?" — ask "why?" once decision is made.
- **Lockout dates explicit, not silent.** Nickname lock shows next eligible date (Flow 5). Throttle shows duration remaining (Flow 6).

#### Feedback Patterns

- **Inline validation, no blocking dialogs.** Score limits, duplicate players, nickname conflicts — red border + hint text. Modal dialogs reserved for irreversible destructive actions (none in MVP).
- **Optimistic UI + 15s undo for high-frequency commits.** Submit (Flow 1) and Confirm (Flow 2) both use undo window — eliminates double-check screens while protecting misclicks.
- **Two-stage feedback for finalizing actions.** Confirm tap → undo toast (15s) → IF NOT undone → micro-celebration. Celebration appears only after action is truly finalized.
- **Micro-celebrations as reward currency.** Post-confirmation insights, rank movement banners, milestone chips. Cheap to implement, high emotional ROI per Step 4 emotional principles.
- **Empty states are CTAs, never blank.** <5 matches → demo data toggle. Zero filter results → "Try removing filters". Zero H2H → "Start a match against [opponent]". Every empty state offers a next step.

---

### Flow Optimization Principles

Cross-flow rules promoted from per-flow optimizations.

1. **Speed-budget every critical flow.** Record (Flow 1) ≤10s. Confirm (Flow 2) ≤5s from push. Onboard (Flow 4) ≤60s OAuth-to-Hub. If a flow can't meet budget, simplify the flow — don't relax the budget.

2. **Pre-fill from history, never from assumption.** Last-used values pre-selected only when user has prior data. New users see neutral defaults.

3. **Server enforces, client communicates.** Rate limits, cooldowns, nickname locks, immutability, idempotency — all enforced server-side. Client surfaces state, never gates on client-only logic.

4. **Idempotency keys on every state-changing POST.** Match submit, confirm, reject — all carry idempotency keys to dedupe under flaky networks. Architectural requirement.

5. **Deep links always work.** Push notifications, share links, QR codes resolve to specific in-app screens with proper auth gating. Pre-OAuth deep links survive the redirect.

6. **Phase-aware UI without phase-aware code.** Hub buttons gate on feature flags, not hardcoded phases. Adding Want-to-Play (Phase 2) is a flag flip.

7. **One gesture per outcome.** Confirm = one tap. Submit = one tap. Reject + reason = one tap to open reason picker, one tap to choose reason. Multi-tap reserved for power-user flows (template creation).

8. **Match scope is rule-template-determined.** Number of games and win condition fixed by selected rule template before play begins. Auto-detected match end is deterministic. No mid-match "add another game?" prompts.

## Component Strategy

### Design System Components

The project does **not** use a pre-built UI component library (no Vuetify, PrimeVue, shadcn-vue, etc.). The design system "foundation" is composed of primitives and tokens, not widgets.

**Foundation layer:**

| Layer | Provides | Coverage |
|-------|----------|----------|
| Tailwind v4 utilities | layout, spacing, typography scale, colors via tokens | 100% of visual language |
| MD3 token palette (10 MVP tokens) | `surface*`, `on-surface`, `primary*`, `secondary*`, `tertiary`, `error`, `outline-variant` | Color/surface system |
| Native HTML + browser APIs | `<button>`, `<input>`, `<dialog>` (Safari ≥15.4), Web Share API, Service Worker push | Base behavior |
| Stitch screens | Visual references for ~12-15 MVP screens after regeneration | Composition |

**Browser baseline:** Chrome ≥110, Safari ≥15.4, Firefox ≥115. Older Safari users see degraded modal positioning, not broken UX. Native `<dialog>` fallback (div + focus-trap) deferred to Phase 2 if telemetry warrants.

All 37 components in the inventory are **custom Vue 3 SFCs**. No widget-level reuse from external libraries.

### Custom Components

**Component inventory (37 total) by domain:**

**A. Match Recording (Flow 1):** `MatchTypeToggle`, `PlayerSlotGrid`, `FrequencyOrderedPlayerList`, `RuleTemplateChip`, `RuleTemplateBuilder`, `ScoreStepper`, `SubmitFAB`

**B. Confirmation (Flows 2, 2b):** `MatchReviewCard`, `ConfirmRejectButtonPair`, `UndoToast`, `MicroCelebrationBanner`, `RejectReasonSelector`, `RejectFreeTextField`

**C. Statistics & Discovery (Flows 3, 7):** `LeaderboardTable`, `PositionTab`, `RankMovementBanner`, `PlayerDetailDrawer`, `H2HCrossTabMatrix`, `QuickStatsPopover`, `MatchCard`, `MatchFilterChips`, `MatchDetailView`

**D. Identity & Onboarding (Flows 4, 5):** `GoogleOAuthButton`, `NicknameInput`, `AvatarPicker`, `AvatarBase`, `AvatarSelfHub`, `AvatarQuickStats`, `AvatarHeaderButton` (alias of `AvatarSelfHub`), `TutorialCarousel`, `PersonalCabinetView`, `LocaleSwitcher`, `DemoDataToggle`, `SignOutConfirmDialog`

> Note: `OAuthRedirectHandler` is **not a component** — it is a router guard. Specified in `architecture-frontend.md` Routing section, not in this inventory.

**E. Global / Cross-cutting:** `HomeHub`, `MyMatchesView`, `AppThrottleBanner`, `WarningBanner`, `ToastNotification` (generic), `ShareButton`, `EmptyState`

**F. Layout primitives:** `Surface`, `AmbientShadow`, `EditorialHeading`

#### Decision Boundaries (explicit non-goals)

- **No virtualized scroll** — pagination 20/page chosen for MVP (`LeaderboardTable`). Re-evaluate if telemetry shows scroll fatigue at 100+ players.
- **No drag-and-drop** — slot assignment via tap, no rearrangement.
- **No rich text editor** — reject free-text is plain ≤200-char `<textarea>`.
- **No image upload** — avatars are 24 preset SVG only.
- **No automated visual regression suite** — manual Stitch comparison at story acceptance (MVP); Percy/Chromatic deferred to Phase 2.
- **No native `<dialog>` fallback** — accept Safari ≥15.4 baseline.

#### Component Spec Template

```markdown
### [Component Name]

**Purpose:** [Clear purpose statement]
**Usage:** [When and how to use]
**Anatomy:** [Visual breakdown of parts]
**States:** [All possible states]
**Variants:** [Different sizes/styles]
**Responsive:** [Breakpoint behavior — mobile-first, sm:/lg: changes]
**Accessibility:** [ARIA labels, keyboard navigation]
**Content Guidelines:** [What content works best]
**Interaction Behavior:** [How users interact]
**Last Synced:** YYYY-MM-DD with Stitch screen [name]
```

#### Tier 1 — Full Specs (6 critical-path components)

##### ScoreStepper

**Purpose:** Capture per-game score per team in ≤2 taps to common values (≤5 goals).
**Usage:** Score Entry screen, one instance per team per game.
**Anatomy:** `[-]` button · numeric display (`headline-md`) · `[+]` button · `[+5]` button (conditional).
**States:** `default` · `at-min` (0, `[-]` disabled) · `at-effective-max` (computed from `effective_max = win_condition === 'continue_on_tie' && tied ? null : score_limit`; null = no max) · `error` (server-rejected value, red border + hint) · `disabled` (game complete).
**Variants:** `with-plus5` (when `score_limit ≥ 5`) / `compact` (when `< 5`).
**Responsive:** Touch target ≥44×44px both breakpoints. `lg:` increases spacing between steppers; button size unchanged.
**Accessibility:** `<button aria-label="Increase score for [team]">`, `aria-live="polite"` on numeric display, keyboard `+`/`-` shortcuts when focused, `role="spinbutton"` with `aria-valuenow`/`aria-valuemin`/`aria-valuemax` (max omitted when null).
**Content:** 0–99 integers. No decimals.
**Interaction:** Tap `+1` → optimistic increment, auto-advance to next game when both teams reach win_condition AND tie_break resolved.
**Last Synced:** —

##### PlayerSlotGrid

**Purpose:** Select 2 (1v1) or 4 (2v2) players in ≤3 taps via auto-fill from frequency-ordered avatar grid.
**Usage:** Match recording Step 1.
**Anatomy:** Slot frame (`surface-bright`) · avatar placeholder · player name label · clear `[×]`. 2v2: 2-column layout — own team bottom (Defense / Attack), opponents top.
**States:** `empty` · `filled` · `duplicate-error` (red border + "Already in match") · `team-switching` (2v2 only) · `submitter-not-included-warning` (third-party submitter — shows hint "Confirmation requires 1 player from each team").
**Variants:** `1v1` (2 slots, vertical) / `2v2` (4 slots, defense-attack columns).
**Responsive:** 1v1 stays vertical at all breakpoints. 2v2: columns at all breakpoints; `lg:` widens slot frames, no layout reflow.
**Accessibility:** Each slot `<button aria-label="Empty slot, tap to assign player">` / `aria-label="Alex, tap to remove"`. Team-switcher separate labeled control with `aria-pressed`.
**Content:** 24px SVG emoji avatar + nickname truncated at 14 chars + ellipsis.
**Interaction:** Tap empty slot → `FrequencyOrderedPlayerList` overlay → tap player → auto-advance to next empty slot.
**Last Synced:** —

##### UndoToast

**Purpose:** Provide server-authoritative reversal window after Submit/Confirm without blocking dialog.
**Usage:** Bottom-screen overlay after Flow 1 submit and Flow 2 confirm.
**Anatomy:** Toast container (`surface-container-highest`, `AmbientShadow`) · message · circular countdown indicator · `[Undo]` text button.
**States:** `active` (countdown running, server window open) · `undoing` (Undo tapped, POST in flight, button disabled) · `undone` (server confirmed undo, dismiss + revert) · `expired` (server window closed, dismiss + invoke `onExpired`).
**Variants:** `submit` ("Match submitted — undo?") / `confirm` ("Match confirmed — undo?").
**Responsive:** Fixed bottom inset; `sm:` full-width; `lg:` max-width 480px centered.
**Accessibility:** `role="status"` `aria-live="polite"`. Countdown announced once at appearance ("undo available for 15 seconds"). Undo button focusable; Esc dismisses without undo.
**Content:** ≤60 chars message, single CTA.
**Interaction:** **Server is source of truth for the 15s window.** Client countdown is decorative. Component accepts `clock` provider (default = real `Date.now`, tests inject fake) and callbacks `onUndo`, `onExpired`. Host composer (`useMatchSubmitWithUndo`) wires server confirmation of undo state. No event-bus emission — explicit callbacks only.
**Last Synced:** —

##### LeaderboardTable

**Purpose:** Discover own rank movement and positional standing via paginated list with sticky self-row.
**Usage:** Flow 3 Leaderboard view; Hub deep-link target.
**Anatomy:** Sortable column headers · paginated row list (20 rows/page) · **sticky self-row at viewport top** with original-rank numeric badge · pagination controls · empty-state slot.
**States:** `loading` (skeleton rows) · `viewer-ineligible` (current user has <5 confirmed matches → demo-data toggle CTA, table shows other players normally) · `data-empty` (no players match filter at all) · `filtered-empty` ("Try removing filters") · `error`.
**Variants:** `overall` / `attack` / `defense` (driven by `PositionTab`).
**Responsive:** Mobile (`<sm`): hide secondary metric column. `lg:` shows all columns + denser row spacing.
**Accessibility:** Headers `<th aria-sort>`, sticky self-row uses `aria-current="row"`, pagination controls `aria-label="Page X of Y"`. Sort interactions: keyboard Enter/Space on headers.
**Content:** Rank · avatar · nickname · primary metric · secondary metric (`lg:` only on small viewports). Self-row visually distinguished via `primary-container` background.
**Interaction:** Tap row → `PlayerDetailDrawer`. Sort header → re-rank within page. Sticky self-row preserved across sort/page changes (server returns self-row separately if not on current page).
**Last Synced:** —

##### MatchReviewCard

**Purpose:** Confirm or reject a match in ≤5s from push deep-link with full glance context.
**Usage:** `/match/:id/review` (Flow 2 entry).
**Anatomy:** Players block (avatars + names per team) · scores grid · rule template chip · timestamp · `ConfirmRejectButtonPair`.
**States:** `default` (await action) · `confirming` (POST in flight, both buttons disabled with spinner) · `rejecting` (mirrored) · `disabled` (review state changed externally).
**Variants:** `1v1` / `2v2` slot composition.
**Responsive:** Mobile: vertical stack. `lg:` two-column (players left, scores right) with action buttons full-width below.
**Accessibility:** Logical reading order — players, scores, rule, then actions. Confirm/Reject explicit `aria-label` ("Confirm match between Alex and you, score 5-3"). Race state uses `role="alert"` for opponent-already-rejected banner.
**Content:** Zero PII. Pseudonymized for deleted submitter.
**Interaction:** Confirm → `confirming` state until server ACK → `UndoToast`. Reject → `rejecting` state → push to `RejectReasonSelector`.
**Last Synced:** —

##### AvatarBase + Wrappers (replaces polymorphic AvatarInteractive)

**`AvatarBase`** — pure visual primitive.

- **Purpose:** Render player avatar with optional rank badge and team-color outline.
- **Usage:** Used inside wrappers; not used directly by feature views.
- **Anatomy:** Circular SVG avatar · optional rank badge slot · optional team-color outline.
- **States:** `default` · `pressed` · `loading` (async deleted-player check pending — neutral skeleton) · `default-error` (deleted-player resolved → grey placeholder, no PII).
- **Variants:** Sizes `sm` (24px), `md` (40px), `lg` (64px).
- **Responsive:** Size invariant across breakpoints; chosen by parent.
- **Accessibility:** Renders as `<span>` (no interactivity). Decorative `aria-hidden="true"` on SVG; alt text exposed by parent wrapper.
- **Content:** SVG emoji from preset grid.
- **Interaction:** None (pure visual). Wrappers add behavior.

**`AvatarSelfHub`** — Hub-header only, opens Personal Cabinet.

- **Purpose:** Single explicit Cabinet entry per Avatar Interaction Rule (cross-reference Step 12 UX Patterns).
- **Usage:** Hub header exclusively.
- **Anatomy:** `AvatarBase` size `md` + invisible button overlay.
- **Accessibility:** `<button aria-label="Open your personal cabinet">`.
- **Interaction:** Tap → route `/cabinet`.

**`AvatarQuickStats`** — anywhere-else, opens popover.

- **Purpose:** Quick-stats popover entry (own avatar non-Hub OR any other player anywhere).
- **Usage:** Leaderboard rows, match cards, popovers, detail screens.
- **Anatomy:** `AvatarBase` + invisible button overlay.
- **Accessibility:** `<button aria-label="Open [nickname]'s quick stats">`.
- **Interaction:** Tap → opens `QuickStatsPopover`.

`AvatarHeaderButton` is an alias of `AvatarSelfHub`.

#### Tier 1 — Medium Specs (6 newly promoted; full content/interaction at story time)

##### Surface

**Purpose:** Enforce No-Line Rule + 3-level surface hierarchy as wrapper primitive. Every container surface uses this — no raw `bg-*` classes in feature components.
**Anatomy:** `<div>` with token-driven background, no border ever.
**States:** `default`.
**Variants:** `level="base"` (`surface`) / `level="container"` (`surface-container`) / `level="highest"` (`surface-container-highest`) / `level="bright"` (`surface-bright`, match-flow inputs only).
**Accessibility:** Pure wrapper, `role` and ARIA inherited from content.
**Responsive:** None — wrapper is layout-agnostic.

##### HomeHub

**Purpose:** Predictable start screen, phase-aware via feature flags. Never branches contextually on launch.
**Anatomy:** Avatar header (uses `AvatarSelfHub`) · rank movement banner slot · primary action buttons (gated by feature flags) · `MyMatches` entry.
**States:** `loading` (initial profile fetch) · `default` · `error` (profile load failed → retry CTA, no nuclear logout).
**Variants:** Phase-driven via `useFeatureFlags` — MVP shows `["New Match", "My Matches", "My Game"]`; Phase 2 adds "Want to Play"; Phase 3 adds "Tournament".
**Responsive:** Mobile single-column; `lg:` two-column with feed/banner left, actions right.
**Accessibility:** Action buttons explicit `aria-label`; rank movement banner `role="status"` `aria-live="polite"`.

##### ConfirmRejectButtonPair

**Purpose:** Single-tap commit pair for match review. Idempotency-aware (reads key from `useMatchReviewStore`, does not generate).
**Anatomy:** Two side-by-side buttons — Confirm (`primary-container`) / Reject (text button).
**States:** `default` · `confirming` (Confirm shows spinner, Reject disabled) · `rejecting` (mirrored) · `disabled` (review state changed externally).
**Variants:** None.
**Responsive:** Stacks vertically on `<sm`, side-by-side `sm+`.
**Accessibility:** Both buttons explicit labels with match context. Disabled state announces reason via `aria-describedby`.

##### MicroCelebrationBanner

**Purpose:** Post-confirmation reward surface (after `UndoToast` `expired`). Cheap emotional payoff per Step 4 principles.
**Anatomy:** Inline banner with insight chip + optional drill-link CTA.
**States:** `entering` (fade-in) · `default` · `dismissing` (fade-out, auto after 4s).
**Variants:** Insight templates — `streak`, `partnership`, `personal-best`, `rank-jump`. Content sourced from i18n catalog.
**Responsive:** Full-width on mobile; `lg:` constrained to feed column width.
**Accessibility:** `role="status"` `aria-live="polite"`. Drill-link is real `<a>`/`<button>`, not div+onclick.
**Content Catalog Dependency:** Trigger conditions + EN/DE copy live in `i18n/celebrations.yaml`. Content design owned by UX (Sally); translation owned by Pavel (current solo setup). Block component story acceptance until catalog populated for MVP triggers.

##### MatchTypeToggle

**Purpose:** Switch between 1v1 and 2v2 in match draft.
**Anatomy:** Segmented control with two options.
**States:** `default` · `disabled` (after first player selected — switching would clear slots).
**Variants:** None.
**Responsive:** Fixed compact size both breakpoints.
**Accessibility:** `role="radiogroup"`; each segment `role="radio"` with `aria-checked`.

##### FrequencyOrderedPlayerList

**Purpose:** Player picker overlay with frequency-based ordering (recent partners first), alphabetical tiebreak.
**Anatomy:** Modal overlay · search input · scrollable avatar grid (3 columns mobile, 5 columns `lg:`).
**States:** `loading` · `default` · `empty-search-result` (CTA: "Invite [search term]" if Phase 2+) · `error`.
**Variants:** None.
**Responsive:** Bottom sheet on mobile; centered modal on `lg:`.
**Accessibility:** Search input `aria-label="Search players"`; results announced via `aria-live`. Modal traps focus; Esc dismisses.

#### Tier 2/3 — Stub Specs

Each Tier 2/3 component carries a stub spec (purpose + usage). Full spec is authored at story-implementation time per Definition of DoD (see Implementation Strategy). Component lists appear in the Implementation Roadmap below.

### Component Implementation Strategy

**Implementation principles:**

1. **Build on Tailwind tokens, not raw hex.** Design-token contract enforced via lint/code-review.
2. **Stitch first → Vue second sync rule, enforced by export step in commit pipeline.** Every Stitch screen change exports `screen.png` + DESIGN.md to `_project-spec/DESIGN/<screen-name>/` via Stitch MCP `get_screen` before commit. CI rejects PRs that touch a Vue component without a corresponding `_project-spec/DESIGN/` artifact (when applicable). Manual visual comparison at story acceptance — no automated visual regression for MVP (Phase 2 deferred).
3. **Idempotency keys live in stores, not components.** `useMatchDraftStore` generates a key on draft start; `useMatchReviewStore` generates a key on review-screen mount. Components read the key from the store and pass it to POST. Same intent across re-mounts → same key.
4. **Server is source of truth for time-bounded state.** Undo windows, cooldowns, rate limits — client surfaces decoratively, server enforces.
5. **Polymorphism via wrappers, not props.** Three-mode `AvatarInteractive` rejected → three explicit components over `AvatarBase`. Rule: behavior variants get separate components; visual variants get props.
6. **Component extraction rule.** Extract into a named SFC if **≥2 use sites OR ≥3 states/variants OR non-trivial behavior (timer, async, focus management)**. Otherwise inline at use site. Re-evaluate when a third use site appears.
7. **A11y baseline.** Every component ships ARIA label, keyboard support, `aria-live` for dynamic content. Tier 1 fully tested; Tier 2 happy path; Tier 3 smoke (per Test Priority below).
8. **State is shared via Pinia, not prop-drilled.** `useMatchDraftStore`, `useMatchReviewStore`, `useFeatureFlags`, `useAuthStore`. Components stateless where possible.

**Definition of Done — component:**

A component is "done" when ALL of the following hold:

- ✅ Stitch screen exists (or n/a if pure logical/wrapper) and exported to `_project-spec/DESIGN/<screen>/`
- ✅ Vue 3 SFC implements all spec'd states and variants
- ✅ Unit tests cover state transitions (Tier 1: 100%; Tier 2: happy path + critical edges; Tier 3: smoke)
- ✅ A11y check: ARIA labels present, keyboard navigation tested, `axe-core` reports no violations on default state
- ✅ Manual visual comparison vs Stitch screen at story acceptance (Pavel signs off in PR)
- ✅ Spec frontmatter `Last Synced: YYYY-MM-DD with Stitch screen [name]` updated
- ✅ Used in at least one feature view OR documented as primitive-only with reference

**Test Priority Strategy:**

| Tier | State coverage | A11y | Visual |
|------|---------------|------|--------|
| Tier 1 | 100% (all states, all variants) | full audit (axe + manual keyboard) | manual comparison Stitch → component at story |
| Tier 2 | happy path + critical edges (errors, empty) | axe-core scan | manual comparison |
| Tier 3 | smoke (default state renders) | axe-core scan | spot-check |

**Cross-references:** Avatar Interaction Rule is documented in Step 12 (UX Patterns) as a global pattern. `AvatarSelfHub` and `AvatarQuickStats` specs reference it; they do not duplicate it.

### Implementation Roadmap

**Phase 1 — Tier 1 Core (12 components — blocks MVP critical path):**

| Component | Blocks Flow | Reason |
|-----------|-------------|--------|
| `Surface` | All | Foundation invariant — No-Line Rule + 3-level hierarchy |
| `ScoreStepper` | F1 | Score Entry impossible without |
| `PlayerSlotGrid` | F1 | Player Selection core |
| `MatchTypeToggle` | F1 | Match draft entry point |
| `FrequencyOrderedPlayerList` | F1 | Player picker overlay |
| `MatchReviewCard` | F2 | Push deep-link target |
| `ConfirmRejectButtonPair` | F2 | Single-tap commit primitive |
| `UndoToast` | F1, F2 | Replaces FR13 double-check; commit-protection invariant |
| `MicroCelebrationBanner` | F2 | Post-undo reward — Phase 1 epic completion |
| `LeaderboardTable` | F3 | Loop payoff surface |
| `AvatarBase` + `AvatarSelfHub` + `AvatarQuickStats` | F2, F3, F5, F7 | Cross-flow universal nav primitive |
| `HomeHub` | All | First-impression critical path |

**Phase 2 — Tier 2 MVP-required (~14 components):**

`SubmitFAB`, `RuleTemplateChip`, `RuleTemplateBuilder` (promoted from Tier 3 — custom rule templates = MVP differentiator vs spreadsheet alternatives), `RejectReasonSelector`, `RejectFreeTextField`, `PositionTab`, `MatchCard`, `MatchFilterChips`, `GoogleOAuthButton`, `NicknameInput`, `AvatarPicker`, `TutorialCarousel`, `PersonalCabinetView`, `MyMatchesView`, `EmptyState` (promoted from Tier 3 — global pattern requires Tier 2 baseline; used as slot composition by `LeaderboardTable`, `MyMatchesView`, `H2HCrossTabMatrix`).

**Phase 3 — Tier 3 supporting + polish (~11 components):**

`RankMovementBanner`, `PlayerDetailDrawer`, `H2HCrossTabMatrix`, `QuickStatsPopover`, `MatchDetailView`, `LocaleSwitcher`, `DemoDataToggle`, `SignOutConfirmDialog`, `AppThrottleBanner`, `WarningBanner`, `ToastNotification` (generic), `ShareButton`, `AmbientShadow`, `EditorialHeading`.

**Phase 1.5 deferred:** "Rod" Scoreboard (live mode signature component).

9. **Telemetry for speed budgets** (forecast for Step 14 instrumentation): every critical flow emits `flow_completed { flow_name, elapsed_ms }` event. Without instrumentation, speed targets are aspirational.

## UX Consistency Patterns

For Tic-Tac-Tore, "predictability = speed." We use a limited set of patterns so that users can act on muscle memory, especially under time constraints (office breaks).

### Button Hierarchy

We strictly separate buttons by importance and context:

| Hierarchy | Component / Style | Usage |
|-----------|-------------------|-------|
| **Primary (Floating)** | `SubmitFAB` | Single primary action on match recording screen. Always in the same place (bottom-right). |
| **Primary (Container)** | `primary-container` | Key transitions and confirmations (e.g., "Confirm" in match review). |
| **Secondary** | `surface-container-highest` | Secondary actions, toggles (e.g., match type selection 1v1/2v2). |
| **Tertiary / Utility** | Text buttons (`on-surface`) | "Undo", "Reject", "Cancel" actions. Minimal visual weight. |
| **Contextual Speed** | Large vs. Small Steppers | `+5` button is visually larger than `+1` in score entry to be instantly found by the eye. |

### Feedback Patterns

Feedback is always non-blocking and aimed at reinforcing success or preventing errors without breaking the flow.

- **Optimistic UI + Undo:** We don't ask "Are you sure?". We perform the action and give a 15-second window to cancel via `UndoToast`. This removes redundant clicks in 99% of cases.
- **Micro-Celebrations:** After final confirmation (when the Undo window closes), a `MicroCelebrationBanner` appears with an insight. This turns routine into a reward.
- **Inline Validation:** Errors (exceeding score limit, duplicate player) are shown via red borders and hint text directly at the element. No modal error dialogs.
- **Global Throttling:** When server limits are hit (spam), a non-blocking banner appears at the top with a wait timer.

### Form Patterns

Our forms are selections, not text entry.

- **Smart Defaults:** We always pre-fill the last used rules and match type. In 90% of cases, the user needs zero setup.
- **3-Tap Player Selection:** Slot auto-filling works "left-to-right, bottom-to-top." The user simply taps avatars in the list 3 times, and they fly into their slots.
- **Frequency-First Sorting:** In player selection lists, recent opponents are always at the top. Alphabetical order is a fallback for rare cases.

### Navigation Patterns

- **Avatar Interaction Rule:** 
    - Avatar in Hub header → route to Personal Cabinet.
    - Avatar anywhere else → open `QuickStatsPopover`.
- **Education via Onboarding:** Instead of visual hints in the interface, avatar interaction rules are explained on the 3rd slide of onboarding (Flow 4). This keeps the UI "clean" for experienced users.
- **Push-First Entry:** The primary entry point for match confirmations is via rich push notifications.
- **Home Hub Predictability:** The main screen structure is fixed to build muscle memory.

### Additional Patterns

- **Empty State is a CTA:** Every empty screen (no matches, no stats) must contain a Call to Action: "Record First Match," "Toggle Demo Data," or "Invite a Colleague."
- **Spatial UI Mapping:** In Live mode (Phase 1.5), element placement strictly maps to physical player positions at the table.
- **No-Line Separation:** Boundaries between sections are created by tonal shifts (Surface Hierarchy), not lines. This makes the interface feel less "cluttered" and more modern.

## Responsive Design & Accessibility

### Responsive Strategy

The Tic-Tac-Tore experience is designed for the modern mobile ecosystem, acknowledging that users in an office environment (itemis GmbH) utilize current-generation devices.

- **Modern Mobile Focus:** Primary optimization for high-resolution displays (Pixel 7 Pro, S10 Plus, iPhone 14+). 
- **Landscape Live Mode (Phase 1.5):** A dedicated spatial layout for matches in progress, where the device sits flat on the table and maps to the physical field.
- **Desktop Dashboarding:** Large screens (≥1024px) utilize a multi-panel layout for statistics analysis, providing persistent side navigation and data-dense H2H matrices to minimize vertical scrolling.

### Breakpoint Strategy

Targeting modern viewports allows for a more relaxed and readable "Clubhouse" atmosphere.

- **Compact (Standard Mobile):** 375px – 639px. Focused single-column flows.
- **Medium (Tablet / Large Mobile):** 640px – 1023px. Two-column Hub layout, increased data density in lists.
- **Expanded (Desktop):** 1024px+. Full-screen dashboards with spatial separation of navigation and content.

### Accessibility Strategy

Targeting **WCAG AA** compliance to ensure a professional and inclusive experience.

- **Visual Clarity:** "The Clubhouse Editorial" theme provides a 14.2:1 contrast ratio (exceeding AAA) for primary text.
- **Tactile Reliability:** Touch targets are maintained at a minimum of 48x48px to prevent errors during high-intensity office sessions.
- **Focus Management:** Bright `ring-primary` indicators are mandatory for keyboard navigation, compensating for the lack of visual borders (No-Line Rule).
- **Safe Area Awareness:** Layout explicitly respects `env(safe-area-inset-*)` to ensure interactive elements are never obscured by modern device notches or rounded corners.

### Testing Strategy

- **Real Device Matrix:** Testing is conducted on owner-available Pixel 7 Pro and Samsung S10 Plus, plus colleague-provided iPhone 14+ devices.
- **Browser Baseline:** Optimization for Chromium (Android) and WebKit (iOS) rendering engines.
- **Automated Checks:** Integration of `axe-core` within the Playwright test suite for component-level accessibility audits.

### Implementation Guidelines

- **Relative Units:** Use `rem` and `em` for typography and spacing to support user-driven text scaling.
- **Tailwind v4 Modern Primitives:** Leverage container queries and logical properties where beneficial for modern viewports.
- **Semantic HTML First:** Use native elements (`<button>`, `<main>`, `<nav>`) to ensure consistent assistive technology behavior.
