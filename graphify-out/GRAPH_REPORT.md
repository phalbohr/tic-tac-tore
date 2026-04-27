# Graph Report - .  (2026-04-27)

## Corpus Check
- 185 files · ~235,951 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 592 nodes · 620 edges · 61 communities detected
- Extraction: 84% EXTRACTED · 16% INFERRED · 0% AMBIGUOUS · INFERRED: 99 edges (avg confidence: 0.81)
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
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 70|Community 70]]
- [[_COMMUNITY_Community 71|Community 71]]
- [[_COMMUNITY_Community 72|Community 72]]
- [[_COMMUNITY_Community 73|Community 73]]
- [[_COMMUNITY_Community 146|Community 146]]
- [[_COMMUNITY_Community 147|Community 147]]
- [[_COMMUNITY_Community 148|Community 148]]
- [[_COMMUNITY_Community 149|Community 149]]
- [[_COMMUNITY_Community 150|Community 150]]
- [[_COMMUNITY_Community 151|Community 151]]
- [[_COMMUNITY_Community 152|Community 152]]
- [[_COMMUNITY_Community 153|Community 153]]
- [[_COMMUNITY_Community 154|Community 154]]
- [[_COMMUNITY_Community 155|Community 155]]
- [[_COMMUNITY_Community 156|Community 156]]

## God Nodes (most connected - your core abstractions)
1. `Match` - 12 edges
2. `JwtService` - 11 edges
3. `StatisticsService` - 10 edges
4. `Live Match Screen UI Mockup` - 10 edges
5. `StatisticsIntegrationTest` - 9 edges
6. `MatchService` - 9 edges
7. `MatchApprovalIntegrationTest` - 8 edges
8. `MatchRejectionIntegrationTest` - 8 edges
9. `MatchRepositoryTest` - 8 edges
10. `MatchServiceTest` - 8 edges

## Surprising Connections (you probably didn't know these)
- `Tic-Tac-Tore Product Requirements` --implements--> `Layered Architecture`  [INFERRED]
  _bmad-output/planning-artifacts/prd.md → docs/architecture-backend.md
- `Match Entity` --conceptually_related_to--> `Live Match Mode`  [INFERRED]
  docs/data-models-backend.md → _project-spec/DESIGN/project-design-description.md
- `Match Recording Flow` --implements--> `Live Match Interface`  [INFERRED]
  docs/integration-architecture.md → _project-spec/DESIGN/stitch_tic_tac_tore/live_match_annotated_updates_refined/code.html
- `g()` --calls--> `Match`  [INFERRED]
  /Users/ppolukhin/Projects/tic-tac-tore/frontend/coverage/prettify.js → /Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java
- `Home Hub UI` --implementation_framework_for--> `Vue 3 Technical Stack`  [INFERRED]
  _project-spec/DESIGN/stitch_tic_tac_tore/home_hub_font_updated/code.html → _project-spec/context7/vue3-composition-api.md

## Hyperedges (group relationships)
- **Record -> Confirm -> Discover Loop** — integrationarchitecture_match_recording_flow, apicontractsbackend_matchapi, apicontractsbackend_statisticsapi [INFERRED 0.95]
- **Verified Data Capture System** — prd_data_integrity, apicontractsbackend_matchapi, rationale_data_integrity_existential [INFERRED 0.90]
- **Mobile-First PWA Design Paradigm** — uxspec_tictactore, architecturefrontend_component_based_spa, projectdesigndescription_live_match [INFERRED 0.85]
- **Match Lifecycle Management** — architecture_ad02, integration_architecture_match_recording_flow, live_match_annotated_updates_refined_live_match [INFERRED 0.85]

## Communities

### Community 0 - "Community 0"
Cohesion: 0.07
Nodes (6): H2HProjection, LeaderboardProjection, MatchRepository, MatchRepositoryTest, PlayerStatsProjection, StatisticsService

