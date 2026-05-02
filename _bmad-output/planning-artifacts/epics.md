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
FR35: Create a "Want to Play" pool.
FR36: Join an existing open pool.
FR37: Push notifications for pool events.
FR38: Challenge a specific player or group.
FR39: Create named player groups ("teams").
FR40: Set a default team and default rule template.
FR41: Create tournaments with configurable parameters (cup/championship).
FR42: Register for an open tournament.
FR43: Automatic tournament bracket generation with strength-based seeding.
FR44: Play tournament matches at own pace.
FR45: Tournament matches use configured rule system.
FR46: Maintain tournament-specific standings and stats.
FR47: Equal match distribution for 2v2 random pairing.
FR48: System awards achievements (badges, milestones).
FR49: Humorous anti-achievements for fails.
FR50: View award wall for collected achievements.
FR51: View progress toward locked achievements.
FR52: Narrative match broadcasts.
FR53: Auto-generated statistical insights.
FR54: Installable Progressive Web App (PWA).
FR55: Push notifications for confirmations, pools, tournaments.
FR56: Onboarding tutorial for new users.
FR57: Demo/seed data for new users.
FR58: Automated context-aware rate limiting on match submissions.
FR59: Language translation architecture via translation files.
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
FR18: Epic 8 - Tournament confirmation deadline
FR19: Epic 4 - Sortable leaderboard
FR20: Epic 4 - Statistics filtering
FR21: Epic 4 - Positional breakdown
FR22: Epic 4 - Visual stat bars
FR23: Epic 4 - Team statistics
FR24: Epic 4 - Head-to-head stats
FR25: Epic 4 - Team/Favorites filtering
FR26: Epic 8 - Tournament stats
FR27: Epic 4 - Statistics pagination
FR28: Epic 4 - Games threshold filter
FR29: Epic 1 - Google OAuth2
FR30: Epic 7 - Avatar management
FR31: Epic 1 - Nickname management
FR32: Epic 1 - Language selection
FR33: Epic 1 - Account deletion & Anonymization
FR34: Epic 1 - Minimal data storage
FR35: Epic 6 - Create "Want to Play" pool
FR36: Epic 6 - Join existing pool
FR37: Epic 6 - Pool notifications
FR38: Epic 6 - Challenge player/group
FR39: Epic 6 - Named player groups
FR40: Epic 6 - Default team and rules
FR41: Epic 8 - Tournament creation
FR42: Epic 8 - Team registration & confirmation
FR43: Epic 8 - Bracket generation & Seeding
FR44: Epic 8 - Tournament match flow
FR45: Epic 8 - Tournament match flow
FR46: Epic 8 - Tournament standings & archive
FR47: Epic 8 - Tournament match flow
FR48: Epic 7 - Achievement system
FR49: Epic 7 - Anti-achievements
FR50: Epic 7 - Award wall
FR51: Epic 7 - Award wall
FR52: Epic 7 - Narrative broadcasts
FR53: Epic 7 - Auto-generated statistical insights
FR54: Epic 2 - PWA Installability
FR55: Epic 3 - Push notifications (Confirmations)
FR56: Epic 1 - Onboarding tutorial
FR57: Epic 4 - Demo/seed data
FR58: Epic 3 - Submission rate limiting
FR59: Epic 1 - Localization architecture
FR60: Epic 4 - Match history view

## Epic List

### Epic 1: Quick Start (Auth & Basic Profile)
Focus on frictionless onboarding via Google OAuth2, automatic nickname generation from email, and basic profile settings.
**FRs covered:** FR29, FR30, FR31, FR32, FR33, FR34, FR56, FR59.

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

### Epic 6: Social & Matchmaking
Features to help players find opponents and organize casual matches.
**FRs covered:** FR35, FR36, FR37, FR38, FR39, FR40.

