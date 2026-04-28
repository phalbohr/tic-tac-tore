# Graph Report - tic-tac-tore  (2026-04-27)

## Corpus Check
- 113 files · ~235,951 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 471 nodes · 513 edges · 62 communities detected
- Extraction: 81% EXTRACTED · 19% INFERRED · 0% AMBIGUOUS · INFERRED: 97 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 96|Community 96]]
- [[_COMMUNITY_Community 97|Community 97]]
- [[_COMMUNITY_Community 98|Community 98]]
- [[_COMMUNITY_Community 99|Community 99]]
- [[_COMMUNITY_Community 100|Community 100]]
- [[_COMMUNITY_Community 101|Community 101]]
- [[_COMMUNITY_Community 102|Community 102]]
- [[_COMMUNITY_Community 103|Community 103]]
- [[_COMMUNITY_Community 104|Community 104]]
- [[_COMMUNITY_Community 105|Community 105]]
- [[_COMMUNITY_Community 106|Community 106]]
- [[_COMMUNITY_Community 107|Community 107]]
- [[_COMMUNITY_Community 108|Community 108]]
- [[_COMMUNITY_Community 109|Community 109]]
- [[_COMMUNITY_Community 110|Community 110]]

## God Nodes (most connected - your core abstractions)
1. `Match` - 11 edges
2. `JwtService` - 10 edges
3. `Live Match Screen UI Mockup` - 10 edges
4. `StatisticsService` - 9 edges
5. `StatisticsIntegrationTest` - 8 edges
6. `MatchService` - 8 edges
7. `MatchApprovalIntegrationTest` - 7 edges
8. `MatchRejectionIntegrationTest` - 7 edges
9. `MatchRepositoryTest` - 7 edges
10. `MatchServiceTest` - 7 edges

## Surprising Connections (you probably didn't know these)
- `g()` --calls--> `Match`  [INFERRED]
  frontend/coverage/prettify.js → src/main/java/com/tictactore/model/Match.java
- `Tic-Tac-Tore Product Requirements` --implements--> `Layered Architecture`  [INFERRED]
  _bmad-output/planning-artifacts/prd.md → docs/architecture-backend.md
- `Match Entity` --conceptually_related_to--> `Live Match Mode`  [INFERRED]
  docs/data-models-backend.md → _project-spec/DESIGN/project-design-description.md
- `Match Recording Flow` --implements--> `Live Match Interface`  [INFERRED]
  docs/integration-architecture.md → _project-spec/DESIGN/stitch_tic_tac_tore/live_match_annotated_updates_refined/code.html
- `The 500-Line Rule (IP-04)` --rationale_for--> `Backend (Spring Boot 3.4.0, Java 21)`  [EXTRACTED]
  _bmad-output/planning-artifacts/architecture.md → docs/source-tree-analysis.md

## Hyperedges (group relationships)
- **Record -> Confirm -> Discover Loop** — integrationarchitecture_match_recording_flow, apicontractsbackend_matchapi, apicontractsbackend_statisticsapi [INFERRED 0.95]
- **Verified Data Capture System** — prd_data_integrity, apicontractsbackend_matchapi, rationale_data_integrity_existential [INFERRED 0.90]
- **Mobile-First PWA Design Paradigm** — uxspec_tictactore, architecturefrontend_component_based_spa, projectdesigndescription_live_match [INFERRED 0.85]
- **Match Lifecycle Management** — architecture_ad02, integration_architecture_match_recording_flow, live_match_annotated_updates_refined_live_match [INFERRED 0.85]

## Communities

### Community 0 - "Community 0"
Cohesion: 0.09
Nodes (6): H2HProjection, LeaderboardProjection, MatchRepository, MatchRepositoryTest, PlayerStatsProjection, StatisticsService

### Community 1 - "Community 1"
Cohesion: 0.07
Nodes (8): DevTestController, MatchApprovalTest, MatchMapper, MatchOperation, MatchOperationTest, MatchServiceTest, UserRepository, UserRepositoryTest

### Community 2 - "Community 2"
Cohesion: 0.11
Nodes (7): CustomOAuth2SuccessHandler, CustomOAuth2SuccessHandlerTest, JwtAuthenticationFilter, JwtService, JwtServiceTest, OncePerRequestFilter, SimpleUrlAuthenticationSuccessHandler

### Community 3 - "Community 3"
Cohesion: 0.15
Nodes (3): DevDataController, Match, StatisticsServiceTest

### Community 4 - "Community 4"
Cohesion: 0.18
Nodes (3): MatchApi, MatchController, MatchService