### Community 1 - "Community 1"
Cohesion: 0.06
Nodes (7): MatchApprovalTest, MatchMapper, MatchOperation, MatchOperationTest, MatchServiceTest, UserRepository, UserRepositoryTest

### Community 2 - "Community 2"
Cohesion: 0.09
Nodes (6): CustomOAuth2SuccessHandlerTest, DevTestController, JwtAuthenticationFilter, JwtService, JwtServiceTest, OncePerRequestFilter

### Community 3 - "Community 3"
Cohesion: 0.12
Nodes (3): DevDataController, Match, StatisticsServiceTest

### Community 4 - "Community 4"
Cohesion: 0.16
Nodes (3): MatchApi, MatchController, MatchService

### Community 5 - "Community 5"
Cohesion: 0.36
Nodes (13): addSearchBox(), addSortIndicators(), enableUI(), getNthColumn(), getTable(), getTableBody(), getTableHeader(), loadColumns() (+5 more)

### Community 6 - "Community 6"
Cohesion: 0.13
Nodes (15): CONFIGURE TEAMS Button, CUSTOM, DTFB, 04 / EXPERIENCE Section, 01 / FORMAT Section, ITSF, LIVE MATCH, 1 VS 1 (+7 more)

### Community 7 - "Community 7"
Cohesion: 0.18
Nodes (4): H2HParams, LeaderboardParams, StatisticsApi, StatisticsController

### Community 8 - "Community 8"
Cohesion: 0.15
Nodes (13): Complete Match Button, Defense A, Defense B, MATCH ENTRY Header, Previous Match Score Indicator, Match Entry Screen (Landscape Mobile), Score Input (Team Alpha), Score Input (Team Bravo) (+5 more)

### Community 9 - "Community 9"
Cohesion: 0.44
Nodes (10): a(), B(), c(), D(), g(), i(), k(), o() (+2 more)

### Community 10 - "Community 10"
Cohesion: 0.17
Nodes (12): Design System Specification, DTFB Bundesliga Regulations 2024, ITSF Standard Matchplay Rules 2024, Vue 3 Technical Stack, Home Hub UI, Live Match Scoring UI, Matchmaking UI (I Want to Play), New Match Configuration UI (+4 more)

### Community 11 - "Community 11"
Cohesion: 0.18
Nodes (11): Cancel Goal Button, Complete Match Button, Confirm Goal Button, Undo Action Button, Player J. DOE, Player M. KING, Player P. BOSS, Player S. SMITH (+3 more)

### Community 12 - "Community 12"
Cohesion: 0.24
Nodes (1): StatisticsIntegrationTest

### Community 13 - "Community 13"
Cohesion: 0.25
Nodes (1): MatchApprovalIntegrationTest

### Community 14 - "Community 14"
Cohesion: 0.25
Nodes (1): MatchRejectionIntegrationTest

### Community 15 - "Community 15"
Cohesion: 0.29
Nodes (1): MatchIntegrationTest

### Community 16 - "Community 16"
Cohesion: 0.25
Nodes (1): GlobalExceptionHandler

### Community 17 - "Community 17"
Cohesion: 0.38
Nodes (1): MatchPendingIntegrationTest

### Community 18 - "Community 18"
Cohesion: 0.38
Nodes (1): MatchLifecycleIntegrationTest

### Community 19 - "Community 19"
Cohesion: 0.33
Nodes (1): MatchCreationIntegrationTest

### Community 20 - "Community 20"
Cohesion: 0.29
Nodes (1): MatchApi

### Community 21 - "Community 21"
Cohesion: 0.29
Nodes (7): Confirm Changes Button, Dark Theme with Green Accents, Language Selection Toggle, Nickname Input Field, Personal Cabinet Screen, Profile Header, Push Notifications Toggle

### Community 22 - "Community 22"
Cohesion: 0.29
Nodes (7): AD-01: Immutable RuleConfiguration, AD-02: Isolated Verification Pipeline, AD-03: Stateless JWT with Redis Denylist, AD-04: GDPR Compliance via Pseudonymization, AD-05: 3-Tier Statistics Model, AD-06: PWA-First Infrastructure, Custom Layered Monolith (Spring Boot 4.0 + Vite 8)

