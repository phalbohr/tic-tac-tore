---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
workflowType: 'architecture'
project_name: 'tic-tac-tore'
user_name: 'Pavel'
date: '2026-04-27'
lastStep: 8
status: 'complete'
completedAt: '2026-04-27'
---


# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**
The core of the system is based on 60 requirements covering the match lifecycle from creation to publication in statistics. Architecturally, this requires a robust state machine for matches and a flexible rule engine that influences input validation and analytics calculations.

**Non-Functional Requirements:**
- **Performance:** First Contentful Paint < 1.5s, match entry < 10s. Requires frontend optimization and lightweight API responses.
- **Security:** Google OAuth2, stateless JWT (24h expiry), strict authorization (confirmations restricted to opponents).
- **Compliance:** GDPR-by-design (pseudonymization, account deletion with anonymized history preservation).

**Scale & Complexity:**
- Primary domain: Full-stack (Spring Boot + Vue 3 PWA)
- Complexity level: High (domain-driven complexity)
- Estimated architectural components: ~12-15 core backend services + ~37 frontend components.

### Technical Constraints & Dependencies
- **Brownfield:** Requires refactoring of existing logic (3 tables, 7 endpoints) to meet the new specification.
- **PWA APIs:** Dependency on browser support for Push API and Wake Lock.
- **Stateless Backend:** Use of JWT without server-side sessions for horizontal scalability.

### Cross-Cutting Concerns Identified
- **Rule Engine Integration:** Rules permeate the system from UI to DB. Use of immutable models for `RuleConfiguration`.
- **Data Integrity Pipeline:** The confirmation workflow is a critical failure point. Requires isolation from the Analytics Engine.
- **Internationalization (i18n):** EN/DE support from day one.

## Starter Template Evaluation

### Primary Technology Domain
**Full-stack (Spring Boot + Vue 3 PWA)** with an extended toolkit for layout and quality control.

### Selected Starter: Custom Layered Monolith (Spring Boot 4.0 + Vite 8)

**Rationale for Selection:**
A clean architectural foundation focusing on strong typing, deep test coverage control (JaCoCo), and structured styling (SCSS + Tailwind). Use of Pinia for managing complex match states.

**Initialization Commands:**

```bash
# Backend (Spring Initializr + JaCoCo)
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,postgresql,security,oauth2-client,validation,actuator \
  -d javaVersion=21 -d bootVersion=4.0.6 -d type=maven-project \
  -d name=tic-tac-tore-api -o backend.zip

# Frontend (Vite + Vue + Pinia + SCSS)
npm create vite@latest frontend -- --template vue-ts
cd frontend && npm install pinia sass-embedded
npm install -D vite-plugin-pwa tailwindcss @tailwindcss/vite
```

**Architectural Decisions Provided by Starter:**

**Language & Runtime:**
Java 21 (LTS) and TypeScript 7.0.

**Styling Solution:**
**Tailwind CSS v4 + SCSS**: Tailwind handles utility classes (layout, spacing). SCSS handles complex visual states and animations using the **`ch-`** (Clubhouse) prefix for style isolation.

**State Management:**
**Pinia**: Source of truth for `MatchDraft`, `VerificationQueue`, and `UserStats`.

**Testing & Quality:**
**JUnit 5 + AssertJ + JaCoCo**: Full backend test cycle with coverage analysis. **Vitest + Playwright** for frontend.

**Build Tooling:**
Maven and Vite 8 (Rolldown).

## Core Architectural Decisions

### Data Architecture
- **AD-01: Immutable RuleConfiguration:** All rule sets (ITSF, DTFB, Custom) are stored as immutable records. Any change in group settings creates a new `rule_config_id`. Rationale: Prevents historical statistics drift and ensures data integrity.
- **AD-02: Isolated Verification Pipeline:** Match state machine (`PENDING` -> `CONFIRMED` -> `PUBLISHED`) is handled by a dedicated service. Rationale: Protects the Analytics Engine from "dirty" data; statistics only query `PUBLISHED` matches.

### Security & Authentication
- **AD-03: Stateless JWT with Redis Denylist:** Authentication via Google OAuth2 with 24h JWT tokens. Immediate token revocation (e.g., on account deletion) is handled via a Redis-based denylist with Bloom filters for performance. Rationale: Standard industry practice for scalable stateless security.
- **AD-04: GDPR Compliance via Pseudonymization:** On account deletion, user identity is replaced by a permanent anonymous placeholder (`ex-player-UUID`), preserving the match graph while removing PII.

### Analytics & UI
- **AD-05: 3-Tier Statistics Model:**
  - Tier 1 (Universal): Wins/Losses/Draws across all matches.
  - Tier 2 (Conditional): Positional stats (Attacker/Defender) aggregated where rules permit.
  - Tier 3 (Exact): Deep metrics specific to a single `rule_config_id`.
- **AD-06: PWA-First Infrastructure:** Use of Service Workers for Push API (match confirmations) and Screen Wake Lock API (Live Mode). Rationale: High-quality app experience without app store overhead.

## Implementation Patterns & Consistency Rules

### Naming Conventions
- **Database:** Tables in `snake_case` and plural (e.g., `matches`, `rule_configurations`). Columns in `snake_case`.
- **API:** REST endpoints in `kebab-case` and plural (e.g., `/api/v1/match-drafts`). Path parameters as `:id`.
- **Frontend Code:** Vue components in `PascalCase` (`MatchCard.vue`), functions and variables in `camelCase`.
- **Backend Code:** Standard Java `PascalCase` for classes and `camelCase` for methods/variables.
- **Data Formats:** All JSON fields must use `camelCase` for seamless JS/TS integration.

