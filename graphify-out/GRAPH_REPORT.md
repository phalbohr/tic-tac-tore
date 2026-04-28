# Graph Report - .  (2026-04-28)

## Corpus Check
- 187 files · ~243,189 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 607 nodes · 631 edges · 65 communities detected
- Extraction: 84% EXTRACTED · 16% INFERRED · 0% AMBIGUOUS · INFERRED: 100 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Statistics Repository & Projections|Statistics Repository & Projections]]
- [[_COMMUNITY_Match Operations & Services|Match Operations & Services]]
- [[_COMMUNITY_Security & OAuth2 Authentication|Security & OAuth2 Authentication]]
- [[_COMMUNITY_Match Model & Domain Logic|Match Model & Domain Logic]]
- [[_COMMUNITY_Match Controller & Service Implementation|Match Controller & Service Implementation]]
- [[_COMMUNITY_Coverage Table Sorting|Coverage Table Sorting]]
- [[_COMMUNITY_Matchmaking & Rules UI|Matchmaking & Rules UI]]
- [[_COMMUNITY_Statistics API & Params|Statistics API & Params]]
- [[_COMMUNITY_Match Scoring UI Components|Match Scoring UI Components]]
- [[_COMMUNITY_Coverage Report Formatting|Coverage Report Formatting]]
- [[_COMMUNITY_Design System & Wiki|Design System & Wiki]]
- [[_COMMUNITY_Live Match UI Controls|Live Match UI Controls]]
- [[_COMMUNITY_Statistics Integration Tests|Statistics Integration Tests]]
- [[_COMMUNITY_Match Approval Integration Tests|Match Approval Integration Tests]]
- [[_COMMUNITY_Match Rejection Integration Tests|Match Rejection Integration Tests]]
- [[_COMMUNITY_Match Creation Integration Tests|Match Creation Integration Tests]]
- [[_COMMUNITY_Global Exception Handling|Global Exception Handling]]
- [[_COMMUNITY_Pending Match Integration Tests|Pending Match Integration Tests]]
- [[_COMMUNITY_Match Lifecycle Integration Tests|Match Lifecycle Integration Tests]]
- [[_COMMUNITY_Match Creation Flow Tests|Match Creation Flow Tests]]
- [[_COMMUNITY_Match API Definition|Match API Definition]]
- [[_COMMUNITY_Personal Cabinet UI|Personal Cabinet UI]]
- [[_COMMUNITY_Backend Architecture Patterns|Backend Architecture Patterns]]
- [[_COMMUNITY_Coverage Navigation|Coverage Navigation]]
- [[_COMMUNITY_Frontend Statistics Service|Frontend Statistics Service]]
- [[_COMMUNITY_Backend Statistics API|Backend Statistics API]]
- [[_COMMUNITY_Tournament Selection UI|Tournament Selection UI]]
- [[_COMMUNITY_Security Configuration|Security Configuration]]
- [[_COMMUNITY_Resource Not Found Handling|Resource Not Found Handling]]
- [[_COMMUNITY_Project Documentation Guides|Project Documentation Guides]]
- [[_COMMUNITY_User Preferences UI|User Preferences UI]]
- [[_COMMUNITY_Product Readiness & PRD|Product Readiness & PRD]]
- [[_COMMUNITY_OAuth2 Config Tests|OAuth2 Config Tests]]
- [[_COMMUNITY_Security Config Tests|Security Config Tests]]
- [[_COMMUNITY_OAuth2 Integration Tests|OAuth2 Integration Tests]]
- [[_COMMUNITY_Application Context Tests|Application Context Tests]]
- [[_COMMUNITY_Spring Boot Main Application|Spring Boot Main Application]]
- [[_COMMUNITY_System Clock Configuration|System Clock Configuration]]
- [[_COMMUNITY_Backend Architectural Decisions|Backend Architectural Decisions]]
- [[_COMMUNITY_Core Integration Architecture|Core Integration Architecture]]
- [[_COMMUNITY_Multi-Layer Security & Analysis|Multi-Layer Security & Analysis]]
- [[_COMMUNITY_Match Request Validation|Match Request Validation]]
- [[_COMMUNITY_User Domain Model|User Domain Model]]
- [[_COMMUNITY_Game Domain Model|Game Domain Model]]
- [[_COMMUNITY_SVG Logo Assets|SVG Logo Assets]]
- [[_COMMUNITY_Pool Selection & Matchmaking|Pool Selection & Matchmaking]]
- [[_COMMUNITY_Undo Toast Rationale|Undo Toast Rationale]]
- [[_COMMUNITY_Analytics Engine Rationale|Analytics Engine Rationale]]
- [[_COMMUNITY_Data Integrity Rationale|Data Integrity Rationale]]
- [[_COMMUNITY_Asymmetric Participation Rationale|Asymmetric Participation Rationale]]
- [[_COMMUNITY_Match Recording Flow Implementation|Match Recording Flow Implementation]]
- [[_COMMUNITY_Hub UI & Design System|Hub UI & Design System]]
- [[_COMMUNITY_Social Matchmaking Persona|Social Matchmaking Persona]]
- [[_COMMUNITY_Tournament Management Persona|Tournament Management Persona]]
- [[_COMMUNITY_Community 150|Community 150]]
- [[_COMMUNITY_Community 151|Community 151]]
- [[_COMMUNITY_Community 152|Community 152]]
- [[_COMMUNITY_Community 153|Community 153]]
- [[_COMMUNITY_Community 154|Community 154]]
- [[_COMMUNITY_Community 155|Community 155]]
- [[_COMMUNITY_Community 156|Community 156]]
- [[_COMMUNITY_Community 157|Community 157]]
- [[_COMMUNITY_Community 158|Community 158]]
- [[_COMMUNITY_Community 159|Community 159]]
- [[_COMMUNITY_Community 160|Community 160]]

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
- **Domain Hierarchy** — prd_layer_verified_data_capture, prd_layer_multi_rule_analytics_engine, prd_layer_tournament_management, prd_layer_social_matchmaking [EXTRACTED 1.00]

