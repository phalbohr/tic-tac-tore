---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-09-01T09:45:00+02:00'
storyId: '8.1'
storyKey: '8-1-tournament-creation-and-configuration'
storyFile: '_bmad-output/implementation-artifacts/8-1-tournament-creation-and-configuration.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-8-1-tournament-creation-and-configuration.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-8-1/TournamentControllerATDDTest.java'
  - 'frontend/e2e/tournament-creation.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-1/useTournamentStore.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-1/CreateTournamentModal.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/8-1-tournament-creation-and-configuration.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/overview.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/api-request.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 8.1

## Story Context
- **Story Key:** `8-1-tournament-creation-and-configuration`
- **Story ID:** `8.1`
- **Title:** Story 8.1: Tournament Creation & Configuration
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/8-1-tournament-creation-and-configuration.md`

## Acceptance Criteria Summary
1. **AC 1:** Given an authenticated user navigating to Tournaments (`/tournaments`) or Home Hub, when they tap "Create Tournament", then `CreateTournamentModal.vue` opens with fields for Name, Format (`CUP` / `CHAMPIONSHIP`), Mode (`ONE_VS_ONE_PERSONAL`, `TWO_VS_TWO_FIXED_TEAMS`, `TWO_VS_TWO_RANDOM_PAIRINGS`), Rule System dropdown, Min/Max Participants, Registration Deadline (future date), Round Count (championship), and Playoff Option toggle (`FR41`).
2. **AC 2:** Given an authenticated user submitting a valid tournament form via `POST /api/v1/tournaments`, then the backend persists `Tournament` with status `REGISTRATION_OPEN`, creator link, rule configuration link, and returns `201 Created` with `TournamentResponse` (`FR41`).
3. **AC 3:** Given format and mode selection, user can choose Single Elimination Cup (`CUP`) or Round Robin Championship (`CHAMPIONSHIP`), and 1v1 Personal (`ONE_VS_ONE_PERSONAL`), 2v2 Fixed Teams (`TWO_VS_TWO_FIXED_TEAMS`), or 2v2 Random Pairings (`TWO_VS_TWO_RANDOM_PAIRINGS`).
4. **AC 4:** Given invalid parameters (blank/short/long name, past deadline, minParticipants < 2, maxParticipants < minParticipants, 2v2 mode with minParticipants < 4, non-existent ruleConfigId, unauthenticated), backend rejects with `400 Bad Request` / `401 Unauthorized` / `404 Not Found`.
5. **AC 5:** Given `GET /api/v1/tournaments` (with optional `status` filter) or `GET /api/v1/tournaments/{id}`, backend returns `200 OK` with list or single tournament details (`FR41`, `FR46`).
6. **AC 6:** Given successful form submission in frontend, modal closes, success toast notification appears, `useTournamentStore` updates, and view refreshes tournaments list (`/tournaments`).

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, well-structured REST endpoints, defined database constraints, and consistent fullstack patterns in the repository.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Open Create Tournament Modal from UI & Home Hub CTA | E2E (Playwright) / Component | `CreateTournamentModal.vue` & `tournament-creation.spec.ts` | P0 | 1. Modal opens from `/tournaments` action button<br>2. Home Hub CTA navigates to `/tournaments`<br>3. Render form fields and smart defaults |
| **AC 2** | Persist Tournament & Return 201 Created | API / Slice | `TournamentController` & `TournamentService` | P0 | 1. `POST /api/v1/tournaments` creates Cup tournament<br>2. `POST /api/v1/tournaments` creates Championship with round count & playoff |
| **AC 3** | Format & Mode options handling | Component (Vitest) & API | `CreateTournamentModal.spec.ts` & `TournamentControllerATDDTest.java` | P1 | 1. Cup vs Championship format options<br>2. 1v1, 2v2 Fixed, 2v2 Random pairing modes<br>3. Dynamic display of round count for championship |
| **AC 4** | Parameter validation & Error rejection | API / Integration & E2E | `TournamentController` & `tournament-creation.spec.ts` | P0 | 1. Blank/invalid name -> 400 Bad Request<br>2. Past registration deadline -> 400 Bad Request<br>3. 2v2 with minParticipants < 4 -> 400 Bad Request<br>4. Non-existent ruleConfigId -> 404 Not Found |
| **AC 5** | Query tournaments list & single tournament | API / Integration & E2E | `TournamentController` & `tournament-creation.spec.ts` | P1 | 1. `GET /api/v1/tournaments` returns all tournaments<br>2. `GET /api/v1/tournaments?status=REGISTRATION_OPEN` filters by status<br>3. `GET /api/v1/tournaments/{id}` returns details or 404 |
| **AC 6** | Frontend Store update & toast notification | Unit (Vitest) & E2E | `useTournamentStore.spec.ts` & `tournament-creation.spec.ts` | P0 | 1. `createTournament` prepends to store and sets current<br>2. Modal closes and success toast appears in UI |
| **UX** | Clubhouse No-Line styling compliance (`UX-DR3`) | E2E (Playwright) | `frontend/e2e/tournament-creation.spec.ts` | P2 | 1. Modal container uses tonal elevation with 0px solid border |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend API Tests:** [`TournamentControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-1/TournamentControllerATDDTest.java) (7 test scenarios covering Cup/Championship creation, validations, filtering, not found)
- **Frontend E2E Tests:** [`tournament-creation.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tournament-creation.spec.ts) (5 test scenarios marked with `test.skip()`)
- **Frontend Store Tests:** [`useTournamentStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-1/useTournamentStore.spec.ts) (4 store test scenarios)
- **Frontend Component Tests:** [`CreateTournamentModal.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-1/CreateTournamentModal.spec.ts) (5 component test scenarios)
- **Fixtures:** [`tournament-data.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/fixtures/tournament-data.ts)

