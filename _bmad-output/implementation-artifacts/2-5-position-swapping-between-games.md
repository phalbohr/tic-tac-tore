---
baseline_commit: HEAD
---

# Story 2.5: Position Swapping Between Games

## 📖 Story Foundation
**User Story:** As a 2v2 participant, I want to indicate positions, So that stats remain accurate.
**Epic:** Epic 2: Retrospective Match Entry & Rule Systems
**Status:** ready-for-dev

**Acceptance Criteria:**
- **Given** a 2v2 match is being recorded
- **When** new game starts
- **Then** prompt to confirm/swap Attacker/Defender
- **And** data persisted for each game
- **And** the match submission payload (Story 2.4) correctly includes positional data

---

## 🎯 Developer Context & Guardrails

### 1. Goal & Sequencing
- **Goal:** Implement the ability for players to specify and swap their positions (Attacker/Defender) between games in a 2v2 match.
- **Trigger:** Tapping to start a new game in the UI, or modifying positions in the drafting flow before submission.
- **Context:** Story 2.4 implemented the match submission payload. Story 2.5 expands the payload to include positional data, which is critical for accurate player-level statistics (Epic 4).

### 2. Architecture & Data Integrity Guardrails (Backend `code-1-guide` Compliance)
- **Three-Layer Transaction Architecture (Rule 3):** Strict separation of retry and transaction boundaries:
  - **Outer Service (`MatchServiceImpl`):** Annotated with `@Retryable` ONLY. Orchestrates validation and calls the inner operation. NEVER combine `@Retryable` and `@Transactional`.
  - **Inner Operation (`MatchOperation`):** Annotated with `@Idempotent` + `@Transactional`. Handles atomic database saves so each retry opens a clean transaction.
- **Optimistic Locking (Rule 2):** Mutable entities (`Match`, `Game`) MUST declare `@Version Long version;` (or `Integer` wrapper). Do NOT use primitive `long` and do NOT map the column explicitly (`@Column` is forbidden).
- **Tell, Don't Ask & JPA Best Practices (Rules 5, 9):** Domain rules and status transitions belong inside `Match.java`. Always capture and return the instance returned by `repository.save()`.
- **Strict DTO Boundary & Layering (Rules 7, 11, 12):**
  - Never pass raw entities across REST boundaries. Use updated `CreateMatchRequest`, `MatchResponse`, and `GameDto` in `src/main/java/com/tictactore/dto/`.
  - Service throws plain domain exceptions (`InvalidPositionException`, `DuplicatePositionException`) without HTTP annotations. `GlobalExceptionHandler` maps them to clean 400 JSON responses.

### 3. Frontend Implementation Guardrails (Vue 3 + Pinia + TEA Framework)
- **State Management:** Extend the Pinia match store (`useMatchStore`) to track positions for each player in each game of a 2v2 match. Ensure changes trigger UI updates.
- **Component Design (`5-style` Compliance):**
  - Use `<script setup>` syntax exclusively.
  - Apply `ch-` prefix for custom UI styles (e.g., `ch-position-swap`).
  - Follow the 500-line rule for any new classes or components.
  - Components with single-word filenames must have multi-word names declared (e.g., via `defineOptions({ name: 'PositionSwapDialog' })`) to satisfy ESLint rule.
- **UI Verification Requirement:** Use `frontend_verification_instructions` tool to visually verify positional UI elements (e.g., attacker/defender icons, swap buttons) using Playwright.
- **E2E Testing (TEA Architecture):**
  - Use `page.getByRole()` instead of brittle CSS selectors for Playwright tests.
  - E2E scripts belong in `frontend/e2e/`.
  - Do not assert directly against Pinia internal state; assert against UI reflections (e.g., "Attacker" label rendering in the correct player slot).

### 4. Code Standards & Testing (`5-style` & `6-test`)
- **Zero Comments Policy:** No inline, block, or Javadoc comments. Extract complex logic into named variables/methods instead.
- **Var Usage:** Always use `var` for local variables in Java.
- **Magic Values:** Extract string literals or numeric constants to `private static final` constants.
- **Test Structure (AAA Format):**
  - Tests MUST be structured with Arrange-Act-Assert blocks separated by a SINGLE blank line and ZERO comments.
  - Example `test-guide`: Real dependencies mock only external systems; integration tests mock only out-of-process resources. Use dedicated E2E test endpoints (e.g., `/api/e2e/matches`) guarded by `@Profile("test")` / `@Profile("e2e")` and `env.acceptsProfiles()`.

---

## 🛠️ Project Structure Notes

- Update `frontend/src/stores/matchStore.ts` (or similar) to handle positions.
- Update `backend/src/main/java/com/tictactore/dto/GameDto.java` and relevant Request classes to accept `Attacker`/`Defender` values for 2v2 matches.
- Backend entities: Update `Game` and `Match` models. Ensure positional constraints are enforced (e.g., exactly 1 attacker and 1 defender per team in a 2v2 game).

---

## ✅ Dev Agent Record

### File List
- `_bmad-output/implementation-artifacts/2-5-position-swapping-between-games.md`
