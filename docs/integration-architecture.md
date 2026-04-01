# Integration Architecture

> Auto-generated: 2026-03-31 | Source: Deep Scan

## Overview

Tic-Tac-Tore is a multi-part application with a clear client-server architecture. The Vue 3 frontend communicates with the Spring Boot backend exclusively via REST API calls over HTTP.

---

## Communication Protocol

- **Type:** REST API (JSON over HTTP)
- **Authentication:** Bearer JWT tokens in `Authorization` header
- **CORS:** Backend allows `http://localhost:3000` (frontend dev server)
- **Session:** Stateless (JWT-based, no server-side sessions)

---

## Integration Points

### 1. OAuth2 Authentication Flow

```
┌──────────┐     ┌──────────────┐     ┌─────────────┐
│ Frontend  │     │   Backend    │     │   Google     │
│ (Vue 3)   │     │ (Spring Boot)│     │   OAuth2    │
└─────┬─────┘     └──────┬───────┘     └──────┬──────┘
      │                  │                    │
      │  1. Click "Login with Google"         │
      ├─────────────────►│                    │
      │  GET /oauth2/authorization/google     │
      │                  │                    │
      │                  │  2. Redirect to    │
      │                  ├───────────────────►│
      │                  │  Google consent    │
      │                  │                    │
      │                  │  3. Callback with  │
      │                  │◄───────────────────┤
      │                  │  auth code         │
      │                  │                    │
      │  4. Redirect to frontend with JWT     │
      │◄─────────────────┤                    │
      │  /oauth2/redirect?token=<JWT>         │
      │                  │                    │
      │  5. Store JWT in localStorage         │
      │  6. Set auth store state              │
```

**Backend Handler:** `CustomOAuth2SuccessHandler.java`

- Extracts email, name, providerId from Google OAuth2 attributes
- Creates or updates User in database
- Generates JWT via `JwtService.generateToken(email)`
- Redirects to `${TTT_OAUTH2_REDIRECT_URI}?token=<jwt>`

**Frontend Handler:** `OAuth2Redirect.vue`

- Extracts `token` from URL query params
- Calls `authStore.login(token, user)`
- Stores token in `localStorage`
- Redirects to home page

---

### 2. Match Recording Flow

```
Frontend (DevRecordingView)         Backend (MatchController)
        │                                    │
        │  POST /api/v1/matches              │
        ├───────────────────────────────────►│
        │  Body: MatchRequest               │
        │  Headers: Bearer <JWT>            │
        │                                    │
        │  201 Created                       │
        │◄───────────────────────────────────┤
        │  Body: MatchResponse              │
```

**Frontend → Backend Data Mapping:**

| Frontend Field         | Backend DTO Field                      |
| ---------------------- | -------------------------------------- |
| `players.creator.id`   | Derived from JWT (authenticated user)  |
| `formData.teammateId`  | `teamADefenderId` (or teamAAttackerId) |
| `formData.opponent1Id` | `teamBAttackerId`                      |
| `formData.opponent2Id` | `teamBDefenderId`                      |
| `games[].team1Score`   | `games[].teamAScore`                   |
| `games[].team2Score`   | `games[].teamBScore`                   |

---

### 3. Match Approval Flow

```
Frontend (DashboardView)            Backend (MatchController)
        │                                    │
        │  GET /api/v1/matches/pending       │
        ├───────────────────────────────────►│
        │  Headers: Bearer <JWT>             │
        │                                    │
        │  200 OK                            │
        │◄───────────────────────────────────┤
        │  Body: List<MatchResponse>         │
        │                                    │
        │  PUT /api/v1/matches/{id}/approve  │
        ├───────────────────────────────────►│
        │  OR                                │
        │  PUT /api/v1/matches/{id}/reject   │
        ├───────────────────────────────────►│
        │                                    │
        │  204 No Content                    │
        │◄───────────────────────────────────┤
```

**Frontend Behavior:** Removes match from local list on approve/reject without refetching.

---

### 4. Statistics & Leaderboard Flow