### Epic 7: Engagement & Polish
Personalization via avatars, achievement systems (badges), and UI refinements.
**FRs covered:** FR48, FR49, FR50, FR51, FR52, FR53.

### Epic 8: Tournament Management
Tools for organizing tournaments, featuring bracket generation and seeding, integrated with Live Mode.
**FRs covered:** FR18, FR26, FR41, FR42, FR43, FR44, FR45, FR46, FR47.

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

### Story 1.1a: Stateless JWT with Redis Denylist & Bloom Filters
As a security-conscious system, I want to immediately revoke compromised or deleted account tokens, so that user sessions are truly terminated.
**Acceptance Criteria:**
- **Given** a valid JWT token
- **When** the account is deleted or token is revoked
- **Then** the token is added to a Redis-based denylist
- **And** a Bloom filter is used for fast-path revocation checks before querying Redis
- **And** the `JwtAuthenticationFilter` rejects any token found in the denylist (AD-03)

### Story 1.1b: E2E Test for OAuth2 Login Flow
As a Quality Engineer, I want an E2E Playwright test for the Google OAuth2 login flow, so that regressions in routing, asset loading, or proxying are caught automatically before merging.
**Acceptance Criteria:**
- **Given** the local development environment is running
- **When** Playwright executes the login test suite
- **Then** it navigates to the home page and verifies that the `main.css` styles are loaded correctly
- **And** it clicks the "Sign in with Google" button and verifies that the Vite proxy successfully redirects to the backend `/oauth2/authorization/google` endpoint
- **And** the CI pipeline executes this test successfully during the `frontend-e2e` stage

### Story 1.2: Localization and Translation Architecture
As a user, I want the app to be localized, so that I can use it in my language.
**Acceptance Criteria:**
- **Given** the application is deployed
- **When** the user switches language preferences (e.g., English to German)
- **Then** the UI updates immediately using externalized translation keys without requiring a page reload or code change (FR59)

### Story 1.3: Automatic Profile Generation & First Entry
As a new player, I want my profile to be created automatically, So that I can start recording matches immediately.
**Acceptance Criteria:**
- **Given** first-time authentication
- **When** profile is created
- **Then** nickname is generated from email prefix
- **And** default placeholder avatar assigned

### Story 1.4: Profile Management in Personal Cabinet
As a player, I want to change my nickname and language in the cabinet, So that I can personalize my experience.
**Acceptance Criteria:**
- **Given** player is on Home Hub
- **When** they tap avatar in header (UX-DR5)
- **Then** cabinet page opens (UX-DR6/7)
- **And** nickname can be changed once per month (FR31)

### Story 1.5: Account Deletion with Anonymization
As a user, I want to be able to delete my account, So that my personal data is removed while preserving match history.
**Acceptance Criteria:**
- **Given** "Delete Account" clicked in cabinet
- **When** destructive action confirmed
- **Then** email/nickname removed
- **And** identity replaced with "retired player" placeholder (AD-04)

### Story 1.6: Avatar Selection & Management
As a player, I want to select an avatar, so that I can personalize my profile.
**Acceptance Criteria:**
- **Given** the player is logged into their personal cabinet
- **When** they click to edit their avatar and upload/select an image
- **Then** the system saves the new avatar and updates their identity globally across all app views (FR30)

### Story 1.7: Onboarding Tutorial
As a new user, I want an onboarding tutorial, so that I can learn how to use the app.
**Acceptance Criteria:**
- **Given** a newly registered user logs in for the first time
- **When** the Home Hub renders
- **Then** the system triggers a dismissible onboarding tutorial overlay explaining core features (FR56)

## Epic 2: Retrospective Match Entry & Rule Systems

Core functionality allowing players to quickly record 1v1 and 2v2 matches after they are played.

### Story 2.1: Rule System Selection & Inline Creation
As an organizer, I want to select rules or create custom ones, So that match data is accurate.
**Acceptance Criteria:**
- **Given** on match creation screen
- **When** rule system selected
- **Then** ITSF/DTFB presets available
- **And** Custom template creates immutable record (AD-01)

