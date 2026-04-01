# Source Tree Analysis

> Auto-generated: 2026-03-31 | Source: Deep Scan

## Repository Structure

**Type:** Multi-part (Backend + Frontend in single repository)

```
tic-tac-tore/
├── src/                            # ── BACKEND (Spring Boot 3.4.0, Java 21) ──
│   ├── main/
│   │   ├── java/com/tictactore/
│   │   │   ├── TicTacToreApplication.java    ★ Entry point (@SpringBootApplication, @EnableRetry)
│   │   │   ├── annotation/
│   │   │   │   └── Idempotent.java           # Custom marker annotation for idempotent ops
│   │   │   ├── api/                          # OpenAPI interface contracts (Hybrid Split)
│   │   │   │   ├── MatchApi.java             # @Operation/@ApiResponse for Match endpoints
│   │   │   │   └── StatisticsApi.java        # @Operation/@ApiResponse for Statistics endpoints
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java       # Security filter chain, CORS, OAuth2 setup
│   │   │   │   └── ClockConfig.java          # Clock bean for testable time operations
│   │   │   ├── controller/
│   │   │   │   ├── MatchController.java      # /api/v1/matches — CRUD + approval workflow
│   │   │   │   ├── StatisticsController.java # /api/v1/statistics — leaderboard, stats, H2H
│   │   │   │   ├── DevDataController.java    # /api/v1/dev/seed-matches — test data seeding
│   │   │   │   └── DevTestController.java    # /api/v1/dev/seed — test user + token seeding
│   │   │   ├── dto/
│   │   │   │   ├── MatchRequest.java         # Match creation input (4 players + games)
│   │   │   │   ├── MatchResponse.java        # Match output (names + status + games)
│   │   │   │   ├── GameRequest.java          # Game scores input
│   │   │   │   ├── GameResponse.java         # Game scores output
│   │   │   │   └── statistics/
│   │   │   │       ├── LeaderboardParams.java      # Query params DTO (@ParameterObject)
│   │   │   │       ├── LeaderboardEntryResponse.java
│   │   │   │       ├── LeaderboardType.java        # Enum: OVERALL, ATTACKER, DEFENDER
│   │   │   │       ├── PlayerStatsResponse.java
│   │   │   │       ├── PositionStatsResponse.java
│   │   │   │       ├── H2HParams.java              # Query params DTO (@ParameterObject)
│   │   │   │       ├── H2HStatsResponse.java
│   │   │   │       └── TimePeriod.java             # Enum: WEEKLY, MONTHLY, YEARLY, ALL_TIME
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java     # @ControllerAdvice — maps exceptions → HTTP
│   │   │   │   └── ResourceNotFoundException.java  # 404 domain exception
│   │   │   ├── mapper/
│   │   │   │   └── MatchMapper.java          # Match entity → MatchResponse conversion
│   │   │   ├── model/                        # JPA Entities (domain layer)
│   │   │   │   ├── User.java                 # Player profile (email, name, providerId)
│   │   │   │   ├── Match.java                # Match record with business logic methods
│   │   │   │   ├── Game.java                 # Individual game scores within a match
│   │   │   │   ├── MatchStatus.java          # Enum: DRAFT, PENDING_APPROVAL, CONFIRMED
│   │   │   │   └── WinnerTeam.java           # Enum: TEAM_A, TEAM_B
│   │   │   ├── repository/
│   │   │   │   ├── MatchRepository.java      # Complex native SQL queries (leaderboard, H2H)
│   │   │   │   ├── UserRepository.java       # findByEmail for OAuth2 lookup
│   │   │   │   ├── LeaderboardProjection.java
│   │   │   │   ├── H2HProjection.java
│   │   │   │   └── PlayerStatsProjection.java
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java    # JWT validation filter (OncePerRequestFilter)
│   │   │   │   └── CustomOAuth2SuccessHandler.java # Google OAuth2 → JWT token generation
│   │   │   └── service/
│   │   │       ├── MatchService.java         # Orchestration layer (@Retryable)
│   │   │       ├── MatchOperation.java       # Transactional operations (@Transactional)
│   │   │       ├── StatisticsService.java    # Leaderboard, personal stats, H2H analytics
│   │   │       └── JwtService.java           # JWT generation, validation, claim extraction
│   │   └── resources/
│   │       └── application.yml               # H2 DB, OAuth2, JWT config
│   └── test/java/com/tictactore/             # ── BACKEND TESTS (19 files) ──
│       ├── controller/                       # Integration tests (IT suffix)
│       │   ├── match/
│       │   │   ├── MatchCreationIT.java
│       │   │   ├── MatchApprovalIT.java
│       │   │   ├── MatchRejectionIT.java
│       │   │   ├── MatchLifecycleIT.java
│       │   │   └── MatchPendingIT.java
│       │   ├── StatisticsControllerIT.java
│       │   └── OAuth2ControllerIT.java
│       ├── service/
│       │   ├── MatchServiceTest.java
│       │   ├── MatchOperationTest.java
│       │   ├── MatchApprovalServiceTest.java
│       │   ├── JwtServiceTest.java
│       │   └── StatisticsServiceTest.java
│       ├── repository/
│       │   ├── UserRepositoryTest.java
│       │   └── MatchRepositoryTest.java
│       ├── security/
│       │   ├── OAuth2ConfigIT.java
│       │   ├── SecurityConfigIT.java
│       │   └── CustomOAuth2SuccessHandlerTest.java
│       └── TicTacToreApplicationTests.java
│
├── frontend/                       # ── FRONTEND (Vue 3, TypeScript) ──
│   ├── src/
│   │   ├── main.ts                 ★ Entry point (createApp + Pinia + Router)
│   │   ├── App.vue                 # Root layout (TheNavigation + RouterView)
│   │   ├── assets/
│   │   │   └── main.css            # Global styles
│   │   ├── components/             # Reusable UI components
│   │   │   ├── TheNavigation.vue         # Sticky nav bar + auth controls
│   │   │   ├── MatchRecordingForm.vue    # Player selection form (Step 1)
│   │   │   ├── MatchScoring.vue          # Game scoring with position mgmt (Step 2)
│   │   │   ├── PendingApprovals.vue      # Match approval/reject cards
│   │   │   ├── PlayerStatsSummary.vue    # Personal stats dashboard
│   │   │   ├── H2HAnalyticsTable.vue     # Sortable H2H table
│   │   │   ├── HelloWorld.vue            # Example (unused)
│   │   │   ├── TheWelcome.vue            # Welcome content (unused)
│   │   │   ├── WelcomeItem.vue           # Welcome card layout (unused)
│   │   │   └── icons/                    # SVG icon components (5 files)
│   │   ├── views/                  # Route-based page components
│   │   │   ├── HomeView.vue              # Landing page (public)
│   │   │   ├── LoginView.vue             # Google OAuth login
│   │   │   ├── OAuth2Redirect.vue        # OAuth2 callback → token capture
│   │   │   ├── DashboardView.vue         # Match approvals (auth required)
│   │   │   ├── LeaderboardView.vue       # Rankings + personal stats + H2H
│   │   │   ├── DevRecordingView.vue      # Match recording flow (dev)
│   │   │   ├── TokenView.vue             # JWT debug viewer
│   │   │   └── AboutView.vue             # Placeholder (auth required)
│   │   ├── router/
│   │   │   └── index.ts                  # 8 routes + auth navigation guard
│   │   ├── stores/                 # Pinia state management
│   │   │   ├── auth.ts                   # JWT token + user state + localStorage
│   │   │   └── counter.ts                # Example store (unused)
│   │   └── services/
│   │       └── statisticsService.ts      # API client (leaderboard, stats, H2H)
│   ├── e2e/                        # Playwright E2E tests
│   │   ├── auth.spec.ts
│   │   └── vue.spec.ts
│   ├── package.json                # Dependencies manifest
│   ├── vite.config.ts              # Vite + Vue + Tailwind + DevTools
│   ├── vitest.config.ts            # Unit test config (jsdom)
│   ├── playwright.config.ts        # E2E test config
│   ├── tsconfig.json               # TypeScript (composite, 3 sub-configs)
│   └── eslint.config.ts            # ESLint + Oxlint + Prettier
│
├── conductor/                      # ── PROJECT MANAGEMENT ──
│   ├── index.md                    # Conductor hub index
│   ├── product.md                  # Product requirements & features
│   ├── product-guidelines.md       # Visual identity & UX philosophy
│   ├── tech-stack.md               # Technology decisions
│   ├── workflow.md                 # Task tracking & workflow
│   ├── tracks.md                   # Active/archived tracks
│   ├── rca-journal.md              # Root cause analysis journal
│   ├── code_styleguides/           # Coding standards
│   ├── manual-testing/             # Manual test procedures
│   └── archive/                    # Completed track archives
│
├── .gemini/rules/                  # ── CODE CONVENTIONS ──
│   ├── code-guide.md               # 19 mandatory coding rules
│   ├── openapi-guide.md            # OpenAPI documentation rules
│   ├── test-guide.md               # Testing conventions
│   └── review-guide.md             # Code review checklist
│
├── pom.xml                         # Maven build (Spring Boot 3.4.0, Java 21)
├── mvnw / mvnw.cmd                 # Maven wrapper
└── .gitignore
```

