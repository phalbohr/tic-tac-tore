# API Contracts — Backend

> Auto-generated: 2026-03-31 | Source: Deep Scan of Spring Boot REST API

## Base URL

```
/api/v1
```

## Authentication

- **Method:** Bearer JWT Token (via `Authorization: Bearer <token>` header)
- **Provider:** Google OAuth2 → Backend-issued JWT
- **Token Lifetime:** 86,400,000ms (24 hours)

---

## Match API

**Tag:** Matches
**Base Path:** `/api/v1/matches`
**Controller:** `MatchController.java`
**API Interface:** `MatchApi.java`

### POST `/matches` — Record a New Match

Creates a new 2v2 foosball match. Initial status is `PENDING_APPROVAL`.

**Auth:** Required

**Request Body:** `MatchRequest`

```json
{
  "teamAAttackerId": "UUID",
  "teamADefenderId": "UUID",
  "teamBAttackerId": "UUID",
  "teamBDefenderId": "UUID",
  "games": [
    { "teamAScore": 10, "teamBScore": 5 },
    { "teamAScore": 3, "teamBScore": 10 }
  ]
}
```

**Validation:**
- All 4 player UUIDs required and must be distinct
- `games`: 1–3 items, each validated
- `teamAScore`, `teamBScore`: integer, 0–100
- Creator (authenticated user) must be one of the 4 players

**Response:** `201 Created` → `MatchResponse`

```json
{
  "id": "UUID",
  "creatorName": "string",
  "teamAAttackerName": "string",
  "teamADefenderName": "string",
  "teamBAttackerName": "string",
  "teamBDefenderName": "string",
  "status": "PENDING_APPROVAL",
  "games": [
    { "gameNumber": 1, "teamAScore": 10, "teamBScore": 5 }
  ],
  "createdAt": "2026-03-31T12:00:00"
}
```

**Errors:**
- `400` — Validation failed or players not unique
- `404` — One or more participants not found

---

### GET `/matches/pending` — Pending Approvals for Current User

Returns matches where the authenticated user is an opponent and must approve/reject.

**Auth:** Required

**Response:** `200 OK` → `List<MatchResponse>`

---

### PUT `/matches/{id}/approve` — Approve a Pending Match

Transitions match from `PENDING_APPROVAL` → `CONFIRMED`. Calculates winner.

**Auth:** Required
**Path Param:** `id` (UUID)

**Response:** `204 No Content`

**Errors:**
- `400` — User is not an opponent, or match not in `PENDING_APPROVAL` status
- `404` — Match not found

**Concurrency:** Uses optimistic locking (`@Version`) + `@Retryable` (3 attempts, 100ms backoff)

---

### PUT `/matches/{id}/reject` — Reject a Pending Match

Transitions match from `PENDING_APPROVAL` → `DRAFT` for corrections.

**Auth:** Required
**Path Param:** `id` (UUID)

**Response:** `204 No Content`

**Errors:**
- `400` — User is not an opponent, or match not in `PENDING_APPROVAL` status
- `404` — Match not found

---

## Statistics API

**Tag:** Statistics
**Base Path:** `/api/v1/statistics`
**Controller:** `StatisticsController.java`
**API Interface:** `StatisticsApi.java`

### GET `/statistics/leaderboard` — Global Leaderboard

Paginated player rankings with win rate calculations. Uses native SQL with CTEs.

**Auth:** Not required

**Query Parameters:**

| Param | Type | Default | Values |
|-------|------|---------|--------|
| `type` | LeaderboardType | `OVERALL` | `OVERALL`, `ATTACKER`, `DEFENDER` |
| `period` | TimePeriod | `ALL_TIME` | `WEEKLY`, `MONTHLY`, `YEARLY`, `ALL_TIME` |
| `minMatches` | Integer | `0` | ≥ 0 |
| `page` | Integer | `0` | ≥ 0 |
| `size` | Integer | `20` | ≥ 1 |

