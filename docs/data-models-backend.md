# Data Models — Backend

> Auto-generated: 2026-03-31 | Source: Deep Scan of JPA Entities

## Overview

The database schema consists of 3 tables managed by Spring Data JPA with Hibernate auto-DDL. No explicit migration framework (Flyway/Liquibase) is currently used.

**Database:** H2 (development) / PostgreSQL (production, Supabase-compatible)

---

## Entity Relationship Diagram

```
┌─────────────┐       ┌──────────────────────┐       ┌─────────────┐
│   users      │       │      matches          │       │    games     │
├─────────────┤       ├──────────────────────┤       ├─────────────┤
│ id (PK,UUID)│◄──┐   │ id (PK, UUID)         │   ┌──►│ id (PK,UUID)│
│ email       │   ├───│ creator_id (FK)        │   │   │ game_number │
│ name        │   ├───│ team_a_attacker_id(FK) │   │   │ team_a_score│
│ provider_id │   ├───│ team_a_defender_id(FK) │   │   │ team_b_score│
└─────────────┘   ├───│ team_b_attacker_id(FK) │   │   │ match_id(FK)│
                  └───│ team_b_defender_id(FK) │   │   └─────────────┘
                      │ status                  │   │
                      │ winner                  │   │
                      │ version                 │   │
                      │ created_at              │   │
                      │ games ──────────────────┼───┘
                      └──────────────────────────┘
```

---

## Table: `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, auto-generated | Unique player identifier |
| `email` | `VARCHAR` | UNIQUE, NOT NULL, Email format | OAuth2 email (login identity) |
| `name` | `VARCHAR` | NOT NULL | Display name |
| `provider_id` | `VARCHAR` | NULLABLE | Google OAuth2 provider ID |

**JPA Entity:** `User.java`
**Lombok:** `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

---

## Table: `matches`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, auto-generated | Unique match identifier |
| `version` | `BIGINT` | Optimistic lock (`@Version`) | Concurrency control counter |
| `creator_id` | `UUID` | FK → users.id, NOT NULL | User who recorded the match |
| `team_a_attacker_id` | `UUID` | FK → users.id, NOT NULL | Team A attacker player |
| `team_a_defender_id` | `UUID` | FK → users.id, NOT NULL | Team A defender player |
| `team_b_attacker_id` | `UUID` | FK → users.id, NOT NULL | Team B attacker player |
| `team_b_defender_id` | `UUID` | FK → users.id, NOT NULL | Team B defender player |
| `status` | `VARCHAR` (enum) | NOT NULL, default `PENDING_APPROVAL` | Match lifecycle state |
| `winner` | `VARCHAR` (enum) | NULLABLE | Calculated match winner (`TEAM_A` / `TEAM_B`) |
| `created_at` | `TIMESTAMP` | NOT NULL, immutable | Auto-set creation time |

**JPA Entity:** `Match.java`
**Fetch Strategy:** All user relationships are `LAZY`
**Cascade:** `games` → `CascadeType.ALL`, `orphanRemoval = true`
**Lombok:** `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

### Business Logic (Domain Methods)

| Method | Description |
|--------|-------------|
| `calculateWinner()` | Determines winner: first team to win 2 games |
| `approve()` | PENDING_APPROVAL → CONFIRMED + calculates winner |
| `reject()` | PENDING_APPROVAL → DRAFT |
| `isUserInTeamA(User)` | Checks if user is attacker or defender on Team A |
| `isUserInTeamB(User)` | Checks if user is attacker or defender on Team B |
| `isAttacker(User)` | Checks if user is attacker on either team |
| `isDefender(User)` | Checks if user is defender on either team |
| `isWinner(User)` | Determines if user is on the winning team |
| `addGame(Game)` | Bidirectional association helper |

---

## Table: `games`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, auto-generated | Unique game identifier |
| `game_number` | `INTEGER` | NOT NULL, 1–3 | Sequence within match |
| `team_a_score` | `INTEGER` | NOT NULL, ≥ 0, default 0 | Team A score |
| `team_b_score` | `INTEGER` | NOT NULL, ≥ 0, default 0 | Team B score |
| `match_id` | `UUID` | FK → matches.id | Parent match |

**JPA Entity:** `Game.java`
**Fetch Strategy:** `match` → `LAZY`
**Lombok:** `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

---

## Enums

### MatchStatus

```java
public enum MatchStatus {
    DRAFT,              // Not submitted
    PENDING_APPROVAL,   // Awaiting opponent confirmation
    CONFIRMED           // Verified, counts in statistics
}
```

### WinnerTeam

```java
public enum WinnerTeam {
    TEAM_A,
    TEAM_B
}
```

---

## Repository Layer

### MatchRepository

**Extends:** `JpaRepository<Match, UUID>`

| Method | Type | Description |
|--------|------|-------------|
| `findPendingApprovalsForUser(userId, status)` | JPQL | Matches where user is opponent of creator |
| `findAllConfirmedMatchesForUser(userId)` | JPQL | All confirmed matches for a player |
| `findLeaderboard(type, since, minMatches, pageable)` | Native SQL (CTE) | Paginated leaderboard rankings |
| `findH2HStats(userId, since, myPosition, opponentPosition, pageable)` | Native SQL (CTE) | Head-to-head stats with positional filters |
| `findPlayerStats(userId, since)` | Native SQL (CTE) | Aggregate player statistics by position |

### UserRepository

**Extends:** `JpaRepository<User, UUID>`

| Method | Type | Description |
|--------|------|-------------|
| `findByEmail(email)` | Derived | Lookup user by email (OAuth2 identity) |

---

## Projection Interfaces

### LeaderboardProjection

```java
String getPlayerId();
String getPlayerName();
Long getTotalMatches();
Long getWins();
Long getLosses();
Double getWinRate();
```

### H2HProjection

```java
String getOpponentId();
String getOpponentName();
Long getTotalMatches();
Long getWins();
Long getLosses();
Double getWinRate();
```

### PlayerStatsProjection

```java
Long getOverallMatches();
Long getOverallWins();
Long getAttackerMatches();
Long getAttackerWins();
Long getDefenderMatches();
Long getDefenderWins();
```

---

## Migration Strategy

Currently using Hibernate auto-DDL (`spring.jpa.hibernate.ddl-auto` — implied from H2 in-memory setup). No migration framework configured. For production deployment with PostgreSQL, a migration strategy (Flyway or Liquibase) should be implemented.

---

## Concurrency Control

- **Match entity** uses `@Version` (optimistic locking) with `Long` wrapper type
- **MatchService** wraps approve/reject in `@Retryable` (3 attempts, 100ms backoff)
- **MatchOperation** handles transactional operations (`@Transactional`) separately from retry logic
- This follows the Three-Layer Transaction Architecture pattern from the project's code guide
