---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-09-03T17:18:00+02:00'
workflowType: 'testarch-atdd'
storyId: '8.7'
storyKey: '8-7-tournament-standings-and-archive'
storyFile: '_bmad-output/implementation-artifacts/8-7-tournament-standings-and-archive.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-8-7-tournament-standings-and-archive.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentStandingResponseTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentCompletedEventTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentArchiveRepositoryTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentStandingsServiceTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentMatchServiceCompletionTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentStandingsControllerTest.java'
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentStandings.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/tournamentStore.archive.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentsView.archive.spec.ts'
  - 'frontend/e2e/tests/api/tournament-standings-archive.spec.ts'
  - 'frontend/e2e/tournament-standings-archive.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/8-7-tournament-standings-and-archive.md'
  - '.agent/skills/bmad-testarch-atdd/resources/tea-index.csv'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/data-factories.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/component-tdd.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-quality.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-healing-patterns.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/selector-resilience.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/timing-debugging.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-levels-framework.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/test-priorities-matrix.md'
  - '.agent/skills/bmad-testarch-atdd/resources/knowledge/ci-burn-in.md'
---

# Acceptance Test-Driven Development (ATDD) Checklist: Story 8.7

## Story Context
- **Story Key:** `8-7-tournament-standings-and-archive`
- **Story ID:** `8.7`
- **Title:** Story 8.7: Tournament Standings & Archive
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/8-7-tournament-standings-and-archive.md`

## Acceptance Criteria Summary
1. **AC 1 (Dynamic Standings Endpoint):** `GET /api/v1/tournaments/{tournamentId}/standings` returns `200 OK` with structured list of `TournamentStandingResponse` sorted by ranking criteria, calculated dynamically from all confirmed tournament matches (`FR26`, `FR46`).
2. **AC 2 (Championship & Random Pairing Scoring & Multi-Tier Tie-Breakers):** 3 points for win, 0 for loss. Accumulates `matchesPlayed`, `wins`, `losses`, `gamesWon`, `gamesLost`, `gameDifference`. Tie-breaking: (1) points DESC, (2) wins DESC, (3) gameDifference DESC, (4) matchesPlayed ASC, (5) nickname ASC. 2v2 random pairing stub substitute matches do not increment stats/points for stub player (`FR33`, `FR47`).
3. **AC 3 (Knockout Cup Standings & Elimination Tracking):** Knocked-out participants marked `isEliminated = true` and ranked by deepest round reached and match wins (`FR26`).
4. **AC 4 (Automated Tournament Completion & Event Publishing):** When final match completes, `Tournament.status` transitions from `IN_PROGRESS` to `COMPLETED`, `TournamentCompletedEvent` is published, and tournament becomes immutable in archive (`FR46`).
5. **AC 5 (Paginated Historical Archive Endpoint):** `GET /api/v1/tournaments?status=COMPLETED` returns `200 OK` with `Page<TournamentResponse>` sorted by `updatedAt` / `createdAt` DESC (`FR46`).
6. **AC 6 (Frontend Tournaments Archive Tab):** Dedicated "Archive" tab displays historical tournaments with COMPLETED badge, completion date, and participant count. Clicking card opens bracket/modal with standings view (`FR46`).
7. **AC 7 (TournamentStandings.vue Component):** Clubhouse-styled table with Rank, player avatar/nickname, P, W, L, Diff, Pts, and Winner/Active/Eliminated status badges (`UX-DR3`).
8. **AC 8 (GDPR Anonymization):** Deleted users displayed as "Anonymous" with default avatar without breaking table layout or crashing calculations (`FR33`).

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, well-defined backend contracts, Vue components, and architectural patterns aligned with `code-1-guide` and `code-2-test`.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Standings API Endpoint | WebMvc & ATDD API | `TournamentStandingsControllerTest.java`, `tournament-standings-archive.spec.ts` | P0 | 1. Querying standings returns 200 with sorted array<br>2. Contains rank, nickname, points, game difference |
| **AC 2** | Round Robin / 2v2 Scoring & Tie-Breakers | Unit (JUnit 5) | `TournamentStandingsServiceTest.java` | P0 | 1. Win awards 3 pts, loss 0 pts<br>2. Game counts and game diff calculated<br>3. Tie-breaking multi-tier ordering<br>4. Stub substitute stats excluded for stub player |
| **AC 3** | Cup Knockout Standings & Elimination | Unit (JUnit 5) | `TournamentStandingsServiceTest.java` | P1 | 1. Defeated players flagged `isEliminated = true`<br>2. Ranked by round reached and wins |
| **AC 4** | Tournament Completion & Event Publishing | Unit / Service | `TournamentMatchServiceCompletionTest.java`, `TournamentCompletedEventTest.java` | P0 | 1. Cup final match completion transitions status to `COMPLETED`<br>2. Round Robin last match completion transitions status to `COMPLETED`<br>3. `TournamentCompletedEvent` published with winner ID<br>4. Non-final matches do not complete tournament |
| **AC 5** | Paginated Archive Endpoint & Repository | Integration / Repo | `TournamentArchiveRepositoryTest.java`, `tournament-standings-archive.spec.ts` | P1 | 1. `findByStatus(COMPLETED, Pageable)` returns page of completed tournaments<br>2. Ordered by createdAt DESC |
| **AC 6** | Frontend Archive Tab & Navigation | Component (Vitest) & E2E | `TournamentsView.archive.spec.ts`, `tournament-standings-archive.spec.ts` | P0 | 1. Tab switching between Active and Archive<br>2. Displays completed cards with date and badge<br>3. Opens modal with standings tab |
| **AC 7** | Frontend Standings Table Component | Component (Vitest) | `TournamentStandings.spec.ts` | P0 | 1. Standings table with ranks, stats, points<br>2. Winner trophy badge on rank 1 for completed tournaments<br>3. Eliminated badge on knocked-out participants<br>4. Clubhouse token styling (`bg-surface-container-low`, `rounded-2xl`) |
| **AC 8** | GDPR Anonymization | Unit & Component | `TournamentStandingsServiceTest.java`, `TournamentStandings.spec.ts` | P1 | 1. Deleted user rendered as "Anonymous"<br>2. Default placeholder avatar |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend DTO Record Tests:** [`TournamentStandingResponseTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentStandingResponseTest.java)
- **Backend Event Record Tests:** [`TournamentCompletedEventTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentCompletedEventTest.java)
- **Backend Repository Archive Tests:** [`TournamentArchiveRepositoryTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentArchiveRepositoryTest.java)
- **Backend Standings Service Tests:** [`TournamentStandingsServiceTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentStandingsServiceTest.java)
- **Backend Match Completion Tests:** [`TournamentMatchServiceCompletionTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentMatchServiceCompletionTest.java)
- **Backend Controller Tests:** [`TournamentStandingsControllerTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentStandingsControllerTest.java)
- **Frontend Standings Component Tests (Vitest):** [`TournamentStandings.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentStandings.spec.ts)
- **Frontend Pinia Store Tests (Vitest):** [`tournamentStore.archive.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/tournamentStore.archive.spec.ts)
- **Frontend View Archive Tests (Vitest):** [`TournamentsView.archive.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-7/TournamentsView.archive.spec.ts)
- **Frontend API Tests (Playwright):** [`frontend/e2e/tests/api/tournament-standings-archive.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/api/tournament-standings-archive.spec.ts) (marked with `test.skip()`)
- **Frontend E2E Tests (Playwright):** [`frontend/e2e/tournament-standings-archive.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tournament-standings-archive.spec.ts) (marked with `test.skip()`)

