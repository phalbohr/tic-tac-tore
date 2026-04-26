# Project Overview — Tic-Tac-Tore

> Auto-generated: 2026-03-31 | Updated: 2026-04-03 | Source: Deep Scan + Design Specs

## What Is This?

**Tic-Tac-Tore** is a foosball (table football) statistics and tournament platform for office environments. The full product vision includes match recording (1v1 and 2v2), multiple rule systems (ITSF, DTFB, Custom), tournament management, real-time match tracking, team management, and comprehensive positional statistics — all with a mobile-first design optimized for use at the foosball table.

---

## Purpose & Vision

- **Objective Rivalry Resolution:** Use data to settle office debates about who is the best player
- **Cultural Catalyst:** Foster office community through friendly competition
- **Personal Mastery:** Provide clear insights into skill progression and performance patterns

---

## Full Product Scope (Target)

The complete product vision is defined in the [Design Description](../_project-spec/DESIGN/project-design-description.md). Key planned screens and features:

### Screens

| Screen | Orientation | Description |
|--------|------------|-------------|
| **Home Hub** | Portrait | 6 main buttons: New Match, Teams & Rules, Played Matches, Statistics, Tournament, Want to Play |
| **Personal Cabinet** | Portrait | Avatar, nickname (change limit: 1/month), language selection (EN/DE) |
| **Want to Play** | Portrait | Create/join match pools (1v1/2v2), scheduled or fill-based start |
| **Teams & Rules** | Portrait | Favorite players, saved teams, custom rule templates, defaults |
| **New Match** | Portrait | Standalone or tournament match; rule system selection; team setup |
| **Results Entry** | Landscape | Retrospective score entry on kicker table top-view UI |
| **Live Match** | Landscape | Real-time goal tracking with per-goal positional attribution |
| **Played Matches** | Portrait | Match history, approval queue, all/favorites filter |
| **Statistics** | Portrait | Individual, team, and H2H stats with extensive positional breakdowns |
| **Tournaments** | Portrait | Create/register tournaments (cup/championship, 1v1/2v2, mixed) |

### Rule Systems

| System | Description |
|--------|-------------|
| **ITSF** | International standard — Best of 3/5, winner-only scoring |
| **DTFB** | German Bundesliga — Exactly 2 games, 1 point per game won |
| **Custom** | Configurable: win condition, score limit, tie-break, point distribution, position swap rules |

### Match Modes

| Mode | Description |
|------|-------------|
| **Results Entry** | Post-game retrospective entry; tracks game-level results and positions |
| **Live Match** | Real-time goal-by-goal tracking; per-goal positional attribution for detailed stats |

### Tournament System

- Formats: Cup (knockout) or Championship (round-robin with optional playoffs)
- Modes: Personal 1v1, Team 2v2 (fixed pairs), Personal 2v2 (random pairings from pool)
- Configurable rules, min/max participants, scheduling
- Tournament stats roll into global statistics while preserving tournament-specific history

---

## Implementation Status

> The current codebase is an early MVP/prototype. Most implemented features deviate from the target [Design Description](../_project-spec/DESIGN/project-design-description.md) and will require significant rework to match the full spec.

### Implemented (MVP — Needs Rework to Match Spec)

| Feature | MVP State | Gap vs Target Spec |
|---------|-----------|-------------------|
| **2v2 Match Recording** | Basic form, hardcoded best-of-3 | Target: configurable rules (ITSF/DTFB/Custom), score limits, win-by-2 tie-break; kicker table top-view UI; both Results Entry and Live Match modes |
| **Position Rotation** | Hardcoded: Game 1 free, Game 2 mandatory swap, Game 3 free | Target: mandatory swap is only one option in Custom Rules; other rules allow free swap between games or within games |
| **Match Confirmation** | PENDING_APPROVAL → CONFIRMED/DRAFT, single opponent confirms | Target: if neutral player submits, both teams must confirm; rejection sends notification to creator |
| **Public Leaderboards** | Overall/Attacker/Defender rankings, period filter, min matches | Target: must support rule system filter (ITSF/DTFB/Custom), 1v1/2v2 separate stats, tournament filter, many more statistical columns (points, draws, goals scored/conceded by position) |
| **Personal Statistics** | Overall/Attacker/Defender win rates | Target: detailed bar visualizations (matches W/D/L, games by position, goals scored/conceded by position), points system, separate 1v1 and 2v2 stats |
| **H2H Analytics** | Opponent list with W/L/WinRate | Target: 3 separate tables (Matches with/vs, Games with/vs, Goals with detailed positional cross-breakdowns: attacker-vs-defender, etc.) |
| **Google OAuth2 Login** | Working JWT-based auth | Mostly complete; target adds personal cabinet (avatar, nickname, language) |

### Not Started

