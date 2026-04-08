---
stepsCompleted: [1, 2, 3]
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
  - ".gemini/project-spec/DESIGN/DESIGN.md"
  - ".gemini/project-spec/DESIGN/stitch_tic_tac_tore/home_hub_font_updated/screen.png"
  - ".gemini/project-spec/DESIGN/stitch_tic_tac_tore/personal_cabinet_font_updated/screen.png"
  - ".gemini/project-spec/DESIGN/stitch_tic_tac_tore/new_match_selection_cleaned/screen.png"
  - ".gemini/project-spec/DESIGN/stitch_tic_tac_tore/results_entry_landscape_mobile/screen.png"
  - ".gemini/project-spec/DESIGN/stitch_tic_tac_tore/live_match_annotated_updates_refined/screen.png"
  - ".gemini/project-spec/DESIGN/stitch_tic_tac_tore/teams_rules_font_updated/screen.png"
  - ".gemini/project-spec/DESIGN/stitch_tic_tac_tore/i_want_to_play_font_updated/screen.png"
  - ".gemini/project-spec/DESIGN/stitch_tic_tac_tore/tournament_match_annotated_updates_applied/screen.png"
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
| Rate limiting on submissions | Automatic, context-aware thresholds |

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