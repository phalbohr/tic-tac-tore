---
stepsCompleted:
  - step-01
  - step-02
  - step-03
inputDocuments:
  - _bmad-output/planning-artifacts/prd.md
  - _bmad-output/planning-artifacts/architecture.md
  - _bmad-output/planning-artifacts/ux-design-specification.md
---

# tic-tac-tore - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for tic-tac-tore, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Player can create a retrospective match record for a completed 1v1 match.
FR2: Player can create a retrospective match record for a completed 2v2 match.
FR3: Player can select a rule system (ITSF, DTFB, or Custom template) before entering match data.
FR4: Player can record a live match in real-time by tapping screen quadrants.
FR5: Third-party observer (referee) can record a match with adapted UI.
FR6: System automatically completes a game/match when win condition is met.
FR7: Player can swap teammate positions between games.
FR8: Player can swap teammate positions within a game during live match.
FR9: Player can undo the last recorded goal during live match entry.
FR10: System displays a live activity timeline during live match.
FR11: System prevents screen dimming during live match.
FR12: System sends a confirmation request to opponents when a match is submitted.
FR13: Confirming player completes confirmation via single-tap with a 15-second undo window.
FR14: System applies context-aware confirmation rules (1v1, 2v2, referee).
FR15: System enforces a 24-hour cooldown after first confirmation before publishing.
FR16: Confirmed match data is immutable.
FR17: Player can reject a submitted match for correction.
FR18: System enforces a 48-hour confirmation window for tournament matches.
FR19: Player can view an individual leaderboard with sortable columns.
FR20: Filter statistics by rule system, match type, and time period.
FR21: View individual statistics with positional breakdown (attack/defense).
FR22: View visual stat bars showing proportional breakdowns.
FR23: View team statistics showing pair-level performance.
FR24: View head-to-head statistics with cross-tabulated tables.
FR25: Filter statistics by named teams or "Favorites".
FR26: Filter statistics by specific tournament.
FR27: System paginates statistics with configurable page size.
FR28: Minimum games played threshold for leaderboard visibility.
FR29: User sign-up and login via Google OAuth2.
FR30: Set avatar in personal cabinet.
FR31: Change nickname at most once per month.
FR32: Select interface language (EN/DE).
FR33: Account deletion with identity anonymization.
FR34: System stores minimal personal data.
FR41: Create tournaments with configurable parameters (cup/championship).
FR43: Automatic tournament bracket generation with strength-based seeding.
FR48: System awards achievements (badges, milestones).
FR49: Humorous anti-achievements for fails.
FR54: Installable Progressive Web App (PWA).
FR55: Push notifications for confirmations, pools, tournaments.
FR57: Demo/seed data for new users.
FR58: Automated context-aware rate limiting on match submissions.
FR60: View match history with filtering and pending section.

### NonFunctional Requirements

NFR1: Performance - First Contentful Paint < 1.5s, TTI < 3s (4G).
NFR2: Match entry duration < 10s (end-to-end).
NFR3: Security - Google OAuth2, stateless JWT (24h expiry).
NFR4: Security - Rate limiting (max 10 submissions/hour/user).
NFR5: Compliance - GDPR (anonymization, minimal data, LIA before launch).
NFR6: Reliability - Flyway migrations, optimistic locking.
NFR7: UX - Optimistic UI updates (<50ms visual feedback).
NFR8: Architecture - Rule templates are immutable (KD-01).
NFR9: Architecture - Rule-agnostic statistics (AD-05).

### Additional Requirements

- **AD-01: Immutable RuleConfiguration**: Any change creates a new record.
- **AD-02: Isolated Verification Pipeline**: Statistics only query PUBLISHED matches.
- **AD-03: Token Revocation**: Redis-based denylist for account deletions.
- **AD-04: GDPR Anonymization**: Permanent placeholder identity for deleted players.
- **IP-04: 500-Line Rule**: No source file or test class exceeds 500 lines.
- **Starter Template**: Custom Layered Monolith (Spring Boot 4.0 + Vite 8).
- **Styling**: Tailwind CSS v4 + SCSS with `ch-` prefix isolation.

### UX Design Requirements

