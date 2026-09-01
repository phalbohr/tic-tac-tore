---
stepsCompleted: ['step-01-preflight-and-context', 'step-02-generation-mode', 'step-03-test-strategy', 'step-04-generate-tests', 'step-04c-aggregate', 'step-05-validate-and-complete']
lastStep: 'step-05-validate-and-complete'
lastSaved: '2026-09-01T17:18:00+02:00'
workflowType: 'testarch-atdd'
storyId: '8.3'
storyKey: '8-3-automated-bracket-generation-and-seeding'
storyFile: '_bmad-output/implementation-artifacts/8-3-automated-bracket-generation-and-seeding.md'
atddChecklistPath: '_bmad-output/test-artifacts/atdd-checklist-8-3-automated-bracket-generation-and-seeding.md'
generatedTestFiles:
  - '_bmad-output/test-artifacts/atdd-redphase-8-3/TournamentBracketControllerATDDTest.java'
  - 'frontend/e2e/tests/api/tournament-bracket.spec.ts'
  - 'frontend/e2e/tournament-bracket.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-3/tournamentBracketStore.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-3/TournamentBracket.spec.ts'
  - '_bmad-output/test-artifacts/atdd-redphase-8-3/TournamentMatchCard.spec.ts'
inputDocuments:
  - '_bmad/tea/config.yaml'
  - '_bmad-output/implementation-artifacts/8-3-automated-bracket-generation-and-seeding.md'
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

# Acceptance Test-Driven Development (ATDD) Checklist: Story 8.3

## Story Context
- **Story Key:** `8-3-automated-bracket-generation-and-seeding`
- **Story ID:** `8.3`
- **Title:** Story 8.3: Automated Bracket Generation & Seeding
- **Stack Type:** `fullstack` (Spring Boot Java backend + Vue.js TypeScript frontend with Vitest & Playwright)
- **Story File:** `_bmad-output/implementation-artifacts/8-3-automated-bracket-generation-and-seeding.md`

## Acceptance Criteria Summary
1. **AC 1:** Given tournaments in `REGISTRATION_OPEN` where `registrationDeadline <= Instant.now()`, when `TournamentScheduler` runs, then it acquires pessimistic lock, checks participant count, and starts or cancels tournament (`FR41`, `FR43`).
2. **AC 2:** Given fewer than `minParticipants`, when start routine runs, status transitions to `CANCELLED`, `TournamentCancelledEvent` is emitted, and push notifications are sent to participants (`FR41`, `FR55`).
3. **AC 3:** Given at least `minParticipants` in 1v1 or 2v2 fixed teams, when seeding executes, participants are ranked (1 to $N$) by win rate and total wins from `MatchRepository` with tie-breaking rules (`FR43`).
4. **AC 4:** Given seeded list for `CUP` (Single Elimination), `CupBracketGenerator` generates binary tree of size $P = 2^{\lceil \log_2 N \rceil}$ with `next_match_id` links and auto-advancing `BYE` matches (`FR43`).
5. **AC 5:** Given seeded list for `CHAMPIONSHIP` (Round Robin), `ChampionshipBracketGenerator` generates all round pairings via Berger polygon algorithm (Round 1 `READY`, subsequent `PENDING`) (`FR41`, `FR43`).
6. **AC 6:** When tournament start commits, status becomes `IN_PROGRESS`, `TournamentStartedEvent` is emitted, and push notifications notify players (`FR43`, `FR55`).
7. **AC 7:** Authenticated `GET /api/v1/tournaments/{tournamentId}/bracket` and `GET .../matches` return `200 OK` with complete bracket/schedule tree, seeds, status, and scores (`FR43`, `FR46`).
8. **AC 8:** Frontend `TournamentBracket.vue` (Cup) or `TournamentSchedule.vue` (Championship) renders interactive bracket in Clubhouse styling (`bg-surface-container-low`, no 1px solid borders per `UX-DR3`).

## Generation Mode
- **Selected Mode:** AI Generation Mode
- **Rationale:** Clear acceptance criteria, deterministic domain logic (strength calculation, binary bracket placement, Berger polygon algorithm), well-structured REST endpoints, and consistent fullstack conventions.

## Test Strategy & Prioritization Matrix

### Acceptance Criteria Mapping

