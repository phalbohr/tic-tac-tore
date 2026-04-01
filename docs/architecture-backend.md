# Architecture — Backend

> Auto-generated: 2026-03-31 | Part: backend | Tech: Spring Boot 3.4.0, Java 21

## Executive Summary

The backend is a Spring Boot REST API serving a foosball statistics platform. It provides match recording with an opponent approval workflow, comprehensive player statistics with positional analytics, and Google OAuth2 authentication with JWT-based stateless sessions.

---

## Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.4.0 |
| Persistence | Spring Data JPA / Hibernate | (managed) |
| Security | Spring Security + OAuth2 Client | (managed) |
| Auth Tokens | JJWT | 0.12.3 |
| Resilience | Spring Retry + AOP | (managed) |
| API Docs | SpringDoc OpenAPI | 2.3.0 |
| Database (dev) | H2 | (managed) |
| Database (prod) | PostgreSQL | (runtime) |
| Build | Maven | (wrapper) |
| Code Gen | Lombok | (managed) |
| Test | JUnit 5, Spring Test, JaCoCo | (managed) |

---

## Architecture Pattern

**Layered Architecture** with clear separation of concerns:

```
┌────────────────────────────────────────────┐
│         Controller Layer (REST API)        │
│  MatchController, StatisticsController     │
│  DevDataController, DevTestController      │
├────────────────────────────────────────────┤
│         API Interface Layer (OpenAPI)      │
│  MatchApi, StatisticsApi                   │
│  (Hybrid Split: docs in interface,        │
│   routing in controller)                   │
├────────────────────────────────────────────┤
│         Service Layer (Orchestration)      │
│  MatchService (@Retryable)                 │
│  StatisticsService (@Transactional RO)     │
├────────────────────────────────────────────┤
│         Operation Layer (Transactions)     │
│  MatchOperation (@Transactional)           │
├────────────────────────────────────────────┤
│         Domain Layer (Entities)            │
│  User, Match, Game                         │
│  (Business logic inside entities)          │
├────────────────────────────────────────────┤
│         Repository Layer (Data Access)     │
│  MatchRepository, UserRepository           │
│  (JPA + native SQL with CTEs)              │
├────────────────────────────────────────────┤
│         Infrastructure                     │
│  SecurityConfig, JwtAuthenticationFilter   │
│  CustomOAuth2SuccessHandler, ClockConfig   │
└────────────────────────────────────────────┘
```

---

## Key Architectural Decisions

### 1. Three-Layer Transaction Architecture

The project separates retry logic from transactional boundaries:

- **Service layer** (`MatchService`): `@Retryable` — orchestration + validation
- **Operation layer** (`MatchOperation`): `@Transactional` — atomic DB operations
- **Rule:** Never combine `@Retryable` and `@Transactional` on the same method

This ensures each retry attempt opens a fresh transaction, avoiding stale entity states.

### 2. Domain-Driven Entity Design (Tell, Don't Ask)

Business logic lives inside entity classes:
- `Match.approve()` — state transition + winner calculation
- `Match.reject()` — state transition
- `Match.calculateWinner()` — best-of-3 determination
- `Match.isUserInTeamA/B()`, `isAttacker()`, `isDefender()`, `isWinner()`

### 3. Optimistic Locking

`Match` entity uses `@Version` with `Long` wrapper type for concurrent approval handling. Combined with `@Retryable` (3 attempts, 100ms backoff) for automatic conflict resolution.

### 4. Hybrid Split OpenAPI Documentation

API documentation follows Interface-Driven Documentation pattern:
- **Interfaces** (`MatchApi`, `StatisticsApi`): `@Operation`, `@ApiResponse`, `@Tag`
- **Controllers**: Routing only (`@GetMapping`, `@Valid`, `@PathVariable`)
- SpringDoc + therapi-runtime-javadoc for description generation

### 5. Stateless Authentication