### Story 2.2: Match Type & Player Selection (Portrait)
As a player, I want to select format and participants, So that I can record match result.
**Acceptance Criteria:**
- **Given** "New Match" tapped on Home Hub
- **When** creation screen opens in portrait (UX-DR1)
- **Then** 2 or 4 slots available
- **And** UI follows "No-Line" rule (UX-DR3)

### Story 2.3: Score Entry & Automatic Completion
As a player, I want to quickly enter game scores, So that system determines outcome automatically.
**Acceptance Criteria:**
- **Given** players/rules selected
- **When** score entry opens (+/- steppers)
- **Then** game completes on limit (FR6)
- **And** match completes on win condition

### Story 2.4: Match Submission with Undo Window
As a player, I want to submit and have a short undo window, So that I can correct mistakes.
**Acceptance Criteria:**
- **Given** match scores are complete
- **When** "Submit" clicked
- **Then** 15-second Undo Toast appears (UX-DR4)
- **And** match sent to Epic 3 after expiry
- **Note:** This story is sequenced before position swapping to accelerate end-to-end testing of the verification pipeline (Epic 3), but the MVP release is blocked until position swapping (Story 2.5) is also integrated.

### Story 2.5: Position Swapping Between Games
As a 2v2 participant, I want to indicate positions, So that stats remain accurate.
**Acceptance Criteria:**
- **Given** a 2v2 match is being recorded
- **When** new game starts
- **Then** prompt to confirm/swap Attacker/Defender
- **And** data persisted for each game
- **And** the match submission payload (Story 2.4) correctly includes positional data

## Epic 3: Data Verification & Trust

Implementation of the match verification pipeline.

### Story 3.1: Confirmation Requests & Push Notifications
As a player who just submitted a match, I want the system to notify opponents, so that they can verify the results.
**Acceptance Criteria:**
- **Given** a match has been successfully submitted by the creator
- **When** the creator's 15-second local undo window expires without cancellation
- **Then** the system creates a pending confirmation record and sends a push notification to all required opponents (FR12, FR55)

### Story 3.2: Single-tap Confirmation with Undo Window
As an opponent, I want to quickly confirm a match with an undo option, so that I can easily verify results and correct mis-taps.
**Acceptance Criteria:**
- **Given** an opponent views a pending match confirmation request
- **When** they tap the "Confirm" button
- **Then** the UI immediately displays a 15-second "Undo" toast notification (UX-DR4, FR13)
- **And** the match is permanently committed and marked immutable only if the undo window expires without the user tapping undo (FR16)

### Story 3.3: Match Rejection with Reason
As an opponent, I want to reject an incorrect match entry, so that stats remain accurate.
**Acceptance Criteria:**
- **Given** an opponent is viewing a pending confirmation request
- **When** they tap "Reject" and provide a required reason string
- **Then** the match state changes to rejected and is returned to the creator's queue with a notification explaining the rejection (FR17)

### Story 3.4: Context-Aware Verification Rules
As a system, I want to apply specific verification rules based on match context, so that security fits the scenario.
**Acceptance Criteria:**
- **Given** a submitted match pending confirmation
- **When** the confirmation logic is evaluated
- **Then** it requires 1 opponent for participant-entered 1v1, both for referee-entered 1v1, 1 opponent for standard 2v2 (triggers cooldown), both opponents for random 2v2, and 1 per team for referee 2v2 (FR14)

### Story 3.5: Publication Rules & 24-hour Cooldown
As a system, I want to apply a cooldown period for publications, so that results aren't immediately final unless fully confirmed.
**Acceptance Criteria:**
- **Given** a standard 2v2 match in pending state
- **When** the first opponent successfully confirms the match
- **Then** the system initiates a 24-hour publication cooldown (FR15)
- **And** the match is published to statistics immediately only when the second opponent confirms or the 24-hour timer expires

