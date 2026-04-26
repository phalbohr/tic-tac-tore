# Development Guide

> Auto-generated: 2026-03-31 | Source: Deep Scan

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 21 | Required for backend |
| Node.js | ^20.19.0 or ≥22.12.0 | Required for frontend |
| npm | (bundled with Node) | Frontend package manager |
| Maven | (wrapper included) | `./mvnw` — no global install needed |

---

## Quick Start

### 1. Backend

```bash
# From project root
./mvnw spring-boot:run
```

Backend starts on **http://localhost:8080**.

H2 Console: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

Swagger UI: **http://localhost:8080/swagger-ui.html**

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on **http://localhost:3000**.

### 3. Development Login

Since Google OAuth2 requires credentials, use the dev seed endpoint:

1. Start backend
2. Open frontend at `http://localhost:3000`
3. Click "Pavel" or "Account B" buttons in the navigation bar
4. These call `POST /api/v1/dev/seed` to create test users and generate JWT tokens

---

## Environment Variables

### Backend

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `TTT_GOOGLE_CLIENT_ID` | For OAuth | — | Google OAuth2 client ID |
| `TTT_GOOGLE_CLIENT_SECRET` | For OAuth | — | Google OAuth2 client secret |
| `TTT_JWT_SECRET` | No | Built-in key | JWT signing secret (HS256) |
| `TTT_OAUTH2_REDIRECT_URI` | No | `http://localhost:3000/oauth2/redirect` | Post-OAuth redirect |

### Frontend

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `VITE_API_BASE_URL` | No | `http://localhost:8080/api/v1` | Backend API base URL |

---

## Build Commands

### Backend

```bash
# Compile
./mvnw clean compile

# Package (JAR)
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Test coverage report
./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html
```

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Dev server (hot reload, port 3000)
npm run dev

# Production build (type-check + vite build)
npm run build

# Preview production build
npm run preview

# Unit tests (Vitest)
npm run test:unit

# E2E tests (Playwright)
npm run test:e2e

# Lint (ESLint + Oxlint, with auto-fix)
npm run lint

# Format (Prettier)
npm run format
```

---

## Testing

### Backend Tests (19 files)

| Category | Suffix | Count | Description |
|----------|--------|-------|-------------|
| Integration | `*IT.java` | 7 | Full Spring context + H2 + MockMvc |
| Unit | `*Test.java` | 9 | Mocked dependencies |
| Repository | `*Test.java` | 2 | JPA queries with test data |
| Security | `*IT.java` / `*Test.java` | 3 | OAuth2 + JWT + security config |

**Run:**
```bash
./mvnw test
```

### Frontend Tests

**Unit Tests (Vitest):**
- 14 spec files across components, views, services, stores, router
- Environment: jsdom

```bash
cd frontend && npm run test:unit
```

**E2E Tests (Playwright):**
- `auth.spec.ts` — Authentication flow
- `vue.spec.ts` — General app tests

```bash
cd frontend && npm run test:e2e
```

---

## Code Conventions

The project follows strict coding conventions documented in `_project-spec/rules/`:

### Backend (Java)
- **`code-guide.md`** — 19 mandatory rules including:
  - `var` for local variables
  - English-only technical text
  - Hybrid Split OpenAPI documentation
  - Three-Layer Transaction Architecture (`@Retryable` / `@Transactional` separation)
  - Tell, Don't Ask (domain logic in entities)
  - Zero Comments Policy on production classes
  - No magic values

- **`openapi-guide.md`** — Interface-driven OpenAPI documentation rules
- **`test-guide.md`** — AAA pattern, `@DisplayName`, no comments in tests, `*IT` suffix for integration tests
- **`review-guide.md`** — Code review checklist (architecture, security, performance, complexity)

### Frontend (TypeScript/Vue)
- Vue 3 Composition API (`<script setup>`)
- TypeScript strict mode
- Tailwind CSS for styling
- ESLint + Oxlint + Prettier

---

## Project Structure

| Directory | Content |
|-----------|---------|
| `src/main/java/com/tictactore/` | Backend Java source (41 files) |
| `src/main/resources/` | Backend configuration |
| `src/test/java/com/tictactore/` | Backend tests (19 files) |
| `frontend/src/` | Frontend Vue/TypeScript source |
| `frontend/e2e/` | Playwright E2E tests |
| `conductor/` | Project management & tracking |
| `_project-spec/rules/` | Code convention guides |
| `docs/` | Generated project documentation |

---

## Deployment

### Current Status

- **No CI/CD pipelines** configured (no `.github/workflows/`)
- **No Docker** configuration (no Dockerfile or docker-compose)
- **No infrastructure as code**

### Production Database

The application is configured for PostgreSQL (Supabase-compatible) in production. The `postgresql` driver is included as a runtime dependency. To connect:

```yaml
# application-prod.yml (to be created)
spring:
  datasource:
    url: jdbc:postgresql://<host>:<port>/<database>
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Ports

| Service | Dev Port | Notes |
|---------|----------|-------|
| Backend | 8080 | Spring Boot default |
| Frontend | 3000 | Vite dev server |
| H2 Console | 8080/h2-console | Dev only |