UX-DR1: Portrait orientation for retrospective entry (mobile-in-hand posture).
UX-DR2: Home Hub starts with 2-3 primary buttons, growing with product phases.
UX-DR3: No-Line Rule: boundaries via background shifts, no 1px borders.
UX-DR4: 15-second Undo Toast after confirmation/submission (single-tap UX).
UX-DR5: Avatar Interaction: Header tap -> Cabinet, Elsewhere tap -> Quick Stats Popover.
UX-DR6: Typography: Space Grotesk (display), Manrope (body).
UX-DR7: "The Clubhouse Editorial" theme: warm darks, no pure white text.
UX-DR8: Mobile-first responsive: full-width mobile, 480px max-width on lg screens for cards.
UX-DR9: Push-first entry: rich notifications as primary entry for confirmations.

### FR Coverage Map

FR1: Epic 2 - Retrospective match 1v1
FR2: Epic 2 - Retrospective match 2v2
FR3: Epic 2 - Rule system selection
FR4: Epic 5 - Live match recording
FR5: Epic 5 - Referee live mode
FR6: Epic 2 - Auto-completion logic
FR7: Epic 2 - Position swap (between games)
FR8: Epic 5 - Position swap (within game)
FR9: Epic 5 - Goal undo (live mode)
FR10: Epic 5 - Activity timeline
FR11: Epic 5 - Screen wake lock
FR12: Epic 3 - Confirmation request
FR13: Epic 3 - Single-tap confirmation + undo
FR14: Epic 3 - Context-aware verification rules
FR15: Epic 3 - 24h publication cooldown
FR16: Epic 3 - Match immutability
FR17: Epic 3 - Match rejection
FR18: Epic 6 - Tournament confirmation deadline
FR19: Epic 4 - Sortable leaderboard
FR20: Epic 4 - Statistics filtering
FR21: Epic 4 - Positional breakdown
FR22: Epic 4 - Visual stat bars
FR23: Epic 4 - Team statistics
FR24: Epic 4 - Head-to-head stats
FR25: Epic 4 - Team/Favorites filtering
FR26: Epic 6 - Tournament stats
FR27: Epic 4 - Statistics pagination
FR28: Epic 4 - Games threshold filter
FR29: Epic 1 - Google OAuth2
FR30: Epic 7 - Avatar management
FR31: Epic 1 - Nickname management
FR32: Epic 1 - Language selection
FR33: Epic 1 - Account deletion & Anonymization
FR34: Epic 1 - Minimal data storage
FR41: Epic 6 - Tournament creation
FR43: Epic 6 - Bracket generation & Seeding
FR48: Epic 7 - Achievement system
FR49: Epic 7 - Anti-achievements
FR54: Epic 2 - PWA Installability
FR55: Epic 3 - Push notifications (Confirmations)
FR57: Epic 4 - Demo/seed data
FR58: Epic 3 - Submission rate limiting
FR60: Epic 4 - Match history view

## Epic List

### Epic 1: Quick Start (Auth & Basic Profile)
Focus on frictionless onboarding via Google OAuth2, automatic nickname generation from email, and basic profile settings.
**FRs covered:** FR29, FR31, FR32, FR33, FR34.

### Epic 2: Retrospective Match Entry & Rule Systems
Core functionality allowing players to quickly record 1v1 and 2v2 matches after they are played.
**FRs covered:** FR1, FR2, FR3, FR6, FR7, FR54.

### Epic 3: Data Verification & Trust
Implementation of the match verification pipeline, including opponent confirmations and anti-spam measures.
**FRs covered:** FR12, FR13, FR14, FR15, FR16, FR17, FR55, FR58.

### Epic 4: Individual & Team Analytics
Comprehensive statistics suite including global leaderboards and demo data for new users.
**FRs covered:** FR19, FR20, FR21, FR22, FR23, FR24, FR25, FR27, FR28, FR57, FR60.

### Epic 5: Live Mode (Real-time Scoring)
Real-time match tracking with goal-by-goal recording, timeline, and optimized UI for table-side use.
**FRs covered:** FR4, FR5, FR8, FR9, FR10, FR11.

### Epic 6: Tournament Management
Tools for organizing tournaments, featuring bracket generation and seeding, integrated with Live Mode.
**FRs covered:** FR18, FR41, FR43, FR44, FR45, FR46, FR47.

### Epic 7: Engagement & Polish
Personalization via avatars, achievement systems (badges), and UI refinements.
**FRs covered:** FR30, FR48, FR49, FR50, FR51, FR52, FR53.

## Epic 1: Quick Start (Auth & Basic Profile)

Focus on frictionless onboarding via Google OAuth2, automatic nickname generation from email, and basic profile settings in the personal cabinet.