### Story 3.6: Submission Rate Limiting (Anti-Spam)
As an administrator, I want to prevent spam submissions, so that the platform remains trustworthy.
**Acceptance Criteria:**
- **Given** an authenticated user submitting matches
- **When** they submit more than 10 standalone matches in one hour
- **Then** the backend rejects the submission with an HTTP 429 status and displays a rate-limit error banner on the client (FR58)

## Epic 4: Individual & Team Analytics

Comprehensive statistics suite.

### Story 4.1: Empty State & Demo Data
As a new user, I want to see sample statistics when I have no games, so that I can understand the value of the platform.
**Acceptance Criteria:**
- **Given** user has < 1 confirmed match
- **When** Analytics section is opened
- **Then** system displays generated demo data (FR57)
- **And** a "Demo Mode" toggle allows hiding/showing it
- **And** demo data is automatically disabled after 5 real matches
### Story 4.2: Global Leaderboard with Filtering
As a player, I want to view a leaderboard, so that I can compare my ranking with others.
**Acceptance Criteria:**
- **Given** the player navigates to the leaderboard
- **When** the view loads
- **Then** it displays a sortable list of players by rank and win-rate
- **And** provides filters for rule system, match type, and time period (FR20)
- **And** paginates the results with a configurable page size (FR27)
- **And** only includes players meeting the minimum games played threshold (FR28)

### Story 4.3: Positional Statistics (Attack vs. Defense)
As a player, I want to see my stats by position, so that I understand my strengths.
**Acceptance Criteria:**
- **Given** the player views individual statistics
- **When** they select the positional breakdown tab
- **Then** the system displays separate statistics for Attacker and Defender roles (FR21)
- **And** shows visual stat bars for proportional breakdowns (FR22)

### Story 4.4: Team (Pair) Statistics
As a player, I want to see how I perform with different partners, so that I can find the best synergy.
**Acceptance Criteria:**
- **Given** the player navigates to team statistics
- **When** the view loads
- **Then** it displays pair-level performance for teammate combinations (FR23)
- **And** allows filtering by a specific player

### Story 4.5: Head-to-Head (H2H) Comparison
As a player, I want to compare my stats against a specific opponent, so that I know our historical matchup.
**Acceptance Criteria:**
- **Given** the player views head-to-head statistics
- **When** they select an opponent
- **Then** the system displays three cross-tabulated tables for matches, games, and goals (FR24)
- **And** includes detailed positional breakdowns

### Story 4.6: Unified Match History (My Matches)
As a player, I want to view my match history, so that I can track my recent performance.
**Acceptance Criteria:**
- **Given** the player navigates to their match history
- **When** the view loads
- **Then** it displays tabs for Confirmed and Pending matches (FR60)
- **And** provides options to filter by all players


## Epic 5: Live Mode (Real-time Scoring)

Real-time match tracking with goal-by-goal recording, timeline, and optimized UI for table-side use.

### Story 5.1: Real-time Scoring Interface (Landscape)
As a player, I want to tap screen quadrants, So that goals are recorded instantly without manual input.
**Acceptance Criteria:**
- **Given** device in landscape mode
- **When** screen quadrant tapped
- **Then** goal awarded to specific player (FR4)
- **And** haptic feedback provided

### Story 5.2: Live Activity Timeline & Undo
As a player, I want to see the goal sequence and correct errors, So that the match protocol is accurate.
**Acceptance Criteria:**
- **Given** a goal is recorded
- **When** viewing the interface
- **Then** event appears in live timeline (FR10)
- **And** "Undo Last Goal" removes it from history (FR9)

### Story 5.3: Live Position Swapping
As a 2v2 participant, I want to swap positions during the match, So that player-level stats are correct.
**Acceptance Criteria:**
- **Given** live match active
- **When** "Swap Positions" tapped (during timeout/break)
- **Then** system updates current Attacker/Defender for subsequent goals (FR8)