## Communities

### Community 0 - "Statistics Repository & Projections"
Cohesion: 0.07
Nodes (6): H2HProjection, LeaderboardProjection, MatchRepository, MatchRepositoryTest, PlayerStatsProjection, StatisticsService

### Community 1 - "Match Operations & Services"
Cohesion: 0.06
Nodes (7): MatchApprovalTest, MatchMapper, MatchOperation, MatchOperationTest, MatchServiceTest, UserRepository, UserRepositoryTest

### Community 2 - "Security & OAuth2 Authentication"
Cohesion: 0.08
Nodes (8): CustomOAuth2SuccessHandler, CustomOAuth2SuccessHandlerTest, DevTestController, JwtAuthenticationFilter, JwtService, JwtServiceTest, OncePerRequestFilter, SimpleUrlAuthenticationSuccessHandler

### Community 3 - "Match Model & Domain Logic"
Cohesion: 0.12
Nodes (3): DevDataController, Match, StatisticsServiceTest

### Community 4 - "Match Controller & Service Implementation"
Cohesion: 0.16
Nodes (3): MatchApi, MatchController, MatchService

### Community 5 - "Coverage Table Sorting"
Cohesion: 0.36
Nodes (13): addSearchBox(), addSortIndicators(), enableUI(), getNthColumn(), getTable(), getTableBody(), getTableHeader(), loadColumns() (+5 more)

### Community 6 - "Matchmaking & Rules UI"
Cohesion: 0.13
Nodes (15): CONFIGURE TEAMS Button, CUSTOM, DTFB, 04 / EXPERIENCE Section, 01 / FORMAT Section, ITSF, LIVE MATCH, 1 VS 1 (+7 more)

### Community 7 - "Statistics API & Params"
Cohesion: 0.18
Nodes (4): H2HParams, LeaderboardParams, StatisticsApi, StatisticsController

### Community 8 - "Match Scoring UI Components"
Cohesion: 0.15
Nodes (13): Complete Match Button, Defense A, Defense B, MATCH ENTRY Header, Previous Match Score Indicator, Match Entry Screen (Landscape Mobile), Score Input (Team Alpha), Score Input (Team Bravo) (+5 more)

