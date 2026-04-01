# Architecture — Frontend

> Auto-generated: 2026-03-31 | Part: frontend | Tech: Vue 3, TypeScript, Vite

## Executive Summary

The frontend is a single-page application built with Vue 3 (Composition API) and TypeScript. It provides match recording with a multi-step form, an opponent approval dashboard, public leaderboards with filterable rankings, personal statistics with positional analytics, and head-to-head comparisons. Authentication uses Google OAuth2 with JWT tokens stored in localStorage.

---

## Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Framework | Vue 3 (Composition API) | ^3.5.27 |
| Language | TypeScript | ~5.9.3 |
| Build Tool | Vite | ^7.3.1 |
| State Management | Pinia | ^3.0.4 |
| Routing | Vue Router | ^5.0.1 |
| Styling | Tailwind CSS | ^4.1.18 |
| CSS Preprocessor | Sass | ^1.97.3 |
| Unit Testing | Vitest | ^4.0.18 |
| E2E Testing | Playwright | ^1.58.1 |
| Linting | ESLint + Oxlint | ^9.39.2 / ~1.42.0 |
| Formatting | Prettier | 3.8.1 |
| Node | — | ^20.19.0 or ≥22.12.0 |

---

## Architecture Pattern

**Component-Based SPA** with a clear separation:

```
┌─────────────────────────────────────┐
│          Views (Pages)              │
│  Route-based components that        │
│  orchestrate data fetching          │
├─────────────────────────────────────┤
│          Components (UI)            │
│  Reusable, presentational +         │
│  interactive components             │
├─────────────────────────────────────┤
│          Stores (Pinia)             │
│  Centralized state management       │
│  (auth token, user data)            │
├─────────────────────────────────────┤
│          Services (API)             │
│  HTTP client layer for backend      │
│  communication                      │
├─────────────────────────────────────┤
│          Router (Vue Router)        │
│  Route definitions +                │
│  navigation guards                  │
└─────────────────────────────────────┘
```

---

## Routing

### Route Map

| Path | Name | Component | Auth | Description |
|------|------|-----------|------|-------------|
| `/` | `home` | HomeView | Public | Landing page |
| `/login` | `login` | LoginView | Public (redirects if auth) | Google OAuth login |
| `/oauth2/redirect` | `oauth2-redirect` | OAuth2Redirect | Public | OAuth2 callback handler |
| `/dashboard` | `dashboard` | DashboardView | **Required** | Match approvals |
| `/leaderboards` | `leaderboards` | LeaderboardView | Public | Rankings + stats |
| `/dev-recording` | `dev-recording` | DevRecordingView | Public | Match recording (dev) |
| `/token` | `token` | TokenView | Public | JWT debug viewer |
| `/about` | `about` | AboutView | **Required** | Placeholder |

### Navigation Guard

```
beforeEach:
  if (route.meta.requiresAuth && !authStore.isAuthenticated) → redirect /login
  if (route.name === 'login' && authStore.isAuthenticated) → redirect /home
```

---

## State Management

### Auth Store (`stores/auth.ts`)

**Persistence:** localStorage (`token`, `user` keys)

| State | Type | Description |
|-------|------|-------------|
| `token` | `string | null` | JWT token |
| `user` | `User | null` | `{ id, name, email }` |

| Computed | Type | Description |
|----------|------|-------------|
| `isAuthenticated` | `boolean` | `!!token` |

| Action | Description |
|--------|-------------|
| `login(token, user)` | Sets state + persists to localStorage |
| `logout()` | Clears state + removes from localStorage |

---

## Service Layer

### statisticsService.ts

**Base URL:** `VITE_API_BASE_URL` or `http://localhost:8080/api/v1`

**Generic Wrapper:** `apiFetch<T>(endpoint, params, options)`
- Builds query string from params
- Adds `Authorization: Bearer` header
- Supports `AbortSignal` for request cancellation

| Method | Endpoint | Auth | Returns |
|--------|----------|------|---------|
| `getLeaderboard(params)` | `GET /statistics/leaderboard` | Optional | `Page<LeaderboardEntry>` |
| `getPersonalStats(params)` | `GET /statistics/me` | Required | `PlayerStats` |
| `getH2HStats(params)` | `GET /statistics/h2h` | Required | `Page<H2HStats>` |

### Direct Fetch Calls (in Views)

| View | Method | Endpoint |
|------|--------|----------|
| DashboardView | GET | `/matches/pending` |
| DashboardView | PUT | `/matches/{id}/approve` |
| DashboardView | PUT | `/matches/{id}/reject` |
| DevRecordingView | POST | `/matches` |
| DevRecordingView | POST | `/dev/seed` |
| TheNavigation | POST | `/dev/seed` |

---

## Component Architecture

### Active Business Components