### Story 5.4: Third-party Referee Mode
As a referee, I want a simplified scoring UI, So that I can track a match I am not playing in.
**Acceptance Criteria:**
- **Given** "Referee Mode" selected
- **When** scoring starts
- **Then** UI provides large Left/Right buttons for goals (FR5)
- **And** result attributed to players correctly

### Story 5.5: Screen Wake Lock & Continuity
As a player, I want the screen to stay on, So that I don't have to touch it between goals.
**Acceptance Criteria:**
- **Given** live match starts
- **When** the match is active
- **Then** system prevents screen dimming (FR11)
- **And** wake lock released on match finish

## Epic 6: Social & Matchmaking

Features to help players find opponents and organize casual matches.

### Story 6.1: Named Player Groups ("Teams")
As a player, I want to create named groups of people I frequently play with, so that I can filter stats and matches easily.
**Acceptance Criteria:**
- **Given** a player navigates to their teams management page
- **When** they create a new group and add selected players
- **Then** the system persists this named group
- **And** the group becomes available as a filter in the statistics views and match creation player selectors (FR39)
- **And** the group becomes available as a filter in the Match History view (from Epic 4)

### Story 6.2: Default Team and Rule Template
As a player, I want to set default teams and rules, so that I don't have to select them every time.
**Acceptance Criteria:**
- **Given** a player modifies their user preferences
- **When** they set a default group and a default rule template
- **Then** future match creation screens automatically pre-populate the selected rule template and filter the player list by the default group (FR40)

### Story 6.3: Create "Want to Play" Pool
As a player, I want to create a pool to find opponents, so that I can organize a match.
**Acceptance Criteria:**
- **Given** a player is on the Home Hub
- **When** they tap to create a "Want to Play" pool and submit the form
- **Then** the system records a new pool specifying the match type (1v1/2v2) and desired start conditions (FR35)

### Story 6.4: Join Existing Pool
As a player, I want to join an open pool, so that I can quickly get into a game.
**Acceptance Criteria:**
- **Given** an active "Want to Play" pool exists with open slots
- **When** another player views the pool list and clicks "Join"
- **Then** their user ID is added to the pool's participant list (FR36)
- **And** the pool status updates if the required number of players is reached

### Story 6.5: Pool Notifications
As a player, I want to be notified when pools matching my criteria are available, so that I don't miss out on games.
**Acceptance Criteria:**
- **Given** a player has push notifications enabled
- **When** a new pool is created that matches their saved preferences, or a pool they are in becomes full
- **Then** the system immediately dispatches a push notification to their device (FR37)

### Story 6.6: Challenge Player or Group
As a player, I want to challenge specific people, so that we can settle a rivalry.
**Acceptance Criteria:**
- **Given** a player is browsing the leaderboard or player directory
- **When** they tap the "Challenge" button next to a player or team
- **Then** the system creates a direct match invitation and notifies the target player(s) (FR38)

## Epic 7: Engagement & Polish

Personalization and onboarding features.

### Story 7.1: Achievement System (Badges)
As a player, I want to earn achievements, so that I feel rewarded for my progress.
**Acceptance Criteria:**
- **Given** a player performs actions in the app
- **When** they meet specific milestones or statistical thresholds
- **Then** the system awards them an achievement badge (FR48)
- **And** it is visible in their profile

### Story 7.2: Humorous Anti-achievements
As a player, I want to receive funny anti-achievements, so that the experience is lighthearted.
**Acceptance Criteria:**
- **Given** a player experiences a memorable fail
- **When** the specific fail condition is met
- **Then** the system awards a lighthearted anti-achievement (FR49)

### Story 7.3: Award Wall and Progress Tracking
As a player, I want to view all my achievements, so that I can see what I have collected and what is pending.
**Acceptance Criteria:**
- **Given** the player views their profile
- **When** they navigate to the award wall
- **Then** they can see all collected achievements (FR50)
- **And** view progress toward locked achievements (FR51)

