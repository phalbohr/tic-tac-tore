# Initial Concept

**Foosball Statistics Platform**

A web application where table football (kicker/foosball) players can record match results for both singles (1v1) and doubles (2v2) games. Players compete in a best-of-3 format, with specific position rotation rules for doubles matches. The platform will track comprehensive statistics including position-based performance, head-to-head records, and leaderboards.

**Match Format**
* Singles: 1v1 matches
* Doubles: 2v2 with defined positions (Attacker and Defender)
* Scoring: Best of 3 games wins the match
* Position Rotation (2v2 only):
    * Game 1: Players choose initial positions
    * Game 2: Teammates must swap positions (mandatory)
    * Game 3: Free position selection

# Product Definition - Foosball Statistics Platform

## Vision
To create a vibrant, engaging foosball culture within the office by providing a platform that tracks performance, settles friendly rivalries, and celebrates the social spirit of the game.

## Target Audience
- **Office Colleagues:** Designed specifically for coworkers playing during breaks, catering to both casual participants and those aiming for the top of the leaderboard.

## Strategic Goals
- **Objective Rivalry Resolution:** Use hard data to settle office debates about who is the "true" champion.
- **Cultural Catalyst:** Foster office community and social interaction through the shared language of friendly competition.
- **Personal Mastery:** Provide players with clear insights into their own skill progression and performance patterns over time.

## Core Features
- **Match Confirmation Workflow:** A critical system for data integrity where participants verify match results, preventing "stat padding" and building platform trust.
    - **Multi-Stage Lifecycle:** Matches progress through `PENDING_APPROVAL` (submitted), `CONFIRMED` (verified by opponent), or return to `DRAFT` if rejected for correction.
    - **Opponent Sovereignty:** Only players from the opposing team have the authority to confirm or dispute a match record.
- **Comprehensive Positional Statistics:** Deep-dive analytics for 1v1 and 2v2 formats, focusing on specific performance metrics for Attacker and Defender roles.
- **Public Office Leaderboards:** High-visibility rankings (Overall, Attack, and Defense) to drive daily engagement and healthy office rivalry.
- **Head-to-Head Analytics:** Detailed breakdown of performance against specific opponents and team compositions.
- **Secure Authentication:** User sign-in via Google OAuth2 to ensure data privacy and personalized player profiles.

## Experience & Tone
- **Socially Competitive:** A balance of friendly, approachable social play with the excitement of semi-competitive rankings. The platform is designed to be transparent, public, and encouraging for the entire office.