## Next Steps (Task-by-Task Activation)

During implementation of Story 8.1 in `dev-story`:
1. **Task 1 (Database Migration & Domain Entities):**
   - Create Flyway migration `V18__create_tournament_tables.sql`.
   - Implement `TournamentFormat`, `TournamentMode`, `TournamentStatus`, and `Tournament` entity with `@Version`.
   - Create `TournamentRepository` and repository tests.
2. **Task 2 (Backend DTOs, Service & Controller):**
   - Move/activate `TournamentControllerATDDTest.java` into `src/test/java/com/tictactore/controller/TournamentControllerATDDTest.java`.
   - Implement `CreateTournamentRequest`, `TournamentResponse`, `TournamentService`, `TournamentServiceImpl`, and `TournamentController`.
   - Verify unit & ATDD controller tests turn GREEN.
3. **Task 3 (Frontend Types, Service, Store & i18n):**
   - Move/activate `useTournamentStore.spec.ts` into `frontend/src/features/tournament/stores/__tests__/tournamentStore.spec.ts`.
   - Implement types `tournament.ts`, service `tournamentService.ts`, store `tournamentStore.ts`, and i18n keys in `en.json` and `de.json`.
   - Verify store tests turn GREEN.
4. **Task 4 (Frontend UI Components & Home Hub Integration):**
   - Move/activate `CreateTournamentModal.spec.ts` into `frontend/src/features/tournament/components/__tests__/CreateTournamentModal.spec.ts`.
   - Implement `CreateTournamentModal.vue`, `TournamentsView.vue`, register route in `router/index.ts`, and add navigation CTA in `HomeView.vue`.
   - Verify component tests turn GREEN.
5. **Task 5 (Testing & Quality Verification):**
   - Unskip `frontend/e2e/tournament-creation.spec.ts` (remove `test.skip()`).
   - Run end-to-end and slice tests, then execute `./scripts/ci-local.sh`.