### Community 5 - "Community 5"
Cohesion: 0.13
Nodes (15): CONFIGURE TEAMS Button, CUSTOM, DTFB, 04 / EXPERIENCE Section, 01 / FORMAT Section, ITSF, LIVE MATCH, 1 VS 1 (+7 more)

### Community 6 - "Community 6"
Cohesion: 0.27
Nodes (11): addSortIndicators(), enableUI(), getNthColumn(), getTable(), getTableBody(), getTableHeader(), loadColumns(), loadData() (+3 more)

### Community 7 - "Community 7"
Cohesion: 0.15
Nodes (13): Complete Match Button, Defense A, Defense B, MATCH ENTRY Header, Previous Match Score Indicator, Match Entry Screen (Landscape Mobile), Score Input (Team Alpha), Score Input (Team Bravo) (+5 more)

### Community 8 - "Community 8"
Cohesion: 0.35
Nodes (8): a(), B(), D(), g(), i(), k(), Q(), y()

### Community 9 - "Community 9"
Cohesion: 0.18
Nodes (11): Cancel Goal Button, Complete Match Button, Confirm Goal Button, Undo Action Button, Player J. DOE, Player M. KING, Player P. BOSS, Player S. SMITH (+3 more)

### Community 10 - "Community 10"
Cohesion: 0.22
Nodes (4): H2HParams, LeaderboardParams, StatisticsApi, StatisticsController

### Community 11 - "Community 11"
Cohesion: 0.28
Nodes (1): StatisticsIntegrationTest

### Community 12 - "Community 12"
Cohesion: 0.29
Nodes (1): MatchApprovalIntegrationTest

### Community 13 - "Community 13"
Cohesion: 0.29
Nodes (1): MatchRejectionIntegrationTest

### Community 14 - "Community 14"
Cohesion: 0.33
Nodes (1): MatchIntegrationTest

### Community 15 - "Community 15"
Cohesion: 0.29
Nodes (1): GlobalExceptionHandler

### Community 16 - "Community 16"
Cohesion: 0.29
Nodes (7): Confirm Changes Button, Dark Theme with Green Accents, Language Selection Toggle, Nickname Input Field, Personal Cabinet Screen, Profile Header, Push Notifications Toggle

### Community 17 - "Community 17"
Cohesion: 0.29
Nodes (7): AD-01: Immutable RuleConfiguration, AD-02: Isolated Verification Pipeline, AD-03: Stateless JWT with Redis Denylist, AD-04: GDPR Compliance via Pseudonymization, AD-05: 3-Tier Statistics Model, AD-06: PWA-First Infrastructure, Custom Layered Monolith (Spring Boot 4.0 + Vite 8)

### Community 18 - "Community 18"
Cohesion: 0.47
Nodes (1): MatchPendingIntegrationTest

### Community 19 - "Community 19"
Cohesion: 0.47
Nodes (1): MatchLifecycleIntegrationTest

### Community 20 - "Community 20"
Cohesion: 0.4
Nodes (1): MatchCreationIntegrationTest

### Community 21 - "Community 21"
Cohesion: 0.33
Nodes (1): MatchApi

### Community 22 - "Community 22"
Cohesion: 0.33
Nodes (6): Tournaments Screen, Tournament Card: Founder's Cup, Tournament Card: Rookie Rumble, Tournament Card: The Wooden Road Classic, Top App Bar, Tournament List

### Community 23 - "Community 23"
Cohesion: 0.7
Nodes (4): goToNext(), goToPrevious(), makeCurrent(), toggleClass()

### Community 24 - "Community 24"
Cohesion: 0.6
Nodes (3): apiFetch(), getLeaderboard(), getPersonalStats()

### Community 25 - "Community 25"
Cohesion: 0.4
Nodes (1): StatisticsApi

### Community 26 - "Community 26"
Cohesion: 0.4
Nodes (5): Backend Code Conventions, OpenAPI Rules, Code Reviewer Guide, Spring Boot 3 Technical Stack, Testing Conventions

### Community 27 - "Community 27"
Cohesion: 0.4
Nodes (5): Favorite Players Section, Quick Match Defaults Section, Rule Templates Section, Saved Teams Section, Teams & Rules Screen

### Community 28 - "Community 28"
Cohesion: 0.5
Nodes (1): SecurityConfig

### Community 29 - "Community 29"
Cohesion: 0.5
Nodes (2): ResourceNotFoundException, RuntimeException

### Community 30 - "Community 30"
Cohesion: 0.5
Nodes (4): Layered Architecture, Optimistic Locking, Three-Layer Transaction Architecture, Tic-Tac-Tore Product Requirements

### Community 31 - "Community 31"
Cohesion: 0.5
Nodes (4): Match API, Match Entity, Match Recording Flow, Live Match Mode