| Feature | Description |
|---------|-------------|
| 1v1 Matches | Separate match format with different UI layout (2 players, not 4) |
| Multiple Rule Systems | ITSF, DTFB, Custom rules with configurable parameters |
| Live Match Mode | Real-time goal-by-goal tracking with per-goal positional attribution |
| Results Entry UI | Kicker table top-view interface (landscape orientation on mobile) |
| Tournament System | Cup (knockout) / Championship (round-robin), 3 tournament modes |
| Want to Play Pools | Matchmaking pools with scheduled or fill-based start |
| Teams & Favorites | Saved player pairs, favorite players, default team/rules |
| Team Statistics | Pair-level stats (not just individual), filterable by player |
| Personal Cabinet | Avatar, nickname (1 change/month), language selection |
| Notifications | Approval requests, pool alerts, tournament invites |
| i18n (German) | Interface localization (EN → DE) |
| Dark Design Theme | Target design system (green/red/yellow dark theme) not applied |
| Home Hub Screen | 6-button main screen (currently using generic landing page) |

---

## Design System

Defined in [DESIGN.md](../_project-spec/DESIGN/DESIGN.md):

- **Color mode:** Dark
- **Primary:** `#2d5a27` (green — foosball table)
- **Secondary:** `#a64d32` (red — player figures)
- **Tertiary:** `#f1c40f` (yellow — player figures)
- **Neutral:** `#2b2624` (warm brown — brick wall)
- **Headlines:** Space Grotesk
- **Body/Labels:** Manrope
- **Roundedness:** Moderate (level 2)
- **UI Mockups:** Available in `_project-spec/DESIGN/stitch_tic_tac_tore/` (Google Stitch)

> **Note:** The current frontend uses Tailwind CSS with a light Indigo/Gray theme. The dark warm design system from the specs has not yet been implemented.

---

## Tech Stack Summary

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 21, Spring Boot 3.4.0, Spring Security, Spring Data JPA |
| **Frontend** | Vue 3, TypeScript, Pinia, Vue Router, Tailwind CSS |
| **Database** | H2 (dev) / PostgreSQL (prod, Supabase-compatible) |
| **Auth** | Google OAuth2 → Backend-issued JWT |
| **Build** | Maven (backend), Vite (frontend) |
| **Testing** | JUnit 5 + JaCoCo (backend), Vitest + Playwright (frontend) |
| **API Docs** | SpringDoc OpenAPI + Swagger UI |

---

## Repository Structure

**Type:** Multi-part (Backend + Frontend in single repository)

| Part | Root | Type | Stack |
|------|------|------|-------|
| Backend | `src/` | REST API | Spring Boot 3.4.0, Java 21 |
| Frontend | `frontend/` | SPA | Vue 3, TypeScript, Vite |

---

## Architecture Highlights

- **Stateless auth:** JWT tokens, no server-side sessions
- **Three-Layer Transaction Architecture:** `@Retryable` (service) → `@Transactional` (operation) → Entity (domain logic)
- **Domain-driven entities:** Business logic inside JPA entities (Tell, Don't Ask)
- **Optimistic locking:** Match approval uses `@Version` + retry
- **Hybrid Split OpenAPI:** Documentation in interfaces, routing in controllers
- **Component-based frontend:** Vue 3 Composition API with Pinia stores

---

## Documentation Index

| Document | Description |
|----------|-------------|
| [Architecture — Backend](./architecture-backend.md) | Backend layers, patterns, security |
| [Architecture — Frontend](./architecture-frontend.md) | Frontend components, routing, state |
| [API Contracts](./api-contracts-backend.md) | REST endpoints, request/response schemas |
| [Data Models](./data-models-backend.md) | Database schema, entities, relationships |
| [Component Inventory](./component-inventory-frontend.md) | Vue components catalog |
| [Integration Architecture](./integration-architecture.md) | Frontend ↔ Backend communication |
| [Source Tree Analysis](./source-tree-analysis.md) | Annotated directory structure |
| [Development Guide](./development-guide.md) | Setup, build, test commands |

---

## Project Status

### Completed Phases

1. **Phase 1:** Backend Statistics Calculation & APIs
2. **Phase 2:** Public Leaderboards & Comprehensive Player Statistics

### Current State

- MVP prototype with basic 2v2 match recording and approval
- Leaderboards, personal stats, H2H analytics present but simplified vs target spec
- No CI/CD, Docker, or database migration framework
- Frontend uses placeholder Tailwind light theme (target dark design system not applied)
- Significant rework needed on existing features + large surface of new features to build

---

## Getting Started

1. **Backend:** `./mvnw spring-boot:run` (starts on port 8080)
2. **Frontend:** `cd frontend && npm install && npm run dev` (starts on port 3000)
3. **Login:** Use dev buttons in navigation bar, or set up Google OAuth2 credentials
4. **Explore API:** Visit `http://localhost:8080/swagger-ui.html`