### Community 9 - "Coverage Report Formatting"
Cohesion: 0.44
Nodes (10): a(), B(), c(), D(), g(), i(), k(), o() (+2 more)

### Community 10 - "Design System & Wiki"
Cohesion: 0.17
Nodes (12): Design System Specification, DTFB Bundesliga Regulations 2024, ITSF Standard Matchplay Rules 2024, Vue 3 Technical Stack, Home Hub UI, Live Match Scoring UI, Matchmaking UI (I Want to Play), New Match Configuration UI (+4 more)

### Community 11 - "Live Match UI Controls"
Cohesion: 0.18
Nodes (11): Cancel Goal Button, Complete Match Button, Confirm Goal Button, Undo Action Button, Player J. DOE, Player M. KING, Player P. BOSS, Player S. SMITH (+3 more)

### Community 12 - "Statistics Integration Tests"
Cohesion: 0.24
Nodes (1): StatisticsIntegrationTest

### Community 13 - "Match Approval Integration Tests"
Cohesion: 0.25
Nodes (1): MatchApprovalIntegrationTest

### Community 14 - "Match Rejection Integration Tests"
Cohesion: 0.25
Nodes (1): MatchRejectionIntegrationTest

### Community 15 - "Match Creation Integration Tests"
Cohesion: 0.29
Nodes (1): MatchIntegrationTest

### Community 16 - "Global Exception Handling"
Cohesion: 0.25
Nodes (1): GlobalExceptionHandler

### Community 17 - "Pending Match Integration Tests"
Cohesion: 0.38
Nodes (1): MatchPendingIntegrationTest

### Community 18 - "Match Lifecycle Integration Tests"
Cohesion: 0.38
Nodes (1): MatchLifecycleIntegrationTest

### Community 19 - "Match Creation Flow Tests"
Cohesion: 0.33
Nodes (1): MatchCreationIntegrationTest

### Community 20 - "Match API Definition"
Cohesion: 0.29
Nodes (1): MatchApi

### Community 21 - "Personal Cabinet UI"
Cohesion: 0.29
Nodes (7): Confirm Changes Button, Dark Theme with Green Accents, Language Selection Toggle, Nickname Input Field, Personal Cabinet Screen, Profile Header, Push Notifications Toggle

### Community 22 - "Backend Architecture Patterns"
Cohesion: 0.29
Nodes (7): AD-01: Immutable RuleConfiguration, AD-02: Isolated Verification Pipeline, AD-03: Stateless JWT with Redis Denylist, AD-04: GDPR Compliance via Pseudonymization, AD-05: 3-Tier Statistics Model, AD-06: PWA-First Infrastructure, Custom Layered Monolith (Spring Boot 4.0 + Vite 8)

### Community 23 - "Coverage Navigation"
Cohesion: 0.73
Nodes (4): goToNext(), goToPrevious(), makeCurrent(), toggleClass()

### Community 24 - "Frontend Statistics Service"
Cohesion: 0.67
Nodes (4): apiFetch(), getH2HStats(), getLeaderboard(), getPersonalStats()

### Community 25 - "Backend Statistics API"
Cohesion: 0.33
Nodes (1): StatisticsApi

### Community 26 - "Tournament Selection UI"
Cohesion: 0.33
Nodes (6): Tournaments Screen, Tournament Card: Founder's Cup, Tournament Card: Rookie Rumble, Tournament Card: The Wooden Road Classic, Top App Bar, Tournament List

### Community 27 - "Security Configuration"
Cohesion: 0.4
Nodes (1): SecurityConfig

### Community 28 - "Resource Not Found Handling"
Cohesion: 0.4
Nodes (2): ResourceNotFoundException, RuntimeException

### Community 29 - "Project Documentation Guides"
Cohesion: 0.4
Nodes (5): Backend Code Conventions, OpenAPI Rules, Code Reviewer Guide, Spring Boot 3 Technical Stack, Testing Conventions

### Community 30 - "User Preferences UI"
Cohesion: 0.4
Nodes (5): Favorite Players Section, Quick Match Defaults Section, Rule Templates Section, Saved Teams Section, Teams & Rules Screen

