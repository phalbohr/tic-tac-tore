# Project Overview — Tic-Tac-Tore

> Auto-generated: 2026-03-31 | Source: Deep Scan

## What Is This?

**Tic-Tac-Tore** is a foosball (table football) statistics platform for office environments. Players record 2v2 match results, opponents verify scores through an approval workflow, and the platform tracks comprehensive statistics including positional performance (Attacker/Defender), head-to-head records, and public leaderboards.

---

## Purpose & Vision

- **Objective Rivalry Resolution:** Use data to settle office debates about who is the best player
- **Cultural Catalyst:** Foster office community through friendly competition
- **Personal Mastery:** Provide clear insights into skill progression and performance patterns

---

## Core Features

| Feature | Description |
|---------|-------------|
| **Match Recording** | 2v2 matches with position tracking (Attacker/Defender) |
| **Match Confirmation Workflow** | Opponents must verify results (PENDING_APPROVAL → CONFIRMED) |
| **Position Rotation Rules** | Game 1: free, Game 2: mandatory swap, Game 3: free |
| **Public Leaderboards** | Overall, Attacker, and Defender rankings |
| **Personal Statistics** | Win rate by position with period filtering |
| **Head-to-Head Analytics** | Performance against specific opponents with positional filters |
| **Google OAuth2 Login** | Secure authentication via Google accounts |

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

- Match recording and approval workflow functional
- Leaderboards with positional filtering operational
- Personal stats and H2H analytics implemented
- No CI/CD or Docker configuration yet
- No database migration framework (Hibernate auto-DDL)
- Development-oriented auth flow (seed endpoints for testing)

---

## Getting Started

1. **Backend:** `./mvnw spring-boot:run` (starts on port 8080)
2. **Frontend:** `cd frontend && npm install && npm run dev` (starts on port 3000)
3. **Login:** Use dev buttons in navigation bar, or set up Google OAuth2 credentials
4. **Explore API:** Visit `http://localhost:8080/swagger-ui.html`