### Structure & Organization
- **Feature-Based Layout:** Components and logic grouped by business feature (e.g., `features/match/`), not by technical type.
- **Test Co-location:** Unit tests (`*.spec.ts` or Java equivalents) must reside next to the code they test. E2E tests live in a dedicated `e2e/` directory.
- **The 500-Line Rule (IP-04):** No source file or test class should exceed 500 lines. If this limit is reached, the file MUST be split by functional methods or sub-features.
- **Automated Enforcement:** Build-time checks via **Checkstyle** (Java `FileLength`) and **ESLint** (`max-lines`) enforce this limit. The build must fail if any file exceeds the threshold.

### State & Communication
- **Pinia Stores:** Named as `use[Name]Store`. Actions for state mutations, getters for derived state.
- **Event Prefixing:** Custom component events must use the `ch:` prefix (e.g., `@ch:goal-recorded`).
- **SCSS Prefixing:** All unique Clubhouse styles must use the `ch-` prefix to avoid utility class conflicts.

### Standardized Formats
- **Date/Time:** ISO 8601 strings only (`YYYY-MM-DDTHH:mm:ssZ`).
- **Errors:** Standard error object: `{ "code": "ERROR_CODE", "message": "Human readable", "details": {} }`.

## Project Structure & Boundaries

### Complete Project Directory Structure

```text
tic-tac-tore/
├── pom.xml                     # Maven config (JaCoCo, Checkstyle, Spring Boot 4.0)
├── docker-compose.yml           # Infrastructure: PostgreSQL, Redis
├── src/
│   ├── main/
│   │   ├── java/com/itemis/tictactore/
│   │   │   ├── api/            # REST Controllers, DTOs, Exception Handlers
│   │   │   ├── service/        # Business Logic (Feature-based: match, analytics, etc.)
│   │   │   ├── domain/         # Immutable Entities & Value Objects (RuleConfiguration)
│   │   │   ├── repository/     # Spring Data JPA Repositories
│   │   │   └── config/         # Security (JWT), PWA, Redis, Quality Rules
│   │   └── resources/
│   │       └── db/migration/    # Database versioning (Flyway)
│   └── test/java/...           # Co-located unit/integration tests (<500 lines rule)
├── frontend/
│   ├── package.json
│   ├── vite.config.ts          # Vite 8 + Rolldown + PWA Plugin
│   ├── tailwind.config.js      # Stitch design tokens
│   ├── src/
│   │   ├── features/           # Domain-specific logic & components
│   │   │   ├── match/          # Match recording & Live Mode
│   │   │   ├── stats/          # 3-tier analytics
│   │   │   └── profile/        # User identity & settings
│   │   ├── core/               # Shared cross-cutting concerns
│   │   │   ├── components/     # Shared UI Atoms (ch- prefixed primitives)
│   │   │   ├── api/            # Centralized API client & interceptors
│   │   │   └── utils/          # Formatting, date helpers, etc.
│   │   ├── assets/             # SCSS (ch- prefix), global typography
│   │   ├── App.vue             # Root component
│   │   └── main.ts             # Entry point (Pinia, Router initialization)
│   └── e2e/                    # Playwright end-to-end tests
└── docs/                       # Architecture & API specifications
```

### Architectural Boundaries

- **API Boundary:** External access strictly via `/api/v1/`. Stateless JWT validation at the security filter layer.
- **Component Boundary:** Shared UI components in `core/components/` are domain-agnostic. Feature components in `features/` handle business logic and use core primitives.
- **Style Boundary:** All custom Clubhouse styles must use the `ch-` prefix to maintain isolation from Tailwind utility classes.
- **Data Boundary:** `RuleConfiguration` and confirmed `Match` entities are immutable. Direct DB access is restricted to the repository layer.

## Architecture Validation Results

### Coherence Validation ✅
All technology choices (Spring Boot 4.0, Vite 8, Redis) are compatible. The layered architecture effectively isolates the immutable Domain layer from the API and Infrastructure concerns. Decision compatibility is verified, and patterns align with the chosen technology stack.

### Requirements Coverage Validation ✅
- **Functional:** Full match lifecycle, verification pipeline, and 3-tier analytics model are architecturally supported by AD-01, AD-02, and AD-05.
- **Non-Functional:** Performance (sub-10s entry) and Security (stateless JWT) requirements are met via PWA-first approach and Redis-based denylist.
- **Compliance:** GDPR is fully addressed through pseudonymization patterns (AD-04), preserving the match graph while removing personal identifiers.

### Implementation Readiness Validation ✅
The architecture is fully ready for AI-agent implementation. Key enforcers include:
- **Automated Quality Gates:** The 500-line rule implemented via Checkstyle/ESLint.
- **Clear Project Boundaries:** Feature-based organization combined with Shared UI (Core Components).
- **Strict Naming Conventions:** Unified database, API, and code naming rules.

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION
**Confidence Level:** HIGH

**Key Strengths:**
- Immutable domain model for rule configurations ensuring data integrity.
- Isolated verification pipeline protecting analytics from unverified data.
- Standardized styling via ch-prefixed SCSS and Shared UI components.

### Implementation Handoff

**AI Agent Guidelines:**
- Adhere to the **500-line split rule** for all classes and files.
- Group all new functionality within the **feature-based directory structure**.
- Ensure all custom styles use the **ch- prefix**.
- Maintain **immutability** for RuleConfiguration and published Match entities.

**First Implementation Priority:**
Project initialization using Spring Boot 4.0 (Java 21) and Vite 8 (Vue 3 + Pinia + SCSS).