**Response:** `200 OK` → `Page<LeaderboardEntryResponse>`

```json
{
  "content": [
    {
      "rank": 1,
      "playerId": "UUID",
      "playerName": "string",
      "totalMatches": 42,
      "wins": 30,
      "losses": 12,
      "winRate": 71.43
    }
  ],
  "totalPages": 3,
  "totalElements": 25,
  "size": 10,
  "number": 0
}
```

---

### GET `/statistics/me` — Personal Statistics

Returns authenticated player's stats across all positions.

**Auth:** Required

**Query Parameters:**

| Param | Type | Default |
|-------|------|---------|
| `period` | TimePeriod | `ALL_TIME` |

**Response:** `200 OK` → `PlayerStatsResponse`

```json
{
  "playerId": "UUID",
  "playerName": "string",
  "overall": { "matches": 50, "wins": 30, "losses": 20, "winRate": 60.00 },
  "attacker": { "matches": 25, "wins": 18, "losses": 7, "winRate": 72.00 },
  "defender": { "matches": 25, "wins": 12, "losses": 13, "winRate": 48.00 }
}
```

---

### GET `/statistics/h2h` — Head-to-Head Statistics

Paginated H2H records against specific opponents with positional filters.

**Auth:** Required

**Query Parameters:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `period` | TimePeriod | `ALL_TIME` | Time window filter |
| `myPosition` | LeaderboardType | `OVERALL` | Filter by my role |
| `opponentPosition` | LeaderboardType | `OVERALL` | Filter by opponent role |
| `page` | Integer | `0` | Page number |
| `size` | Integer | `20` | Page size |

**Response:** `200 OK` → `Page<H2HStatsResponse>`

```json
{
  "content": [
    {
      "opponentId": "UUID",
      "opponentName": "string",
      "matches": 15,
      "wins": 9,
      "losses": 6,
      "winRate": 60.00
    }
  ]
}
```

---

## Development Endpoints

> These endpoints exist for development/testing only. Not secured.

### POST `/api/v1/dev/seed` — Seed Test Users

Creates test users and returns their data with JWT tokens.

**Auth:** Not required
**Response:** `200 OK` → `Map<String, Map<String, String>>`

### POST `/api/v1/dev/seed-matches` — Seed Test Matches

Generates sample match data for testing.

**Auth:** Not required
**Response:** `200 OK` → `String`

---

## Error Response Format

All errors return a consistent JSON structure via `GlobalExceptionHandler`:

```json
{
  "error": "Human-readable error message"
}
```

Validation errors include field details:

```json
{
  "error": "Validation failed",
  "details": {
    "teamAAttackerId": "must not be null",
    "games": "size must be between 1 and 3"
  }
}
```

**Error Mapping:**

| Exception | HTTP Status |
|-----------|-------------|
| `ResourceNotFoundException` | 404 |
| `MethodArgumentNotValidException` | 400 |
| `IllegalArgumentException` | 400 |
| `IllegalStateException` | 401 |
| All other exceptions | 500 |

---

## Enums Reference

### MatchStatus
| Value | Description |
|-------|-------------|
| `DRAFT` | Being prepared, not submitted |
| `PENDING_APPROVAL` | Submitted, awaiting opponent confirmation |
| `CONFIRMED` | Verified, affects rankings |

### LeaderboardType
| Value | Description |
|-------|-------------|
| `OVERALL` | All matches regardless of position |
| `ATTACKER` | Only matches played as attacker |
| `DEFENDER` | Only matches played as defender |

### TimePeriod
| Value | Description |
|-------|-------------|
| `WEEKLY` | Last 7 days |
| `MONTHLY` | Last 30 days |
| `YEARLY` | Last 365 days |
| `ALL_TIME` | Since epoch |

### WinnerTeam
| Value | Description |
|-------|-------------|
| `TEAM_A` | Team A won the match |
| `TEAM_B` | Team B won the match |
