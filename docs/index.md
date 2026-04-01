# Project Documentation Index — Tic-Tac-Tore

> Generated: 2026-03-31 | Scan Level: deep | Mode: initial_scan

## Project Overview

- **Type:** Multi-part (Backend + Frontend in single repository)
- **Primary Languages:** Java 21 (Backend), TypeScript (Frontend)
- **Architecture:** Layered REST API + Component-Based SPA

### Quick Reference

#### Backend (backend)

- **Type:** REST API
- **Tech Stack:** Spring Boot 3.4.0, Spring Security, Spring Data JPA, JJWT
- **Root:** `src/`
- **Entry Point:** `src/main/java/com/tictactore/TicTacToreApplication.java`
- **Architecture Pattern:** Layered (Controller → Service → Operation → Entity → Repository)

#### Frontend (frontend)

- **Type:** Single-Page Application
- **Tech Stack:** Vue 3, TypeScript, Pinia, Vue Router, Tailwind CSS, Vite
- **Root:** `frontend/`
- **Entry Point:** `frontend/src/main.ts`
- **Architecture Pattern:** Component-Based SPA (Views → Components → Stores → Services)

---

## Generated Documentation

### Project-Wide

- [Project Overview](./project-overview.md) — Purpose, features, tech stack summary, status
- [Source Tree Analysis](./source-tree-analysis.md) — Annotated directory structure with integration points
- [Integration Architecture](./integration-architecture.md) — Backend ↔ Frontend communication flows
- [Development Guide](./development-guide.md) — Setup, build, test commands, environment variables

### Backend

- [Architecture — Backend](./architecture-backend.md) — Layers, patterns, security, testing strategy
- [API Contracts — Backend](./api-contracts-backend.md) — REST endpoints, request/response schemas, enums
- [Data Models — Backend](./data-models-backend.md) — Database schema, entities, relationships, queries

### Frontend

- [Architecture — Frontend](./architecture-frontend.md) — Components, routing, state management, data fetching
- [Component Inventory — Frontend](./component-inventory-frontend.md) — Vue component catalog with props/emits

---

## Existing Project Documentation

### Product & Design

- [Product Definition](../conductor/product.md) — Vision, audience, strategic goals, core features
- [Product Guidelines](../conductor/product-guidelines.md) — Visual identity, UX philosophy, notification strategy
- [Tech Stack](../conductor/tech-stack.md) — Technology decisions and justifications

### Code Conventions

- [Code Guide](../.gemini/rules/code-guide.md) — 19 mandatory coding conventions (backend)
- [OpenAPI Guide](../.gemini/rules/openapi-guide.md) — OpenAPI documentation rules
- [Test Guide](../.gemini/rules/test-guide.md) — Testing conventions (AAA, naming, structure)
- [Code Review Guide](../.gemini/rules/review-guide.md) — Review checklist (architecture, security, performance)

### Project Management

- [Conductor Hub](../conductor/index.md) — Project management index
- [Workflow](../conductor/workflow.md) — Task tracking and workflow details
- [Tracks](../conductor/tracks.md) — Active and archived work tracks
- [RCA Journal](../conductor/rca-journal.md) — Root cause analysis journal

### Other

- [Frontend README](../frontend/README.md) — Frontend setup instructions
- [Design](../.stitch/DESIGN.md) — Design documentation

---

## Getting Started

### Backend

```bash
./mvnw spring-boot:run
# Starts on http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# Starts on http://localhost:3000
```

### Development Login

Use the navigation bar buttons ("Pavel" / "Account B") to log in with seeded test accounts. These call `POST /api/v1/dev/seed` to create test users and generate JWT tokens automatically.

---

## Metadata

- [Project Parts (JSON)](./project-parts.json) — Machine-readable project structure and integration points
- [Scan Report](./project-scan-report.json) — Workflow state and scan progress