### Community 31 - "Product Readiness & PRD"
Cohesion: 0.4
Nodes (5): Critical Gap: Missing Epics and Stories, Readiness Status: NOT READY, Persona: Max (The Competitor), Tic-Tac-Tore Product, PRD Holistic Quality Rating: 4/5

### Community 32 - "OAuth2 Config Tests"
Cohesion: 0.5
Nodes (1): OAuth2ConfigTest

### Community 33 - "Security Config Tests"
Cohesion: 0.5
Nodes (1): SecurityConfigTest

### Community 34 - "OAuth2 Integration Tests"
Cohesion: 0.5
Nodes (1): OAuth2IntegrationTest

### Community 35 - "Application Context Tests"
Cohesion: 0.5
Nodes (1): TicTacToreApplicationTests

### Community 36 - "Spring Boot Main Application"
Cohesion: 0.5
Nodes (1): TicTacToreApplication

### Community 37 - "System Clock Configuration"
Cohesion: 0.5
Nodes (1): ClockConfig

### Community 38 - "Backend Architectural Decisions"
Cohesion: 0.5
Nodes (4): Layered Architecture, Optimistic Locking, Three-Layer Transaction Architecture, Tic-Tac-Tore Product Requirements

### Community 39 - "Core Integration Architecture"
Cohesion: 0.5
Nodes (4): Match API, Match Entity, Match Recording Flow, Live Match Mode

### Community 40 - "Multi-Layer Security & Analysis"
Cohesion: 0.5
Nodes (4): The 500-Line Rule (IP-04), OAuth2 Authentication Flow, Backend (Spring Boot 3.4.0, Java 21), Frontend (Vue 3, TypeScript)

### Community 42 - "Match Request Validation"
Cohesion: 0.67
Nodes (1): isPlayersUnique()

### Community 43 - "User Domain Model"
Cohesion: 0.67
Nodes (1): User

### Community 44 - "Game Domain Model"
Cohesion: 0.67
Nodes (1): Game

### Community 46 - "SVG Logo Assets"
Cohesion: 0.67
Nodes (3): Inner Dark Blue V Path, Outer Green V Path, Vue.js Brand Logo

### Community 47 - "Pool Selection & Matchmaking"
Cohesion: 0.67
Nodes (3): Available Pools List, Create a New Pool Form, I Want to Play Matchmaking Screen

### Community 48 - "Undo Toast Rationale"
Cohesion: 1.0
Nodes (3): FR13: 15s Undo Toast, KD-03: Speed over Precision, Verified Data Capture Layer

### Community 49 - "Analytics Engine Rationale"
Cohesion: 0.67
Nodes (3): KD-01: Unified Rule Model, KD-02: 3-Tier Statistics Output, Multi-Rule Analytics Engine Layer

### Community 72 - "Data Integrity Rationale"
Cohesion: 1.0
Nodes (2): Data Integrity & Immutability, Rationale: Data Integrity is Existential

### Community 73 - "Asymmetric Participation Rationale"
Cohesion: 1.0
Nodes (2): Rationale: Asymmetric Participation, UX Design Specification

### Community 74 - "Match Recording Flow Implementation"
Cohesion: 1.0
Nodes (2): Match Recording Flow, Live Match Interface

### Community 75 - "Hub UI & Design System"
Cohesion: 1.0
Nodes (2): Tic-Tac-Tore Main Hub, Pitch Parquet Design System

### Community 76 - "Social Matchmaking Persona"
Cohesion: 1.0
Nodes (2): Social Matchmaking Layer, Persona: Lisa (The Social Player)

### Community 77 - "Tournament Management Persona"
Cohesion: 1.0
Nodes (2): Tournament Management Layer, Persona: Oleg (The Organizer)

### Community 150 - "Community 150"
Cohesion: 1.0
Nodes (1): Graphify Knowledge Graph

### Community 151 - "Community 151"
Cohesion: 1.0
Nodes (1): Privacy & DSGVO Compliance