### Community 23 - "Community 23"
Cohesion: 0.73
Nodes (4): goToNext(), goToPrevious(), makeCurrent(), toggleClass()

### Community 24 - "Community 24"
Cohesion: 0.67
Nodes (4): apiFetch(), getH2HStats(), getLeaderboard(), getPersonalStats()

### Community 25 - "Community 25"
Cohesion: 0.33
Nodes (1): StatisticsApi

### Community 26 - "Community 26"
Cohesion: 0.33
Nodes (6): Tournaments Screen, Tournament Card: Founder's Cup, Tournament Card: Rookie Rumble, Tournament Card: The Wooden Road Classic, Top App Bar, Tournament List

### Community 27 - "Community 27"
Cohesion: 0.6
Nodes (2): CustomOAuth2SuccessHandler, SimpleUrlAuthenticationSuccessHandler

### Community 28 - "Community 28"
Cohesion: 0.4
Nodes (1): SecurityConfig

### Community 29 - "Community 29"
Cohesion: 0.4
Nodes (2): ResourceNotFoundException, RuntimeException

### Community 30 - "Community 30"
Cohesion: 0.4
Nodes (5): Backend Code Conventions, OpenAPI Rules, Code Reviewer Guide, Spring Boot 3 Technical Stack, Testing Conventions

### Community 31 - "Community 31"
Cohesion: 0.4
Nodes (5): Favorite Players Section, Quick Match Defaults Section, Rule Templates Section, Saved Teams Section, Teams & Rules Screen

### Community 32 - "Community 32"
Cohesion: 0.5
Nodes (1): OAuth2ConfigTest

### Community 33 - "Community 33"
Cohesion: 0.5
Nodes (1): SecurityConfigTest

### Community 34 - "Community 34"
Cohesion: 0.5
Nodes (1): OAuth2IntegrationTest

### Community 35 - "Community 35"
Cohesion: 0.5
Nodes (1): TicTacToreApplicationTests

### Community 36 - "Community 36"
Cohesion: 0.5
Nodes (1): TicTacToreApplication

### Community 37 - "Community 37"
Cohesion: 0.5
Nodes (1): ClockConfig

### Community 38 - "Community 38"
Cohesion: 0.5
Nodes (4): Layered Architecture, Optimistic Locking, Three-Layer Transaction Architecture, Tic-Tac-Tore Product Requirements

### Community 39 - "Community 39"
Cohesion: 0.5
Nodes (4): Match API, Match Entity, Match Recording Flow, Live Match Mode

### Community 40 - "Community 40"
Cohesion: 0.5
Nodes (4): The 500-Line Rule (IP-04), OAuth2 Authentication Flow, Backend (Spring Boot 3.4.0, Java 21), Frontend (Vue 3, TypeScript)

### Community 42 - "Community 42"
Cohesion: 0.67
Nodes (1): isPlayersUnique()

### Community 43 - "Community 43"
Cohesion: 0.67
Nodes (1): User

### Community 44 - "Community 44"
Cohesion: 0.67
Nodes (1): Game

### Community 46 - "Community 46"
Cohesion: 0.67
Nodes (3): Inner Dark Blue V Path, Outer Green V Path, Vue.js Brand Logo

### Community 47 - "Community 47"
Cohesion: 0.67
Nodes (3): Available Pools List, Create a New Pool Form, I Want to Play Matchmaking Screen

### Community 70 - "Community 70"
Cohesion: 1.0
Nodes (2): Data Integrity & Immutability, Rationale: Data Integrity is Existential

### Community 71 - "Community 71"
Cohesion: 1.0
Nodes (2): Rationale: Asymmetric Participation, UX Design Specification

### Community 72 - "Community 72"
Cohesion: 1.0
Nodes (2): Match Recording Flow, Live Match Interface