### Community 32 - "Community 32"
Cohesion: 0.5
Nodes (4): The 500-Line Rule (IP-04), OAuth2 Authentication Flow, Backend (Spring Boot 3.4.0, Java 21), Frontend (Vue 3, TypeScript)

### Community 34 - "Community 34"
Cohesion: 0.67
Nodes (1): OAuth2ConfigTest

### Community 35 - "Community 35"
Cohesion: 0.67
Nodes (1): SecurityConfigTest

### Community 36 - "Community 36"
Cohesion: 0.67
Nodes (1): OAuth2IntegrationTest

### Community 37 - "Community 37"
Cohesion: 0.67
Nodes (1): TicTacToreApplicationTests

### Community 38 - "Community 38"
Cohesion: 0.67
Nodes (1): TicTacToreApplication

### Community 39 - "Community 39"
Cohesion: 0.67
Nodes (1): ClockConfig

### Community 40 - "Community 40"
Cohesion: 0.67
Nodes (3): Inner Dark Blue V Path, Outer Green V Path, Vue.js Brand Logo

### Community 41 - "Community 41"
Cohesion: 0.67
Nodes (3): Available Pools List, Create a New Pool Form, I Want to Play Matchmaking Screen

### Community 54 - "Community 54"
Cohesion: 1.0
Nodes (1): User

### Community 55 - "Community 55"
Cohesion: 1.0
Nodes (1): Game

### Community 56 - "Community 56"
Cohesion: 1.0
Nodes (2): Data Integrity & Immutability, Rationale: Data Integrity is Existential

### Community 57 - "Community 57"
Cohesion: 1.0
Nodes (2): Rationale: Asymmetric Participation, UX Design Specification

### Community 58 - "Community 58"
Cohesion: 1.0
Nodes (2): Match Recording Flow, Live Match Interface

### Community 59 - "Community 59"
Cohesion: 1.0
Nodes (2): Tic-Tac-Tore Main Hub, Pitch Parquet Design System

### Community 96 - "Community 96"
Cohesion: 1.0
Nodes (1): Graphify Knowledge Graph

### Community 97 - "Community 97"
Cohesion: 1.0
Nodes (1): Privacy & DSGVO Compliance

### Community 98 - "Community 98"
Cohesion: 1.0
Nodes (1): Domain-Driven Entity Design

### Community 99 - "Community 99"
Cohesion: 1.0
Nodes (1): Component-Based SPA

### Community 100 - "Community 100"
Cohesion: 1.0
Nodes (1): Statistics API

### Community 101 - "Community 101"
Cohesion: 1.0
Nodes (1): Rationale: Speed over Precision

### Community 102 - "Community 102"
Cohesion: 1.0
Nodes (1): Design System Specification

### Community 103 - "Community 103"
Cohesion: 1.0
Nodes (1): Pitch Parquet Design

### Community 104 - "Community 104"
Cohesion: 1.0
Nodes (1): Vue 3 Technical Stack

### Community 105 - "Community 105"
Cohesion: 1.0
Nodes (1): ITSF Rules Summary (German)

### Community 106 - "Community 106"
Cohesion: 1.0
Nodes (1): ITSF Standard Matchplay Rules 2024

### Community 107 - "Community 107"
Cohesion: 1.0
Nodes (1): DTFB Bundesliga Regulations 2024

### Community 108 - "Community 108"
Cohesion: 1.0
Nodes (1): Tic-Tac-Tore Home Hub Screen

### Community 109 - "Community 109"
Cohesion: 1.0
Nodes (1): Architecture Decision Document

### Community 110 - "Community 110"
Cohesion: 1.0
Nodes (1): Tic-Tac-Tore Logo