### Story 1.1: Project Initialization & Authentication via Google OAuth2
As a new player, I want to log in to the application via Google, So that I don't waste time filling out registration forms.
**Acceptance Criteria:**
- **Given** the technical architecture specifies Spring Boot 4.0 and Vite 8
- **When** the project is initialized from the starter template
- **Then** the application is accessible via a local development server
- **And** the "Sign in with Google" button is functional (UX-DR7)
- **And** successful auth creates a player record and redirects to Home Hub

### Story 1.2: Automatic Profile Generation & First Entry
As a new player, I want my profile to be created automatically, So that I can start recording matches immediately.
**Acceptance Criteria:**
- **Given** first-time authentication
- **When** profile is created
- **Then** nickname is generated from email prefix
- **And** default placeholder avatar assigned

### Story 1.3: Profile Management in Personal Cabinet
As a player, I want to change my nickname and language in the cabinet, So that I can personalize my experience.
**Acceptance Criteria:**
- **Given** player is on Home Hub
- **When** they tap avatar in header (UX-DR5)
- **Then** cabinet page opens (UX-DR6/7)
- **And** nickname can be changed once per month (FR31)

### Story 1.4: Account Deletion with Anonymization
As a user, I want to be able to delete my account, So that my personal data is removed while preserving match history.
**Acceptance Criteria:**
- **Given** "Delete Account" clicked in cabinet
- **When** destructive action confirmed
- **Then** email/nickname removed
- **And** identity replaced with "retired player" placeholder (AD-04)

## Epic 2: Retrospective Match Entry & Rule Systems

Core functionality allowing players to quickly record 1v1 and 2v2 matches after they are played.

### Story 2.1: Match Type & Player Selection (Portrait)
As a player, I want to select format and participants, So that I can record match result.
**Acceptance Criteria:**
- **Given** "New Match" tapped on Home Hub
- **When** creation screen opens in portrait (UX-DR1)
- **Then** 2 or 4 slots available
- **And** UI follows "No-Line" rule (UX-DR3)

### Story 2.2: Rule System Selection & Inline Creation
As an organizer, I want to select rules or create custom ones, So that match data is accurate.
**Acceptance Criteria:**
- **Given** on match creation screen
- **When** rule system selected
- **Then** ITSF/DTFB presets available
- **And** Custom template creates immutable record (AD-01)

### Story 2.3: Score Entry & Automatic Completion
As a player, I want to quickly enter game scores, So that system determines outcome automatically.
**Acceptance Criteria:**
- **Given** players/rules selected
- **When** score entry opens (+/- steppers)
- **Then** game completes on limit (FR6)
- **And** match completes on win condition

### Story 2.4: Position Swapping Between Games
As a 2v2 participant, I want to indicate positions, So that stats remain accurate.
**Acceptance Criteria:**
- **When** new game starts in 2v2
- **Then** prompt to confirm/swap Attacker/Defender
- **And** data persisted for each game

### Story 2.5: Match Submission with Undo Window
As a player, I want to submit and have a short undo window, So that I can correct mistakes.
**Acceptance Criteria:**
- **When** "Submit" clicked
- **Then** 15-second Undo Toast appears (UX-DR4)
- **And** match sent to Epic 3 after expiry

## Epic 3: Data Verification & Trust

Implementation of the match verification pipeline.

### Story 3.1: Confirmation Requests & Push Notifications
**Acceptance Criteria:** Match submitted -> Undo window expired -> Push sent to opponents (FR55).

### Story 3.2: Single-tap Confirmation with Undo Window
**Acceptance Criteria:** Tap "Confirm" -> 15-second Undo Toast (UX-DR4) -> Committed after expiry.

### Story 3.3: Publication Rules & 24-hour Cooldown
**Acceptance Criteria:** First confirmation received -> 24h cooldown starts (FR15) -> Published if second confirms or time expires.

### Story 3.4: Match Rejection with Reason
**Acceptance Criteria:** Tap "Reject" -> Reason prompt -> Notification to creator (FR17).

### Story 3.5: Submission Rate Limiting (Anti-Spam)
**Acceptance Criteria:** Submissions > 10/hour -> Blocked with wait banner (FR58).

## Epic 4: Individual & Team Analytics

Comprehensive statistics suite.

### Story 4.1: Global Leaderboard with Filtering
**Acceptance Criteria:** Leaderboard loads -> Sortable rank/win-rate -> Filters (FR20) -> Pagination (FR27) -> If player is in top 10, show top 10; otherwise, show relative ranking (4 above, current player, 5 below).