| AC # | Acceptance Criterion | Test Level | Target Area | Priority | Scenarios |
|---|---|---|---|---|---|
| **AC 1** | Scheduler & Pessimistic Lock | Integration & Unit | `TournamentScheduler`, `TournamentRepository` | P0 | 1. Scheduler picks deadline-expired open tournaments<br>2. Pessimistic lock prevents concurrent double execution |
| **AC 2** | Low Capacity Cancellation | API / Service & E2E | `TournamentLifecycleService`, `tournament-bracket.spec.ts` | P1 | 1. Cancellation when confirmed < minParticipants<br>2. TournamentCancelledEvent and push notifications emitted |
| **AC 3** | Strength-based Seeding (1v1 & 2v2) | Unit / Service | `StrengthBasedSeedingStrategyTest.java` | P0 | 1. Win rate + total wins ranking<br>2. Team average ranking for 2v2<br>3. Tie-breaking by createdAt and ID |
| **AC 4** | Cup Binary Elimination Bracket & BYEs | Unit, API & E2E | `CupBracketGeneratorTest.java`, `tournament-bracket.spec.ts` | P0 | 1. Power of 2 bracket size calculation<br>2. Standard binary pairing (1 vs N, etc.)<br>3. BYE assignment and Round 2 auto-advance |
| **AC 5** | Championship Berger Round Robin | Unit, API & E2E | `ChampionshipBracketGeneratorTest.java`, `tournament-bracket.spec.ts` | P0 | 1. Berger circle pairings for all rounds<br>2. Round 1 READY, Round 2+ PENDING |
| **AC 6** | Start Transition & Push Notifications | Service & Listener | `TournamentLifecycleServiceTest.java`, `TournamentNotificationListenerTest.java` | P0 | 1. IN_PROGRESS status transition<br>2. TournamentStartedEvent dispatched to all participants |
| **AC 7** | REST Endpoints for Bracket & Matches | API / Slice & E2E | `TournamentBracketControllerATDDTest.java`, `tournament-bracket.spec.ts` (API) | P0 | 1. GET /bracket returns full tree with seeds and rounds<br>2. GET /matches returns round-filtered list<br>3. POST /start manual trigger |
| **AC 8** | Interactive UI & Clubhouse Tokens | Component (Vitest) & E2E | `TournamentBracket.spec.ts`, `TournamentMatchCard.spec.ts`, `tournament-bracket.spec.ts` (E2E) | P0 | 1. Multi-round bracket rendering with seeds and badges<br>2. Round robin schedule tabs/accordion<br>3. Clubhouse no 1px solid border styling (`UX-DR3`) |

## TDD Red Phase Status

🔴 **RED Phase Scaffolds Generated:**
- **Backend API Tests:** [`TournamentBracketControllerATDDTest.java`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-3/TournamentBracketControllerATDDTest.java) (API controller slice test covering start, bracket, and matches endpoints)
- **Frontend API Tests:** [`tournament-bracket.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tests/api/tournament-bracket.spec.ts) (6 test scenarios marked with `test.skip()`)
- **Frontend E2E Tests:** [`tournament-bracket.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/frontend/e2e/tournament-bracket.spec.ts) (3 E2E test scenarios marked with `test.skip()`)
- **Frontend Store Tests:** [`tournamentBracketStore.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-3/tournamentBracketStore.spec.ts) (3 Pinia store test scenarios)
- **Frontend Component Tests:**
  - [`TournamentBracket.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-3/TournamentBracket.spec.ts) (3 component test scenarios)
  - [`TournamentMatchCard.spec.ts`](file:///Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/test-artifacts/atdd-redphase-8-3/TournamentMatchCard.spec.ts) (2 component test scenarios)

## Next Steps (Task-by-Task Activation)

During implementation of Story 8.3 in `dev-story`:
1. **Task 1 (Database Migration & JPA Entities):**
   - Create Flyway migration `V20__create_tournament_match_tables.sql`.
   - Implement `TournamentMatchStatus` enum and `TournamentMatch` entity (`@Version private Long version;`).
   - Update `TournamentRegistration` entity with `seed` and `strengthScore`.
   - Implement `TournamentMatchRepository` and `TournamentRepository.findByIdWithLock`.
2. **Task 2 (Seeding Strategy, Bracket Generators & Lifecycle Service):**
   - Implement `TournamentSeedingStrategy`, `StrengthBasedSeedingStrategy`, `RandomSeedingStrategy`.
   - Implement `CupBracketGenerator` (binary bracket + BYE handling) and `ChampionshipBracketGenerator` (Berger round robin).
   - Implement `TournamentLifecycleService` and `TournamentScheduler`.
   - Verify unit tests (`StrengthBasedSeedingStrategyTest`, `CupBracketGeneratorTest`, `ChampionshipBracketGeneratorTest`, `TournamentLifecycleServiceTest`).
3. **Task 3 (Domain Events, Push Notifications, Controller & DTOs):**
   - Move/activate `TournamentBracketControllerATDDTest.java` into `src/test/java/com/tictactore/controller/TournamentBracketControllerTest.java`.
   - Implement `TournamentStartedEvent`, `TournamentCancelledEvent`, `TournamentNotificationListener`, DTOs, and `TournamentController` endpoints.
   - Verify backend controller tests turn GREEN.
4. **Task 4 (Frontend Types, Service, Store, Components & i18n):**
   - Move/activate `tournamentBracketStore.spec.ts` into `frontend/src/features/tournament/stores/__tests__/`.
   - Move/activate component tests into `frontend/src/features/tournament/components/__tests__/`.
   - Implement `tournamentBracketService.ts`, `TournamentBracket.vue`, `TournamentSchedule.vue`, `TournamentMatchCard.vue`, and update `TournamentsView.vue`.
   - Verify store and component tests turn GREEN.
5. **Task 5 (Testing & Quality Verification):**
   - Unskip `frontend/e2e/tests/api/tournament-bracket.spec.ts` and `frontend/e2e/tournament-bracket.spec.ts` (remove `test.skip()`).
   - Run end-to-end and slice tests, then execute `./scripts/ci-local.sh`.