## Critical Folders Summary

| Folder                          | Purpose                             | Part     |
| ------------------------------- | ----------------------------------- | -------- |
| `src/main/java/com/tictactore/` | Backend application code (41 files) | Backend  |
| `src/main/java/.../model/`      | JPA entities with domain logic      | Backend  |
| `src/main/java/.../service/`    | Business logic + orchestration      | Backend  |
| `src/main/java/.../controller/` | REST endpoints                      | Backend  |
| `src/main/java/.../repository/` | Data access + native SQL queries    | Backend  |
| `src/main/java/.../security/`   | JWT filter + OAuth2 handler         | Backend  |
| `src/main/resources/`           | Application configuration           | Backend  |
| `src/test/`                     | Backend tests (19 files)            | Backend  |
| `frontend/src/components/`      | Reusable Vue components             | Frontend |
| `frontend/src/views/`           | Route-based pages                   | Frontend |
| `frontend/src/stores/`          | Pinia state management              | Frontend |
| `frontend/src/services/`        | API client layer                    | Frontend |
| `frontend/src/router/`          | Route definitions + guards          | Frontend |
| `conductor/`                    | Project management & tracking       | Shared   |
| `.gemini/rules/`                | Code conventions & guidelines       | Shared   |

## Integration Points

```
Frontend (port 3000)  ──── REST API ────►  Backend (port 8080)
     │                                          │
     ├── GET /statistics/*                      ├── Spring Security
     ├── POST /matches                          ├── OAuth2 (Google)
     ├── PUT /matches/{id}/approve              ├── JWT generation
     ├── PUT /matches/{id}/reject               │
     ├── GET /matches/pending                   ├── H2 DB (dev)
     └── OAuth2 redirect flow ◄─────────────────┘   PostgreSQL (prod)
```