```
Frontend (LeaderboardView)          Backend (StatisticsController)
        │                                    │
        │  GET /statistics/leaderboard       │
        ├───────────────────────────────────►│
        │  ?type=OVERALL&period=ALL_TIME     │
        │  &minMatches=0&page=0&size=10      │
        │                                    │
        │  200 OK                            │
        │◄───────────────────────────────────┤
        │  Body: Page<LeaderboardEntry>      │
        │                                    │
        │  GET /statistics/me                │
        ├───────────────────────────────────►│
        │  ?period=ALL_TIME                  │
        │  Headers: Bearer <JWT>             │
        │                                    │
        │  200 OK                            │
        │◄───────────────────────────────────┤
        │  Body: PlayerStatsResponse         │
        │                                    │
        │  GET /statistics/h2h               │
        ├───────────────────────────────────►│
        │  ?period=ALL_TIME                  │
        │  &myPosition=OVERALL               │
        │  &opponentPosition=OVERALL         │
        │                                    │
        │  200 OK                            │
        │◄───────────────────────────────────┤
        │  Body: Page<H2HStatsResponse>      │
```

**Frontend Patterns:**

- `statisticsService.ts` wraps all API calls with generic `apiFetch<T>()`
- `AbortController` used for cancelling stale requests on filter changes
- `Promise.all()` for parallel personal stats + H2H fetching
- Client-side sorting on H2H table

---

## Shared Data Contracts

### DTOs that Cross the Boundary

| Frontend Type      | Backend DTO                | Direction          |
| ------------------ | -------------------------- | ------------------ |
| `LeaderboardEntry` | `LeaderboardEntryResponse` | Backend → Frontend |
| `PlayerStats`      | `PlayerStatsResponse`      | Backend → Frontend |
| `PositionStats`    | `PositionStatsResponse`    | Backend → Frontend |
| `H2HStats`         | `H2HStatsResponse`         | Backend → Frontend |
| `Page<T>`          | Spring `Page<T>`           | Backend → Frontend |
| `Match`            | `MatchResponse`            | Backend → Frontend |
| Match form data    | `MatchRequest`             | Frontend → Backend |

### Enum Values Shared

| Enum              | Values                                    |
| ----------------- | ----------------------------------------- |
| `LeaderboardType` | `OVERALL`, `ATTACKER`, `DEFENDER`         |
| `TimePeriod`      | `WEEKLY`, `MONTHLY`, `YEARLY`, `ALL_TIME` |
| `MatchStatus`     | `DRAFT`, `PENDING_APPROVAL`, `CONFIRMED`  |

---

## Environment Configuration

### Ports

| Service  | Port | Config                           |
| -------- | ---- | -------------------------------- |
| Backend  | 8080 | Spring Boot default              |
| Frontend | 3000 | `vite.config.ts` → `server.port` |

### CORS (Backend → Frontend)

```java
// SecurityConfig.java
allowedOrigins: ["http://localhost:3000"]
allowedMethods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
allowedHeaders: ["Authorization", "Content-Type", "X-XSRF-TOKEN"]
allowCredentials: true
```

### API Base URL (Frontend)

```typescript
// statisticsService.ts
API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";
```

---

## Security Boundary

```
┌───────────────────────────────────────────┐
│              FRONTEND (Vue 3)             │
│  ┌──────────────────────────────────────┐ │
│  │ Router Guard (meta.requiresAuth)     │ │
│  │ Checks: authStore.isAuthenticated    │ │
│  │ Redirects to /login if not auth      │ │
│  └──────────────────────────────────────┘ │
│  Token stored in: localStorage            │
│  Sent as: Authorization: Bearer <JWT>     │
└───────────────────────┬───────────────────┘
                        │ HTTP
┌───────────────────────▼───────────────────┐
│              BACKEND (Spring Boot)        │
│  ┌──────────────────────────────────────┐ │
│  │ JwtAuthenticationFilter              │ │
│  │ Validates: JWT signature + expiry    │ │
│  │ Sets: SecurityContext                │ │
│  └──────────────────────────────────────┘ │
│  ┌──────────────────────────────────────┐ │
│  │ SecurityConfig (URL-based rules)     │ │
│  │ permitAll: /statistics/*, /dev/*,    │ │
│  │   /swagger-ui/*, /h2-console/*       │ │
│  │ authenticated: everything else       │ │
│  └──────────────────────────────────────┘ │
└───────────────────────────────────────────┘
```

---

## Database Access (Backend Only)

The frontend has **no direct database access**. All data flows through the backend REST API.

```
Frontend ──► REST API ──► Service Layer ──► Repository ──► PostgreSQL/H2
```