### Community 73 - "Community 73"
Cohesion: 1.0
Nodes (2): Tic-Tac-Tore Main Hub, Pitch Parquet Design System

### Community 146 - "Community 146"
Cohesion: 1.0
Nodes (1): Graphify Knowledge Graph

### Community 147 - "Community 147"
Cohesion: 1.0
Nodes (1): Privacy & DSGVO Compliance

### Community 148 - "Community 148"
Cohesion: 1.0
Nodes (1): Domain-Driven Entity Design

### Community 149 - "Community 149"
Cohesion: 1.0
Nodes (1): Component-Based SPA

### Community 150 - "Community 150"
Cohesion: 1.0
Nodes (1): Statistics API

### Community 151 - "Community 151"
Cohesion: 1.0
Nodes (1): Rationale: Speed over Precision

### Community 152 - "Community 152"
Cohesion: 1.0
Nodes (1): Pitch Parquet Design

### Community 153 - "Community 153"
Cohesion: 1.0
Nodes (1): ITSF Rules Summary (German)

### Community 154 - "Community 154"
Cohesion: 1.0
Nodes (1): Tic-Tac-Tore Home Hub Screen

### Community 155 - "Community 155"
Cohesion: 1.0
Nodes (1): Architecture Decision Document

### Community 156 - "Community 156"
Cohesion: 1.0
Nodes (1): Tic-Tac-Tore Logo