## Next Steps (Task-by-Task Activation)

During implementation of Story 8.7 in `bmad-dev-story`:
1. **Task 1: Database Migration & Repository Enhancements (AC4, AC5)**
   - Create Flyway migration `V22__add_tournament_archive_indexes.sql`.
   - Update `TournamentRepository.java` (`findByStatus`, `findAllByOrderByCreatedAtDesc` with `Pageable`).
   - Move and activate `TournamentArchiveRepositoryTest.java`.
2. **Task 2: Backend Standings Service & Scoring Calculation (AC1, AC2, AC3, AC8)**
   - Update `TournamentStandingResponse.java` record.
   - Enhance `TournamentStandingsServiceImpl.java` (game counts, tie-breakers, stub exclusions, GDPR fallback, rank assignment).
   - Move and activate `TournamentStandingResponseTest.java` and `TournamentStandingsServiceTest.java`.
3. **Task 3: Automated Tournament Completion & Event Publishing (AC4)**
   - Create `TournamentCompletedEvent.java` record.
   - Update `TournamentMatchServiceImpl.java` to detect completion, set `TournamentStatus.COMPLETED`, and publish event.
   - Move and activate `TournamentCompletedEventTest.java` and `TournamentMatchServiceCompletionTest.java`.
4. **Task 4: Backend Controller Endpoints & OpenAPI Documentation (AC1, AC5)**
   - Add `GET /{id}/standings` and update `GET /` with pagination and status filtering in `TournamentController.java`.
   - Move and activate `TournamentStandingsControllerTest.java`.
5. **Task 5: Frontend Types, API Service & Pinia Store (AC1, AC5, AC6)**
   - Update `types/tournament.ts`, `tournamentService.ts`, and `tournamentStore.ts`.
   - Move and activate `tournamentStore.archive.spec.ts`.
6. **Task 6: Frontend Standings Component & Modal Integration (AC5, AC7, AC8)**
   - Create `TournamentStandings.vue` with Clubhouse design tokens.
   - Move and activate `TournamentStandings.spec.ts`.
7. **Task 7: Frontend Archive Tab & Navigation in TournamentsView (AC6, AC7)**
   - Update `TournamentsView.vue` (Archive tab, pagination, standings modal tab).
   - Add translation keys to `en.json` and `de.json`.
   - Move and activate `TournamentsView.archive.spec.ts`.
   - Unskip and verify Playwright API & E2E tests (`tournament-standings-archive.spec.ts`).
8. **Task 8: Verification & Full CI Run**
   - Execute `./scripts/ci-local.sh` and ensure 100% pass rate.