## Knowledge Gaps
- **89 isolated node(s):** `User`, `Game`, `Graphify Knowledge Graph`, `Tic-Tac-Tore Product Requirements`, `Data Integrity & Immutability` (+84 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 11`** (9 nodes): `StatisticsIntegrationTest.java`, `StatisticsIntegrationTest`, `.createConfirmedMatch()`, `.createUser()`, `.getH2HStats_shouldFilterByPositions()`, `.getH2HStats_shouldReturnResults()`, `.getLeaderboard_shouldFilterByRole()`, `.getPersonalStats_shouldReturnResults()`, `.setUp()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 12`** (8 nodes): `MatchApprovalIntegrationTest`, `.approveMatch_failAsCreator()`, `.approveMatch_failAsTeammate()`, `.approveMatch_notFound()`, `.approveMatch_success()`, `.createUser()`, `.setUp()`, `MatchApprovalIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 13`** (8 nodes): `MatchRejectionIntegrationTest`, `.createUser()`, `.rejectMatch_failAsCreator()`, `.rejectMatch_failAsTeammate()`, `.rejectMatch_notFound()`, `.rejectMatch_success()`, `.setUp()`, `MatchRejectionIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 14`** (7 nodes): `MatchIntegrationTest`, `.createMatch_nonUniquePlayers()`, `.createMatch_success()`, `.createMatch_userNotFound()`, `.createUser()`, `.setUp()`, `MatchIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 15`** (7 nodes): `GlobalExceptionHandler`, `.handleGeneralException()`, `.handleIllegalArgument()`, `.handleIllegalState()`, `.handleResourceNotFound()`, `.handleValidationExceptions()`, `GlobalExceptionHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 18`** (6 nodes): `MatchPendingIntegrationTest`, `.createMatch()`, `.createUser()`, `.getPendingMatches_success()`, `.setUp()`, `MatchPendingIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 19`** (6 nodes): `MatchLifecycleIntegrationTest`, `.createUser()`, `.fullMatchLifecycle_success()`, `.setUp()`, `.user()`, `MatchLifecycleIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 20`** (6 nodes): `MatchCreationIntegrationTest`, `.createMatch_failDuplicatePlayers()`, `.createMatch_success()`, `.createUser()`, `.setUp()`, `MatchCreationIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 21`** (6 nodes): `MatchApi`, `.approveMatch()`, `.createMatch()`, `.getPendingMatches()`, `.rejectMatch()`, `MatchApi.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 25`** (5 nodes): `StatisticsApi.java`, `StatisticsApi`, `.getH2HStats()`, `.getLeaderboard()`, `.getPersonalStats()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 28`** (4 nodes): `SecurityConfig`, `.corsConfigurationSource()`, `.filterChain()`, `SecurityConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 29`** (4 nodes): `ResourceNotFoundException`, `.ResourceNotFoundException()`, `RuntimeException`, `ResourceNotFoundException.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 34`** (3 nodes): `OAuth2ConfigTest`, `.googleClientRegistration_shouldBeLoaded()`, `OAuth2ConfigTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 35`** (3 nodes): `SecurityConfigTest`, `.unauthenticatedAccess_shouldBeRedirectedToLogin()`, `SecurityConfigTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 36`** (3 nodes): `OAuth2IntegrationTest`, `.accessingProtectedResource_withoutAuth_shouldRedirect()`, `OAuth2IntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 37`** (3 nodes): `TicTacToreApplicationTests.java`, `TicTacToreApplicationTests`, `.contextLoads()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 38`** (3 nodes): `TicTacToreApplication.java`, `TicTacToreApplication`, `.main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 39`** (3 nodes): `ClockConfig`, `.clock()`, `ClockConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 54`** (2 nodes): `User.java`, `User`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 55`** (2 nodes): `Game`, `Game.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 56`** (2 nodes): `Data Integrity & Immutability`, `Rationale: Data Integrity is Existential`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 57`** (2 nodes): `Rationale: Asymmetric Participation`, `UX Design Specification`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 58`** (2 nodes): `Match Recording Flow`, `Live Match Interface`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 59`** (2 nodes): `Tic-Tac-Tore Main Hub`, `Pitch Parquet Design System`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 96`** (1 nodes): `Graphify Knowledge Graph`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 97`** (1 nodes): `Privacy & DSGVO Compliance`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 98`** (1 nodes): `Domain-Driven Entity Design`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 99`** (1 nodes): `Component-Based SPA`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 100`** (1 nodes): `Statistics API`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 101`** (1 nodes): `Rationale: Speed over Precision`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 102`** (1 nodes): `Design System Specification`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 103`** (1 nodes): `Pitch Parquet Design`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 104`** (1 nodes): `Vue 3 Technical Stack`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 105`** (1 nodes): `ITSF Rules Summary (German)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 106`** (1 nodes): `ITSF Standard Matchplay Rules 2024`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 107`** (1 nodes): `DTFB Bundesliga Regulations 2024`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 108`** (1 nodes): `Tic-Tac-Tore Home Hub Screen`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 109`** (1 nodes): `Architecture Decision Document`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 110`** (1 nodes): `Tic-Tac-Tore Logo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Match` connect `Community 3` to `Community 0`, `Community 1`, `Community 8`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Why does `StatisticsService` connect `Community 0` to `Community 3`?**
  _High betweenness centrality (0.019) - this node is a cross-community bridge._
- **Why does `g()` connect `Community 8` to `Community 3`?**
  _High betweenness centrality (0.015) - this node is a cross-community bridge._
- **What connects `User`, `Game`, `Graphify Knowledge Graph` to the rest of the system?**
  _89 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.11 - nodes in this community are weakly interconnected._