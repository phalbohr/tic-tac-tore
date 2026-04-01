# Component Inventory — Frontend

> Auto-generated: 2026-03-31 | Source: Deep Scan of Vue 3 Components

## Overview

The frontend contains 14 Vue components: 6 active business components, 3 unused example/template components, and 5 SVG icon components.

---

## Active Business Components

### TheNavigation.vue

**Category:** Layout / Navigation
**Location:** `frontend/src/components/TheNavigation.vue`

- Sticky top navigation bar with shadow
- Left side: Navigation links (Home, New Match, Dashboard, Leaderboard, Token)
- Right side: Auth controls (login as dev users or logout)
- On mount: Seeds test data via `POST /api/v1/dev/seed`
- Methods: `loginAs(key)`, `handleLogout()`

---

### MatchRecordingForm.vue

**Category:** Form / Input
**Location:** `frontend/src/components/MatchRecordingForm.vue`

- Player selection form for 2v2 match setup (Step 1 of recording flow)
- 3 select dropdowns: Teammate, Opponent 1, Opponent 2
- Dynamic filtering prevents selecting the same player twice
- Props: `availableUsers: User[]`
- Emits: `submit` with `{ teammateId, opponent1Id, opponent2Id }`
- Computed validation: All 3 fields must be selected

---

### MatchScoring.vue

**Category:** Form / Game Logic
**Location:** `frontend/src/components/MatchScoring.vue`

- Game scoring interface for best-of-3 format (Step 2 of recording flow)
- Enforces position rotation rules:
  - Game 1: Free position selection
  - Game 2: Mandatory position swap (auto-computed)
  - Game 3: Free position selection
- Score inputs (0-99) for each team per game
- Tracks game progression and determines when match can finish
- Props: `players: { creator, teammate, opponent1, opponent2 }`
- Emits: `finish` with `Game[]`

---

### PendingApprovals.vue

**Category:** Data Display / Actions
**Location:** `frontend/src/components/PendingApprovals.vue`

- Card-based layout for pending match approvals
- Displays: Creator name, timestamp, team compositions, game scores
- Action buttons: Approve (emerald) and Reject (red) per match
- Empty state message when no pending approvals
- Props: `matches: Match[]`
- Emits: `approve: matchId`, `reject: matchId`

---

### PlayerStatsSummary.vue

**Category:** Data Display / Analytics
**Location:** `frontend/src/components/PlayerStatsSummary.vue`

- Personal statistics dashboard (pure presentational)
- Overall section: 4-column grid (Matches, Wins, Losses, Win Rate)
- Attacker stats card with progress bar visualization
- Defender stats card with progress bar visualization
- Props: `stats: PlayerStats`

---

### H2HAnalyticsTable.vue

**Category:** Data Display / Analytics
**Location:** `frontend/src/components/H2HAnalyticsTable.vue`

- Sortable head-to-head comparison table
- Columns: Opponent, Matches, Wins, Losses, Win Rate
- Client-side sorting (ascending/descending toggle per column)
- Win Rate displayed as styled badge (indigo)
- `aria-sort` attributes for accessibility
- Empty state: "No head-to-head records yet"
- Props: `h2hRecords: H2HStats[]`

---

## Unused / Example Components

### HelloWorld.vue

**Category:** Example
**Location:** `frontend/src/components/HelloWorld.vue`
- Vue scaffolding example component
- Props: `msg: string`
- Not referenced in any active view

### TheWelcome.vue

**Category:** Example
**Location:** `frontend/src/components/TheWelcome.vue`
- Welcome page with 5 WelcomeItem sections
- References Documentation, Tooling, Ecosystem, Community, Support
- Not routed or referenced from active views

### WelcomeItem.vue

**Category:** Layout (Example)
**Location:** `frontend/src/components/WelcomeItem.vue`
- Layout component with icon, heading, and default slots
- Used only by TheWelcome.vue

---

## Icon Components

**Location:** `frontend/src/components/icons/`

| Component | Description |
|-----------|-------------|
| `IconCommunity.vue` | Community/people SVG icon |
| `IconDocumentation.vue` | Document SVG icon |
| `IconEcosystem.vue` | Ecosystem/globe SVG icon |
| `IconSupport.vue` | Support/heart SVG icon |
| `IconTooling.vue` | Tooling/wrench SVG icon |

All are simple SVG wrappers with no props. Used only by `TheWelcome.vue`.

---

## View Components

| View | Route | Auth | Key Components Used |
|------|-------|------|-------------------|
| `HomeView.vue` | `/` | Public | — (standalone) |
| `LoginView.vue` | `/login` | Public | — (standalone) |
| `OAuth2Redirect.vue` | `/oauth2/redirect` | Public | — (redirect handler) |
| `DashboardView.vue` | `/dashboard` | Required | PendingApprovals |
| `LeaderboardView.vue` | `/leaderboards` | Public | PlayerStatsSummary, H2HAnalyticsTable |
| `DevRecordingView.vue` | `/dev-recording` | Public | MatchRecordingForm, MatchScoring |
| `TokenView.vue` | `/token` | Public | — (standalone) |
| `AboutView.vue` | `/about` | Required | — (placeholder) |

---

## Component Dependency Graph

```
App.vue
├── TheNavigation (always rendered)
└── RouterView
    ├── HomeView
    ├── LoginView
    ├── OAuth2Redirect
    ├── DashboardView
    │   └── PendingApprovals
    ├── LeaderboardView
    │   ├── PlayerStatsSummary
    │   └── H2HAnalyticsTable
    ├── DevRecordingView
    │   ├── MatchRecordingForm
    │   └── MatchScoring
    ├── TokenView
    └── AboutView
```

---

## TypeScript Interfaces

### Shared Types (from statisticsService.ts)

```typescript
type LeaderboardType = 'OVERALL' | 'ATTACKER' | 'DEFENDER'
type TimePeriod = 'WEEKLY' | 'MONTHLY' | 'YEARLY' | 'ALL_TIME'

interface LeaderboardEntry {
  rank: number; playerId: string; playerName: string
  totalMatches: number; wins: number; losses: number; winRate: number
}

interface PositionStats {
  matches: number; wins: number; losses: number; winRate: number
}

interface PlayerStats {
  playerId: string; playerName: string
  overall: PositionStats; attacker: PositionStats; defender: PositionStats
}

interface H2HStats {
  opponentId: string; opponentName: string
  matches: number; wins: number; losses: number; winRate: number
}

interface Page<T> {
  content: T[]; totalPages: number; totalElements: number
  size: number; number: number
}
```