- Google OAuth2 for identity verification
- Backend-issued JWT tokens (HS256, 24h expiry)
- No server-side sessions (`STATELESS` session management)
- `JwtAuthenticationFilter` validates tokens on every request

---

## Security Architecture

### Authentication Flow

1. Frontend redirects to `/oauth2/authorization/google`
2. Spring Security handles OAuth2 dance with Google
3. `CustomOAuth2SuccessHandler` creates/updates user, generates JWT
4. Redirects to frontend with JWT as query parameter
5. Frontend stores JWT in localStorage, sends as `Bearer` token

### Authorization Rules

| Path Pattern | Access |
|-------------|--------|
| `/api/v1/statistics/**` | Public |
| `/api/v1/dev/**` | Public (dev only) |
| `/swagger-ui/**`, `/v3/api-docs/**` | Public |
| `/h2-console/**` | Public (dev only) |
| All other paths | Authenticated |

### Business Authorization

- Match creation: Creator must be one of the 4 players
- Match approval/rejection: Only opponents (not teammates of creator) can approve/reject

---

## Data Architecture

See [Data Models](./data-models-backend.md) for full schema details.

**Summary:** 3 tables (`users`, `matches`, `games`) with 5 FK relationships from `matches` to `users`.

**Query Strategy:**
- Simple lookups: JPA derived queries and JPQL
- Complex analytics: Native SQL with Common Table Expressions (CTEs)
- Pagination: Spring Data `Pageable` + custom count queries
- Projections: Interface-based for efficient DTO mapping

---

## API Design

See [API Contracts](./api-contracts-backend.md) for full endpoint details.

**Summary:** 7 endpoints across 2 domain controllers + 2 dev controllers.

| Domain | Endpoints |
|--------|-----------|
| Matches | POST create, GET pending, PUT approve, PUT reject |
| Statistics | GET leaderboard, GET personal, GET H2H |

---

## Error Handling

`GlobalExceptionHandler` (`@ControllerAdvice`) provides consistent error responses:

| Exception | HTTP Status | Response |
|-----------|-------------|----------|
| `ResourceNotFoundException` | 404 | `{"error": "..."}` |
| `MethodArgumentNotValidException` | 400 | `{"error": "...", "details": {...}}` |
| `IllegalArgumentException` | 400 | `{"error": "..."}` |
| `IllegalStateException` | 401 | `{"error": "..."}` |
| Generic `Exception` | 500 | `{"error": "An unexpected error occurred"}` |

---

## Testing Strategy

**19 test files** covering:

| Category | Files | Description |
|----------|-------|-------------|
| Integration (IT) | 7 | Full Spring context, H2 DB, MockMvc |
| Service Unit | 5 | Mocked dependencies, business logic |
| Repository | 2 | JPA queries with test data |
| Security | 3 | OAuth2 config, JWT filter, success handler |

**Conventions:**
- Integration tests: `*IT.java` suffix
- Unit tests: `*Test.java` suffix
- AAA pattern (no section comments)
- `@DisplayName` for readable test names
- JaCoCo for coverage reporting

---

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `TTT_GOOGLE_CLIENT_ID` | Google OAuth2 client ID | — (required) |
| `TTT_GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | — (required) |
| `TTT_JWT_SECRET` | JWT signing key | Built-in default |
| `TTT_OAUTH2_REDIRECT_URI` | OAuth2 redirect URL | `http://localhost:3000/oauth2/redirect` |

### Build & Run

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Test
./mvnw test

# Test coverage
./mvnw test jacoco:report
```

---

## Package Structure

```
com.tictactore
├── annotation      # Custom annotations (@Idempotent)
├── api             # OpenAPI interface contracts
├── config          # Spring configuration beans
├── controller      # REST controllers
├── dto             # Data transfer objects
│   └── statistics  # Statistics-specific DTOs
├── exception       # Exception handling
├── mapper          # Entity ↔ DTO mappers
├── model           # JPA entities + enums
├── repository      # Data access layer
├── security        # JWT filter + OAuth2 handler
└── service         # Business logic
```