| Component | Props | Emits | Description |
|-----------|-------|-------|-------------|
| `TheNavigation` | — | — | Sticky nav bar, auth controls, dev login |
| `MatchRecordingForm` | `availableUsers: User[]` | `submit: {data}` | Player selection with duplicate filtering |
| `MatchScoring` | `players: {...}` | `finish: Game[]` | Game scoring, position management, best-of-3 |
| `PendingApprovals` | `matches: Match[]` | `approve: id`, `reject: id` | Match approval/reject cards |
| `PlayerStatsSummary` | `stats: PlayerStats` | — | Overall + positional stats dashboard |
| `H2HAnalyticsTable` | `h2hRecords: H2HStats[]` | — | Sortable H2H comparison table |

### Unused/Example Components

- `HelloWorld.vue` — Vue example (not referenced)
- `TheWelcome.vue` — Welcome content (not routed)
- `WelcomeItem.vue` — Welcome card layout
- `icons/` — 5 SVG icon components (used only in TheWelcome)

---

## Key UI Flows

### Match Recording Flow (DevRecordingView)

```
Step 1: Player Selection          Step 2: Game Scoring
┌─────────────────────┐          ┌─────────────────────┐
│ MatchRecordingForm   │   emit  │ MatchScoring         │
│                      │─'submit'→│                      │
│ - Select teammate    │          │ - Game 1: Free pos   │
│ - Select opponent 1  │          │ - Game 2: Forced swap│
│ - Select opponent 2  │          │ - Game 3: Free pos   │
│ - Duplicate filter   │          │ - Best-of-3 logic    │
│                      │          │                      │
└─────────────────────┘          │──'finish'──→ POST /matches
                                 └─────────────────────┘
```

### Leaderboard View (Filters + Pagination)

```
┌─────────────────────────────────────────┐
│ LeaderboardView                          │
│                                          │
│ Tabs: [Overall] [Attacker] [Defender] [My Stats]
│ Period: [All Time] [Weekly] [Monthly] [Yearly]
│ Min Matches: [0] [10] [20] [50] [100]   │
│                                          │
│ ┌─ If Leaderboard tab ──────────────┐   │
│ │ Table: Rank|Player|W|L|Win Rate   │   │
│ │ Pagination: < 1 2 3 >             │   │
│ └───────────────────────────────────┘   │
│                                          │
│ ┌─ If My Stats tab ─────────────────┐   │
│ │ PlayerStatsSummary component       │   │
│ │ H2H Filters: myPos / opponentPos  │   │
│ │ H2HAnalyticsTable component        │   │
│ │ H2H Pagination                     │   │
│ └───────────────────────────────────┘   │
└──────────────────────────────────────────┘
```

---

## Data Fetching Patterns

1. **AbortController** — `LeaderboardView` cancels stale requests when filters change
2. **Promise.all** — Parallel fetching of personal stats + H2H data
3. **Watchers** — Vue `watch()` on filter refs triggers `fetchData()`
4. **Optimistic UI** — `DashboardView` removes approved/rejected matches from local array without refetching

---

## Authentication Architecture

```
localStorage
  ├── token: "eyJhbGciOiJIUzI1NiJ9..."
  └── user: '{"id":"...","name":"...","email":"..."}'
       │
       ▼
  Pinia Auth Store
  ├── token: Ref<string | null>
  ├── user: Ref<User | null>
  └── isAuthenticated: Computed<boolean>
       │
       ├──► Router Guard (beforeEach)
       │    Checks meta.requiresAuth
       │
       └──► API Requests
            Authorization: Bearer <token>
```

---

## Styling

- **Framework:** Tailwind CSS (via `@tailwindcss/vite` plugin)
- **Color palette:** Indigo (primary), Emerald/Green (success), Red (error), Gray (neutral), Amber (accent)
- **Responsive:** sm/md/lg breakpoints
- **Design tokens:** Rounded corners (`rounded-xl`), shadows, consistent spacing

---

## Testing Strategy

### Unit Tests (Vitest + jsdom)

| Area | Files | Description |
|------|-------|-------------|
| Components | 6 specs | H2HAnalyticsTable, HelloWorld, MatchRecordingForm, MatchScoring, PendingApprovals, PlayerStatsSummary |
| Views | 5 specs | DashboardView, HomeView, LeaderboardView, LoginView, OAuth2Redirect |
| Services | 1 spec | statisticsService |
| Stores | 1 spec | auth store |
| Router | 1 spec | Navigation guard |

### E2E Tests (Playwright)

| File | Description |
|------|-------------|
| `auth.spec.ts` | Authentication flow |
| `vue.spec.ts` | General Vue app tests |

---

## Build & Development

```bash
# Install
cd frontend && npm install

# Dev server (port 3000)
npm run dev

# Build (type-check + vite build)
npm run build

# Unit tests
npm run test:unit

# E2E tests
npm run test:e2e

# Lint (ESLint + Oxlint)
npm run lint

# Format
npm run format
```

---

## Package Structure

```
frontend/src/
├── main.ts              # App bootstrap
├── App.vue              # Root layout
├── assets/              # Global CSS
├── components/          # Reusable UI
├── views/               # Route pages
├── router/              # Route config + guards
├── stores/              # Pinia stores
└── services/            # API client
```