### Community 152 - "Community 152"
Cohesion: 1.0
Nodes (1): Domain-Driven Entity Design

### Community 153 - "Community 153"
Cohesion: 1.0
Nodes (1): Component-Based SPA

### Community 154 - "Community 154"
Cohesion: 1.0
Nodes (1): Statistics API

### Community 155 - "Community 155"
Cohesion: 1.0
Nodes (1): Rationale: Speed over Precision

### Community 156 - "Community 156"
Cohesion: 1.0
Nodes (1): Pitch Parquet Design

### Community 157 - "Community 157"
Cohesion: 1.0
Nodes (1): ITSF Rules Summary (German)

### Community 158 - "Community 158"
Cohesion: 1.0
Nodes (1): Tic-Tac-Tore Home Hub Screen

### Community 159 - "Community 159"
Cohesion: 1.0
Nodes (1): Architecture Decision Document

### Community 160 - "Community 160"
Cohesion: 1.0
Nodes (1): Tic-Tac-Tore Logo

## Knowledge Gaps
- **100 isolated node(s):** `Graphify Knowledge Graph`, `Tic-Tac-Tore Product Requirements`, `Data Integrity & Immutability`, `Privacy & DSGVO Compliance`, `UX Design Specification` (+95 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Statistics Integration Tests`** (10 nodes): `StatisticsIntegrationTest.java`, `StatisticsIntegrationTest`, `.createConfirmedMatch()`, `.createUser()`, `.getH2HStats_shouldFilterByPositions()`, `.getH2HStats_shouldReturnResults()`, `.getLeaderboard_shouldFilterByRole()`, `.getPersonalStats_shouldReturnResults()`, `.setUp()`, `StatisticsIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Match Approval Integration Tests`** (9 nodes): `MatchApprovalIntegrationTest`, `.approveMatch_failAsCreator()`, `.approveMatch_failAsTeammate()`, `.approveMatch_notFound()`, `.approveMatch_success()`, `.createUser()`, `.setUp()`, `MatchApprovalIntegrationTest.java`, `MatchApprovalIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Match Rejection Integration Tests`** (9 nodes): `MatchRejectionIntegrationTest`, `.createUser()`, `.rejectMatch_failAsCreator()`, `.rejectMatch_failAsTeammate()`, `.rejectMatch_notFound()`, `.rejectMatch_success()`, `.setUp()`, `MatchRejectionIntegrationTest.java`, `MatchRejectionIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Match Creation Integration Tests`** (8 nodes): `MatchIntegrationTest`, `.createMatch_nonUniquePlayers()`, `.createMatch_success()`, `.createMatch_userNotFound()`, `.createUser()`, `.setUp()`, `MatchIntegrationTest.java`, `MatchIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Global Exception Handling`** (8 nodes): `GlobalExceptionHandler`, `.handleGeneralException()`, `.handleIllegalArgument()`, `.handleIllegalState()`, `.handleResourceNotFound()`, `.handleValidationExceptions()`, `GlobalExceptionHandler.java`, `GlobalExceptionHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Pending Match Integration Tests`** (7 nodes): `MatchPendingIntegrationTest`, `.createMatch()`, `.createUser()`, `.getPendingMatches_success()`, `.setUp()`, `MatchPendingIntegrationTest.java`, `MatchPendingIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Match Lifecycle Integration Tests`** (7 nodes): `MatchLifecycleIntegrationTest`, `.createUser()`, `.fullMatchLifecycle_success()`, `.setUp()`, `.user()`, `MatchLifecycleIntegrationTest.java`, `MatchLifecycleIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Match Creation Flow Tests`** (7 nodes): `MatchCreationIntegrationTest`, `.createMatch_failDuplicatePlayers()`, `.createMatch_success()`, `.createUser()`, `.setUp()`, `MatchCreationIntegrationTest.java`, `MatchCreationIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Match API Definition`** (7 nodes): `MatchApi`, `.approveMatch()`, `.createMatch()`, `.getPendingMatches()`, `.rejectMatch()`, `MatchApi.java`, `MatchApi.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Backend Statistics API`** (6 nodes): `StatisticsApi.java`, `StatisticsApi`, `.getH2HStats()`, `.getLeaderboard()`, `.getPersonalStats()`, `StatisticsApi.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Security Configuration`** (5 nodes): `SecurityConfig`, `.corsConfigurationSource()`, `.filterChain()`, `SecurityConfig.java`, `SecurityConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Resource Not Found Handling`** (5 nodes): `ResourceNotFoundException`, `.ResourceNotFoundException()`, `RuntimeException`, `ResourceNotFoundException.java`, `ResourceNotFoundException.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuth2 Config Tests`** (4 nodes): `OAuth2ConfigTest`, `.googleClientRegistration_shouldBeLoaded()`, `OAuth2ConfigTest.java`, `OAuth2ConfigTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Security Config Tests`** (4 nodes): `SecurityConfigTest`, `.unauthenticatedAccess_shouldBeRedirectedToLogin()`, `SecurityConfigTest.java`, `SecurityConfigTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuth2 Integration Tests`** (4 nodes): `OAuth2IntegrationTest`, `.accessingProtectedResource_withoutAuth_shouldRedirect()`, `OAuth2IntegrationTest.java`, `OAuth2IntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Application Context Tests`** (4 nodes): `TicTacToreApplicationTests.java`, `TicTacToreApplicationTests`, `.contextLoads()`, `TicTacToreApplicationTests.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Spring Boot Main Application`** (4 nodes): `TicTacToreApplication.java`, `TicTacToreApplication`, `.main()`, `TicTacToreApplication.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `System Clock Configuration`** (4 nodes): `ClockConfig`, `.clock()`, `ClockConfig.java`, `ClockConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Match Request Validation`** (3 nodes): `isPlayersUnique()`, `MatchRequest.java`, `MatchRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `User Domain Model`** (3 nodes): `User.java`, `User`, `User.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Game Domain Model`** (3 nodes): `Game`, `Game.java`, `Game.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Data Integrity Rationale`** (2 nodes): `Data Integrity & Immutability`, `Rationale: Data Integrity is Existential`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Asymmetric Participation Rationale`** (2 nodes): `Rationale: Asymmetric Participation`, `UX Design Specification`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Match Recording Flow Implementation`** (2 nodes): `Match Recording Flow`, `Live Match Interface`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Hub UI & Design System`** (2 nodes): `Tic-Tac-Tore Main Hub`, `Pitch Parquet Design System`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Social Matchmaking Persona`** (2 nodes): `Social Matchmaking Layer`, `Persona: Lisa (The Social Player)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Tournament Management Persona`** (2 nodes): `Tournament Management Layer`, `Persona: Oleg (The Organizer)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 150`** (1 nodes): `Graphify Knowledge Graph`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 151`** (1 nodes): `Privacy & DSGVO Compliance`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 152`** (1 nodes): `Domain-Driven Entity Design`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 153`** (1 nodes): `Component-Based SPA`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 154`** (1 nodes): `Statistics API`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 155`** (1 nodes): `Rationale: Speed over Precision`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 156`** (1 nodes): `Pitch Parquet Design`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 157`** (1 nodes): `ITSF Rules Summary (German)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 158`** (1 nodes): `Tic-Tac-Tore Home Hub Screen`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 159`** (1 nodes): `Architecture Decision Document`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 160`** (1 nodes): `Tic-Tac-Tore Logo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Match` connect `Match Model & Domain Logic` to `Statistics Repository & Projections`, `Coverage Report Formatting`, `Match Operations & Services`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **Why does `StatisticsService` connect `Statistics Repository & Projections` to `Match Model & Domain Logic`?**
  _High betweenness centrality (0.015) - this node is a cross-community bridge._
- **Why does `g()` connect `Coverage Report Formatting` to `Match Model & Domain Logic`?**
  _High betweenness centrality (0.011) - this node is a cross-community bridge._
- **What connects `Graphify Knowledge Graph`, `Tic-Tac-Tore Product Requirements`, `Data Integrity & Immutability` to the rest of the system?**
  _100 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Statistics Repository & Projections` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Match Operations & Services` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Security & OAuth2 Authentication` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._