## Knowledge Gaps
- **90 isolated node(s):** `Graphify Knowledge Graph`, `Tic-Tac-Tore Product Requirements`, `Data Integrity & Immutability`, `Privacy & DSGVO Compliance`, `UX Design Specification` (+85 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 12`** (10 nodes): `StatisticsIntegrationTest.java`, `StatisticsIntegrationTest`, `.createConfirmedMatch()`, `.createUser()`, `.getH2HStats_shouldFilterByPositions()`, `.getH2HStats_shouldReturnResults()`, `.getLeaderboard_shouldFilterByRole()`, `.getPersonalStats_shouldReturnResults()`, `.setUp()`, `StatisticsIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 13`** (9 nodes): `MatchApprovalIntegrationTest`, `.approveMatch_failAsCreator()`, `.approveMatch_failAsTeammate()`, `.approveMatch_notFound()`, `.approveMatch_success()`, `.createUser()`, `.setUp()`, `MatchApprovalIntegrationTest.java`, `MatchApprovalIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 14`** (9 nodes): `MatchRejectionIntegrationTest`, `.createUser()`, `.rejectMatch_failAsCreator()`, `.rejectMatch_failAsTeammate()`, `.rejectMatch_notFound()`, `.rejectMatch_success()`, `.setUp()`, `MatchRejectionIntegrationTest.java`, `MatchRejectionIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 15`** (8 nodes): `MatchIntegrationTest`, `.createMatch_nonUniquePlayers()`, `.createMatch_success()`, `.createMatch_userNotFound()`, `.createUser()`, `.setUp()`, `MatchIntegrationTest.java`, `MatchIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 16`** (8 nodes): `GlobalExceptionHandler`, `.handleGeneralException()`, `.handleIllegalArgument()`, `.handleIllegalState()`, `.handleResourceNotFound()`, `.handleValidationExceptions()`, `GlobalExceptionHandler.java`, `GlobalExceptionHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 17`** (7 nodes): `MatchPendingIntegrationTest`, `.createMatch()`, `.createUser()`, `.getPendingMatches_success()`, `.setUp()`, `MatchPendingIntegrationTest.java`, `MatchPendingIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 18`** (7 nodes): `MatchLifecycleIntegrationTest`, `.createUser()`, `.fullMatchLifecycle_success()`, `.setUp()`, `.user()`, `MatchLifecycleIntegrationTest.java`, `MatchLifecycleIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 19`** (7 nodes): `MatchCreationIntegrationTest`, `.createMatch_failDuplicatePlayers()`, `.createMatch_success()`, `.createUser()`, `.setUp()`, `MatchCreationIntegrationTest.java`, `MatchCreationIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 20`** (7 nodes): `MatchApi`, `.approveMatch()`, `.createMatch()`, `.getPendingMatches()`, `.rejectMatch()`, `MatchApi.java`, `MatchApi.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 25`** (6 nodes): `StatisticsApi.java`, `StatisticsApi`, `.getH2HStats()`, `.getLeaderboard()`, `.getPersonalStats()`, `StatisticsApi.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 27`** (5 nodes): `CustomOAuth2SuccessHandler`, `.CustomOAuth2SuccessHandler()`, `SimpleUrlAuthenticationSuccessHandler`, `CustomOAuth2SuccessHandler.java`, `CustomOAuth2SuccessHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 28`** (5 nodes): `SecurityConfig`, `.corsConfigurationSource()`, `.filterChain()`, `SecurityConfig.java`, `SecurityConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 29`** (5 nodes): `ResourceNotFoundException`, `.ResourceNotFoundException()`, `RuntimeException`, `ResourceNotFoundException.java`, `ResourceNotFoundException.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 32`** (4 nodes): `OAuth2ConfigTest`, `.googleClientRegistration_shouldBeLoaded()`, `OAuth2ConfigTest.java`, `OAuth2ConfigTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 33`** (4 nodes): `SecurityConfigTest`, `.unauthenticatedAccess_shouldBeRedirectedToLogin()`, `SecurityConfigTest.java`, `SecurityConfigTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 34`** (4 nodes): `OAuth2IntegrationTest`, `.accessingProtectedResource_withoutAuth_shouldRedirect()`, `OAuth2IntegrationTest.java`, `OAuth2IntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 35`** (4 nodes): `TicTacToreApplicationTests.java`, `TicTacToreApplicationTests`, `.contextLoads()`, `TicTacToreApplicationTests.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 36`** (4 nodes): `TicTacToreApplication.java`, `TicTacToreApplication`, `.main()`, `TicTacToreApplication.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 37`** (4 nodes): `ClockConfig`, `.clock()`, `ClockConfig.java`, `ClockConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 42`** (3 nodes): `isPlayersUnique()`, `MatchRequest.java`, `MatchRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 43`** (3 nodes): `User.java`, `User`, `User.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 44`** (3 nodes): `Game`, `Game.java`, `Game.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 70`** (2 nodes): `Data Integrity & Immutability`, `Rationale: Data Integrity is Existential`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 71`** (2 nodes): `Rationale: Asymmetric Participation`, `UX Design Specification`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 72`** (2 nodes): `Match Recording Flow`, `Live Match Interface`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 73`** (2 nodes): `Tic-Tac-Tore Main Hub`, `Pitch Parquet Design System`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 146`** (1 nodes): `Graphify Knowledge Graph`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 147`** (1 nodes): `Privacy & DSGVO Compliance`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 148`** (1 nodes): `Domain-Driven Entity Design`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 149`** (1 nodes): `Component-Based SPA`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 150`** (1 nodes): `Statistics API`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 151`** (1 nodes): `Rationale: Speed over Precision`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 152`** (1 nodes): `Pitch Parquet Design`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 153`** (1 nodes): `ITSF Rules Summary (German)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 154`** (1 nodes): `Tic-Tac-Tore Home Hub Screen`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 155`** (1 nodes): `Architecture Decision Document`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 156`** (1 nodes): `Tic-Tac-Tore Logo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Match` connect `Community 3` to `Community 0`, `Community 9`, `Community 1`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Why does `StatisticsService` connect `Community 0` to `Community 3`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Why does `g()` connect `Community 9` to `Community 3`?**
  _High betweenness centrality (0.012) - this node is a cross-community bridge._
- **What connects `Graphify Knowledge Graph`, `Tic-Tac-Tore Product Requirements`, `Data Integrity & Immutability` to the rest of the system?**
  _90 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._