### Story 4.2: Positional Statistics (Attack vs. Defense)
**Acceptance Criteria:** Positional tab opened -> Separate Attacker/Defender stats (FR21) -> Visual bars (FR22).

### Story 4.3: Team (Pair) Statistics
**Acceptance Criteria:** Teams list loads -> Pair-level win rate/count (FR23) -> Visual "Heatmap" of activity (days/hours).

### Story 4.4: Head-to-Head (H2H) Comparison
**Acceptance Criteria:** Opponent selected -> Cross-tabulated tables (FR24) -> Positional breakdown.

### Story 4.5: Unified Match History (My Matches)
**Acceptance Criteria:** My Matches opened (FR60) -> Confirmed/Pending tabs -> Detailed score cards.

### Story 4.6: Empty State & Demo Data
As a new user, I want to see sample statistics when I have no games, So that I can understand the value of the platform.
**Acceptance Criteria:**
- **Given** user has < 1 confirmed match
- **When** Analytics section is opened
- **Then** system displays generated demo data (FR57)
- **And** a "Demo Mode" toggle allows hiding/showing it
- **And** demo data is automatically disabled after 5 real matches

## Epic 5: Live Mode (Real-time Scoring)

Real-time match tracking with goal-by-goal recording, timeline, and optimized UI for table-side use.

### Story 5.1: Real-time Scoring Interface (Landscape)
As a player, I want to tap screen quadrants, So that goals are recorded instantly without manual input.
**Acceptance Criteria:**
- **Given** device in landscape mode
- **When** screen quadrant tapped
- **Then** goal awarded to specific player (FR4)
- **And** haptic feedback provided

### Story 5.2: Screen Wake Lock & Continuity
As a player, I want the screen to stay on, So that I don't have to touch it between goals.
**Acceptance Criteria:**
- **When** live match starts
- **Then** system prevents screen dimming (FR11)
- **And** wake lock released on match finish

### Story 5.3: Live Activity Timeline & Undo
As a player, I want to see the goal sequence and correct errors, So that the match protocol is accurate.
**Acceptance Criteria:**
- **When** goal recorded
- **Then** event appears in live timeline (FR10)
- **And** "Undo Last Goal" removes it from history (FR9)

### Story 5.4: Live Position Swapping
As a 2v2 participant, I want to swap positions during the match, So that player-level stats are correct.
**Acceptance Criteria:**
- **Given** live match active
- **When** "Swap Positions" tapped (during timeout/break)
- **Then** system updates current Attacker/Defender for subsequent goals (FR8)

### Story 5.5: Third-party Referee Mode
As a referee, I want a simplified scoring UI, So that I can track a match I am not playing in.
**Acceptance Criteria:**
- **Given** "Referee Mode" selected
- **When** scoring starts
- **Then** UI provides large Left/Right buttons for goals (FR5)
- **And** result attributed to players correctly

## Epic 6: Tournament Management

Tools for organizing tournaments, featuring bracket generation and seeding, integrated with Live Mode.

### Story 6.1: Tournament Creation & Configuration
**Acceptance Criteria:** Format (Cup/Champ) + Rules + Registration deadlines (FR41).

### Story 6.2: Team Registration & Confirmation
**Acceptance Criteria:** Register -> Partner confirms -> Team added to roster (FR42).

### Story 6.3: Automated Bracket Generation & Seeding
**Acceptance Criteria:** Registration closed -> Auto-schedule bracket (FR43) -> Seeding based on ratings.

### Story 6.4: Tournament Match Flow (Live Integration)
**Acceptance Criteria:** Tournament match started -> Uses Epic 5 Live Mode -> 48h confirmation deadline (FR18).

### Story 6.5: Tournament Standings & Archive
**Acceptance Criteria:** Live table -> Results history -> Winner celebration view (FR46).

## Epic 7: Engagement & Polish

Personalization and onboarding features.

### Story 7.1: Avatar Selection & Management
**Acceptance Criteria:** Cabinet -> Choose from 24 SVG presets (FR30) -> Updated globally.

### Story 7.2: Achievement System (Badges)
**Acceptance Criteria:** Condition met (e.g., win streak) -> Badge awarded (FR48) -> Visible in profile -> Prominent badges displayed next to name in leaderboard for 1 week.

### Story 7.3: Humorous Anti-achievements
**Acceptance Criteria:** Fail condition met -> Funny badge awarded (FR49) -> Displayed next to name in leaderboard for 1 week for fun social dynamics.