### Story 7.4: Narrative Match Broadcasts
As a user, I want to see narrative broadcasts of matches, so that I can experience the match highlights.
**Acceptance Criteria:**
- **Given** a live match has been recorded
- **When** the system processes the match protocol
- **Then** it generates a narrative match broadcast narrating real player actions (FR52)

### Story 7.5: Auto-generated Statistical Insights
As a player, I want to receive insights, so that I can understand my improvement over time.
**Acceptance Criteria:**
- **Given** a player has sufficient match data
- **When** the system analyzes their performance
- **Then** it provides auto-generated statistical insights and progress tracking (FR53)

## Epic 8: Tournament Management

Tools for organizing tournaments, featuring bracket generation and seeding, integrated with Live Mode.

### Story 8.1: Tournament Creation & Configuration
As an organizer, I want to configure tournament parameters, so that I can set up a structured competition.
**Acceptance Criteria:**
- **Given** a user is logged in and navigates to the Tournaments section
- **When** they submit the "Create Tournament" form with valid parameters
- **Then** a new tournament is persisted with the configured format, mode, rule system, participant limits, and registration deadlines (FR41)

### Story 8.2: Team Registration & Confirmation
As a player, I want to register for a tournament with a partner, so that we can compete.
**Acceptance Criteria:**
- **Given** an open tournament in the registration phase
- **When** a player submits a registration requesting a specific partner
- **Then** the partner receives an immediate push notification to accept the invite
- **And** the team's registration is only considered complete once the partner accepts (FR42)

### Story 8.3: Automated Bracket Generation & Seeding
As a system, I want to auto-generate the tournament bracket, so that matches are organized fairly.
**Acceptance Criteria:**
- **Given** a tournament's registration deadline has passed
- **When** the system initiates the tournament start routine
- **Then** it algorithmically evaluates participant strength metrics and seeds the bracket (FR43)
- **And** generates all required initial matches based on the selected tournament format

### Story 8.4: Equal Match Distribution (2v2 Random Pairing)
As a system, I want to distribute matches equally in random pairing mode, so that the tournament is fair.
**Acceptance Criteria:**
- **Given** a 2v2 random pairing tournament generates its match schedule
- **When** the pairings are calculated
- **Then** the algorithm guarantees each participant is assigned an equal number of matches as per the format constraints (FR47)

### Story 8.5: Asynchronous Tournament Match Execution
As a participant, I want to play tournament matches flexibly, so that the event proceeds smoothly.
**Acceptance Criteria:**
- **Given** a tournament is active with generated matches available
- **When** participants are physically ready
- **Then** they can start and record any of their assigned pending matches without adhering to a global schedule (FR44)

### Story 8.6: Tournament Rule System Enforcement
As a system, I want to enforce the tournament's rules, so that matches are consistent.
**Acceptance Criteria:**
- **Given** a player initiates match entry for a tournament match
- **When** the entry screen loads
- **Then** the rule system selection is locked to the tournament's configured rule set and cannot be overridden (FR45)

### Story 8.7: Tournament Standings & Archive
As a player, I want to see the tournament results, so that I can track progress and historical winners.
**Acceptance Criteria:**
- **Given** an active or completed tournament
- **When** a user views the tournament details page
- **Then** the system displays live standings updated after every confirmed match (FR26)
- **And** completed tournaments are permanently retained in an accessible archive (FR46)

### Story 8.8: Tournament Confirmation Deadline
As a system, I want to enforce confirmation deadlines, so that the tournament isn't blocked.
**Acceptance Criteria:**
- **Given** a tournament match is submitted for confirmation
- **When** the 48-hour tournament confirmation window expires without opponent action
- **Then** the system automatically applies a technical defeat to the unresponsive party or auto-confirms based on the rules (FR18)
